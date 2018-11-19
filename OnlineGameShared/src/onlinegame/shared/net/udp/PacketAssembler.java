package onlinegame.shared.net.udp;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import onlinegame.shared.Logger;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.net.Protocol;

public final class PacketAssembler
{
    //packet header: see PacketSplitter class
    
    private static final int headerSize = 12;
    
    private final TMap<PacketId, Packet> packetMap = new THashMap<>();
    private final Queue<Packet> packetQueue = new ArrayDeque<>();
    
    private long timeout;
    
    public PacketAssembler()
    {
        this(1000);
    }
    public PacketAssembler(long timeout)
    {
        this.timeout = timeout;
    }
    
    public void clearOldPackets()
    {
        if (packetQueue.isEmpty())
        {
            return;
        }
        
        long time = System.currentTimeMillis();
        
        do
        {
            Packet p = packetQueue.peek();
            long delta = time - p.creationTime;
            if (delta >= timeout || delta <= -timeout) //in case the system time changes
            {
                packetMap.remove(p.id);
                packetQueue.poll();
            }
            else
            {
                break;
            }
        }
        while (!packetQueue.isEmpty());
    }
    
    public int getPendingPacketCount()
    {
        return packetMap.size();
    }
    
    public UDPMessage readPacket(DatagramPacket p)
    {
        clearOldPackets();
        
        ByteBuffer buf = ByteBuffer.wrap(p.getData(), p.getOffset(), p.getLength());
        if (buf.remaining() < headerSize)
        {
            Logger.logError("UDP Packet too small (" + buf.remaining() + ", at least " + headerSize + " required)");
            return null;
        }
        
        short udpId = buf.getShort();
        short sessionHash = buf.getShort();
        int checksum = buf.getInt();
        short packetId = buf.getShort();
        int fragmentCount = (buf.get() & 0xff) + 1;
        int fragmentId = buf.get() & 0xff;
        
        if (udpId != Protocol.UDP_ID)
        {
            Logger.logError("Invalid UDP_ID.");
            return null;
        }
        int actualChecksum = SharedUtil.hashCode(buf.array(), 8, buf.limit() - 8);
        if (checksum != actualChecksum)
        {
            Logger.logError("Invalid UDP checksum.");
            return null;
        }
        
        if (fragmentCount == 1)
        {
            if (fragmentId != 0)
            {
                Logger.logError("Invalid fragmentId: " + fragmentId);
                return null;
            }
            return new UDPMessage(p.getAddress(), p.getPort(), sessionHash, packetId, buf.array(), buf.position(), buf.remaining());
        }
        
        PacketId pid = new PacketId(p.getAddress(), p.getPort(), sessionHash, packetId);
        
        Packet packet = packetMap.get(pid);
        if (packet == null)
        {
            packet = new Packet(pid, fragmentCount);
            packetMap.put(pid, packet);
            packetQueue.add(packet);
        }
        
        byte[] data = new byte[buf.remaining()];
        System.arraycopy(buf.array(), buf.position(), data, 0, buf.remaining());
        
        packet.add(fragmentId, data);
        
        UDPMessage finished = packet.assembleIfReady();
        if (finished == null)
        {
            return null;
        }
        else
        {
            packetMap.remove(pid);
            packetQueue.remove(packet);
            return finished;
        }
    }
    
    private static final class Packet
    {
        private final PacketId id;
        
        private final byte[][] fragments;
        
        private final long creationTime = System.currentTimeMillis();
        
        private Packet(PacketId id, int fragmentCount)
        {
            this.id = id;
            fragments = new byte[fragmentCount][];
        }
        
        private void add(int fragmentId, byte[] data)
        {
            if (fragmentId < 0 || fragmentId > fragments.length)
            {
                Logger.logError("Invalid fragmentId: " + fragmentId);
                return;
            }
            if (data == null)
            {
                throw new NullPointerException();
            }
            if (fragments[fragmentId] != null)
            {
                Logger.logError("Fragment already recieved (" + fragmentId + ")");
                return;
            }
            
            fragments[fragmentId] = data;
        }
        
        private UDPMessage assembleIfReady()
        {
            int packetSize = 0;
            for (int i = 0; i < fragments.length; i++)
            {
                if (fragments[i] == null)
                {
                    return null;
                }
                else
                {
                    packetSize += fragments[i].length;
                }
            }
            
            byte[] data = new byte[packetSize];
            
            int pos = 0;
            for (int i = 0; i < fragments.length; i++)
            {
                System.arraycopy(fragments[i], 0, data, pos, fragments[i].length);
                pos += fragments[i].length;
            }
            
            return new UDPMessage(id.addr, id.port, id.sessionHash, id.packetId, data);
        }
    }
    
    private static final class PacketId
    {
        private final InetAddress addr;
        private final int port;
        private final short sessionHash;
        private final short packetId;
        
        private PacketId(InetAddress addr, int port, short sessionHash, short packetId)
        {
            this.addr = addr;
            this.port = port;
            this.sessionHash = sessionHash;
            this.packetId = packetId;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof PacketId)) return false;
            
            PacketId pid = (PacketId)o;
            return Objects.equals(addr, pid.addr) && port == pid.port && sessionHash == pid.sessionHash && packetId == pid.packetId;
        }

        @Override
        public int hashCode()
        {
            int hash = Objects.hashCode(addr);
            hash = 19 * hash + port;
            hash = 19 * hash + sessionHash;
            hash = 19 * hash + packetId;
            return hash;
        }
    }
}
