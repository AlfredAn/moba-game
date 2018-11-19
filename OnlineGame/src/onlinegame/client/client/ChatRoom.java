package onlinegame.client.client;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import onlinegame.shared.ArrayDequeList;
import onlinegame.shared.net.OutputMessage;

/**
 *
 * @author Alfred
 */
public final class ChatRoom
{
    private static final int capacity = 1024;
    private final ArrayDequeList<String> messages = new ArrayDequeList<>();
    private final List<WeakReference<ChatListener>> listeners = new LinkedList<>();
    
    public final Network net;
    
    public final int id;
    public final String name, tag;
    
    private boolean active = true;
    
    public ChatRoom(Network net, int id, String name, String tag)
    {
        this.net = net;
        this.id = id;
        this.name = name;
        this.tag = tag;
    }
    
    public void addListener(ChatListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(new WeakReference<>(listener));
            
            for (int i = 0; i < messages.size(); i++)
            {
                listener.readChatMessage(messages.get(i));
            }
        }
    }
    
    public void sendMessage(String message)
    {
        OutputMessage msg = MessageBuilder.chatMessage(this, message);
        net.sendMessage(msg);
    }
    
    void message(String msg)
    {
        addMessage(msg);
    }
    
    void leave()
    {
        active = false;
    }
    
    public boolean isActive()
    {
        return active;
    }
    
    public void addMessage(String msg)
    {
        if (messages.size() == capacity)
        {
            messages.poll();
        }
        messages.add(msg);
        
        synchronized (listeners)
        {
            Iterator<WeakReference<ChatListener>> itr = listeners.iterator();
            while (itr.hasNext())
            {
                WeakReference<ChatListener> ref = itr.next();
                ChatListener l = ref.get();
                
                if (l == null || !l.isCLActive())
                {
                    itr.remove();
                }
                else
                {
                    l.readChatMessage(msg);
                }
            }
        }
    }
    
    public String getFromStart(int i)
    {
        return messages.get(i);
    }
    
    public String getFromEnd(int i)
    {
        return messages.get(messages.size() - i - 1);
    }
    
    public int size()
    {
        return messages.size();
    }
    
    public int capacity()
    {
        return capacity;
    }
}
