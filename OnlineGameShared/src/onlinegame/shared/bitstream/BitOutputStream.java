package onlinegame.shared.bitstream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Alfred
 */
public final class BitOutputStream extends FilterOutputStream implements BitOutput
{
    private static final int[] bitmask = BitInputStream.bitmask;
    //private static final int[] signmask = BitInputStream.signmask;
    
    //private static final long[] longbitmask = BitInputStream.longbitmask;
    //private static final long[] longsignmask = BitInputStream.longsignmask;
    
    private int buf = 0;
    private int rem = 8;
    
    private long writtenBits = 0;
    
    public BitOutputStream(OutputStream out)
    {
        super(out);
    }
    
    @Override
    public void align() throws IOException
    {
        if (rem != 8)
        {
            out.write(buf << rem);
            
            buf = 0;
            rem = 8;
        }
    }
    
    @Override
    public void flush() throws IOException
    {
        out.flush();
    }
    
    public void writeInt(int value, int bits) throws IOException
    {
        if (bits < 0)
        {
            throw new IllegalArgumentException("Negative bit count: " + bits);
        }
        else if (bits == 0)
        {
            return;
        }
        else while (bits > 32)
        {
            int n = Math.min(32, bits - 32);
            writeInt(0, n);
            bits -= n;
        }
        
        writtenBits += bits;
        
        value &= bitmask[bits];  // only right most bits valid
        
        while (bits >= rem)
        {
            buf = (buf << rem) | (value >>> (bits - rem));
            
            out.write(buf);
            
            value &= bitmask[bits - rem];
            bits -= rem;
            rem = 8;
            buf = 0;
        }
        
        if (bits > 0)
        {
            buf = (buf << bits) | value;
            rem -= bits;
        }
    }
    
    @Override
    public void writeInt(long value, int bits) throws IOException
    {
        if (bits <= 32)
        {
            writeInt((int)value, bits);
            return;
        }
        else while (bits > 64)
        {
            int n = Math.min(32, bits - 64);
            writeInt(0, n);
            bits -= n;
        }
        
        int b = bits - 32;
        writeInt((int)(value >>> b), 32);
        writeInt((int)value, b);
    }
    
    @Override
    public void write(int b) throws IOException
    {
        if (rem == 8)
        {
            out.write(b);
            return;
        }
        
        writeInt(b, 8);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        
        if (rem == 8)
        {
            out.write(b, off, len);
        }
        else
        {
            int max = off + len;
            for (int i = off; i < max; i++)
            {
                writeInt(b[i], 8);
            }
        }
    }
    
    @Override
    public void writeBoolean(boolean b) throws IOException
    {
        writeInt(b ? 1 : 0, 1);
    }
    
    @Override
    public void writeFloat(float f) throws IOException
    {
        writeInt(Float.floatToIntBits(f), 32);
    }
    
    @Override
    public void writeDouble(double d) throws IOException
    {
        writeInt(Double.doubleToLongBits(d), 64);
    }
    
    private static final IntFormat LENGTH_FORMAT = new IntFormat(false, 4, 8, 16, 24, 32);
    
    @Override
    public void writeString(String s) throws IOException
    {
        writeString(s, StandardCharsets.UTF_8);
    }
    @Override
    public void writeString(String s, Charset charset) throws IOException
    {
        byte[] bytes = s.getBytes(charset);
        
        writeInt(bytes.length, LENGTH_FORMAT);
        write(bytes);
    }
    
    private int writeIntHeader(IntFormat fmt, int minBits) throws IOException
    {
        int[] breakPoints = fmt.breakPoints;
        int len = breakPoints.length;
        
        if (len == 1)
        {
            return breakPoints[0];
        }
        
        int pos = 0;
        while (breakPoints[pos] < minBits)
        {
            pos++;
            
            if (pos == len)
            {
                pos--;
                break;
            }
        }
        
        int bits = breakPoints[pos];
        int bitsToWrite = pos;
        while (bitsToWrite >= 32)
        {
            writeInt(0xffffffff, 32);
            bitsToWrite -= 32;
        }
        if (bitsToWrite > 0)
        {
            writeInt(bitmask[pos], pos);
        }
        if (pos < len-1)
        {
            writeInt(0, 1);
        }
        
        return bits;
    }
    
    public void writeInt(int value, IntFormat fmt) throws IOException
    {
        boolean signed = fmt.signed;
        
        int minBits; //minimum number of bits that are needed to represent the value
        if (signed)
        {
            if (value > 0)
            {
                minBits = 33 - Integer.numberOfLeadingZeros(value);
            }
            else if (value < 0)
            {
                minBits = 33 - Integer.numberOfLeadingZeros(-value - 1);
            }
            else
            {
                minBits = 0;
            }
        }
        else
        {
            minBits = 32 - Integer.numberOfLeadingZeros(value);
        }
        
        int bits = writeIntHeader(fmt, minBits);
        writeInt(value, bits);
    }
    
    @Override
    public void writeInt(long value, IntFormat fmt) throws IOException
    {
        boolean signed = fmt.signed;
        
        int minBits; //minimum number of bits that are needed to represent the value
        if (signed)
        {
            if (value > 0)
            {
                minBits = 65 - Long.numberOfLeadingZeros(value);
            }
            else if (value < 0)
            {
                minBits = 65 - Long.numberOfLeadingZeros(-value - 1);
            }
            else
            {
                minBits = 0;
            }
        }
        else
        {
            minBits = 64 - Long.numberOfLeadingZeros(value);
        }
        
        int bits = writeIntHeader(fmt, minBits);
        writeInt(value, bits);
    }
    
    public long writtenBits()
    {
        return writtenBits;
    }
    
    public long writtenBytes()
    {
        return writtenBits / 8;
    }
}
