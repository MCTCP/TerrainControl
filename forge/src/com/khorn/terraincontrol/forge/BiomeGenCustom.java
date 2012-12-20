package com.khorn.terraincontrol.forge;

import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.biome.BiomeGenBase;

import com.khorn.terraincontrol.configuration.BiomeConfig;

public class BiomeGenCustom extends BiomeGenBase
{
    private int skyColor;
    private int grassColor;
    private boolean grassColorIsMultiplier;
    private int foliageColor;
    private boolean foliageColorIsMultiplier;

    private boolean grassColorSet = false;
    private boolean foliageColorSet = false;

    public BiomeGenCustom(int id, String name)
    {
        super(id);
        this.setBiomeName(name);

    }

    /**
     * Needs a BiomeConfig that has all the visual settings present.
     * 
     * @param config
     */
    public void setVisuals(BiomeConfig config)
    {
        this.temperature = config.BiomeTemperature;
        this.rainfall = config.BiomeWetness;
        this.waterColorMultiplier = config.WaterColor;
        this.skyColor = config.SkyColor;
        this.grassColor = config.GrassColor;
        this.grassColorIsMultiplier = config.GrassColorIsMultiplier;
        this.foliageColor = config.FoliageColor;
        this.foliageColorIsMultiplier = config.FoliageColorIsMultiplier;

        if (this.grassColor != 0xffffff)
            this.grassColorSet = true;

        if (this.foliageColor != 0xffffff)
            this.foliageColorSet = true;

        // color ?
        // this.x = 522674;

        // duno.
        // this.A = 9154376;

    }

    public void CopyBiome(BiomeGenBase baseBiome)
    {
        this.fillerBlock = baseBiome.fillerBlock;
        this.topBlock = baseBiome.topBlock;
        this.biomeName = baseBiome.biomeName;
        this.color = baseBiome.color;
        this.minHeight = baseBiome.minHeight;
        this.maxHeight = baseBiome.maxHeight;
        this.temperature = baseBiome.temperature;

        this.theBiomeDecorator = baseBiome.theBiomeDecorator;
        this.waterColorMultiplier = baseBiome.waterColorMultiplier;
        // this.spawnableMonsterList = baseBiome.spawnableMonsterList;

    }

    // Sky color from Temp
    @Override
    public int getSkyColorByTemp(float v)
    {
        return this.skyColor;
    }

    // getGrassColorAtCoords
    @Override
    public int getBiomeGrassColor()
    {
        if (!this.grassColorSet)
            return super.getBiomeGrassColor();
        if (grassColorIsMultiplier)
        {
            double temperature = getFloatTemperature();
            double rainfall = getFloatRainfall();

            return ((ColorizerFoliage.getFoliageColor(temperature, rainfall) & 0xFEFEFE) + this.grassColor) / 2;
        } else
        {
            return this.grassColor;
        }

    }

    // getFoliageColorAtCoords
    @Override
    public int getBiomeFoliageColor()
    {
        if (!this.foliageColorSet)
            return super.getBiomeFoliageColor();
        if (foliageColorIsMultiplier)
        {
            double temperature = getFloatTemperature();
            double rainfall = getFloatRainfall();

            return ((ColorizerFoliage.getFoliageColor(temperature, rainfall) & 0xFEFEFE) + this.foliageColor) / 2;
        } else
        {
            return this.foliageColor;
        }
    }
}
