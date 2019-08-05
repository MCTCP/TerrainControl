package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.configuration.settingType.Setting;
import com.pg85.otg.configuration.settingType.Settings;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import java.util.List;

public class BO3Settings extends Settings
{
    static final Setting<Boolean>
            TREE = booleanSetting("Tree", true),
            ROTATE_RANDOMLY = booleanSetting("RotateRandomly", false),
			IS_OTG_PLUS = booleanSetting("IsOTGPlus",false)
            ;

    static final Setting<Double> RARITY = doubleSetting("Rarity", 100, 0.000001, 100);

    static final Setting<Integer>
            FREQUENCY = intSetting("Frequency", 0, 0, 9999),
            MIN_HEIGHT = intSetting("MinHeight", 0, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT),
            MAX_HEIGHT = intSetting("MaxHeight", 256, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT),
            MAX_BRANCH_DEPTH = intSetting("MaxBranchDepth", 10, 1, 100),
            MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK = intSetting("MaxPercentageOutsideSourceBlock", 100, 0, 100),
            SPAWN_HEIGHT_OFFSET = intSetting("SpawnHeightOffset", 0, -255, 255),
            SPAWN_HEIGHT_VARIANCE = intSetting("SpawnHeightVariance", 0, -255, 255)
            ;

    static final Setting<String>
            AUTHOR = stringSetting("Author", "Unknown"),
            DESCRIPTION = stringSetting("Description", "No description given"),
            VERSION = stringSetting("Version", "3")
            ;

    static final Setting<List<String>> EXCLUDED_BIOMES = stringListSetting("ExcludedBiomes", "All");

    static final Setting<MaterialSet>
            SOURCE_BLOCKS = materialSetSetting("SourceBlocks", DefaultMaterial.AIR),
            EXTRUDE_THROUGH_BLOCKS = materialSetSetting("ExtrudeThroughBlocks", DefaultMaterial.AIR);

    // Enum settings
    static final Setting<OutsideSourceBlock> OUTSIDE_SOURCE_BLOCK = enumSetting("OutsideSourceBlock", OutsideSourceBlock.placeAnyway);
    public static final Setting<SpawnHeightEnum> SPAWN_HEIGHT = enumSetting("SpawnHeight", SpawnHeightEnum.highestBlock);
    static final Setting<ExtrudeMode> EXTRUDE_MODE = enumSetting("ExtrudeMode", ExtrudeMode.None);

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

        StructurePartSpawnHeight toStructurePartSpawnHeight()
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
