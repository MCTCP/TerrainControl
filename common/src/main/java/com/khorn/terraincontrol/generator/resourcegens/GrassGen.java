package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GrassGen extends Resource
{
    private static final String UNUSED_SECOND_ARGUMENT = "-unusedArgument-";
    
    private List<Integer> sourceBlocks;
    private PlantType plant;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        
        try {
            // Test whether the old second argument is a number
            readInt(args.get(1), 0, 16);
            // If yes, parse it
            plant = PlantType.getPlant(args.get(0) + ":" + args.get(1));
        } catch(InvalidConfigException e) {
            // Nope, old second argument is indeed unused
            plant = PlantType.getPlant(args.get(0));
        }

        // Not used for terrain generation, they are used by the Forge
        // implementation to fire Forge events. We'll probably want to rewrite
        // this in the future to not use block ids
        blockId = plant.getBlockId();
        blockData = plant.getBottomBlockData();

        frequency = readInt(args.get(2), 1, 500);
        rarity = readRarity(args.get(3));
        sourceBlocks = new ArrayList<Integer>();
        for (int i = 4; i < args.size(); i++)
        {
            sourceBlocks.add(readBlockId(args.get(i)));
        }
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Handled by process().
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextInt(100) >= rarity)
                continue;
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;
            int y = world.getHighestBlockYAt(x, z);

            int i;
            while (((i = world.getTypeId(x, y, z)) == 0 || i == DefaultMaterial.LEAVES.id || i == DefaultMaterial.LEAVES_2.id) && (y > 0))
                y--;

            if ((!world.isEmpty(x, y + 1, z)) || (!sourceBlocks.contains(world.getTypeId(x, y, z))))
                continue;
            plant.spawn(world, x, y + 1, z);
        }
    }

    @Override
    public String makeString()
    {
        return "Grass(" + plant.getName() + ","+UNUSED_SECOND_ARGUMENT+"," + frequency + "," + rarity + makeMaterial(sourceBlocks) + ")";
    }
}