package onlinegame.shared.game.projectiles;

import java.io.IOException;
import onlinegame.shared.GameUtil;
import onlinegame.shared.MathUtil;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;
import onlinegame.shared.game.Entity;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;

/**
 *
 * @author Alfred
 */
public final class TargetedProjectile extends Entity
{
    public final int source, target;
    public final float speed;
    public final double startTime;
    
    public TargetedProjectile(int id, int team, int source, int target, float speed, double startTime)
    {
        super(id, team);
        this.source = source;
        this.target = target;
        this.speed = speed;
        this.startTime = startTime;
    }
    public TargetedProjectile(BitInput in, int id, Entity prev) throws IOException
    {
        super(in, id, prev);
        
        if (prev == null)
        {
            source = in.readInt(IntFormat.UINTV);
            target = in.readInt(IntFormat.UINTV);
            speed = (float)in.readInt(12) / 16f; //the speed is sent as a 12-bit fixed point number
            startTime = GameUtil.ticksToSeconds(in.readInt(GameProtocol.TICK_TIME_FORMAT));
        }
        else
        {
            TargetedProjectile p = (TargetedProjectile)prev;
            source = p.source;
            target = p.target;
            speed = p.speed;
            startTime = p.startTime;
        }
    }
    
    @Override
    public void writeData(BitOutput out, Entity prev) throws IOException
    {
        super.writeData(out, prev);
        
        if (prev == null)
        {
            out.writeInt(source, IntFormat.UINTV);
            out.writeInt(target, IntFormat.UINTV);
            out.writeInt(MathUtil.clamp(Math.round(speed*16), 0, 4095), 12);
            out.writeInt(GameUtil.secondsToTicks(startTime), GameProtocol.TICK_TIME_FORMAT);
        }
    }
    
    @Override
    public float getXPos(GameState gs, double time)
    {
        Entity src = gs.findEntity(source);
        if (src == null)
        {
            throw new IllegalStateException("Source doesn't exist.");
        }
        return src.getXPos(gs, time);
    }
    
    @Override
    public float getYPos(GameState gs, double time)
    {
        Entity src = gs.findEntity(source);
        if (src == null)
        {
            throw new IllegalStateException("Source doesn't exist.");
        }
        return src.getYPos(gs, time);
    }
    
    @Override
    public int getTypeId()
    {
        return GameProtocol.E_PROJECTILE_TARGETED;
    }
    
    @Override
    protected boolean eq(Entity other)
    {
        TargetedProjectile p = (TargetedProjectile)other;
        return 
                super.eq(p) &&
                source == p.source &&
                target == p.target &&
                speed == p.speed &&
                startTime == p.startTime;
    }
}
