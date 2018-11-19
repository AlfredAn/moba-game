package onlinegame.shared.game.stats;

/**
 *
 * @author Alfred
 */
public final class ActorStatus extends Status
{
    private final int[] resourceAmount, resourceMax;
    
    public ActorStatus(int[] resourceType, float[] resourceAmount, float[] resourceMax)
    {
        super(resourceType);
        
        this.resourceAmount = new int[getNumResources()];
        this.resourceMax = new int[getNumResources()];
        
        for (int i = 0; i < getNumResources(); i++)
        {
            this.resourceAmount[i] = (int)resourceAmount[i];
            this.resourceMax[i] = (int)resourceMax[i];
        }
    }
    
    public ActorStatus(int[] resourceType, int[] resourceAmount, int[] resourceMax)
    {
        super(resourceType);
        
        this.resourceAmount = resourceAmount;
        this.resourceMax = resourceMax;
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
