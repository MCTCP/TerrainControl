package com.khorn.terraincontrol.biomelayers;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class ArraysCache
{
    private static final int[][][] SmallArrays = new int[4][][];
    private static final int[] SmallArraysNext = new int[4];
    private static final ArrayList[] BigArrays = new ArrayList[4];
    private static final int[] BigArraysNext = new int[4];
    private static final boolean[] ArraysInUse = new boolean[4];

    public static int GetCacheId()
    {
        synchronized (ArraysInUse)
        {
            for (int i = 0; i < ArraysInUse.length; i++)
            {
                if (!ArraysInUse[i])
                {

                    ArraysInUse[i] = true;
                    if (SmallArrays[i] == null)
                        SmallArrays[i] = new int[128][];
                    if (BigArrays[i] == null)
                        BigArrays[i] = new ArrayList();

                    return i;
                }
            }
        }
        return 0; // Exception ??
    }

    public static void ReleaseCacheId(int id)
    {
        synchronized (ArraysInUse)
        {
            ArraysInUse[id] = false;
            SmallArraysNext[id] = 0;
            BigArraysNext[id] = 0;
        }
    }

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
}