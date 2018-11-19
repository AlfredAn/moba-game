package onlinegame.shared.net.udp;

import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;

/**
 *
 * @author Alfred
 */
public interface UDPConnection
{
    public abstract void setNotify(Object o);
    
    public abstract InputMessage readMessage();
    public abstract void sendMessage(OutputMessage msg);
    public abstract void flush();
    public abstract void close();
    public abstract boolean isClosed();
    public abstract boolean isConnected();
}
