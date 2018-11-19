package onlinegame.shared;

/**
 *
 * @author Alfred
 */
public final class FrameTimer
{
    //femtoseconds used as time unit
    
    private static final long SECOND = 1_000_000_000_000_000L;
    private static final long MILLISECOND = SECOND / 1_000L;
    private static final long NANOSECOND = SECOND / 1_000_000_000L;
    
    private double targetFPS;
    private long targetFrameTime;
    
    private long lastFrame;
    private long frameCount;
    
    private long delta;
    
    private int accuracy;
    
    public static final int
            LOW = 0,
            MEDIUM = 1,
            HIGH = 2,
            HIGHER = 3,
            VERY_HIGH = 4,
            MAX = 5;
    
    /*public static void main(String[] args)
    {
        FrameTimer ft = new FrameTimer(60, MEDIUM);
        
        long time = System.nanoTime();
        long expectedDelta = 1_000_000_000L / 60;
        Random r = new Random();
        long accErr = 0;
        
        while (true)
        {
            ft.update();
            
            long newTime = System.nanoTime();
            long delta = newTime - time;
            time = newTime;
            
            long error = delta - expectedDelta;
            accErr += error;
            
            System.out.println("delta: " + SharedUtil.getTimeString(delta));
            System.out.println("accErr: " + SharedUtil.getTimeString(accErr));
            System.out.println("----");
            
            try
            {
                Thread.sleep(12 + r.nextInt(8));
            }
            catch (InterruptedException e) {}
        }
        
        /*long totalError = 0;
        long absError = 0;
        long maxError = 0;
        
        long i = 0;
        
        while (true)
        {
            i++;
            ft.update();
            
            long newTime = System.nanoTime();
            long delta = newTime - time;
            
            long error = delta - expectedDelta;
            totalError += error;
            absError += Math.abs(error);
            maxError = Math.max(maxError, Math.abs(error));
            
            Logger.log("dt: " + SharedUtil.getTimeString(delta) + ", error: " + SharedUtil.getTimeString(error));
            Logger.log("tErr: " + SharedUtil.getTimeString(totalError) + ", avgErr: " + SharedUtil.getTimeString(absError / i) + ", maxErr: " + SharedUtil.getTimeString(maxError));
            time = newTime;
        }*/
    //}*/
    
    public FrameTimer()
    {
        this(0);
    }
    public FrameTimer(double targetFramerate)
    {
        this(targetFramerate, MEDIUM);
    }
    public FrameTimer(double targetFramerate, int accuracy)
    {
        setTargetFramerate(targetFramerate);
        setAccuracy(accuracy);
        
        reset();
    }
    
    public void reset()
    {
        lastFrame = System.nanoTime() * NANOSECOND;
        frameCount = 0;
    }
    
    public void setAccuracy(int accuracy)
    {
        if (accuracy < LOW || accuracy > MAX)
        {
            throw new IllegalArgumentException();
        }
        this.accuracy = accuracy;
    }
    
    public int getAccuracy()
    {
        return accuracy;
    }
    
    public void update()
    {
        long startTime = System.nanoTime() * NANOSECOND;
        long newTime = startTime;
        delta = newTime - lastFrame;
        
        waitLoop: while (delta < targetFrameTime)
        {
            long sleepTime = targetFrameTime - delta;
            
            switch (accuracy)
            {
                case LOW:
                    if (sleepTime < MILLISECOND)
                    {
                        break waitLoop;
                    }
                    break;
                case MEDIUM:
                    break;
                case HIGH:
                    sleepTime -= MILLISECOND;
                    break;
                case HIGHER:
                    sleepTime -= MILLISECOND + MILLISECOND / 2;
                    break;
                case VERY_HIGH:
                    sleepTime -= MILLISECOND * 2;
                    if (sleepTime <= 0)
                    {
                        //busy-wait to avoid overshooting
                        newTime = System.nanoTime() * NANOSECOND;
                        delta = newTime - lastFrame;
                        continue;
                    }
                    break;
                case MAX:
                    //busy-wait
                    newTime = System.nanoTime() * NANOSECOND;
                    delta = newTime - lastFrame;
                    continue;
            }
            sleepTime = Math.max(sleepTime / MILLISECOND, 0); //convert time to milliseconds and clamp
            
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {}
            newTime = System.nanoTime() * NANOSECOND;
            delta = newTime - lastFrame;
        }
        
        if (delta < targetFrameTime*2)
        {
            lastFrame += targetFrameTime;
            delta = targetFrameTime;
        }
        else
        {
            //falling too much behind, reset the frame timer
            lastFrame = startTime;
        }
        
        frameCount++;
    }
    
    public void setTargetFramerate(double fps)
    {
        if (fps <= 0)
        {
            targetFPS = 0;
            targetFrameTime = 0;
        }
        else
        {
            targetFPS = fps;
            targetFrameTime = Math.round(SECOND / fps);
        }
    }
    
    public double getTargetFramerate()
    {
        return targetFPS;
    }
    
    public long getTargetFrametimeNanos()
    {
        return targetFrameTime / NANOSECOND;
    }
    
    public double getTargetFrametime()
    {
        return ((double)targetFrameTime) / SECOND;
    }
    
    public long getDeltaNanos()
    {
        return delta / NANOSECOND;
    }
    
    public double getDelta()
    {
        return ((double)delta) / SECOND;
    }
    
    public long getFrameCount()
    {
        return frameCount;
    }
}
