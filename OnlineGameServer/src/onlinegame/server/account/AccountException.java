package onlinegame.server.account;

import java.io.IOException;

/**
 *
 * @author Alfred
 */
public class AccountException extends IOException
{
    private static final long serialVersionUID = 0x3e2e6dd4f7884443L;
    
    public AccountException()
    {
        super();
    }
    
    public AccountException(String message)
    {
        super(message);
    }
    
    public AccountException(Throwable cause)
    {
        super(cause);
    }
    
    public AccountException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
