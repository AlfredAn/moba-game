package onlinegame.server.rooms;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import onlinegame.server.account.Session;

/**
 *
 * @author Alfred
 */
public final class LobbyManager
{
    private LobbyManager() {}
    
    private static final List<Lobby> lobbyList = new ArrayList<>();
    private static final Map<Integer, Lobby> lobbyMap = new HashMap<>();
    
    private static boolean changed = true;
    
    public static Lobby createLobby(String name, Session owner)
    {
        Lobby lobby = new Lobby(name, owner);
        
        synchronized (lobbyList)
        {
            lobbyList.add(lobby);
            lobbyMap.put(lobby.id, lobby);
            
            lobby.addPlayer(owner);
            
            changed = true;
        }
        
        return lobby;
    }
    
    public static void update()
    {
        synchronized (lobbyList)
        {
            for (int i = 0; i < lobbyList.size(); i++)
            {
                Lobby l = lobbyList.get(i);
                l.update();
                if (lobbyList.get(i) != l)
                {
                    i--;
                }
            }
        }
    }
    
    public static Lobby getLobby(int id)
    {
        synchronized (lobbyList)
        {
            return lobbyMap.get(id);
        }
    }
    
    public static void removeLobby(Lobby lobby)
    {
        synchronized (lobbyList)
        {
            lobby.close();
            lobbyList.remove(lobby);
            lobbyMap.remove(lobby.id);
            
            changed = true;
        }
    }
    
    public static boolean isChanged()
    {
        synchronized (lobbyList)
        {
            boolean b = changed;
            changed = false;
            return b;
        }
    }
    
    public static void notifyChange()
    {
        synchronized (lobbyList)
        {
            changed = true;
        }
    }
    
    public static void writeList(DataOutput out) throws IOException
    {
        synchronized (lobbyList)
        {
            int len = 0;
            for (int i = 0; i < lobbyList.size(); i++)
            {
                Lobby l = lobbyList.get(i);
                if (l.getStage() == null && !l.isClosed())
                {
                    len++;
                }
            }
            
            out.writeShort(len);
            
            for (int i = 0; i < lobbyList.size(); i++)
            {
                Lobby l = lobbyList.get(i);
                if (l.getStage() != null || l.isClosed())
                {
                    continue;
                }
                
                out.writeInt(l.id);
                out.writeUTF(l.getName());
                out.writeUTF(l.getOwner().getAccount().getUsername());
                out.writeByte(l.getPlayerCount());
                out.writeByte(l.getMaxPlayers());
                out.writeUTF(l.getGameMode());
                out.writeUTF(l.getMap().name);
            }
        }
    }
}
