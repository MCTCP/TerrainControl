package com.pg85.otg.generator.biome;

public class ArraysCacheManager
{

    private static final ArraysCache[] ARRAYS_CACHES = new ArraysCache[4];

    static
    {
        for (int i = 0; i < ARRAYS_CACHES.length; i++)
            ARRAYS_CACHES[i] = new ArraysCache();

    }

    static ArraysCache getCache()
    {
        synchronized (ARRAYS_CACHES)
        {
            for (ArraysCache ArraysCache : ARRAYS_CACHES)
            {
                if (ArraysCache.isFree)
                {
                    ArraysCache.isFree = false;
                    return ArraysCache;
                }
            }

        }
        return null; // Exception ??
    }

    static void releaseCache(ArraysCache cache)
    {
        synchronized (ARRAYS_CACHES)
        {
            cache.release();
        }
    }

    private ArraysCacheManager()
    {
    }
}