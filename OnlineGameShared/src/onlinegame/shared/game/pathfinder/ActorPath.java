package onlinegame.shared.game.pathfinder;

import java.io.IOException;
import java.util.Arrays;
import onlinegame.shared.GameUtil;
import onlinegame.shared.MathUtil;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.net.GameProtocolException;

/**
 *
 * @author Alfred
 */
public final class ActorPath
{
    private final float[] xPoints, yPoints;
    public final double startTime;
    public final double length;
    
    public ActorPath(float xPos, float yPos, double startTime)
    {
        this(new float[] {xPos}, new float[] {yPos}, startTime);
    }
    public ActorPath(float[] xPoints, float[] yPoints, double startTime)
    {
        if (xPoints.length != yPoints.length)
        {
            throw new IllegalArgumentException("xPoints and yPoints must be of equal length.");
        }
        else if (xPoints.length == 0)
        {
            throw new IllegalArgumentException("There must be at least one point.");
        }
        
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        
        this.startTime = startTime;
        length = calcLength();
    }
    
    public ActorPath(Node[] nodes, double startTime)
    {
        int len = nodes.length;
        xPoints = new float[len];
        yPoints = new float[len];
        
        for (int i = 0; i < len; i++)
        {
            xPoints[i] = (float)nodes[i].x;
            yPoints[i] = (float)nodes[i].y;
        }
        
        this.startTime = startTime;
        length = calcLength();
    }
    
    public ActorPath(BitInput in) throws IOException
    {
        int len = in.readInt(IntFormat.USHORTV);
        
        if (len == 0)
        {
            throw new GameProtocolException("There must be at least one point!");
        }
        
        xPoints = new float[len];
        yPoints = new float[len];
        
        for (int i = 0; i < len; i++)
        {
            xPoints[i] = in.readFloat();
            yPoints[i] = in.readFloat();
        }
        
        startTime = GameUtil.ticksToSeconds(in.readInt(GameProtocol.TICK_TIME_FORMAT)) + (double)(in.readInt(8) & 0xFF) / 256 * GameUtil.SERVER_TICK_DELTA;
        length = calcLength();
    }
    
    private double calcLength()
    {
        double len = 0;
        int num = numPoints();
        
        if (num == 0) return 0;
        
        double x1 = xPoints[0], y1 = yPoints[0];
        for (int i = 1; i < num; i++)
        {
            double x2 = xPoints[i], y2 = yPoints[i];
            len += MathUtil.dist(x1, y1, x2, y2);
            
            x1 = x2;
            y1 = y2;
        }
        
        return len;
    }
    
    public void write(BitOutput out) throws IOException
    {
        int len = numPoints();
        out.writeInt(len, IntFormat.USHORTV);
        
        for (int i = 0; i < len; i++)
        {
            out.writeFloat(xPoints[i]);
            out.writeFloat(yPoints[i]);
        }
        
        out.writeInt(GameUtil.secondsToTicks(startTime), GameProtocol.TICK_TIME_FORMAT);
        out.writeInt(((int)(startTime / GameUtil.SERVER_TICK_DELTA * 256)) % 256, 8); //write fractional part as an 8-bit integer
    }
    
    public int numPoints()
    {
        return xPoints.length;
    }
    
    public float getXPoint(int i)
    {
        return xPoints[i];
    }
    
    public float getYPoint(int i)
    {
        return yPoints[i];
    }
    
    public float getStartX()
    {
        return getXPoint(0);
    }
    
    public float getStartY()
    {
        return getYPoint(0);
    }
    
    public float getEndX()
    {
        return getXPoint(numPoints()-1);
    }
    
    public float getEndY()
    {
        return getYPoint(numPoints()-1);
    }
    
    private double cachedX = Float.NaN, cachedY = Float.NaN;
    private int cachedPrevPoint = -1;
    private double cachedPos = Float.NaN;
    
    public float getPathXPos(double time, double speed)
    {
        return getPathXPos((time - startTime) * speed);
    }
    
    public float getPathYPos(double time, double speed)
    {
        return getPathYPos((time - startTime) * speed);
    }
    
    public int getPathPrevPoint(double time, double speed)
    {
        return getPathPrevPoint((time - startTime) * speed);
    }
    
    public float getPathXPos(double pos)
    {
        if (cachedPos != pos)
        {
            calcPathPos(pos);
        }
        return (float)cachedX;
    }
    
    public float getPathYPos(double pos)
    {
        if (cachedPos != pos)
        {
            calcPathPos(pos);
        }
        return (float)cachedY;
    }
    
    public int getPathPrevPoint(double pos)
    {
        if (cachedPos != pos)
        {
            calcPathPos(pos);
        }
        return cachedPrevPoint;
    }
    
    //can be optimized by going in reverse if pos > length/2
    //or by pre-calculating the distances and using binary search
    private void calcPathPos(double pos)
    {
        cachedPos = (float)pos;
        
        int len = numPoints();
        
        if (len == 1 || pos <= 0)
        {
            //use first point
            cachedX = xPoints[0];
            cachedY = yPoints[0];
            cachedPrevPoint = 0;
            return;
        }
        else if (pos >= length)
        {
            //use last point
            int i = len-1;
            cachedX = xPoints[i];
            cachedY = yPoints[i];
            cachedPrevPoint = i;
            return;
        }
        
        double dist = 0;
        double x1 = xPoints[0], y1 = yPoints[0];
        
        for (int i = 1; i < len; i++)
        {
            double x2 = xPoints[i], y2 = yPoints[i];
            double segmentLength = MathUtil.dist(x1, y1, x2, y2);
            double newDist = dist + segmentLength;
            
            if (pos <= newDist)
            {
                double f = (pos - dist) / segmentLength;
                cachedX = MathUtil.lerp(x1, x2, f);
                cachedY = MathUtil.lerp(y1, y2, f);
                cachedPrevPoint = i-1;
                return;
            }
            
            dist = newDist;
            x1 = x2;
            y1 = y2;
        }
        
        cachedX = x1;
        cachedY = y1;
        cachedPrevPoint = len-1;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof ActorPath)) return false;
        
        ActorPath p = (ActorPath)o;
        return
                startTime == p.startTime &&
                length == p.length &&
                Arrays.equals(xPoints, p.xPoints) &&
                Arrays.equals(yPoints, p.yPoints);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 19 * hash + (int)(Double.doubleToLongBits(startTime) ^ (Double.doubleToLongBits(startTime) >>> 32));
        hash = 19 * hash + (int)(Double.doubleToLongBits(length) ^ (Double.doubleToLongBits(length) >>> 32));
        hash = 19 * hash + Arrays.hashCode(xPoints);
        hash = 19 * hash + Arrays.hashCode(yPoints);
        return hash;
    }
}
