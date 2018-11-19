package onlinegame.client;

import onlinegame.shared.Logger;
import onlinegame.client.graphics.GLUtil;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * A {@code Display} contains an OpenGL context in a GLFW window, either windowed
 * or in fullscreen.
 * 
 * @author Alfred
 */
public final class Display
{
    public static final int
            DEFAULT_WIDTH = 1200,
            DEFAULT_HEIGHT = 675;
    
    private long window = NULL;
    private int width, height;
    private int newWidth, newHeight;
    private boolean isFullscreen, sizeChanged;
    private GLFWVidMode desktopVidmode;
    
    private final IntBuffer wBuf = BufferUtils.createIntBuffer(1);
    private final IntBuffer hBuf = BufferUtils.createIntBuffer(1);
    
    private ClientWindowCallback callback;
    private final GLFWErrorCallback err;
    
    /**
     * Creates a new {@code Display} with the default parameters.
     */
    public Display()
    {
        Logger.log("Creating display...");
        
        Logger.log("Initializing LWJGL version " + Version.getVersion() + "...");
        
        glfwSetErrorCallback(err = GLFWErrorCallback.createPrint(System.err));
        
        if (glfwInit() != GL_TRUE)
        {
            throw new RuntimeException("Failed to initialize GLFW.");
        }
        
        Logger.log("Creating main window...");
        window = createWindow(false, Settings.getCurrent().geti(Settings.MSAA));
        
        if (window == NULL)
        {
            throw new RuntimeException("Failed to create GLFW window.");
        }
        
        Logger.log("GL context version: " + glGetString(GL_VERSION));
        
        GLUtil.checkErrors();
    }
    
    public void update()
    {
        glfwSwapBuffers(window);
        
        if (newWidth != width || newHeight != height)
        {
            width = newWidth;
            height = newHeight;
            glViewport(0, 0, width, height);
            sizeChanged = true;
            
            Logger.log("Window size: " + this.width + "x" + this.height);
        }
        else
        {
            sizeChanged = false;
        }
    }
    
    public void destroy()
    {
        Logger.log("Destroying display...");
        
        destroyWindow();
        window = NULL;
        err.release();
    }
    
    private long createWindow(boolean fullscreen, int msaa)
    {
        if (fullscreen)
        {
            return createWindow(getDesktopWidth(), getDesktopHeight(), true, msaa);
        }
        else
        {
            return createWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, false, msaa);
        }
    }
    private long createWindow(int width, int height, boolean fullscreen, int msaa)
    {
        return createWindow(width, height, "OnlineGameClient", fullscreen, msaa);
    }
    private long createWindow(int width, int height, CharSequence title,
            boolean fullscreen, int msaa)
    {
        //set window hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
        glfwWindowHint(GLFW_DECORATED, GL_TRUE);
        
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        
        glfwWindowHint(GLFW_SAMPLES, msaa);
        
        //create the window
        long display = fullscreen ? glfwGetPrimaryMonitor() : NULL;
        long win = glfwCreateWindow(width, height, title, display, NULL);
        
        if (win == NULL)
        {
            //failed to create window
            return NULL;
        }
        
        //WindowCallback.set(win, new ClientWindowCallback(this));
        callback = new ClientWindowCallback(this, win);
        
        //get the actual window size
        glfwGetWindowSize(win, wBuf, hBuf);
        
        isFullscreen = fullscreen;
        this.width = wBuf.get();
        this.height = hBuf.get();
        wBuf.clear();
        hBuf.clear();
        
        newWidth = width;
        newHeight = height;
        
        sizeChanged = true;
        
        if (!fullscreen)
        {
            centerWindow(win);
        }
        
        glfwMakeContextCurrent(win);
        glfwShowWindow(win);
        
        //GLContext.createFromCurrent();
        GL.createCapabilities();
        
        Logger.log("Window size: " + this.width + "x" + this.height);
        
        if (!settingsInited)
        {
            Settings.initGLDependent();
            settingsInited = true;
        }
        glfwSwapInterval(Settings.getCurrent().geti(Settings.VSYNC));
        
        glViewport(0, 0, width, height);
        
        GLUtil.checkErrors();
        
        return win;
    }
    
    private static boolean settingsInited = false;
    
    private void centerWindow(long window)
    {
        glfwSetWindowPos(
                window,
                (getDesktopWidth() - width) / 2,
                (getDesktopHeight() - height) / 2);
    }
    
    void sizeChangeCallback(int width, int height)
    {
        newWidth = width;
        newHeight = height;
    }
    
    private void destroyWindow()
    {
        glfwDestroyWindow(window);
        callback.release();
        callback = null;
    }
    
    void setFullscreen(boolean fullscreen)
    {
        if (fullscreen == isFullscreen)
        {
            return;
        }
        
        destroyWindow();
        window = createWindow(fullscreen, Settings.getCurrent().geti(Settings.MSAA));
        
        isFullscreen = fullscreen;
        
        Logger.log("Fullscreen set to " + fullscreen + ".");
    }
    
    void toggleFullscreen()
    {
        setFullscreen(!isFullscreen);
    }
    
    public void requestClose()
    {
        glfwSetWindowShouldClose(window, GL_TRUE);
    }
    
    public boolean isCloseRequested()
    {
        return glfwWindowShouldClose(window) == GL_TRUE;
    }
    
    private GLFWVidMode getDesktopVidmode()
    {
        if (desktopVidmode == null)
        {
            desktopVidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        }
        
        return desktopVidmode;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public int getDesktopWidth()
    {
        return getDesktopVidmode().width();
    }
    
    public int getDesktopHeight()
    {
        return getDesktopVidmode().height();
    }
    
    public boolean isFullscreen()
    {
        return isFullscreen;
    }
    
    public boolean hasSizeChanged()
    {
        return sizeChanged;
    }
    
    public void setCursorMode(int inputMode)
    {
        glfwSetInputMode(window, GLFW_CURSOR, inputMode);
    }
    
    public void setCursorPosition(double x, double y)
    {
        glfwSetCursorPos(window, x, y);
    }
}
