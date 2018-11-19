package onlinegame.shared.net.udp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;

/**
 *
 * @author Alfred
 */
final class UDPServerConnection implements UDPConnection
{
    public final InetAddress addr;
    private int port;
    public final short sessionHash;
    
    public final UDPServer server;
    
    private boolean isClosed = false;
    private volatile boolean isConnected = false;
    
    private volatile Object notify;
    
    private final Queue<InputMessage> input = new ConcurrentLinkedQueue<>();
    
    UDPServerConnection(UDPServer server, InetAddress addr, short sessionHash)
    {
        this.server = server;
        this.addr = addr;
        this.sessionHash = sessionHash;
    }
    
    @Override
    public void setNotify(Object o)
    {
        notify = o;
    }
    
    void connect(int port)
    {
        this.port = port;
        
        synchronized (tempMsg)
        {
            for (OutputMessage msg : tempMsg)
            {
                server.sendMessage(msg, addr, port, idCounter++);
            }
        }
        isConnected = true;
        tempMsg.clear();
    }
    
    void newMessage(byte[] data)
    {
        InputMessage msg = new InputMessage((byte)-1, data, 0, data.length);
        input.add(msg);
        
        if (notify != null)
        {
            synchronized (notify)
            {
                notify.notifyAll();
            }
        }
    }
    
    @Override
    public InputMessage readMessage()
    {
        return input.poll();
    }
    
    private List<OutputMessage> tempMsg = new ArrayList<>();
    private short idCounter = 0;
    
    @Override
    public void sendMessage(OutputMessage msg)
    {
        if (isConnected)
        {
            server.sendMessage(msg, addr, port, idCounter++);
        }
        else
        {
            synchronized (tempMsg)
            {
                tempMsg.add(msg);
            }
        }
    }
    
    @Override
    public void flush()
    {
        
    }
    
    @Override
    public void close()
    {
        server.removeConnection(this);
        isClosed = true;
    }
    
    @Override
    public boolean isClosed()
    {
        return isClosed;
    }
    
    @Override
    public boolean isConnected()
    {
        return isConnected && !isClosed;
    }
}
