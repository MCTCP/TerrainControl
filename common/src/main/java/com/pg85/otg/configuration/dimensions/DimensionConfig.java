package com.pg85.otg.configuration.dimensions;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.configuration.world.WorldConfig;

public class DimensionConfig extends DimensionConfigBase
{
	public DimensionConfig() { }
	
	public DimensionConfig(String presetName)
	{
		super(presetName);
	}

	public DimensionConfig(DimensionConfigGui dimConfig)
	{		
		this.PresetName = dimConfig.PresetName;
		this.Seed = dimConfig.Seed;
		this.WorldBorderRadiusInChunks = dimConfig.WorldBorderRadiusInChunks;
		this.PregeneratorRadiusInChunks = dimConfig.PregeneratorRadiusInChunks;
		
		this.Settings = dimConfig.Settings.clone();
		this.GameRules = dimConfig.GameRules.clone();
	}
	
	public DimensionConfig(String presetName, WorldConfig worldConfig)
	{	
		super(presetName, worldConfig);
	}
	
	@Override
	public DimensionConfig clone()
	{
		DimensionConfig clone = new DimensionConfig();
		
		clone.PresetName = this.PresetName;
		clone.Seed = this.Seed;
		clone.WorldBorderRadiusInChunks = this.WorldBorderRadiusInChunks;
		clone.PregeneratorRadiusInChunks = this.PregeneratorRadiusInChunks;
		
		clone.Settings = this.Settings.clone();
		clone.GameRules = this.GameRules.clone();
		
		return clone;
	}
	
	public static DimensionConfig fromYamlString(String readStringFromStream)
	{
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DimensionConfig dimConfig = null;
       	try {
       		dimConfig = mapper.readValue(readStringFromStream, DimensionConfig.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
       	
		return dimConfig;
	}
}