package onlinegame.shared.account;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.Cipher;

/**
 *
 * @author Alfred
 */
public final class Encryption
{
    private Encryption() {}
    
    private static final SecureRandom random = new SecureRandom();
    private static final String ALGORITHM = "RSA";
    private static final int saltLength = 8;
    
    public static void touch()
    {
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
        }
        catch (GeneralSecurityException e) {}
    }
    
    public static KeyPair generateKeyPair()
    {
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static byte[] encryptString(String text, PublicKey key)
    {
        return encrypt(text.getBytes(StandardCharsets.UTF_8), key);
    }
    public static byte[] encrypt(byte[] data, PublicKey key)
    {
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        
        byte[] salted = new byte[data.length + saltLength];
        
        System.arraycopy(salt, 0, salted, 0, saltLength);
        System.arraycopy(data, 0, salted, saltLength, data.length);
        
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(salted);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static String decryptString(byte[] data, PrivateKey key)
    {
        return new String(decrypt(data, key), StandardCharsets.UTF_8);
    }
    public static byte[] decrypt(byte[] data, PrivateKey key)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(data);
            byte[] result = new byte[decrypted.length - saltLength];
            System.arraycopy(decrypted, saltLength, result, 0, result.length);
            return result;
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException(e);
        }
    }
}
