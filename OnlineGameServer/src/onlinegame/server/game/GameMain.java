package onlinegame.server.game;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import onlinegame.server.account.Session;
import onlinegame.server.game.engine.SActor;
import onlinegame.server.game.engine.SEntity;
import onlinegame.server.game.engine.SGameState;
import onlinegame.shared.ArrayDequeList;
import onlinegame.shared.FrameTimer;
import onlinegame.shared.GameUtil;
import onlinegame.shared.Logger;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;
import onlinegame.shared.game.GameMap;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;
import onlinegame.shared.net.GameProtocolException;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;
import onlinegame.shared.net.udp.UDPConnection;
import onlinegame.shared.net.udp.UDPServer;

/**
 *
 * @author Alfred
 */
public final class GameMain implements Runnable
{
    private static final UDPServer server;
    static
    {
        try
        {
            Logger.log("Starting UDP server...");
            server = new UDPServer(Protocol.DEFAULT_PORT);
        }
        catch (SocketException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private static final AtomicInteger idCounter = new AtomicInteger();
    
    public final int id;
    private final PlayerInfo[][] players;
    private final GameMap map;
    
    private final Thread thread;
    private volatile boolean shouldStop = false;
    
    GameMain(PlayerInfo[][] players, GameMap map)
    {
        id = idCounter.getAndIncrement();
        
        if (players.length != 2)
        {
            throw new IllegalArgumentException("invalid array size (must be 2): " + players.length);
        }
        else if (players[0].length == 0 && players[1].length == 0)
        {
            throw new IllegalArgumentException("cannot create game with no players");
        }
        
        this.players = new PlayerInfo[2][];
        this.players[0] = Arrays.copyOf(players[0], players[0].length);
        this.players[1] = Arrays.copyOf(players[1], players[1].length);
        
        this.map = map;
        gameState = new SGameState(map, players);
        
        thread = new Thread(this, "Game " + id);
        thread.setPriority(5);
        thread.setDaemon(true);
    }
    
    private final PlayerConnection[][] conns = new PlayerConnection[2][];
    
    @Override
    public void run()
    {
        Logger.log("game run()");
        
        for (int t = 0; t < 2; t++)
        {
            conns[t] = new PlayerConnection[players[t].length];
            for (int p = 0; p < players[t].length; p++)
            {
                conns[t][p] = new PlayerConnection(players[t][p]);
                setupConnection(t, p);
            }
        }
        
        gameLoop();
    }
    
    private void setupConnection(int t, int p)
    {
        PlayerInfo player = players[t][p];
        InetAddress address = player.getAdress();
        short sessionHash = player.getSessionHash();
        
        if (address == null || sessionHash == 0)
        {
            conns[t][p].con = null;
        }
        else
        {
            Logger.log(address);
            UDPConnection con = server.createConnection(address, sessionHash);
            conns[t][p].con = con;
            Logger.log("setup udp connection!!");
        }
    }
    
    private final SGameState gameState;
    
    private void gameLoop()
    {
        FrameTimer timer = new FrameTimer(GameUtil.SERVER_TICKRATE, FrameTimer.MEDIUM);
        
        while (!shouldStop)
        {
            for (int t = 0; t < 2; t++)
            {
                for (int p = 0; p < players[t].length; p++)
                {
                    PlayerInfo player = players[t][p];
                    Session s = player.account.getSession();
                    PlayerConnection pCon = conns[t][p];
                    UDPConnection udpCon = pCon.con;
                    
                    if (udpCon == null || udpCon.isClosed())
                    {
                        if (s != null && s.getClient().isLoggedIn())
                        {
                            //client has reconnected
                            setupConnection(t, p);
                            Logger.log("Reconnected!");
                        }
                    }
                    else
                    {
                        if (s == null || !s.getClient().isLoggedIn())
                        {
                            //client has disconnected
                            udpCon.close();
                            pCon.con = null;
                            Logger.log("Disconnected!");
                        }
                        else
                        {
                            //client is ready, recieve messages and process it
                            processClient(pCon);
                        }
                    }
                }
            }
            
            if (isEveryoneDisconnected())
            {
                GameHandler.stopGame(this);
            }
            
            gameState.update();
            if (gameState.getCurrentTick() % GameUtil.SERVER_SEND_INTERVAL == 0) sendUpdates();
            
            timer.update();
        }
    }
    
    private final ArrayDequeList<GameState> gameStates = new ArrayDequeList<>();
    private final TIntObjectMap<GameState> gameStateIdMap = new TIntObjectHashMap<>();
    
    private void sendUpdates()
    {
        GameState snapshot = gameState.getSnapshot();
        gameStates.add(snapshot);
        gameStateIdMap.put(snapshot.currentTick, snapshot);
        
        int gsKeepFirst = Integer.MAX_VALUE;
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                int ack = conns[t][p].lastGSAck;
                
                GameState delta;
                if ((gameState.getCurrentTick() % GameUtil.SERVER_SAFE_INTERVAL == 0) || conns[t][p].lastGSSent == null)
                {
                    delta = gameStateIdMap.get(ack);
                }
                else
                {
                    delta = conns[t][p].lastGSSent;
                }
                conns[t][p].lastGSSent = snapshot;
                sendUpdates(t, p, snapshot, delta);
                
                gsKeepFirst = Math.min(gsKeepFirst, ack);
            }
        }
        
        //remove old gameStates
        for (int i = 0; i < gameStates.size(); i++)
        {
            GameState gs = gameStates.get(i);
            if (gs.currentTick < gsKeepFirst || gameStates.size() >= 512)
            {
                gameStates.remove();
                gameStateIdMap.remove(gs.currentTick);
            }
            else
            {
                break;
            }
         }
    }
    private void sendUpdates(int team, int player, GameState snapshot, GameState delta)
    {
        UDPConnection con = conns[team][player].con;
        if (con == null)
        {
            return;
        }
        
        OutputMessage msg = new OutputMessage();
        BitOutput out = msg.getBitStream();
        
        try
        {
            out.writeInt(conns[team][player].lastCmd, 12); //sequence number of last recieved command
            //out.writeInt(conns[team][player].lastGSAck, GameProtocol.TICK_TIME_FORMAT); //gsKeepFirst
            out.writeInt(delta == null ? 0 : delta.currentTick, GameProtocol.TICK_TIME_FORMAT); //deltaTick
            snapshot.write(out, delta);
            out.align();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        con.sendMessage(msg);
    }
    
    private boolean isEveryoneDisconnected()
    {
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                Session s = players[t][p].account.getSession();
                if (s != null && s.getClient().isLoggedIn())
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void processClient(PlayerConnection player)
    {
        InputMessage msg;
        while ((msg = player.con.readMessage()) != null)
        {
            try
            {
                BitInput in = msg.getBitStream();
                
                player.lastGSAck = in.readInt(GameProtocol.TICK_TIME_FORMAT);
                int cmdSeq = in.readInt(12); //command sequence number
                int delta = (cmdSeq - player.lastCmd - 1) & 0xfff; //delta minus one, truncated to 12 bits
                if ((delta & 0x800) == 0x800) //check if delta is negative
                {
                    //this command is old, ignore it
                    continue;
                }
                player.lastCmd = (short)cmdSeq;
                
                int cmdId = in.readInt(4);
                switch (cmdId)
                {
                    case GameProtocol.CMD_NONE:
                        break;
                    case GameProtocol.CMD_MOVE:
                        float moveX = in.readFloat();
                        float moveY = in.readFloat();
                        gameState.moveCommand(gameState.getPlayerChampion(player.info.team, player.info.playerId), moveX, moveY);
                        break;
                    case GameProtocol.CMD_ATTACK:
                        int targetId = in.readInt(IntFormat.UINTV);
                        SEntity target = gameState.findEntity(targetId);
                        if (!(target instanceof SActor))
                        {
                            throw new GameProtocolException("Invalid target (" + target + ")");
                        }
                        gameState.attackCommand(gameState.getPlayerChampion(player.info.team, player.info.playerId), (SActor)target);
                        break;
                    default:
                        throw new GameProtocolException("Invalid cmdId: " + cmdId);
                }
            }
            catch (IOException e)
            {
                SharedUtil.logErrorMsg(e);
            }
        }
    }
    
    void start()
    {
        thread.start();
    }
    
    void stop()
    {
        if (shouldStop)
        {
            return;
        }
        
        Logger.log("Stopping game...");
        
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                UDPConnection con = conns[t][p].con;
                if (con != null)
                {
                    con.close();
                }
                
                Session s = players[t][p].account.getSession();
                if (s != null)
                {
                    s.setGame(null);
                }
            }
        }
        
        shouldStop = true;
    }
    
    private static final class PlayerConnection
    {
        private final PlayerInfo info;
        private UDPConnection con;
        private short lastCmd;
        private int lastGSAck;
        private GameState lastGSSent = null;
        
        private PlayerConnection(PlayerInfo info)
        {
            this.info = info;
        }
    }
}
