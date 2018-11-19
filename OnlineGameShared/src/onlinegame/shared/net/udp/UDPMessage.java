package onlinegame.shared.net.udp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import onlinegame.shared.bitstream.BitInputStream;
import onlinegame.shared.SharedUtil;

/**
 *
 * @author Alfred
 */
public final class UDPMessage
{
    public final InetAddress addr;
    public final int port;
    public final short sessionHash;
    public final short packetId;
    public final byte[] data;
    
    public UDPMessage(InetAddress addr, int port, short sessionHash, short packetId, byte[] data, int dataOff, int dataLen)
    {
        this.addr = addr;
        this.port = port;
        this.sessionHash = sessionHash;
        this.packetId = packetId;
        this.data = SharedUtil.copyOf(data, dataOff, dataLen);
    }
    
    UDPMessage(InetAddress addr, int port, short sessionHash, short packetId, byte[] data)
    {
        this.addr = addr;
        this.port = port;
        this.sessionHash = sessionHash;
        this.packetId = packetId;
        this.data = data;
    }
    
    public UDPMessage(DatagramPacket packet, short sessionHash, short packetId)
    {
        this(packet.getAddress(), packet.getPort(), sessionHash, packetId, packet.getData(), packet.getOffset(), packet.getLength());
    }
    
    public InputStream getStream()
    {
        return new ByteArrayInputStream(data);
    }
    
    public BitInputStream getBitStream()
    {
        return new BitInputStream(getStream());
    }
}
