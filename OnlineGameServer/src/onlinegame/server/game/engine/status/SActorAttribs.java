package onlinegame.server.game.engine.status;

import onlinegame.shared.game.stats.Attribs;
import static onlinegame.shared.game.stats.Attribs.*;
import onlinegame.shared.game.stats.BaseAttribs;

/**
 *
 * @author Alfred
 */
public final class SActorAttribs extends Attribs
{
    public final BaseAttribs base;
    private final float[] attribs;
    
    public SActorAttribs(BaseAttribs base)
    {
        this.base = base;
        attribs = new float[NUM_ATTRIBS];
        
        recalculate();
    }
    
    @Override
    public float get(int attrib)
    {
        return attribs[attrib];
    }
    
    public void recalculate()
    {
        float[] a = attribs;
        base.copy(a); //reset to base attribs
        
        //attack interval
        a[ATTACK_INTERVAL] = 1f / a[ATTACK_SPEED];
        
        //calculate attack animation timings
        a[ATTACK_TIME] = a[ATTACK_TIME_FACTOR] * a[ATTACK_INTERVAL];
        
        a[ATTACK_PREHIT_OFFSET ] = a[ATTACK_PREHIT_OFFSET_FACTOR ] * a[ATTACK_TIME];
        a[ATTACK_POSTHIT_OFFSET] = a[ATTACK_POSTHIT_OFFSET_FACTOR] * a[ATTACK_TIME];
        a[ATTACK_RELOAD_OFFSET ] = a[ATTACK_RELOAD_OFFSET_FACTOR ] * a[ATTACK_TIME];
    }
}
