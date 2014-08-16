package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BiomeGroup extends ConfigFunction<WorldConfig>
{

    private int groupid;
    private String name;
    private int groupRarity;
    private int generationDepth = 0;
    private float avgTemp = 0;
    private boolean coldGroup = false;
    protected static double freezeTemp;
    private Map<String, LocalBiome> biomes = new LinkedHashMap<String, LocalBiome>(32);

    public BiomeGroup(){
        
    }
    
    public BiomeGroup(WorldConfig config, String[] args)
    {
        this.setHolder(config);
        try
        {
            this.load(Arrays.asList(args));
        } catch (InvalidConfigException ex)
        {
            Logger.getLogger(BiomeGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BiomeGroup(WorldConfig config, Setting<List<String>> defaultBiomeGroup)
    {
        this.setHolder(config);
        this.name = defaultBiomeGroup.getName();
        LinkedList<String> args = new LinkedList<String>(defaultBiomeGroup.getDefaultValue());
        args.addFirst(name);
        try
        {
            load(args);
        } catch (InvalidConfigException ex)
        {
            Logger.getLogger(BiomeGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BiomeGroup(WorldConfig config, String groupName, int size, int rarity, List<String> biomes)
    {
        this.setHolder(config);
        this.name = groupName;
        this.generationDepth = size;
        this.groupRarity = rarity;
        for (String biome : filterBiomes(biomes))
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
        for (String biome : filterBiomes(readBiomes(args, 3)))
        {
            this.biomes.put(biome, null);
        }
    }

    public void loadBiomeData(LocalWorld world)
    {
        for (String biome : this.biomes.keySet())
        {
            LocalBiome localBiome = world.getBiomeByName(biome);
            this.avgTemp += localBiome.getBiomeConfig().biomeTemperature;
            this.biomes.put(biome, localBiome);
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

    protected ArrayList<String> filterBiomes(List<String> biomes)
    {
        ArrayList<String> output = new ArrayList<String>(32);
        Set<String> customBiomes = this.getHolder().customBiomeGenerationIds.keySet();
        for (String key : biomes)
        {
            key = key.trim();
            if (customBiomes.contains(key))
            {
                output.add(key);
                continue;
            }

            if (DefaultBiome.Contain(key))
                output.add(key);

        }
        return output;
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
        List<String> readBiomes = new LinkedList<String>();
        for (ListIterator<String> it = strings.listIterator(start); it.hasNext();)
        {
            readBiomes.add(it.next());
        }
        return readBiomes;
    }

    public String getName()
    {
        return name;
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
        {
            if (biome.getValue().getBiomeConfig().biomeSize == depth)
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

}
