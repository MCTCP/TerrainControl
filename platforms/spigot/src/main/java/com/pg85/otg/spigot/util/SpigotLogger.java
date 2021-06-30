package com.pg85.otg.spigot.util;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.LogCategory;
import com.pg85.otg.logging.LogLevel;
import com.pg85.otg.logging.Logger;
import org.apache.logging.log4j.LogManager;

public class SpigotLogger extends Logger
{
	private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Constants.MOD_ID_SHORT);

	@Override
	public void log (LogLevel level, LogCategory category, String message)
	{
		if (this.minimumLevel.compareTo(level) < 0)
		{
			// Only log messages that we want to see...
			return;
		}

		// Add prefix to log output
		// Spigot does not need prefix
		// message = "[" + Constants.MOD_ID_SHORT + "] " + message;
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
