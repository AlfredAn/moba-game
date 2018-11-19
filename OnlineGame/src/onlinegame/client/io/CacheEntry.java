package onlinegame.client.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Alfred
 */
final class CacheEntry
{
    private final byte[] data;
    
    CacheEntry(InputStream is) throws IOException
    {
        this(is, 16384);
    }
    CacheEntry(InputStream is, int bufferSize) throws IOException
    {
        byte[] buf = new byte[bufferSize];
        ByteArrayOutputStream os = new ByteArrayOutputStream(bufferSize);
        
        while (true)
        {
            int num = is.read(buf);
            
            if (num == -1)
            {
                break;
            }
            
            os.write(buf, 0, num);
        }
        
        data = os.toByteArray();
        
        FileLoader.cacheSize.addAndGet(data.length);
    }
    
    int size()
    {
        return data.length;
    }
    
    InputStream getInputStream()
    {
        return new ByteArrayInputStream(data);
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        FileLoader.cacheSize.addAndGet(-data.length);
        
        super.finalize();
    }
}
