package com.pg85.otg.config;

import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.List;

public final class ErroredFunction<T> extends ConfigFunction<T>
{
	private final String name;
	private final List<String> args;
	public final String error;
	public boolean isLogged = false;

	public ErroredFunction(String name, List<String> args, String error)
	{
		this.name = name;
		this.args = args;
		this.error = error;
	}

	@Override
	public String toString()
	{
		return "## INVALID " + name.toUpperCase() + " - " + error + " ##" + System.getProperty(
				"line.separator") + name + "(" + StringHelper.join(args,
						",") + ")";
	}

	public void log(ILogger logger, String biomeName)
	{
		if(!this.isLogged)
		{
			this.isLogged = true;
			logger.log(LogMarker.WARN, "Errored setting ignored for biome " + biomeName + " : " + toString());
		}
	}
}
