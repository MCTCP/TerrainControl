package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

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

        this.plant = PlantType.getPlant(args.get(0));

        // Not used for terrain generation, they are used by the Forge
        // implementation to fire Forge events.
        this.material = plant.getBottomMaterial();

        this.frequency = readInt(args.get(1), 1, 100);
        this.rarity = readRarity(args.get(2));
        this.minAltitude = readInt(args.get(3), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        this.maxAltitude = readInt(args.get(4), this.minAltitude, TerrainControl.WORLD_HEIGHT);
        this.sourceBlocks = readMaterials(args, 5);
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
        return "Plant(" + this.plant.getName() + "," + this.frequency + "," +
            this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);

        for (int i = 0; i < 64; i++)
        {
            int j = x + rand.nextInt(8) - rand.nextInt(8);
            int k = y + rand.nextInt(4) - rand.nextInt(4);
            int m = z + rand.nextInt(8) - rand.nextInt(8);
            if ((!world.isEmpty(j, k, m)) || (!this.sourceBlocks.contains(world.getMaterial(j, k - 1, m))))
                continue;

            this.plant.spawn(world, j, k, m);
        }
    }

}