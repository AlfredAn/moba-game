package onlinegame.server.game.engine;

import onlinegame.shared.game.Minion;

/**
 *
 * @author Alfred
 */
public abstract class SMinion extends SActor
{
    public SMinion(SGameState game, float xPos, float yPos, int team)
    {
        super(game, xPos, yPos, team, baseActor);
    }
    
    @Override
    public abstract Minion getSnapshot();
}
