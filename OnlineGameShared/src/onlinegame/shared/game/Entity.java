package onlinegame.shared.game;

import java.io.IOException;
import java.util.Comparator;
import onlinegame.shared.bitstream.BitInput;
import onlinegame.shared.bitstream.BitOutput;
import onlinegame.shared.bitstream.IntFormat;
import onlinegame.shared.game.projectiles.TargetedProjectile;
import onlinegame.shared.net.GameProtocolException;

/**
 *
 * @author Alfred
 */
public abstract class Entity
{
    public static Entity read(BitInput in, GameState prev) throws IOException
    {
        int id = in.readInt(IntFormat.UINTV);
        if (id == 0) return null;
        
        Entity ref = prev == null ? null : prev.findEntity(id);
        
        int type = ref == null ? in.readInt(8) : ref.getTypeId();
        switch (type)
        {
            case GameProtocol.E_CHAMPION:
                return new Champion(in, id, (Champion)ref);
            case GameProtocol.E_MINION_CASTER:
                return new CasterMinion(in, id, (CasterMinion)ref);
            case GameProtocol.E_PROJECTILE_TARGETED:
                return new TargetedProjectile(in, id, (TargetedProjectile)ref);
            default:
                throw new GameProtocolException("Invalid entity type: " + type);
        }
    }
    
    public final int id;
    public final int team;
    
    public Entity(int id, int team)
    {
        this.id = id;
        this.team = team;
    }
    protected Entity(BitInput in, int id, Entity prev) throws IOException
    {
        //id = in.readInt(IntFormat.UINTV);
        this.id = id;
        
        if (prev == null)
        {
            team = in.readInt(2);
        }
        else
        {
            team = prev.team;
        }
    }
    
    public abstract int getTypeId();
    
    public final float getXPos(GameState gs)
    {
        return getXPos(gs, gs.currentTime);
    }
    public final float getYPos(GameState gs)
    {
        return getYPos(gs, gs.currentTime);
    }
    
    public abstract float getXPos(GameState gs, double time);
    public abstract float getYPos(GameState gs, double time);
    
    public final void write(BitOutput out, Entity prev) throws IOException
    {
        if (equals(prev))
        {
            return; //this entity is not changed and thus doesn't need to be sent
        }
        
        writeData(out, prev);
    }
    
    protected void writeData(BitOutput out, Entity prev) throws IOException
    {
        //write id and type
        out.writeInt(id, IntFormat.UINTV);
        
        if (prev == null)
        {
            out.writeInt(getTypeId(), 8);
            out.writeInt(team, 2);
        }
    }
    
    @Override
    public final boolean equals(Object other)
    {
        if (other == this) return true;
        if (!(other instanceof Entity)) return false;
        return eq((Entity)other);
    }
    
    protected boolean eq(Entity other)
    {
        return  this == other ||
                (
                getTypeId() == other.getTypeId() &&
                id == other.id &&
                team == other.team
                );
    }
    
    public static final Comparator<Entity> idComparator = new EntityIdComparator();
    private static final class EntityIdComparator implements Comparator<Entity>
    {
        @Override
        public int compare(Entity e1, Entity e2)
        {
            return Integer.compare(e1.id, e2.id);
        }
    }
}
