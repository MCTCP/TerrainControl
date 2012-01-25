package com.Khorn.TerrainControl.Bukkit;


import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.Generator.ChunkProviderTC;
import com.Khorn.TerrainControl.LocalWorld;
import org.bukkit.World;
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


    public void Init(BukkitWorld _world)
    {
        this.world = _world;
        this.chunkProviderTC = new ChunkProviderTC(_world.getSettings(), _world);

        if (this.world.getSettings().ModeTerrain == WorldConfig.TerrainMode.TerrainTest)
            this.NotGenerate = true;
        else
            this.BlockPopulator.add(new TCBlockPopulator(_world));


    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        return this.BlockPopulator;
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {

        return super.canSpawn(world, x, z);    //To change body of overridden methods use File | Settings | File Templates.
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
