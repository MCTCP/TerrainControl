package com.pg85.otg.config.dimensions;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import com.pg85.otg.OTG;
import com.pg85.otg.config.standard.PluginStandardValues;

public class ModPackConfigManager
{
	private HashMap<String, DimensionsConfig> defaultConfigs = new HashMap<String, DimensionsConfig>();

	public ModPackConfigManager(Path otgRootFolder)
	{
		indexModPackConfigs(otgRootFolder);
	}

	private void indexModPackConfigs(Path otgRootFolder)
	{
		File configDir = new File(otgRootFolder.toFile() + File.separator + PluginStandardValues.MODPACK_CONFIGS_FOLDER + File.separator);
		if(configDir.exists())
		{
			for(File f : configDir.listFiles())
			{
				DimensionsConfig forgeWorldConfig = DimensionsConfig.defaultConfigfromFile(f, otgRootFolder, true);
				if(forgeWorldConfig != null)
				{
					// If there's multiple configs targeting the same preset for their overworld,
					// only add the first.
					if(!this.defaultConfigs.containsKey(forgeWorldConfig.Overworld.PresetName))
					{
						this.defaultConfigs.put(forgeWorldConfig.Overworld.PresetName, forgeWorldConfig);
					}
				}
			}
		}
	}

	public DimensionsConfig getModPackConfig(String presetName)
	{
		return this.defaultConfigs.get(presetName);
	}

	public void setAllModPackConfigs(ArrayList<DimensionsConfig> modPackConfigs)
	{
		this.defaultConfigs = new HashMap<String, DimensionsConfig>();
		for(DimensionsConfig forgeWorldConfig : modPackConfigs)
		{
			// If there's multiple configs targeting the same preset for their overworld,
			// only add the first.
			if(!this.defaultConfigs.containsKey(forgeWorldConfig.Overworld.PresetName))
			{
				this.defaultConfigs.put(forgeWorldConfig.Overworld.PresetName, forgeWorldConfig);
			}
		}	
	}
	
	public ArrayList<DimensionsConfig> getAllModPackConfigs()
	{
		return new ArrayList<DimensionsConfig>(this.defaultConfigs.values());
	}

	public HashMap<Integer, String> getReservedDimIds()
	{
		HashMap<Integer, String> reservedIds = new HashMap<Integer, String>();
		String overworldPresetName = OTG.getDimensionsConfig().Overworld.PresetName;
		for(DimensionsConfig dimsConfig : this.defaultConfigs.values())
		{
			if((overworldPresetName == null && dimsConfig.Overworld.PresetName == null) || (overworldPresetName != null && overworldPresetName.equals(dimsConfig.Overworld.PresetName)))
			{
				for(DimensionConfig dimConfig1 : dimsConfig.Dimensions)
				{
					// The modpack config has reserved this id for one of its dimensions.
					if(dimConfig1.DimensionId != 0)
					{
						reservedIds.put(Integer.valueOf(dimConfig1.DimensionId), dimConfig1.PresetName);
					}
				}
			}
		}
		return reservedIds;
	}
}
