package com.pg85.otg.config.dimensions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.util.interfaces.IWorldConfig;

// Make sure isNewConfig isn't serialised
@JsonIgnoreProperties(value = { "isNewConfig" })
abstract class DimensionConfigBase
{
	// isNewConfig isn't serialised
	public boolean isNewConfig = false;
	
	// Capital letters since we'll be serialising to yaml (and we want to make it look nice)
	public String PresetName;
	int DimensionId;
	boolean ShowInWorldCreationGUI = true;
	public String Seed = WorldStandardValues.WORLD_SEED.getDefaultValue(null);
    String GameType = "Survival";
    boolean BonusChest = false;
    boolean AllowCheats = false;
    
	public DimensionConfigBase() { }
	
	DimensionConfigBase(String presetName, int dimensionId, boolean showInWorldCreationGUI)
	{
		this.PresetName = presetName;
		this.DimensionId = dimensionId;
		this.ShowInWorldCreationGUI = showInWorldCreationGUI;
	}
	
	DimensionConfigBase(String presetName, int dimensionId, boolean showInWorldCreationGUI, IWorldConfig worldConfig)
	{
		this.PresetName = presetName;
		this.DimensionId = dimensionId;
		this.ShowInWorldCreationGUI = showInWorldCreationGUI;
		this.Seed = worldConfig.getWorldSeed();
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
}
