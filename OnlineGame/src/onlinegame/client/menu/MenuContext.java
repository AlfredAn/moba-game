package onlinegame.client.menu;

import onlinegame.client.client.Network;

/**
 *
 * @author Alfred
 */
public interface MenuContext
{
    public void setMenu(Menu menu);
    
    public Menu getMenu();
    
    public Network getNetwork();
    
    public float getWidth();
    
    public float getHeight();
}
