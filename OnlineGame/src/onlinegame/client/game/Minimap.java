package onlinegame.client.game;

import onlinegame.client.Input;
import onlinegame.client.Settings;
import onlinegame.client.game.graphics.MinimapRenderer;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.DynamicModel;
import onlinegame.client.graphics.texture.Texture;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuImage;
import onlinegame.shared.MathUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Alfred
 */
final class Minimap extends GameMenuItem
{
    private final Texture background;
    private final MenuImage image;
    private final MinimapGameStateDrawer gsd;
    
    private final DynamicModel frustumModel = new DynamicModel(GL_LINE_LOOP);
    
    private boolean holdingMove, holdingScrollMap, holdingMoveOnMe, holdingScrollMapOnMe;
    
    Minimap(Menu menu, GameInterface gui, float x, float y, float width, float height)
    {
        super(menu, gui, x, y, width, height);
        
        background = MinimapRenderer.renderMinimap(gui.game.map);
        image = new MenuImage(null, x, y, width, height, background);
        image.setShowBorder(true);
        
        gsd = new MinimapGameStateDrawer(this);
    }
    
    @Override
    public void update()
    {
        super.update();
        gsd.update();
        
        if (holdingScrollMap)
        {
            if (!Input.keyDown(Input.KEY_SCROLL_MINIMAP) || Input.keyReleased(Input.KEY_SCROLL_MINIMAP))
            {
                holdingScrollMap = false;
            }
        }
        else if (Input.keyPressed(Input.KEY_SCROLL_MINIMAP))
        {
            holdingScrollMap = true;
            holdingScrollMapOnMe = isMouseOver();
        }
        
        if (holdingMove)
        {
            if (!Input.keyDown(Input.KEY_MOVE) || Input.keyReleased(Input.KEY_MOVE))
            {
                holdingMove = false;
            }
        }
        else if (Input.keyPressed(Input.KEY_MOVE))
        {
            holdingMove = true;
            holdingMoveOnMe = isMouseOver();
        }
        
        if (isMouseOver())
        {
            double mx = (Input.mouseX() - x) / width;
            double my = (Input.mouseY() - y) / height;
            if (mx >= -.001 && my >= -.001 && mx < 1.001 && my < 1.001)
            {
                Game game = gui.game;
                mx = MathUtil.clamp(mx, 0, 1) * game.map.getScaledWidth();
                my = MathUtil.clamp(my, 0, 1) * game.map.getScaledHeight();
                
                boolean allowHold = Settings.getCurrent().getb(Settings.HOLD_TO_MOVE);
                if (Input.keySelectHeldOrPressed(Input.KEY_MOVE, allowHold) && holdingMove && holdingMoveOnMe)
                {
                    game.net.moveCommand((float)mx, (float)my, !allowHold || Input.keyPressed(Input.KEY_MOVE));
                }
            }
        }
        
        if (Input.keyDown(Input.KEY_SCROLL_MINIMAP) && holdingScrollMap && holdingScrollMapOnMe)
        {
            Game game = gui.game;
            double mx = (Input.mouseX() - x) / width * game.map.getScaledWidth();
            double my = (Input.mouseY() - y) / height * game.map.getScaledHeight();
            
            game.setScrollPos(
                (float)mx,
                (float)my, !Input.keyPressed(Input.KEY_SCROLL_MINIMAP));
        }
    }
    
    @Override
    public void onReposition()
    {
        image.setPosition(x, y);
    }
    
    @Override
    public void create()
    {
        background.create();
        image.create();
        gsd.create();
    }
    
    @Override
    public void destroy()
    {
        background.destroy();
        image.destroy();
        gsd.destroy();
        frustumModel.destroy();
    }
    
    private final Vector3f point = new Vector3f();
    private final Vector4f point4 = new Vector4f();
    
    @Override
    public void draw()
    {
        image.draw();
        
        Draw.setScissor((int)x, (int)y, (int)width, (int)height);
        
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glLineWidth(1.5f);
        
        gsd.draw();
        
        Draw.mat.pushMatrix();
        Matrix4f mat = Draw.mat.getDirect();
        
        Game game = gui.game;
        game.getMatrix(mat);
        
        //draw view area
        mat.frustumCorner(Matrix4f.CORNER_NXNYPZ, point);
        game.projectToGround(point.x, point.y, point.z, point4);
        float x1 = point4.x, y1 = point4.y;
        
        mat.frustumCorner(Matrix4f.CORNER_NXPYPZ, point);
        game.projectToGround(point.x, point.y, point.z, point4);
        float x2 = point4.x, y2 = point4.y;
        
        mat.frustumCorner(Matrix4f.CORNER_PXPYPZ, point);
        game.projectToGround(point.x, point.y, point.z, point4);
        float x3 = point4.x, y3 = point4.y;
        
        mat.frustumCorner(Matrix4f.CORNER_PXNYPZ, point);
        game.projectToGround(point.x, point.y, point.z, point4);
        float x4 = point4.x, y4 = point4.y;
        
        Draw.mat.popMatrix();
        
        float invscale = game.map.invscale;
        
        frustumModel.builder.reset();
        
        frustumModel.builder.vertex(x + x1 * invscale, y + y1 * invscale);
        frustumModel.builder.vertex(x + x2 * invscale, y + y2 * invscale);
        frustumModel.builder.vertex(x + x3 * invscale, y + y3 * invscale);
        frustumModel.builder.vertex(x + x4 * invscale, y + y4 * invscale);
        
        frustumModel.builder.index(0, 1, 2, 3);
        
        frustumModel.build();
        Draw.color.set(Color4f.WHITE);
        glLineWidth(2f);
        Draw.drawModel(frustumModel.getModel());
        
        //glDisable(GL_MULTISAMPLE);
        glDisable(GL_LINE_SMOOTH);
        //glDisable(GL_POLYGON_SMOOTH);
        glLineWidth(1);
        
        Draw.resetScissor();
    }
    
    @Override
    protected float getMouseOverMinX() { return super.getMouseOverMinX() - 1; }
    @Override
    protected float getMouseOverMinY() { return super.getMouseOverMinY() - 1; }
    @Override
    protected float getMouseOverMaxX() { return super.getMouseOverMaxX() + 1; }
    @Override
    protected float getMouseOverMaxY() { return super.getMouseOverMaxY() + 1; }
}
