package com.pg85.otg.customobjects;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraftTypes.TreeType;

import java.io.File;
import java.util.*;

/**
 * Represents a collection of custom objects. Those objects can be loaded from
 * a directory, or can be loaded manually and then added to this collection.
 *
 */
public class CustomObjectCollection
{
    private ArrayList<CustomObject> objectsGlobalObjects = new ArrayList<CustomObject>();
    private HashMap<String, CustomObject> objectsByNameGlobalObjects = new HashMap<String, CustomObject>();
    private ArrayList<String> objectsNotFoundGlobalObjects = new ArrayList<String>();

    private HashMap<String, ArrayList<CustomObject>> objectsPerWorld = new HashMap<String, ArrayList<CustomObject>>();
    private HashMap<String, HashMap<String, CustomObject>> objectsByNamePerWorld = new HashMap<String, HashMap<String, CustomObject>>();
    private HashMap<String, ArrayList<String>> objectsNotFoundPerWorld = new HashMap<String, ArrayList<String>>();

    HashMap<String, File> CustomObjectFilesGlobalObjects = null;
    HashMap<String, HashMap<String, File>> CustomObjectFilesPerWorld = new HashMap<String, HashMap<String, File>>();

    public CustomObject loadObject(File file, String worldName)
    {
    	CustomObject object = null;
    	// Try to load single file
    	if(file.isFile())
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
                CustomObjectLoader loader = OTG.getCustomObjectManager().getObjectLoaders().get(objectType.toLowerCase());
                if (loader != null)
                {
                	object = loader.loadFromFile(objectName, file);

                	if(worldName != null)
                	{
                		ArrayList<CustomObject> worldObjects = objectsPerWorld.get(worldName);
                		if(worldObjects == null)
                		{
                			worldObjects = new ArrayList<CustomObject>();
                			objectsPerWorld.put(worldName, worldObjects);
                		}
            			worldObjects.add(object);
                	} else {
                    	objectsGlobalObjects.add(object);
                	}
                    object.onEnable(null);
                }
            }
    	} else {
    		OTG.log(LogMarker.INFO, "Given path does not exist: " + file.getAbsolutePath());
    		throw new RuntimeException();
    	}
    	return object;
    }

    /**
     * Adds an object to the list of loaded objects. If an object with the
     * same name (case insensitive) already exists, nothing happens.
     * @param object The object to add to the list of loaded objects.
     */
    public void addLoadedGlobalObject(CustomObject object)
    {
        String lowerCaseName = object.getName().toLowerCase();
        if (!objectsByNameGlobalObjects.containsKey(lowerCaseName))
        {
        	objectsByNameGlobalObjects.put(lowerCaseName, object);
        	objectsGlobalObjects.add(object);
        }
    }

    public void ReloadCustomObjectFiles()
    {
        objectsGlobalObjects.clear();
        objectsByNameGlobalObjects.clear();
        objectsNotFoundGlobalObjects.clear();

        objectsPerWorld.clear();
        objectsByNamePerWorld.clear();
        objectsNotFoundPerWorld.clear();

        CustomObjectFilesGlobalObjects = null;
        CustomObjectFilesPerWorld.clear();
    }

    /**
     * Gets the object with the given name.
     * @param name Name of the object.
     * @return The object, or null if not found.
     */
    public CustomObject getObjectByName(String name, String worldName)
    {
    	worldName = OTG.getEngine().GetPresetName(worldName);
    	//OTG.log(LogMarker.INFO, "getObjectByName " + worldName != null ? worldName : "");

    	CustomObject object = null;

    	// Check if the object has been cached

    	if(worldName != null)
    	{
    		HashMap<String, CustomObject> worldObjectsByName = objectsByNamePerWorld.get(worldName);
	    	if(worldObjectsByName != null)
	    	{
	    		object = worldObjectsByName.get(name.toLowerCase());
	    	}
    	}

    	boolean bSearchedWorldObjects = false;

    	if(object == null && worldName != null)
    	{
	    	ArrayList<String> worldObjectsNotFoundByName = objectsNotFoundPerWorld.get(worldName);
	    	if(worldObjectsNotFoundByName != null && worldObjectsNotFoundByName.contains(name.toLowerCase()))
	    	{
	    		bSearchedWorldObjects = true;
	    	}
    	}

    	// Only check the GlobalObjects if the WorldObjects directory has already been searched
    	if(object == null && (worldName == null || bSearchedWorldObjects))
    	{
    		object = objectsByNameGlobalObjects.get(name.toLowerCase());
    	}

    	if(object != null)
    	{
    		return object;
    	}

    	if(name.equalsIgnoreCase("UseWorld") || name.equalsIgnoreCase("UseWorldAll"))
    	{
    		//OTG.log(LogMarker.INFO, "UseWorld is not used by OTG, skipping it.");
    		return null;
    	}
    	else if(name.equalsIgnoreCase("UseBiome") || name.equalsIgnoreCase("UseBiomeAll"))
    	{
    		//OTG.log(LogMarker.INFO, "UseBiome is not used by OTG, skipping it.");
    		return null;
    	}

    	// Check if the object has been queried before but could not be found

    	boolean bSearchedGlobalObjects = false;

    	if(objectsNotFoundGlobalObjects != null && objectsNotFoundGlobalObjects.contains(name.toLowerCase()))
    	{
    		bSearchedGlobalObjects = true;
    	}

    	if(bSearchedGlobalObjects && (worldName == null || bSearchedWorldObjects))
    	{
    		return null;
    	}

    	// Index GlobalObjects and WorldObjects directories

    	if(CustomObjectFilesGlobalObjects == null)
    	{
    		CustomObjectFilesGlobalObjects = new HashMap<String, File>();
    		if(new File(OTG.getEngine().getOTGDataFolder() + File.separator + "GlobalObjects").exists())
    		{
    			indexAllCustomObjectFilesInDir(new File(OTG.getEngine().getOTGDataFolder() + File.separator + "GlobalObjects"), CustomObjectFilesGlobalObjects);
    		}

	        // Add vanilla custom objects
	        for (TreeType type : TreeType.values())
	        {
	        	addLoadedGlobalObject(new TreeObject(type));
	        }
    	}

    	if(!CustomObjectFilesPerWorld.containsKey(worldName))
    	{
    		HashMap<String, File> worldCustomObjectFiles = new HashMap<String, File>();
    		CustomObjectFilesPerWorld.put(worldName, worldCustomObjectFiles);
			if(worldName != null && new File(OTG.getEngine().getOTGDataFolder() + File.separator + PluginStandardValues.PresetsDirectoryName + File.separator + worldName + File.separator + "WorldObjects").exists())
			{
				indexAllCustomObjectFilesInDir(new File(OTG.getEngine().getOTGDataFolder() + File.separator + PluginStandardValues.PresetsDirectoryName + File.separator + worldName + File.separator + "WorldObjects"), worldCustomObjectFiles);
			}
    	}

    	// Search WorldObjects

		if(worldName != null && !bSearchedWorldObjects)
		{
			HashMap<String, File> worldCustomObjectFiles = CustomObjectFilesPerWorld.get(worldName);
    		if(worldCustomObjectFiles != null)
    		{
    			File searchForFile = worldCustomObjectFiles.get(name.toLowerCase());
    			if(searchForFile != null)
    			{
    				object = loadObject(searchForFile, worldName);

    				if(object != null)
    				{
    			    	HashMap<String, CustomObject> worldObjectsByName = objectsByNamePerWorld.get(worldName);
    			    	if(worldObjectsByName == null)
    			    	{
    			    		worldObjectsByName = new HashMap<String, CustomObject>();
    			    		objectsByNamePerWorld.put(worldName, worldObjectsByName);
    			    	}
    			    	worldObjectsByName.put(name.toLowerCase(), object);
	        	    	return object;
    				} else {
    	        		OTG.log(LogMarker.ERROR, "Could not load BO2/BO3, it probably contains errors: " + searchForFile);
    	        		return null;
    				}
    			}
    		}

    		// Not found
	    	ArrayList<String> worldObjectsNotFound = objectsNotFoundPerWorld.get(worldName);
	    	if(worldObjectsNotFound == null)
	    	{
	    		worldObjectsNotFound = new ArrayList<String>();
    	    	objectsNotFoundPerWorld.put(worldName, worldObjectsNotFound);
	    	}
	    	worldObjectsNotFound.add(name.toLowerCase());
		}

		// Search GlobalObjects

		if(!bSearchedGlobalObjects)
		{
    		object = objectsByNameGlobalObjects.get(name.toLowerCase());

    		if(object != null)
    		{
    			return object;
    		}

			File searchForFile = CustomObjectFilesGlobalObjects.get(name.toLowerCase());

			if(searchForFile != null)
	    	{
	        	object = loadObject(searchForFile, worldName);

	        	if(object != null)
	        	{
	        		objectsByNameGlobalObjects.put(name.toLowerCase(), object);
	    	    	return object;
	        	} else {
	        		OTG.log(LogMarker.ERROR, "Could not load BO2/BO3, it probably contains errors: " + searchForFile);
	        		return null;
	        	}
	    	}

			// Not Found
			objectsNotFoundGlobalObjects.add(name.toLowerCase());
		}

    	OTG.log(LogMarker.ERROR, "Could not find BO2/BO3 " + name + " in GlobalObjects " + (worldName != null ? "and WorldObjects" : "") + " directory " + (worldName != null ? "for world " + worldName : "") + ".");

        return null;
    }

    private void indexAllCustomObjectFilesInDir(File searchDir, HashMap<String, File> customObjectFiles)
    {
    	if(searchDir.exists())
    	{
    		if(searchDir.isDirectory())
    		{
	    		for(File fileInDir : searchDir.listFiles())
	    		{
	    			if(fileInDir.isDirectory())
	    			{
	    				indexAllCustomObjectFilesInDir(fileInDir, customObjectFiles);
	    			} else {
	    				if(fileInDir.getName().toLowerCase().endsWith(".bo3") || fileInDir.getName().toLowerCase().endsWith(".bo2"))
	    				{
		    				if(!customObjectFiles.containsKey(fileInDir.getName().toLowerCase().replace(".bo3", "").replace(".bo2", "")))
		    				{
		    					customObjectFiles.put(fileInDir.getName().toLowerCase().replace(".bo3", "").replace(".bo2", ""), fileInDir);
		    				} else {
		    					if(OTG.getPluginConfig().SpawnLog)
		    					{
		    						OTG.log(LogMarker.WARN, "Duplicate file found: " + fileInDir.getName() + ".");
		    					}
		    				}
	    				}
	    			}
	    		}
    		} else {
    			if(searchDir.getName().toLowerCase().endsWith(".bo3") || searchDir.getName().toLowerCase().endsWith(".bo2"))
    			{
	    			if(!customObjectFiles.containsKey(searchDir.getName().toLowerCase().replace(".bo3", "").replace(".bo2", "")))
					{
	    				customObjectFiles.put(searchDir.getName().toLowerCase().replace(".bo3", "").replace(".bo2", ""), searchDir);
					} else {
						if(OTG.getPluginConfig().SpawnLog)
						{
							OTG.log(LogMarker.WARN, "Duplicate file found: " + searchDir.getName() + ".");
						}
					}
    			}
    		}
    	}
    }
}