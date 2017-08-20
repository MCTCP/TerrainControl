package com.khorn.terraincontrol.configuration.settingType;

import java.util.ArrayList;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

/**
 * Reads and writes a material. Materials are read using
 * {@link TerrainControl#readMaterial(String)} and written using
 * {@link LocalMaterialData#toString()}.
 *
 */
public class MaterialListSetting extends Setting<ArrayList<LocalMaterialData>>
{
    private final DefaultMaterial[] defaultValue;

    public MaterialListSetting(String name, DefaultMaterial[] defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    public ArrayList<LocalMaterialData> getDefaultValue()
    {
    	ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
    	for(DefaultMaterial defaultMaterial : defaultValue)
    	{
    		materials.add(TerrainControl.toLocalMaterialData(defaultMaterial, 0));
    	}
        return materials;
    }

    @Override
    public ArrayList<LocalMaterialData> read(String string) throws InvalidConfigException
    {
    	String[] materialNames = string.split(",");
    	ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
    	for(String materialName : materialNames)
    	{
    		LocalMaterialData material = TerrainControl.readMaterial(materialName.trim());
			materials.add(material);
    	}
        return materials;
    }
    
    @Override
    public String write(ArrayList<LocalMaterialData> value)
    {
        return StringHelper.join(value, ", ");
    }
}
