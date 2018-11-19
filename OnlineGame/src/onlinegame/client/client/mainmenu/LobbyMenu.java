package onlinegame.client.client.mainmenu;

import java.io.DataInputStream;
import java.io.IOException;
import onlinegame.client.ClientGameUtil;
import onlinegame.client.Fonts;
import onlinegame.client.client.MessageBuilder;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.IColor4f;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuBox;
import onlinegame.client.menu.MenuButton;
import onlinegame.client.menu.MenuChatModule;
import onlinegame.client.menu.MenuItem;
import onlinegame.client.menu.MenuLabel;
import onlinegame.client.menu.MenuListBox;
import onlinegame.client.menu.SubMenu;
import onlinegame.shared.GameUtil;
import onlinegame.shared.net.GameProtocolException;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class LobbyMenu extends SubMenu
{
    private final MenuListBox[] teamList = new MenuListBox[2];
    
    private final MenuButton startGameButton, switchTeamButton, leaveLobbyButton;
    //private final MenuChatBox chatBox;
    //private final MenuTextBox chatEntryBox;
    
    private final MenuLabel nameLabel;
    
    private String lobbyName = "<unknown>", lobbyOwner = "<unknown>", lobbyGameMode = "<unknown>", lobbyMap = "<unknown>";
    private int lobbyPlayers = 0, lobbyMaxPlayers = 0;
    
    private final MenuLabel settingsLabel;
    
    public LobbyMenu(Menu superMenu)
    {
        super(superMenu);
        
        itemAlign = Align.TOPLEFT;
        doScale = false;
        
        //lobby title
        add(nameLabel = new MenuLabel(this, 600, 130, lobbyName, Fonts.lobbyMedium));
        
        //player lists
        for (int i = 0; i < 2; i++)
        {
            MenuBox box;
            add(box = new MenuBox(this, 10 + i*440, 160, 440, 40, GameUtil.getTeamName(i), Fonts.lobbyMedium));
            box.setColor(ClientGameUtil.getTeamColor(i));
            
            add(teamList[i] = new MenuListBox(this, 10 + i*440, 200, 440, 150));
            teamList[i].setShowScrollBar(false);
        }
        
        add(startGameButton = new MenuButton(this, 910, 160, 280, 50, "Start Game", Fonts.lobbyMedium));
        add(switchTeamButton = new MenuButton(this, 910, 230, 280, 50, "Switch Team", Fonts.lobbyMedium));
        add(leaveLobbyButton = new MenuButton(this, 910, 300, 280, 50, "Leave Lobby", Fonts.lobbyMedium));
        
        //settings menu
        MenuBox box;
        add(box = new MenuBox(this, 750, 360, 300, 135, "", Fonts.lobbySmall));
        box.setColor(new IColor4f(.6875f, .6875f, .6875f));
        add(new MenuLabel(this, 900, 380, "Game Settings", Fonts.lobbyMedium, Align.CENTER));
        add(settingsLabel = new MenuLabel(this, 760, 410, getSettingsText(), Fonts.lobbySmall, Align.TOPLEFT));
        
        //lobby chat
        add(new MenuChatModule(this, 10, 360, 590, 255, "Lobby Chat", "lobby"));
        
        addListener();
    }
    
    @Override
    public void readMessage(InputMessage msg) throws IOException
    {
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.S_LOBBY_UPDATE:
                teamList[0].clear();
                teamList[1].clear();
                
                in = msg.getDataStream();
                
                lobbyName = in.readUTF();
                lobbyOwner = in.readUTF();
                lobbyPlayers = in.readByte();
                lobbyMaxPlayers = in.readByte();
                lobbyGameMode = in.readUTF();
                lobbyMap = in.readUTF();
                
                nameLabel.setText(lobbyName);
                settingsLabel.setText(getSettingsText());
                
                String myName = ctx.getNetwork().getUsername();
                startGameButton.setEnabled(lobbyOwner.equals(myName));
                int myTeam = -1;
                
                for (int i = 0; i < lobbyPlayers; i++)
                {
                    int team = in.readByte();
                    String username = in.readUTF();
                    
                    if (team < 0 || team >= 2)
                    {
                        throw new GameProtocolException("Invalid team id: " + team);
                    }
                    
                    teamList[team].addItem(new TeamListItem(teamList[team], username, team, username.equals(lobbyOwner)));
                    
                    if (username.equals(myName))
                    {
                        myTeam = team;
                    }
                }
                
                switchTeamButton.setEnabled(myTeam != -1 && teamList[1-myTeam].size() < lobbyMaxPlayers / 2);
                
                break;
        }
    }
    
    @Override
    public void onPress(MenuItem item)
    {
        if (item == switchTeamButton)
        {
            OutputMessage msg = MessageBuilder.lobbySwitchTeam();
            ctx.getNetwork().sendMessage(msg);
        }
        else if (item == leaveLobbyButton)
        {
            OutputMessage msg = MessageBuilder.lobbyLeave();
            ctx.getNetwork().sendMessage(msg);
        }
        else if (item == startGameButton)
        {
            OutputMessage msg = MessageBuilder.lobbyStartChampSelect();
            ctx.getNetwork().sendMessage(msg);
        }
    }
    
    /*@Override
    public void onTextEntry(MenuTextBox textBox, String text)
    {
        if (textBox == chatEntryBox)
        {
            chatBox.getRoom().sendMessage(text);
        }
    }*/
    
    private String getSettingsText()
    {
        StringBuilder sb = new StringBuilder(200);
        
        sb.append("Name: ").append(lobbyName);
        sb.append("\nOwner: ").append(lobbyOwner);
        sb.append("\nPlayers: ").append(lobbyPlayers).append("/").append(lobbyMaxPlayers);
        sb.append("\nGame Mode: ").append(lobbyGameMode);
        sb.append("\nMap: ").append(lobbyMap);
        
        return sb.toString();
    }
}
