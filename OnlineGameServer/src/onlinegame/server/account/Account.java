package onlinegame.server.account;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import onlinegame.server.io.IO;
import onlinegame.shared.Logger;
import onlinegame.shared.account.AccountChecks;

/**
 *
 * @author Alfred
 */
public final class Account
{
    public static final String basePath = IO.basePath + "account/";
    
    public static final long signature = 0xf63916cf4913ee1dL;
    public static final int latestVersion = 1;
    
    private long lastSave = System.nanoTime();
    private boolean dirty = false;
    
    private final String username;
    private final Password password;
    private final long dateCreated;
    
    Session session;
    
    static
    {
        File dir = new File(basePath);
        if (!dir.exists())
        {
            Logger.log("Creating account directory...");
            if (!dir.mkdirs())
            {
                throw new RuntimeException("Unable to create account directory (\"" + basePath + "\")");
            }
        }
    }
    
    private Account(String username, Password password)
    {
        this(username, password, 1435767376000L); //default date
    }
    
    private Account(String username, Password password, long dateCreated)
    {
        this.username = username;
        this.password = password;
        this.dateCreated = dateCreated;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public long getDateCreated()
    {
        return dateCreated;
    }
    
    public Session getSession()
    {
        return session;
    }
    
    public boolean checkPassword(String password)
    {
        return this.password.check(password);
    }
    
    private static String getPath(String username)
    {
        return basePath + AccountChecks.canonicalUsername(username) + ".account";
    }
    
    private static File getFile(String username)
    {
        return new File(getPath(username));
    }
    
    static boolean exists(String username)
    {
        String path = getPath(username);
        return new File(path).exists();
    }
    
    static Account create(String username, String password) throws IOException
    {
        if (exists(username))
        {
            throw new AccountException("Username " + username + " is already taken.");
        }
        
        long dateCreated = System.currentTimeMillis();
        Account acc = new Account(username, Password.fromPlaintext(password), dateCreated);
        acc.save();
        return acc;
    }
    
    static Account load(String username) throws IOException
    {
        File file = getFile(username);
        boolean save = false;
        Account acc = null;
        
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));)
        {
            long sig = in.readLong();
            if (sig != signature)
            {
                throw new AccountException("Invalid account file signature: " + Long.toHexString(sig));
            }
            
            int version = in.readInt();
            switch (version)
            {
                case 0:
                    acc = loadV0(in);
                    break;
                case 1:
                    acc = loadV1(in);
                    break;
                default:
                    throw new AccountException("Unsupported account file version: " + version);
            }
            
            if (version != latestVersion)
            {
                save = true;
            }
        }
        
        if (acc == null)
        {
            throw new AccountException("Unexpected error occured when loading account \"" + username + "\".");
        }
        
        if (save)
        {
            acc.save();
        }
        return acc;
    }
    
    private static Account loadV1(DataInput in) throws IOException
    {
        String username = in.readUTF();
        
        String passwordHash = in.readUTF();
        String passwordSalt = in.readUTF();
        Password password = Password.fromHash(passwordHash, passwordSalt);
        long dateCreated = in.readLong();
        
        Account acc = new Account(username, password, dateCreated);
        return acc;
    }
    
    private static Account loadV0(DataInput in) throws IOException
    {
        String username = in.readUTF();
        
        String passwordHash = in.readUTF();
        String passwordSalt = in.readUTF();
        Password password = Password.fromHash(passwordHash, passwordSalt);
        
        Account acc = new Account(username, password);
        return acc;
    }
    
    void save() throws IOException
    {
        File file = getFile(username);
        
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));)
        {
            final int version = 1;
            
            if (latestVersion != version)
            {
                throw new RuntimeException("Account.save() is not up to date");
            }
            
            //file signature and version
            out.writeLong(signature);
            out.writeInt(version);
            
            //canonical username
            out.writeUTF(username);
            
            //password hash and salt
            out.writeUTF(password.hash);
            out.writeUTF(password.salt);
            
            //creation date
            out.writeLong(dateCreated);
            
            out.flush();
        }
        
        lastSave = System.nanoTime();
    }
}
