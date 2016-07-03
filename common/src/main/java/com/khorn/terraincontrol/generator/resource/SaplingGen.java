package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.Rotation;

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
    public List<Integer> treeChances;
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
        treeChances = new ArrayList<Integer>();

        for (int i = 1; i < args.size() - 1; i += 2)
        {
            CustomObject object = getHolder().worldConfig.worldObjects.parseCustomObject(args.get(i));
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

    /**
     * Creates a {@link SaplingGen} instance.
     * @param biomeConfig     The biome config.
     * @param saplingType     The sapling type.
     * @param typesAndChances Pairs of two objects. The first entry in each pair
     *                        must be, after transformation using
     *                        {@link Object#toString()}, a valid tree name. The
     *                        second entry in each pair must be an instance of
     *                        {@link Number}, this is the spawn chance
     *                        percentage.
     */
    public SaplingGen(BiomeConfig biomeConfig, SaplingType saplingType, Object... typesAndChances)
    {
        super(biomeConfig);
        this.saplingType = saplingType;
        for (int i = 0; i < typesAndChances.length - 1; i += 2)
        {
            String treeName = typesAndChances[i].toString();
            int chance = ((Number) typesAndChances[i + 1]).intValue();
            CustomObject object = getHolder().worldConfig.worldObjects.parseCustomObject(
                    treeName);

            trees.add(object);
            treeNames.add(treeName);
            treeChances.add(chance);
        }
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
        String output = "Sapling(" + saplingType;

        for (int i = 0; i < treeNames.size(); i++)
        {
            output += "," + treeNames.get(i) + "," + treeChances.get(i);
        }
        return output + ")";
    }

}
