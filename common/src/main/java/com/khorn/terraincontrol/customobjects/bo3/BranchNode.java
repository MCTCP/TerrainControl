package com.khorn.terraincontrol.customobjects.bo3;


import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.util.Rotation;


/**
 * Simple class to hold the spawn chance and rotation of a BO3 in the Branch or
 * WeightedBranch function in the BO3 file.
 */
public class BranchNode implements Comparable<BranchNode>
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
     * 
     * @param rotation The rotation of the branch
     * @param chance The spawn chance of the branch
     * @param branch The branch
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

    /**
     * @return The string representation of this branch as seen in BO3 configs
     */
    public String toBranchString()
    {
        return ',' + customObject.getName() + ',' + rotation.name() + ',' + chance;
    }

    @Override
    public int compareTo(BranchNode that)
    {
        return (int) (this.chance - that.chance);
    }

}
