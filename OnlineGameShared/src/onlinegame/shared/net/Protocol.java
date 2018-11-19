package onlinegame.shared.net;

/**
 *
 * @author Alfred
 */
public final class Protocol
{
    private Protocol() {}
    
    public static final int DEFAULT_PORT = 15636;
    
    public static final long CLIENT_MAGIC_NUMBER = 0x739a763c9b8d828eL;
    public static final long SERVER_MAGIC_NUMBER = 0x1a25b4aa74ef7546L;
    
    public static final short UDP_ID = (short)0x8b91;
    
    public static final int VERSION = 0;
    
    /*
    All messages are preceded first by the length (ushort) followed by the
    message id (ubyte).
    */
    
    //from client
    public static final byte
            
            /*
            C_REGISTER_REQUEST:
            
            String username
            encrypted
            {
                String password
            }
            */
            C_REGISTER_REQUEST = 0,
            
            /*
            C_LOGIN_REQUEST:
            
            String username
            encrypted
            {
                String password
            }
            */
            C_LOGIN_REQUEST = 1,
            
            /*
            C_CHAT_MSG:
            
            int id
            String msg
            */
            C_CHAT_MSG = 2,
            
            /*
            C_LOBBY_CREATE:
            
            String name
            */
            C_LOBBY_CREATE = 3,
            
            /*
            C_LOBBY_JOIN:
            
            int id
            */
            C_LOBBY_JOIN = 4,
            
            /*
            C_LOBBY_LEAVE:
            
            no payload
            */
            C_LOBBY_LEAVE = 5,
            
            /*
            C_LOBBY_SWITCHTEAM:
            
            no payload
            */
            C_LOBBY_SWITCHTEAM = 6,
            
            /*
            C_LOBBY_STARTCHAMPSELECT:
            
            no payload
            */
            C_LOBBY_STARTCHAMPSELECT = 7,
            
            /*
            C_CHAMPSELECT_SELECT:
            
            short championId
            */
            C_CHAMPSELECT_SELECT = 8,
            
            /*
            C_CHAMPSELECT_LOCK:
            
            no payload
            */
            C_CHAMPSELECT_LOCK = 9;
    
    //from server
    public static final byte
            /*
            S_REGISTER_RESPONSE:
            
            boolean success
            String message
            */
            S_REGISTER_RESPONSE = 0,
            
            /*
            S_LOGIN_RESPONSE:
            
            boolean success
            String message
            if (success)
            {
                String username
                short sessionHash
            }
            */
            S_LOGIN_RESPONSE = 1,
            
            /*
            S_INIT:
            
            Object publicKey (for encryption)
            */
            S_INIT = 2,
            
            /*
            S_CHAT_JOIN:
            
            int id
            String name
            String tag
            */
            S_CHAT_JOIN = 3,
            
            /*
            S_CHAT_LEAVE:
            
            int id
            */
            S_CHAT_LEAVE = 4,
            
            /*
            S_CHAT_MSG:
            
            int id
            String msg
            */
            S_CHAT_MSG = 5,
            
            /*
            S_LOBBY_JOIN:
            also send S_LOBBY_UPDATE along with this
            
            boolean success
            String message
            */
            S_LOBBY_JOIN = 6,
            
            /*
            S_LOBBY_LEAVE:
            
            no payload
            */
            S_LOBBY_LEAVE = 7,
            
            /*
            S_LOBBY_UPDATE:
            
            String lobbyName
            String owner
            byte players
            byte maxPlayers
            String gameMode
            String map
            
            for each player in lobby
            {
                byte team (0 or 1)
                String username
            }
            */
            S_LOBBY_UPDATE = 8,
            
            /*
            S_LOBBY_LIST:
            
            short numberOfLobbies
            
            for each lobby
            {
                int id
                String lobbyName
                String owner
                byte players
                byte maxPlayers
                String gameMode
                String map
            }
            */
            S_LOBBY_LIST = 9,
            
            /*
            S_CHAMPSELECT_START:
            
            byte yourTeam (0 or 1)
            byte playersInYourTeam
            byte playersInEnemyTeam
            
            for each player in your team
            {
                String username
            }
            for each player in enemy team
            {
                String username
            }
            */
            S_CHAMPSELECT_START = 10,
            
            /*
            S_CHAMPSELECT_UPDATE:
            
            String infoText
            int timeLeft (in milliseconds)
            
            for each player in your team
            {
                byte state (0 = waiting, 1 = picking, 2 = locked in)
                short championId
            }
            for each player in enemy team
            {
                byte state (0 = waiting, 1 = picking, 2 = locked in)
                short championId
            }
            */
            S_CHAMPSELECT_UPDATE = 11,
            
            /*
            S_GAME_STARTLOAD:
            
            byte yourTeam (0 or 1)
            byte playersInYourTeam
            byte playersInEnemyTeam
            
            for each player in team 0 (blue)
            {
                String username
                short championId
            }
            for each player in team 1 (red)
            {
                String username
                short championId
            }
            */
            S_GAME_STARTLOAD = 12;
}
