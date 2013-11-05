package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.util.Rotation;
import java.util.Comparator;

/**
 * A Node class to be used in collections to represent a branch that comes with
 * an associated rotation and chance.
 */
public class BranchNode
{

    /**
     * The rotation of a branch
     */
    private Rotation rotation;
    /**
     * The chance of a branch
     */
    private double chance;
    /**
     * The branch with associated rotation and chance values
     */
    private CustomObject customObject;

    /**
     * Creates an instance of BranchNode with given rotation, chance, and branch
     * fields
     * <p/>
     * @param rotation The rotation of the branch
     * @param chance   The spawn chance of the branch
     * @param branch   The branch
     */
    public BranchNode(Rotation rotation, double chance, CustomObject branch)
    {
        this.rotation = rotation;
        this.chance = chance;
        this.customObject = branch;
    }

    /**
     * @return the spawn chance of the branch
     */
    public double getChance()
    {
        return chance;
    }

    /**
     * @return the rotation object associated with the branch
     */
    public Rotation getRotation()
    {
        return rotation;
    }

    /**
     * @return the branch CustomObject
     */
    public CustomObject getCustomObject()
    {
        return customObject;
    }

    private static class BranchChanceComparator implements Comparator<BranchNode>
    {

        @Override
        public int compare(BranchNode o1, BranchNode o2)
        {
            return (int) (o1.chance - o2.chance);
        }

    }

    /**
     * @return a BranchNode comparator that compares each Node based on the
     *         chance field
     */
    public static Comparator<BranchNode> getComparator()
    {
        return new BranchChanceComparator();
    }

    /**
     * @return The string representation of this branch as seen in BO3 configs
     */
    public String toBranchString()
    {
        return ',' + customObject.getName() + ',' + rotation.name() + ',' + chance;
    }

}
