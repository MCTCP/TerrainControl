package com.pg85.otg.forge.biomes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map.Entry;
import java.util.Set;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.standard.MojangSettings.EntityCategory;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.BiomeIds;
import com.pg85.otg.util.helpers.StringHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomeRegistryManager
{
	public static Biome getRegisteredBiome(String resourceLocationString, String worldName)
	{
		if(resourceLocationString != null && !resourceLocationString.trim().isEmpty())
		{
			String[] resourceLocationStringArr = resourceLocationString.split(":");
			if(resourceLocationStringArr.length == 1) // When querying for biome name without domain search the local world's biomes 
			{
				ResourceLocation resourceLocation = new ResourceLocation(PluginStandardValues.MOD_ID.toLowerCase(), worldName + "_" + resourceLocationStringArr[0].replaceAll(" ", "_"));
				return ForgeRegistries.BIOMES.getValue(resourceLocation);
			}
			if(resourceLocationStringArr.length == 2)
			{
				ResourceLocation resourceLocation = new ResourceLocation(resourceLocationStringArr[0],resourceLocationStringArr[1]);
				return ForgeRegistries.BIOMES.getValue(resourceLocation);
			}
		}

		return null;
	}
	
	public static int getRegisteredBiomeId(String resourceLocationString, String worldName)
	{
		if(resourceLocationString != null && !resourceLocationString.trim().isEmpty())
		{
			String[] resourceLocationStringArr = resourceLocationString.split(":");
			if(resourceLocationStringArr.length == 1) // When querying for biome name without domain search the local world's biomes 
			{
				ResourceLocation resourceLocation = new ResourceLocation(PluginStandardValues.MOD_ID.toLowerCase(), worldName + "_" + resourceLocationStringArr[0].replaceAll(" ", "_"));
				Biome requestedBiome = ForgeRegistries.BIOMES.getValue(resourceLocation);
				return ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(requestedBiome);
			}
			if(resourceLocationStringArr.length == 2)
			{
				ResourceLocation resourceLocation = new ResourceLocation(resourceLocationStringArr[0],resourceLocationStringArr[1]);
				Biome requestedBiome = ForgeRegistries.BIOMES.getValue(resourceLocation);
				return ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(requestedBiome);
			}
		}
		return -1;
	}

	public static void UnregisterBiome(LocalBiome localBiome, String worldName)
	{
		//if(((ForgeEngine)OTG.getEngine()).worldLoader.isConfigUnique(localBiome.getBiomeConfig().getName()))
		{
	        String biomeNameForRegistry = StringHelper.toComputerFriendlyName(localBiome.getName());
	        String resourceDomain = PluginStandardValues.PLUGIN_NAME.toLowerCase();
	        ResourceLocation registryKey = new ResourceLocation(resourceDomain, worldName + "_" + biomeNameForRegistry);

	        ((ForgeEngine)OTG.getEngine()).unRegisterForgeBiome(registryKey);
	        ((ForgeEngine)OTG.getEngine()).unregisterOTGBiomeId(worldName, localBiome.getIds().getOTGBiomeId());
		}	
	}

	public static void ClearOTGBiomeIds()
	{
		BitSet biomeRegistryAvailabiltyMap = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryAvailabiltyMap();
		for(Entry<ResourceLocation, Biome> biome : ForgeRegistries.BIOMES.getEntries())
		{
			if(!(biome.getKey().getResourceDomain().toLowerCase().equals("openterraingenerator")))
			{
				continue;
			}
	
			OTG.log(LogMarker.DEBUG, "Unregistering " + biome.getValue().biomeName);
	
			int biomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(biome.getValue());
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
	
	        //if (biomeIds.isVirtual())
	        if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0) // This biome uses ReplaceToBiomeName and should use the ReplaceToBiomeName biome's id.
	        {
	        	// Only register by resourcelocation.
	        	// TODO: Make sure this is enough for Forge 1.12+ <- It looks like the server may not send the biomes to the client if they are not added to the registry. TODO: Check if only virtual biomes have this problem.
	        	forgeEngine.registerForgeBiome(registryKey, customBiome);
	        	
	        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
	        	customBiome.savedId = biomeIds.getSavedId();        	
	        }
	        else if(biomeIds.getSavedId() > -1) 
	        {
	        	// This is a biome for an existing world, make sure it uses the same biome id as before.         
	        	int newId = forgeEngine.registerForgeBiomeWithId(biomeIds.getSavedId(), registryKey, customBiome);       	
	        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
	        	customBiome.savedId = newId;
	        	
	        } else {
	
	            // Normal insertion, get next free id and register id+resourcelocation
	
	        	// TODO: Make this prettier?
	        	int newId = forgeEngine.registerForgeBiomeWithId(registryKey, customBiome);
	        	biomeIds.setSavedId(newId);
	        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
	        	customBiome.savedId = newId;       	        
	        }
	        
	    	OTG.log(LogMarker.DEBUG, "{}, {}, {}, {}", biomeConfig.getName(), biomeIds.getSavedId(), biomeIds.getOTGBiomeId(), registryKey.toString());
	
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
	
	    BiomeRegistryManager.registerBiomeInBiomeDictionary(biome, existingBiome, biomeConfig, configProvider);
	    
	    return forgeBiome;
	}

	static void registerBiomeInBiomeDictionary(Biome biome, Biome sourceBiome, BiomeConfig biomeConfig, ConfigProvider configProvider)
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
	    		LocalBiome replaceToBiome = configProvider.getBiomeByOTGIdOrNull(((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(sourceBiome != null ? sourceBiome : biome));
	
	        	if(replaceToBiome == null)
	        	{
	        		int replaceToBiomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(sourceBiome != null ? sourceBiome : biome);
	        		replaceToBiome = OTG.getBiomeByOTGId(replaceToBiomeId);
	        	}
	    		if(replaceToBiome != null && replaceToBiome.getBiomeConfig().biomeDictId != null)
	    		{
	    			types = BiomeRegistryManager.getTypesList(replaceToBiome.getBiomeConfig().biomeDictId.split(","));
	    		}
	    	}
	    } else {
	    	// If not replaceToBiomeName then attach BiomeDictId
	        if(biomeConfig.biomeDictId != null && biomeConfig.biomeDictId.trim().length() > 0)
	        {
	        	types = BiomeRegistryManager.getTypesList(biomeConfig.biomeDictId.split(","));
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

	static ArrayList<Type> getTypesList(String[] typearr)
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
		    	
		String resourceDomain = resourceLocationArr[0];
		String resourceLocation = resourceLocationArr[1];
	
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
			throw new RuntimeException("Biome " + biomeResourceLocation + " not found for InheritMobsFromBiomeName in " + biomeConfigStub.getBiomeName() + ".bc");
		}
	}	
}
