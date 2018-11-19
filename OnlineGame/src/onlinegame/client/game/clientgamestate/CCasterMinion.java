package onlinegame.client.game.clientgamestate;

import onlinegame.client.game.graphics.GameModels;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.shared.game.CasterMinion;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;

/**
 *
 * @author Alfred
 */
public final class CCasterMinion extends CMinion
{
    protected CCasterMinion(CGameState game, GameState gs, CasterMinion cm, CasterMinion next)
    {
        super(game, gs, cm, next, CasterMinion.class);
    }
    
    @Override
    public void draw(boolean isSelected)
    {
        Draw.mat.pushMatrix();
        Draw.mat.translate(xPos, yPos, 0);
        Draw.color.set(minionTeamCol[team]);
        if (isSelected) {Draw.color.blend(Color4f.WHITE, .25f);}
        
        Draw.drawModel(GameModels.casterMinionModel);
        
        Draw.mat.popMatrix();
    }
    
    @Override
    public int getTypeId()
    {
        return GameProtocol.E_MINION_CASTER;
    }
    
    @Override
    public CasterMinion getState()
    {
        return (CasterMinion)state;
    }
}
