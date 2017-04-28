package com.khorn.terraincontrol.forge;

import com.google.common.collect.Maps;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ClientConfigProvider;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObjectCollection;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import com.khorn.terraincontrol.forge.gui.GuiHandler;
import com.khorn.terraincontrol.forge.gui.TCGuiCreateWorld;
import com.khorn.terraincontrol.forge.gui.TCGuiWorldSelection;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Responsible for loading and unloading the world.
 *
 */
public final class WorldLoader
{	
    private final File configsDir;
    //private final Map<String, ServerConfigProvider> configMap = Maps.newHashMap();
    private final Map<String, CustomObjectCollection> configHolderMap = Maps.newHashMap();
    public final HashMap<String, ForgeWorld> worlds = new HashMap<String, ForgeWorld>();
    public final HashMap<String, ForgeWorld> unloadedWorlds = new HashMap<String, ForgeWorld>();

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
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }
        this.configsDir = dataFolder;
    }

    public ForgeWorld getUnloadedWorld(String name) 
    {
    	return this.unloadedWorlds.get(name);
    }
    
    public ForgeWorld getWorld(String name)
    {
        return this.worlds.get(name);
    }
    
    public boolean isConfigUnique(String name)
    {
		boolean bFound = false;
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
    	return true;
    }

    /*
    public LocalBiome getBiomeById(int id)
    {
    	// Check if config alread exists for main world
    	// Otherwise check other existing dimensions
    	
    	for(ForgeWorld world : this.worlds.values())
    	{
    		if(world.isMainWorld)
    		{
				LocalBiome bc = world.getBiomeByIdLocal(id);
        		if(bc != null)
        		{
        			return bc;
        		}
    		}
    	}    	
    	for(ForgeWorld world : this.worlds.values())
    	{
    		if(!world.isMainWorld)
    		{
	    		LocalBiome bc = world.getBiomeByIdLocal(id);
	    		if(bc != null)
	    		{
	    			return bc;
	    		}
    		}
    	}
    	return null;
    }
    
    public LocalBiome getBiome(String name)
    {
    	// Check if config alread exists for main world
    	// Otherwise check other existing dimensions
    	
    	for(ForgeWorld world : this.worlds.values())
    	{
    		if(world.isMainWorld)
    		{
    			try
    			{
	        		LocalBiome bc = world.getBiomeByNameLocal(name);
	        		if(bc != null)
	        		{
	        			return bc;
	        		}
    			}
    			catch(BiomeNotFoundException ex) { }
    		}
    	}    	
    	for(ForgeWorld world : this.worlds.values())
    	{
    		if(!world.isMainWorld)
    		{
    			try
    			{
		    		LocalBiome bc = world.getBiomeByNameLocal(name);
		    		if(bc != null)
		    		{
		    			return bc;
		    		}
    			}
    			catch(BiomeNotFoundException ex) { }
    		}
    	}
        return null;
    }
    
    public BiomeConfig getConfig(String name)
    {
    	// Check if config alread exists for main world
    	// Otherwise check other existing dimensions
    	
    	for(ForgeWorld world : this.worlds.values())
    	{
    		if(world.isMainWorld)
    		{
    			try
    			{
	        		LocalBiome bc = world.getBiomeByName(name);
	        		if(bc != null)
	        		{
	        			return bc.getBiomeConfig();
	        		}
    			}
    			catch(BiomeNotFoundException ex) { }
    		}
    	}    	
    	for(ForgeWorld world : this.worlds.values())
    	{
    		if(!world.isMainWorld)
    		{
    			try
    			{
		    		LocalBiome bc = world.getBiomeByName(name);
		    		if(bc != null)
		    		{
		    			return bc.getBiomeConfig();
		    		}
    			}
    			catch(BiomeNotFoundException ex) { }
    		}
    	}
        return null;
    }
    */
    
    public File getConfigsFolder()
    {
        return this.configsDir;
    }

    protected File getWorldDir(String worldName)
    {
        return new File(this.configsDir, "worlds/" + worldName);
    }

    /**
     * For a dedicated server, we need to register custom biomes
     * really early, even before we can know that TerrainControl
     * is the desired world type. As a workaround, we tentatively
     * load the configs if a config folder exists in the usual
     * location.
     * @param server The Minecraft server.
     */
    public void onServerAboutToLoad(String worldName)
    {    	                
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || !server.isDedicatedServer())
        {
            // Registry works differently on singleplayer
            // We cannot load things yet
            return;
        }
        
        worldName = ((DedicatedServer) server).getStringProperty("level-name", "");
        if(worldName == null)
        {
        	throw new NotImplementedException();
        }
        
        if (worldName.isEmpty())
        {
            return;
        }
        ForgeWorld forgeWorld = this.getOrCreateForgeWorld(worldName, true);
        if (forgeWorld == null)
        {
            // TerrainControl is probably not enabled for this world
            return;
        }
    }

    public void onServerStopped()
    {
        unloadAllWorlds();
    }

    public void unloadAllWorlds()
    {    	
    	ArrayList<ForgeWorld> worldsToRemove = new ArrayList<ForgeWorld>();
        for (ForgeWorld world : this.worlds.values())
        {
            if (world != null)
            {
            	worldsToRemove.add(world);
            }
        }
        for (ForgeWorld worldToRemove : worldsToRemove)
        {
            TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", worldToRemove.getName());
            //this.configMap.remove(worldToRemove.getName());
            worldToRemove.unRegisterBiomes();
            this.worlds.remove(worldToRemove.getName());
        }
        
        this.configHolderMap.clear();
        this.unloadedWorlds.clear();
    }

    public void unloadWorld(World world, boolean unRegisterBiomes)
    {
    	unloadWorld((ForgeWorld)((ForgeEngine)TerrainControl.getEngine()).getWorld(world), unRegisterBiomes);
    }
    
    public void unloadWorld(ForgeWorld world, boolean unRegisterBiomes)
    {
    	// Used when dimensions are unloaded.
    	
        //TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", world.getName());
        //this.configMap.remove(world.getName());
        //if(unRegisterBiomes)
        {
        	//world.unRegisterBiomes();
        }
        this.unloadedWorlds.put(world.getName(), this.worlds.get(world.getName()));
        this.worlds.remove(world.getName());        
    }

    @Nullable
    public ForgeWorld getOrCreateForgeWorld(World mcWorld)
    {
    	if(!(mcWorld.getWorldInfo().getTerrainType() instanceof TCWorldType))
    	{
    		throw new NotImplementedException();
    	}
    	
        ForgeWorld forgeWorld = this.getOrCreateForgeWorld(WorldHelper.getName(mcWorld), mcWorld.provider.getDimension() == 0);
        if (forgeWorld != null && forgeWorld.getWorld() == null)
        {
            forgeWorld.provideWorldInstance((WorldServer) mcWorld);
        }

        return forgeWorld;
    }

    @Nullable
    public ForgeWorld getOrCreateForgeWorld(String worldName, boolean isMainWorld)
    {
    	File worldConfigsFolder = this.getWorldDir(worldName);
        if (!worldConfigsFolder.exists())
        {
            // TerrainControl is probably not enabled for this world
            return null;
        }

        ForgeWorld world = this.getWorld(worldName);
        if (world == null)
        {
            world = new ForgeWorld(worldName, isMainWorld);
            ServerConfigProvider config = null;//this.configMap.get(worldName);
            if (config == null)
            {
                TerrainControl.log(LogMarker.INFO, "Loading configs for world \"{}\"..", world.getName());
                
                // Restore cached custom objects if this is a dimension that is being re-loaded.
                // TODO: Also cache worldconfig and biomes? <- Couldn't get this to work (crashes on dim reload).
                CustomObjectCollection customObjects = this.configHolderMap.get(worldName);
                if(customObjects != null)
                {
                	config = new ServerConfigProvider(worldConfigsFolder, world, customObjects);
                	TerrainControl.log(LogMarker.INFO, customObjects.getAll().size() + " world custom objects restored from cache.");
                } else {
                	config = new ServerConfigProvider(worldConfigsFolder, world);	
                }

                if(isMainWorld)
                {
                	// Apply world creation menu settings
                	// Client side only
	                try
	                {
	                	applyWorldCreationMenuSettings(config);
	                } catch(NoSuchMethodError ex) { }
                }

                // Remove fake biome to avoid Forge detecting it on restart and causing level.dat to be restored
                Iterator<Map.Entry<ResourceLocation, Biome>> iterator = Biome.REGISTRY.registryObjects.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry<ResourceLocation, Biome> mapEntry = iterator.next();
                    Biome biome = mapEntry.getValue();
                    int biomeId = Biome.getIdForBiome(biome);
                    if (biomeId == BiomeGenCustom.MAX_TC_BIOME_ID)
                    {
                        iterator.remove();
                    }
                }
                IntIdentityHashBiMap<Biome> underlyingIntegerMap = new IntIdentityHashBiMap<Biome>(256);
                Iterator<Biome> biomeIterator = Biome.REGISTRY.underlyingIntegerMap.iterator();
                while (biomeIterator.hasNext())
                {
                    Biome biome = biomeIterator.next();
                    int biomeId = Biome.getIdForBiome(biome);
                    if (biomeId == BiomeGenCustom.MAX_TC_BIOME_ID)
                    {
                        continue; // TODO: Won't this cause an infinite loop? Should be break;?
                    }
                    underlyingIntegerMap.put(biome, biomeId);
                }
                Biome.REGISTRY.underlyingIntegerMap = underlyingIntegerMap;                
            }
            world.provideConfigs(config);
            
            //this.configMap.put(worldName, config);
            
            this.configHolderMap.put(worldName, config.getCustomObjects());
            
            this.worlds.put(worldName, world);
            this.unloadedWorlds.remove(worldName);
        }

        return world;
    }
    
    @SideOnly(Side.CLIENT)
    private void applyWorldCreationMenuSettings(ServerConfigProvider config)
    {
        // If this is a new world use the pre-generator and world border settings from world creation menu
    	if(GuiHandler.lastGuiOpened.equals(TCGuiCreateWorld.class))
    	{
			if(((ForgeEngine)TerrainControl.getEngine()).getPregenerator().getPregenerationRadius() > -1)
			{
				config.getWorldConfig().PreGenerationRadius = ((ForgeEngine)TerrainControl.getEngine()).getPregenerator().getPregenerationRadius();
			}
			if(((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius > -1)
			{
				config.getWorldConfig().WorldBorderRadius = ((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius;    				
			}
			config.saveWorldConfig();
    	}
    	else if(GuiHandler.lastGuiOpened.equals(TCGuiWorldSelection.class))
    	{
    		((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius = config.getWorldConfig().WorldBorderRadius;
    	}
    }

    /*
    @SideOnly(Side.CLIENT)
    public void onQuitFromServer()
    {        
    	TerrainControl.log(LogMarker.INFO, "onQuitFromServer");
    	
    	ArrayList<ForgeWorld> worldsToRemove = new ArrayList<ForgeWorld>();
        for (ForgeWorld world : this.worlds.values())
        {
            if (world != null)
            {
            	worldsToRemove.add(world);
            }
        }
        for (ForgeWorld worldToRemove : worldsToRemove)
        {
            TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", worldToRemove.getName());
            //this.configMap.remove(worldToRemove.getName());
            this.configHolderMap.remove(worldToRemove.getName());
			worldToRemove.unRegisterBiomes();
            this.worlds.remove(worldToRemove.getName());
        }
        
        this.unloadedWorlds.clear();
    }

    @SideOnly(Side.CLIENT)
    public void unloadClientWorld(ForgeWorld world)
    {
    	TerrainControl.log(LogMarker.INFO, "unloadClientWorld");
    	
        TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", world.getName());
        this.worlds.remove(world.getName());
        this.unloadedWorlds.remove(world.getName());
        world.unRegisterBiomes();
    }
    */
    
    public void clearBiomeDictionary(ForgeWorld world)
    {    	   
    	// Hell and Sky are the only vanilla biome not overridden by a TC biome in the Forge Biome Registry when
    	// ForgeBiomes are created. We won't be re-registering them to the BiomeDict when the biomes 
    	// are created so restore their BiomeDictionary info here after clearing the BiomeDictionary.
    	Type[] hellTypes = BiomeDictionary.getTypesForBiome(Biomes.HELL);
    	Type[] skyTypes = BiomeDictionary.getTypesForBiome(Biomes.SKY);
    	    
    	HashMap<Biome, Type[]> typesToRestore = new HashMap<Biome, Type[]>();
    	    	    	
    	// Don't remove any biomedict info for biomes added by other mods
    	for(Entry<ResourceLocation, Biome> biome : Biome.REGISTRY.registryObjects.entrySet())
    	{
    		String resourceDomain = biome.getKey().getResourceDomain();
    		
    		if(!(resourceDomain.startsWith("minecraft") && !resourceDomain.startsWith("openterraingenerator")))
    		{
				if(!typesToRestore.containsKey(biome.getValue()))
				{
					typesToRestore.put(biome.getValue(), BiomeDictionary.isBiomeRegistered(biome.getValue()) ? BiomeDictionary.getTypesForBiome(biome.getValue()) : new Type[0]);
				}
    		}    		
    	}
    	
    	// When unloading a custom dimension only unregister that dimension's biomes
    	if(world != null)
    	{
    		for(LocalWorld loadedWorld : this.worlds.values())
    		{
    			if(loadedWorld != world)
    			{
	    			for(LocalBiome localBiome : ((ForgeWorld)loadedWorld).biomeNames.values())
	    			{
	    				if(!typesToRestore.containsKey(((ForgeBiome)localBiome).biomeBase))
	    				{
	    					typesToRestore.put(((ForgeBiome)localBiome).biomeBase, BiomeDictionary.isBiomeRegistered(((ForgeBiome)localBiome).biomeBase) ? BiomeDictionary.getTypesForBiome(((ForgeBiome)localBiome).biomeBase) : new Type[0]);
	    					//typesToRestore.put(((ForgeBiome)localBiome).biomeBase, BiomeDictionary.getTypesForBiome(((ForgeBiome)localBiome).biomeBase));
	    				}
	    			}
    			}
    		}
    	}
    	   	
		try {
			Field[] fields = BiomeDictionary.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.isArray())
				{
			        field.setAccessible(true);
			        field.set(null, Array.newInstance(field.getType().getComponentType(), Array.getLength(field.get(null))));
				}
				if(fieldClass.getSuperclass().equals(java.util.AbstractMap.class))
				{
			        field.setAccessible(true);			        
			        field.set(null, new HashMap());
				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BiomeDictionary.registerBiomeType(Biomes.HELL, hellTypes);
		BiomeDictionary.registerBiomeType(Biomes.SKY, skyTypes);
		for(Entry<Biome, Type[]> biomeToRestore : typesToRestore.entrySet())
		{
			BiomeDictionary.registerBiomeType(biomeToRestore.getKey(), biomeToRestore.getValue());
		}
    }

    public void unRegisterDefaultBiomes()
	{
		//BitSet biomeRegistryAvailabiltyMap = getBiomeRegistryAvailabiltyMap();
		// Unregister default biomes so they can be replaced by TC biomes (this allows us to fully customise the biomes)
		for(DefaultBiome defaultBiome : DefaultBiome.values())
		{ 		    				
			// Make an exception for the hell and sky biomes. 
			// The hell and end chunk providers refer specifically to 
			// Biomes.HELL and Biomes.SKY and query the biome registry
			// for them. Other biomes are not referred to in this way.
			if(defaultBiome.Name.equals("The Void") || defaultBiome.Name.equals("Hell") || defaultBiome.Name.equals("Sky")) { continue; }
			
	        ResourceLocation registryKey = ForgeWorld.vanillaResouceLocations.get(defaultBiome.Id);
			((ForgeEngine)TerrainControl.getEngine()).unRegisterForgeBiome(registryKey);
			
			//TerrainControl.log(LogMarker.INFO, "Unregistering " + defaultBiome.Name);
			
			//biomeRegistryAvailabiltyMap.set(defaultBiome.Id, false); // This should be enough to make Forge re-use the biome id
		}
	}
    
    public void unRegisterTCBiomes()
	{
		BitSet biomeRegistryAvailabiltyMap = getBiomeRegistryAvailabiltyMap();
		// Unregister default biomes so they can be replaced by TC biomes (this allows us to fully customise the biomes)
		for(Entry<ResourceLocation, Biome> biome : Biome.REGISTRY.registryObjects.entrySet())
		{
			if(!(biome.getKey().getResourceDomain().equals("OpenTerrainGenerator") || biome.getKey().getResourceDomain().equals("minecraft")))
			{
				continue;
			}
				    	
			// Make an exception for the hell and sky biomes. 
			// The hell and end chunk providers refer specifically to 
			// Biomes.HELL and Biomes.SKY and query the biome registry
			// for them. Other biomes are not referred to in this way.
			
			if(
				biome.getValue().getBiomeName().equals("The End") || 
				biome.getValue().getBiomeName().equals("The Void") || 
				biome.getValue().getBiomeName().equals("Hell") || 
				biome.getValue().getBiomeName().equals("Sky")
			)
			{
				continue;
			}
			
			TerrainControl.log(LogMarker.DEBUG, "Unregistering " + biome.getValue().getBiomeName());
			
			biomeRegistryAvailabiltyMap.set(Biome.getIdForBiome(biome.getValue()), false); // This should be enough to make Forge re-use the biome id
		}
	}       
	
	public BitSet getBiomeRegistryAvailabiltyMap()
	{
		BitSet biomeRegistryAvailabiltyMap = null;
		try {
			Field[] fields = Biome.REGISTRY.getClass().getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(BitSet.class))
				{
					field.setAccessible(true);
					biomeRegistryAvailabiltyMap = (BitSet) field.get(Biome.REGISTRY);
			        break;
				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return biomeRegistryAvailabiltyMap;
	}
	
    @SideOnly(Side.CLIENT)
    public void registerClientWorld(WorldClient mcWorld, DataInputStream wrappedStream) throws IOException
    {
        ForgeWorld world = new ForgeWorld(ConfigFile.readStringFromStream(wrappedStream), mcWorld.provider.getDimension() == 0);
        ClientConfigProvider configs = new ClientConfigProvider(wrappedStream, world, Minecraft.getMinecraft().isSingleplayer());
        world.provideClientConfigs(mcWorld, configs);
        this.worlds.put(world.getName(), world);
        this.unloadedWorlds.remove(world.getName());
    }
}
