package com.pg85.otg.forge.util;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.logging.Logger;

import org.apache.logging.log4j.LogManager;

public final class ForgeLogger extends Logger
{
	private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Constants.MOD_ID_SHORT);

	@Override
	public void log(LogLevel level, LogCategory category, String message)
	{
		if (this.minimumLevel.compareTo(level) < 0)
		{
			// Only log messages that we want to see...
			return;
		}

		// Forge automatically adds the OpenTerrainGenerator prefix, 
		// so we don't need to do that ourselves.
		switch (level)
		{
			case FATAL:
				this.logger.fatal(category.getLogTag() + " " + message);
				break;
			case ERROR:
				this.logger.error(category.getLogTag() + " " + message);
				break;
			case WARN:
				this.logger.warn(category.getLogTag() + " " + message);
				break;
			case INFO:
				this.logger.info(category.getLogTag() + " " + message);
				break;
			default:
				break;
		}
	}
}
