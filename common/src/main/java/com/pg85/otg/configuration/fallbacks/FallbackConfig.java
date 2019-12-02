package com.pg85.otg.configuration.fallbacks;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.configuration.ConfigFile;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.io.SettingsMap;

public class FallbackConfig extends ConfigFile {

	private List<BlockFallback> fallbacks = new ArrayList<BlockFallback>();

	public FallbackConfig(SettingsMap settingsReader) {

		super(settingsReader.getName());

		this.renameOldSettings(settingsReader);
		this.readConfigSettings(settingsReader);

		this.correctSettings(true);
	}

	@Override
	protected void writeConfigSettings(SettingsMap writer) {
		writer.bigTitle("The Block Fallback File",
				"Designates block replacements when the original block cannot be found. (Usually occurs when missing other mods)",
				"Usage: BlockFallback(SourceBlockName,ReplacementBlockName[,ReplacementBlockName[,...]])",
				"Fallback blocks are checked with a left - right priority, the first valid block found is the one that will be used.");

		writer.addConfigFunctions(fallbacks);
	}

	@Override
	protected void readConfigSettings(SettingsMap reader) {
		for (ConfigFunction<FallbackConfig> res : reader.getConfigFunctions(this, false)) {
			if (res != null && res instanceof BlockFallback) {
				fallbacks.add((BlockFallback) res);
			}
		}

	}

	@Override
	protected void correctSettings(boolean logWarnings) {
		// Nothing to correct the moment

	}

	@Override
	protected void renameOldSettings(SettingsMap reader) {
		// Nothing to rename at the moment

	}

    public List<BlockFallback> getAllFallbacks()
    {
        return fallbacks;
    }

}
