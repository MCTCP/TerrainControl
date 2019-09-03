package com.pg85.otg.forge.world;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfigFinder;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.io.FileSettingsWriter;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.configuration.world.WorldConfig.DefaulWorldData;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.biomes.ForgeBiomeRegistryManager;
import com.pg85.otg.forge.configuration.standard.ForgeMojangSettings;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.network.server.packets.DimensionSyncPacket;
import com.pg85.otg.forge.util.WorldHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.helpers.FileHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Responsible for loading and unloading the world.
 *
 */
public final class WorldLoader
{	
	private final File configsDir;
    private final HashMap<String, ForgeWorld> worlds = new HashMap<String, ForgeWorld>();
    private final HashMap<String, ForgeWorld> unloadedWorlds = new HashMap<String, ForgeWorld>();

    public WorldLoader(File configsDir)
    {
        File dataFolder;
        try
        {
            Field minecraftDir = Loader.class.getDeclaredField("minecraftDir");
            minecraftDir.setAccessible(true);
            dataFolder = new File((File) minecraftDir.get(null), "mods" + File.separator + "OpenTerrainGenerator");
        } catch (Throwable e)
        {
            dataFolder = new File("mods" + File.separator + "OpenTerrainGenerator");
            System.out.println("Could not reflect the Minecraft directory, save location may be unpredicatble.");
            OTG.printStackTrace(LogMarker.FATAL, e);
        }
        this.configsDir = dataFolder;
    }
    
    public File getConfigsFolder()
    {
        return this.configsDir;
    }

    private File getWorldDir(String worldName)
    {
        return new File(this.configsDir, PluginStandardValues.PresetsDirectoryName + File.separator + worldName);
    }

    public void onServerStopped()
    {
        removeAllWorlds();
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
    
    public ForgeWorld getWorld(String name)
    {
    	if(name == null)
    	{
    		return null;
    	}
    	if(name.equals("overworld"))
    	{
    		return this.getOverWorld();
    	}

    	ForgeWorld forgeWorld = null;
        synchronized(this.worlds)
        {
        	forgeWorld = this.worlds.get(name);
        }
        return forgeWorld;
    }

    public ForgeWorld getWorld(World world)
    {
    	if(world.provider.getDimension() == 0)
    	{
    		return this.getOverWorld();
    	}
    	else if(world.provider.getDimension() > 1)
    	{
			LocalWorld localWorld = this.getWorld(world.provider.getDimensionType().getName());
			if(localWorld == null)
			{
				return this.getUnloadedWorld(world.provider.getDimensionType().getName());
			}
    		return (ForgeWorld) localWorld;
    	}
    	
    	return null;
    }
    
    public ForgeWorld getWorldByDimId(int dimensionId)
    {
    	ForgeWorld forgeWorld;
    	if(dimensionId == 0)
    	{
    		forgeWorld = this.getOverWorld();
    	} else {
        	DimensionType dimType = DimensionManager.getProviderType(dimensionId);
    		forgeWorld = (ForgeWorld)OTG.getWorld(dimType.getName());
    	}
    	return forgeWorld;
    }
       
    public ForgeWorld getUnloadedWorld(String name)
    {
    	ForgeWorld forgeWorld = null;
        synchronized(this.unloadedWorlds)
        {
        	forgeWorld = this.unloadedWorlds.get(name);
        }
        return forgeWorld;
    }
    
    public ForgeWorld getUnloadedWorldByDimId(int dimensionId)
    {
    	ForgeWorld forgeWorld;
    	if(dimensionId == 0)
    	{
    		forgeWorld = this.getOverWorld();
    	} else {
        	DimensionType dimType = DimensionManager.getProviderType(dimensionId);
    		forgeWorld = (ForgeWorld)OTG.getUnloadedWorld(dimType.getName());
    	}
    	return forgeWorld;
    }
        
    public boolean isWorldUnloaded(String worldName)
    {
    	boolean isUnloaded = false;
    	synchronized(worlds)
    	{
    		synchronized(unloadedWorlds)
    		{
    			isUnloaded = unloadedWorlds.containsKey(worldName);
    		}
    	}

    	return isUnloaded;
    }
    
    public ArrayList<LocalWorld> getAllLoadedWorlds()
    {
    	ArrayList<LocalWorld> allWorlds = new ArrayList<LocalWorld>();
    	synchronized(worlds)
    	{
			allWorlds.addAll(worlds.values());
    	}
    	return allWorlds;
    }
    
    public ArrayList<ForgeWorld> getUnloadedWorlds()
    {
    	ArrayList<ForgeWorld> unloadedWorldsClone = new ArrayList<ForgeWorld>();
    	synchronized(unloadedWorlds)
    	{
    		unloadedWorldsClone.addAll(unloadedWorlds.values());
    	}
    	return unloadedWorldsClone;
    }
        
    public ArrayList<LocalWorld> getAllWorlds()
    {
    	ArrayList<LocalWorld> allWorlds = new ArrayList<LocalWorld>();
    	synchronized(worlds)
    	{
    		synchronized(unloadedWorlds)
    		{
    			allWorlds.addAll(worlds.values());
    			allWorlds.addAll(unloadedWorlds.values());
    		}
    	}
    	return allWorlds;
    }
    
    public void unloadWorld(World world, boolean unRegisterBiomes)
    {
    	unloadWorld(this.getWorld(world), unRegisterBiomes);
    }

    private void unloadWorld(ForgeWorld world, boolean unRegisterBiomes)
    {
    	// Used when dimensions are unloaded.

        OTG.log(LogMarker.INFO, "Unloading world \"{}\"...", world.getName());

        ForgeWorld loadedWorld = this.worlds.get(world.getName());
        if(loadedWorld != null) // For some reason Forge MP server unloads overworld twice on shutdown(?)
        {
        	synchronized(this.worlds)
        	{
        		synchronized(this.unloadedWorlds)
        		{
        			this.unloadedWorlds.put(world.getName(), this.worlds.get(world.getName()));
            		this.worlds.remove(world.getName());
        		}
        	}
        	
            if(!loadedWorld.getWorld().isRemote)
            {
            	ServerPacketManager.sendDimensionLoadUnloadPacketToAllPlayers(false, world.getName(), loadedWorld.getWorld().getMinecraftServer());
            }
        }
    }
    
    public void removeLoadedWorld(String worldName)
    {
    	synchronized(worlds)
    	{
    		ForgeWorld forgeWorld = worlds.get(worldName);
    		if(forgeWorld != null)
    		{
   				forgeWorld.deleteWorldSessionData();
	    		worlds.remove(worldName);
    		}
    	}
    }
    
    public void removeUnloadedWorld(String worldName)
    {
    	synchronized(unloadedWorlds)
    	{
    		ForgeWorld forgeWorld = unloadedWorlds.get(worldName);
    		if(forgeWorld != null)
    		{
	    		forgeWorld.deleteWorldSessionData();
	    		unloadedWorlds.remove(worldName);
    		}
    	}
    }
    
    private void removeAllWorlds()
    {
    	ArrayList<ForgeWorld> worldsToRemove = new ArrayList<ForgeWorld>();
    	synchronized(this.worlds)
    	{
    		synchronized(this.unloadedWorlds)
    		{
		        for (ForgeWorld world : this.worlds.values())
		        {
		            if (world != null)
		            {
		            	worldsToRemove.add(world);
		            }
		        }
		        for (ForgeWorld worldToRemove : worldsToRemove)
		        {
		            OTG.log(LogMarker.INFO, "Unloading world \"{}\"...", worldToRemove.getName());
		            worldToRemove.unRegisterBiomes();
		            this.worlds.remove(worldToRemove.getName());
		        }

	        	this.unloadedWorlds.clear();
    		}
    	}
    }
    
	public void unloadAndUnregisterAllWorlds()
	{	
    	// Default settings are not restored on world unload / server quit because this was causing problems
    	// (unloading dimensions while their worlds were still ticking etc)
    	// Unload all world and biomes on server start / connect instead, for client where data is kept when leaving the game.

    	OTGDimensionManager.UnloadAllCustomDimensionData();
    	this.removeAllWorlds();
        // Clear the BiomeDictionary (it will be refilled when biomes are loaded in createBiomeFor)
    	this.clearBiomeDictionary(null);
    	ForgeBiomeRegistryManager.clearOTGBiomeIds();
	}

    @Nullable ForgeWorld getOrCreateForgeWorld(World mcWorld)
    {   	
    	if(!mcWorld.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator"))
    	{	
    		throw new RuntimeException("Error: OTG tried to load a world that is missing OTG information. Was this world created via OTG? For Forge Single Player, be sure to use the OTG world creation screen.");
    	}

    	String worldName = WorldHelper.getName(mcWorld);
    	File worldConfigsFolder = null;
    	
    	worldConfigsFolder = this.getWorldDir(OTG.getDimensionsConfig().getDimensionConfig(worldName).PresetName);    	    	
        if (worldConfigsFolder == null || !worldConfigsFolder.exists())
        {
            // OpenTerrainGenerator is not enabled for this world
            return null;
        }

        ForgeWorld world = this.getWorld(worldName);
        if (world == null)
        {           	
            world = new ForgeWorld(worldName);
            OTG.log(LogMarker.DEBUG, "Loading configs for world \"{}\"..", world.getName());

            ServerConfigProvider config = new ServerConfigProvider(worldConfigsFolder, world, mcWorld.getSaveHandler().getWorldDirectory());            
            world.provideConfigs(config);
            
            OTG.log(LogMarker.DEBUG, "Completed loading configs for world \"{}\"..", world.getName());
        }
        if (world != null && world.getWorld() == null)
        {
        	world.provideWorldInstance((WorldServer) mcWorld);
        }
        
        synchronized(this.worlds)
        {
        	synchronized(this.unloadedWorlds)
        	{
        		this.worlds.put(worldName, world);
        		this.unloadedWorlds.remove(worldName);
        	}
        }
        
        return world;
    }

    public void createDefaultOTGWorld(String worldName)
    {
		// Create default OTG world

    	File worldDir = new File(OTG.getEngine().getWorldsDirectory() + File.separator + worldName);
        List<File> worldDirs = new ArrayList<File>(2);
        worldDirs.add(new File(worldDir, WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME));
        worldDirs.add(new File(worldDir, WorldStandardValues.WORLD_OBJECTS_DIRECTORY_NAME));
        FileHelper.makeFolders(worldDirs);		
		
		// World config
		DefaulWorldData defaultWorldData = WorldConfig.createDefaultOTGWorldConfig(worldDir, worldName);
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
            FileSettingsWriter.writeToFile(biomeConfig.getSettingsAsMap(), writeFile, defaultWorldConfig.settingsMode);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void registerClientWorldBukkit(WorldClient mcWorld, DataInputStream wrappedStream) throws IOException
    {
    	ForgeWorld world = DimensionSyncPacket.registerClientWorldBukkit(mcWorld, wrappedStream, this.worlds, this.unloadedWorlds);
        synchronized(worlds)
        {
        	synchronized(unloadedWorlds)
        	{
        		worlds.put(world.getName(), world);
        		unloadedWorlds.remove(world.getName());
        	}
        }
    }

    // Only used when registering client worlds from a packet, don't use this separately.
	public void LoadClientWorldFromPacket(ForgeWorld world)
	{
        synchronized(worlds)
        {
        	synchronized(unloadedWorlds)
        	{
        		worlds.put(world.getName(), world);
        		unloadedWorlds.remove(world.getName());
        	}
        }		
	}
    
    public void clearBiomeDictionary(ForgeWorld world)
    {
    	// Hell and Sky are the only vanilla biome not overridden by a TC biome in the Forge Biome Registry when
    	// ForgeBiomes are created. We won't be re-registering them to the BiomeDict when the biomes
    	// are created so restore their BiomeDictionary info here after clearing the BiomeDictionary.

    	HashMap<Biome, Set<Type>> typesToRestore = new HashMap<Biome, Set<Type>>();

    	// Don't remove any biomedict info for vanilla biomes or biomes added by other mods
    	for(Entry<ResourceLocation, Biome> biome : ForgeRegistries.BIOMES.getEntries())
    	{
    		String resourceDomain = biome.getKey().getNamespace();

    		if(
				!resourceDomain.equals("openterraingenerator") &&
				!resourceDomain.equals("terraincontrol")
			)
    		{
				if(!typesToRestore.containsKey(biome.getValue()))
				{
					typesToRestore.put(biome.getValue(), ForgeRegistries.BIOMES.containsValue(biome.getValue()) ? BiomeDictionary.getTypes(biome.getValue()) : new HashSet<Type>());
				}
    		}
    	}

    	// When unloading a custom dimension only unregister that dimension's biomes
    	if(world != null)
    	{
    		synchronized(this.worlds)
    		{
	    		for(LocalWorld loadedWorld : this.worlds.values())
	    		{
	    			if(loadedWorld != world)
	    			{
		    			for(LocalBiome localBiome : ((ForgeWorld)loadedWorld).biomeNames.values())
		    			{
		    				if(!typesToRestore.containsKey(((ForgeBiome)localBiome).biomeBase))
		    				{
		    					typesToRestore.put(((ForgeBiome)localBiome).biomeBase, ForgeRegistries.BIOMES.containsValue(((ForgeBiome)localBiome).biomeBase) ? BiomeDictionary.getTypes(((ForgeBiome)localBiome).biomeBase) : new HashSet<Type>());
		    				}
		    			}
	    			}
	    		}
    		}
    	}
		try {
			Field[] declaredfields = BiomeDictionary.class.getDeclaredFields();
			for(Field field : declaredfields)
			{
				Class<?> fieldClass = field.getType();

				// Where is typeInfoList?

				// biomeInfoMap
				if(fieldClass.equals(java.util.Map.class))
				{
			        field.setAccessible(true);
			        HashMap biomeRegistryAvailabiltyMap = (HashMap)field.get(BiomeDictionary.class);
			        biomeRegistryAvailabiltyMap.clear();
				}
			}
			declaredfields = BiomeDictionary.Type.class.getDeclaredFields();
			for(Field field : declaredfields)
			{
				Class<?> fieldClass = field.getType();

				// byName (used to be typeInfoList)?
				if(fieldClass.equals(java.util.Map.class))
				{
			        field.setAccessible(true);
			        Map<String, Type> byName = (HashMap<String, Type>)field.get(BiomeDictionary.class);
			        for(Type type : byName.values())
			        {
						Field[] typeDeclaredfields = Type.class.getDeclaredFields();
						for(Field typeField : typeDeclaredfields)
						{
							Class<?> typeFieldClass = typeField.getType();

							// biomes
							if(typeFieldClass.equals(Set.class))
							{
								typeField.setAccessible(true);
						        Set<Biome> biomes = (Set<Biome>)typeField.get(type);
						        try
						        {
						        	biomes.clear();
						        }
						        catch(UnsupportedOperationException ex)
						        {
						        	// This is ubiomes, ignore
						        }
							}
						}
			        }
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		for(Entry<Biome, Set<Type>> biomeToRestore : typesToRestore.entrySet())
		{
			BiomeDictionary.addTypes(biomeToRestore.getKey(), biomeToRestore.getValue().toArray(new Type[biomeToRestore.getValue().size()]));
		}
    }
}
