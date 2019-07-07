package com.pg85.otg.customobjects.bo4.bo4function;

import com.pg85.otg.customobjects.bofunctions.BranchNode;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Simple class to hold the spawn chance and rotation of a BO3 in the Branch or
 * WeightedBranch function in the BO3 file.
 */
class BO4BranchNode extends BranchNode
{
	/**
	 * The max branch depth of the branch given to it by its parent
	 * Used to make certain branches longer than others
	 */
	int branchDepth;
	boolean isRequiredBranch;
	boolean isWeightedBranch;
    String branchGroup;

    /**
     * Creates an instance of BranchNode with given rotation, chance, and branch fields
     */
    BO4BranchNode(int branchDepth, boolean isRequiredBranch, boolean isWeightedBranch, Rotation rotation, double chance, StructuredCustomObject customObject, String customObjectName, String branchGroup)
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

    /**
     * @return The string representation of this branch as seen in BO3 configs
     */
    @Override
	protected String toBranchString()
    {
        return ',' + customObjectName + ',' + rotation.name() + ',' + chance + ',' + branchDepth;
    }   
}
