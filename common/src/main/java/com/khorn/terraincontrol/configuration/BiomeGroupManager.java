package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;

import java.util.*;

/**
 * Manages a collection of biome groups that are accesible by their name and
 * id.
 *
 */
public final class BiomeGroupManager
{

    public static final int MAX_BIOME_GROUP_COUNT = 127;
    private int cumulativeGroupRarity = 0;
    private Map<String, BiomeGroup> nameToGroup = new LinkedHashMap<String, BiomeGroup>(4);
    private Map<Integer, BiomeGroup> idToGroup = new LinkedHashMap<Integer, BiomeGroup>(4);

    public BiomeGroupManager()
    {

    }

    /**
     * Registers a new group. If the group could not be added (for example
     * because the maximum amount of biomes has been reached) a message is
     * logged and the group is not registered.
     * @param newGroup The group to register.
     */
    public void registerGroup(BiomeGroup newGroup)
    {
        if (isRoomForMoreGroups())
        {
            BiomeGroup existingWithSameName = nameToGroup.get(newGroup.getName());
            if (existingWithSameName != null)
            {
                TerrainControl.log(LogMarker.WARN, "Two biome groups have the same name \"{}\". Removing the second one.",
                        newGroup.getName());
                TerrainControl.printStackTrace(LogMarker.WARN, new Exception());
            } else
            {
                int newGroupId = getNextGroupId();
                newGroup.setGroupId(newGroupId);

                nameToGroup.put(newGroup.getName(), newGroup);
                idToGroup.put(newGroupId, newGroup);
            }
        } else
        {
            TerrainControl.log(LogMarker.WARN, "Biome group \"{}\" could not be added. Max biome group count reached.", newGroup.getName());
        }
    }

    /**
     * Gets the next group id. This group id is based on which group ids are
     * currently in used.
     * @return The next group id.
     */
    private int getNextGroupId()
    {
        // Adding +1 ensures that the id 0 will never be in use. The id 0
        // seems to be used as a null value by the biome generator
        return getGroupCount() + 1;
    }

    /**
     * Checks if the next group id will still fit in the group limit.
     * @return True if the next group id will fit, false otherwise.
     */
    private boolean isRoomForMoreGroups()
    {
        return getNextGroupId() < MAX_BIOME_GROUP_COUNT;
    }

    /**
     * Gets the group with the given group id.
     * @param groupId Id of the group.
     * @return The group, or null if no such group exists.
     */
    public BiomeGroup getGroupById(int groupId)
    {
        return idToGroup.get(groupId);
    }

    /**
     * Gets the group with the given name.
     * @param name Name of the group, case sensitive.
     * @return The group.
     */
    public BiomeGroup getGroupByName(String name)
    {
        return nameToGroup.get(name);
    }

    /**
     * Gets all groups.
     * @return All groups.
     */
    public Collection<BiomeGroup> getGroups()
    {
        return idToGroup.values();
    }

    /**
     * Gets the amount of groups currently registered. Calling this method is
     * equivalent to calling {@code getGroups().size()}.
     * @return The amount of groups.
     */
    public int getGroupCount()
    {
        return idToGroup.size();
    }

    /**
     * Gets whether there are no groups registered. Calling this method is
     * equivalent to calling {@code getGroups().isEmpty()}.
     * @return True if there are no groups registered, false if there are
     * groups registered.
     */
    public boolean hasNoGroups()
    {
        return idToGroup.isEmpty();
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
        return getGroupById(groupId).getDepthMap(depth);
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

    public void processBiomeData(LocalWorld world)
    {
        for (BiomeGroup entry : idToGroup.values())
        {
            entry.processBiomeData(world);
        }
    }

    /**
     * Filters all biome names in the groups. Invalid biomes names will be
     * removed.
     * @param customBiomeNames Set of all custom biomes in the world.
     */
    public void filterBiomes(Set<String> customBiomeNames)
    {
        for (Iterator<BiomeGroup> it = idToGroup.values().iterator(); it.hasNext();)
        {
            BiomeGroup group = it.next();
            group.filterBiomes(customBiomeNames);
            if (group.hasNoBiomes())
            {
                it.remove();
            }
        }
    }

}
