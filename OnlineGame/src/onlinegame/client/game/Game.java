package onlinegame.client.game;

import java.io.IOException;
import onlinegame.client.Input;
import onlinegame.client.Models;
import onlinegame.client.Settings;
import onlinegame.client.Timing;
import onlinegame.client.client.Client;
import onlinegame.client.game.clientgamestate.CChampion;
import onlinegame.client.game.clientgamestate.CEntity;
import onlinegame.client.game.clientgamestate.CGameState;
import onlinegame.client.game.graphics.GameModels;
import onlinegame.client.game.graphics.MapRenderer;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.Model;
import onlinegame.client.graphics.ModelBuilder;
import onlinegame.client.graphics.texture.Texture;
import onlinegame.client.graphics.texture.TextureBuilder;
import onlinegame.shared.CollisionUtil;
import onlinegame.shared.MathUtil;
import onlinegame.shared.game.GameMap;
import org.joml.Matrix4f;
import org.joml.MatrixStack;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

/**
 *
 * @author Alfred
 */
public final class Game
{
    public final Client client;
    public final GameNetwork net;
    public final GameMap map;
    
    //private FrameBuffer fBuf;
    private Model groundModel, wallsModel, clickModel, bigDebugModel;
    private final Texture
            groundTex = new TextureBuilder()
                .filename("textures/game/map/grass.png")
                .mipmap(-1)
                .anisotropy(16)
                .wrap(GL_REPEAT)
                .finish(),
            wallTex = new TextureBuilder()
                .filename("textures/game/map/wall.png")
                .mipmap(-1)
                .anisotropy(16)
                .wrap(GL_REPEAT)
                .finish(),
            mouseTex = new TextureBuilder()
                .filename("textures/cursor.png")
                .minFilter(GL_NEAREST)
                .magFilter(GL_NEAREST)
                .finish();
    
    //private final GameStateDrawer gameStateDrawer;
    private final CGameState gameState;
    
    private float scrollX, scrollY, scrollTowardsX, scrollTowardsY;
    private boolean fasterScrollTransition = false;
    
    private final Vector4f mapMousePos = new Vector4f();
    private static final float scrollSpeed = 24;
    
    private float zoomTowards = 0;
    private float zoomLevel = 0;
    private float zoomScale = 1;
    
    private final GameInterface gui;
    
    private CEntity mouseOver = null, mouseOverAttackable = null;
    
    public Game(Client client) throws IOException
    {
        this.client = client;
        map = GameMap.TEST;
        
        setScrollPos(4, map.getScaledHeight() - 4, false);
        
        net = new GameNetwork(this);
        
        //gameStateDrawer = new GameStateDrawer();
        gameState = new CGameState(map);
        
        gui = new GameInterface(this);
    }
    
    public void create()
    {
        //gameStateDrawer.create();
        GameModels.create();
        
        groundTex.create();
        wallTex.create();
        mouseTex.create();
        
        ModelBuilder mb = new ModelBuilder(GL_TRIANGLES, false, true, true);
        
        float xs = 19f * client.getWidth() / client.getHeight() / 2, ys = 22f/2;
        
        mb.normal(0, 0, 1);
        
        mb.texCoord(-xs, -ys);
        mb.vertex(-xs, -ys);
        
        mb.texCoord(-xs, ys);
        mb.vertex(-xs, ys);
        
        mb.texCoord(xs, ys);
        mb.vertex(xs, ys);
        
        mb.texCoord(xs, -ys);
        mb.vertex(xs, -ys);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        
        groundModel = mb.finish();
        
        wallsModel = MapRenderer.renderMap(map);
        clickModel = Models.renderCuboidNoBottom(-.1f, -.1f, 0, .1f, .1f, .2f);
        bigDebugModel = Models.renderCuboidNoBottom(0, 0, 0, .5f, .5f, .15f);
        
        //fBuf = new FrameBuffer((int)client.getWidth(), (int)client.getHeight());
        
        double mx = Input.mouseX();
        double my = Input.mouseY();
        Input.setMouseMode(Input.MOUSE_GRABBED);
        Input.setMousePos(mx, my);
        Input.setMouseClamp(0, 0, client.getWidth(), client.getHeight());
        
        gui.create();
    }
    
    public void destroy()
    {
        //gameStateDrawer.destroy();
        GameModels.destroy();
        
        groundTex.destroy();
        wallTex.destroy();
        mouseTex.destroy();
        
        groundModel.destroy();
        wallsModel.destroy();
        clickModel.destroy();
        bigDebugModel.destroy();
        
        //fBuf.destroy();
        
        Input.setMouseMode(Input.MOUSE_NORMAL);
        Input.resetMouseClamp();
        
        gui.destroy();
    }
    
    public void stop()
    {
        net.stop();
    }
    
    public void update()
    {
        zoomTowards = MathUtil.clamp(zoomTowards - (float)Input.yScroll() * 1f/8, -1, 0);
        double ff = 1 - Math.pow(1e-21, Timing.getDelta());
        zoomLevel = MathUtil.lerp(zoomLevel, zoomTowards, (float)ff);
        zoomScale = (float)Math.pow(2, zoomLevel);
        
        //scroll view if the mouse is at the edge of the screen
        float sx = 0, sy = 0;
        float mx = (float)Input.mouseX();
        float my = (float)Input.mouseY();
        float w = client.getWidth();
        float h = client.getHeight();
        float mw = map.getScaledWidth();
        float mh = map.getScaledHeight();
        
        if ((mx <= 0 && scrollX > 0 ) || Input.keyDown(Input.KEY_SCROLL_LEFT )) sx--;
        if ((my <= 0 && scrollY > 0 ) || Input.keyDown(Input.KEY_SCROLL_UP   )) sy--;
        if ((mx >= w && scrollX < mw) || Input.keyDown(Input.KEY_SCROLL_RIGHT)) sx++;
        if ((my >= h && scrollY < mh) || Input.keyDown(Input.KEY_SCROLL_DOWN )) sy++;
        
        double len = Math.sqrt(sx * sx + sy * sy);
        if (len > 0)
        {
            float sp = (float)(scrollSpeed * zoomScale * Timing.getDelta() / len);
            sx *= sp;
            sy *= sp;
            setScrollPos(scrollTowardsX + sx, scrollTowardsY + sy, false);
        }
        
        //calculate mouse position on terrain
        Draw.mat.pushMatrix();
        
        Matrix4f tempMat = Draw.mat.getDirect();
        getMatrix(tempMat);
        tempMat.invert();
        mapMousePos.set(2 * mx / w - 1, 1 - 2 * my / h, 0, 1);
        tempMat.transform(mapMousePos);
        mapMousePos.div(mapMousePos.w);
        projectToGround(mapMousePos.x, mapMousePos.y, mapMousePos.z, (eyeDX * zoomScale) + scrollX, (eyeDY * zoomScale) + scrollY, (eyeDZ * zoomScale), mapMousePos);
        
        Draw.mat.popMatrix();
        
        //scroll exponentially towards target pos
        double f = 1 - Math.pow(fasterScrollTransition ? 1e-35 : 1e-21, Timing.getDelta());
        scrollX = (float)MathUtil.lerp(scrollX, scrollTowardsX, f);
        scrollY = (float)MathUtil.lerp(scrollY, scrollTowardsY, f);
        
        gui.update();
        
        
        //send and recieve network updates
        net.update();
        
        //update local game state
        if (Timing.getDelta() > 0) gameState.update(Timing.getDelta(), net);
        
        
        mouseOver = null;
        mouseOverAttackable = null;
        
        double minDist = Double.POSITIVE_INFINITY;
        double attackableMinDist = Double.POSITIVE_INFINITY;
        
        //GameState gs = net.getCurrentGameState();
        //Champion me = gs == null ? null : gs.getPlayer(client.getNetwork().getUsername());
        if (gameState.isInitialized())
        {
            CChampion me = gameState.getPlayer(client.getNetwork().getUsername());
            for (int i = 0; i < gameState.numEntities(); i++)
            {
                CEntity e = gameState.getEntity(i);
                float x = e.getXPos();
                float y = e.getYPos();
                
                double d = CollisionUtil.rayBox3DDistSqr(
                        x - e.getClickXSize()/2, y - e.getClickYSize()/2, 0,
                        x + e.getClickXSize()/2, y + e.getClickYSize()/2, e.getClickZSize(),
                        (eyeDX * zoomScale) + scrollX, (eyeDY * zoomScale) + scrollY, (eyeDZ * zoomScale),
                        mapMousePos.x - (eyeDX * zoomScale) - scrollX, mapMousePos.y - (eyeDY * zoomScale) - scrollY, mapMousePos.z - (eyeDZ * zoomScale));
                
                if (d < minDist)
                {
                    minDist = d;
                    mouseOver = e;
                }
                if (d < attackableMinDist && me != null && me.canAttack(e))
                {
                    attackableMinDist = d;
                    mouseOverAttackable = e;
                }
            }
        }
        
        if (mouseOverAttackable == null)
        {
            //check if a move command should be sent
            boolean allowHold = Settings.getCurrent().getb(Settings.HOLD_TO_MOVE);
            if (!gui.isHovered() && Input.keySelectHeldOrPressed(Input.KEY_MOVE, allowHold) && !gui.isHoldingMoveOnUI())
            {
                net.moveCommand(mapMousePos.x, mapMousePos.y, !allowHold || Input.keyPressed(Input.KEY_MOVE));
                //Logger.log("Click: (" + (int)(mapMousePos.x*2) + ", " + (int)(mapMousePos.y*2) + ")");
            }
        }
        else
        {
            if (!gui.isHovered() && Input.keyPressed(Input.KEY_MOVE) && !gui.isHoldingMoveOnUI())
            {
                //send attack command
                net.attackCommand(mouseOverAttackable.getState());
            }
        }
    }
    
    public void setSize(float width, float height)
    {
        gui.setSize(width, height);
    }
    
    public void projectToGround(float startX, float startY, float startZ, Vector4f dest)
    {
        projectToGround(startX, startY, startZ, (eyeDX * zoomScale) + scrollX, (eyeDY * zoomScale) + scrollY, (eyeDZ * zoomScale), dest);
    }
    public static void projectToGround(float startX, float startY, float startZ, float eyeX, float eyeY, float eyeZ, Vector4f dest)
    {
        float dx = startX - eyeX;
        float dy = startY - eyeY;
        float dz = startZ - eyeZ;
        float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= len;
        dy /= len;
        dz /= len;
        
        float f = startZ / dz;
        dest.x = startX - f * dx;
        dest.y = startY - f * dy;
        dest.z = 0;
        dest.w = 1;
    }
    
    private static final float eyeDX = 0, eyeDY = 3.75f, eyeDZ = 10;
    //private static final float (eyeDX * zoomScale) = 0, (eyeDY * zoomScale) = 0.00001f, (eyeDZ * zoomScale) = 10;
    
    public void getMatrix(MatrixStack mat)
    {
        getMatrix(mat.getDirect());
    }
    public void getMatrix(Matrix4f mat)
    {
        mat.setPerspective((float)Math.toRadians(Settings.getCurrent().getd(Settings.FOVY)), client.getWidth() / client.getHeight(), 1, 256);
        mat.lookAt((eyeDX * zoomScale), (eyeDY * zoomScale), (eyeDZ * zoomScale), 0, 0, 0, 0, 0, 1);
        mat.scale(-1, 1, 1);
        mat.translate(-scrollX, -scrollY, 0);
    }
    
    public float getScrollX()
    {
        return scrollX;
    }
    
    public float getScrollY()
    {
        return scrollY;
    }
    
    public void setScrollPos(float x, float y, boolean smooth)
    {
        setScrollPos(x, y, smooth, false);
    }
    public void setScrollPos(float x, float y, boolean smooth, boolean fast)
    {
        if (smooth)
        {
            scrollTowardsX = MathUtil.clamp(x, 0, map.getScaledWidth());
            scrollTowardsY = MathUtil.clamp(y, 0, map.getScaledHeight());
            fasterScrollTransition = fast;
        }
        else
        {
            scrollX = MathUtil.clamp(x, 0, map.getScaledWidth());
            scrollY = MathUtil.clamp(y, 0, map.getScaledHeight());
            scrollTowardsX = scrollX;
            scrollTowardsY = scrollY;
        }
    }
    
    public void draw()
    {
        if (groundModel == null)
        {
            return;
        }
        
        Draw.mat.pushMatrix();
        getMatrix(Draw.mat);
        
        //fBuf.bind();
        
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glCullFace(GL_BACK);
        
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        
        glEnable(GL_MULTISAMPLE);
        
        glClearColor(.75f, .75f, .75f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        Draw.setGameShaders(true);
        
        drawWalls();
        drawGround();
        drawGameState();
        drawMisc();
        
        Draw.setGameShaders(false);
        
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_MULTISAMPLE);
        
        //fBuf.unbind();
        
        //fBuf.bindTexture();
        //fBuf.draw();
        //fBuf.unbindTexture();
        
        Draw.mat.popMatrix();
        
        drawGUI();
    }
    
    private void drawGround()
    {
        Draw.setTexture(groundTex);
        Draw.color.set(Color4f.WHITE);
        
        Draw.mat.pushMatrix();
        Draw.mat.translate(MathUtil.floor(scrollX + .5f), MathUtil.floor(scrollY + .5f), 0);
        
        Draw.drawModel(groundModel);
        
        Draw.mat.popMatrix();
        
        Draw.resetTexture();
    }
    
    private void drawWalls()
    {
        Draw.setTexture(wallTex);
        Draw.color.set(Color4f.WHITE);
        
        Draw.drawModel(wallsModel);
        
        Draw.resetTexture();
    }
    
    private void drawGameState()
    {
        //GameState gs = net.getCurrentGameState();
        
        //if (gs != null)
        {
            //gameStateDrawer.drawGameState(gs, mouseOver);
            gameState.draw(mouseOver);
        }
    }
    
    private void drawMisc()
    {
        
    }
    
    private void drawDebugBox(float x, float y)
    {
        Draw.mat.pushMatrix();
        Draw.mat.translate(x, y, 0);
        Draw.color.set(Color4f.GREEN);
        
        Draw.drawModel(clickModel);
        
        Draw.mat.popMatrix();
    }
    
    private void drawBigDebugBox(float x, float y)
    {
        Draw.mat.pushMatrix();
        Draw.mat.translate(x, y, 0);
        Draw.color.set(Color4f.MAGENTA);
        
        Draw.drawModel(bigDebugModel);
        
        Draw.mat.popMatrix();
    }
    
    private void drawDebugLine(float x1, float y1, float x2, float y2)
    {
        Draw.mat.pushMatrix();
        Draw.mat.translate(0, 0, .1f);
        Draw.color.set(Color4f.BLACK);
        
        Draw.setGameShaders(false);
        Draw.drawLine(x1, y1, x2, y2);
        Draw.setGameShaders(true);
        
        Draw.mat.popMatrix();
    }
    
    private void drawGUI()
    {
        gui.draw();
        
        Draw.mat.pushMatrix();
        Draw.mat.getDirect().setOrtho2D(0, client.getWidth(), client.getHeight(), 0);
        
        //draw cursor
        Draw.setTexture(mouseTex);
        Draw.color.set(Color4f.WHITE);
        
        Draw.fillRect((int)Input.mouseX(), (int)Input.mouseY()-1, mouseTex.getWidth(), mouseTex.getHeight());
        
        Draw.resetTexture();
        
        Draw.mat.popMatrix();
    }
    
    public CGameState getGameState()
    {
        return gameState;
    }
}
