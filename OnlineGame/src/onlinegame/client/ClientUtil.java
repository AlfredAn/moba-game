package onlinegame.client;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public final class ClientUtil
{
    private ClientUtil() {}
    
    public static String getClipboardContents()
    {
        try
        {
            return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        }
        catch (UnsupportedFlavorException | IOException e)
        {
            return "";
        }
    }
}
