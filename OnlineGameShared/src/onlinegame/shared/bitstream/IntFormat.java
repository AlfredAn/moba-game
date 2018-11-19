package onlinegame.shared.bitstream;

import java.util.Arrays;

/**
 *
 * @author Alfred
 */
public final class IntFormat
{
    public static final IntFormat
            BIT = new IntFormat(false, 1),
            BOOLEAN = BIT,
            NIBBLE = new IntFormat(false, 4),
            
            UBYTE = new IntFormat(false, 8),
            BYTE = new IntFormat(true, 8),
            
            USHORT = new IntFormat(false, 16),
            SHORT = new IntFormat(true, 16),
            
            UINT = new IntFormat(false, 32),
            INT = new IntFormat(true, 32),
            
            ULONG = new IntFormat(false, 64),
            LONG = new IntFormat(true, 64),
            
            USHORTV = new IntFormat(false, 4, 8, 16),
            SHORTV = new IntFormat(true, 4, 8, 16),
            
            UINTV = new IntFormat(false, 4, 8, 16, 24, 32),
            INTV = new IntFormat(true, 4, 8, 16, 24, 32),
            
            UINTV2 = new IntFormat(false, 8, 16, 24, 32),
            INTV2 = new IntFormat(true, 8, 16, 24, 32),
            
            ULONGV = new IntFormat(false, 8, 16, 32, 48, 64),
            LONGV = new IntFormat(true, 8, 16, 32, 48, 64),
            
            SIZE = new IntFormat(false, 4, 8, 16, 24, 31);
    
    final boolean signed;
    final int[] breakPoints;
    
    public IntFormat(boolean signed, int... breakPoints)
    {
        if (breakPoints.length == 0)
        {
            throw new IllegalArgumentException("You need to specify at least one breakpoint.");
        }
        for (int i = 0; i < breakPoints.length; i++)
        {
            int val = breakPoints[i];
            if (val < 0)
            {
                throw new IllegalArgumentException("Negative bit count at index " + i + ".");
            }
        }
        
        this.signed = signed;
        this.breakPoints = Arrays.copyOf(breakPoints, breakPoints.length);
        Arrays.sort(this.breakPoints);
    }
    
    public boolean isSigned()
    {
        return signed;
    }
    
    public int getNumBreakPoints()
    {
        return breakPoints.length;
    }
    
    public int getBreakPoint(int index)
    {
        return breakPoints[index];
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof IntFormat)) return false;
        
        IntFormat other = (IntFormat)o;
        return signed == other.signed && Arrays.equals(breakPoints, other.breakPoints);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + (signed ? 1 : 0);
        hash = 79 * hash + Arrays.hashCode(breakPoints);
        return hash;
    }
}
