package com.pg85.otg.util.logging;

public enum LogCategory
{
	MAIN,
	CUSTOM_OBJECTS,
	STRUCTURE_PLOTTING,
	CONFIGS,
	BIOME_REGISTRY,
	DECORATION,
	PERFORMANCE,
	MOBS;
	
	public String getLogTag()
	{
		String categoryTag = "";
		switch(this)
		{
			case BIOME_REGISTRY:
				categoryTag = "[BiomeRegistry]";
				break;
			case CONFIGS:
				categoryTag = "[Configs]";
				break;
			case CUSTOM_OBJECTS:
				categoryTag = "[CustomObjects]";
				break;
			case DECORATION:
				categoryTag = "[Decoration]";
				break;
			case MAIN:
				categoryTag = "[Main]";
				break;
			case MOBS:
				categoryTag = "[Mobs]";
				break;
			case PERFORMANCE:
				categoryTag = "[Performance]";
				break;				
			case STRUCTURE_PLOTTING:
				categoryTag = "[StructurePlotting]";
				break;
			default:
				break;		
		}
		return categoryTag;
	}
}