package onlinegame.server.game.engine;

import onlinegame.shared.game.CasterMinion;

/**
 *
 * @author Alfred
 */
public final class SCasterMinion extends SMinion
{
    public SCasterMinion(SGameState game, float xPos, float yPos, int team)
    {
        super(game, xPos, yPos, team);
    }
    
    @Override
    public CasterMinion getSnapshot()
    {
        return new CasterMinion(id, team, path, action, attribs);
    }
}
