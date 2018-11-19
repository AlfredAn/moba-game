package onlinegame.shared;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Alfred
 */
public final class Geometry
{
    private Geometry() {}
    
    public static void planePlaneIntersect(Vector4f plane1, Vector4f plane2, Vector3f destLineStart, Vector3f destLineDirection)
    {
        planePlaneIntersect(
                plane1.x, plane1.y, plane1.z, plane1.w,
                plane2.x, plane2.y, plane2.z, plane2.w,
                destLineStart, destLineDirection);
    }
    
    public static void planePlaneIntersect(
            float p1a, float p1b, float p1c, float p1d,
            float p2a, float p2b, float p2c, float p2d,
                    Vector3f destLineStart, Vector3f destLineDirection)
    {
        //choose two arbitrary points on the first plane
        float x1, y1, z1, x2, y2, z2;
        if (p1c != 0)
        {
            x1 = 0;
            y1 = 0;
            z1 = -p1d / p1c;

            x2 = 1;
            y2 = 0;
            z2 = -(p1a + p1d) / p1c;
        }
        else if (p1b != 0)
        {
            x1 = 0;
            y1 = 0;
            z1 = 0;

            x2 = 1;
            y2 = -p1a / p1d;
            z2 = 0;
        }
        else if (p1a != 0)
        {
            x1 = 0;
            y1 = 0;
            z1 = 0;

            x2 = 0;
            y2 = 1;
            z2 = 0;
        }
        else
        {
            throw new IllegalArgumentException("Invalid plane equation (A==B==C==0)");
        }
        
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        
        /*line equations (parametric):
        x = x1 + dx*t
        y = y1 + dy*t
        z = z1 + dz*t
        */
        
        //intersect line with second plane
        float t = (-p2a*x1 - p2b*y1 - p2c*z1 - p2d) / (p2a*dx + p2b*dy + p2c*dz);
        
        //find intersection point
        float ix = x1 + dx * t;
        float iy = y1 + dy * t;
        float iz = z1 + dz * t;
        
        //cross product of plane normals (direction of intersection line)
        float cx = p1b * p2c - p1c * p2b;
        float cy = p1c * p2a - p1a * p2c;
        float cz = p1a * p2b - p1b * p2a;
        
        destLineStart.set(ix, iy, iz);
        destLineDirection.set(cx, cy, cz);
    }
}
