package onlinegame.server;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import onlinegame.server.rooms.ChatRoom;
import onlinegame.server.rooms.LobbyManager;
import onlinegame.shared.Logger;
import onlinegame.shared.net.Protocol;
import onlinegame.shared.net.ConnectionListener;
import onlinegame.shared.net.OutputMessage;

/**
 *
 * @author Alfred
 */
public final class OnlineGameServer implements Runnable, ConnectionListener
{
    private static OnlineGameServer server;
    
    public static final ChatRoom globalChat = new ChatRoom("Global Chat", "global");
    
    //private static final Object mutex = new Object();
    
    private Listener listener;
    
    private final List<Client> clients = new LinkedList<>();
    //private final List<Client> clients2 = new ArrayList<>();
    
    public static void main(String[] args)
    {
        server = new OnlineGameServer();
    }
    
    public static OnlineGameServer getInstance()
    {
        return server;
    }
    
    private OnlineGameServer()
    {
        Thread thread = new Thread(this, "Main Thread");
        thread.setPriority(5);
        thread.setDaemon(false);
        thread.start();
    }
    
    @Override
    public void run()
    {
        Logger.log("Starting server...");
        
        ServerEncryption.touch();
        
        listener = new Listener(Protocol.DEFAULT_PORT);
        
        long time = System.nanoTime();
        final long tickTime = 50_000_000L; //50 ms
        
        while (true)
        {
            long newTime = System.nanoTime();
            long delta = newTime - time;
            while (delta < tickTime - 1_500_000)
            {
                //wait until next tick
                long sleepTime = tickTime - delta;
                try
                {
                    Thread.sleep(sleepTime / 1_000_000L, (int)(sleepTime % 1_000_000));
                }
                catch (InterruptedException e) {}
                
                newTime = System.nanoTime();
                delta = newTime - time;
            }
            time += tickTime;
            if (time < newTime - tickTime)
            {
                //dropped tick
                time = newTime;
            }
            
            //accept new clients
            Client c;
            while ((c = listener.acceptClient()) != null)
            {
                Logger.log(c.getAddress() + " connected.");
                clients.add(c);
            }
            
            //update and check all clients
            Iterator<Client> itr = clients.iterator();
            while (itr.hasNext())
            {
                c = itr.next();
                
                c.update();
                
                if (c.isClosed())
                {
                    Logger.log(c.getAddress() + " disconnected.");
                    itr.remove();
                }
            }
            
            LobbyManager.update();
            
            //send lobby list updates
            if (LobbyManager.isChanged())
            {
                OutputMessage msg = MessageBuilder.lobbyList();
                sendToAllNotInLobby(msg);
            }
            
            //flush all client connections
            for (Client cc : clients)
            {
                cc.flush();
            }
            
            /*synchronized (clients2)
            {
                clients2.clear();
                clients2.addAll(clients);
            }*/
            
            /*synchronized (mutex)
            {
                try
                {
                    mutex.wait(50);
                }
                catch (InterruptedException e) {}
            }*/
        }
    }
    
    private void sendToAll(OutputMessage msg)
    {
        for (int i = 0; i < clients.size(); i++)
        {
            Client c = clients.get(i);
            c.sendMessage(msg);
        }
    }
    
    private void sendToAllNotInLobby(OutputMessage msg)
    {
        for (int i = 0; i < clients.size(); i++)
        {
            Client c = clients.get(i);
            
            if (c.getSession() != null && c.getSession().getLobby() == null)
            {
                c.sendMessage(msg);
            }
        }
    }
    
    @Override
    public void newMessage()
    {
        /*synchronized (mutex)
        {
            mutex.notifyAll();
        }*/
    }
    
    public static void wakeup()
    {
        getInstance().newMessage();
    }
}



















