package onlinegame.client.client.launcher;

import onlinegame.client.Fonts;
import onlinegame.client.OnlineGameClient;
import onlinegame.client.client.Network;
import onlinegame.client.graphics.Align;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuButton;
import onlinegame.client.menu.MenuContext;
import onlinegame.client.menu.MenuItem;
import onlinegame.client.menu.MenuLabel;
import onlinegame.client.menu.MenuTextBox;
import onlinegame.shared.net.Connection;

/**
 *
 * @author Alfred
 */
public class ConnectMenu extends Menu
{
    private final MenuTextBox ipBox;
    private final MenuButton connectButton, exitButton;
    
    public ConnectMenu(MenuContext ctx)
    {
        super(ctx);
        
        MenuItem item;
        float y = 30;
        
        add(item = title = new MenuLabel(this, 0, y, "League of DOTA Fortress 2", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 70;
        add(item = new MenuLabel(this, 0, y, .7f, "Connect to server", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 150;
        add(item = new MenuLabel(this, 0, y, .5f, "Enter server IP:", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 30;
        add(item = ipBox = new MenuTextBox(this, -400, y, 800, Fonts.textBox.get().getNormalHeight() + 40, Fonts.textBox));
        ipBox.setContent("127.0.0.1");
        
        y += item.getHeight() + 30;
        add(item = connectButton = new MenuButton(this, -300, y, 600, Fonts.menu.get().getNormalHeight()*.6f + 50, .6f, "Connect", Fonts.menu));
        
        y += item.getHeight() + 30;
        add(item = exitButton = new MenuButton(this, -300, y, 600, Fonts.menu.get().getNormalHeight()*.6f + 50, .6f, "Exit", Fonts.menu));
    }
    
    @Override
    public void onPress(MenuItem item)
    {
        if (item == connectButton)
        {
            alert("Connecting to server...", "Cancel", "connecting");
            ctx.getNetwork().connect(ipBox.getContent());
        }
        else if (item == exitButton)
        {
            OnlineGameClient.exit();
        }
    }
    
    @Override
    public void onCloseAlert(String tag)
    {
        if (tag.equals("connecting"))
        {
            ctx.getNetwork().close();
        }
    }
    
    @Override
    public void update()
    {
        super.update();
        
        if (isAlertOpen("connecting"))
        {
            Network net = ctx.getNetwork();
            int status = net.getConnectionState();
            
            switch (status)
            {
                case Connection.ST_CONNECTED:
                    ctx.setMenu(new LaunchMenu(ctx));
                    break;
                case Connection.ST_CLOSED:
                    alert(net.getErrorMessage());
                    break;
            }
        }
    }
}
