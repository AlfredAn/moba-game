package onlinegame.client;

import onlinegame.shared.Logger;
import onlinegame.client.graphics.ModelBuilder;
import onlinegame.client.graphics.Model;
import static org.lwjgl.opengl.GL11.*;

public final class Models
{
    private Models() {}
    
    public static Model fillRect, drawRect, drawLine, drawCircle16, fillCircle16;
    
    static void load()
    {
        Logger.log("Loading models...");
        
        //fillRect
        ModelBuilder mb = new ModelBuilder(GL_TRIANGLES, false, true);
        
        mb.texCoord(0, 1);
        mb.vertex(0, 1);
        
        mb.texCoord(0, 0);
        mb.vertex(0, 0);
        
        mb.texCoord(1, 1);
        mb.vertex(1, 1);
        
        ///
        mb.index(0, 1, 2);
        mb.flush();
        ///
        
        mb.texCoord(1, 1);
        mb.vertex(1, 1);
        
        mb.texCoord(0, 0);
        mb.vertex(0, 0);
        
        mb.texCoord(1, 0);
        mb.vertex(1, 0);
        
        mb.index(0, 1, 2);
        
        fillRect = mb.finish();
        
        //drawRect
        mb = new ModelBuilder(GL_LINE_LOOP);
        
        mb.vertex(0, 0);
        mb.vertex(1, 0);
        mb.vertex(1, 1);
        mb.vertex(0, 1);
        mb.index(0, 1, 2, 3);
        
        drawRect = mb.finish();
        
        //drawLine
        mb = new ModelBuilder(GL_LINES);
        
        mb.vertex(0, 0);
        mb.vertex(1, 1);
        mb.index(0, 1);
        
        drawLine = mb.finish();
        
        drawCircle16 = renderOffsetUnitCircle(16, false);
        fillCircle16 = renderOffsetUnitCircle(16, true);
    }
    
    private static final double TWO_PI = Math.PI * 2;
    
    public static Model renderOffsetUnitCircle(int numVertices, boolean filled)
    {
        if (numVertices < 3) throw new IllegalArgumentException();
        
        ModelBuilder mb = new ModelBuilder(filled ? GL_TRIANGLE_FAN : GL_LINE_LOOP, false, filled, false, numVertices, numVertices);
        
        double step = TWO_PI / numVertices;
        for (int i = 0; i < numVertices; i++)
        {
            double angle = step * i;
            float x = (float)((1.0 + Math.cos(angle)) / 2.0);
            float y = (float)((1.0 + Math.sin(angle)) / 2.0);
            if (filled) mb.texCoord(x, y);
            mb.vertex(x, y);
            mb.index(i);
        }
        
        return mb.finish();
    }
    
    public static Model renderCuboidNoBottom(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        ModelBuilder mb = new ModelBuilder(GL_TRIANGLES, false, false, true, 20, 30);
        
        //+z (top)
        mb.normal(0, 0, 1);
        
        mb.vertex(x1, y1, z2);
        mb.vertex(x1, y2, z2);
        mb.vertex(x2, y2, z2);
        mb.vertex(x2, y1, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //-x (west)
        mb.normal(-1, 0, 0);
        
        mb.vertex(x1, y1, z2);
        mb.vertex(x1, y1, z1);
        mb.vertex(x1, y2, z1);
        mb.vertex(x1, y2, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //+x (east)
        mb.normal(1, 0, 0);
        
        mb.vertex(x2, y2, z2);
        mb.vertex(x2, y2, z1);
        mb.vertex(x2, y1, z1);
        mb.vertex(x2, y1, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //-y (north)
        mb.normal(0, -1, 0);
        
        mb.vertex(x2, y1, z2);
        mb.vertex(x2, y1, z1);
        mb.vertex(x1, y1, z1);
        mb.vertex(x1, y1, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //+y (south)
        mb.normal(0, 1, 0);
        
        mb.vertex(x1, y2, z2);
        mb.vertex(x1, y2, z1);
        mb.vertex(x2, y2, z1);
        mb.vertex(x2, y2, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        return mb.finish();
    }
    
    public static Model renderCuboid(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        ModelBuilder mb = new ModelBuilder(GL_TRIANGLES, false, false, true, 24, 36);
        
        //-z (bottom)
        mb.normal(0, 0, 1);
        
        mb.vertex(x2, y1, z1);
        mb.vertex(x2, y2, z1);
        mb.vertex(x1, y2, z1);
        mb.vertex(x1, y1, z1);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //+z (top)
        mb.normal(0, 0, 1);
        
        mb.vertex(x1, y1, z2);
        mb.vertex(x1, y2, z2);
        mb.vertex(x2, y2, z2);
        mb.vertex(x2, y1, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //-x (west)
        mb.normal(-1, 0, 0);
        
        mb.vertex(x1, y1, z2);
        mb.vertex(x1, y1, z1);
        mb.vertex(x1, y2, z1);
        mb.vertex(x1, y2, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //+x (east)
        mb.normal(1, 0, 0);
        
        mb.vertex(x2, y2, z2);
        mb.vertex(x2, y2, z1);
        mb.vertex(x2, y1, z1);
        mb.vertex(x2, y1, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //-y (north)
        mb.normal(0, -1, 0);
        
        mb.vertex(x2, y1, z2);
        mb.vertex(x2, y1, z1);
        mb.vertex(x1, y1, z1);
        mb.vertex(x1, y1, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        //+y (south)
        mb.normal(0, 1, 0);
        
        mb.vertex(x1, y2, z2);
        mb.vertex(x1, y2, z1);
        mb.vertex(x2, y2, z1);
        mb.vertex(x2, y2, z2);
        
        mb.index(0, 1, 2);
        mb.index(0, 2, 3);
        mb.flush();
        
        return mb.finish();
    }
    
    static void destroy()
    {
        fillRect.destroy();
        drawRect.destroy();
        drawLine.destroy();
        fillCircle16.destroy();
    }
}