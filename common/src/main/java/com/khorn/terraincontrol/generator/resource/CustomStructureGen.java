package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.customobjects.CustomObjectStructure;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomStructureGen extends Resource
{
    private List<CustomObject> objects;
    private List<Double> objectChances;
    private List<String> objectNames;

    public CustomStructureGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        this.objects = new ArrayList<CustomObject>();
        this.objectNames = new ArrayList<String>();
        this.objectChances = new ArrayList<Double>();
        for (int i = 0; i < args.size() - 1; i += 2)
        {
            CustomObject object = getHolder().worldConfig.worldObjects.parseCustomObject(args.get(i));
            if (object == null || !object.canSpawnAsObject())
            {
                throw new InvalidConfigException("No custom object found with the name " + args.get(i));
            }
            if (object.getBranches(Rotation.NORTH).length == 0)
            {
                throw new InvalidConfigException("The object " + args.get(i) + " isn't a structure: it has no branches");
            }
            this.objects.add(object);
            this.objectNames.add(args.get(i));
            this.objectChances.add(readRarity(args.get(i + 1)));
        }

        // Inject ourselves in the BiomeConfig
        if (getHolder().structureGen != null)
        {
            throw new InvalidConfigException("There can only be one CustomStructure resource in each BiomeConfig");
        }
        getHolder().structureGen = this;
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Left blank, as spawnInChunk(..) already handles this.
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        // Find all structures that reach this chunk, and spawn them
        int searchRadius = world.getConfigs().getWorldConfig().maximumCustomStructureRadius;

        int currentChunkX = chunkCoord.getChunkX();
        int currentChunkZ = chunkCoord.getChunkZ();
        for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
        {
            for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
            {
                CustomObjectStructure structureStart = world.getStructureCache().getStructureStart(searchChunkX, searchChunkZ);
                if (structureStart != null)
                {
                    structureStart.spawnForChunk(chunkCoord);
                }
            }
        }
    }

    @Override
    public String toString()
    {
        if (objects.isEmpty())
        {
            return "CustomStructure()";
        }
        String output = "CustomStructure(" + this.objectNames.get(0) + "," + this.objectChances.get(0);
        for (int i = 1; i < this.objectNames.size(); i++)
        {
            output += "," + this.objectNames.get(i) + "," + this.objectChances.get(i);
        }
        return output + ")";
    }

    public CustomObjectCoordinate getRandomObjectCoordinate(Random random, int chunkX, int chunkZ)
    {
        if (this.objects.isEmpty())
        {
            return null;
        }
        for (int objectNumber = 0; objectNumber < this.objects.size(); objectNumber++)
        {
            if (random.nextDouble() * 100.0 < this.objectChances.get(objectNumber))
            {
                return this.objects.get(objectNumber).makeCustomObjectCoordinate(random, chunkX, chunkZ);
            }
        }
        return null;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        if (getClass() == other.getClass()){
            try {
                CustomStructureGen otherO = (CustomStructureGen) other;
                return otherO.objectNames.size() == this.objectNames.size() && otherO.objectNames.containsAll(this.objectNames);
            } catch (Exception ex){
                TerrainControl.log(LogMarker.WARN, ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 61 * hash + super.hashCode();
        hash = 61 * hash + (this.objects != null ? this.objects.hashCode() : 0);
        hash = 61 * hash + (this.objectChances != null ? this.objectChances.hashCode() : 0);
        hash = 61 * hash + (this.objectNames != null ? this.objectNames.hashCode() : 0);
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
        final CustomStructureGen compare = (CustomStructureGen) other;
        return (this.objects == null ? this.objects == compare.objects
                : this.objects.equals(compare.objects))
               && (this.objectChances == null ? this.objectChances == compare.objectChances
                   : this.objectChances.equals(compare.objectChances))
               && (this.objectNames == null ? this.objectNames == compare.objectNames
                   : this.objectNames.equals(compare.objectNames));
    }

    @Override
    public int getPriority()
    {
        return -41;
    }

}
