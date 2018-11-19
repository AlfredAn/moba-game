package onlinegame.shared.game.pathfinder;

import onlinegame.shared.MathUtil;
import onlinegame.shared.game.GameMap;

/**
 *
 * @author Alfred
 */
public final class CellTraversal
{
    private CellTraversal() {}
    
    public static boolean collisionLine(GameMap map, double x1, double y1, double x2, double y2)
    {
        double invscale = 1. / map.scale;
        x1 *= invscale;
        y1 *= invscale;
        x2 *= invscale;
        y2 *= invscale;
        
        int ix1 = MathUtil.floor(x1);
        int iy1 = MathUtil.floor(y1);
        int ix2 = MathUtil.floor(x2);
        int iy2 = MathUtil.floor(y2);
        
        if (ix1 < 0 || iy1 < 0 || ix1 >= map.width || iy1 >= map.height
         || ix2 < 0 || iy2 < 0 || ix2 >= map.width || iy2 >= map.height)
        {
            //out of bounds
            return true;
        }
        
        if (!map.isWalkableFast(ix1, iy1)) return true;
        
        if (iy1 == iy2)
        {
            if (ix1 == ix2)
            {
                //zero length line
                //xList.add(ix1);
                //yList.add(iy1);
                return false; //already tested first point above and it was walkable
            }
            
            //horizontal line
            if (ix2 > ix1)
            {
                for (int x = ix1+1; x <= ix2; x++)
                {
                    if (!map.isWalkableFast(x, iy1)) return true;
                    //xList.add(x);
                    //yList.add(iy1);
                }
            }
            else
            {
                for (int x = ix1-1; x >= ix2; x--)
                {
                    if (!map.isWalkableFast(x, iy1)) return true;
                    //xList.add(x);
                    //yList.add(iy1);
                }
            }
            return false;
        }
        if (ix1 == ix2)
        {
            //vertical line
            if (iy2 > iy1)
            {
                for (int y = iy1+1; y <= iy2; y++)
                {
                    if (!map.isWalkableFast(ix1, y)) return true;
                    //xList.add(ix1);
                    //yList.add(y);
                }
            }
            else
            {
                for (int y = iy1-1; y >= iy2; y--)
                {
                    if (!map.isWalkableFast(ix1, y)) return true;
                    //xList.add(ix1);
                    //yList.add(y);
                }
            }
            return false;
        }
        
        double dx = x2 - x1;
        double dy = y2 - y1;
        
        int signx = dx > 0 ? 1 : -1;
        int signy = dy > 0 ? 1 : -1;
        
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        
        double vx = 1. / dx;
        double vy = 1. / dy;
        
        double dxperdy = dx * vy;
        double dyperdx = dy * vx;
        
        int x = ix1;
        int y = iy1;
        double xrem = signx > 0 ? 1.0 - MathUtil.frac(x1) : MathUtil.frac(x1);
        double yrem = signy > 0 ? 1.0 - MathUtil.frac(y1) : MathUtil.frac(y1);
        
        //first point is already tested
        //if (!map.isWalkableFast(x, y)) return true;
        //xList.add(x);
        //yList.add(y);
        
        boolean reachedX = false, reachedY = false;
        while (!reachedX || !reachedY)
        {
            double xdist = xrem * vx;
            double ydist = yrem * vy;
            
            if (xdist < ydist)
            {
                x += signx;
                yrem -= xrem * dyperdx;
                xrem = 1.0;
                reachedX |= x == ix2;
            }
            else
            {
                y += signy;
                xrem -= yrem * dxperdy;
                yrem = 1.0;
                reachedY |= y == iy2;
            }
            
            if (!map.isWalkableFast(x, y)) return true;
            //xList.add(x);
            //yList.add(y);
        }
        
        return false;
    }
}
