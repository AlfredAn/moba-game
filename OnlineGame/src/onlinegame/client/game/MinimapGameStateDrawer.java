package onlinegame.client.game;

import onlinegame.client.Textures;
import onlinegame.client.game.clientgamestate.CChampion;
import onlinegame.client.game.clientgamestate.CEntity;
import onlinegame.client.game.clientgamestate.CGameState;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.DynamicModel;
import onlinegame.shared.Logger;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.pathfinder.ActorPath;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

/**
 *
 * @author Alfred
 */
final class MinimapGameStateDrawer
{
    final Minimap minimap;
    /*private final Texture unitMask = new TextureBuilder()
            .filename("textures/game/minimap/mapicon-24.png")
            .anisotropy(-1)
            .padding(1)
            .finish();*/
    
    private final DynamicModel pathModel = new DynamicModel(GL_LINE_STRIP);
    
    MinimapGameStateDrawer(Minimap minimap)
    {
        this.minimap = minimap;
    }
    
    void create()
    {
        //unitMask.create();
    }
    
    void destroy()
    {
        //unitMask.destroy();
        pathModel.destroy();
    }
    
    void update()
    {
        
    }
    
    void draw()
    {
        Game game = minimap.gui.game;
        //GameState gs = game.net.getCurrentGameState();
        CGameState gs = game.getGameState();
        if (gs == null) return;
        float invscale = game.map.invscale;
        
        int num = gs.numEntities();
        for (int i = 0; i < num; i++)
        {
            CEntity e = gs.getEntity(i);
            
            switch (e.getTypeId())
            {
                case GameProtocol.E_CHAMPION:
                    CChampion c = (CChampion)e;
                    
                    //draw minimap icon
                    //Draw.setTexture(Textures.getChampionProfileTexture(c.champion));
                    //Draw.setMaskTexture(unitMask);
                    //Draw.setTextureFilter(Draw.FILTER_MASK_TEAM);
                    //Draw.color.set(Color4f.WHITE);
                    
                    switch (c.team)
                    {
                        case GameProtocol.TEAM_BLUE:
                            Draw.color.set(.125f, .125f, 1f); //filterColor
                            //Draw.filterColor.set(.125f, .125f, .21875f);
                            break;
                        case GameProtocol.TEAM_RED:
                            Draw.color.set(1f, .125f, .125f);
                            //Draw.filterColor.set(.21875f, .125f, .125f);
                            break;
                        case GameProtocol.TEAM_NEUTRAL:
                            Draw.color.set(1f, 0, 1f);
                            //Draw.filterColor.set(.21875f, .125f, .21875f);
                            break;
                        default:
                            Draw.color.set(1f, 1f, 1f);
                            Logger.logError("lol (MinimapGameStateDrawer) (" + c.team + ")");
                            break;
                    }
                    
                    glEnable(GL_MULTISAMPLE);
                    float drawX = minimap.getX()+c.getXPos()*invscale;
                    float drawY = minimap.getY()+c.getYPos()*invscale;
                    Draw.fillCircle(drawX - 12, drawY - 12, 24, 24);
                    
                    Draw.setTexture(Textures.getChampionProfileWithLodBias(c.getChampion()));
                    Draw.color.set(Color4f.WHITE);
                    Draw.fillCircle(drawX - 10.5f, drawY - 10.5f, 21, 21);
                    Draw.resetTexture();
                    glDisable(GL_MULTISAMPLE);
                    
                    //Draw.resetTexture();
                    //Draw.resetMaskTexture();
                    //Draw.resetTextureFilter();
                    
                    //draw path
                    ActorPath p = c.getState().path;
                    
                    if (p.numPoints() > 1)
                    {
                        int startPoint = c.getPathPrevPoint();
                        if (startPoint < p.numPoints()-1)
                        {
                            pathModel.builder.reset();
                            
                            //float px = c.getXPos(gs.currentTime);
                            //float py = c.getYPos(gs.currentTime);
                            pathModel.builder.vertex(
                                    minimap.getX() + c.getXPos() * invscale,
                                    minimap.getY() + c.getYPos() * invscale);
                            
                            pathModel.builder.index(0);
                            
                            for (int j = startPoint+1; j < p.numPoints(); j++)
                            {
                                int index = pathModel.builder.vertex(
                                        minimap.getX() + p.getXPoint(j) * invscale,
                                        minimap.getY() + p.getYPoint(j) * invscale);
                                pathModel.builder.index(index);
                                /*float x = p.getXPoint(j);
                                float y = p.getYPoint(j);

                                Draw.drawLine(
                                        minimap.getX() + px * invscale,
                                        minimap.getY() + py * invscale,
                                        minimap.getX() +  x * invscale,
                                        minimap.getY() +  y * invscale);

                                px = x;
                                py = y;*/
                            }
                            
                            pathModel.build();
                            
                            Draw.color.set(Color4f.WHITE);
                            Draw.drawModel(pathModel.getModel());
                        }
                    }
                    
                    break;
            }
        }
    }
}
