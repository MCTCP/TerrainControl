package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

/**
 * Reads and writes a material. Materials are read using
 * {@link TerrainControl#readMaterial(String)} and written using
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
        return TerrainControl.toLocalMaterialData(defaultValue, 0);
    }

    @Override
    public LocalMaterialData read(String string) throws InvalidConfigException
    {
        return TerrainControl.readMaterial(string);
    }

}
