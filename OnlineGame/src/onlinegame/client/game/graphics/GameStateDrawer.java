package onlinegame.client.game.graphics;

import onlinegame.client.Models;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.IColor4f;
import onlinegame.client.graphics.Model;
import onlinegame.shared.Logger;
import onlinegame.shared.game.Entity;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;

/**
 *
 * @author Alfred
 */
public final class GameStateDrawer
{
    private Model championModel, casterMinionModel;
    
    public void create()
    {
        championModel = Models.renderCuboidNoBottom(-.3f, -.3f, 0, .3f, .3f, 1.7f);
        casterMinionModel = Models.renderCuboidNoBottom(-.2f, -.2f, 0, .2f, .2f, .4f);
    }
    
    public void destroy()
    {
        championModel.destroy();
        casterMinionModel.destroy();
    }
    
    public void drawGameState(GameState gs, Entity selected)
    {
        for (int i = 0; i < gs.numEntities(); i++)
        {
            Entity e = gs.getEntity(i);
            drawEntity(e, selected == e, gs);
        }
    }
    
    private static final IColor4f[] minionTeamCol =
    {
        new IColor4f(.125f, .125f, .75f),
        new IColor4f(.75f, .125f, .125f),
        new IColor4f(.625f, .625f, .125f)
    };
    
    private void drawEntity(Entity e, boolean isSelected, GameState gs)
    {
        switch (e.getTypeId())
        {
            case GameProtocol.E_CHAMPION:
                Draw.mat.pushMatrix();
                Draw.mat.translate(e.getXPos(gs), e.getYPos(gs), 0);
                Draw.color.set(Color4f.BLUE);
                if (isSelected) {Draw.color.blend(Color4f.WHITE, .25f);}
                
                Draw.drawModel(championModel);
                
                Draw.mat.popMatrix();
                break;
            case GameProtocol.E_MINION_CASTER:
                Draw.mat.pushMatrix();
                Draw.mat.translate(e.getXPos(gs), e.getYPos(gs), 0);
                Draw.color.set(minionTeamCol[e.team]);
                if (isSelected) {Draw.color.blend(Color4f.WHITE, .25f);}
                
                Draw.drawModel(casterMinionModel);
                
                Draw.mat.popMatrix();
                break;
            default:
                Logger.logError("Invalid entity id for drawing: " + e.getTypeId());
                break;
        }
    }
}
