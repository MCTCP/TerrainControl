package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.MobAlternativeNames;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;

import java.util.List;
import java.util.logging.Level;

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
    @SuppressWarnings("unchecked")
    public void setEffects(BiomeConfig config)
    {
        this.temperature = config.BiomeTemperature;
        this.rainfall = config.BiomeWetness;
        if (this.rainfall == 0)
        {
            this.setDisableRain();
        }
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

        // Mob spawning
        addMobs(this.spawnableMonsterList, config.spawnMonstersAddDefaults, config.spawnMonsters);
        addMobs(this.spawnableCreatureList, config.spawnCreaturesAddDefaults, config.spawnCreatures);
        addMobs(this.spawnableWaterCreatureList, config.spawnWaterCreaturesAddDefaults, config.spawnWaterCreatures);
        addMobs(this.spawnableCaveCreatureList, config.spawnAmbientCreaturesAddDefaults, config.spawnAmbientCreatures);

        // color ?
        // this.x = 522674;

        // duno.
        // this.A = 9154376;

    }

    // Adds the mobs to the internal list. Displays a warning for each mob type
    // it doesn't understand
    protected void addMobs(List<SpawnListEntry> internalList, boolean addDefaults, List<WeightedMobSpawnGroup> configList)
    {
        if (!addDefaults)
        {
            internalList.clear();
        }
        for (WeightedMobSpawnGroup mobGroup : configList)
        {
            Class<? extends Entity> entityClass = getEntityClass(mobGroup);
            if (entityClass != null)
            {
                internalList.add(new SpawnListEntry(entityClass, mobGroup.getWeight(), mobGroup.getMin(), mobGroup.getMax()));
            } else
            {
                TerrainControl.log(Level.WARNING, "Mob type " + mobGroup.getMobName() + " not found in " + this.biomeName);
            }
        }
    }

    // Gets the class of the entity.
    @SuppressWarnings("unchecked")
    protected Class<? extends Entity> getEntityClass(WeightedMobSpawnGroup mobGroup)
    {
        String mobName = MobAlternativeNames.getInternalMinecraftName(mobGroup.getMobName());
        return (Class<? extends Entity>) EntityList.stringToClassMapping.get(mobName);
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

    @Override
    public String toString()
    {
        return "BiomeGenCustom of " + biomeName;
    }
}
