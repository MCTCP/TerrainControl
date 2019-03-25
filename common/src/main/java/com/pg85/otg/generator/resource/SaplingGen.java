package com.pg85.otg.generator.resource;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.Rotation;

import java.util.*;

/**
 * Represents a custom sapling generator. This generator can grow vanilla trees,
 * but also custom objects.
 *
 */
public class SaplingGen extends ConfigFunction<BiomeConfig>
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
    public List<Double> treeChances;
    public List<String> treeNames;
    public List<CustomObject> trees;

    public SaplingGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(3, args);

        saplingType = SaplingType.get(args.get(0));
        if (saplingType == null)
        {
            throw new InvalidConfigException("Unknown sapling type " + args.get(0));
        }

        trees = new ArrayList<CustomObject>();
        treeNames = new ArrayList<String>();
        treeChances = new ArrayList<Double>();

        for (int i = 1; i < args.size() - 1; i += 2)
        {
            String treeName = args.get(i);
            trees.add(getTreeObject(treeName, biomeConfig.worldConfig.getName()));
            treeNames.add(treeName);
            treeChances.add(readDouble(args.get(i + 1), 1, 100));
        }
    }

    private static CustomObject getTreeObject(String objectName, String worldName) throws InvalidConfigException
    {
        CustomObject maybeTree = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(objectName, worldName);
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
    public boolean growSapling(LocalWorld world, Random random, boolean isWideTree, int x, int y, int z)
    {
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

                if (tree.spawnForced(world, random, rotation, spawnX, y, spawnZ))
                {
                    // Success!
                    return true;
                }
            }
        }
        return false;
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
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        return other.getClass().equals(getClass()) && saplingType.equals(((SaplingGen)other).saplingType);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Sapling(").append(saplingType);

        for (int i = 0; i < treeNames.size(); i++)
        {
            sb.append(",").append(treeNames.get(i)).append(",").append(treeChances.get(i));
        }
        return sb.append(')').toString();
    }

}
