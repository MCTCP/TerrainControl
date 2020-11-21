package com.pg85.otg.customobjects.resource;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.SaplingType;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents a custom sapling generator. This generator can grow vanilla trees,
 * but also custom objects.
 *
 */
public class SaplingGen extends ConfigFunction<IBiomeConfig>
{
    private static final Map<Rotation, int[]> TREE_OFFSET;
    static
    {
        TREE_OFFSET = new EnumMap<Rotation, int[]>(Rotation.class);
        TREE_OFFSET.put(Rotation.NORTH, new int[] {0, 0});
        TREE_OFFSET.put(Rotation.EAST, new int[] {1, 0});
        TREE_OFFSET.put(Rotation.SOUTH, new int[] {1, 1});
        TREE_OFFSET.put(Rotation.WEST, new int[] {0, 1});
    }

    public SaplingType saplingType;
    // wideTrunk and saplingMaterial are used by custom saplings only
    public LocalMaterialData saplingMaterial;
    public boolean wideTrunk;
    private List<Double> treeChances;
    private List<String> treeNames;
    private List<CustomObject> trees;
    private String worldName;
    private String biomeName;
    private boolean treesLoaded = false;
    
    public SaplingGen(String worldName, IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        assureSize(3, args);

        saplingType = SaplingType.get(args.get(0));

        if (saplingType == SaplingType.Custom)
        {
            try {
            	saplingMaterial = materialReader.readMaterial(args.get(1));
            } catch (InvalidConfigException e) {
                logger.log(LogMarker.ERROR, "Invalid custom sapling configuration! Syntax: Sapling(Custom, material, widetrunk, TreeName, TreeChance, ...)");
            }
        }
        if (saplingType == null && saplingMaterial == null)
        {
            throw new InvalidConfigException("Unknown sapling type " + args.get(0));
        }

        trees = new ArrayList<CustomObject>();
        treeNames = new ArrayList<String>();
        treeChances = new ArrayList<Double>();
        biomeName = biomeConfig.getName();
        int ind = 1;
        if (saplingType == SaplingType.Custom)
        {
            ind = 3;
            if (args.get(2).equalsIgnoreCase("true"))
            {
                wideTrunk = true;
            }
            else if (args.get(2).equalsIgnoreCase("false"))
            {
                wideTrunk = false;
            }
            else
            {
                wideTrunk = false;
                ind = 2;
            }
        }
        for (int i = ind; i < args.size() - 1; i += 2)
        {
            String treeName = args.get(i);
            treeNames.add(treeName);
            treeChances.add(readDouble(args.get(i + 1), 1, 100));
        }
    }

    private static CustomObject getTreeObject(String objectName, String worldName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException
    {
        CustomObject maybeTree = customObjectManager.getGlobalObjects().getObjectByName(objectName, worldName, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
        if (maybeTree == null)
        {
            throw new InvalidConfigException("Unknown object " + objectName);
        }
        if (!maybeTree.canSpawnAsTree())
        {
            throw new InvalidConfigException("Cannot spawn " + objectName + " as tree");
        }
        return maybeTree;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final SaplingGen compare = (SaplingGen) other;
        return this.saplingType == compare.saplingType
                && (this.treeNames == null ? this.treeNames == compare.treeNames
                        : this.treeNames.equals(compare.treeNames))
                && (this.treeNames == null ? this.treeNames == compare.treeNames
                        : this.treeNames.equals(compare.treeNames))
                && (this.treeChances == null ? this.treeChances == compare.treeChances
                        : this.treeChances.equals(compare.treeChances));
    }

    public int getPriority()
    {
        return -30;
    }

    /**
     * Grows a tree from this sapling.
     * @param world      World to spawn in.
     * @param random     Random number generator.
     * @param isWideTree Whether the tree is a wide (2x2 trunk) tree. Used to
     *                   correctly rotate those trees.
     * @param x          X to spawn. For wide trees, this is the lowest x of
     *                   the trunk.
     * @param y          Y to spawn.
     * @param z          Z to spawn. For wide trees, this is the lowest z of
     *                   the trunk.
     * @return Whether a tree was grown.
     */
    public boolean growSapling(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean isWideTree, int x, int y, int z, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	loadTreeObjects(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
    	
        for (int treeNumber = 0; treeNumber < trees.size(); treeNumber++)
        {
            if (random.nextInt(100) < treeChances.get(treeNumber))
            {
                CustomObject tree = trees.get(treeNumber);
                Rotation rotation = tree.canRotateRandomly() ? Rotation.getRandomRotation(random) : Rotation.NORTH;

                // Correct spawn location for rotated wide trees
                int spawnX = x;
                int spawnZ = z;
                if (isWideTree)
                {
                    int[] offset = TREE_OFFSET.get(rotation);
                    spawnX += offset[0];
                    spawnZ += offset[1];
                }

                if (tree.spawnFromSapling(structureCache, worldGenRegion, random, rotation, spawnX, y, spawnZ))
                {
                    // Success!
                    return true;
                }
            }
        }
        return false;
    }

    private void loadTreeObjects(Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	if(!this.treesLoaded)
    	{
    		this.treesLoaded = true;
    		
    		for(String treeName : this.treeNames)
    		{
    			CustomObject tree;
				try {
					tree = getTreeObject(treeName, worldName, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
	    			this.trees.add(tree);
				} catch (InvalidConfigException e) {
					this.trees.add(null);
					logger.log(LogMarker.WARN, "Could not find Object " + treeName + " for Sapling() resource in biome " + this.biomeName);
				}
    		}
    		
    		ArrayList<CustomObject> newTrees = new ArrayList<CustomObject>();
    		ArrayList<String> newTreeNames = new ArrayList<String>();
    		ArrayList<Double> newTreeChances = new ArrayList<Double>();
   		
        	for(int i = 0; i < this.trees.size(); i++)
        	{
        		if(this.trees.get(i) != null)
        		{
        			newTrees.add(this.trees.get(i));
        			newTreeNames.add(this.treeNames.get(i));
        			newTreeChances.add(this.treeChances.get(i));
        		}
        	}
        	
        	this.trees = newTrees;
        	this.treeNames = newTreeNames;
        	this.treeChances = newTreeChances;
    	}
    }
    
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + (this.trees != null ? this.trees.hashCode() : 0);
        hash = 37 * hash + (this.treeNames != null ? this.treeNames.hashCode() : 0);
        hash = 37 * hash + (this.treeChances != null ? this.treeChances.hashCode() : 0);
        hash = 37 * hash + (this.saplingType != null ? this.saplingType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
    {
        return other.getClass().equals(getClass()) && saplingType.equals(((SaplingGen)other).saplingType);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Sapling(").append(saplingType);

        if (saplingType == SaplingType.Custom)
        {
            sb.append(",").append(saplingMaterial.getName()).append(",").append(wideTrunk);
        }

        for (int i = 0; i < treeNames.size(); i++)
        {
            sb.append(",").append(treeNames.get(i)).append(",").append(treeChances.get(i));
        }
        return sb.append(')').toString();
    }
}
