package onlinegame.client.menu;

import onlinegame.client.Fonts;
import onlinegame.client.client.ChatListener;
import onlinegame.client.client.ChatRoom;
import onlinegame.client.graphics.text.TextModel;

/**
 *
 * @author Alfred
 */
public final class MenuChatBox extends MenuListBox implements ChatListener
{
    private ChatRoom chatRoom;
    private final String roomTag;
    
    public MenuChatBox(Menu menu, float x, float y, float width, float height)
    {
        this(menu, x, y, width, height, null);
    }
    public MenuChatBox(Menu menu, float x, float y, float width, float height, String roomTag)
    {
        super(menu, x, y, width, height);
        color.set(.875f, .875f, .875f);
        
        this.roomTag = roomTag;
    }
    
    @Override
    public void update()
    {
        super.update();
        
        if (chatRoom == null && roomTag != null)
        {
            ChatRoom room = menu.ctx.getNetwork().getChatRoom(roomTag);
            if (room != null)
            {
                setRoom(room);
            }
        }
    }
    
    @Override
    public void readChatMessage(String msg)
    {
        boolean scrollDown = getScrollPos() == maxScrollDist();
        
        if (listItems.size() == chatRoom.capacity())
        {
            MenuListItem l = listItems.peek();
            removeItem(l);
            scroll(-l.getHeight());
        }
        
        String displayStr = TextModel.addLineBreaks(Fonts.lobbySmall.get(), msg, width - 20);
        addItem(new MenuChatListItem(this, displayStr));
        
        if (scrollDown)
        {
            scrollToBottom();
        }
    }
    
    @Override
    public boolean isCLActive()
    {
        return true;
    }
    
    @Override
    protected float startY()
    {
        return y + 2;
    }
    
    @Override
    protected float bottomPadding()
    {
        return 4;
    }
    
    public void setRoom(ChatRoom room)
    {
        chatRoom = room;
        
        destroy();
        listItems.clear();
        create();
        
        room.addListener(this);
    }
    
    public ChatRoom getRoom()
    {
        return chatRoom;
    }
}
