package com.pg85.otg.customobject.bo3;

import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.settingType.Settings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.util.BO3Enums.ExtrudeMode;
import com.pg85.otg.customobject.util.BO3Enums.OutsideSourceBlock;
import com.pg85.otg.customobject.util.BO3Enums.SpawnHeightEnum;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;

class BO3Settings extends Settings
{
	static final Setting<Boolean>
		TREE = booleanSetting("Tree", true),
		ROTATE_RANDOMLY = booleanSetting("RotateRandomly", false),
		IS_OTG_PLUS = booleanSetting("IsOTGPlus",false),
		DO_REPLACE_BLOCKS = booleanSetting("DoReplaceBlocks", true)
	;

	static final Setting<Double> RARITY = doubleSetting("Rarity", 100, 0.000001, 100);

	static final Setting<Integer>
		FREQUENCY = intSetting("Frequency", 0, 0, 9999),
		MAX_SPAWN = intSetting("MaxSpawn", 0, 0, 9999),
		MIN_HEIGHT = intSetting("MinHeight", 0, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		MAX_HEIGHT = intSetting("MaxHeight", 256, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
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
		SOURCE_BLOCKS = materialSetSetting("SourceBlocks", LocalMaterials.AIR_NAME),
		EXTRUDE_THROUGH_BLOCKS = materialSetSetting("ExtrudeThroughBlocks", LocalMaterials.AIR_NAME)
	;

	// Enum settings
	static final Setting<OutsideSourceBlock> OUTSIDE_SOURCE_BLOCK = enumSetting("OutsideSourceBlock", OutsideSourceBlock.placeAnyway);
	static final Setting<SpawnHeightEnum> SPAWN_HEIGHT = enumSetting("SpawnHeight", SpawnHeightEnum.highestBlock);
	static final Setting<ExtrudeMode> EXTRUDE_MODE = enumSetting("ExtrudeMode", ExtrudeMode.None);
}
