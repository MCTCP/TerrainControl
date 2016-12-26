package com.khorn.terraincontrol.forge;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.generator.ForgeVanillaBiomeGenerator;
import com.khorn.terraincontrol.forge.generator.TXBiomeProvider;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.util.helpers.ReflectionHelper;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TXWorldType extends WorldType
{

    private final WorldLoader worldLoader;

    public TXWorldType(WorldLoader worldLoader)
    {
        super(PluginStandardValues.PLUGIN_NAME);
        this.worldLoader = Preconditions.checkNotNull(worldLoader, "worldLoader");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean showWorldInfoNotice()
    {
        return true;
    }

    @Override
    public BiomeProvider getBiomeProvider(World mcWorld)
    {
        // Ignore client worlds
        if (mcWorld.isRemote)
        {
            return super.getBiomeProvider(mcWorld);
        }

        ForgeWorld world = this.worldLoader.getOrCreateForgeWorld(mcWorld);
        if (world == null)
        {
            return super.getBiomeProvider(mcWorld);
        }

        Class<? extends BiomeGenerator> biomeGenClass = world.getConfigs().getWorldConfig().biomeMode;
        BiomeGenerator biomeGenerator = TerrainControl.getBiomeModeManager().createCached(biomeGenClass, world);
        BiomeProvider biomeProvider = this.createBiomeProvider(world, biomeGenerator);
        world.setBiomeGenerator(biomeGenerator);
        return biomeProvider;
    }

    /**
     * Gets the appropriate BiomeProvider. For the vanilla biome generator we
     * have to use BiomeProvider, for other biome modes TCBiomeProvider is
     * the right option.
     * 
     * @param world ForgeWorld instance, needed to instantiate the
     *            BiomeProvider.
     * @param biomeGenerator Biome generator.
     * @return The most appropriate BiomeProvider.
     */
    private BiomeProvider createBiomeProvider(ForgeWorld world, BiomeGenerator biomeGenerator)
    {
        World mcWorld = world.getWorld();
        BiomeProvider biomeProvider;
        if (biomeGenerator instanceof ForgeVanillaBiomeGenerator)
        {
            biomeProvider = mcWorld.provider.getBiomeProvider();
            // Let our biome generator depend on Minecraft's
            ((ForgeVanillaBiomeGenerator) biomeGenerator).setBiomeProvider(biomeProvider);
        } else
        {
            biomeProvider = new TXBiomeProvider(world, biomeGenerator);
            // Let Minecraft's biome generator depend on ours
            ReflectionHelper.setValueInFieldOfType(mcWorld.provider, BiomeProvider.class, biomeProvider);
        }

        return biomeProvider;
    }

    @Override
    public IChunkGenerator getChunkGenerator(World mcWorld, String generatorOptions)
    {
        ForgeWorld world = this.worldLoader.getWorld(WorldHelper.getName(mcWorld));
        if (world != null && world.getConfigs().getWorldConfig().ModeTerrain != WorldConfig.TerrainMode.Default)
        {
            return world.getChunkGenerator();
        } else
            return super.getChunkGenerator(mcWorld, generatorOptions);
    }

    @Override
    public int getMinimumSpawnHeight(World mcWorld)
    {
        LocalWorld world = this.worldLoader.getWorld(mcWorld);
        if (world == null)
        {
            // MCPC+ has an interesting load order sometimes
            return 64;
        }
        return world.getConfigs().getWorldConfig().waterLevelMax;
    }
}
