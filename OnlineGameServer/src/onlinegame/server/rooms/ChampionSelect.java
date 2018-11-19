package onlinegame.server.rooms;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import onlinegame.server.MessageBuilder;
import onlinegame.server.account.Session;
import onlinegame.server.game.PlayerInfo;
import onlinegame.shared.Logger;
import onlinegame.shared.db.ChampionDB;
import onlinegame.shared.db.ChampionDB.ChampionData;
import onlinegame.shared.game.GameMap;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class ChampionSelect extends LobbyStage
{
    private final GameMap map;
    
    private final CSPlayer[][] players;
    private final ChatRoom[] chat;
    
    private long timerEnd;
    private boolean finishedPicking = false;
    private boolean hasClosed = false;
    
    ChampionSelect(Lobby lobby, GameMap map)
    {
        super(lobby);
        
        this.map = map;
        
        chat = new ChatRoom[2];
        chat[0] = new ChatRoom("Team Chat", "championselect");
        chat[1] = new ChatRoom("Team Chat", "championselect");
        
        players = new CSPlayer[2][];
        for (byte t = 0; t < 2; t++)
        {
            players[t] = new CSPlayer[lobby.getPlayerCount(t)];
            for (int p = 0; p < players[t].length; p++)
            {
                players[t][p] = new CSPlayer(lobby.getPlayer(t, p), t);
                players[t][p].session.joinChat(chat[t]);
            }
        }
        
        sendToAll(MessageBuilder.champSelectStart(this, 0), 0);
        sendToAll(MessageBuilder.champSelectStart(this, 1), 1);
        
        setTimer(60);
        
        updateAll();
        
        Logger.log("Started champion select!!");
    }
    
    @Override
    public void update()
    {
        if (hasClosed)
        {
            return;
        }
        if (getTimeLeft() <= 0)
        {
            if (!finishedPicking)
            {
                //check if everyone has picked a champion
                if (!hasEveryonePicked())
                {
                    Logger.log("Lobby removed - someone didn't pick a champion.");
                    LobbyManager.removeLobby(lobby);
                    return;
                }

                //force everyone to lock in
                lockAll();
                updateAll();
            }
            else
            {
                //start the game!!
                PlayerInfo[][] playerInfo = new PlayerInfo[2][];
                for (int t = 0; t < 2; t++)
                {
                    playerInfo[t] = new PlayerInfo[players[t].length];
                    for (int p = 0; p < playerInfo[t].length; p++)
                    {
                        CSPlayer player = players[t][p];
                        playerInfo[t][p] = new PlayerInfo(player.session, player.champion, t, p);
                    }
                }

                lobby.startGame(playerInfo, map);
                close();
            }
        }
    }
    
    @Override
    public void readMessage(InputMessage msg, Session sender) throws IOException
    {
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.C_CHAMPSELECT_SELECT:
                CSPlayer player = find(sender);
                if (player != null && player.state == CSPlayer.ST_PICKING)
                {
                    in = msg.getDataStream();
                    player.champion = ChampionDB.get(in.readShort());
                    
                    updateAll(player.team);
                }
                break;
            case Protocol.C_CHAMPSELECT_LOCK:
                player = find(sender);
                if (player != null && player.state == CSPlayer.ST_PICKING && player.champion != null)
                {
                    player.state = CSPlayer.ST_LOCKED;
                    
                    if (isEveryoneLocked())
                    {
                        lockAll();
                    }
                    
                    updateAll();
                }
                break;
        }
    }
    
    private void lockAll()
    {
        if (finishedPicking)
        {
            Logger.logError("already finished picking!!");
            return;
        }
        if (!hasEveryonePicked())
        {
            Logger.logError("not everyone has picked - cannot lock all");
            return;
        }
        finishedPicking = true;
        setTimer(3);
    }
    
    private boolean hasEveryonePicked()
    {
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                if (players[t][p].champion == null)
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isEveryoneLocked()
    {
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                if (players[t][p].state != CSPlayer.ST_LOCKED)
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    private CSPlayer find(Session session)
    {
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                if (session == players[t][p].session)
                {
                    return players[t][p];
                }
            }
        }
        Logger.logError("ChampionSelect: Player \"" + session.getAccount().getUsername() + "\" not found.");
        return null;
    }
    
    private void sendToAll(OutputMessage msg)
    {
        sendToAll(msg, 0);
        sendToAll(msg, 1);
    }
    
    private void sendToAll(OutputMessage msg, int team)
    {
        for (int i = 0; i < players[team].length; i++)
        {
            players[team][i].session.getClient().sendMessage(msg);
        }
    }
    
    private void updateAll()
    {
        updateAll(0);
        updateAll(1);
    }
    
    private void updateAll(int team)
    {
        if (team < 0 || team > 1)
        {
            throw new IllegalArgumentException("asdfjkel");
        }
        
        for (int i = 0; i < players[team].length; i++)
        {
            OutputMessage msg = MessageBuilder.champSelectUpdate(this, players[team][i]);
            players[team][i].session.getClient().sendMessage(msg);
        }
    }
    
    @Override
    public void close()
    {
        chat[0].close();
        chat[1].close();
        hasClosed = true;
    }
    
    public void startMessage(DataOutput out, int team) throws IOException
    {
        out.writeByte((byte)team); //your team
        out.writeByte((byte)players[team].length); //size of your team
        out.writeByte((byte)players[1-team].length); //size of enemy team
        
        writePlayerNames(out, team); //your team
        writePlayerNames(out, 1-team); //enemy team
    }
    
    public void updateMessage(DataOutput out, CSPlayer player) throws IOException
    {
        out.writeUTF(getInfoText(player));
        out.writeInt(getTimeLeft());
        
        writePlayerInfo(out, player.team, true);
        writePlayerInfo(out, 1-player.team, false);
    }
    
    private void writePlayerNames(DataOutput out, int team) throws IOException
    {
        for (int i = 0; i < players[team].length; i++)
        {
            out.writeUTF(players[team][i].session.getAccount().getUsername());
        }
    }
    
    private void writePlayerInfo(DataOutput out, int team, boolean showChampion) throws IOException
    {
        for (int i = 0; i < players[team].length; i++)
        {
            CSPlayer player = players[team][i];
            out.writeByte(player.state);
            out.writeShort(player.champion == null || !showChampion ? -1 : player.champion.id);
        }
    }
    
    private String getInfoText(CSPlayer player)
    {
        if (finishedPicking)
        {
            return "The game is starting soon...";
        }
        
        switch (player.state)
        {
            case CSPlayer.ST_WAITING:
                return "Waiting...";
            case CSPlayer.ST_PICKING:
                if (player.champion == null)
                {
                    return "It's your turn to pick!";
                }
                else
                {
                    return "Lock in your choice.";
                }
            case CSPlayer.ST_LOCKED:
                return "Waiting for everyone to pick...";
            default:
                return "Invalid State (" + player.state + ")";
        }
    }
    
    private void setTimer(int seconds)
    {
        timerEnd = System.nanoTime() + (long)seconds * 1_000_000_000L;
    }
    
    /**
     * In milliseconds
     */
    private int getTimeLeft()
    {
        long timeNow = System.nanoTime();
        return timerEnd <= timeNow ? 0 : (int)((timerEnd - timeNow) / 1_000_000);
    }
    
    public static final class CSPlayer
    {
        private static final byte
                ST_WAITING = 0,
                ST_PICKING = 1,
                ST_LOCKED = 2;
        
        public final Session session;
        public final byte team;
        private ChampionData champion = null;
        private byte state = ST_PICKING;
        
        private CSPlayer(Session session, byte team)
        {
            this.session = session;
            this.team = team;
        }
    }
}
