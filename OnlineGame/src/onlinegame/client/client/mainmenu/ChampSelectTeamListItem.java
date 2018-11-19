package onlinegame.client.client.mainmenu;

import onlinegame.client.ClientGameUtil;
import onlinegame.client.Fonts;
import onlinegame.client.Textures;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.IColor4f;
import onlinegame.client.menu.MenuImage;
import onlinegame.client.menu.MenuLabel;
import onlinegame.client.menu.MenuListBox;
import onlinegame.client.menu.MenuListItem;
import onlinegame.shared.Logger;
import onlinegame.shared.db.ChampionDB.ChampionData;

/**
 *
 * @author Alfred
 */
public final class ChampSelectTeamListItem extends MenuListItem
{
    private ChampionData champion;
    private final MenuImage championImage;
    private final MenuLabel usernameLabel;
    private final MenuLabel stateLabel;
    
    public ChampSelectTeamListItem(MenuListBox list, String username, int state, int team)
    {
        super(list, 80);
        
        championImage = new MenuImage(null, x + 5, y + 5, 64, 64);
        
        usernameLabel = new MenuLabel(null, x + 76, y + 37, username, Fonts.lobbySmallMedium, Align.BOTTOMLEFT);
        stateLabel = new MenuLabel(null, x + 76, y + 43, getStateText(state), Fonts.lobbySmall, Align.TOPLEFT);
        stateLabel.setColor(new IColor4f(0f, 0f, 0f, .5f));
        
        color.set(ClientGameUtil.getDesaturatedTeamColor(team));
    }
    
    private String getStateText(int state)
    {
        switch (state)
        {
            case 0:
                return "Waiting...";
            case 1:
                return "Picking...";
            case 2:
                return "Locked In";
            default:
                Logger.logError("Unknown state " + state + "!");
                return "<unknown>";
        }
    }
    
    public void setState(int state)
    {
        stateLabel.setText(getStateText(state));
    }
    
    public void setChampion(ChampionData champion)
    {
        this.champion = champion;
        championImage.setTexture(Textures.getChampionProfile(champion));
    }
    
    public ChampionData getChampion()
    {
        return champion;
    }
    
    @Override
    public void create()
    {
        championImage.create();
        
        usernameLabel.create();
        stateLabel.create();
    }
    
    @Override
    public void destroy()
    {
        championImage.destroy();
        
        usernameLabel.destroy();
        stateLabel.destroy();
    }
    
    @Override
    public void draw()
    {
        Draw.color.set(color);
        Draw.fillRect(x, y, width, height - 7);
        
        Draw.color.set(Color4f.BLACK);
        Draw.drawRect(x, y, width, height - 7);
        
        championImage.draw();
        
        usernameLabel.draw();
        stateLabel.draw();
    }
}
