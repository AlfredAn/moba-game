package onlinegame.client.client.mainmenu;

import java.io.DataInputStream;
import java.io.IOException;
import onlinegame.client.Fonts;
import onlinegame.client.Timing;
import onlinegame.client.client.MessageBuilder;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuButton;
import onlinegame.client.menu.MenuChatModule;
import onlinegame.client.menu.MenuGridBox;
import onlinegame.client.menu.MenuItem;
import onlinegame.client.menu.MenuLabel;
import onlinegame.client.menu.MenuListBox;
import onlinegame.client.menu.SubMenu;
import onlinegame.shared.Logger;
import onlinegame.shared.db.ChampionDB;
import onlinegame.shared.db.ChampionDB.ChampionData;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class ChampionSelectMenu extends SubMenu
{
    private long timeLeft = -1;
    
    private final int yourTeamId;
    private final String[][] names;
    
    private final MenuLabel infoText, timerText;
    
    private final MenuListBox yourTeam;
    private final MenuListBox enemyTeam;
    
    private final MenuGridBox championGrid;
    
    private final MenuButton lockButton;
    
    public ChampionSelectMenu(Menu superMenu, int yourTeamId, String[][] names)
    {
        super(superMenu);
        
        itemAlign = Align.TOPLEFT;
        doScale = false;
        
        this.yourTeamId = yourTeamId;
        this.names = names;
        
        add(infoText = new MenuLabel(this, 600, 50, "<unknown>", Fonts.lobbyMedium));
        add(timerText = new MenuLabel(this, 292, 50, "", Fonts.lobbyMedium, Align.LEFT));
        
        add(new MenuLabel(this, 141, 50, "Your Team", Fonts.lobbyMedium));
        
        add(yourTeam = new MenuListBox(this, 10, 80, 262, 400));
        yourTeam.setShowScrollBar(false);
        yourTeam.setShowBorder(false);
        yourTeam.setColor(Color4f.TRANSPARENT);
        
        for (int i = 0; i < names[0].length; i++)
        {
            yourTeam.addItem(new ChampSelectTeamListItem(yourTeam, names[0][i], 0, yourTeamId));
        }
        
        add(new MenuLabel(this, 1059, 50, "Enemy Team", Fonts.lobbyMedium));
        
        add(enemyTeam = new MenuListBox(this, 928, 80, 262, 400));
        enemyTeam.setShowScrollBar(false);
        enemyTeam.setShowBorder(false);
        enemyTeam.setColor(Color4f.TRANSPARENT);
        
        for (int i = 0; i < names[1].length; i++)
        {
            enemyTeam.addItem(new ChampSelectTeamListItem(enemyTeam, names[1][i], 0, 1-yourTeamId));
        }
        
        add(championGrid = new MenuGridBox(this, 282, 80, 635, 240, 66, 66));
        championGrid.setXPadding(3);
        championGrid.setYPadding(3);
        
        add(lockButton = new MenuButton(this, 475, 330, 250, 50, "Lock In", Fonts.lobbyMedium));
        lockButton.setEnabled(false);
        
        for (int i = 0; i < ChampionDB.count(); i++)
        {
            championGrid.addItem(new ChampSelectGridItem(championGrid, ChampionDB.get(i)));
        }
        
        add(new MenuChatModule(this, 282, 390, 635, 225, "Team Chat", "championselect"));
        
        addListener();
    }
    
    @Override
    public void onPress(MenuItem item)
    {
        if (item == lockButton)
        {
            ctx.getNetwork().sendMessage(MessageBuilder.champSelectLock());
        }
        else if (item instanceof ChampSelectGridItem)
        {
            ChampSelectGridItem champItem = (ChampSelectGridItem)item;
            ChampionData champ = champItem.champion;
            
            OutputMessage msg = MessageBuilder.champSelectSelect(champ);
            ctx.getNetwork().sendMessage(msg);
        }
    }
    
    @Override
    public void update()
    {
        super.update();
        
        int preSeconds = (int)(timeLeft / 1_000_000_000L);
        if (timeLeft != -1)
        {
            timeLeft = Math.max(timeLeft - Timing.getDeltaNanos(), 0);
        }
        int seconds = (int)(timeLeft / 1_000_000_000L);
        
        if (seconds != preSeconds)
        {
            timerText.setText(Integer.toString(seconds));
        }
    }
    
    @Override
    public void readMessage(InputMessage msg) throws IOException
    {
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.S_CHAMPSELECT_UPDATE:
                in = msg.getDataStream();
                
                infoText.setText(in.readUTF());
                int tt = in.readInt();
                timeLeft = tt == -1 ? -1 : (long)tt * 1_000_000L;
                
                timerText.setText(Integer.toString((int)(timeLeft / 1_000_000_000L)));
                
                String myName = ctx.getNetwork().getUsername();
                int myState = -1;
                ChampionData myChamp = null;
                
                for (int t = 0; t < 2; t++)
                {
                    MenuListBox list = t == 0 ? yourTeam : enemyTeam;
                    for (int i = 0; i < names[t].length; i++)
                    {
                        byte state = in.readByte();
                        short champId = in.readShort();
                        ChampionData champ = champId == -1 ? null : ChampionDB.get(champId);
                        
                        ChampSelectTeamListItem li = (ChampSelectTeamListItem)(list.get(i));
                        
                        li.setState(state);
                        li.setChampion(champ);
                        
                        if (names[t][i].equals(myName))
                        {
                            myState = state;
                            myChamp = champ;
                        }
                    }
                }
                
                if (myState == -1)
                {
                    Logger.logError("Self not found in player list.");
                }
                
                lockButton.setEnabled(myState == 1 && myChamp != null);
                break;
        }
    }
}













