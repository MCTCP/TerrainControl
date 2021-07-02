package com.pg85.otg.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.pg85.otg.interfaces.ILogger;

public abstract class Logger implements ILogger
{
	protected LogLevel minimumLevel = LogLevel.INFO;
	private boolean logCustomObjects;
	private boolean logStructurePlotting;
	private boolean logConfigs;
	private boolean logBiomeRegistry;
	private boolean logPerformance;
	private boolean logDecoration;
	private boolean logMobs;

	public void init(LogLevel level, boolean logCustomObjects, boolean logStructurePlotting, boolean logConfigs, boolean logBiomeRegistry, boolean logPerformance, boolean logDecoration, boolean logMobs)
	{
		this.minimumLevel = level;
		this.logCustomObjects = logCustomObjects; 
		this.logStructurePlotting = logStructurePlotting;
		this.logConfigs = logConfigs;
		this.logBiomeRegistry = logBiomeRegistry;
		this.logPerformance = logPerformance;
		this.logDecoration = logDecoration;
		this.logMobs = logMobs;
	}

	@Override
	public boolean getLogCategoryEnabled(LogCategory category)
	{
		switch(category)
		{
			case MAIN:
				return true;
			case CUSTOM_OBJECTS:
				return this.logCustomObjects;
			case STRUCTURE_PLOTTING:
				return this.logStructurePlotting;
			case CONFIGS:
				return this.logConfigs;
			case BIOME_REGISTRY:
				return this.logBiomeRegistry;
			case PERFORMANCE:
				return this.logPerformance;		
			case DECORATION:
				return this.logDecoration;
			case MOBS:
				return this.logMobs;				
			default:
				return false;
		}
	}
	
	@Override
	public void printStackTrace(LogLevel level, LogCategory categoy, Exception e)
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		log(level, categoy, stringWriter.toString());
	}
}
