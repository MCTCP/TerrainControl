package com.pg85.otg.forge;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfigFinder;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.io.FileSettingsReader;
import com.pg85.otg.configuration.io.FileSettingsWriter;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.configuration.world.WorldConfig.DefaulWorldData;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.forge.biomes.BiomeRegistryManager;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.generator.ForgeChunkBuffer;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.LocalMaterialData;
import com.pg85.otg.util.helpers.FileHelper;
import com.pg85.otg.util.minecraftTypes.DefaultBiome;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;
import com.pg85.otg.forge.util.ForgeLogger;
import com.pg85.otg.forge.util.ForgeMaterialData;
import com.pg85.otg.generator.ChunkBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ForgeEngine extends OTGEngine
{	
	public WorldConfig LoadWorldConfigFromDisk(File worldDir)
	{
        File worldConfigFile = new File(worldDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        if(!worldConfigFile.exists())
        {
        	return null;
        }
        SettingsMap settingsMap = FileSettingsReader.read(worldDir.getName(), worldConfigFile);
        return new WorldConfig(worldDir, settingsMap, null, null);
	}
	
	// OTG+

    public void onSave(World world)
    {
    	//OTG.log(LogMarker.INFO, "ForgeEngine onSave");
    	ForgeWorld forgeWorld = (ForgeWorld) getWorld(world);
    	if(forgeWorld != null && forgeWorld.getObjectSpawner().saveRequired && !forgeWorld.GetWorldSession().getPreGeneratorIsRunning())
    	{
    		forgeWorld.getStructureCache().SaveToDisk();
    	}
    }

    long lastPregeneratorStatusUpdateTime = System.currentTimeMillis();
    public void ProcessPregeneratorTick()
    {
    	for(LocalWorld world : getAllWorlds())
    	{
    		((ForgeWorldSession)world.GetWorldSession()).getPregenerator().ProcessTick();
    	}
    	
    	if(System.currentTimeMillis() - lastPregeneratorStatusUpdateTime  > 1000l)
    	{
    		lastPregeneratorStatusUpdateTime = System.currentTimeMillis();
        	ServerPacketManager.SendPregeneratorStatusPacketToAllPlayers(FMLCommonHandler.instance().getMinecraftServerInstance());
    	}
    }

	//
    
	protected WorldLoader worldLoader;

    public ForgeEngine(WorldLoader worldLoader)
    {
        super(new ForgeLogger());
        this.worldLoader = worldLoader;
    }

    public Biome getRegisteredBiome(int id)
    {
    	if(ids == null)
    	{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						field.setAccessible(true);
						ids = (BiMap<Integer, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    	}

    	try
    	{
    		return ids.get(id);
    	} catch(NullPointerException e)
    	{
    		return null;
    	}
    }

    BiMap<Integer, Biome> ids = null;
    public int getBiomeRegistryId(Biome biome)
    {
    	if(ids == null)
    	{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						field.setAccessible(true);
						ids = (BiMap<Integer, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    	}

    	try
    	{
    		return biome == null ? -1 : ids.inverse().get(biome);
    	}
    	catch(NullPointerException ex)
    	{
    		return -1;
    	}
    }

    public int getBiomeRegistryId(ResourceLocation resourceLocation)
    {
    	if(ids == null)
    	{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						field.setAccessible(true);
						ids = (BiMap<Integer, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    	}

		if(names == null)
		{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				boolean isFirst = true;
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						if(isFirst)
						{
							isFirst = false; // Skip the first BiMap, which should be id's. TODO: Make this prettier.
							continue;
						}
						field.setAccessible(true);
						names = (BiMap<ResourceLocation, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		Biome biome = names.get(resourceLocation);
		if(biome != null)
		{
			return ids.inverse().get(biome);
		}

		if(1 == 1) { throw new RuntimeException("This should not happen, please submit a bug report to the OTG git."); }

		return -1;
    }

    public void registerForgeBiome(ResourceLocation resourceLocation, Biome biome)
    {
    	OTG.log(LogMarker.TRACE, "Registering biome " + resourceLocation.toString());

		if(names == null)
		{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				boolean isFirst = true;
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						if(isFirst)
						{
							isFirst = false; // Skip the first BiMap, which should be id's. TODO: Make this prettier.
							continue;
						}
						field.setAccessible(true);
						names = (BiMap<ResourceLocation, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

        names.put(resourceLocation, biome);
    }

    BiMap<ResourceLocation, Biome> names = null;

    public int registerForgeBiomeWithId(ResourceLocation resourceLocation, Biome biome)
    {
    	return registerForgeBiomeWithId(-1, resourceLocation, biome);
    }
    
    public int registerForgeBiomeWithId(int id, ResourceLocation resourceLocation, Biome biome)
    {
    	OTG.log(LogMarker.TRACE, "Registering biome " + resourceLocation.toString() + " " + id);

		BitSet biomeRegistryAvailabiltyMap = getBiomeRegistryAvailabiltyMap();
    	
    	// Get the next free id
    	if(id == -1)
    	{
    		id = biomeRegistryAvailabiltyMap.nextClearBit(0);
    	}
    	
    	if(ids == null)
    	{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						field.setAccessible(true);
						ids = (BiMap<Integer, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    	}

		if(names == null)
		{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				boolean isFirst = true;
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						if(isFirst)
						{
							isFirst = false; // Skip the first BiMap, which should be id's. TODO: Make this prettier.
							continue;
						}
						field.setAccessible(true);
						names = (BiMap<ResourceLocation, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		names.put(resourceLocation, biome);

        Biome biomeAtId = ids.get(id);
        if(biomeAtId != null)
        {
        	throw new RuntimeException("Tried to register biome " + resourceLocation.toString() + " to a id " + id + " but it is occupied by biome: " + biomeAtId.getRegistryName().toString() + ". This can happen when using the CustomBiomes setting in the world config or when changing mod/biome configurations for previously created worlds. OTG 1.12.2 v7 and above use dynamic biome id's for new worlds, this avoids the problem completely.");
        }

        ids.put(id, biome);

		biomeRegistryAvailabiltyMap.set(id, true); // Mark the id as used

        return id;
    }

    public void unRegisterForgeBiome(ResourceLocation resourceLocation)
    {
		OTG.log(LogMarker.INFO, "Unregistering biome " + resourceLocation.toString());

		Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);

		BitSet biomeRegistryAvailabiltyMap = getBiomeRegistryAvailabiltyMap();
		try
		{
			int biomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(biome);
			// If this biome uses replaceToBiomeName and has an id > 255 then it is not actually registered in the biome id
			// registry and biomeId will be 0. Check if biomeId is actually registered to this biome.
			if(biomeId > -1 && ((ForgeEngine)OTG.getEngine()).getRegisteredBiome(biomeId) == biome)
			{
				biomeRegistryAvailabiltyMap.set(biomeId, false); // This should be enough to make Forge re-use the biome id
			}
		}
		catch(IndexOutOfBoundsException ex)
		{
			// This can happen when:
			// A. The dimension was unloaded automatically because noone was in it and then the world was unloaded because the server shut down.
			// B. The dimensions was unloaded automatically because noone was in it and then deleted and recreated.

			// This can happen when a world was deleted and recreated and the index was set as "can be re-used" but when re-registering the biomes
			// it wasn't set back to "used" because it looked like the biome registry already had the biome properly registered.

			throw new RuntimeException("Could not unregister " + biome.biomeName + ", aborting.");

			//biomeRegistryAvailabiltyMap.set(localBiome.getIds().getSavedId(), false); // This should be enough to make Forge re-use the biome id
		}

		if(names == null)
		{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				boolean isFirst = true;
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						if(isFirst)
						{
							isFirst = false; // Skip the first BiMap, which should be id's. TODO: Make this prettier.
							continue;
						}
						field.setAccessible(true);
						names = (BiMap<ResourceLocation, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

    	if(ids == null)
    	{
			try {
				Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BiMap.class))
					{
						field.setAccessible(true);
						ids = (BiMap<Integer, Biome>)field.get(ForgeRegistries.BIOMES);
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    	}

        names.remove(resourceLocation, biome);

		try
		{
			int biomeId = ids.inverse().get(biome);
			ids.remove(biomeId, biome);
		}
		catch(NullPointerException e) { }
    }

	public BitSet getBiomeRegistryAvailabiltyMap()
	{
		BitSet biomeRegistryAvailabiltyMap = null;
		try {
			Field[] fields = ForgeRegistries.BIOMES.getClass().getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(BitSet.class))
				{
					field.setAccessible(true);
					biomeRegistryAvailabiltyMap = (BitSet)field.get(ForgeRegistries.BIOMES);
			        break;
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return biomeRegistryAvailabiltyMap;
	}

    public WorldLoader getWorldLoader()
    {
    	return worldLoader;
    }

    public boolean getCartographerEnabled()
    {
    	ForgeWorld world = getOverWorld(); // If overworld is null then the overworld is not an OTG world
    	return world == null ? false : world.getConfigs().getWorldConfig().Cartographer;
    }

    public ForgeWorld getOverWorld()
    {
		ArrayList<LocalWorld> allWorlds = getAllWorlds();
		for(LocalWorld world : allWorlds)
		{
			// Overworld can be null for MP clients who are in a dimension, try 'overworld'
			if(
				(
					((ForgeWorld)world).getWorld() != null && 
					((ForgeWorld)world).getWorld().provider != null && 
					((ForgeWorld)world).getWorld().provider.getDimension() == 0
				) || (
						((ForgeWorld)world).getName().equals("overworld")
				)				
			)
			{
				return (ForgeWorld)world;
			}
		}
		return null;
    }

    public ForgeWorld getWorldByDimId(int dimensionId)
    {
    	ForgeWorld forgeWorld;
    	if(dimensionId == 0)
    	{
    		forgeWorld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
    	} else {
        	DimensionType dimType = DimensionManager.getProviderType(dimensionId);
    		forgeWorld = (ForgeWorld)OTG.getWorld(dimType.getName());
    	}
    	return forgeWorld;
    }

    public ForgeWorld getUnloadedWorldByDimId(int dimensionId)
    {
    	ForgeWorld forgeWorld;
    	if(dimensionId == 0)
    	{
    		forgeWorld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
    	} else {
        	DimensionType dimType = DimensionManager.getProviderType(dimensionId);
    		forgeWorld = (ForgeWorld)OTG.getUnloadedWorld(dimType.getName());
    	}
    	return forgeWorld;
    }
    
    public ForgeWorld getWorld(World world)
    {
    	if(world.provider.getDimension() == 0)
    	{
    		return (ForgeWorld)((ForgeEngine)OTG.getEngine()).getOverWorld();
    	}
    	else if(world.provider.getDimension() > 1)
    	{
			LocalWorld localWorld = this.worldLoader.getWorld(world.provider.getDimensionType().getName());
			if(localWorld == null)
			{
				return this.worldLoader.getUnloadedWorld(world.provider.getDimensionType().getName());
			}
    		return this.worldLoader.getWorld(world.provider.getDimensionType().getName());
    	}
    	ForgeWorld forgeWorld = this.worldLoader.getWorld(world.getWorldInfo().getWorldName());
		if(forgeWorld == null)
		{
			return this.worldLoader.getUnloadedWorld(world.getWorldInfo().getWorldName());
		}

        return forgeWorld;
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        return this.worldLoader.getWorld(name);
    }

    @Override
    public LocalWorld getUnloadedWorld(String name)
    {
    	return this.worldLoader.getUnloadedWorld(name);
    }

    public ArrayList<ForgeWorld> getUnloadedWorlds()
    {
    	return this.worldLoader.getUnloadedWorlds();
    }

    @Override
    public ArrayList<LocalWorld> getAllWorlds()
    {
    	return this.worldLoader.getAllWorlds();
    }
    
    @Override
    public File getOTGDataFolder()
    {
        return this.worldLoader.getConfigsFolder();
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(this.getOTGDataFolder(), PluginStandardValues.BO_DirectoryName);
    }

    @Override
    public File getWorldsDirectory()
    {
        return new File(this.getOTGDataFolder(), PluginStandardValues.PresetsDirectoryName);
    }
    
    public void CreateDefaultOTGWorld(String worldName)
    {
		// Create default OTG world

    	File worldDir = new File(OTG.getEngine().getWorldsDirectory() + "/" + worldName);
        List<File> worldDirs = new ArrayList<File>(2);
        worldDirs.add(new File(worldDir, WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME));
        worldDirs.add(new File(worldDir, WorldStandardValues.WORLD_OBJECTS_DIRECTORY_NAME));
        FileHelper.makeFolders(worldDirs);		
		
		// World config
		DefaulWorldData defaultWorldData = WorldConfig.CreateDefaultOTGWorldConfig(worldDir, worldName);
		SettingsMap settingsMap = defaultWorldData.settingsMap;
		WorldConfig defaultWorldConfig = defaultWorldData.worldConfig;			
    	FileSettingsWriter.writeToFile(settingsMap, new File(worldDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME), WorldConfig.ConfigMode.WriteAll);
    	
    	// Biome configs
        // Build a set of all biomes to load
        Collection<BiomeLoadInstruction> biomesToLoad = new HashSet<BiomeLoadInstruction>();

        // Loop through all default biomes and create the default
        // settings for them
        List<BiomeLoadInstruction> defaultBiomes = new ArrayList<BiomeLoadInstruction>();
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            int id = defaultBiome.Id;
            BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(ForgeMojangSettings.fromId(id), ForgeWorld.STANDARD_WORLD_HEIGHT);
            defaultBiomes.add(instruction);
        }
        
        // If we're creating a new world with new configs then add the default biomes
        for (BiomeLoadInstruction defaultBiome : defaultBiomes)
        {
    		biomesToLoad.add(new BiomeLoadInstruction(defaultBiome.getBiomeName(), defaultBiome.getBiomeTemplate()));
        }
        
        List<File> biomeDirs = new ArrayList<File>(1);
        biomeDirs.add(new File(worldDir, WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME));
        
        // Load all files
        BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder(OTG.getPluginConfig().biomeConfigExtension);
        Map<String, BiomeConfigStub> biomeConfigStubs = biomeConfigFinder.findBiomes(defaultWorldConfig, null, defaultWorldConfig.worldHeightScale, biomeDirs, biomesToLoad);
        
        // Write all biomes

        for (BiomeConfigStub biomeConfigStub : biomeConfigStubs.values())
        {
            // Settings reading
            BiomeConfig biomeConfig = new BiomeConfig(biomeConfigStub.getLoadInstructions(), biomeConfigStub, biomeConfigStub.getSettings(), defaultWorldConfig);

            // Settings writing
            File writeFile = biomeConfigStub.getFile();
            if (!biomeConfig.biomeExtends.isEmpty())
            {
                writeFile = new File(writeFile.getAbsolutePath() + ".inherited");
            }
            FileSettingsWriter.writeToFile(biomeConfig.getSettingsAsMap(), writeFile, defaultWorldConfig.SettingsMode);
        }
    }
    
    @Override
    public LocalMaterialData readMaterial(String input) throws InvalidConfigException
    {
        return ForgeMaterialData.ofString(input);
    }

    @Override
    public LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return ForgeMaterialData.ofDefaultMaterial(defaultMaterial, blockData);
    }
	
	public void UnloadAndUnregisterAllWorlds()
	{	
    	// Default settings are not restored on world unload / server quit because this was causing problems
    	// (unloading dimensions while their worlds were still ticking etc)
    	// Unload all world and biomes on server start / connect instead, for client where data is kept when leaving the game.

    	OTGDimensionManager.UnloadAllCustomDimensionData();
    	this.worldLoader.unloadAllWorlds();
        // Clear the BiomeDictionary (it will be refilled when biomes are loaded in createBiomeFor)
    	this.worldLoader.clearBiomeDictionary(null);
    	BiomeRegistryManager.ClearOTGBiomeIds();
	}

	@Override
	public String GetPresetName(String worldName)
	{
		// If this dim's name is the same as the preset worldname then this is an OTG overworld
		if(worldName.equals("overworld") || worldName.equals(OTG.GetDimensionsConfig().WorldName))
    	{
    		return OTG.GetDimensionsConfig().Overworld.PresetName;	
    	} else {
    		// If this is an OTG dim other than the overworld then the world name will always match the preset name
    		return worldName;
    	}
	}

	public static LinkedHashMap<String, DimensionConfigGui> presets = new LinkedHashMap<String, DimensionConfigGui>();
	public static void loadPresets()
	{
		presets.clear();
		
	    ArrayList<String> worldNames = new ArrayList<String>();
	    File OTGWorldsDirectory = new File(OTG.getEngine().getOTGDataFolder().getAbsolutePath() + "/" + PluginStandardValues.PresetsDirectoryName);
	    if(OTGWorldsDirectory.exists() && OTGWorldsDirectory.isDirectory())
	    {
	    	for(File worldDir : OTGWorldsDirectory.listFiles())
	    	{
	    		if(worldDir.isDirectory() && !worldDir.getName().toLowerCase().trim().startsWith("dim-"))
	    		{
	    			for(File file : worldDir.listFiles())
	    			{
	    				if(file.getName().equals("WorldConfig.ini"))
	    				{
			    			worldNames.add(worldDir.getName());
			    			WorldConfig worldConfig = ((ForgeEngine)OTG.getEngine()).LoadWorldConfigFromDisk(worldDir);
					        presets.put(worldDir.getName(), new DimensionConfigGui(worldDir.getName(), worldConfig));
					        break;
	    				}
	    			}
	    		}
	    	}
		}
	}
	
	@Override
	public boolean fireReplaceBiomeBlocksEvent(int x, int z, ChunkBuffer chunkBuffer,
			LocalWorld localWorld)
	{
        return ForgeEventFactory.onReplaceBiomeBlocks(((ForgeWorld)localWorld).getChunkGenerator(), x, z, ((ForgeChunkBuffer)chunkBuffer).getChunkPrimer(), ((ForgeWorld)localWorld).getWorld());
	}
}