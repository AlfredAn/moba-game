package onlinegame.client.client.mainmenu;

import java.io.DataInputStream;
import java.io.IOException;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuContext;
import onlinegame.client.menu.SubMenu;
import onlinegame.shared.Logger;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class MainMenu extends Menu
{
    private SubMenu subMenu;
    
    public MainMenu(MenuContext ctx)
    {
        super(ctx);
        
        itemAlign = Align.TOPLEFT;
        doScale = false;
        
        add(subMenu = new LobbySelectMenu(this));
        
        addListener();
    }
    
    @Override
    public void readMessage(InputMessage msg) throws IOException
    {
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.S_LOBBY_JOIN:
                in = msg.getDataStream();
                
                boolean success = in.readBoolean();
                String message = in.readUTF();
                
                if (success)
                {
                    Logger.log("Joined lobby!");
                    
                    closeAlert();
                    swapSubMenu(new LobbyMenu(this));
                }
                else
                {
                    alert(message);
                }
                break;
            case Protocol.S_CHAMPSELECT_START:
                in = msg.getDataStream();
                int yourTeam = in.readByte();
                
                String[][] names = new String[2][];
                names[0] = new String[in.readByte()];
                names[1] = new String[in.readByte()];
                
                for (int t = 0; t < 2; t++)
                {
                    for (int i = 0; i < names[t].length; i++)
                    {
                        names[t][i] = in.readUTF();
                    }
                }
                
                Logger.log("Champion select started!");
                
                swapSubMenu(new ChampionSelectMenu(this, yourTeam, names));
                break;
            case Protocol.S_LOBBY_LEAVE:
                Logger.log("Left lobby.");
                swapSubMenu(new LobbySelectMenu(this));
                break;
        }
    }
    
    private void swapSubMenu(SubMenu menu)
    {
        remove(subMenu);
        add(subMenu = menu);
        subMenu.create();
    }
    
    @Override
    public void draw()
    {
        super.draw();
        
        Draw.mat.pushMatrix();
        transform(Draw.mat);
        
        Draw.color.set(Color4f.BLACK);
        
        if (!(subMenu instanceof ChampionSelectMenu))
        {
            Draw.drawLine(0, 100, 1200, 100);
        }
        
        Draw.drawLine(0, 625, 1200, 625);
        
        Draw.mat.popMatrix();
    }
}
