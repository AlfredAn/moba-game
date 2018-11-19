package onlinegame.client;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import onlinegame.shared.Logger;
import onlinegame.client.graphics.GLUtil;
import java.io.IOException;
import onlinegame.client.client.Client;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.text.TextModel;
import onlinegame.shared.MathUtil;
import onlinegame.shared.account.Encryption;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Alfred
 */
public final class OnlineGameClient implements Runnable
{
    private static OnlineGameClient ogc;
    private static final boolean isDist = true;
    
    private Display display;
    private boolean exit = false;
    private Client client;
    
    private static final TMap<String, String> argMap = new THashMap<>();
    
    public static void main(String[] args) throws IOException
    {
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            int index = arg.indexOf('=');
            if (index != -1)
            {
                String name = arg.substring(0, index);
                String value = arg.substring(index+1);
                argMap.put(name, value);
            }
        }
        
        ogc = new OnlineGameClient();
    }
    
    public static String getArgument(String name)
    {
        String result = argMap.get(name);
        return result == null ? "" : result;
    }
    
    private OnlineGameClient()
    {
        Thread thread = new Thread(this, "Main Thread");
        thread.setPriority(7);
        thread.setDaemon(false);
        thread.start();
    }
    
    @Override
    public void run()
    {
        try
        {
            init();
            mainLoop();
            destroy();
        }
        finally
        {
            glfwTerminate();
        }
        
        Logger.log("Client closed.");
    }
    
    private void mainLoop()
    {
        Logger.log("Starting main loop...");
        
        while (!exit)
        {
            update();
            draw();
            
            if (display.isCloseRequested())
            {
                exit();
            }
        }
    }
    
    private boolean fullscreenLock = false;
    
    private void update()
    {
        Input.update();
        Timing.update();
        
        boolean notifyDisplayChange = false;
        boolean isInGame = client != null && client.isInGame();
        boolean shouldInitGL = false;
        if ((!isInGame && display.isFullscreen()) || (Input.keyPressed(Input.KEY_FULLSCREEN) && !fullscreenLock && isInGame))
        {
            fullscreenLock = true;
            destroyGL();
            display.toggleFullscreen();
            shouldInitGL = true;
            Input.update();
            Input.reset();
            
            if (client != null)
            {
                notifyDisplayChange = true;
            }
        }
        
        if (!Input.keyDown(Input.KEY_FULLSCREEN))
        {
            fullscreenLock = false;
        }
        
        if (client != null && (display.hasSizeChanged() || notifyDisplayChange))
        {
            Logger.log("notifyDisplayChange");
            client.notifyDisplayChange(display.isFullscreen(), display.getWidth(), display.getHeight());
        }
        
        if (shouldInitGL)
        {
            initGL();
        }
        
        if (client != null)
        {
            client.update();
        }
        
        if (Input.keyPressed(Input.KEY_EXIT))
        {
            exit();
        }
    }
    
    private void draw()
    {
        if (client != null)
        {
            client.draw();
        }
        else
        {
            glClearColor(.25f, .25f, .25f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);
        }
        
        display.update();
        
        GLUtil.checkErrors();
    }
    
    /**
     * Causes the client to shut down after the current frame is finished.
     */
    public static void exit()
    {
        ogc.exit = true;
    }
    
    private void init()
    {
        Logger.log("Initializing client...");
        
        Encryption.touch();
        
        initLibraries();
        
        display = new Display();
        Settings.init();
        Input.init(display);
        initGL();
        
        Timing.init();
        
        Logger.log("-----");
        Logger.log("Client started successfully!");
        Logger.log("-----");
    }
    
    private void initGL()
    {
        try
        {
            Shaders.load();
            Models.load();
            TextModel.init();
            Fonts.load();
            Textures.load();
            Draw.init();
            
            if (client == null)
            {
                client = new Client(display.getWidth(), display.getHeight());
            }
            client.create();
            Draw.displayWidth = MathUtil.round(client.getWidth());
            Draw.displayHeight = MathUtil.round(client.getHeight());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error loading graphics resources.", e);
        }
    }
    
    private void destroyGL()
    {
        if (client != null)
        {
            client.destroy();
        }
        Draw.destroy();
        Textures.destroy();
        Fonts.destroy();
        TextModel.destroyStatic();
        Models.destroy();
        Shaders.destroy();
    }
    
    private void destroy()
    {
        Logger.log("Shutting down client...");
        
        destroyGL();
        
        display.destroy();
    }
    
    private void initLibraries()
    {
        //System.setProperty("org.lwjgl.util.Debug", "true");
        
        if (isDist)
        {
            Logger.log("Setting LWJGL library path...");
            
            //System.setProperty("org.lwjgl.librarypath", new File("native/" + natives + "/" + arch).getAbsolutePath());
            //Logger.log(new File("native").getAbsolutePath());
            System.setProperty("org.lwjgl.librarypath", new File("native").getAbsolutePath());
        }
    }
}





















