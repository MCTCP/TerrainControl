package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;

import java.util.*;

public class BiomeGroupManager
{

    public static final int MAX_BIOME_GROUP_COUNT = 127;
    private int groupCount = 0;
    private int cumulativeGroupRarity = 0;
    private Map<String, Integer> nameToId = new LinkedHashMap<String, Integer>(4);
    private Map<Integer, BiomeGroup> idToGroup = new LinkedHashMap<Integer, BiomeGroup>(4);

    public BiomeGroupManager()
    {

    }

    public BiomeGroup registerGroup(BiomeGroup newGroup)
    {
        return __register(newGroup);
    }
    
    public BiomeGroup registerGroup(WorldConfig config, String[] args)
    {
        BiomeGroup newGroup = new BiomeGroup(config, args);
        return __register(newGroup);
    }

    public BiomeGroup registerGroup(WorldConfig config, String groupName, int size, int rarity, List<String> biomes)
    {
        BiomeGroup newGroup = new BiomeGroup(config, groupName, size, rarity, biomes);
        return __register(newGroup);
    }

    private BiomeGroup __register(BiomeGroup newGroup)
    {
        if (canAddGroup(newGroup.getName()))
        {
            Integer existing = nameToId.get(newGroup.getName());
            if (existing != null)
            {
                newGroup.setGroupid(existing);
                idToGroup.put(existing, newGroup);
            } else
            {
                nameToId.put(newGroup.getName(), ++groupCount);
                newGroup.setGroupid(groupCount);
                idToGroup.put(groupCount, newGroup);
            }
            return newGroup;
        }
        return null;
    }

    private boolean canAddGroup(String name)
    {
        if (groupCount < MAX_BIOME_GROUP_COUNT)
        {
            return true;
        }
        TerrainControl.log(LogMarker.WARN, "Biome group `{}` could not be added. Max biome group count reached.", name);
        return false;
    }

    public BiomeGroup getGroup(Integer id)
    {
        return idToGroup.get(id);
    }

    public BiomeGroup getGroup(String name)
    {
        return getGroup(nameToId.get(name));
    }

    public Collection<BiomeGroup> getGroups()
    {
        return idToGroup.values();
    }

    public int size()
    {
        return groupCount;
    }

    public boolean isEmpty()
    {
        return groupCount == 0;
    }

    public SortedMap<Integer, BiomeGroup> getGroupDepthMap(int depth)
    {
        TreeMap<Integer, BiomeGroup> map = new TreeMap<Integer, BiomeGroup>();
        this.cumulativeGroupRarity = 0;
        for (BiomeGroup group : getGroups())
        {
            if (group.getGenerationDepth() == depth)
            {
                this.cumulativeGroupRarity += group.getGroupRarity();
                map.put(this.cumulativeGroupRarity, group);
            }
        }
        if (cumulativeGroupRarity < map.size() * 100)
        {
            map.put(map.size() * 100, null);
        }
        return map;
    }

    public boolean isGroupDepthMapEmpty(int depth)
    {
        for (BiomeGroup group : getGroups())
        {
            if (group.getGenerationDepth() == depth)
            {
                return false;
            }
        }
        return true;
    }

    public SortedMap<Integer, LocalBiome> getBiomeDepthMap(int groupId, int depth)
    {
        return getGroup(groupId).getDepthMap(depth);
    }

    public boolean isBiomeDepthMapEmpty(int depth)
    {
        for (BiomeGroup group : getGroups())
        {
            if (!group.getDepthMap(depth).isEmpty())
                return false;
        }
        return true;
    }

    public static int getMaxRarityFromPossibles(Map<Integer, ?> map)
    {
        Integer[] totalRarity = map.keySet().toArray(new Integer[map.size()]);
        return totalRarity[totalRarity.length - 1];
    }
    
    public void processBiomeData(LocalWorld world){
        for (BiomeGroup entry : idToGroup.values())
        {
                entry.loadBiomeData(world);
        }
    }

}
