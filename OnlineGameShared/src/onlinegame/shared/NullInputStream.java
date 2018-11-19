package onlinegame.shared;

import java.io.InputStream;
import java.util.Arrays;

/**
 *
 * @author Alfred
 */
public final class NullInputStream extends InputStream
{
    @Override
    public int read()
    {
        return 0;
    }
    
    @Override
    public int read(byte[] b)
    {
        Arrays.fill(b, (byte)0);
        return b.length;
    }
    
    @Override
    public int read(byte[] b, int off, int len)
    {
        Arrays.fill(b, off, off+len, (byte)0);
        return len;
    }
    
    @Override
    public long skip(long n)
    {
        return n;
    }
    
    @Override
    public int available()
    {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public void mark(int readlimit) {}
    
    @Override
    public void reset() {}
    
    @Override
    public boolean markSupported()
    {
        return true;
    }
}
