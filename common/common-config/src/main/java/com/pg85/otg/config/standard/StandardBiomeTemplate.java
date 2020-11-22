package com.pg85.otg.config.standard;

import com.pg85.otg.config.minecraft.DefaultBiome;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.minecraft.EntityNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A biome generator holds all <i>default</i> settings of a biome.
 * 
 */
public class StandardBiomeTemplate
{
    public final int worldHeight;

    public String defaultExtends = "";
    public boolean defaultWaterLakes = true;
    public Object[] defaultTree; // Parameters for tree resource
    public int defaultDandelions = 2;
    public int defaultPoppies = 0;
    public int defaultBlueOrchids = 0;
    public int defaultTallFlowers = 0;
    public int defaultSunflowers = 0;
    public int defaultTulips = 0;
    public int defaultAzureBluets = 0;
    public int defaultOxeyeDaisies = 0;
    public int defaultAlliums = 0;
    public int defaultGrass = 10;
    public boolean defaultGrassIsGrouped = false;
    public int defaultDoubleGrass = 0;
    public boolean defaultDoubleGrassIsGrouped = false;
    public int defaultFerns = 0;
    public int defaultLargeFerns = 0;
    public int defaultDeadBush = 0;
    public int defaultMushroom = 0;
    public int defaultReed = 0;
    public int defaultCactus = 0;
    public int defaultMelons = 0;
    public int defaultWaterSand = 3;
    public int defaultWaterGravel = 1;
    public int defaultSwampPatches = 0;
    public Object[] defaultWell; // Parameters for well resource
    public float defaultBiomeSurface = 0.1F;
    public float defaultBiomeVolatility = 0.3F;
    public LocalMaterialData defaultSurfaceBlock = LocalMaterials.GRASS;
    public LocalMaterialData defaultGroundBlock = LocalMaterials.DIRT;
    public float defaultBiomeTemperature = 0.5F;
    public float defaultBiomeWetness = 0.5F;
    public ArrayList<String> defaultIsle = new ArrayList<String>();
    public ArrayList<String> defaultBorder = new ArrayList<String>();
    public ArrayList<String> defaultNotBorderNear = new ArrayList<String>();
    public String defaultRiverBiome = DefaultBiome.RIVER.Name;
    public int defaultSize = 4;
    public int defaultSizeWhenIsle = 6;
    public int defaultSizeWhenBorder = 8;
    public int defaultRarity = 100;
    public int defaultRarityWhenIsle = 97;
    public int defaultColor = 0x000000;
    public int defaultWaterLily = 0;
    public int defaultWaterColorMultiplier = 0x3F76E4;
    public int defaultGrassColor = 0xFFFFFF;
    public int defaultFoliageColor = 0xFFFFFF;
    public boolean defaultStrongholds = true;
    public boolean defaultOceanMonuments = false;
    public boolean defaultWoodlandMansions = false;
    public boolean defaultNetherFortressEnabled = false;
    public VillageType defaultVillageType = VillageType.disabled;
    public RareBuildingType defaultRareBuildingType = RareBuildingType.disabled;
    public MineshaftType defaultMineshaftType = MineshaftType.normal;
    public int defaultEmeraldOre = 0;
    public boolean defaultHasVines;
    public int defaultBoulder = 0;
    public Object[] defaultSurfaceSurfaceAndGroundControl = new Object[0];
    public boolean defaultIceSpikes;
    public boolean defaultDisableBiomeHeight;
    public double[] defaultCustomHeightControl = new double[Constants.PIECES_PER_CHUNK_Y + 1];;
    public double defaultFossilRarity = 0;
    public String defaultBiomeDictId = "";
    public String defaultInheritMobsBiomeName = "";
    public String defaultReplaceToBiomeName = "";
    public boolean inheritSaplingResource = false;
    
    public List<WeightedMobSpawnGroup> defaultCreatures = Arrays.asList(
            new WeightedMobSpawnGroup(EntityNames.SHEEP, 12, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.PIG, 10, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.CHICKEN, 10, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.COW, 8, 4, 4));
    public List<WeightedMobSpawnGroup> defaultMonsters = Arrays.asList(
            new WeightedMobSpawnGroup(EntityNames.SPIDER, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.ZOMBIE, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.SKELETON, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.CREEPER, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.SLIME, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.ENDERMAN, 10, 1, 4),
            new WeightedMobSpawnGroup(EntityNames.WITCH, 5, 1, 1));
    public List<WeightedMobSpawnGroup> defaultAmbientCreatures = Collections.singletonList(
            new WeightedMobSpawnGroup(EntityNames.BAT, 10, 8, 8));
    public List<WeightedMobSpawnGroup> defaultWaterCreatures = Collections.singletonList(
            new WeightedMobSpawnGroup(EntityNames.SQUID, 10, 4, 4));

    public StandardBiomeTemplate(int worldHeight)
    {
        this.worldHeight = worldHeight;
    }
}
