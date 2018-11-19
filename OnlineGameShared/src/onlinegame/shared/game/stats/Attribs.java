package onlinegame.shared.game.stats;

import java.io.IOException;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;

/**
 *
 * @author Alfred
 */
public abstract class Attribs
{
    public static final int
            MOVE_SPEED = 0,   //meters per second
            
            ATTACK_RANGE = 1, //meters
            ATTACK_SPEED = 2, //attacks per second
            
            //length of different attack stages (this is multiplied by ATTACK_TIME to get the actual time)
            ATTACK_PREHIT_OFFSET_FACTOR = 3,
            ATTACK_POSTHIT_OFFSET_FACTOR = 4,
            ATTACK_RELOAD_OFFSET_FACTOR = 5,
            
            ATTACK_PREHIT_OFFSET = 6,
            ATTACK_POSTHIT_OFFSET = 7,
            ATTACK_RELOAD_OFFSET = 8,
            
            ATTACK_TIME_FACTOR = 9, //proportion of attack interval taken up by the animation
            
            ATTACK_TIME = 10, //ATTACK_TIME = ATTACK_TIME_FACTOR / ATTACK_SPEED
            ATTACK_INTERVAL = 11, //time between two attacks, ATTACK_INTERVAL = 1 / ATTACK_SPEED
            ATTACK_PROJECTILE_SPEED = 12, //meters per second
            
            RES_HEALTH_MAX = 13,
            RES_MANA_MAX = 14,
            
            NUM_ATTRIBS = 15;
    
    private static final boolean[] isSynced =
    {
        true,  // 0
        false, // 1
        false, // 2
        false, // 3
        false, // 4
        false, // 5
        false, // 6
        false, // 7
        false, // 8
        false, // 9
        false, //10
        false, //11
        false, //12
        false, //13
        false, //14
    };
    
    public static boolean isSynced(int attrib)
    {
        return isSynced[attrib];
    }
    
    public abstract float get(int attrib);
    
    public static BaseAttribs read(BitInput in) throws IOException
    {
        float[] a = new float[NUM_ATTRIBS];
        
        for (int i = 0; i < NUM_ATTRIBS; i++)
        {
            if (isSynced(i))
            {
                a[i] = in.readFloat();
            }
        }
        
        return new BaseAttribs(a);
    }
    
    public final void write(BitOutput out) throws IOException
    {
        for (int i = 0; i < NUM_ATTRIBS; i++)
        {
            if (isSynced(i))
            {
                out.writeFloat(get(i));
            }
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof Attribs)) return false;
        
        Attribs a = (Attribs)o;
        for (int i = 0; i < NUM_ATTRIBS; i++)
        {
            if (isSynced(i) && get(i) != a.get(i)) return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        for (int i = 0; i < NUM_ATTRIBS; i++)
        {
            if (isSynced(i))
            {
                hash = hash * 71 + Float.floatToIntBits(get(i));
            }
        }
        return hash;
    }
}
