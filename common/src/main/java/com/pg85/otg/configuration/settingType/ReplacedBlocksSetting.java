package com.pg85.otg.configuration.settingType;

import com.pg85.otg.configuration.biome.settings.ReplacedBlocksMatrix;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;

/**
 * Setting that handles {@link ReplacedBlocksMatrix}.
 *
 */
class ReplacedBlocksSetting extends Setting<ReplacedBlocksMatrix>
{

    ReplacedBlocksSetting(String name)
    {
        super(name);
    }

    @Override
    public ReplacedBlocksMatrix getDefaultValue()
    {
        return ReplacedBlocksMatrix.createEmptyMatrix(PluginStandardValues.WORLD_HEIGHT);
    }

    @Override
    public ReplacedBlocksMatrix read(String string) throws InvalidConfigException
    {
        return new ReplacedBlocksMatrix(string, PluginStandardValues.WORLD_HEIGHT);
    }

}
