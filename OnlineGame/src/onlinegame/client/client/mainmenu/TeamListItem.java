package onlinegame.client.client.mainmenu;

import onlinegame.client.ClientGameUtil;
import onlinegame.client.Fonts;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.IColor4f;
import onlinegame.client.menu.MenuLabel;
import onlinegame.client.menu.MenuListBox;
import onlinegame.client.menu.MenuListItem;

/**
 *
 * @author Alfred
 */
public final class TeamListItem extends MenuListItem
{
    private final MenuLabel label, ownerLabel;
    
    public TeamListItem(MenuListBox list, String username, int team, boolean isOwner)
    {
        super(list, 30);
        
        label = new MenuLabel(null, x + 10, y + 15, username, Fonts.lobbySmall, Align.LEFT);
        
        if (isOwner)
        {
            ownerLabel = new MenuLabel(null, x + width - 10, y + 15, "(owner)", Fonts.lobbySmall, Align.RIGHT);
            ownerLabel.setColor(new IColor4f(0f, 0f, 0f, .5f));
        }
        else
        {
            ownerLabel = null;
        }
        
        color.set(ClientGameUtil.getDesaturatedTeamColor(team));
    }
    
    @Override
    public void create()
    {
        label.create();
        
        if (ownerLabel != null)
        {
            ownerLabel.create();
        }
    }
    
    @Override
    public void destroy()
    {
        label.destroy();
        
        if (ownerLabel != null)
        {
            ownerLabel.destroy();
        }
    }
    
    @Override
    public void draw()
    {
        Draw.color.set(color);
        Draw.fillRect(x, y, width, height);
        
        label.draw();
        
        if (ownerLabel != null)
        {
            ownerLabel.draw();
        }
        
        Draw.color.set(Color4f.BLACK);
        Draw.drawRect(x, y, width, height);
    }
}
