package onlinegame.shared.game.actions;

import java.io.IOException;
import onlinegame.shared.GameUtil;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.net.GameProtocolException;

/**
 *
 * @author Alfred
 */
public abstract class Action
{
    public static Action read(BitInput in) throws IOException
    {
        int type = in.readInt(4);
        
        switch (type)
        {
            case A_NONE:
                return null;
            case A_ATTACK:
                return new AttackAction(in);
            default:
                throw new GameProtocolException("Invalid Action type: " + type);
        }
    }
    
    public static void write(Action a, BitOutput out) throws IOException
    {
        if (a == null)
        {
            out.writeInt(A_NONE, 4);
            return;
        }
        a.writeData(out);
    }
    
    public static final int
            A_NONE = 0,
            A_ATTACK = 1;
    
    public final double startTime, endTime;
    
    protected Action(BitInput in) throws IOException
    {
        int startTick = in.readInt(GameProtocol.TICK_TIME_FORMAT);
        int endTick = startTick + in.readInt(IntFormat.UINTV2);
        
        startTime = GameUtil.ticksToSeconds(startTick);
        endTime = GameUtil.ticksToSeconds(endTick);
        
        if (endTime - startTime < 0) throw new GameProtocolException("An Action cannot have negative length: " + (endTime - startTime));
    }
    protected Action(double startTime, double length)
    {
        this.startTime = startTime;
        endTime = startTime + length;
        
        if (length < 0) throw new IllegalArgumentException("An Action cannot have negative length: " + length);
    }
    
    public abstract int getActionId();
    
    public boolean allowMovement(double time) { return false; } //whether you can move while performing the action
    public boolean allowCancel(double time) { return false; }   //whether the action is cancelable, by attempting to perform another action simultaneously
    
    public double allowMovementSince(double time) { return allowMovement(time) ? startTime : Double.POSITIVE_INFINITY; }
    public double allowCancelSince(double time) { return allowCancel(time) ? startTime : Double.POSITIVE_INFINITY; }
    
    protected void writeData(BitOutput out) throws IOException
    {
        int startTick = GameUtil.secondsToTicks(startTime);
        int endTick = GameUtil.secondsToTicks(endTime);
        
        out.writeInt(getActionId(), 4);
        out.writeInt(startTick, GameProtocol.TICK_TIME_FORMAT);
        out.writeInt(endTick - startTick, IntFormat.UINTV2);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;
        
        Action a = (Action)o;
        return  getActionId() == a.getActionId() &&
                startTime == a.startTime &&
                endTime == a.endTime;
    }
}
