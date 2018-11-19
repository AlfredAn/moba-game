package onlinegame.shared;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains methods for printing messages as well as error messages to the log.
 *
 * @author Alfred
 */
public final class Logger
{
    private Logger() {}
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    private static String format(String msg)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getCurrentTimeStamp()).append(": ");
        String whitespace = "\n" + SharedUtil.repeatChar(' ', sb.length());
        sb.append(msg.replace("\n", whitespace));
        return sb.toString();
    }
    
    /**
     * 
     * @return The current date and time, neatly formatted.
     */
    public static String getCurrentTimeStamp()
    {
        Date now = new Date();
        return getTimeStamp(now);
    }
    
    public static String getTimeStamp(long date)
    {
        Date d = new Date(date);
        return getTimeStamp(d);
    }
    
    public static String getTimeStamp(Date date)
    {
        return sdf.format(date);
    }
    
    /**
     * Prints a message to the standard output along with the current date and time.
     *
     * @param msg The message to log
     */
    public static void log(String msg)
    {
        System.out.println(format(msg));
    }
    
    /**
     * Prints a char to the standard output along with the current date and time.
     *
     * @param c The char to log
     */
    public static void log(char c)
    {
        System.out.println(getCurrentTimeStamp() + ": " + c);
    }
    
    /**
     * Prints a char array to the standard output along with the current date and time.
     *
     * @param c The char array to log
     */
    public static void log(char[] c)
    {
        System.out.println(format(String.valueOf(c)));
    }
    
    /**
     * Prints an object to the standard output along with the current date and time.
     *
     * @param o The object to log
     */
    public static void log(Object o)
    {
        System.out.println(format(String.valueOf(o)));
    }
    
    /**
     * Prints a byte to the standard output along with the current date and time.
     *
     * @param b The byte to log
     */
    public static void log(byte b)
    {
        System.out.println(getCurrentTimeStamp() + ": " + b);
    }
    
    /**
     * Prints a short to the standard output along with the current date and time.
     *
     * @param s The short to log
     */
    public static void log(short s)
    {
        System.out.println(getCurrentTimeStamp() + ": " + s);
    }
    
    /**
     * Prints an int to the standard output along with the current date and time.
     *
     * @param i The int to log
     */
    public static void log(int i)
    {
        System.out.println(getCurrentTimeStamp() + ": " + i);
    }
    
    /**
     * Prints a long to the standard output along with the current date and time.
     *
     * @param l The long to log
     */
    public static void log(long l)
    {
        System.out.println(getCurrentTimeStamp() + ": " + l);
    }
    
    /**
     * Prints a boolean to the standard output along with the current date and time.
     *
     * @param b The boolean to log
     */
    public static void log(boolean b)
    {
        System.out.println(getCurrentTimeStamp() + ": " + b);
    }
    
    /**
     * Prints a float to the standard output along with the current date and time.
     *
     * @param f The float to log
     */
    public static void log(float f)
    {
        System.out.println(getCurrentTimeStamp() + ": " + f);
    }
    
    /**
     * Prints a float to the standard output along with the current date and time.
     *
     * @param d The double to log
     */
    public static void log(double d)
    {
        System.out.println(getCurrentTimeStamp() + ": " + d);
    }
    
    /**
     * Prints an error message as well as the current date and time to the error log.
     *
     * @param msg The error message to log
     */
    public static void logError(String msg)
    {
        logError(msg, null);
    }
    
    /**
     * Prints a stack trace as well as the current date and time to the error log.
     *
     * @param t The {@link Throwable} to log the stack trace of
     */
    public static void logError(Throwable t)
    {
        logError(null, t);
    }
    
    /**
     * Prints an error message, a stack trace as well as the current date and time to the error log.
     *
     * @param msg The error message to log
     * @param t The {@link Throwable} to log the stack trace of
     */
    public static void logError(String msg, Throwable t)
    {
        System.err.print(getCurrentTimeStamp() + ": ");
        
        if (msg != null && !msg.isEmpty())
        {
            System.err.println(msg);
        }
        
        if (t != null)
        {
            t.printStackTrace(System.err);
        }
    }
}
