package com.pg85.otg.customobject;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;
import com.pg85.otg.util.minecraft.TreeType;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a collection of custom objects. Those objects can be loaded from a
 * directory, or can be loaded manually and then added to this collection.
 *
 */
public class CustomObjectCollection
{
	private Object indexingFilesLock = new Object();
	
    private ArrayList<CustomObject> objectsGlobalObjects = new ArrayList<CustomObject>();
    private HashMap<String, CustomObject> objectsByNameGlobalObjects = new HashMap<String, CustomObject>();
    private ArrayList<String> objectsNotFoundGlobalObjects = new ArrayList<String>();

    private HashMap<String, ArrayList<CustomObject>> objectsPerWorld = new HashMap<String, ArrayList<CustomObject>>();
    private HashMap<String, HashMap<String, CustomObject>> objectsByNamePerWorld = new HashMap<String, HashMap<String, CustomObject>>();
    private HashMap<String, ArrayList<String>> objectsNotFoundPerWorld = new HashMap<String, ArrayList<String>>();

    private HashMap<String, File> customObjectFilesGlobalObjects = null;
    private HashMap<String, HashMap<String, File>> customObjectFilesPerWorld = new HashMap<String, HashMap<String, File>>();

    private CustomObject loadObject(File file, String worldName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	synchronized(indexingFilesLock)
    	{
	        CustomObject object = null;
	        // Try to load single file
	        if (file.isFile())
	        {
	            // Get name and extension
	            String fileName = file.getName();
	            int index = fileName.lastIndexOf('.');
	            // If we come across a directory descend into it without enabling
	            // the objects
	            if (index != -1)
	            {
	                String objectType = fileName.substring(index + 1, fileName.length());
	                String objectName = fileName.substring(0, index);
	
	                // Get the object
	                CustomObjectLoader loader = customObjectManager.getObjectLoaders().get(
	                        objectType.toLowerCase());
	                if (loader != null)
	                {
	                    object = loader.loadFromFile(objectName, file, logger);
	
	                    if (worldName != null)
	                    {
	                        ArrayList<CustomObject> worldObjects = objectsPerWorld.get(worldName);
	                        if (worldObjects == null)
	                        {
	                            worldObjects = new ArrayList<CustomObject>();
	                            objectsPerWorld.put(worldName, worldObjects);
	                        }
	                        worldObjects.add(object);
	                    } else {
	                        objectsGlobalObjects.add(object);
	                    }
	
	                    if (!object.onEnable(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker) || !object.loadChecks(modLoadedChecker))
	                    {
	                        // Remove the object
	                        removeLoadedObject(worldName, object);
							
							// Try bo4
							loader = customObjectManager.getObjectLoaders().get("bo4");
							if (loader != null)
							{
								object = loader.loadFromFile(objectName, file, logger);
								if (worldName != null)
								{
									ArrayList<CustomObject> worldObjects = objectsPerWorld.get(worldName);
									if (worldObjects == null)
									{
										worldObjects = new ArrayList<CustomObject>();
										objectsPerWorld.put(worldName, worldObjects);
									}
									worldObjects.add(object);
								} else {
									objectsGlobalObjects.add(object);
								}

								if (!object.onEnable(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker) || !object.loadChecks(modLoadedChecker))
								{
									// Remove the object
									removeLoadedObject(worldName, object);
									return null;
								}
							}
	                    }
	                }
	            }
	        } else {
	            logger.log(LogMarker.FATAL, "Given path does not exist: " + file.getAbsolutePath());
	            throw new RuntimeException("Given path does not exist: " + file.getAbsolutePath());
	        }
	        return object;
    	}
    }
          
    private void removeLoadedObject(String worldName, CustomObject object)
    {
        if (worldName != null)
        {
            HashMap<String, CustomObject> worldObjectsByName = objectsByNamePerWorld.get(worldName);
            if(worldObjectsByName != null)
            {
	            worldObjectsByName.remove(object.getName().toLowerCase());
	            if (worldObjectsByName.size() == 0)
	            {
	            	objectsByNamePerWorld.remove(worldName, worldObjectsByName);
	            }
            }
            
            ArrayList<CustomObject> worldObjects = objectsPerWorld.get(worldName);
            worldObjects.remove(object);
            if (worldObjects.size() == 0)
            {
                objectsPerWorld.remove(worldName, worldObjects);
            }
        } else {
            objectsGlobalObjects.remove(object);
        }
    }

    /**
     * Adds an object to the list of loaded objects. If an object with the same name
     * (case insensitive) already exists, nothing happens.
     * 
     * @param object The object to add to the list of loaded objects.
     */
    void addLoadedGlobalObject(CustomObject object)
    {
    	synchronized(indexingFilesLock)
    	{
	        String lowerCaseName = object.getName().toLowerCase();
	        if (!objectsByNameGlobalObjects.containsKey(lowerCaseName))
	        {
	            objectsByNameGlobalObjects.put(lowerCaseName, object);
	            objectsGlobalObjects.add(object);
	        }
    	}
    }

    public void unloadCustomObjectFiles()
    {
    	synchronized(indexingFilesLock)
    	{
	        objectsGlobalObjects.clear();
	        objectsByNameGlobalObjects.clear();
	        objectsNotFoundGlobalObjects.clear();
    		
	        objectsPerWorld.clear();
	        objectsByNamePerWorld.clear();
	        objectsNotFoundPerWorld.clear();	
    	}
    }
    
    void reloadCustomObjectFiles()
    {
    	synchronized(indexingFilesLock)
    	{
	        objectsGlobalObjects.clear();
	        objectsByNameGlobalObjects.clear();
	        objectsNotFoundGlobalObjects.clear();
	
	        objectsPerWorld.clear();
	        objectsByNamePerWorld.clear();
	        objectsNotFoundPerWorld.clear();
	
	        customObjectFilesGlobalObjects = null;
	        customObjectFilesPerWorld.clear();
    	}
    }

    public ArrayList<String> getAllBONamesForWorld(String worldName)
    {
    	HashMap<String, File> files = customObjectFilesPerWorld.get(worldName);
    	return files == null ? null : new ArrayList<String>(files.keySet());
    }
    
    public CustomObject getObjectByName(String name, String worldName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	synchronized(indexingFilesLock)
    	{
    		return getObjectByName(name, worldName, true, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
    	}
    }
    
    void indexGlobalObjectsFolder(boolean spawnLog, ILogger logger, Path otgRootFolder)
    {
    	synchronized(indexingFilesLock)
    	{
	        if (customObjectFilesGlobalObjects == null)
	        {
	        	logger.log(LogMarker.INFO, "Indexing GlobalObjects folder.");
	            customObjectFilesGlobalObjects = new HashMap<String, File>();
	            if (new File(otgRootFolder + File.separator + Constants.GLOBAL_OBJECTS_FOLDER).exists())
	            {
	            	indexAllCustomObjectFilesInDir(new File(otgRootFolder + File.separator + Constants.GLOBAL_OBJECTS_FOLDER), customObjectFilesGlobalObjects, spawnLog, logger);
	            }
	
	            // Add vanilla custom objects
	            for (TreeType type : TreeType.values())
	            {
	                addLoadedGlobalObject(new TreeObject(type));
	            }
	            logger.log(LogMarker.INFO, "GlobalObjects folder indexed.");
	        }
    	}
    }
    
    void indexWorldObjectsFolder(String worldName, boolean spawnLog, ILogger logger, Path otgRootFolder)
    {
    	synchronized(indexingFilesLock)
    	{
	        if (worldName != null && !customObjectFilesPerWorld.containsKey(worldName))
	        {
	        	logger.log(LogMarker.INFO, "Indexing WorldObjects folder for world " + worldName);
	            HashMap<String, File> worldCustomObjectFiles = new HashMap<String, File>();
	            customObjectFilesPerWorld.put(worldName, worldCustomObjectFiles);
	            if (worldName != null && new File(otgRootFolder + File.separator + Constants.PRESETS_FOLDER + File.separator + worldName + File.separator + Constants.WORLD_OBJECTS_FOLDER).exists())
	            {
	                indexAllCustomObjectFilesInDir(
                		new File(otgRootFolder + File.separator + Constants.PRESETS_FOLDER + File.separator + worldName + File.separator + Constants.WORLD_OBJECTS_FOLDER),
                        worldCustomObjectFiles, spawnLog, logger);
	            }
	            logger.log(LogMarker.INFO, "WorldObjects folder for world " + worldName + " indexed.");
	        }
    	}
    }
    
    /**
     * Gets the object with the given name.
     * 
     * @param name Name of the object.
     * @return The object, or null if not found.
     */
    private CustomObject getObjectByName(String name, String worldName, boolean searchGlobalObjects, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	synchronized(indexingFilesLock)
    	{
	        worldName = worldName == null ? null : presetNameProvider.getPresetName(worldName);
	        // OTG.log(LogMarker.INFO, "getObjectByName " + worldName != null ? worldName : "");
	
	        CustomObject object = null;
	
	        // Check if the object has been cached
	
	        if (worldName != null)
	        {
	            if (worldName.equals("overworld"))
	            {
	                worldName = presetNameProvider.getPresetName("overworld");
	            }
	
	            HashMap<String, CustomObject> worldObjectsByName = objectsByNamePerWorld.get(worldName);
	            if (worldObjectsByName != null)
	            {
	                object = worldObjectsByName.get(name.toLowerCase());
	            }
	        }
	
	        boolean bSearchedWorldObjects = false;
	
	        if (object == null && worldName != null)
	        {
	            ArrayList<String> worldObjectsNotFoundByName = objectsNotFoundPerWorld.get(worldName);
	            if (worldObjectsNotFoundByName != null && worldObjectsNotFoundByName.contains(name.toLowerCase()))
	            {
	            	// TODO: If a user adds a new object while the game is running, it won't be picked up, even when developermode:true.
	                bSearchedWorldObjects = true;
	            }
	        }
	
	        // Only check the GlobalObjects if the WorldObjects directory has already been searched
	        if (object == null && searchGlobalObjects && (worldName == null || bSearchedWorldObjects))
	        {
	            object = objectsByNameGlobalObjects.get(name.toLowerCase());
	        }
	
	        if (object != null)
	        {
	            return object;
	        }
	
	        if (name.equalsIgnoreCase("UseWorld") || name.equalsIgnoreCase("UseWorldAll"))
	        {
	            // OTG.log(LogMarker.INFO, "UseWorld is not used by OTG, skipping it.");
	            return null;
	        } else if (name.equalsIgnoreCase("UseBiome") || name.equalsIgnoreCase("UseBiomeAll"))
	        {
	            // OTG.log(LogMarker.INFO, "UseBiome is not used by OTG, skipping it.");
	            return null;
	        }
	
	        // Check if the object has been queried before but could not be found
	
	        boolean bSearchedGlobalObjects = false;
	
	        if (objectsNotFoundGlobalObjects != null && objectsNotFoundGlobalObjects.contains(name.toLowerCase()))
	        {
	        	// TODO: If a user adds a new object while the game is running, it won't be picked up, even when developermode:true.
	            bSearchedGlobalObjects = true;
	        }
	
	        if ((!searchGlobalObjects || bSearchedGlobalObjects) && (worldName == null || bSearchedWorldObjects))
	        {
	            return null;
	        }
	
	        // Index GlobalObjects and WorldObjects directories
	
	        indexGlobalObjectsFolder(spawnLog, logger, otgRootFolder);
	        indexWorldObjectsFolder(worldName, spawnLog, logger, otgRootFolder);
	
	        // Search WorldObjects
	
	        if (worldName != null && !bSearchedWorldObjects)
	        {
	            HashMap<String, File> worldCustomObjectFiles = customObjectFilesPerWorld.get(worldName);
	            if (worldCustomObjectFiles != null)
	            {
	                File searchForFile = worldCustomObjectFiles.get(name.toLowerCase());
	                if (searchForFile != null)
	                {
	                    object = loadObject(searchForFile, worldName, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
	                    if (object != null)
	                    {
	                        HashMap<String, CustomObject> worldObjectsByName = objectsByNamePerWorld.get(worldName);
	                        if (worldObjectsByName == null)
	                        {
	                            worldObjectsByName = new HashMap<String, CustomObject>();
	                            objectsByNamePerWorld.put(worldName, worldObjectsByName);
	                        }
	                        worldObjectsByName.put(name.toLowerCase(), object);
	                        return object;
	                    } else {
	                        if (spawnLog)
	                        {
	                        	logger.log(LogMarker.WARN,
	                                    "Could not load BO2/BO3, it probably contains errors: " + searchForFile);
	                        }
	                        return null;
	                    }
	                }
	            }
	
	            // Not found
	            ArrayList<String> worldObjectsNotFound = objectsNotFoundPerWorld.get(worldName);
	            if (worldObjectsNotFound == null)
	            {
	                worldObjectsNotFound = new ArrayList<String>();
	                objectsNotFoundPerWorld.put(worldName, worldObjectsNotFound);
	            }
	            worldObjectsNotFound.add(name.toLowerCase());
	        }
	
	        // Search GlobalObjects
	
	        if (searchGlobalObjects && !bSearchedGlobalObjects)
	        {
	            object = objectsByNameGlobalObjects.get(name.toLowerCase());
	
	            if (object != null)
	            {
	                return object;
	            }
	
	            File searchForFile = customObjectFilesGlobalObjects.get(name.toLowerCase());
	
	            if (searchForFile != null)
	            {
	                object = loadObject(searchForFile, worldName, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
	
	                if (object != null)
	                {
	                    objectsByNameGlobalObjects.put(name.toLowerCase(), object);
	                    return object;
	                } else
	                {
	                    if (spawnLog)
	                    {
	                        logger.log(LogMarker.WARN,
	                                "Could not load BO2/BO3, it probably contains errors: " + searchForFile);
	                    }
	                    return null;
	                }
	            }
	
	            // Not Found
	            objectsNotFoundGlobalObjects.add(name.toLowerCase());
	        }
	
	        if (spawnLog)
	        {
	            logger.log(LogMarker.WARN,
	                    "Could not find BO2/BO3 " + name + " in GlobalObjects " + (worldName != null ? "and WorldObjects" : "") + " directory " + (worldName != null ? "for world " + worldName : "") + ".");
	        }
	
	        return null;
    	}
    }

    private void indexAllCustomObjectFilesInDir(File searchDir, HashMap<String, File> customObjectFiles, boolean spawnLog, ILogger logger)
    {
        if (searchDir.exists())
        {
            if (searchDir.isDirectory())
            {
                for (File fileInDir : searchDir.listFiles())
                {
                    if (fileInDir.isDirectory())
                    {
                        indexAllCustomObjectFilesInDir(fileInDir, customObjectFiles, spawnLog, logger);
                    } else
                    {
                        if (fileInDir.getName().toLowerCase().endsWith(
                                ".bo4data") || fileInDir.getName().toLowerCase().endsWith(
                                        ".bo4") || fileInDir.getName().toLowerCase().endsWith(
                                                ".bo3") || fileInDir.getName().toLowerCase().endsWith(".bo2"))
                        {
                            if (fileInDir.getName().toLowerCase().endsWith(
                                    ".bo4data") || !customObjectFiles.containsKey(
                                            fileInDir.getName().toLowerCase().replace(".bo4data", "").replace(".bo4",
                                                    "").replace(".bo3", "").replace(".bo2", "")))
                            {
                                customObjectFiles.put(
                                        fileInDir.getName().toLowerCase().replace(".bo4data", "").replace(".bo4",
                                                "").replace(".bo3", "").replace(".bo2", ""),
                                        fileInDir);
                            } else {
                                if (spawnLog)
                                {
                                    logger.log(LogMarker.WARN, "Duplicate file found: " + fileInDir.getName() + ".");
                                }
                            }
                        }
                    }
                }
            } else {
                if (searchDir.getName().toLowerCase().endsWith(
                        ".bo4data") || searchDir.getName().toLowerCase().endsWith(
                                ".bo4") || searchDir.getName().toLowerCase().endsWith(
                                        ".bo3") || searchDir.getName().toLowerCase().endsWith(".bo2"))
                {
                    if (searchDir.getName().toLowerCase().endsWith(".bo4data") || !customObjectFiles.containsKey(
                            searchDir.getName().toLowerCase().replace(".bo4", "").replace(".bo3", "").replace(".bo2",
                                    "")))
                    {
                        customObjectFiles.put(
                                searchDir.getName().toLowerCase().replace(".bo4data", "").replace(".bo4", "").replace(
                                        ".bo3", "").replace(".bo2", ""),
                                searchDir);
                    } else {
                        if (spawnLog)
                        {
                            logger.log(LogMarker.WARN, "Duplicate file found: " + searchDir.getName() + ".");
                        }
                    }
                }
            }
        }
    }
}
