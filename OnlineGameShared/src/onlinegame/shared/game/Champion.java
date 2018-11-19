package onlinegame.shared.game;

import onlinegame.shared.game.actions.Action;
import java.io.IOException;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.db.ChampionDB;
import onlinegame.shared.db.ChampionDB.ChampionData;
import onlinegame.shared.game.pathfinder.ActorPath;
import onlinegame.shared.game.stats.Attribs;
import onlinegame.shared.net.GameProtocolException;

/**
 *
 * @author Alfred
 */
public final class Champion extends Actor
{
    public final ChampionData champion;
    public final String username;
    
    public Champion(int id, int team, ActorPath path, Action action, Attribs attribs, ChampionData champion, String username)
    {
        super(id, team, path, action, attribs);
        this.champion = champion;
        this.username = username;
    }
    protected Champion(BitInput in, int id, Champion prev) throws IOException
    {
        super(in, id, prev);
        champion = prev == null ? ChampionDB.get(in.readInt(16)) : prev.champion;
        username = prev == null ? in.readString() : prev.username;
        
        if (champion == null)
        {
            throw new GameProtocolException("Invalid champion id!");
        }
    }
    
    @Override
    public int getTypeId()
    {
        return GameProtocol.E_CHAMPION;
    }
    
    @Override
    protected void writeData(BitOutput out, Entity prev) throws IOException
    {
        super.writeData(out, prev);
        
        if (prev == null)
        {
            out.writeInt(champion.id, 16);
            out.writeString(username);
        }
    }
    
    @Override
    protected boolean eq(Entity other)
    {
        Champion c = (Champion)other;
        return 
                super.eq(c) &&
                champion == c.champion;
    }
}
