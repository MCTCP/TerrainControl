package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

public class DungeonGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int _y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;
        world.PlaceDungeons(rand, x, _y, z);
    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, BiomeConfig biomeConfig) throws NumberFormatException
    {
        res.Frequency = CheckValue(Props[0], 1, 100);
        res.Rarity = CheckValue(Props[1], 0, 100);
        res.MinAltitude = CheckValue(Props[2], 0, biomeConfig.worldConfig.WorldHeight);
        res.MaxAltitude = CheckValue(Props[3], 0, biomeConfig.worldConfig.WorldHeight, res.MinAltitude);

        return true;
    }

    @Override
    protected String WriteString(Resource res, String blockSources)
    {
        return res.Frequency + "," + res.Rarity + "," + res.MinAltitude + "," + res.MaxAltitude;
    }
}