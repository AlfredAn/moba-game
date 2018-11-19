package onlinegame.client.menu;

import onlinegame.client.Fonts.FontContainer;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.text.TextModel;

/**
 *
 * @author Alfred
 */
public final class MenuLabel extends MenuItem
{
    private TextModel tm;
    private final FontContainer font;
    private String text;
    private final int align;
    private final float originX, originY, scale;
    private boolean gridAlign = true;
    
    /*public MenuLabel(Menu menu, String text, FontContainer font)
    {
        this(menu, text, font, TextAlign.CENTER);
    }
    public MenuLabel(Menu menu, String text, FontContainer font, int align)
    {
        this(
                menu,
                menu.getWidth() * TextAlign.getXAlign(align),
                menu.getHeight() * TextAlign.getInvYAlign(align),
                text, font, align);
    }*/
    public MenuLabel(Menu menu, float x, float y, String text, FontContainer font)
    {
        this(menu, x, y, 1, text, font, Align.CENTER);
    }
    public MenuLabel(Menu menu, float x, float y, String text, FontContainer font, int align)
    {
        this(menu, x, y, 1, text, font, align);
    }
    public MenuLabel(Menu menu, float x, float y, float scale, String text, FontContainer font)
    {
        this(menu, x, y, scale, text, font, Align.CENTER);
    }
    public MenuLabel(Menu menu, float x, float y, float scale, String text, FontContainer font, int align)
    {
        super(menu,
                x - TextModel.getStringWidth(font.get(), text) * Align.getXAlign(align) * scale,
                y - TextModel.getStringHeight(font.get(), text) * Align.getInvYAlign(align) * scale,
                TextModel.getStringWidth(font.get(), text) * scale,
                TextModel.getStringHeight(font.get(), text) * scale);
        
        this.font = font;
        this.text = text;
        this.align = align;
        this.scale = scale;
        
        originX = x;
        originY = y;
    }
    
    @Override
    public void create()
    {
        tm = new TextModel(font.get());
        tm.setTextAlign(Align.TOPLEFT);
        tm.setYInvert(true);
        //btm.setGridAlign(true);
        tm.addString(text, 0, 0);
        tm.setGridAlign(gridAlign);
        tm.render();
        
        //width = tm.getStringWidth();
        //height = tm.getStringHeight();
        
        //x -= width * TextAlign.getXAlign(align);
        //y -= height * TextAlign.getInvYAlign(align);
    }
    
    @Override
    public void destroy()
    {
        tm.destroy();
    }
    
    @Override
    public void draw()
    {
        Draw.mat.pushMatrix();
        Draw.mat.scale(scale);
        
        Draw.color.set(color);
        
        if (gridAlign)
        {
            Draw.drawText(tm, (int)(x / scale + .5f), (int)(y / scale + .5f));
        }
        else
        {
            Draw.drawText(tm, x / scale, y / scale);
        }
        
        Draw.mat.popMatrix();
    }
    
    public void setText(String text)
    {
        if (text.equals(this.text))
        {
            return;
        }
        
        this.text = text;
        
        if (tm == null)
        {
            return;
        }
        
        tm.clear();
        tm.addString(text, 0, 0);
        tm.render();
        
        width = tm.getStringWidth();
        height = tm.getStringHeight();
        
        x = originX - width * Align.getXAlign(align);
        y = originY - height * Align.getInvYAlign(align);
    }
    
    public void setGridAlign(boolean gridAlign)
    {
        this.gridAlign = gridAlign;
    }
    
    public boolean getGridAlign()
    {
        return gridAlign;
    }
    
    public String getText()
    {
        return text;
    }
    
    public FontContainer getFont()
    {
        return font;
    }
}



















