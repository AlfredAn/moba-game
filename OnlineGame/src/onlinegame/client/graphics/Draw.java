package onlinegame.client.graphics;

import onlinegame.client.Models;
import onlinegame.client.Shaders;
import onlinegame.client.graphics.text.BitmapFont;
import onlinegame.client.graphics.text.TextModel;
import onlinegame.client.graphics.texture.Texture;
import org.joml.MatrixStack;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

/**
 *
 * @author Alfred
 */
public final class Draw
{
    private Draw() {}
    
    private static final int
            SH_PLAIN = 0,
            SH_TEXTURED = 1,
            SH_TEXT = 2,
            SH_TEXTURED_MASKED = 3,
            SH_GAMEBASE = 4,
            SH_GAME_UNTEXTURED = 5;
    
    public static final int
            FILTER_NONE = 0,
            FILTER_GRAYSCALE = 1,
            FILTER_MASK_TEAM = 2;
    private static final int FILTER_COUNT = 3;
    
    private static ShaderProgram[] shaders;
    private static int[]
            matrixUniforms,
            colorUniforms,
            textureFilterUniforms,
            filterColorUniforms;
    
    private static int textureFilter = FILTER_NONE;
    private static boolean useGameShader = false;
    
    public static int displayWidth, displayHeight;
    
    /**
     * The color that will be used for drawing when applicable.
     */
    public static final MColor4f color = new MColor4f();
    public static final MColor4f filterColor = new MColor4f();
    
    public static final MatrixStack mat = new MatrixStack(16);
    
    private static Texture texture;
    private static Texture maskTexture;
    
    public static void init()
    {
        mat.clear();
        color.set(Color4f.BLACK);
        
        shaders = new ShaderProgram[]
        {
            Shaders.plain,
            Shaders.textured,
            Shaders.text,
            Shaders.maskedTextured,
            
            Shaders.gameBase,
            Shaders.gameUntextured
        };
        matrixUniforms = new int[]
        {
            0,
            0,
            0,
            0,
            
            0,
            0
        };
        colorUniforms = new int[]
        {
            1,
            1,
            3,
            1,
            
            1,
            1
        };
        textureFilterUniforms = new int[]
        {
            -1,
            3,
            -1,
            4,
            
            3,
            -1
        };
        filterColorUniforms = new int[]
        {
            -1,
            -1,
            -1,
            5,
            
            -1,
            -1
        };
    }
    
    public static void destroy()
    {
        
    }
    
    /*private static void calcMVP()
    {
        projection.mul(view, mvp);
        mvp.mul(model);
    }*/
    
    private static void transform2D(float x, float y, float width, float height)
    {
        transform2D(x, y, width, height, false);
    }
    private static void transform2D(float x, float y, float width, float height, boolean align)
    {
        if (align)
        {
            mat.translate((int)x + .5f, (int)y + .5f, 0);
        }
        else
        {
            mat.translate(x, y, 0);
        }
        
        mat.scale(width, height, 1);
    }
    
    private static void transform2DLineAligned(float x1, float y1, float x2, float y2)
    {
        x1 = (int)x1 + .5f;
        y1 = (int)y1 + .5f;
        x2 = (int)x2 + .5f;
        y2 = (int)y2 + .5f;
        transform2D(x1, y1, x2 - x1, y2 - y1, false);
    }
    
    private static void useShader(int shader)
    {
        ShaderProgram sh = shaders[shader];
        sh.use();
        
        //setup uniforms
        int mu = matrixUniforms[shader];
        int cu = colorUniforms[shader];
        int tfu = textureFilterUniforms[shader];
        int fcu = filterColorUniforms[shader];
        
        if (mu != -1)
        {
            GLUtil.uniformMatrix4f(sh.getUniformLocation(mu), mat.getDirect());
        }
        
        if (cu != -1)
        {
            GLUtil.uniformColor4f(sh.getUniformLocation(cu), color);
        }
        
        if (tfu != -1)
        {
            GLUtil.uniform1i(sh.getUniformLocation(tfu), textureFilter);
        }
        
        if (fcu != -1)
        {
            GLUtil.uniformColor4f(sh.getUniformLocation(fcu), filterColor);
        }
        
        GLUtil.checkErrors();
    }
    
    public static void setTexture(Texture tex)
    {
        texture = tex;
    }
    
    public static void resetTexture()
    {
        setTexture(null);
    }
    
    public static void setMaskTexture(Texture tex)
    {
        maskTexture = tex;
    }
    
    public static void resetMaskTexture()
    {
        setMaskTexture(null);
    }
    
    public static void resetTextureFilter()
    {
        setTextureFilter(FILTER_NONE);
    }
    
    public static void setTextureFilter(int filter)
    {
        if (filter < 0 || filter >= FILTER_COUNT)
        {
            throw new IllegalArgumentException("Invalid filter id: " + filter);
        }
        textureFilter = filter;
    }
    
    public static void resetScissor()
    {
        glDisable(GL_SCISSOR_TEST);
    }
    
    public static void setScissor(int x, int y, int width, int height)
    {
        glEnable(GL_SCISSOR_TEST);
        glScissor(x, displayHeight - y - height, width, height);
    }
    
    public static void setGameShaders(boolean enable)
    {
        useGameShader = enable;
    }
    
    private static boolean shouldAbortDraw()
    {
        return color.a == 0;
    }
    
    public static void drawLine(float x1, float y1, float x2, float y2)
    {
        drawTransformedModel(Models.drawRect, x1, y1, x2 - x1, y2 - y1, true, true);
    }
    
    public static void fillRect(float x, float y, float width, float height)
    {
        drawTransformedModel(Models.fillRect, x, y, width, height);
    }
    
    public static void drawRect(float x, float y, float width, float height)
    {
        drawTransformedModel(Models.drawRect, x, y, width, height, true);
    }
    
    public static void fillCircle(float x, float y, float width, float height)
    {
        drawTransformedModel(Models.fillCircle16, x, y, width, height);
    }
    
    public static void drawCircle(float x, float y, float width, float height)
    {
        drawTransformedModel(Models.drawCircle16, x, y, width, height);
    }
    
    public static void drawText(TextModel btm, float x, float y)
    {
        if (shouldAbortDraw()) return;
        
        mat.pushMatrix();
        mat.translate(x, y, 0);
        useShader(SH_TEXT);
        mat.popMatrix();
        
        BitmapFont font = btm.font;
        GLUtil.uniform2f(shaders[SH_TEXT].getUniformLocation(1), 1f / font.getPageWidth(), 1f / font.getPageHeight()); //u_Scale
        GLUtil.uniform1i(shaders[SH_TEXT].getUniformLocation(2), 0);
        
        btm.draw();
    }
    
    private static void drawTransformedModel(Model model, float x, float y, float width, float height)
    {
        drawTransformedModel(model, x, y, width, height, false);
    }
    private static void drawTransformedModel(Model model, float x, float y, float width, float height, boolean align)
    {
        drawTransformedModel(model, x, y, width, height, align, false);
    }
    private static void drawTransformedModel(Model model, float x, float y, float width, float height, boolean align, boolean lineAlign)
    {
        mat.pushMatrix();
        if (lineAlign)
        {
            transform2DLineAligned(x, y, x + width, y + height);
        }
        else
        {
            transform2D(x, y, width, height, align);
        }
        
        drawModel(model);
        
        mat.popMatrix();
    }
    
    public static void drawModel(Model model)
    {
        if (shouldAbortDraw()) return;
        
        if (texture == null || texture.getBuilder().textureType() != GL_TEXTURE_2D)
        {
            useShader(useGameShader ? SH_GAME_UNTEXTURED : SH_PLAIN);
        }
        else
        {
            if (maskTexture == null)
            {
                int sh = useGameShader ? SH_GAMEBASE : SH_TEXTURED;
                useShader(sh);
                GLUtil.uniform1i(shaders[sh].getUniformLocation(2), 0);

                glActiveTexture(GL_TEXTURE0);
                texture.bind();
            }
            else
            {
                useShader(SH_TEXTURED_MASKED);
                GLUtil.uniform1i(shaders[SH_TEXTURED_MASKED].getUniformLocation(2), 0);
                GLUtil.uniform1i(shaders[SH_TEXTURED_MASKED].getUniformLocation(3), 1);

                glActiveTexture(GL_TEXTURE0);
                texture.bind();
                glActiveTexture(GL_TEXTURE1);
                maskTexture.bind();
            }
        }
        
        model.draw();
        
        if (texture != null)
        {
            if (maskTexture == null)
            {
                texture.unbind();
            }
            else
            {
                glActiveTexture(GL_TEXTURE0);
                texture.unbind();
                glActiveTexture(GL_TEXTURE1);
                maskTexture.unbind();
            }
        }
    }
}