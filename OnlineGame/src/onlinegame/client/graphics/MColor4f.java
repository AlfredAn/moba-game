package onlinegame.client.graphics;

public final class MColor4f extends Color4f
{
    /**
     * The red component of the color.
     */
    public float r;
    
    /**
     * The green component of the color.
     */
    public float g;
    
    /**
     * The blue component of the color.
     */
    public float b;
    
    /**
     * The alpha component of the color.
     */
    public float a;
    
    /**
     * Creates a new color that is black with an alpha of one.
     */
    public MColor4f()
    {
        a = 1;
    }
    
    /**
     * Creates a new color with the given values and the alpha component set to one.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     */
    public MColor4f(float r, float g, float b)
    {
        set(r, g, b);
    }
    
    /**
     * Creates a new color with the given values.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public MColor4f(float r, float g, float b, float a)
    {
        set(r, g, b, a);
    }
    
    /**
     * Creates a new color that is a copy of the given one.
     * 
     * @param c The source color.
     */
    public MColor4f(MColor4f c)
    {
        set(c);
    }
    
    /**
     * Creates a new color that is a copy of the given one.
     * 
     * @param c The source color.
     */
    public MColor4f(IColor4f c)
    {
        set(c);
    }
    
    /**
     * Creates a new color that is a copy of the given one.
     * 
     * @param c The source color.
     */
    public MColor4f(Color4f c)
    {
        set(c);
    }
    
    /**
     * Sets this color's values. The alpha component is set to one.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     */
    public void set(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1f;
    }
    
    /**
     * Sets this color's alpha component and leaves the others unchanged.
     * 
     * @param a The alpha component.
     */
    public void setAlpha(float a)
    {
        this.a = a;
    }
    
    /**
     * Sets this color's values.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public void set(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    /**
     * Sets this color to be identical to the given one.
     * 
     * @param c The source color.
     */
    public void set(MColor4f c)
    {
        r = c.r;
        g = c.g;
        b = c.b;
        a = c.a;
    }
    
    /**
     * Sets this color to be identical to the given one.
     * 
     * @param c The source color.
     */
    public void set(IColor4f c)
    {
        r = c.r;
        g = c.g;
        b = c.b;
        a = c.a;
    }
    
    /**
     * Sets this color to be identical to the given one.
     * 
     * @param c The source color.
     */
    public void set(Color4f c)
    {
        c.put(this);
    }
    
    public void blend(Color4f c, float f)
    {
        blend(c.getR(), c.getG(), c.getB(), c.getA(), f);
    }
    
    public void blend(float r, float g, float b, float a, float f)
    {
        float invf = 1 - f;
        this.r = this.r * invf + r * f;
        this.g = this.g * invf + g * f;
        this.b = this.b * invf + b * f;
        this.a = this.a * invf + a * f;
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