package onlinegame.shared.account;

import java.util.HashMap;
import java.util.Locale;
import onlinegame.shared.SharedUtil;

/**
 *
 * @author Alfred
 */
public final class AccountChecks
{
    private AccountChecks() {}
    
    public static final int
            USERNAME_MINLENGTH = 4,
            USERNAME_MAXLENGTH = 30,
            
            PASSWORD_MINLENGTH = 6,
            PASSWORD_MAXLENGTH = 45;
    
    private static final String allowedUsernameChars =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZÅ abcdefghijklmnopqrstuvwxyzåàâÇ"
            + "çèÉéÊêëîïÔôœùûĄąĘęÓóĆćŁłŃńŚśŹźŻżÄäÉéÖöÜüßÁáÉéÍíÑñÓóÚúÜüΑαΒβΓγΔδΕ"
            + "εΖζΗηΘθΙιΚκΛλΜμΝνΞξΟοΠπΡρΣσςΤτΥυΦφΧχΨψΩωΆΈΉΊΌΎΏάέήόίύώΪΫϊϋΰΐĂăÂâ"
            + "ÎîȘșŞşȚțŢţÀàÈèÉéÌìÍíÒòÓóÙùÚúÁáĄąÄäÉéĘęĚěÍíÓóÔôÚúŮůÝýČčďťĹĺŇňŔŕŘř"
            + "ŠšŽžÀÁÂÃÇÉÊÍÓÔÕÚàáâãçéêíóôõúАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабв"
            + "гдеёжзийклмнопрстуфхцчшщъыьэю_";
    
    private static final String canonicalChars =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZÅ_abcdefghijklmnopqrstuvwxyzåàâÇ"
            + "çèÉéÊêëîïÔôœùûĄąĘęÓóĆćŁłŃńŚśŹźŻżÄäÉéÖöÜüßÁáÉéÍíÑñÓóÚúÜüAαBβΓγΔδE"
            + "εZζHηΘθIιKκΛλMµNνΞξOoΠπPρΣσςTτYυΦφXχΨψΩωΆΈΉΊΌΎΏάέήόίύώΪΫϊϋΰΐĂăÂâ"
            + "ÎîȘșŞşȚțŢţÀàÈèÉéÌìÍíÒòÓóÙùÚúÁáĄąÄäÉéĘęĚěÍíÓóÔôÚúŮůÝýČčďťĹĺŇňŔŕŘř"
            + "ŠšŽžÀÁÂÃÇÉÊÍÓÔÕÚàáâãçéêíóôõúAБBГДEËЖЗИЙКЛMHOПPCTУФXЦЧШЩЪЫЬЭЮЯaбв"
            + "гдeëжзийклмнoпpcтуфxцчшщъыьэю_";
    
    private static final HashMap<Character, Character> usernameCharMap;
    
    static
    {
        usernameCharMap = new HashMap<>();
        
        for (int i = 0; i < allowedUsernameChars.length(); i++)
        {
            char c1 = allowedUsernameChars.charAt(i);
            char c2 = canonicalChars.charAt(i);
            usernameCharMap.put(c1, c2);
        }
    }
    
    public static String checkLogin(String username, String password)
    {
        return checkLogin(username, password, password);
    }
    public static String checkLogin(String username, String password, String confirmPass)
    {
        String err = checkUsername(username);
        if (err != null)
        {
            return err;
        }
        
        return checkPassword(password, confirmPass);
    }
    
    public static String checkUsername(String username)
    {
        username = trimmedUsername(username);
        
        if (username.length() < USERNAME_MINLENGTH)
        {
            return "Your username must be at least " + USERNAME_MINLENGTH + " characters long.";
        }
        
        if (username.length() > USERNAME_MAXLENGTH)
        {
            return "Your username cannot be more than " + USERNAME_MAXLENGTH + " characters long.";
        }
        
        int errCount = 0;
        int len = username.length();
        for (int i = 0; i < len; i++)
        {
            char c = username.charAt(i);
            if (!usernameCharMap.containsKey(c))
            {
                errCount++;
            }
        }
        
        if (errCount > 0)
        {
            return "Your username contains " + errCount + " disallowed character" + SharedUtil.plural(errCount) + ".";
        }
        
        return null;
    }
    
    public static String trimmedUsername(String username)
    {
        return SharedUtil.removeDuplicateSpaces(username);
    }
    
    public static String canonicalUsername(String username)
    {
        String trimmed = trimmedUsername(username);
        String lowerCase = trimmed.toLowerCase(Locale.ENGLISH);
        String canonical = SharedUtil.replaceChars(lowerCase, usernameCharMap);
        
        return canonical;
    }
    
    /**
     * Checks whether the given password is valid.
     * @param password The password
     * @return {@code null} if the password is valid, or an error message otherwise.
     */
    public static String checkPassword(String password)
    {
        return checkPassword(password, password);
    }
    
    /**
     * Checks whether the given password is valid.
     * @param password The password
     * @param confirm The confirmation of the password
     * @return {@code null} if the password is valid, or an error message otherwise.
     */
    public static String checkPassword(String password, String confirm)
    {
        if (password.length() < PASSWORD_MINLENGTH)
        {
            return "Your password must be at least " + PASSWORD_MINLENGTH + " characters long.";
        }
        
        if (password.length() > PASSWORD_MAXLENGTH)
        {
            return "Your password cannot be more than " + PASSWORD_MAXLENGTH + " characters long.";
        }
        
        if (!password.equals(confirm))
        {
            return "Your passwords must match.";
        }
        
        return null;
    }
}


























