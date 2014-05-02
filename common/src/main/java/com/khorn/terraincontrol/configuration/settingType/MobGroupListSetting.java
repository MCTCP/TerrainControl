package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.Collections;
import java.util.List;

/**
 * Reads and writes a list of mobs. Mobs are read using
 * {@link WeightedMobSpawnGroup#fromJson(String)} and written using
 * {@link WeightedMobSpawnGroup#toJson(List).
 *
 */
class MobGroupListSetting extends Setting<List<WeightedMobSpawnGroup>>
{

    MobGroupListSetting(String name)
    {
        super(name);
    }

    @Override
    public List<WeightedMobSpawnGroup> getDefaultValue()
    {
        return Collections.emptyList();
    }

    @Override
    public List<WeightedMobSpawnGroup> read(String string) throws InvalidConfigException
    {
        return WeightedMobSpawnGroup.fromJson(string);
    }

    @Override
    public String write(List<WeightedMobSpawnGroup> groups)
    {
        return WeightedMobSpawnGroup.toJson(groups);
    }

}
