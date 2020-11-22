package com.pg85.otg.config.dimensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.config.world.WorldConfig;

class DimensionConfigGui extends DimensionConfigBase
{
	private List<String> dimensions = new ArrayList<String>();
	private String author = null;
	private String description = null;
	private String worldPackerModName = null;
	
	public DimensionConfigGui()
	{
		super();
	}
		
	public DimensionConfigGui(String presetName, int dimensionId, boolean showInWorldCreationGUI, WorldConfig worldConfig)
	{	
		super(presetName, dimensionId, showInWorldCreationGUI, worldConfig);
		
		this.dimensions = worldConfig.dimensions;
		this.author = worldConfig.author;
		this.description = worldConfig.description;
		this.worldPackerModName = worldConfig.worldPackerModName;
	}
	
	@Override
	public DimensionConfigGui clone()
	{	
		DimensionConfigGui clone = new DimensionConfigGui();
		
		clone.dimensions = this.dimensions;
		clone.author = this.author;
		clone.description = this.description;
		clone.worldPackerModName = this.worldPackerModName;
		
		clone.PresetName = this.PresetName;
		clone.DimensionId = this.DimensionId;
		clone.ShowInWorldCreationGUI = this.ShowInWorldCreationGUI;		
		clone.Seed = this.Seed;
		clone.WorldBorderRadiusInChunks = this.WorldBorderRadiusInChunks;
		clone.PregeneratorRadiusInChunks = this.PregeneratorRadiusInChunks;
		
		clone.Settings = this.Settings.clone();
		clone.GameRules = this.GameRules.clone();
				
		return clone;
	}
	
	public static DimensionConfigGui fromYamlString(String readStringFromStream)
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