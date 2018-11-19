package onlinegame.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import onlinegame.shared.net.Connection;
import onlinegame.shared.Logger;

/**
 *
 * @author Alfred
 */
public final class Listener implements Runnable
{
    private static final long timeout = 10 * 1_000_000_000L;
    
    public static final int
            ST_NOTLISTENING = 0,
            ST_LISTENING = 1,
            ST_STOPPED = 2;
    
    public final int port;
    private final Thread thread;
    
    private volatile int state = ST_NOTLISTENING;
    
    private final List<Client> newClients = new LinkedList<>();
    private final Deque<Client> verifiedClients = new ArrayDeque<>();
    
    public Listener(int port)
    {
        this.port = port;
        
        Logger.log("Setting up listening socket on port " + port + "...");
        
        thread = new Thread(this, "Listener: port " + port);
        thread.setPriority(5);
        thread.setDaemon(true);
        thread.start();
    }
    
    @Override
    public void run()
    {
        try (ServerSocket serverSocket = new ServerSocket();)
        {
            serverSocket.setPerformancePreferences(0, 1, 0);
            serverSocket.bind(new InetSocketAddress(port));
            
            Logger.log("Listening socket created!");
            
            while (true)
            {
                Socket socket = serverSocket.accept();
                Client client = new Client(socket);
                
                synchronized (newClients)
                {
                    newClients.add(client);
                }
            }
        }
        catch (IOException e)
        {
            Logger.logError(e);
        }
        finally
        {
            state = ST_STOPPED;
        }
    }
    
    public Client acceptClient()
    {
        synchronized (newClients)
        {
            updateClientList();
            return verifiedClients.poll();
        }
    }
    
    //expected to be synchronized
    private void updateClientList()
    {
        Iterator<Client> it = newClients.iterator();
        while (it.hasNext())
        {
            Client c = it.next();
            int s = c.getConnectionState();
            
            if (s == Connection.ST_CONNECTED)
            {
                it.remove();
                verifiedClients.add(c);
            }
            else if (s == Connection.ST_CLOSED || System.nanoTime() - c.creationTime > timeout)
            {
                //it.remove();
                //c.close();
                //Logger.log("Client timed out!");
            }
        }
    }
    
    public int getState()
    {
        return state;
    }
}
