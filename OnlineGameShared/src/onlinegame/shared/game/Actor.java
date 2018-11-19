package onlinegame.shared.game;

import onlinegame.shared.game.actions.Action;
import java.io.IOException;
import java.util.Objects;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.game.pathfinder.ActorPath;
import onlinegame.shared.game.stats.Attribs;
import onlinegame.shared.game.stats.BaseAttribs;

/**
 *
 * @author Alfred
 */
public abstract class Actor extends Entity
{
    public final ActorPath path;
    public final Action action;
    public final BaseAttribs attribs;
    
    public Actor(int id, int team, ActorPath path, Action action, Attribs attribs)
    {
        super(id, team);
        this.path = path;
        this.action = action;
        this.attribs = attribs instanceof BaseAttribs ? (BaseAttribs)attribs : new BaseAttribs(attribs);
    }
    protected Actor(BitInput in, int id, Actor prev) throws IOException
    {
        super(in, id, prev);
        
        if (prev == null || in.readBoolean())
        {
            path = new ActorPath(in);
        }
        else
        {
            path = prev.path;
        }
        
        if (prev == null || in.readBoolean())
        {
            attribs = Attribs.read(in);
        }
        else
        {
            attribs = prev.attribs;
        }
        
        if (prev == null || in.readBoolean())
        {
            action = Action.read(in);
        }
        else
        {
            action = prev.action;
        }
    }
    
    public float getXPos(double time)
    {
        return getXPos(null, time);
    }
    
    public float getYPos(double time)
    {
        return getYPos(null, time);
    }
    
    @Override
    public float getXPos(GameState gs, double time)
    {
        return path.getPathXPos(time, attribs.get(Attribs.MOVE_SPEED));
    }
    
    @Override
    public float getYPos(GameState gs, double time)
    {
        return path.getPathYPos(time, attribs.get(Attribs.MOVE_SPEED));
    }
    
    public int getPathPrevPoint(GameState gs)
    {
        return getPathPrevPoint(gs.currentTime);
    }
    public int getPathPrevPoint(double time)
    {
        return path.getPathPrevPoint(time, attribs.get(Attribs.MOVE_SPEED));
    }
    
    @Override
    protected void writeData(BitOutput out, Entity prev) throws IOException
    {
        super.writeData(out, prev);
        
        if (prev == null)
        {
            path.write(out);
            attribs.write(out);
            Action.write(action, out);
        }
        else
        {
            if (path.equals(((Actor)prev).path))
            {
                out.writeBoolean(false);
            }
            else
            {
                out.writeBoolean(true);
                path.write(out);
            }
            if (attribs.equals(((Actor)prev).attribs))
            {
                out.writeBoolean(false);
            }
            else
            {
                out.writeBoolean(true);
                attribs.write(out);
            }
            if (Objects.equals(action, ((Actor)prev).action))
            {
                out.writeBoolean(false);
            }
            else
            {
                out.writeBoolean(true);
                Action.write(action, out);
            }
        }
    }
    
    @Override
    protected boolean eq(Entity other)
    {
        Actor a = (Actor)other;
        return
                super.eq(a) &&
                path.equals(a.path) &&
                attribs.equals(a.attribs) &&
                Objects.equals(action, a.action);
    }
    
    public final boolean canAttack(Entity other)
    {
        return other instanceof Actor && canAttack((Actor)other);
    }
    public boolean canAttack(Actor other)
    {
        return team != other.team;
    }
}
