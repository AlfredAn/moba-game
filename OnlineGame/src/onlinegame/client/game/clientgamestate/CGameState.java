package onlinegame.client.game.clientgamestate;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.List;
import onlinegame.client.game.GameNetwork;
import onlinegame.shared.GameUtil;
import onlinegame.shared.game.Champion;
import onlinegame.shared.game.Entity;
import onlinegame.shared.game.GameMap;
import onlinegame.shared.game.GameState;

/**
 *
 * @author Alfred
 */
public final class CGameState
{
    private final List<CEntity> entities = new ArrayList<>();
    private final List<CEntity> adding = new ArrayList<>();
    private final List<CEntity> removing = new ArrayList<>();
    private final TIntObjectMap<CEntity> idMap = new TIntObjectHashMap<>();
    
    private int currentTick = -GameUtil.SERVER_TICKRATE;
    private double currentTime = -1;
    
    public final GameMap map;
    
    private static final double lagBuffer = 0.035; //seconds
    
    private boolean isInitialized = false;
    
    private GameState gameState;
    private GameState nextGameState;
    private double gsDelta;
    
    public CGameState(GameMap map)
    {
        this.map = map;
    }
    
    public void update(double delta, GameNetwork net)
    {
        currentTime += delta;
        
        GameState lastGs = net.getCurrentGameState();
        int maxTick = lastGs == null ? currentTick : lastGs.currentTick;
        
        double expectedTime = lastGs == null ? (currentTime) : (lastGs.currentTime - lagBuffer - GameUtil.SERVER_TICK_DELTA);
        double timeshift = 0.5 * (expectedTime - currentTime) * Math.min(delta, 2); //speed up/slow down time to catch up with the server
        
        if (lastGs != null && (Math.abs(expectedTime - currentTime) >= 1.0 || !isInitialized))
        {
            currentTime = expectedTime;
            timeshift = 0;
        }
        
        double actualDelta = delta + timeshift;
        currentTime += timeshift;
        
        while (GameUtil.secondsToTicks((float)currentTime) > (currentTick) && currentTick < maxTick)
        {
            //advance to the next tick
            currentTick++;
            
            GameState gs = net.getGameState(currentTick);
            
            GameState next = null;
            for (int t = currentTick+1; t <= maxTick; t++)
            {
                next = net.getGameState(t);
                if (next != null) break;
            }
            
            if (gs != null)
            {
                refresh(gs, next);
            }
        }
        
        addAndRemove();
        
        for (int i = 0; i < entities.size(); i++)
        {
            CEntity e = entities.get(i);
            e.update((float)actualDelta);
        }
        
        addAndRemove();
    }
    
    public void draw(CEntity mouseOver)
    {
        for (int i = 0; i < entities.size(); i++)
        {
            CEntity e = entities.get(i);
            e.draw(mouseOver == e);
        }
    }
    
    private void refresh(GameState gs, GameState next)
    {
        gameState = gs;
        nextGameState = next;
        
        gsDelta = currentTime - gameState.currentTime;
        
        isInitialized = true;
        
        addAndRemove();
        
        for (int i = 0; i < numEntities(); i++)
        {
            CEntity ce = getEntity(i);
            Entity e = gs.findEntity(ce.id);
            
            if (e == null && ce.isRemovedByServer())
            {
                removeEntity(ce);
            }
        }
        
        for (int i = 0; i < gs.numEntities(); i++)
        {
            Entity e = gs.getEntity(i);
            CEntity ce = findEntity(e.id);
            
            Entity ne = null;
            if (next != null)
            {
                ne = next.findEntity(e.id);
            }
            
            if (ce == null)
            {
                addEntity(CEntity.create(this, gs, e, ne));
            }
            else
            {
                ce.refresh(gs, e, ne);
            }
        }
        
        addAndRemove();
    }
    
    public boolean isInitialized()
    {
        return isInitialized;
    }
    
    public GameState getGameState()
    {
        return gameState;
    }
    
    public int getNumPlayers(int team)
    {
        if (!isInitialized) throw new IllegalStateException("CGameState not yet initialized!");
        return gameState.getNumPlayers(team);
    }
    
    public CChampion getPlayer(int team, int player)
    {
        if (!isInitialized) throw new IllegalStateException("CGameState not yet initialized!");
        
        Champion c = gameState.getPlayer(team, player);
        return c == null ? null : (CChampion)findEntity(c.id);
    }
    
    public CChampion getPlayer(String username)
    {
        if (!isInitialized) throw new IllegalStateException("CGameState not yet initialized!");
        
        Champion c = gameState.getPlayer(username);
        return c == null ? null : (CChampion)findEntity(c.id);
    }
    
    public double getCurrentTime()
    {
        return currentTime;
    }
    
    public int getCurrentTick()
    {
        return currentTick;
    }
    
    public void addEntity(CEntity e)
    {
        adding.add(e);
    }
    
    public void removeEntity(CEntity e)
    {
        removing.add(e);
    }
    
    public int numEntities()
    {
        return entities.size();
    }
    
    public CEntity getEntity(int index)
    {
        return entities.get(index);
    }
    
    public CEntity findEntity(int id)
    {
        return idMap.get(id);
    }
    
    public boolean entityExists(CEntity e)
    {
        return e != null && idMap.containsKey(e.id);
    }
    
    public double getGSDelta()
    {
        return gsDelta;
    }
    
    private void addAndRemove()
    {
        entities.addAll(adding);
        for (int i = 0; i < adding.size(); i++)
        {
            CEntity e = adding.get(i);
            idMap.put(e.id, e);
        }
        adding.clear();
        
        entities.removeAll(removing);
        for (int i = 0; i < removing.size(); i++)
        {
            CEntity e = removing.get(i);
            idMap.remove(e.id);
        }
        removing.clear();
    }
}
