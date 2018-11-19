package onlinegame.client;

import onlinegame.shared.Logger;
import onlinegame.shared.MathUtil;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import static org.lwjgl.glfw.GLFW.*;

public final class Input
{
    public static final int
            KEY_EXIT = 0,
            KEY_FULLSCREEN = 1,
            KEY_LEFTCLICK = 2,
            KEY_ERASE = 3,
            KEY_PASTE = 4,
            KEY_ENTER = 5,
            KEY_SCROLL_RIGHT = 6,
            KEY_SCROLL_UP = 7,
            KEY_SCROLL_LEFT = 8,
            KEY_SCROLL_DOWN = 9,
            KEY_MOVE = 10,
            KEY_SCROLL_MINIMAP = 11;
    private static final int
            KEY_AMOUNT = 12, KEY_ALTS = 1;
    
    private static int[][] keys;
    private static int[][] keyMods;
    private static int[][] keyScancode;
    private static boolean[]
            keyDown, keyPressed, keyReleased, keyRepeated,
            keyDownEvent, keyPressEvent, keyReleaseEvent, keyRepeatEvent;
    
    private static double xScroll, yScroll, xScrollNext, yScrollNext;
    
    private static double mouseX, mouseY, newMouseX, newMouseY;
    private static boolean mouseInWindow, newMouseInWindow;
    
    public static final int
            MOUSE_NORMAL = 0,
            MOUSE_HIDDEN = 1,
            MOUSE_GRABBED = 2;
    
    private static int mouseMode = MOUSE_NORMAL;
    private static boolean clampMouse;
    private static double mouseMinX, mouseMaxX, mouseMinY, mouseMaxY;
    
    private static final StringBuffer chars = new StringBuffer(64);
    private static String lastChars = "";
    
    static Display display;
    
    private static final Object updateLock = new Object();
    
    private Input() {}
    
    static void init(Display d)
    {
        Logger.log("Loading keybindings...");
        
        keys = new int[KEY_AMOUNT][KEY_ALTS];
        keyMods = new int[KEY_AMOUNT][KEY_ALTS];
        keyScancode = new int[KEY_AMOUNT][KEY_ALTS];
        
        keyDown = new boolean[KEY_AMOUNT];
        keyPressed = new boolean[KEY_AMOUNT];
        keyReleased = new boolean[KEY_AMOUNT];
        keyRepeated = new boolean[KEY_AMOUNT];
        
        keyDownEvent = new boolean[KEY_AMOUNT];
        keyPressEvent = new boolean[KEY_AMOUNT];
        keyReleaseEvent = new boolean[KEY_AMOUNT];
        keyRepeatEvent = new boolean[KEY_AMOUNT];
        
        for (int i = 0; i < KEY_AMOUNT; i++)
        {
            for (int j = 0; j < KEY_ALTS; j++)
            {
                keys[i][j] = -2;
                keyScancode[i][j] = -2;
                keyMods[i][j] = 0;
            }
            
            keyDown[i] = false;
            keyPressed[i] = false;
            keyReleased[i] = false;
            keyRepeated[i] = false;
            
            keyDownEvent[i] = false;
            keyPressEvent[i] = false;
            keyReleaseEvent[i] = false;
            keyRepeatEvent[i] = false;
        }
        
        ////keybindings
        keys[KEY_EXIT][0] = GLFW_KEY_ESCAPE;
        
        keys[KEY_FULLSCREEN][0] = GLFW_KEY_F11;
        
        keys[KEY_LEFTCLICK][0] = -1000; //left click
        
        keys[KEY_ERASE][0] = GLFW_KEY_BACKSPACE;
        
        keys[KEY_PASTE][0] = GLFW_KEY_V;
        keyMods[KEY_PASTE][0] = GLFW_MOD_CONTROL;
        
        keys[KEY_ENTER][0] = GLFW_KEY_ENTER;
        
        keys[KEY_SCROLL_RIGHT][0] = GLFW_KEY_RIGHT;
        keys[KEY_SCROLL_UP][0] = GLFW_KEY_UP;
        keys[KEY_SCROLL_LEFT][0] = GLFW_KEY_LEFT;
        keys[KEY_SCROLL_DOWN][0] = GLFW_KEY_DOWN;
        
        keys[KEY_MOVE][0] = -1001; //right click
        
        keys[KEY_SCROLL_MINIMAP][0] = -1000; //left click
        ////
        
        mouseX = 0;
        mouseY = 0;
        newMouseX = 0;
        newMouseY = 0;
        
        mouseInWindow = false;
        newMouseInWindow = false;
        
        xScroll = 0;
        yScroll = 0;
        xScrollNext = 0;
        yScrollNext = 0;
        
        clampMouse = false;
        
        display = d;
    }
    
    static void update()
    {
        synchronized (updateLock)
        {
            glfwPollEvents();
            
            for (int i = 0; i < KEY_AMOUNT; i++)
            {
                keyDown[i] = keyDownEvent[i];
                keyPressed[i] = keyPressEvent[i];
                keyReleased[i] = keyReleaseEvent[i];
                keyRepeated[i] = keyRepeatEvent[i];
                
                keyPressEvent[i] = false;
                keyReleaseEvent[i] = false;
                keyRepeatEvent[i] = false;
            }
            
            if (clampMouse)
            {
                mouseX = MathUtil.clamp(newMouseX, mouseMinX, mouseMaxX);
                mouseY = MathUtil.clamp(newMouseY, mouseMinY, mouseMaxY);
                
                if (mouseX != newMouseX || mouseY != newMouseY)
                {
                    setMousePos(mouseX, mouseY);
                }
            }
            else
            {
                mouseX = newMouseX;
                mouseY = newMouseY;

                mouseInWindow = newMouseInWindow;
            }
            
            mouseInWindow = mouseMode == MOUSE_GRABBED || newMouseInWindow;
            
            xScroll = xScrollNext;
            yScroll = yScrollNext;
            xScrollNext = 0;
            yScrollNext = 0;
            
            if (chars.length() == 0)
            {
                lastChars = "";
            }
            else
            {
                lastChars = chars.toString();
                chars.setLength(0);
            }
        }
    }
    
    static void reset()
    {
        synchronized (updateLock)
        {
            for (int i = 0; i < KEY_AMOUNT; i++)
            {
                keyDown[i] = false;
                keyPressed[i] = false;
                keyReleased[i] = false;
                keyRepeated[i] = false;
                
                keyDownEvent[i] = false;
                keyPressEvent[i] = false;
                keyReleaseEvent[i] = false;
                keyRepeatEvent[i] = false;
            }
        }
    }
    
    static void keyCallback(int key, int scancode, int action, int mods)
    {
        int id;
        for (int i = 0; i < KEY_AMOUNT; i++)
        {
            for (int j = 0; j < KEY_ALTS; j++)
            {
                if (key == keys[i][j] && (key != GLFW_KEY_UNKNOWN || (scancode == keyScancode[i][j] && scancode != -2)))
                {
                    id = i;
                    if (id != -2 && (keyMods[id][0] & mods) == keyMods[id][0])
                    {
                        synchronized (updateLock)
                        {
                            if (action == GLFW_PRESS)
                            {
                                keyPressEvent[id] = true;
                                keyDownEvent[id] = true;
                            }
                            else if (action == GLFW_RELEASE)
                            {
                                keyReleaseEvent[id] = true;
                                keyDownEvent[id] = false;
                            }
                            else if (action == GLFW_REPEAT)
                            {
                                keyRepeatEvent[id] = true;
                            }
                        }
                    }
                }
            }
        }
        
        
    }
    
    static void mouseButtonCallback(int button, int action, int mods)
    {
        keyCallback(-1000 - button, -2, action, mods);
    }
    
    static void mousePositionCallback(double xPos, double yPos)
    {
        synchronized (updateLock)
        {
            newMouseX = xPos;
            newMouseY = yPos;
        }
    }
    
    static void mouseEnterCallback(boolean entered)
    {
        synchronized (updateLock)
        {
            newMouseInWindow = entered;
        }
    }
    
    static void charCallback(char c)
    {
        synchronized (updateLock)
        {
            if (chars.length() >= chars.capacity()) return;
            chars.append(c);
        }
    }
    
    static void scrollCallback(double x, double y)
    {
        synchronized (updateLock)
        {
            xScrollNext += x;
            yScrollNext += y;
        }
    }
    
    /*private static int getKeyId(int key, int scancode)
    {
        for (int i = 0; i < KEY_AMOUNT; i++)
        {
            for (int j = 0; j < KEY_ALTS; j++)
            {
                if (key == keys[i][j] && (key != GLFW_KEY_UNKNOWN || (scancode == keyScancode[i][j] && scancode != -2)))
                {
                    return i;
                }
            }
        }
        
        return -2;
    }*/
    
    private static void assertKey(int keyId)
    {
        if (keyId < 0 || keyId >= KEY_AMOUNT)
        {
            throw new IllegalArgumentException("Invalid key id: " + keyId);
        }
    }
    
    public static boolean keyDown(int keyId)
    {
        assertKey(keyId);
        return keyDown[keyId];
    }
    
    public static boolean keyPressed(int keyId)
    {
        assertKey(keyId);
        return keyPressed[keyId];
    }
    
    public static boolean keyDownOrPressed(int keyId)
    {
        assertKey(keyId);
        return keyDown[keyId] || keyPressed[keyId];
    }
    
    public static boolean keyReleased(int keyId)
    {
        assertKey(keyId);
        return keyReleased[keyId];
    }
    
    public static boolean keyRepeated(int keyId)
    {
        assertKey(keyId);
        return keyRepeated[keyId];
    }
    
    public static boolean keySelectHeldOrPressed(int keyId, boolean allowHold)
    {
        assertKey(keyId);
        return keyPressed[keyId] || (allowHold && keyDown[keyId]);
    }
    
    public static double mouseX()
    {
        return mouseX;
    }
    
    public static double mouseY()
    {
        return mouseY;
    }
    
    public static boolean mouseInWindow()
    {
        return mouseInWindow;
    }
    
    public static double xScroll()
    {
        return xScroll;
    }
    
    public static double yScroll()
    {
        return yScroll;
    }
    
    private static final int[] glfwModes =
    {
        GLFW_CURSOR_NORMAL,
        GLFW_CURSOR_HIDDEN,
        GLFW_CURSOR_DISABLED
    };
    public static void setMouseMode(int mode)
    {
        display.setCursorMode(glfwModes[mode]);
        mouseMode = mode;
    }
    
    public static void setMousePos(double x, double y)
    {
        display.setCursorPosition(x, y);
        newMouseX = x;
        newMouseY = y;
    }
    
    public static void setMouseClamp(double x1, double y1, double x2, double y2)
    {
        mouseMinX = x1;
        mouseMaxX = x2;
        mouseMinY = y1;
        mouseMaxY = y2;
        clampMouse = true;
        
        double mx = MathUtil.clamp(newMouseX, mouseMinX, mouseMaxX);
        double my = MathUtil.clamp(newMouseY, mouseMinY, mouseMaxY);
        
        if (mx != newMouseX || my != newMouseY)
        {
            setMousePos(mx, my);
        }
    }
    
    public static void resetMouseClamp()
    {
        clampMouse = false;
    }
    
    public static boolean mouseInArea(double x1, double y1, double x2, double y2)
    {
        return (mouseInWindow
                && mouseX >= x1 && mouseX < x2
                && mouseY >= y1 && mouseY < y2);
    }
    
    private static final Vector4f tempVec = new Vector4f();
    
    public static boolean mouseInArea(Matrix4f transform, double x1, double y1, double x2, double y2)
    {
        tempVec.set((float)mouseX, (float)mouseY, 0, 1);
        tempVec.mul(transform);
        tempVec.div(tempVec.w);
        
        return (mouseInWindow
                && tempVec.x >= x1 && tempVec.x < x2
                && tempVec.y >= y1 && tempVec.y < y2);
    }
    
    public static String keyboardString()
    {
        return lastChars;
    }
}






























