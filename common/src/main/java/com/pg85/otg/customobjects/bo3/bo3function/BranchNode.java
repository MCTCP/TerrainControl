package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Simple class to hold the spawn chance and rotation of a BO3 in the Branch or
 * WeightedBranch function in the BO3 file.
 */
public class BranchNode implements Comparable<BranchNode>
{
	// TODO: implement proper get / set?

	/**
	 * The max branch depth of the branch given to it by its parent
	 * Used to make certain branches longer than others
	 */
	int branchDepth;

	public boolean isRequiredBranch;

	boolean isWeightedBranch;

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
    public String customObjectName;

    public String branchGroup;

    /**
     * Creates an instance of BranchNode with given rotation, chance, and branch
     * fields
     * <p/>
     *
     * @param branchDepth The max branch depth of the branch. Only the max branch depth of the first branch part is used when spawning a branch structure!
     * @param rotation The rotation of the branch
     * @param chance The spawn chance of the branch
     * @param branch The branch
     */
    public BranchNode(int branchDepth, boolean isRequiredBranch, boolean isWeightedBranch, Rotation rotation, double chance, CustomObject customObject, String customObjectName, String branchGroup)
    {
    	this.branchDepth = branchDepth;
        this.rotation = rotation;
        this.chance = chance;

        this.customObjectName = customObject != null ? customObject.getName() : customObjectName != null && customObjectName.length() > 0 ? customObjectName : null;
        this.customObject = customObject;

        this.isRequiredBranch = isRequiredBranch;
        this.isWeightedBranch = isWeightedBranch;
        this.branchGroup = branchGroup;
    }

    // Non-OTG+
    public BranchNode(Rotation rotation, double chance, CustomObject branch, String customObjectName)
    {
        this.rotation = rotation;
        this.chance = chance;
        this.customObject = branch;
        this.customObjectName = branch != null ? branch.getName() : customObjectName != null && customObjectName.length() > 0 ? customObjectName : null;
    }
    //

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
    public CustomObject getCustomObject(boolean lazyLoad, LocalWorld world)
    {
    	if(customObject != null || !lazyLoad)
    	{
    		return customObject;
    	}

    	customObject = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(customObjectName, world.getName());
    	customObjectName = customObject != null ? customObject.getName() : null;

		return customObject;
    }

    /**
     * @return The string representation of this branch as seen in BO3 configs
     */
    public String toBranchString()
    {
        return ',' + customObjectName + ',' + rotation.name() + ',' + chance + ',' + branchDepth;
    }

    @Override
    public int compareTo(BranchNode that)
    {
        return (int) (this.chance - that.chance);
    }

}
