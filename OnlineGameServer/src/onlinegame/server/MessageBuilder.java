package onlinegame.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import onlinegame.server.game.PlayerInfo;
import onlinegame.server.rooms.ChampionSelect;
import onlinegame.server.rooms.ChampionSelect.CSPlayer;
import onlinegame.server.rooms.ChatRoom;
import onlinegame.server.rooms.Lobby;
import onlinegame.server.rooms.LobbyManager;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class MessageBuilder
{
    private MessageBuilder() {}
    
    public static OutputMessage registerResponse(boolean success, String message)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_REGISTER_RESPONSE);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeBoolean(success);
            out.writeUTF(message);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage loginResponseFail(String message)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_LOGIN_RESPONSE);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeBoolean(false);
            out.writeUTF(message);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage loginResponseSuccess(String message, String username, short sessionHash)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_LOGIN_RESPONSE);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeBoolean(true);
            out.writeUTF(message);
            out.writeUTF(username);
            out.writeShort(sessionHash);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage init(PublicKey key)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_INIT);
        
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(msg.getStream());

            out.writeUnshared(ServerEncryption.getPublicKey());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage chatJoin(ChatRoom chat)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_CHAT_JOIN);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeInt(chat.id);
            out.writeUTF(chat.name);
            out.writeUTF(chat.tag);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage chatLeave(ChatRoom chat)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_CHAT_LEAVE);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeInt(chat.id);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage chatMessage(ChatRoom chat, String message)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_CHAT_MSG);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeInt(chat.id);
            out.writeUTF(message);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage lobbyJoin(boolean success, String message)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_LOBBY_JOIN);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeBoolean(success);
            out.writeUTF(message);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage lobbyLeave()
    {
        OutputMessage msg = new OutputMessage(Protocol.S_LOBBY_LEAVE);
        return msg;
    }
    
    public static OutputMessage lobbyUpdate(Lobby lobby)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_LOBBY_UPDATE);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeUTF(lobby.getName());
            out.writeUTF(lobby.getOwner().getAccount().getUsername());
            out.writeByte(lobby.getPlayerCount());
            out.writeByte(lobby.getMaxPlayers());
            out.writeUTF(lobby.getGameMode());
            out.writeUTF(lobby.getMap().name);
            
            //for each team
            for (int t = 0; t < 2; t++)
            {
                //for each player in team
                for (int i = 0; i < lobby.getPlayerCount(t); i++)
                {
                    out.writeByte(t);
                    out.writeUTF(lobby.getPlayer(t, i).getAccount().getUsername());
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage lobbyList()
    {
        OutputMessage msg = new OutputMessage(Protocol.S_LOBBY_LIST);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            LobbyManager.writeList(out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage champSelectStart(ChampionSelect cSelect, int team)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_CHAMPSELECT_START);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            cSelect.startMessage(out, team);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage champSelectUpdate(ChampionSelect cSelect, CSPlayer player)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_CHAMPSELECT_UPDATE);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            cSelect.updateMessage(out, player);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
    
    public static OutputMessage gameStartLoad(int yourTeam, PlayerInfo[][] playerInfo)
    {
        OutputMessage msg = new OutputMessage(Protocol.S_GAME_STARTLOAD);
        DataOutputStream out = msg.getDataStream();
        
        try
        {
            out.writeByte(yourTeam);
            out.writeByte(playerInfo[0].length);
            out.writeByte(playerInfo[1].length);
            
            for (int t = 0; t < 2; t++)
            {
                for (int p = 0; p < playerInfo[t].length; p++)
                {
                    PlayerInfo player = playerInfo[t][p];
                    out.writeUTF(player.account.getUsername());
                    out.writeShort(player.champion.id);
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        return msg;
    }
}
