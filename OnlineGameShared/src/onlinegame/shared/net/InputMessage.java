package onlinegame.shared.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import onlinegame.shared.bitstream.BitInputStream;

/**
 *
 * @author Alfred
 */
public final class InputMessage
{
    private byte id;
    private final byte[] bytes;
    
    public InputMessage(ByteBuffer buf)
    {
        this((byte)-1, buf.array(), buf.position(), buf.remaining());
    }
    
    public InputMessage(byte id, byte[] bytes, int len)
    {
        this(id, bytes, 0, len);
    }
    public InputMessage(byte id, byte[] bytes, int off, int len)
    {
        this.id = id;
        
        this.bytes = new byte[len];
        System.arraycopy(bytes, off, this.bytes, 0, len);
    }
    
    public InputMessage(InputStream in) throws IOException
    {
        if (in instanceof DataInputStream)
        {
            bytes = read((DataInputStream)in);
        }
        else
        {
            bytes = read(new DataInputStream(in));
        }
    }
    
    public InputMessage(DataInputStream in) throws IOException
    {
        bytes = read(in);
    }
    
    private byte[] read(DataInputStream in) throws IOException
    {
        int len = in.readShort() & 0xffff;
        
        id = in.readByte();
        
        byte[] b = new byte[len];
        in.readFully(b);
        
        return b;
    }
    
    public byte getId()
    {
        return id;
    }
    
    public byte[] getData()
    {
        return Arrays.copyOf(bytes, bytes.length);
    }
    
    public InputStream getStream()
    {
        return new ByteArrayInputStream(bytes);
    }
    
    public DataInputStream getDataStream()
    {
        return new DataInputStream(getStream());
    }
    
    public BitInputStream getBitStream()
    {
        return new BitInputStream(getStream());
    }
}
