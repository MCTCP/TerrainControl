package com.khorn.terraincontrol.generator.biome;

import java.util.ArrayList;

public class ArraysCache
{

    private final int[][] smallArrays = new int[128][];
    private int smallArraysNext = 0;
    private final ArrayList<int[]> bigArrays = new ArrayList<int[]>();
    private int bigArraysNext = 0;

    public boolean isFree = true;

    public OutputType outputType = OutputType.FULL;

    public ArraysCache()
    {

    }

    public void release()
    {
        this.smallArraysNext = 0;
        this.bigArraysNext = 0;
        this.isFree = true;
        this.outputType = OutputType.FULL;
    }

    public int[] getArray(int size)
    {

        if (size <= 256)
        {
            int[] array = this.smallArrays[this.smallArraysNext];
            if (array == null)
            {
                array = new int[256];
                this.smallArrays[this.smallArraysNext] = array;
            }
            this.smallArraysNext++;

            return array;
        }

        int[] array;
        if (this.bigArraysNext == this.bigArrays.size())
        {
            array = new int[size];
            this.bigArrays.add(array);
        } else
        {
            array = (int[]) this.bigArrays.get(this.bigArraysNext);
            if (array.length < size)
            {
                array = new int[size];
                this.bigArrays.set(this.bigArraysNext, array);
            }
        }

        this.bigArraysNext++;
        return array;

    }
}
