package com.pg85.otg.forge;

import java.io.File;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.forge.biomes.OTGBiomeProvider;
import com.pg85.otg.forge.generator.ForgeVanillaBiomeGenerator;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.util.helpers.ReflectionHelper;

import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OTGWorldType extends WorldType
{

    public final WorldLoader worldLoader;

    public OTGWorldType(WorldLoader worldLoader)
    {
        super(PluginStandardValues.PLUGIN_NAME_SHORT);
        this.worldLoader = worldLoader;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasInfoNotice()
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

        // Create dirs for a new world if necessary (only needed for overworld, when creating a new OTG world)
        if(mcWorld.provider.getDimension() == 0)
        {
        	// Happens when the MP server is started
        	if(OTG.GetDimensionsConfig() == null)
        	{
        		// Check if a dimsconfig is saved for the world
        		DimensionsConfig savedConfig = DimensionsConfig.LoadFromFile(mcWorld.getSaveHandler().getWorldDirectory());
        		if(savedConfig != null)
        		{
        			OTG.SetDimensionsConfig(savedConfig);
        		} else {
            		// This is a new world, create a DimensionsConfig for it based on modpack config or worldconfig.
        			DimensionsConfig modPackConfig = DimensionsConfig.getModPackConfig(mcWorld.getSaveHandler().getWorldDirectory().getName());
        	        if(modPackConfig != null)
        	        {
        	        	DimensionsConfig dimsConfig = new DimensionsConfig(mcWorld.getSaveHandler().getWorldDirectory());
        	        	dimsConfig.Overworld = modPackConfig.Overworld;
        	        	dimsConfig.Dimensions = modPackConfig.Dimensions;
        	        	OTG.SetDimensionsConfig(dimsConfig);
        	        	OTG.GetDimensionsConfig().Save();
        	        } else {
        	        	// Create config from worldconfig, only works if worldname is the same as preset name
        	        	WorldConfig worldConfig = ((ForgeEngine)OTG.getEngine()).LoadWorldConfigFromDisk(new File(OTG.getEngine().getOTGDataFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + mcWorld.getSaveHandler().getWorldDirectory().getName()));
        	        	if(worldConfig != null)
        	        	{
        	        		DimensionsConfig dimsConfig = new DimensionsConfig(mcWorld.getSaveHandler().getWorldDirectory());
        	        		dimsConfig.Overworld = new DimensionConfig(mcWorld.getSaveHandler().getWorldDirectory().getName(), worldConfig);
        	        		for(String dimToAdd : worldConfig.Dimensions)
        	        		{
        	        			WorldConfig dimWorldConfig = ((ForgeEngine)OTG.getEngine()).LoadWorldConfigFromDisk(new File(OTG.getEngine().getOTGDataFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + dimToAdd));
        	        			if(dimWorldConfig != null)
        	        			{
        	        				dimsConfig.Dimensions.add(new DimensionConfig(dimToAdd, dimWorldConfig));
        	        			}
        	        		}
        	        		OTG.SetDimensionsConfig(dimsConfig);
        	        		OTG.GetDimensionsConfig().Save();
        	        	}
        	        }
        		}
        	}
	        File worldDirectory = new File(OTG.getEngine().getOTGDataFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + OTG.GetDimensionsConfig().Overworld.PresetName);
	
	        if (!worldDirectory.exists())
	        {
	            System.out.println("OpenTerrainGenerator: settings does not exist, creating defaults");

	            if (!worldDirectory.mkdirs())
	            {
	                System.out.println("OpenTerrainGenerator: cant create folder " + worldDirectory.getAbsolutePath());
	            }
	        }

	        File worldObjectsDir = new File(OTG.getEngine().getOTGDataFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + OTG.GetDimensionsConfig().Overworld.PresetName + File.separator + WorldStandardValues.WORLD_OBJECTS_DIRECTORY_NAME);
	        worldObjectsDir.mkdirs();

	        File worldBiomesDir = new File(OTG.getEngine().getOTGDataFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + OTG.GetDimensionsConfig().Overworld.PresetName + File.separator + WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME);
	        worldBiomesDir.mkdirs();
        }

    	// For server
        if(!mcWorld.getMinecraftServer().isSinglePlayer())
        {
	        WorldSettings worldSettings = new WorldSettings(mcWorld.getWorldInfo().getSeed(), mcWorld.getWorldInfo().getGameType(), mcWorld.getWorldInfo().isMapFeaturesEnabled(), mcWorld.getWorldInfo().isHardcoreModeEnabled(), OTGPlugin.txWorldType);
	        worldSettings.setGeneratorOptions("OpenTerrainGenerator");
	        mcWorld.getWorldInfo().setAllowCommands(mcWorld.getWorldInfo().areCommandsAllowed());
	        mcWorld.getWorldInfo().populateFromWorldSettings(worldSettings);
    	}
        //

        ForgeWorld world = this.worldLoader.getOrCreateForgeWorld(mcWorld);
        if (world == null) // TODO: When does this happen, if the world is not an OTG world?
        {
            throw new RuntimeException("This shouldn't happen, please contact team OTG about this crash.");
            //return super.getBiomeProvider(mcWorld);
        }

        Class<? extends BiomeGenerator> biomeGenClass = world.getConfigs().getWorldConfig().biomeMode;
        BiomeGenerator biomeGenerator = OTG.getBiomeModeManager().createCached(biomeGenClass, world);
        BiomeProvider biomeProvider = this.createBiomeProvider(world, biomeGenerator);
        world.setBiomeGenerator(biomeGenerator);
        return biomeProvider;
    }

    /**
     * Gets the appropriate BiomeProvider. For the vanilla biome generator we
     * have to use BiomeProvider, for other biome modes OTGBiomeProvider is
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
        } else {
            biomeProvider = new OTGBiomeProvider(world, biomeGenerator);
            // Let Minecraft's biome generator depend on ours
            ReflectionHelper.setValueInFieldOfType(mcWorld.provider, BiomeProvider.class, biomeProvider);
        }

        return biomeProvider;
    }

    @Override
    public net.minecraft.world.gen.IChunkGenerator getChunkGenerator(World mcWorld, String generatorOptions)
    {
        ForgeWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(mcWorld);
        if (world != null && world.getConfigs().getWorldConfig().ModeTerrain != WorldConfig.TerrainMode.Default)
        {
            return world.getChunkGenerator();
        } else
            return super.getChunkGenerator(mcWorld, generatorOptions);
    }

    @Override
    public int getMinimumSpawnHeight(World mcWorld)
    {
        LocalWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(mcWorld);
        if (world == null)
        {
            // MCPC+ has an interesting load order sometimes
            return 64;
        }
        return world.getConfigs().getWorldConfig().waterLevelMax;
    }
}
