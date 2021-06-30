package com.pg85.otg.config.settingType;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;

/**
 * Reads and writes a material. Materials are read using
 * {@link OTG#readMaterial(String)} and written using
 * {@link LocalMaterialData#toString()}.
 *
 */
public class MaterialSetting extends Setting<LocalMaterialData>
{
	private final String defaultValue;
	private boolean processedMaterial = false;
	private LocalMaterialData defaultMaterial;

	public MaterialSetting(String name, String defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	@Override
	public LocalMaterialData getDefaultValue(IMaterialReader materialReader)
	{		
		if(!processedMaterial)
		{
			processedMaterial = true;
			try {
				defaultMaterial = materialReader.readMaterial(defaultValue);
			} catch (InvalidConfigException e) {
				e.printStackTrace();
			}
		}
		return defaultMaterial;
	}

	@Override
	public LocalMaterialData read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return materialReader.readMaterial(string);
	}
}
