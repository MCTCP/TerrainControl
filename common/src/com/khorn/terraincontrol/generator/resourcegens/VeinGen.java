package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MathHelper;
import com.khorn.terraincontrol.util.RandomHelper;

import java.util.List;
import java.util.Random;

public class VeinGen extends Resource
{
    public double veinRarity; // Chance for the vein to spawn in a chunk
    public int minRadius; // Minimum size of the vein in blocks (inclusive)
    public int maxRadius; // Maximum size of the vein in blocks (inclusive)
    public int oreSize; // Average size of a ore in the vein
    public int oreFrequency; // Frequency of the ores in the vein
    public int oreRarity; // Rarity of the ores in the vein
    public int minAltitude; // Minimum altitude of the vein
    public int maxAltitude; // Maximum altitude of the vein
    public List<Integer> sourceBlocks; // Blocks for the ore to spawn in

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {

    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int currentChunkX, int currentChunkZ)
    {
        // Find all veins that reach this chunk, and spawn them
        int searchRadius = (this.maxRadius + 15) / 16;

        for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
        {
            for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
            {
                Vein vein = getVeinStartInChunk(world, searchChunkX, searchChunkZ);
                if (vein != null && vein.reachesChunk(currentChunkX, currentChunkZ))
                {
                    vein.spawn(world, random, currentChunkX, currentChunkZ, this);
                }
            }
        }
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(9, args);

        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));
        minRadius = readInt(args.get(1), 10, 200);
        maxRadius = readInt(args.get(2), minRadius, 201);
        veinRarity = readDouble(args.get(3), 0.0000001, 100);
        oreSize = readInt(args.get(4), 1, 64);
        oreFrequency = readInt(args.get(5), 1, 100);
        oreRarity = readInt(args.get(6), 1, 100);
        minAltitude = readInt(args.get(7), TerrainControl.worldDepth, TerrainControl.worldHeight - 1);
        maxAltitude = readInt(args.get(8), minAltitude + 1, TerrainControl.worldHeight);
        sourceBlocks = readBlockIds(args, 9);
    }

    @Override
    public String makeString()
    {
        String result = "Vein(" + makeMaterial(blockId, blockData) + "," + minRadius + "," + maxRadius + "," + veinRarity + ",";
        result += oreSize + "," + oreFrequency + "," + oreRarity + "," + minAltitude + "," + maxAltitude + makeMaterial(sourceBlocks) + ")";
        return result;
    }

    /**
     * Returns the vein that starts in the chunk.
     * 
     * @param chunkX The x of the chunk.
     * @param chunkZ The z of the chunk.
     * @return The vein that starts in the chunk, or null if there is no starting vein.
     */
    public Vein getVeinStartInChunk(LocalWorld world, int chunkX, int chunkZ)
    {
        // Create a random generator that is constant for this chunk and vein
        Random random = RandomHelper.getRandomForCoords(chunkX, chunkZ, (blockId * 16 + blockData) * (minRadius + maxRadius + 100) + world.getSeed());

        if (random.nextDouble() * 100.0 < veinRarity)
        {
            int veinX = chunkX * 16 + random.nextInt(16) + 8;
            int veinY = MathHelper.getRandomNumberInRange(random, minAltitude, maxAltitude);
            int veinZ = chunkZ * 16 + random.nextInt(16) + 8;
            int veinSize = MathHelper.getRandomNumberInRange(random, minRadius, maxRadius);
            return new Vein(veinX, veinY, veinZ, veinSize);
        }

        return null;
    }
}
