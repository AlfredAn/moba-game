package onlinegame.server.rooms;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import onlinegame.server.MessageBuilder;
import onlinegame.server.account.Session;
import onlinegame.shared.Logger;
import onlinegame.shared.net.OutputMessage;

/**
 *
 * @author Alfred
 */
public final class ChatRoom
{
    private static final AtomicInteger idCounter = new AtomicInteger();
    
    //private static final Map<Integer, WeakReference<ChatRoom>> map = new HashMap<>();
    //private static final Map<WeakReference<ChatRoom>, Integer> reverseMap = new HashMap<>();
    
    //private static final ReferenceQueue<ChatRoom> refq = new ReferenceQueue<>();
    
    /*public static ChatRoom get(int id)
    {
        WeakReference<ChatRoom> ref;
        synchronized (map)
        {
            ref = map.get(id);
        }
        return ref == null ? null : ref.get();
    }*/
    
    /*@SuppressWarnings("unchecked")
    private static void add(ChatRoom room)
    {
        synchronized (map)
        {
            //remove dead entries
            WeakReference<ChatRoom> ref;
            while ((ref = (WeakReference<ChatRoom>)refq.poll()) != null)
            {
                ChatRoom r = ref.get();
                if (r == null)
                {
                    map.remove(reverseMap.get(ref));
                    reverseMap.remove(ref);
                }
            }
            
            WeakReference<ChatRoom> ref2 = new WeakReference<>(room, refq);
            map.put(room.id, ref2);
            reverseMap.put(ref2, room.id);
        }
    }*/
    
    private final List<Session> sessions = new LinkedList<>();
    
    public final int id;
    public final String name;
    public final String tag;
    
    private boolean isClosed = false;
    
    public ChatRoom(String name, String tag)
    {
        id = idCounter.getAndIncrement();
        this.name = name;
        this.tag = tag;
        
        //add(this);
    }
    
    public void addClient(Session session)
    {
        if (isClosed)
        {
            Logger.logError("ChatRoom is closed!");
            return;
        }
        
        Iterator<Session> itr = sessions.iterator();
        while (itr.hasNext())
        {
            Session s = itr.next();
            
            if (!s.isActive() || s == session)
            {
                itr.remove();
            }
        }
        
        OutputMessage msg = MessageBuilder.chatJoin(this);
        session.getClient().sendMessage(msg);
        
        sessions.add(session);
        
        sendMessage(session.getAccount().getUsername() + " has joined the room.");
    }
    
    public void removeClient(Session session)
    {
        sessions.remove(session);
        
        OutputMessage msg = MessageBuilder.chatLeave(this);
        session.getClient().sendMessage(msg);
        
        sendMessage(session.getAccount().getUsername() + " has left the room.");
    }
    
    public void sendMessage(String message)
    {
        if (isClosed)
        {
            Logger.logError("ChatRoom is closed!");
            return;
        }
        
        OutputMessage msg = MessageBuilder.chatMessage(this, message);
        sendToAll(msg);
    }
    
    public void close()
    {
        if (isClosed) return;
        
        //OutputMessage msg = MessageBuilder.chatLeave(this);
        //sendToAll(msg);
        
        for (Session s : sessions)
        {
            s.leaveChat(this);
        }
        
        isClosed = true;
        
        sessions.clear();
    }
    
    public int numClients()
    {
        return sessions.size();
    }
    
    public boolean isClosed()
    {
        return isClosed;
    }
    
    private void sendToAll(OutputMessage msg)
    {
        Iterator<Session> itr = sessions.iterator();
        while (itr.hasNext())
        {
            Session session = itr.next();
            
            if (session.isActive())
            {
                session.getClient().sendMessage(msg);
            }
            else
            {
                itr.remove();
            }
        }
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            close();
        }
        finally
        {
            super.finalize();
        }
    }
}
