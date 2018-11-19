package onlinegame.client.client;

import java.io.IOException;
import onlinegame.client.client.launcher.ConnectMenu;
import onlinegame.client.game.Game;
import onlinegame.client.graphics.Draw;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuContext;
import onlinegame.shared.Logger;
import onlinegame.shared.MathUtil;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.Protocol;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

/**
 *
 * @author Alfred
 */
public final class Client implements MenuContext, NetworkListener
{
    private Network net;
    private float width, height;
    private Menu menu;
    private Game game;
    
    public Client(float width, float height)
    {
        this.width = width;
        this.height = height;
        
        menu = new ConnectMenu(this);
        net = new Network(this);
        
        net.addListener(this);
    }
    
    public void create()
    {
        if (menu != null)
        {
            menu.create();
        }
        if (game != null)
        {
            game.create();
        }
    }
    
    public void destroy()
    {
        if (menu != null)
        {
            menu.destroy();
        }
        if (game != null)
        {
            game.destroy();
        }
    }
    
    public void update()
    {
        net.update();
        
        if (menu != null)
        {
            menu.update();
            
            if (!(menu instanceof ConnectMenu) && !net.isConnected())
            {
                resetClient();
            }
        }
        
        if (game != null)
        {
            game.update();
            
            if (!net.isConnected())
            {
                resetClient();
            }
        }
    }
    
    private void resetClient()
    {
        setMenu(new ConnectMenu(this));
        
        String err = net.getErrorMessage();
        if (err == null)
        {
            err = "An unknown network error has occured.";
        }
        
        menu.alert(err);
        
        net.close();
        net = new Network(this);
        net.addListener(this);
        
        if (game != null)
        {
            game.destroy();
            game.stop();
        }
        game = null;
    }
    
    @Override
    public void readMessage(InputMessage msg)
    {
        switch (msg.getId())
        {
            case Protocol.S_GAME_STARTLOAD:
                Logger.log("startload");
                setMenu(null);
                
                try
                {
                    game = new Game(this);
                    game.create();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    resetClient();
                }
                break;
        }
    }
    
    @Override
    public boolean isActive()
    {
        return true;
    }
    
    @Override
    public void setMenu(Menu menu)
    {
        if (this.menu != null)
        {
            this.menu.destroy();
        }
        this.menu = menu;
        
        if (menu != null)
        {
            menu.create();
        }
    }
    
    @Override
    public Menu getMenu()
    {
        return menu;
    }
    
    @Override
    public Network getNetwork()
    {
        return net;
    }
    
    private void setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        
        if (menu != null)
        {
            menu.setSize(width, height);
        }
        if (game != null)
        {
            game.setSize(width, height);
        }
        
        Draw.displayWidth = MathUtil.round(width);
        Draw.displayHeight = MathUtil.round(height);
    }
    
    public void notifyDisplayChange(boolean fullscreen, float width, float height)
    {
        setSize(width, height);
    }
    
    @Override
    public float getWidth()
    {
        return width;
    }
    
    @Override
    public float getHeight()
    {
        return height;
    }
    
    public boolean isInGame()
    {
        return game != null;
    }
    
    public void draw()
    {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_MULTISAMPLE);
        
        if (game == null)
        {
            glClearColor(.75f, .75f, .75f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);
        }
        
        Draw.mat.clear();
        //left bottom right top (0, height, width, 0)
        Draw.mat.getDirect().setOrtho2D(0, width, height, 0);
        
        if (game != null)
        {
            game.draw();
        }
        
        if (menu != null)
        {
            menu.draw();
        }
    }
}