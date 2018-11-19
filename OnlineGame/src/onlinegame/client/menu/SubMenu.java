package onlinegame.client.menu;

/**
 *
 * @author Alfred
 */
public class SubMenu extends Menu
{
    public final Menu superMenu;
    
    public SubMenu(Menu superMenu)
    {
        super(superMenu.ctx);
        this.superMenu = superMenu;
    }
    
    public SubMenu(Menu superMenu, float width, float height)
    {
        super(superMenu.ctx, width, height);
        this.superMenu = superMenu;
    }
    
    @Override
    public void alert(MenuAlert alert)
    {
        superMenu.alert(alert);
    }
    
    @Override
    public boolean isActive()
    {
        return superMenu.isActive() && superMenu.contains(this);
    }
    
    @Override
    public boolean hasAlert()
    {
        return superMenu.hasAlert();
    }
}
