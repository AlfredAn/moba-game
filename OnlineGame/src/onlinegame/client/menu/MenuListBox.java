package onlinegame.client.menu;

import onlinegame.client.Fonts;
import onlinegame.client.Input;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.shared.ArrayDequeList;
import onlinegame.shared.MathUtil;

/**
 *
 * @author Alfred
 */
public class MenuListBox extends MenuItem
{
    final ArrayDequeList<MenuListItem> listItems = new ArrayDequeList<>();
    private float contentHeight = 0, scrollPos = 0;
    
    private boolean showScrollBar = true;
    private boolean showBorder = true;
    
    private MenuListItem selected = null;
    boolean isCreated = false;
    private MenuLabel emptyLabel;
    
    public MenuListBox(Menu menu, float x, float y, float width, float height)
    {
        super(menu, x, y, width, height);
        color.set(.6875f, .6875f, .6875f);
    }
    
    public void setShowScrollBar(boolean showScrollBar)
    {
        this.showScrollBar = showScrollBar;
        
        if (!showScrollBar)
        {
            scrollPos = 0;
        }
    }
    
    public void addItem(MenuListItem li)
    {
        listItems.add(li);
        li.yOffset = contentHeight + startY() - y;
        contentHeight += li.getHeight();
        
        if (isCreated)
        {
            li.create();
        }
    }
    
    public void removeItem(MenuListItem li)
    {
        if (isCreated)
        {
            li.destroy();
        }
        int ind = listItems.indexOf(li);
        listItems.remove(ind);
        
        for (int i = ind; i < listItems.size(); i++)
        {
            listItems.get(i).yOffset -= li.getHeight();
        }
        
        if (listItems.isEmpty())
        {
            contentHeight = 0;
        }
        else
        {
            contentHeight -= li.getHeight();
        }
    }
    
    public final MenuListItem get(int i)
    {
        return listItems.get(i);
    }
    
    public final int size()
    {
        return listItems.size();
    }
    
    @Override
    public void create()
    {
        for (int i = 0; i < listItems.size(); i++)
        {
            listItems.get(i).create();
        }
        
        if (emptyLabel != null)
        {
            emptyLabel.create();
        }
        
        isCreated = true;
    }
    
    @Override
    public void destroy()
    {
        for (int i = 0; i < listItems.size(); i++)
        {
            listItems.get(i).destroy();
        }
        
        if (emptyLabel != null)
        {
            emptyLabel.destroy();
        }
        
        isCreated = false;
    }
    
    public void clear()
    {
        for (int i = 0; i < listItems.size(); i++)
        {
            listItems.get(i).destroy();
        }
        listItems.clear();
        
        selected = null;
        
        contentHeight = 0;
        scrollPos = 0;
    }
    
    public void onPress(MenuItem item)
    {
        if (menu != null)
        {
            menu.onPress(item);
        }
    }
    
    @Override
    public void update()
    {
        super.update();
        
        if (showScrollBar && isMouseOver())
        {
            float scroll = -(float)Input.yScroll();
            scroll(scroll * 40);
        }
        
        boolean selectionChanged = false;
        
        for (int i = 0; i < listItems.size(); i++)
        {
            MenuListItem item = listItems.get(i);
            item.update();
            if (item.wasClicked() && item.isSelectable())
            {
                selected = item;
                selectionChanged = true;
            }
        }
        
        if (selectionChanged)
        {
            selected.onSelect();
        }
    }
    
    public void setEmptyText(String text)
    {
        if (emptyLabel == null)
        {
            emptyLabel = new MenuLabel(null, x + width / 2 - 5, y + height / 2, text, Fonts.lobbyMedium, Align.CENTER);
            emptyLabel.setColor(Color4f.GRAY);
        }
        else
        {
            emptyLabel.setText(text);
        }
    }
    
    public void setSelected(MenuListItem li)
    {
        selected = li;
        if (selected != null)
        {
            selected.onSelect();
        }
    }
    
    public void setShowBorder(boolean showBorder)
    {
        this.showBorder = showBorder;
    }
    
    public void scroll(float dist)
    {
        scrollPos = MathUtil.clamp(scrollPos + dist, 0, maxScrollDist());
    }
    
    public void scrollToBottom()
    {
        scrollPos = maxScrollDist();
    }
    
    public MenuListItem getSelected()
    {
        return selected;
    }
    
    public float maxScrollDist()
    {
        return Math.max(bottomPadding() + contentHeight - height, 0);
    }
    
    public float getScrollPos()
    {
        return scrollPos;
    }
    
    protected float startX()
    {
        return x;
    }
    
    protected float startY()
    {
        return y;
    }
    
    protected float bottomPadding()
    {
        return 0;
    }
    
    protected float getContentHeight()
    {
        return contentHeight;
    }
    
    @Override
    public void draw()
    {
        Draw.color.set(color);
        Draw.fillRect(x, y, width, height);
        
        Draw.setScissor((int)x, (int)y, (int)width - (showScrollBar ? 10 : 0) + 1, (int)height + 1);
        
        if (listItems.isEmpty() && emptyLabel != null)
        {
            emptyLabel.draw();
        }
        
        drawContent();
        
        Draw.resetScissor();
        
        if (showScrollBar)
        {
            float msd = maxScrollDist();

            float scrollWidth = 10;
            float scrollHeight = (int)(Math.min(Math.max(getContentHeight() == 0 ? height : (height * height / getContentHeight()), 15), height) + .5f);

            float scrollX = x + width - scrollWidth;

            float scrollMin = scrollHeight;
            float scrollMax = height;

            float scrollF = msd == 0 ? 0 : scrollPos / msd;

            float scrollY = y + (scrollMax - scrollMin) * scrollF;

            Draw.color.set(.6875f, .6875f, .6875f);
            Draw.fillRect(scrollX, y, scrollWidth, height);

            Draw.color.set(Color4f.GRAY);
            Draw.fillRect(scrollX, scrollY, scrollWidth, scrollHeight);

            Draw.color.set(Color4f.BLACK);
            Draw.drawRect(scrollX, scrollY, scrollWidth, scrollHeight);
            Draw.drawRect(scrollX, y, scrollWidth, height);
        }
        
        //outline
        if (showBorder)
        {
            Draw.color.set(Color4f.BLACK);
            Draw.drawRect(x, y, width, height);
        }
    }
    
    protected void drawContent()
    {
        //tempMat.set(Draw.view);
        
        float xx = startX();
        float yy = startY() - scrollPos;
        for (int i = 0; i < listItems.size(); i++)
        {
            Draw.mat.pushMatrix();
            Draw.mat.translate(xx, yy, 0);
            //Draw.view.translate(xx, yy, 0);
            
            MenuListItem li = listItems.get(i);
            
            float h = li.getHeight();
             
            if (yy < y + height && yy > y - h)
            {
                li.draw();
            }
            
            yy += h;
            
            Draw.mat.popMatrix();
            //Draw.view.set(tempMat);
        }
    }
}
