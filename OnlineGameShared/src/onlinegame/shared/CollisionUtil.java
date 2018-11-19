package onlinegame.shared;

/**
 *
 * @author Alfred
 */
public final class CollisionUtil
{
    private CollisionUtil() {}
    
    public static boolean pointBoxCollision(double px, double py, double x1, double y1, double x2, double y2)
    {
        return px >= x1 && py >= y1 && px <= x2 && py <= y2;
    }
    
    public static boolean rayBox(
            double rx, double ry, double rdx, double rdy,
            double bx1, double by1, double bx2, double by2)
    {
        Logger.logError("CollisionUtil.rayBox is untested!");
        double invx = 1 / rdx;
        double tx1 = (bx1 - rx) * invx;
        double tx2 = (bx2 - rx) * invx;
        
        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);
        
        double invy = 1 / rdy;
        double ty1 = (by1 - ry) * invy;
        double ty2 = (by2 - ry) * invy;
        
        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));
        
        return tmax >= tmin;
    }
    
    public static double rayBoxDist(
            double rx0, double ry0, double rdx, double rdy,
            double bx1, double by1, double bx2, double by2)
    {
        return Math.sqrt(rayBoxDistSqr(rx0, ry0, rdx, rdy, bx1, by1, bx2, by2));
    }
    public static double rayBoxDistSqr(
            double rx0, double ry0, double rdx, double rdy,
            double bx1, double by1, double bx2, double by2)
    {
        Logger.logError("CollisionUtil.rayBoxDistSqr is untested!");
        double invx = 1 / rdx;
        double tx1 = (bx1 - rx0) * invx;
        double tx2 = (bx2 - rx0) * invx;
        
        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);
        
        double invy = 1 / rdy;
        double ty1 = (by1 - ry0) * invy;
        double ty2 = (by2 - ry0) * invy;
        
        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));
        
        if (tmax >= tmin && tmax >= 0)
        {
            double intersectX, intersectY;
            if (tmin < 0)
            {
                intersectX = tmax * rdx;
                intersectY = tmax * rdy;
            }
            else
            {
                intersectX = tmin * rdx;
                intersectY = tmin * rdy;
            }
            //actualIntersectX = intersectX + rx0;
            //actualIntersectY = intersectY + ry0;
            return intersectX * intersectX + intersectY * intersectY;
        }
        else
        {
            return Double.POSITIVE_INFINITY;
        }
    }
    
    public static boolean boxBoxCollision(
            double b1x1, double b1y1, double b1x2, double b1y2,
            double b2x1, double b2y1, double b2x2, double b2y2)
    {
        return (b1x1 <= b2x2 &&
                b1x2 >= b2x1 &&
                b1y1 <= b2y2 &&
                b1y2 >= b2y1);
    }
    
    //http://tavianator.com/fast-branchless-raybounding-box-intersections/
    public static boolean rayBox3D(
            double bx1, double by1, double bz1,
            double bx2, double by2, double bz2,
            double lx0, double ly0, double lz0,
            double ldx, double ldy, double ldz)
    {
        double invdx = 1. / ldx;
        double tx1 = (bx1 - lx0) * invdx;
        double tx2 = (bx2 - lx0) * invdx;
        
        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);
        
        double invdy = 1. / ldy;
        double ty1 = (by1 - ly0) * invdy;
        double ty2 = (by2 - ly0) * invdy;
        
        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));
        
        double invdz = 1. / ldz;
        double tz1 = (bz1 - lz0) * invdz;
        double tz2 = (bz2 - lz0) * invdz;
        
        tmin = Math.max(tmin, Math.min(tz1, tz2));
        tmax = Math.min(tmax, Math.max(tz1, tz2));
        
        return tmax >= tmin;
    }
    
    public static double rayBox3DDistSqr(
            double bx1, double by1, double bz1,
            double bx2, double by2, double bz2,
            double lx0, double ly0, double lz0,
            double ldx, double ldy, double ldz)
    {
        double invdx = 1. / ldx;
        double tx1 = (bx1 - lx0) * invdx;
        double tx2 = (bx2 - lx0) * invdx;
        
        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);
        
        double invdy = 1. / ldy;
        double ty1 = (by1 - ly0) * invdy;
        double ty2 = (by2 - ly0) * invdy;
        
        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));
        
        double invdz = 1. / ldz;
        double tz1 = (bz1 - lz0) * invdz;
        double tz2 = (bz2 - lz0) * invdz;
        
        tmin = Math.max(tmin, Math.min(tz1, tz2));
        tmax = Math.min(tmax, Math.max(tz1, tz2));
        
        if (tmax >= tmin)
        {
            if (tmin >= 0)
            {
                double ix = tmin * ldx;
                double iy = tmin * ldy;
                double iz = tmin * ldz;
                return ix * ix + iy * iy + iz * iz;
            }
            else
            {
                double ix = tmax * ldx;
                double iy = tmax * ldy;
                double iz = tmax * ldz;
                return ix * ix + iy * iy + iz * iz;
            }
        }
        return Double.POSITIVE_INFINITY;
    }
}
