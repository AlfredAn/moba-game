package onlinegame.shared.game.stats;

/**
 *
 * @author Alfred
 */
public final class BaseAttribs extends Attribs
{
    private final float[] attribs;
    
    public BaseAttribs(float[] attribs)
    {
        if (attribs.length != NUM_ATTRIBS) throw new IllegalArgumentException();
        this.attribs = attribs;
    }
    
    public BaseAttribs(Attribs attribs)
    {
        if (attribs instanceof BaseAttribs)
        {
            this.attribs = ((BaseAttribs)attribs).attribs;
        }
        else
        {
            this.attribs = new float[NUM_ATTRIBS];
            for (int i = 0; i < NUM_ATTRIBS; i++)
            {
                this.attribs[i] = attribs.get(i);
            }
        }
    }
    
    public BaseAttribs(BaseAttribs attribs)
    {
        this.attribs = attribs.attribs;
    }
    
    @Override
    public float get(int attrib)
    {
        return attribs[attrib];
    }
    
    public void copy(float[] dest)
    {
        System.arraycopy(attribs, 0, dest, 0, dest.length);
    }
}
