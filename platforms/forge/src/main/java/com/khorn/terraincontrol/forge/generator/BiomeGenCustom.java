package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.util.MobSpawnGroupHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for all custom biomes.
 */
public class BiomeGenCustom extends Biome
{

    public static final int MAX_TC_BIOME_ID = 1023;

    private int skyColor;

    public final int generationId;

    private BiomeGenCustom(BiomeConfig config, BiomeIds id)
    {
        super(new BiomePropertiesCustom(config));
        this.generationId = id.getGenerationId();

        this.skyColor = config.skyColor;
          
        // TODO: Is clearing really necessary here?
        // Don't use the TC default values for mob spawning for Forge, 
        // instead we'll copy mobs lists from the vanilla biomes so we 
        // also get mobs added by other mods.
        // These mobs should be included in config's mob lists.
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        this.spawnableWaterCreatureList.clear();

        // Mob spawning
        addMobs(this.spawnableMonsterList, config.spawnMonstersMerged);//, improvedMobSpawning);
        addMobs(this.spawnableCreatureList, config.spawnCreaturesMerged);//, improvedMobSpawning);
        addMobs(this.spawnableWaterCreatureList, config.spawnWaterCreaturesMerged);//, improvedMobSpawning);
        addMobs(this.spawnableCaveCreatureList, config.spawnAmbientCreaturesMerged);//, improvedMobSpawning);
    }
    
    /**
     * Extension of BiomeProperties so that we are able to access the protected
     * methods.
     */
    private static class BiomePropertiesCustom extends BiomeProperties
    {
        BiomePropertiesCustom(BiomeConfig biomeConfig)
        {
            super(biomeConfig.getName());
            this.setBaseHeight(biomeConfig.biomeHeight);
            this.setHeightVariation(biomeConfig.biomeVolatility);
            this.setRainfall(biomeConfig.biomeWetness);
            this.setWaterColor(biomeConfig.waterColor);
            float safeTemperature = biomeConfig.biomeTemperature;
            if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
            {
                // Avoid temperatures between 0.1 and 0.2, Minecraft restriction
                safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
            }
            this.setTemperature(safeTemperature);
            if (biomeConfig.biomeWetness <= 0.0001)
            {
                this.setRainDisabled();
            }
            if (biomeConfig.biomeTemperature <= WorldStandardValues.SNOW_AND_ICE_MAX_TEMP)
            {
                this.setSnowEnabled();
            }             
        }
    }

    public static Biome getOrCreateBiome(BiomeConfig biomeConfig, BiomeIds biomeIds, boolean isMainWorld)
    {
        // If this biome should replace a vanilla biome then use the vanilla biome's resourcelocation to register the biome.
        ResourceLocation registryKey = ForgeWorld.vanillaResouceLocations.get(biomeIds.getGenerationId());
    	if(registryKey == null)
    	{   
	        int generationId = biomeIds.getGenerationId();
    		
	        // This is a custom biome, get or register it
	        String biomeNameForRegistry = StringHelper.toComputerFriendlyName(biomeConfig.getName());                      
	        String resourceDomain = PluginStandardValues.PLUGIN_NAME.toLowerCase();
	        // 0-39 and 127-167 are vanilla biomes so register them as such   
	        // so that biomes are properly recognised by non-modded clients
	        if((generationId >= 0 && generationId <= 39) || (generationId >= 127 && generationId <= 167))
	        {	        	
	        	//resourceDomain = "minecraft";	        	
	        	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
	        }
	        
	        registryKey = new ResourceLocation(resourceDomain, biomeNameForRegistry);
    	}
        
        // Check if registered earlier
        Biome alreadyRegisteredBiome = Biome.REGISTRY.registryObjects.get(registryKey);
        if (alreadyRegisteredBiome != null)
        {
        	if(isMainWorld) // Override Vanilla biomes
        	{       		        		
        		((ForgeEngine)TerrainControl.getEngine()).unRegisterForgeBiome(registryKey);
        	} else {
        		return alreadyRegisteredBiome;
        	}
        	/*
        	else {
	        	
        		if(1 == 1) { throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash."); }
        		
	        	// Even if the biome is present in registryObjects it may be because the biome was added and removed earlier (only possible for other dimensions) 
	        	// and only its availability was set to "can re-use". If so set the availability back to "used"
	        	// TODO: Is alreadyRegisteredBiome really a reference to the same biome object? Need to replace it with new?        	
	    		BitSet biomeRegistryAvailabiltyMap = ((ForgeEngine)TerrainControl.getEngine()).getWorldLoader().getBiomeRegistryAvailabiltyMap();
				biomeRegistryAvailabiltyMap.set(Biome.getIdForBiome(alreadyRegisteredBiome), true); // Set back to "used" so it doesn't get overridden

        		return alreadyRegisteredBiome;
        	}
        	*/
        }

        // No existing biome, create new one
        BiomeGenCustom customBiome = new BiomeGenCustom(biomeConfig, biomeIds);              
        
        int savedBiomeId = biomeIds.getSavedId();
        ForgeEngine forgeEngine = ((ForgeEngine) TerrainControl.getEngine());

        // We need to init array size because Mojang uses a strange custom
        // ArrayList. RegistryID arrays are not correctly (but randomly!) copied
        // when resized which will cause the ReplaceToBiomeName feature not to
        // work properly.
        if (Biome.getBiome(MAX_TC_BIOME_ID) == null)
        {
            ResourceLocation maxTcBiomeKey = new ResourceLocation(PluginStandardValues.PLUGIN_NAME.toLowerCase(), "null");
            forgeEngine.registerForgeBiome(MAX_TC_BIOME_ID, maxTcBiomeKey,
                    new BiomeGenCustom(biomeConfig, new BiomeIds(MAX_TC_BIOME_ID, MAX_TC_BIOME_ID)));
        }
       
        if (biomeIds.isVirtual())
        {
            // Virtual biomes hack: register, then let original biome overwrite
            // In this way, the id --> biome mapping returns the original biome,
            // and the biome --> id mapping returns savedBiomeId for both the
            // original and custom biome
            Biome existingBiome = Biome.getBiomeForId(savedBiomeId);
            if (existingBiome == null)
            {
                // Original biome not yet registered. This is because it's a
                // custom biome that is loaded after this virtual biome, so it
                // will soon be registered
                forgeEngine.registerForgeBiome(biomeIds.getGenerationId(), registryKey, customBiome);
                TerrainControl.log(LogMarker.TRACE, ",{},{},{}", biomeConfig.getName(), savedBiomeId,
                        biomeIds.getGenerationId());
            } else {
                ResourceLocation existingBiomeKey = Biome.REGISTRY.inverseObjectRegistry.get(existingBiome);
                forgeEngine.registerForgeBiome(biomeIds.getSavedId(), registryKey, customBiome);
                forgeEngine.registerForgeBiome(biomeIds.getSavedId(), existingBiomeKey, existingBiome);
                TerrainControl.log(LogMarker.TRACE, ",{},{},{}", biomeConfig.getName(), savedBiomeId,
                        biomeIds.getGenerationId());
            }
        }
        else if (savedBiomeId < 256 && !biomeIds.isVirtual())
        {
            // Normal insertion
            Biome.REGISTRY.register(savedBiomeId, registryKey, customBiome);
            TerrainControl.log(LogMarker.TRACE, ",{},{},{}", biomeConfig.getName(), savedBiomeId,
                    biomeIds.getGenerationId());
        }
        
        return customBiome;
    }

    // Sky color from Temp
    @Override
    public int getSkyColorByTemp(float v)
    {
        return this.skyColor;
    }

    @Override
    public String toString()
    {
        return "BiomeGenCustom of " + getBiomeName();
    }
   
    // Fix for swamp colors (there's a custom noise applied to swamp colors)
    // TODO: Make these colors configurable via the BiomeConfig
    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos)
    {
    	if(this.getBiomeName().equals("Swampland") || this.getBiomeName().equals("Swampland M"))
    	{
	        double d0 = GRASS_COLOR_NOISE.getValue((double)pos.getX() * 0.0225D, (double)pos.getZ() * 0.0225D);
	        return d0 < -0.1D ? 5011004 : 6975545;
    	} else {
    		return super.getGrassColorAtPos(pos);
    	}
    }
    
    // Fix for swamp colors (there's a custom noise applied to swamp colors)
    // TODO: Make these colors configurable via the BiomeConfig
    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos)
    {
    	if(this.getBiomeName().equals("Swampland") || this.getBiomeName().equals("Swampland M"))
    	{
    		return 6975545;
    	} else {
    		return super.getFoliageColorAtPos(pos);
    	}
    }
   
    // Adds the mobs to the internal list
    public void addMobs(List<SpawnListEntry> internalList, List<WeightedMobSpawnGroup> configList)//, boolean improvedMobSpawning)
    {    
    	List<SpawnListEntry> newList = new ArrayList<SpawnListEntry>();
    	List<SpawnListEntry> newListParent = new ArrayList<SpawnListEntry>();
    	// Add mobs defined in bc's mob spawning settings
        List<SpawnListEntry> spawnListEntry = MobSpawnGroupHelper.toMinecraftlist(configList);
        newList.addAll(spawnListEntry);
        
    	for(SpawnListEntry entryParent : internalList)
    	{
			boolean bFound = false;
			for(SpawnListEntry entryChild : newList)
			{
				if(entryChild.entityClass.equals(entryParent.entityClass))
				{
					bFound = true;
				}
			}
			if(!bFound)
			{
				newListParent.add(entryParent);
			}
    	}
    	newList.addAll(newListParent);
    	
        internalList.clear();
        
        for(SpawnListEntry spe : newList)
        {
        	if(spe.itemWeight > 0 && spe.maxGroupCount > 0)
        	{
        		internalList.add(spe);		
        	}
        }              
    }
}
