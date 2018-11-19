package onlinegame.client.graphics;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.opengl.OpenGLException;

/**
 * Provides various useful OpenGL-related utilities.
 * 
 * @author Alfred
 */
public final class GLUtil
{
    private GLUtil() {}
    
    public static final boolean checkGLErrors = false;
    
    /**
     * Checks for errors in the current OpenGL context and throws an exception
     * if there are any.
     * 
     * @throws OpenGLException If an OpenGL error has occurred
     */
    public static void checkErrors()
    {
        if (!checkGLErrors)
        {
            return;
        }
        
        int e = glGetError();
        
        if (e != 0)
        {
            throw new OpenGLException(e);
        }
    }
    
    private static final FloatBuffer floatBuf = BufferUtils.createFloatBuffer(16);
    
    public static void uniform1i(int location, int i1)
    {
        glUniform1i(location, i1);
    }
    
    public static void uniform2i(int location, int i1, int i2)
    {
        glUniform2i(location, i1, i2);
    }
    
    public static void uniform3i(int location, int i1, int i2, int i3)
    {
        glUniform3i(location, i1, i2, i3);
    }
    
    public static void uniform4i(int location, int i1, int i2, int i3, int i4)
    {
        glUniform4i(location, i1, i2, i3, i4);
    }
    
    public static void uniform1f(int location, float f1)
    {
        glUniform1f(location, f1);
    }
    
    public static void uniform2f(int location, float f1, float f2)
    {
        glUniform2f(location, f1, f2);
    }
    
    public static void uniform3f(int location, float f1, float f2, float f3)
    {
        glUniform3f(location, f1, f2, f3);
    }
    
    public static void uniform4f(int location, float f1, float f2, float f3, float f4)
    {
        glUniform4f(location, f1, f2, f3, f4);
    }
    
    public static void uniformColor4f(int location, MColor4f col)
    {
        uniform4f(location, col.r, col.g, col.b, col.a);
    }
    
    public static void uniformMatrix4f(int location, Matrix4f mat)
    {
        floatBuf.clear();
        mat.get(floatBuf);
        floatBuf.limit(16);
        glUniformMatrix4fv(location, false, floatBuf);
    }
}
