package com.khorn.terraincontrol.biomegenerators;

import java.util.ArrayList;

public class ArraysCache
{

    private final int[][] SmallArrays = new int[256][];
    private int SmallArraysNext = 0;
    private final ArrayList BigArrays = new ArrayList();
    private int BigArraysNext = 0;

    public int [][] BiomeTestOutput = new int[32][];
    public int []  BiomeTestSize = new int[32];
    public int []  BiomeTestX = new int[32];
    public int []  BiomeTestZ = new int[32];
    public boolean [] BiomeTestConverted = new boolean[32];

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
        BiomeTestOutput = new int[32][];
        BiomeTestSize = new int[32];
        BiomeTestX = new int[32];
        BiomeTestZ = new int[32];
        BiomeTestConverted = new boolean[32];


    }

    @SuppressWarnings({"unchecked"})
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
