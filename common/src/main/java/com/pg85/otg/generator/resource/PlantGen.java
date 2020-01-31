package com.pg85.otg.generator.resource;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import java.util.List;
import java.util.Random;

public class PlantGen extends Resource
{
    private final int maxAltitude;

    private final int minAltitude;
    private final PlantType plant;
    private final MaterialSet sourceBlocks;

    public PlantGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(6, args);

        plant = PlantType.getPlant(args.get(0));

        // Not used for terrain generation, they are used by the Forge
        // implementation to fire Forge events.
        material = plant.getBottomMaterial();

        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), PluginStandardValues.WORLD_DEPTH,
                PluginStandardValues.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(4), minAltitude,
                PluginStandardValues.WORLD_HEIGHT - 1);
        sourceBlocks = readMaterials(args, 5);
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
        final PlantGen compare = (PlantGen) other;
        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                   : this.sourceBlocks.equals(compare.sourceBlocks));
    }

    @Override
    public int getPriority()
    {
        return -33;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 71 * hash + super.hashCode();
        hash = 71 * hash + this.minAltitude;
        hash = 71 * hash + this.maxAltitude;
        hash = 71 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "Plant(" + plant.getName() + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
    	sourceBlocks.parseForWorld(world);
        int y = RandomHelper.numberInRange(rand, minAltitude, maxAltitude);

        int j;
        int k;
        int m;
        LocalMaterialData worldMaterial;
        LocalMaterialData worldMaterialBelow;
        
        for (int i = 0; i < 64; i++)
        {
            j = x + rand.nextInt(8) - rand.nextInt(8);
            k = y + rand.nextInt(4) - rand.nextInt(4);
            m = z + rand.nextInt(8) - rand.nextInt(8);
            worldMaterial = world.getMaterial(j, k , m, chunkBeingPopulated);
            worldMaterialBelow = world.getMaterial(j, k - 1, m, chunkBeingPopulated);
            if (
        		(worldMaterial == null || worldMaterial.toDefaultMaterial() != DefaultMaterial.AIR) ||
        		(worldMaterialBelow == null || !sourceBlocks.contains(worldMaterialBelow))
    		)
            {
                continue;
            }

            plant.spawn(world, j, k, m, chunkBeingPopulated);
        }
    }
}