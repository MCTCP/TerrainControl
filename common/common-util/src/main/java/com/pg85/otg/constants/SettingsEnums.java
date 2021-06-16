package com.pg85.otg.constants;

public class SettingsEnums
{
	public enum TerrainMode
	{
		Normal,
		TerrainTest,
		NotGenerate
	}

	public enum BiomeMode
	{
		Normal,
		BeforeGroups, // Legacy: Converted to NoGroups when loaded from config.
		NoGroups,
		FromImage
	}
	
	public enum CustomStructureType
	{
		BO3,
		BO4
	}
	
	public enum ImageMode
	{
		Repeat,
		Mirror,
		ContinueNormal,
		FillEmpty,
	}

	public enum ImageOrientation
	{
		North,
		East,
		South,
		West,
	}

	public enum ConfigMode
	{
		WriteAll,
		WriteDisable,
		WriteWithoutComments
	} 
	
	public enum VillageType
	{
		disabled,
		wood,
		sandstone,
		taiga,
		savanna,
		snowy
	}	
	
	public enum MineshaftType
	{
		disabled,
		normal,
		mesa
	}	
	
	public enum RareBuildingType
	{
		disabled,
		desertPyramid,
		jungleTemple,
		swampHut,
		igloo
	}
	
	public enum OceanRuinsType
	{
		disabled,
		warm,
		cold
	}
	
	public enum RuinedPortalType
	{
		disabled,
		normal,
		desert,
		jungle,
		swamp,
		mountain,
		ocean,
		nether
	}
	
	public enum GrassColorModifier
	{
		None,
		Swamp,
		DarkForest
	}
}
