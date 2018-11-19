package onlinegame.client.client;

import java.io.IOException;
import onlinegame.shared.net.InputMessage;

/**
 *
 * @author Alfred
 */
public interface NetworkListener
{
    public void readMessage(InputMessage msg) throws IOException;
    
    public boolean isActive();
}
