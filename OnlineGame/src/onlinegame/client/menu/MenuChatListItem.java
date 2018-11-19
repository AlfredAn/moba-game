package onlinegame.client.menu;

import onlinegame.client.Fonts;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.text.TextModel;

/**
 *
 * @author Alfred
 */
final class MenuChatListItem extends MenuListItem
{
    private static final float
            HPADDING = 5,
            VPADDING = 8;
    
    private final MenuLabel label;
    
    MenuChatListItem(MenuChatBox chatBox, String msg)
    {
        super(chatBox, TextModel.getStringHeight(Fonts.lobbySmall.get(), msg) + VPADDING);
        
        label = new MenuLabel(null, HPADDING, VPADDING / 2, msg, Fonts.lobbySmall, Align.TOPLEFT);
    }
    
    @Override
    public void create()
    {
        label.create();
    }
    
    @Override
    public void destroy()
    {
        label.destroy();
    }
    
    @Override
    public void draw()
    {
        label.draw();
    }
}
