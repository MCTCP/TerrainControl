package com.pg85.otg.config.settingType;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.interfaces.IMaterialReader;

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
	public ReplacedBlocksMatrix getDefaultValue(IMaterialReader materialReader)
	{
		return ReplacedBlocksMatrix.createEmptyMatrix(Constants.WORLD_HEIGHT, materialReader);
	}

	@Override
	public ReplacedBlocksMatrix read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return new ReplacedBlocksMatrix(string, Constants.WORLD_HEIGHT, materialReader);
	}
}
