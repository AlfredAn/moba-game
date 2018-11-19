package onlinegame.shared.game.pathfinder;

/**
 *
 * @author Alfred
 */
public final class Direction
{
    private Direction() {}
    
    public static final byte
            XMIN = 0b000100,
            XMID = 0b000010,
            XMAX = 0b000001,
            YMIN = 0b100000,
            YMID = 0b010000,
            YMAX = 0b001000,
            
            XMASK = 0b000111,
            YMASK = 0b111000;
    
    public static final byte
            NW = XMIN + YMAX,
            N  = XMID + YMAX,
            NE = XMAX + YMAX,
            W  = XMIN + YMID,
            C  = XMID + YMID,
            E  = XMAX + YMID,
            SW = XMIN + YMIN,
            S  = XMID + YMIN,
            SE = XMAX + YMIN;
}
