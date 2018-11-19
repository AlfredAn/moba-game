package onlinegame.shared.game.pathfinder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import java.util.PriorityQueue;
import onlinegame.shared.Logger;
import onlinegame.shared.MathUtil;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.game.GameMap;
import org.joml.Vector2d;

/**
 *
 * @author Alfred
 */
public final class Pathfinder
{
    public final GameMap map;
    
    private final double hitRadius;
    private final Graph graph;
    private final TIntObjectMap<Node> posNodeMap;
    //private final FloydWarshall floydWarshall;
    
    public Pathfinder(GameMap map, double hitRadius)
    {
        this.map = map;
        this.hitRadius = hitRadius;
        graph = genGraph();
        //floydWarshall = FloydWarshall.generate(graph);
        
        posNodeMap = new TIntObjectHashMap<>(graph.size() * 2 + 5, .5f, -1);
        for (int i = 0; i < graph.size(); i++)
        {
            Node n = graph.get(i);
            int x = (int)(n.x / map.scale);
            int y = (int)(n.y / map.scale);
            posNodeMap.put(x + y * map.width, n);
        }
    }
    
    private Graph genGraph()
    {
        Graph g = new Graph();
        
        double fixedRad = hitRadius - .00001;
        
        //add all nodes
        for (int y = 0; y < map.height; y++)
        {
            for (int x = 0; x < map.width; x++)
            {
                if (map.isWalkable(x, y)) continue;
                
                double x1 = (double)x * map.scale - hitRadius;
                double y1 = (double)y * map.scale - hitRadius;
                double x2 = (double)(x+1) * map.scale + hitRadius;
                double y2 = (double)(y+1) * map.scale + hitRadius;
                
                boolean cnw = map.isWalkable(x-1, y-1); //north-west
                boolean cw  = map.isWalkable(x-1, y  ); //west
                boolean csw = map.isWalkable(x-1, y+1); //south-west
                
                boolean cn  = map.isWalkable(x,   y-1); //north
              //boolean cc  = false;                    //center
                boolean cs  = map.isWalkable(x,   y+1); //south
                
                boolean cne = map.isWalkable(x+1, y-1); //north-east
                boolean ce  = map.isWalkable(x+1, y  ); //east
                boolean cse = map.isWalkable(x+1, y+1); //south-east
                
                //north-west
                if (cnw && cn && cw && !collisionBox(x1 - fixedRad, y1 - fixedRad, x1 + fixedRad, y1 + fixedRad))
                {
                    g.add(new Node(x1, y1));
                }
                //north-east
                if (cne && cn && ce && !collisionBox(x2 - fixedRad, y1 - fixedRad, x2 + fixedRad, y1 + fixedRad))
                {
                    g.add(new Node(x2, y1));
                }
                //south-west
                if (csw && cs && cw && !collisionBox(x1 - fixedRad, y2 - fixedRad, x1 + fixedRad, y2 + fixedRad))
                {
                    g.add(new Node(x1, y2));
                }
                //south-east
                if (cse && cs && ce && !collisionBox(x2 - fixedRad, y2 - fixedRad, x2 + fixedRad, y2 + fixedRad))
                {
                    g.add(new Node(x2, y2));
                }
            }
        }
        
        int numEdges = 0;
        
        //connect nodes
        for (int i = 0; i < g.size(); i++)
        {
            Node n1 = g.get(i);
            for (int j = 0; j < i; j++)
            {
                Node n2 = g.get(j);
                if (!collisionLine(n1.x, n1.y, n2.x, n2.y, fixedRad))
                {
                    n1.connect(n2);
                    numEdges++;
                }
            }
        }
        
        //check for unconnected nodes
        int op = 0;
        for (int i = 0; i < g.size(); i++)
        {
            Node n = g.get(i);
            if (n.numNeighbors() == 0)
            {
                g.remove(i);
                i--;
            }
        }
        
        Logger.log("Generated graph for " + map.name + ": " + g.size() + " nodes, " + numEdges + " edges.");
        
        return g;
    }
    
    private final Vector2d tempVec2 = new Vector2d();
    public boolean nearestFreeSpace(double x, double y, double maxDist, Vector2d dest)
    {
        if (hitRadius > 1)
        {
            throw new IllegalStateException("Find a better way to do this..... set the booleans in clampInsideCell based on surrounding walls");
        }
        
        double invscale = 1. / map.scale;
        x *= invscale;
        y *= invscale;
        int ix = MathUtil.floor(x);
        int iy = MathUtil.floor(y);
        
        double rad = hitRadius * invscale;
        double safeRad = rad - 0.00001;
        
        /*if (!collisionBoxUnscaled(x - safeRad, y - safeRad, x + safeRad, y + safeRad))
        {
            dest.x = x;
            dest.y = y;
            return true;
        }*/
        
        //System.out.println("----searching----");
        
        
        
        double fx = MathUtil.frac(x);
        double fy = MathUtil.frac(y);
        
        double bestX = 0, bestY = 0, bestDistSqr = Double.POSITIVE_INFINITY;
        double stopDist = Double.POSITIVE_INFINITY;
        
        int max = RadialOrder.count();
        for (int i = 0; i < max; i++)
        {
            int xx = RadialOrder.getX(i);
            int yy = RadialOrder.getY(i);
            double cd = RadialOrder.getDist(i);
            
            //System.out.println("(" + xx + ", " + yy + ")");
            if (cd >= stopDist)
            {
                //System.out.println("stopDist exceeded!");
                break;
            }
            int testx = xx + ix;
            int testy = yy + iy;
            if (map.isWalkable(testx, testy))
            {
                int dir = RadialOrder.getDir(i);
                double ucx = RadialOrder.getClosestX(fx, dir);
                double ucy = RadialOrder.getClosestY(fy, dir);
                //System.out.println("p: (" + ucx + ", " + ucy + ")");
                if (!clampInsideCell(ucx, ucy, testx, testy, rad, safeRad, tempVec2))
                {
                    //System.out.println("collision detected in clamp");
                    continue;
                }
                //System.out.println("f: (" + tempVec2.x + ", " + tempVec2.y + ")");
                double dx = xx + tempVec2.x - fx;
                double dy = yy + tempVec2.y - fy;
                double px = x + dx;
                double py = y + dy;
                if (collisionBoxUnscaled(px - safeRad, py - safeRad, px + safeRad, py + safeRad))
                {
                    //System.out.println("collision test failed!");
                    continue;
                }
                
                double dsqr = dx * dx + dy * dy;
                //System.out.println("dx: " + dx + ", dy: " + dy + ", dsqr: " + dsqr);
                if (dsqr < bestDistSqr)
                {
                    if (stopDist == Double.POSITIVE_INFINITY)
                    {
                        stopDist = cd + 1;
                        stopDist *= stopDist;
                    }
                    bestDistSqr = dsqr;
                    bestX = px;
                    bestY = py;
                    //System.out.println("New best!");
                }
            }
        }
        
        if (bestDistSqr >= maxDist * maxDist)
        {
            //System.out.println("false");
            return false;
        }
        
        dest.x = bestX * map.scale;
        dest.y = bestY * map.scale;
        //System.out.println("Given: (" + x*map.scale + ", " + y*map.scale + ")");
        //System.out.println("Found: (" + dest.x + ", " + dest.y + ")");
        return true;
    }
    
    private boolean clampInsideCell(double fx, double fy, int ix, int iy, double srad, double trad, Vector2d dest)
    {
        double mrad = 1.0 - srad;
        double mtrad = 1.0 - trad;
        
        boolean cnw = !map.isWalkable(ix-1, iy-1); //north-west
        boolean cw  = !map.isWalkable(ix-1, iy  ); //west
        boolean csw = !map.isWalkable(ix-1, iy+1); //south-west
        
        boolean cn  = !map.isWalkable(ix,   iy-1); //north
      //boolean cc  = false;                       //center
        boolean cs  = !map.isWalkable(ix,   iy+1); //south
        
        boolean cne = !map.isWalkable(ix+1, iy-1); //north-east
        boolean ce  = !map.isWalkable(ix+1, iy  ); //east
        boolean cse = !map.isWalkable(ix+1, iy+1); //south-east
        
        double tx, ty;
        
        tx = fx;
        ty = fy;
        if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
            (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
            (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
            (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
        {
            dest.set(tx, ty);
            return true;
        }
        
        if (fx < srad)
        {
            tx = srad;
            ty = fy;
            if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
            {
                dest.set(tx, ty);
                return true;
            }
            if (fy < srad)
            {
                tx = fx;
                ty = srad;
                if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                    (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                    (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                    (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
                {
                    dest.set(tx, ty);
                    return true;
                }
                
                tx = srad;
                ty = srad;
                if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                    (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                    (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                    (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
                {
                    dest.set(tx, ty);
                    return true;
                }
            }
            if (fy > mrad)
            {
                tx = fx;
                ty = mrad;
                if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                    (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                    (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                    (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
                {
                    dest.set(tx, ty);
                    return true;
                }
                
                tx = srad;
                ty = mrad;
                if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                    (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                    (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                    (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
                {
                    dest.set(tx, ty);
                    return true;
                }
            }
        }
        if (fx > mrad)
        {
            tx = mrad;
            ty = fy;
            if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
            {
                dest.set(tx, ty);
                return true;
            }
            if (fy < srad)
            {
                tx = fx;
                ty = srad;
                if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                    (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                    (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                    (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
                {
                    dest.set(tx, ty);
                    return true;
                }
                
                tx = mrad;
                ty = srad;
                if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                    (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                    (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                    (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
                {
                    dest.set(tx, ty);
                    return true;
                }
            }
            if (fy > mrad)
            {
                tx = fx;
                ty = mrad;
                if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                    (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                    (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                    (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
                {
                    dest.set(tx, ty);
                    return true;
                }
                
                tx = mrad;
                ty = mrad;
                if ((tx >= trad  || (!cw && (!cnw || ty >= trad) && (!csw || ty <= mtrad))) &&
                    (tx <= mtrad || (!ce && (!cne || ty >= trad) && (!cse || ty <= mtrad))) &&
                    (ty >= trad  || (!cn && (!cnw || tx >= trad) && (!cne || tx <= mtrad))) &&
                    (ty <= mtrad || (!cs && (!csw || tx >= trad) && (!cse || tx <= mtrad))))
                {
                    dest.set(tx, ty);
                    return true;
                }
            }
        }
        
        
        
        /*if (testOff(fx, fy, dix, diy, trad, dest)) return true; //test input pos
        
        if (testOff(fx, srad, dix, diy, trad, dest)) return true; //test input x and min/max y
        if (testOff(fx, mrad, dix, diy, trad, dest)) return true;
        
        if (testOff(srad, fy, dix, diy, trad, dest)) return true;
        if (testOff(mrad, fy, dix, diy, trad, dest)) return true;
        
        if (testOff(srad, srad, dix, diy, trad, dest)) return true;
        if (testOff(srad, mrad, dix, diy, trad, dest)) return true;
        if (testOff(mrad, srad, dix, diy, trad, dest)) return true;
        if (testOff(mrad, mrad, dix, diy, trad, dest)) return true;*/
        
        return false;
    }
    private boolean testOff(double fx, double fy, double ix, double iy, double rad, Vector2d dest)
    {
        double x = ix + fx;
        double y = iy + fy;
        if (collisionBoxUnscaled(x - rad, y - rad, x + rad, y + rad))
        {
            return false;
        }
        else
        {
            dest.x = fx;
            dest.y = fy;
            return true;
        }
    }
    /*private boolean clampInsideCell(double fx, double fy, int ix, int iy, double setrad, double testrad, Vector2d dest)
    {
        double oneminusrad = 1.0 - setrad;
        double oneminustestrad = 1.0 - testrad;
        
        boolean cnw = !map.isWalkable(ix-1, iy-1); //north-west
        boolean cw  = !map.isWalkable(ix-1, iy  ); //west
        boolean csw = !map.isWalkable(ix-1, iy+1); //south-west
        
        boolean cn  = !map.isWalkable(ix,   iy-1); //north
      //boolean cc  = false;                       //center
        boolean cs  = !map.isWalkable(ix,   iy+1); //south
        
        boolean cne = !map.isWalkable(ix+1, iy-1); //north-east
        boolean ce  = !map.isWalkable(ix+1, iy  ); //east
        boolean cse = !map.isWalkable(ix+1, iy+1); //south-east
        
        int xmove = 0, ymove = 0;
        
        //clamp from west
        if (fx < testrad && (cw || (cnw && fy < testrad) || (csw && fy > oneminustestrad)))
        {
            //fx = setrad;
            xmove = 1;
        }
        //clamp from east
        else if (fx > oneminustestrad && (ce || (cne && fy < testrad) || (cse && fy > oneminustestrad)))
        {
            //fx = oneminusrad;
            if (xmove != 0)
                xmove = 2;
            else
                xmove = -1;
        }
        
        //clamp from north
        if (fy < testrad && (cn || (cnw && fx < testrad) || (cne && fx > oneminustestrad)))
        {
            //fy = setrad;
            ymove = 1;
        }
        //clamp from south
        if (fy > oneminustestrad && (cs || (csw && fx < testrad) || (cse && fx > oneminustestrad)))
        {
            //fy = oneminusrad;
            if (ymove != 0)
                ymove = 2;
            else
                ymove = -1;
        }
        
        if (xmove == 2 && ymove == 2)
        {
            return false;
        }
        else if (xmove == 2)
        {
            if (ymove == 1)
            {
                fy = setrad;
            }
            else if (ymove == -1)
            {
                fy = oneminusrad;
            }
            if (ymove != 0)
            {
                //clamp from west
                if (fx < testrad && (cw || (cnw && fy < testrad) || (csw && fy > oneminustestrad)))
                {
                    //fx = setrad;
                    xmove = 1;
                }
                //clamp from east
                else if (fx > oneminustestrad && (ce || (cne && fy < testrad) || (cse && fy > oneminustestrad)))
                {
                    //fx = oneminusrad;
                    if (xmove != 0)
                        xmove = 2;
                    else
                        xmove = -1;
                }
                if (xmove == 2)
                {
                    return false;
                }
                else if (xmove == 1)
                {
                    fx = setrad;
                }
                else if (xmove == -1)
                {
                    fx = oneminusrad;
                }
            }
        }
        else if (ymove == 2)
        {
            if (xmove == 1)
            {
                fx = setrad;
            }
            else if (xmove == -1)
            {
                fx = oneminusrad;
            }
            if (xmove != 0)
            {
                //clamp from north
                if (fy < testrad && (cn || (cnw && fx < testrad) || (cne && fx > oneminustestrad)))
                {
                    //fy = setrad;
                    ymove = 1;
                }
                //clamp from south
                if (fy > oneminustestrad && (cs || (csw && fx < testrad) || (cse && fx > oneminustestrad)))
                {
                    //fy = oneminusrad;
                    if (ymove != 0)
                        ymove = 2;
                    else
                        ymove = -1;
                }
                if (ymove == 2)
                {
                    return false;
                }
                else if (ymove == 1)
                {
                    fy = setrad;
                }
                else if (ymove == -1)
                {
                    fy = oneminusrad;
                }
            }
        }
        else
        {
            if (xmove == 1)
            {
                fx = setrad;
            }
            else if (xmove == -1)
            {
                fx = oneminusrad;
            }
            if (ymove == 1)
            {
                fy = setrad;
            }
            else
            {
                fy = oneminusrad;
            }
        }
        
        dest.x = fx;
        dest.y = fy;
        
        return true;
    }*/
    
    private boolean collisionBoxUnscaled(double x1, double y1, double x2, double y2)
    {
        int ix1 = MathUtil.floor(x1);
        int iy1 = MathUtil.floor(y1);
        int ix2 = MathUtil.floor(x2);
        int iy2 = MathUtil.floor(y2);
        
        if (ix1 < 0 || iy1 < 0 || ix2 >= map.width || iy2 >= map.height)
        {
            return true;
        }
        
        for (int x = ix1; x <= ix2; x++)
        {
            for (int y = iy1; y <= iy2; y++)
            {
                if (!map.isWalkableFast(x, y)) return true;
            }
        }
        return false;
    }
    
    public boolean collisionBox(double x1, double y1, double x2, double y2)
    {
        double invscale = 1. / map.scale;
        int ix1 = MathUtil.floor(x1 * invscale);
        int iy1 = MathUtil.floor(y1 * invscale);
        int ix2 = MathUtil.floor(x2 * invscale);
        int iy2 = MathUtil.floor(y2 * invscale);
        
        if (ix1 < 0 || iy1 < 0 || ix2 >= map.width || iy2 >= map.height)
        {
            return true;
        }
        
        for (int x = ix1; x <= ix2; x++)
        {
            for (int y = iy1; y <= iy2; y++)
            {
                if (!map.isWalkableFast(x, y)) return true;
            }
        }
        return false;
    }
    
    public boolean collisionLine(double x1, double y1, double x2, double y2, double radius)
    {
        ///can probably be optimized using quadtrees
        
        if (radius == 0)
        {
            return collisionLine(x1, y1, x2, y1);
        }
        
        double scw = map.getScaledWidth(), sch = map.getScaledHeight();
        if (x1 < 0 || y1 < 0 || x1 >= scw || y1 >= sch
         || x2 < 0 || y2 < 0 || x2 >= scw || y2 >= sch)
        {
            return true;
        }
        
        radius = Math.abs(radius);
        
        double dx = x2 - x1;
        double dy = y2 - y1;
        
        double l1x1, l1y1, l1x2, l1y2; //first line
        double l2x1, l2y1, l2x2, l2y2; //middle line
        double l3x1, l3y1, l3x2, l3y2; //last line
        
        //if slope is negative (towards top right)
        if (dx * dy < 0)
        {
            //top left points
            l1x1 = x1 - radius;
            l1y1 = y1 - radius;
            l1x2 = x2 - radius;
            l1y2 = y2 - radius;
            
            //bottom right points
            l3x1 = x1 + radius;
            l3y1 = y1 + radius;
            l3x2 = x2 + radius;
            l3y2 = y2 + radius;
            
            if (dx > 0) //left to right
            {
                //bottom left start, top right end
                l2x1 = x1 - radius;
                l2y1 = y1 + radius;
                l2x2 = x2 + radius;
                l2y2 = y2 - radius;
            }
            else //right to left
            {
                //top right start, bottom left end
                l2x1 = x1 + radius;
                l2y1 = y1 - radius;
                l2x2 = x2 - radius;
                l2y2 = y2 + radius;
            }
        }
        else //slope is positive (towards bottom right)
        {
            //top right points
            l1x1 = x1 + radius;
            l1y1 = y1 - radius;
            l1x2 = x2 + radius;
            l1y2 = y2 - radius;
            
            //bottom left points
            l3x1 = x1 - radius;
            l3y1 = y1 + radius;
            l3x2 = x2 - radius;
            l3y2 = y2 + radius;
            
            if (dx > 0) //left to right
            {
                //top left start, bottom right end
                l2x1 = x1 - radius;
                l2y1 = y1 - radius;
                l2x2 = x2 + radius;
                l2y2 = y2 + radius;
            }
            else //right to left
            {
                //bottom right start, top left end
                l2x1 = x1 + radius;
                l2y1 = y1 + radius;
                l2x2 = x2 - radius;
                l2y2 = y2 - radius;
            }
        }
        
        int numLines = Math.max((int)Math.ceil(2 * radius / map.scale) + 1, 3);
        if (numLines % 2 == 0)
        {
            //force numLines to be odd
            numLines++;
        }
        
        double df = 1. / (numLines-1);
        for (int i = 0; i < numLines; i++)
        {
            double f = df * i;
            double lx1 = MathUtil.lerp3(l1x1, l2x1, l3x1, f);
            double ly1 = MathUtil.lerp3(l1y1, l2y1, l3y1, f);
            double lx2 = MathUtil.lerp3(l1x2, l2x2, l3x2, f);
            double ly2 = MathUtil.lerp3(l1y2, l2y2, l3y2, f);
            
            if (collisionLine(lx1, ly1, lx2, ly2)) return true;
        }
        
        return false;
    }
    
    public boolean collisionLine(double x1, double y1, double x2, double y2)
    {
        return CellTraversal.collisionLine(map, x1, y1, x2, y2);
    }
    
    private final PriorityQueue<Node> frontier = new PriorityQueue<>();
    private final THashSet<Node> frontierSet = new THashSet<>();
    
    public Node[] findPath(double startX, double startY, double goalX, double goalY)
    {
        int raycasts = 1;
        long startTime = System.nanoTime();
        
        double fixedrad = hitRadius - .00001;
        if (
                collisionBox(startX - fixedrad, startY - fixedrad, startX + fixedrad, startY + fixedrad) ||
                collisionBox(goalX - fixedrad, goalY - fixedrad, goalX + fixedrad, goalY + fixedrad))
        {
            //start or goal is blocked
            return null;
        }
        
        Graph g = graph;
        Node start = new Node(startX, startY, g.size());
        Node goal = new Node(goalX, goalY, 0);
        
        if (!collisionLine(startX, startY, goalX, goalY, fixedrad))
        {
            //goal can be directly reached from start
            return new Node[] {start, goal};
        }
        
        for (int i = 0; i < g.size(); i++)
        {
            Node n = g.get(i);
            
            start.addNeighbor(n, MathUtil.dist(startX, startY, n.x, n.y));
            
            //init node
            n.visited = false;
            n.cost = Double.POSITIVE_INFINITY;
            n.costIfStartBlocked = Double.POSITIVE_INFINITY;
            n.estCostIfStartBlocked = Double.POSITIVE_INFINITY;
            n.checkedVisibilityFromStart = false;
            n.parentIfStartBlocked = null;
        }
        
        //init start and goal nodes
        //start.cost = 0;
        start.pathNodes = 1;
        goal.cost = Double.POSITIVE_INFINITY;
        goal.costIfStartBlocked = Double.POSITIVE_INFINITY;
        goal.estCostIfStartBlocked = Double.POSITIVE_INFINITY;
        
        //init open set
        frontier.clear();
        frontier.add(start);
        frontierSet.clear();
        frontierSet.add(start);
        
        //return value, null if no path was found
        Node[] result = null;
        
        while (!frontier.isEmpty())
        {
            Node current = frontier.poll();
            frontierSet.remove(current);
            
            if (current.visited)
            {
                continue;
            }
            
            if (current == goal)
            {
                //goal found, construct the path
                result = new Node[goal.pathNodes];
                
                Node n = goal;
                int i = result.length - 1;
                result[i] = goal;
                
                while ((n = n.parent) != null)
                {
                    i--;
                    result[i] = n;
                }
                if (i != 0) throw new Error("Pathfinder error, path nodes != goal.pathNodes (goal.pathNodes=" + goal.pathNodes + ", i=" + i + ")");
                break;
            }
            else if (current.parent == start && !current.checkedVisibilityFromStart)
            {
                current.checkedVisibilityFromStart = true;
                
                raycasts++;
                if (collisionLine(start.x, start.y, current.x, current.y, fixedrad))
                {
                    if (current.parentIfStartBlocked == null)
                    {
                        current.cost = Double.POSITIVE_INFINITY;
                        current.estCost = Double.POSITIVE_INFINITY;
                    }
                    else
                    {
                        current.cost = current.costIfStartBlocked;
                        current.costIfStartBlocked = Double.POSITIVE_INFINITY;
                        current.estCost = current.estCostIfStartBlocked;
                        current.estCostIfStartBlocked = Double.POSITIVE_INFINITY;
                        current.parent = current.parentIfStartBlocked;
                        current.pathNodes = current.pathNodesIfStartBlocked;
                        frontier.add(current);
                        frontierSet.add(current);
                    }
                    continue;
                }
            }
            
            current.visited = true;
            
            //calculate goal cost and stuff
            raycasts++;
            if (!collisionLine(current.x, current.y, goal.x, goal.y, fixedrad))
            {
                double goalCost = current.cost + MathUtil.dist(current.x, current.y, goal.x, goal.y);
                if (goalCost < goal.cost)
                {
                    goal.cost = goalCost;
                    goal.estCost = goalCost;
                    goal.parent = current;
                    goal.pathNodes = current.pathNodes + 1;
                    if (!frontierSet.add(goal))
                    {
                        frontier.remove(goal);
                    }
                    frontier.add(goal);
                    /*if (!frontier.contains(goal))
                    {
                        frontier.add(goal);
                    }*/
                }
            }
            
            int numNeigbors = current.numNeighbors();
            for (int i = 0; i < numNeigbors; i++)
            {
                Node next = current.getNeighbor(i);
                
                if (!next.visited)
                {
                    double newCost = current.cost + current.getNeighborCost(i);
                    
                    if (next.parent == start && !next.checkedVisibilityFromStart)
                    {
                        if (newCost < next.costIfStartBlocked)
                        {
                            next.costIfStartBlocked = newCost;
                            next.estCostIfStartBlocked = newCost + heuristic(next, goal);
                            next.parentIfStartBlocked = current;
                            next.pathNodesIfStartBlocked = current.pathNodes + 1;
                            if (!frontierSet.add(next))
                            {
                                frontier.remove(next);
                            }
                            frontier.add(next);
                            /*if (!frontier.contains(next))
                            {
                                frontier.add(next);
                            }*/
                        }
                        /*next.checkedVisibilityFromStart = true;
                        if (collisionLine(start.x, start.y, next.x, next.y, fixedrad))
                        {
                            next.cost = Double.POSITIVE_INFINITY;
                            next.parent = null;
                        }*/
                    }
                    else if (newCost < next.cost)
                    {
                        next.cost = newCost;
                        next.estCost = newCost + heuristic(next, goal);
                        next.parent = current;
                        next.pathNodes = current.pathNodes + 1;
                        if (!frontierSet.add(next))
                        {
                            frontier.remove(next);
                        }
                        frontier.add(next);
                        /*if (!frontier.contains(next))
                        {
                            frontier.add(next);
                        }*/
                    }
                }
            }
        }
        
        //cleanup
        frontier.clear();
        frontierSet.clear();
        
        long delta = System.nanoTime() - startTime;
        Logger.log("Pathfinding time: " + SharedUtil.getTimeString(delta) + ", raycasts: " + raycasts);
        
        return result;
    }
    
    private double heuristic(Node first, Node second)
    {
        return MathUtil.dist(first.x, first.y, second.x, second.y);
    }
    
    /*private double floydWarshallHeuristic(Node first, Node goal, Node closestToGoal, double minGoalDist)
    {
        double floyd = floydWarshall.getDistance(first, closestToGoal) - minGoalDist;
        double floydSqr = floyd * floyd;
        
        double distSqr = MathUtil.distSqr(first.x, first.y, goal.x, goal.y);
        if (distSqr > floydSqr)
        {
            return Math.sqrt(distSqr);
        }
        else
        {
            return floyd;
        }
    }*/
}
