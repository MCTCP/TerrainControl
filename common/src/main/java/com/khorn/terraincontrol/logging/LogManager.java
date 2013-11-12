package com.khorn.terraincontrol.logging;

import com.khorn.terraincontrol.util.helpers.StringHelper;
import java.util.ArrayList;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.khorn.terraincontrol.logging.LogManagerFactory.prefix;

public class LogManager implements Loggable
{

    /**
     * The Logger that will be used to register log records to.
     */
    private final Logger logger;
    /**
     * This is here to provide a means for logging under a situation where
     * logger
     * has yet to be set.
     */
    private static ArrayList<LogRecord> backLog = new ArrayList<LogRecord>(2);
    /**
     * Formats all log records being published
     */
    public static Formatter formatter;

    static
    {
        formatter = new Formatter()
        {
            @Override
            public String format(LogRecord record)
            {
                return new StringBuilder()
                    .append('[').append(prefix).append(']').append(' ')
                    .append(formatMessage(record)).toString();
            }
        };
    }

    public LogManager(Logger logger)
    {
        this.logger = logger;
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
        this.log(level, "{0}", new Object[]
        {
            StringHelper.join(messages, " ")
        });
    }

    @Override
    public void log(Level level, String message, Object param)
    {
        if (!backLog.isEmpty())
            doBackLog();
        LogRecord lr = new LogRecord(level, message);
        lr.setMessage(formatter.format(lr));
        lr.setParameters(new Object[]
        {
            param
        });
        logger.log(lr);
    }

    @Override
    public void log(Level level, String message, Object[] params)
    {
        if (!backLog.isEmpty())
            doBackLog();
        LogRecord lr = new LogRecord(level, message);
        lr.setParameters(params);
        lr.setMessage(formatter.format(lr));
        logger.log(lr);
    }

    @Override
    public void log(LogRecord lr)
    {
        if (!backLog.isEmpty())
            doBackLog();
        _log(lr);
    }

    /**
     * Adds a record to the backlog to be published at next invocation of
     * logger.log().
     * <p/>
     * @param lr The record to be published
     */
    public static void backLog(LogRecord lr)
    {
        backLog.add(lr);
    }

    /**
     * Publishes all logs in backlog, should be called before logger.log() to
     * avoid out of order logging.
     */
    private void doBackLog()
    {
        //>>	Print all backlog
        for (LogRecord logRecord : backLog)
        {
            this._log(logRecord);
        }
        //>>	Clear the queue
        backLog.clear();
    }

    /**
     * Provides a way for doBackLog to publish logs without having itself called
     * again.
     * <p/>
     * @param lr The record to be published
     */
    private void _log(LogRecord lr)
    {
        lr.setMessage(formatter.format(lr));
        logger.log(lr);
    }

    @Override
    public boolean isLoggable(Level level)
    {
        return logger.isLoggable(level);
    }

}
