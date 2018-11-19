package onlinegame.client;

import java.io.IOException;
import onlinegame.client.graphics.ShaderProgram;

/**
 *
 * @author Alfred
 */
public final class Shaders
{
    private Shaders() {}
    
    public static ShaderProgram plain, textured, text, maskedTextured;
    
    public static ShaderProgram gameBase, gameUntextured;
    
    static void load() throws IOException
    {
        plain = new ShaderProgram(
                "shaders/plain.vert",
                "shaders/plain.frag",
                new String[] {"in_Position"},
                new String[] {"u_Matrix", "u_Color"});
        
        textured = new ShaderProgram(
                "shaders/textured.vert",
                "shaders/textured.frag",
                new String[] {"in_Position", "in_TexCoord"},
                new String[] {"u_Matrix", "u_Color", "u_Sampler", "u_Filter"});
        
        text = new ShaderProgram(
                "shaders/text/text.vert",
                "shaders/text/text.frag",
                new String[] {"in_Position", "in_TexCoord"},
                new String[] {"u_Matrix", "u_Scale", "u_Sampler", "u_Color"});
        
        maskedTextured = new ShaderProgram(
                "shaders/maskedTexture.vert",
                "shaders/maskedTexture.frag",
                new String[] {"in_Position", "in_TexCoord"},
                new String[] {"u_Matrix", "u_Color", "u_Sampler", "u_MaskSampler", "u_Filter", "u_FilterColor"});
        
        gameBase = new ShaderProgram(
                "shaders/game/gameBase.vert",
                "shaders/game/gameBase.frag",
                new String[] {"in_Position", "in_TexCoord", "in_Normal"},
                new String[] {"u_Matrix", "u_Color", "u_Sampler", "u_Filter"});
        
        gameUntextured = new ShaderProgram(
                "shaders/game/gameUntextured.vert",
                "shaders/game/gameUntextured.frag",
                new String[] {"in_Position", "in_Normal"},
                new String[] {"u_Matrix", "u_Color"});
    }
    
    static void destroy()
    {
        plain.destroy();
        textured.destroy();
        text.destroy();
        
        gameBase.destroy();
        gameUntextured.destroy();
    }
}
