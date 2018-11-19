/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package onlinegame.shared.game.actions;

import java.io.IOException;
import onlinegame.shared.GameUtil;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;
import static onlinegame.shared.game.actions.Action.A_ATTACK;

/**
 *
 * @author Alfred
 */
public final class AttackAction extends Action
{
    public final int targetId;
    
    /*
    4 stages of an attack:
    
    1. Aiming, can be canceled
    2. Pre-hit, cannot be canceled
    3. Post-hit, cannot be canceled
    4. Reloading, can be canceled
    */
    
    public final double
            preHitStart,
            postHitStart,
            reloadStart;
    
    AttackAction(BitInput in) throws IOException
    {
        super(in);
        
        targetId = in.readInt(IntFormat.UINTV);
        
        int startTick = GameUtil.secondsToTicks(startTime);
        
        int preHitTick = startTick + in.readInt(IntFormat.UINTV2);
        int postHitTick = preHitTick + in.readInt(IntFormat.UINTV2);
        int reloadTick = postHitTick + in.readInt(IntFormat.UINTV2);
        
        preHitStart = GameUtil.ticksToSeconds(preHitTick);
        postHitStart = GameUtil.ticksToSeconds(postHitTick);
        reloadStart = GameUtil.ticksToSeconds(reloadTick);
    }
    public AttackAction(double startTick, double length, int target, double preHitOffset, double postHitOffset, double reloadOffset)
    {
        super(startTick, length);
        
        this.targetId = target;
        
        preHitStart = startTime + preHitOffset;
        postHitStart = startTime + postHitOffset;
        reloadStart = startTime + reloadOffset;
    }
    
    @Override
    protected void writeData(BitOutput out) throws IOException
    {
        super.writeData(out);
        
        out.writeInt(targetId, IntFormat.UINTV);
        
        int startTick = GameUtil.secondsToTicks(startTime);
        int preHitTick = GameUtil.secondsToTicks(preHitStart);
        int postHitTick = GameUtil.secondsToTicks(postHitStart);
        int reloadTick = GameUtil.secondsToTicks(reloadStart);
        
        out.writeInt(preHitTick - startTick, IntFormat.UINTV2);
        out.writeInt(postHitTick - preHitTick, IntFormat.UINTV2);
        out.writeInt(reloadTick - postHitTick, IntFormat.UINTV2);
    }
    
    @Override
    public boolean allowCancel(double time)
    {
        return time < preHitStart || time >= reloadStart;
    }
    
    @Override
    public double allowCancelSince(double time)
    {
        if (time < preHitStart)
        {
            return startTime;
        }
        else if (time < reloadStart)
        {
            return Double.POSITIVE_INFINITY;
        }
        else
        {
            return reloadStart;
        }
    }
    
    public static final int
            ST_AIMING    = 0,
            ST_PREHIT    = 1,
            ST_POSTHIT   = 2,
            ST_RELOADING = 3;
    
    public int getStage(double time)
    {
        if (time < preHitStart)
        {
            return 0; //aiming
        }
        else if (time < postHitStart)
        {
            return 1; //pre-hit
        }
        else if (time < reloadStart)
        {
            return 2; //post-hit
        }
        else
        {
            return 3; //reloading
        }
    }
    
    public double getFracStage(double time)
    {
        switch (getStage(time))
        {
            case ST_AIMING:
                double len = preHitStart - startTime;
                return len == 0 ? 0.5 : Math.max((time - startTime) / len, 0);
            case ST_PREHIT:
                len = postHitStart - preHitStart;
                return len == 0 ? 0.5 : (time - preHitStart) / len;
            case ST_POSTHIT:
                len = reloadStart - postHitStart;
                return len == 0 ? 0.5 : (time - postHitStart) / len;
            case ST_RELOADING:
                len = endTime - reloadStart;
                return len == 0 ? 0.5 : Math.min((time - reloadStart) / len, 1);
            default:
                return 0; //cannot ever happen
        }
    }
    
    @Override
    public int getActionId()
    {
        return A_ATTACK;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o))
        {
            return false;
        }
        AttackAction a = (AttackAction)o;
        return targetId == a.targetId &&
                preHitStart == a.preHitStart &&
                postHitStart == a.postHitStart &&
                reloadStart == a.reloadStart;
    }
}
