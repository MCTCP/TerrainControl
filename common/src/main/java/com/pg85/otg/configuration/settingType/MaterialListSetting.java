package com.pg85.otg.configuration.settingType;

import java.util.ArrayList;
import java.util.Arrays;

import com.pg85.otg.OTG;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

/**
 * Reads and writes a material. Materials are read using
 * {@link OTG#readMaterial(String)} and written using
 * {@link LocalMaterialData#toString()}.
 *
 */
public class MaterialListSetting extends Setting<ArrayList<LocalMaterialData>>
{
    private final String[] defaultValue;
    private boolean processedMaterials;
    private LocalMaterialData[] defaultMaterials;

    public MaterialListSetting(String name, String[] defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    public ArrayList<LocalMaterialData> getDefaultValue()
    {
    	if(!processedMaterials)
    	{
    		processedMaterials = true;
	    	ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
	    	for(String defaultMaterial : defaultValue)
	    	{
	    		LocalMaterialData material = null;
				try {
					material = OTG.getEngine().readMaterial(defaultMaterial);
				} catch (InvalidConfigException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		if(material != null)
	    		{
	    			materials.add(material);
	    		}
	    	}
    	}
    	return this.defaultMaterials == null ? new ArrayList<LocalMaterialData>() : new ArrayList<LocalMaterialData>(Arrays.asList(this.defaultMaterials));
    }

    @Override
    public ArrayList<LocalMaterialData> read(String string) throws InvalidConfigException
    {
    	String[] materialNames = string.split(",");
    	ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
    	for(String materialName : materialNames)
    	{
    		LocalMaterialData material = OTG.getEngine().readMaterial(materialName.trim());
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
