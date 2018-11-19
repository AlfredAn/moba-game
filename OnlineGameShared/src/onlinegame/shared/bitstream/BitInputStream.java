package onlinegame.shared.bitstream;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Alfred
 */
public final class BitInputStream extends FilterInputStream implements BitInput
{
    static final int[] bitmask;
    static final int[] signmask;
    
    static final long[] longbitmask;
    static final long[] longsignmask;
    
    static
    {
        //int arrays
        bitmask = new int[33];
        signmask = new int[33];
        
        signmask[0] = 0x0;
        bitmask[32] = 0xffffffff;
        
        int po2 = 1;
        for (int i = 0; i < 32; i++)
        {
            bitmask[i] = po2 - 1;
            signmask[i+1] = po2;
            po2 *= 2;
        }
        
        //long arrays
        longbitmask = new long[65];
        longsignmask = new long[65];
        
        longsignmask[0] = 0x0L;
        longbitmask[64] = 0xffffffff_ffffffffL;
        
        long lpo2 = 1;
        for (int i = 0; i < 64; i++)
        {
            longbitmask[i] = lpo2 - 1;
            longsignmask[i+1] = lpo2;
            lpo2 *= 2;
        }
    }
    
    private int buf = 0;
    private int buflen = 0;
    
    private int markBuf = 0;
    private int markBuflen = 0;
    
    public BitInputStream(InputStream in)
    {
        super(in);
    }
    
    @Override
    public void align()
    {
        buf = 0;
        buflen = 0;
    }
    
    @Override
    public int readInt(int bits) throws IOException
    {
        if (bits == 0)
        {
            return 0;
        }
        else if (bits < 0)
        {
            throw new IllegalArgumentException("Invalid bit count: " + bits);
        }
        else if (bits == 8 && buflen == 0)
        {
            int result = in.read();
            if (result == -1)
            {
                throw new EOFException();
            }
            return result;
        }
        else while (bits > 32)
        {
            int n = Math.min(32, bits - 32);
            readInt(n);
            bits -= n;
        }
        
        int result = 0;
        
        while (bits > buflen)
        {
            result |= buf << (bits - buflen);
            bits -= buflen;
            
            if (bits == 0)
            {
                buflen = 0;
                return result;
            }
            if ((buf = in.read()) == -1)
            {
                buflen = 0;
                throw new EOFException();
            }
            buflen = 8;
        }

        if (bits > 0)
        {
            result |= buf >> (buflen - bits);
            buf &= bitmask[buflen - bits];
            buflen -= bits;
        }
        
        return result;
    }
    
    @Override
    public long readLong(int bits) throws IOException
    {
        if (bits <= 32)
        {
            return readInt(bits) & 0xffffffffL;
        }
        else while (bits > 64)
        {
            int n = Math.min(32, bits - 64);
            readInt(n);
            bits -= n;
        }
        
        long i1 = readInt(32) & 0xffffffffL;
        
        int b = bits - 32;
        long i2 = readInt(b) & 0xffffffffL;
        
        return (i1 << b) | i2;
    }
    
    @Override
    public int readInt(int bits, boolean signed) throws IOException
    {
        int i = readInt(bits);
        
        if (bits > 32) bits = 32;
        
        if (!signed || bits == 32 || (i & signmask[bits]) == 0)
        {
            return i;
        }
        
        return i | ~bitmask[bits];
    }
    
    @Override
    public long readLong(int bits, boolean signed) throws IOException
    {
        long l = readLong(bits);
        
        if (bits > 64) bits = 64;
        
        if (!signed || bits == 64 || (l & longsignmask[bits]) == 0)
        {
            return l;
        }
        
        return l | ~longbitmask[bits];
    }
    
    @Override
    public int read() throws IOException
    {
        if (buflen == 0)
        {
            return in.read();
        }
        
        try
        {
            return readInt(8);
        }
        catch (EOFException e)
        {
            return -1;
        }
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (len == 0)
        {
            return 0;
        }
        
        int readLen = in.read(b, off, len);
        
        if (buflen == 0)
        {
            return readLen;
        }
        
        int max = off + readLen;
        for (int i = off; i < max; i++)
        {
            b[i] = (byte)readNextByte((int)b[i] & 0xff);
        }
        
        return readLen;
    }
    
    private int readNextByte(int next)
    {
        int bits = 8;
        int retval = 0;
        
        while (bits > buflen)
        {
            retval |= buf << (bits - buflen);
            bits -= buflen;
            
            if (bits == 0)
            {
                buflen = 0;
                return retval;
            }
            
            buf = next;
            buflen = 8;
        }

        if (bits > 0)
        {
            retval |= buf >>> (buflen - bits);
            buf &= bitmask[buflen - bits];
            buflen -= bits;
        }
        
        return retval;
    }
    
    @Override
    public long skip(long bytes) throws IOException
    {
        if (bytes == 0)
        {
            return 0;
        }
        if (buflen == 0)
        {
            return in.skip(bytes);
        }
        
        long skipLen = in.skip(bytes - 1);
        
        try
        {
            readInt(8);
            skipLen++;
        }
        catch (EOFException ignored) {}
        
        return skipLen;
    }
    
    public long skipBits(long bits) throws IOException
    {
        if ((bits&7) == 0)
        {
            return skip(bits/8);
        }
        
        long rem = bits;
        
        if (rem >= 16)
        {
            long toSkip = (rem - 1) / 8;
            long skipped = in.skip(toSkip);
            rem -= skipped * 8;
            if (skipped < toSkip)
            {
                readInt(buflen);
                rem -= buflen;
                return bits - rem;
            }
        }
        
        //rem < 16
        try
        {
            readInt((int)rem);
            rem = 0;
        }
        catch (EOFException ignored) {}
        
        return bits - rem;
    }
    
    public int availableBits() throws IOException
    {
        return in.available() * 8 + buflen;
    }
    
    @Override
    public void mark(int readlimit)
    {
        in.mark(readlimit);
        
        if (in.markSupported())
        {
            markBuf = buf;
            markBuflen = buflen;
        }
    }
    
    @Override
    public void reset() throws IOException
    {
        in.reset();
        
        if (in.markSupported())
        {
            buf = markBuf;
            buflen = markBuflen;
        }
    }
    
    @Override
    public boolean markSupported()
    {
        return in.markSupported();
    }
    
    @Override
    public void readFully(byte[] b) throws IOException
    {
        readFully(b, 0, b.length);
    }
    
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException
    {
        if (len < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len)
        {
            int count = read(b, off + n, len - n);
            if (count < 0)
            {
                throw new EOFException();
            }
            n += count;
        }
    }
    
    @Override
    public boolean readBoolean() throws IOException
    {
        return readInt(1) == 1;
    }
    
    @Override
    public float readFloat() throws IOException
    {
        return Float.intBitsToFloat(readInt(32));
    }
    
    @Override
    public double readDouble() throws IOException
    {
        return Double.longBitsToDouble(readLong(64));
    }
    
    private static final IntFormat LENGTH_FORMAT = new IntFormat(false, 4, 8, 16, 24, 32);
    
    @Override
    public String readString() throws IOException
    {
        return readString(StandardCharsets.UTF_8);
    }
    @Override
    public String readString(Charset charset) throws IOException
    {
        int len = readInt(LENGTH_FORMAT);
        
        if (len == 0)
        {
            return "";
        }
        
        byte[] bytes = new byte[len];
        readFully(bytes);
        
        return new String(bytes, charset);
    }
    
    private int parseIntHeader(IntFormat fmt) throws IOException
    {
        int[] breakPoints = fmt.breakPoints;
        int maxPos = breakPoints.length - 1;
        
        int pos = 0;
        while (pos < maxPos && readBoolean())
        {
            pos++;
        }
        
        return breakPoints[pos];
    }
    
    @Override
    public int readInt(IntFormat fmt) throws IOException
    {
        return readInt(parseIntHeader(fmt), fmt.signed);
    }
    
    @Override
    public long readLong(IntFormat fmt) throws IOException
    {
        return readLong(parseIntHeader(fmt), fmt.signed);
    }
}
