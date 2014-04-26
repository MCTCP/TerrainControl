package com.khorn.terraincontrol.bukkit.generator;

import com.khorn.terraincontrol.util.ChunkCoordinate;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TCChunkGenerator extends ChunkGenerator
{
    private ChunkProviderTC chunkProviderTC;
    private ArrayList<BlockPopulator> BlockPopulator = new ArrayList<BlockPopulator>();
    private boolean NotGenerate = false;
    private TCPlugin plugin;

    public TCChunkGenerator(TCPlugin _plugin)
    {
        this.plugin = _plugin;
    }

    /**
     * Initializes the world if it hasn't already been initialized.
     * 
     * @param world
     *            The world of this generator.
     */
    private void makeSureWorldIsInitialized(World world)
    {
        if (this.chunkProviderTC == null)
        {
            // Not yet initialized, do it now
            this.plugin.onWorldInit(world);
        }
    }

    /**
     * Called whenever a BukkitWorld instance becomes available.
     * 
     * @param _world
     *            The BukkitWorld instance.
     */
    public void onInitialize(BukkitWorld _world)
    {
        this.chunkProviderTC = new ChunkProviderTC(_world.getSettings(), _world);

        WorldConfig.TerrainMode mode = _world.getSettings().worldConfig.ModeTerrain;

        if (mode == WorldConfig.TerrainMode.Normal || mode == WorldConfig.TerrainMode.OldGenerator)
            this.BlockPopulator.add(new TCBlockPopulator(_world));

        if (mode == WorldConfig.TerrainMode.NotGenerate)
            this.NotGenerate = true;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        makeSureWorldIsInitialized(world);

        return this.BlockPopulator;
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        makeSureWorldIsInitialized(world);

        Material material = world.getHighestBlockAt(x, z).getType();
        return material.isSolid();
    }

    @Override
    public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes)
    {
        makeSureWorldIsInitialized(world);

        if (this.NotGenerate)
            return new byte[16][];
        byte[] BlockArray = this.chunkProviderTC.generate(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));

        byte[][] SectionBlocks = new byte[16][];

        // TODO Too slow, for fix need change generator output.
        int max_y = BlockArray.length / 256;
        for (int _x = 0; _x < 16; _x++)
            for (int _z = 0; _z < 16; _z++)
                for (int y = 0; y < max_y; y++)
                {
                    byte block = BlockArray[(_x << ChunkProviderTC.HEIGHT_BITS_PLUS_FOUR | _z << ChunkProviderTC.HEIGHT_BITS | y)];
                    if (block != 0)
                    {
                        int sectionId = y >> 4;
                        if (SectionBlocks[sectionId] == null)
                        {
                            SectionBlocks[sectionId] = new byte[4096];
                        }
                        SectionBlocks[sectionId][(y & 0xF) << 8 | _z << 4 | _x] = block;
                    }
                }
        return SectionBlocks;

    }
}