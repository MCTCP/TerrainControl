package com.pg85.otg.configuration.settingType;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ReplacedBlocksMatrix;
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
        return ReplacedBlocksMatrix.createEmptyMatrix(OTG.WORLD_HEIGHT);
    }

    @Override
    public ReplacedBlocksMatrix read(String string) throws InvalidConfigException
    {
        return new ReplacedBlocksMatrix(string, OTG.WORLD_HEIGHT);
    }

}
