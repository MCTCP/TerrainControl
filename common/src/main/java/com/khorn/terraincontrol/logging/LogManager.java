
package com.khorn.terraincontrol.logging;

import com.khorn.terraincontrol.util.helpers.StringHelper;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogManager implements Loggable
{

    private final Logger logger;
    private static ArrayList<LogRecord> backLog = new ArrayList<LogRecord>(2);

    public LogManager(Logger logger)
    {
        this.logger = logger;
    }
    
    public static void backLog (LogRecord lr)
    {
        backLog.add(lr);
    }
    
    private void doBackLog(){
        for (LogRecord logRecord : backLog)
        {
            this.log(logRecord);
        }
        backLog.clear();
    }
    
    @Override
    public void logIfLevel(Level ifLevel, String... messages)
    {
        if (logger.getLevel().intValue() == ifLevel.intValue())
        {
            this.log(ifLevel, messages);
        }
    }

    @Override
    public void logIfLevel(Level ifLevel, String messages, Object[] params)
    {
        if (logger.getLevel().intValue() == ifLevel.intValue())
        {
            this.log(ifLevel, messages, params);
        }
    }

    @Override
    public void logIfLevel(Level min, Level max, String... messages)
    {
        if (logger.getLevel().intValue() <= max.intValue() && logger.getLevel().intValue() >= min.intValue())
        {
            this.log((min == Level.ALL ? max : (max == Level.OFF ? min : max)), messages);
        }
    }

    @Override
    public void logIfLevel(Level min, Level max, String messages, Object[] params)
    {
        if (logger.getLevel().intValue() <= max.intValue() && logger.getLevel().intValue() >= min.intValue())
        {
            this.log((min == Level.ALL ? max : (max == Level.OFF ? min : max)), messages, params);
        }
    }

    @Override
    public void log(Level level, String... messages)
    {
        this.log(level, "{0}", new Object[]{ StringHelper.join(messages, " ") });
    }

    @Override
    public void log(Level level, String message, Object param)
    {
        if (!backLog.isEmpty())
        {
            doBackLog();
        }
        LogRecord lr = new LogRecord(level, message);
        lr.setMessage(LogManagerFactory.formatter.format(lr));
        lr.setParameters(new Object[]{ param });
        logger.log(lr);
    }

    @Override
    public void log(Level level, String message, Object[] params)
    {
        if (!backLog.isEmpty())
        {
            doBackLog();
        }
        LogRecord lr = new LogRecord(level, message);
        lr.setParameters(params);
        lr.setMessage(LogManagerFactory.formatter.format(lr));
        logger.log(lr);
    }
    
    @Override
    public void log(LogRecord lr)
    {
        lr.setMessage(LogManagerFactory.formatter.format(lr));
        logger.log(lr);
    }

    @Override
    public boolean isLoggable(Level level)
    {
        return logger.isLoggable(level);
    }
}
