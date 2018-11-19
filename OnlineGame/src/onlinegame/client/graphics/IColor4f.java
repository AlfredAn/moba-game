package onlinegame.client.graphics;

public final class IColor4f extends Color4f
{
    /**
     * The red component of the color.
     */
    public final float r;
    
    /**
     * The green component of the color.
     */
    public final float g;
    
    /**
     * The blue component of the color.
     */
    public final float b;
    
    /**
     * The alpha component of the color.
     */
    public final float a;
    
    /**
     * Creates a new color with the given values and the alpha component set to one.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     */
    public IColor4f(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        a = 1;
    }
    
    /**
     * Creates a new color with the given values.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public IColor4f(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    /**
     * Creates a new color that is a copy of the given one.
     * 
     * @param c The source color.
     */
    public IColor4f(IColor4f c)
    {
        r = c.r;
        g = c.g;
        b = c.b;
        a = c.a;
    }
    
    /**
     * Creates a new color that is a copy of the given one.
     * 
     * @param c The source color.
     */
    public IColor4f(MColor4f c)
    {
        r = c.r;
        g = c.g;
        b = c.b;
        a = c.a;
    }
    
    /**
     * Creates a new color that is a copy of the given one.
     * 
     * @param c The source color.
     */
    public IColor4f(Color4f c)
    {
        r = c.getR();
        g = c.getG();
        b = c.getB();
        a = c.getA();
    }
    
    @Override
    public float getR()
    {
        return r;
    }
    
    @Override
    public float getG()
    {
        return g;
    }
    
    @Override
    public float getB()
    {
        return b;
    }
    
    @Override
    public float getA()
    {
        return a;
    }
    
    @Override
    public void put(MColor4f dest)
    {
        dest.r = r;
        dest.g = g;
        dest.b = b;
        dest.a = a;
    }
}