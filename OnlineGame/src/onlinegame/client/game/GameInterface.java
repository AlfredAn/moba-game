package onlinegame.client.game;

import onlinegame.client.Input;
import onlinegame.client.client.Network;
import onlinegame.client.menu.Menu;
import onlinegame.client.menu.MenuContext;

/**
 *
 * @author Alfred
 */
final class GameInterface implements MenuContext
{
    final Game game;
    
    private final GameMenu menu;
    private boolean holdingScrollMap = false, holdingScrollMapOnUI = false, holdingMove = false, holdingMoveOnUI = false;
    
    GameInterface(Game game)
    {
        this.game = game;
        
        menu = new GameMenu(this);
    }
    
    boolean isHovered()
    {
        return menu.isMouseOverElement();
    }
    
    boolean isSelected()
    {
        return menu.hasSelected();
    }
    
    void create()
    {
        menu.create();
    }
    
    void destroy()
    {
        menu.destroy();
    }
    
    void update()
    {
        if (holdingScrollMap)
        {
            if (!Input.keyDown(Input.KEY_SCROLL_MINIMAP) || Input.keyReleased(Input.KEY_SCROLL_MINIMAP))
            {
                holdingScrollMap = false;
            }
        }
        else if (Input.keyPressed(Input.KEY_SCROLL_MINIMAP))
        {
            holdingScrollMap = true;
            holdingScrollMapOnUI = isHovered();
        }
        
        if (holdingMove)
        {
            if (!Input.keyDown(Input.KEY_MOVE) || Input.keyReleased(Input.KEY_MOVE))
            {
                holdingMove = false;
            }
        }
        else if (Input.keyPressed(Input.KEY_MOVE))
        {
            holdingMove = true;
            holdingMoveOnUI = isHovered();
        }
        
        menu.update();
    }
    
    boolean isHoldingScrollMapOnGame()
    {
        return holdingScrollMap && !holdingScrollMapOnUI;
    }
    
    boolean isHoldingScrollMapOnUI()
    {
        return holdingScrollMap && holdingScrollMapOnUI;
    }
    
    boolean isHoldingMoveOnGame()
    {
        return holdingMove && !holdingMoveOnUI;
    }
    
    boolean isHoldingMoveOnUI()
    {
        return holdingMove && holdingMoveOnUI;
    }
    
    void draw()
    {
        menu.draw();
    }
    
    void setSize(float width, float height)
    {
        menu.setSize(width, height);
    }
    
    @Override
    public float getWidth()
    {
        return game.client.getWidth();
    }
    
    @Override
    public float getHeight()
    {
        return game.client.getHeight();
    }
    
    @Override
    public Network getNetwork()
    {
        return game.client.getNetwork();
    }
    
    @Override
    public Menu getMenu()
    {
        return menu;
    }
    
    @Override
    public void setMenu(Menu menu)
    {
        throw new UnsupportedOperationException();
    }
}
