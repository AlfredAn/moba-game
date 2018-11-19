package onlinegame.client.game;

import onlinegame.client.graphics.Align;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuItem;

/**
 *
 * @author Alfred
 */
abstract class GameMenuItem extends MenuItem
{
    final GameInterface gui;
    
    private int autoAlign = Align.TOPLEFT;
    private final float originalX, originalY;
    
    GameMenuItem(Menu menu, GameInterface gui, float x, float y, float width, float height)
    {
        super(menu, x, y, width, height);
        
        originalX = x;
        originalY = y;
        this.gui = gui;
    }
    
    final void setAutoAlign(int align)
    {
        autoAlign = align;
        onMenuResize();
    }
    
    @Override
    public void onMenuResize()
    {
        float xa = Align.getXAlign(autoAlign);
        float ya = Align.getInvYAlign(autoAlign);
        
        setPosition(menu.getWidth() * xa + originalX - width * xa, menu.getHeight() * ya + originalY - height * ya);
    }
}
