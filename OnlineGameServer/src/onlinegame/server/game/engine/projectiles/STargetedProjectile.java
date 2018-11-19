package onlinegame.server.game.engine.projectiles;

import onlinegame.server.game.engine.SEntity;
import onlinegame.server.game.engine.SGameState;
import onlinegame.shared.MathUtil;
import onlinegame.shared.game.projectiles.TargetedProjectile;

/**
 *
 * @author Alfred
 */
public class STargetedProjectile extends SEntity
{
    public final SEntity source, target;
    public final float speed;
    public final double startTime;
    
    public STargetedProjectile(SGameState game, SEntity source, SEntity target, float speed, double startTime)
    {
        super(game, source.getXPos(), source.getYPos(), source.team);
        this.source = source;
        this.target = target;
        this.speed = speed;
        this.startTime = startTime;
        
        xPos = source.getXPos();
        yPos = source.getYPos();
    }
    
    @Override
    public void update()
    {
        super.update();
        
        if (!game.entityExists(target))
        {
            game.removeEntity(this);
            return;
        }
        
        double delta = game.getTickDelta();
        double dx = target.getXPos() - xPos;
        double dy = target.getYPos() - yPos;
        double dist = Math.sqrt(dx * dx + dy * dy) / speed / delta;
        
        if (dist < 1)
        {
            xPos = target.getXPos();
            yPos = target.getYPos();
            
            //target hit
            //System.out.println("HIT!!!");
            game.removeEntity(this);
        }
        else
        {
            float f = (float)(1. / dist);
            xPos = MathUtil.lerp(xPos, target.getXPos(), f);
            yPos = MathUtil.lerp(yPos, target.getYPos(), f);
        }
    }
    
    @Override
    public TargetedProjectile getSnapshot()
    {
        return new TargetedProjectile(id, team, source.id, target.id, speed, startTime);
    }
}
