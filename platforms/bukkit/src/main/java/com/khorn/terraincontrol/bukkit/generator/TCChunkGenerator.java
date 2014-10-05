package com.khorn.terraincontrol.bukkit.generator;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.util.ChunkCoordinate;
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
        this.chunkProviderTC = new ChunkProviderTC(_world.getConfigs(), _world);

        WorldConfig.TerrainMode mode = _world.getConfigs().getWorldConfig().ModeTerrain;

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
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        BukkitChunkBuffer chunkBuffer = new BukkitChunkBuffer(chunkCoord);
        this.chunkProviderTC.generate(chunkBuffer);

        return chunkBuffer.accessBytes();
    }
}