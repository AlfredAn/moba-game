package onlinegame.client.graphics;

import static org.lwjgl.opengl.GL15.*;

/**
 *
 * @author Alfred
 */
public final class DynamicModel
{
    public final ModelBuilder builder;
    private Model model;
    
    public DynamicModel(int primType)
    {
        this(new ModelBuilder(primType));
    }
    
    public DynamicModel(int primType, boolean hasColor)
    {
        this(new ModelBuilder(primType, hasColor));
    }
    
    public DynamicModel(int primType, boolean hasColor, boolean hasTexture)
    {
        this(new ModelBuilder(primType, hasColor, hasTexture));
    }
    
    public DynamicModel(int primType, boolean hasColor, boolean hasTexture, boolean hasNormals)
    {
        this(new ModelBuilder(primType, hasColor, hasTexture, hasNormals));
    }
    
    public DynamicModel(int primType, boolean hasColor, boolean hasTexture, boolean hasNormals, int vertexCapacity, int indexCapacity)
    {
        this(new ModelBuilder(primType, hasColor, hasTexture, hasNormals, vertexCapacity, indexCapacity));
    }
    
    public DynamicModel(ModelBuilder builder)
    {
        this.builder = builder;
    }
    
    public void destroy()
    {
        if (model != null)
        {
            model.destroy();
            model = null;
        }
    }
    
    public Model build()
    {
        if (model == null)
        {
            model = builder.finish(GL_DYNAMIC_DRAW);
        }
        else
        {
            builder.finish(model);
        }
        return model;
    }
    
    public Model getModel()
    {
        return model;
    }
}
