package onlinegame.shared.game;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import onlinegame.shared.GameUtil;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;
import onlinegame.shared.net.GameProtocolException;

/**
 *
 * @author Alfred
 */
public final class GameState
{
    private final Entity[] entities;
    private final TIntObjectMap<Entity> idMap;
    
    private final Champion[][] players;
    
    public final int currentTick;
    public final double currentTime;
    
    public GameState(Entity[] entities, int currentTick, int[][] playerIds)
    {
        this.entities = entities;
        this.currentTick = currentTick;
        currentTime = GameUtil.ticksToSeconds(currentTick);
        idMap = init();
        
        players = new Champion[2][];
        for (int t = 0; t < 2; t++)
        {
            players[t] = new Champion[playerIds[t].length];
            for (int p = 0; p < players[t].length; p++)
            {
                Entity e = findEntity(playerIds[t][p]);
                players[t][p] = (Champion)e;
            }
        }
    }
    public GameState(BitInput in, GameState prev) throws IOException
    {
        currentTick = in.readInt(GameProtocol.TICK_TIME_FORMAT);
        currentTime = GameUtil.ticksToSeconds(currentTick);
        
        int numEntities = in.readInt(IntFormat.SIZE);
        entities = new Entity[numEntities];
        
        if (prev == null)
        {
            for (int i = 0; i < numEntities; i++)
            {
                entities[i] = Entity.read(in, null);
                //System.out.println("create: " + entities[i].getClass().getSimpleName() + "(" + entities[i].id + ")");
                
                if (entities[i] == null) throw new GameProtocolException("Null entities are not allowed!");
            }
            idMap = init();
        }
        else
        {
            idMap = new TIntObjectHashMap<>(entities.length * 2 + 1, .5f);
            
            while (true) //temporarily put all the destroyed entity id:s in the map (which for now acts as a set)
            {
                int id = in.readInt(IntFormat.UINTV);
                if (id == 0) break;
                
                idMap.put(id, null);
            }
            
            int i = 0;
            while (true) //read all updated entities
            {
                Entity e = Entity.read(in, prev);
                
                //if (e != null) System.out.println("update: " + e.getTypeId() + "(" + e.id + ")");
                
                if (e == null) break; //reached the last entity
                if (i >= numEntities) throw new GameProtocolException("Too many entities sent!");
                entities[i++] = e; //store entity and increment counter
                idMap.put(e.id, null);
            }
            
            for (int j = 0; j < prev.numEntities(); j++) //add all entities that weren't updated
            {
                Entity e = prev.getEntity(j);
                if (!idMap.containsKey(e.id))
                {
                    if (i >= numEntities) throw new GameProtocolException("This should never happen ;_;");
                    entities[i++] = e;
                }
            }
            if (i < numEntities - 1) throw new GameProtocolException("fdjoflajhoo");
            
            idMap.clear();
            for (int j = 0; j < entities.length; j++) //clear the map and fill it with the entities
            {
                Entity e = entities[j];
                idMap.put(e.id, e);
            }
        }
        players = readPlayers(in, prev);
    }
    
    private TIntObjectMap<Entity> init()
    {
        TIntObjectMap<Entity> map = new TIntObjectHashMap<>(entities.length * 2 + 1, .5f);
        for (int i = 0; i < entities.length; i++)
        {
            Entity e = entities[i];
            map.put(e.id, e);
        }
        return map;
    }
    
    private Champion[][] readPlayers(BitInput in, GameState prev) throws IOException
    {
        Champion[][] players;
        if (prev == null || in.readBoolean())
        {
            players = new Champion[2][];
            players[0] = new Champion[in.readInt(3)];
            players[1] = new Champion[in.readInt(3)];
            
            for (int t = 0; t < 2; t++)
            {
                for (int p = 0; p < players[t].length; p++)
                {
                    Entity e = findEntity(in.readInt(IntFormat.UINTV));
                    if (!(e instanceof Champion)) throw new GameProtocolException("Non-champion in player list. (" + e + ")");
                    players[t][p] = (Champion)e;
                }
            }
        }
        else
        {
            players = prev.players;
        }
        return players;
    }
    
    private void writePlayers(BitOutput out, GameState prev) throws IOException
    {
        if (prev != null)
        {
            if (Arrays.deepEquals(players, prev.players))
            {
                out.writeBoolean(false);
                return;
            }
            else
            {
                out.writeBoolean(true);
            }
        }
        out.writeInt(players[0].length, 3);
        out.writeInt(players[1].length, 3);
        
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                out.writeInt(players[t][p].id, IntFormat.UINTV);
            }
        }
    }
    
    public void write(BitOutput out, GameState prev) throws IOException
    {
        //write basic info
        out.writeInt(currentTick, GameProtocol.TICK_TIME_FORMAT);
        out.writeInt(entities.length, IntFormat.SIZE); //number of entities
        
        if (prev == null)
        {
            for (int i = 0; i < entities.length; i++)
            {
                entities[i].write(out, null);
            }
        }
        else
        {
            Entity[] prevEntities = prev.entities;
            //write the id of all destroyed entities (null-terminated)
            for (int i = 0; i < prevEntities.length; i++)
            {
                Entity e = prevEntities[i];
                if (!idMap.containsKey(e.id))
                {
                    out.writeInt(e.id, IntFormat.UINTV);
                }
            }
            out.writeInt(0, IntFormat.UINTV); //write 0 to mark the end
            
            //write all updated entities (null-terminated)
            for (int i = 0; i < entities.length; i++)
            {
                Entity e = entities[i];
                Entity delta = prev.findEntity(e.id);
                e.write(out, delta);
            }
            out.writeInt(0, IntFormat.UINTV);
        }
        writePlayers(out, prev);
    }
    
    public int numEntities()
    {
        return entities.length;
    }
    
    public Entity getEntity(int i)
    {
        return entities[i];
    }
    
    public Entity findEntity(int id)
    {
        return idMap.get(id);
    }
    
    public int getNumPlayers(int team)
    {
        return players[team].length;
    }
    
    public Champion getPlayer(int team, int player)
    {
        return players[team][player];
    }
    
    public Champion getPlayer(String username)
    {
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                Champion c = players[t][p];
                if (c.username.equals(username))
                {
                    return c;
                }
            }
        }
        return null;
    }
    
    public static final Comparator<GameState> tickComparator = new GameStateTickComparator(); //ascending tick comparator
    private static final class GameStateTickComparator implements Comparator<GameState>
    {
        @Override
        public int compare(GameState first, GameState second)
        {
            return Integer.compare(first.currentTick, second.currentTick);
        }
    }
}
