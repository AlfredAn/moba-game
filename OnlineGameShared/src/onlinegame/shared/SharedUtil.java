package onlinegame.shared;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class SharedUtil
{
    private SharedUtil() {}
    
    public static final Random random = new Random();
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    public static String getCurrentTimeStamp()
    {
        Date now = new Date();
        return sdf.format(now);
    }
    
    public static String getTimeString(double seconds)
    {
        return getTimeString((long)(seconds * 1_000_000_000));
    }
    
    private static final long[] units = new long[]
    {
        1L, //nanoseconds
        1_000L, //microseconds
        1_000_000L, //milliseconds
        1_000_000_000L, //seconds
        60L * 1_000_000_000L, //minutes
        60L * 60L * 1_000_000_000L, //hours
        24L * 60L * 60L * 1_000_000_000L, //days
        7L * 24L * 6L * 60L * 1_000_000_000L, //weeks
        (long)(365.25 / 12L * 24L * 60L * 60L * 1_000_000_000L), //months
        (long)(365.25 * 24L * 60L * 60L * 1_000_000_000L) //years
    };
            
    private static final String[] unitNames = new String[]
    {
        "ns",
        "µs",
        "ms",
        "s",
        " min",
        "h",
        " days",
        " weeks",
        " months",
        " years",
    };
    
    private static final DecimalFormat fmt2dec =
            new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private static final DecimalFormat fmt1dec =
            new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private static final DecimalFormat fmt0dec =
            new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    
    public static String getTimeString(long nanos)
    {
        int unit = 0;
        double val;
        String sign = nanos < 0 ? "-" : "";
        nanos = Math.abs(nanos);
        
        for (int i = 0; i < units.length; i++)
        {
            if (units[i] <= nanos)
            {
                unit = i;
            }
        }
        
        val = (double)nanos / units[unit];
        DecimalFormat fmt;
        
        if (val >= 100)
        {
            fmt = fmt0dec;
        }
        else if (val >= 10)
        {
            fmt = fmt1dec;
        }
        else
        {
            fmt = fmt2dec;
        }
        
        return sign + fmt.format(val) + unitNames[unit];
    }
    
    public static String repeatChar(char c, int count)
    {
        if (count < 0)
        {
            throw new IllegalArgumentException("Negative count: " + count);
        }
        
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        
        return new String(chars);
    }
    
    public static String repeatString(String str, int count)
    {
        if (count < 0)
        {
            throw new IllegalArgumentException("Negative count: " + count);
        }
        
        StringBuilder sb = new StringBuilder(str.length() * count);
        
        for (int i = 0; i < count; i++)
        {
            sb.append(str);
        }
        
        return sb.toString();
    }
    
    public static void logErrorMsg(Throwable e)
    {
        Logger.logError(getErrorMsg(e));
    }
    
    public static String getErrorMsg(Throwable e)
    {
        StackTraceElement[] stackTrace = e.getStackTrace();
        String caller;
        if (stackTrace.length > 0)
        {
            caller = stackTrace[0].toString();
        }
        else
        {
            caller = "(unknown)";
        }
        
        String msg = e.getMessage();
        return caller + " - " + e.getClass().getSimpleName() + (msg == null ? "" : (": " + e.getMessage()));
    }
    
    public static String plural(int num)
    {
        return plural("s", num);
    }
    public static String plural(String suffix, int num)
    {
        return num == 1 ? "" : suffix;
    }
    
    public static String plural(long num)
    {
        return plural("s", num);
    }
    public static String plural(String suffix, long num)
    {
        return num == 1 ? "" : suffix;
    }
    
    public static String plural(double num)
    {
        return plural("s", num);
    }
    public static String plural(String suffix, double num)
    {
        return num == 1 ? "" : suffix;
    }
    
    /**
     * Removes all duplicate spaces (i.e. wherever there are more than one
     * space in a row, the rest is removed). All leading and trailing spaces are
     * also removed, as in the {@link String.trim()} method.
     * @param s The string to process.
     * @return The given string, with all duplicate, leading, and trailing
     * whitespace removed.
     */
    public static String removeDuplicateSpaces(String s)
    {
        int len = s.length();
        StringBuilder sb = new StringBuilder(len);
        
        boolean prev = false;
        for (int i = 0; i < len; i++)
        {
            char c = s.charAt(i);
            if (c == ' ')
            {
                if (!prev && i != 0)
                {
                    sb.append(' ');
                }
                prev = true;
            }
            else
            {
                sb.append(c);
                prev = false;
            }
        }
        
        if (sb.length() == len)
        {
            //nothing was removed, return original string
            return s;
        }
        else
        {
            return sb.toString();
        }
    }
    
    public static String replaceChars(String s, Map<Character, Character> map)
    {
        boolean changed = false;
        int len = s.length();
        StringBuilder sb = new StringBuilder(len);
        
        for (int i = 0; i < len; i++)
        {
            char originalChar = s.charAt(i);
            Character newChar = map.get(originalChar);
            
            if (newChar == null)
            {
                sb.append(originalChar);
            }
            else
            {
                sb.append(newChar);
                changed = true;
            }
        }
        
        if (changed)
        {
            return sb.toString();
        }
        
        //nothing was changed
        return s;
    }
    
    public static String possessive(String s)
    {
        char last = Character.toLowerCase(s.charAt(s.length() - 1));
        
        if (last == 's' || last == 'z')
        {
            return s + "'";
        }
        return s + "'s";
    }
    
    public static int hashCode(int i)
    {
        return hashCode(i, 1);
    }
    public static int hashCode(int i, int startVal)
    {
        return 31 * startVal + i;
    }
    
    public static int hashCode(byte[] array)
    {
        return hashCode(array, 0, array.length);
    }
    public static int hashCode(byte[] array, int startVal)
    {
        return hashCode(array, 0, array.length, startVal);
    }
    public static int hashCode(byte[] array, int off, int len)
    {
        return hashCode(array, off, len, 1);
    }
    public static int hashCode(byte[] array, int off, int len, int startVal)
    {
        if (array == null)
        {
            return startVal - 1;
        }
        
        int max = off + len;
        if (off < 0 || len < 0 || max > array.length)
        {
            throw new IndexOutOfBoundsException();
        }
        
        int result = startVal;
        for (int i = off; i < max; i++)
        {
            result = 31 * result + array[i];
        }
        
        return result;
    }
    
    public static byte[] copyOf(byte[] src, int off, int len)
    {
        byte[] dest = new byte[len];
        System.arraycopy(src, off, dest, 0, len);
        return dest;
    }
}
