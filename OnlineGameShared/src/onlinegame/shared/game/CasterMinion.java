package onlinegame.shared.game;

import onlinegame.shared.game.actions.Action;
import java.io.IOException;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.game.pathfinder.ActorPath;
import onlinegame.shared.game.stats.Attribs;

/**
 *
 * @author Alfred
 */
public final class CasterMinion extends Minion
{
    public CasterMinion(int id, int team, ActorPath path, Action action, Attribs attribs)
    {
        super(id, team, path, action, attribs);
    }
    protected CasterMinion(BitInput in, int id, CasterMinion prev) throws IOException
    {
        super(in, id, prev);
    }
    
    @Override
    public int getTypeId()
    {
        return GameProtocol.E_MINION_CASTER;
    }
}
