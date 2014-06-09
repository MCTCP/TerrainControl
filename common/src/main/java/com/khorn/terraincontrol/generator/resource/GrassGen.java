package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.List;
import java.util.Random;

public class GrassGen extends Resource
{
    public static enum GroupOption
    {
        Grouped,
        NotGrouped
    }

    private MaterialSet sourceBlocks;
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
        // implementation to fire Forge events
        material = plant.getBottomMaterial();

        frequency = readInt(args.get(2), 1, 500);
        rarity = readRarity(args.get(3));
        sourceBlocks = readMaterials(args, 4);
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Handled by spawnInChunk().
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        switch (groupOption)
        {
            case Grouped:
                spawnGrouped(world, random, chunkCoord);
                break;
            case NotGrouped:
                spawnNotGrouped(world, random, chunkCoord);
                break;
        }
    }

    protected void spawnGrouped(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
        if (random.nextDouble() * 100.0 <= this.rarity)
        {
            // Passed Rarity test, place about Frequency grass in this chunk
            int centerX = chunkCoord.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
            int centerZ = chunkCoord.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);
            int centerY = world.getHighestBlockYAt(centerX, centerZ);
            LocalMaterialData id;

            // Fix y position
            while (((id = world.getMaterial(centerX, centerY, centerZ)).isMaterial(DefaultMaterial.AIR) || id.isMaterial(DefaultMaterial.LEAVES) || id.isMaterial(DefaultMaterial.LEAVES_2)) && (centerY > 0))
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
                if (world.isEmpty(x, y, z) && this.sourceBlocks.contains(world.getMaterial (x, y - 1, z)))
                {
                    plant.spawn(world, x, y, z);
                }

            }
        }
    }

    protected void spawnNotGrouped(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextInt(100) >= rarity)
                continue;
            int x = chunkCoord.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
            int z = chunkCoord.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);
            int y = world.getHighestBlockYAt(x, z);

            LocalMaterialData material;
            while (((material = world.getMaterial(x, y, z)).isMaterial(DefaultMaterial.AIR) || material.isMaterial(DefaultMaterial.LEAVES) || material.isMaterial(DefaultMaterial.LEAVES_2)) && (y > 0))
                y--;

            if ((!world.isEmpty(x, y + 1, z)) || (!sourceBlocks.contains(world.getMaterial(x, y, z))))
                continue;
            plant.spawn(world, x, y + 1, z);
        }
    }

    @Override
    public String makeString()
    {
        return "Grass(" + plant.getName() + "," + groupOption + "," + frequency + "," + rarity + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        return getClass() == other.getClass() && ((GrassGen) other).plant.equals(plant);
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