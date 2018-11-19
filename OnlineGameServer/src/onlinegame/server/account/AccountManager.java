package onlinegame.server.account;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import onlinegame.server.Client;
import onlinegame.shared.Logger;
import onlinegame.shared.account.AccountChecks;

/**
 *
 * @author Alfred
 */
public final class AccountManager
{
    private AccountManager() {}
    
    private static final Map<String, Account> accounts = new HashMap<>();
    
    public static Account get(String username)
    {
        username = AccountChecks.canonicalUsername(username);
        synchronized (accounts)
        {
            Account acc = accounts.get(username);

            if (acc == null)
            {
                try
                {
                    acc = Account.load(username);
                }
                catch (IOException e)
                {
                    Logger.logError("Error loading account " + username + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    return null;
                }
                accounts.put(username, acc);
                
                Logger.log("Account \"" + acc.getUsername() + "\" loaded.");
            }
            
            return acc;
        }
    }
    
    public static Account create(String username, String password) throws IOException
    {
        synchronized (accounts)
        {
            if (accounts.get(username) != null)
            {
                throw new AccountException("Account " + username + " already exists!");
            }
            
            Account acc = Account.create(username, password);
            
            accounts.put(username.toLowerCase(), acc);
            
            Logger.log("Account \"" + username + "\" created!");
            
            return acc;
        }
    }
    
    public static Session createSession(Account account, Client client)
    {
        Session prev = account.session;
        if (prev != null && prev.getClient() != null)
        {
            //multiple logins - disconnect the first
            prev.stop();
        }
        
        Session s = new Session(account, client);
        account.session = s;
        return s;
    }
}
