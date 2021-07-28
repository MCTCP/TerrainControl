package com.pg85.otg.customobject;

import java.io.File;

import com.pg85.otg.customobject.util.NBTHelper;
import com.pg85.otg.interfaces.ILogger;

public interface CustomObjectLoader
{
	/**
	 * Returns a CustomObject with the given name and file. The object shouldn't yet be initialisized.
	 *
	 * @param objectName Name of the object.
	 * @param file		File of the object.
	 * @return The object.
	 */
	public CustomObject loadFromFile(String objectName, File file, ILogger logger);

	/**
	 * Called whenever Open Terrain Generator is being shut down / reloaded.
	 */
	public default void onShutdown()
	{
		// Clean up the cache
		NBTHelper.clearCache();
	}
}
