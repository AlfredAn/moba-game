package onlinegame.shared;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Alfred
 */
public final class ByteBufferInputStream extends InputStream
{
    private final ByteBuffer buf;
    
    public ByteBufferInputStream(ByteBuffer buf)
    {
        this.buf = buf;
    }
    
    @Override
    public int read()
    {
        if (buf.remaining() == 0)
        {
            return -1;
        }
        return buf.get();
    }
    
    @Override
    public int read(byte[] b, int off, int len)
    {
        if (len <= 0)
        {
            return 0;
        }
        
        int remaining = buf.remaining();
        if (remaining == 0)
        {
            return -1;
        }
        
        len = Math.min(len, remaining);
        buf.get(b, off, len);
        
        return len;
    }
    
    @Override
    public long skip(long n)
    {
        if (n <= 0)
        {
            return 0;
        }
        
        int skip = (int)Math.min(n, buf.remaining());
        buf.position(buf.position() + skip);
        
        return skip;
    }
    
    @Override
    public int available()
    {
        return buf.remaining();
    }
}
