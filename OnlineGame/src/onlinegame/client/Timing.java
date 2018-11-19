package onlinegame.client;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

/**
 *
 * @author Alfred
 */
public final class Timing
{
    private Timing() {}
    
    private static long lastFrame, deltaNanos;
    private static double delta;
    
    private static final TLongList frames = new TLongArrayList();
    
    public static void init()
    {
        lastFrame = System.nanoTime();
        delta = 0.001;
        deltaNanos = 1_000_000;
        
        frames.clear();
        frames.add(lastFrame);
    }
    
    public static void update()
    {
        long thisFrame = System.nanoTime();
        
        deltaNanos = thisFrame - lastFrame;
        delta = (double)deltaNanos / 1_000_000_000d;
        
        lastFrame = thisFrame;
        
        long oneSecondAgo = thisFrame - 1_000_000_000L;
        for (int i = 0; i < frames.size(); i++)
        {
            long t = frames.get(i);
            if (t > oneSecondAgo)
            {
                frames.remove(0, i);
                break;
            }
        }
        frames.add(lastFrame);
    }
    
    public static double getDelta()
    {
        return delta;
    }
    
    public static long getDeltaNanos()
    {
        return deltaNanos;
    }
    
    public static long nanoTime()
    {
        return lastFrame;
    }
    
    public static int getFPS()
    {
        return frames.size();
    }
}





















