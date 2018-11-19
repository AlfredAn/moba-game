package onlinegame.shared.net.udp;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import onlinegame.shared.bitstream.BitOutputStream;

/**
 *
 * @author Alfred
 */
public final class UDPOutput extends ByteArrayOutputStream
{
    public final InetAddress addr;
    public final int port;
    public final short sessionHash;
    public final short packetId;
    
    public UDPOutput(InetAddress addr, int port, short sessionHash, short packetId)
    {
        this(32, addr, port, sessionHash, packetId);
    }
    public UDPOutput(int initialCapacity, InetAddress addr, int port, short sessionHash, short packetId)
    {
        super(initialCapacity);
        
        this.addr = addr;
        this.port = port;
        this.sessionHash = sessionHash;
        this.packetId = packetId;
    }
    
    public UDPMessage toMessage()
    {
        return new UDPMessage(addr, port, sessionHash, packetId, buf, 0, count);
    }
    
    public BitOutputStream getBitStream()
    {
        return new BitOutputStream(this);
    }
}
