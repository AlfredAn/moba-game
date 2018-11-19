package onlinegame.shared.db;

import onlinegame.shared.Logger;

/**
 *
 * @author Alfred
 */
public final class ChampionDB
{
    private ChampionDB() {}
    
    public static final ChampionData
            TEST_CHAMPION = new ChampionData(0, "Test Champion", "testChamp");
    
    private static final ChampionData[] champions = new ChampionData[]
    {
        TEST_CHAMPION
    };
    
    public static ChampionData get(int id)
    {
        if (id < 0 || id >= count())
        {
            Logger.logError("Champion " + id + " not found!");
            return null;
        }
        return champions[id];
    }
    
    public static int count()
    {
        return champions.length;
    }
    
    public static final class ChampionData
    {
        public final int id;
        public final String
                name,
                filename;
        
        private ChampionData(int id, String name, String filename)
        {
            this.id = id;
            this.name = name;
            this.filename = filename;
        }
    }
}
