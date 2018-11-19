package onlinegame.server.game.engine;

import java.util.ArrayList;
import java.util.List;
import onlinegame.shared.game.Entity;

/**
 *
 * @author Alfred
 */
public abstract class SEntity
{
    public final SGameState game;
    public final int id;
    
    protected float xPos, yPos;
    public final int team;
    
    public SEntity(SGameState game, float xPos, float yPos, int team)
    {
        this.game = game;
        id = game.assignId();
        this.xPos = xPos;
        this.yPos = yPos;
        this.team = team;
    }
    
    public void update() {}
    
    public void postUpdate() {}
    
    public boolean collision(SEntity other)
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
    public List<SEntity> collision()
    {
        ArrayList<SEntity> collisions = new ArrayList<>();
        
        for (int i = 0; i < game.numEntities(); i++)
        {
            SEntity e = game.getEntity(i);
            if (collision(e))
            {
                collisions.add(e);
            }
        }
        
        return collisions;
    }
    
    public abstract Entity getSnapshot();
    
    public final float getXPos()
    {
        return xPos;
    }
    
    public final float getYPos()
    {
        return yPos;
    }
}
