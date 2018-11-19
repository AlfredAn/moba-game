package onlinegame.client.game.graphics;

import onlinegame.client.Models;
import onlinegame.client.graphics.Model;

/**
 *
 * @author Alfred
 */
public final class GameModels
{
    private GameModels() {}
    
    public static Model championModel, casterMinionModel, projectileModel;
    
    public static void create()
    {
        championModel = Models.renderCuboidNoBottom(-.3f, -.3f, 0, .3f, .3f, 1.7f);
        casterMinionModel = Models.renderCuboidNoBottom(-.2f, -.2f, 0, .2f, .2f, .4f);
        projectileModel = Models.renderCuboid(-.1f, -.1f, -.1f, .1f, .1f, .1f);
    }
    
    public static void destroy()
    {
        championModel.destroy();
        casterMinionModel.destroy();
        projectileModel.destroy();
    }
}
