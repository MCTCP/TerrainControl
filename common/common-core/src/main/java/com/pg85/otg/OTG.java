package com.pg85.otg;

import com.pg85.otg.logging.LogMarker;

/**
 * Main entry-point. Used for logging and to access OTGEngine.
 * OTGEngine is implemented and provided by the platform-specific 
 * layer and holds any objects and methods used during a session. 
 */
public class OTG
{
	private static OTGEngine Engine;

	private OTG() { }

	// Engine

	public static OTGEngine getEngine()
	{
		return Engine;
	}

	public static void startEngine(OTGEngine engine)
	{
		if (OTG.Engine != null)
		{
			throw new IllegalStateException("Engine is already set.");
		}

		OTG.Engine = engine;
		engine.onStart();
	}

	public static void stopEngine()
	{
		Engine.onShutdown();
		Engine = null;
	}

	// Logging

	public static void log(LogMarker level, String message, Object... params)
	{
		Engine.getLogger().log(level, message, params);
	}
}
