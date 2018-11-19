package onlinegame.client.graphics.text;

import de.matthiasmann.twl.utils.PNGDecoder;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import onlinegame.client.Settings;
import onlinegame.client.graphics.GLUtil;
import onlinegame.client.io.FileLoader;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

//all fonts must have at least 1 pixel padding on every side as well as 4 pixels spacing in both directions

public final class BitmapFont
{
    private static final byte
            BIT_0 = (byte)0x80,
            BIT_1 = 0x40,
            BIT_2 = 0x20,
            BIT_3 = 0x10,
            BIT_4 = 0x08,
            BIT_5 = 0x04,
            BIT_6 = 0x02,
            BIT_7 = 0x01;
    
    private final String filename, foldername;
    
    //BLOCK 1: INFO
    private boolean readInfoBlock = false;
    private int fontSize;
    private boolean
            smooth,
            unicode,
            italic,
            bold,
            fixedHeight;
    private int charSet,
            stretchH,
            aa,
            paddingUp,
            paddingRight,
            paddingDown,
            paddingLeft,
            spacingHoriz,
            spacingVert,
            outline;
    private String fontName;
    
    //BLOCK 2: COMMON
    private boolean readCommonBlock = false;
    private int
            lineHeight,
            base,
            scaleW,
            scaleH,
            pages;
    private boolean packed;
    private int
            alphaChnl,
            redChnl,
            greenChnl,
            blueChnl;
    
    //BLOCK 3: PAGES
    private boolean readPagesBlock = false;
    private String[] pageName;
    
    //BLOCK 4: CHARS
    private boolean readCharsBlock = false;
    private BitmapChar[] chars;
    
    //BLOCK 5: KERNING PAIRS
    private boolean readKerningBlock = false;
    private BitmapKerningPair[] kerningPairs;
    
    //Character/kerning maps
    private TMap<CharId, BitmapChar> charMap;
    private TMap<CharPair, BitmapKerningPair> kerningMap;
    
    //Font pages
    private BitmapFontPage[] fontPages;
    
    //Custom attributes
    private int normalHeight;
    private boolean useFilter, useMipmap;
    
    public BitmapFont(String name) throws IOException
    {
        this(name, false, false);
    }
    public BitmapFont(String name, boolean filter, boolean mipmap) throws IOException
    {
        filename = name;
        foldername = new File(filename).getParent();
        useFilter = filter;
        useMipmap = mipmap;
        
        try
            (
                DataInputStream in = new DataInputStream(FileLoader.getJarResource(name));
            )
        {
            load(in);
        }
        
        finish();
    }
    
    private void load(DataInputStream in) throws IOException
    {
        readIdentifier(in);
        readBlock(in); //INFO
        readBlock(in); //COMMON
        readBlock(in); //PAGES
        readBlock(in); //CHARS
        
        try
        {
            readBlock(in); //KERNING PAIRS
        }
        catch (EOFException e)
        {
            if (readKerningBlock)
            {
                throw e;
            }
            else
            {
                readKerningBlock = true;
                kerningPairs = new BitmapKerningPair[0];
            }
        }
    }
    
    private void readIdentifier(DataInputStream in) throws IOException
    {
        int m1 = in.readUnsignedByte();
        int m2 = in.readUnsignedByte();
        int m3 = in.readUnsignedByte();
        
        if (m1 != 66 || m2 != 77 || m3 != 70)
        {
            throw new IOException("Invalid file identifier: "
                + m1 + ", " + m2 + ", " + m3);
        }
        
        int version = in.readUnsignedByte();
        
        if (version != 3)
        {
            throw new IOException("Unsupported format version: " + version);
        }
    }
    
    private void readBlock(DataInputStream in) throws IOException
    {
        int blockId = in.readUnsignedByte();
        long size = readUnsignedInt(in);
        
        switch (blockId)
        {
            case 1:
                if (readInfoBlock)
                {
                    throw new IOException("Duplicate info block.");
                }
                readInfoBlock(in, (int)size);
                readInfoBlock = true;
                break;
            case 2:
                if (readCommonBlock)
                {
                    throw new IOException("Duplicate common block.");
                }
                readCommonBlock(in, (int)size);
                readCommonBlock = true;
                break;
            case 3:
                if (readPagesBlock)
                {
                    throw new IOException("Duplicate pages block.");
                }
                if (!readCommonBlock)
                {
                    throw new IOException("Pages block appeared before common.");
                }
                readPagesBlock(in, (int)size);
                readPagesBlock = true;
                break;
            case 4:
                if (readCharsBlock)
                {
                    throw new IOException("Duplicate chars block.");
                }
                if (!readCommonBlock)
                {
                    throw new IOException("Chars block appeared before common.");
                }
                readCharsBlock(in, (int)size);
                readCharsBlock = true;
                break;
            case 5:
                if (readKerningBlock)
                {
                    throw new IOException("Duplicate kerning pairs block.");
                }
                readKerningBlock(in, (int)size);
                readKerningBlock = true;
                break;
            default:
                throw new IOException("Invalid block id: " + blockId);
        }
    }
    
    private void readInfoBlock(DataInputStream in, int size) throws IOException
    {
        if (size < 15)
        {
            throw new IOException("Invalid size of info block: " + size);
        }
        
        fontSize = readShort(in);
        
        byte bits = in.readByte();
        smooth = (bits & BIT_0) == BIT_0;
        unicode = (bits & BIT_1) == BIT_1;
        italic = (bits & BIT_2) == BIT_2;
        bold = (bits & BIT_3) == BIT_3;
        fixedHeight = (bits & BIT_4) == BIT_4;
        
        charSet = in.readUnsignedByte();
        stretchH = readUnsignedShort(in);
        aa = in.readUnsignedByte();
        paddingUp = in.readUnsignedByte();
        paddingRight = in.readUnsignedByte();
        paddingDown = in.readUnsignedByte();
        paddingLeft = in.readUnsignedByte();
        spacingHoriz = in.readUnsignedByte();
        spacingVert = in.readUnsignedByte();
        outline = in.readUnsignedByte();
        fontName = readCString(in);
    }
    
    private void readCommonBlock(DataInputStream in, int size) throws IOException
    {
        if (size != 15)
        {
            throw new IOException("Invalid size of common block: " + size);
        }
        
        lineHeight = readUnsignedShort(in);
        base = readUnsignedShort(in);
        normalHeight = base;
        scaleW = readUnsignedShort(in);
        scaleH = readUnsignedShort(in);
        pages = readUnsignedShort(in);
        
        if (pages != 1)
        {
            throw new IOException("Unsupported page amount: " + pages);
        }
        
        byte bits = in.readByte();
        packed = (bits & BIT_7) == BIT_7;
        
        alphaChnl = in.readUnsignedByte();
        redChnl = in.readUnsignedByte();
        greenChnl = in.readUnsignedByte();
        blueChnl = in.readUnsignedByte();
    }
    
    private void readPagesBlock(DataInputStream in, int size) throws IOException
    {
        if (size < pages * 2)
        {
            throw new IOException("Invalid size of pages block: " + size);
        }
        
        pageName = new String[pages];
        
        for (int i = 0; i < pages; i++)
        {
            pageName[i] = readCString(in);
        }
    }
    
    private void readCharsBlock(DataInputStream in, int size) throws IOException
    {
        if (size % 20 != 0 || size < 0)
        {
            throw new IOException("Invalid size of chars block: " + size);
        }
        
        int charAmount = size / 20;
        chars = new BitmapChar[charAmount];
        
        for (int i = 0; i < chars.length; i++)
        {
            chars[i] = new BitmapChar(in);
        }
    }
    
    private void readKerningBlock(DataInputStream in, int size) throws IOException
    {
        if (size % 10 != 0 || size < 0)
        {
            throw new IOException("Invalid size of kerning block: " + size);
        }
        
        int kerningPairAmount = size / 10;
        kerningPairs = new BitmapKerningPair[kerningPairAmount];
        
        for (int i = 0; i < kerningPairs.length; i++)
        {
            kerningPairs[i] = new BitmapKerningPair(in);
        }
    }
    
    private void finish() throws IOException
    {
        if (!readInfoBlock || !readCommonBlock || !readPagesBlock || !readCharsBlock || !readKerningBlock)
        {
            throw new IOException("Font file is missing blocks.");
        }
        
        createMaps();
        loadPages();
        
        chars = null;
        kerningPairs = null;
        
        errorChar = getChar('\ufffd');
    }
    
    private void createMaps()
    {
        charMap = new THashMap<>((int)((double)chars.length / .75) + 1, .75f);
        
        for (int i = 0; i < chars.length; i++)
        {
            BitmapChar c = chars[i];
            if (c == null)
            {
                throw new RuntimeException("korv");
            }
            CharId key = new CharId(c.id);
            if (charMap.containsKey(key))
            {
                continue;
            }
            charMap.put(key, c);
        }
        
        kerningMap = new THashMap<>((int)((double)kerningPairs.length / .75) + 1, .75f);
        
        for (int i = 0; i < kerningPairs.length; i++)
        {
            BitmapKerningPair k = kerningPairs[i];
            if (k == null)
            {
                throw new RuntimeException("korv");
            }
            kerningMap.put(new CharPair(k.first, k.second), k);
        }
    }
    
    private void loadPages() throws IOException
    {
        fontPages = new BitmapFontPage[pages];
        ByteBuffer pageBuffer = BufferUtils.createByteBuffer(scaleW * scaleH * 4);
        
        for (int i = 0; i < pages; i++)
        {
            fontPages[i] = new BitmapFontPage(foldername + "/" + pageName[i], pageBuffer);
        }
        
        pageName = null;
    }
    
    private final CharId tempId = new CharId((char)0);
    private final CharPair tempPair = new CharPair((char)0, (char)0);
    
    private BitmapChar errorChar;
    
    public BitmapChar getChar(char c)
    {
        tempId.set(c);
        BitmapChar bc = charMap.get(tempId);
        
        if (bc == null)
        {
            return errorChar;
        }
        
        return bc;
    }
    
    public short getKerningAmount(BitmapChar first, BitmapChar second)
    {
        if (first == null || second == null)
        {
            return 0;
        }
        
        BitmapKerningPair bkp = getKerningPair(first, second);
        
        if (bkp == null)
        {
            return 0;
        }
        
        return bkp.amount;
    }
    
    public short getKerningAmount(char first, char second)
    {
        BitmapKerningPair bkp = getKerningPair(first, second);
        
        if (bkp == null)
        {
            return 0;
        }
        
        return bkp.amount;
    }
    
    public BitmapKerningPair getKerningPair(BitmapChar first, BitmapChar second)
    {
        if (first == null || second == null)
        {
            return null;
        }
        
        return getKerningPair(first.id, second.id);
    }
    
    public BitmapKerningPair getKerningPair(char first, char second)
    {
        tempPair.set(first, second);
        return kerningMap.get(tempPair);
    }
    
    public BitmapFontPage getPage(int page)
    {
        return fontPages[page];
    }
    
    public class BitmapChar
    {
        public final char id;
        public final int x, y, width, height;
        public final short xOffset, yOffset, xAdvance, page, chnl;
        
        private BitmapChar(DataInputStream in) throws IOException
        {
            id = (char)readInt(in);
            x = readUnsignedShort(in);
            y = readUnsignedShort(in);
            width = readUnsignedShort(in);
            height = readUnsignedShort(in);
            xOffset = readShort(in);
            yOffset = readShort(in);
            xAdvance = readShort(in);
            page = (short)in.readUnsignedByte();
            chnl = (short)in.readUnsignedByte();
        }
        
        @Override
        public String toString()
        {
            return  "c: " + id +
                    ", id: " + (int)id +
                    ", w: " + width +
                    ", h: " + height +
                    ", x: " + x +
                    ", y: " + y +
                    ", xOff: " + xOffset +
                    ", yOff: " + yOffset +
                    ", xAdvance: " + xAdvance;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof BitmapChar))
            {
                return false;
            }
            
            return ((BitmapChar)o).id == id;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 47 * hash + this.id;
            return hash;
        }
    }
    
    public class BitmapKerningPair
    {
        public final char first, second;
        public final short amount;
        
        private BitmapKerningPair(DataInputStream in) throws IOException
        {
            first = (char)readInt(in);
            second = (char)readInt(in);
            amount = readShort(in);
        }
        
        @Override
        public String toString()
        {
            return "[BitmapKerningPair: '" + first + "', '" + second + "', " + amount + "]";
        }
    }
    
    public class BitmapFontPage
    {
        public final String name;
        public final int texture;
        
        private BitmapFontPage(String name, ByteBuffer pageBuffer) throws IOException
        {
            this.name = name;
            
            pageBuffer.clear();
            
            try
                (
                    InputStream in = FileLoader.getJarResource(name);
                )
            {
                PNGDecoder decoder = new PNGDecoder(in);
                
                int w = decoder.getWidth();
                int h = decoder.getHeight();
                
                if (w != scaleW || h != scaleH)
                {
                    throw new IOException("Image sizes don't match: " + name);
                }
                
                decoder.decode(pageBuffer, w, PNGDecoder.Format.LUMINANCE);
                pageBuffer.flip();
            }
            
            texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, scaleW, scaleH, 0, GL_RED, GL_UNSIGNED_BYTE, pageBuffer);
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, useFilter ? GL_LINEAR : GL_NEAREST);
            
            int filter = (useFilter && useMipmap) ? Settings.getCurrent().geti(Settings.TEXTURE_FILTER) : 0;
            
            if (filter == 0)
            {
                //no filter
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, useFilter ? GL_LINEAR : GL_NEAREST);
            }
            else if (filter < 0)
            {
                //mipmapping
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, Math.min(-filter, 2));
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -.75f);
                glGenerateMipmap(GL_TEXTURE_2D);
            }
            else
            {
                //anisotropic filtering
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, filter);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 2);
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -.75f);
                glGenerateMipmap(GL_TEXTURE_2D);
            }
            
            glBindTexture(GL_TEXTURE_2D, 0);
            
            GLUtil.checkErrors();
        }
        
        private void destroy()
        {
            glDeleteTextures(texture);
            GLUtil.checkErrors();
        }
    }
    
    class CharId
    {
        private char id;
        
        public CharId(char id)
        {
            this.id = id;
        }
        
        public void set(char id)
        {
            this.id = id;
        }
        
        public void set(CharId c)
        {
            id = c.id;
        }
        
        public int getId()
        {
            return id;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o == null || !(o instanceof CharId))
            {
                return false;
            }
            
            CharId c = (CharId)o;
            return (id == c.id);
        }

        @Override
        public int hashCode()
        {
            return id;
        }
    }
    
    class CharPair
    {
        private char first, second;
        
        public CharPair(char first, char second)
        {
            this.first = first;
            this.second = second;
        }
        
        public void set(char first, char second)
        {
            this.first = first;
            this.second = second;
        }
        
        public void set(CharPair c)
        {
            first = c.first;
            second = c.second;
        }
        
        public int getFirst()
        {
            return first;
        }
        
        public int getSecond()
        {
            return second;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o == null || !(o instanceof CharPair))
            {
                return false;
            }
            
            CharPair c = (CharPair)o;
            return (first == c.first && second == c.second);
        }

        @Override
        public int hashCode()
        {
            return first + second * 5839;
        }
    }
    
    private short readShort(DataInputStream in) throws IOException
    {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        return (short)((b2 << 8) + b1);
    }
    
    private int readUnsignedShort(DataInputStream in) throws IOException
    {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        return (b2 << 8) + b1;
    }
    
    private int readInt(DataInputStream in) throws IOException
    {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        
        return ((b4 << 24) + (b3 << 16) + (b2 << 8) + b1);
    }
    
    private long readUnsignedInt(DataInputStream in) throws IOException
    {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        
        return ((b4 << 24) + (b3 << 16) + (b2 << 8) + b1);
    }
    
    private String readCString(DataInputStream in) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        
        while (true)
        {
            byte b = in.readByte();
            
            if (b == 0)
            {
                break;
            }
            else
            {
                sb.append((char)b);
            }
        }
        
        return sb.toString();
    }
    
    public void destroy()
    {
        for (int i = 0; i < fontPages.length; i++)
        {
            fontPages[i].destroy();
        }
    }
    
    public void setNormalHeight(int normalHeight)
    {
        this.normalHeight = normalHeight;
    }
    
    public void setNormalHeight(char c)
    {
        normalHeight = base - getChar(c).yOffset;
    }
    
    public int getNormalHeight()
    {
        return normalHeight;
    }
    
    public String getName()
    {
        return fontName;
    }
    
    public int getLineHeight()
    {
        return lineHeight;
    }
    
    public int getBase()
    {
        return base;
    }
    
    public int getPageWidth()
    {
        return scaleW;
    }
    
    public int getPageHeight()
    {
        return scaleH;
    }
    
    @Override
    public String toString()
    {
        return toString(0);
    }
    
    public String toDebugString()
    {
        return toString(1);
    }
    
    public String toFullString()
    {
        return toString(2);
    }
    
    private String toString(int verbosity)
    {
        StringBuilder sb = new StringBuilder();
        
        if (verbosity <= 0)
        {
            sb.append("[BitmapFont: fontName = \"").append(fontName);
            sb.append("\", fontSize = ").append(fontSize);
            sb.append(", pages = ").append(pages);
            sb.append(", chars = ").append(chars.length);
            sb.append(", kerningPairs = ").append(kerningPairs.length);
            sb.append("]");
            
            return sb.toString();
        }
        
        sb.append("[BitmapFont]");
        
        sb.append("\n\n---Info---");
        
        sb.append("\nfontSize = ").append(fontSize);
        sb.append("\nsmooth = ").append(smooth);
        sb.append("\nunicode = ").append(unicode);
        sb.append("\nitalic = ").append(italic);
        sb.append("\nbold = ").append(bold);
        sb.append("\nfixedHeight = ").append(fixedHeight);
        sb.append("\ncharSet = ").append(charSet);
        sb.append("\nstretchH = ").append(stretchH);
        sb.append("\naa = ").append(aa);
        sb.append("\npaddingUp = ").append(paddingUp);
        sb.append("\npaddingRight = ").append(paddingRight);
        sb.append("\npaddingDown = ").append(paddingDown);
        sb.append("\npaddingLeft = ").append(paddingLeft);
        sb.append("\nspacingHoriz = ").append(spacingHoriz);
        sb.append("\nspacingVert = ").append(spacingVert);
        sb.append("\noutline = ").append(outline);
        sb.append("\nfontName = \"").append(fontName).append("\"");
        
        sb.append("\n\n---Common---");
        
        sb.append("\nlineHeight = ").append(lineHeight);
        sb.append("\nbase = ").append(base);
        sb.append("\nscaleW = ").append(scaleW);
        sb.append("\nscaleH = ").append(scaleH);
        sb.append("\npages = ").append(pages);
        sb.append("\npacked = ").append(packed);
        sb.append("\nalphaChnl = ").append(alphaChnl);
        sb.append("\nredChnl = ").append(redChnl);
        sb.append("\ngreenChnl = ").append(greenChnl);
        sb.append("\nblueChnl = ").append(blueChnl);
        
        sb.append("\n\n---Pages---");
        
        sb.append("\nfontPages.length = ").append(fontPages.length);
        
        for (int i = 0; i < fontPages.length; i++)
        {
            sb.append("\n").append(fontPages[i].name);
        }
        
        sb.append("\n\n---Chars---");
        
        sb.append("\nchars.length = ").append(chars.length);
        
        if (verbosity >= 2)
        {
            for (int i = 0; i < chars.length; i++)
            {
                BitmapChar c = chars[i];
                
                sb.append("\n\nid = ").append((int)c.id).append(" ('").append(c.id).append("')");
                sb.append("\nx = ").append(c.x);
                sb.append("\ny = ").append(c.y);
                sb.append("\nwidth = ").append(c.width);
                sb.append("\nheight = ").append(c.height);
                sb.append("\nxOffset = ").append(c.xOffset);
                sb.append("\nyOffset = ").append(c.yOffset);
                sb.append("\nxAdvance = ").append(c.xAdvance);
                sb.append("\npage = ").append(c.page);
                sb.append("\nchnl = ").append(c.chnl);
            }
        }
        
        sb.append("\n\n---Kerning Pairs---");
        
        sb.append("\nkerningPairs.length = ").append(kerningPairs.length);
        
        if (verbosity >= 2)
        {
            for (int i = 0; i < kerningPairs.length; i++)
            {
                BitmapKerningPair k = kerningPairs[i];
                
                sb.append("\n\nfirst = ").append((int)k.first).append(" ('").append(k.first).append("')");
                sb.append("\nsecond = ").append((int)k.second).append(" ('").append(k.second).append("')");
                sb.append("\namount = ").append(k.amount);
            }
        }
        
        return sb.toString();
    }
}