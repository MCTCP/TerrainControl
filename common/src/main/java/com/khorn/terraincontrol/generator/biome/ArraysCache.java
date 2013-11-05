package com.khorn.terraincontrol.generator.biome;

import java.util.ArrayList;

public class ArraysCache
{

    private final int[][] SmallArrays = new int[128][];
    private int SmallArraysNext = 0;
    private final ArrayList<int[]> BigArrays = new ArrayList<int[]>();
    private int BigArraysNext = 0;

    public boolean isFree = true;

    public OutputType outputType = OutputType.FULL;

    public ArraysCache()
    {

    }

    public void Release()
    {
        SmallArraysNext = 0;
        BigArraysNext = 0;
        isFree = true;
        outputType = OutputType.FULL;
    }

    public int[] GetArray(int size)
    {

        if (size <= 256)
        {
            int[] array = SmallArrays[SmallArraysNext];
            if (array == null)
            {
                array = new int[256];
                SmallArrays[SmallArraysNext] = array;
            }
            SmallArraysNext++;

            return array;
        }

        int[] array;
        if (BigArraysNext == BigArrays.size())
        {
            array = new int[size];
            BigArrays.add(array);
        } else
        {
            array = (int[]) BigArrays.get(BigArraysNext);
            if (array.length < size)
            {
                array = new int[size];
                BigArrays.set(BigArraysNext, array);
            }
        }

        BigArraysNext++;
        return array;

    }
}
