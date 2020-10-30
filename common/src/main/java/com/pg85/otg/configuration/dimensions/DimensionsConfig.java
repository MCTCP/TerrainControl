package com.pg85.otg.configuration.dimensions;

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
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.logging.LogMarker;

public class DimensionsConfig
{	
	public String ModPackConfigName = null;
	public int ModPackConfigVersion = 0;
	
	File worldSavesDir;
	// Use capitals since we're serialising to yaml and we want to make it look nice
	public boolean ShowOTGWorldCreationMenu = true;
	public int version = 0; // Only changed when saving, otherwise legacy configs that don't have the version field will get this value.
	private static int currentVersion = 1; // The current version
	public String WorldName;
	public DimensionConfig Overworld;
	public ArrayList<DimensionConfig> Dimensions = new ArrayList<DimensionConfig>();

	public DimensionsConfig() { }
	
	public DimensionsConfig(File mcWorldSaveDir)
	{
		this.WorldName = mcWorldSaveDir.getName();
       	this.worldSavesDir = mcWorldSaveDir.getParentFile();
	}
	
	public DimensionsConfig(File mcWorldSavesDir, String worldDir)
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

	public void save()
	{
		// Don't save default configs (loaded via defaultConfigfromFile)
		// TODO: Make this prettier, Save shouldn't work depending on which constructor was used ><. Split this class up?
		if(worldSavesDir != null)
		{
			File dimensionsConfigFile = new File(worldSavesDir.getAbsolutePath() + File.separator + WorldName + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.DimensionsConfigFileName);
			File dimensionsConfigBackupFile = new File(worldSavesDir.getAbsolutePath() + File.separator + WorldName + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.DimensionsConfigBackupFileName);
		
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
					+ "If your world's " + WorldStandardValues.DimensionsConfigFileName + " and its backup have been corrupted, you can "
					+ "replace it with your own backup or create a new world with the same dimensions and copy its " 
					+ WorldStandardValues.DimensionsConfigFileName + " (edit the WorldName node if necessary)."
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
	
	static DimensionsConfig defaultConfigfromFile(File file, Path otgRootFolder, boolean isModPackConfig)
	{
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DimensionsConfig presetsConfig = null;
        
       	try {
			presetsConfig = mapper.readValue(file, DimensionsConfig.class);
		}
       	catch (IOException e)
       	{
			OTG.log(LogMarker.WARN, "Modpack Config " + file.getName() + " could not be read.");
			e.printStackTrace();
		}
       	       	
       	if(presetsConfig != null)
       	{
       		updateConfig(presetsConfig, otgRootFolder, isModPackConfig);
       	}
       	
       	return presetsConfig;
	}
	
	private static void updateConfig(DimensionsConfig dimsConfig, Path otgRootFolder, boolean isModPackConfig)
	{
		boolean doSave = false;
       	// Update the config if necessary
       	if(dimsConfig.version == 0)
       	{
       		doSave = true;
       		// Update the IsOTGPlus field from the worldconfig, added for v1.
       		if(dimsConfig.Overworld != null && dimsConfig.Overworld.PresetName != null)
       		{
       			WorldConfig worldConfig = WorldConfig.fromDisk(Paths.get(otgRootFolder.toString(), PluginStandardValues.PRESETS_FOLDER + File.separator + dimsConfig.Overworld.PresetName));
       			if(worldConfig != null)
       			{
       				dimsConfig.Overworld.Settings.IsOTGPlus = worldConfig.isOTGPlus;
       			}
       		}
       		if(dimsConfig.Dimensions != null)
       		{
       			for(DimensionConfig dimConfig : dimsConfig.Dimensions)
       			{
       				WorldConfig worldConfig = WorldConfig.fromDisk(Paths.get(otgRootFolder.toString(), PluginStandardValues.PRESETS_FOLDER + File.separator + dimConfig.PresetName));
           			if(worldConfig != null)
           			{
           				dimConfig.Settings.IsOTGPlus = worldConfig.isOTGPlus;
           			}
       			}
       		}
       	}
       	if(!isModPackConfig)
       	{
			DimensionsConfig modPackConfig = OTG.getEngine().getModPackConfigManager().getModPackConfig(dimsConfig.Overworld.PresetName);
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
	public static DimensionsConfig loadFromFile(File mcWorldSaveDir, Path otgRootFolder)
	{
		File dimensionsConfigFile = new File(mcWorldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.DimensionsConfigFileName);		
		File dimensionsConfigBackupFile = new File(mcWorldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.DimensionsConfigBackupFileName);
        
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
				OTG.log(LogMarker.WARN, "Failed to load " + dimensionsConfigFile.getAbsolutePath() + ", trying to load backup.");
			}

	       	if (presetsConfig != null)
	       	{
		       	presetsConfig.WorldName = mcWorldSaveDir.getName();
		       	presetsConfig.worldSavesDir = mcWorldSaveDir.getParentFile();
		       	try
		       	{
			       	updateConfig(presetsConfig, otgRootFolder, false);
					return presetsConfig;
		       	}
		       	catch(Exception ex)
		       	{
		       		ex.printStackTrace();
		       		OTG.log(LogMarker.WARN, "Failed to load " + dimensionsConfigFile.getAbsolutePath() + ", trying to load backup.");
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
		       	presetsConfig.WorldName = mcWorldSaveDir.getName();
		       	presetsConfig.worldSavesDir = mcWorldSaveDir.getParentFile();
		       	try
		       	{
			       	updateConfig(presetsConfig, otgRootFolder, false);
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
			+ "If your world's " + WorldStandardValues.DimensionsConfigFileName + " and its backup have been corrupted, you can "
			+ "replace it with your own backup or create a new world with the same dimensions and copy its " 
			+ WorldStandardValues.DimensionsConfigFileName + " (edit the WorldName node if necessary)."
		);
	}
}
