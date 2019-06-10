package com.pg85.otg.configuration.settingType;

import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.LocalMaterialData;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

/**
 * Reads and writes a material. Materials are read using
 * {@link OTG#readMaterial(String)} and written using
 * {@link LocalMaterialData#toString()}.
 *
 */
public class MaterialSetting extends Setting<LocalMaterialData>
{
    private final DefaultMaterial defaultValue;

    public MaterialSetting(String name, DefaultMaterial defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    public LocalMaterialData getDefaultValue()
    {
        return OTG.toLocalMaterialData(defaultValue, 0);
    }

    @Override
    public LocalMaterialData read(String string) throws InvalidConfigException
    {
    	LocalMaterialData material = OTG.readMaterial(string);
        return material;
    }
}
