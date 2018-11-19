package onlinegame.client.graphics.texture;

import java.nio.ByteBuffer;
import java.util.Objects;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

/**
 *
 * @author Alfred
 */
public final class TextureBuilder
{
    private boolean finished = false;
    
    private String filename = null;
    private ByteBuffer data = null;
    private int width = -1;
    private int height = -1;
    private int textureType = GL_TEXTURE_2D;
    private int internalFormat = GL_RGBA8;
    private int wrapS = GL_CLAMP_TO_EDGE;
    private int wrapT = GL_CLAMP_TO_EDGE;
    private int minFilter = GL_LINEAR;
    private int magFilter = GL_LINEAR;
    private int mipmap = 0;
    private int anisotropy = 0;
    private float lodBias = 0;
    private int padding = 0;
    
    private int dataHash = 0;
    
    public TextureBuilder() {}
    
    public Texture finish()
    {
        check();
        finished = true;
        
        if (!(filename == null ^ data == null))
        {
            throw new IllegalStateException("You need to define EITHER a filename OR data.");
        }
        
        if (mipmap != 0)
        {
            if (minFilter == GL_NEAREST)
            {
                minFilter = GL_NEAREST_MIPMAP_LINEAR;
            }
            else if (minFilter == GL_LINEAR)
            {
                minFilter = GL_LINEAR_MIPMAP_LINEAR;
            }
        }
        
        if (mipmap == -1)
        {
            mipmap = Integer.MAX_VALUE;
        }
        if (anisotropy == -1)
        {
            anisotropy = Integer.MAX_VALUE;
        }
        
        dataHash = Objects.hashCode(data);
        if (dataHash == 0) dataHash = 1;
        
        return new Texture(this);
    }
    
    private void check()
    {
        if (finished)
        {
            throw new IllegalStateException("TextureBuilder is already finished.");
        }
    }
    
    public TextureBuilder filename(String filename)
    {
        check();
        this.filename = filename;
        return this;
    }
    
    public TextureBuilder data(ByteBuffer data, int width, int height)
    {
        check();
        this.data = data;
        this.width = width;
        this.height = height;
        return this;
    }
    
    public TextureBuilder textureType(int textureType)
    {
        check();
        this.textureType = textureType;
        return this;
    }
    
    public TextureBuilder internalFormat(int internalFormat)
    {
        check();
        this.internalFormat = internalFormat;
        return this;
    }
    
    public TextureBuilder wrap(int wrap)
    {
        check();
        this.wrapS = wrap;
        this.wrapT = wrap;
        return this;
    }
    
    public TextureBuilder wrapS(int wrapS)
    {
        check();
        this.wrapS = wrapS;
        return this;
    }
    
    public TextureBuilder wrapT(int wrapT)
    {
        check();
        this.wrapT = wrapT;
        return this;
    }
    
    public TextureBuilder minFilter(int minFilter)
    {
        check();
        this.minFilter = minFilter;
        return this;
    }
    
    public TextureBuilder magFilter(int magFilter)
    {
        check();
        this.magFilter = magFilter;
        return this;
    }
    
    public TextureBuilder mipmap(int mipmap)
    {
        check();
        this.mipmap = mipmap;
        return this;
    }
    
    public TextureBuilder anisotropy(int anisotropy)
    {
        check();
        this.anisotropy = anisotropy;
        return this;
    }
    
    public TextureBuilder lodBias(float lodBias)
    {
        check();
        this.lodBias = lodBias;
        return this;
    }
    
    public TextureBuilder padding(int padding)
    {
        check();
        this.padding = padding;
        return this;
    }
    
    public String filename()
    {
        return filename;
    }
    
    public ByteBuffer data()
    {
        return data;
    }
    
    public int width()
    {
        return width;
    }
    
    public int height()
    {
        return height;
    }
    
    public int textureType()
    {
        return textureType;
    }
    
    public int internalFormat()
    {
        return internalFormat;
    }
    
    public int wrapS()
    {
        return wrapS;
    }
    
    public int wrapT()
    {
        return wrapT;
    }
    
    public int minFilter()
    {
        return minFilter;
    }
    
    public int magFilter()
    {
        return magFilter;
    }
    
    public int mipmap()
    {
        return mipmap;
    }
    
    public int anisotropy()
    {
        return anisotropy;
    }
    
    public float lodBias()
    {
        return lodBias;
    }
    
    public int padding()
    {
        return padding;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof TextureBuilder)) return false;
        
        TextureBuilder other = (TextureBuilder)o;
        
        return (
                   Objects.equals(filename, other.filename)
                && width == other.width
                && height == other.height
                && textureType == other.textureType
                && minFilter == other.minFilter
                && magFilter == other.magFilter
                && mipmap == other.mipmap
                && anisotropy == other.anisotropy
                && lodBias == other.lodBias
                && padding == other.padding
                && (dataHash == 0 || dataHash == other.dataHash)
                && Objects.equals(data, other.data));
    }
    
    private int dataHash()
    {
        if (finished)
        {
            return dataHash;
        }
        else
        {
            int h = Objects.hashCode(data);
            return h == 0 ? 1 : h;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(filename);
        hash = 89 * hash + dataHash();
        hash = 89 * hash + width;
        hash = 89 * hash + height;
        hash = 89 * hash + textureType;
        hash = 89 * hash + minFilter;
        hash = 89 * hash + magFilter;
        hash = 89 * hash + mipmap;
        hash = 89 * hash + anisotropy;
        hash = 89 * hash + lodBias == 0 ? 0 : Float.floatToIntBits(lodBias);
        hash = 89 * hash + padding;
        return hash;
    }
}
