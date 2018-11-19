package onlinegame.client.client.launcher;

import java.io.DataInputStream;
import java.io.IOException;
import onlinegame.client.Fonts;
import onlinegame.client.OnlineGameClient;
import onlinegame.client.client.MessageBuilder;
import onlinegame.client.client.Network;
import onlinegame.client.client.mainmenu.MainMenu;
import onlinegame.client.graphics.Align;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuButton;
import onlinegame.client.menu.MenuContext;
import onlinegame.client.menu.MenuItem;
import onlinegame.client.menu.MenuLabel;
import onlinegame.client.menu.MenuTextBox;
import onlinegame.shared.Logger;
import onlinegame.shared.account.AccountChecks;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class LoginMenu extends Menu
{
    private final MenuTextBox usernameBox, passwordBox;
    private final MenuButton loginButton, backButton;
    
    public LoginMenu(MenuContext ctx)
    {
        super(ctx);
        
        MenuItem item;
        
        float y = 30;
        
        add(item = title = new MenuLabel(this, 0, y, "League of DOTA Fortress 2", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 70;
        add(item = new MenuLabel(this, 0, y, .7f, "Login", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 70;
        add(item = new MenuLabel(this, 0, y, .5f, "Username", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 20;
        add(item = usernameBox = new MenuTextBox(this, -600, y, 1200, Fonts.textBox.get().getNormalHeight() + 40, Fonts.textBox));
        usernameBox.setCharLimit(30);
        usernameBox.setContent(OnlineGameClient.getArgument("user"));
        
        y += item.getHeight() + 30;
        add(item = new MenuLabel(this, 0, y, .5f, "Password", Fonts.menu, Align.TOP));
        
        //password box
        y += item.getHeight() + 20;
        add(item = passwordBox = new MenuTextBox(this, -600, y, 1200, Fonts.textBox.get().getNormalHeight() + 40, Fonts.textBox));
        passwordBox.setHidden(true);
        passwordBox.setCharLimit(45);
        passwordBox.setContent(OnlineGameClient.getArgument("pass"));
        
        y += item.getHeight() + 30;
        add(item = loginButton = new MenuButton(this, -600, y, 575, Fonts.menu.get().getNormalHeight() * .6f + 50, .6f, "Login", Fonts.menu));
        
        add(item = backButton = new MenuButton(this, 25, y, 575, Fonts.menu.get().getNormalHeight() * .6f + 50, .6f, "Back", Fonts.menu));
        
        addListener();
    }
    
    @Override
    public void readMessage(InputMessage msg) throws IOException
    {
        if (!isAlertOpen("login"))
        {
            return;
        }
        
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.S_LOGIN_RESPONSE:
                in = msg.getDataStream();
                
                boolean success = in.readBoolean();
                String text = in.readUTF();
                
                if (success)
                {
                    ctx.getNetwork().setUsername(in.readUTF());
                    ctx.getNetwork().setSessionHash(in.readShort());
                    ctx.setMenu(new MainMenu(ctx));
                }
                else
                {
                    Logger.logError(text);
                    alert(text);
                }
                
                break;
        }
    }
    
    @Override
    public void onPress(MenuItem item)
    {
        if (item == loginButton)
        {
            String username = AccountChecks.trimmedUsername(usernameBox.getContent());
            String password = passwordBox.getContent();
            
            alert("Logging in...", null, "login");
            
            Network net = ctx.getNetwork();
            OutputMessage msg = MessageBuilder.loginRequest(username, password, net.getCrypt());
            net.sendMessage(msg);
        }
        else if (item == backButton)
        {
            ctx.setMenu(new LaunchMenu(ctx));
        }
    }
}
