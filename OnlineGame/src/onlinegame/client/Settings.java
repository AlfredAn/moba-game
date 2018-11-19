package onlinegame.client;

import onlinegame.client.graphics.GLUtil;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Alfred
 */
public final class Settings
{
    private static Settings current;
    
    public static Settings getCurrent()
    {
        return current;
    }
    
    /**
     * The amount of scaling the menus should have. A value of {@link MENU_SCALING_AUTO} means that the
     * scaling is automatic and adapts to the window size.
     */
    public static final int MENU_SCALING = 0;
    
    /**
     * Makes the scaling automatic and adapts it to the window size.
     * Value for {@link MENU_SCALING}.
     */
    public static final int MENU_SCALING_AUTO = 0;
    
    /**
     * Vertical sync.
     * 
     * Possible values:
     * <ul>
     * <li>{@link VSYNC_OFF}</li>
     * <li>{@link VSYNC_ON}</li>
     * <li>{@link VSYNC_ADAPTIVE}</li>
     * </ul>
     */
    public static final int VSYNC = 1;
    
    /**
     * Turns off VSYNC. Value for {@link VSYNC}.
     */
    public static final int VSYNC_OFF = 0;
    
    /**
     * Turns on VSYNC. Value for {@link VSYNC}.
     */
    public static final int VSYNC_ON = 1;
    
    /**
     * Turns on adaptive VSYNC. Value for {@link VSYNC}. This means that VSYNC will be on when the frame rate
     * is higher than the monitor's native refresh rate and off otherwise.
     */
    public static final int VSYNC_ADAPTIVE = -1;
    
    /**
     * Texture filtering mode.
     * 
     * Values:
     * <ul>
     * <li>Equal to zero: Bilinear</li>
     * <li>Less than zero: Mipmapping</li>
     * <li>Greater than zero: Anisotropic filtering</li>
     * </ul>
     */
    public static final int TEXTURE_FILTER = 2;
    
    public static final int FOVY = 3;
    
    public static final int MSAA = 4;
    
    public static final int HOLD_TO_MOVE = 5;
    
    private static final int SETTING_COUNT = 6;
    
    private final double[] settings = new double[SETTING_COUNT];
    
    static
    {
        init();
    }
    
    static void init()
    {
        if (current != null)
        {
            return;
        }
        
        current = new Settings();
        
        GLUtil.checkErrors();
    }
    
    static void initGLDependent()
    {
        if (glfwExtensionSupported("WGL_EXT_swap_control_tear") == GL_TRUE)
        {
            //Logger.log("Adaptive VSYNC!");
            //current.set(VSYNC, VSYNC_ADAPTIVE);
        }
        
        if (glfwExtensionSupported("GL_EXT_texture_filter_anisotropic") == GL_TRUE)
        {
            //enable anisotropic filtering
            int maxAf = glGetInteger(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            current.set(TEXTURE_FILTER, Math.min(16, maxAf));
        }
        else
        {
            //enable mipmapping
            current.set(TEXTURE_FILTER, -16);
        }
    }
    
    public Settings()
    {
        set(MENU_SCALING, MENU_SCALING_AUTO);
        set(VSYNC, VSYNC_OFF);
        set(TEXTURE_FILTER, 0);
        set(FOVY, 60);
        set(MSAA, 4);
        set(HOLD_TO_MOVE, false);
    }
    
    public void set(int setting, double value)
    {
        settings[setting] = value;
    }
    
    public void set(int setting, int value)
    {
        set(setting, (double)value);
    }
    
    public void set(int setting, boolean value)
    {
        set(setting, value ? 1 : 0);
    }
    
    public double getd(int setting)
    {
        return settings[setting];
    }
    
    public float getf(int setting)
    {
        return (float)settings[setting];
    }
    
    public int geti(int setting)
    {
        return (int)settings[setting];
    }
    
    public boolean getb(int setting)
    {
        return settings[setting] != 0;
    }
}
