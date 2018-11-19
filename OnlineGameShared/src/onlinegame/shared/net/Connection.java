package onlinegame.shared.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import onlinegame.shared.Logger;

/**
 *
 * @author Alfred
 */
public final class Connection implements Runnable
{
    private String hostname;
    private int port;
    private Socket socket;
    private InetAddress address;
    private final Thread thread;
    private final boolean isServer;
    private Output output;
    
    public static final int
            ST_NOTCONNECTED = 0,
            ST_CONNECTED = 1,
            ST_CLOSED = 2;
    
    private volatile int state = ST_NOTCONNECTED;
    private volatile boolean closing = false;
    private volatile String errorMsg = null;
    
    private final Object errLock = new Object();
    
    private static final OutputMessage flushDummyMessage = new OutputMessage((byte)-1);
    
    private final ArrayDeque<InputMessage> inputMessages = new ArrayDeque<>();
    private final ArrayDeque<OutputMessage> outputMessages = new ArrayDeque<>();
    
    private DataInputStream in;
    private DataOutputStream out;
    
    private ConnectionListener listener;
    
    /**
     * For the client.
     * @param hostname The host to connect to.
     * @param port The network port to use.
     */
    public Connection(String hostname, int port)
    {
        this.hostname = hostname;
        this.port = port;
        isServer = false;
        
        Logger.log("Connecting to " + hostname + ":" + port + "...");
        
        thread = new Thread(this, "Connection: " + hostname + ":" + port);
        thread.setPriority(5);
        thread.setDaemon(true);
        thread.start();
    }
    
    public void setListener(ConnectionListener listener)
    {
        this.listener = listener;
    }
    
    /**
     * For the server.
     * @param socket The newly accepted socket.
     */
    public Connection(Socket socket)
    {
        if (socket == null)
        {
            throw new NullPointerException();
        }
        
        this.socket = socket;
        isServer = true;
        
        address = socket.getInetAddress();
        
        thread = new Thread(this, "Connection: " + address);
        thread.setPriority(5);
        thread.setDaemon(true);
        thread.start();
    }
    
    @Override
    public void run()
    {
        try
        {
            if (!isServer)
            {
                socket = new Socket();
                socket.setPerformancePreferences(0, 1, 0);
                socket.connect(new InetSocketAddress(hostname, port), 5000);
                
                address = socket.getInetAddress();
            }
            
            if (closing)
            {
                state = ST_CLOSED;
                return;
            }
            
            in =
                    new DataInputStream(
                    new BufferedInputStream(
                    socket.getInputStream()));
            out =
                    new DataOutputStream(
                    new BufferedOutputStream(
                    socket.getOutputStream()));
            
            long sendNum = isServer ? Protocol.SERVER_MAGIC_NUMBER : Protocol.CLIENT_MAGIC_NUMBER;
            long getNum = isServer ? Protocol.CLIENT_MAGIC_NUMBER : Protocol.SERVER_MAGIC_NUMBER;
            
            out.writeLong(sendNum);
            out.writeInt(Protocol.VERSION);
            out.flush();
            
            long mNum = in.readLong();
            if (mNum != getNum)
            {
                throw new GameProtocolException("Magic number doesn't match.");
            }
            
            int version = in.readInt();
            if (version != Protocol.VERSION)
            {
                throw new GameProtocolException("Version mismatch. (v=" + version + ")");
            }
            
            state = ST_CONNECTED;
            
            thread.setName("Connection: " + address + " (input)");
            
            output = new Output();
            
            while (!closing)
            {
                InputMessage msg = new InputMessage(in);
                
                synchronized (inputMessages)
                {
                    inputMessages.add(msg);
                }
                
                //Logger.log("RECIEVED");
                
                if (listener != null)
                {
                    listener.newMessage();
                }
            }
        }
        catch (Throwable e)
        {
            setErrorMsg(e);
            //Logger.logError(address == null ? hostname + ":" + port : address.toString(), e);
        }
        finally
        {
            //Logger.log("Closed input thread.");
            close();
        }
    }
    
    private class Output implements Runnable
    {
        private final Thread thread;
        
        private Output()
        {
            thread = new Thread(this, "Connection: " + address + " (output)");
            thread.setPriority(5);
            thread.setDaemon(true);
            thread.start();
        }
        
        @Override
        public void run()
        {
            try
            {
                while (!closing)
                {
                    synchronized (outputMessages)
                    {
                        OutputMessage msg;
                        do
                        {
                            msg = outputMessages.poll();
                            
                            if (msg != null)
                            {
                                if (msg == flushDummyMessage)
                                {
                                    out.flush();
                                }
                                else
                                {
                                    msg.send(out);
                                    //Logger.log("SENT!");
                                }
                            }
                        }
                        while (msg != null);
                        
                        outputMessages.wait(5000);
                    }
                }
            }
            catch (Throwable e)
            {
                setErrorMsg(e);
            }
            finally
            {
                //Logger.log("Closed output thread.");
                close();
            }
        }
    }
    
    private void setErrorMsg(Throwable e)
    {
        Logger.log(e);
        
        synchronized (errLock)
        {
            if (errorMsg != null)
            {
                return;
            }
            
            if (e instanceof UnknownHostException)
            {
               errorMsg = "Cannot resolve hostname.";
            }
            else if (e instanceof ConnectException && e.getMessage().equals("Connection refused: connect"))
            {
                errorMsg = "Connection refused.";
            } 
            else if ((e instanceof SocketTimeoutException) || (e instanceof ConnectException && e.getMessage().equals("Connection timed out: connect")))
            {
                errorMsg = "Connection timed out.";
            }
            else
            {
                errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
        }
    }
    
    public void close()
    {
        if (!closing)
        {
            //Logger.log("Closing connection...");
        }
        
        closing = true;
        state = ST_CLOSED;
        
        if (socket != null)
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                Logger.logError("Error closing socket.", e);
            }
        }
    }
    
    public InputMessage readMessage()
    {
        synchronized (inputMessages)
        {
            return inputMessages.poll();
        }
    }
    
    public void sendMessage(OutputMessage msg)
    {
        synchronized (outputMessages)
        {
            outputMessages.add(msg);
            outputMessages.notifyAll();
        }
    }
    
    public void flush()
    {
        sendMessage(flushDummyMessage);
    }
    
    public int getState()
    {
        return state;
    }
    
    public boolean isConnected()
    {
        return state == ST_CONNECTED;
    }
    
    public boolean isClosed()
    {
        return state == ST_CLOSED;
    }
    
    public String getErrorMessage()
    {
        return errorMsg;
    }
    
    public InetAddress getAddress()
    {
        return address;
    }
}



















