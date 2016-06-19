package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.configuration.io.SimpleSettingsMap;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.configuration.standard.StandardBiomeTemplate;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObjectCollection;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Holds the WorldConfig and all BiomeConfigs.
 *
 * <p>Note: this is an internal class that is pending a rename. For backwards
 * compatibility it is still here as a public class with this name.
 */
public final class ClientConfigProvider implements ConfigProvider
{

    private CustomObjectCollection customObjects;
    private WorldConfig worldConfig;

    /**
     * Holds all biome configs. Generation Id => BiomeConfig
     * <p>
     * Must be simple array for fast access. Warning: some ids may contain
     * null values, always check.
     */
    private LocalBiome[] biomes;

    public ClientConfigProvider(DataInputStream stream, LocalWorld world) throws IOException
    {
        // We need a valid CustomObjects object with things like the trees in
        // it, so that the configs can load without errors
        // An empty CustomObjects instance with the global objects as fallback
        // would work just as well
        this.customObjects = TerrainControl.getCustomObjectManager().getGlobalObjects();

        // Create WorldConfig
        SettingsMap worldSettingsReader = new SimpleSettingsMap(world.getName(), false);
        worldSettingsReader.putSetting(WorldStandardValues.WORLD_FOG, stream.readInt());
        worldSettingsReader.putSetting(WorldStandardValues.WORLD_NIGHT_FOG, stream.readInt());
        worldConfig = new WorldConfig(new File("."), worldSettingsReader, world, customObjects);

        // Custom biomes + ids
        int count = stream.readInt();
        while (count-- > 0)
        {
            String biomeName = ConfigFile.readStringFromStream(stream);
            int id = stream.readInt();
            worldConfig.customBiomeGenerationIds.put(biomeName, id);
        }

        // BiomeConfigs
        StandardBiomeTemplate defaultSettings = new StandardBiomeTemplate(worldConfig.worldHeightCap);
        biomes = new LocalBiome[world.getMaxBiomesCount()];

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            String biomeName = ConfigFile.readStringFromStream(stream);
            SettingsMap biomeReader = new SimpleSettingsMap(biomeName, false);
            biomeReader.putSetting(BiomeStandardValues.BIOME_TEMPERATURE, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.BIOME_WETNESS, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.SKY_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.WATER_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR_IS_MULTIPLIER, stream.readBoolean());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR_IS_MULTIPLIER, stream.readBoolean());

            BiomeLoadInstruction instruction = new BiomeLoadInstruction(biomeName, id, defaultSettings);
            BiomeConfig config = new BiomeConfig(instruction, biomeReader, worldConfig);

            LocalBiome biome = world.createBiomeFor(config, new BiomeIds(id));
            biomes[id] = biome;
        }
    }

    @Override
    public WorldConfig getWorldConfig()
    {
        return worldConfig;
    }

    @Override
    public LocalBiome getBiomeByIdOrNull(int id)
    {
        if (id < 0 || id > biomes.length)
        {
            return null;
        }
        return biomes[id];
    }

    @Override
    public void reload()
    {
        // Does nothing on client world
    }

    @Override
    public LocalBiome[] getBiomeArray()
    {
        return this.biomes;
    }

    @Override
    public CustomObjectCollection getCustomObjects()
    {
        return this.customObjects;
    }

}
