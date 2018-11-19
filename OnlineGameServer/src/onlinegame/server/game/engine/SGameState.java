package onlinegame.server.game.engine;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.List;
import onlinegame.server.game.PlayerInfo;
import onlinegame.shared.GameUtil;
import onlinegame.shared.game.Entity;
import onlinegame.shared.game.GameMap;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;
import onlinegame.shared.game.pathfinder.Pathfinder;

/**
 *
 * @author Alfred
 */
public final class SGameState
{
    private final List<SEntity> entities = new ArrayList<>();
    private final List<SEntity> adding = new ArrayList<>();
    private final List<SEntity> removing = new ArrayList<>();
    private final TIntObjectMap<SEntity> idMap = new TIntObjectHashMap<>();
    
    private int idCounter = 1;
    
    private int currentTick = 0;
    
    private final SChampion[][] players;
    private final int[][] playerIds;
    
    public final GameMap map;
    public final Pathfinder pathfinder;
    
    public SGameState(GameMap map, PlayerInfo[][] playerInfo)
    {
        this.map = map;
        pathfinder = new Pathfinder(map, .3);
        
        players = new SChampion[2][];
        playerIds = new int[2][];
        for (int t = 0; t < 2; t++)
        {
            players[t] = new SChampion[playerInfo[t].length];
            playerIds[t] = new int[playerInfo[t].length];
            for (int p = 0; p < playerInfo[t].length; p++)
            {
                PlayerInfo player = playerInfo[t][p];
                if (t == 0)
                {
                    players[t][p] = new SChampion(this, 4, map.getScaledHeight() - 4, t, player.champion, player.account);
                }
                else
                {
                    players[t][p] = new SChampion(this, map.getScaledWidth() - 4, 4, t, player.champion, player.account);
                }
                playerIds[t][p] = players[t][p].id;
                addEntity(players[t][p]);
            }
        }
        
        addEntity(new SCasterMinion(this, 16, map.getScaledHeight() - 16, GameProtocol.TEAM_RED));
        
        addAndRemove();
    }
    
    public double getCurrentTime()
    {
        return GameUtil.ticksToSeconds(currentTick);
    }
    
    public int getCurrentTick()
    {
        return currentTick;
    }
    
    public double getTickDelta()
    {
        return GameUtil.SERVER_TICK_DELTA;
    }
    
    public void update()
    {
        currentTick++;
        
        for (int i = 0; i < entities.size(); i++)
        {
            entities.get(i).update();
        }
        
        addAndRemove();
        
        for (int i = 0; i < entities.size(); i++)
        {
            entities.get(i).postUpdate();
        }
    }
    
    public void moveCommand(SActor a, float x, float y)
    {
        a.moveCommand(x, y);
    }
    
    public void attackCommand(SActor source, SActor target)
    {
        source.attackCommand(target);
    }
    
    private void addAndRemove()
    {
        entities.addAll(adding);
        for (int i = 0; i < adding.size(); i++)
        {
            SEntity e = adding.get(i);
            idMap.put(e.id, e);
        }
        adding.clear();
        
        entities.removeAll(removing);
        for (int i = 0; i < removing.size(); i++)
        {
            SEntity e = removing.get(i);
            idMap.remove(e.id);
        }
        removing.clear();
        
        //Collections.shuffle(entities); //change this later to put attacking entities first
    }
    
    public void addEntity(SEntity e)
    {
        adding.add(e);
        
        //System.out.println("create : " + e.getClass().getSimpleName() + " (" + e.id + ")");
    }
    
    public void removeEntity(SEntity e)
    {
        removing.add(e);
        
        //System.out.println("destroy: " + e.getClass().getSimpleName() + " (" + e.id + ")");
    }
    
    public int numEntities()
    {
        return entities.size();
    }
    
    public SEntity getEntity(int index)
    {
        return entities.get(index);
    }
    
    public SEntity findEntity(int id)
    {
        return idMap.get(id);
    }
    
    public boolean entityExists(SEntity e)
    {
        return e != null && idMap.containsKey(e.id);
    }
    
    public SChampion getPlayerChampion(int team, int player)
    {
        return players[team][player];
    }
    
    public int assignId()
    {
        return idCounter++;
    }
    
    public GameState getSnapshot()
    {
        Entity[] e = new Entity[entities.size()];
        
        for (int i = 0; i < entities.size(); i++)
        {
            e[i] = entities.get(i).getSnapshot();
        }
        
        return new GameState(e, currentTick, playerIds);
    }
}
