package onlinegame.shared.bitstream;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author Alfred
 */
public interface BitInput
{
    void align() throws IOException;
    
    int readInt(int bits) throws IOException;
    long readLong(int bits) throws IOException;
    
    int readInt(int bits, boolean signed) throws IOException;
    long readLong(int bits, boolean signed) throws IOException;
    
    int readInt(IntFormat fmt) throws IOException;
    long readLong(IntFormat fmt) throws IOException;
    
    void readFully(byte[] b) throws IOException;
    void readFully(byte[] b, int off, int len) throws IOException;
    
    boolean readBoolean() throws IOException;
    float readFloat() throws IOException;
    double readDouble() throws IOException;
    
    String readString() throws IOException;
    String readString(Charset charset) throws IOException;
}
