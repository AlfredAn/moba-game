package onlinegame.client.menu;

import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.texture.Texture;

/**
 *
 * @author Alfred
 */
public final class MenuImage extends MenuItem
{
    private Texture texture = null;
    private boolean showBorder = true;
    
    public MenuImage(Menu menu, float x, float y, float width, float height)
    {
        super(menu, x, y, width, height);
        color.set(Color4f.WHITE);
    }
    public MenuImage(Menu menu, float x, float y, float width, float height, Texture texture)
    {
        super(menu, x, y, width, height);
        this.texture = texture;
        color.set(Color4f.WHITE);
    }
    
    public void setTexture(Texture tex)
    {
        texture = tex;
    }
    
    public void setShowBorder(boolean showBorder)
    {
        this.showBorder = showBorder;
    }
    
    @Override
    public void draw()
    {
        if (texture != null)
        {
            Draw.color.set(color);
            Draw.setTexture(texture);

            Draw.fillRect(x, y, width, height);

            Draw.resetTexture();
        }
        
        if (showBorder)
        {
            Draw.color.set(Color4f.BLACK);
            Draw.drawRect(x-1, y-1, width+1, height+1);
        }
    }
    
    @Override
    protected float getMouseOverMinX() { return super.getMouseOverMinX() - (showBorder ? 1 : 0); }
    @Override
    protected float getMouseOverMinY() { return super.getMouseOverMinY() - (showBorder ? 1 : 0); }
    @Override
    protected float getMouseOverMaxX() { return super.getMouseOverMaxX() + (showBorder ? 1 : 0); }
    @Override
    protected float getMouseOverMaxY() { return super.getMouseOverMaxY() + (showBorder ? 1 : 0); }
}
