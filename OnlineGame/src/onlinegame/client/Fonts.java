package onlinegame.client;

import java.io.IOException;
import onlinegame.client.graphics.text.BitmapFont;

/**
 *
 * @author Alfred
 */
public final class Fonts
{
    private Fonts() {}
    
    public static FontContainer
            menu = new FontContainer(),
            textBox = new FontContainer(),
            
            lobbySmall = new FontContainer(),
            lobbySmallMedium = new FontContainer(),
            lobbyMedium = new FontContainer();
    
    static void load() throws IOException
    {
        //mipmapped fonts
        //all fonts must have at least 1 pixel padding on every side as well as 4 pixels spacing in both directions to use mipmapping
        menu.create("fonts/menu.fnt", 'T', true);
        textBox.create("fonts/textBox.fnt", 'T', true);
        
        //non-mipmapped fonts
        lobbySmall.create("fonts/lobby_small.fnt", 'T');
        lobbySmallMedium.create("fonts/lobby_small_medium.fnt", 'T');
        lobbyMedium.create("fonts/lobby_medium.fnt", 'T');
    }
    
    static void destroy()
    {
        menu.destroy();
        textBox.destroy();
        
        lobbySmall.destroy();
        lobbySmallMedium.destroy();
        lobbyMedium.destroy();
    }
    
    public static class FontContainer
    {
        private BitmapFont font;
        
        private FontContainer() {}
        
        private void create(String name) throws IOException
        {
            create(name, (char)0);
        }
        
        private void create(String name, char normalHeightChar) throws IOException
        {
            create(name, normalHeightChar, false);
        }
        
        private void create(String name, char normalHeightChar, boolean filter) throws IOException
        {
            set(new BitmapFont(name, filter, filter));
            
            if (normalHeightChar != 0)
            {
                font.setNormalHeight(normalHeightChar);
            }
        }
        
        private void set(BitmapFont font)
        {
            this.font = font;
        }
        
        private void destroy()
        {
            font.destroy();
            font = null;
        }
        
        public boolean isLoaded()
        {
            return font != null;
        }
        
        public BitmapFont get()
        {
            if (font == null)
            {
                throw new IllegalStateException("Font is not loaded.");
            }
            return font;
        }
    }
}
