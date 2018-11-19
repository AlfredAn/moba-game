package onlinegame.server.game;

import java.util.ArrayList;
import java.util.List;
import onlinegame.server.MessageBuilder;
import onlinegame.server.account.Session;
import onlinegame.shared.Logger;
import onlinegame.shared.game.GameMap;

/**
 *
 * @author Alfred
 */
public final class GameHandler
{
    private GameHandler() {}
    
    private static final List<GameMain> gameList = new ArrayList<>();
    
    public static GameMain startGame(PlayerInfo[][] players, GameMap map)
    {
        Logger.log("Creating game...");
        
        GameMain game = new GameMain(players, map);
        synchronized (gameList)
        {
            gameList.add(game);
        }
        
        for (int t = 0; t < 2; t++)
        {
            for (int p = 0; p < players[t].length; p++)
            {
                Session s = players[t][p].account.getSession();
                if (s != null)
                {
                    if (s.getLobby() != null)
                    {
                        s.getLobby().removePlayer(s);
                    }
                    s.setGame(game);
                    s.getClient().sendMessage(MessageBuilder.gameStartLoad(t, players));
                    Logger.log("startload sent!");
                }
                else
                {
                    Logger.logError("disconnect during game creation!");
                }
            }
        }
        
        game.start();
        
        return game;
    }
    
    static void stopGame(GameMain game)
    {
        game.stop();
        
        synchronized (gameList)
        {
            gameList.remove(game);
        }
    }
}
