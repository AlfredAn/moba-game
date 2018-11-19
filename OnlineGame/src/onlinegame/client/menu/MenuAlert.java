package onlinegame.client.menu;

import onlinegame.client.Fonts;
import onlinegame.client.Fonts.FontContainer;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.text.TextModel;
import org.joml.Matrix4f;

/**
 *
 * @author Alfred
 */
public class MenuAlert extends Menu
{
    public final Menu menu;
    public final String alertText, buttonText, tag;
    
    private final MenuLabel alertLabel;
    private final MenuButton closeButton;
    
    public MenuAlert(Menu menu, String alertText, String buttonText)
    {
        this(menu, alertText, buttonText, null);
    }
    public MenuAlert(Menu menu, float width, float height, String alertText, String buttonText)
    {
        this(menu, width, height, alertText, buttonText, null);
    }
    public MenuAlert(Menu menu, String alertText, String buttonText, String tag)
    {
        this(menu, Math.max(500, TextModel.getStringWidth(font(menu).get(), alertText) * scale(menu) + 110), buttonText == null ? 130 : 260, alertText, buttonText, tag);
    }
    public MenuAlert(Menu menu, float width, float height, String alertText, String buttonText, String tag)
    {
        super(menu.ctx, width, height);
        this.menu = menu;
        
        this.alertText = String.valueOf(alertText);
        this.buttonText = buttonText;
        this.tag = tag;
        
        if (buttonText == null)
        {
            closeButton = null;
            alertLabel = new MenuLabel(this, 0, height / 2, scale(menu), alertText, font(menu), Align.CENTER);
            
            add(alertLabel);
        }
        else
        {
            closeButton = new MenuButton(this,
                    -300 / 2,
                    height - 65 - font(menu).get().getNormalHeight() * scale(menu),
                    300,
                    40 + font(menu).get().getNormalHeight() * scale(menu),
                    scale(menu),
                    buttonText, font(menu));

            alertLabel = new MenuLabel(this, 0, closeButton.getY() / 2, scale(menu), alertText, font(menu), Align.CENTER);
            
            add(alertLabel);
            add(closeButton);
        }
    }
    
    private static boolean n(Menu menu)
    {
        return menu.itemAlign == Align.TOPLEFT;
    }
    
    private static FontContainer font(Menu menu)
    {
        if (menu.itemAlign == Align.TOPLEFT)
        {
            return Fonts.lobbyMedium;
        }
        else
        {
            return Fonts.menu;
        }
    }
    
    private static float scale(Menu menu)
    {
        if (menu.itemAlign == Align.TOPLEFT)
        {
            return 2;
        }
        else
        {
            return .4f;
        }
    }
    
    private static float menuScale(Menu menu)
    {
        if (menu.itemAlign == Align.TOPLEFT)
        {
            return .5f;
        }
        else
        {
            return menu.getMenuScale();
        }
    }
    
    private static float scaledHeight(Menu menu)
    {
        if (menu.itemAlign == Align.TOPLEFT)
        {
            return menu.getHeight() * 2;
        }
        else
        {
            return menu.getScaledHeight();
        }
    }
    
    public String getTag()
    {
        return tag;
    }
    
    @Override
    public void onPress(MenuItem item)
    {
        if (item == closeButton)
        {
            menu.closeAlert();
        }
    }
    
    @Override
    public void transform(Matrix4f dest, int align)
    {
        //align is ignored until I fix this class
        
        float h = scaledHeight(menu);
        float y = h/2 - getHeight() / 2;
        
        dest.translate(menu.getWidth() / 2, 0, 0);
        dest.scale(menuScale(menu));
        dest.translate(0, (int)(y / 2) * 2, 0);
    }
    
    @Override
    public void draw()
    {
        Draw.mat.pushMatrix();
        transform(Draw.mat);
        
        Draw.color.set(.125f, .5625f, 1f);
        Draw.fillRect(-getWidth()/2, 0, getWidth(), getHeight());
        //Draw.fillRect(-getWidth()/2, 0, getWidth(), getHeight());
        
        Draw.color.set(0f, 0f, 0f);
        Draw.drawRect(-getWidth()/2, 0, getWidth(), getHeight());
        //Draw.drawRect(-getWidth()/2, 0, getWidth(), getHeight());
        
        Draw.mat.popMatrix();
        
        super.draw();
    }
}
