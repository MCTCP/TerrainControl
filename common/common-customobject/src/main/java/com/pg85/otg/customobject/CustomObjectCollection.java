package com.pg85.otg.customobject;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
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

	private HashMap<String, ArrayList<CustomObject>> objectsPerPreset = new HashMap<String, ArrayList<CustomObject>>();
	private HashMap<String, HashMap<String, CustomObject>> objectsByNamePerPreset = new HashMap<String, HashMap<String, CustomObject>>();
	private HashMap<String, ArrayList<String>> objectsNotFoundPerPreset = new HashMap<String, ArrayList<String>>();

	private HashMap<String, File> customObjectFilesGlobalObjects = null;
	private HashMap<String, File> globalTemplates = null;
	private HashMap<String, HashMap<String, File>> customObjectFilesPerPreset = new HashMap<String, HashMap<String, File>>();
	private HashMap<String, HashMap<String, File>> boTemplateFilesPerPreset = new HashMap<>();

	public CustomObject loadObject(File file, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
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
	
						if (presetFolderName != null)
						{
							ArrayList<CustomObject> presetObjects = objectsPerPreset.get(presetFolderName);
							if (presetObjects == null)
							{
								presetObjects = new ArrayList<CustomObject>();
								objectsPerPreset.put(presetFolderName, presetObjects);
							}
							presetObjects.add(object);
						} else {
							objectsGlobalObjects.add(object);
						}
	
						if (!object.onEnable(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker) || !object.loadChecks(modLoadedChecker))
						{
							// Remove the object
							removeLoadedObject(presetFolderName, object);
							
							// Try bo4
							loader = customObjectManager.getObjectLoaders().get("bo4");
							if (loader != null)
							{
								object = loader.loadFromFile(objectName, file, logger);
								if (presetFolderName != null)
								{
									ArrayList<CustomObject> presetObjects = objectsPerPreset.get(presetFolderName);
									if (presetObjects == null)
									{
										presetObjects = new ArrayList<CustomObject>();
										objectsPerPreset.put(presetFolderName, presetObjects);
									}
									presetObjects.add(object);
								} else {
									objectsGlobalObjects.add(object);
								}

								if (!object.onEnable(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker) || !object.loadChecks(modLoadedChecker))
								{
									// Remove the object
									removeLoadedObject(presetFolderName, object);
									return null;
								}
							}
						}
					}
				}
			} else {
				logger.log(LogLevel.FATAL, LogCategory.CUSTOM_OBJECTS, "Given path does not exist: " + file.getAbsolutePath());
				throw new RuntimeException("Given path does not exist: " + file.getAbsolutePath());
			}
			return object;
		}
	}
		  
	private void removeLoadedObject(String presetFolderName, CustomObject object)
	{
		if (presetFolderName != null)
		{
			HashMap<String, CustomObject> presetObjectsByName = objectsByNamePerPreset.get(presetFolderName);
			if(presetObjectsByName != null)
			{
				presetObjectsByName.remove(object.getName().toLowerCase());
				if (presetObjectsByName.size() == 0)
				{
					objectsByNamePerPreset.remove(presetFolderName, presetObjectsByName);
				}
			}
			
			ArrayList<CustomObject> worldObjects = objectsPerPreset.get(presetFolderName);
			worldObjects.remove(object);
			if (worldObjects.size() == 0)
			{
				objectsPerPreset.remove(presetFolderName, worldObjects);
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

	void addGlobalObjectFile(String name, File file)
	{
		synchronized(indexingFilesLock)
		{
			if (!customObjectFilesGlobalObjects.containsKey(name.toLowerCase(Locale.ROOT)))
			{
				customObjectFilesGlobalObjects.put(name.toLowerCase(Locale.ROOT), file);
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
			
			objectsPerPreset.clear();
			objectsByNamePerPreset.clear();
			objectsNotFoundPerPreset.clear();	
		}
	}
	
	void reloadCustomObjectFiles()
	{
		synchronized(indexingFilesLock)
		{
			objectsGlobalObjects.clear();
			objectsByNameGlobalObjects.clear();
			objectsNotFoundGlobalObjects.clear();
	
			objectsPerPreset.clear();
			objectsByNamePerPreset.clear();
			objectsNotFoundPerPreset.clear();
	
			customObjectFilesGlobalObjects = null;
			globalTemplates = null;
			customObjectFilesPerPreset.clear();
			boTemplateFilesPerPreset.clear();
		}
	}

	public ArrayList<String> getAllBONamesForPreset(String presetFolderName, ILogger logger, Path otgRootPath)
	{
		HashMap<String, File> files = customObjectFilesPerPreset.get(presetFolderName);
		if (files == null)
		{
			indexPresetObjectsFolder(presetFolderName, logger, otgRootPath);
			files = customObjectFilesPerPreset.get(presetFolderName);
		}
		return files == null ? null : new ArrayList<>(files.keySet());
	}

	public ArrayList<String> getTemplatesForPreset(String presetFolderName, ILogger logger, Path otgRootPath)
	{
		HashMap<String, File> files = boTemplateFilesPerPreset.get(presetFolderName);
		if (files == null)
		{
			indexPresetObjectsFolder(presetFolderName, logger, otgRootPath);
			files = customObjectFilesPerPreset.get(presetFolderName);
		}
		return files == null ? null : new ArrayList<>(files.keySet());
	}

	public ArrayList<String> getGlobalObjectNames(ILogger logger, Path otgRootPath)
	{
		if (customObjectFilesGlobalObjects == null)
		{
			indexGlobalObjectsFolder(logger, otgRootPath);
		}
		return customObjectFilesGlobalObjects == null
				? null
				: new ArrayList<>(customObjectFilesGlobalObjects.keySet());
	}

	public ArrayList<String> getGlobalTemplates(ILogger logger, Path otgRootPath)
	{
		if (globalTemplates == null)
		{
			indexGlobalObjectsFolder(logger, otgRootPath);
		}
		return globalTemplates == null
				? null
				: new ArrayList<>(globalTemplates.keySet());
	}

	// Adds an object to a preset, if it has been loaded
	// Does not add the object if the preset's BO's have not yet been indexed
	public void addObjectToPreset(String presetFolderName, String objectName, File boFile, CustomObject object)
	{
		HashMap<String, CustomObject> objectsByName = objectsByNamePerPreset.get(presetFolderName);
		if (objectsByName != null) objectsByName.put(objectName, object);
		HashMap<String, File> customObjectFiles = customObjectFilesPerPreset.get(presetFolderName);
		if (customObjectFiles != null) customObjectFiles.put(objectName, boFile);
	}
	
	public CustomObject getObjectByName(String name, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		synchronized(indexingFilesLock)
		{
			return getObjectByName(name, presetFolderName, true, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		}
	}
	
	void indexGlobalObjectsFolder(ILogger logger, Path otgRootFolder)
	{
		synchronized(indexingFilesLock)
		{
			if (customObjectFilesGlobalObjects == null)
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.INFO, LogCategory.CUSTOM_OBJECTS, "Indexing GlobalObjects folder.");
				}
				customObjectFilesGlobalObjects = new HashMap<String, File>();
				globalTemplates = new HashMap<>();
				if (new File(otgRootFolder + File.separator + Constants.GLOBAL_OBJECTS_FOLDER).exists())
				{
					indexAllCustomObjectFilesInDir(new File(otgRootFolder + File.separator + Constants.GLOBAL_OBJECTS_FOLDER), customObjectFilesGlobalObjects, globalTemplates, logger);
				}
	
				// Add vanilla custom objects
				for (TreeType type : TreeType.values())
				{
					addLoadedGlobalObject(new TreeObject(type));
				}
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.INFO, LogCategory.CUSTOM_OBJECTS, "GlobalObjects folder indexed.");
				}
			}
		}
	}
	
	void indexPresetObjectsFolder(String presetFolderName, ILogger logger, Path otgRootFolder)
	{
		synchronized(indexingFilesLock)
		{
			if (presetFolderName != null && !customObjectFilesPerPreset.containsKey(presetFolderName))
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.INFO, LogCategory.CUSTOM_OBJECTS, "Indexing Objects folder for preset " + presetFolderName);
				}
				HashMap<String, File> presetCustomObjectFiles = new HashMap<String, File>();
				customObjectFilesPerPreset.put(presetFolderName, presetCustomObjectFiles);
				HashMap<String, File> templateFiles = new HashMap<String, File>();
				boTemplateFilesPerPreset.put(presetFolderName, templateFiles);
				if (presetFolderName != null)
				{
					// TODO: Rename folders
					String objectsFolderName = 
						new File(otgRootFolder + File.separator + Constants.PRESETS_FOLDER + File.separator + presetFolderName + File.separator + Constants.WORLD_OBJECTS_FOLDER).exists() ? Constants.WORLD_OBJECTS_FOLDER :
						new File(otgRootFolder + File.separator + Constants.PRESETS_FOLDER + File.separator + presetFolderName + File.separator + Constants.LEGACY_WORLD_OBJECTS_FOLDER).exists() ? Constants.LEGACY_WORLD_OBJECTS_FOLDER : null
					;					
					if(objectsFolderName != null)
					{
						indexAllCustomObjectFilesInDir(
						new File(otgRootFolder + File.separator + Constants.PRESETS_FOLDER + File.separator + presetFolderName + File.separator + objectsFolderName),
						presetCustomObjectFiles, templateFiles, logger);
					}
				}
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.INFO, LogCategory.CUSTOM_OBJECTS, "Objects folder for preset " + presetFolderName + " indexed.");
				}
			}
		}
	}
	
	/**
	 * Gets the object with the given name.
	 * 
	 * @param name Name of the object.
	 * @return The object, or null if not found.
	 */
	private CustomObject getObjectByName(String name, String presetFolderName, boolean searchGlobalObjects, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		synchronized(this.indexingFilesLock)
		{
			// OTG.log(LogMarker.INFO, "getObjectByName " + presetFolderName != null ? presetFolderName : "");
	
			CustomObject object = null;
	
			// Check if the object has been cached
	
			if (presetFolderName != null)
			{
				HashMap<String, CustomObject> presetObjectsByName = this.objectsByNamePerPreset.get(presetFolderName);
				if (presetObjectsByName != null)
				{
					object = presetObjectsByName.get(name.toLowerCase());
				}
			}
	
			boolean bSearchedPresetObjects = false;
	
			if (object == null && presetFolderName != null)
			{
				ArrayList<String> presetObjectsNotFoundByName = this.objectsNotFoundPerPreset.get(presetFolderName);
				if (presetObjectsNotFoundByName != null && presetObjectsNotFoundByName.contains(name.toLowerCase()))
				{
					// TODO: If a user adds a new object while the game is running, it won't be picked up, even when developermode:true.
					bSearchedPresetObjects = true;
				}
			}
	
			// Only check the GlobalObjects if the preset's Objects directory has already been searched
			if (object == null && searchGlobalObjects && (presetFolderName == null || bSearchedPresetObjects))
			{
				object = this.objectsByNameGlobalObjects.get(name.toLowerCase());
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
	
			if (this.objectsNotFoundGlobalObjects != null && this.objectsNotFoundGlobalObjects.contains(name.toLowerCase()))
			{
				// TODO: If a user adds a new object while the game is running, it won't be picked up, even when developermode:true.
				bSearchedGlobalObjects = true;
			}
	
			if ((!searchGlobalObjects || bSearchedGlobalObjects) && (presetFolderName == null || bSearchedPresetObjects))
			{
				return null;
			}
	
			// Index GlobalObjects and preset's Objects directories
	
			indexGlobalObjectsFolder(logger, otgRootFolder);
			indexPresetObjectsFolder(presetFolderName, logger, otgRootFolder);
	
			// Search preset Objects
	
			if (presetFolderName != null && !bSearchedPresetObjects)
			{
				HashMap<String, File> presetCustomObjectFiles = this.customObjectFilesPerPreset.get(presetFolderName);
				if (presetCustomObjectFiles != null)
				{
					File searchForFile = presetCustomObjectFiles.get(name.toLowerCase());
					if (searchForFile != null)
					{
						object = loadObject(searchForFile, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
						if (object != null)
						{
							HashMap<String, CustomObject> presetObjectsByName = this.objectsByNamePerPreset.get(presetFolderName);
							if (presetObjectsByName == null)
							{
								presetObjectsByName = new HashMap<String, CustomObject>();
								this.objectsByNamePerPreset.put(presetFolderName, presetObjectsByName);
							}
							presetObjectsByName.put(name.toLowerCase(), object);
							return object;
						} else {
							if (logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
							{
								logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not load BO2/BO3, it likely contains errors: " + searchForFile);
							}
							return null;
						}
					}
				}
	
				// Not found
				ArrayList<String> presetObjectsNotFound = this.objectsNotFoundPerPreset.get(presetFolderName);
				if (presetObjectsNotFound == null)
				{
					presetObjectsNotFound = new ArrayList<String>();
					this.objectsNotFoundPerPreset.put(presetFolderName, presetObjectsNotFound);
				}
				presetObjectsNotFound.add(name.toLowerCase());
			}
	
			// Search GlobalObjects
	
			if (searchGlobalObjects && !bSearchedGlobalObjects)
			{
				object = this.objectsByNameGlobalObjects.get(name.toLowerCase());
	
				if (object != null)
				{
					return object;
				}
	
				File searchForFile = this.customObjectFilesGlobalObjects.get(name.toLowerCase());
	
				if (searchForFile != null)
				{
					object = loadObject(searchForFile, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	
					if (object != null)
					{
						this.objectsByNameGlobalObjects.put(name.toLowerCase(), object);
						return object;
					} else {
						if (logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
						{
							logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not load BO2/BO3, it probably contains errors: " + searchForFile);
						}
						return null;
					}
				}
	
				// Not Found
				this.objectsNotFoundGlobalObjects.add(name.toLowerCase());
			}
	
			if (logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not find BO2/BO3 " + name + " in GlobalObjects " + (presetFolderName != null ? "and Objects" : "") + " directory " + (presetFolderName != null ? "for preset " + presetFolderName : "") + ".");
			}
	
			return null;
		}
	}

	private void indexAllCustomObjectFilesInDir(File searchDir, HashMap<String, File> customObjectFiles, HashMap<String, File> templateFiles, ILogger logger)
	{
		if (searchDir.exists())
		{
			if (searchDir.isDirectory())
			{
				for (File fileInDir : searchDir.listFiles())
				{
					if (fileInDir.isDirectory())
					{
						indexAllCustomObjectFilesInDir(fileInDir, customObjectFiles, templateFiles, logger);
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
								if (logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
								{
									logger.log(LogLevel.WARN, LogCategory.CUSTOM_OBJECTS, "Duplicate file found: " + fileInDir.getName() + ".");
								}
							}
						}
						else if (fileInDir.getName().toLowerCase().endsWith("bo3template"))
						{
							templateFiles.put(fileInDir.getName().toLowerCase(Locale.ROOT).replace(".bo3template", ""), fileInDir);
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
						if (logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
						{
							logger.log(LogLevel.WARN, LogCategory.CUSTOM_OBJECTS, "Duplicate file found: " + searchDir.getName() + ".");
						}
					}
				}
				else if (searchDir.getName().toLowerCase().endsWith("bo3template"))
				{
					templateFiles.put(searchDir.getName().toLowerCase(Locale.ROOT).replace(".bo3template", ""), searchDir);
				}
			}
		}
	}
}
