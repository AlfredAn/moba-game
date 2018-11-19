package onlinegame.client.graphics;

import onlinegame.shared.MathUtil;

/**
 *
 * @author Alfred
 */
public abstract class Color4f
{
    public abstract float getR();
    public abstract float getG();
    public abstract float getB();
    public abstract float getA(); 
    
    public abstract void put(MColor4f dest);
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Color4f))
        {
            return false;
        }
        
        Color4f col = (Color4f)o;
        return col.getR() == getR() && col.getG() == getG() && col.getB() == getB() && col.getA() == getA();
    }
    
    @Override
    public final int hashCode()
    {
        int hash = 5;
        hash = 53 * hash + Float.floatToIntBits(getR());
        hash = 53 * hash + Float.floatToIntBits(getG());
        hash = 53 * hash + Float.floatToIntBits(getB());
        hash = 53 * hash + Float.floatToIntBits(getA());
        return hash;
    }
    
    public final int toInt()
    {
        return toInt(getR(), getG(), getB(), getA());
    }
    
    public static int toInt(float r, float g, float b)
    {
        return toInt(r, g, b, 1);
    }
    public static int toInt(float r, float g, float b, float a)
    {
        int ir = MathUtil.clamp((int)(r * 256), 0, 255);
        int ig = MathUtil.clamp((int)(g * 256), 0, 255);
        int ib = MathUtil.clamp((int)(b * 256), 0, 255);
        int ia = MathUtil.clamp((int)(a * 256), 0, 255);
        
        return (ia << 24) | (ib << 16) | (ig << 8) | (ir);
    }
    
    ////Colors (computer generated)
    
    public static final IColor4f TRANSPARENT = new IColor4f(0, 0, 0, 0);
    
    /**
     * White
     * RGB(255, 255, 255)
     **/
    public static final IColor4f WHITE = new IColor4f(255f / 255, 255f / 255, 255f / 255);
    /**
     * Lighter Gray
     * RGB(224, 224, 224)
     **/
    public static final IColor4f LIGHTER_GRAY = new IColor4f(224f / 255, 224f / 255, 224f / 255);
    /**
     * Light Gray
     * RGB(192, 192, 192)
     **/
    public static final IColor4f LIGHT_GRAY = new IColor4f(192f / 255, 192f / 255, 192f / 255);
    /**
     * Medium Gray
     * RGB(160, 160, 160)
     **/
    public static final IColor4f MEDIUM_GRAY = new IColor4f(160f / 255, 160f / 255, 160f / 255);
    /**
     * Gray
     * RGB(128, 128, 128)
     **/
    public static final IColor4f GRAY = new IColor4f(128f / 255, 128f / 255, 128f / 255);
    /**
     * Dim Gray
     * RGB(96, 96, 96)
     **/
    public static final IColor4f DIM_GRAY = new IColor4f(96f / 255, 96f / 255, 96f / 255);
    /**
     * Dark Gray
     * RGB(64, 64, 64)
     **/
    public static final IColor4f DARK_GRAY = new IColor4f(64f / 255, 64f / 255, 64f / 255);
    /**
     * Darker Gray
     * RGB(32, 32, 32)
     **/
    public static final IColor4f DARKER_GRAY = new IColor4f(32f / 255, 32f / 255, 32f / 255);
    /**
     * Black
     * RGB(0, 0, 0)
     **/
    public static final IColor4f BLACK = new IColor4f(0f / 255, 0f / 255, 0f / 255);
    /**
     * Red
     * RGB(255, 0, 0)
     **/
    public static final IColor4f RED = new IColor4f(255f / 255, 0f / 255, 0f / 255);
    /**
     * Orange
     * RGB(255, 128, 0)
     **/
    public static final IColor4f ORANGE = new IColor4f(255f / 255, 128f / 255, 0f / 255);
    /**
     * Yellow
     * RGB(255, 255, 0)
     **/
    public static final IColor4f YELLOW = new IColor4f(255f / 255, 255f / 255, 0f / 255);
    /**
     * Lime
     * RGB(128, 255, 0)
     **/
    public static final IColor4f LIME = new IColor4f(128f / 255, 255f / 255, 0f / 255);
    /**
     * Green
     * RGB(0, 255, 0)
     **/
    public static final IColor4f GREEN = new IColor4f(0f / 255, 255f / 255, 0f / 255);
    /**
     * Cyan
     * RGB(0, 255, 255)
     **/
    public static final IColor4f CYAN = new IColor4f(0f / 255, 255f / 255, 255f / 255);
    /**
     * Sky Blue
     * RGB(0, 128, 255)
     **/
    public static final IColor4f SKY_BLUE = new IColor4f(0f / 255, 128f / 255, 255f / 255);
    /**
     * Blue
     * RGB(0, 0, 255)
     **/
    public static final IColor4f BLUE = new IColor4f(0f / 255, 0f / 255, 255f / 255);
    /**
     * Purple
     * RGB(128, 0, 255)
     **/
    public static final IColor4f PURPLE = new IColor4f(128f / 255, 0f / 255, 255f / 255);
    /**
     * Magenta
     * RGB(255, 0, 255)
     **/
    public static final IColor4f MAGENTA = new IColor4f(255f / 255, 0f / 255, 255f / 255);
    /**
     * Deep Pink
     * RGB(255, 0, 128)
     **/
    public static final IColor4f DEEP_PINK = new IColor4f(255f / 255, 0f / 255, 128f / 255);
}
