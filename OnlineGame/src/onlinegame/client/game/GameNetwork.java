package onlinegame.client.game;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.util.Collections;
import onlinegame.client.Timing;
import onlinegame.shared.ArrayDequeList;
import onlinegame.shared.Logger;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;
import onlinegame.shared.game.Entity;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;
import onlinegame.shared.net.udp.UDPClientConnection;
import onlinegame.shared.net.udp.UDPConnection;

/**
 *
 * @author Alfred
 */
public final class GameNetwork
{
    public final Game game;
    
    private final UDPConnection con;
    
    private GameState currentGameState;
    
    private int cmdId = GameProtocol.CMD_NONE;
    private short cmdSeq = 0;
    private float cmdX, cmdY;
    private int cmdEntity = 0;
    
    private short cmdAck = 0;
    
    //private int gsKeepFirst = 0;
    
    private long sendTimeLeft = 0;
    private static final long
            sendInterval = 1_000_000_000L / 30L; //30Hz
    
    private final ArrayDequeList<GameState> gameStates = new ArrayDequeList<>();
    private final TIntObjectMap<GameState> gameStateIdMap = new TIntObjectHashMap<>();
    
    private final ArrayDequeList<PendingGameState> pendingGameStates = new ArrayDequeList<>();
    
    private boolean shouldSend = true;
    
    GameNetwork(Game game) throws IOException
    {
        this.game = game;
        
        con = new UDPClientConnection(game.client.getNetwork().getServerAddress(), Protocol.DEFAULT_PORT, game.client.getNetwork().getSessionHash());
        
        Logger.log("GameNetwork started");
    }
    
    void update()
    {
        recieve();
        
        sendTimeLeft -= Timing.getDeltaNanos();
        if (sendTimeLeft <= 0)
        {
            send();
            if (sendTimeLeft <= -sendInterval) //guard against fps drops
            {
                sendTimeLeft = sendInterval;
            }
            else
            {
                sendTimeLeft += sendInterval;
            }
        }
    }
    
    private void recieve()
    {
        //recieve messages
        InputMessage msg;
        while ((msg = con.readMessage()) != null)
        {
            BitInput in = msg.getBitStream();
            try
            {
                cmdAck = (short)in.readInt(12);
                //gsKeepFirst = in.readInt(GameProtocol.TICK_TIME_FORMAT);
                
                int deltaTick = in.readInt(GameProtocol.TICK_TIME_FORMAT);
                GameState delta = gameStateIdMap.get(deltaTick);
                if (deltaTick != 0 && delta == null)
                {
                    if (deltaTick >= gameStates.peek().currentTick)
                    {
                        //save message for potential later use
                        PendingGameState pgs = new PendingGameState(in, deltaTick);
                        pendingGameStates.add(pgs);
                        
                        if (pendingGameStates.size() > 1 && pgs.deltaTick < pendingGameStates.get(pendingGameStates.size()-2).deltaTick)
                        {
                            Collections.sort(pendingGameStates);
                        }
                        
                        while (pendingGameStates.size() >= 126)
                        {
                            pendingGameStates.poll();
                        }
                    }
                    //prehaps save these and use later in case the delta GameState arrives later?
                    /*Logger.logError("Missing delta GameState! "
                            + "(this: " + deltaTick
                            + ", earliest: "+ gameStates.peekFirst().currentTick
                            + ", latest: " + gameStates.peekLast().currentTick + ")");*/
                    continue;
                }
                
                readGameState(in, delta);
                
                GameState prec = currentGameState;
                currentGameState = gameStates.getLast();
                if (prec != currentGameState) forceSend();
            }
            catch (IOException e)
            {
                Logger.logError(e.getMessage(), e);
                //SharedUtil.logErrorMsg(e);
            }
        }
    }
    
    private void readGameState(BitInput in, GameState delta) throws IOException
    {
        GameState newGameState = new GameState(in, delta);
        /*if (!gameStates.isEmpty() && newGameState.currentTick < gameStates.peekLast().currentTick)
        {
            continue; //ignore game state if it is older than the last recieved one
        }*/
        gameStates.add(newGameState);
        gameStateIdMap.put(newGameState.currentTick, newGameState);
        
        if (gameStates.size() > 1 && newGameState.currentTick < gameStates.get(gameStates.size()-2).currentTick)
        {
            //resort the queue into ascending order if the gamestate is old
            //can be optimized by inserting the new game state in the right place instead of sorting
            Collections.sort(gameStates, GameState.tickComparator);
        }
        
        for (int i = 0; i < pendingGameStates.size(); i++)
        {
            PendingGameState pgs = pendingGameStates.get(i);
            if (pgs.deltaTick == newGameState.currentTick)
            {
                //this GameState can now be read!
                readGameState(pgs.in, newGameState);
                pendingGameStates.remove(i);
                i--;
            }
        }
        
        //remove old gameStates
        while (gameStates.size() >= 510)
        {
            GameState gs = gameStates.poll();
            gameStateIdMap.remove(gs.currentTick);
        }
        /*for (int i = 0; i < gameStates.size(); i++)
        {
            GameState gs = gameStates.get(i);
            if (gameStates.size() >= 512)
            {
                gameStates.remove();
                gameStateIdMap.remove(gs.currentTick);
            }
            else
            {
                break;
            }
        }*/
        
        
    }
    
    private void send()
    {
        if (cmdSeq == cmdAck && !shouldSend)
        {
            //all commands are acknowledged by server; no need to resend them
            return;
        }
        
        shouldSend = false;
        
        //send user input
        OutputMessage omsg = new OutputMessage();
        BitOutput out = omsg.getBitStream();
        
        try
        {
            out.writeInt(gameStates.isEmpty() ? 0 : gameStates.peekLast().currentTick, GameProtocol.TICK_TIME_FORMAT); //last recieved gameState
            out.writeInt(cmdSeq, 12);
            out.writeInt(cmdId, 4);
            switch (cmdId)
            {
                case GameProtocol.CMD_NONE:
                    break;
                case GameProtocol.CMD_MOVE:
                    out.writeFloat(cmdX);
                    out.writeFloat(cmdY);
                    break;
                case GameProtocol.CMD_ATTACK:
                    out.writeInt(cmdEntity, IntFormat.UINTV);
                    break;
                default:
                    throw new IllegalStateException("Illegal value of cmdId: " + cmdId);
            }
            out.align();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        con.sendMessage(omsg);
    }
    
    private void newCmd(boolean urgent)
    {
        cmdSeq++;
        cmdSeq &= 0xfff; //limit to 12 bits
        if (urgent)
        {
            sendTimeLeft = 0; //make sure the new command is sent asap
        }
    }
    
    void clearCommand(boolean urgent)
    {
        newCmd(urgent);
        cmdId = GameProtocol.CMD_NONE;
    }
    
    void moveCommand(float x, float y, boolean urgent)
    {
        if (cmdId == GameProtocol.CMD_MOVE && (Math.abs(x - cmdX) < .0001 && Math.abs(y - cmdY) < .0001)) return;
        
        newCmd(urgent);
        cmdId = GameProtocol.CMD_MOVE;
        cmdX = x;
        cmdY = y;
    }
    
    void attackCommand(Entity e)
    {
        if (cmdId == GameProtocol.CMD_ATTACK && cmdEntity == e.id) return;
        
        newCmd(true);
        cmdId = GameProtocol.CMD_ATTACK;
        cmdEntity = e.id;
    }
    
    private void forceSend()
    {
        shouldSend = true;
    }
    
    void stop()
    {
        con.close();
    }
    
    public GameState getCurrentGameState()
    {
        return currentGameState;
    }
    
    private int prev = 0;
    public GameState getNewGameState()
    {
        int hash = System.identityHashCode(currentGameState);
        if (hash == prev) return null;
        
        prev = hash;
        return currentGameState;
    }
    
    public int getNumSavedGameStates()
    {
        return gameStates.size();
    }
    
    public GameState getGameState(int tick)
    {
        return gameStateIdMap.get(tick);
    }
    
    private static class PendingGameState implements Comparable<PendingGameState>
    {
        private final BitInput in;
        private final int deltaTick;
        
        private PendingGameState(BitInput in, int deltaTick)
        {
            this.in = in;
            this.deltaTick = deltaTick;
        }
        
        @Override
        public int compareTo(PendingGameState other)
        {
            return Integer.compare(deltaTick, other.deltaTick);
        }
    }
}




















