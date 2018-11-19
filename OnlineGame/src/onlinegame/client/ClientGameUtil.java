package onlinegame.client;

import onlinegame.client.graphics.IColor4f;

/**
 *
 * @author Alfred
 */
public final class ClientGameUtil
{
    private ClientGameUtil() {}
    
    private static final IColor4f[] teamColor = new IColor4f[]
    {
        new IColor4f(.25f, .25f, 1f),
        new IColor4f(1f, .25f, .25f)
    };
    
    private static final IColor4f[] teamColor2 = new IColor4f[]
    {
        new IColor4f(.5f, .5f, .875f),
        new IColor4f(.875f, .5f, .5f)
    };
    
    public static IColor4f getTeamColor(int team)
    {
        return teamColor[team];
    }
    
    public static IColor4f getDesaturatedTeamColor(int team)
    {
        return teamColor2[team];
    }
}
