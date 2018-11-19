package onlinegame.shared.net.udp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import onlinegame.shared.MathUtil;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class PacketSplitter
{
    /*public static void main(String[] args) throws Throwable
    {
        Logger.log("gen");
        byte[] testData = new byte[8192];
        Random r = new Random(12345);
        r.nextBytes(testData);
        
        DatagramPacket packet = new DatagramPacket(testData, testData.length, InetAddress.getByName("localhost"), 9876);
        
        DatagramPacket[] packets;
        
        packets = split(packet, (short)12345, (short)23456, 1400);
        
        Logger.log("split");
        for (int i = 0; i < packets.length; i++)
        {
            Logger.log(packets[i].getLength());
        }
        
        List<DatagramPacket> list = Arrays.asList(packets);
        Collections.shuffle(Arrays.asList(packets));
        packets = (DatagramPacket[])list.toArray();
        
        Logger.log("assemble");
        PacketAssembler as = new PacketAssembler(1000);
        
        for (int i = 0; i < packets.length-1; i++)
        {
            as.readPacket(packets[i]);
            //Logger.log(packets[i].getLength());
            //Logger.log(as.readPacket(packets[i]));
        }
        
        DatagramPacket result = as.readPacket(packets[packets.length-1]);
        byte[] resultBytes = result.getData();
        
        Logger.log(Arrays.equals(testData, resultBytes));
    }*/
    
    //header:
    //short UDP_ID
    //short sessionHash
    //int checksum (including the header from this point)
    //short packetId
    //ubyte fragmentCount (minus one)
    //ubyte fragmentId
    //
    //total size: 12 bytes
    
    private static final int headerSize = 12;
    
    public static DatagramPacket[] split(DatagramPacket sourcePacket, short sessionHash, short packetId, int maxSize)
    {
        return split(sourcePacket.getData(), sourcePacket.getOffset(), sourcePacket.getLength(), sourcePacket.getAddress(), sourcePacket.getPort(), sessionHash, packetId, maxSize);
    }
    public static DatagramPacket[] split(byte[] data, InetAddress addr, int port, short sessionHash, short packetId, int maxSize)
    {
        return split(data, 0, data.length, addr, port, sessionHash, packetId, maxSize);
    }
    public static DatagramPacket[] split(byte[] data, int off, int len, InetAddress addr, int port, short sessionHash, short packetId, int maxSize)
    {
        int maxBytesPerPacket, bytesPerPacket, fragmentCount;
        if (len <= 0)
        {
            len = 0;
            maxBytesPerPacket = 0;
            fragmentCount = 1;
            bytesPerPacket = 0;
        }
        else
        {
            maxBytesPerPacket = maxSize - headerSize;

            fragmentCount = MathUtil.ceilDiv(len, maxBytesPerPacket);
            if (fragmentCount > 256)
            {
                throw new IllegalArgumentException("Message too big.");
            }

            bytesPerPacket = MathUtil.ceilDiv(len, fragmentCount);
        }
        
        int dataOffset = off;
        
        DatagramPacket[] packets = new DatagramPacket[fragmentCount];
        for (int fragmentId = 0; fragmentId < fragmentCount; fragmentId++)
        {
            int numBytes = Math.min(len - dataOffset, bytesPerPacket);
            
            byte[] arr = new byte[headerSize + numBytes];
            ByteBuffer buf = ByteBuffer.wrap(arr);
            
            buf.clear();
            
            buf.putShort(Protocol.UDP_ID);
            buf.putShort(sessionHash);
            buf.putInt(0); //placeholder for checksum (position == 4)
            buf.putShort(packetId); //position == 8
            buf.put((byte)(fragmentCount - 1));
            buf.put((byte)fragmentId);
            
            buf.put(data, dataOffset, numBytes);
            dataOffset += numBytes;
            
            buf.flip();
            
            //generate hash stating from position 8 (packetId) to the end of the buffer
            buf.putInt(4, SharedUtil.hashCode(buf.array(), 8, buf.limit() - 8));
            
            packets[fragmentId] = new DatagramPacket(arr, arr.length, addr, port);
        }
        
        return packets;
    }
}
