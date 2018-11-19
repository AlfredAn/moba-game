package onlinegame.client.menu;

import onlinegame.client.Fonts.FontContainer;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.IColor4f;

/**
 *
 * @author Alfred
 */
public final class MenuButton extends MenuItem
{
    private final MenuLabel label;
    
    public MenuButton(Menu menu, float x, float y, float width, float height, String text, FontContainer font)
    {
        this(menu, x, y, width, height, 1, text, font);
    }
    public MenuButton(Menu menu, float x, float y, float width, float height, float textScale, String text, FontContainer font)
    {
        super(menu, x, y, width, height);
        color.set(new IColor4f(.375f, .375f, 1f));
        
        label = new MenuLabel(null, x + width / 2, y + height / 2, textScale, text, font);
    }
    
    @Override
    public void create()
    {
        label.create();
    }
    
    @Override
    public void destroy()
    {
        label.destroy();
    }
    
    public String getText()
    {
        return label.getText();
    }
    
    @Override
    public void draw()
    {
        Draw.color.set(color);
        Draw.fillRect(x, y, width, height);
        
        label.draw();
        
        switch (getMouseState())
        {
            case M_HOVER:
                Draw.color.set(1f, 1f, 1f, .125f);
                Draw.fillRect(x, y, width, height);
                break;
            case M_PRESS:
                Draw.color.set(0f, 0f, 0f, .125f);
                Draw.fillRect(x, y, width, height);
                break;
            default:
                if (!isEnabled())
                {
                    Draw.color.set(.5f, .5f, .5f, .625f);
                    Draw.fillRect(x, y, width, height);
                }
                break;
        }
        
        Draw.color.set(Color4f.BLACK);
        Draw.drawRect(x, y, width, height);
    }
    
    public void setTextColor(Color4f col)
    {
        label.setColor(col);
    }
}
