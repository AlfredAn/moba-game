package onlinegame.shared.game.pathfinder;

import java.util.ArrayList;
import java.util.Collections;
import onlinegame.shared.MathUtil;

/**
 *
 * @author Alfred
 */
public final class RadialOrder
{
    private RadialOrder() {}
    
    private static final double maxDist = 50;
    private static final int count;
    private static final int[] x, y, distSqr;
    private static final double[] dist;
    private static final byte[] dir;
    
    public static double maxDist()
    {
        return maxDist;
    }
    
    public static int count()
    {
        return count;
    }
    
    public static int getX(int i)
    {
        return x[i];
    }
    
    public static int getY(int i)
    {
        return y[i];
    }
    
    public static int getDistSqr(int i)
    {
        return distSqr[i];
    }
    
    public static double getDist(int i)
    {
        return dist[i];
    }
    
    public static byte getDir(int i)
    {
        return dir[i];
    }
    
    public static double getClosestX(double fx, int dir)
    {
        switch (dir & Direction.XMASK)
        {
            case Direction.XMIN:
                return 1;
            case Direction.XMID:
                return fx;
            case Direction.XMAX:
                return 0;
        }
        throw new IllegalArgumentException("Invalid dir: 0b" + Integer.toBinaryString(dir));
    }
    
    public static double getClosestY(double fy, int dir)
    {
        switch (dir & Direction.YMASK)
        {
            case Direction.YMIN:
                return 1;
            case Direction.YMID:
                return fy;
            case Direction.YMAX:
                return 0;
        }
        throw new IllegalArgumentException("Invalid dir: 0b" + Integer.toBinaryString(dir));
    }
    
    static
    {
        int r = MathUtil.ceilPositive(maxDist);
        int rsqr = (int)(maxDist * maxDist);
        
        ArrayList<Coord> coordList = new ArrayList<>((2*r+1) * (2*r+1));
        
        for (int y = -r; y <= r; y++)
        {
            for (int x = -r; x <= r; x++)
            {
                int distSqr = x * x + y * y;
                if (distSqr <= rsqr)
                {
                    coordList.add(new Coord(x, y, distSqr));
                }
            }
        }
        
        Collections.sort(coordList);
        
        count = coordList.size();
        x = new int[count];
        y = new int[count];
        distSqr = new int[count];
        dist = new double[count];
        dir = new byte[count];
        
        for (int i = 0; i < count; i++)
        {
            Coord c = coordList.get(i);
            x[i] = c.x;
            y[i] = c.y;
            distSqr[i] = c.distSqr;
            dist[i] = Math.sqrt(c.distSqr);
            
            int xs = c.x < 0 ? Direction.XMIN : (c.x == 0 ? Direction.XMID : Direction.XMAX);
            int ys = c.y < 0 ? Direction.YMIN : (c.y == 0 ? Direction.YMID : Direction.YMAX);
            dir[i] = (byte)(xs | ys);
        }
    }
    
    private static final class Coord implements Comparable<Coord>
    {
        private final int x, y;
        private final int distSqr;
        
        private Coord(int x, int y, int distSqr)
        {
            this.x = x;
            this.y = y;
            this.distSqr = distSqr;
        }
        
        @Override
        public int compareTo(Coord c)
        {
            return Integer.compare(distSqr, c.distSqr);
        }
    }
}






















