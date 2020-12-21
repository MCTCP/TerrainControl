package com.pg85.otg.config.dimensions;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Contains data for a dimension created from a preset, with world creation / O menu settings applied.
 * Saved and loaded with the world.
 * * May also be used for ModPack Configs in the future, hence the yaml code.
 * TODO: Read/Write from bytestream when saving with world data.
 */
public class DimensionConfig
{
	// Use capitals since we're serialising to yaml and want to make it look nice.
	public String PresetName;
	
	// Parameterless constructor for deserialisation
	public DimensionConfig() {}
	
	public DimensionConfig(String presetName)
	{
		this.PresetName = presetName;
	}
	
	public DimensionConfig clone()
	{
		DimensionConfig clone = new DimensionConfig(this.PresetName);
		return clone;
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
}
