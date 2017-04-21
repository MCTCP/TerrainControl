package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public class VeinGen extends Resource
{

    public int maxAltitude; // Maximum altitude of the vein
    public int maxRadius; // Maximum size of the vein in blocks (inclusive)
    public int minAltitude; // Minimum altitude of the vein
    public int minRadius; // Minimum size of the vein in blocks (inclusive)
    public int oreFrequency; // Frequency of the ores in the vein
    public int oreRarity; // Rarity of the ores in the vein
    public int oreSize; // Average size of a ore in the vein
    public MaterialSet sourceBlocks; // Blocks for the ore to spawn in
    public double veinRarity; // Chance for the vein to spawn in a chunk

    public VeinGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(9, args);

        this.material = readMaterial(args.get(0));
        this.minRadius = readInt(args.get(1), 10, 200);
        this.maxRadius = readInt(args.get(2), this.minRadius, 201);
        this.veinRarity = readDouble(args.get(3), 0.0000001, 100);
        this.oreSize = readInt(args.get(4), 1, 64);
        this.oreFrequency = readInt(args.get(5), 1, 100);
        this.oreRarity = readInt(args.get(6), 1, 100);
        this.minAltitude = readInt(args.get(7), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT - 1);
        this.maxAltitude = readInt(args.get(8), this.minAltitude, TerrainControl.WORLD_HEIGHT);
        this.sourceBlocks = readMaterials(args, 9);
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
        final VeinGen compare = (VeinGen) other;
        return this.veinRarity == compare.veinRarity
               && this.minRadius == compare.minRadius
               && this.maxRadius == compare.maxRadius
               && this.oreSize == compare.oreSize
               && this.oreFrequency == compare.oreFrequency
               && this.oreRarity == compare.oreRarity
               && this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                   : this.sourceBlocks.equals(compare.sourceBlocks));
    }

    @Override
    public int getPriority()
    {
        return 9;
    }

    /**
     * Returns the vein that starts in the chunk.
     * @param world  The world to spawn in.
     * @param chunkX The x of the chunk.
     * @param chunkZ The z of the chunk.
     * @return The vein that starts in the chunk, or null if there is no
     *         starting vein.
     */
    public Vein getVeinStartInChunk(LocalWorld world, int chunkX, int chunkZ)
    {
        // Create a random generator that is constant for this chunk and vein
        Random random = RandomHelper.getRandomForCoords(chunkX, chunkZ, this.material.hashCode() * (this.minRadius + this.maxRadius + 100) + world.getSeed());

        if (random.nextDouble() * 100.0 < this.veinRarity)
        {
            int veinX = chunkX * 16 + random.nextInt(16) + 8;
            int veinY = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);
            int veinZ = chunkZ * 16 + random.nextInt(16) + 8;
            int veinSize = RandomHelper.numberInRange(random, this.minRadius, this.maxRadius);
            return new Vein(veinX, veinY, veinZ, veinSize);
        }

        return null;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 29 * hash + super.hashCode();
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.veinRarity) ^ (Double.doubleToLongBits(this.veinRarity) >>> 32));
        hash = 29 * hash + this.minRadius;
        hash = 29 * hash + this.maxRadius;
        hash = 29 * hash + this.oreSize;
        hash = 29 * hash + this.oreFrequency;
        hash = 29 * hash + this.oreRarity;
        hash = 29 * hash + this.minAltitude;
        hash = 29 * hash + this.maxAltitude;
        hash = 29 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "Vein(" + this.material + "," + this.minRadius + "," + this.maxRadius + "," + this.veinRarity + "," + oreSize + "," +
            this.oreFrequency + "," + this.oreRarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        // Find all veins that reach this chunk, and spawn them
        int searchRadius = (this.maxRadius + 15) / 16;

        int currentChunkX = chunkCoord.getChunkX();
        int currentChunkZ = chunkCoord.getChunkZ();
        for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
        {
            for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
            {
                Vein vein = getVeinStartInChunk(world, searchChunkX, searchChunkZ);
                if (vein != null && vein.reachesChunk(currentChunkX, currentChunkZ))
                {
                    vein.spawn(world, random, chunkCoord, this);
                }
            }
        }
    }

}
