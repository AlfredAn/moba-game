package onlinegame.client.game.clientgamestate;

import onlinegame.client.game.graphics.GameModels;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.shared.db.ChampionDB.ChampionData;
import onlinegame.shared.game.Champion;
import onlinegame.shared.game.GameProtocol;
import onlinegame.shared.game.GameState;
import onlinegame.shared.game.actions.Action;
import onlinegame.shared.game.actions.AttackAction;

/**
 *
 * @author Alfred
 */
public final class CChampion extends CActor
{
    protected CChampion(CGameState game, GameState gs, Champion c, Champion next)
    {
        super(game, gs, c, next, Champion.class);
    }
    
    @Override
    public void draw(boolean isSelected)
    {
        Draw.mat.pushMatrix();
        Draw.mat.translate(xPos, yPos, 0);
        Draw.color.set(Color4f.BLUE);
        
        Action a = action;
        if (a != null && a.getActionId() == Action.A_ATTACK)
        {
            AttackAction aa = (AttackAction)a;
            float fracStage = (float)aa.getFracStage(game.getCurrentTime());
            switch (aa.getStage(game.getCurrentTime()))
            {
                case AttackAction.ST_AIMING:
                    Draw.color.blend(Color4f.YELLOW, fracStage * 0.125f);
                    break;
                case AttackAction.ST_PREHIT:
                    Draw.color.blend(Color4f.YELLOW, (1 - fracStage) * 0.125f);
                    Draw.color.blend(Color4f.RED, fracStage * 0.25f);
                    break;
                case AttackAction.ST_POSTHIT:
                    Draw.color.blend(Color4f.RED, (1 - fracStage) * 0.25f);
                    Draw.color.blend(Color4f.WHITE, (1 - fracStage) * 0.25f + 0.125f);
                    break;
                case AttackAction.ST_RELOADING:
                    Draw.color.blend(Color4f.WHITE, (1 - fracStage) * 0.125f);
                    break;
            }
        }
        
        if (isSelected) {Draw.color.blend(Color4f.WHITE, .1875f);}
        
        Draw.drawModel(GameModels.championModel);
        
        Draw.mat.popMatrix();
    }
    
    @Override
    public int getTypeId()
    {
        return GameProtocol.E_CHAMPION;
    }
    
    public ChampionData getChampion()
    {
        return ((Champion)state).champion;
    }
    
    @Override
    public float getClickXSize() {return .6f;}
    @Override
    public float getClickYSize() {return .6f;}
    @Override
    public float getClickZSize() {return 1.7f;}
    
    @Override
    public float getProjectileSpawnZ() {return .9f;}
    @Override
    public float getProjectileHitZ() {return 1.1f;}
    
    @Override
    public Champion getState()
    {
        return (Champion)state;
    }
}
