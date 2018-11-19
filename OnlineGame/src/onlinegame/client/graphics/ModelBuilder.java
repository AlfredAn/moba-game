package onlinegame.client.graphics;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import onlinegame.shared.MathUtil;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;

/**
 * Allows you to create models easily.
 * 
 * @author Alfred
 */
public final class ModelBuilder
{
    private static final int
            posOff = 0,
            colOff = 12;
    
    private int texOff, normOff, stride;
    private int indexOff = 0, vertexId = 0;
    
    private ByteBuffer vertexBuffer;
    private IntBuffer indexBuffer;
    
    private boolean
            hasColor,
            hasTexture,
            hasNormals;
    
    private int primMode;
    
    private final MColor4f col = new MColor4f(Color4f.WHITE);
    private float ss = 0, tt = 0;
    private float nx, ny, nz;
    
    /**
     * Starts the model creation. The model will have no color or texture data.
     * 
     * @param mode The OpenGL primitive mode.
     */
    public ModelBuilder(int mode)
    {
        this(mode, false, false);
    }
    
    /**
     * Starts the model creation.
     * 
     * @param mode The OpenGL primitive mode.
     * @param hasColor Whether the vertex data should contain color.
     */
    public ModelBuilder(int mode, boolean hasColor)
    {
        this(mode, hasColor, false);
    }
    
    /**
     * Starts the model creation.
     * 
     * @param mode The OpenGL primitive mode.
     * @param hasColor Whether the vertex data should contain color.
     * @param hasTexture Whether the vertex data should contain texture coords.
     */
    public ModelBuilder(int mode, boolean hasColor, boolean hasTexture)
    {
        this(mode, hasColor, hasTexture, false);
    }
    
    /**
     * Starts the model creation.
     * 
     * @param mode The OpenGL primitive mode.
     * @param hasColor Whether the vertex data should contain color.
     * @param hasTexture Whether the vertex data should contain texture coords.
     * @param hasNormals Whether the vertex data should contain normals.
     */
    public ModelBuilder(int mode, boolean hasColor, boolean hasTexture, boolean hasNormals)
    {
        this(mode, hasColor, hasTexture, hasNormals, 16, 16);
    }
    
    /**
     * Starts the model creation.
     * 
     * @param mode The OpenGL primitive mode.
     * @param hasColor Whether the vertex data should contain color.
     * @param hasTexture Whether the vertex data should contain texture coords.
     * @param hasNormals Whether the vertex data should contain normals.
     * @param vertexCapacity The initial vertex buffer capacity.
     * @param indexCapacity The initial index buffer capacity.
     */
    public ModelBuilder(int mode, boolean hasColor, boolean hasTexture, boolean hasNormals, int vertexCapacity, int indexCapacity)
    {
        vertexBuffer = BufferUtils.createByteBuffer(vertexCapacity * stride);
        indexBuffer = BufferUtils.createIntBuffer(indexCapacity);
        reset(mode, hasColor, hasTexture, hasNormals);
    }
    
    public void reset()
    {
        reset(primMode, hasColor, hasTexture, hasNormals);
    }
    
    /**
     * Starts the model creation. The model will have no color or texture data.
     * 
     * @param mode The OpenGL primitive mode.
     */
    public void reset(int mode)
    {
        reset(mode, false, false);
    }
    
    /**
     * Starts the model creation.
     * 
     * @param mode The OpenGL primitive mode.
     * @param hasColor Whether the vertex data should contain color.
     */
    public void reset(int mode, boolean hasColor)
    {
        reset(mode, hasColor, false);
    }
    
    /**
     * Starts the model creation.
     * 
     * @param mode The OpenGL primitive mode.
     * @param hasColor Whether the vertex data should contain color.
     * @param hasTexture Whether the vertex data should contain texture coords.
     */
    public void reset(int mode, boolean hasColor, boolean hasTexture)
    {
        reset(mode, hasColor, hasTexture, false);
    }
    
    /**
     * Starts the model creation.
     * 
     * @param mode The OpenGL primitive mode.
     * @param hasColor Whether the vertex data should contain color.
     * @param hasTexture Whether the vertex data should contain texture coords.
     * @param hasNormals Whether the vertex data should contain normals.
     */
    public void reset(int mode, boolean hasColor, boolean hasTexture, boolean hasNormals)
    {
        this.hasColor = hasColor;
        this.hasTexture = hasTexture;
        this.hasNormals = hasNormals;
        
        indexOff = 0;
        vertexId = 0;
        
        ss = 0;
        tt = 0;
        nx = 0;
        ny = 0;
        nz = 0;
        col.set(Color4f.WHITE);
        
        int off = colOff;
        if (hasColor) off += 4;
        texOff = off;
        if (hasTexture) off += 8;
        normOff = off;
        if (hasNormals) off += 4;
        stride = off;
        
        primMode = mode;
        
        vertexBuffer.clear();
        indexBuffer.clear();
    }
    
    /**
     * Sets the color of the following vertices. The alpha will be set to one.
     * The new color will persist on the current model until it is changed again.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     */
    public void color(float r, float g, float b)
    {
        col.set(r, g, b);
    }
    
    /**
     * Sets the color of the following vertices.
     * The new color will persist on the current model until it is changed again.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public void color(float r, float g, float b, float a)
    {
        col.set(r, g, b, a);
    }
    
    /**
     * Sets the color of the following vertices.
     * The new color will persist on the current model until it is changed again.
     * 
     * @param c The color to set.
     */
    public void color(Color4f c)
    {
        col.set(c);
    }
    
    /**
     * Set the texture coordinates for the next vertex.
     * 
     * @param s The s coordinate.
     * @param t The t coordinate.
     */
    public void texCoord(float s, float t)
    {
        ss = s;
        tt = t;
    }
    
    /**
     * Set the normal vector for the next vertex.
     * The vector will be normalized.
     * 
     * @param x
     * @param y
     * @param z
     */
    public void normal(float x, float y, float z)
    {
        //normalize normal vector
        float lenSqr = x * x + y * y + z * z;
        if (Math.abs(lenSqr - 1) <= .0000004f)
        {
            nx = x;
            ny = y;
            nz = z;
        }
        else
        {
            float len = (float)Math.sqrt(lenSqr);
            nx = x / len;
            ny = y / len;
            nz = z / len;
        }
    }
    
    /**
     * Adds a vertex to the buffer using the currently set color and/or
     * texture coordinates (if applicable). The vertex will have a z coordinate
     * of zero.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The index of the new vertex, relative to the last time
     * {@link flush()} was called.
     * 
     * @throws IllegalStateException If this method is called before the model
     * is started.
     */
    public int vertex(float x, float y)
    {
        return vertex(x, y, 0);
    }
    
    /**
     * Adds a vertex to the buffer using the currently set color and/or
     * texture coordinates (if applicable).
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return The index of the new vertex, relative to the last time
     * {@link flush()} was called.
     * 
     * @throws IllegalStateException If this method is called before the model
     * is started.
     */
    public int vertex(float x, float y, float z)
    {
        ensureVertexCapacity(stride);
        
        //add to buffer
        vertexBuffer.putFloat(x);
        vertexBuffer.putFloat(y);
        vertexBuffer.putFloat(z);
        
        if (hasColor)
        {
            //convert to normalized bytes
            vertexBuffer.put((byte)(col.r * 255 + .5f));
            vertexBuffer.put((byte)(col.g * 255 + .5f));
            vertexBuffer.put((byte)(col.b * 255 + .5f));
            vertexBuffer.put((byte)(col.a * 255 + .5f));
        }
        
        if (hasTexture)
        {
            vertexBuffer.putFloat(ss);
            vertexBuffer.putFloat(tt);
        }
        
        if (hasNormals)
        {
            //store as a packed int (GL_INT_2_10_10_10_REV)
            int xx = MathUtil.clamp(MathUtil.round((nx * 1023 - 1) / 2), -512, 511);
            int yy = MathUtil.clamp(MathUtil.round((ny * 1023 - 1) / 2), -512, 511);
            int zz = MathUtil.clamp(MathUtil.round((nz * 1023 - 1) / 2), -512, 511);
            int ww = 0;
            
            vertexBuffer.putInt((ww << 30) | (zz << 20) | (yy << 10) | (xx));
            
            /*vertexBuffer.put((byte)Math.round((nx * 1023 - 1) / 2));
            vertexBuffer.put((byte)Math.round((ny * 1023 - 1) / 2));
            vertexBuffer.put((byte)Math.round((nz * 1023 - 1) / 2));
            vertexBuffer.put((byte)0); //padding*/
        }
        
        return (vertexId++) - indexOff;
    }
    
    /**
     * Adds a vertex to the index buffer. The vertex indices start at zero and
     * will reset to zero every time {@link flush()} is called.
     * 
     * @param i The index of the vertex you want to add.
     */
    public void index(int i)
    {
        ensureIndexCapacity(1);
        indexBuffer.put(i + indexOff);
    }
    
    /**
     * Adds two vertices to the index buffer. The vertex indices start at zero and
     * will reset to zero every time {@link flush()} is called.
     * 
     * @param i1 The index of the first vertex you want to add.
     * @param i2 The second vertex.
     */
    public void index(int i1, int i2)
    {
        ensureIndexCapacity(2);
        indexBuffer.put(i1 + indexOff);
        indexBuffer.put(i2 + indexOff);
    }
    
    /**
     * Adds three vertices to the index buffer. The vertex indices start at zero and
     * will reset to zero every time {@link flush()} is called.
     * 
     * @param i1 The index of the first vertex you want to add.
     * @param i2 The second vertex.
     * @param i3 The third vertex.
     */
    public void index(int i1, int i2, int i3)
    {
        ensureIndexCapacity(3);
        indexBuffer.put(i1 + indexOff);
        indexBuffer.put(i2 + indexOff);
        indexBuffer.put(i3 + indexOff);
    }
    
    /**
     * Adds four vertices to the index buffer. The vertex indices start at zero and
     * will reset to zero every time {@link flush()} is called.
     * 
     * @param i1 The index of the first vertex you want to add.
     * @param i2 The second vertex.
     * @param i3 The third vertex.
     * @param i4 The fourth vertex.
     */
    public void index(int i1, int i2, int i3, int i4)
    {
        ensureIndexCapacity(4);
        indexBuffer.put(i1 + indexOff);
        indexBuffer.put(i2 + indexOff);
        indexBuffer.put(i3 + indexOff);
        indexBuffer.put(i4 + indexOff);
    }
    
    private void ensureVertexCapacity(int bytes)
    {
        //ensure capacity
        if (vertexBuffer.capacity() - vertexBuffer.position() < bytes)
        {
            vertexBuffer.flip();
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(Math.max(vertexBuffer.capacity() * 2, (int)(1.5 * (vertexBuffer.capacity() + bytes))));
            newBuffer.put(vertexBuffer);
            vertexBuffer = newBuffer;
        }
    }
    
    private void ensureIndexCapacity(int num)
    {
        if (indexBuffer.capacity() - indexBuffer.position() < num)
        {
            indexBuffer.flip();
            IntBuffer newBuffer = BufferUtils.createIntBuffer(Math.max(indexBuffer.capacity() * 2, (int)(1.5 * (indexBuffer.capacity() + num))));
            newBuffer.put(indexBuffer);
            indexBuffer = newBuffer;
        }
    }
    
    /**
     * Flushes the vertex indices and makes them relative to the current vertex
     * buffer position, effectively restarting them at zero.
     */
    public void flush()
    {
        indexOff = vertexId;
    }
    
    /**
     * 
     * @return The amount of memory the vertex and index data uses.
     */
    public int getMemoryUsage()
    {
        return vertexBuffer.position() + indexBuffer.position() * 4;
    }
    
    /**
     * Creates the model and uploads the vertex data to OpenGL. Also resets
     * the class to its initial state.
     * 
     * @return The {@link Model} created.
     * 
     * @throws IllegalStateException If this method is called before the model
     * is started.
     */
    public Model finish()
    {
        return finish(GL_STATIC_DRAW);
    }
    
    /**
     * Creates the model and uploads the vertex data to OpenGL. Also resets
     * the class to its initial state.
     * 
     * @param usageHint Usage hint (GL_STATIC_DRAW, GL_DYNAMIC_DRAW, GL_STREAM_DRAW)
     * @return The {@link Model} created.
     * 
     * @throws IllegalStateException If this method is called before the model
     * is started.
     */
    public Model finish(int usageHint)
    {
        vertexBuffer.flip();
        indexBuffer.flip();
        
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, usageHint);
        
        setVertexAttribPointers();
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        int vboi = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboi);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, usageHint);
        
        glBindVertexArray(0);
        
        Model m = new Model(vao, vbo, vboi, primMode, 0, indexBuffer.limit(), vertexBuffer.limit(), indexBuffer.limit());
        
        return m;
    }
    
    public void finish(Model m)
    {
        glBindVertexArray(m.vao);
        
        //vertex data
        if (m.vboSize < vertexBuffer.position())
        {
            //create new vbo instead of using subData
            glDeleteBuffers(m.vbo);
            
            int newSize = Math.max((int)(m.vboSize * 1.5) + 1, (int)(vertexBuffer.position() * 1.25) + 1);
            if (newSize > vertexBuffer.position())
            {
                int more = newSize - vertexBuffer.position();
                ensureVertexCapacity(more);
                for (int i = 0; i < more; i++)
                {
                    vertexBuffer.put((byte)0);
                }
            }
            
            m.vbo = glGenBuffers();
            m.vboSize = newSize;
            glBindBuffer(GL_ARRAY_BUFFER, m.vbo);
            vertexBuffer.flip();
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
            setVertexAttribPointers();
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
        else
        {
            glBindBuffer(GL_ARRAY_BUFFER, m.vbo);
            vertexBuffer.flip();
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
            setVertexAttribPointers();
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
        
        //index data
        if (m.vboiSize < indexBuffer.position())
        {
            //create new vboi instead of using subData
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glDeleteBuffers(m.vboi);
            
            m.len = indexBuffer.position();
            
            int newSize = Math.max((int)(m.vboiSize * 1.5) + 1, (int)(indexBuffer.position() * 1.25) + 1);
            if (newSize > indexBuffer.position())
            {
                int more = newSize - indexBuffer.position();
                ensureIndexCapacity(more);
                for (int i = 0; i < more; i++)
                {
                    indexBuffer.put(0);
                }
            }
            
            m.vboi = glGenBuffers();
            m.vboiSize = newSize;
            
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m.vboi);
            indexBuffer.flip();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);
        }
        else
        {
            m.len = indexBuffer.position();
            indexBuffer.flip();
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indexBuffer);
        }
    }
    
    private void setVertexAttribPointers()
    {
        //positions
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, posOff);
        
        int i = 1;
        if (hasColor)
        {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, 4, GL_UNSIGNED_BYTE, true, stride, colOff);
            i++;
        }
        if (hasTexture)
        {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, 2, GL_FLOAT, false, stride, texOff);
            i++;
        }
        if (hasNormals)
        {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, 4, GL_INT_2_10_10_10_REV, true, stride, normOff);
            i++;
        }
    }
}





















