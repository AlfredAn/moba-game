package onlinegame.client;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

final class ClientWindowCallback
{
    private final KeyCallback key;
    private final MouseButtonCallback mouseButton;
    private final CursorPosCallback cursorPos;
    private final CursorEnterCallback cursorEnter;
    private final FramebufferSizeCallback framebufferSize;
    private final CharCallback charr;
    private final ScrollCallback scroll;
    
    ClientWindowCallback(Display display, long window)
    {
        glfwSetKeyCallback(window, key = new KeyCallback());
        glfwSetMouseButtonCallback(window, mouseButton = new MouseButtonCallback());
        glfwSetCursorPosCallback(window, cursorPos = new CursorPosCallback());
        glfwSetCursorEnterCallback(window, cursorEnter = new CursorEnterCallback());
        glfwSetFramebufferSizeCallback(window, framebufferSize = new FramebufferSizeCallback(display));
        glfwSetCharCallback(window, charr = new CharCallback());
        glfwSetScrollCallback(window, scroll = new ScrollCallback());
    }
    
    void release()
    {
        key.release();
        mouseButton.release();
        cursorPos.release();
        cursorEnter.release();
        framebufferSize.release();
        charr.release();
        scroll.release();
    }
    
    private static class KeyCallback extends GLFWKeyCallback
    {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods)
        {
            Input.keyCallback(key, scancode, action, mods);
        }
    }
    
    private static class MouseButtonCallback extends GLFWMouseButtonCallback
    {
        @Override
        public void invoke(long window, int button, int action, int mods)
        {
            Input.mouseButtonCallback(button, action, mods);
        }
    }
    
    private static class CursorPosCallback extends GLFWCursorPosCallback
    {
        @Override
        public void invoke(long window, double xPos, double yPos)
        {
            Input.mousePositionCallback(xPos, yPos);
        }
    }
    
    private static class CursorEnterCallback extends GLFWCursorEnterCallback
    {
        @Override
        public void invoke(long window, int entered)
        {
            Input.mouseEnterCallback(entered != 0);
        }
    }
    
    private static class FramebufferSizeCallback extends GLFWFramebufferSizeCallback
    {
        private final Display display;
        private FramebufferSizeCallback(Display display)
        {
            this.display = display;
        }
        
        @Override
        public void invoke(long window, int width, int height)
        {
            display.sizeChangeCallback(width, height);
        }
    }
    
    private static class CharCallback extends GLFWCharCallback
    {
        @Override
        public void invoke(long window, int codepoint)
        {
            Input.charCallback((char)codepoint);
        }
    }
    
    private static class ScrollCallback extends GLFWScrollCallback
    {
        @Override
        public void invoke(long window, double xOff, double yOff)
        {
            Input.scrollCallback(xOff, yOff);
        }
    }
}