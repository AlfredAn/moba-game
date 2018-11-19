package onlinegame.client.menu;

import onlinegame.client.Input;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.MColor4f;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Draw;
import org.joml.Matrix4f;

/**
 *
 * @author Alfred
 */
public abstract class MenuItem
{
    public final Menu menu;
    private int mouseState;
    private boolean mouseOver;
    private boolean wasClicked;
    
    protected final MColor4f color = new MColor4f();
    protected float x, y, width, height;
    protected boolean isEnabled = true;
    
    public static final int
            M_NONE = 0,
            M_HOVER = 1,
            M_PRESS = 2,
            M_PRESSOUTSIDE = 3;
    
    public MenuItem(Menu menu, float x, float y, float width, float height, int align)
    {
        this(
                menu,
                x - Align.getXAlign(align) * width,
                y - Align.getInvYAlign(align) * height,
                width,
                height);
    }
    public MenuItem(Menu menu, float x, float y, float width, float height)
    {
        this.menu = menu;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void setPosition(float x, float y)
    {
        this.x = x;
        this.y = y;
        onReposition();
    }
    
    protected void onReposition() {}
    
    protected void onMenuResize() {}
    
    public void setEnabled(boolean enabled)
    {
        isEnabled = enabled;
    }
    
    public boolean isEnabled()
    {
        return isEnabled;
    }
    
    public boolean isSelectable()
    {
        return false;
    }
    
    public boolean isSelected()
    {
        return menu != null && isSelectable() && menu.getSelected() == this;
    }
    
    public final void setColor(Color4f color)
    {
        this.color.set(color);
    }
    
    public float getX()
    {
        return x;
    }
    
    public float getY()
    {
        return y;
    }
    
    public float getWidth()
    {
        return width;
    }
    
    public float getHeight()
    {
        return height;
    }
    
    public int getMouseState()
    {
        return mouseState;
    }
    
    public boolean wasClicked()
    {
        return wasClicked;
    }
    
    public boolean isActive()
    {
        return menu == null || !menu.hasAlert();
    }
    
    protected void onClick() {}
    protected void onSelect() {}
    
    public void create() {}
    public void destroy() {}
    
    public void update()
    {
        wasClicked = false;
        
        if (!isActive() || !isEnabled())
        {
            mouseState = M_NONE;
            return;
        }
        
        mouseOver = checkMouseOver();
        boolean mouseDown = Input.keyDown(Input.KEY_LEFTCLICK);
        boolean mousePressed = Input.keyPressed(Input.KEY_LEFTCLICK);
        boolean mouseReleased = Input.keyReleased(Input.KEY_LEFTCLICK);
        
        switch (mouseState)
        {
            case M_NONE:
                if (mouseOver)
                {
                    mouseState = M_HOVER;
                }
                break;
            case M_HOVER:
                if (!mouseOver)
                {
                    mouseState = M_NONE;
                }
                else if (mousePressed)
                {
                    mouseState = M_PRESS;
                }
                break;
            case M_PRESS:
                if (!mouseOver)
                {
                    mouseState = M_PRESSOUTSIDE;
                }
                if (!mouseDown || mouseReleased)
                {
                    mouseState = M_NONE;
                    
                    if (mouseOver)
                    {
                        wasClicked = true;
                    }
                }
                break;
            case M_PRESSOUTSIDE:
                if (mouseOver)
                {
                    mouseState = M_PRESS;
                }
                if (!mouseDown || mouseReleased)
                {
                    mouseState = M_NONE;
                }
                break;
        }
        
        if (wasClicked)
        {
            if (menu != null)
            {
                menu.onPress(this);
            }
            onClick();
        }
    }
    
    public boolean isMouseOver()
    {
        return mouseOver;
    }
    
    /**
     *
     * @return Whether the mouse is over the component.
     */
    protected boolean checkMouseOver()
    {
        if (!isClickable())
        {
            return false;
        }
        
        if (menu == null)
        {
            return Input.mouseInArea(getMouseOverMinX(), getMouseOverMinY(), getMouseOverMaxX(), getMouseOverMaxY());
        }
        
        Draw.mat.pushMatrix();
        Matrix4f tempMat = Draw.mat.getDirect();
        tempMat.identity();
        menu.transform(tempMat);
        tempMat.invert();
        
        boolean result = Input.mouseInArea(tempMat, getMouseOverMinX(), getMouseOverMinY(), getMouseOverMaxX(), getMouseOverMaxY());
        
        Draw.mat.popMatrix();
        
        return result;
    }
    
    protected float getMouseOverMinX() { return getX(); }
    protected float getMouseOverMinY() { return getY(); }
    protected float getMouseOverMaxX() { return getX() + getWidth(); }
    protected float getMouseOverMaxY() { return getY() + getHeight(); }
    
    public boolean isClickable()
    {
        return true;
    }
    
    public void draw() {}
    
    @Override
    protected void finalize() throws Throwable
    {
        destroy();
        
        super.finalize();
    }
}
