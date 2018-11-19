package onlinegame.client.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import onlinegame.shared.Logger;
import onlinegame.shared.net.Connection;
import onlinegame.shared.net.GameProtocolException;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public class Network
{
    public final Client client;
    private final Object mutex = new Object();
    
    private ClientEncryption crypt;
    
    private Connection connection;
    private final List<WeakReference<NetworkListener>> listeners = new LinkedList<>();
    private final List<NetworkListener> tempListeners = new ArrayList<>();
    
    private final Map<String, ChatRoom> chatRoomTagMap = new HashMap<>();
    private final Map<Integer, ChatRoom> chatRoomIdMap = new HashMap<>();
    
    private String username = "<undefined>";
    private short sessionHash = 0;
    
    Network(Client client)
    {
        this.client = client;
    }
    
    public InetAddress getServerAddress()
    {
        return connection.getAddress();
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    public void setSessionHash(short sessionHash)
    {
        this.sessionHash = sessionHash;
    }
    
    public short getSessionHash()
    {
        return sessionHash;
    }
    
    public ChatRoom getChatRoom(int id)
    {
        synchronized (chatRoomIdMap)
        {
            ChatRoom r = chatRoomIdMap.get(id);
            return r;
        }
    }
    
    public ChatRoom getChatRoom(String tag)
    {
        synchronized (chatRoomIdMap)
        {
            ChatRoom r = chatRoomTagMap.get(tag);
            return r;
        }
    }
    
    public void addListener(NetworkListener listener)
    {
        synchronized (listeners)
        {
            tempListeners.add(listener);
        }
    }
    
    void update()
    {
        try
        {
            readMessages();
        }
        catch (IOException e)
        {
            Logger.logError(e);
            close(e);
        }
        
        if (connection == null)
        {
            close();
        }
        else
        {
            connection.flush();
        }
    }
    
    public void sendMessage(OutputMessage msg)
    {
        if (connection == null)
        {
            return;
        }
        
        connection.sendMessage(msg);
    }
    
    private void readMessages() throws IOException
    {
        //read all pending messages
        
        if (connection == null)
        {
            return;
        }
        
        InputMessage msg = null;
        while ((msg = connection.readMessage()) != null)
        {
            readMessage(msg);
            synchronized (listeners)
            {
                if (!tempListeners.isEmpty())
                {
                    for (NetworkListener l : tempListeners)
                    {
                        listeners.add(new WeakReference<>(l));
                    }
                    tempListeners.clear();
                }
                
                Iterator<WeakReference<NetworkListener>> itr = listeners.iterator();
                while (itr.hasNext())
                {
                    WeakReference<NetworkListener> ref = itr.next();
                    NetworkListener l = ref.get();
                    
                    if (l == null || !l.isActive())
                    {
                        itr.remove();
                    }
                    else
                    {
                        l.readMessage(msg);
                    }
                }
            }
        }
    }
    
    private void readMessage(InputMessage msg) throws IOException
    {
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.S_INIT:
                ObjectInputStream oin = new ObjectInputStream(msg.getStream());
                
                try
                {
                    Object o = oin.readUnshared();
                    
                    if (!(o instanceof PublicKey))
                    {
                        throw new GameProtocolException("Invalid type: Expected PublicKey, got " + o.getClass().getName() + ".");
                    }
                    
                    crypt = new ClientEncryption((PublicKey)o);
                    Logger.log("INIT!!");
                }
                catch (ClassNotFoundException e)
                {
                    throw new GameProtocolException("Invalid type.", e);
                }
                break;
            case Protocol.S_CHAT_JOIN:
                in = msg.getDataStream();
                
                int id = in.readInt();
                String name = in.readUTF();
                String tag = in.readUTF();
                
                Logger.log("Joined room: " + name + " (" + id + ")");
                
                synchronized (chatRoomIdMap)
                {
                    ChatRoom room = new ChatRoom(this, id, name, tag);
                    chatRoomIdMap.put(id, room);
                    chatRoomTagMap.put(tag, room);
                    Logger.log("Chat rooms: " + chatRoomIdMap.size());
                }
                break;
            case Protocol.S_CHAT_MSG:
                in = msg.getDataStream();
                int cid = in.readInt();
                String message = in.readUTF();
                
                ChatRoom room = getChatRoom(cid);
                
                if (room == null)
                {
                    Logger.logError("S_CHAT_MSG: Not in room " + cid);
                    break;
                }
                
                room.message(message);
                break;
            case Protocol.S_CHAT_LEAVE:
                in = msg.getDataStream();
                cid = in.readInt();
                
                room = getChatRoom(cid);
                
                if (room == null)
                {
                    Logger.logError("S_CHAT_LEAVE: Not in room " + cid);
                    break;
                }
                
                room.leave();
                
                synchronized (chatRoomIdMap)
                {
                    chatRoomIdMap.remove(cid);

                    if (getChatRoom(room.tag) == room)
                    {
                        chatRoomTagMap.remove(room.tag);
                    }
                }
                break;
        }
    }
    
    /*public boolean register(TaskListener listener, String username, String password)
    {
        try
        {
            if (tasks.size() == 1)
            {
                RegisterTask task = new RegisterTask(this, username, password);
                task.listener(listener);
                tasks.add(task);
                return true;
            }
        }
        catch (IOException e)
        {
            Logger.logError(e);
        }
        
        Logger.logError("Unable to start register task: another task is already in progress");
        
        return false;
    }*/
    
    public void connect(String hostname)
    {
        connect(hostname, Protocol.DEFAULT_PORT);
    }
    public void connect(String hostname, int port)
    {
        synchronized (mutex)
        {
            close();
            connection = new Connection(hostname, port);
        }
    }
    
    private String errMsg = null;
    
    public void close()
    {
        close("Connection closed.");
    }
    public void close(Throwable error)
    {
        close(error.getClass().getSimpleName() + ": " + error.getMessage());
    }
    public void close(String errorMsg)
    {
        synchronized (mutex)
        {
            if (connection != null)
            {
                connection.close();
                connection = null;
            }
            
            crypt = null;
            
            synchronized (listeners)
            {
                listeners.clear();
            }
            errMsg = errorMsg;
        }
    }
    
    public ClientEncryption getCrypt()
    {
        return crypt;
    }
    
    public String getErrorMessage()
    {
        if (isConnected())
        {
            return null;
        }
        
        String err;
        synchronized (mutex)
        {
            err = connection == null ? null : connection.getErrorMessage();
        }
        
        if (err == null)
        {
            if (errMsg == null)
            {
                return "You have been disconnected.";
            }
            else
            {
                return errMsg;
            }
        }
        else
        {
            return err;
        }
    }
    
    public boolean isConnected()
    {
        return connection != null && connection.isConnected();
    }
    
    public int getConnectionState()
    {
        return connection == null ? Connection.ST_NOTCONNECTED : connection.getState();
    }
}
