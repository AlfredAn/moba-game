package onlinegame.shared.game.stats;

/**
 *
 * @author Alfred
 */
public final class AttribBuilder extends Attribs
{
    private final float[] attribs = new float[NUM_ATTRIBS];
    
    public AttribBuilder() {}
    public AttribBuilder(BaseAttribs base)
    {
        base.copy(attribs);
    }
    
    public BaseAttribs finish()
    {
        return new BaseAttribs(this);
    }
    
    public AttribBuilder set(int i, float val)
    {
        attribs[i] = val;
        return this;
    }
    
    @Override
    public float get(int i)
    {
        return attribs[i];
    }
}
