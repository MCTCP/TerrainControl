package com.pg85.otg.configuration.settingType;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

/**
 * Reads and writes a material. Materials are read using
 * {@link OTG#readMaterial(String)} and written using
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
    		materials.add(MaterialHelper.toLocalMaterialData(defaultMaterial, 0));
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
    		LocalMaterialData material = MaterialHelper.readMaterial(materialName.trim());
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
