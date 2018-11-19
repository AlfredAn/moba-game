package onlinegame.client.graphics;

import onlinegame.shared.Logger;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

/**
 *
 * @author Alfred
 */
public final class Model
{
    int vao, vbo, vboi, mode, off, len, vboSize, vboiSize;
    private boolean isDestroyed;
    
    /**
     * Creates the {@code Model} object from the specified parameters
     * and OpenGL objects.
     * 
     * @param vao The vertex array object.
     * @param vbo The vertex buffer object.
     * @param vboi The index buffer object.
     * @param mode The OpenGL draw mode, e.g. GL_TRIANGLES.
     * @param off The offset to start at in the index buffer.
     * @param len The number of vertices to draw.
     * @param vboSize The size of the vbo in bytes.
     * @param vboiSize The size of the vboi in bytes (divided by 4).
     */
    public Model(int vao, int vbo, int vboi, int mode, int off, int len, int vboSize, int vboiSize)
    {
        this.vao = vao;
        this.vbo = vbo;
        this.vboi = vboi;
        this.mode = mode;
        this.off = off;
        this.len = len;
        this.vboSize = vboSize;
        this.vboiSize = vboiSize;
    }
    
    /**
     * Destroys all associated OpenGL objects and frees the graphics memory.
     * Must be called before the object is discarded.
     */
    public void destroy()
    {
        glDeleteBuffers(vbo);
        glDeleteBuffers(vboi);
        glDeleteVertexArrays(vao);
        isDestroyed = true;
        
        GLUtil.checkErrors();
    }
    
    /**
     * Draws the object. Temporary method.
     */
    public void draw()
    {
        glBindVertexArray(vao);
        glDrawElements(mode, len, GL_UNSIGNED_INT, off);
        
        GLUtil.checkErrors();
    }
    
    @Override
    public void finalize() throws Throwable
    {
        if (!isDestroyed)
        {
            Logger.logError("Model was not destroyed.\nmode=" + mode + "\noff=" + off + "\nlen=" + len);
            destroy();
        }
        
        super.finalize();
    }
}
