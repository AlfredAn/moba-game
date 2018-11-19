package onlinegame.client.game.clientgamestate;

import static onlinegame.client.game.clientgamestate.CMinion.minionTeamCol;
import onlinegame.client.game.graphics.GameModels;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.shared.Logger;
import onlinegame.shared.MathUtil;
import onlinegame.shared.game.Entity;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;
import onlinegame.shared.game.projectiles.TargetedProjectile;

/**
 *
 * @author Alfred
 */
public final class CTargetedProjectile extends CEntity
{
    private CEntity source, target;
    
    private float zPos;
    private float targetX, targetY, targetZ;
    
    protected CTargetedProjectile(CGameState game, GameState gs, TargetedProjectile tp, TargetedProjectile next)
    {
        super(game, gs, tp, next, TargetedProjectile.class);
        
        source = game.findEntity(tp.source);
        target = game.findEntity(tp.target);
        
        zPos = 1.0f;
        targetZ = 1.0f;
        
        if (source == null)
        {
            Entity src = gs.findEntity(tp.source);
            if (src == null)
            {
                //source position cannot be found
                Logger.logError("CTargetedProjectile: Source position cannot be found. Consider including start pos in TargetedProjectile");
                game.removeEntity(this);
                return;
            }
            else
            {
                xPos = src.getXPos(gs, game.getCurrentTime());
                yPos = src.getYPos(gs, game.getCurrentTime());
            }
        }
        else
        {
            xPos = source.getXPos();
            yPos = source.getYPos();
            zPos = source.getProjectileSpawnZ();
        }
        
        if (target == null)
        {
            Entity tar = gs.findEntity(tp.target);
            if (tar == null)
            {
                //source position cannot be found
                Logger.logError("CTargetedProjectile: Target position cannot be found. Consider including target pos in TargetedProjectile");
                game.removeEntity(this);
                return;
            }
            else
            {
                targetX = tar.getXPos(gs, game.getCurrentTime());
                targetY = tar.getYPos(gs, game.getCurrentTime());
            }
        }
        else
        {
            targetX = target.getXPos();
            targetY = target.getYPos();
            targetZ = target.getProjectileHitZ();
        }
        
        if (tp.startTime < game.getCurrentTime())
        {
            moveTowardsTarget(game.getCurrentTime() - tp.startTime);
        }
    }
    
    @Override
    public void update(double delta)
    {
        if (!game.entityExists(source)) source = null;
        
        if (game.entityExists(target))
        {
            targetX = target.getXPos();
            targetY = target.getYPos();
        }
        else
        {
            target = null;
        }
        
        moveTowardsTarget(delta);
    }
    
    private void moveTowardsTarget(double delta)
    {
        //don't use zPos in this calculation as the server won't take it into account
        double dx = targetX - xPos;
        double dy = targetY - yPos;
        double dist = Math.sqrt(dx * dx + dy * dy) / getState().speed / delta;
        
        if (dist < 1)
        {
            xPos = targetX;
            yPos = targetY;
            zPos = targetZ;
            
            //target hit
            //System.out.println("HIT!!!");
            game.removeEntity(this);
        }
        else
        {
            float f = (float)(1. / dist);
            xPos = MathUtil.lerp(xPos, targetX, f);
            yPos = MathUtil.lerp(yPos, targetY, f);
            zPos = MathUtil.lerp(zPos, targetZ, f);
        }
    }
    
    @Override
    public void draw(boolean isSelected)
    {
        Draw.mat.pushMatrix();
        Draw.mat.translate(xPos, yPos, zPos);
        Draw.color.set(minionTeamCol[team]);
        Draw.color.blend(Color4f.LIGHT_GRAY, .25f);
        
        Draw.drawModel(GameModels.projectileModel);
        
        Draw.mat.popMatrix();
    }
    
    @Override
    public int getTypeId()
    {
        return GameProtocol.E_PROJECTILE_TARGETED;
    }
    
    @Override
    public TargetedProjectile getState()
    {
        return (TargetedProjectile)state;
    }
    
    @Override
    public boolean isRemovedByServer()
    {
        return false;
    }
}
