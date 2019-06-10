package com.pg85.otg.configuration.dimensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.configuration.world.WorldConfig;

public class DimensionConfigGui extends DimensionConfigBase
{
	public List<String> Dimensions = new ArrayList<String>();
	public String author = null;
	public String description = null;
	public String worldPackerModName = null;
	
	public DimensionConfigGui()
	{
		super();
	}
	
	public DimensionConfigGui(String presetName)
	{
		super(presetName);
	}
	
	public DimensionConfigGui(String presetName, WorldConfig worldConfig)
	{	
		super(presetName, worldConfig);
		
		this.Dimensions = worldConfig.Dimensions;
		this.author = worldConfig.author;
		this.description = worldConfig.description;
		this.worldPackerModName = worldConfig.worldPackerModName;
	}	
	
	@Override
	public DimensionConfigGui clone()
	{	
		DimensionConfigGui clone = new DimensionConfigGui();
		
		clone.Dimensions = this.Dimensions;
		clone.author = this.author;
		clone.description = this.description;
		clone.worldPackerModName = this.worldPackerModName;
		
		clone.PresetName = this.PresetName;
		clone.Seed = this.Seed;
		clone.WorldBorderRadiusInChunks = this.WorldBorderRadiusInChunks;
		clone.PregeneratorRadiusInChunks = this.PregeneratorRadiusInChunks;
		
		clone.Settings = this.Settings.clone();
		clone.GameRules = this.GameRules.clone();
				
		return clone;
	}
	
	public static DimensionConfigGui FromBaseConfig(DimensionConfig dimConfigBase)
	{
		DimensionConfigGui dimConfig = new DimensionConfigGui();
		
		dimConfig.PresetName = dimConfigBase.PresetName;
		dimConfig.Seed = dimConfigBase.Seed;
		dimConfig.WorldBorderRadiusInChunks = dimConfigBase.WorldBorderRadiusInChunks;
		dimConfig.PregeneratorRadiusInChunks = dimConfigBase.PregeneratorRadiusInChunks;
		
		dimConfig.Settings = dimConfigBase.Settings.clone();
		dimConfig.GameRules = dimConfigBase.GameRules.clone();
		
		return dimConfig;
	}
	
	public static DimensionConfigGui FromYamlString(String readStringFromStream)
	{
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DimensionConfigGui dimConfig = null;
       	try {
       		dimConfig = mapper.readValue(readStringFromStream, DimensionConfigGui.class);
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