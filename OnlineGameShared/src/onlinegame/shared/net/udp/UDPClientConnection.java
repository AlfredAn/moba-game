package onlinegame.shared.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import onlinegame.shared.Logger;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;

/**
 *
 * @author Alfred
 */
public final class UDPClientConnection implements UDPConnection
{
    public final InetAddress addr;
    public final int port;
    public final short sessionHash;
    
    private final DatagramSocket socket;
    private volatile boolean exit = false;
    
    private final Queue<InputMessage> input = new ConcurrentLinkedQueue<>();
    private final Queue<OutputMessage> output = new ArrayDeque<>();
    
    private long downBytesPerSec = 0;
    private long lastDownTime = System.nanoTime();
    
    private volatile Object notify;
    
    public UDPClientConnection(String addr, int port, short sessionHash) throws IOException
    {
        this(InetAddress.getByName(addr), port, sessionHash);
    }
    public UDPClientConnection(InetAddress addr, int port, short sessionHash) throws IOException
    {
        this.addr = addr;
        this.port = port;
        this.sessionHash = sessionHash;
        
        socket = new DatagramSocket();
        socket.connect(addr, port);
        
        new Reciever();
        new Sender();
    }
    
    @Override
    public void setNotify(Object o)
    {
        notify = o;
    }
    
    @Override
    public InputMessage readMessage()
    {
        return input.poll();
    }
    
    @Override
    public void sendMessage(OutputMessage msg)
    {
        synchronized (output)
        {
            output.add(msg);
            output.notifyAll();
        }
    }
    
    @Override
    public void flush()
    {
        
    }
    
    @Override
    public void close()
    {
        exit = true;
        socket.close();
        Logger.log("Closing connection!");
    }
    
    @Override
    public boolean isClosed()
    {
        return exit;
    }
    
    private final class Reciever implements Runnable
    {
        private Reciever()
        {
            Thread t = new Thread(this, "UDP Reciever (port " + port + ")");
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
                    
                    long time = System.nanoTime();
                    long d = time - lastDownTime;
                    if (d >= 1_000_000_000L)
                    {
                        if (d >= 2_000_000_000L)
                        {
                            lastDownTime = time;
                            downBytesPerSec = (long)(((double)downBytesPerSec) * 1e9 / d);
                        }
                        else
                        {
                            lastDownTime += (d / 1_000_000_000L) * 1_000_000_000L;
                        }

                        Logger.log("DOWN: " + downBytesPerSec + " bytes/sec");
                        downBytesPerSec = 0;
                    }
                    downBytesPerSec += packet.getLength();
                    
                    //if (Math.random() < .5f) continue; //simulate packet loss
                    UDPMessage readPacket = as.readPacket(packet);
                    if (readPacket == null)
                    {
                        continue;
                    }
                    
                    InputMessage msg = new InputMessage((byte)-1, readPacket.data, 0, readPacket.data.length);
                    input.add(msg);
                    
                    if (notify != null)
                    {
                        synchronized (notify)
                        {
                            notify.notifyAll();
                        }
                    }
                }
                catch (IOException | BufferUnderflowException e)
                {
                    SharedUtil.logErrorMsg(e);
                }
            }
            
            Logger.log("exited connection thread!");
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
            short idCounter = 0;
            
            sendMessage(new OutputMessage());
            flush();
            
            while (!exit)
            {
                OutputMessage msg;
                synchronized (output)
                {
                    while ((msg = output.poll()) == null)
                    {
                        try
                        {
                            output.wait(5000);
                        }
                        catch (InterruptedException e) {}
                    }
                }
                
                try
                {
                    byte[] data = msg.getData();
                    DatagramPacket[] packets = PacketSplitter.split(data, addr, port, sessionHash, idCounter, 1400);
                    
                    for (int i = 0; i < packets.length; i++)
                    {
                        //if (Math.random() < .5f) continue; //simulate packet loss
                        socket.send(packets[i]);
                    }
                }
                catch (IOException | BufferOverflowException e)
                {
                    SharedUtil.logErrorMsg(e);
                }
            }
        }
    }
    
    @Override
    public boolean isConnected()
    {
        return !exit;
    }
}
