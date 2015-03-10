package com.khorn.terraincontrol.bukkit.generator;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class TCChunkGenerator extends ChunkGenerator
{
    private ChunkProviderTC chunkProviderTC;
    private boolean NotGenerate = false;
    private boolean initialized = false;

    /**
     * Called whenever a BukkitWorld instance becomes available.
     * 
     * @param _world
     *            The BukkitWorld instance.
     */
    public void onInitialize(BukkitWorld _world)
    {
        Preconditions.checkState(!this.initialized, "Already initialized");

        this.chunkProviderTC = new ChunkProviderTC(_world.getConfigs(), _world);

        WorldConfig.TerrainMode mode = _world.getConfigs().getWorldConfig().ModeTerrain;

        if (mode == WorldConfig.TerrainMode.NotGenerate)
            this.NotGenerate = true;

        this.initialized = true;
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        Material material = world.getHighestBlockAt(x, z).getType();
        return material.isSolid();
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes)
    {
        if (this.NotGenerate)
            return new short[16][];
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        BukkitChunkBuffer chunkBuffer = new BukkitChunkBuffer(chunkCoord);
        this.chunkProviderTC.generate(chunkBuffer);

        return chunkBuffer.accessRawValues();
    }
}