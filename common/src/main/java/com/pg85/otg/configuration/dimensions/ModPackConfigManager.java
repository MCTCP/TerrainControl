package com.pg85.otg.configuration.dimensions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.pg85.otg.configuration.standard.PluginStandardValues;

public class ModPackConfigManager
{
	private HashMap<String, DimensionsConfig> defaultConfigs = new HashMap<String, DimensionsConfig>();
	
	public ModPackConfigManager(File otgRootFolder)
	{
		indexModPackConfigs(otgRootFolder);
	}
	
	private void indexModPackConfigs(File otgRootFolder)
	{
		// TODO: Doesn't Forge provide a better way of getting the config dir?
		File configDir = new File(otgRootFolder.getParentFile().getParentFile() + File.separator + "config" + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator);
		if(configDir.exists())
		{
			for(File f : configDir.listFiles())
			{
				DimensionsConfig forgeWorldConfig = DimensionsConfig.defaultConfigfromFile(f, otgRootFolder);
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

	public ArrayList<DimensionsConfig> getAllModPackConfigs()
	{
		return new ArrayList<DimensionsConfig>(this.defaultConfigs.values());
	}

	public HashMap<Integer, String> getReservedDimIds()
	{
		new HashMap<Integer, String>();
		HashMap<Integer, String> reservedIds = new HashMap<Integer, String>();
		for(DimensionsConfig dimsConfig : this.defaultConfigs.values())
		{
			for(DimensionConfig dimConfig1 : dimsConfig.Dimensions)
			{
				// The modpack config has reserved this id for one of its dimensions.
				if(dimConfig1.DimensionId != 0)
				{
					// If there's multiple modpacks reserving a dim id for the same preset, only use the first.
					if(!reservedIds.containsKey(Integer.valueOf(dimConfig1.DimensionId)))
					{
						reservedIds.put(Integer.valueOf(dimConfig1.DimensionId), dimConfig1.PresetName);
					}
				}
			}
		}
		return reservedIds;
	}
}
