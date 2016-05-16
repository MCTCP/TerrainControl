package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.generator.ForgeVanillaBiomeGenerator;
import com.khorn.terraincontrol.forge.generator.TCBiomeProvider;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;

public class TCWorldType extends WorldType
{
    public ForgeWorld worldTC;

    public TCWorldType(String paramString)
    {
        super(paramString);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean showWorldInfoNotice()
    {
        return true;
    }

    @Override
    public BiomeProvider getBiomeProvider(World world)
    {
        try
        {
            if (world instanceof WorldClient)
            {
                return super.getBiomeProvider(world);
            }
        } catch (NoClassDefFoundError e)
        {
            // There isn't a WorldClient class, so we are on a stand-alone
            // server. Continue normally.
        }

        // Load everything
        File worldDirectory = new File(TerrainControl.getEngine().getTCDataFolder(), "worlds" + File.separator + world.getSaveHandler().getWorldDirectory().getName());

        if (!worldDirectory.exists())
        {
            System.out.println("TerrainControl: settings does not exist, creating defaults");

            if (!worldDirectory.mkdirs())
                System.out.println("TerrainControl: cant create folder " + worldDirectory.getAbsolutePath());
        }

        this.worldTC = new ForgeWorld(world.getWorldInfo().getWorldName());
        ServerConfigProvider config = new ServerConfigProvider(worldDirectory, worldTC);
        this.worldTC.Init(world, config);

        Class<? extends BiomeGenerator> biomeGenClass = worldTC.getConfigs().getWorldConfig().biomeMode;
        BiomeGenerator biomeManager = TerrainControl.getBiomeModeManager().createCached(biomeGenClass, worldTC);
        BiomeProvider chunkManager = createBiomeProvider(worldTC, biomeManager);
        this.worldTC.setBiomeManager(biomeManager);

        return chunkManager;
    }

    /**
     * Gets the appropriate BiomeProvider. For the vanilla biome generator we
     * have to use BiomeProvider, for other biome modes TCWorldChunkManager is
     * the right option.
     * 
     * @param world ForgeWorld instance, needed to instantiate the
     *            WorldChunkManager.
     * @param biomeGenerator Biome generator.
     * @return The most appropriate WorldChunkManager.
     */
    private BiomeProvider createBiomeProvider(ForgeWorld world, BiomeGenerator biomeGenerator)
    {
        if (biomeGenerator instanceof ForgeVanillaBiomeGenerator)
        {
            BiomeProvider worldChunkManager = super.getBiomeProvider(world.getWorld());
            ((ForgeVanillaBiomeGenerator) biomeGenerator).setBiomeProvider(worldChunkManager);
            return worldChunkManager;
        } else
        {
            return new TCBiomeProvider(this.worldTC, biomeGenerator);
        }
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions)
    {
        if (this.worldTC.getConfigs().getWorldConfig().ModeTerrain != WorldConfig.TerrainMode.Default)
        {
            return this.worldTC.getChunkGenerator();
        } else
            return super.getChunkGenerator(world, generatorOptions);
    }

    @Override
    public int getMinimumSpawnHeight(World mcWorld)
    {
        LocalWorld world = WorldHelper.toLocalWorld(mcWorld);
        if (world == null)
        {
            // MCPC+ has an interesting load order sometimes
            return 64;
        }
        return world.getConfigs().getWorldConfig().waterLevelMax;
    }
}
