package com.pg85.otg.presets;

import com.pg85.otg.OTG;
import com.pg85.otg.util.interfaces.IPresetNameProvider;

public class PresetNameProvider implements IPresetNameProvider
{
	@Override
	public String getPresetName(String worldName)
	{
		// If this dim's name is the same as the preset worldname then this is an OTG overworld
		if(worldName.equals("overworld") || worldName.equals(OTG.getDimensionsConfig().WorldName))
    	{
    		return OTG.getDimensionsConfig().Overworld.PresetName;	
    	} else {
    		// If this is an OTG dim other than the overworld then the world name will always match the preset name
    		return worldName;
    	}
	}
}
