package onlinegame.client.client.mainmenu;

import java.io.DataInputStream;
import java.io.IOException;
import onlinegame.client.Fonts;
import onlinegame.client.client.MessageBuilder;
import onlinegame.client.graphics.Align;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuButton;
import onlinegame.client.menu.MenuItem;
import onlinegame.client.menu.MenuLabel;
import onlinegame.client.menu.MenuListBox;
import onlinegame.client.menu.MenuListItem;
import onlinegame.client.menu.MenuTextBox;
import onlinegame.client.menu.SubMenu;
import onlinegame.shared.SharedUtil;
import onlinegame.shared.net.InputMessage;
import onlinegame.shared.net.OutputMessage;
import onlinegame.shared.net.Protocol;

/**
 *
 * @author Alfred
 */
public final class LobbySelectMenu extends SubMenu
{
    private final MenuListBox lobbyList;
    final MenuButton joinGameButton;
    private final MenuButton createGameButton;
    
    private final MenuTextBox gameNameBox;
    
    public LobbySelectMenu(Menu superMenu)
    {
        super(superMenu);
        
        itemAlign = Align.TOPLEFT;
        doScale = false;
        
        /////lobbyList
        add(new MenuLabel(this, 405, 130, "Join a game", Fonts.lobbyMedium, Align.CENTER));
        
        for (int i = 0; i < LobbyListItem.AMOUNT; i++)
        {
            add(new MenuLabel(this, 100 + LobbyListItem.getDX(i), 175, LobbyListItem.getName(i), Fonts.lobbySmall, Align.CENTER));
        }
        
        add(lobbyList = new MenuListBox(this, 100, 190, 610, 300));
        lobbyList.setEmptyText("No games available. Create one!");
        
        add(joinGameButton = new MenuButton(this, 280, 510, 250, 50, "Join Game", Fonts.lobbyMedium));
        joinGameButton.setEnabled(false);
        
        ////create game
        add(new MenuLabel(this, 955, 130, "Start a game", Fonts.lobbyMedium, Align.CENTER));
        
        add(new MenuLabel(this, 847, 172, "Name:", Fonts.lobbySmall, Align.RIGHT));
        
        add(gameNameBox = new MenuTextBox(this, 855, 160, 200, 25, Fonts.lobbySmall, 5));
        gameNameBox.setAutoScroll(true);
        gameNameBox.setCharLimit(32);
        gameNameBox.setContent(SharedUtil.possessive(ctx.getNetwork().getUsername()) + " game");
        
        add(createGameButton = new MenuButton(this, 830, 205, 250, 50, "Create Game", Fonts.lobbyMedium));
        
        addListener();
    }
    
    @Override
    public void readMessage(InputMessage msg) throws IOException
    {
        DataInputStream in;
        
        switch (msg.getId())
        {
            case Protocol.S_LOBBY_LIST:
                in = msg.getDataStream();
                
                LobbyListItem lli = (LobbyListItem)lobbyList.getSelected();
                int sel = lli == null ? -1 : lli.id;
                
                joinGameButton.setEnabled(false);
                
                int lobbyCount = in.readShort();
                
                lobbyList.clear();
                for (int i = 0; i < lobbyCount; i++)
                {
                    int id = in.readInt();
                    String name = in.readUTF();
                    String owner = in.readUTF();
                    byte players = in.readByte();
                    byte maxPlayers = in.readByte();
                    String gameMode = in.readUTF();
                    String map = in.readUTF();
                    
                    LobbyListItem li =
                            new LobbyListItem(lobbyList, id, name, owner, players + "/" + maxPlayers, gameMode, map);
                    lobbyList.addItem(li);
                    
                    if (li.id == sel)
                    {
                        lobbyList.setSelected(li);
                    }
                }
                
                break;
        }
    }
    
    @Override
    public void onPress(MenuItem item)
    {
        if (item == createGameButton)
        {
            OutputMessage msg = MessageBuilder.lobbyCreate(gameNameBox.getContent());
            ctx.getNetwork().sendMessage(msg);
            
            alert("Creating game...", null, "createGame");
        }
        else if (item == joinGameButton)
        {
            MenuListItem li = lobbyList.getSelected();
            if (li != null && li instanceof LobbyListItem)
            {
                LobbyListItem lobby = (LobbyListItem)li;
                
                OutputMessage msg = MessageBuilder.lobbyJoin(lobby.id);
                ctx.getNetwork().sendMessage(msg);
            }
        }
    }
    
    @Override
    public void draw()
    {
        super.draw();
        
        //Draw.color.set(Color4f.BLACK);
        //Draw.drawLine(625, 100, 625, 675);
    }
}






















