package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.forge.util.MobSpawnGroupHelper;

import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

/**
 * Used for all custom biomes.
 */
public class BiomeGenCustom extends Biome
{
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

    public static Biome getOrCreateBiome(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        if (DefaultBiome.Contain(biomeConfig.getName()))
        {
            // This is a default biome, retrieve by id
            return Biome.getBiome(biomeIds.getGenerationId());
        }

        // This is a custom biome, get or register it
        String biomeNameForRegistry = StringHelper.toComputerFriendlyName(biomeConfig.getName());
        ResourceLocation registryKey = new ResourceLocation(PluginStandardValues.PLUGIN_NAME.toLowerCase(), biomeNameForRegistry);

        // Check if registered earlier
        @SuppressWarnings("unchecked")
        RegistryNamespaced<ResourceLocation, Biome> registry = ((RegistryNamespaced<ResourceLocation, Biome>) GameRegistry.findRegistry(
                Biome.class));
        Biome alreadyRegisteredBiome = registry.getObject(registryKey);
        if (alreadyRegisteredBiome != null)
        {
            TerrainControl.log(LogMarker.INFO, "Biome {} already registered, skipping.", biomeNameForRegistry);
            return alreadyRegisteredBiome;
        }

        // No existing biome, create new one
        BiomeGenCustom biome = new BiomeGenCustom(biomeConfig, registryKey, biomeIds);
        if (!biomeIds.isVirtual()) {
            registry.register(biomeIds.getGenerationId(), biome.getRegistryName(), biome);
        }
        return biome;
    }

    private int skyColor;

    public final int generationId;

    public BiomeGenCustom(BiomeConfig config, ResourceLocation registryKey, BiomeIds id)
    {
        super(new BiomePropertiesCustom(config));
        setRegistryName(registryKey);
        this.generationId = id.getGenerationId();

        this.skyColor = config.skyColor;

        // Mob spawning
        addMobs(this.spawnableMonsterList, config.spawnMonsters);
        addMobs(this.spawnableCreatureList, config.spawnCreatures);
        addMobs(this.spawnableWaterCreatureList, config.spawnWaterCreatures);
        addMobs(this.spawnableCaveCreatureList, config.spawnAmbientCreatures);
    }

    // Adds the mobs to the internal list
    protected void addMobs(List<SpawnListEntry> internalList, List<WeightedMobSpawnGroup> configList)
    {
        internalList.clear();
        internalList.addAll(MobSpawnGroupHelper.toMinecraftlist(configList));
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

}
