package com.pg85.otg.customobject;

import com.pg85.otg.customobject.bo2.BO2Loader;
import com.pg85.otg.customobject.bo3.BO3Loader;
import com.pg85.otg.customobject.bo4.BO4Loader;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the registry for the custom object types. It also stores
 * the global objects. World objects are stored in the WorldConfig class.
 * <p />
 * 
 * Open Terrain Generator supports multiple types of custom objects. By default, it
 * supports BO2s, BO3s and a number of special "objects" like trees and
 * UseWorld.
 * <p />
 * 
 * All those implement CustomObject. Plugin developers can register their
 * own custom object types. If you have a number of CustomObjects that you
 * want to register, just add your object to the global objects in the
 * onStart event using registerGlobalObject. If you have your own file
 * format, just use registerCustomObjectLoader(extension, loader).
 * <p />
 * 
 * Even trees are custom objects. If you want to add your own tree type, add
 * your tree to the global objects and make sure that it's canSpawnAsObject
 * returns false.
 * <p />
 * 
 * If your object implements StructuredCustomObject instead of CustomObject,
 * it will be able to have other objects attached to it, forming a
 * structure. As long as each individual object fits in a chunk, Terrain
 * Control will make sure that the structure gets spawned correctly, chunk
 * for chunk.
 */
public class CustomObjectManager implements ICustomObjectManager
{
	private final Map<String, CustomObjectLoader> loaders;	
	private final CustomObjectCollection globalCustomObjects;

	public CustomObjectManager(boolean developerMode, ILogger logger, Path otgRootFolder, Path otgPresetsFolder, CustomObjectResourcesManager manager)
	{
		// These are the actual lists, not just a copy.
		this.loaders = new HashMap<String, CustomObjectLoader>();

		// Register loaders
		registerCustomObjectLoader("bo2", new BO2Loader());
		registerCustomObjectLoader("bo3", new BO3Loader(manager));
		registerCustomObjectLoader("bo4", new BO4Loader(manager));
		registerCustomObjectLoader("bo4data", new BO4Loader(manager));

		this.globalCustomObjects = new CustomObjectCollection();

		// TODO: Move this to completeable futures. Run a preliminary search for all folders, and then create a completeable future for them.
		// This would allow it to run concurrently and then block until all of the futures are done. This will allow us to concurrently index and
		// also block until the indexing is done.
		if(!developerMode)
		{
			new Thread() { 
				public void run()
				{
					globalCustomObjects.indexGlobalObjectsFolder(logger, otgRootFolder);
					
					for(File file : otgPresetsFolder.toFile().listFiles())
					{
						if(file.isDirectory())
						{
							String presetFolderName = file.getName();
							globalCustomObjects.indexPresetObjectsFolder(presetFolderName, logger, otgRootFolder);
						}
					}
					if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						logger.log(LogLevel.INFO, LogCategory.CUSTOM_OBJECTS, "All CustomObject files indexed.");
					}
				}
			}.start();
		}
	}
	
	public void reloadCustomObjectFiles()
	{
		this.globalCustomObjects.reloadCustomObjectFiles();
	}

	/**
	 * Registers a custom object loader. Register before the config files are
	 * getting loaded, please!
	 *
	 * @param extension The extension of the file. This loader will be responsible for
	 *				  all files with this extension.
	 * @param loader	The loader.
	 */
	private void registerCustomObjectLoader(String extension, CustomObjectLoader loader)
	{
		loaders.put(extension.toLowerCase(), loader);
	}

	/**
	 * Register a global object.
	 *
	 * @param object The object to register.
	 */
	public void registerGlobalObject(CustomObject object)
	{
		this.globalCustomObjects.addLoadedGlobalObject(object);
	}

	/**
	 *  Register a global object that has a file
	 */
	public void registerGlobalObject(CustomObject object, File file)
	{
		this.globalCustomObjects.addGlobalObjectFile(object.getName(), file);
		registerGlobalObject(object);
	}

	/**
	 * Gets all global objects.
	 * @return The global objects.
	 */
	public CustomObjectCollection getGlobalObjects()
	{
		return this.globalCustomObjects;
	}

	/**
	 * Gets an unmodifiable view of all object loaders, indexed by the
	 * lowercase extension without the dot (for example "bo3").
	 * @return The loaders.
	 */
	public Map<String, CustomObjectLoader> getObjectLoaders()
	{
		return Collections.unmodifiableMap(this.loaders);
	}	
	
	/**
	 * Calls the {@link CustomObjectLoader#onShutdown()} method of each
	 * loader, then unloads them.
	 */
	public void shutdown()
	{
		for (CustomObjectLoader loader : this.loaders.values())
		{
			loader.onShutdown();
		}
		this.loaders.clear();
	}  
}
