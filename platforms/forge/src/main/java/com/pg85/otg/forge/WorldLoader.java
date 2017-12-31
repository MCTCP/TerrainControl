package com.pg85.otg.forge;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ClientConfigProvider;
import com.pg85.otg.configuration.ConfigFile;
import com.pg85.otg.configuration.ServerConfigProvider;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.WorldProviderOTG;
import com.pg85.otg.forge.gui.GuiHandler;
import com.pg85.otg.forge.gui.OTGGuiCreateWorld;
import com.pg85.otg.forge.gui.OTGGuiWorldSelection;
import com.pg85.otg.forge.util.WorldHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraftTypes.DefaultBiome;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
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

    public ArrayList<LocalWorld> getAllLoadedWorlds()
    {
    	ArrayList<LocalWorld> allWorlds = new ArrayList<LocalWorld>();
    	synchronized(worlds)
    	{
			allWorlds.addAll(worlds.values());
    	}
    	return allWorlds;
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

    public ArrayList<ForgeWorld> getUnloadedWorlds()
    {
    	ArrayList<ForgeWorld> unloadedWorldsClone = new ArrayList<ForgeWorld>();
    	synchronized(unloadedWorlds)
    	{
    		unloadedWorldsClone.addAll(unloadedWorlds.values());
    	}
    	return unloadedWorldsClone;
    }

    public boolean isWorldUnloaded(String worldName)
    {
    	boolean isLoaded = false;
    	boolean isUnloaded = false;
    	synchronized(worlds)
    	{
    		synchronized(unloadedWorlds)
    		{
    			isLoaded = worlds.containsKey(worldName);
    			isUnloaded = unloadedWorlds.containsKey(worldName);
    		}
    	}

    	return !isLoaded && isUnloaded;
    }

    public void RemoveUnloadedWorld(String worldName)
    {
    	synchronized(unloadedWorlds)
    	{
    		ForgeWorld forgeWorld = unloadedWorlds.get(worldName);
    		forgeWorld.DeleteWorldSessionData();
    		unloadedWorlds.remove(worldName);
    	}
    }

    WorldLoader(File configsDir)
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

    public ForgeWorld getUnloadedWorld(String name)
    {
    	ForgeWorld forgeWorld = null;
        synchronized(this.unloadedWorlds)
        {
        	forgeWorld = this.unloadedWorlds.get(name);
        }
        return forgeWorld;
    }

    public ForgeWorld getWorld(String name)
    {
    	ForgeWorld forgeWorld = null;
        synchronized(this.worlds)
        {
        	forgeWorld = this.worlds.get(name);
        }
        return forgeWorld;
    }

    public boolean isConfigUnique(String name)
    {
		boolean bFound = false;
		synchronized(this.worlds)
		{
			synchronized(this.unloadedWorlds)
			{
		    	for(ForgeWorld world : this.worlds.values())
		    	{
		    		LocalBiome bc = world.getBiomeByNameOrNull(name);
		    		if(bc != null)
		    		{
		    			if(bFound)
		    			{
		    				return false;
		    			}
		    			bFound = true;
		    		}
		    	}
		    	for(ForgeWorld world : this.unloadedWorlds.values())
		    	{
		    		LocalBiome bc = world.getBiomeByNameOrNull(name);
		    		if(bc != null)
		    		{
		    			if(bFound)
		    			{
		    				return false;
		    			}
		    			bFound = true;
		    		}
		    	}
			}
		}
    	return true;
    }

    public File getConfigsFolder()
    {
        return this.configsDir;
    }

    protected File getWorldDir(String worldName)
    {
        return new File(this.configsDir, "worlds/" + worldName);
    }

    public void onServerStopped()
    {
        unloadAllWorlds();
    }

    public void unloadAllWorlds()
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

		        synchronized(this.unloadedWorlds)
		        {
		        	this.unloadedWorlds.clear();
		        }
    		}
    	}
    }

    public void unloadWorld(World world, boolean unRegisterBiomes)
    {
    	unloadWorld((ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(world), unRegisterBiomes);
    }

    public void unloadWorld(ForgeWorld world, boolean unRegisterBiomes)
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
        }
    }

    public static void preLoadWorld(String worldName)
    {
    	boolean isMainWorld = true;

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

    	File worldConfigsFolder = new File(dataFolder, "worlds/" + worldName);
        if (!worldConfigsFolder.exists())
        {
            // OpenTerrainGenerator is probably not enabled for this world
            return;
        }

        ForgeWorld world = null;
        if (world == null)
        {
            world = new ForgeWorld(worldName, isMainWorld);
            ServerConfigProvider config = null;
            if (config == null)
            {
                OTG.log(LogMarker.INFO, "Loading configs for world \"{}\"..", world.getName());

                config = new ServerConfigProvider(worldConfigsFolder, world);
            }
            world.provideConfigs(config);
        }
    }

    @Nullable
    public ForgeWorld getOrCreateForgeWorld(World mcWorld)
    {
    	if(
			(
				mcWorld.getWorldInfo() instanceof DerivedWorldInfo &&
				!((DerivedWorldInfo)mcWorld.getWorldInfo()).delegate.getGeneratorOptions().equals("OpenTerrainGenerator")
			) || (
				!(mcWorld.getWorldInfo() instanceof DerivedWorldInfo) &&
				!mcWorld.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator")
			)
		)
    	{
    		throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
    	}

    	String worldName = WorldHelper.getName(mcWorld);
    	boolean isMainWorld = mcWorld.provider.getDimension() == 0;

    	File worldConfigsFolder = this.getWorldDir(worldName);
        if (!worldConfigsFolder.exists())
        {
            // OpenTerrainGenerator is probably not enabled for this world
            return null;
        }

        ForgeWorld world = this.getWorld(worldName);
        if (world == null)
        {
            world = new ForgeWorld(worldName, isMainWorld);
            ServerConfigProvider config = null;//this.configMap.get(worldName);
            if (config == null)
            {
                OTG.log(LogMarker.INFO, "Loading configs for world \"{}\"..", world.getName());

                config = new ServerConfigProvider(worldConfigsFolder, world);

                if(isMainWorld)
                {
                	// Apply world creation menu settings
                	// Client side only
	                try
	                {
	                	applyWorldCreationMenuSettings(config);
	                } catch(NoSuchMethodError ex) { }
                }
            }
            world.provideConfigs(config);

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
        }

        return world;
    }

    @SideOnly(Side.CLIENT)
    private void applyWorldCreationMenuSettings(ServerConfigProvider config)
    {
        // If this is a new world use the pre-generator and world border settings from world creation menu
    	if(GuiHandler.lastGuiOpened.equals(OTGGuiCreateWorld.class))
    	{
			config.getWorldConfig().PreGenerationRadius = GuiHandler.PregenerationRadius;
			config.getWorldConfig().WorldBorderRadius = GuiHandler.WorldBorderRadius;
			config.saveWorldConfig();
    	}
    	else if(GuiHandler.lastGuiOpened.equals(OTGGuiWorldSelection.class))
    	{
    		GuiHandler.PregenerationRadius = config.getWorldConfig().PreGenerationRadius;
    		GuiHandler.WorldBorderRadius = config.getWorldConfig().WorldBorderRadius;
    	}
    }

    public void clearBiomeDictionary(ForgeWorld world)
    {
    	// Hell and Sky are the only vanilla biome not overridden by a TC biome in the Forge Biome Registry when
    	// ForgeBiomes are created. We won't be re-registering them to the BiomeDict when the biomes
    	// are created so restore their BiomeDictionary info here after clearing the BiomeDictionary.

    	Set<Type> hellTypesSet = BiomeDictionary.getTypes(Biomes.HELL);
    	Set<Type> skyTypesSet = BiomeDictionary.getTypes(Biomes.SKY);

    	Type[] hellTypes = hellTypesSet.toArray(new Type[hellTypesSet.size()]);
    	Type[] skyTypes = skyTypesSet.toArray(new Type[skyTypesSet.size()]);

    	HashMap<Biome, Set<Type>> typesToRestore = new HashMap<Biome, Set<Type>>();

    	// Don't remove any biomedict info for biomes added by other mods
    	for(Entry<ResourceLocation, Biome> biome : ForgeRegistries.BIOMES.getEntries())
    	{
    		String resourceDomain = biome.getKey().getResourceDomain();

    		if(!(resourceDomain.startsWith("minecraft") && !resourceDomain.startsWith("openterraingenerator")))
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

		BiomeDictionary.addTypes(Biomes.HELL, hellTypes);
		BiomeDictionary.addTypes(Biomes.SKY, skyTypes);
		for(Entry<Biome, Set<Type>> biomeToRestore : typesToRestore.entrySet())
		{
			BiomeDictionary.addTypes(biomeToRestore.getKey(), biomeToRestore.getValue().toArray(new Type[biomeToRestore.getValue().size()]));
		}
    }

    public void unRegisterDefaultBiomes()
	{
		// Unregister default biomes so they can be replaced by TC biomes (this allows us to fully customise the biomes)
		for(DefaultBiome defaultBiome : DefaultBiome.values())
		{
			// Make an exception for the hell and sky biomes.
			// The hell and end chunk providers refer specifically to
			// Biomes.HELL and Biomes.SKY and query the biome registry
			// for them. Other biomes are not referred to in this way.
			if(defaultBiome.Name.equals("The Void") || defaultBiome.Name.equals("Hell") || defaultBiome.Name.equals("Sky")) { continue; }

	        ResourceLocation registryKey = ForgeWorld.vanillaResouceLocations.get(defaultBiome.Id);
			((ForgeEngine)OTG.getEngine()).unRegisterForgeBiome(registryKey);
		}
	}

    public void unRegisterTCBiomes()
	{
		BitSet biomeRegistryAvailabiltyMap = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryAvailabiltyMap();
		// Unregister default biomes so they can be replaced by TC biomes (this allows us to fully customise the biomes)
		for(Entry<ResourceLocation, Biome> biome : ForgeRegistries.BIOMES.getEntries())
		{
			if(!(biome.getKey().getResourceDomain().toLowerCase().equals("openterraingenerator") || biome.getKey().getResourceDomain().equals("minecraft")))
			{
				continue;
			}

			// Make an exception for the hell and sky biomes.
			// The hell and end chunk providers refer specifically to
			// Biomes.HELL and Biomes.SKY and query the biome registry
			// for them. Other biomes are not referred to in this way.

			if(
				biome.getKey().getResourcePath().equals("void") ||
				biome.getKey().getResourcePath().equals("hell") ||
				biome.getKey().getResourcePath().equals("sky")
			)
			{
				continue;
			}

			OTG.log(LogMarker.TRACE, "Unregistering " + biome.getValue().biomeName);

			int biomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(biome.getValue());
			// If this biome uses replaceToBiomeName and has an id > 255 then it is not actually registered in the biome id
			// registry and biomeId will be 0. Check if biomeId is actually registered to this biome.
			if(biomeId > -1 && biome == Biome.getBiome(biomeId))
			{
				biomeRegistryAvailabiltyMap.set(biomeId, false); // This should be enough to make Forge re-use the biome id
			}
		}
	}

    @SideOnly(Side.CLIENT)
    public void registerClientWorldBukkit(WorldClient mcWorld, DataInputStream wrappedStream) throws IOException
    {
        ForgeWorld world = new ForgeWorld(ConfigFile.readStringFromStream(wrappedStream), mcWorld.provider.getDimension() == 0);
        ClientConfigProvider configs = new ClientConfigProvider(wrappedStream, world, Minecraft.getMinecraft().isSingleplayer());
        world.provideClientConfigsBukkit(mcWorld, configs);

        synchronized(this.worlds)
        {
        	synchronized(this.unloadedWorlds)
        	{
        		this.worlds.put(world.getName(), world);
        		this.unloadedWorlds.remove(world.getName());
        	}
        }
    }

	// Called once when the client connects to the server, at that point dimensions and OTG worlds are loaded
	// but MC worlds have not yet been sent.
	// Called a second time when a player logs in, at that point the forge worlds that were created earlier have their
	// MC worlds attached.
    @SideOnly(Side.CLIENT)
    public void registerClientWorld(DataInputStream wrappedStream) throws IOException
    {
        // Dimension info
    	int worldCount = wrappedStream.readInt();
    	HashMap<Integer, String> dimsToRemove = OTGDimensionManager.GetAllOTGDimensions(); // TODO: use String[] instead?
    	boolean isSinglePlayer = Minecraft.getMinecraft().isSingleplayer();

    	for(int i = 0; i < worldCount; i++)
    	{
    		int dimensionId = wrappedStream.readInt(); // TODO: Create dimensions on client? Is that even necessary, creating worlds should be enough?

    		Integer integerToRemove = 0;
    		boolean bFound = false;
    		for(Entry<Integer, String> dimToRemove : dimsToRemove.entrySet())
    		{
    			if(dimToRemove.getKey().intValue() == dimensionId) // TODO: Is this really necessary?
    			{
    				bFound = true;
    				integerToRemove = dimToRemove.getKey();
    			}
    		}
    		if(bFound)
    		{
    			dimsToRemove.remove(integerToRemove);
    		}

			String worldName = ConfigFile.readStringFromStream(wrappedStream);

			ForgeWorld overWorld = null;
			if(i == 0)
			{
				overWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(worldName);
				if(overWorld == null)
				{
					overWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getUnloadedWorld(worldName);
				}
			}

    		if((i == 0 && overWorld == null) || !DimensionManager.isDimensionRegistered(dimensionId))
    		{
	    		if(i != 0)
	    		{
	    			OTGDimensionManager.registerDimension(dimensionId, DimensionType.register(worldName, "OTG", dimensionId, WorldProviderOTG.class, false));
	    		}

	            ForgeWorld world = new ForgeWorld(worldName, dimensionId == 0);
	            world.clientDimensionId = dimensionId;
	            ClientConfigProvider configs = new ClientConfigProvider(wrappedStream, world, isSinglePlayer);
	            world.provideClientConfigs(configs);
	            synchronized(this.worlds)
	            {
	            	synchronized(this.unloadedWorlds)
	            	{
	            		this.worlds.put(world.getName(), world);
	            		this.unloadedWorlds.remove(world.getName());
	            	}
	            }
    		} else {

    			// World already exists, read the data from the stream but don't create a world.
    			new ClientConfigProvider(wrappedStream, new ForgeWorld(), isSinglePlayer);
    		}
    	}

    	for(Entry<Integer, String> removedDim : dimsToRemove.entrySet())
    	{
    		// This dimension has been deleted on the server, remove it
    		ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getUnloadedWorld(removedDim.getValue());
    		if(forgeWorld == null)
    		{
    			forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(removedDim.getValue()); // This can happen because the client considers all worlds loaded when it receives them from the server.
    		}

    		OTGDimensionManager.DeleteDimension(removedDim.getKey(), forgeWorld, Minecraft.getMinecraft().player.getServer(), false);
    	}
    }
}
