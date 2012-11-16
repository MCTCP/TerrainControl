package com.khorn.terraincontrol.forge;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.ColorizerFoliage;

import com.khorn.terraincontrol.configuration.BiomeConfig;


public class CustomBiome extends BiomeGenBase /* vk */
{
    private int skyColor;
    private int grassColor;
    private boolean grassColorIsMultiplier;
    private int foliageColor;
    private boolean foliageColorIsMultiplier;

    private boolean grassColorSet = false;
    private boolean foliageColorSet = false;

    public CustomBiome(int id, String name)
    {
        super(id);
        this.setBiomeName(name);

    }

    public void SetBiome(BiomeConfig config)
    {

        this.minHeight = config.BiomeHeight; // MCP name is wrong
        this.maxHeight = config.BiomeVolatility; // MCP name is wrong
        this.topBlock = config.SurfaceBlock;
        this.fillerBlock = config.GroundBlock;
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
        //this.x = 522674;

        // duno.
        //this.A = 9154376;


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
        //this.spawnableMonsterList = baseBiome.spawnableMonsterList;

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
        if(grassColorIsMultiplier) {
        	double temperature = getFloatTemperature();
            double rainfall = getFloatRainfall();

            return ((ColorizerFoliage.getFoliageColor(temperature, rainfall)& 0xFEFEFE) + this.grassColor) / 2;
        } else {
        	return this.grassColor;
        }
        
    }

    // getFoliageColorAtCoords
    @Override
    public int getBiomeFoliageColor()
    {
        if (!this.foliageColorSet)
            return super.getBiomeFoliageColor();
        if(foliageColorIsMultiplier) {
        	double temperature = getFloatTemperature();
            double rainfall = getFloatRainfall();

            return ((ColorizerFoliage.getFoliageColor(temperature, rainfall)& 0xFEFEFE)  + this.foliageColor) / 2;
        } else {
        	return this.foliageColor;
        }
    }
}
