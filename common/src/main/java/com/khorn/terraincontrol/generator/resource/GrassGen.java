package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GrassGen extends Resource
{
    public static enum GroupOption
    {
        Grouped,
        NotGrouped
    }

    private List<Integer> sourceBlocks;
    private PlantType plant;
    private GroupOption groupOption;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);

        // The syntax for the first to arguments used to be blockId,blockData
        // Then it became plantType,unusedParam (plantType can still be
        // blockId:blockData)
        // Now it is plantType,groupOption
        this.groupOption = GroupOption.NotGrouped;
        String secondArgument = args.get(1);
        try
        {
            // Test whether the second argument is the data value (deprecated)
            readInt(secondArgument, 0, 16);
            // If yes, parse it
            plant = PlantType.getPlant(args.get(0) + ":" + secondArgument);
        } catch (InvalidConfigException e)
        {
            // Nope, second argument is not a number
            plant = PlantType.getPlant(args.get(0));
            if (secondArgument.equalsIgnoreCase(GroupOption.Grouped.toString()))
            {
                this.groupOption = GroupOption.Grouped;
                // For backwards compatibility, the second argument is not
                // checked further
            }
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
        // Handled by spawnInChunk().
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        switch (groupOption)
        {
            case Grouped:
                spawnGrouped(world, random, chunkX, chunkZ);
                break;
            case NotGrouped:
                spawnNotGrouped(world, random, chunkX, chunkZ);
                break;
        }
    }

    protected void spawnGrouped(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        if (random.nextDouble() * 100.0 <= this.rarity)
        {
            // Passed Rarity test, place about Frequency grass in this chunk
            int centerX = chunkX * 16 + 8 + random.nextInt(16);
            int centerZ = chunkZ * 16 + 8 + random.nextInt(16);
            int centerY = world.getHighestBlockYAt(centerX, centerZ);
            int id;

            // Fix y position
            while (((id = world.getTypeId(centerX, centerY, centerZ)) == 0 || id == DefaultMaterial.LEAVES.id || id == DefaultMaterial.LEAVES_2.id) && (centerY > 0))
            {
                centerY--;
            }
            centerY++;

            // Try to place grass
            // Because of the changed y position, only one in four attempts
            // will have success
            for (int i = 0; i < frequency * 4; i++)
            {
                int x = centerX + random.nextInt(8) - random.nextInt(8);
                int y = centerY + random.nextInt(4) - random.nextInt(4);
                int z = centerZ + random.nextInt(8) - random.nextInt(8);
                if (world.isEmpty(x, y, z) && this.sourceBlocks.contains(world.getTypeId(x, y - 1, z)))
                {
                    plant.spawn(world, x, y, z);
                }

            }
        }
    }

    protected void spawnNotGrouped(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextInt(100) >= rarity)
                continue;
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;
            int y = world.getHighestBlockYAt(x, z);

            int id;
            while (((id = world.getTypeId(x, y, z)) == 0 || id == DefaultMaterial.LEAVES.id || id == DefaultMaterial.LEAVES_2.id) && (y > 0))
                y--;

            if ((!world.isEmpty(x, y + 1, z)) || (!sourceBlocks.contains(world.getTypeId(x, y, z))))
                continue;
            plant.spawn(world, x, y + 1, z);
        }
    }

    @Override
    public String makeString()
    {
        return "Grass(" + plant.getName() + "," + groupOption + "," + frequency + "," + rarity + makeMaterial(sourceBlocks) + ")";
    }

    @Override
    public boolean isAnalogousTo(Resource other)
    {
        return getClass() == other.getClass() && other.blockId == this.blockId && other.blockData == this.blockData;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 11 * hash + super.hashCode();
        hash = 11 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final GrassGen compare = (GrassGen) other;
        return (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                : this.sourceBlocks.equals(compare.sourceBlocks));
    }

}