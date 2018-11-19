package onlinegame.server.game.engine.status;

import onlinegame.server.game.engine.SActor;
import onlinegame.shared.game.stats.Attribs;
import onlinegame.shared.game.stats.Status;

/**
 *
 * @author Alfred
 */
public final class SActorStatus extends Status
{
    public final SActor actor;
    public final SActorAttribs attribs;
    
    private final float[] resourceAmount;
    private final float[] resourceMax;
    
    public SActorStatus(SActor actor, int... resourceType)
    {
        super(resourceType);
        
        this.actor = actor;
        attribs = actor.attribs;
        
        resourceAmount = new float[getNumResources()];
        resourceMax = new float[getNumResources()];
        
        refresh();
        
        for (int i = 0; i < getNumResources(); i++)
        {
            resourceAmount[i] = resourceMax[i];
        }
    }
    
    public void refresh()
    {
        for (int i = 0; i < getNumResources(); i++)
        {
            float max = Float.POSITIVE_INFINITY;
            switch (getResourceType(i))
            {
                case HEALTH:
                    max = attribs.get(Attribs.RES_HEALTH_MAX);
                    break;
                case MANA:
                    max = attribs.get(Attribs.RES_MANA_MAX);
                    break;
            }
            
            resourceMax[i] = max;
            if (max < resourceAmount[i])
            {
                resourceAmount[i] = max;
            }
        }
    }
    
    @Override
    public float getResourceAmount(int r)
    {
        return resourceAmount[r];
    }
    
    @Override
    public float getResourceMax(int r)
    {
        return resourceMax[r];
    }
}
