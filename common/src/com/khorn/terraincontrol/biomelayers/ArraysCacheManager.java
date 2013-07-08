package com.khorn.terraincontrol.biomelayers;

@SuppressWarnings("rawtypes")
public class ArraysCacheManager
{

    private static final ArrayCache[] ArrayCaches = new ArrayCache[4];
    public static boolean NextRiver = false;

    static
    {
        for (int i = 0; i < ArrayCaches.length; i++)
            ArrayCaches[i] = new ArrayCache();

    }

    public static ArrayCache GetCache()
    {
        synchronized (ArrayCaches)
        {
            for (ArrayCache ArrayCache : ArrayCaches)
            {
                if (ArrayCache.isFree)
                {
                    ArrayCache.isFree = false;
                    ArrayCache.ReturnRiver = NextRiver;
                    NextRiver = false;
                    return ArrayCache;
                }
            }

        }
        return null; // Exception ??
    }

    public static void ReleaseCache(ArrayCache cache)
    {
        synchronized (ArrayCaches)
        {
            cache.Release();
        }
    }

    /*
    @SuppressWarnings({"unchecked"})
    public static int[] GetArray(int cacheId, int size)
    {
        if (size <= 256)
        {
            int[] array = SmallArrays[cacheId][SmallArraysNext[cacheId]];
            if (array == null)
            {
                array = new int[256];
                SmallArrays[cacheId][SmallArraysNext[cacheId]] = array;
            }
            SmallArraysNext[cacheId]++;

            return array;
        }
        int[] array;
        if (BigArraysNext[cacheId] == BigArrays[cacheId].size())
        {
            array = new int[size];
            BigArrays[cacheId].add(array);
        } else
        {
            array = (int[]) BigArrays[cacheId].get(BigArraysNext[cacheId]);
            if (array.length < size)
            {
                array = new int[size];
                BigArrays[cacheId].set(BigArraysNext[cacheId], array);
            }
        }

        BigArraysNext[cacheId]++;
        return array;
    }
    */
}