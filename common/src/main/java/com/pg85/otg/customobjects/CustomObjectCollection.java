package com.pg85.otg.customobjects;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraft.defaults.TreeType;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * Represents a collection of custom objects. Those objects can be loaded from a
 * directory, or can be loaded manually and then added to this collection.
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

    private HashMap<String, File> customObjectFilesGlobalObjects = null;
    private HashMap<String, HashMap<String, File>> customObjectFilesPerWorld = new HashMap<String, HashMap<String, File>>();

    private CustomObject loadObject(File file, String worldName)
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
                CustomObjectLoader loader = OTG.getCustomObjectManager().getObjectLoaders().get(
                        objectType.toLowerCase());
                if (loader != null)
                {
                    object = loader.loadFromFile(objectName, file);

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

                    if (!object.onEnable() || !object.loadChecks())
                    {
                        // Remove the object
                        removeLoadedObject(worldName, object);
						
						// Try bo4
						loader = OTG.getCustomObjectManager().getObjectLoaders().get("bo4");
						if (loader != null)
						{
							object = loader.loadFromFile(objectName, file);
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

							if (!object.onEnable() || !object.loadChecks())
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
            OTG.log(LogMarker.FATAL, "Given path does not exist: " + file.getAbsolutePath());
            throw new RuntimeException("Given path does not exist: " + file.getAbsolutePath());
        }
        return object;
    }

    private void removeLoadedObject(String worldName, CustomObject object)
    {
        if (worldName != null)
        {
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
        String lowerCaseName = object.getName().toLowerCase();
        if (!objectsByNameGlobalObjects.containsKey(lowerCaseName))
        {
            objectsByNameGlobalObjects.put(lowerCaseName, object);
            objectsGlobalObjects.add(object);
        }
    }

    void reloadCustomObjectFiles()
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

    public ArrayList<BO4> getAllBO4sForWorld(String worldName)
    {
        ArrayList<BO4> allBO4s = new ArrayList<BO4>();

        if (worldName != null)
        {
            if (worldName.equals("overworld"))
            {
                worldName = OTG.getEngine().getPresetName("overworld");
            }

            HashMap<String, File> worldObjectFilesByName = customObjectFilesPerWorld.get(worldName);
            if (worldObjectFilesByName != null)
            {
                for (Entry<String, File> entry : worldObjectFilesByName.entrySet())
                {
                    CustomObject object = getObjectByName(entry.getKey(), worldName);
                    if (object instanceof BO4)
                    {
                        allBO4s.add((BO4) object);
                    }
                }
            }
        }
        return allBO4s;
    }

    /**
     * Gets the object with the given name.
     * 
     * @param name Name of the object.
     * @return The object, or null if not found.
     */
    public CustomObject getObjectByName(String name, String worldName)
    {
        worldName = OTG.getEngine().getPresetName(worldName);
        // OTG.log(LogMarker.INFO, "getObjectByName " + worldName != null ?
        // worldName :
        // "");

        CustomObject object = null;

        // Check if the object has been cached

        if (worldName != null)
        {
            if (worldName.equals("overworld"))
            {
                worldName = OTG.getEngine().getPresetName("overworld");
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
                bSearchedWorldObjects = true;
            }
        }

        // Only check the GlobalObjects if the WorldObjects directory has
        // already been
        // searched
        if (object == null && (worldName == null || bSearchedWorldObjects))
        {
            object = objectsByNameGlobalObjects.get(name.toLowerCase());
        }

        if (object != null)
        {
            return object;
        }

        if (name.equalsIgnoreCase("UseWorld") || name.equalsIgnoreCase("UseWorldAll"))
        {
            // OTG.log(LogMarker.INFO, "UseWorld is not used by OTG, skipping
            // it.");
            return null;
        } else if (name.equalsIgnoreCase("UseBiome") || name.equalsIgnoreCase("UseBiomeAll"))
        {
            // OTG.log(LogMarker.INFO, "UseBiome is not used by OTG, skipping
            // it.");
            return null;
        }

        // Check if the object has been queried before but could not be found

        boolean bSearchedGlobalObjects = false;

        if (objectsNotFoundGlobalObjects != null && objectsNotFoundGlobalObjects.contains(name.toLowerCase()))
        {
            bSearchedGlobalObjects = true;
        }

        if (bSearchedGlobalObjects && (worldName == null || bSearchedWorldObjects))
        {
            return null;
        }

        // Index GlobalObjects and WorldObjects directories

        if (customObjectFilesGlobalObjects == null)
        {
            customObjectFilesGlobalObjects = new HashMap<String, File>();
            if (new File(OTG.getEngine().getOTGRootFolder() + File.separator + "GlobalObjects").exists())
            {
                indexAllCustomObjectFilesInDir(
                        new File(OTG.getEngine().getOTGRootFolder() + File.separator + "GlobalObjects"),
                        customObjectFilesGlobalObjects);
            }

            // Add vanilla custom objects
            for (TreeType type : TreeType.values())
            {
                addLoadedGlobalObject(new TreeObject(type));
            }
        }

        if (!customObjectFilesPerWorld.containsKey(worldName))
        {
            HashMap<String, File> worldCustomObjectFiles = new HashMap<String, File>();
            customObjectFilesPerWorld.put(worldName, worldCustomObjectFiles);
            if (worldName != null && new File(OTG.getEngine().getOTGRootFolder() + File.separator + PluginStandardValues.PresetsDirectoryName + File.separator + worldName + File.separator + "WorldObjects").exists())
            {
                indexAllCustomObjectFilesInDir(
                        new File(OTG.getEngine().getOTGRootFolder() + File.separator + PluginStandardValues.PresetsDirectoryName + File.separator + worldName + File.separator + "WorldObjects"),
                        worldCustomObjectFiles);
            }
        }

        // Search WorldObjects

        if (worldName != null && !bSearchedWorldObjects)
        {
            HashMap<String, File> worldCustomObjectFiles = customObjectFilesPerWorld.get(worldName);
            if (worldCustomObjectFiles != null)
            {
                File searchForFile = worldCustomObjectFiles.get(name.toLowerCase());
                if (searchForFile != null)
                {
                    object = loadObject(searchForFile, worldName);

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
                    } else
                    {
                        if (OTG.getPluginConfig().spawnLog)
                        {
                            OTG.log(LogMarker.WARN,
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

        if (!bSearchedGlobalObjects)
        {
            object = objectsByNameGlobalObjects.get(name.toLowerCase());

            if (object != null)
            {
                return object;
            }

            File searchForFile = customObjectFilesGlobalObjects.get(name.toLowerCase());

            if (searchForFile != null)
            {
                object = loadObject(searchForFile, worldName);

                if (object != null)
                {
                    objectsByNameGlobalObjects.put(name.toLowerCase(), object);
                    return object;
                } else
                {
                    if (OTG.getPluginConfig().spawnLog)
                    {
                        OTG.log(LogMarker.WARN,
                                "Could not load BO2/BO3, it probably contains errors: " + searchForFile);
                    }
                    return null;
                }
            }

            // Not Found
            objectsNotFoundGlobalObjects.add(name.toLowerCase());
        }

        if (OTG.getPluginConfig().spawnLog)
        {
            OTG.log(LogMarker.WARN,
                    "Could not find BO2/BO3 " + name + " in GlobalObjects " + (worldName != null ? "and WorldObjects" : "") + " directory " + (worldName != null ? "for world " + worldName : "") + ".");
        }

        return null;
    }

    private void indexAllCustomObjectFilesInDir(File searchDir, HashMap<String, File> customObjectFiles)
    {
        if (searchDir.exists())
        {
            if (searchDir.isDirectory())
            {
                for (File fileInDir : searchDir.listFiles())
                {
                    if (fileInDir.isDirectory())
                    {
                        indexAllCustomObjectFilesInDir(fileInDir, customObjectFiles);
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
                                if (OTG.getPluginConfig().spawnLog)
                                {
                                    OTG.log(LogMarker.WARN, "Duplicate file found: " + fileInDir.getName() + ".");
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
                        if (OTG.getPluginConfig().spawnLog)
                        {
                            OTG.log(LogMarker.WARN, "Duplicate file found: " + searchDir.getName() + ".");
                        }
                    }
                }
            }
        }
    }
}