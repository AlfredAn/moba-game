package onlinegame.client.game;

import onlinegame.client.graphics.Align;
import onlinegame.client.menu.Menu;

/**
 *
 * @author Alfred
 */
final class GameMenu extends Menu
{
    final GameInterface gui;
    
    private final Minimap minimap;
    
    GameMenu(GameInterface gui)
    {
        super(gui);
        
        itemAlign = Align.TOPLEFT;
        doScale = false;
        
        this.gui = gui;
        
        add(minimap = new Minimap(this, gui, 0, 0, 256, 256));
        minimap.setAutoAlign(Align.BOTTOMRIGHT);
    }
}
