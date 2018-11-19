package onlinegame.client.client.mainmenu;

import onlinegame.client.Textures;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.texture.Texture;
import onlinegame.client.menu.MenuGridBox;
import onlinegame.client.menu.MenuGridItem;
import onlinegame.client.menu.MenuImage;
import onlinegame.shared.db.ChampionDB.ChampionData;

/**
 *
 * @author Alfred
 */
public final class ChampSelectGridItem extends MenuGridItem
{
    public final ChampionData champion;
    private final MenuImage img;
    
    public ChampSelectGridItem(MenuGridBox grid, ChampionData champion)
    {
        super(grid);
        
        this.champion = champion;
        Texture tex = Textures.getChampionProfile(champion);
        img = new MenuImage(null, x+1, y+1, 64, 64, tex);
    }
    
    @Override
    public void create()
    {
        img.create();
    }
    
    @Override
    public void destroy()
    {
        img.destroy();
    }
    
    @Override
    public void draw()
    {
        if (!isEnabled())
        {
            Draw.setTextureFilter(Draw.FILTER_GRAYSCALE);
        }
        img.draw();
        if (!isEnabled())
        {
            Draw.resetTextureFilter();
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
    }
}
