package com.pg85.otg.customobject.bo4;

import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.settingType.Settings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.util.BO3Enums.SpawnHeightEnum;
import com.pg85.otg.util.bo3.Rotation;

class BO4Settings extends Settings
{
	static final Setting<Boolean>
		ISOTGPLUS = booleanSetting("IsOTGPlus", true),
		REMOVEAIR = booleanSetting("RemoveAir", true),
		CANOVERRIDE = booleanSetting("CanOverride", false),
		MUSTBEBELOWOTHER = booleanSetting("MustBeBelowOther", false),
		MUSTBEINSIDEWORLDBORDERS = booleanSetting("MustBeInsideWorldBorders", false),
		CANSPAWNONWATER = booleanSetting("CanSpawnOnWater", true),
		SPAWNONWATERONLY = booleanSetting("SpawnOnWaterOnly", false),
		SPAWNUNDERWATER = booleanSetting("SpawnUnderWater", false),
		SPAWNATWATERLEVEL = booleanSetting("SpawnAtWaterLevel", false),
		REPLACEWITHBIOMEBLOCKS = booleanSetting("ReplaceWithBiomeBlocks", true),
		OVERRIDECHILDSETTINGS = booleanSetting("OverrideChildSettings", true),
		OVERRIDEPARENTHEIGHT = booleanSetting("OverrideParentHeight", false),
		ISSPAWNPOINT = booleanSetting("IsSpawnPoint", false),
		SMOOTHSTARTTOP = booleanSetting("SmoothStartTop", false),
		SMOOTHSTARTWOOD = booleanSetting("SmoothStartWood", false),
		DO_REPLACE_BLOCKS = booleanSetting("DoReplaceBlocks", true),
		USE_CENTER_FOR_HIGHEST_BLOCK = booleanSetting("UseCenterForHighestBlock", true)		
	;

	static final Setting<Rotation> INHERITBO3ROTATION = rotationSetting("InheritBO3Rotation", Rotation.NORTH);

	static final Setting<Integer>
		FREQUENCY = intSetting("Frequency", 0, 0, 9999),
		MIN_HEIGHT = intSetting("MinHeight", 0, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		MAX_HEIGHT = intSetting("MaxHeight", 256, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		BRANCH_FREQUENCY = intSetting("BranchFrequency", 0, 0, 9999),
		HEIGHT_OFFSET = intSetting("HeightOffset", 0, -255, 255),
		SMOOTH_HEIGHT_OFFSET = intSetting("SmoothHeightOffset", 0, -255, 255),
		SMOOTHRADIUS = intSetting("SmoothRadius", 0, -1, 9999)
	;

	static final Setting<String>
		AUTHOR = stringSetting("Author", "Unknown"),
		DESCRIPTION = stringSetting("Description", "No description given"),
		INHERITBO3 = stringSetting("InheritBO3", ""),
		REPLACEABOVE = stringSetting("ReplaceAbove", ""),
		REPLACEBELOW = stringSetting("ReplaceBelow", ""),
		BO3GROUP = stringSetting("BO3Group", ""),
		BRANCH_FREQUENCY_GROUP = stringSetting("BranchFrequencyGroup", ""),
		REPLACEWITHGROUNDBLOCK = stringSetting("ReplaceWithGroundBlock", "DIRT"),
		REPLACEWITHSURFACEBLOCK = stringSetting("ReplaceWithSurfaceBlock", "GRASS"),
		REPLACEWITHSTONEBLOCK = stringSetting("ReplaceWithStoneBlock", "STONE"),
		SMOOTHINGSURFACEBLOCK = stringSetting("SmoothingSurfaceBlock", ""),
		SMOOTHINGGROUNDBLOCK = stringSetting("SmoothingGroundBlock", ""),
		MUSTBEINSIDE = stringSetting("MustBeInside", ""),
		CANNOTBEINSIDE = stringSetting("CannotBeInside", ""),
		REPLACESBO3 = stringSetting("ReplacesBO3", ""),
		FIXED_ROTATION = stringSetting("FixedRotation", "")				
	;

	// Enum settings
	static final Setting<SpawnHeightEnum> SPAWN_HEIGHT = enumSetting("SpawnHeight", SpawnHeightEnum.highestBlock);
}
