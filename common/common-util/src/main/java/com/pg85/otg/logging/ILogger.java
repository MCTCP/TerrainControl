package com.pg85.otg.logging;

public interface ILogger
{
	public void log(LogMarker level, String message, Object... params);

	public void printStackTrace(LogMarker fatal, Exception e);

	public void setLevel(LogMarker level);
}
