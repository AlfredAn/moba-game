package onlinegame.client.client.launcher;

import onlinegame.client.Fonts;
import onlinegame.client.OnlineGameClient;
import onlinegame.client.graphics.Align;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuButton;
import onlinegame.client.menu.MenuContext;
import onlinegame.client.menu.MenuItem;
import onlinegame.client.menu.MenuLabel;

/**
 *
 * @author Alfred
 */
public final class LaunchMenu extends Menu
{
    private MenuButton loginButton, registerButton, exitButton;
    
    public LaunchMenu(MenuContext ctx)
    {
        super(ctx);
        
        MenuItem item;
        float y = 30;
        
        add(item = title = new MenuLabel(this, 0, y, "League of DOTA Fortress 2", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 100;
        add(item = loginButton = new MenuButton(this, -300, y, 600, Fonts.menu.get().getNormalHeight()*.6f + 50, .6f, "Login", Fonts.menu));
        
        y += item.getHeight() + 30;
        add(item = registerButton = new MenuButton(this, -300, y, 600, Fonts.menu.get().getNormalHeight()*.6f + 50, .6f, "Register", Fonts.menu));
        
        y += item.getHeight() + 30;
        add(item = exitButton = new MenuButton(this, -300, y, 600, Fonts.menu.get().getNormalHeight()*.6f + 50, .6f, "Exit", Fonts.menu));
    }
    
    @Override
    public void onPress(MenuItem item)
    {
        if (item == loginButton)
        {
            ctx.setMenu(new LoginMenu(ctx));
        }
        else if (item == registerButton)
        {
            ctx.setMenu(new RegisterMenu(ctx));
        }
        else if (item == exitButton)
        {
            OnlineGameClient.exit();
        }
    }
}
