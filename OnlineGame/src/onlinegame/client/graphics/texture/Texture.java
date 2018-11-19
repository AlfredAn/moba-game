package onlinegame.client.graphics.texture;

import de.matthiasmann.twl.utils.PNGDecoder;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import onlinegame.client.Settings;
import onlinegame.client.graphics.GLUtil;
import onlinegame.client.io.FileLoader;
import onlinegame.shared.Logger;
import onlinegame.shared.MathUtil;
import onlinegame.shared.SharedUtil;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

/**
 *
 * @author Alfred
 */
public final class Texture
{
    private static final TMap<TextureBuilder, TextureObj> texMap = new THashMap<>();
    
    private TextureBuilder builder;
    private TextureObj texObj;
    
    private boolean isCreated = false;
    
    Texture(TextureBuilder builder)
    {
        this.builder = builder;
    }
    
    public void create()
    {
        if (isCreated)
        {
            return;
        }
        
        TextureObj tex = texMap.get(builder);
        
        if (tex == null)
        {
            tex = new TextureObj(builder);
            texMap.put(builder, tex);
        }
        
        tex.addRef();
        texObj = tex;
        
        //save memory by reusing the instance of TextureBuilder
        builder = texObj.builder;
        
        isCreated = true;
    }
    
    public void destroy()
    {
        if (!isCreated)
        {
            return;
        }
        
        texObj.removeRef();
        texObj = null;
        
        isCreated = false;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            if (isCreated)
            {
                Logger.logError("Texture " + texObj.builder.filename() + " not properly destroyed!");
                destroy();
            }
        }
        finally
        {
            super.finalize();
        }
    }
    
    public TextureBuilder getBuilder()
    {
        return builder;
    }
    
    private void check()
    {
        if (!isCreated)
        {
            throw new IllegalStateException("Texture not yet created.");
        }
    }
    
    public int getWidth()
    {
        check();
        return texObj.width;
    }
    
    public int getHeight()
    {
        check();
        return texObj.height;
    }
    
    public void bind()
    {
        check();
        glBindTexture(builder.textureType(), texObj.texture);
    }
    
    public void unbind()
    {
        check();
        glBindTexture(builder.textureType(), 0);
    }
    
    private static final class TextureObj
    {
        private final TextureBuilder builder;
        private volatile int referenceCount = 0;
        private boolean isDestroyed = false;
        
        private final int texture;
        private int width, height; //final
        
        private TextureObj(TextureBuilder builder)
        {
            this.builder = builder;
            
            if (builder.textureType() != GL_TEXTURE_2D)
            {
                throw new IllegalArgumentException("Only GL_TEXTURE_2D is currently supported.");
            }
            
            ByteBuffer buf = builder.data();
            if (buf == null)
            {
                try
                {
                    buf = load(builder.filename());
                }
                catch (IOException e)
                {
                    Logger.logError("Error loading texture " + builder.filename() + ": " + SharedUtil.getErrorMsg(e));

                    try
                    {
                        buf = load("textures/error.png");
                    }
                    catch (IOException e2)
                    {
                        throw new RuntimeException("Error loading error texture. (" + e2.getMessage() + ")", e2);
                    }
                }
            }
            else
            {
                width = builder.width();
                height = builder.height();
            }
            
            if (builder.padding() != 0)
            {
                if (builder.padding() < 0) throw new IllegalArgumentException("Padding cannot be less than 0!");
                
                int newWidth = width + builder.padding()*2;
                int newHeight = height + builder.padding()*2;
                ByteBuffer newBuf = BufferUtils.createByteBuffer(newWidth * newHeight * 4);
                
                int startX = builder.padding();
                int startY = builder.padding();
                int endX = newWidth - builder.padding();
                int endY = newHeight - builder.padding();
                
                for (int y = startY; y < endY; y++)
                {
                    for (int x = startX; x < endX; x++)
                    {
                        int off = 4 * (y * newWidth + x);
                        newBuf.put(off, buf.get());
                        newBuf.put(off+1, buf.get());
                        newBuf.put(off+2, buf.get());
                        newBuf.put(off+3, buf.get());
                    }
                }
                buf = newBuf;
                width = newWidth;
                height = newHeight;
            }
            
            texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            
            glTexImage2D(GL_TEXTURE_2D, 0, builder.internalFormat(), width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, builder.wrapS());
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, builder.wrapT());
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, builder.magFilter());
            
            int filter = Settings.getCurrent().geti(Settings.TEXTURE_FILTER);
            int mipmap, anisotropy;
            
            if (filter == 0)
            {
                mipmap = 0;
                anisotropy = 0;
            }
            else if (filter < 0)
            {
                //mipmap = Math.min(-filter, builder.mipmap());
                mipmap = Math.min(MathUtil.clog2(Math.max(width, height)), builder.mipmap());
                anisotropy = 0;
            }
            else
            {
                mipmap = Math.min(MathUtil.clog2(Math.max(width, height)), builder.mipmap());
                anisotropy = Math.min(filter, builder.anisotropy());
            }
            
            //Logger.log("Mipmap level: " + mipmap + ", Anisotropy: " + anisotropy);
            
            if (mipmap == 0)
            {
                //no mipmapping
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, builder.minFilter());
            }
            else
            {
                //mipmapping
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, builder.minFilter());
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipmap);
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, builder.lodBias());
                glGenerateMipmap(GL_TEXTURE_2D);
            }
            
            if (anisotropy != 0)
            {
                //anisotropic filtering
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisotropy);
            }
            
            glBindTexture(GL_TEXTURE_2D, 0);
            
            GLUtil.checkErrors();
        }
        
        private ByteBuffer load(String filename) throws IOException
        {
            ByteBuffer buf;
            try (InputStream in = FileLoader.getJarResource(filename))
            {
                PNGDecoder decoder = new PNGDecoder(in);
                
                width = decoder.getWidth();
                height = decoder.getHeight();
                int stride = width * 4;
                int size = stride * height;
                
                buf = BufferUtils.createByteBuffer(size);
                decoder.decode(buf, stride, PNGDecoder.Format.RGBA);
            }
            
            buf.flip();
            return buf;
        }
        
        private void check()
        {
            if (isDestroyed)
            {
                throw new IllegalStateException("TextureObj is destroyed!");
            }
        }
        
        private void destroy()
        {
            isDestroyed = true;
            
            glDeleteTextures(texture);
            GLUtil.checkErrors();
        }
        
        private void addRef()
        {
            check();
            referenceCount++;
        }
        
        private void removeRef()
        {
            check();
            referenceCount--;
            if (referenceCount == 0)
            {
                destroy();
                texMap.remove(builder);
            }
            else if (referenceCount < 0)
            {
                throw new RuntimeException("referenceCount < 0");
            }
        }
    }
}
