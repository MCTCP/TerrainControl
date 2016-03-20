package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.forge.util.MobSpawnGroupHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;

public class BiomeGenCustom extends BiomeGenBase
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

    private int skyColor;
    private int grassColor;
    private boolean grassColorIsMultiplier;
    private int foliageColor;
    private boolean foliageColorIsMultiplier;

    private boolean grassColorSet = false;
    private boolean foliageColorSet = false;

    public final int generationId;

    public BiomeGenCustom(BiomeConfig config, BiomeIds id)
    {
        super(new BiomePropertiesCustom(config));
        this.generationId = id.getGenerationId();

        this.skyColor = config.skyColor;
        this.grassColor = config.grassColor;
        this.grassColorIsMultiplier = config.grassColorIsMultiplier;
        this.foliageColor = config.foliageColor;
        this.foliageColorIsMultiplier = config.foliageColorIsMultiplier;

        if (this.grassColor != 0xffffff)
            this.grassColorSet = true;

        if (this.foliageColor != 0xffffff)
            this.foliageColorSet = true;

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
    public int getModdedBiomeGrassColor(int original)
    {
        if (!this.grassColorSet)
            return original;
        if (grassColorIsMultiplier)
        {
            return ((ColorizerFoliage.getFoliageColor(Math.min(getTemperature(), 1.0f), getRainfall()) & 0xFEFEFE) + grassColor) / 2;
        } else
        {
            return grassColor;
        }

    }

    @Override
    public int getModdedBiomeFoliageColor(int original)
    {
        if (!this.foliageColorSet)
            return original;
        if (foliageColorIsMultiplier)
        {
            return ((ColorizerFoliage.getFoliageColor(Math.min(getTemperature(), 1.0f), getRainfall()) & 0xFEFEFE) + foliageColor) / 2;
        } else
        {
            return foliageColor;
        }
    }

    @Override
    public String toString()
    {
        return "BiomeGenCustom of " + getBiomeName();
    }

}
