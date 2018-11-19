package onlinegame.client.menu;

/**
 *
 * @author Alfred
 */
public class MenuGridItem extends MenuListItem
{
    public MenuGridItem(MenuGridBox grid)
    {
        super(grid, grid.getCellWidth(), grid.getCellHeight());
    }
}
