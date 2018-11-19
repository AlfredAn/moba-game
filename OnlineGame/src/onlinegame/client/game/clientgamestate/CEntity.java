package onlinegame.client.game.clientgamestate;

import onlinegame.shared.game.CasterMinion;
import onlinegame.shared.game.Champion;
import onlinegame.shared.game.Entity;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;
import onlinegame.shared.game.projectiles.TargetedProjectile;

/**
 *
 * @author Alfred
 */
public abstract class CEntity
{
    public static CEntity create(CGameState game, GameState gs, Entity e, Entity next)
    {
        if (next != null && e.getTypeId() != next.getTypeId()) throw new IllegalArgumentException("next must be of the same type as e!!");
        
        switch (e.getTypeId())
        {
            case GameProtocol.E_CHAMPION:
                return new CChampion(game, gs, (Champion)e, (Champion)next);
            case GameProtocol.E_MINION_CASTER:
                return new CCasterMinion(game, gs, (CasterMinion)e, (CasterMinion)next);
            case GameProtocol.E_PROJECTILE_TARGETED:
                return new CTargetedProjectile(game, gs, (TargetedProjectile)e, (TargetedProjectile)next);
            default:
                throw new IllegalArgumentException("Invalid entity type: " + e.getTypeId());
        }
    }
    
    public final CGameState game;
    public final int id;
    
    protected float xPos, yPos;
    protected Entity state, nextState;
    public final int team;
    
    private final Class<? extends Entity> validStateClass;
    
    protected CEntity(CGameState game, GameState gs, Entity e, Entity next, Class<? extends Entity> validStateClass)
    {
        this.game = game;
        id = e.id;
        team = e.team;
        
        this.validStateClass = validStateClass;
        
        refresh(gs, e, next);
    }
    
    public final void refresh(GameState gs, Entity e, Entity next)
    {
        if (!validStateClass.isInstance(e))
        {
            throw new ClassCastException("Tried to refresh() a " + validStateClass.getSimpleName() + " with a " + e.getClass().getSimpleName());
        }
        
        if (state != null && state.equals(e))
        {
            state = e;
            nextState = next;
            return;
        }
        
        doRefresh(gs, e);
        state = e;
        nextState = next;
    }
    
    protected void doRefresh(GameState gs, Entity e)
    {
        xPos = e.getXPos(gs, gs.currentTime);
        yPos = e.getYPos(gs, gs.currentTime);
    }
    
    public void update(double delta) {}
    
    public void draw(boolean isSelected) {}
    
    public abstract int getTypeId();
    
    public float getXPos()
    {
        return xPos;
    }
    
    public float getYPos()
    {
        return yPos;
    }
    
    public boolean isClickable() {return false;}
    
    public float getClickXSize() {return .4f;}
    public float getClickYSize() {return .4f;}
    public float getClickZSize() {return .4f;}
    
    public float getProjectileSpawnZ() {return 1.0f;}
    public float getProjectileHitZ() {return 1.0f;}
    
    public Entity getState()
    {
        return state;
    }
    
    public Entity getNextState()
    {
        return nextState;
    }
    
    public boolean isRemovedByServer()
    {
        return true;
    }
}
