package onlinegame.shared.net;

import java.io.IOException;

/**
 *
 * @author Alfred
 */
public class GameProtocolException extends IOException
{
    private static final long serialVersionUID = 0x8ab832fd844429a7L;
    
    public GameProtocolException()
    {
        super();
    }
    
    public GameProtocolException(String message)
    {
        super(message);
    }
    
    public GameProtocolException(Throwable cause)
    {
        super(cause);
    }
    
    public GameProtocolException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
