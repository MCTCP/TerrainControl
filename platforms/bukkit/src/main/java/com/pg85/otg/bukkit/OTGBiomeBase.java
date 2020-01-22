package com.pg85.otg.bukkit;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.util.EnumHelper;
import com.pg85.otg.bukkit.util.MobSpawnGroupHelper;
import com.pg85.otg.bukkit.util.WorldHelper;
import com.pg85.otg.common.BiomeIds;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.settings.WeightedMobSpawnGroup;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StringHelper;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import org.bukkit.block.Biome;

import java.util.List;

public class OTGBiomeBase extends BiomeBase
{
    private static final int MAX_OTG_BIOME_ID = 4095;
    public int otgBiomeId;
    private int savedId;

    /**
     * Mojang made the methods on BiomeBase.a protected (so only accessable for
     * classes in the package net.minecraft.world.biome package and for
     * subclasses of BiomeBase.a). To get around this, we have to subclass
     * BiomeBase.a.
     */
    private static class BiomeBase_a extends BiomeBase.a
    {

        public BiomeBase_a(String name, BiomeConfig biomeConfig)
        {
            super(name);

            // Minecraft doesn't like temperatures between 0.1 and 0.2, so avoid
            // them: round them to either 0.1 or 0.2
            float adjustedTemperature = biomeConfig.biomeTemperature;
            if (adjustedTemperature >= 0.1 && adjustedTemperature <= 0.2)
            {
                if (adjustedTemperature >= 1.5)
                    adjustedTemperature = 0.2f;
                else
                    adjustedTemperature = 0.1f;
            }

            c(biomeConfig.biomeHeight);
            d(biomeConfig.biomeVolatility);
            a(adjustedTemperature);
            b(biomeConfig.biomeWetness);
            if (biomeConfig.biomeWetness <= 0.0001)
            {
                a(); // disableRain()
            }
            if (biomeConfig.biomeTemperature <= WorldStandardValues.SNOW_AND_ICE_MAX_TEMP)
            {
                b(); // enableSnowfall()
            }
        }
    }
    
    private OTGBiomeBase(BiomeConfig biomeConfig)
    {
        super(new BiomeBase_a(biomeConfig.getName(), biomeConfig));

        // Sanity check
        if (this.getHumidity() != biomeConfig.biomeWetness)
        {
            throw new AssertionError("Biome temperature mismatch");
        }

        this.q = ((BukkitMaterialData) biomeConfig.surfaceBlock).internalBlock();
        this.r = ((BukkitMaterialData) biomeConfig.groundBlock).internalBlock();

        // Mob spawning
        addMobs(this.t, biomeConfig.spawnMonsters);
        addMobs(this.u, biomeConfig.spawnCreatures);
        addMobs(this.v, biomeConfig.spawnWaterCreatures);
        addMobs(this.w, biomeConfig.spawnAmbientCreatures);
    }

    // Adds the mobs to the internal list.
    private void addMobs(List<BiomeMeta> internalList, List<WeightedMobSpawnGroup> configList)
    {
        internalList.clear();
        internalList.addAll(MobSpawnGroupHelper.toMinecraftlist(configList));
    }
    
    static OTGBiomeBase createInstance(BiomeConfig biomeConfig, BiomeIds biomeIds, String worldName, boolean isReload)
    {
        // We need to init array size because Mojang uses a strange custom
        // ArrayList. RegistryID arrays are not correctly (but randomly!) copied
        // when resized.
        if(BiomeBase.getBiome(MAX_OTG_BIOME_ID) == null)
        {
            BiomeBase.REGISTRY_ID.a(
        		MAX_OTG_BIOME_ID,
	            new MinecraftKey(PluginStandardValues.PLUGIN_NAME, "null"),
	            new OTGBiomeBase(biomeConfig)
            );
        }
    	
        String biomeNameWithoutSpaces = worldName.toLowerCase() + "_" + StringHelper.toComputerFriendlyName(biomeConfig.getName());
        MinecraftKey biomeKey = new MinecraftKey(PluginStandardValues.MOD_ID, biomeNameWithoutSpaces);
          
        OTGBiomeBase customBiome = new OTGBiomeBase(biomeConfig);

        if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0) // This biome uses ReplaceToBiomeName and should use the ReplaceToBiomeName biome's id.
        {
        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
        	customBiome.savedId = biomeIds.getSavedId();
        	
            // Virtual biomes hack: register, then let original biome overwrite
            // In this way, the id --> biome mapping returns the original biome,
            // and the biome --> id mapping returns savedBiomeId for both the
            // original and custom biome
            BiomeBase existingBiome = BiomeBase.getBiome(customBiome.savedId);

            BiomeBase.REGISTRY_ID.a(customBiome.savedId, biomeKey, customBiome);
            
            if (existingBiome != null)
            {
                MinecraftKey existingBiomeKey = BiomeBase.REGISTRY_ID.b(existingBiome);
                BiomeBase.REGISTRY_ID.a(customBiome.savedId, existingBiomeKey, existingBiome);

                // String existingBiomeName = existingBiome.getClass().getSimpleName();
                // if(existingBiome instanceof CustomBiome) {
                //     existingBiomeName = String.valueOf(((CustomBiome) existingBiome).generationId);
                // }
            }
        }
        else if(biomeIds.getSavedId() > -1) 
        {
        	// This is a biome for an existing world, make sure it uses the same biome id as before.         
        	BiomeBase biomeAtId = BiomeBase.REGISTRY_ID.getId(biomeIds.getSavedId());
            if(biomeAtId != null && !isReload)
            {
            	throw new RuntimeException(
        			"Tried to register biome " + biomeKey.toString() + " to a id " + biomeIds.getSavedId() + " but it is occupied by biome: " + biomeAtId.toString() + ". "
					+ "This can happen when using the CustomBiomes setting in the world config or when changing mod/biome configurations for previously created worlds. "
					+ "This can also happen when migrating a world from OTG v6 or lower to OTG v8 or higher, if the world had biome conflicts in v6."
					+ "OTG 1.12.2 v8 and above use dynamic biome id's for new worlds, this avoids the problem completely.");
            }
            
        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
        	customBiome.savedId = biomeIds.getSavedId(); 
        	
            // Normal insertion       	
            BiomeBase.REGISTRY_ID.a(customBiome.savedId, biomeKey, customBiome);
        	
        } else {

            // Normal insertion, get next free id and register id+resourcelocation

        	int newId = 0;
        	while(BiomeBase.REGISTRY_ID.getId(newId) != null)
        	{
        		if(newId == MAX_OTG_BIOME_ID)
        		{
        			throw new RuntimeException("Biome could not be registered, no free biome id's!");
        		}
        		newId++;
        	}
        	biomeIds.setSavedId(newId);
        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
        	customBiome.savedId = newId; 
        	
            // Normal insertion       	
            BiomeBase.REGISTRY_ID.a(customBiome.savedId, biomeKey, customBiome);
        }
        
    	OTG.log(LogMarker.DEBUG, "{}, {}, {}, {}", biomeConfig.getName(), biomeIds.getSavedId(), biomeIds.getOTGBiomeId(), biomeKey.toString());

        // Add biome to Bukkit enum if it's not there yet
        try {
            Biome.valueOf(biomeNameWithoutSpaces.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            EnumHelper.addEnum(Biome.class, biomeNameWithoutSpaces.toUpperCase(), new Class[0], new Object[0]);
        }

        // Sanity check: check if biome was actually registered
        int registeredSavedId = WorldHelper.getSavedId(customBiome);
        if (registeredSavedId != customBiome.savedId)
        {
            throw new AssertionError("Biome " + biomeConfig.getName() + " is not properly registered: got id " + registeredSavedId + ", should be " + customBiome.savedId);
        }

        checkRegistry();

        return customBiome;    
    }
    
    /**
     * Check if biome ID registry is well filled.
     */
    private static void checkRegistry()
    {
        for(int i = 0; i < 256; i++)
    	{
            BiomeBase biome = getBiome(i);
            if(biome != null && biome instanceof OTGBiomeBase && ((OTGBiomeBase) biome).savedId != i)
            {
                throw new AssertionError("Biome ID #" + i + " returns custom biome #" + ((OTGBiomeBase) biome).savedId + " instead of its own.");
            }
        }
    }
}
