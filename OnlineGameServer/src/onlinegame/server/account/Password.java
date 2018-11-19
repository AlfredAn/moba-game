package onlinegame.server.account;

/**
 *
 * @author Alfred
 */
public final class Password
{
    public final String hash, salt;
    
    private Password(String hash, String salt)
    {
        this.hash = hash;
        this.salt = salt;
    }
    
    static Password fromHash(String hash, String salt)
    {
        return new Password(hash, salt);
    }
    
    public static Password fromPlaintext(String password)
    {
        return fromPlaintext(password, BCrypt.gensalt());
    }
    
    private static Password fromPlaintext(String password, String salt)
    {
        return new Password(BCrypt.hashpw(password, salt), salt);
    }
    
    public boolean check(String password)
    {
        Password pass = Password.fromPlaintext(password, salt);
        return hash.equals(pass.hash);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Password))
        {
            return false;
        }
        
        Password p = (Password)o;
        return hash.equals(p.hash) && salt.equals(p.salt);
    }

    @Override
    public int hashCode()
    {
        return hash.hashCode() ^ salt.hashCode();
    }
    
    @Override
    public String toString()
    {
        return "Password(hash=\"" + hash + "\", salt=\"" + salt + "\")";
    }
}
