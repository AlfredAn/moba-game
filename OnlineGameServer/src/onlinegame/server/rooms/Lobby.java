package onlinegame.server.rooms;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import onlinegame.server.MessageBuilder;
import onlinegame.server.account.Session;
import onlinegame.server.game.GameHandler;
import onlinegame.server.game.PlayerInfo;
import onlinegame.shared.Logger;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.game.GameMap;
import onlinegame.shared.net.OutputMessage;

/**
 *
 * @author Alfred
 */
public final class Lobby
{
    private static final AtomicInteger idCounter = new AtomicInteger();
    
    public final int id;
    
    private final String name, gameMode;
    private final GameMap map;
    private Session owner;
    private final int playersInTeam;
    
    private final List<Session>[] players;
    
    private ChatRoom chat;
    
    private LobbyStage stage = null;
    private boolean isClosed = false;
    
    Lobby(String name, Session owner)
    {
        this(name, owner, 5, "5v5", GameMap.TEST);
    }
    @SuppressWarnings("unchecked")
    private Lobby(String name, Session owner, int playersInTeam, String gameMode, GameMap map)
    {
        id = idCounter.getAndIncrement();
        
        this.name = name;
        this.owner = owner;
        this.playersInTeam = playersInTeam;
        this.gameMode = gameMode;
        this.map = map;
        
        players = (List<Session>[])Array.newInstance(List.class, 2);
        players[0] = new ArrayList<>(playersInTeam);
        players[1] = new ArrayList<>(playersInTeam);
        
        chat = new ChatRoom("Lobby Chat", "lobby");
    }
    
    public void update()
    {
        if (stage != null)
        {
            stage.update();
        }
    }
    
    private boolean checkClosed()
    {
        if (isClosed)
        {
            Logger.logError("Lobby is closed!!");
            return true;
        }
        return false;
    }
    
    public boolean addPlayer(Session session)
    {
        if (checkClosed()) return false;
        if (stage != null)
        {
            Logger.logError("Cannot add player: wrong lobby stage!");
            return false;
        }
        
        if (isFull())
        {
            return false;
        }
        
        int team = (getPlayerCount(0) > getPlayerCount(1)) ? 1 : 0;
        players[team].add(session);
        
        if (session.getLobby() != null)
        {
            session.getLobby().removePlayer(session);
        }
        
        OutputMessage msg = MessageBuilder.lobbyJoin(true, "");
        session.getClient().sendMessage(msg);
        
        sendUpdate();
        
        session.joinChat(chat);
        
        LobbyManager.notifyChange();
        
        return true;
    }
    
    public void removePlayer(Session session)
    {
        if (checkClosed()) return;
        if (stage instanceof ChampionSelect)
        {
            Logger.log("Lobby removed because " + session.getAccount().getUsername() + " left it.");
            LobbyManager.removeLobby(this);
        }
        
        boolean removed = false;
        outer: for (int t = 0; t < 2; t++)
        {
            for (int i = 0; i < players[t].size(); i++)
            {
                Session s = players[t].get(i);
                if (s == session)
                {
                    players[t].remove(i);
                    removed = true;
                    break outer;
                }
            }
        }
        
        if (!removed)
        {
            return;
        }
        
        OutputMessage msg = MessageBuilder.lobbyLeave();
        session.getClient().sendMessage(msg);
        
        session.leaveChat(chat);
        
        if (session == owner && getPlayerCount() > 0)
        {
            //choose new owner
            setOwner(getRandomPlayer());
        }
        
        sendUpdate();
        
        LobbyManager.notifyChange();
        
        if (getPlayerCount() == 0)
        {
            LobbyManager.removeLobby(this);
        }
    }
    
    public void switchTeam(Session session)
    {
        if (checkClosed()) return;
        if (stage != null)
        {
            Logger.logError("Cannot switch team: wrong lobby stage!");
            return;
        }
        
        int team = getTeam(session);
        
        if (team == -1)
        {
            return;
        }
        
        if (getPlayerCount(1 - team) == playersInTeam)
        {
            return;
        }
        
        players[team].remove(session);
        players[1 - team].add(session);
        
        sendUpdate();
    }
    
    public void startChampSelect(Session caller)
    {
        if (checkClosed()) return;
        if (stage != null)
        {
            Logger.logError("Cannot start champion select: wrong lobby stage!");
            return;
        }
        
        if (caller != owner)
        {
            Logger.logError("Only the owner can start the game.");
            return;
        }
        
        if (getPlayerCount() > playersInTeam * 2 || getPlayerCount(0) > playersInTeam || getPlayerCount(1) > playersInTeam)
        {
            throw new RuntimeException("Internal error in Lobby class.");
        }
        
        chat.close();
        chat = null;
        stage = new ChampionSelect(this, map);
        
        LobbyManager.notifyChange();
    }
    
    void startGame(PlayerInfo[][] players, GameMap map)
    {
        close(true);
        GameHandler.startGame(players, map);
        
        Logger.log("Game started loading!");
    }
    
    public boolean hasPlayer(Session session)
    {
        for (int t = 0; t < 2; t++)
        {
            for (int i = 0; i < players[t].size(); i++)
            {
                Session s = players[t].get(i);
                if (s == session)
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void sendUpdate()
    {
        OutputMessage msg = MessageBuilder.lobbyUpdate(this);
        sendToAll(msg);
    }
    /*private void sendUpdate(Session session)
    {
        OutputMessage msg = MessageBuilder.lobbyUpdate(this);
        session.getClient().sendMessage(msg);
    }*/
    
    private void sendToAll(OutputMessage msg)
    {
        if (checkClosed()) return;
        for (int t = 0; t < 2; t++)
        {
            for (int i = 0; i < players[t].size(); i++)
            {
                players[t].get(i).getClient().sendMessage(msg);
            }
        }
    }
    
    private void setOwner(Session owner)
    {
        if (checkClosed()) return;
        this.owner = owner;
        chat.sendMessage(owner.getAccount().getUsername() + " is now the owner of this lobby.");
        LobbyManager.notifyChange();
    }
    
    void close()
    {
        close(false);
    }
    void close(boolean silent)
    {
        if (isClosed)
        {
            return;
        }
        
        if (!silent)
        {
            OutputMessage msg = MessageBuilder.lobbyLeave();
            sendToAll(msg);
        }
        
        players[0].clear();
        players[1].clear();
        isClosed = true;
        
        if (chat != null)
        {
            chat.close();
        }
        
        if (stage != null)
        {
            stage.close();
        }
    }
    
    public LobbyStage getStage()
    {
        return stage;
    }
    
    public boolean isClosed()
    {
        return isClosed;
    }
    
    public String getName()
    {
        return name;
    }
    
    public Session getOwner()
    {
        return owner;
    }
    
    public int getPlayerCount()
    {
        return players[0].size() + players[1].size();
    }
    
    public int getPlayerCount(int team)
    {
        return players[team].size();
    }
    
    public int getMaxPlayers()
    {
        return playersInTeam * 2;
    }
    
    public Session getPlayer(int team, int i)
    {
        return players[team].get(i);
    }
    
    private Session getRandomPlayer()
    {
        int num = SharedUtil.random.nextInt(getPlayerCount());
        
        if (num < getPlayerCount(0))
        {
            return getPlayer(0, num);
        }
        else
        {
            return getPlayer(1, num - getPlayerCount(0));
        }
    }
    
    public int getTeam(Session player)
    {
        for (int t = 0; t < 2; t++)
        {
            for (int i = 0; i < players[t].size(); i++)
            {
                if (players[t].get(i) == player)
                {
                    return t;
                }
            }
        }
        
        return -1;
    }
    
    public boolean isFull()
    {
        return getPlayerCount() == playersInTeam * 2;
    }
    
    public String getGameMode()
    {
        return gameMode;
    }
    
    public GameMap getMap()
    {
        return map;
    }
}
