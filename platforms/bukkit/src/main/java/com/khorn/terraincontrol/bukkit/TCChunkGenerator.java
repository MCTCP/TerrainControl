package com.khorn.terraincontrol.bukkit;

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
    private int heightBits;
    private int heightBitsPlusFour;

    public TCChunkGenerator(TCPlugin _plugin)
    {
        this.plugin = _plugin;
    }

    public void Init(BukkitWorld _world)
    {
        this.heightBits = _world.getHeightBits();
        this.heightBitsPlusFour = heightBits + 4;
        this.chunkProviderTC = new ChunkProviderTC(_world.getSettings(), _world);

        WorldConfig.TerrainMode mode = _world.getSettings().ModeTerrain;

        if (mode == WorldConfig.TerrainMode.Normal || mode == WorldConfig.TerrainMode.OldGenerator)
            this.BlockPopulator.add(new TCBlockPopulator(_world));

        if (mode == WorldConfig.TerrainMode.NotGenerate)
            this.NotGenerate = true;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        this.plugin.onWorldInit(world);
        return this.BlockPopulator;
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        this.plugin.onWorldInit(world);

        Material material = world.getHighestBlockAt(x, z).getType();
        return material.isSolid();
    }

    @Override
    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes)
    {
        if (this.NotGenerate)
            return new byte[16][];
        byte[] BlockArray = this.chunkProviderTC.generate(x, z);

        byte[][] SectionBlocks = new byte[16][];

        // TODO Too slow, for fix need change generator output.
        int max_y = BlockArray.length / 256;
        for (int _x = 0; _x < 16; _x++)
            for (int _z = 0; _z < 16; _z++)
                for (int y = 0; y < max_y; y++)
                {
                    byte block = BlockArray[(_x << heightBitsPlusFour | _z << heightBits | y)];
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