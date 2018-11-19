package onlinegame.server.rooms;

import java.io.IOException;
import onlinegame.server.account.Session;
import onlinegame.shared.net.InputMessage;

/**
 *
 * @author Alfred
 */
public abstract class LobbyStage
{
    public final Lobby lobby;
    
    LobbyStage(Lobby lobby)
    {
        this.lobby = lobby;
    }
    
    public void update() {}
    public void close() {}
    
    public void readMessage(InputMessage msg, Session sender) throws IOException {}
}
