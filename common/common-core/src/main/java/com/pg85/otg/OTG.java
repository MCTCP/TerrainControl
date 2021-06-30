package com.pg85.otg;

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
		if (Engine != null)
		{
			throw new IllegalStateException("Engine is already set.");
		}

		Engine = engine;
		engine.onStart();
	}

	public static void stopEngine()
	{
		Engine.onShutdown();
		Engine = null;
	}
}
