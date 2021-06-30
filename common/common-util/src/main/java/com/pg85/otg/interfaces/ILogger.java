package com.pg85.otg.interfaces;

import com.pg85.otg.logging.LogCategory;
import com.pg85.otg.logging.LogLevel;

public interface ILogger
{
	public void init(LogLevel level, boolean logCustomObjects, boolean logStructurePlotting, boolean logConfigs, boolean logBiomeRegistry, boolean logDecoration, boolean logMobs);

	public boolean getLogCategoryEnabled(LogCategory category);	
	public void log(LogLevel level, LogCategory category, String message);
	public void printStackTrace(LogLevel marker, LogCategory category, Exception e);	
}
