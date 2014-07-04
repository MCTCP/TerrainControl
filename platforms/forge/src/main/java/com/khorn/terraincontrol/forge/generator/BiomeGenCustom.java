package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.forge.util.MobSpawnGroupHelper;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;

public class BiomeGenCustom extends BiomeGenBase
{

    private int skyColor;
    private int grassColor;
    private boolean grassColorIsMultiplier;
    private int foliageColor;
    private boolean foliageColorIsMultiplier;

    private boolean grassColorSet = false;
    private boolean foliageColorSet = false;

    public final int generationId;

    public BiomeGenCustom(String name, BiomeIds id)
    {
        super(id.getSavedId(), // Use the saved id for Minecraft compatibility
                !id.isVirtual() // Only register non-virtual biomes
        );
        this.setBiomeName(name);
        this.generationId = id.getGenerationId();
    }

    /**
     * Needs a BiomeConfig that has all the visual settings present.
     * 
     * @param config
     */
    @SuppressWarnings("unchecked")
    public void setEffects(BiomeConfig config)
    {
        this.temperature = config.biomeTemperature;
        this.rainfall = config.biomeWetness;
        if (this.rainfall == 0)
        {
            this.setDisableRain();
        }
        this.waterColorMultiplier = config.waterColor;
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

        // color ?
        // this.x = 522674;

        // duno.
        // this.A = 9154376;

    }

    // Adds the mobs to the internal list
    protected void addMobs(List<SpawnListEntry> internalList, List<WeightedMobSpawnGroup> configList)
    {
        internalList.clear();
        internalList.addAll(MobSpawnGroupHelper.toMinecraftlist(configList));
    }

    /**
     * Copies all properties from another biome. Later on, some properties may
     * get overwritten or modified by {@link #setEffects(BiomeConfig)}. Other
     * properties are not used by Terrain Control, but may be used by other
     * mods.
     * @param baseBiome The biome to copy the settings from.
     */
    public void copyBiome(BiomeGenBase baseBiome)
    {
        this.fillerBlock = baseBiome.fillerBlock;
        this.topBlock = baseBiome.topBlock;
        this.color = baseBiome.color;
        this.rootHeight = baseBiome.rootHeight;
        this.heightVariation = baseBiome.heightVariation;
        this.temperature = baseBiome.temperature;

        this.theBiomeDecorator = baseBiome.theBiomeDecorator;
        this.waterColorMultiplier = baseBiome.waterColorMultiplier;

        this.spawnableMonsterList = baseBiome.getSpawnableList(EnumCreatureType.monster);
        this.spawnableCreatureList = baseBiome.getSpawnableList(EnumCreatureType.creature);
        this.spawnableWaterCreatureList = baseBiome.getSpawnableList(EnumCreatureType.waterCreature);
        this.spawnableCaveCreatureList = baseBiome.getSpawnableList(EnumCreatureType.ambient);
    }

    // Sky color from Temp
    @Override
    public int getSkyColorByTemp(float v)
    {
        return this.skyColor;
    }

    // getGrassColorAtCoords
    @Override
    public int getModdedBiomeGrassColor(int original)
    {
        if (!this.grassColorSet)
            return original;
        if (grassColorIsMultiplier)
        {
            return ((ColorizerFoliage.getFoliageColor(temperature, rainfall) & 0xFEFEFE) + grassColor) / 2;
        } else
        {
            return grassColor;
        }

    }

    // getFoliageColorAtCoords
    @Override
    public int getModdedBiomeFoliageColor(int original)
    {
        if (!this.foliageColorSet)
            return original;
        if (foliageColorIsMultiplier)
        {
            return ((ColorizerFoliage.getFoliageColor(temperature, rainfall) & 0xFEFEFE) + foliageColor) / 2;
        } else
        {
            return foliageColor;
        }
    }

    @Override
    public String toString()
    {
        return "BiomeGenCustom of " + biomeName;
    }

}
