package onlinegame.shared;

/**
 *
 * @author Alfred
 */
public final class GameUtil
{
    private GameUtil() {}
    
    /*
            SERVER_TICKRATE = 600,
            SERVER_SEND_INTERVAL = 10,
            SERVER_SAFE_INTERVAL = 60, //must be a multiple of SEND_INTERVAL
            SERVER_PATH_UPDATE_INTERVAL = 20;
    */
    
    public static final int
            SERVER_TICKRATE = 600,
            SERVER_SEND_INTERVAL = 10,
            SERVER_SAFE_INTERVAL = 10, //must be a multiple of SEND_INTERVAL
            SERVER_PATH_UPDATE_INTERVAL = 20;
    
    public static final double
            SERVER_TICK_DELTA = 1f / SERVER_TICKRATE,
            SERVER_CMD_MAX_QUEUE_TIME = 2.000f; //seconds
    
    private static final String[] teamName =
    {
        "Blue Team",
        "Red Team"
    };
    
    public static String getTeamName(int team)
    {
        return teamName[team];
    }
    
    public static double ticksToSeconds(int ticks)
    {
        return ticks * SERVER_TICK_DELTA;
    }
    
    public static int secondsToTicks(double seconds)
    {
        return (int)Math.floor(seconds * SERVER_TICKRATE + .000001);
    }
}
