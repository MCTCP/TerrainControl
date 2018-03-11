package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.asm.mixin.iface.IMixinForgeRegistry;
import com.khorn.terraincontrol.forge.util.MobSpawnGroupHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.List;

/**
 * Used for all custom biomes.
 */
public class TXBiome extends Biome
{
    private int skyColor;

    public final BiomeIds id;

    private TXBiome(BiomeConfig config, BiomeIds id)
    {
        super(new BiomePropertiesCustom(config));
        this.id = id;

        this.skyColor = config.skyColor;

        // Mob spawning
        addMobs(this.spawnableMonsterList, config.spawnMonsters);
        addMobs(this.spawnableCreatureList, config.spawnCreatures);
        addMobs(this.spawnableWaterCreatureList, config.spawnWaterCreatures);
        addMobs(this.spawnableCaveCreatureList, config.spawnAmbientCreatures);
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

    public static Biome getOrCreateBiome(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        final String biomeNameForRegistry = StringHelper.toComputerFriendlyName(biomeConfig.getName());
        final ResourceLocation registryKey = new ResourceLocation(PluginStandardValues.PLUGIN_NAME.toLowerCase(), biomeNameForRegistry);

        final int generationBiomeId = biomeIds.getGenerationId();
        final int savedBiomeId = biomeIds.getSavedId();

        Biome alreadyRegisteredBiome = Biome.getBiome(generationBiomeId);
        if (alreadyRegisteredBiome != null) {
            if (!StringHelper.toComputerFriendlyName(alreadyRegisteredBiome.biomeName).equalsIgnoreCase(biomeNameForRegistry)) {
                throw new RuntimeException("Attempt was made to register biome '" + registryKey + "' with id '" + generationBiomeId + "' but this "
                        + "has already been registered to '" + alreadyRegisteredBiome.getRegistryName() + "'!");
            }

            return alreadyRegisteredBiome;
        }

        final ForgeRegistry<Biome> registry = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;

        alreadyRegisteredBiome = registry.getValue(registryKey);
        if (alreadyRegisteredBiome != null)
        {
            final int existingBiomeId = registry.getID(alreadyRegisteredBiome);
            if (biomeIds.getGenerationId() != existingBiomeId) {
                throw new RuntimeException("'" + registryKey + "' was registered with id '" + existingBiomeId + "' but an attempt has been made to "
                        + "register it with another id of '" + generationBiomeId + "'. Biome ids must be the same across all world configs.");
            }
            return alreadyRegisteredBiome;
        }

        alreadyRegisteredBiome = Biome.getBiome(generationBiomeId);
        if (alreadyRegisteredBiome != null) {
            throw new RuntimeException("Attempt was made to attempted to register biome '" + registryKey + "' with id '" + generationBiomeId + "' "
                    + "but that has already been registered to '" + alreadyRegisteredBiome.getRegistryName() + "'!");
        }

        if (generationBiomeId > 256 && savedBiomeId > 256) {
            throw new RuntimeException("Attempt was made to register virtual biome '" + registryKey + "' with id '" + generationBiomeId + "' but "
                    + "the save id '" + savedBiomeId + "' is not 256 or below! Minecraft only supports up to 256 in world data (check what you "
                    + "specified for ReplaceToBiomeName).");
        }

        final TXBiome customBiome = new TXBiome(biomeConfig, biomeIds);
        customBiome.setRegistryName(registryKey);

        final ForgeEngine forgeEngine = ((ForgeEngine) TerrainControl.getEngine());

        if (biomeIds.isVirtual())
        {
            if (generationBiomeId < 256) {
                throw new RuntimeException("Attempt to register virtual biome '" + registryKey + "' but the virtual id '" + generationBiomeId + "' "
                        + "is not above the biome limit! Either raise the biome id to > 256 or remove what is specified for 'ReplaceToBiomeName'");

            }
            Biome existingBiome = Biome.getBiome(savedBiomeId);
            if (existingBiome == null)
            {
                throw new RuntimeException("Attempt to register virtual biome '" + registryKey + "' but the saved biome id '" + savedBiomeId + "' "
                        + "does not exist!");
            } else
            {
                forgeEngine.registerForgeBiome(generationBiomeId, registryKey, customBiome);
                TerrainControl.log(LogMarker.DEBUG, ",{},{},{}", registryKey, savedBiomeId, generationBiomeId);
            }
        } else if (savedBiomeId < 256)
        {
            final Biome existingBiome = registry.getValue(savedBiomeId);
            if (existingBiome != null) {
                throw new RuntimeException("'" + registryKey + "' is attempting to register as id '" + savedBiomeId + "' but that is already " +
                        "registered to '" + existingBiome.getRegistryName() + "'!");
            }

            forgeEngine.registerForgeBiome(savedBiomeId, registryKey, customBiome);
            TerrainControl.log(LogMarker.DEBUG, ",{},{},{}", biomeConfig.getName(), savedBiomeId, biomeIds.getGenerationId());
        }

        if (!BiomeDictionary.hasAnyType(customBiome)) {
            BiomeDictionary.makeBestGuess(customBiome);
        }

        return customBiome;
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
        return "BiomeGenCustom of " + this.biomeName;
    }

}
