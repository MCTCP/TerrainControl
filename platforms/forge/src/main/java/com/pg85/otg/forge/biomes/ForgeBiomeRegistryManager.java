package com.pg85.otg.forge.biomes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.standard.MojangSettings.EntityCategory;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.BiomeIds;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ForgeBiomeRegistryManager
{
    private BiMap<Integer, Biome> ids = null;
    private BiMap<ResourceLocation, Biome> names = null;
	
	public static int getRegisteredBiomeId(String resourceLocationString, String worldName)
	{
		if(resourceLocationString != null && !resourceLocationString.trim().isEmpty())
		{
			String[] resourceLocationStringArr = resourceLocationString.split(":");
			if(resourceLocationStringArr.length == 1) // When querying for biome name without domain search the local world's biomes 
			{
				ResourceLocation resourceLocation = new ResourceLocation(PluginStandardValues.MOD_ID.toLowerCase(), worldName + "_" + resourceLocationStringArr[0].replaceAll(" ", "_"));
				Biome requestedBiome = ForgeRegistries.BIOMES.getValue(resourceLocation);
				return ((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().getBiomeRegistryId(requestedBiome);
			}
			if(resourceLocationStringArr.length == 2)
			{
				ResourceLocation resourceLocation = new ResourceLocation(resourceLocationStringArr[0],resourceLocationStringArr[1]);
				Biome requestedBiome = ForgeRegistries.BIOMES.getValue(resourceLocation);
				return ((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().getBiomeRegistryId(requestedBiome);
			}
		}
		return -1;
	}

	private static void unregisterBiome(LocalBiome localBiome, String worldName)
	{
        String biomeNameForRegistry = StringHelper.toComputerFriendlyName(localBiome.getName());
        String resourceDomain = PluginStandardValues.PLUGIN_NAME.toLowerCase();
        ResourceLocation registryKey = new ResourceLocation(resourceDomain, worldName + "_" + biomeNameForRegistry);

        ((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().unRegisterForgeBiome(registryKey);
        ((ForgeEngine)OTG.getEngine()).unregisterOTGBiomeId(worldName, localBiome.getIds().getOTGBiomeId());
	}

	public static void clearOTGBiomeIds()
	{
		BitSet biomeRegistryAvailabiltyMap = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().getBiomeRegistryAvailabiltyMap();
		for(Entry<ResourceLocation, Biome> biome : ForgeRegistries.BIOMES.getEntries())
		{
			if(!(biome.getKey().getNamespace().toLowerCase().equals("openterraingenerator")))
			{
				continue;
			}
	
			OTG.log(LogMarker.DEBUG, "Unregistering " + biome.getValue().biomeName);
	
			int biomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().getBiomeRegistryId(biome.getValue());
			// If this biome uses replaceToBiomeName and has an id > 255 then it is not actually registered in the biome id
			// registry and biomeId will be 0. Check if biomeId is actually registered to this biome.
			if(biomeId > -1 && biome == Biome.getBiome(biomeId))
			{
				biomeRegistryAvailabiltyMap.set(biomeId, false); // This should be enough to make Forge re-use the biome id
			}
		}
	}

	public static ForgeBiome getOrCreateBiome(BiomeConfig biomeConfig, BiomeIds biomeIds, String worldName, ConfigProvider configProvider)
	{
		Biome biome = null;
		
	    String biomeNameForRegistry = worldName.toLowerCase() + "_" + StringHelper.toComputerFriendlyName(biomeConfig.getName());
	    String resourceDomain = PluginStandardValues.MOD_ID;
	    ResourceLocation registryKey = new ResourceLocation(resourceDomain, biomeNameForRegistry);
	
	    // Check if registered earlier
		Biome alreadyRegisteredBiome = ForgeRegistries.BIOMES.getValue(registryKey);
	    if (alreadyRegisteredBiome != null)
	    {        	
	    	// This can happen when an unloaded world is loaded, its biomes have already been registered
	    	biome = alreadyRegisteredBiome;
	    } else {
	
	        // No existing biome, create new one
	        OTGBiome customBiome = new OTGBiome(biomeConfig, registryKey);
	
	        ForgeEngine forgeEngine = ((ForgeEngine) OTG.getEngine());
	
	        if (biomeIds.isVirtual())
	        //if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0) // This biome uses ReplaceToBiomeName and should use the ReplaceToBiomeName biome's id.
	        {
	        	// Only register by resourcelocation.
	        	// TODO: Make sure this is enough for Forge 1.12+ <- It looks like the server may not send the biomes to the client if they are not added to the registry. TODO: Check if only virtual biomes have this problem.
	        	forgeEngine.getBiomeRegistryManager().registerForgeBiome(registryKey, customBiome);
	        	
	        	customBiome.savedId = biomeIds.getSavedId();        	
	        }
	        else if(biomeIds.getSavedId() > -1) 
	        {
	        	// This is a biome for an existing world, make sure it uses the same biome id as before.         
	        	int newId = forgeEngine.getBiomeRegistryManager().registerForgeBiomeWithId(biomeIds.getSavedId(), registryKey, customBiome);       	
	        	customBiome.savedId = newId;
	        	
	        } else {
	
	            // Normal insertion, get next free id and register id+resourcelocation
	
	        	// TODO: Make this prettier?
	        	int newId = forgeEngine.getBiomeRegistryManager().registerForgeBiomeWithId(registryKey, customBiome);
	        	biomeIds.setSavedId(newId);
	        	customBiome.savedId = newId;       	        
	        }
	        
	    	OTG.log(LogMarker.DEBUG, "{}: {}, {}, {}, {}", biomeIds.isVirtual() ? "Virtual " : "Custom ", biomeConfig.getName(), biomeIds.getSavedId(), biomeIds.getOTGBiomeId(), registryKey.toString());
	
	        biome = customBiome;
	    }
	    
		// Always try to register biomes and create Biome Configs. Biomes with id's > 255 are registered
		// only for biome -> id queries, any (saved)id -> biome query will return the ReplaceToBiomeName biome.
	
	    Biome existingBiome = Biome.getBiome(biomeIds.getSavedId());
	
	    if (biomeIds.getSavedId() >= 256 || biomeIds.getSavedId() < 0)
	    {
	        throw new RuntimeException("Could not allocate the requested id " + biomeIds.getSavedId() + " for biome " + biomeConfig.getName() + ". All available id's under 256 have been allocated\n" + ". To proceed, adjust your WorldConfig or use the ReplaceToBiomeName feature to make the biome virtual.");
	    }
	
	    ForgeBiome forgeBiome = new ForgeBiome(biome, biomeConfig, biomeIds);
	
	    ForgeBiomeRegistryManager.registerBiomeInBiomeDictionary(biome, existingBiome, biomeConfig, configProvider);
	    
	    return forgeBiome;
	}

	private static void registerBiomeInBiomeDictionary(Biome biome, Biome sourceBiome, BiomeConfig biomeConfig, ConfigProvider configProvider)
	{
	    // Add inherited BiomeDictId's for replaceToBiomeName. Biome dict id's are stored twice,
	    // there is 1 list of biomedict types per biome id and one list of biomes (not id's) per biome dict type.
	
	    ArrayList<Type> types = new ArrayList<Type>();
	    if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0)
	    {
	    	// Inherit from an existing biome
	    	if(sourceBiome != null) // Non-otg biome
	    	{
	    		Set<Type> existingTypes = BiomeDictionary.getTypes(sourceBiome);
	   			types = new ArrayList<Type>(existingTypes);
	    	} else {
	    		LocalBiome replaceToBiome = configProvider.getBiomeByOTGIdOrNull(((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().getBiomeRegistryId(sourceBiome != null ? sourceBiome : biome));
	
	        	if(replaceToBiome == null)
	        	{
	        		int replaceToBiomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().getBiomeRegistryId(sourceBiome != null ? sourceBiome : biome);
	        		replaceToBiome = OTG.getBiomeByOTGId(replaceToBiomeId);
	        	}
	    		if(replaceToBiome != null && replaceToBiome.getBiomeConfig().biomeDictId != null)
	    		{
	    			types = ForgeBiomeRegistryManager.getTypesList(replaceToBiome.getBiomeConfig().biomeDictId.split(","));
	    		}
	    	}
	    } else {
	    	// If not replaceToBiomeName then attach BiomeDictId
	        if(biomeConfig.biomeDictId != null && biomeConfig.biomeDictId.trim().length() > 0)
	        {
	        	types = ForgeBiomeRegistryManager.getTypesList(biomeConfig.biomeDictId.split(","));
	        }
	    }
	
		Type[] typeArr = new Type[types.size()];
		types.toArray(typeArr);
	
		if(!ForgeRegistries.BIOMES.containsValue(biome))
		{
			OTG.log(LogMarker.WARN, "Biome " + biome.biomeName + " could not be found in the registry. This could be because it is a virtual biome (id > 255) but does not have a ReplaceToBiomeName configured.");
		}
	
		BiomeDictionary.addTypes(biome, typeArr);
	}

	private static ArrayList<Type> getTypesList(String[] typearr)
	{
		ArrayList<Type> types = new ArrayList<Type>();
		for(String typeString : typearr)
		{
			if(typeString != null && typeString.trim().length() > 0)
			{
		        Type type = null;
				typeString = typeString.trim();
		        try
		        {
		        	type = Type.getType(typeString, null);
		        }
		        catch(Exception ex)
		        {
		        	OTG.log(LogMarker.WARN, "Can't find BiomeDictId: \"" + typeString + "\".");
		        }
		        if(type != null)
		        {
		        	types.add(type);
		        }
			}
		}
		return types;
	}

	public static void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{
		Biome biome = null;
	
		String[] resourceLocationArr = biomeResourceLocation.split(":");
		    	
		String resourceDomain = resourceLocationArr.length > 1 ? resourceLocationArr[0] : null;
		String resourceLocation = resourceLocationArr.length > 1 ? resourceLocationArr[1] : resourceLocationArr[0];
	
		biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(resourceDomain, resourceLocation));
	
		if(biome != null)
		{
			// Merge the vanilla biome's mob spawning lists with the mob spawning lists from the BiomeConfig.
			// Mob spawning settings for the same creature will not be inherited (so BiomeConfigs can override vanilla mob spawning settings).
			// We also inherit any mobs that have been added to vanilla biomes' mob spawning lists by other mods.
			biomeConfigStub.spawnMonstersMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnMonstersMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityCategory.MONSTER));
			biomeConfigStub.spawnCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityCategory.CREATURE));
			biomeConfigStub.spawnAmbientCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnAmbientCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityCategory.AMBIENT_CREATURE));
			biomeConfigStub.spawnWaterCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnWaterCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityCategory.WATER_CREATURE));
		} else {
			OTG.log(LogMarker.WARN, "Biome " + biomeResourceLocation + " not found for InheritMobsFromBiomeName in " + biomeConfigStub.getBiomeName() + ".bc");
		}
	}
   
    private Biome getRegisteredBiome(int id)
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

    private void registerForgeBiome(ResourceLocation resourceLocation, Biome biome)
    {
    	OTG.log(LogMarker.DEBUG, "Registering biome " + resourceLocation.toString());

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

    private int registerForgeBiomeWithId(ResourceLocation resourceLocation, Biome biome)
    {
    	return registerForgeBiomeWithId(-1, resourceLocation, biome);
    }
    
    private int registerForgeBiomeWithId(int id, ResourceLocation resourceLocation, Biome biome)
    {
    	OTG.log(LogMarker.DEBUG, "Registering biome " + resourceLocation.toString() + " " + id);

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
        	OTG.log(LogMarker.WARN,
    			"Tried to register biome " + resourceLocation.toString() + " to a id " + id + " but it is occupied by biome: " + biomeAtId.getRegistryName().toString() + ". "
				+ "This can happen when using the CustomBiomes setting in the world config or when changing mod/biome configurations for previously created worlds. "
				+ "This can also happen when migrating a world from OTG v6 or lower to OTG v8 or higher, if the world had biome conflicts in v6."
				+ "OTG 1.12.2 v8 and above use dynamic biome id's for new worlds, this avoids the problem completely.");
        	
        	// TODO: This could cause problems, but is necessary to support v6 worlds with biome id conflicts
        	id = biomeRegistryAvailabiltyMap.nextClearBit(0);
        	OTG.log(LogMarker.WARN, "Substituting id " + id + " for biome " + resourceLocation.toString());
        }

        ids.put(id, biome);

		biomeRegistryAvailabiltyMap.set(id, true); // Mark the id as used

        return id;
    }

    private void unRegisterForgeBiome(ResourceLocation resourceLocation)
    {
		OTG.log(LogMarker.DEBUG, "Unregistering biome " + resourceLocation.toString());

		Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);

		BitSet biomeRegistryAvailabiltyMap = getBiomeRegistryAvailabiltyMap();
		try
		{
			int biomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().getBiomeRegistryId(biome);
			// If this biome uses replaceToBiomeName and has an id > 255 then it is not actually registered in the biome id
			// registry and biomeId will be 0. Check if biomeId is actually registered to this biome.
			if(biomeId > -1 && ((ForgeEngine)OTG.getEngine()).getBiomeRegistryManager().getRegisteredBiome(biomeId) == biome)
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

			OTG.log(LogMarker.FATAL, "Could not unregister " + biome.biomeName + ", aborting.");
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

	public static void unregisterBiomes(HashMap<String, LocalBiome> biomeNames, ForgeWorld forgeWorld)
	{
		// Unregister only the biomes registered by this world
		for(LocalBiome localBiome : forgeWorld.biomeNames.values())
		{
			ForgeBiomeRegistryManager.unregisterBiome(localBiome, forgeWorld.getName());
		}

		((ForgeEngine)OTG.getEngine()).getWorldLoader().clearBiomeDictionary(forgeWorld);
	}

	public static List<BiomeLoadInstruction> getDefaultBiomes()
	{
        // Loop through all default biomes and create the default
        // settings for them
        List<BiomeLoadInstruction> standardBiomes = new ArrayList<BiomeLoadInstruction>();
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            int id = defaultBiome.Id;
            BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(ForgeMojangSettings.fromId(id), ForgeWorld.STANDARD_WORLD_HEIGHT);
            standardBiomes.add(instruction);
        }

        return standardBiomes;
	}

	public int getAvailableBiomeIdsCount()
	{
		BitSet biomeRegistryAvailabiltyMap = getBiomeRegistryAvailabiltyMap();
		int availableIds = 0;
    	for(int i = 0; i < ForgeWorld.MAX_SAVED_BIOMES_COUNT; i++)
    	{
     		if(i >= biomeRegistryAvailabiltyMap.size() || !biomeRegistryAvailabiltyMap.get(i))
    		{
    			availableIds++;
    		}
    	}
    	return availableIds;
	}
}
