package com.pg85.otg.customobjects.customstructure;

import java.util.Random;
import java.util.Stack;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;

public class BranchDataItem
{
	private static int BranchDataItemCounter = -1;
	
	CustomObjectCoordinate Branch;
	ChunkCoordinate ChunkCoordinate;
	BranchDataItem Parent;
	boolean DoneSpawning = false;
	boolean SpawnDelayed = false;
	boolean CannotSpawn = false;

	boolean wasDeleted = false;
	boolean isBeingRolledBack = false;
	int branchNumber = -1;
	int CurrentDepth = 0;
	int MaxDepth = 0;
	
	private boolean MinimumSize = false;
	private LocalWorld World;
	private Random Random;
	private Stack<BranchDataItem> Children = new Stack<BranchDataItem>();
	
	public BranchDataItem()
	{
		throw new RuntimeException();
	}

	BranchDataItem(LocalWorld world, Random random, BranchDataItem parent, CustomObjectCoordinate branch, String startBO3Name, int currentDepth, int maxDepth, boolean minimumSize)
	{
		World = world;
		Random = random;
		Parent = parent;
		Branch = branch;
		Branch.StartBO3Name = startBO3Name;
		CurrentDepth = currentDepth;
		MaxDepth = maxDepth;
		MinimumSize = minimumSize;
		ChunkCoordinate = com.pg85.otg.util.ChunkCoordinate.fromBlockCoords(Branch.getX(), Branch.getZ());

		BranchDataItem.BranchDataItemCounter += 1; // TODO: Reset this somewhere for each new world created?
		branchNumber = BranchDataItem.BranchDataItemCounter;
	}	
	
	Stack<BranchDataItem> getChildren(boolean dontSpawn)
	{
		if(World == null)
		{
			throw new RuntimeException();
		}

    	if(!dontSpawn && Children.size() == 0)
    	{
	        Branch[] branches = Branch.getStructuredObject().getBranches();
	        for (Branch branch1 : branches)
	        {
		    	CustomObjectCoordinate childCoordObject = branch1.toCustomObjectCoordinate(World, Random, Branch.getRotation(), Branch.getX(), Branch.getY(), Branch.getZ(), Branch.StartBO3Name != null ? Branch.StartBO3Name : Branch.BO3Name);
		    	// Can be null if spawn roll fails TODO: dont roll for spawn in branch.toCustomObjectCoordinate?
		    	if(childCoordObject != null)
		    	{
		    		BO3 childBO3 = ((BO3)childCoordObject.getObject());
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
		    				OTG.log(LogMarker.WARN, "canOverride optional branches cannot be in a branch group, ignoring branch: " + childBO3.getName() + " in BO3: " + Branch.BO3Name);
		    			}
		    			continue;
		    		}

		    		if(childBO3.getSettings().overrideParentHeight)
		    		{
    		    		if(childBO3.getSettings().spawnHeight == SpawnHeightEnum.highestBlock || childBO3.getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock || childBO3.getSettings().SpawnAtWaterLevel)
    		    		{
    		    			childCoordObject.y = World.getHighestBlockYAt(childCoordObject.getX(), childCoordObject.getZ(), true, childBO3.getSettings().spawnHeight != SpawnHeightEnum.highestSolidBlock || childBO3.getSettings().SpawnAtWaterLevel, childBO3.getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock && !childBO3.getSettings().SpawnAtWaterLevel, true);
    		    		}
    		    		else if(childBO3.getSettings().spawnHeight == SpawnHeightEnum.randomY)
    		    		{
    		    			childCoordObject.y = RandomHelper.numberInRange(Random, childBO3.getSettings().minHeight, childBO3.getSettings().maxHeight);
    		    		}
		    		}
		    		childCoordObject.y += childBO3.getSettings().heightOffset;
		    		//if(childCoordObject.y < childBO3.settings.minHeight || childCoordObject.y > childBO3.settings.maxHeight)
		    		{
		    			//continue; // TODO: Don't do this for required branches? instead do rollback?
		    		}

		    		int currentDepth1 = childCoordObject.isRequiredBranch ? CurrentDepth : CurrentDepth + 1;
		    		int maxDepth1 = MaxDepth;

		    		// If this branch has a branch depth value other than 0 then override current branch depth with the value
		    		if(childCoordObject.branchDepth > 0 && !MinimumSize)
		    		{
		    			currentDepth1 = 0;
			    		maxDepth1 = childCoordObject.branchDepth;
		    		}

		    		if(MinimumSize)
		    		{
		    			maxDepth1 = 0;
		    		}

		    		if((maxDepth1 > 0 && currentDepth1 <= maxDepth1) || childCoordObject.isRequiredBranch)
		    		{
    		    		Children.add(new BranchDataItem(World, Random, this, childCoordObject, Branch.StartBO3Name != null ? Branch.StartBO3Name : Branch.BO3Name, currentDepth1, maxDepth1, MinimumSize));
		    		}
		    	}
	        }
    	}
    	return Children;
	}

	public boolean getHasOptionalBranches()
	{
        Branch[] branches = Branch.getStructuredObject().getBranches();
        for (Branch branch1 : branches)
        {
	    	CustomObjectCoordinate childCoordObject = branch1.toCustomObjectCoordinate(World, Random, Branch.getRotation(), Branch.getX(), Branch.getY(), Branch.getZ(), Branch.StartBO3Name != null ? Branch.StartBO3Name : Branch.BO3Name);
	    	// Can be null if spawn roll fails TODO: dont roll for spawn in branch.toCustomObjectCoordinate?
	    	if(childCoordObject != null)
	    	{
	    		if(!childCoordObject.isRequiredBranch && MaxDepth > 0)
	    		{
	    			return true;
	    		}
	    		else if(childCoordObject.isRequiredBranch)
	    		{
	    			// Check if this is not an infinite loop
	    			// This can happen if for instance BO3 Top spawns BO3 Bottom as a required branch and BO3 Bottom also spawns BO3 Top as a required branch
	    			BranchDataItem parent = Parent;
	    			Boolean bInfinite = false;
	    			while(parent != null && parent.Branch.isRequiredBranch)
	    			{
	    				if(parent.Branch.getObject().getName().equals(childCoordObject.getObject().getName()))
	    				{
	    					bInfinite = true;
	    					break;
	    				}
	    				parent = parent.Parent;
	    			}
	    			if(bInfinite)
	    			{
	    				continue;
	    			}

		    		int currentDepth1 = childCoordObject.isRequiredBranch ? CurrentDepth : CurrentDepth + 1;
		    		int maxDepth1 = MaxDepth;

		    		// If this branch has a branch depth value other than 0 then override current branch depth with the value
		    		if(childCoordObject.branchDepth > 0 && !MinimumSize)
		    		{
		    			currentDepth1 = 0;
			    		maxDepth1 = childCoordObject.branchDepth;
		    		}

		    		if(MinimumSize)
		    		{
		    			maxDepth1 = 0;
		    		}

		    		boolean hasRandomBranches = new BranchDataItem(World, Random, this, childCoordObject, Branch.StartBO3Name != null ? Branch.StartBO3Name : Branch.BO3Name, currentDepth1, maxDepth1, MinimumSize).getHasOptionalBranches();
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