package com.pg85.otg.config.settingType;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;

/**
 * Setting that handles {@link ReplaceBlockMatrix}.
 *
 */
class ReplacedBlocksSetting extends Setting<ReplaceBlockMatrix>
{

	ReplacedBlocksSetting(String name)
	{
		super(name);
	}

	@Override
	public ReplaceBlockMatrix getDefaultValue(IMaterialReader materialReader)
	{
		return ReplaceBlockMatrix.createEmptyMatrix(Constants.WORLD_HEIGHT, materialReader);
	}

	@Override
	public ReplaceBlockMatrix read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return new ReplaceBlockMatrix(string, Constants.WORLD_HEIGHT, materialReader);
	}
}
