package onlinegame.client.menu;

import onlinegame.client.Fonts;
import onlinegame.client.client.ChatRoom;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;

/**
 *
 * @author Alfred
 */
public final class MenuChatModule extends SubMenu
{
    private final MenuChatBox chatBox;
    private final MenuTextBox chatEntryBox;
    
    public MenuChatModule(Menu superMenu, float x, float y, float width, float height, String title, String tag)
    {
        super(superMenu);
        
        itemAlign = Align.TOPLEFT;
        doScale = false;
        
        MenuBox box;
        add(box = new MenuBox(this, x, y, width, 40, title, Fonts.lobbyMedium));
        box.setColor(Color4f.MEDIUM_GRAY);
        
        add(chatBox = new MenuChatBox(this, x, y + 40, width, height - 65, tag));
        
        add(chatEntryBox = new MenuTextBox(this, x, y + height - 25, width, 25, Fonts.lobbySmall, 5));
        chatEntryBox.setClearOnEnter(true);
        chatEntryBox.setAutoScroll(true);
        chatEntryBox.setCharLimit(1024);
    }
    
    @Override
    public void onTextEntry(MenuTextBox textBox, String text)
    {
        if (textBox == chatEntryBox)
        {
            ChatRoom room = chatBox.getRoom();
            if (room != null)
            {
                room.sendMessage(text);
            }
        }
    }
}
