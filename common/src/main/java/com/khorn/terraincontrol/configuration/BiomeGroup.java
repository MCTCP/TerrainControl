package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.util.*;
import java.util.Map.Entry;

public final class BiomeGroup extends ConfigFunction<WorldConfig>
{

    private int groupid;
    private String name;
    private int groupRarity;
    private int generationDepth = 0;
    private float avgTemp = 0;
    private boolean coldGroup = false;
    protected static double freezeTemp;
    private Map<String, LocalBiome> biomes = new LinkedHashMap<String, LocalBiome>(32);

    /**
     * Empty constructor, needed for reading this group.
     * @see #BiomeGroup(WorldConfig, String, int, int, List) Constructor to
     * properly initialize this biome group manually.
     */
    public BiomeGroup()
    {

    }

    /**
     * Creates a new biome group based on the given settings. Using these
     * settings makes sure that:
     * <ul>
     *  <li>values from old configs are read correctly.</li>
     *  <li>proper default settings are used, as specified by those settings.</li>
     * </ul>
     * @param config     WorldConfig this biome group will be in.
     * @param biomeNames Setting used to read the names of the biomes in this
     *                   group. The name of this setting will be used as the
     *                   name of the group.
     * @param size       Setting used for reading the size of the group.
     * @param rarity     Setting used for reading the rarity of the group.
     * @return The group.
     */
    public static BiomeGroup ofSettings(WorldConfig config, Setting<List<String>> biomeNames, Setting<Integer> size, Setting<Integer> rarity)
    {
        String groupName = biomeNames.getName();
        List<String> biomeNameValues = config.readSettings(biomeNames);
        int sizeValue = config.readSettings(size);
        int rarityValue = config.readSettings(rarity);
        return new BiomeGroup(config, groupName, sizeValue, rarityValue, biomeNameValues);
    }

    /**
     * Creates a new <code>BiomeGroup</code>.
     * @param config    WorldConfig this biome group is part of.
     * @param groupName The name of this group.
     * @param size      Size value of this biome group.
     * @param rarity    Rarity value of this biome group.
     * @param biomes    List of names of the biomes that spawn in this group.
     */
    public BiomeGroup(WorldConfig config, String groupName, int size, int rarity, List<String> biomes)
    {
        this.setHolder(config);
        this.name = groupName;
        this.generationDepth = size;
        this.groupRarity = rarity;
        for (String biome : biomes)
        {
            this.biomes.put(biome, null);
        }
    }

    @Override
    protected void load(List<String> args) throws InvalidConfigException
    {
        //>>	Must have atleast a GroupName and a Biome that belongs to it
        assureSize(4, args);
        this.name = args.get(0);
        this.generationDepth = readInt(args.get(1), 0, getHolder().GenerationDepth);
        this.groupRarity = readInt(args.get(2), 1, Integer.MAX_VALUE);
        for (String biome : readBiomes(args, 3))
        {
            this.biomes.put(biome, null);
        }
    }

    public void loadBiomeData(LocalWorld world)
    {
        for (Iterator<Entry<String, LocalBiome>> it = this.biomes.entrySet().iterator(); it.hasNext();)
        {
            Entry<String, LocalBiome> entry = it.next();
            String biomeName = entry.getKey();
            LocalBiome localBiome = world.getBiomeByName(biomeName);
            this.avgTemp += localBiome.getBiomeConfig().biomeTemperature;
            entry.setValue(localBiome);
        }
        this.avgTemp /= this.biomes.size();
    }

    @Override
    public Class<WorldConfig> getHolderType()
    {
        return WorldConfig.class;
    }

    @Override
    public String makeString()
    {
        return "BiomeGroup(" + name + ", " + generationDepth + ", " + groupRarity + ", " + StringHelper.join(biomes.keySet(), ", ") + ")";
    }

    /**
     * Reads all biomes from the start position until the end of the
     * list.
     * <p/>
     * @param strings The input strings.
     * @param start   The position to start. The first element in the list
     *                has index 0, the last one size() - 1.
     * <p/>
     * @return All biome names.
     * <p/>
     * @throws InvalidConfigException If one of the elements in the list is
     *                                not a valid block id.
     */
    protected List<String> readBiomes(List<String> strings, int start) throws InvalidConfigException
    {
        return new ArrayList<String>(strings.subList(start, strings.size()));
    }

    public String getName()
    {
        return name;
    }

    /**
     * Filters the biomes in this group, removing all biomes that have an
     * unrecognized name.
     * @param customBiomeNames Set of known custom biomes.
     */
    void filterBiomes(Set<String> customBiomeNames)
    {
        for (Iterator<String> it = this.biomes.keySet().iterator(); it.hasNext();)
        {
            String biomeName = it.next();
            if (DefaultBiome.Contain(biomeName) || customBiomeNames.contains(biomeName))
            {
                continue;
            }
            // Invalid biome name, remove
            TerrainControl.log(LogMarker.WARN, "Invalid biome name {} in biome group {}", biomeName, this.name);
            it.remove();
        }
    }

    public List<String> getBiomes()
    {
        return Collections.unmodifiableList(new ArrayList<String>(biomes.keySet()));
    }

    public boolean contains(String name)
    {
        for (String biome : this.biomes.keySet())
        {
            if (biome.equals(name))
                return true;
        }
        return false;
    }

    public void setGroupid(int groupid)
    {
        if (groupid <= BiomeGroupManager.MAX_BIOME_GROUP_COUNT)
        {
            this.groupid = groupid;
        } else
        {
            this.groupid = -1;
        }
    }

    public int getGroupid()
    {
        return this.groupid;
    }

    public boolean isColdGroup()
    {
        return this.coldGroup || this.avgTemp < 0.33;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<WorldConfig> other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public SortedMap<Integer, LocalBiome> getDepthMap(int depth)
    {
        int cumulativeBiomeRarity = 0;
        TreeMap<Integer, LocalBiome> map = new TreeMap<Integer, LocalBiome>();
        for (Entry<String, LocalBiome> biome : this.biomes.entrySet())
        {                                                           //>>	When depth given is negative, include all biomes in group
            if (biome.getValue().getBiomeConfig().biomeSize == depth || depth < 0)
            {
                cumulativeBiomeRarity += biome.getValue().getBiomeConfig().biomeRarity;
                map.put(cumulativeBiomeRarity, biome.getValue());
            }
        }
//        if (cumulativeBiomeRarity < map.size() * 100)
//        {
//            map.put(map.size() * 100, null);
//        }
        return map;
    }

    public int getGroupRarity()
    {
        return groupRarity;
    }

    public void setGroupRarity(int newRarity)
    {
        this.groupRarity = newRarity;
    }

    public int getGenerationDepth()
    {
        return generationDepth;
    }

    /**
     * Gets whether this group has any biomes.
     * @return True if the group has no biomes and is thus empty, false
     * if the group has biomes.
     */
    public boolean hasNoBiomes()
    {
        return biomes.isEmpty();
    }

}
