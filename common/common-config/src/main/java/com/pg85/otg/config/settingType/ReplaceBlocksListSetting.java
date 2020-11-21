package com.pg85.otg.config.settingType;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.biome.ReplaceBlocks;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.interfaces.IMaterialReader;

import java.util.Collections;
import java.util.List;

/**
 * Reads and writes a list of mobs. Mobs are read using
 * {@link WeightedMobSpawnGroup#fromJson(String)} and written using
 * {@link WeightedMobSpawnGroup#toJson(List)}.
 *
 */
class ReplaceBlocksListSetting extends Setting<List<ReplaceBlocks>>
{
    ReplaceBlocksListSetting(String name)
    {
        super(name);
    }

    @Override
    public List<ReplaceBlocks> getDefaultValue(IMaterialReader materialReader)
    {
        return Collections.emptyList();
    }

    @Override
    public List<ReplaceBlocks> read(String string, IMaterialReader materialReader) throws InvalidConfigException
    {
        return ReplaceBlocks.fromJson(string);
    }

    @Override
    public String write(List<ReplaceBlocks> groups)
    {
        return ReplaceBlocks.toJson(groups);
    }

}
