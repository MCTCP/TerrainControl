package com.pg85.otg.config.dimensions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldConfig;

public class DimensionsConfig
{	
	private String ModPackConfigName = null;
	private int ModPackConfigVersion = 0;
	
	private Path worldSavesDir;
	// Use capitals since we're serialising to yaml and we want to make it look nice
	public boolean ShowOTGWorldCreationMenu = true;
	private int version = 0; // Only changed when saving, otherwise legacy configs that don't have the version field will get this value.
	private static int currentVersion = 1; // The current version
	public String WorldName;
	public DimensionConfig Overworld;
	ArrayList<DimensionConfig> Dimensions = new ArrayList<DimensionConfig>();

	public DimensionsConfig() { }
	
	public DimensionsConfig(Path mcWorldSaveDir)
	{
		this.WorldName = mcWorldSaveDir.toFile().getName();
       	this.worldSavesDir = mcWorldSaveDir.getParent();
	}
	
	public DimensionsConfig(Path mcWorldSavesDir, String worldDir)
	{
		this.worldSavesDir = mcWorldSavesDir;
		this.WorldName = worldDir;
	}
		
	@JsonIgnore	
	public List<DimensionConfig> getAllDimensions()
	{
		List<DimensionConfig> dimensions = new ArrayList<DimensionConfig>();
		if(Overworld != null)
		{
			dimensions.add(Overworld);
		}
		dimensions.addAll(Dimensions);
		return dimensions;
	}
	
	public String toYamlString()
	{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void save()
	{
		// Don't save default configs (loaded via defaultConfigfromFile)
		// TODO: Make this prettier, Save shouldn't work depending on which constructor was used ><. Split this class up?
		if(worldSavesDir != null)
		{
			File dimensionsConfigFile = new File(worldSavesDir.toFile().getAbsolutePath() + File.separator + WorldName + File.separator + Constants.MOD_ID + File.separator + Constants.DimensionsConfigFileName);
			File dimensionsConfigBackupFile = new File(worldSavesDir.toFile().getAbsolutePath() + File.separator + WorldName + File.separator + Constants.MOD_ID + File.separator + Constants.DimensionsConfigBackupFileName);
		
			// Create an ObjectMapper mapper for YAML
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

			// Write Yaml file
			try
			{
				if(!dimensionsConfigFile.exists())
				{
					dimensionsConfigFile.getParentFile().mkdirs();
					try {
						dimensionsConfigFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException("Could not create " + dimensionsConfigFile.getAbsolutePath() + ", exiting.");
					}
				} else {
					Files.move(dimensionsConfigFile.toPath(), dimensionsConfigBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				
				this.version = DimensionsConfig.currentVersion;
				mapper.writeValue(dimensionsConfigFile, this);

				ArrayList<String> list = new ArrayList<String>();

				// Add a comment to the top of the file (since jackson can't add comments...)				
				BufferedReader read = new BufferedReader(new FileReader(dimensionsConfigFile));
	
				String dataRow = read.readLine(); 
				while (dataRow != null)
				{
				    list.add(dataRow);
				    dataRow = read.readLine(); 
				}
				read.close();
				
				// Write the Yaml file with comment
				FileWriter writer = new FileWriter(dimensionsConfigFile);
				String headerComments = "#TODO: Provide instructions for modpack devs.";
				writer.append(headerComments);
	
				for (int i = 0; i < list.size(); i++)
				{
				    writer.append(System.getProperty("line.separator"));
				    writer.append(list.get(i));
				}
				writer.flush();
				writer.close();					
			}
			catch(IOException e)
			{
				e.printStackTrace();
				throw new RuntimeException(
					"OTG encountered a critical error writing " + dimensionsConfigFile.getAbsolutePath() + ", exiting. "
					+ "OTG automatically backs up files before writing and will try to use the backup when loading. "
					+ "If your world's " + Constants.DimensionsConfigFileName + " and its backup have been corrupted, you can "
					+ "replace it with your own backup or create a new world with the same dimensions and copy its " 
					+ Constants.DimensionsConfigFileName + " (edit the WorldName node if necessary)."
				);
			}		
		}
	}

	public DimensionConfig getDimensionConfig(String worldName)
	{
    	if(worldName.equals("overworld") || worldName.equals(this.WorldName)) // TODO: Any way to work around using "overworld"? This way presets named overworld will cause problems.
    	{
    		return this.Overworld;
    	} else {
    		if(this.Dimensions != null)
    		{
	    		for(DimensionConfig dimConfig : this.Dimensions)
	    		{
	    			if(dimConfig.PresetName.equals(worldName))
	    			{
	    				return dimConfig;
	    			}
	    		}
    		}
    	}
		return null;
	}

	public static DimensionsConfig fromYamlString(String readStringFromStream)
	{
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DimensionsConfig presetsConfig = null;
       	try {
			presetsConfig = mapper.readValue(readStringFromStream, DimensionsConfig.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
       	
		return presetsConfig;
	}
	
	static DimensionsConfig defaultConfigfromFile(File file, Path otgRootFolder, boolean isModPackConfig, ModPackConfigManager modPackConfigManager, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DimensionsConfig presetsConfig = null;
        
       	try {
			presetsConfig = mapper.readValue(file, DimensionsConfig.class);
		}
       	catch (IOException e)
       	{
       		logger.log(LogMarker.WARN, "Modpack Config " + file.getName() + " could not be read.");
			e.printStackTrace();
		}
       	       	
       	if(presetsConfig != null)
       	{
       		updateConfig(presetsConfig, otgRootFolder, isModPackConfig, modPackConfigManager, biomeResourcesManager, spawnLog, logger, materialReader);
       	}
       	
       	return presetsConfig;
	}
	
	private static void updateConfig(DimensionsConfig dimsConfig, Path otgRootFolder, boolean isModPackConfig, ModPackConfigManager modPackConfigManager, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		boolean doSave = false;
       	// Update the config if necessary
       	if(dimsConfig.version == 0)
       	{
       		doSave = true;
       		// Update the IsOTGPlus field from the worldconfig, added for v1.
       		if(dimsConfig.Overworld != null && dimsConfig.Overworld.PresetName != null)
       		{
       			IWorldConfig worldConfig = WorldConfig.fromDisk(Paths.get(otgRootFolder.toString(), Constants.PRESETS_FOLDER + File.separator + dimsConfig.Overworld.PresetName), biomeResourcesManager, spawnLog, logger, materialReader);
       			if(worldConfig != null)
       			{
       				dimsConfig.Overworld.Settings.IsOTGPlus = worldConfig.isOTGPlus();
       			}
       		}
       		if(dimsConfig.Dimensions != null)
       		{
       			for(DimensionConfig dimConfig : dimsConfig.Dimensions)
       			{
       				IWorldConfig worldConfig = WorldConfig.fromDisk(Paths.get(otgRootFolder.toString(), Constants.PRESETS_FOLDER + File.separator + dimConfig.PresetName), biomeResourcesManager, spawnLog, logger, materialReader);
           			if(worldConfig != null)
           			{
           				dimConfig.Settings.IsOTGPlus = worldConfig.isOTGPlus();
           			}
       			}
       		}
       	}
       	if(!isModPackConfig)
       	{
			DimensionsConfig modPackConfig = modPackConfigManager.getModPackConfig(dimsConfig.Overworld.PresetName);
			if(modPackConfig != null)
			{
				// The modpack config has been updated, overwrite settings for overworld and existing dims.
				if(modPackConfig.ModPackConfigName != null && modPackConfig.ModPackConfigVersion > dimsConfig.ModPackConfigVersion)
				{
					doSave = true;
					dimsConfig.ModPackConfigName = modPackConfig.ModPackConfigName;
					dimsConfig.ModPackConfigVersion = modPackConfig.ModPackConfigVersion;
					String seed = dimsConfig.Overworld.Seed;
					String gameType = dimsConfig.Overworld.GameType;
					boolean bonusChest = dimsConfig.Overworld.BonusChest;
					boolean allowCheats = dimsConfig.Overworld.AllowCheats;
					int dimId = 0;
					int pregenerationRadius = dimsConfig.Overworld.PregeneratorRadiusInChunks;
					int worldBorderRadius = dimsConfig.Overworld.WorldBorderRadiusInChunks;
					DimensionConfig modPackDimConfigClone;
					dimsConfig.Overworld = modPackConfig.Overworld.clone();
					dimsConfig.Overworld.GameType = gameType;
					dimsConfig.Overworld.BonusChest = bonusChest;
					dimsConfig.Overworld.AllowCheats = allowCheats;
					dimsConfig.Overworld.Seed = seed;
					dimsConfig.Overworld.PregeneratorRadiusInChunks = pregenerationRadius;
					dimsConfig.Overworld.WorldBorderRadiusInChunks = worldBorderRadius;
					for(DimensionConfig modPackDimConfig : modPackConfig.Dimensions)
					{
						boolean bFound = false;
						for(DimensionConfig dimConfig : new ArrayList<DimensionConfig>(dimsConfig.Dimensions))
						{
	   						if(dimConfig.PresetName.equals(modPackDimConfig.PresetName))
	   						{
	   							bFound = true;
	   							dimsConfig.Dimensions.remove(dimConfig);
	   							seed = dimConfig.Seed;
	   							gameType = dimConfig.GameType;
	   							bonusChest = dimConfig.BonusChest;
	   							allowCheats = dimConfig.AllowCheats;	   							
	   							dimId = dimConfig.DimensionId;
	   							pregenerationRadius = dimConfig.PregeneratorRadiusInChunks;
	   							worldBorderRadius = dimConfig.WorldBorderRadiusInChunks;
	   							modPackDimConfigClone = modPackDimConfig.clone();
	   							modPackDimConfigClone.Seed = seed;
	   							modPackDimConfigClone.GameType = gameType;
	   							modPackDimConfigClone.BonusChest = bonusChest;
	   							modPackDimConfigClone.AllowCheats = allowCheats;
	   							modPackDimConfigClone.DimensionId = dimId;
	   							modPackDimConfigClone.PregeneratorRadiusInChunks = pregenerationRadius;
	   							modPackDimConfigClone.WorldBorderRadiusInChunks = worldBorderRadius;	
	   							dimsConfig.Dimensions.add(modPackDimConfigClone);
	   						}
						}
						if(!bFound)
						{
							dimsConfig.Dimensions.add(modPackDimConfig.clone());
						}
					}
				}
			}
       	}
   		if(doSave)
   		{
   			dimsConfig.save();
   		}
	}
	
	/**
	 * 
	 * @param mcWorldSaveDir Refers to mc/saves/worlddir/
	 * @return
	 */
	public static DimensionsConfig loadFromFile(Path mcWorldSaveDir, Path otgRootFolder, ModPackConfigManager modPackConfigManager, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		File dimensionsConfigFile = new File(mcWorldSaveDir + File.separator + Constants.MOD_ID + File.separator + Constants.DimensionsConfigFileName);		
		File dimensionsConfigBackupFile = new File(mcWorldSaveDir + File.separator + Constants.MOD_ID + File.separator + Constants.DimensionsConfigBackupFileName);
        
		if(!dimensionsConfigFile.exists() && !dimensionsConfigBackupFile.exists())
		{
			return null;
		}
		
		if(dimensionsConfigFile.exists())
		{
			DimensionsConfig presetsConfig = null;        
	        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	        
	       	try {
				presetsConfig = mapper.readValue(dimensionsConfigFile, DimensionsConfig.class);
			} catch (IOException e) {
				e.printStackTrace();
				logger.log(LogMarker.WARN, "Failed to load " + dimensionsConfigFile.getAbsolutePath() + ", trying to load backup.");
			}

	       	if (presetsConfig != null)
	       	{
		       	presetsConfig.WorldName = mcWorldSaveDir.toFile().getName();
		       	presetsConfig.worldSavesDir = mcWorldSaveDir.getParent();
		       	try
		       	{
			       	updateConfig(presetsConfig, otgRootFolder, false, modPackConfigManager, biomeResourcesManager, spawnLog, logger, materialReader);
					return presetsConfig;
		       	}
		       	catch(Exception ex)
		       	{
		       		ex.printStackTrace();
		       		logger.log(LogMarker.WARN, "Failed to load " + dimensionsConfigFile.getAbsolutePath() + ", trying to load backup.");
		       	}
			}
		}
		
		if(dimensionsConfigBackupFile.exists())
		{
			DimensionsConfig presetsConfig = null;        
	        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	        
	       	try {
				presetsConfig = mapper.readValue(dimensionsConfigBackupFile, DimensionsConfig.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
	       	
	       	if (presetsConfig != null)
	       	{
		       	presetsConfig.WorldName = mcWorldSaveDir.toFile().getName();
		       	presetsConfig.worldSavesDir = mcWorldSaveDir.getParent();
		       	try
		       	{
			       	updateConfig(presetsConfig, otgRootFolder, false, modPackConfigManager, biomeResourcesManager, spawnLog, logger, materialReader);
					return presetsConfig;
		       	}
		       	catch(Exception ex)
		       	{
		       		ex.printStackTrace();
		       	}
			}
		}
		
		throw new RuntimeException(
			"OTG encountered a critical error loading " + dimensionsConfigFile.getAbsolutePath() + " and could not load a backup, exiting."
			+ "OTG automatically backs up files before writing and tries to use the backup when loading. "
			+ "If your world's " + Constants.DimensionsConfigFileName + " and its backup have been corrupted, you can "
			+ "replace it with your own backup or create a new world with the same dimensions and copy its " 
			+ Constants.DimensionsConfigFileName + " (edit the WorldName node if necessary)."
		);
	}
}
