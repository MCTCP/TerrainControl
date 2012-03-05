package com.Khorn.TerrainControl.Bukkit;

import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.Generator.ChunkProviderTC;
import net.minecraft.server.Block;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TCChunkGenerator extends ChunkGenerator
{
    private BukkitWorld world;
    private ChunkProviderTC chunkProviderTC;
    private ArrayList<BlockPopulator> BlockPopulator = new ArrayList<BlockPopulator>();
    private boolean NotGenerate = false;
    private TCPlugin plugin;

    public TCChunkGenerator(TCPlugin _plugin)
    {
        this.plugin = _plugin;
    }

    public void Init(BukkitWorld _world)
    {
        this.world = _world;
        this.chunkProviderTC = new ChunkProviderTC(_world.getSettings(), _world);

        if (this.world.getSettings().ModeTerrain == WorldConfig.TerrainMode.Normal)
            this.BlockPopulator.add(new TCBlockPopulator(_world));

        if (this.world.getSettings().ModeTerrain == WorldConfig.TerrainMode.NotGenerate)
            this.NotGenerate = true;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        this.plugin.WorldInit(world);
        return this.BlockPopulator;
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        this.plugin.WorldInit(world);

        int i = ((CraftWorld) world).getHandle().a(x, z); // TODO - Fix obfuscation
        return i != 0 && Block.byId[i].material.isSolid();
    }

    @Override
    public byte[] generate(World world, Random random, int x, int z)
    {
        if (this.NotGenerate)
            return new byte[16 * 16 * this.world.getHeight()];
        else
            return this.chunkProviderTC.generate(x, z);
    }
}