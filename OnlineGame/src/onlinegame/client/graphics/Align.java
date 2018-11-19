package onlinegame.client.graphics;

/**
 *
 * @author Alfred
 */
public final class Align
{
    private Align() {}
    
    private static final int
            XMIN = 0b000100,
            XMID = 0b000010,
            XMAX = 0b000001,
            YMIN = 0b100000,
            YMID = 0b010000,
            YMAX = 0b001000,
            
            XALIGN = 0b000111,
            YALIGN = 0b111000;
    
    public static final int
            TOPLEFT =     XMIN + YMAX,
            TOP =         XMID + YMAX,
            TOPRIGHT =    XMAX + YMAX,
            LEFT =        XMIN + YMID,
            CENTER =      XMID + YMID,
            RIGHT =       XMAX + YMID,
            BOTTOMLEFT =  XMIN + YMIN,
            BOTTOM =      XMID + YMIN,
            BOTTOMRIGHT = XMAX + YMIN;
    
    public static float getXAlign(int align)
    {
        switch (align & XALIGN)
        {
            case XMIN:
                return 0f;
            case XMID:
                return .5f;
            case XMAX:
                return 1f;
        }
        
        throw new IllegalArgumentException("Illegal alignment: " + align);
    }
    
    public static float getYAlign(int align)
    {
        switch (align & YALIGN)
        {
            case YMIN:
                return 0f;
            case YMID:
                return .5f;
            case YMAX:
                return 1f;
        }
        
        throw new IllegalArgumentException("Illegal alignment: " + align);
    }
    
    public static float getInvYAlign(int align)
    {
        switch (align & YALIGN)
        {
            case YMIN:
                return 1f;
            case YMID:
                return .5f;
            case YMAX:
                return 0f;
        }
        
        throw new IllegalArgumentException("Illegal alignment: " + align);
    }
    
    public static String toString(int align)
    {
        switch (align)
        {
            case TOPLEFT:
                return "Top Left";
            case TOP:
                return "Top";
            case TOPRIGHT:
                return "Top Right";
            case LEFT:
                return "Left";
            case CENTER:
                return "Center";
            case RIGHT:
                return "Right";
            case BOTTOMLEFT:
                return "Bottom Left";
            case BOTTOM:
                return "Bottom";
            case BOTTOMRIGHT:
                return "Bottom Right";
            default:
                return "Error";
        }
    }
}
