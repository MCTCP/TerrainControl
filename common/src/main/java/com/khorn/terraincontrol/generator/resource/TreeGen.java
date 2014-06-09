package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeGen extends Resource
{
    private List<CustomObject> trees;
    private List<String> treeNames;
    private List<Integer> treeChances;

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        for (int i = 0; i < frequency; i++)
        {
            for (int treeNumber = 0; treeNumber < trees.size(); treeNumber++)
            {
                if (random.nextInt(100) < treeChances.get(treeNumber))
                {
                    int x = chunkCoord.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
                    int z = chunkCoord.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);
                    if (trees.get(treeNumber).spawnAsTree(world, random, x, z))
                    {
                        // Success, on to the next tree!
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(3, args);

        frequency = readInt(args.get(0), 1, 100);

        trees = new ArrayList<CustomObject>();
        treeNames = new ArrayList<String>();
        treeChances = new ArrayList<Integer>();

        for (int i = 1; i < args.size() - 1; i += 2)
        {
            CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(args.get(i), getHolder().worldConfig);
            if (object == null)
            {
                throw new InvalidConfigException("Custom object " + args.get(i) + " not found!");
            }
            if (!object.canSpawnAsTree())
            {
                throw new InvalidConfigException("Custom object " + args.get(i) + " is not a tree!");
            }
            trees.add(object);
            treeNames.add(args.get(i));
            treeChances.add(readInt(args.get(i + 1), 1, 100));
        }
    }

    @Override
    public String makeString()
    {
        String output = "Tree(" + frequency;
        for (int i = 0; i < treeNames.size(); i++)
        {
            output += "," + treeNames.get(i) + "," + treeChances.get(i);
        }
        return output + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Left blank, as process() already handles this
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        if (getClass() == other.getClass()){
            try {
                TreeGen otherO = (TreeGen) other;
                return otherO.treeNames.size() == this.treeNames.size() && otherO.treeNames.containsAll(this.treeNames);
            } catch (Exception ex){
                TerrainControl.log(LogMarker.WARN, ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 53 * hash + super.hashCode();
        hash = 53 * hash + (this.trees != null ? this.trees.hashCode() : 0);
        hash = 53 * hash + (this.treeNames != null ? this.treeNames.hashCode() : 0);
        hash = 53 * hash + (this.treeChances != null ? this.treeChances.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final TreeGen compare = (TreeGen) other;
        return (this.treeNames == null ? this.treeNames == compare.treeNames
                : this.treeNames.equals(compare.treeNames))
               && (this.treeNames == null ? this.treeNames == compare.treeNames
                   : this.treeNames.equals(compare.treeNames))
               && (this.treeChances == null ? this.treeChances == compare.treeChances
                   : this.treeChances.equals(compare.treeChances));
    }
    
    
}
