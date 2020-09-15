package com.pg85.otg.customobjects.structures.bo4;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.customobjects.structures.CustomStructure;
import com.pg85.otg.customobjects.structures.CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;

import java.util.*;
import java.util.Map.Entry;

/**
 * Represents a collection of all {@link CustomObject}s in a structure. It is
 * calculated by finding the branches of one object, then finding the branches
 * of those branches, etc., until
 * {@link CustomObject#getMaxBranchDepth()} is reached.
 *
 */
public class BO4CustomStructure extends CustomStructure
{
    private Random worldRandom;

	private SmoothingAreaGenerator smoothingAreaManager = new SmoothingAreaGenerator();
	
    // Stores all the branches of this branching structure that should spawn along with the chunkcoordinates they should spawn in
    public Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn = new HashMap<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>>();
    public Map<ChunkCoordinate, String> ObjectsToSpawnInfo = new HashMap<ChunkCoordinate, String>();
   
    boolean IsSpawned;
    private boolean isStructureAtSpawn = false;

    private int minY;

    // A smoothing area is drawn around all outer blocks (or blocks neighbouring air) on the lowest layer of blocks in each BO3 of this branching structure that has a SmoothRadius set greater than 0.
    public Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();

    private int branchesTried = 0;

    public boolean startChunkBlockChecksDone = false;
    
    private Stack<BranchDataItem> AllBranchesBranchData = new Stack<BranchDataItem>();
    private HashMap<ChunkCoordinate, ArrayList<BranchDataItem>> AllBranchesBranchDataByChunk = new HashMap<ChunkCoordinate, ArrayList<BranchDataItem>>();
	private HashMap<String, ArrayList<ChunkCoordinate>> AllBranchesBranchDataByName = new HashMap<String, ArrayList<ChunkCoordinate>>(); // Used to find distance between branches and branch groups
	private HashMap<String, HashMap<ChunkCoordinate, ArrayList<Integer>>> AllBranchesBranchDataByGroup = new HashMap<String, HashMap<ChunkCoordinate, ArrayList<Integer>>>(); // Used to find distance between branches and branch groups
    private HashSet<Integer> AllBranchesBranchDataHash = new HashSet<Integer>();
    private boolean SpawningCanOverrideBranches = false;
    private int Cycle = 0;
    
    private BranchDataItem currentSpawningRequiredChildrenForOptionalBranch;
    private boolean spawningRequiredChildrenForOptionalBranch = false;
    private boolean spawnedBranchThisCycle = false;
    private boolean spawnedBranchLastCycle = false;
    
    public BO4CustomStructure(BO4CustomStructureCoordinate start)
    {
    	this.start = start;
    }
    
    public BO4CustomStructure(LocalWorld world, BO4CustomStructureCoordinate structureStart, Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn, Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn, int minY)
    {
    	this(world, structureStart);
    	this.objectsToSpawn = objectsToSpawn;
    	this.smoothingAreasToSpawn = smoothingAreasToSpawn;
    	this.minY = minY;
    }

    public BO4CustomStructure()
    {
    
    }
    
    public BO4CustomStructure(LocalWorld world, BO4CustomStructureCoordinate start)
    {
        this.isStructureAtSpawn = false;

        if(start == null)
        {
        	return;
        }
        if (!(start.getObject() instanceof StructuredCustomObject))
        {
            throw new IllegalArgumentException("Start object must be a structure!");
        }

        this.start = start;
        this.random = RandomHelper.getRandomForCoords(start.getX() + 8, start.getY(), start.getZ() + 7, world.getSeed());
    }
    
    public BO4CustomStructure(LocalWorld world, BO4CustomStructureCoordinate start, boolean isStructureAtSpawn, ArrayList<String> targetBiomes, ChunkCoordinate chunkBeingPopulated)
    {
        this.isStructureAtSpawn = isStructureAtSpawn;

        if (!(start.getObject() instanceof StructuredCustomObject))
        {
            throw new IllegalArgumentException("Start object must be a structure!");
        }

        this.start = start;
        this.random = RandomHelper.getRandomForCoords(start.getX() + 8, start.getY(), start.getZ() + 7, world.getSeed());

		long startTime = System.currentTimeMillis();

		if(!doStartChunkBlockChecks(world, chunkBeingPopulated))
		{
			return;
		}

		branchesTried = 0;
		
		try
		{
			calculateBranches(false, world, targetBiomes, chunkBeingPopulated);
		} catch (InvalidConfigException ex) {
			OTG.log(LogMarker.FATAL, "An unknown error occurred while calculating branches for BO3 " + this.start.bo3Name + ". This is probably an error in the BO3's branch configuration, not a bug. If you can track this down, please tell me what caused it!");
			throw new RuntimeException();
		}

		for(Entry<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> chunkCoordSet : objectsToSpawn.entrySet())
		{
			String structureInfo = "";
			for(CustomStructureCoordinate customObjectCoord : chunkCoordSet.getValue())
			{
				structureInfo += customObjectCoord.getObject().getName() + ":" + customObjectCoord.getRotation() + ", ";
			}
			if(structureInfo.length() > 0)
			{
				structureInfo = structureInfo.substring(0,  structureInfo.length() - 2);
				ObjectsToSpawnInfo.put(chunkCoordSet.getKey(), "Branches in chunk X" + chunkCoordSet.getKey().getChunkX() + " Z" + chunkCoordSet.getKey().getChunkZ() + " : " + structureInfo);
			}
		}
		
		for(Entry<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> chunkCoordSet : objectsToSpawn.entrySet())
		{
        	// Don't spawn BO3's that have been overriden because of replacesBO3
        	for (CustomStructureCoordinate coordObject : chunkCoordSet.getValue())
        	{
        		BO4Config objectConfig = ((BO4)coordObject.getObject()).getConfig();
        		if(objectConfig.replacesBO3Branches.size() > 0)
        		{
        			for(String BO3ToReplace : objectConfig.replacesBO3Branches)
        			{
        				for (BO4CustomStructureCoordinate coordObjectToReplace : chunkCoordSet.getValue())
        				{
        					if(((BO4)coordObjectToReplace.getObject()).getName().equals(BO3ToReplace))
        					{
        						if(checkCollision(coordObject, coordObjectToReplace))
        						{
        							coordObjectToReplace.isSpawned = true;
        						}
        					}
        				}
        			}
        		}
        	}
		}

        // Calculate smoothing areas around the entire branching structure
        // Smooth the terrain in all directions bordering the structure so
        // that there is a smooth transition in height from the surrounding
        // terrain to the BO3. This way BO3's won't float above the ground
        // or spawn inside a hole with vertical walls.
		smoothingAreasToSpawn = smoothingAreaManager.calculateSmoothingAreas(objectsToSpawn, (BO4CustomStructureCoordinate)this.start, world);
		smoothingAreaManager.customObjectStructureSpawn(smoothingAreasToSpawn);
		
		for(ChunkCoordinate chunkCoord : objectsToSpawn.keySet())
		{
			world.getStructureCache().bo4StructureCache.put(chunkCoord, this);
			world.getStructureCache().getPlotter().invalidateChunkInStructuresPerChunkCache(chunkCoord);
			// Make sure not to override any ModData/Spawner/Particle data added by CustomObjects
			if(world.getStructureCache().worldInfoChunks.containsKey(chunkCoord))
			{
				CustomStructure existingObject = world.getStructureCache().worldInfoChunks.get(chunkCoord);
				this.modDataManager.modData.addAll(existingObject.modDataManager.modData);
				this.particlesManager.particleData.addAll(existingObject.particlesManager.particleData);
				this.spawnerManager.spawnerData.addAll(existingObject.spawnerManager.spawnerData);
			}
			world.getStructureCache().worldInfoChunks.put(chunkCoord, this);
		}

		for(ChunkCoordinate chunkCoord : smoothingAreasToSpawn.keySet())
		{
			world.getStructureCache().bo4StructureCache.put(chunkCoord, this);
			world.getStructureCache().getPlotter().invalidateChunkInStructuresPerChunkCache(chunkCoord);
			// Make sure not to override any ModData/Spawner/Particle data added by CustomObjects
			if(world.getStructureCache().worldInfoChunks.containsKey(chunkCoord))
			{
				CustomStructure existingObject = world.getStructureCache().worldInfoChunks.get(chunkCoord);
				this.modDataManager.modData.addAll(existingObject.modDataManager.modData);
				this.particlesManager.particleData.addAll(existingObject.particlesManager.particleData);
				this.spawnerManager.spawnerData.addAll(existingObject.spawnerManager.spawnerData);
			}
			world.getStructureCache().worldInfoChunks.put(chunkCoord, this);
		}

		if(objectsToSpawn.size() > 0)
		{
			IsSpawned = true;
			if(OTG.getPluginConfig().spawnLog)
			{
				int totalBO3sSpawned = 0;
				for(ChunkCoordinate entry : objectsToSpawn.keySet())
				{
					totalBO3sSpawned += objectsToSpawn.get(entry).size();
				}

				OTG.log(LogMarker.INFO, this.start.getObject().getName() + " " + totalBO3sSpawned + " object(s) plotted in " + (System.currentTimeMillis() - startTime) + " Ms and " + Cycle + " cycle(s), " + (branchesTried + 1) + " object(s) tried.");
			}
		}
    }

    private boolean doStartChunkBlockChecks(LocalWorld world, ChunkCoordinate chunkBeingPopulated)
    {
    	if(!startChunkBlockChecksDone)
    	{
	    	startChunkBlockChecksDone = true;

			// Requesting the Y position or material of a block in an unpopulated chunk causes some of that chunk's blocks to be calculated, this is expensive and should be kept at a minimum.

			// Y checks:
			// If BO3's have a minimum and maximum Y configured by the player then we don't really need
	    	// to check if the BO3 fits in the Y direction, that is the user's responsibility!

			// Material checks:
			// A BO3 may need to perform material checks when using !CanSpawnOnWater or SpawnOnWaterOnly

	    	short startY = 0;
	    		    	
			if(
				((BO4)this.start.getObject()).getConfig().spawnHeight == SpawnHeightEnum.highestBlock || 
				((BO4)this.start.getObject()).getConfig().spawnHeight == SpawnHeightEnum.highestSolidBlock
			)
			{
				if(((BO4)this.start.getObject()).getConfig().spawnAtWaterLevel)
				{
					LocalBiome biome = world.getBiome(this.start.getX() + 8, this.start.getZ() + 7);
					startY = (short) (biome.getBiomeConfig().useWorldWaterLevel ? world.getConfigs().getWorldConfig().waterLevelMax : biome.getBiomeConfig().waterLevelMax);
				} else {
					// OTG.log(LogMarker.INFO, "Request height for chunk X" + ChunkCoordinate.fromBlockCoords(Start.getX(), Start.getZ()).getChunkX() + " Z" + ChunkCoordinate.fromBlockCoords(Start.getX(), Start.getZ()).getChunkZ());
					// If this chunk has not yet been populated then this will cause it to be! (ObjectSpawner.Populate() is called)

					int highestBlock = 0;

					if(!((BO4)this.start.getObject()).getConfig().spawnUnderWater)
					{
						highestBlock = world.getHighestBlockYAt(this.start.getX() + 8, this.start.getZ() + 7, true, true, false, true, true, null);
					} else {
						highestBlock = world.getHighestBlockYAt(this.start.getX() + 8, this.start.getZ() + 7, true, false, true, true, true, null);
					}

					if(highestBlock < 0)
					{
						//OTG.log(LogMarker.INFO, "Structure " + Start.BO3Name + " could not be plotted at Y < 1. If you are creating empty chunks intentionally (for a sky world for instance) then make sure you don't use the highestBlock setting for your BO3's");
						if(((BO4)this.start.getObject()).getConfig().heightOffset > 0) // Allow floating structures that use highestblock + heightoffset
						{
							highestBlock = ((BO4)this.start.getObject()).getConfig().heightOffset;
						} else {
							return false;
						}
					} else {
						startY  = (short) (highestBlock + 1);
					}
				}
			} else {
				if(((BO4)this.start.getObject()).getConfig().maxHeight != ((BO4)this.start.getObject()).getConfig().minHeight)
				{
					startY = (short) (((BO4)this.start.getObject()).getConfig().minHeight + new Random().nextInt(((BO4)this.start.getObject()).getConfig().maxHeight - ((BO4)this.start.getObject()).getConfig().minHeight));
				} else {
					startY = (short) ((BO4)this.start.getObject()).getConfig().minHeight;
				}
			}

			if(startY < ((BO4)this.start.getObject()).getConfig().minHeight || startY > ((BO4)this.start.getObject()).getConfig().maxHeight)
			{
				return false;
			}

			startY += ((BO4)this.start.getObject()).getConfig().heightOffset;

			if(startY < PluginStandardValues.WORLD_DEPTH || startY >= PluginStandardValues.WORLD_HEIGHT)
			{
				return false;
			}

			if(!((BO4)this.start.getObject()).getConfig().canSpawnOnWater)
			{
				if(world.getMaterial(this.start.getX() + 8, world.getHighestBlockYAt(this.start.getX() + 8, this.start.getZ() + 7, true, true, false, true, true, null), this.start.getZ() + 7, null).isLiquid())
				{
					return false;
				}
			}

			if(((BO4)this.start.getObject()).getConfig().spawnOnWaterOnly)
			{
				if(
					!(
						world.getMaterial(this.start.getX(), world.getHighestBlockYAt(this.start.getX(), this.start.getZ(), true, true, false, true, true, null), this.start.getZ(), null).isLiquid() &&
						world.getMaterial(this.start.getX(), world.getHighestBlockYAt(this.start.getX(), this.start.getZ() + 15, true, true, false, true, true, null), this.start.getZ() + 15, null).isLiquid() &&
						world.getMaterial(this.start.getX() + 15, world.getHighestBlockYAt(this.start.getX() + 15, this.start.getZ(), true, true, false, true, true, null), this.start.getZ(), null).isLiquid() &&
						world.getMaterial(this.start.getX() + 15, world.getHighestBlockYAt(this.start.getX() + 15, this.start.getZ() + 15, true, true, false, true, true, null), this.start.getZ() + 15, null).isLiquid()
					)
				)
				{
					return false;
				}								
			}
			
			this.start.y = startY;
    	}
    	return true;
    }
   
    /**
     * Gets an Object[] { ChunkCoordinate, ChunkCoordinate } containing the top left and bottom right chunk
     * If this structure were spawned as small as possible (with branchDepth 0)
     * @throws InvalidConfigException
     */
    public Object[] getMinimumSize(LocalWorld world) throws InvalidConfigException
    {
    	if(
			((BO4)this.start.getObject()).getConfig().minimumSizeTop != -1 &&
			((BO4)this.start.getObject()).getConfig().minimumSizeBottom != -1 &&
			((BO4)this.start.getObject()).getConfig().minimumSizeLeft != -1 &&
			((BO4)this.start.getObject()).getConfig().minimumSizeRight != -1)
    	{
    		Object[] returnValue = { ((BO4)this.start.getObject()).getConfig().minimumSizeTop, ((BO4)this.start.getObject()).getConfig().minimumSizeRight, ((BO4)this.start.getObject()).getConfig().minimumSizeBottom, ((BO4)this.start.getObject()).getConfig().minimumSizeLeft };
    		return returnValue;
    	}
    	
    	calculateBranches(true, world, null, null);

        // Calculate smoothing areas around the entire branching structure
        // Smooth the terrain in all directions bordering the structure so
        // that there is a smooth transition in height from the surrounding
        // terrain to the BO3. This way BO3's won't float above the ground
        // or spawn inside a hole with vertical walls.

		// Don't calculate smoothing areas for minimumSize, instead just add smoothradius / 16 to each side

		ChunkCoordinate startChunk = ChunkCoordinate.fromBlockCoords(this.start.getX(), this.start.getZ());

		ChunkCoordinate top = startChunk;
		ChunkCoordinate left = startChunk;
		ChunkCoordinate bottom = startChunk;
		ChunkCoordinate right = startChunk;

		for(ChunkCoordinate chunkCoord : objectsToSpawn.keySet())
		{
			if(chunkCoord.getChunkX() > right.getChunkX())
			{
				right = chunkCoord;
			}
			if(chunkCoord.getChunkZ() > bottom.getChunkZ())
			{
				bottom = chunkCoord;
			}
			if(chunkCoord.getChunkX() < left.getChunkX())
			{
				left = chunkCoord;
			}
			if(chunkCoord.getChunkZ() < top.getChunkZ())
			{
				top = chunkCoord;
			}
			for(CustomStructureCoordinate struct : objectsToSpawn.get(chunkCoord))
			{
				if(struct.getY() < minY)
				{
					minY = struct.getY();
				}
			}
		}

		minY += ((BO4)this.start.getObject()).getConfig().heightOffset;

		int smoothingRadiusInChunks = (int)Math.ceil(((BO4)this.start.getObject()).getConfig().smoothRadius / (double)16);  // TODO: this assumes that smoothradius is the same for every BO3 within this structure, child branches may have overriden their own smoothradius! This may cause problems if a child branch has a larger smoothradius than the starting structure
    	((BO4)this.start.getObject()).getConfig().minimumSizeTop = Math.abs(startChunk.getChunkZ() - top.getChunkZ()) + smoothingRadiusInChunks;
    	((BO4)this.start.getObject()).getConfig().minimumSizeRight = Math.abs(startChunk.getChunkX() - right.getChunkX()) + smoothingRadiusInChunks;
    	((BO4)this.start.getObject()).getConfig().minimumSizeBottom = Math.abs(startChunk.getChunkZ() - bottom.getChunkZ()) + smoothingRadiusInChunks;
    	((BO4)this.start.getObject()).getConfig().minimumSizeLeft = Math.abs(startChunk.getChunkX() - left.getChunkX()) + smoothingRadiusInChunks;

    	Object[] returnValue = { ((BO4)this.start.getObject()).getConfig().minimumSizeTop, ((BO4)this.start.getObject()).getConfig().minimumSizeRight, ((BO4)this.start.getObject()).getConfig().minimumSizeBottom, ((BO4)this.start.getObject()).getConfig().minimumSizeLeft };

    	if(OTG.getPluginConfig().spawnLog)
    	{
    		OTG.log(LogMarker.INFO, "");
        	OTG.log(LogMarker.INFO, this.start.getObject().getName() + " minimum size: Width " + ((Integer)returnValue[1] + (Integer)returnValue[3] + 1) + " Length " + ((Integer)returnValue[0] + (Integer)returnValue[2] + 1) + " top " + (Integer)returnValue[0] + " right " + (Integer)returnValue[1] + " bottom " + (Integer)returnValue[2] + " left " + (Integer)returnValue[3]);
    	}

    	objectsToSpawn.clear();

    	return returnValue;
    }

    private void calculateBranches(boolean minimumSize, LocalWorld world, ArrayList<String> targetBiomes, ChunkCoordinate chunkBeingPopulated) throws InvalidConfigException
    {
    	if(OTG.getPluginConfig().spawnLog)
    	{
	    	String sminimumSize = minimumSize ? " (minimumSize)" : "";
	    	OTG.log(LogMarker.INFO, "");
	    	OTG.log(LogMarker.INFO, "-------- CalculateBranches " + this.start.bo3Name + sminimumSize +" --------");
    	}

        BranchDataItem branchData = new BranchDataItem(random, null, (BO4CustomStructureCoordinate)this.start, null, 0, 0, minimumSize);

        if(OTG.getPluginConfig().spawnLog)
        {
        	OTG.log(LogMarker.INFO, "");
	        OTG.log(LogMarker.INFO, "---- Cycle 0 ----");
	        OTG.log(LogMarker.INFO, "Plotted X" + branchData.chunkCoordinate.getChunkX() + " Z" + branchData.chunkCoordinate.getChunkZ() + " - " + branchData.branch.getObject().getName());
        }

        addToCaches(branchData, ((BO4)branchData.branch.getObject()));       

    	Cycle = 0;
    	boolean canOverrideBranchesSpawned = false;
    	SpawningCanOverrideBranches = false;
    	boolean processingDone = false;
    	while(!processingDone)
    	{
    		spawnedBranchLastCycle = spawnedBranchThisCycle;
    		spawnedBranchThisCycle = false;

    		Cycle += 1;

    		if(OTG.getPluginConfig().spawnLog)
    		{
    			OTG.log(LogMarker.INFO, "");
    			OTG.log(LogMarker.INFO, "---- Cycle " + Cycle + " ----");
    		}

    		traverseAndSpawnChildBranches(branchData, minimumSize, true, world, targetBiomes, chunkBeingPopulated);

			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.INFO, "All branch groups with required branches only have been processed for cycle " + Cycle + ", plotting branch groups with optional branches.");
			}
			traverseAndSpawnChildBranches(branchData, minimumSize, false, world, targetBiomes, chunkBeingPopulated);

			processingDone = true;
            for(BranchDataItem branchDataItem3 : AllBranchesBranchData)
            {
            	if(!branchDataItem3.doneSpawning)
            	{
            		processingDone = false;
            		break;
            	}
            }

        	// CanOverride optional branches are spawned only after the main structure has spawned.
        	// This is useful for knocking out walls between rooms and adding interiors.
            if(processingDone && !canOverrideBranchesSpawned)
            {
            	canOverrideBranchesSpawned = true;
            	SpawningCanOverrideBranches = true;
            	processingDone = false;
	            for(BranchDataItem branchDataItem3 : AllBranchesBranchData)
	            {
	            	for(BranchDataItem childBranch : branchDataItem3.getChildren(false, world, chunkBeingPopulated))
	            	{
	            		if(
            				!childBranch.branch.isRequiredBranch &&
            				((BO4)childBranch.branch.getObject()).getConfig().canOverride
        				)
	            		{
	            			branchDataItem3.doneSpawning = false;
	            			childBranch.doneSpawning = false;
	            			childBranch.cannotSpawn = false;

	            			if(branchDataItem3.wasDeleted)
	            			{
	            				throw new RuntimeException(); // TODO: Remove after testing
	            			}

	            			if(childBranch.wasDeleted)
	            			{
	            				throw new RuntimeException(); // TODO: Remove after testing
	            			}
	            		}
	            	}
	            }
            }

    		if(branchData.cannotSpawn)
    		{
    			if(minimumSize)
    			{
    				if(OTG.getPluginConfig().spawnLog)
    				{
    					OTG.log(LogMarker.WARN, "Error: Branching BO4 " + this.start.bo3Name + " could not be spawned in minimum configuration (isRequiredBranch branches only).");
    				}
            		throw new InvalidConfigException("Error: Branching BO4 " + this.start.bo3Name + " could not be spawned in minimum configuration (isRequiredBranch branches only).");
    			}
    			
    	        AllBranchesBranchData.clear();
    	        AllBranchesBranchDataByChunk.clear();
    	    	AllBranchesBranchDataByName.clear();
    	    	AllBranchesBranchDataByGroup.clear();
    	        AllBranchesBranchDataHash.clear();
    			
    			return;
    		}
    	}

        for(BranchDataItem branchToAdd : AllBranchesBranchData)
        {
        	if(!branchToAdd.cannotSpawn)
        	{
        		if(branchToAdd.branch == null)
        		{
        			throw new RuntimeException(); // TODO: Remove after testing
        		}
        		addToChunk(branchToAdd.branch, branchToAdd.chunkCoordinate, objectsToSpawn);
        	}
        }
        
        AllBranchesBranchData.clear();
        AllBranchesBranchDataByChunk.clear();
    	AllBranchesBranchDataByName.clear();
    	AllBranchesBranchDataByGroup.clear();
        AllBranchesBranchDataHash.clear();
    }

    private void traverseAndSpawnChildBranches(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly, LocalWorld world, ArrayList<String> targetBiomes, ChunkCoordinate chunkBeingPopulated)
    {
    	if(!branchData.doneSpawning)
    	{
    		addBranches(branchData, minimumSize, false, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
    	} else {
    		if(!branchData.cannotSpawn)
    		{
    			for(BranchDataItem branchDataItem2 : branchData.getChildren(false, world, chunkBeingPopulated))
    			{
    				// BranchData.DoneSpawning can be set to true by a child branch
    				// that tried to spawn but couldnt
    				if(!branchDataItem2.cannotSpawn && branchData.doneSpawning)
    				{
    					traverseAndSpawnChildBranches(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
    				}
    			}
    		}
    	}
    }
    
	// Branches:
    // BO4 structures are rasterized and split into 16x16 chunks, each BO4 containing the blocks for one chunk. Branch syntax is used to glue all the 
    // chunks of a structure together and allows OTG to spawn one chunk at a time. Each BO4 can have branches that are used to place more BO4's at specified 
    // coordinates relative to the parent.   
    //
    // Procedural generation:
    // Branches can be randomised to create procedurally generated structures. OTG branch spawning mechanics work by the principle of spawning a basic (required) 
    // structure layout first, then spawning any optional additions/modifications on top. Different kinds of branches are used to create these.
    //
    // Branch spawn cycles:
    // Each structure starts with one master BO4, that can contain any number of branches and/or branch groups, that can in turn contain branches.
    // Branches are spawned in pulses/cycles, where each pulse/cycle spawns one layer of child-branches. Branches are spawned one at a time, in the order
    // they are listed in the BO4. There is a specific spawn order for different types of branches, this is explained in the Spawn order 
    // section below.
    //
    // Required and optional branches:
    // Required branches are used to mark parts of structures that must spawn together in the same cycle (such as a 2x2 chunk room, consisting of 4 BO4's 
    // that must all spawn), optional branches are used to create randomised components and add interiors and decorations. 
    //
    // Branch groups:
    // Branches can be assigned to a group. When a group contains multiple branches, OTG tries to spawn each of the branches. 
    // If a branch spawns, it considers the group successfully spawned and skips the remaining branches in the group.
    //
    // Spawn requirements and rollbacks:
    // Branches can have spawn requirements such as a biome or material to be placed upon (water/land), they can also use collision detection to make sure they 
    // don't overlap with other branches. When a required branch or branch group fails to spawn, its parent is rolled back, and spawning of the rolled-back branch's 
    // sibling branches continues as if the branch had originally failed to spawn. This will delete the current branch and in the parent branch tries to spawn any 
    // branches that couldn't spawn because of this branch. This means that each branch only tries to spawn once, if it fails, it is never tried again, even if the 
    // surrounding chunks/branches have changed and spawning might be possible. This is done to prevent infinite recursion. 
    // If rollbacks cascade up to the master BO4, the structure fails to spawn entirely.
    //
    // Branch depth:
    // For BO4's with optional branches, the master BO4 passes a branch depth to each of its branches. This number is passed down to each child branch, and is decreased 
    // by one for each optional branch. When branch depth is depleted, optional branches are no longer able to spawn and only required branches attempt to spawn.
    // This allows the maximum size of the structure's optional parts (such as tunnels with random length) to be set via the master BO4. Alternatively, branch depth can 
	// be overridden for each branch, this can be useful for spawning optional components at the ends of a branch, when the master BO4's branch depth is depleted (to cap the end of a tunnel f.e). 
    // Be careful not to create infinite loops when overriding the master BO4's branch depth. 
    // 
    // CanOverride branches:
    // CanOverride branches can override any other branch, so aren't affected by collision detection. CanOverride branches don't care about spawning on !canOverride branches, 
    // though !canOverride branches do care about spawning on canOverride branches.   
    //
    // CanOverride required branches:
    // CanOverride required branches are used for things that need to be spawned in the same cycle as their parent branch, and override/overlap with existing branches. 
    // Typically things that should be part of the base layout of the structure, for instance non-optional components that override/modify the base structure or marker BO4's 
    // used for collision detection.
    //
    // CanOverride optional branches:
    // Optional things that should be spawned after the base of the structure has spawned, and override/overlap with existing branches. For instance randomised room interiors, 
    // adapter/modifier pieces that knock out walls/ceilings/floors between rooms and creates stairs/bridges etc.
    //
    // Spawn order:
    // *CanOverride optional branches are completely ignored during step 1-3, that spawn the basic (required) structure layout.
    // 1. Traverse all spawned branches until a branch is found that has unspawned children and try spawning required branches, if optional branches 
    // exist in the same branchgroups (if any) that haven't tried to spawn yet, skip the required branches. 
    // 2. Traverse all spawned branches until a branch is found that has unspawned children and try spawning optional branches. If an optional branch spawns, 
    // immediately spawn its required branches (same as 1, skips required branches if optional branches queued).
    // 3. Repeat 1-2 until all branches have spawned or until a required branch (or branchgroup containing a required branch) cannot spawn, which triggers a rollback. If
    // rollbacks cascade up to the master BO4, cancel spawning the structure.
    // 4. Spawn CanOverride optional branches and their children: 
    // Optional CanOverride branches never cause a rollback for their parent, so are used to add optional additions/modification on top of existing branches, and are completely 
    // ignored during step 1-3. Step 4 repeats steps 1-3, starting at any unspawned optional CanOverride:true branches that were ignored before. Each optional CanOverride branch spawns 
    // its children as usual, until all branches are depleted. Optional CanOverride:true branches should not have other optional CanOverride:true branches as children, they are ignored.
    //
    // *NOTE: Optional CanOverride:true branches should not be placed in a branchgroup with required branches. If an optional CanOverride:true branch is placed in a group with a 
    // required branch, it is ignored during steps 1-3. During step 4, the branch will try to spawn, but won't cause a rollback for its branchgroup if it fails, since the branchgroup is 
    // guaranteed to have already spawned a branch during steps 1-3.
    //
    // *NOTE2: MustSpawnBelow branches retry spawning each cycle during steps 1-3 until no branch was spawned the last cycle. If they still can't spawn, they fail and can cause a rollback.
   
    // TODO: Create a new branch type that cannot cause rollbacks and waits till all other branches are done spawning. Make optional canoverride branches behave like other branches,
    // so they are no longer an exception to the rule. Allow the new branch type to chain as many times as desired, so you could spawn a foundation, then walls, then roads, then 
    // structures fe. Would also have to add a CollisionGroup setting for BO4's, to easily specify which branches can/can't collide.  

    // TODO: Remove the 16x16 requirement, allow users to use larger Bo4's, make OTG do the slicing.
    
    // TODO: Don't allow canOverride optional branches in the same branch group as required branches.
    private void addBranches(BranchDataItem branchDataItem, boolean minimumSize, boolean traverseOnlySpawnedChildren, boolean spawningRequiredBranchesOnly, LocalWorld world, ArrayList<String> targetBiomes, ChunkCoordinate chunkBeingPopulated)
    {
    	// CanOverride optional branches are spawned only after the main structure has spawned.
    	// This is useful for adding interiors and knocking out walls between rooms.
    	if(!SpawningCanOverrideBranches)
    	{
	    	for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false, world, chunkBeingPopulated))
	    	{
	    		if(
    				(
						!branchDataItem3.cannotSpawn ||
						!branchDataItem3.doneSpawning
					) && (
						((BO4)branchDataItem3.branch.getObject()).getConfig().canOverride &&
						!branchDataItem3.branch.isRequiredBranch
					)
				)
	    		{
	    			branchDataItem3.cannotSpawn = true;
	    			branchDataItem3.doneSpawning = true;
	    		}
	    	}
    	}

    	// Mark branch as done spawning if we know all branches will be done spawning at the end of this method.
    	// If we are spawning optional branches then we know this branch will be done spawning when this method returns
    	// (all optional branches will try to spawn, then if none have spawned any leftover required branches will try to spawn)
    	// and won't try to spawn anything in the second phase of this branch spawning cycle
    	if(!spawningRequiredBranchesOnly)// || isRollBack)
    	{
    		branchDataItem.doneSpawning = true;
    	} else {
    		// If we are spawning required branches then there might also
    		// be optional branches, which will not have had a chance to spawn when this method returns
    		// The second (optional branches) phase of this branch spawning cycle will call AddBranches on the branch for the
    		// second time to try to spawn them and will set DoneSpawning to true.
			boolean hasOnlyRequiredBranches = true;
			for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false, world, chunkBeingPopulated))
			{
				if(!branchDataItem3.branch.isRequiredBranch && !branchDataItem3.doneSpawning && !branchDataItem3.cannotSpawn)
				{
					hasOnlyRequiredBranches = false;
					break;
				}
			}
			if(hasOnlyRequiredBranches)
			{
				// if this branch has only required branches then we know
				// it won't be spawning anything in the second phase of
				// this branch spawning cycle
				branchDataItem.doneSpawning = true;
			}
    	}

    	if(!branchDataItem.cannotSpawn)
    	{    		
	        for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false, world, chunkBeingPopulated))
	        {        	
	        	if(!AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber) && !childBranchDataItem.spawnDelayed)
	        	{
		        	// Check if children should be spawned
	        		boolean canSpawn = true;

	        		boolean wasntBelowOther = false;
	        		boolean wasntInsideOther = false;
	        		boolean cannotSpawnInsideOther = false;
	        		boolean wasntOnWater = false;
	        		boolean wasOnWater = false;
	        		boolean spaceIsOccupied = false;
	        		boolean chunkIsIneligible = false;
        			boolean branchFrequencyNotPassed = false;
        			boolean branchFrequencyGroupsNotPassed = false;

        			BO4 bo3 = ((BO4)childBranchDataItem.branch.getObject());

        			if(bo3 == null || bo3.isInvalidConfig)
        			{
		        		childBranchDataItem.doneSpawning = true;
		        		childBranchDataItem.cannotSpawn = true;
		        		if(bo3 == null)
		        		{
		        			if(OTG.getPluginConfig().spawnLog)
		        			{
		        				OTG.log(LogMarker.WARN, "Error: Could not find BO3 file: " + childBranchDataItem.branch.bo3Name + ".BO3 which is a branch of " + branchDataItem.branch.bo3Name + ".BO3");
		        			}
		        		}
        			}

	        		if(childBranchDataItem.doneSpawning || childBranchDataItem.cannotSpawn)
	        		{
	        			continue;
	        		}

	        		// Before spawning any required branch make sure there are no optional branches in its branch group that haven't tried to spawn yet.
	        		// Skip the branch if there are still spawnable optional branches.
    	        	if(spawningRequiredBranchesOnly)
    	        	{
    	    			if(childBranchDataItem.branch.isRequiredBranch)
    	    			{
    		    			boolean hasOnlyRequiredBranches = true;
    		    			if(childBranchDataItem.branch.branchGroup != null && childBranchDataItem.branch.branchGroup.length() > 0)
    		    			{
	    		    			for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false, world, chunkBeingPopulated))
	    		    			{
	    		    				if(
    		    						!branchDataItem3.branch.isRequiredBranch &&
    		    						branchDataItem3.branch.branchGroup != null &&
    		    						branchDataItem3.branch.branchGroup.length() > 0 &&
    		    						childBranchDataItem.branch.branchGroup.equals(branchDataItem3.branch.branchGroup) &&
    		    						!branchDataItem3.wasDeleted &&
	    								!branchDataItem3.cannotSpawn &&
		    							!branchDataItem3.doneSpawning
		    						)
	    		    				{
	    		    					hasOnlyRequiredBranches = false;
	    		    					break;
	    		    				}
	    		    			}
    		    			}
    		    			if(!hasOnlyRequiredBranches)
    		    			{
    		    				continue;
    		    			}
    	    			} else {
    	    				continue;
    	    			}
    	        	}

    	        	// Check if there is enough branch depth left to spawn this branch
	        		if(canSpawn && (childBranchDataItem.maxDepth == 0 || childBranchDataItem.currentDepth > childBranchDataItem.maxDepth) && !childBranchDataItem.branch.isRequiredBranch)
	        		{
	        			canSpawn = false;
	        		}

	        		branchesTried += 1;

	        		// Ignore weightedbranches when measuring minimumSize
	        		if(minimumSize && childBranchDataItem.branch.isWeightedBranch)
	        		{
	        			childBranchDataItem.doneSpawning = true;
        				childBranchDataItem.cannotSpawn = true;
        				continue;
	        		}

	        		// Do spawn checks
	        		int smoothRadius = ((BO4)this.start.getObject()).getConfig().overrideChildSettings && bo3.getConfig().overrideChildSettings ? ((BO4)this.start.getObject()).getConfig().smoothRadius : bo3.getConfig().smoothRadius;
	        		if(smoothRadius == -1 || bo3.getConfig().smoothRadius == -1)
	        		{
	        			smoothRadius = 0;
	        		}

	        		Stack<BranchDataItem> collidingBranches = null;
	        		if(canSpawn)
	        		{
	        			if(!minimumSize && world.chunkHasDefaultStructure(this.worldRandom, childBranchDataItem.chunkCoordinate))
	        			{
	        				chunkIsIneligible = true;
	        				canSpawn = false;
	        			}

	        			if(!minimumSize && bo3.getConfig().mustBeInsideWorldBorders && !world.isInsideWorldBorder(childBranchDataItem.chunkCoordinate))
	        			{
	        				chunkIsIneligible = true;
	        				canSpawn = false;
	        			}
	        			
		        		if(canSpawn && !minimumSize && bo3.getConfig().spawnOnWaterOnly)
		    			{
		    				if(
		    					!(
	    							world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX(), world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX(), childBranchDataItem.chunkCoordinate.getBlockZ(), true, true, false, true, true, null), childBranchDataItem.chunkCoordinate.getBlockZ(), null).isLiquid() &&
		    						world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX(), world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX(), childBranchDataItem.chunkCoordinate.getBlockZ() + 15, true, true, false, true, true, null), childBranchDataItem.chunkCoordinate.getBlockZ() + 15, null).isLiquid() &&
		    						world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX() + 15, world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX() + 15, childBranchDataItem.chunkCoordinate.getBlockZ(), true, true, false, true, true, null), childBranchDataItem.chunkCoordinate.getBlockZ(), null).isLiquid() &&
		    						world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX() + 15, world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX() + 15, childBranchDataItem.chunkCoordinate.getBlockZ() + 15, true, true, false, true, true, null), childBranchDataItem.chunkCoordinate.getBlockZ() + 15, null).isLiquid()
		    					)
		    				)
		    				{
		    					wasntOnWater = true;
		    					canSpawn = false;
		    				}
		    			}
		        		
		        		if(canSpawn && !minimumSize && !bo3.getConfig().canSpawnOnWater)
		    			{
		    				if(world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX() + 8, world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX() + 8, childBranchDataItem.chunkCoordinate.getBlockZ() + 7, true, true, false, true, true, null), childBranchDataItem.chunkCoordinate.getBlockZ() + 7, null).isLiquid())
		    				{
		    					wasOnWater = true;
		    					canSpawn = false;
		    				}
		    			}
		        		
	        			if(canSpawn && bo3.getConfig().mustBeBelowOther)
	        			{
	        				canSpawn = checkMustBeBelowOther(childBranchDataItem);
	        				if(!canSpawn)
	        				{
	        					wasntBelowOther = true;
	        				}
	        			}

	        			if(canSpawn && bo3.getConfig().mustBeInsideBranches.size() > 0)
	        			{	        				
	        				canSpawn = checkMustBeInside(childBranchDataItem, bo3);
	        				if(!canSpawn)
	        				{
	        					wasntInsideOther = true;
	        				}
	        			}

	        			if(canSpawn && bo3.getConfig().cannotBeInsideBranches.size() > 0)
	        			{
	        				canSpawn = checkCannotBeInside(childBranchDataItem, bo3);
	        				if(!canSpawn)
	        				{
	        					cannotSpawnInsideOther = true;
	        				}
	        			}

	        		    if(canSpawn && bo3.getConfig().branchFrequency > 0)
	        		    {
	        		    	canSpawn = checkBranchFrequency(childBranchDataItem, bo3);
	        		    	if(!canSpawn)
	        		    	{
	        		    		branchFrequencyNotPassed = true;
	        		    	}
	        		    }
	        		    
	        		    if(canSpawn && bo3.getConfig().branchFrequencyGroups.size() > 0)
	        		    {
	        		    	canSpawn = checkBranchFrequencyGroups(childBranchDataItem, bo3);
	        		    	if(!canSpawn)
	        		    	{
	        		    		branchFrequencyGroupsNotPassed = true;
	        		    	}
	        		    }	        		    

	        		    if(!minimumSize && canSpawn)
	        		    {
	        	    		if(!checkYBounds(childBranchDataItem.branch))
	        	    		{
		    					canSpawn = false;
        						chunkIsIneligible = true;
	        	    		}
	        		    }
	        		    
	        		    if(!minimumSize && canSpawn)
	        		    {
	        			    // Check if any other structures in the world are in this chunk
	        			    if(
        			    		(
    			    				world.isInsidePregeneratedRegion(childBranchDataItem.chunkCoordinate) || 
    			    				world.getStructureCache().bo4StructureCache.containsKey(childBranchDataItem.chunkCoordinate)
			    				)
    			    		)
	        			    {
		    					canSpawn = false;
        						chunkIsIneligible = true;
	        			    }
	        		    }
	        	    	
	        			if(canSpawn)
	        			{
	        				// Returns null if the branch cannot spawn in the given biome or if there's another BO4 structure in the chunk, otherwise returns colliding branches. 
	        				// CanOverride branches never collide with other branches, but may be unable to spawn if there's not enough space for smoothing areas.
	    					collidingBranches = checkSpawnRequirementsAndCollisions(childBranchDataItem, minimumSize, world, targetBiomes);
	    					if(collidingBranches == null)
	    					{
		    					canSpawn = false;
        						chunkIsIneligible = true;
	    					}
	    					else if(collidingBranches.size() > 0)
	        				{
	    						// !CanOverride branch has a collision, can't spawn.
		    					canSpawn = false;
		    					spaceIsOccupied = true;	    					
	        				}
	        			}
	        		}

	        		// Spawn the branch, for optional branches, this will immediately try to spawn any required child-branches, which may fail.
	        		// As usual, if there are optional branches in the same branchgroups as required branches, the required branches won't try to spawn this cycle.
		        	if(canSpawn)
		        	{
		        		if(OTG.getPluginConfig().spawnLog)
		        		{
			        		String allParentsString = "";
			        		BranchDataItem tempBranch = childBranchDataItem;
			        		while(tempBranch.parent != null)
			        		{
			        			allParentsString += " <-- X" + tempBranch.parent.branch.getChunkX() + " Z" + tempBranch.parent.branch.getChunkZ() + " Y" + tempBranch.parent.branch.getY() + " " + tempBranch.parent.branch.bo3Name + ":" + tempBranch.parent.branch.getRotation();
			        			tempBranch = tempBranch.parent;
			        		}

			        		OTG.log(LogMarker.INFO, "Plotted X" + childBranchDataItem.chunkCoordinate.getChunkX() + " Z" + childBranchDataItem.chunkCoordinate.getChunkZ() + (minimumSize ? "" : " Y" + (childBranchDataItem.branch.getY())) + " " +  childBranchDataItem.branch.bo3Name + ":" + childBranchDataItem.branch.getRotation() + (childBranchDataItem.branch.isRequiredBranch ? " required" : " optional") + " cycle " + Cycle + allParentsString);
		        		}

	        	    	if(childBranchDataItem.getChildren(false, world, chunkBeingPopulated).size() == 0)
	        	    	{
	        	    		childBranchDataItem.doneSpawning = true;
	        	    	}

	        	    	// Mark any branches in the same branch group so they wont try to spawn
		        		for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false, world, chunkBeingPopulated))
		        		{
		        			if(
	        					childBranchDataItem2 != childBranchDataItem &&
	    						(childBranchDataItem.branch.branchGroup != null && childBranchDataItem.branch.branchGroup.length() >= 0) &&
	        					childBranchDataItem.branch.branchGroup.equals(childBranchDataItem2.branch.branchGroup)
        					)
		        			{
		        				childBranchDataItem2.doneSpawning = true;
		        				childBranchDataItem2.cannotSpawn = true;
        					}
		        		}

		        		spawnedBranchThisCycle = true;

		        		addToCaches(childBranchDataItem, bo3);		        		

		        		// If an optional branch spawns then immediately spawn its required branches as well (if any)
		        		// If this causes a rollback the rollback will stopped at this branch and we can resume spawning
		        		// the current branch's children as if it was unable to spawn.
		        		if(
	        				!spawningRequiredChildrenForOptionalBranch &&
	        				!childBranchDataItem.branch.isRequiredBranch
        				)
		        		{
		        			if(OTG.getPluginConfig().spawnLog)
		        			{
		        				OTG.log(LogMarker.INFO, "Plotting all required child branches that are not in a branch group with optional branches.");
		        			}

		        			spawningRequiredChildrenForOptionalBranch = true;
        					currentSpawningRequiredChildrenForOptionalBranch = childBranchDataItem;
			        		traverseAndSpawnChildBranches(childBranchDataItem, minimumSize, true, world, targetBiomes, chunkBeingPopulated);
			        		spawningRequiredChildrenForOptionalBranch = false;

			        		// Make sure the branch wasn't rolled back because the required branches couldn't spawn.
			        		// TODO: Make traverseAndSpawnChildBranches return bool instead?
			        		boolean bFound = false;
			        		ArrayList<BranchDataItem> branchDataItemStack = AllBranchesBranchDataByChunk.get(childBranchDataItem.chunkCoordinate);
			        		if(branchDataItemStack != null)
			        		{
			        			for(BranchDataItem b : branchDataItemStack)
			        			{
			        				if(b == childBranchDataItem)
			        				{
			        					bFound = true;
			        					break;
			        				}
			        			}
			        		}
			        		canSpawn = bFound;

		        			if(OTG.getPluginConfig().spawnLog)
		        			{
		        				OTG.log(LogMarker.INFO, "Done spawning required children for optional branch X" + childBranchDataItem.chunkCoordinate.getChunkX() + " Z" + childBranchDataItem.chunkCoordinate.getChunkZ() + (minimumSize ? "" : " Y" + (childBranchDataItem.branch.getY())) + " " +  childBranchDataItem.branch.bo3Name + ":" + childBranchDataItem.branch.getRotation());
		        			}
		        		}
		        		// If AddBranches was called during a rollback then only traverse branches for children that spawn during this call
		        		// Otherwise existing branches could have their children spawn more than once per cycle
		        		else if(
	        				traverseOnlySpawnedChildren &&
	        				!spawningRequiredChildrenForOptionalBranch &&
	        				childBranchDataItem.branch.isRequiredBranch
        				)
		        		{
			        		traverseAndSpawnChildBranches(childBranchDataItem, minimumSize, true, world, targetBiomes, chunkBeingPopulated);
		        		}
		        	}

		        	if(!canSpawn)
		        	{
		        		if(!childBranchDataItem.doneSpawning && !childBranchDataItem.cannotSpawn)
		        		{
		        			// WasntBelowOther branches that cannot spawn get to retry
		        			// each cycle until no branch spawned last cycle
		        			if(!wasntBelowOther || !spawnedBranchLastCycle)
		        			{
				        		childBranchDataItem.doneSpawning = true;
				        		childBranchDataItem.cannotSpawn = true;
		        			} else {
		        				branchDataItem.doneSpawning = false;
		        				if(branchDataItem.wasDeleted)
		        				{
		        					throw new RuntimeException(); // TODO: Remove after testing
		        				}
		        			}

			        		boolean bBreak = false;

			        		boolean branchGroupFailedSpawning = false;
			        		if(childBranchDataItem.branch.isRequiredBranch)
			        		{
			        			branchGroupFailedSpawning = true;

			        	    	// Check if there are any more branches in this group that haven't tried to spawn yet.
			        			// *At this point, all optional branches should have had a chance to spawn.
			        			for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false, world, chunkBeingPopulated))
				        		{
				        			if(
			        					childBranchDataItem2 != childBranchDataItem &&
			    						(childBranchDataItem.branch.branchGroup != null && childBranchDataItem.branch.branchGroup.length() >= 0) &&
			        					childBranchDataItem.branch.branchGroup.equals(childBranchDataItem2.branch.branchGroup) &&
				        				!childBranchDataItem2.doneSpawning &&
				        				!childBranchDataItem2.cannotSpawn
		        					)
				        			{
				        				branchGroupFailedSpawning = false;
				        				break;
		        					}
				        		}
			        		}

			        		// If the branch group can't spawn, roll back this branch. Otherwise, check if there are other branches
	        				// with the same spawn requirements and mark them so they won't try to spawn. If no branches in a branch 
			        		// group can be spawned, roll back this branch.
			        		if((!wasntBelowOther || !spawnedBranchLastCycle) && branchGroupFailedSpawning)
			        		{
			            		// Branch could not spawn, abort this branch because it contains a branch group that could not be spawned.

			            		if(OTG.getPluginConfig().spawnLog)
			            		{
			        	    		String allParentsString = "";
			        	    		BranchDataItem tempBranch = branchDataItem;
			        	    		while(tempBranch.parent != null)
			        	    		{
			        	    			allParentsString += " <-- X" + tempBranch.parent.branch.getChunkX() + " Z" + tempBranch.parent.branch.getChunkZ() + " Y" + tempBranch.parent.branch.getY() + " " + tempBranch.parent.branch.bo3Name + ":" + tempBranch.parent.branch.getRotation();
			        	    			tempBranch = tempBranch.parent;
			        	    		}

			        	    		String occupiedByObjectsString = "";
			        	    		if(spaceIsOccupied)
			        	    		{
			        	    			for(BranchDataItem collidingObject : collidingBranches)
			        	    			{
			        	    				String occupiedByObjectString = collidingObject.branch.bo3Name + ":" + collidingObject.branch.getRotation() + " X" + collidingObject.branch.getChunkX() + " Z" + collidingObject.branch.getChunkZ() + " Y" + collidingObject.branch.getY();
					        	    		tempBranch = collidingObject;
					        	    		while(tempBranch.parent != null)
					        	    		{
					        	    			occupiedByObjectString += " <-- X" + tempBranch.parent.branch.getChunkX() + " Z" + tempBranch.parent.branch.getChunkZ() + " Y" + tempBranch.parent.branch.getY() + " " + tempBranch.parent.branch.bo3Name + ":" + tempBranch.parent.branch.getRotation();
					        	    			tempBranch = tempBranch.parent;
					        	    		}
					        	    		occupiedByObjectsString += " " + occupiedByObjectString;
			        	    			}
			        	    		}

			        	    		String reason = (branchFrequencyGroupsNotPassed ? "BranchFrequencyGroupNotPassed " : "") + (branchFrequencyNotPassed ? "BranchFrequencyNotPassed " : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (wasntInsideOther ? "WasntInsideOther " : "") + (cannotSpawnInsideOther ? "CannotSpawnInsideOther " : "") + (wasntOnWater ? "WasntOnWater " : "") + (wasOnWater ? "WasOnWater " : "") + (!branchFrequencyGroupsNotPassed && !branchFrequencyNotPassed && !wasntBelowOther && !cannotSpawnInsideOther && !wasntOnWater && !wasOnWater && !wasntBelowOther && !chunkIsIneligible && spaceIsOccupied ? "SpaceIsOccupied by" + occupiedByObjectsString : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (chunkIsIneligible ? "TerrainIsUnsuitable (StartChunkBlockChecks (height or material) not passed or Y < 0 or Frequency/BO3Group checks not passed or BO3 collided with other CustomStructure or smoothing area collided with other CustomStructure or BO3 not in allowed Biome or Smoothing area not in allowed Biome)" : "");
			        	    		OTG.log(LogMarker.INFO, "Rolling back X" + branchDataItem.branch.getChunkX() + " Z" + branchDataItem.branch.getChunkZ() + " Y" + branchDataItem.branch.getY() + " " + branchDataItem.branch.bo3Name + ":" + branchDataItem.branch.getRotation() + allParentsString + " because required branch "+ childBranchDataItem.branch.bo3Name + " couldn't spawn. Reason: " + reason);
			            		}

		            			rollBackBranch(branchDataItem, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
		            			bBreak = true;
			        		} else {
				        		// if this child branch could not spawn then in some cases other child branches won't be able to either
				        		// mark those child branches so they dont try to spawn and roll back the whole branch if a required branch can't spawn
			        			// mustBeBelowOther / spawnOnWaterOnly / canSpawnOnWater
				        		for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false, world, chunkBeingPopulated))
				        		{
				        			if(!wasntBelowOther || !spawnedBranchLastCycle)
				        			{
					        			if(
				        					childBranchDataItem == childBranchDataItem2 ||
					        				(
				        						!(childBranchDataItem2.cannotSpawn || childBranchDataItem2.doneSpawning) &&
				        						(
			        								(
		        										childBranchDataItem.branch.getY() < 0 ||
		        										chunkIsIneligible ||
		        										(wasntBelowOther && ((BO4)childBranchDataItem2.branch.getObject()).getConfig().mustBeBelowOther) ||
		        										(wasntOnWater && ((BO4)childBranchDataItem2.branch.getObject()).getConfig().spawnOnWaterOnly) ||
		        										(wasOnWater && !((BO4)childBranchDataItem2.branch.getObject()).getConfig().canSpawnOnWater)
			        								) &&
			        								childBranchDataItem.branch.getX() == childBranchDataItem2.branch.getX() &&
			        								childBranchDataItem.branch.getY() == childBranchDataItem2.branch.getY() &&
			        								childBranchDataItem.branch.getZ() == childBranchDataItem2.branch.getZ()
					        					)
				        					)
			        					)
					        			{
					        				childBranchDataItem2.doneSpawning = true;
					        				childBranchDataItem2.cannotSpawn = true;

							        		branchGroupFailedSpawning = false;
							        		if(childBranchDataItem2.branch.isRequiredBranch)
							        		{
							        			branchGroupFailedSpawning = true;

							        	    	// Check if there are any more branches in this group that haven't tried to spawn yet.
							        			// *At this point, all optional branches should have had a chance to spawn.
								        		for(BranchDataItem childBranchDataItem3 : branchDataItem.getChildren(false, world, chunkBeingPopulated))
								        		{
								        			if(
							        					childBranchDataItem3 != childBranchDataItem2 &&
							    						(childBranchDataItem2.branch.branchGroup != null && childBranchDataItem2.branch.branchGroup.length() >= 0) &&
							    						childBranchDataItem2.branch.branchGroup.equals(childBranchDataItem3.branch.branchGroup) &&
								        				!childBranchDataItem3.doneSpawning &&
								        				!childBranchDataItem3.cannotSpawn
						        					)
								        			{
								        				branchGroupFailedSpawning = false;
								        				break;
						        					}
								        		}
							        		}

					        				if(branchGroupFailedSpawning)
					        				{
							            		if(OTG.getPluginConfig().spawnLog)
							            		{
							        	    		String allParentsString = "";
							        	    		BranchDataItem tempBranch = branchDataItem;
							        	    		while(tempBranch.parent != null)
							        	    		{
							        	    			allParentsString += " <-- X" + tempBranch.parent.branch.getChunkX() + " Z" + tempBranch.parent.branch.getChunkZ() + " Y" + tempBranch.parent.branch.getY() + " " + tempBranch.parent.branch.bo3Name + ":" + tempBranch.parent.branch.getRotation();
							        	    			tempBranch = tempBranch.parent;
							        	    		}

							        	    		String occupiedByObjectsString = "";
							        	    		if(spaceIsOccupied)
							        	    		{
							        	    			for(BranchDataItem collidingObject : collidingBranches)
							        	    			{
							        	    				String occupiedByObjectString = collidingObject.branch.bo3Name + ":" + collidingObject.branch.getRotation() + " X" + collidingObject.branch.getChunkX() + " Z" + collidingObject.branch.getChunkZ() + " Y" + collidingObject.branch.getY();
									        	    		tempBranch = collidingObject;
									        	    		while(tempBranch.parent != null)
									        	    		{
									        	    			occupiedByObjectString += " <-- X" + tempBranch.parent.branch.getChunkX() + " Z" + tempBranch.parent.branch.getChunkZ()+ " Y" + tempBranch.parent.branch.getY() + " " + tempBranch.parent.branch.bo3Name + ":" + tempBranch.parent.branch.getRotation();
									        	    			tempBranch = tempBranch.parent;
									        	    		}
									        	    		occupiedByObjectsString += " " + occupiedByObjectString;
							        	    			}
							        	    		}

							        	    		String reason =
							        	    				(branchFrequencyGroupsNotPassed ? "BranchFrequencyGroupNotPassed " : "") +
							        	    				(branchFrequencyNotPassed ? "BranchFrequencyNotPassed " : "") +				
							        	    				(wasntBelowOther ? "WasntBelowOther " : "") +
							        	    				(wasntInsideOther ? "WasntInsideOther " : "") +
							        	    				(cannotSpawnInsideOther ? "CannotSpawnInsideOther " : "") +
							        	    				(wasntOnWater ? "WasntOnWater " : "") +
							        	    				(wasOnWater ? "WasOnWater " : "") +
							        	    				(childBranchDataItem.branch.getY() < 0 ? " WasBelowY0 " : "") +
							        	    				(!branchFrequencyGroupsNotPassed && !branchFrequencyNotPassed && !wasntBelowOther && !cannotSpawnInsideOther && !wasntOnWater && !wasOnWater && !wasntBelowOther && !chunkIsIneligible && spaceIsOccupied ? "SpaceIsOccupied by" + occupiedByObjectsString : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (chunkIsIneligible ? "ChunkIsIneligible: Either the chunk is occupied by another structure or a default structure, or the BO3/smoothing area is not allowed in the Biome)" : "");
							        	    		OTG.log(LogMarker.INFO, "Rolling back X" + branchDataItem.branch.getChunkX() + " Z" + branchDataItem.branch.getChunkZ() + " Y" + branchDataItem.branch.getY() + " " + branchDataItem.branch.bo3Name + ":" + branchDataItem.branch.getRotation() + allParentsString + " because required branch "+ childBranchDataItem.branch.bo3Name + " couldn't spawn. Reason: " + reason);
							            		}
						            			rollBackBranch(branchDataItem, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
						            			bBreak = true;
						            			break;
					        				}
					        			}
				        			}
				        		}
			        		}
			        		if(bBreak)
			        		{
			        			break;
			        		}
		        		}
		        	}
	        	}
	        	else if(childBranchDataItem.spawnDelayed)
	        	{
	        		childBranchDataItem.spawnDelayed = false;
	        	}
	        }

    		// when spawning optional branches spawn them first then traverse any previously spawned required branches
	        // When calling AddBranches during a rollback to continue spawning a branch group, don't traverse already spawned children (otherwise the branch could spawn children more than once per cycle).
	        if(
        		!traverseOnlySpawnedChildren &&
        		!spawningRequiredBranchesOnly &&
        		!branchDataItem.cannotSpawn
    		)
	        {
	        	for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false, world, chunkBeingPopulated))
	        	{
	        		if(AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber))
	        		{
						if(
							(
								childBranchDataItem.branch.isRequiredBranch ||
								(
									SpawningCanOverrideBranches &&
									!((BO4)childBranchDataItem.branch.getObject()).getConfig().canOverride
								)
							) &&
							!childBranchDataItem.cannotSpawn &&
							(
								!childBranchDataItem.spawnDelayed ||
								!spawnedBranchLastCycle
							)
						)
						{
							traverseAndSpawnChildBranches(childBranchDataItem, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
						}
	        		}
	        	}
	        }

	        // When calling AddBranches during a rollback to continue spawning a branch group don't traverse already spawned children (otherwise the branch could spawn children more than once per cycle).
	        if(
        		!traverseOnlySpawnedChildren &&
        		spawningRequiredBranchesOnly &&
        		!branchDataItem.cannotSpawn
    		)
	        {
	        	for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false, world, chunkBeingPopulated))
	        	{
	        		if(AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber))
	        		{
						if(childBranchDataItem.branch.isRequiredBranch)
						{
							traverseAndSpawnChildBranches(childBranchDataItem, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
						}
	        		}
	        	}
	        }
    	}
    }

    private boolean checkBranchFrequency(BranchDataItem childBranchDataItem, BO4 bo3)
    {
    	boolean branchFrequencyPassed = true;
        // Check if no other branch of the same type (filename) is within the minimum radius (branch frequency)
		int radius = bo3.getConfig().branchFrequency;
		if(radius > 0)
		{
			float distanceBetweenBranches = 0;
			
			ArrayList<ChunkCoordinate> chunkCoords = AllBranchesBranchDataByName.get(bo3.getName());
			if(chunkCoords != null)
			{
            	// Check BO3 frequency
       			for(ChunkCoordinate cachedChunk : chunkCoords)
    			{
                    // Find distance between two points
       				distanceBetweenBranches = (int)Math.floor(Math.sqrt(Math.pow(childBranchDataItem.chunkCoordinate.getChunkX() - cachedChunk.getChunkX(), 2) + Math.pow(childBranchDataItem.chunkCoordinate.getChunkZ() - cachedChunk.getChunkZ(), 2)));
                    if (distanceBetweenBranches <= radius)
                    {
                    	// Other branch of the same type is too nearby, cannot spawn here!
                    	branchFrequencyPassed = false;
                        break;
                    }
    			}				
			}
		}
		return branchFrequencyPassed;
	}

    private boolean checkBranchFrequencyGroups(BranchDataItem childBranchDataItem, BO4 bo3)
    {	
    	boolean branchFrequencyGroupsPassed = true;
		// Check if no other branches that are a member of the same branch frequency group as this branch are within the minimum radius (branch group frequency)
		if(bo3.getConfig().branchFrequencyGroups.size() > 0)
		{
	    	int radius = bo3.getConfig().branchFrequency;
        	float distanceBetweenStructures = 0;
        	int cachedChunkRadius = 0;
        	ChunkCoordinate cachedChunk = null;
        	for(Entry<String, Integer> entry : bo3.getConfig().branchFrequencyGroups.entrySet())
        	{
        		HashMap<ChunkCoordinate, ArrayList<Integer>> spawnedStructure = AllBranchesBranchDataByGroup.get(entry.getKey());
        		if(spawnedStructure != null)
        		{
        			for(Entry<ChunkCoordinate, ArrayList<Integer>> cachedChunkEntry : spawnedStructure.entrySet())
        			{
        				cachedChunk = cachedChunkEntry.getKey();
        				cachedChunkRadius = 0;
        				for(Integer integer : cachedChunkEntry.getValue())
        				{
        					if(integer.intValue() > cachedChunkRadius)
        					{
        						cachedChunkRadius = integer.intValue();	
        					}
        				}
        				radius = entry.getValue().intValue() >= cachedChunkRadius ? entry.getValue().intValue() : cachedChunkRadius;
                        // Find distance between two points
        				distanceBetweenStructures = (int)Math.floor(Math.sqrt(Math.pow(childBranchDataItem.chunkCoordinate.getChunkX() - cachedChunk.getChunkX(), 2) + Math.pow(childBranchDataItem.chunkCoordinate.getChunkZ() - cachedChunk.getChunkZ(), 2)));
                        if (distanceBetweenStructures <= radius)
                        {
	    					// Branch with same branchFrequencyGroup was closer than branchFrequencyGroup's frequency in chunks, don't spawn
                        	branchFrequencyGroupsPassed = false;
                        	break;
                        }
        			}
        			if(!branchFrequencyGroupsPassed)
        			{
        				break;
        			}
        		}
        	}
		}
		return branchFrequencyGroupsPassed;
	}
    
	private boolean checkMustBeBelowOther(BranchDataItem childBranchDataItem)
    {
		// Check for mustBeBelowOther
		boolean bFoundOther = false;
		if(AllBranchesBranchDataByChunk.containsKey(childBranchDataItem.chunkCoordinate))
		{
			for(BranchDataItem branchDataItem2 : AllBranchesBranchDataByChunk.get(childBranchDataItem.chunkCoordinate))
			{
				if(
					branchDataItem2.chunkCoordinate.equals(childBranchDataItem.chunkCoordinate) &&
					!((BO4) branchDataItem2.branch.getObject()).getConfig().canOverride &&
					branchDataItem2.branch.getY() >= childBranchDataItem.branch.getY()
				)
				{
					bFoundOther = true;
					break;
				}
			}
		}
		return bFoundOther;
	}

	/**
     * 
     * @param childBranchDataItem
     * @param bo3
     * @return True if the branch can spawn
     */
    private boolean checkCannotBeInside(BranchDataItem childBranchDataItem, BO4 bo3)
    {
		boolean foundSpawnBlocker = false;
		if(AllBranchesBranchDataByChunk.containsKey(childBranchDataItem.chunkCoordinate))
		{
			ArrayList<BranchDataItem> branchDataInChunk = AllBranchesBranchDataByChunk.get(childBranchDataItem.chunkCoordinate);
			for(String cantBeInsideBO3 : bo3.getConfig().cannotBeInsideBranches)
			{
    			for(BranchDataItem branchDataItem3 : branchDataInChunk)
				{
					if(branchDataItem3 != childBranchDataItem && branchDataItem3 != childBranchDataItem.parent)
					{
						for(String branchName : ((BO4)branchDataItem3.branch.getObject()).getConfig().getInheritedBO3s()) // getInheritedBO3s also contains this BO3
						{
							if(branchName.equals(cantBeInsideBO3))
							{
   	    						if(checkCollision(childBranchDataItem.branch, branchDataItem3.branch))
   	    						{
   	     	        				if(OTG.getPluginConfig().spawnLog)
   	    	        				{
   	     	        					OTG.log(LogMarker.INFO, "CannotBeInside branch " + childBranchDataItem.branch.bo3Name + " was blocked by " + branchDataItem3.branch.bo3Name);
   	    	        				}
   	     	        				foundSpawnBlocker = true;
   	    							break;
   	    						}
							}
						}
   						if(foundSpawnBlocker)
   						{
   							break;
   						}
					}
				}
    			if(foundSpawnBlocker)
    			{
					break;
    			}
    		}
		}
		return !foundSpawnBlocker;
	}

	private boolean checkMustBeInside(BranchDataItem childBranchDataItem, BO4 bo3)
	{
		// AND/OR is supported, comma is OR, space is and, f.e: branch1, branch2 branch3, branch 4.
		if(AllBranchesBranchDataByChunk.containsKey(childBranchDataItem.chunkCoordinate))
		{		    	    			
			ArrayList<BranchDataItem> branchDataInChunk = AllBranchesBranchDataByChunk.get(childBranchDataItem.chunkCoordinate);			
			for(String mustBeInsideBO3 : bo3.getConfig().mustBeInsideBranches)
			{
				boolean foundSpawnRequirement = true;
				String[] andSwitch = mustBeInsideBO3.split(" ");
				boolean bFoundPart = false;
				for(String mustBeInsideBO3Name : andSwitch)
				{
					bFoundPart = false;
	    			for(BranchDataItem branchDataItem3 : branchDataInChunk)
					{
						if(branchDataItem3 != childBranchDataItem && branchDataItem3 != childBranchDataItem.parent)
						{
							for(String branchName : ((BO4)branchDataItem3.branch.getObject()).getConfig().getInheritedBO3s()) // getInheritedBO3s also contains this BO3
							{
								if(branchName.equals(mustBeInsideBO3Name))
								{
	   	    						if(checkCollision(childBranchDataItem.branch, branchDataItem3.branch))
	   	    						{
	   	    							bFoundPart = true;
	   	    							break;
	   	    						}
								}
							}
	   						if(bFoundPart)
	   						{
	   							break;
	   						}
						}
					}
	    			if(!bFoundPart)
	    			{
	    				foundSpawnRequirement = false;
	    			}
				}
				if(foundSpawnRequirement)
				{
					return true;
				}
    		}
		}
		return false;
	}

	private void rollBackBranch(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly, LocalWorld world, ArrayList<String> targetBiomes, ChunkCoordinate chunkBeingPopulated)
    {
    	// When spawning an optional branch its required branches are spawned immediately as well (if there are no optional branches in the same branchGroup)
    	// This can cause a rollback if the required branches cannot spawn. Make sure that the parent branch of the optional branch isn't rolled back since it
    	// is currently still being processed and is spawning its optional branches.
    	if(spawningRequiredChildrenForOptionalBranch && currentSpawningRequiredChildrenForOptionalBranch.parent == branchData)
    	{
    		return;
    	}

    	// Remove all children of this branch from AllBranchesBranchData
    	// And set this branches' CannotSpawn to true
    	// check if the parent has any required branches that cannot spawn
    	// and roll back until there is a viable branch pattern

    	branchData.cannotSpawn = true;
    	branchData.doneSpawning = true;

    	branchData.wasDeleted = true;

    	branchData.isBeingRolledBack = true;
    	deleteBranchChildren(branchData,minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);

    	if(AllBranchesBranchDataHash.contains(branchData.branchNumber))
    	{
    		if(OTG.getPluginConfig().spawnLog)
    		{
	    		String allParentsString = "";
	    		BranchDataItem tempBranch = branchData;
	    		while(tempBranch.parent != null)
	    		{
	    			allParentsString += " <-- X" + tempBranch.parent.branch.getChunkX() + " Z" + tempBranch.parent.branch.getChunkZ() + " Y" + tempBranch.parent.branch.getY() + " " + tempBranch.parent.branch.bo3Name + ":" + tempBranch.parent.branch.getRotation();
	    			tempBranch = tempBranch.parent;
	    		}
	    		OTG.log(LogMarker.INFO, "Deleted X" + branchData.branch.getChunkX() + " Z" + branchData.branch.getChunkZ() + " Y" + branchData.branch.getY() + " " + branchData.branch.bo3Name + ":" + branchData.branch.getRotation()  + (branchData.branch.isRequiredBranch ? " required" : " optional") + " cycle " + Cycle + allParentsString);
    		}

    		removeFromCaches(branchData);    	
    	}

    	if(!((BO4)branchData.branch.getObject()).getConfig().canOverride)
    	{
	    	// If this branch is allowing lower-lying .mustBeBelowOther branches to spawn then roll those back as well

    		ArrayList<BranchDataItem> allBranchesBranchData2 = new ArrayList<BranchDataItem>();
    		ArrayList<BranchDataItem> branchDataByChunk = AllBranchesBranchDataByChunk.get(branchData.chunkCoordinate);
    		if(branchDataByChunk != null)
    		{
	    		allBranchesBranchData2.addAll(branchDataByChunk);
	    		for(BranchDataItem branchDataItem2 : allBranchesBranchData2)
	    		{
	    			if(AllBranchesBranchDataHash.contains(branchDataItem2.branchNumber))
	    			{
		    			if(branchDataItem2 != branchData)
		    			{
			    			if(((BO4)branchDataItem2.branch.getObject()).getConfig().mustBeBelowOther && branchDataItem2.chunkCoordinate.equals(branchData.chunkCoordinate))
			    			{
			    				boolean branchAboveFound = false;
			    				for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(branchDataItem2.chunkCoordinate))
		    					{
			    					if(
		    							branchDataItem3 != branchData &&
		    							!((BO4)branchDataItem3.branch.getObject()).getConfig().mustBeBelowOther &&
		    							!((BO4)branchDataItem3.branch.getObject()).getConfig().canOverride &&
		    							branchDataItem3.chunkCoordinate.equals(branchDataItem2.chunkCoordinate)
									)
			    					{
			    						if(branchDataItem3.branch.getY() >= branchDataItem2.branch.getY())
			    						{
			    							branchAboveFound = true;
			    							break;
			    						}
			    					}
		    					}
			    				if(!branchAboveFound)
			    				{
			    					rollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
			    				}
			    			}
		    			}
	    			}
	    		}
    		}
    	}

    	// If this branch is allowing mustBeInside branches to spawn then roll those back as well
    	ArrayList<BranchDataItem> allBranchesBranchData2 = new ArrayList<BranchDataItem>();
    	ArrayList<BranchDataItem> branchDataByChunk = AllBranchesBranchDataByChunk.get(branchData.chunkCoordinate);
		if(branchDataByChunk != null)
		{
			allBranchesBranchData2.addAll(branchDataByChunk);
	    	for(BranchDataItem branchDataItem2 : allBranchesBranchData2)
	    	{
	    		if(AllBranchesBranchDataHash.contains(branchDataItem2.branchNumber))
	    		{
		    		if(branchDataItem2 != branchData)
					{
		    			if(
							((BO4)branchDataItem2.branch.getObject()).getConfig().mustBeInsideBranches.size() > 0 &&
							branchDataItem2.chunkCoordinate.equals(branchData.chunkCoordinate)
						)
		    			{
							boolean currentBO3Found = false;
							for(String mustBeInsideBO3Name : ((BO4)branchDataItem2.branch.getObject()).getConfig().mustBeInsideBranches)
							{
								for(String branchName : ((BO4)branchData.branch.getObject()).getConfig().getInheritedBO3s())
								{
									if(branchName.equals(mustBeInsideBO3Name))
									{
										currentBO3Found = true;
										break;
									}
								}
								if(currentBO3Found)
								{
									break;
								}
							}
							// The BO3 that is currently being rolled back may have been allowing this mustBeInside branch to spawn
							if(currentBO3Found) 
							{
								// Check if the branch can remain spawned without the branch we're rolling back
	    	    				if(!checkMustBeInside(branchDataItem2, ((BO4)branchDataItem2.branch.getObject())))
	    	    				{
	    	    					rollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
	    	    				}
							}
		    			}
					}
	    		}
	    	}
		}
		// if this branch is a required branch
		// then roll back the parent as well
		if(branchData.parent != null && !branchData.parent.isBeingRolledBack)
		{
    		if(branchData.branch.isRequiredBranch)
    		{
    			//OTG.log(LogMarker.INFO, "RollBackBranch 4: " + branchData.Parent.Branch.BO3Name + " <> " + branchData.Branch.BO3Name);
    			rollBackBranch(branchData.parent, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
    		} else {

    			// Mark for spawning the parent and all other branches in the same branch group that spawn after this branch (unless they have already been spawned successfully)
    			boolean parentDoneSpawning = true;
    			boolean currentBranchFound = false;
        		for (BranchDataItem branchDataItem2 : branchData.parent.getChildren(false, world, chunkBeingPopulated))
        		{
        			if(currentBranchFound)
        			{
        				if(
    						branchData.branch.branchGroup != null && branchData.branch.branchGroup.length() >= 0 &&
    						branchData.branch.branchGroup.equals(branchDataItem2.branch.branchGroup)
						)
        				{
	            			if(
            					!branchDataItem2.wasDeleted &&
            					!AllBranchesBranchDataHash.contains(branchDataItem2.branchNumber))
	            			{
	        					branchDataItem2.cannotSpawn = false;
	        					branchDataItem2.doneSpawning = false;
	            			}
        				}
        			}
        			if(branchDataItem2 == branchData)
        			{
        				currentBranchFound = true;
        			}
        			if(!branchDataItem2.doneSpawning && !branchDataItem2.cannotSpawn)
        			{
        				parentDoneSpawning = false;
        			}
        		}

        		// When rolling back after failing to spawn the required branches for an optional branch that just spawned don't roll back all the way to the optional
        		// branch's parent and continue spawning there. Instead only rollback up to the optional branch, then let the normal spawn cycle continue spawning the parent.
        		if(
    				!parentDoneSpawning &&
    				!(
						spawningRequiredChildrenForOptionalBranch &&
						currentSpawningRequiredChildrenForOptionalBranch == branchData
					)
				)
    			{
        			branchData.parent.doneSpawning = false;

	        		// Rollbacks only happen when:

        			if(!spawningRequiredChildrenForOptionalBranch)
        			{
        				if(spawningRequiredBranchesOnly)
        				{
                			// 1. The branch being rolled back has spawned all its required-only branch groups but not yet its optional branches and one of the required child branches
                			// (that spawn in the same cycle) failed to spawn one of its required children and is rolled back.
            				// AddBranches should be called for the parent of the branch being rolled back and its parent if a branch group failed to spawn (and so on).

                			// Since we're using SpawningRequiredBranchesOnly AddBranches can traverse all child branches without problems.
            				addBranches(branchData.parent, minimumSize, false, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
        				} else {
        					// 2. During the second phase of a cycle branch groups with optional branches are spawned, the optional branches get a chance to spawn first, after that the
        					// required branches try to spawn, if that fails the branch is rolled back.
        					// 3. A branch was rolled back that was a requirement for another branch (mustbeinside/mustbebelowother), causing the other branch to be rolled back as well.

                			// Since we're not using SpawningRequiredBranchesOnly AddBranches should only traverse child branches for any branches that it spawns from the branch group its re-trying.
        					// Otherwise some branches may have the same children traversed multiple times in a single phase.
            				addBranches(branchData.parent, minimumSize, true, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
        				}
        			} else {

        				// 4. While spawning required children for an optional branch (SpawningRequiredChildrenForOptionalBranch == true).
            			// AddBranches should be called only for children of the optional branch since they may have multiple required branches in the same branch group.

            			// In this case AddBranches should not set DoneSpawning to true on the branches (unless they have only required branches) to make sure that any optional
            			// branches are spawned in the second phase of the cycle.
        				if(!spawningRequiredBranchesOnly)
        				{
        					throw new RuntimeException();
        				}

        				spawningRequiredChildrenForOptionalBranch = false;
            			// Since we're using SpawningRequiredBranchesOnly AddBranches can traverse all child branches without problems.
        				addBranches(branchData.parent, minimumSize, false, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
        				spawningRequiredChildrenForOptionalBranch = true;
        			}
    			}
    		}
		}

    	branchData.isBeingRolledBack = false;
    }

    private void deleteBranchChildren(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly, LocalWorld world, ArrayList<String> targetBiomes, ChunkCoordinate chunkBeingPopulated)
    {
    	// Remove all children of this branch from AllBranchesBranchData
    	Stack<BranchDataItem> children = branchData.getChildren(true, world, chunkBeingPopulated);
        for(BranchDataItem branchDataItem : children)
        {
        	branchDataItem.cannotSpawn = true;
        	branchDataItem.doneSpawning = true;
        	branchDataItem.wasDeleted = true;

        	if(branchDataItem.getChildren(true, world, chunkBeingPopulated).size() > 0)
        	{
    			deleteBranchChildren(branchDataItem, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
        	}
        	if(AllBranchesBranchDataHash.contains(branchDataItem.branchNumber))
        	{
        		if(OTG.getPluginConfig().spawnLog)
        		{
	        		String allParentsString = "";
	        		BranchDataItem tempBranch = branchDataItem;
	        		while(tempBranch.parent != null)
	        		{
	        			allParentsString += " <-- X" + tempBranch.parent.branch.getChunkX() + " Z" + tempBranch.parent.branch.getChunkZ() + " Y" + tempBranch.parent.branch.getY() + " " + tempBranch.parent.branch.bo3Name + ":" + tempBranch.parent.branch.getRotation();
	        			tempBranch = tempBranch.parent;
	        		}

	        		OTG.log(LogMarker.INFO, "Deleted X" + branchDataItem.branch.getChunkX() + " Z" + branchDataItem.branch.getChunkZ() + " Y" + branchDataItem.branch.getY() + " " + branchDataItem.branch.bo3Name + ":" + branchDataItem.branch.getRotation() + (branchDataItem.branch.isRequiredBranch ? " required" : " optional") + " cycle " + Cycle + allParentsString);
        		}

        		removeFromCaches(branchDataItem);

	        	if(!((BO4)branchDataItem.branch.getObject()).getConfig().canOverride)
	        	{
	    	    	// If this branch is allowing lower-lying .mustBeBelowOther branches to spawn then roll those back as well
	        		ArrayList<BranchDataItem> allBranchesBranchData2 = new ArrayList<BranchDataItem>();
	        		ArrayList<BranchDataItem> branchDataByChunk = AllBranchesBranchDataByChunk.get(branchDataItem.chunkCoordinate);
	        		if(branchDataByChunk != null)
	        		{
		        		allBranchesBranchData2.addAll(branchDataByChunk);
		        		for(BranchDataItem branchDataItem2 : allBranchesBranchData2)
		        		{
		        			if(AllBranchesBranchDataHash.contains(branchDataItem2.branchNumber))
		        			{
			        			if(branchDataItem2 != branchDataItem)
			        			{
			    	    			if(((BO4)branchDataItem2.branch.getObject()).getConfig().mustBeBelowOther && branchDataItem2.chunkCoordinate.equals(branchDataItem.chunkCoordinate))
			    	    			{
			    	    				boolean branchAboveFound = false;
			    	    				for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(branchDataItem2.chunkCoordinate))
		    	    					{
			    	    					if(
			        							branchDataItem3 != branchDataItem &&
			        							!((BO4)branchDataItem3.branch.getObject()).getConfig().mustBeBelowOther &&
			        							!((BO4)branchDataItem3.branch.getObject()).getConfig().canOverride &&
			        							branchDataItem3.chunkCoordinate.equals(branchDataItem2.chunkCoordinate)
			    							)
			    	    					{
			    	    						if(branchDataItem3.branch.getY() >= branchDataItem2.branch.getY())
			    	    						{
			    	    							branchAboveFound = true;
			    	    							break;
			    	    						}
			    	    					}
		    	    					}
			    	    				if(!branchAboveFound)
			    	    				{
			    	    					rollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
			    	    				}
			    	    			}
			        			}
		        			}
		        		}
	        		}
	        	}

	        	ArrayList<BranchDataItem> allBranchesBranchData2 = new ArrayList<BranchDataItem>();
        		ArrayList<BranchDataItem> branchDataByChunk = AllBranchesBranchDataByChunk.get(branchDataItem.chunkCoordinate);
        		if(branchDataByChunk != null)
        		{
	        		allBranchesBranchData2.addAll(branchDataByChunk);
		        	// If this branch is allowing mustBeInside branches to spawn then roll those back as well
		        	for(BranchDataItem branchDataItem2 : allBranchesBranchData2)
		        	{
		        		if(AllBranchesBranchDataHash.contains(branchDataItem2.branchNumber))
		        		{
			        		if(branchDataItem2 != branchDataItem)
			    			{
			        			if(
			    					((BO4)branchDataItem2.branch.getObject()).getConfig().mustBeInsideBranches.size() > 0 &&
			    					branchDataItem2.chunkCoordinate.equals(branchDataItem.chunkCoordinate)
			    				)
			        			{
									boolean currentBO3Found = false;
									for(String mustBeInsideBO3Name : ((BO4)branchDataItem2.branch.getObject()).getConfig().mustBeInsideBranches)
									{
										for(String branchName : ((BO4)branchDataItem.branch.getObject()).getConfig().getInheritedBO3s())
										{
											if(branchName.equals(mustBeInsideBO3Name))
											{
												currentBO3Found = true;
												break;
											}
										}
										if(currentBO3Found)
										{
											break;
										}
									}
									// The BO3 that is currently being rolled back may have been allowing this mustBeInside branch to spawn
									if(currentBO3Found) 
									{
										// Check if the branch can remain spawned without the branch we're rolling back
		    	    					if(!checkMustBeInside(branchDataItem2, ((BO4)branchDataItem2.branch.getObject())))
			    	    				{
			    	    					rollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world, targetBiomes, chunkBeingPopulated);
			    	    				}
									}
			        			}
			    			}
		        		}
		        	}
        		}
        	}
        }
    }
    
    private void addToCaches(BranchDataItem branchData, BO4 bo3)
    {
    	AllBranchesBranchData.add(branchData);
    	AllBranchesBranchDataHash.add(branchData.branchNumber);
    	
		ArrayList<BranchDataItem> branchDataItemStack = AllBranchesBranchDataByChunk.get(branchData.chunkCoordinate);
		if(branchDataItemStack != null)
		{
			branchDataItemStack.add(branchData);
		} else {
			branchDataItemStack = new ArrayList<BranchDataItem>();
			branchDataItemStack.add(branchData);
			AllBranchesBranchDataByChunk.put(branchData.chunkCoordinate, branchDataItemStack);
		}

		ArrayList<ChunkCoordinate> sameNameBo3s = AllBranchesBranchDataByName.get(branchData.branch.bo3Name);
		if(sameNameBo3s == null)
		{
			sameNameBo3s = new ArrayList<ChunkCoordinate>();
			AllBranchesBranchDataByName.put(branchData.branch.bo3Name, sameNameBo3s);
		}
		sameNameBo3s.add(branchData.chunkCoordinate);
			
		// Get branch groups
		for(Entry<String, Integer> entry : bo3.getConfig().branchFrequencyGroups.entrySet())
		{
			HashMap<ChunkCoordinate, ArrayList<Integer>> branchGroupInfo = AllBranchesBranchDataByGroup.get(entry.getKey());
			if(branchGroupInfo == null)
			{
				branchGroupInfo = new HashMap<ChunkCoordinate, ArrayList<Integer>>();
				AllBranchesBranchDataByGroup.put(entry.getKey(), branchGroupInfo);
			}
			ArrayList<Integer> branchGroupFrequency = branchGroupInfo.get(branchData.chunkCoordinate);
			if(branchGroupFrequency == null)
			{
				branchGroupFrequency = new ArrayList<Integer>();
				branchGroupFrequency.add(entry.getValue());
				branchGroupInfo.put(branchData.chunkCoordinate, branchGroupFrequency);
			} else {
				branchGroupFrequency.add(entry.getValue());
			}
		}
    }
    
	private void removeFromCaches(BranchDataItem branchDataItem)
	{		
		AllBranchesBranchData.remove(branchDataItem);
		AllBranchesBranchDataHash.remove(branchDataItem.branchNumber);
		ArrayList<BranchDataItem> branchDataItemStack = AllBranchesBranchDataByChunk.get(branchDataItem.chunkCoordinate);
		if(branchDataItemStack != null)
		{
			branchDataItemStack.remove(branchDataItem);
			if(branchDataItemStack.size() == 0)
			{
				AllBranchesBranchDataByChunk.remove(branchDataItem.chunkCoordinate);
			}
			ArrayList<ChunkCoordinate> allCoordsForBo3 = AllBranchesBranchDataByName.get(branchDataItem.branch.bo3Name);
			allCoordsForBo3.remove(branchDataItem.chunkCoordinate);
			if(allCoordsForBo3.size() == 0)
			{
				AllBranchesBranchDataByName.remove(branchDataItem.branch.bo3Name);
			}
			for(Entry<String, Integer> entry : ((BO4)branchDataItem.branch.getObject()).getConfig().branchFrequencyGroups.entrySet())
			{
				HashMap<ChunkCoordinate, ArrayList<Integer>> branchesByGroup = AllBranchesBranchDataByGroup.get(entry.getKey());
				ArrayList<Integer> frequenciesForGroupAtChunk = branchesByGroup.get(branchDataItem.chunkCoordinate);
				frequenciesForGroupAtChunk.remove(entry.getValue());
				if(frequenciesForGroupAtChunk.size() == 0)
				{
					branchesByGroup.remove(branchDataItem.chunkCoordinate);
					if(branchesByGroup.size() == 0)
					{
						AllBranchesBranchDataByGroup.remove(entry.getKey());
					}
				}
			}
		}
	}

	// Returns null if the branch cannot spawn in the given biome or if there's another BO4 structure in the chunk, otherwise returns colliding branches. 
	// CanOverride branches never collide with other branches, but may be unable to spawn if there's not enough space for smoothing areas.
    private Stack<BranchDataItem> checkSpawnRequirementsAndCollisions(BranchDataItem branchData, boolean minimumSize, LocalWorld world, ArrayList<String> targetBiomes)
    {
    	CustomStructureCoordinate coordObject = branchData.branch;

    	if(!minimumSize)
    	{
    		// If targetbiomes isn't null, then check for targetbiomes
    		if(targetBiomes != null)
    		{
    			// If targetbiomes size is 0, allow all biomes.
    			if(targetBiomes.size() > 0)
    			{
        			LocalBiome biome3 = world.getBiome(branchData.chunkCoordinate.getChunkX() * 16 + 8, branchData.chunkCoordinate.getChunkZ() * 16 + 7);
        			if(!targetBiomes.contains(biome3.getName()))
        			{
        				return null;
        			}    				
    			}
    		}
		    // Check if the structure can spawn in this biome
    		else if(!isStructureAtSpawn)
		    {
		    	ArrayList<String> biomeStructures;

            	LocalBiome biome3 = world.getBiome(branchData.chunkCoordinate.getChunkX() * 16 + 8, branchData.chunkCoordinate.getChunkZ() * 16 + 7);
                BiomeConfig biomeConfig3 = biome3.getBiomeConfig();
                // Get Bo3's for this biome
                ArrayList<String> structuresToSpawn = new ArrayList<String>();
                for (CustomStructureGen res : biomeConfig3.getCustomStructures())
                {
            		for(String bo3Name : res.objectNames)
            		{
            			structuresToSpawn.add(bo3Name);
            		}
                }

                biomeStructures = structuresToSpawn;

                boolean canSpawnHere = false;
                for(String structureToSpawn : biomeStructures)
                {
                	if(structureToSpawn.equals(this.start.getObject().getName()))
                	{
                		canSpawnHere = true;
                		break;
                	}
                }

                if(!canSpawnHere)
				{
                	return null;
				}
		    }

	    	int smoothRadius = ((BO4)this.start.getObject()).getConfig().smoothRadius; // For collision detection use Start's SmoothingRadius. TODO: Improve this and use smoothingradius of individual branches?
	    	if(smoothRadius == -1 || ((BO4)coordObject.getObject()).getConfig().smoothRadius == -1)
	    	{
	    		smoothRadius = 0;
	    	}
	    	if(smoothRadius > 0)
	        {
	        	// get all chunks within smoothRadius and check structureCache for collisions
	    		double radiusInChunks = Math.ceil((smoothRadius) / (double)16);
	        	for(int x = branchData.chunkCoordinate.getChunkX() - (int)radiusInChunks; x <= branchData.chunkCoordinate.getChunkX() + radiusInChunks; x++)
	        	{
	            	for(int z = branchData.chunkCoordinate.getChunkZ() - (int)radiusInChunks; z <= branchData.chunkCoordinate.getChunkZ() + radiusInChunks; z++)
	            	{
	            		double distanceBetweenStructures = Math.floor((float) Math.sqrt(Math.pow(branchData.chunkCoordinate.getChunkX() - x, 2) + Math.pow(branchData.chunkCoordinate.getChunkZ() - z, 2)));
	            		if(distanceBetweenStructures <= radiusInChunks)
	            		{
	            		    // Check if any other structures in world are in this chunk
	            			if(world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(x,z)) || world.getStructureCache().bo4StructureCache.containsKey(ChunkCoordinate.fromChunkCoords(x,z)))
	            		    {
	            		        // Structures' bounding boxes are overlapping, don't add this branch.
	            				return null;
	            		    }

	                		// If targetbiomes isn't null, then check for targetbiomes
	                		if(targetBiomes != null)
	                		{
	                			// If targetbiomes size is 0, allow all biomes.
	                			if(targetBiomes.size() > 0)
	                			{
	                				LocalBiome biome3 = world.getBiome(x * 16 + 8, z * 16 + 7);
	                    			if(!targetBiomes.contains(biome3.getName()))
	                    			{
	                    				return null;
	                    			}    				
	                			}
	                		}
	                		else if(!isStructureAtSpawn)
	            			{
		            		    // Check if the structure can spawn in this biome
		            			ArrayList<String> biomeStructures;

	        	            	LocalBiome biome3 = world.getBiome(x * 16 + 8, z * 16 + 7);
	        	                BiomeConfig biomeConfig3 = biome3.getBiomeConfig();
	        	                // Get Bo3's for this biome
	        	                ArrayList<String> structuresToSpawn = new ArrayList<String>();
	        	                for (CustomStructureGen res : biomeConfig3.getCustomStructures())
	        	                {
	        	            		for(String bo3Name : res.objectNames)
	        	            		{
	        	            			structuresToSpawn.add(bo3Name);
	        	            		}
	        	                }

	        	                biomeStructures = structuresToSpawn;

		                        boolean canSpawnHere = false;
		                        for(String structureToSpawn : biomeStructures)
		                        {
		                        	if(structureToSpawn.equals(this.start.getObject().getName()))
		                        	{
		                        		canSpawnHere = true;
		                        		break;
		                        	}
		                        }

		                        if(!canSpawnHere)
		        				{
		                        	return null;
		        				}
	            			}
	            		}
	            	}
	        	}
	        }
    	}

    	// collidingObjects are only used for size > 0 check and to see if this branch tried to spawn on top of its parent
    	Stack<BranchDataItem> collidingObjects = new Stack<BranchDataItem>();
    	
        if(!((BO4) coordObject.getObject()).getConfig().canOverride)
        {
	        Stack<BranchDataItem> existingBranches = new Stack<BranchDataItem>();
	        if(AllBranchesBranchDataByChunk.containsKey(branchData.chunkCoordinate))
	        {
	        	for(BranchDataItem existingBranchData : AllBranchesBranchDataByChunk.get(branchData.chunkCoordinate))
		        {
		        	if(branchData.chunkCoordinate.equals(existingBranchData.chunkCoordinate) && !((BO4)existingBranchData.branch.getObject()).getConfig().canOverride)
		        	{
		        		existingBranches.add(existingBranchData);
		        	}
		        }
	        }

	        if (existingBranches.size() > 0)
	        {
	        	for (BranchDataItem cachedBranch : existingBranches)
	        	{
	        		if(checkCollision(coordObject, cachedBranch.branch))
	        		{
	        			collidingObjects.add(cachedBranch);
	        		}
	        	}
	        }
        }

    	return collidingObjects;
    }

    private boolean checkYBounds(CustomStructureCoordinate branchData1Branch)
    {
        int startY = branchData1Branch.getY() + ((BO4)branchData1Branch.getObject()).getConfig().getminY();
        return startY >= 0;
    }
    
    private boolean checkCollision(CustomStructureCoordinate branchData1Branch, CustomStructureCoordinate branchData2Branch)
    {
    	if(
			!((BO4)branchData1Branch.getObject()).isCollidable() ||
			!((BO4)branchData2Branch.getObject()).isCollidable()
		)
    	{
    		return false;
    	}

    	// minX/maxX/minZ/maxZ are always positive.

    	CustomStructureCoordinate branchData1BranchMinRotated = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(((BO4)branchData1Branch.getObject()).getConfig().getminX(), ((BO4)branchData1Branch.getObject()).getConfig().getminY(), ((BO4)branchData1Branch.getObject()).getConfig().getminZ(), branchData1Branch.getRotation());
    	CustomStructureCoordinate branchData1BranchMaxRotated = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(((BO4)branchData1Branch.getObject()).getConfig().getmaxX(),((BO4)branchData1Branch.getObject()).getConfig().getmaxY(), ((BO4)branchData1Branch.getObject()).getConfig().getmaxZ(), branchData1Branch.getRotation());

        int startX = branchData1Branch.getX() + Math.min(branchData1BranchMinRotated.getX(),branchData1BranchMaxRotated.getX());
        int endX = branchData1Branch.getX() + Math.max(branchData1BranchMinRotated.getX(),branchData1BranchMaxRotated.getX());
        int startY = branchData1Branch.getY() + Math.min(branchData1BranchMinRotated.getY(),branchData1BranchMaxRotated.getY());
        int endY = branchData1Branch.getY() + Math.max(branchData1BranchMinRotated.getY(),branchData1BranchMaxRotated.getY());
        int startZ = branchData1Branch.getZ() + Math.min(branchData1BranchMinRotated.getZ(),branchData1BranchMaxRotated.getZ());
        int endZ = branchData1Branch.getZ() + Math.max(branchData1BranchMinRotated.getZ(),branchData1BranchMaxRotated.getZ());

        CustomStructureCoordinate branchData2BranchMinRotated = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(((BO4)branchData2Branch.getObject()).getConfig().getminX(), ((BO4)branchData2Branch.getObject()).getConfig().getminY(), ((BO4)branchData2Branch.getObject()).getConfig().getminZ(), branchData2Branch.getRotation());
        CustomStructureCoordinate branchData2BranchMaxRotated = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(((BO4)branchData2Branch.getObject()).getConfig().getmaxX(), ((BO4) branchData2Branch.getObject()).getConfig().getmaxY(), ((BO4)branchData2Branch.getObject()).getConfig().getmaxZ(), branchData2Branch.getRotation());

        int cachedBranchStartX = branchData2Branch.getX() + Math.min(branchData2BranchMinRotated.getX(),branchData2BranchMaxRotated.getX());
        int cachedBranchEndX = branchData2Branch.getX() + Math.max(branchData2BranchMinRotated.getX(),branchData2BranchMaxRotated.getX());
        int cachedBranchStartY = branchData2Branch.getY() + Math.min(branchData2BranchMinRotated.getY(),branchData2BranchMaxRotated.getY());
        int cachedBranchEndY = branchData2Branch.getY() + Math.max(branchData2BranchMinRotated.getY(),branchData2BranchMaxRotated.getY());
        int cachedBranchStartZ = branchData2Branch.getZ() + Math.min(branchData2BranchMinRotated.getZ(),branchData2BranchMaxRotated.getZ());
        int cachedBranchEndZ = branchData2Branch.getZ() + Math.max(branchData2BranchMinRotated.getZ(),branchData2BranchMaxRotated.getZ());

        if (
    		cachedBranchEndX >= startX &&
    		cachedBranchStartX <= endX &&
    		cachedBranchEndY >= startY &&
    		cachedBranchStartY <= endY &&
    		cachedBranchEndZ >= startZ &&
    		cachedBranchStartZ <= endZ
		)
        {
            // Structures' bounding boxes are overlapping
            return true;
        }

    	return false;
    }

    /**
     * Add the object to the list of BO4's to be spawned for this chunk
     * @param coordObject
     * @param chunkCoordinate
     */
    private void addToChunk(BO4CustomStructureCoordinate coordObject, ChunkCoordinate chunkCoordinate, Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectList)
    {
    	//OTG.log(LogMarker.INFO, "AddToChunk X" + chunkCoordinate.getChunkX() + " Z" + chunkCoordinate.getChunkZ());

        // Get the set of structures to spawn that is currently being stored
        // for the target chunk or create a new one if none exists
        Stack<BO4CustomStructureCoordinate> objectsInChunk = objectList.get(chunkCoordinate);
        if (objectsInChunk == null)
        {
            objectsInChunk = new Stack<BO4CustomStructureCoordinate>();
        }
    	// Add the structure to the set
    	objectsInChunk.add(coordObject);
        objectList.put(chunkCoordinate, objectsInChunk);
    }

    /**
    * Checks if this structure or any of its branches are inside the given
    * chunk and spawns all objects that are including their smoothing areas (if any)
    *
    * @param chunkCoordinate
    */
    public void spawnInChunk(ChunkCoordinate chunkCoordinate, LocalWorld world, ChunkCoordinate chunkBeingPopulated)
    {
    	//OTG.log(LogMarker.INFO, "SpawnForChunk X" + chunkCoordinate.getChunkX() + " Z" + chunkCoordinate.getChunkZ() + " " + this.start.bo3Name);
    	   	
    	if ((!objectsToSpawn.containsKey(chunkCoordinate) && !smoothingAreasToSpawn.containsKey(chunkCoordinate)))
        {
            return;
        }
    	
        // Get all BO3's that should spawn in the given chunk, if any
        // Note: The given chunk may not necessarily be the chunkCoordinate of this.Start
        Stack<BO4CustomStructureCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
        if (objectsInChunk != null)
        {
        	BO4Config config = ((BO4)this.start.getObject()).getConfig();
            LocalBiome biome = null;
            BiomeConfig biomeConfig = null;
            if(config.spawnUnderWater)
        	{
            	biome = world.getBiome(this.start.getX() + 8, this.start.getZ() + 7);
            	biomeConfig = biome.getBiomeConfig();
        	}

            // Spawn ReplaceAbove / ReplaceBelow before anything else.
            for (BO4CustomStructureCoordinate coordObject : objectsInChunk)
            {
                if (coordObject.isSpawned)
                {
                    continue;
                }

                BO4 bo4 = ((BO4)coordObject.getObject());
                BO4Config objectConfig = bo4.getConfig();                
                if (
            		!((BO4)coordObject.getObject()).trySpawnAt(
        				world, 
        				random,
        				coordObject.getRotation(),
        				chunkCoordinate, 
        				coordObject.x, 
        				coordObject.y, 
        				coordObject.z, 
        				config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceAbove : objectConfig.replaceAbove, 
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceBelow : objectConfig.replaceBelow, 
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithBiomeBlocks : objectConfig.replaceWithBiomeBlocks,								
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithSurfaceBlock : objectConfig.replaceWithSurfaceBlock, 
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithGroundBlock : objectConfig.replaceWithGroundBlock,
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithStoneBlock : objectConfig.replaceWithStoneBlock,
						config.spawnUnderWater,  
						!config.spawnUnderWater ? -1 : (biomeConfig.useWorldWaterLevel ? world.getConfigs().getWorldConfig().waterLevelMax : biomeConfig.waterLevelMax), 
						false, 
						true, 
						chunkBeingPopulated
					)
        		)
                {
                	if(OTG.getPluginConfig().spawnLog)
                	{
                		OTG.log(LogMarker.WARN, "Could not spawn chunk " + coordObject.bo3Name + " for structure " + this.start.getObject().getName());
                	}
            		objectsToSpawn.remove(chunkCoordinate);
            		smoothingAreasToSpawn.remove(chunkCoordinate);	
            		BO4.OriginalTopBlocks.clear();
                	return;
                }
            }

            // Spawn smooth areas in this chunk if any exist
        	smoothingAreaManager.spawnSmoothAreas(chunkCoordinate, smoothingAreasToSpawn, this.start, world, chunkBeingPopulated);

        	// Spawn blocks/modData/spawners/particles/entities.
            for (BO4CustomStructureCoordinate coordObject : objectsInChunk)
            {
                if (coordObject.isSpawned)
                {
                    continue;
                }

                BO4 bo3 = ((BO4)coordObject.getObject());
                BO4Config objectConfig = bo3.getConfig();

                if (
            		!((BO4)coordObject.getObject()).trySpawnAt(
        				world, 
        				random, 
        				coordObject.getRotation(),
        				chunkCoordinate, 
        				coordObject.x, 
        				coordObject.y, 
        				coordObject.z,         				
        				config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceAbove : objectConfig.replaceAbove, 
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceBelow : objectConfig.replaceBelow, 
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithBiomeBlocks : objectConfig.replaceWithBiomeBlocks, 
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithSurfaceBlock : objectConfig.replaceWithSurfaceBlock, 
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithGroundBlock : objectConfig.replaceWithGroundBlock,
						config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithStoneBlock : objectConfig.replaceWithStoneBlock,
						config.spawnUnderWater,  
						!config.spawnUnderWater ? -1 : (biomeConfig.useWorldWaterLevel ? world.getConfigs().getWorldConfig().waterLevelMax : biomeConfig.waterLevelMax), 
						false, 
						false, 
						chunkBeingPopulated
					)
        		)
                {
                	if(OTG.getPluginConfig().spawnLog)
                	{
                		OTG.log(LogMarker.WARN, "Could not spawn chunk " + coordObject.bo3Name + " for structure " + this.start.getObject().getName());
                	}
            		objectsToSpawn.remove(chunkCoordinate);
            		smoothingAreasToSpawn.remove(chunkCoordinate);	
            		BO4.OriginalTopBlocks.clear();
                	return;
                } else {
                	this.modDataManager.spawnModData(objectConfig.getModData(), coordObject, chunkCoordinate);
                	this.spawnerManager.spawnSpawners(objectConfig.getSpawnerData(), coordObject, chunkCoordinate);
                	this.particlesManager.spawnParticles(objectConfig.getParticleData(), coordObject, chunkCoordinate);
                	this.entitiesManager.spawnEntities(world, objectConfig.getEntityData(), coordObject, chunkCoordinate);
                    coordObject.isSpawned = true;
                }
            }
        } else {
            // Spawn smooth areas in this chunk if any exist
        	smoothingAreaManager.spawnSmoothAreas(chunkCoordinate, smoothingAreasToSpawn, this.start, world, chunkBeingPopulated);
        }

		objectsToSpawn.remove(chunkCoordinate);
		smoothingAreasToSpawn.remove(chunkCoordinate);	
		BO4.OriginalTopBlocks.clear();
    }
}
