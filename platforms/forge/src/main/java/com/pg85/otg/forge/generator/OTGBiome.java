package com.pg85.otg.forge.generator;

import com.pg85.otg.BiomeIds;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.BiomeConfig;
import com.pg85.otg.configuration.WeightedMobSpawnGroup;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.asm.IOTGASMBiome;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StringHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;

/**
 * Used for all custom biomes.
 * Note: This class implements some fishy stuff in equals() and hashCode().
 * As a result, comparing any OTGBiome with biomeName "Ocean" to any other
 * OTGBiome with biomeName "Ocean" will return true.
 */
public class OTGBiome extends Biome implements IOTGASMBiome
{

    public static final int MAX_TC_BIOME_ID = 1023;

    private int skyColor;

    public int otgBiomeId;
    public int savedId;

    private OTGBiome(BiomeConfig config, ResourceLocation registryKey)
    {
        super(new BiomePropertiesCustom(config));

        this.setRegistryName(registryKey);
        
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

    public static Biome getOrCreateBiome(BiomeConfig biomeConfig, BiomeIds biomeIds, boolean isMainWorld, String worldName)
    {
        // If this biome should replace a vanilla biome then use the vanilla biome's resourcelocation to register the biome.
        //ResourceLocation registryKey = ForgeWorld.vanillaResouceLocations.get(biomeIds.getGenerationId());
    	//if(registryKey == null)
    	//{
	        //int generationId = biomeIds.getGenerationId();

	        // This is a custom biome, get or register it
	        String biomeNameForRegistry = worldName.toLowerCase() + "_" + StringHelper.toComputerFriendlyName(biomeConfig.getName());
	        //String resourceDomain = PluginStandardValues.PLUGIN_NAME.toLowerCase();
	        String resourceDomain = OTGPlugin.MOD_ID;
	        // 0-39 and 127-167 are vanilla biomes so register them as such
	        // so that biomes are properly recognised by non-modded clients
	        //if((generationId >= 0 && generationId <= 39) || (generationId >= 127 && generationId <= 167))
	        {
	        	//throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
	        }

	        //registryKey = new ResourceLocation(resourceDomain, biomeNameForRegistry);
	        ResourceLocation registryKey = new ResourceLocation(resourceDomain, biomeNameForRegistry);
    	//} else {
    		//throw new RuntimeException("This shouldn't happen.");
    	//}

        // Check if registered earlier
    	Biome alreadyRegisteredBiome = ForgeRegistries.BIOMES.getValue(registryKey);
        if (alreadyRegisteredBiome != null)
        {        	
        	// This can happen when an unloaded world is loaded, its biomes have already been registered
        	return alreadyRegisteredBiome;
        	
        	
        	// This happens for SP clients, where client and server share registries. TODO: Not anymore? Disabled packets for SP since client and server share data?
        	//if(Minecraft.getMinecraft().isSingleplayer())
        	//{
        		//return alreadyRegisteredBiome;
        	//}
        	
        	//throw new RuntimeException("This shouldn't happen");
        	
        	//if(isMainWorld)
        	//{
        		//((ForgeEngine)OTG.getEngine()).unRegisterForgeBiome(registryKey);
        	//} else {
        		//return alreadyRegisteredBiome;
        	//}
        }

        // No existing biome, create new one
        OTGBiome customBiome = new OTGBiome(biomeConfig, registryKey);

        ForgeEngine forgeEngine = ((ForgeEngine) OTG.getEngine());

        //if (biomeIds.isVirtual())
        if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0) // This biome uses ReplaceToBiomeName and should use the ReplaceToBiomeName biome's id.
        {
            // Biome.REGISTRY.inverseObjectRegistry has been changed so can't use the old approach anymore
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
        
    	OTG.log(LogMarker.INFO, "{}, {}, {}, {}", biomeConfig.getName(), biomeIds.getSavedId(), biomeIds.getOTGBiomeId(), registryKey.toString());

        // Register biome, silence the logger to hide warnings against using the "minecraft" resourceDomain.
        //if(registryKey.getResourceDomain().equals("minecraft"))
        //{
	        //Level loglvl =((Logger)FMLLog.getLogger()).getLevel();
	        //((Logger)FMLLog.getLogger()).setLevel(Level.OFF);
	        //customBiome.setRegistryName(registryKey);
	        //((Logger)FMLLog.getLogger()).setLevel(loglvl);
        //} else {
        	//customBiome.setRegistryName(registryKey);
        //}

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
        return "OTGBiome of " + biomeName;
    }

    // Fix for swamp colors (there's a custom noise applied to swamp colors)
    // TODO: Make these colors configurable via the BiomeConfig
    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos)
    {
    	if(this.biomeName.equals("Swampland") || this.biomeName.equals("Swampland M"))
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
    	if(this.biomeName.equals("Swampland") || this.biomeName.equals("Swampland M"))
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

    @Override
	public int getSavedId()
	{
		return savedId;
	}

    @Override
    public boolean equals(Object obj)
    {
    	// TODO: Remove this?
		// TODO: This is a super ugly hack, make this prettier..
		// Need to do this to make sure that any OTGBiome of Ocean registered in the biome registry
		// can be found when querying biome names / id's using a different OTGBiome of Ocean.
		// This is because MC and mods may cache the ocean biome at some point and use it as a default value/fallback.
		// Since OTG replaces the biome in the registry every time a new (over)world is created this causes problems.
    	return obj instanceof OTGBiome && ((OTGBiome)obj).biomeName.equals("Ocean") && this.biomeName.equals("Ocean") ? true : super.equals(obj);
    }
}