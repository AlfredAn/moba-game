package onlinegame.server.game.engine;

import onlinegame.server.game.engine.projectiles.STargetedProjectile;
import onlinegame.shared.GameUtil;
import onlinegame.shared.MathUtil;
import onlinegame.shared.game.actions.Action;
import onlinegame.shared.game.Actor;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.actions.AttackAction;
import onlinegame.shared.game.pathfinder.ActorPath;
import onlinegame.shared.game.pathfinder.Node;
import onlinegame.shared.game.stats.AttribBuilder;
import onlinegame.shared.game.stats.Attribs;
import onlinegame.shared.game.stats.BaseAttribs;
import onlinegame.server.game.engine.status.SActorAttribs;
import org.joml.Vector2d;
import static onlinegame.shared.game.stats.Attribs.*;

/**
 *
 * @author Alfred
 */
public abstract class SActor extends SEntity
{
    public static final BaseAttribs baseActor = new AttribBuilder()
            .set(MOVE_SPEED, 3.0f)
            
            .set(ATTACK_RANGE, 5.5f) //7.0f
            .set(ATTACK_SPEED, 1.5f)
            
            .set(ATTACK_PREHIT_OFFSET_FACTOR, 0.35f)
            .set(ATTACK_POSTHIT_OFFSET_FACTOR, 0.55f)
            .set(ATTACK_RELOAD_OFFSET_FACTOR, 0.85f)
            
            .set(ATTACK_TIME_FACTOR, 0.70f)
            
            .set(ATTACK_PROJECTILE_SPEED, 10.0f)
            
            .set(RES_HEALTH_MAX, 500)
            .set(RES_MANA_MAX, 200)
            
            .finish();
    
    protected ActorPath path;
    protected SActor target;
    protected Action action;
    
    private int targetPathUpdateTick;
    private double startAttackCmdTime = Double.NEGATIVE_INFINITY;
    
    protected float hitRadius = 0.3f;
    
    public final SActorAttribs attribs;
    
    private int cmdId = GameProtocol.CMD_NONE;
    private double cmdTime;
    private float cmdX, cmdY;
    private SActor cmdTarget = null;
    
    private double nextAttackTime = 0f;
    
    private boolean hasFired = false;
    
    public SActor(SGameState game, float xPos, float yPos, int team, BaseAttribs attribs)
    {
        super(game, xPos, yPos, team);
        path = new ActorPath(xPos, yPos, game.getCurrentTime());
        this.attribs = new SActorAttribs(attribs);
    }
    
    @Override
    public void update()
    {
        super.update();
        
        updatePathPos();
        
        double time = game.getCurrentTime();
        if (time - cmdTime > GameUtil.SERVER_CMD_MAX_QUEUE_TIME) clearCmd(); //remove queued commands that are too old
        performCmd(time, true);
        boolean tryAgain = processTarget(time);
        
        if (action != null)
        {
            switch (action.getActionId())
            {
                case Action.A_ATTACK:
                    AttackAction aa = (AttackAction)action;
                    if (time >= aa.postHitStart && !hasFired)
                    {
                        //spawn projectile
                        game.addEntity(new STargetedProjectile(game, this, target, attribs.get(Attribs.ATTACK_PROJECTILE_SPEED), aa.postHitStart));
                        nextAttackTime = aa.startTime + attribs.get(Attribs.ATTACK_INTERVAL);
                        hasFired = true;
                    }
                    break;
            }
            
            if (game.getCurrentTime() >= action.endTime)
            {
                //System.out.println("action ended!");
                double endTime = action.endTime;
                cancelAction();
                
                targetPathUpdateTick = game.getCurrentTick();
                
                performCmd(endTime, false);
                if (tryAgain)
                {
                    processTarget(endTime);
                }
            }
        }
        
        startAttackCmdTime = Double.NEGATIVE_INFINITY;
    }
    
    //returns whether an Action or movement towards the target was prevented by the currently active Action
    private boolean processTarget(double time)
    {
        if (time != game.getCurrentTime()) updatePathPos(time);
        
        if (target != null)
        {
            if (!game.entityExists(target))
            {
                //target no longer exists - cancel the action and stop moving
                cancelAction();
                stopMoving(time);
                
                if (time != game.getCurrentTime()) updatePathPos(game.getCurrentTime());
                return false;
            }
            else if (MathUtil.dist(xPos, yPos, target.xPos, target.yPos) < attribs.get(Attribs.ATTACK_RANGE))
            {
                //stop moving
                
                if (action != null && action.getActionId() == Action.A_ATTACK)
                {
                    //an attack is already in progress
                    return false;
                }
                
                if (time < nextAttackTime)
                {
                    //can't attack yet
                    stopMoving(time);
                    if (time != game.getCurrentTime()) updatePathPos(game.getCurrentTime());
                    return false;
                }
                
                double attackStart;
                if (Math.max(nextAttackTime, startAttackCmdTime) >= game.getCurrentTime() - GameUtil.SERVER_TICK_DELTA - 0.00001)
                {
                    if (time == game.getCurrentTime())
                    {
                        attackStart = Math.max(nextAttackTime, startAttackCmdTime);
                    }
                    else
                    {
                        attackStart = time;
                    }
                }
                else
                {
                    attackStart = time;
                }
                
                boolean performAction;
                if (action == null)
                {
                    performAction = true;
                }
                else
                {
                    if (action.allowCancel(game.getCurrentTime()))
                    {
                        attackStart = Math.max(attackStart, action.allowCancelSince(game.getCurrentTime()));
                        cancelAction();
                        performAction = true;
                    }
                    else
                    {
                        performAction = false;
                    }
                }
                
                if (performAction)
                {
                    stopMoving(attackStart);
                    
                    //use basic attack
                    action = new AttackAction(
                            attackStart,
                            attribs.get(Attribs.ATTACK_TIME),
                            target.id,
                            attribs.get(Attribs.ATTACK_PREHIT_OFFSET),
                            attribs.get(Attribs.ATTACK_POSTHIT_OFFSET),
                            attribs.get(Attribs.ATTACK_RELOAD_OFFSET));
                    hasFired = false;
                    
                    //System.out.println("action started! (" + action.startTime + " - " + action.endTime + ")");
                    
                    if (time != game.getCurrentTime()) updatePathPos(game.getCurrentTime());
                    return false;
                }
                else
                {
                    stopMoving(time);
                    if (time != game.getCurrentTime()) updatePathPos(game.getCurrentTime());
                    return true;
                }
            }
            else if ((Math.abs(path.getEndX() - target.xPos) > .0001 || Math.abs(path.getEndY() - target.yPos) > .0001))
            {
                if (action == null || action.allowMovement(time))
                {
                    //move towards target
                    if ((game.getCurrentTick() - targetPathUpdateTick) % GameUtil.SERVER_PATH_UPDATE_INTERVAL == 0) //save cpu time by only updating path every X ticks
                    {
                        findPathTo(action == null ? time : Math.max(action.allowMovementSince(time), game.getCurrentTime()-GameUtil.SERVER_TICK_DELTA), target.xPos, target.yPos);
                    }
                    if (time != game.getCurrentTime()) updatePathPos(game.getCurrentTime());
                    return false;
                }
                else
                {
                    if (time != game.getCurrentTime()) updatePathPos(game.getCurrentTime());
                    return true;
                }
            }
            else
            {
                if (time != game.getCurrentTime()) updatePathPos(game.getCurrentTime());
                return false;
            }
        }
        else
        {
            if (time != game.getCurrentTime()) updatePathPos(game.getCurrentTime());
            return false;
        }
    }
    
    @Override
    public void postUpdate()
    {
        super.postUpdate();
        attribs.recalculate();
    }
    
    private void performCmd(double time, boolean allowBefore)
    {
        switch (cmdId)
        {
            case GameProtocol.CMD_MOVE:
                if (performMoveCommand(time, cmdX, cmdY, allowBefore)) clearCmd();
                break;
            case GameProtocol.CMD_ATTACK:
                if (performAttackCommand(time, cmdTarget, allowBefore)) clearCmd();
                break;
        }
    }
    
    private void queueCmd(int cmdId)
    {
        this.cmdId = cmdId;
        cmdTime = game.getCurrentTime();
    }
    
    private void clearCmd()
    {
        cmdId = GameProtocol.CMD_NONE;
        cmdTarget = null;
    }
    
    public void moveCommand(float x, float y)
    {
        queueCmd(GameProtocol.CMD_MOVE);
        cmdX = x;
        cmdY = y;
    }
    
    public void attackCommand(SActor target)
    {
        if (!game.entityExists(target) || !canAttack(target)) return;
        
        queueCmd(GameProtocol.CMD_ATTACK);
        cmdTarget = target;
    }
    
    private boolean performMoveCommand(double time, float x, float y, boolean allowBefore)
    {
        if (action != null && !action.allowMovement(time))
        {
            if (action.allowCancel(time))
            {
                if (allowBefore)
                {
                    time = Math.max(Math.min(time, action.allowCancelSince(time)), time - GameUtil.SERVER_TICK_DELTA);
                }
                
                //cancel the action and proceed with the movement command
                cancelAction();
            }
            else
            {
                //cancel the movement command
                return false;
            }
        }
        else if (action != null && action.allowMovement(time))
        {
            if (allowBefore)
            {
                time = Math.max(Math.min(time, action.allowMovementSince(time)), time - GameUtil.SERVER_TICK_DELTA);
            }
        }
        
        target = null;
        targetPathUpdateTick = game.getCurrentTick();
        findPathTo(time, x, y);
        return true;
    }
    
    private boolean performAttackCommand(double time, SActor target, boolean allowBefore)
    {
        if (!game.entityExists(target) || !canAttack(target)) return true;
        
        if (action != null && !action.allowMovement(time))
        {
            if (action.allowCancel(time))
            {
                if (allowBefore)
                {
                    time = Math.max(Math.min(time, action.allowCancelSince(time)), time - GameUtil.SERVER_TICK_DELTA);
                }
                
                //cancel the action and proceed with the attack command
                cancelAction();
            }
            else
            {
                //cancel the attack command
                return false;
            }
        }
        else if (action != null && action.allowMovement(time))
        {
            if (allowBefore)
            {
                time = Math.max(Math.min(time, action.allowMovementSince(time)), time - GameUtil.SERVER_TICK_DELTA);
            }
        }
        
        this.target = target;
        targetPathUpdateTick = game.getCurrentTick();
        startAttackCmdTime = time;
        return true;
        /*if (action != null)
        {
            if (action.allowCancel(time))
            {
                //cancel the action and proceed with the attack command
                cancelAction();
            }
            else
            {
                //cancel the attack command
                return false;
            }
        }
        
        this.target = target;
        targetPathUpdateTick = game.getCurrentTick();
        startAttackCmdTime = time;
        return true;*/
    }
    
    private final Vector2d tempVec2 = new Vector2d();
    
    protected void findPathTo(double time, float x, float y)
    {
        updatePathPos();
        
        if (!game.pathfinder.nearestFreeSpace(x, y, game.map.scale * 64, tempVec2)) return;
        Node[] nodes = game.pathfinder.findPath(xPos, yPos, tempVec2.x, tempVec2.y);
        
        if (nodes == null)
        {
            stopMoving();
            //path = new ActorPath(xPos, yPos, game.getCurrentTime());
        }
        else
        {
            path = new ActorPath(nodes, time);
        }
    }
    
    public void stopMoving()
    {
        stopMoving(game.getCurrentTime());
    }
    
    public void stopMoving(double time)
    {
        updatePathPos(time);
        
        if (path.numPoints() == 1 && path.getStartX() == xPos && path.getStartY() == yPos && path.startTime <= time) return; //already stopped
        path = new ActorPath(xPos, yPos, time);
    }
    
    private void updatePathPos()
    {
        updatePathPos(game.getCurrentTime());
    }
    
    private void updatePathPos(double time)
    {
        xPos = path.getPathXPos(time, attribs.get(Attribs.MOVE_SPEED));
        yPos = path.getPathYPos(time, attribs.get(Attribs.MOVE_SPEED));
    }
    
    public void cancelAction()
    {
        action = null;
    }
    
    public final boolean canAttack(SEntity other)
    {
        return other instanceof SActor && canAttack((SActor)other);
    }
    public boolean canAttack(SActor other)
    {
        return team != other.team;
    }
    
    @Override
    public abstract Actor getSnapshot();
}
