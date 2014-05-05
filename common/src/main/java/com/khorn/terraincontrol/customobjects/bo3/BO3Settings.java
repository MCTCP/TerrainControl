package com.khorn.terraincontrol.customobjects.bo3;

import static com.khorn.terraincontrol.TerrainControl.WORLD_DEPTH;
import static com.khorn.terraincontrol.TerrainControl.WORLD_HEIGHT;

import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.configuration.settingType.Settings;
import com.khorn.terraincontrol.customobjects.StructurePartSpawnHeight;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.List;

public class BO3Settings extends Settings
{
    public static final Setting<Boolean>
            TREE = booleanSetting("Tree", true),
            ROTATE_RANDOMLY = booleanSetting("RotateRandomly", false);

    public static final Setting<Double> RARITY = doubleSetting("Rarity", 100, 0.000001, 100);

    public static final Setting<Integer>
            FREQUENCY = intSetting("Frequency", 1, 1, 200),
            MIN_HEIGHT = intSetting("MinHeight", 0, WORLD_DEPTH, WORLD_HEIGHT),
            MAX_HEIGHT = intSetting("MaxHeight", 256, WORLD_DEPTH, WORLD_HEIGHT),
            MAX_BRANCH_DEPTH = intSetting("MaxBranchDepth", 10, 1, 100),
            MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK = intSetting("MaxPercentageOutsideSourceBlock", 100, 0, 100);

    public static final Setting<String>
            AUTHOR = stringSetting("Author", "Unknown"),
            DESCRIPTION = stringSetting("Description", "No description given"),
            VERSION = stringSetting("Version", "3");

    public static final Setting<List<String>> EXCLUDED_BIOMES = stringListSetting("ExcludedBiomes", "All");

    public static final Setting<MaterialSet> SOURCE_BLOCKS = materialSetSetting("SourceBlocks", DefaultMaterial.AIR);

    // Enum settings
    public static final Setting<OutsideSourceBlock> OUTSIDE_SOURCE_BLOCK = enumSetting("OutsideSourceBlock", OutsideSourceBlock.placeAnyway);
    public static final Setting<SpawnHeightEnum> SPAWN_HEIGHT = enumSetting("SpawnHeight", SpawnHeightEnum.highestBlock);

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

    // What to do when outside the source block
    public static enum OutsideSourceBlock
    {
        dontPlace,
        placeAnyway
    }

}
