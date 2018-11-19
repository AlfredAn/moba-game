package onlinegame.shared.game.stats;

import java.io.IOException;
import java.util.Arrays;
import onlinegame.shared.MathUtil;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;

/**
 *
 * @author Alfred
 */
public abstract class Status
{
    public static final int
            HEALTH = 0,
            MANA   = 1,
            
            NUM_RESOURCES = 2;
    
    private final int[] resourceType;
    
    public Status(int[] resourceType)
    {
        this.resourceType = Arrays.copyOf(resourceType, resourceType.length);
        
        for (int i = 0; i < this.resourceType.length; i++)
        {
            if (this.resourceType[i] >= NUM_RESOURCES)
            {
                throw new IllegalArgumentException("Invalid resource: " + this.resourceType[i]);
            }
        }
    }
    
    private static final IntFormat
            resourceMaxFormat = new IntFormat(false, 7, 13, 31);
    
    public static ActorStatus read(BitInput in) throws IOException
    {
        int num = in.readInt(IntFormat.UINTV);
        
        int[] resourceType = new int[num];
        int[] resourceAmount = new int[num];
        int[] resourceMax = new int[num];
        
        for (int i = 0; i < num; i++)
        {
            resourceType[i] = in.readInt(4);
            resourceMax[i] = in.readInt(resourceMaxFormat);
            
            int bits = resourceMax[i] == 0 ? 0 : MathUtil.ceilingLog2(resourceMax[i]);
            resourceAmount[i] = in.readInt(bits);
        }
        
        return new ActorStatus(resourceType, resourceAmount, resourceMax);
    }
    
    public void write(BitOutput out) throws IOException
    {
        out.writeInt(getNumResources(), IntFormat.UINTV);
        
        for (int i = 0; i < getNumResources(); i++)
        {
            out.writeInt(resourceType[i], 4);
            out.writeInt((int)getResourceMax(i), resourceMaxFormat);
            //fiskmåsar
        }
    }
    
    public final int getNumResources()
    {
        return resourceType.length;
    }
    
    public final int getResourceType(int r)
    {
        return resourceType[r];
    }
    
    public abstract float getResourceAmount(int r);
    public abstract float getResourceMax(int r);
    
    @Override
    public final boolean equals(Object other)
    {
        if (other == this) return true;
        if (other == null) return false;
        if (!(other instanceof Status)) return false;
        Status o = (Status)other;
        
        if (getNumResources() != o.getNumResources()) return false;
        
        for (int i = 0; i < getNumResources(); i++)
        {
            if (getResourceType(i)   != o.getResourceType(i))   return false;
            if (getResourceAmount(i) != o.getResourceAmount(i)) return false;
            if (getResourceMax(i)    != o.getResourceMax(i))    return false;
        }
        
        return true;
    }
}
