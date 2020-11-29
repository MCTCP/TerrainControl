package com.pg85.otg.customobject;

import com.pg85.otg.customobject.bo2.BO2Loader;
import com.pg85.otg.customobject.bo3.BO3Loader;
import com.pg85.otg.customobject.bo4.BO4Loader;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.interfaces.ICustomObjectManager;

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

    public CustomObjectManager(boolean spawnLog, boolean developerMode, ILogger logger, Path otgRootFolder, Path otgPresetsFolder, CustomObjectResourcesManager manager)
    {
        // These are the actual lists, not just a copy.
        this.loaders = new HashMap<String, CustomObjectLoader>();

        // Register loaders
        registerCustomObjectLoader("bo2", new BO2Loader());
        registerCustomObjectLoader("bo3", new BO3Loader(manager));
        registerCustomObjectLoader("bo4", new BO4Loader(manager));
        registerCustomObjectLoader("bo4data", new BO4Loader(manager));

        this.globalCustomObjects = new CustomObjectCollection();
               
        if(!developerMode)
        {
	        new Thread() { 
	    		public void run()
	    		{
	    			globalCustomObjects.indexGlobalObjectsFolder(spawnLog, logger, otgRootFolder);
	    			
	    			for(File file : otgPresetsFolder.toFile().listFiles())
	    		    {
	    		    	if(file.isDirectory())
	    		    	{
	    		    		String presetName = file.getName();
	    		    		globalCustomObjects.indexPresetObjectsFolder(presetName, spawnLog, logger, otgRootFolder);
	    		    	}
	    		    }
	    		    logger.log(LogMarker.INFO, "All CustomObject files indexed.");
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
     *                  all files with this extension.
     * @param loader    The loader.
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
    	globalCustomObjects.addLoadedGlobalObject(object);
    }
        
    /**
     * Gets all global objects.
     * @return The global objects.
     */
    public CustomObjectCollection getGlobalObjects()
    {
        return globalCustomObjects;
    }

    /**
     * Gets an unmodifiable view of all object loaders, indexed by the
     * lowercase extension without the dot (for example "bo3").
     * @return The loaders.
     */
    public Map<String, CustomObjectLoader> getObjectLoaders()
    {
        return Collections.unmodifiableMap(loaders);
    }    
    
    /**
     * Calls the {@link CustomObjectLoader#onShutdown()} method of each
     * loader, then unloads them.
     */
    public void shutdown()
    {
        for (CustomObjectLoader loader : loaders.values())
        {
            loader.onShutdown();
        }
        loaders.clear();
    }  
}
