package onlinegame.shared;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Alfred
 */
public final class ByteBufferOutputStream extends OutputStream
{
    private ByteBuffer buf;
    private final boolean resizeable;
    
    public ByteBufferOutputStream(ByteBuffer buf)
    {
        this(buf, false);
    }
    public ByteBufferOutputStream(ByteBuffer buf, boolean resizeable)
    {
        this.buf = buf;
        this.resizeable = resizeable;
        
        if (resizeable && (buf.position() != 0 || buf.limit() != buf.capacity()))
        {
            throw new IllegalArgumentException("Resizeable ByteBufferOutputStreams must start with a buffer that has position == 0 and limit == capacity.");
        }
    }
    
    private void grow(int minCapacity)
    {
        int oldCapacity = buf.limit();
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
        {
            newCapacity = minCapacity;
        }
        
        ByteBuffer newBuf;
        if (buf.isDirect())
        {
            newBuf = ByteBuffer.allocateDirect(newCapacity);
        }
        else
        {
            newBuf = ByteBuffer.allocate(newCapacity);
        }
        newBuf.order(buf.order());
        buf.flip();
        newBuf.put(buf);
    }
    
    @Override
    public void write(int b) throws IOException
    {
        if (buf.remaining() == 0)
        {
            if (resizeable)
            {
                grow(buf.limit() + 1);
            }
            else
            {
                throw new IOException("buffer overflow");
            }
        }
        
        buf.put((byte)b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (len > buf.remaining())
        {
            if (resizeable)
            {
                grow(buf.limit() + len);
            }
            else
            {
                throw new IOException("buffer overflow");
            }
        }
        
        buf.put(b, off, len);
    }
    
    public int remaining()
    {
        return buf.remaining();
    }
    
    public ByteBuffer getBuffer()
    {
        return buf;
    }
    
    public boolean isResizeable()
    {
        return resizeable;
    }
}
