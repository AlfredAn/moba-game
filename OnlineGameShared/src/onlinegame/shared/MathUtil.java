package onlinegame.shared;

/**
 *
 * @author Alfred
 */
public final class MathUtil
{
    private MathUtil() {}
    
    public static float frac(float n)
    {
        return n - floor(n);
    }
    
    public static double frac(double n)
    {
        return n - floor(n);
    }
    
    public static float fracPositive(float n)
    {
        return n - floorPositive(n);
    }
    
    public static double fracPositive(double n)
    {
        return n - floorPositive(n);
    }
    
    public static int floor(float n)
    {
        int in = (int)n;
        return in == n || n >= 0 ? in : in-1;
    }
    
    public static int floor(double n)
    {
        int in = (int)n;
        return in == n || n >= 0 ? in : in-1;
    }
    
    public static int floorPositive(float n)
    {
        return (int)n;
    }
    
    public static int floorPositive(double n)
    {
        return (int)n;
    }
    
    public static int ceilPositive(float n)
    {
        int in = (int)n;
        return in == n ? in : in+1;
    }
    
    public static int ceilPositive(double n)
    {
        int in = (int)n;
        return in == n ? in : in+1;
    }
    
    public static int ceil(float n)
    {
        int in = (int)n;
        return in == n || n < 0 ? in : in+1;
    }
    
    public static int ceil(double n)
    {
        int in = (int)n;
        return in == n || n < 0 ? in : in+1;
    }
    
    public static int round(float n)
    {
        return floor(n + .5f);
    }
    
    public static int round(double n)
    {
        return floor(n + .5);
    }
    
    public static int roundPositive(float n)
    {
        return (int)(n + .5f);
    }
    
    public static int roundPositive(double n)
    {
        return (int)(n + .5);
    }
    
    public static double clamp(double n, double min, double max)
    {
        if (n <= min)
        {
            return min;
        }
        else if (n >= max)
        {
            return max;
        }
        return n;
    }
    
    public static float clamp(float n, float min, float max)
    {
        if (n <= min)
        {
            return min;
        }
        else if (n >= max)
        {
            return max;
        }
        return n;
    }
    
    public static int clamp(int n, int min, int max)
    {
        if (n <= min)
        {
            return min;
        }
        else if (n >= max)
        {
            return max;
        }
        return n;
    }
    
    public static long clamp(long n, long min, long max)
    {
        if (n <= min)
        {
            return min;
        }
        else if (n >= max)
        {
            return max;
        }
        return n;
    }
    
    public static float lerp(float a, float b, float f)
    {
        return a * (1-f) + b * f;
    }
    
    public static double lerp(double a, double b, double f)
    {
        return a * (1-f) + b * f;
    }
    
    public static float lerp3(float a, float b, float c, float f)
    {
        if (f < .5f)
        {
            return lerp(a, b, f*2);
        }
        else
        {
            return lerp(b, c, f*2 - 1);
        }
    }
    
    public static double lerp3(double a, double b, double c, double f)
    {
        if (f < .5)
        {
            return lerp(a, b, f*2);
        }
        else
        {
            return lerp(b, c, f*2 - 1);
        }
    }
    
    public static float dist(float x1, float y1, float x2, float y2)
    {
        double dx = (double)x2 - x1, dy = (double)y2 - y1;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }
    
    public static double dist(double x1, double y1, double x2, double y2)
    {
        double dx = x2 - x1, dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public static float distSqr(float x1, float y1, float x2, float y2)
    {
        float dx = x2 - x1, dy = y2 - y1;
        return dx * dx + dy * dy;
    }
    
    public static double distSqr(double x1, double y1, double x2, double y2)
    {
        double dx = x2 - x1, dy = y2 - y1;
        return dx * dx + dy * dy;
    }
    
    public static int ceilingLog2(int x)
    {
        if (x <= 0)
        {
            throw new ArithmeticException();
        }
        
        return 32 - Integer.numberOfLeadingZeros(x-1);
    }
    
    public static int log2(int x)
    {
        if (x <= 0)
        {
            throw new ArithmeticException();
        }
        
        return 31 - Integer.numberOfLeadingZeros(x);
    }
    
    //clamped log2, input clamped to >= 1
    public static int clog2(int x)
    {
        return log2(Math.max(1, x));
    }
    
    public static int log2(long x)
    {
        if (x <= 0)
        {
            throw new ArithmeticException();
        }
        
        return 63 - Long.numberOfLeadingZeros(x);
    }
    
    //clamped log2, input clamped to >= 1
    public static int clog2(long x)
    {
        return log2(Math.max(1, x));
    }
    
    /**
     * XOR's the two arrays and places the result in {@code b1}.
     * 
     * @param b1
     * @param b2
     */
    public static void xor(byte[] b1, byte[] b2)
    {
        int minLen = Math.min(b1.length, b2.length);
        
        for (int i = 0; i < minLen; i++)
        {
            b1[i] ^= b2[i];
        }
    }
    
    public static int ceilDiv(int num, int divisor)
    {
        return (num + divisor - 1) / divisor;
    }
    
    public static long ceilDiv(long num, long divisor)
    {
        return (num + divisor - 1) / divisor;
    }
}
