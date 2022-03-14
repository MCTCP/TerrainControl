package com.pg85.otg.customobject.bo2;

import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.settingType.Settings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialSet;

class BO2Settings extends Settings
{
	static final Setting<Boolean>
		SPAWN_SUNLIGHT = booleanSetting("spawnSunlight", true),
		SPAWN_DARKNESS = booleanSetting("spawnDarkness", true),
		SPAWN_WATER = booleanSetting("spawnWater", false),
		SPAWN_LAVA = booleanSetting("spawnLava", false),
		SPAWN_ABOVE_GROUND = booleanSetting("spawnAboveGround", false),
		SPAWN_UNDER_GROUND = booleanSetting("spawnUnderGround", false),
		RANDON_ROTATION = booleanSetting("randomRotation", true),
		DIG = booleanSetting("dig", false),
		TREE = booleanSetting("tree", false),
		BRANCH = booleanSetting("branch", false),
		NEEDS_FOUNDATION = booleanSetting("needsFoundation", true),
		DO_REPLACE_BLOCKS = booleanSetting("doReplaceBlocks", true)
	;

	static final Setting<Double> COLLISION_PERCENTAGE = doubleSetting("collisionPercentage", 2, 0, 100);

	static final Setting<Integer>
		RARITY = intSetting("rarity", 100, 1, 1000000),
		SPAWN_ELEVATION_MIN = intSetting("spawnElevationMin", 0, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		SPAWN_ELEVATION_MAX = intSetting("spawnElevationMax", 128, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1)
	;

	static final Setting<MaterialSet>
		SPAWN_ON_BLOCK_TYPE = materialSetSetting("spawnOnBlockType", LocalMaterials.GRASS_NAME),
		COLLISION_BLOCK_TYPE = materialSetSetting("collisionBlockType", "All")
	;
}
