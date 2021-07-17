package com.pg85.otg.config.dimensions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;

/**
 * Contains data for a dimension created from a preset, with world creation / O menu settings applied.
 * Saved and loaded with the world.
 * * May also be used for ModPack Configs in the future, hence the yaml code.
 * TODO: Read/Write from bytestream when saving with world data.
 */
public class DimensionConfig
{
	// Use capitals since we're serialising to yaml and want to make it look nice.
	public int Version;
	public String PresetFolderName;
	public OTGDimension OverWorld;
	public OTGDimension Nether;
	public OTGDimension End;
	public List<OTGDimension> Dimensions = new ArrayList<>();

	// Parameterless constructor for deserialisation
	public DimensionConfig() {}
	
	public DimensionConfig(String presetFolderName)
	{
		this.Version = 0;
		this.PresetFolderName = presetFolderName;
	}
	
	public DimensionConfig clone()
	{
		DimensionConfig clone = new DimensionConfig(this.PresetFolderName);
		return clone;
	}
	
	public static DimensionConfig fromDisk(String presetFolderName)
	{
		File dimensionConfig = new File(OTG.getEngine().getOTGRootFolder().toString(), Constants.DIMENSION_CONFIGS_FOLDER + File.separator + presetFolderName + ".yaml");
		if(dimensionConfig.exists())
		{
			DimensionConfig dimConfig = new DimensionConfig(presetFolderName);
	        String content = "";
	        try
	        {
	            content = new String(Files.readAllBytes(dimensionConfig.toPath()));
	        }
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	        }
	        DimensionConfig loadedConfig = fromYamlString(content);
	        dimConfig.Version = loadedConfig.Version;
	        dimConfig.OverWorld = loadedConfig.OverWorld;
	        dimConfig.Nether = loadedConfig.Nether;
	        dimConfig.End = loadedConfig.End;
	        dimConfig.Dimensions = loadedConfig.Dimensions;
	        return dimConfig;
		}
		return null;
	}

	public static DimensionConfig fromYamlString(String input)
	{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		DimensionConfig dimConfig = null;

		try {
			dimConfig = mapper.readValue(input, DimensionConfig.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dimConfig;
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
	
	public static class OTGDimension
	{
		public String PresetFolderName;
		public long Seed;
		
		public OTGDimension() {}
		
		public OTGDimension(String presetFolderName, long seed)
		{
			this.PresetFolderName = presetFolderName;
			this.Seed = seed;
		}
	}
}
