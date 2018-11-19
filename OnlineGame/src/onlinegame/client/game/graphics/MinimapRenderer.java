package onlinegame.client.game.graphics;

import java.nio.ByteBuffer;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.texture.Texture;
import onlinegame.client.graphics.texture.TextureBuilder;
import onlinegame.shared.game.GameMap;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Alfred
 */
public final class MinimapRenderer
{
    private MinimapRenderer() {}
    
    private static final int
            COL_GROUND = Color4f.toInt(111f / 255, 155f / 255, 36f / 255),
            COL_WALL = Color4f.toInt(.0625f, .0625f, .0625f),
            COL_INVALID = COL_WALL,
            
            COL_ERROR = Color4f.MAGENTA.toInt();
    
    public static Texture renderMinimap(GameMap map)
    {
        ByteBuffer buf = BufferUtils.createByteBuffer(map.width * map.height * 4);
        
        for (int y = 0; y < map.height; y++)
        {
            for (int x = 0; x < map.width; x++)
            {
                switch (map.getCellFast(x, y))
                {
                    case GameMap.C_INVALID:
                        buf.putInt(COL_INVALID);
                        break;
                    case GameMap.C_EMPTY:
                        buf.putInt(COL_GROUND);
                        break;
                    case GameMap.C_WALL:
                        buf.putInt(COL_WALL);
                        break;
                    default:
                        buf.putInt(COL_ERROR);
                        break;
                }
            }
        }
        
        buf.flip();
        
        return new TextureBuilder()
                .data(buf, map.width, map.height)
                .finish();
    }
}
