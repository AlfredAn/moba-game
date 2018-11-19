package onlinegame.client.menu;

import java.io.IOException;
import java.util.ArrayList;
import onlinegame.client.Input;
import onlinegame.client.Settings;
import onlinegame.client.client.NetworkListener;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.shared.net.InputMessage;
import org.joml.Matrix4f;
import org.joml.MatrixStack;

/**
 *
 * @author Alfred
 */
public abstract class Menu implements NetworkListener
{
    public final MenuContext ctx;
    
    private final ArrayList<MenuItem> items = new ArrayList<>();
    private final ArrayList<SubMenu> subMenus = new ArrayList<>();
    protected float width, height;
    
    protected MenuLabel title;
    
    protected MenuItem selected;
    protected MenuAlert alert;
    
    private boolean mouseOverElement = false;
    
    protected int itemAlign = Align.TOP;
    protected boolean doScale = true;
    
    public Menu(MenuContext ctx)
    {
        this(ctx, ctx.getWidth(), ctx.getHeight());
    }
    public Menu(MenuContext ctx, float width, float height)
    {
        this.ctx = ctx;
        this.width = width;
        this.height = height;
    }
    
    public void add(MenuItem item)
    {
        items.add(item);
    }
    
    public void add(SubMenu menu)
    {
        subMenus.add(menu);
    }
    
    public boolean contains(SubMenu menu)
    {
        return subMenus.contains(menu);
    }
    
    public void remove(SubMenu menu)
    {
        subMenus.remove(menu);
        menu.destroy();
    }
    
    public void create()
    {
        for (int i = 0; i < items.size(); i++)
        {
            items.get(i).create();
        }
        
        for (int i = 0; i < subMenus.size(); i++)
        {
            subMenus.get(i).create();
        }
        
        if (alert != null)
        {
            alert.create();
        }
    }
    
    protected int getNumItems()
    {
        return items.size();
    }
    
    protected MenuItem getItem(int i)
    {
        return items.get(i);
    }
    
    public void destroy()
    {
        for (int i = 0; i < items.size(); i++)
        {
            items.get(i).destroy();
        }
        
        for (int i = 0; i < subMenus.size(); i++)
        {
            subMenus.get(i).destroy();
        }
        
        if (alert != null)
        {
            alert.destroy();
        }
    }
    
    public void update()
    {
        boolean selectionChanged = false;
        mouseOverElement = false;
        
        for (int i = 0; i < items.size(); i++)
        {
            MenuItem item = items.get(i);
            item.update();
            if (item.wasClicked() && item.isSelectable())
            {
                selected = item;
                selectionChanged = true;
            }
            if (item.isMouseOver())
            {
                mouseOverElement = true;
            }
        }
        
        for (int i = 0; i < subMenus.size(); i++)
        {
            Menu menu = subMenus.get(i);
            
            MenuItem preSelected = menu.getSelected();
            menu.update();
            
            if (menu.getSelected() != preSelected && menu.getSelected() != null)
            {
                selected = menu.getSelected();
                selectionChanged = true;
            }
            else if (selectionChanged)
            {
                menu.selected = null;
            }
        }
        
        if (alert != null)
        {
            alert.update();
        }
        
        if (selectionChanged && selected != null)
        {
            selected.onSelect();
        }
        
        if (Input.keyReleased(Input.KEY_LEFTCLICK) && isMouseOver() && !selectionChanged)
        {
            selected = null;
        }
    }
    
    public boolean isMouseOverElement()
    {
        return mouseOverElement;
    }
    
    public boolean isMouseOver()
    {
        Draw.mat.pushMatrix();
        Matrix4f tempMat = Draw.mat.getDirect();
        tempMat.identity();
        transform(tempMat);
        tempMat.invert();
        
        boolean result = Input.mouseInArea(tempMat, 0, 0, getWidth(), getHeight());
        
        Draw.mat.popMatrix();
        
        return result;
    }
    
    public final boolean hasSelected()
    {
        return selected != null;
    }
    
    protected final MenuItem getSelected()
    {
        return selected;
    }
    
    public void onPress(MenuItem item) {}
    public void onTextEntry(MenuTextBox textBox, String text) {}
    
    protected final void addListener()
    {
        ctx.getNetwork().addListener(this);
    }
    
    @Override
    public void readMessage(InputMessage msg) throws IOException {}
    
    @Override
    public boolean isActive()
    {
        return ctx.getMenu() == this;
    }
    
    public final float getXOrigin(int align)
    {
        float a = Align.getXAlign(align);
        return a * width;
    }
    
    public final float getYOrigin(int align)
    {
        float a = Align.getInvYAlign(align);
        return a * height;
    }
    
    public final void transform(MatrixStack mat, int align)
    {
        transform(mat.getDirect(), align);
    }
    
    public void transform(Matrix4f mat, int align)
    {
        mat.translate(getXOrigin(align), getYOrigin(align), 0);
        if (doScale)
        {
            mat.scale(getMenuScale());
        }
    }
    
    /*@Deprecated
    public final void getMatrix(Matrix4f dest, int align)
    {
        dest.translation(getXOrigin(align), getYOrigin(align), 0);
        if (doScale)
        {
            dest.scale(getMenuScale());
        }
    }*/
    
    /*@Deprecated
    public final void getInverseMatrix(Matrix4f dest, int align)
    {
        getMatrix(dest, align);
        dest.invert();
        /*if (!doScale)
        {
            dest.translation(-getXOrigin(align), -getYOrigin(align), 0);
            return;
        }
        
        dest.scaling(1f / getMenuScale());
        dest.translate(-getXOrigin(align), -getYOrigin(align), 0);*/
    //}*/
    
    public final void transform(MatrixStack dest)
    {
        transform(dest.getDirect());
    }
    
    public final void transform(Matrix4f dest)
    {
        /*if (this instanceof MenuAlert)
        {
            Matrix4f temp = new Matrix4f();
            getMatrix(temp);
            dest.mul(temp);
            return;
        }*/
        
        transform(dest, itemAlign);
    }
    
    /*@Deprecated
    public void getMatrix(Matrix4f dest)
    {
        getMatrix(dest, itemAlign);
    }*/
    
    /*@Deprecated
    public void getInverseMatrix(Matrix4f dest)
    {
        getInverseMatrix(dest, itemAlign);
    }*/
    
    public String getAlertTag()
    {
        return alert == null ? null : alert.getTag();
    }
    
    public boolean isAlertOpen(String tag)
    {
        String t2 = getAlertTag();
        return t2 == null ? false : t2.equals(tag);
    }
    
    public final void closeAlert()
    {
        closeAlert(true);
    }
    private void closeAlert(boolean fireEvent)
    {
        if (alert == null)
        {
            return;
        }
        
        String tag = alert.getTag();
        alert.destroy();
        alert = null;
        
        if (tag != null && fireEvent)
        {
            onCloseAlert(tag);
        }
    }
    
    protected void onCloseAlert(String tag) {}
    
    public void draw()
    {
        Draw.mat.pushMatrix();
        transform(Draw.mat);
        
        if (title != null)
        {
            Draw.color.set(Color4f.MEDIUM_GRAY);

            float w = getScaledWidth();
            Draw.fillRect(-w/2, 0, w, title.getHeight() + 70);

            Draw.color.set(Color4f.BLACK);
            Draw.drawLine(-w/2, title.getHeight() + 70, w, title.getHeight() + 70);
        }
        
        for (int i = 0; i < items.size(); i++)
        {
            MenuItem mi = items.get(i);
            mi.draw();
        }
        
        Draw.mat.popMatrix();
        
        for (int i = 0; i < subMenus.size(); i++)
        {
            Menu m = subMenus.get(i);
            m.draw();
        }
        
        if (alert != null)
        {
            Draw.mat.pushMatrix();
            transform(Draw.mat);
            Draw.color.set(0f, 0f, 0f, .25f);
            if (itemAlign == Align.TOPLEFT)
            {
                Draw.fillRect(0, 0, width, height);
            }
            else
            {
                Draw.fillRect(-getScaledWidth()/2, 0, getScaledWidth(), getScaledHeight());
            }
            Draw.mat.popMatrix();
            
            alert.draw();
        }
    }
    
    public final void alert(String message)
    {
        alert(new MenuAlert(this, message, "OK"));
    }
    
    public final void alert(String message, String buttonText)
    {
        alert(new MenuAlert(this, message, buttonText));
    }
    
    public final void alert(String message, String buttonText, String tag)
    {
        alert(new MenuAlert(this, message, buttonText, tag));
    }
    
    public void alert(MenuAlert alert)
    {
        if (this.alert != null)
        {
            closeAlert(false);
        }
        this.alert = alert;
        selected = null;
        alert.create();
    }
    
    public boolean hasAlert()
    {
        return alert != null;
    }
    
    public void setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        for (int i = 0; i < items.size(); i++)
        {
            items.get(i).onMenuResize();
        }
    }
    
    public float getWidth()
    {
        return width;
    }
    
    public float getHeight()
    {
        return height;
    }
    
    public float getMenuScale()
    {
        float scale = Settings.getCurrent().getf(Settings.MENU_SCALING);
        
        if (scale != 0)
        {
            return scale;
        }
        
        float xScale = width / 1920;
        float yScale = height / 1080;
        
        return Math.min(xScale, yScale);
    }
    
    public float getScaledWidth()
    {
        return width / getMenuScale();
    }
    
    public float getScaledHeight()
    {
        return height / getMenuScale();
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        destroy();
        
        super.finalize();
    }
}

































