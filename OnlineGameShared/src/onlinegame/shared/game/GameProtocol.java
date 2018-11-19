package onlinegame.shared.game;

import onlinegame.shared.bitstream.IntFormat;

/**
 *
 * @author Alfred
 */
public final class GameProtocol
{
    private GameProtocol() {}
    
    public static final IntFormat TICK_TIME_FORMAT   = new IntFormat(false, 23, 31);
    
    public static final int
            TEAM_BLUE = 0,
            TEAM_RED = 1,
            TEAM_NEUTRAL = 2,
            TEAM_NONE = 3;
    
    public static final int
            E_CHAMPION = 0,
            E_MINION_CASTER = 1,
            E_PROJECTILE_TARGETED = 2;
    
    public static final int
            CMD_NONE = 0,
            CMD_MOVE = 1,
            CMD_ATTACK = 2;
}
