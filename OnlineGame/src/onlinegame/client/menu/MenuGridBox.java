package onlinegame.client.menu;

import onlinegame.client.graphics.Draw;

/**
 *
 * @author Alfred
 */
public final class MenuGridBox extends MenuListBox
{
    private final float cellWidth, cellHeight;
    private int numRows;
    
    private float xPadding, yPadding;
    private boolean shouldReflow = false;
    
    public MenuGridBox(Menu menu, float x, float y, float width, float height, float cellWidth, float cellHeight)
    {
        super(menu, x, y, width, height);
        
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        
        calcNumRows();
    }
    
    private void calcNumRows()
    {
        numRows = (int)((width + .01f) / getCellXSpacing());
    }
    
    public void setXPadding(float xPadding)
    {
        this.xPadding = xPadding;
        shouldReflow = true;
        calcNumRows();
    }
    
    public void setYPadding(float yPadding)
    {
        this.yPadding = yPadding;
        shouldReflow = true;
    }
    
    public float getCellWidth()
    {
        return cellWidth;
    }
    
    public float getCellHeight()
    {
        return cellHeight;
    }
    
    public float getCellXSpacing()
    {
        return cellWidth + xPadding;
    }
    
    public float getCellYSpacing()
    {
        return cellHeight + yPadding;
    }
    
    public int getNumRows()
    {
        return numRows;
    }
    
    @Override
    public void addItem(MenuListItem li)
    {
        int len = listItems.size();
        int xPos = len % numRows;
        int yPos = len / numRows;
        
        li.xOffset = xPos * getCellXSpacing();
        li.yOffset = yPos * getCellYSpacing();
        
        listItems.add(li);
        
        if (isCreated)
        {
            li.create();
        }
    }
    
    @Override
    public void removeItem(MenuListItem li)
    {
        if (isCreated)
        {
            li.destroy();
        }
        int ind = listItems.indexOf(li);
        listItems.remove(ind);
        
        int len = listItems.size();
        int xPos = len % numRows;
        int yPos = len / numRows;
        
        for (int i = ind; i < len; i++)
        {
            MenuListItem lii = listItems.get(i);
            lii.xOffset = xPos * getCellXSpacing() + startX() - x;
            lii.yOffset = yPos * getCellYSpacing() + startY() - y;
            
            xPos++;
            if (xPos >= numRows)
            {
                xPos = 0;
                yPos++;
            }
        }
    }
    
    @Override
    public float startX()
    {
        return x + 4;
    }
    
    @Override
    public float startY()
    {
        return y + 4;
    }
    
    @Override
    public float bottomPadding()
    {
        return 4;
    }
    
    @Override
    public float maxScrollDist()
    {
        return Math.max(bottomPadding() + getContentHeight() - height, 0);
    }
    
    @Override
    public float getContentHeight()
    {
        int len = listItems.size();
        int xPos = len % numRows;
        int yPos = len / numRows;
        if (xPos > 0)
        {
            yPos++;
        }
        
        return yPos * getCellYSpacing();
    }
    
    private void reflow()
    {
        calcNumRows();
        
        int xPos = 0, yPos = 0;
        for (int i = 0; i < listItems.size(); i++)
        {
            MenuListItem lii = listItems.get(i);
            lii.xOffset = xPos * getCellXSpacing() + startX() - x;
            lii.yOffset = yPos * getCellYSpacing() + startY() - y;
            
            xPos++;
            if (xPos >= numRows)
            {
                xPos = 0;
                yPos++;
            }
        }
        
        shouldReflow = false;
    }
    
    @Override
    public void draw()
    {
        if (shouldReflow)
        {
            reflow();
        }
        
        super.draw();
    }
    
    @Override
    protected void drawContent()
    {
        //Draw.mat.get(tempMat);
        
        float startX = startX();
        float startY = startY() - getScrollPos();
        
        float cw = getCellXSpacing();
        float ch = getCellYSpacing();
        int nr = numRows;
        
        int xx = 0;
        int yy = 0;
        for (int i = 0; i < listItems.size(); i++)
        {
            float fx = startX + xx * cw;
            float fy = startY + yy * ch;
            
            Draw.mat.pushMatrix();
            Draw.mat.translate(fx, fy, 0);
            
            MenuListItem li = listItems.get(i);
            
            if (fy < y + height && fy > y - ch)
            {
                li.draw();
            }
            
            xx++;
            if (xx >= nr)
            {
                xx = 0;
                yy++;
            }
            
            Draw.mat.popMatrix();
            //Draw.view.set(tempMat);
        }
    }
}


















