package onlinegame.client.game.clientgamestate;

import onlinegame.client.graphics.IColor4f;
import onlinegame.shared.game.GameState;
import onlinegame.shared.game.Minion;

/**
 *
 * @author Alfred
 */
public abstract class CMinion extends CActor
{
    protected static final IColor4f[] minionTeamCol =
    {
        new IColor4f(.125f, .125f, .75f),
        new IColor4f(.75f, .125f, .125f),
        new IColor4f(.625f, .625f, .125f)
    };
    
    protected CMinion(CGameState game, GameState gs, Minion m, Minion next, Class<? extends Minion> validStateClass)
    {
        super(game, gs, m, next, validStateClass);
    }
    
    @Override
    public float getClickXSize() {return 1.0f;}
    @Override
    public float getClickYSize() {return 1.0f;}
    @Override
    public float getClickZSize() {return 0.4f;}
    
    @Override
    public float getProjectileSpawnZ() {return .25f;}
    @Override
    public float getProjectileHitZ() {return .2f;}
    
    @Override
    public Minion getState()
    {
        return (Minion)state;
    }
}
