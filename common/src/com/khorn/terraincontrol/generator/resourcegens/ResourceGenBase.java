package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

public abstract class ResourceGenBase
{
    public void Process(LocalWorld world, Random rand, Resource res, int _x, int _z)
    {
        for (int t = 0; t < res.Frequency; t++)
        {
            if (rand.nextInt(100) > res.Rarity)
                continue;
            int x = _x + rand.nextInt(16) + 8;
            int z = _z + rand.nextInt(16) + 8;
            SpawnResource(world, rand, res, x, z);
        }

    }

    protected abstract void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z);

    public boolean ReadFromString(Resource res, String[] line, BiomeConfig biomeConfig)
    {
        if (line.length < res.Type.MinProperties)
            return false;
        try
        {
            return this.ReadString(res, line, biomeConfig);

        } catch (NumberFormatException e)
        {
            return false;
        }
    }

    public String WriteToString(Resource res)
    {
        String sources = "";
        for (int id : res.SourceBlockId)
            sources += "," + res.BlockIdToName(id);

        return res.Type.name() + "(" + this.WriteString(res, sources) + ")";
    }

    protected abstract String WriteString(Resource res, String blockSources);

    protected abstract boolean ReadString(Resource res, String[] Props, BiomeConfig biomeConfig) throws NumberFormatException;

    protected int CheckValue(String str, int min, int max) throws NumberFormatException
    {
        int value = Integer.valueOf(str);
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    protected int CheckValue(String str, int min, int max, int minValue) throws NumberFormatException
    {
        int value = CheckValue(str, min, max);

        if (value < minValue)
            return minValue + 1;
        else
            return value;
    }

    protected int CheckBlock(String block) throws NumberFormatException
    {
        DefaultMaterial mat = DefaultMaterial.getMaterial(block);
        if (mat != null)
            return mat.id;

        return CheckValue(block, 0, 256);
    }
}