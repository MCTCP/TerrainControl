package com.pg85.otg.network;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.io.SimpleSettingsMap;
import com.pg85.otg.configuration.standard.BiomeStandardValues;
import com.pg85.otg.configuration.standard.StandardBiomeTemplate;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.util.BiomeIds;
import com.pg85.otg.util.helpers.StreamHelper;
import com.pg85.otg.util.minecraft.defaults.BiomeRegistryNames;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the WorldConfig and all BiomeConfigs.
 *
 * <p>Note: this is an internal class that is pending a rename. For backwards
 * compatibility it is still here as a public class with this name.
 */
public final class ClientConfigProvider implements ConfigProvider
{
    private WorldConfig worldConfig;

    /**
     * Holds all biome configs. Generation Id => BiomeConfig
     * <p>
     * Must be simple array for fast access. Warning: some ids may contain
     * null values, always check.
     */
    private LocalBiome[] biomesByOTGId; // For the server, OTGBiomeIds are used, for the client only non-virtual biomes are known and saved Id's are used
    private LocalBiome[] biomesBySavedId; // For the server, OTGBiomeIds are used, for the client only non-virtual biomes are known and saved Id's are used    

    public ClientConfigProvider(DataInputStream stream, LocalWorld world) throws IOException
    {
        // Create WorldConfig
        SettingsMap worldSettingsReader = new SimpleSettingsMap(world.getName(), false);
        worldSettingsReader.putSetting(WorldStandardValues.WORLD_FOG, stream.readInt());
        worldSettingsReader.putSetting(WorldStandardValues.WORLD_NIGHT_FOG, stream.readInt());
        worldSettingsReader.putSetting(WorldStandardValues.WATER_LEVEL_MAX, stream.readInt());
        worldSettingsReader.putSetting(WorldStandardValues.DEFAULT_OCEAN_BIOME, StreamHelper.readStringFromStream(stream));       
        worldSettingsReader.putSetting(WorldStandardValues.DEFAULT_FROZEN_OCEAN_BIOME, StreamHelper.readStringFromStream(stream));
        worldConfig = new WorldConfig(new File("."), worldSettingsReader, world, null);

        // BiomeConfigs
        StandardBiomeTemplate defaultSettings = new StandardBiomeTemplate(worldConfig.worldHeightCap);
        biomesByOTGId = new LocalBiome[world.getMaxBiomesCount()];
        biomesBySavedId = new LocalBiome[world.getMaxBiomesCount()];

        int count = stream.readInt();
        while (count-- > 0)
        {
            int otgBiomeId = stream.readInt();
            int savedBiomeId = stream.readInt();
            String biomeName = StreamHelper.readStringFromStream(stream);
            SettingsMap biomeReader = new SimpleSettingsMap(biomeName, false);
            biomeReader.putSetting(BiomeStandardValues.BIOME_TEMPERATURE, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.BIOME_WETNESS, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.FOG_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.FOG_DENSITY, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.FOG_RAIN_WEIGHT, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.FOG_THUNDER_WEIGHT, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.FOG_TIME_WEIGHT, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.SKY_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.WATER_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR_2, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR_IS_MULTIPLIER, stream.readBoolean());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR_2, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR_IS_MULTIPLIER, stream.readBoolean());

            String replaceToBiomeName = StreamHelper.readStringFromStream(stream);
        	biomeReader.putSetting(BiomeStandardValues.REPLACE_TO_BIOME_NAME, replaceToBiomeName); // <-- This might be used even in MP by client mods?
            
            // TODO: Are these really necessary? <-- Maybe only for Forge SP? <-- In ClientNetworkEventListener for Forge SP packet is ignored so this doesn't actually do anything??
            String biomeDictId = StreamHelper.readStringFromStream(stream);
        	biomeReader.putSetting(BiomeStandardValues.BIOME_DICT_ID, biomeDictId); // <-- This might be used even in MP by client mods?

            BiomeLoadInstruction instruction = new BiomeLoadInstruction(biomeName, defaultSettings);
            BiomeConfig config = new BiomeConfig(instruction, null, biomeReader, worldConfig);

            LocalBiome biome = world.createBiomeFor(config, new BiomeIds(otgBiomeId, savedBiomeId), this, false);
            biomesByOTGId[otgBiomeId] = biome;
            if(savedBiomeId == otgBiomeId || BiomeRegistryNames.getRegistryNameForDefaultBiome(biomeName) != null) // Non-virtual and default biomes only
            {
            	biomesBySavedId[savedBiomeId] = biome;
            }
            
        	OTG.getEngine().setOTGBiomeId(world.getName(), otgBiomeId, config, true);
        }
    }

    @Override
    public WorldConfig getWorldConfig()
    {
        return worldConfig;
    }

    @Override
    public LocalBiome getBiomeByOTGIdOrNull(int id)
    {
        if (id < 0 || id > biomesByOTGId.length)
        {
            return null;
        }
        return biomesByOTGId[id];
    }   

    @Override
    public void reload()
    {
        // Does nothing on client world
    }

    @Override
    public LocalBiome[] getBiomeArrayByOTGId()
    {
        return this.biomesByOTGId;
    }
    
    
	@Override
	public List<LocalBiome> getBiomeArrayLegacy()
	{        
        // For backwards compatibility, sort the biomes by saved id and return default biomes as if they were custom biomes 
        List<LocalBiome> nonDefaultbiomes = new ArrayList<LocalBiome>();
        LocalBiome[] defaultBiomes = new LocalBiome[256];
        for(LocalBiome biome : this.biomesByOTGId)
        {
        	if(biome != null)
        	{
        		Integer defaultBiomeId = DefaultBiome.getId(biome.getName());
        		if(defaultBiomeId != null)
        		{
        			defaultBiomes[defaultBiomeId.intValue()] = biome;
        		} else {
        			nonDefaultbiomes.add(biome);
        		}
        	}
        }

        List<LocalBiome> outputBiomes = new ArrayList<LocalBiome>();
        for(LocalBiome biome : defaultBiomes)
        {
        	if(biome != null)
        	{
        		outputBiomes.add(biome);
        	}
        }
        outputBiomes.addAll(nonDefaultbiomes);
        
		return outputBiomes;
	}
}
