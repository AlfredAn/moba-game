package onlinegame.shared;

/**
 *
 * @author Alfred
 */
public final class Timer
{
    private long time;
    
    public Timer()
    {
        time = System.nanoTime();
    }
    
    public long getDelta()
    {
        long newTime = System.nanoTime();
        return newTime - time;
    }
    
    public long print()
    {
        return print("Timer");
    }
    public long print(String msg)
    {
        long delta = getDelta();
        Logger.log(msg + ": " + SharedUtil.getTimeString(delta));
        return delta;
    }
    
    public long reset()
    {
        long delta = print();
        resetNoPrint();
        return delta;
    }
    
    public long reset(String msg)
    {
        long delta = print(msg);
        resetNoPrint();
        return delta;
    }
    
    public void resetNoPrint()
    {
        time = System.nanoTime();
    }
}
