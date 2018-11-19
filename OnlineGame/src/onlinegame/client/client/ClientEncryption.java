package onlinegame.client.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import onlinegame.shared.account.Encryption;

/**
 *
 * @author Alfred
 */
public final class ClientEncryption
{
    private PublicKey key;
    
    public ClientEncryption(PublicKey key)
    {
        this.key = key;
    }
    
    public void writeString(String s, DataOutputStream out) throws IOException
    {
        writeByteArray(encryptString(s), out);
    }
    public void write(byte[] data, DataOutputStream out) throws IOException
    {
        writeByteArray(encrypt(data), out);
    }
    
    private void writeByteArray(byte[] data, DataOutputStream out) throws IOException
    {
        if (data.length >= 65536)
        {
            throw new IOException("Encrypted data is too large: " + data.length + " bytes.");
        }
        
        out.writeShort((short)data.length);
        out.write(data);
    }
    
    public byte[] encryptString(String s)
    {
        return Encryption.encryptString(s, key);
    }
    public byte[] encrypt(byte[] data)
    {
        return Encryption.encrypt(data, key);
    }
}
