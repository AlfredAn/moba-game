package onlinegame.client.io;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import onlinegame.shared.Logger;

/**
 *
 * @author Alfred
 */
public final class FileLoader
{
    private static final Map<String, SoftReference<CacheEntry>> jarResourceCache;
    private static final Map<SoftReference<CacheEntry>, String> jarResourceReverseMap;
    private static final ReferenceQueue<CacheEntry> refq;
    
    static final AtomicLong cacheSize;
    
    static
    {
        jarResourceCache = new HashMap<>();
        jarResourceReverseMap = new HashMap<>();
        refq = new ReferenceQueue<>();
        cacheSize = new AtomicLong(0);
    }
    
    private FileLoader() {}
    
    //should only be called from a synchronized method
    @SuppressWarnings("unchecked")
    private static void cleanCache()
    {
        while (true)
        {
            SoftReference<CacheEntry> ref = (SoftReference<CacheEntry>)refq.poll();
            
            if (ref == null)
            {
                //no entries to remove
                return;
            }
            
            String key = jarResourceReverseMap.get(ref);
                
            if (key == null)
            {
                Logger.logError("Unable to remove cache reference!");
            }
            
            jarResourceCache.remove(key);
            jarResourceReverseMap.remove(ref);
        }
    }
    
    public static InputStream getJarResource(String path) throws IOException
    {
        return getJarResource(path, false);
    }
    public static InputStream getJarResource(String path, boolean bypassCache) throws IOException
    {
        path = "/" + path;
        
        if (bypassCache)
        {
            InputStream in = FileLoader.class.getResourceAsStream(path);
            if (in instanceof BufferedInputStream)
            {
                return in;
            }
            else
            {
                return new BufferedInputStream(in);
            }
        }
        
        CacheEntry entry;
        
        synchronized (jarResourceCache)
        {
            cleanCache();
            
            SoftReference<CacheEntry> ref = jarResourceCache.get(path);
            entry = ref == null ? null : ref.get();
            
            if (entry == null)
            {
                try
                    (
                        InputStream is = FileLoader.class.getResourceAsStream(path);
                    )
                {
                    if (is == null)
                    {
                        throw new FileNotFoundException("Jar resource not found: " + path);
                    }
                    
                    entry = new CacheEntry(is);
                    SoftReference<CacheEntry> newRef = new SoftReference<>(entry, refq);
                    jarResourceCache.put(path, newRef);
                    jarResourceReverseMap.put(newRef, path);
                }
            }
        }
        
        return entry.getInputStream();
    }
    
    public static int getFileSize(String path)
    {
        synchronized (jarResourceCache)
        {
            SoftReference<CacheEntry> ref = jarResourceCache.get(path);
            CacheEntry entry = ref == null ? null : ref.get();
            
            return entry == null ? -1 : entry.size();
        }
    }
    
    public static void clearCache()
    {
        synchronized (jarResourceCache)
        {
            jarResourceCache.clear();
            jarResourceReverseMap.clear();
        }
    }
}