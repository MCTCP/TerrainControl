package com.pg85.otg.constants;

import com.pg85.otg.util.logging.LogLevel;

public class SettingsEnums
{
	public enum LogLevels
	{
		Off(LogLevel.ERROR),
		Quiet(LogLevel.WARN),
		Standard(LogLevel.INFO);

		private final LogLevel marker;

		LogLevels(LogLevel marker)
		{
			this.marker = marker;
		}

		public LogLevel getLevel()
		{
			return this.marker;
		}
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
	
	public enum IceSpikeType
	{
		Basement,
		HugeSpike,
		SmallSpike;
	}

	public enum TemplateBiomeType
	{
		Overworld,
		Nether,
		End;
	}
}
