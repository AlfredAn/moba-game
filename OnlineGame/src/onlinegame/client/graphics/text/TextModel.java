package onlinegame.client.graphics.text;

import onlinegame.client.graphics.Align;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.WeakHashMap;
import onlinegame.shared.Logger;
import onlinegame.client.graphics.GLUtil;
import onlinegame.client.graphics.text.BitmapFont.BitmapChar;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

//all fonts must have at least 1 pixel padding on every side as well as 4 pixels spacing in both directions

/**
 * 
 * @author Alfred
 */
public final class TextModel
{
    private static final float[] quadPos = new float[]
    {
        0, 0,
        0, 1,
        1, 1,
        1, 0
    };
    
    private static final int[] quadTex = new int[]
    {
        0, 0,
        0, 1,
        1, 1,
        1, 0
    };
    
    private static final int[] quadId = new int[]
    {
        1, 0, 2,
        0, 2, 3
    };
    
    private static final int
            posOff = 0,
            posSize = 8,
            
            texOff = 8,
            texSize = 4,
            
            stride = 12,
            
            charVertexBytes = stride * 4,
            charIndices = 6;
    
    private static int vboi = 0, vboiChars = 0;
    
    public final BitmapFont font;
    
    private int numChars = 0, capacity, renderedCapacity;
    private ByteBuffer vertexBuffer;
    
    private boolean isRendered = false;
    private int vao, vbo, boundVboi;
    
    private boolean
            yInvert = false,
            gridAlign = false;
    
    private int align = Align.TOPLEFT;
    
    private float
            cachedWidth = -1,
            cachedHeight = -1;
    
    private String cachedString = null;
    
    public TextModel(BitmapFont font)
    {
        this(font, 64);
    }
    public TextModel(BitmapFont font, int capacity)
    {
        this.font = font;
        this.capacity = capacity;
        
        vertexBuffer = BufferUtils.createByteBuffer(capacity * charVertexBytes);
    }
    
    public static void init()
    {
        
    }
    
    public static void destroyStatic()
    {
        if (vboi != 0)
        {
            glDeleteBuffers(vboi);
        }
        
        vboi = 0;
        vboiChars = 0;
    }
    
    private static void ensureIndexCapacity(int chars)
    {
        if (vboiChars >= chars)
        {
            //old buffer is already large enough
            return;
        }
        
        //if old buffer exists, delete it
        if (vboi != 0)
        {
            glDeleteBuffers(vboi);
        }
        
        vboiChars = (int)Math.max(Math.max(chars * 1.25, vboiChars * 1.5) + 1, 64);
        
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(vboiChars * quadId.length);
        
        int ioff = 0;
        for (int i = 0; i < vboiChars; i++)
        {
            for (int j = 0; j < quadId.length; j++)
            {
                indexBuffer.put(ioff + quadId[j]);
            }
            ioff += 4;
        }
        
        indexBuffer.flip();
        
        glBindVertexArray(0);
        
        vboi = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboi);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        
        GLUtil.checkErrors();
    }
    
    private void ensureVertexCapacity(int chars)
    {
        if (vertexBuffer.capacity() / charVertexBytes >= numChars + chars)
        {
            return;
        }
        
        capacity = vertexBuffer.capacity() / charVertexBytes;
        capacity = (int)Math.max((numChars + chars) * 1.25, capacity * 1.5) + 1;
        
        ByteBuffer newVertexBuffer = BufferUtils.createByteBuffer(capacity * charVertexBytes);
        vertexBuffer.flip();
        newVertexBuffer.put(vertexBuffer);
        vertexBuffer = newVertexBuffer;
    }
    
    public void addString(String s, float x, float y)
    {
        addCharsPrivate(s.toCharArray(), x, y);
        cachedString = s;
    }
    
    private void addCharsPrivate(char[] c, float x, float y)
    {
        if (c.length == 0)
        {
            return;
        }
        
        BitmapChar prev = null;
        
        BitmapChar errorChar = font.getChar('\ufffd');
        
        int startChar = numChars;
        float startX = x, startY = y;
        float maxX = x;
        
        for (int i = 0; i < c.length; i++)
        {
            char cc = c[i];
            
            if (cc == '\n')
            {
                if (x > maxX)
                {
                    maxX = x;
                }
                x = startX;
                y -= font.getLineHeight();
                continue;
            }
            
            BitmapChar bc = font.getChar(cc);
            
            if (bc == null)
            {
                continue;
            }
            
            if (prev == null)
            {
                //x -= bc.xOffset;
            }
            else
            {
                x += font.getKerningAmount(prev, bc);
            }
            
            if (bc.width > 1 && bc.height > 1)
            {
                addChar(bc, x, y);
            }
            
            if (i < c.length - 1)
            {
                x += bc.xAdvance;
            }
            else
            {
                x += bc.xOffset + bc.width;
            }
            
            prev = bc;
        }
        
        cachedWidth = Math.abs(Math.max(x, maxX) - startX);
        cachedHeight = Math.abs(y - startY) + font.getNormalHeight();
        
        if (numChars == 0)
        {
            return;
        }
        
        float xAlign = Align.getXAlign(align);
        float yAlign = Align.getYAlign(align);
        
        translateChars(startChar, numChars - 1, -cachedWidth * xAlign, -font.getNormalHeight() * yAlign);
        
        if (yInvert)
        {
            flipChars(startChar, numChars - 1, startY * 2);
        }
        
        if (gridAlign)
        {
            gridAlignChars(startChar, numChars - 1);
        }
        
        //Logger.log("w=" + cachedWidth + ", h=" + cachedHeight);
        //Logger.log("xAlign=" + xAlign + ", yAlign=" + yAlign);
        //Logger.log("startX=" + startX + ", startY=" + startY);
        //Logger.log("numChars=" + numChars);
    }
    
    private void testString(String s)
    {
        testChars(s.toCharArray());
    }
    
    private void testChars(char[] c)
    {
        BitmapChar prev = null;
        
        float startX = 0, startY = 0;
        float x = 0, y = 0, maxX = 0;
        
        for (int i = 0; i < c.length; i++)
        {
            char cc = c[i];
            
            if (cc == '\n')
            {
                if (x > maxX)
                {
                    maxX = x;
                }
                x = startX;
                y -= font.getLineHeight();
                continue;
            }
            
            BitmapChar bc = font.getChar(cc);
            
            if (bc == null)
            {
                continue;
            }
            
            if (prev == null)
            {
                //x -= bc.xOffset;
            }
            else
            {
                x += font.getKerningAmount(prev, bc);
            }
            
            if (i < c.length - 1)
            {
                x += bc.xAdvance;
            }
            else
            {
                x += bc.xOffset + bc.width;
            }
            
            prev = bc;
        }
        
        cachedWidth = Math.abs(Math.max(x, maxX) - startX);
        cachedHeight = Math.abs(y - startY) + font.getNormalHeight();
    }
    
    public static String addLineBreaks(BitmapFont font, String s, float maxWidth)
    {
        StringBuilder result = new StringBuilder((int)(s.length() * 1.2) + 1);
        StringBuilder word = new StringBuilder(Math.min(32, s.length() + 2));
        
        BitmapChar prev = null;
        
        float x = 0, preWord = 0, wordLength = 0, additionalWidth = 0;
        boolean afterNewline = false;
        
        for (int i = 0; i < s.length(); i++)
        {
            char cc = s.charAt(i);
            
            if (cc == '\n')
            {
                x = 0;
                preWord = 0;
                wordLength = 0;
                
                result.append(word).append('\n');
                word.setLength(0);
                
                afterNewline = false;
                
                prev = null;
                continue;
            }
            else if (cc == ' ')
            {
                //end the word and write it to the result.
                preWord = x;
                wordLength = 0;
                
                result.append(word);
                word.setLength(0);
                
                afterNewline = false;
            }
            else if (word.length() == 1 && word.charAt(0) == ' ')
            {
                //end the word and write it to the result.
                preWord = x;
                wordLength = 0;
                
                //skip a space directly after a newline that was added by this method
                if (!afterNewline)
                {
                    result.append(word);
                }
                word.setLength(0);
                
                afterNewline = false;
            }
            else
            {
                afterNewline = false;
            }
            
            BitmapChar bc = font.getChar(cc);
            
            if (bc == null)
            {
                continue;
            }
            
            if (prev == null)
            {
                //x -= bc.xOffset;
                //wordLength -= bc.xOffset;
            }
            else
            {
                float kern = font.getKerningAmount(prev, bc);
                x += kern;
                wordLength += kern;
            }
            
            word.append(cc);
            
            additionalWidth = bc.xOffset + bc.width - bc.xAdvance;
            
            if (i < s.length() - 1)
            {
                x += bc.xAdvance;
                wordLength += bc.xAdvance;
            }
            
            prev = bc;
            
            if (preWord > 0 && x + additionalWidth > maxWidth)
            {
                //the word can't fit on the line; add a newline but don't break the word up.
                result.append('\n');
                preWord = 0;
                x = wordLength;
                afterNewline = true;
                prev = null;
            }
            
            if (preWord == 0 && x + additionalWidth > maxWidth)
            {
                //the word is longer that maxWidth and must be broken up.
                word.setLength(word.length() - 1);
                result.append(word).append('\n');
                word.setLength(0);
                word.append(cc);
                x = bc.xAdvance;
                preWord = 0;
                wordLength = bc.xAdvance;
                prev = null;
            }
        }
        
        result.append(word);
        if (result.charAt(result.length() - 1) == '\n')
        {
            result.setLength(result.length() - 1);
        }
        
        if (s.contentEquals(result))
        {
            //result is not changed, return original string to save space
            return s;
        }
        return result.toString();
    }
    
    public void clear()
    {
        vertexBuffer.clear();
        numChars = 0;
    }
    
    public float getStringWidth()
    {
        if (cachedString == null)
        {
            throw new IllegalStateException();
        }
        return cachedWidth;
    }
    
    public float getStringHeight()
    {
        if (cachedString == null)
        {
            throw new IllegalStateException();
        }
        return cachedHeight;
    }
    
    public float getStringWidth(String s)
    {
        if (!s.equals(cachedString))
        {
            testString(s);
            cachedString = s;
        }
        return cachedWidth;
    }
    
    public float getStringHeight(String s)
    {
        if (!s.equals(cachedString))
        {
            testString(s);
            cachedString = s;
        }
        return cachedHeight;
    }
    
    //this is crap and should be replaced by a better system that doesn't use
    //BitmapTextModel objects
    private static final Map<BitmapFont, SoftReference<TextModel>>
            sizeCheckMap = new WeakHashMap<>();
    
    private static TextModel getCachedModel(BitmapFont font)
    {
        SoftReference<TextModel> ref = sizeCheckMap.get(font);
        TextModel tm = ref == null ? null : ref.get();
        
        if (tm == null)
        {
            tm = new TextModel(font);
            sizeCheckMap.put(font, new SoftReference<>(tm));
        }
        
        return tm;
    }
    
    public static float getStringWidth(BitmapFont font, String s)
    {
        return getCachedModel(font).getStringWidth(s);
    }
    
    public static float getStringHeight(BitmapFont font, String s)
    {
        return getCachedModel(font).getStringHeight(s);
    }
    
    private void addChar(BitmapChar bc, float x, float y)
    {
        ensureVertexCapacity(1);
        
        float xPos = x + bc.xOffset;
        float yPos = y - bc.yOffset;
        
        int tx = bc.x;
        int ty = bc.y;
        int width = bc.width;
        int height = bc.height;
        
        float base = font.getBase();
        
        for (int i = 0; i < 4; i++)
        {
            vertexBuffer.putFloat(xPos + width * quadPos[i*2]);
            vertexBuffer.putFloat(yPos - height * quadPos[i*2+1] + base);
            vertexBuffer.putShort((short)(tx + width * quadTex[i*2]));
            vertexBuffer.putShort((short)(ty + height * quadTex[i*2+1]));
        }
        
        numChars++;
    }
    
    private void gridAlignChar(int i)
    {
        if (i < 0 || i >= numChars)
        {
            throw new IndexOutOfBoundsException();
        }
        
        int pos = i * charVertexBytes;
        
        float x = vertexBuffer.getFloat(pos);
        float rx = Math.round(x);
        
        if (x != rx)
        {
            float d1 = vertexBuffer.getFloat(pos + stride  ) - rx;
            float d2 = vertexBuffer.getFloat(pos + stride*2) - rx;
            float d3 = vertexBuffer.getFloat(pos + stride*3) - rx;

            //set new x pos
            vertexBuffer.putFloat(pos,            rx     );
            vertexBuffer.putFloat(pos + stride,   rx + d1);
            vertexBuffer.putFloat(pos + stride*2, rx + d2);
            vertexBuffer.putFloat(pos + stride*3, rx + d3);
        }
        
        float y = vertexBuffer.getFloat(pos + 4);
        float ry = Math.round(y);
        
        if (y != ry)
        {
            float d1 = vertexBuffer.getFloat(pos + stride   + 4) - ry;
            float d2 = vertexBuffer.getFloat(pos + stride*2 + 4) - ry;
            float d3 = vertexBuffer.getFloat(pos + stride*3 + 4) - ry;

            //set new x pos
            vertexBuffer.putFloat(pos            + 4, ry     );
            vertexBuffer.putFloat(pos + stride   + 4, ry + d1);
            vertexBuffer.putFloat(pos + stride*2 + 4, ry + d2);
            vertexBuffer.putFloat(pos + stride*3 + 4, ry + d3);
        }
    }
    
    private void mirrorChar(int i, float offset)
    {
        if (i < 0 || i >= numChars)
        {
            throw new IndexOutOfBoundsException();
        }
        
        int pos = i * charVertexBytes;
        
        //set new x pos
        vertexBuffer.putFloat(pos,            offset - vertexBuffer.getFloat(pos           ));
        vertexBuffer.putFloat(pos + stride,   offset - vertexBuffer.getFloat(pos + stride  ));
        vertexBuffer.putFloat(pos + stride*2, offset - vertexBuffer.getFloat(pos + stride*2));
        vertexBuffer.putFloat(pos + stride*3, offset - vertexBuffer.getFloat(pos + stride*3));
    }
    
    private void flipChar(int i, float offset)
    {
        if (i < 0 || i >= numChars)
        {
            throw new IndexOutOfBoundsException();
        }
        
        int pos = i * charVertexBytes;
        
        //set new y pos
        vertexBuffer.putFloat(pos            + 4, offset - vertexBuffer.getFloat(pos            + 4));
        vertexBuffer.putFloat(pos + stride   + 4, offset - vertexBuffer.getFloat(pos + stride   + 4));
        vertexBuffer.putFloat(pos + stride*2 + 4, offset - vertexBuffer.getFloat(pos + stride*2 + 4));
        vertexBuffer.putFloat(pos + stride*3 + 4, offset - vertexBuffer.getFloat(pos + stride*3 + 4));
    }
    
    private void translateChar(int i, float dx, float dy)
    {
        if (i < 0 || i >= numChars)
        {
            throw new IndexOutOfBoundsException();
        }
        
        int pos = i * charVertexBytes;
        
        if (dx != 0)
        {
            //set new x pos
            vertexBuffer.putFloat(pos,            vertexBuffer.getFloat(pos           ) + dx);
            vertexBuffer.putFloat(pos + stride,   vertexBuffer.getFloat(pos + stride  ) + dx);
            vertexBuffer.putFloat(pos + stride*2, vertexBuffer.getFloat(pos + stride*2) + dx);
            vertexBuffer.putFloat(pos + stride*3, vertexBuffer.getFloat(pos + stride*3) + dx);
        }
        
        if (dy != 0)
        {
            //set new y pos
            vertexBuffer.putFloat(pos            + 4, vertexBuffer.getFloat(pos            + 4) + dy);
            vertexBuffer.putFloat(pos + stride   + 4, vertexBuffer.getFloat(pos + stride   + 4) + dy);
            vertexBuffer.putFloat(pos + stride*2 + 4, vertexBuffer.getFloat(pos + stride*2 + 4) + dy);
            vertexBuffer.putFloat(pos + stride*3 + 4, vertexBuffer.getFloat(pos + stride*3 + 4) + dy);
        }
    }
    
    private void translateChars(int startChar, int endChar, float dx, float dy)
    {
        if (startChar > endChar)
        {
            throw new IllegalArgumentException("startChar cannot be greater than endChar.");
        }
        
        if (dx == 0 && dy == 0)
        {
            return;
        }
        
        for (int i = startChar; i <= endChar; i++)
        {
            translateChar(i, dx, dy);
        }
    }
    
    private void mirrorChars(int startChar, int endChar, float offset)
    {
        if (startChar > endChar)
        {
            throw new IllegalArgumentException("startChar cannot be greater than endChar.");
        }
        
        for (int i = startChar; i <= endChar; i++)
        {
            mirrorChar(i, offset);
        }
    }
    
    private void flipChars(int startChar, int endChar, float offset)
    {
        if (startChar > endChar)
        {
            throw new IllegalArgumentException("startChar cannot be greater than endChar.");
        }
        
        for (int i = startChar; i <= endChar; i++)
        {
            flipChar(i, offset);
        }
    }
    
    private void gridAlignChars(int startChar, int endChar)
    {
        if (startChar > endChar)
        {
            throw new IllegalArgumentException("startChar cannot be greater than endChar.");
        }
        
        for (int i = startChar; i <= endChar; i++)
        {
            gridAlignChar(i);
        }
    }
    
    private void subData()
    {
        int pos = vertexBuffer.position();
        vertexBuffer.clear();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.capacity(), vertexBuffer);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        vertexBuffer.position(pos);
    }
    
    public void render()
    {
        render(GL_STATIC_DRAW);
    }
    public void render(int bufferUsage)
    {
        if (vertexBuffer.capacity() == renderedCapacity && isRendered)
        {
            subData();
            return;
        }
        
        if (isRendered)
        {
            destroy();
        }
        
        if (numChars == 0)
        {
            isRendered = true;
            renderedCapacity = 0;
            return;
        }
        
        int pos = vertexBuffer.position();
        vertexBuffer.clear();
        
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, bufferUsage);
        
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, posOff);
        glVertexAttribPointer(1, 2, GL_UNSIGNED_SHORT, false, stride, texOff);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        glBindVertexArray(0);
        
        GLUtil.checkErrors();
        
        isRendered = true;
        boundVboi = 0;
        
        renderedCapacity = vertexBuffer.capacity();
        vertexBuffer.position(pos);
    }
    
    public void destroy()
    {
        if (!isRendered)
        {
            return;
        }
        
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        
        GLUtil.checkErrors();
        
        isRendered = false;
    }
    
    public void draw()
    {
        if (!isRendered)
        {
            throw new IllegalStateException("Model is not rendered.");
        }
        
        if (renderedCapacity == 0)
        {
            return;
        }
        
        ensureIndexCapacity(vertexBuffer.capacity() / charVertexBytes);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, font.getPage(0).texture);
        
        glBindVertexArray(vao);
        
        if (boundVboi != vboi || boundVboi == 0)
        {
            boundVboi = vboi;
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboi);
        }
        
        glDrawElements(GL_TRIANGLES, numChars * quadId.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        
        GLUtil.checkErrors();
    }
    
    public void setYInvert(boolean yInvert)
    {
        this.yInvert = yInvert;
    }
    
    public boolean isYInverted()
    {
        return yInvert;
    }
    
    public void setTextAlign(int textAlign)
    {
        align = textAlign;
    }
    
    public int getTextAlign()
    {
        return align;
    }
    
    public void setGridAlign(boolean gridAlign)
    {
        this.gridAlign = gridAlign;
    }
    
    public boolean getGridAlign()
    {
        return gridAlign;
    }
    
    private String getBufferContents()
    {
        StringBuilder sb = new StringBuilder(1024);
        ByteBuffer buf = vertexBuffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
        buf.flip();
        
        sb.append("vertexBuffer:");
        
        int i = 0;
        while (buf.hasRemaining())
        {
            sb.append("\n\nchar ").append(i);
            sb.append(":\nx=").append(buf.getFloat());
            sb.append("\ny=").append(buf.getFloat());
            sb.append("\ntx=").append(buf.getShort());
            sb.append("\nty=").append(buf.getShort());
            
            i++;
            buf.position(i * charVertexBytes);
        }
        
        return sb.toString();
    }
    
    @Override
    public void finalize() throws Throwable
    {
        if (isRendered)
        {
            Logger.logError("BitmapTextModel was not properly destroyed.\nnumChars: " + numChars);
            destroy();
        }
        
        super.finalize();
    }
}


















