package onlinegame.client.client.launcher;

import onlinegame.client.client.launcher.LaunchMenu;
import java.io.DataInputStream;
import java.io.IOException;
import onlinegame.client.Fonts;
import onlinegame.client.client.MessageBuilder;
import onlinegame.client.client.Network;
import onlinegame.client.client.NetworkListener;
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
public final class RegisterMenu extends Menu implements NetworkListener
{
    private final MenuTextBox usernameBox, passwordBox, confirmPasswordBox;
    private final MenuButton registerButton, backButton;
    
    public RegisterMenu(MenuContext ctx)
    {
        super(ctx);
        
        MenuItem item;
        
        float y = 30;
        
        add(item = title = new MenuLabel(this, 0, y, "League of DOTA Fortress 2", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 70;
        add(item = new MenuLabel(this, 0, y, .7f, "Create Account", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 70;
        add(item = new MenuLabel(this, 0, y, .5f, "Username", Fonts.menu, Align.TOP));
        
        y += item.getHeight() + 20;
        add(item = usernameBox = new MenuTextBox(this, -600, y, 1200, Fonts.textBox.get().getNormalHeight() + 40, Fonts.textBox));
        usernameBox.setCharLimit(AccountChecks.USERNAME_MAXLENGTH);
        
        y += item.getHeight() + 30;
        add(item = new MenuLabel(this, 0, y, .5f, "Password", Fonts.menu, Align.TOP));
        
        //password box
        y += item.getHeight() + 20;
        add(item = passwordBox = new MenuTextBox(this, -600, y, 1200, Fonts.textBox.get().getNormalHeight() + 40, Fonts.textBox));
        passwordBox.setHidden(true);
        passwordBox.setCharLimit(AccountChecks.PASSWORD_MAXLENGTH);
        
        y += item.getHeight() + 30;
        add(item = new MenuLabel(this, 0, y, .5f, "Confirm Password", Fonts.menu, Align.TOP));
        
        //password box
        y += item.getHeight() + 20;
        add(item = confirmPasswordBox = new MenuTextBox(this, -600, y, 1200, Fonts.textBox.get().getNormalHeight() + 40, Fonts.textBox));
        confirmPasswordBox.setHidden(true);
        confirmPasswordBox.setCharLimit(AccountChecks.PASSWORD_MAXLENGTH);
        
        y += item.getHeight() + 30;
        add(item = registerButton = new MenuButton(this, -600, y, 575, Fonts.menu.get().getNormalHeight() * .6f + 50, .6f, "Register", Fonts.menu));
        
        add(item = backButton = new MenuButton(this, 25, y, 575, Fonts.menu.get().getNormalHeight() * .6f + 50, .6f, "Back", Fonts.menu));
        
        addListener();
    }
    
    @Override
    public void readMessage(InputMessage msg) throws IOException
    {
        if (!isAlertOpen("register"))
        {
            return;
        }
        
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.S_REGISTER_RESPONSE:
                in = msg.getDataStream();
                
                boolean success = in.readBoolean();
                String text = in.readUTF();
                
                if (success)
                {
                    alert(text, "OK", "returnOnClose");
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
    public void onCloseAlert(String tag)
    {
        if (tag.equals("returnOnClose"))
        {
            ctx.setMenu(new LaunchMenu(ctx));
        }
    }
    
    @Override
    public void onPress(MenuItem item)
    {
        if (item == registerButton)
        {
            String username = AccountChecks.trimmedUsername(usernameBox.getContent());
            String password = passwordBox.getContent();
            String confirmPass = confirmPasswordBox.getContent();
            
            String err = AccountChecks.checkLogin(username, password, confirmPass);
            if (err != null)
            {
                alert(err);
                return;
            }
            
            alert("Registering...", null, "register");
            
            Network net = ctx.getNetwork();
            OutputMessage msg = MessageBuilder.registerRequest(username, password, net.getCrypt());
            net.sendMessage(msg);
        }
        else if (item == backButton)
        {
            ctx.setMenu(new LaunchMenu(ctx));
        }
    }
}
