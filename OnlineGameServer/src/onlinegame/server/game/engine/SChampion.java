package onlinegame.server.game.engine;

import onlinegame.server.account.Account;
import onlinegame.shared.db.ChampionDB.ChampionData;
import onlinegame.shared.game.Champion;

/**
 *
 * @author Alfred
 */
public class SChampion extends SActor
{
    public final ChampionData champion;
    public final Account account;
    
    public SChampion(SGameState game, float xPos, float yPos, int team, ChampionData champion, Account acc)
    {
        super(game, xPos, yPos, team, baseActor);
        this.champion = champion;
        account = acc;
    }
    
    @Override
    public Champion getSnapshot()
    {
        return new Champion(id, team, path, action, attribs, champion, account.getUsername());
    }
}
