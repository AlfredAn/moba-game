package onlinegame.shared.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import onlinegame.shared.bitstream.BitOutputStream;

/**
 *
 * @author Alfred
 */
public final class OutputMessage
{
    private final ByteArrayOutputStream os;
    private final byte id;
    
    public OutputMessage()
    {
        this((byte)-1);
    }
    public OutputMessage(byte id)
    {
        this(id, 64);
    }
    public OutputMessage(byte id, int capacity)
    {
        os = new ByteArrayOutputStream(capacity);
        this.id = id;
    }
    
    public OutputStream getStream()
    {
        return os;
    }
    
    public DataOutputStream getDataStream()
    {
        return new DataOutputStream(os);
    }
    
    public BitOutputStream getBitStream()
    {
        return new BitOutputStream(os);
    }
    
    public void send(OutputStream out) throws IOException
    {
        byte[] bytes = os.toByteArray();
        int len = bytes.length;
        
        if (len >= 65536)
        {
            throw new GameProtocolException("Message is too big! (" + len + " bytes)");
        }
        
        //write length as short
        out.write((len >>> 8) & 0xFF);
        out.write(len & 0xFF);
        
        //write id
        out.write(id);
        
        //write message
        out.write(bytes);
    }
    
    public byte[] getData()
    {
        return os.toByteArray();
    }
    
    public byte getId()
    {
        return id;
    }
}

























