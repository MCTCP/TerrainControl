package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.exception.InvalidResourceException;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;

import java.util.List;
import java.util.Random;

public class DungeonGen extends Resource
{
    private int minAltitude;
    private int maxAltitude;

    @Override
    public void load(List<String> args) throws InvalidResourceException
    {
        if (args.size() < 4)
        {
            throw new InvalidResourceException("Too few arguments supplied");
        }
        frequency = getInt(args.get(0), 1, 100);
        rarity = getInt(args.get(1), 1, 100);
        minAltitude = getInt(args.get(2), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = getInt(args.get(3), minAltitude + 1, TerrainControl.worldHeight);
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int z)
    {
        int y = random.nextInt(maxAltitude - minAltitude) + minAltitude;
        world.PlaceDungeons(random, x, y, z);
    }

    @Override
    public ResourceType getType()
    {
        return ResourceType.biomeConfigResource;
    }

    @Override
    public String makeString()
    {
        return "Dungeon(" + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }
}