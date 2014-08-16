package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.generator.biome.ArraysCache;
import java.util.ArrayList;
import java.util.List;

public class LayerLandRandom extends Layer
{

    public LayerLandRandom(long seed, Layer childLayer)
    {
        super(seed);
        this.child = childLayer;
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int x0 = x - 1;
        int z0 = z - 1;
        int xSize0 = xSize + 2;
        int zSize0 = zSize + 2;
        int[] childInts = this.child.getInts(cache, x0, z0, xSize0, zSize0);
        int[] thisInts = cache.getArray(xSize * zSize);

        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                int nwCheck = childInts[(xi + 0 + (zi) * xSize0)] & LandBit;
                int neCheck = childInts[(xi + 2 + (zi) * xSize0)] & LandBit;
                int swCheck = childInts[(xi + 0 + (zi + 2) * xSize0)] & LandBit;
                int seCheck = childInts[(xi + 2 + (zi + 2) * xSize0)] & LandBit;
                int centerCheck = childInts[(xi + 1 + (zi + 1) * xSize0)] & LandBit;
                initChunkSeed(xi + x, zi + z);
                initGroupSeed(xi + x, zi + z);
                thisInts[(xi + zi * xSize)] = childInts[(xi + 1 + (zi + 1) * xSize0)] | LandBit;

                //>>	Chances to reset LandBit
                if ((centerCheck == 0) && ((nwCheck != 0) || (neCheck != 0) || (swCheck != 0) || (seCheck != 0)))
                {
                    if (nextInt(3) != 0)
                        thisInts[(xi + zi * xSize)] ^= LandBit;

                } else if ((centerCheck > 0) && ((nwCheck == 0) || (neCheck == 0) || (swCheck == 0) || (seCheck == 0)))
                {
                    if (nextInt(5) == 0)
                        thisInts[(xi + zi * xSize)] ^= LandBit;

                } else if (centerCheck == 0)
                    thisInts[(xi + zi * xSize)] ^= LandBit;

                //>>	Handle BiomeGroups
                thisInts[(xi + zi * xSize)] = modLandBits(thisInts[(xi + zi * xSize)], childInts, xi, zi, xSize0);
            }
        }
        return thisInts;
    }

    private int modLandBits(int thisInt, int[] childInts, int xi, int zi, int xSize0)
    {
        //>>	Change from Ocean to Land
        if ((thisInt & LandBit) != 0 && (thisInt & BiomeGroupBits) == 0)
        {
            int biomeGroup = getRandomFuzzyBits(childInts, xi, zi, xSize0, BiomeGroupBits);
            if (biomeGroup != 0)
            {
                return ((thisInt & ~BiomeGroupBits) | biomeGroup);
            }
        }
        return thisInt;
    }

    private int getRandomFuzzyBits(int[] childInts, int xi, int zi, int xSize0, int fuzzyBits)
    {
        int northCheck = childInts[xi + 1 + (zi) * xSize0] & fuzzyBits;
        int southCheck = childInts[xi + 1 + (zi + 2) * xSize0] & fuzzyBits;
        int eastCheck = childInts[xi + 2 + (zi + 1) * xSize0] & fuzzyBits;
        int westCheck = childInts[xi + (zi + 1) * xSize0] & fuzzyBits;
        int centerCheck = childInts[xi + 1 + (zi + 1) * xSize0] & fuzzyBits;
        if (northCheck != 0 || southCheck != 0 || eastCheck != 0 || westCheck != 0 || centerCheck != 0)
            return getNonNullRandomInArray(northCheck, southCheck, eastCheck, westCheck, centerCheck);
        return 0;
    }

    private int getNonNullRandomInArray(int... biomes)
    {
        List<Integer> biomesList = new ArrayList<Integer>();
        for (int i : biomes)
        {
            if (i > 0)
                biomesList.add(i);
        }
        int[] nonNull = new int[biomesList.size()];
        int ii = 0;
        for (Integer i : biomesList)
        {
            nonNull[ii++] = i;
        }
        int group = getGroupRandomInArray(nonNull);
//        TerrainControl.log(LogMarker.INFO, "Getting a random group for you! Size: {}, Group: {}", nonNull.length, group >> BiomeGroupShift);
        return group;
    }

}
