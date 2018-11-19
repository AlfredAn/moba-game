package onlinegame.server.game;

import java.net.InetAddress;
import onlinegame.server.account.Account;
import onlinegame.server.account.Session;
import onlinegame.shared.db.ChampionDB.ChampionData;

/**
 *
 * @author Alfred
 */
public final class PlayerInfo
{
    public final int team;
    public final int playerId;
    public final Account account;
    public final ChampionData champion;
    
    public PlayerInfo(Session session, ChampionData champion, int team, int playerId)
    {
        account = session.getAccount();
        this.champion = champion;
        this.team = team;
        this.playerId = playerId;
    }
    
    public InetAddress getAdress()
    {
        Session s = account.getSession();
        return s == null ? null : s.getClient().getAddress();
    }
    
    public short getSessionHash()
    {
        Session s = account.getSession();
        return s == null ? 0 : s.sessionHash;
    }
}
