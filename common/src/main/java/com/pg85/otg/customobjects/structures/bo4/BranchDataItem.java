package com.pg85.otg.customobjects.structures.bo4;

import java.util.Random;
import java.util.Stack;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.customobjects.structures.Branch;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;

class BranchDataItem
{
	private static int BranchDataItemCounter = -1;
	
	BO4CustomStructureCoordinate branch;
	ChunkCoordinate chunkCoordinate;
	BranchDataItem parent;
	boolean doneSpawning = false;
	boolean spawnDelayed = false;
	boolean cannotSpawn = false;

	boolean wasDeleted = false;
	boolean isBeingRolledBack = false;
	int branchNumber = -1;
	int currentDepth = 0;
	int maxDepth = 0;
	
	private boolean minimumSize = false;
	private Random random;
	private Stack<BranchDataItem> children = new Stack<BranchDataItem>();
	private String startBO3Name;

	BranchDataItem(Random random, BranchDataItem parent, BO4CustomStructureCoordinate branch, String startBO3Name, int currentDepth, int maxDepth, boolean minimumSize)
	{
		this.random = random;
		this.parent = parent;
		this.branch = branch;
		this.startBO3Name = startBO3Name;
		this.currentDepth = currentDepth;
		this.maxDepth = maxDepth;
		this.minimumSize = minimumSize;
		this.chunkCoordinate = com.pg85.otg.util.ChunkCoordinate.fromBlockCoords(this.branch.getX(), this.branch.getZ());

		BranchDataItem.BranchDataItemCounter += 1; // TODO: Reset this somewhere for each new world created?
		branchNumber = BranchDataItem.BranchDataItemCounter;
	}	
	
	Stack<BranchDataItem> getChildren(boolean dontSpawn, LocalWorld world)
	{
		if(world == null)
		{
			throw new RuntimeException();
		}

    	if(!dontSpawn && this.children.size() == 0)
    	{
	        Branch[] branches = this.branch.getStructuredObject().getBranches();
	        for (Branch branch1 : branches)
	        {
		    	BO4CustomStructureCoordinate childCoordObject = (BO4CustomStructureCoordinate)branch1.toCustomObjectCoordinate(world, this.random, this.branch.getRotation(), this.branch.getX(), this.branch.getY(), this.branch.getZ(), this.startBO3Name != null ? this.startBO3Name : this.branch.bo3Name);
		    	// Can be null if spawn roll fails TODO: dont roll for spawn in branch.toCustomObjectCoordinate?
		    	if(childCoordObject != null)
		    	{
		    		BO4 childBO3 = ((BO4)childCoordObject.getObject());
		    		if(childBO3 == null)
		    		{
		    			continue;
		    		}

		    		// canOverride optional branches spawn after all other branches have spawned (the "fundament" BO3's),
		    		// they are most commonly spawned on top of those BO3's to add randomised parts.
		    		// For instance interiors for rooms, doors, BO3's that knock out walls or ceilings between rooms etc.
		    		// "interior" BO3's failing to spawn cannot cause the "fundament" BO3's to be rolled back.
		    		// this is enforced by makign sure that canOverride optional branches cannot be in a branch group with other branches.
		    		if(
	    				childCoordObject.branchGroup != null &&
	    				childCoordObject.branchGroup.trim().length() > 0 &&
	    				childBO3.getSettings().canOverride &&
	    				!childCoordObject.isRequiredBranch
    				)
		    		{
		    			if(OTG.getPluginConfig().spawnLog)
		    			{
		    				OTG.log(LogMarker.WARN, "canOverride optional branches cannot be in a branch group, ignoring branch: " + childBO3.getName() + " in BO3: " + this.branch.bo3Name);
		    			}
		    			continue;
		    		}

		    		if(childBO3.getSettings().overrideParentHeight)
		    		{
    		    		if(childBO3.getSettings().spawnHeight == SpawnHeightEnum.highestBlock || childBO3.getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock || childBO3.getSettings().spawnAtWaterLevel)
    		    		{
    		    			childCoordObject.y = (short) world.getHighestBlockYAt(childCoordObject.getX(), childCoordObject.getZ(), true, childBO3.getSettings().spawnHeight != SpawnHeightEnum.highestSolidBlock || childBO3.getSettings().spawnAtWaterLevel, childBO3.getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock && !childBO3.getSettings().spawnAtWaterLevel, true);
    		    		}
    		    		else if(childBO3.getSettings().spawnHeight == SpawnHeightEnum.randomY)
    		    		{
    		    			childCoordObject.y = (short) RandomHelper.numberInRange(this.random, childBO3.getSettings().minHeight, childBO3.getSettings().maxHeight);
    		    		}
		    		}
		    		childCoordObject.y += childBO3.getSettings().heightOffset;
		    		//if(childCoordObject.y < childBO3.settings.minHeight || childCoordObject.y > childBO3.settings.maxHeight)
		    		{
		    			//continue; // TODO: Don't do this for required branches? instead do rollback?
		    		}

		    		int currentDepth1 = childCoordObject.isRequiredBranch ? currentDepth : currentDepth + 1;
		    		int maxDepth1 = this.maxDepth;

		    		// If this branch has a branch depth value other than 0 then override current branch depth with the value
		    		if(childCoordObject.branchDepth > 0 && !this.minimumSize)
		    		{
		    			currentDepth1 = 0;
			    		maxDepth1 = childCoordObject.branchDepth;
		    		}

		    		if(this.minimumSize)
		    		{
		    			maxDepth1 = 0;
		    		}

		    		if((maxDepth1 > 0 && currentDepth1 <= maxDepth1) || childCoordObject.isRequiredBranch)
		    		{
		    			this.children.add(new BranchDataItem(this.random, this, childCoordObject, this.startBO3Name != null ? this.startBO3Name : this.branch.bo3Name, currentDepth1, maxDepth1, this.minimumSize));
		    		}
		    	}
	        }
    	}
    	return this.children;
	}

	public boolean getHasOptionalBranches(LocalWorld world)
	{
        Branch[] branches = this.branch.getStructuredObject().getBranches();
        for (Branch branch1 : branches)
        {
        	BO4CustomStructureCoordinate childCoordObject = (BO4CustomStructureCoordinate)branch1.toCustomObjectCoordinate(world, this.random, this.branch.getRotation(), this.branch.getX(), this.branch.getY(), this.branch.getZ(), this.startBO3Name != null ? this.startBO3Name : this.branch.bo3Name);
	    	// Can be null if spawn roll fails TODO: dont roll for spawn in branch.toCustomObjectCoordinate?
	    	if(childCoordObject != null)
	    	{
	    		if(!childCoordObject.isRequiredBranch && this.maxDepth > 0)
	    		{
	    			return true;
	    		}
	    		else if(childCoordObject.isRequiredBranch)
	    		{
	    			// Check if this is not an infinite loop
	    			// This can happen if for instance BO3 Top spawns BO3 Bottom as a required branch and BO3 Bottom also spawns BO3 Top as a required branch
	    			BranchDataItem parent = this.parent;
	    			Boolean bInfinite = false;
	    			while(parent != null && parent.branch.isRequiredBranch)
	    			{
	    				if(parent.branch.getObject().getName().equals(childCoordObject.getObject().getName()))
	    				{
	    					bInfinite = true;
	    					break;
	    				}
	    				parent = parent.parent;
	    			}
	    			if(bInfinite)
	    			{
	    				continue;
	    			}

		    		int currentDepth1 = childCoordObject.isRequiredBranch ? this.currentDepth : this.currentDepth + 1;
		    		int maxDepth1 = this.maxDepth;

		    		// If this branch has a branch depth value other than 0 then override current branch depth with the value
		    		if(childCoordObject.branchDepth > 0 && !this.minimumSize)
		    		{
		    			currentDepth1 = 0;
			    		maxDepth1 = childCoordObject.branchDepth;
		    		}

		    		if(this.minimumSize)
		    		{
		    			maxDepth1 = 0;
		    		}

		    		boolean hasRandomBranches = new BranchDataItem(this.random, this, childCoordObject, this.startBO3Name != null ? this.startBO3Name : this.branch.bo3Name, currentDepth1, maxDepth1, this.minimumSize).getHasOptionalBranches(world);
		    		if(hasRandomBranches)
		    		{
		    			return true;
		    		}
	    		}
	    	}
        }
        return false;
	}
}