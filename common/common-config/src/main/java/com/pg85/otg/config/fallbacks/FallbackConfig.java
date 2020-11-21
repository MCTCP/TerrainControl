package com.pg85.otg.config.fallbacks;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;

public class FallbackConfig extends ConfigFile
{
	private List<BlockFallback> fallbacks = new ArrayList<BlockFallback>();

	public FallbackConfig(SettingsMap settingsReader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{

		super(settingsReader.getName());

		this.renameOldSettings(settingsReader, logger, materialReader);
		this.readConfigSettings(settingsReader, biomeResourcesManager, spawnLog, logger, materialReader);

		this.correctSettings(true, logger);
	}

	@Override
	protected void writeConfigSettings(SettingsMap writer)
	{
		writer.bigTitle("The Block Fallback File",
				"Designates block replacements when the original block cannot be found. (Usually occurs when missing other mods)",
				"Usage: BlockFallback(SourceBlockName,ReplacementBlockName[,ReplacementBlockName[,...]])",
				"Fallback blocks are checked with a left - right priority, the first valid block found is the one that will be used.");

		writer.addConfigFunctions(fallbacks);
	}

	@Override
	protected void readConfigSettings(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{		
		for (ConfigFunction<FallbackConfig> res : reader.getConfigFunctions(this, false, biomeResourcesManager, spawnLog, logger, materialReader))
		{
			if (res != null && res instanceof BlockFallback)
			{
				fallbacks.add((BlockFallback) res);
			}
		}

	}

	@Override
	protected void correctSettings(boolean logWarnings, ILogger logger)
	{
		// Nothing to correct the moment
	}

	@Override
	protected void renameOldSettings(SettingsMap reader, ILogger logger, IMaterialReader materialReader)
	{
		// Nothing to rename at the moment
	}

    public List<BlockFallback> getAllFallbacks()
    {
        return fallbacks;
    }
}
