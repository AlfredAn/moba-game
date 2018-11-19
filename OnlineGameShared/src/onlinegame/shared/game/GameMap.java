package onlinegame.shared.game;

import de.matthiasmann.twl.utils.PNGDecoder;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import onlinegame.shared.Logger;

/**
 *
 * @author Alfred
 */
public final class GameMap
{
    public static final GameMap
            TEST;
    
    static
    {
        try
        { 
            TEST = new GameMap(0, "Test Map", "/maps/main2.png", .4f);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load maps: " + e.getMessage(), e);
        }
    }
    
    public static final byte
            C_INVALID = 0,
            C_EMPTY = 1,
            C_WALL = 2;
    
    private static final boolean[] isWalkable =
    {
        false,
        true,
        false
    };
    
    private static final boolean[] isOpaque =
    {
        false,
        false,
        true
    };
    
    public final int id;
    public final String name;
    private final byte[] cells;
    public final int width, height;
    public final float scale;
    public final float invscale;
    
    private GameMap(int id, String name, String fname, float scale) throws IOException
    {
        this.id = id;
        this.name = name;
        this.scale = scale;
        invscale = 1f / scale;
        
        ByteBuffer buf;
        try (InputStream in = GameMap.class.getResourceAsStream(fname))
        {
            if (in == null)
            {
                throw new FileNotFoundException(fname);
            }
            
            InputStream bin;
            if (in instanceof BufferedInputStream)
            {
                bin = in;
            }
            else
            {
                bin = new BufferedInputStream(in);
            }
            PNGDecoder dec = new PNGDecoder(bin);
            
            width = dec.getWidth();
            height = dec.getHeight();
            int bytesPerPixel = 4;

            buf = ByteBuffer.allocate(width * height * bytesPerPixel);
            dec.decode(buf, width * bytesPerPixel, PNGDecoder.Format.RGBA);
            buf.flip();
        }
        
        IntBuffer iBuf = buf.asIntBuffer();
        cells = new byte[width * height];
        
        for (int i = 0; i < cells.length; i++)
        {
            int col = iBuf.get() >>> 8;
            switch (col)
            {
                case 0xffffff:
                    cells[i] = C_EMPTY;
                    break;
                case 0x000000:
                    cells[i] = C_WALL;
                    break;
                case 0xff00ff:
                    cells[i] = C_INVALID;
                    break;
                default:
                    Logger.logError("[" + i%width + ", " + i/width + "]: Invalid color: " + Integer.toHexString(col));
                    cells[i] = C_INVALID;
                    break;
            }
        }
    }
    
    public float getScaledWidth()
    {
        return width * scale;
    }
    
    public float getScaledHeight()
    {
        return height * scale;
    }
    
    public static boolean isWalkable(int cellType)
    {
        return isWalkable[cellType];
    }
    
    public static boolean isVisible(int cellType)
    {
        return isOpaque[cellType];
    }
    
    public byte getCell(int x, int y)
    {
        if (isOutOfBounds(x, y))
        {
            return C_INVALID;
        }
        return getCellFast(x, y);
    }
    
    public boolean isWalkable(int x, int y)
    {
        return isWalkable[getCell(x, y)];
    }
    
    public boolean isOpaque(int x, int y)
    {
        return isOpaque[getCell(x, y)];
    }
    
    private boolean isOutOfBounds(int x, int y)
    {
        return x < 0 || x >= width || y < 0 || y >= height;
    }
    
    public byte getCellFast(int i)
    {
        return cells[i];
    }
    
    public boolean isWalkableFast(int i)
    {
        return isWalkable[cells[i]];
    }
    
    public boolean isOpaqueFast(int i)
    {
        return isOpaque[cells[i]];
    }
    
    public byte getCellFast(int x, int y)
    {
        return cells[x + y*width];
    }
    
    public boolean isWalkableFast(int x, int y)
    {
        return isWalkable[cells[x + y*width]];
    }
    
    public boolean isOpaqueFast(int x, int y)
    {
        return isOpaque[cells[x + y*width]];
    }
}
