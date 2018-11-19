package onlinegame.shared.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import onlinegame.shared.Logger;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.net.OutputMessage;

/**
 *
 * @author Alfred
 */
public final class UDPServer
{
    public final int port;
    private final DatagramSocket socket;
    
    private volatile boolean exit = false;
    
    private final Map<ClientId, UDPServerConnection> clientMap = new HashMap<>();
    private final Queue<UDPMessage> sendQueue = new ArrayDeque<>();
    
    public UDPServer(int port) throws SocketException
    {
        this.port = port;
        socket = new DatagramSocket(port);
        
        new Listener();
        new Sender();
    }
    
    public UDPConnection createConnection(InetAddress addr, short sessionHash)
    {
        UDPServerConnection con = new UDPServerConnection(this, addr, sessionHash);
        addConnection(con);
        Logger.log("CONNECTION: " + addr + ":" + port);
        return con;
    }
    
    void addConnection(UDPServerConnection recv)
    {
        synchronized (clientMap)
        {
            clientMap.put(new ClientId(recv.addr, recv.sessionHash), recv);
        }
    }
    
    void removeConnection(UDPServerConnection recv)
    {
        synchronized (clientMap)
        {
            clientMap.remove(new ClientId(recv.addr, recv.sessionHash));
        }
    }
    
    public void sendMessage(OutputMessage msg, InetAddress addr, int port, short packetId)
    {
        byte[] data = msg.getData();
        sendMessage(new UDPMessage(addr, port, (short)0, packetId, data));
    }
    private void sendMessage(UDPMessage packet)
    {
        synchronized (sendQueue)
        {
            sendQueue.add(packet);
            sendQueue.notifyAll();
        }
    }
    
    public void close()
    {
        exit = true;
        socket.close();
    }
    
    private final class Listener implements Runnable
    {
        private Listener()
        {
            Thread t = new Thread(this, "UDP Listener (port " + port + ")");
            t.setDaemon(true);
            t.start();
        }
        
        @Override
        public void run()
        {
            byte[] arr = new byte[65536];
            ByteBuffer buf = ByteBuffer.wrap(arr);
            DatagramPacket packet = new DatagramPacket(arr, arr.length);
            PacketAssembler as = new PacketAssembler();
            
            while (!exit)
            {
                try
                {
                    socket.receive(packet);
                    
                    UDPMessage readPacket = as.readPacket(packet);
                    if (readPacket == null)
                    {
                        continue;
                    }
                    
                    ClientId cid = tempId;
                    cid.addr = readPacket.addr;
                    cid.sessionHash = readPacket.sessionHash;
                    
                    //Logger.log("PACKET: " + cid.addr + ":" + readPacket.port + " (id " + cid.sessionHash + ")");
                    
                    UDPServerConnection c;
                    synchronized (clientMap)
                    {
                        c = clientMap.get(cid);
                    }
                    if (c == null)
                    {
                        Logger.logError("No reciever for packet from " + cid.addr.toString());
                        continue;
                    }
                    
                    if (!c.isConnected())
                    {
                        c.connect(readPacket.port);
                    }
                    if (buf.remaining() > 0)
                    {
                        c.newMessage(readPacket.data);
                    }
                }
                catch (IOException | BufferUnderflowException e)
                {
                    SharedUtil.logErrorMsg(e);
                }
            }
        }
    }
    
    private final class Sender implements Runnable
    {
        private Sender()
        {
            Thread t = new Thread(this, "UDP Sender (port " + port + ")");
            t.setDaemon(true);
            t.start();
        }
        
        @Override
        public void run()
        {
            while (!exit)
            {
                UDPMessage msg;
                synchronized (sendQueue)
                {
                    while ((msg = sendQueue.poll()) == null)
                    {
                        try
                        {
                            sendQueue.wait(5000);
                        }
                        catch (InterruptedException e) {}
                    }
                }
                
                try
                {
                    DatagramPacket p = new DatagramPacket(msg.data, msg.data.length, msg.addr, msg.port);
                    DatagramPacket[] packets = PacketSplitter.split(p, msg.sessionHash, msg.packetId, 1400);
                    
                    for (int i = 0; i < packets.length; i++)
                    {
                        socket.send(packets[i]);
                    }
                }
                catch (IOException e)
                {
                    SharedUtil.logErrorMsg(e);
                }
            }
        }
    }
    
    private final ClientId tempId = new ClientId(null, (short)0);
    
    private static final class ClientId
    {
        private InetAddress addr;
        private short sessionHash;
        
        private ClientId(InetAddress addr, short sessionHash)
        {
            this.addr = addr;
            this.sessionHash = sessionHash;
        }
        
        private ClientId set(InetAddress addr, short sessionHash)
        {
            this.addr = addr;
            this.sessionHash = sessionHash;
            return this;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (!(o instanceof ClientId)) return false;
            
            ClientId rid = (ClientId)o;
            return Objects.equals(addr, rid.addr) && sessionHash == rid.sessionHash;
        }

        @Override
        public int hashCode()
        {
            int hash = Objects.hashCode(addr);
            hash = 97 * hash + sessionHash;
            return hash;
        }
    }
}
