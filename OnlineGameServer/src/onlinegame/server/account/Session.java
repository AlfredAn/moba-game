package onlinegame.server.account;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import onlinegame.server.Client;
import onlinegame.server.MessageBuilder;
import onlinegame.server.game.GameMain;
import onlinegame.server.rooms.ChatRoom;
import onlinegame.server.rooms.Lobby;
import onlinegame.server.rooms.LobbyManager;
import onlinegame.server.rooms.LobbyStage;
import onlinegame.shared.Logger;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class Session
{
    private final Account account;
    private final Client client;
    
    private final List<ChatRoom> chatRooms = new ArrayList<>();
    private Lobby lobby = null;
    private GameMain game = null;
    
    private static final Random random = new Random();
    public final short sessionHash;
    
    Session(Account account, Client client)
    {
        this.account = account;
        this.client = client;
        
        short h = (short)random.nextInt(0x10000);
        sessionHash = h == 0 ? 1 : h; //cannot be 0
    }
    
    public void joinChat(ChatRoom room)
    {
        room.addClient(this);
        chatRooms.add(room);
    }
    
    public void leaveChat(ChatRoom room)
    {
        room.removeClient(this);
        chatRooms.remove(room);
    }
    
    public Lobby getLobby()
    {
        return lobby;
    }
    
    public void setGame(GameMain game)
    {
        this.game = game;
        if (lobby != null && game != null)
        {
            Logger.logError("Started game when lobby != null");
            lobby.removePlayer(this);
            lobby = null;
        }
    }
    
    public GameMain getGame()
    {
        return game;
    }
    
    public void update() {}
    
    public void readMessage(InputMessage msg) throws IOException
    {
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.C_CHAT_MSG:
                in = msg.getDataStream();
                
                int id = in.readInt();
                String message = in.readUTF();
                
                ChatRoom room = getChatRoom(id);
                
                if (room == null)
                {
                    Logger.logError("Invalid chat room: " + id);
                    break;
                }
                
                if (message.length() > 1024)
                {
                    message = message.substring(0, 1024);
                }
                room.sendMessage(account.getUsername() + ": " + message);
                break;
            case Protocol.C_LOBBY_CREATE:
                if (lobby != null)
                {
                    OutputMessage omsg = MessageBuilder.lobbyJoin(false, "You are already in a lobby!");
                    client.sendMessage(omsg);
                    break;
                }
                
                in = msg.getDataStream();
                
                String name = in.readUTF();
                
                if (name.length() > 32)
                {
                    name = name.substring(0, 32);
                }
                else if (name.length() == 0)
                {
                    name = SharedUtil.possessive(account.getUsername()) + " game";
                }
                lobby = LobbyManager.createLobby(name, this);
                break;
            case Protocol.C_LOBBY_JOIN:
                if (lobby != null)
                {
                    OutputMessage omsg = MessageBuilder.lobbyJoin(false, "You are already in a lobby!");
                    client.sendMessage(omsg);
                    break;
                }
                
                in = msg.getDataStream();
                
                id = in.readInt();
                
                Lobby l = LobbyManager.getLobby(id);
                String err = null;
                
                if (l == null)
                {
                    err = "Lobby not found.";
                }
                else if (l.isFull())
                {
                    err = "This lobby is already full!";
                }
                else if (!l.addPlayer(this))
                {
                    err = "Unable to join lobby.";
                }
                
                if (err == null)
                {
                    lobby = l;
                }
                else
                {
                    OutputMessage omsg = MessageBuilder.lobbyJoin(false, err);
                    client.sendMessage(omsg);
                }
                break;
            case Protocol.C_LOBBY_LEAVE:
                if (lobby != null)
                {
                    lobby.removePlayer(this);
                    lobby = null;
                    
                    client.sendMessage(MessageBuilder.lobbyList());
                }
                else
                {
                    Logger.logError("C_LOBBY_LEAVE when not in a lobby.");
                }
                break;
            case Protocol.C_LOBBY_SWITCHTEAM:
                if (lobby != null)
                {
                    lobby.switchTeam(this);
                }
                else
                {
                    Logger.logError("C_LOBBY_SWITCHTEAM when not in a lobby.");
                }
                break;
            case Protocol.C_LOBBY_STARTCHAMPSELECT:
                if (lobby != null)
                {
                    lobby.startChampSelect(this);
                }
                else
                {
                    Logger.logError("C_LOBBY_STARTCHAMPSELECT when not in a lobby.");
                }
                break;
            default:
                if (lobby != null)
                {
                    LobbyStage stage = lobby.getStage();
                    if (stage != null)
                    {
                        stage.readMessage(msg, this);
                    }
                }
                break;
        }
    }
    
    public ChatRoom getChatRoom(int id)
    {
        for (int i = 0; i < chatRooms.size(); i++)
        {
            ChatRoom room = chatRooms.get(i);
            if (room.id == id)
            {
                return room;
            }
        }
        return null;
    }
    
    public Account getAccount()
    {
        return account;
    }
    
    public Client getClient()
    {
        return client;
    }
    
    public boolean isActive()
    {
        return client != null && client.isConnected() && client.getSession() == this;
    }
    
    public boolean stop()
    {
        for (int i = 0; i < chatRooms.size(); i++)
        {
            chatRooms.get(i).removeClient(this);
        }
        
        if (lobby != null)
        {
            lobby.removePlayer(this);
            lobby = null;
        }
        
        account.session = null;
        
        if (!client.isClosed())
        {
            client.close();
        }
        
        return true;
    }
}
