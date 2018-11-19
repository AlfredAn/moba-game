package onlinegame.client.menu;

import onlinegame.client.Fonts.FontContainer;
import onlinegame.client.ClientUtil;
import onlinegame.client.Input;
import onlinegame.client.Timing;
import onlinegame.client.graphics.Align;
import onlinegame.client.graphics.Color4f;
import onlinegame.client.graphics.Draw;
import onlinegame.client.graphics.text.BitmapFont;
import onlinegame.client.graphics.text.TextModel;
import onlinegame.shared.SharedUtil;
import org.joml.Matrix4f;

/**
 *
 * @author Alfred
 */
public final class MenuTextBox extends MenuItem
{
    private String content = "";
    private final MenuLabel label;
    
    private int charLimit = -1;
    private long selectionTimer = -1;
    private boolean showFlashingLine = false;
    
    private final float leftPadding;
    
    private float autoScrollAmount = 0;
    private float scrollPos = 0;
    
    private boolean
            hidden = false,
            allowPaste = true,
            clearOnEnter = false;
    
    public MenuTextBox(Menu menu, float x, float y, float width, float height, FontContainer font)
    {
        this(menu, x, y, width, height, font, 20);
    }
    public MenuTextBox(Menu menu, float x, float y, float width, float height, FontContainer font, float leftPadding)
    {
        super(menu, x, y, width, height);
        color.set(Color4f.CYAN);
        
        label = new MenuLabel(null, x + leftPadding, y + height/2, "", font, Align.LEFT);
        this.leftPadding = leftPadding;
    }
    
    public void setContent(String content)
    {
        this.content = content;
        
        String labelText;
        
        if (hidden)
        {
            labelText = SharedUtil.repeatChar('•', content.length());
        }
        else
        {
            labelText = content;
        }
        label.setText(labelText);
    }
    
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
        setContent(content);
    }
    
    public void setCharLimit(int charLimit)
    {
        this.charLimit = charLimit;
    }
    
    public void setAllowPaste(boolean allowPaste)
    {
        this.allowPaste = allowPaste;
    }
    
    public void setClearOnEnter(boolean clearOnEnter)
    {
        this.clearOnEnter = clearOnEnter;
    }
    
    public void setAutoScroll(boolean autoScroll)
    {
        setAutoScrollAmount(autoScroll ? (width - leftPadding * 2) / 4 : 0);
    }
    
    public void setAutoScrollAmount(float autoScrollAmount)
    {
        this.autoScrollAmount = autoScrollAmount;
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
    public boolean isSelectable()
    {
        return true;
    }
    
    public void setTextColor(Color4f color)
    {
        label.setColor(color);
    }
    
    @Override
    public void update()
    {
        super.update();
        
        boolean changed = false;
        boolean prevShowLine = showFlashingLine;
        showFlashingLine = false;
        
        if (isSelected())
        {
            String keyString = Input.keyboardString();
            String clipboard = (allowPaste && (Input.keyPressed(Input.KEY_PASTE) || Input.keyRepeated(Input.KEY_PASTE))) ? ClientUtil.getClipboardContents() : "";
            
            content += (keyString + clipboard).replace('\n', ' ');
            
            if (charLimit != -1 && content.length() > charLimit)
            {
                content = content.substring(0, charLimit);
            }
            
            BitmapFont font = label.getFont().get();
            
            if (!keyString.isEmpty() || !clipboard.isEmpty())
            {
                if (charLimit == -1 || autoScrollAmount > 0)
                {
                    float sw = TextModel.getStringWidth(font, content);
                    float w = width - leftPadding * 2;
                    float maxTextWidth = w + scrollPos;
                    
                    if (autoScrollAmount > 0)
                    {
                        if (sw > maxTextWidth)
                        {
                            while (sw > maxTextWidth)
                            {
                                scrollPos += autoScrollAmount;
                                maxTextWidth += autoScrollAmount;
                            }
                        }
                    }
                    else
                    {
                        while (sw > maxTextWidth)
                        {
                            content = content.substring(0, content.length() - 1);
                            sw = TextModel.getStringWidth(font, content);
                        }
                    }
                }
                
                selectionTimer = 0;
                
                changed = true;
            }

            if ((Input.keyRepeated(Input.KEY_ERASE) || Input.keyPressed(Input.KEY_ERASE)) && content.length() > 0)
            {
                content = content.substring(0, content.length() - 1);
                changed = true;
                selectionTimer = 0;
                
                if (autoScrollAmount > 0)
                {
                    float sw = TextModel.getStringWidth(font, content);
                    float w = width - leftPadding * 2;
                    
                    if (sw <= w)
                    {
                        scrollPos = 0;
                    }
                    else if (sw <= scrollPos + autoScrollAmount / 2)
                    {
                        scrollPos = Math.max(0, sw - w + autoScrollAmount);
                    }
                }
            }
            
            if (clearOnEnter && Input.keyPressed(Input.KEY_ENTER) && content.length() > 0)
            {
                if (menu != null)
                {
                    menu.onTextEntry(this, content);
                }
                content = "";
                changed = true;
                selectionTimer = 0;
                scrollPos = 0;
            }
            
            selectionTimer += Timing.getDeltaNanos();
            
            showFlashingLine = (selectionTimer % 1_000_000_000L) < 500_000_000L;
        }
        else
        {
            selectionTimer = -1;
        }
        
        if (showFlashingLine != prevShowLine)
        {
            changed = true;
        }
        
        if (changed)
        {
            String labelText;
            
            if (hidden)
            {
                labelText = SharedUtil.repeatChar('•', content.length());
            }
            else
            {
                labelText = content;
            }
            
            label.setText(labelText + (showFlashingLine ? "|" : ""));
        }
    }
    
    @Override
    protected void onSelect()
    {
        selectionTimer = 0;
    }
    
    public String getContent()
    {
        return content;
    }
    
    private final Matrix4f tempMat = new Matrix4f();
    
    @Override
    public void draw()
    {
        Draw.color.set(color);
        Draw.fillRect(x, y, width, height);
        
        if (scrollPos != 0)
        {
            //tempMat.set(Draw.view);
            Draw.mat.pushMatrix();
            //Draw.view.translate(-scrollPos, 0, 0);
            Draw.mat.translate(-scrollPos, 0, 0);
        }
        
        if (autoScrollAmount > 0)
        {
            Draw.setScissor((int)x, (int)y, (int)width, (int)height);
        }
        
        label.draw();
        
        if (autoScrollAmount > 0)
        {
            Draw.resetScissor();
        }
        
        if (scrollPos != 0)
        {
            Draw.mat.popMatrix();
            //Draw.view.set(tempMat);
        }
        
        switch (getMouseState())
        {
            case M_HOVER:
                Draw.color.set(1f, 1f, 1f, .25f);
                Draw.fillRect(x, y, width, height);
                break;
            case M_PRESS:
                Draw.color.set(0f, 0f, 0f, .0675f);
                Draw.fillRect(x, y, width, height);
                break;
        }
        
        Draw.color.set(Color4f.BLACK);
        Draw.drawRect(x, y, width, height);
    }
}
