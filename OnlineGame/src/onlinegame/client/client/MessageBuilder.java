package onlinegame.client.client;

import java.io.DataOutputStream;
import java.io.IOException;
import onlinegame.shared.db.ChampionDB.ChampionData;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class MessageBuilder
{
    private MessageBuilder() {}
    
    public static OutputMessage registerRequest(String username, String password, ClientEncryption crypt)
    {
        OutputMessage msg = new OutputMessage(Protocol.C_REGISTER_REQUEST);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeUTF(username);
            crypt.writeString(password, out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage loginRequest(String username, String password, ClientEncryption crypt)
    {
        OutputMessage msg = new OutputMessage(Protocol.C_LOGIN_REQUEST);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeUTF(username);
            crypt.writeString(password, out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage chatMessage(ChatRoom room, String message)
    {
        OutputMessage msg = new OutputMessage(Protocol.C_CHAT_MSG);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeInt(room.id);
            out.writeUTF(message);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage lobbyCreate(String lobbyName)
    {
        OutputMessage msg = new OutputMessage(Protocol.C_LOBBY_CREATE);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeUTF(lobbyName);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage lobbyJoin(int id)
    {
        OutputMessage msg = new OutputMessage(Protocol.C_LOBBY_JOIN);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeInt(id);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage lobbyLeave()
    {
        return new OutputMessage(Protocol.C_LOBBY_LEAVE);
    }
    
    public static OutputMessage lobbySwitchTeam()
    {
        return new OutputMessage(Protocol.C_LOBBY_SWITCHTEAM);
    }
    
    public static OutputMessage lobbyStartChampSelect()
    {
        return new OutputMessage(Protocol.C_LOBBY_STARTCHAMPSELECT);
    }
    
    public static OutputMessage champSelectSelect(ChampionData champion)
    {
        OutputMessage msg = new OutputMessage(Protocol.C_CHAMPSELECT_SELECT);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeShort((short)champion.id);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage champSelectLock()
    {
        return new OutputMessage(Protocol.C_CHAMPSELECT_LOCK);
    }
}
