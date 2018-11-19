package onlinegame.client;

import onlinegame.client.graphics.texture.Texture;
import onlinegame.client.graphics.texture.TextureBuilder;
import onlinegame.shared.db.ChampionDB;
import onlinegame.shared.db.ChampionDB.ChampionData;

/**
 *
 * @author Alfred
 */
public final class Textures
{
    private Textures() {}
    
    private static final Texture[] championProfile, championProfileWithLodBias;
    
    static
    {
        championProfile = new Texture[ChampionDB.count()];
        championProfileWithLodBias = new Texture[ChampionDB.count()];
        
        for (int i = 0; i < championProfile.length; i++)
        {
            championProfile[i] = new TextureBuilder()
                    .filename("textures/championProfile/" + ChampionDB.get(i).filename + ".png")
                    .mipmap(-1)
                    .anisotropy(-1)
                    .finish();
            championProfileWithLodBias[i] = new TextureBuilder()
                    .filename("textures/championProfile/" + ChampionDB.get(i).filename + ".png")
                    .mipmap(-1)
                    .anisotropy(-1)
                    .lodBias(-.75f)
                    .finish();
        }
    }
    
    public static void load()
    {
        for (int i = 0; i < championProfile.length; i++)
        {
            championProfile[i].create();
            championProfileWithLodBias[i].create();
        }
    }
    
    public static void destroy()
    {
        for (int i = 0; i < championProfile.length; i++)
        {
            championProfile[i].destroy();
            championProfileWithLodBias[i].destroy();
        }
    }
    
    public static Texture getChampionProfile(ChampionData champion)
    {
        if (champion == null)
        {
            return null;
        }
        return championProfile[champion.id];
    }
    
    public static Texture getChampionProfileWithLodBias(ChampionData champion)
    {
        if (champion == null)
        {
            return null;
        }
        return championProfileWithLodBias[champion.id];
    }
}
