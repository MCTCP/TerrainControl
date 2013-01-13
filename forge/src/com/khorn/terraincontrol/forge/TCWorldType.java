package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

import java.io.File;

public class TCWorldType extends WorldType
{
    public SingleWorld worldTC;
    private TCPlugin plugin;

    public TCWorldType(TCPlugin plugin, String paramString)
    {
        super(WorldHelper.getNextWorldTypeID(), paramString);
        this.plugin = plugin;
    }

    // Actually: getBiomeManager
    @Override
    public WorldChunkManager getChunkManager(World world)
    {
        try
        {
            if (world instanceof WorldClient)
            {
                return super.getChunkManager(world);
            }
        } catch (NoClassDefFoundError e)
        {
            // There isn't a WorldClient class, so we are on a stand-alone
            // server. Continue normally.
        }

        // Restore old biomes
        SingleWorld.restoreBiomes();

        // Load everything
        File worldDirectory = new File(plugin.terrainControlDirectory, "worlds" + File.separator + world.getSaveHandler().getSaveDirectoryName());

        if (!worldDirectory.exists())
        {
            System.out.println("TerrainControl: settings does not exist, creating defaults");

            if (!worldDirectory.mkdirs())
                System.out.println("TerrainControl: cant create folder " + worldDirectory.getAbsolutePath());
        }

        this.worldTC = new SingleWorld(world.getSaveHandler().getSaveDirectoryName());
        WorldConfig config = new WorldConfig(worldDirectory, worldTC, false);
        this.worldTC.Init(world, config);

        WorldChunkManager chunkManager = null;

        Class<? extends BiomeGenerator> biomeManagerClass = this.worldTC.getSettings().biomeMode;

        if (biomeManagerClass == TerrainControl.getBiomeModeManager().VANILLA)
        {
            chunkManager = super.getChunkManager(world);
        } else
        {
            chunkManager = new TCWorldChunkManager(this.worldTC);
            BiomeGenerator biomeManager = TerrainControl.getBiomeModeManager().create(biomeManagerClass, worldTC, new BiomeCacheWrapper(chunkManager));
            ((TCWorldChunkManager) chunkManager).setBiomeManager(biomeManager);
            this.worldTC.setBiomeManager(biomeManager);
        }

        return chunkManager;
    }

    @Override
    public IChunkProvider getChunkGenerator(World world, String generatorOptions)
    {
        if (this.worldTC.getSettings().ModeTerrain != WorldConfig.TerrainMode.Default)
        {
            return this.worldTC.getChunkGenerator();
        } else
            return super.getChunkGenerator(world, generatorOptions);
    }

    @Override
    public int getMinimumSpawnHeight(World world)
    {
        return WorldHelper.toLocalWorld(world).getSettings().waterLevelMax;
    }
}
