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
public abstract class Minion extends Actor
{
    public Minion(int id, int team, ActorPath path, Action action, Attribs attribs)
    {
        super(id, team, path, action, attribs);
    }
    protected Minion(BitInput in, int id, Minion prev) throws IOException
    {
        super(in, id, prev);
    }
}
