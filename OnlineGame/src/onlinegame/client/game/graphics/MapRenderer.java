package onlinegame.client.game.graphics;

import onlinegame.client.graphics.Model;
import onlinegame.client.graphics.ModelBuilder;
import onlinegame.shared.game.GameMap;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Alfred
 */
public final class MapRenderer
{
    private MapRenderer() {}
    
    private static final float
            WALL_HEIGHT = 1.75f;
    
    public static Model renderMap(GameMap map)
    {
        int width = map.width;
        int height = map.height;
        float cellSize = map.scale;
        int estFaces = width * height / 4;
        
        ModelBuilder mb = new ModelBuilder(GL_TRIANGLES, false, true, true, estFaces, estFaces);
        
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                byte cell = map.getCell(x, y);
                if (cell != GameMap.C_WALL)
                {
                    continue;
                }
                
                float x1 = x * cellSize;
                float y1 = y * cellSize;
                float z1 = WALL_HEIGHT;
                
                float x2 = (x+1) * cellSize;
                float y2 = (y+1) * cellSize;
                float z2 = 0;
                
                float fx = (float)(x % 2) / 2;
                float fy = (float)(y % 2) / 2;
                
                float s1 = 0;
                float s2 = .5f;
                
                float t1 = 0;
                float t2 = 1;
                
                //render top
                float ts1 = fx;
                float ts2 = fx + .5f;
                float tt1 = fy - .5f;
                float tt2 = fy;
                
                mb.normal(0, 0, 1);
                
                mb.texCoord(ts1, tt1);
                mb.vertex(x1, y1, z1);
                
                mb.texCoord(ts1, tt2);
                mb.vertex(x1, y2, z1);
                
                mb.texCoord(ts2, tt2);
                mb.vertex(x2, y2, z1);
                
                mb.texCoord(ts2, tt1);
                mb.vertex(x2, y1, z1);
                
                mb.index(0, 1, 2);
                mb.index(0, 2, 3);
                mb.flush();
                
                if (!map.isOpaque(x-1, y))
                {
                    //render west side
                    mb.normal(-1, 0, 0);
                    
                    mb.texCoord(s1 + fy, t1);
                    mb.vertex(x1, y1, z1);
                    
                    mb.texCoord(s1 + fy, t2);
                    mb.vertex(x1, y1, z2);
                    
                    mb.texCoord(s2 + fy, t2);
                    mb.vertex(x1, y2, z2);
                    
                    mb.texCoord(s2 + fy, t1);
                    mb.vertex(x1, y2, z1);
                    
                    mb.index(0, 1, 2);
                    mb.index(0, 2, 3);
                    mb.flush();
                }
                if (!map.isOpaque(x+1, y))
                {
                    //render east side
                    mb.normal(1, 0, 0);
                    
                    mb.texCoord(s1 - fy, t1);
                    mb.vertex(x2, y2, z1);
                    
                    mb.texCoord(s1 - fy, t2);
                    mb.vertex(x2, y2, z2);
                    
                    mb.texCoord(s2 - fy, t2);
                    mb.vertex(x2, y1, z2);
                    
                    mb.texCoord(s2 - fy, t1);
                    mb.vertex(x2, y1, z1);
                    
                    mb.index(0, 1, 2);
                    mb.index(0, 2, 3);
                    mb.flush();
                }
                if (!map.isOpaque(x, y-1))
                {
                    //render north side
                    mb.normal(0, -1, 0);
                    
                    mb.texCoord(s1 + fx, t1);
                    mb.vertex(x2, y1, z1);
                    
                    mb.texCoord(s1 + fx, t2);
                    mb.vertex(x2, y1, z2);
                    
                    mb.texCoord(s2 + fx, t2);
                    mb.vertex(x1, y1, z2);
                    
                    mb.texCoord(s2 + fx, t1);
                    mb.vertex(x1, y1, z1);
                    
                    mb.index(0, 1, 2);
                    mb.index(0, 2, 3);
                    mb.flush();
                }
                if (!map.isOpaque(x, y+1))
                {
                    //render south side
                    mb.normal(0, 1, 0);
                    
                    mb.texCoord(s1 - fx, t1);
                    mb.vertex(x1, y2, z1);
                    
                    mb.texCoord(s1 - fx, t2);
                    mb.vertex(x1, y2, z2);
                    
                    mb.texCoord(s2 - fx, t2);
                    mb.vertex(x2, y2, z2);
                    
                    mb.texCoord(s2 - fx, t1);
                    mb.vertex(x2, y2, z1);
                    
                    mb.index(0, 1, 2);
                    mb.index(0, 2, 3);
                    mb.flush();
                }
            }
        }
        
        return mb.finish();
    }
}







