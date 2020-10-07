package com.pg85.otg.configuration.settingType;

import com.pg85.otg.configuration.biome.settings.ReplaceBlocks;
import com.pg85.otg.configuration.biome.settings.WeightedMobSpawnGroup;
import com.pg85.otg.exception.InvalidConfigException;

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
    public List<ReplaceBlocks> getDefaultValue()
    {
        return Collections.emptyList();
    }

    @Override
    public List<ReplaceBlocks> read(String string) throws InvalidConfigException
    {
        return ReplaceBlocks.fromJson(string);
    }

    @Override
    public String write(List<ReplaceBlocks> groups)
    {
        return ReplaceBlocks.toJson(groups);
    }

}
