package com.pg85.otg.configuration.dimensions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.WorldStandardValues;

public class DimensionsConfig
{
	private static HashMap<String, DimensionsConfig> DefaultConfigs = new HashMap<String, DimensionsConfig>();
	private File worldSavesDir;
	
	// Use capitals since we're serialising to yaml and we want to make it look nice
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
		
	public static DimensionsConfig getModPackConfig(String presetName)
	{
		DimensionsConfig forgeWorldConfig = DefaultConfigs.get(presetName);
		if(forgeWorldConfig != null)
		{
			return forgeWorldConfig;
		}
		
		// TODO: Doesn't Forge provide a better way of getting the config dir?
		File configDir = new File(OTG.getEngine().getOTGRootFolder().getParentFile().getParentFile() + "//config//OpenTerrainGenerator//");
		if(configDir.exists())
		{
			for(File f : configDir.listFiles())
			{
				DimensionsConfig forgeWorldConfig2 = DimensionsConfig.defaultConfigfromFile(f);
				if(
					forgeWorldConfig2 != null &&
					(
						(
							// PresetName is null for vanilla overworlds
							forgeWorldConfig2.Overworld.PresetName == null && 
							presetName == null								
						) || (
							forgeWorldConfig2.Overworld.PresetName != null && 
							forgeWorldConfig2.Overworld.PresetName.equals(presetName)
						)
					)
				)
				{
					DefaultConfigs.put(presetName, forgeWorldConfig2);
					return forgeWorldConfig2;
				}
			}
		}
		return null;
	}
	
	private static DimensionsConfig defaultConfigfromFile(File file)
	{
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DimensionsConfig presetsConfig = null;
        
       	try {
			presetsConfig = mapper.readValue(file, DimensionsConfig.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
       	
       	return presetsConfig;
	}
	
	/**
	 * 
	 * @param mcWorldSaveDir Refers to mc/saves/worlddir/
	 * @return
	 */
	public static DimensionsConfig loadFromFile(File mcWorldSaveDir)
	{
		File forgeWorldConfigFile = new File(mcWorldSaveDir + "//OpenTerrainGenerator//" + WorldStandardValues.DimensionsConfigFileName);
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
	       	
	       	presetsConfig.WorldName = mcWorldSaveDir.getName();
	       	presetsConfig.worldSavesDir = mcWorldSaveDir.getParentFile();
		}
       	
       	return presetsConfig;
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
			File forgeWorldConfigFile = new File(worldSavesDir.getAbsolutePath() + "//" + WorldName + "//OpenTerrainGenerator//" + WorldStandardValues.DimensionsConfigFileName);
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
}