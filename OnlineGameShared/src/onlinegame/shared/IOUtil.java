package onlinegame.shared;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Alfred
 */
public final class IOUtil
{
    private IOUtil() {}
    
    public static ByteBuffer readToBuffer(InputStream in) throws IOException
    {
        return readToBuffer(in, 16384, false);
    }
    public static ByteBuffer readToBuffer(InputStream in, boolean direct) throws IOException
    {
        return readToBuffer(in, 16384, direct);
    }
    public static ByteBuffer readToBuffer(InputStream in, int initialCapacity) throws IOException
    {
        return readToBuffer(in, initialCapacity, false);
    }
    public static ByteBuffer readToBuffer(InputStream in, int initialCapacity, boolean direct) throws IOException
    {
        ByteBuffer buf = direct ? ByteBuffer.allocateDirect(initialCapacity) : ByteBuffer.allocate(initialCapacity);
        byte[] arr = new byte[16384];
        
        while (true)
        {
            int readBytes = in.read(arr);
            
            if (readBytes > buf.remaining())
            {
                buf.flip();
                int newSize = buf.capacity() * 2;
                ByteBuffer newBuf = direct ? ByteBuffer.allocateDirect(newSize) : ByteBuffer.allocate(newSize);
                newBuf.put(buf);
                
                buf = newBuf;
            }
            else if (readBytes == -1)
            {
                break;
            }
            buf.put(arr, 0, readBytes);
        }
        
        buf.flip();
        return buf;
    }
}
