package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultMobType;

/**
 * This class holds data for a bukkit nms.BiomeMeta class.
 * The name does not match but ours make more sense.
 */
public class WeightedMobSpawnGroup
{
    // -------------------------------------------- //
    // FIELDS
    // -------------------------------------------- //
    protected String mob = null;
    public String getMobName() { return this.mob; }
    public DefaultMobType getDefaultMobType()
    {
        DefaultMobType ret = DefaultMobType.fromName(this.mob);
        if (ret == null)
        {
            System.err.println("Invalid mob name: "+this.mob);
        }
        return ret;
    }
    public void setMobName(String value) { this.mob = value; }
    
    protected int weight = 0;
    public int getWeight() { return this.weight; }
    public void setWeight(int value) { this.weight = value; }
    
    protected int min = 0;
    public int getMin() { return this.min; }
    public void setMin(int value) { this.min = value; }
    
    protected int max = 0;
    public int getMax() { return this.max; }
    public void setMax(int value) { this.max = value; }
    
    // -------------------------------------------- //
    // CONSTRUCTORS
    // -------------------------------------------- //
    
    public WeightedMobSpawnGroup()
    {
        // Yeah this is empty. But GSON needs a noarg constructor.
    }
    
    public WeightedMobSpawnGroup(String mobName, int weight, int min, int max)
    {
        this.mob = mobName;
        this.weight = weight;
        this.min = min;
        this.max = max;
    }
}
