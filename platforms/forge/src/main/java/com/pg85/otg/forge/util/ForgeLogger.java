package com.pg85.otg.forge.util;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;

public final class ForgeLogger extends Logger
{
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Constants.MOD_ID_SHORT);

    @Override
    public void log(LogMarker level, String message, Object... params)
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
                this.logger.fatal(message, params);
                break;
            case ERROR:
                this.logger.error(message, params);
                break;
            case WARN:
                this.logger.warn(message, params);
                break;
            case INFO:
                this.logger.info(message, params);
                break;
            case DEBUG:
            	// DEBUG only shows up in debug logs
                this.logger.info("DEBUG: " + message, params);
                break;
            case TRACE:
            	// TRACE only shows up in debug logs, also doesn't show up in Eclipse console.
                this.logger.info("TRACE: " + message, params);
                break;
            default:
                // Unknown log level, should never happen
                this.logger.info(message, params); // Still log the message
                throw new RuntimeException("Unknown log marker: " + level);
        }
    }
    
	@Override
	public void log(LogMarker level, Throwable e, int maxDepth)
	{
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        log(level, stringWriter.toString());
	}

	@Override
	public void printStackTrace(LogMarker level, Exception e)
	{
		log(level, e, Integer.MAX_VALUE);
	}
}
