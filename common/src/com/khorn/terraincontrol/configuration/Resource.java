package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.customobjects.CustomObjectCompiled;
import com.khorn.terraincontrol.generator.resourcegens.ResourceType;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;

public class Resource
{
    public ResourceType Type;
    public int MinAltitude;
    public int MaxAltitude;
    public int MinSize;
    public int MaxSize;
    public int BlockId;
    public int BlockData;
    public int[] SourceBlockId = new int[0];
    public int Frequency;
    public int Rarity;
    public TreeType[] TreeTypes = new TreeType[0];
    public int[] TreeChances = new int[0];

    public CustomObjectCompiled[] CUObjects = new CustomObjectCompiled[0];
    public String[] CUObjectsNames = new String[0];


    public Resource(ResourceType type)
    {
        Type = type;
    }

    public Resource(ResourceType type, int blockId, int blockData, int size, int frequency, int rarity, int minAltitude, int maxAltitude, int[] sourceBlockIds)
    {
        this.Type = type;
        this.BlockId = blockId;
        this.BlockData = blockData;
        this.MaxSize = size;
        this.Frequency = frequency;
        this.Rarity = rarity;
        this.MinAltitude = minAltitude;
        this.MaxAltitude = maxAltitude;
        this.SourceBlockId = sourceBlockIds;
    }

    public Resource(ResourceType type, int minSize, int maxSize, int frequency, int rarity, int minAltitude, int maxAltitude)
    {
        this.Type = type;
        this.MaxSize = maxSize;
        this.MinSize = minSize;
        this.Frequency = frequency;
        this.Rarity = rarity;
        this.MinAltitude = minAltitude;
        this.MaxAltitude = maxAltitude;
    }

    public Resource(ResourceType type, int blockId, int frequency, int rarity, int minAltitude, int maxAltitude)
    {
        this.Type = type;
        this.BlockId = blockId;
        this.Frequency = frequency;
        this.Rarity = rarity;
        this.MinAltitude = minAltitude;
        this.MaxAltitude = maxAltitude;
    }

    public Resource(ResourceType type, int frequency, TreeType[] types, int[] treeChances)
    {
        this.Type = type;
        this.Frequency = frequency;
        if (types != null)
        {
            this.TreeTypes = types;
            this.TreeChances = treeChances;
        }
    }

    public boolean CheckSourceId(int blockId)
    {
        for (int id : this.SourceBlockId)
            if (blockId == id)
                return true;
        return false;
    }


    public String BlockIdToName(int id)
    {
        DefaultMaterial material = DefaultMaterial.getMaterial(id);
        if (material != DefaultMaterial.UNKNOWN_BLOCK)
            return material.name();
        else
            return Integer.toString(id);
    }

}
