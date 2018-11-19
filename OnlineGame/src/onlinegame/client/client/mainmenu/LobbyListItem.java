package onlinegame.client.client.mainmenu;

import onlinegame.client.Fonts;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.menu.MenuLabel;
import onlinegame.client.menu.MenuListBox;
import onlinegame.client.menu.MenuListItem;

/**
 *
 * @author Alfred
 */
public final class LobbyListItem extends MenuListItem
{
    public static final int
            NAME = 0,
            OWNER = 1,
            PLAYERS = 2,
            GAMEMODE = 3,
            MAP = 4,
            
            AMOUNT = 5;
    
    private static final float[] dx = new float[]
    {
        100, 250, 350, 420, 520
    };
    
    private static final String[] name = new String[]
    {
        "Name", "Owner", "Players", "Mode", "Map"
    };
    
    public static float getDX(int property)
    {
        return dx[property];
    }
    
    public static String getName(int property)
    {
        return name[property];
    }
    
    public final int id;
    private String[] attribs = new String[AMOUNT];
    private final MenuLabel[] labels = new MenuLabel[AMOUNT];
    
    public LobbyListItem(MenuListBox list, int id, String name, String owner, String players, String gamemode, String map)
    {
        this(list, id, new String[] {name, owner, players, gamemode, map});
    }
    private LobbyListItem(MenuListBox list, int id, String[] attribs)
    {
        super(list, 30);
        
        this.id = id;
        
        if (attribs.length != AMOUNT)
        {
            throw new IllegalArgumentException();
        }
        
        this.attribs = attribs;
        
        for (int i = 0; i < AMOUNT; i++)
        {
            labels[i] = new MenuLabel(null, x + dx[i], y + 15, attribs[i], Fonts.lobbySmall, Align.CENTER);
        }
    }
    
    @Override
    public void create()
    {
        for (int i = 0; i < AMOUNT; i++)
        {
            labels[i].create();
        }
    }
    
    @Override
    public void destroy()
    {
        for (int i = 0; i < AMOUNT; i++)
        {
            labels[i].destroy();
        }
    }
    
    @Override
    public boolean isSelectable()
    {
        return true;
    }
    
    @Override
    public void onSelect()
    {
        if (list.menu instanceof LobbySelectMenu)
        {
            LobbySelectMenu mainmenu = (LobbySelectMenu)list.menu;
            mainmenu.joinGameButton.setEnabled(true);
        }
    }
    
    @Override
    public void draw()
    {
        if (isSelected())
        {
            Draw.color.set(.5625f, .5625f, 1f);
        }
        else
        {
            Draw.color.set(.625f, .625f, .625f);
        }
        Draw.fillRect(x, y, width, height);
        
        for (int i = 0; i < AMOUNT; i++)
        {
            labels[i].draw();
        }
        
        switch (getMouseState())
        {
            case M_HOVER:
                Draw.color.set(1f, 1f, 1f, .125f);
                Draw.fillRect(x, y, width, height);
                break;
            case M_PRESS:
                Draw.color.set(0f, 0f, 0f, .125f);
                Draw.fillRect(x, y, width, height);
                break;
        }
        
        Draw.color.set(Color4f.BLACK);
        Draw.drawRect(x, y, width, height);
    }
}

























