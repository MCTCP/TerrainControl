package com.pg85.otg.config.settingType;

import com.pg85.otg.OTG;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.exception.InvalidConfigException;

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
    public LocalMaterialData getDefaultValue()
    {
    	if(!processedMaterial)
    	{
    		processedMaterial = true;
	        try {
				defaultMaterial = OTG.getEngine().readMaterial(defaultValue);
			} catch (InvalidConfigException e) {
				e.printStackTrace();
			}
    	}
        return defaultMaterial;
    }

    @Override
    public LocalMaterialData read(String string) throws InvalidConfigException
    {
    	return OTG.getEngine().readMaterial(string);
    }
}
