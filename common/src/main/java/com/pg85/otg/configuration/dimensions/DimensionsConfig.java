package com.pg85.otg.configuration.dimensions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.fasterxml.jackson.core.JsonGenerationException;
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
			File forgeWorldConfigFile = new File(worldSavesDir.getAbsolutePath() + File.separator + WorldName + File.separator + "OpenTerrainGenerator" + File.separator + WorldStandardValues.DimensionsConfigFileName);
			if(!forgeWorldConfigFile.exists())
			{
				forgeWorldConfigFile.getParentFile().mkdirs();
				try {
					forgeWorldConfigFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("OTG encountered a critical error, exiting.");
				}
			}
			
			// Create an ObjectMapper mapper for YAML
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	
			// Write object as YAML file
			try
			{
				try {
					this.version = this.currentVersion;
					mapper.writeValue(forgeWorldConfigFile, this);
				} catch (JsonGenerationException e) {
					e.printStackTrace();
					throw new RuntimeException("OTG encountered a critical error, exiting.");
				} catch (JsonMappingException e) {
					e.printStackTrace();
					throw new RuntimeException("OTG encountered a critical error, exiting.");
				}
				
				// Add a comment to the top of the file (since jackson can't add comments...)
				
				BufferedReader read = new BufferedReader(new FileReader(forgeWorldConfigFile));
				ArrayList<String> list = new ArrayList<String>();
	
				String dataRow = read.readLine(); 
				while (dataRow != null){
				    list.add(dataRow);
				    dataRow = read.readLine(); 
				}
				read.close();
				
				FileWriter writer = new FileWriter(forgeWorldConfigFile);
				String headerComments = "#TODO: Provide instructions for modpack devs.";
				writer.append(headerComments);
	
				for (int i = 0; i < list.size(); i++){
				    writer.append(System.getProperty("line.separator"));
				    writer.append(list.get(i));
				}
				writer.flush();
				writer.close();	
			}
			catch(IOException e)
			{
				e.printStackTrace();
				throw new RuntimeException("OTG encountered a critical error, exiting.");
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
	
	static DimensionsConfig defaultConfigfromFile(File file, File otgRootFolder)
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
       		updateConfig(presetsConfig, otgRootFolder);
       	}
       	
       	return presetsConfig;
	}
	
	private static void updateConfig(DimensionsConfig dimsConfig, File otgRootFolder)
	{
       	// Update the config if necessary
       	if(dimsConfig.version == 0)
       	{
       		// Update the IsOTGPlus field from the worldconfig, added for v1.
       		if(dimsConfig.Overworld != null && dimsConfig.Overworld.PresetName != null)
       		{
       			WorldConfig worldConfig = WorldConfig.loadWorldConfigFromDisk(new File(otgRootFolder, PluginStandardValues.PresetsDirectoryName + File.separator + dimsConfig.Overworld.PresetName));
       			if(worldConfig != null)
       			{
       				dimsConfig.Overworld.Settings.IsOTGPlus = worldConfig.isOTGPlus;
       			}
       		}
       		if(dimsConfig.Dimensions != null)
       		{
       			for(DimensionConfig dimConfig : dimsConfig.Dimensions)
       			{
           			WorldConfig worldConfig = WorldConfig.loadWorldConfigFromDisk(new File(otgRootFolder, PluginStandardValues.PresetsDirectoryName + File.separator + dimConfig.PresetName));
           			if(worldConfig != null)
           			{
           				dimConfig.Settings.IsOTGPlus = worldConfig.isOTGPlus;
           			}
       			}
       		}
       		dimsConfig.save();
       	}
	}
	
	/**
	 * 
	 * @param mcWorldSaveDir Refers to mc/saves/worlddir/
	 * @return
	 */
	public static DimensionsConfig loadFromFile(File mcWorldSaveDir, File otgRootFolder)
	{
		File forgeWorldConfigFile = new File(mcWorldSaveDir + File.separator + "OpenTerrainGenerator" + File.separator + WorldStandardValues.DimensionsConfigFileName);
        DimensionsConfig presetsConfig = null;
        
		if(forgeWorldConfigFile.exists())
		{
	        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	        
	       	try {
				presetsConfig = mapper.readValue(forgeWorldConfigFile, DimensionsConfig.class);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

	       	if (presetsConfig != null)
	       	{
		       	presetsConfig.WorldName = mcWorldSaveDir.getName();
		       	presetsConfig.worldSavesDir = mcWorldSaveDir.getParentFile();
			} else {
	       		OTG.log(LogMarker.FATAL, "Failed to load " + mcWorldSaveDir + File.separator + "OpenTerrainGenerator" + File.separator + WorldStandardValues.DimensionsConfigFileName + ", aborting");
			}
		}
       	
		if(presetsConfig != null)
		{
			updateConfig(presetsConfig, otgRootFolder);
		}
		
       	return presetsConfig;
	}
}
