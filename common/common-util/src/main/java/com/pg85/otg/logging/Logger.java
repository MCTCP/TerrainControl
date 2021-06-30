package com.pg85.otg.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.pg85.otg.util.interfaces.ILogger;

public abstract class Logger implements ILogger
{
	protected LogMarker minimumLevel = LogMarker.INFO;
	private boolean logCustomObjects;
	private boolean logStructurePlotting;
	private boolean logConfigs;
	private boolean logBiomeRegistry;
	private boolean logDecoration;
	private boolean logMobs;

	public void init(LogMarker level, boolean logCustomObjects, boolean logStructurePlotting, boolean logConfigs, boolean logBiomeRegistry, boolean logDecoration, boolean logMobs)
	{
		this.minimumLevel = level;
		this.logCustomObjects = logCustomObjects; 
		this.logStructurePlotting = logStructurePlotting;
		this.logConfigs = logConfigs;
		this.logBiomeRegistry = logBiomeRegistry;
		this.logDecoration = logDecoration;
		this.logMobs = logMobs;
	}

	@Override
	public boolean getLogCategoryEnabled(LogCategory category)
	{
		switch(category)
		{
			case PUBLIC:
				return true;			
			case CUSTOM_OBJECTS:
				return this.logCustomObjects;
			case STRUCTURE_PLOTTING:
				return this.logStructurePlotting;
			case CONFIGS:
				return this.logConfigs;
			case BIOME_REGISTRY:
				return this.logBiomeRegistry;
			case DECORATION:
				return this.logDecoration;
			case MOBS:
				return this.logMobs;				
			default:
				return false;
		}
	}
	
	@Override
	public void printStackTrace(LogMarker level, LogCategory categoy, Exception e)
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		log(level, categoy, stringWriter.toString());
	}
}
