package com.khorn.terraincontrol.generator.resourcegens;


/**
 * Represents all sapling types, including mushrooms
 *
 */
public enum SaplingType 
{
    All(-1, true),
    Oak(0, true),
    Redwood(1, true),
    Birch(2, true),
    SmallJungle(3, true),
    BigJungle(4, true),
    RedMushroom(5, false),
    BrownMushroom(6, false);
    
    private final int id;
    private final boolean isTreeSapling;
    
    private SaplingType(int id, boolean isTreeSapling)
    {
        this.id = id;
        this.isTreeSapling = isTreeSapling;
    }
    
    public int getSaplingId()
    {
        return id;
    }
    
    public boolean growsTree()
    {
        return isTreeSapling;
    }
    
    private static SaplingType[] lookupList = new SaplingType[20];
    
    static 
    {
        for(SaplingType type: SaplingType.values())
        {
            if(type.id > 0)
            {
                lookupList[type.id] = type;
            }
        }
    }
    
    public static SaplingType get(String name)
    {
        try 
        {
            return get(Integer.parseInt(name));
        } catch(NumberFormatException invalidNumber)
        {
            try
            {
                return SaplingType.valueOf(name);
            } catch(IllegalArgumentException unknownType)
            {
                return null;
            }
        }
    }
    
    public static SaplingType get(int id)
    {
        if(id == -1)
        {
            return SaplingType.All;
        }
        if(id < 0 || id >= lookupList.length)
        {
            // Should never happen, unless someone uses a wrong id in the Sapling function
            return null;
        }
        return lookupList[id];
    }
}
