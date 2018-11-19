package onlinegame.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import onlinegame.shared.Logger;
import onlinegame.shared.account.Encryption;

/**
 *
 * @author Alfred
 */
public final class ServerEncryption
{
    private ServerEncryption() {}
    
    private static final PublicKey publicKey;
    private static final PrivateKey privateKey;
    
    public static void touch() {}
    
    static
    {
        Logger.log("Generating keypair...");
        KeyPair keyPair = Encryption.generateKeyPair();
        
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }
    
    public static PublicKey getPublicKey()
    {
        return publicKey;
    }
    
    public static String readString(DataInputStream in) throws IOException
    {
        return decryptString(readByteArray(in));
    }
    
    public static byte[] read(DataInputStream in) throws IOException
    {
        return decrypt(readByteArray(in));
    }
    
    private static byte[] readByteArray(DataInputStream in) throws IOException
    {
        int len = in.readShort() & 0xffff;
        
        byte[] data = new byte[len];
        in.readFully(data);
        
        return data;
    }
    
    public static byte[] decrypt(byte[] data)
    {
        return Encryption.decrypt(data, privateKey);
    }
    public static String decryptString(byte[] data)
    {
        return Encryption.decryptString(data, privateKey);
    }
}























