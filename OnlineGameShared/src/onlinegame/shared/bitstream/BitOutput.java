package onlinegame.shared.bitstream;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author Alfred
 */
public interface BitOutput
{
    void align() throws IOException;
    
    void writeInt(int val, int bits) throws IOException;
    void writeInt(int val, IntFormat fmt) throws IOException;
    void writeInt(long val, int bits) throws IOException;
    void writeInt(long val, IntFormat fmt) throws IOException;
    
    void writeBoolean(boolean b) throws IOException;
    void writeFloat(float f) throws IOException;
    void writeDouble(double d) throws IOException;
    
    void writeString(String s) throws IOException;
    void writeString(String s, Charset charset) throws IOException;
}
