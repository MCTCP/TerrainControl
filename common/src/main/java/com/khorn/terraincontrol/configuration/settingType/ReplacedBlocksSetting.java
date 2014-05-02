package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ReplacedBlocksMatrix;
import com.khorn.terraincontrol.exception.InvalidConfigException;

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
        return ReplacedBlocksMatrix.createEmptyMatrix(TerrainControl.WORLD_HEIGHT);
    }

    @Override
    public ReplacedBlocksMatrix read(String string) throws InvalidConfigException
    {
        return new ReplacedBlocksMatrix(string, TerrainControl.WORLD_HEIGHT);
    }

}
