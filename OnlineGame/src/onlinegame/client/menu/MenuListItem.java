package onlinegame.client.menu;

/**
 *
 * @author Alfred
 */
public abstract class MenuListItem extends MenuItem
{
    public final MenuListBox list;
    float xOffset, yOffset;
    
    public MenuListItem(MenuListBox list, float height)
    {
        super(null, 0, 0, list.width, height);
        this.list = list;
    }
    
    MenuListItem(MenuListBox list, float width, float height)
    {
        super(null, 0, 0, width, height);
        this.list = list;
    }
    
    @Override
    public void update()
    {
        super.update();
        
        if (wasClicked())
        {
            if (list != null)
            {
                list.onPress(this);
            }
        }
    }
    
    @Override
    public boolean isSelected()
    {
        return list.getSelected() == this;
    }
    
    @Override
    public boolean isClickable()
    {
        return list.isMouseOver();
    }
    
    @Override
    public float getX()
    {
        return super.getX() + xOffset + list.getX();
    }
    
    @Override
    public float getY()
    {
        return super.getY() + yOffset + list.getY() - list.getScrollPos();
    }
}
