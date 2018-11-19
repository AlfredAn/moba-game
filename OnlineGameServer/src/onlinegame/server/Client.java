package onlinegame.server;

import onlinegame.server.account.Session;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import onlinegame.server.account.Account;
import onlinegame.server.account.AccountException;
import onlinegame.server.account.AccountManager;
import onlinegame.shared.Logger;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.net.Connection;
import onlinegame.shared.account.AccountChecks;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class Client
{
    public final long creationTime;
    private final Connection connection;
    private boolean initialized = false;
    
    private Session session;
    
    public Client(Socket socket)
    {
        creationTime = System.nanoTime();
        connection = new Connection(socket);
        connection.setListener(OnlineGameServer.getInstance());
    }
    
    void update()
    {
        try
        {
            if (!initialized && isConnected())
            {
                //initialize encryption
                sendMessage(MessageBuilder.init(ServerEncryption.getPublicKey()));
                initialized = true;
            }
            
            readMessages();
        }
        catch (IOException e)
        {
            Logger.logError(e);
            close();
        }
        
        if (session != null)
        {
            session.update();
        }
        
        if (isClosed())
        {
            close();
        }
    }
    
    public void flush()
    {
        if (!isClosed())
        {
            connection.flush();
        }
    }
    
    public void sendMessage(OutputMessage msg)
    {
        connection.sendMessage(msg);
    }
    
    private void readMessages() throws IOException
    {
        //read all pending messages
        
        InputMessage msg;
        while ((msg = connection.readMessage()) != null)
        {
            DataInputStream in;
            
            switch (msg.getId())
            {
                case Protocol.C_REGISTER_REQUEST:
                    if (isLoggedIn())
                    {
                        sendMessage(MessageBuilder.registerResponse(false, "You are already logged in!"));
                        return;
                    }
                    
                    in = msg.getDataStream();
                    String username = in.readUTF();
                    String password = ServerEncryption.readString(in);
                    
                    Account acc;
                    try
                    {
                        String errorMsg = AccountChecks.checkLogin(username, password);
                        if (errorMsg != null)
                        {
                            throw new AccountException(errorMsg);
                        }
                        
                        acc = AccountManager.create(username, password);
                    }
                    catch (IOException e)
                    {
                        String err;
                        if (e instanceof AccountException)
                        {
                            err = e.getMessage();
                        }
                        else
                        {
                            err = SharedUtil.getErrorMsg(e);
                        }
                        sendMessage(MessageBuilder.registerResponse(false, err));
                        break;
                    }
                    
                    String text = "Account \"" + acc.getUsername() + "\" has been created!";
                    sendMessage(MessageBuilder.registerResponse(true, text));
                    
                    break;
                case Protocol.C_LOGIN_REQUEST:
                    if (isLoggedIn())
                    {
                        sendMessage(MessageBuilder.loginResponseFail("You are already logged in!"));
                        return;
                    }
                    
                    in = msg.getDataStream();
                    username = in.readUTF();
                    password = ServerEncryption.readString(in);
                    
                    acc = AccountManager.get(username);
                    
                    if (acc != null && acc.checkPassword(password))
                    {
                        //login successful
                        Logger.log(acc.getUsername() + " has logged in.");
                        
                        //start session
                        session = AccountManager.createSession(acc, this);
                        
                        sendMessage(MessageBuilder.loginResponseSuccess("Login successful!", acc.getUsername(), session.sessionHash));
                        
                        //join global chat room
                        session.joinChat(OnlineGameServer.globalChat);
                        
                        //send lobby list
                        sendMessage(MessageBuilder.lobbyList());
                    }
                    else
                    {
                        //failed
                        sendMessage(MessageBuilder.loginResponseFail("Invalid username or password!"));
                    }
                    
                    break;
            }
            
            if (session != null)
            {
                session.readMessage(msg);
            }
        }
    }
    
    public String getUsername()
    {
        return session == null ? null : session.getAccount().getUsername();
    }
    
    public boolean isLoggedIn()
    {
        return isConnected() && session != null && session.isActive();
    }
    
    public Session getSession()
    {
        return session;
    }
    
    public int getConnectionState()
    {
        return connection.getState();
    }
    
    public boolean isConnected()
    {
        return connection.getState() == Connection.ST_CONNECTED;
    }
    
    public boolean isClosed()
    {
        return connection.getState() == Connection.ST_CLOSED;
    }
    
    public InetAddress getAddress()
    {
        return connection.getAddress();
    }
    
    public void close()
    {
        connection.close();
        
        if (session != null)
        {
            session.stop();
        }
    }
}



















