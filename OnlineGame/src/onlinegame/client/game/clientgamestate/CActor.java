package onlinegame.client.game.clientgamestate;

import onlinegame.shared.game.Actor;
import onlinegame.shared.game.GameState;
import onlinegame.shared.game.actions.Action;
import onlinegame.shared.game.pathfinder.ActorPath;
import onlinegame.shared.game.stats.Attribs;
import onlinegame.shared.game.stats.BaseAttribs;

/**
 *
 * @author Alfred
 */
public abstract class CActor extends CEntity
{
    protected CActor(CGameState game, GameState gs, Actor a, Actor next, Class<? extends Actor> validStateClass)
    {
        super(game, gs, a, next, validStateClass);
        
        updatePathAndAction();
    }
    
    protected ActorPath path;
    protected Action action;
    
    @Override
    public void update(double delta)
    {
        updatePathAndAction();
    }
    
    protected final void updatePathAndAction()
    {
        Actor st = getState();
        Actor next = (Actor)nextState;
        
        if (next == null)
        {
            xPos = st.getXPos(game.getCurrentTime());
            yPos = st.getYPos(game.getCurrentTime());
            path = st.path;
            
            action = st.action;
        }
        else
        {
            if (game.getCurrentTime() >= next.path.startTime)
            {
                xPos = next.getXPos(game.getCurrentTime());
                yPos = next.getYPos(game.getCurrentTime());
                path = next.path;
            }
            else
            {
                xPos = st.getXPos(game.getCurrentTime());
                yPos = st.getYPos(game.getCurrentTime());
                path = st.path;
            }
            
            if (st.action != null && game.getCurrentTime() < st.action.endTime)
            {
                action = st.action;
            }
            else if (next.action == null)
            {
                action = null;
            }
            else if (game.getCurrentTime() >= next.action.startTime)
            {
                action = next.action;
            }
        }
    }
    
    public final boolean canAttack(CEntity other)
    {
        return getState().canAttack(other.getState());
    }
    public final boolean canAttack(CActor other)
    {
        return getState().canAttack(other.getState());
    }
    
    public ActorPath getPath()
    {
        return path;
    }
    
    public Action getAction()
    {
        return action;
    }
    
    public BaseAttribs getAttribs()
    {
        return getState().attribs;
    }
    
    public int getPathPrevPoint()
    {
        return path.getPathPrevPoint(game.getCurrentTime(), getAttribs().get(Attribs.MOVE_SPEED));
    }
    
    @Override
    public boolean isClickable()
    {
        return true;
    }
    
    @Override
    public Actor getState()
    {
        return (Actor)state;
    }
}




























