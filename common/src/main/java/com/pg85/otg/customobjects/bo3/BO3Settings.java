package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.configuration.settingType.Setting;
import com.pg85.otg.configuration.settingType.Settings;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

import java.util.List;

public class BO3Settings extends Settings
{
    public static final Setting<Boolean>
            TREE = booleanSetting("Tree", true),
            ROTATE_RANDOMLY = booleanSetting("RotateRandomly", false),
            // OTG+
            REMOVEAIR = booleanSetting("RemoveAir", true),
    		CANOVERRIDE = booleanSetting("CanOverride", false),
			MUSTBEBELOWOTHER = booleanSetting("MustBeBelowOther", false),
			CANSPAWNONWATER = booleanSetting("CanSpawnOnWater", true),
			SPAWNONWATERONLY = booleanSetting("SpawnOnWaterOnly", false),
			SPAWNUNDERWATER = booleanSetting("SpawnUnderWater", false),
			SPAWNATWATERLEVEL = booleanSetting("SpawnAtWaterLevel", false),
			REPLACEWITHBIOMEBLOCKS = booleanSetting("ReplaceWithBiomeBlocks", true),
			OVERRIDECHILDSETTINGS = booleanSetting("OverrideChildSettings", true),
			OVERRIDEPARENTHEIGHT = booleanSetting("OverrideParentHeight", false),
			ISSPAWNPOINT = booleanSetting("IsSpawnPoint",false),
			SMOOTHSTARTTOP = booleanSetting("SmoothStartTop",false),
			SMOOTHSTARTWOOD = booleanSetting("SmoothStartWood",false),
			IS_OTG_PLUS = booleanSetting("IsOTGPlus",false)
            ;

    public static final Setting<Double> RARITY = doubleSetting("Rarity", 100, 0.000001, 100);

    public static final Setting<Rotation> INHERITBO3ROTATION = rotationSetting("InheritBO3Rotation", Rotation.NORTH);

    public static final Setting<Integer>
            FREQUENCY = intSetting("Frequency", 0, 0, 9999),
            MIN_HEIGHT = intSetting("MinHeight", 0, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT),
            MAX_HEIGHT = intSetting("MaxHeight", 256, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT),
            MAX_BRANCH_DEPTH = intSetting("MaxBranchDepth", 10, 1, 100),
            MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK = intSetting("MaxPercentageOutsideSourceBlock", 100, 0, 100),
            SPAWN_HEIGHT_OFFSET = intSetting("SpawnHeightOffset", 0, -255, 255),
            SPAWN_HEIGHT_VARIANCE = intSetting("SpawnHeightVariance", 0, -255, 255),
            // OTG+
    		//FREQUENCY = intSetting("Frequency", 0, 0, 9999),
    		BRANCH_FREQUENCY = intSetting("BranchFrequency", 0, 0, 9999),
			HEIGHT_OFFSET = intSetting("HeightOffset", 0, -255, 255),
			SMOOTH_HEIGHT_OFFSET = intSetting("SmoothHeightOffset", 0, -255, 255),
    		SMOOTHRADIUS = intSetting("SmoothRadius", 0, -1, 9999),
			SPAWNPOINTX =  intSetting("SpawnPointX", 8, -9999, 9999),
			SPAWNPOINTY =  intSetting("SpawnPointY", -1, -1, 9999),
			SPAWNPOINTZ =  intSetting("SpawnPointZ", 7, -9999, 9999)
            ;

    public static final Setting<String>
            AUTHOR = stringSetting("Author", "Unknown"),
            DESCRIPTION = stringSetting("Description", "No description given"),
            VERSION = stringSetting("Version", "3"),
            // OTG+
    		INHERITBO3 = stringSetting("InheritBO3", ""),
    		REPLACEABOVE = stringSetting("ReplaceAbove", ""),
			REPLACEBELOW = stringSetting("ReplaceBelow", ""),
			BO3GROUP = stringSetting("BO3Group", ""),
			BRANCH_FREQUENCY_GROUP = stringSetting("BranchFrequencyGroup", ""),
			REPLACEWITHGROUNDBLOCK = stringSetting("ReplaceWithGroundBlock", "DIRT"),
			REPLACEWITHSURFACEBLOCK = stringSetting("ReplaceWithSurfaceBlock", "GRASS"),
			SMOOTHINGSURFACEBLOCK = stringSetting("SmoothingSurfaceBlock", ""),
			SMOOTHINGGROUNDBLOCK = stringSetting("SmoothingGroundBlock", ""),
			MUSTBEINSIDE = stringSetting("MustBeInside", ""),
			CANNOTBEINSIDE = stringSetting("CannotBeInside", ""),
			REPLACESBO3 = stringSetting("ReplacesBO3", "")
            ;

    public static final Setting<List<String>> EXCLUDED_BIOMES = stringListSetting("ExcludedBiomes", "All");

    public static final Setting<MaterialSet>
            SOURCE_BLOCKS = materialSetSetting("SourceBlocks", DefaultMaterial.AIR),
            EXTRUDE_THROUGH_BLOCKS = materialSetSetting("ExtrudeThroughBlocks", DefaultMaterial.AIR);

    // Enum settings
    public static final Setting<OutsideSourceBlock> OUTSIDE_SOURCE_BLOCK = enumSetting("OutsideSourceBlock", OutsideSourceBlock.placeAnyway);
    public static final Setting<SpawnHeightEnum> SPAWN_HEIGHT = enumSetting("SpawnHeight", SpawnHeightEnum.highestBlock);
    public static final Setting<ExtrudeMode> EXTRUDE_MODE = enumSetting("ExtrudeMode", ExtrudeMode.None);

    // The spawn height
    public static enum SpawnHeightEnum
    {
        randomY(StructurePartSpawnHeight.PROVIDED),
        highestBlock(StructurePartSpawnHeight.HIGHEST_BLOCK),
        highestSolidBlock(StructurePartSpawnHeight.HIGHEST_SOLID_BLOCK);

        private StructurePartSpawnHeight height;

        private SpawnHeightEnum(StructurePartSpawnHeight height)
        {
            this.height = height;
        }

        public StructurePartSpawnHeight toStructurePartSpawnHeight()
        {
            return height;
        }
    }

    // How an object should be extended to a surface
    public static enum ExtrudeMode
    {
        None(-1, -1),
        BottomDown(PluginStandardValues.WORLD_HEIGHT, PluginStandardValues.WORLD_DEPTH),
        TopUp(PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT);

        /**
         * Defines where calculation should begin
         */
        private int startingHeight = 0;

        /**
         * Defines where calculation should end
         */
        private int endingHeight = 0;

        ExtrudeMode(int heightStart, int heightEnd)
        {
            this.startingHeight = heightStart;
            this.endingHeight = heightEnd;
        }

        public int getStartingHeight()
        {
            return startingHeight;
        }

        public int getEndingHeight()
        {
            return endingHeight;
        }
    }

    // What to do when outside the source block
    public static enum OutsideSourceBlock
    {
        dontPlace,
        placeAnyway
    }

}
