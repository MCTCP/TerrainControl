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
import com.pg85.otg.util.bo3.Rotation;
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
    	this(world, structureStart, false, false);
    	this.objectsToSpawn = objectsToSpawn;
    	this.smoothingAreasToSpawn = smoothingAreasToSpawn;
    	this.minY = minY;
    }
    
    BO4CustomStructure(LocalWorld world, BO4CustomStructureCoordinate start, boolean spawn, boolean isStructureAtSpawn)
    {
        this.isStructureAtSpawn = isStructureAtSpawn;

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

		if(spawn)
		{
			branchesTried = 0;

			long startTime = System.currentTimeMillis();

			// Structure at spawn can't hurt to query source blocks, structures with randomY don't need to do any block checks so don't hurt either.
			//if(isStructureAtSpawn || ((BO4)Start.getObject(World.getName())).settings.spawnHeight == SpawnHeightEnum.randomY)
			{
				if(!doStartChunkBlockChecks(world)){ return; } // Just do the damn checks to get the height right....
			}

			// Only detect Y or material of source block if necessary to prevent chunk loading
			// if this BO3 is being plotted in a chunk that has not yet been populated.

			// Need to know the height if this structure can only spawn at a certain height
			//if((((BO4)Start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestBlock || ((BO4)Start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock) && (World.getConfigs().getWorldConfig().disableBedrock || ((BO4)Start.getObject()).getSettings().minHeight > 1 || ((BO4)Start.getObject()).getSettings().maxHeight < 256))
			{
				//if(!DoStartChunkBlockChecks()){ return; }
			}

			if(!((BO4)this.start.getObject()).getSettings().canSpawnOnWater)
			{
				//if(!DoStartChunkBlockChecks()){ return; }
				int highestBlocky = world.getHighestBlockYAt(this.start.getX() + 8, this.start.getZ() + 7, true, true, false, true);;
				//if(Start.y - 1 > OTG.WORLD_DEPTH && Start.y - 1 < OTG.WORLD_HEIGHT && world.getMaterial(Start.getX() + 8, Start.y - 1, Start.getZ() + 7).isLiquid())
				if(this.start.y - 1 > PluginStandardValues.WORLD_DEPTH && this.start.y - 1 < PluginStandardValues.WORLD_HEIGHT && world.getMaterial(this.start.getX() + 8, highestBlocky, this.start.getZ() + 7, true).isLiquid())
				{
					return;
				}
			}

			if(((BO4)this.start.getObject()).getSettings().spawnOnWaterOnly)
			{
				//if(!DoStartChunkBlockChecks()){ return; }
				if(
					!(
						world.getMaterial(this.start.getX(), this.start.y - 1, this.start.getZ(), true).isLiquid() &&
						world.getMaterial(this.start.getX(), this.start.y - 1, this.start.getZ() + 15, true).isLiquid() &&
						world.getMaterial(this.start.getX() + 15, this.start.y - 1, this.start.getZ(), true).isLiquid() &&
						world.getMaterial(this.start.getX() + 15, this.start.y - 1, this.start.getZ() + 15, true).isLiquid()
					)
				)
				{
					return;
				}
			}

			try
			{
				calculateBranches(false, world);
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
	        		BO4Config objectConfig = ((BO4)coordObject.getObject()).getSettings();
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

			//TODO: Smoothing areas should count as must spawn/required branches! <-- Is this really a problem? Smoothing areas from different structures don't overlap?

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
				world.getStructureCache().getPlotter().addToStructuresPerChunkCache(chunkCoord, new ArrayList<String>());
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
				world.getStructureCache().getPlotter().addToStructuresPerChunkCache(chunkCoord, new ArrayList<String>());
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
    }

    private boolean doStartChunkBlockChecks(LocalWorld world)
    {
    	if(!startChunkBlockChecksDone)
    	{
	    	startChunkBlockChecksDone = true;

	    	//OTG.log(LogMarker.INFO, "DoStartChunkBlockChecks");

			// Requesting the Y position or material of a block in an unpopulated chunk causes some of that chunk's blocks to be calculated, this is expensive and should be kept at a minimum.

			// Y checks:
			// If BO3's have a minimum and maximum Y configured by the player then we don't really need
	    	// to check if the BO3 fits in the Y direction, that is the player's responsibility!

			// Material checks:
			// A BO3 may need to perform material checks to when using !CanSpawnOnWater or SpawnOnWaterOnly

	    	short startY = 0;

			if(((BO4)this.start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestBlock || ((BO4)this.start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock)
			{
				if(((BO4)this.start.getObject()).getSettings().spawnAtWaterLevel)
				{
					LocalBiome biome = world.getBiome(this.start.getX() + 8, this.start.getZ() + 7);
					startY = (short) (biome.getBiomeConfig().useWorldWaterLevel ? world.getConfigs().getWorldConfig().waterLevelMax : biome.getBiomeConfig().waterLevelMax);
				} else {
					// OTG.log(LogMarker.INFO, "Request height for chunk X" + ChunkCoordinate.fromBlockCoords(Start.getX(), Start.getZ()).getChunkX() + " Z" + ChunkCoordinate.fromBlockCoords(Start.getX(), Start.getZ()).getChunkZ());
					// If this chunk has not yet been populated then this will cause it to be! (ObjectSpawner.Populate() is called)

					int highestBlock = 0;

					if(!((BO4)this.start.getObject()).getSettings().spawnUnderWater)
					{
						highestBlock = world.getHighestBlockYAt(this.start.getX() + 8, this.start.getZ() + 7, true, true, false, true);
					} else {
						highestBlock = world.getHighestBlockYAt(this.start.getX() + 8, this.start.getZ() + 7, true, false, true, true);
					}

					if(highestBlock < 1)
					{
						//OTG.log(LogMarker.INFO, "Structure " + Start.BO3Name + " could not be plotted at Y < 1. If you are creating empty chunks intentionally (for a sky world for instance) then make sure you don't use the highestBlock setting for your BO3's");
						if(((BO4)this.start.getObject()).getSettings().heightOffset > 0) // Allow floating structures that use highestblock + heightoffset
						{
							highestBlock = ((BO4)this.start.getObject()).getSettings().heightOffset;
						} else {
							return false;
						}
					} else {
						startY  = (short) (highestBlock + 1);
					}
				}
			} else {
				if(((BO4)this.start.getObject()).getSettings().maxHeight != ((BO4)this.start.getObject()).getSettings().minHeight)
				{
					startY = (short) (((BO4)this.start.getObject()).getSettings().minHeight + new Random().nextInt(((BO4)this.start.getObject()).getSettings().maxHeight - ((BO4)this.start.getObject()).getSettings().minHeight));
				} else {
					startY = (short) ((BO4)this.start.getObject()).getSettings().minHeight;
				}
			}

			//if((MinY + startY) < 1 || (startY) < ((BO4)Start.getObject(World.getName())).settings.minHeight || (startY) > ((BO4)Start.getObject(World.getName())).settings.maxHeight)
			if(startY < ((BO4)this.start.getObject()).getSettings().minHeight || startY > ((BO4)this.start.getObject()).getSettings().maxHeight)
			{
				return false;
				//throw new IllegalArgumentException("Structure could not be plotted at these coordinates, it does not fit in the Y direction. " + ((BO4)Start.getObject(World.getName())).getName() + " at Y " + startY);
			}

			startY += ((BO4)this.start.getObject()).getSettings().heightOffset;

			if(startY < PluginStandardValues.WORLD_DEPTH || startY >= PluginStandardValues.WORLD_HEIGHT)
			{
				return false;
			}

			for(ChunkCoordinate chunkCoord : objectsToSpawn.keySet())
			{
				for(CustomStructureCoordinate BO3 : objectsToSpawn.get(chunkCoord))
				{
					BO3.y += startY;
				}
			}

			Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> SmoothingAreasToSpawn2 = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
			SmoothingAreasToSpawn2.putAll(smoothingAreasToSpawn);
			smoothingAreasToSpawn.clear();
			for(ChunkCoordinate chunkCoord2 : SmoothingAreasToSpawn2.keySet())
			{
				ArrayList<SmoothingAreaLine> coords = new ArrayList<SmoothingAreaLine>();
				SmoothingAreaLine coordToAdd;
				for(SmoothingAreaLine coord : SmoothingAreasToSpawn2.get(chunkCoord2))
				{
					if(coord instanceof SmoothingAreaLineDiagonal)
					{
						coordToAdd = new SmoothingAreaLineDiagonal(coord.beginPointX, (short)(coord.beginPointY + this.start.getY()), coord.beginPointZ, coord.endPointX, (short)(coord.endPointY + this.start.getY()), coord.endPointZ, coord.originPointX, (short)-1, coord.originPointZ, coord.finalDestinationPointX, (short)-1, coord.finalDestinationPointZ, ((SmoothingAreaLineDiagonal)coord).diagonalLineOriginPointX, (short)(((SmoothingAreaLineDiagonal)coord).diagonalLineoriginPointY + this.start.getY()), ((SmoothingAreaLineDiagonal)coord).diagonalLineOriginPointZ, ((SmoothingAreaLineDiagonal)coord).diagonalLineFinalDestinationPointX, (short)-1, ((SmoothingAreaLineDiagonal)coord).diagonalLineFinalDestinationPointZ);
						coords.add(coordToAdd);
					} else {
						coordToAdd = new SmoothingAreaLine(coord.beginPointX, (short)(coord.beginPointY + this.start.getY()), coord.beginPointZ, coord.endPointX, (short)(coord.endPointY + this.start.getY()), coord.endPointZ, coord.originPointX, (short)(coord.originPointY + this.start.getY()), coord.originPointZ, coord.finalDestinationPointX, (short)-1, coord.finalDestinationPointZ);
						coords.add(coordToAdd);
					}
				}
				smoothingAreasToSpawn.put(ChunkCoordinate.fromChunkCoords(chunkCoord2.getChunkX(), chunkCoord2.getChunkZ()), coords);
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
    Object[] getMinimumSize(LocalWorld world) throws InvalidConfigException
    {
    	if(
			((BO4)this.start.getObject()).getSettings().minimumSizeTop != -1 &&
			((BO4)this.start.getObject()).getSettings().minimumSizeBottom != -1 &&
			((BO4)this.start.getObject()).getSettings().minimumSizeLeft != -1 &&
			((BO4)this.start.getObject()).getSettings().minimumSizeRight != -1)
    	{
    		Object[] returnValue = { ((BO4)this.start.getObject()).getSettings().minimumSizeTop, ((BO4)this.start.getObject()).getSettings().minimumSizeRight, ((BO4)this.start.getObject()).getSettings().minimumSizeBottom, ((BO4)this.start.getObject()).getSettings().minimumSizeLeft };
    		return returnValue;
    	}
    	
    	calculateBranches(true, world);

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

		minY += ((BO4)this.start.getObject()).getSettings().heightOffset;

		int smoothingRadiusInChunks = (int)Math.ceil(((BO4)this.start.getObject()).getSettings().smoothRadius / (double)16);  // TODO: this assumes that smoothradius is the same for every BO3 within this structure, child branches may have overriden their own smoothradius! This may cause problems if a child branch has a larger smoothradius than the starting structure
    	((BO4)this.start.getObject()).getSettings().minimumSizeTop = Math.abs(startChunk.getChunkZ() - top.getChunkZ()) + smoothingRadiusInChunks;
    	((BO4)this.start.getObject()).getSettings().minimumSizeRight = Math.abs(startChunk.getChunkX() - right.getChunkX()) + smoothingRadiusInChunks;
    	((BO4)this.start.getObject()).getSettings().minimumSizeBottom = Math.abs(startChunk.getChunkZ() - bottom.getChunkZ()) + smoothingRadiusInChunks;
    	((BO4)this.start.getObject()).getSettings().minimumSizeLeft = Math.abs(startChunk.getChunkX() - left.getChunkX()) + smoothingRadiusInChunks;

    	Object[] returnValue = { ((BO4)this.start.getObject()).getSettings().minimumSizeTop, ((BO4)this.start.getObject()).getSettings().minimumSizeRight, ((BO4)this.start.getObject()).getSettings().minimumSizeBottom, ((BO4)this.start.getObject()).getSettings().minimumSizeLeft };

    	if(OTG.getPluginConfig().spawnLog)
    	{
    		OTG.log(LogMarker.INFO, "");
        	OTG.log(LogMarker.INFO, this.start.getObject().getName() + " minimum size: Width " + ((Integer)returnValue[1] + (Integer)returnValue[3] + 1) + " Length " + ((Integer)returnValue[0] + (Integer)returnValue[2] + 1) + " top " + (Integer)returnValue[0] + " right " + (Integer)returnValue[1] + " bottom " + (Integer)returnValue[2] + " left " + (Integer)returnValue[3]);
    	}

    	objectsToSpawn.clear();

    	return returnValue;
    }

    // TODO: Make sure that canOverride optional branches cannot be in the same branch group as required branches.
    // This makes sure that when the first spawn phase is complete and all required branches and non-canOverride optional branches have spawned
    // those can never be rolled back because of canOverride optional branches that are unable to spawn.
    // canOverride required branches: things that need to be spawned in the same cycle as their parent branches, for instance door/wall markers for rooms
    // canOverride optional branches: things that should be spawned after the base of the structure has spawned, for instance room interiors, adapter/modifier pieces that knock out walls/floors between rooms etc.

    private void calculateBranches(boolean minimumSize, LocalWorld world) throws InvalidConfigException
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

    		traverseAndSpawnChildBranches(branchData, minimumSize, true, world);

			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.INFO, "All branch groups with required branches only have been processed for cycle " + Cycle + ", plotting branch groups with optional branches.");
			}
			traverseAndSpawnChildBranches(branchData, minimumSize, false, world);

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
	            	for(BranchDataItem childBranch : branchDataItem3.getChildren(false, world))
	            	{
	            		if(
            				!childBranch.branch.isRequiredBranch &&
            				((BO4)childBranch.branch.getObject()).getSettings().canOverride
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
    					OTG.log(LogMarker.WARN, "Error: Branching BO3 " + this.start.bo3Name + " could not be spawned in minimum configuration (isRequiredBranch branches only).");
    				}
            		throw new InvalidConfigException("Error: Branching BO3 " + this.start.bo3Name + " could not be spawned in minimum configuration (isRequiredBranch branches only).");
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

    private void traverseAndSpawnChildBranches(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly, LocalWorld world)
    {
    	if(!branchData.doneSpawning)
    	{
    		addBranches(branchData, minimumSize, false, spawningRequiredBranchesOnly, world);
    	} else {
    		if(!branchData.cannotSpawn)
    		{
    			for(BranchDataItem branchDataItem2 : branchData.getChildren(false, world))
    			{
    				// BranchData.DoneSpawning can be set to true by a child branch
    				// that tried to spawn but couldnt
    				if(!branchDataItem2.cannotSpawn && branchData.doneSpawning)
    				{
    					traverseAndSpawnChildBranches(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world);
    				}
    			}
    		}
    	}
    }
    
    private void addBranches(BranchDataItem branchDataItem, boolean minimumSize, boolean traverseOnlySpawnedChildren, boolean spawningRequiredBranchesOnly, LocalWorld world)
    {
    	// CanOverride optional branches are spawned only after the main structure has spawned.
    	// This is useful for adding interiors and knocking out walls between rooms
    	if(!SpawningCanOverrideBranches)
    	{
	    	for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false, world))
	    	{
	    		if(
    				(
						!branchDataItem3.cannotSpawn ||
						!branchDataItem3.doneSpawning
					) && (
						((BO4)branchDataItem3.branch.getObject()).getSettings().canOverride &&
						!branchDataItem3.branch.isRequiredBranch
					)
				)
	    		{
	    			branchDataItem3.cannotSpawn = true;
	    			branchDataItem3.doneSpawning = true;
	    		}
	    	}
    	}

    	// TODO: Remove these
    	if(spawningRequiredChildrenForOptionalBranch && traverseOnlySpawnedChildren)
    	{
    		throw new RuntimeException();
    	}

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
			for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false, world))
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
	        for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false, world))
	        {        	
	        	if(!AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber) && !childBranchDataItem.spawnDelayed)
	        	{
		        	// Check if children should be spawned
		        	// Check 1: Check for collision with other branches or other structures
	        		boolean canSpawn = true;

	        		boolean collidedWithParentOrSibling = false;
	        		boolean wasntBelowOther = false;
	        		boolean wasntInsideOther = false;
	        		boolean cannotSpawnInsideOther = false;
	        		boolean wasntOnWater = false;
	        		boolean wasOnWater = false;
	        		boolean spaceIsOccupied = false;
	        		boolean chunkIsIneligible = false;
	        		boolean startChunkBlockChecksPassed = true;
	        		boolean isInsideWorldBorder = true;
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
    	        	if(spawningRequiredBranchesOnly)// && !isRollBack)
    	        	{
    	    			if(childBranchDataItem.branch.isRequiredBranch)
    	    			{
    		    			boolean hasOnlyRequiredBranches = true;
    		    			if(childBranchDataItem.branch.branchGroup != null && childBranchDataItem.branch.branchGroup.length() > 0)
    		    			{
	    		    			for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false, world))
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

	        		if(canSpawn && (childBranchDataItem.maxDepth == 0 || childBranchDataItem.currentDepth > childBranchDataItem.maxDepth) && !childBranchDataItem.branch.isRequiredBranch)
	        		{
	        			canSpawn = false;
	        		}

	        		branchesTried += 1;

	        		// Ignore weightedbranches when measuring
	        		if(minimumSize && childBranchDataItem.branch.isWeightedBranch)
	        		{
	        			childBranchDataItem.doneSpawning = true;
        				childBranchDataItem.cannotSpawn = true;
        				continue;
	        		}

	        		int smoothRadius = ((BO4)this.start.getObject()).getSettings().overrideChildSettings && bo3.getSettings().overrideChildSettings ? ((BO4)this.start.getObject()).getSettings().smoothRadius : bo3.getSettings().smoothRadius;
	        		if(smoothRadius == -1 || bo3.getSettings().smoothRadius == -1)
	        		{
	        			smoothRadius = 0;
	        		}

	        		ChunkCoordinate worldBorderCenterPoint = world.getWorldSession().getWorldBorderCenterPoint();

	        		if(
        				canSpawn &&
        				!minimumSize &&
        				world.getWorldSession().getWorldBorderRadius() > 0 &&
        				(
    						(
								smoothRadius == 0 &&
								!world.isInsideWorldBorder(ChunkCoordinate.fromChunkCoords(childBranchDataItem.branch.getChunkX(), childBranchDataItem.branch.getChunkZ()), true)
							)
    						||
    						(
								smoothRadius > 0 &&
								(
									childBranchDataItem.branch.getChunkX() - Math.ceil(smoothRadius / (double)16) < worldBorderCenterPoint.getChunkX() - (world.getWorldSession().getWorldBorderRadius() - 1) ||
									childBranchDataItem.branch.getChunkX() + Math.ceil(smoothRadius / (double)16) > worldBorderCenterPoint.getChunkX() + (world.getWorldSession().getWorldBorderRadius() - 1) - 1 || // Resources are spawned at an offset of + half a chunk so stop 1 chunk short of the border
									childBranchDataItem.branch.getChunkZ() - Math.ceil(smoothRadius / (double)16) < worldBorderCenterPoint.getChunkZ() - (world.getWorldSession().getWorldBorderRadius() - 1) ||
									childBranchDataItem.branch.getChunkZ() + Math.ceil(smoothRadius / (double)16) > worldBorderCenterPoint.getChunkZ() + (world.getWorldSession().getWorldBorderRadius() - 1) - 1 // Resources are spawned at an offset of + half a chunk so stop 1 chunk short of the border
								)
							)
						)
    				)
	        		{
	        			canSpawn = false;
	        			isInsideWorldBorder = false;
	        		}

        			if(!doStartChunkBlockChecks(world))
        			{
        				canSpawn = false;
        				startChunkBlockChecksPassed = false;
        			} else {
		        	    if(childBranchDataItem.branch.getY() < 0 && !minimumSize)
		        	    {
		    		    	canSpawn = false;
		        	    }
        			}

	        		Stack<BranchDataItem> collidingObjects = null;
	        		if(canSpawn)
	        		{
	        			if(!minimumSize && world.chunkHasDefaultStructure(this.worldRandom, childBranchDataItem.chunkCoordinate))
	        			{
	        				chunkIsIneligible = true;
	        				canSpawn = false;
	        			}
	        			
		        		if(canSpawn && !minimumSize && bo3.getSettings().spawnOnWaterOnly)
		    			{
		    				if(
		    					!(
	    							world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX(), world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX(), childBranchDataItem.chunkCoordinate.getBlockZ(), true, true, false, true), childBranchDataItem.chunkCoordinate.getBlockZ(), true).isLiquid() &&
		    						world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX(), world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX(), childBranchDataItem.chunkCoordinate.getBlockZ() + 15, true, true, false, true), childBranchDataItem.chunkCoordinate.getBlockZ() + 15, true).isLiquid() &&
		    						world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX() + 15, world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX() + 15, childBranchDataItem.chunkCoordinate.getBlockZ(), true, true, false, true), childBranchDataItem.chunkCoordinate.getBlockZ(), true).isLiquid() &&
		    						world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX() + 15, world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX() + 15, childBranchDataItem.chunkCoordinate.getBlockZ() + 15, true, true, false, true), childBranchDataItem.chunkCoordinate.getBlockZ() + 15, true).isLiquid()
		    					)
		    				)
		    				{
		    					wasntOnWater = true;
		    					canSpawn = false;
		    				}
		    			}
		        		if(canSpawn && !minimumSize && !bo3.getSettings().canSpawnOnWater)
		    			{
		    				if(
	    						(world.getMaterial(childBranchDataItem.chunkCoordinate.getBlockX() + 8, world.getHighestBlockYAt(childBranchDataItem.chunkCoordinate.getBlockX() + 8, childBranchDataItem.chunkCoordinate.getBlockZ() + 7, true, true, false, true), childBranchDataItem.chunkCoordinate.getBlockZ() + 7, true).isLiquid())
		    				)
		    				{
		    					wasOnWater = true;
		    					canSpawn = false;
		    				}
		    			}

	        			if(canSpawn && bo3.getSettings().mustBeBelowOther)
	        			{
	        				canSpawn = checkMustBeBelowOther(childBranchDataItem);
	        				if(!canSpawn)
	        				{
	        					wasntBelowOther = true;
	        				}
	        			}

	        			if(canSpawn && bo3.getSettings().mustBeInsideBranches.size() > 0)
	        			{	        				
	        				canSpawn = checkMustBeInside(childBranchDataItem, bo3);
	        				if(!canSpawn)
	        				{
	        					wasntInsideOther = true;
	        				}
	        			}

	        			if(canSpawn && bo3.getSettings().cannotBeInsideBranches.size() > 0)
	        			{
	        				canSpawn = checkCannotBeInside(childBranchDataItem, bo3);
	        				if(!canSpawn)
	        				{
	        					cannotSpawnInsideOther = true;
	        				}
	        			}

	        		    if(canSpawn && bo3.getSettings().branchFrequency > 0)
	        		    {
	        		    	canSpawn = checkBranchFrequency(childBranchDataItem, bo3);
	        		    	if(!canSpawn)
	        		    	{
	        		    		branchFrequencyNotPassed = true;
	        		    	}
	        		    }
	        		    
	        		    if(canSpawn && bo3.getSettings().branchFrequencyGroups.size() > 0)
	        		    {
	        		    	canSpawn = checkBranchFrequencyGroups(childBranchDataItem, bo3);
	        		    	if(!canSpawn)
	        		    	{
	        		    		branchFrequencyGroupsNotPassed = true;
	        		    	}
	        		    }	        		    

	        			if(canSpawn)
	        			{
	        				// Returns collidingObject == null if if the branch cannot spawn in the given biome or if the given chunk is occupied by another structure
	    					collidingObjects = checkSpawnRequirementsAndCollisions(childBranchDataItem, minimumSize, world);
	        				if(collidingObjects.size() > 0)
	        				{
		    					canSpawn = false;
		    					collidedWithParentOrSibling = true;

		        				for(BranchDataItem collidingObject : collidingObjects)
		        				{
		        					// TODO: siblings canOverride children are not taken into account atm!
		        					// TODO: all canOverride branches are now being ignored, change that??

		        					if(collidingObject == null)
		        					{
		        						chunkIsIneligible = true;
		        						collidedWithParentOrSibling = false;
		        						break;
		        					}

	    							//OTG.log(LogMarker.INFO, "collided with: " + collidingObject.BO3Name);

		        					if(
	        							(
        									branchDataItem.parent == null ||
        									collidingObject.branch != branchDataItem.parent.branch
    									) &&
    									!((BO4) collidingObject.branch.getObject()).getSettings().canOverride
									)
		        					{
		        						boolean siblingFound = false;
		        						if(branchDataItem.parent != null)
		        						{
			        						for(BranchDataItem parentSibling : branchDataItem.parent.getChildren(false, world))
			        						{
			        							if(collidingObject.branch == parentSibling.branch)
			        							{
				        							siblingFound = true;
				        							break;
			        							}
			        						}
		        						}
		        						if(!siblingFound)
		        						{
			        						for(BranchDataItem sibling : branchDataItem.getChildren(false, world))
			        						{
			        							if(collidingObject.branch == sibling.branch)
			        							{
				        							siblingFound = true;
				        							break;
			        							}
			        						}
		        						}
		        						if(!siblingFound)
		        						{
		        							spaceIsOccupied = true;
		        							collidedWithParentOrSibling = false;
		        							break;
		        						}
		        					}
		        				}
	        				}
	        			}
	        		}

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

	        	    	if(childBranchDataItem.getChildren(false, world).size() == 0)
	        	    	{
	        	    		childBranchDataItem.doneSpawning = true;
	        	    	}

	        	    	// Mark any required branches in the same branch group so they wont try to spawn
		        		for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false, world))
		        		{
		        			if(
	        					childBranchDataItem2 != childBranchDataItem &&
	    						(childBranchDataItem.branch.branchGroup != null && childBranchDataItem.branch.branchGroup.length() >= 0) &&
	        					childBranchDataItem.branch.branchGroup.equals(childBranchDataItem2.branch.branchGroup) &&
    							childBranchDataItem2.branch.isRequiredBranch
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
			        		traverseAndSpawnChildBranches(childBranchDataItem, minimumSize, true, world);
			        		spawningRequiredChildrenForOptionalBranch = false;

			        		// Make sure the branch wasn't rolled back because the required branches couldn't spawn.
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
			        		traverseAndSpawnChildBranches(childBranchDataItem, minimumSize, true, world);
		        		}
		        	}

		        	if(!canSpawn)
		        	{
		        		if(!childBranchDataItem.doneSpawning && !childBranchDataItem.cannotSpawn)
		        		{
		        			// WasntBelowOther branches that cannot spawn get to retry
		        			// each cycle unless no branch spawned last cycle
		        			// TODO: Won't this cause problems?
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

			        	    	// Check if there are any more required branches in this group that haven't tried to spawn yet.
				        		for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false, world))
				        		{
				        			if(
			        					childBranchDataItem2 != childBranchDataItem &&
			    						(childBranchDataItem.branch.branchGroup != null && childBranchDataItem.branch.branchGroup.length() >= 0) &&
			        					childBranchDataItem.branch.branchGroup.equals(childBranchDataItem2.branch.branchGroup) &&
		    							childBranchDataItem2.branch.isRequiredBranch &&
				        				!childBranchDataItem2.doneSpawning &&
				        				!childBranchDataItem2.cannotSpawn
		        					)
				        			{
				        				branchGroupFailedSpawning = false;
				        				break;
		        					}
				        		}
			        		}

			        		if(!collidedWithParentOrSibling && (!wasntBelowOther || !spawnedBranchLastCycle) && branchGroupFailedSpawning)
			        		{
			            		// Branch could not spawn
			            		// abort this branch because it has a branch group that could not be spawned

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
			        	    			for(BranchDataItem collidingObject : collidingObjects)
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

			        	    		String reason = (branchFrequencyGroupsNotPassed ? "BranchFrequencyGroupNotPassed " : "") + (branchFrequencyNotPassed ? "BranchFrequencyNotPassed " : "") + (!isInsideWorldBorder ? "IsOutsideWorldBorder " : "") + (!startChunkBlockChecksPassed ? "StartChunkBlockChecksNotPassed " : "") + (collidedWithParentOrSibling ? "CollidedWithParentOrSibling " : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (wasntInsideOther ? "WasntInsideOther " : "") + (cannotSpawnInsideOther ? "CannotSpawnInsideOther " : "") + (wasntOnWater ? "WasntOnWater " : "") + (wasOnWater ? "WasOnWater " : "") + (!branchFrequencyGroupsNotPassed && !branchFrequencyNotPassed && isInsideWorldBorder && startChunkBlockChecksPassed && !wasntBelowOther && !cannotSpawnInsideOther && !wasntOnWater && !wasOnWater && !wasntBelowOther && !chunkIsIneligible && spaceIsOccupied ? "SpaceIsOccupied by" + occupiedByObjectsString : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (chunkIsIneligible ? "TerrainIsUnsuitable (StartChunkBlockChecks (height or material) not passed or Y < 0 or Frequency/BO3Group checks not passed or BO3 collided with other CustomStructure or smoothing area collided with other CustomStructure or BO3 not in allowed Biome or Smoothing area not in allowed Biome)" : "");
			        	    		OTG.log(LogMarker.INFO, "Rolling back X" + branchDataItem.branch.getChunkX() + " Z" + branchDataItem.branch.getChunkZ() + " Y" + branchDataItem.branch.getY() + " " + branchDataItem.branch.bo3Name + ":" + branchDataItem.branch.getRotation() + allParentsString + " because required branch "+ childBranchDataItem.branch.bo3Name + " couldn't spawn. Reason: " + reason);
			            		}

		            			rollBackBranch(branchDataItem, minimumSize, spawningRequiredBranchesOnly, world);
		            			bBreak = true;
			        		} else {
				        		// if this child branch could not spawn then in some cases other child branches won't be able to either
				        		// mark those child branches so they dont try to spawn and roll back the whole branch if a required branch can't spawn
				        		for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false, world))
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
		        										(wasntBelowOther && ((BO4)childBranchDataItem2.branch.getObject()).getSettings().mustBeBelowOther) ||
		        										(wasntOnWater && ((BO4)childBranchDataItem2.branch.getObject()).getSettings().spawnOnWaterOnly) ||
		        										(wasOnWater && !((BO4)childBranchDataItem2.branch.getObject()).getSettings().canSpawnOnWater)
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

							        	    	// Check if there are any more required branches in this group that haven't tried to spawn yet.
								        		for(BranchDataItem childBranchDataItem3 : branchDataItem.getChildren(false, world))
								        		{
								        			if(
							        					childBranchDataItem3 != childBranchDataItem2 &&
							    						(childBranchDataItem2.branch.branchGroup != null && childBranchDataItem2.branch.branchGroup.length() >= 0) &&
							    						childBranchDataItem2.branch.branchGroup.equals(childBranchDataItem3.branch.branchGroup) &&
							    						childBranchDataItem3.branch.isRequiredBranch &&
								        				!childBranchDataItem3.doneSpawning &&
								        				!childBranchDataItem3.cannotSpawn
						        					)
								        			{
								        				branchGroupFailedSpawning = false;
								        				break;
						        					}
								        		}
							        		}

					        				if(branchGroupFailedSpawning && !collidedWithParentOrSibling)
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
							        	    			for(BranchDataItem collidingObject : collidingObjects)
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
							        	    				(!isInsideWorldBorder ? "IsOutsideWorldBorder " : "") +
							        	    				(!startChunkBlockChecksPassed ? "StartChunkBlockChecksNotPassed " : "") +
							        	    				(collidedWithParentOrSibling ? "CollidedWithParentOrSibling " : "") +
							        	    				(wasntBelowOther ? "WasntBelowOther " : "") +
							        	    				(wasntInsideOther ? "WasntInsideOther " : "") +
							        	    				(cannotSpawnInsideOther ? "CannotSpawnInsideOther " : "") +
							        	    				(wasntOnWater ? "WasntOnWater " : "") +
							        	    				(wasOnWater ? "WasOnWater " : "") +
							        	    				(childBranchDataItem.branch.getY() < 0 ? " WasBelowY0 " : "") +
							        	    				(!branchFrequencyGroupsNotPassed && !branchFrequencyNotPassed && isInsideWorldBorder && startChunkBlockChecksPassed && !wasntBelowOther && !cannotSpawnInsideOther && !wasntOnWater && !wasOnWater && !wasntBelowOther && !chunkIsIneligible && spaceIsOccupied ? "SpaceIsOccupied by" + occupiedByObjectsString : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (chunkIsIneligible ? "ChunkIsIneligible: Either the chunk is occupied by another structure or a default structure, or the BO3/smoothing area is not allowed in the Biome)" : "");
							        	    		OTG.log(LogMarker.INFO, "Rolling back X" + branchDataItem.branch.getChunkX() + " Z" + branchDataItem.branch.getChunkZ() + " Y" + branchDataItem.branch.getY() + " " + branchDataItem.branch.bo3Name + ":" + branchDataItem.branch.getRotation() + allParentsString + " because required branch "+ childBranchDataItem.branch.bo3Name + " couldn't spawn. Reason: " + reason);
							            		}
						            			rollBackBranch(branchDataItem, minimumSize, spawningRequiredBranchesOnly, world);
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
	        // When calling AddBranches during a rollback to continue spawning a branch group don't traverse already spawned children (otherwise the branch could spawn children more than once per cycle).
	        if(
        		!traverseOnlySpawnedChildren &&
        		!spawningRequiredBranchesOnly &&
        		!branchDataItem.cannotSpawn
    		)
	        {
	        	for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false, world))
	        	{
	        		if(AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber))
	        		{
						if(
							(
								childBranchDataItem.branch.isRequiredBranch ||
								(
									SpawningCanOverrideBranches &&
									!((BO4)childBranchDataItem.branch.getObject()).getSettings().canOverride
								)
							) &&
							!childBranchDataItem.cannotSpawn &&
							(
								!childBranchDataItem.spawnDelayed ||
								!spawnedBranchLastCycle
							)
						)
						{
							traverseAndSpawnChildBranches(childBranchDataItem, minimumSize, spawningRequiredBranchesOnly, world);
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
	        	for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false, world))
	        	{
	        		if(AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber))
	        		{
						if(childBranchDataItem.branch.isRequiredBranch)
						{
							traverseAndSpawnChildBranches(childBranchDataItem, minimumSize, spawningRequiredBranchesOnly, world);
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
		int radius = bo3.getSettings().branchFrequency;
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
		if(bo3.getSettings().branchFrequencyGroups.size() > 0)
		{
	    	int radius = bo3.getSettings().branchFrequency;
        	float distanceBetweenStructures = 0;
        	int cachedChunkRadius = 0;
        	ChunkCoordinate cachedChunk = null;
        	for(Entry<String, Integer> entry : bo3.getSettings().branchFrequencyGroups.entrySet())
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
					!((BO4) branchDataItem2.branch.getObject()).getSettings().canOverride &&
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
			for(String cantBeInsideBO3 : bo3.getSettings().cannotBeInsideBranches)
			{
    			for(BranchDataItem branchDataItem3 : branchDataInChunk)
				{
					if(branchDataItem3 != childBranchDataItem && branchDataItem3 != childBranchDataItem.parent)
					{
						for(String branchName : ((BO4)branchDataItem3.branch.getObject()).getSettings().getInheritedBO3s()) // getInheritedBO3s also contains this BO3
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
			for(String mustBeInsideBO3 : bo3.getSettings().mustBeInsideBranches)
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
							for(String branchName : ((BO4)branchDataItem3.branch.getObject()).getSettings().getInheritedBO3s()) // getInheritedBO3s also contains this BO3
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

	private void rollBackBranch(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly, LocalWorld world)
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
    	deleteBranchChildren(branchData,minimumSize, spawningRequiredBranchesOnly, world);

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

    	if(!((BO4)branchData.branch.getObject()).getSettings().canOverride)
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
			    			if(((BO4)branchDataItem2.branch.getObject()).getSettings().mustBeBelowOther && branchDataItem2.chunkCoordinate.equals(branchData.chunkCoordinate))
			    			{
			    				boolean branchAboveFound = false;
			    				for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(branchDataItem2.chunkCoordinate))
		    					{
			    					if(
		    							branchDataItem3 != branchData &&
		    							!((BO4)branchDataItem3.branch.getObject()).getSettings().mustBeBelowOther &&
		    							!((BO4)branchDataItem3.branch.getObject()).getSettings().canOverride &&
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
			    					rollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world);
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
							((BO4)branchDataItem2.branch.getObject()).getSettings().mustBeInsideBranches.size() > 0 &&
							branchDataItem2.chunkCoordinate.equals(branchData.chunkCoordinate)
						)
		    			{
							boolean currentBO3Found = false;
							for(String mustBeInsideBO3Name : ((BO4)branchDataItem2.branch.getObject()).getSettings().mustBeInsideBranches)
							{
								for(String branchName : ((BO4)branchData.branch.getObject()).getSettings().getInheritedBO3s())
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
	    	    					rollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world);
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
    			rollBackBranch(branchData.parent, minimumSize, spawningRequiredBranchesOnly, world);
    		} else {

    			// Mark for spawning the parent and all other branches in the same branch group that spawn after this branch (unless they have already been spawned successfully)
    			boolean parentDoneSpawning = true;
    			boolean currentBranchFound = false;
        		for (BranchDataItem branchDataItem2 : branchData.parent.getChildren(false, world))
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
            				addBranches(branchData.parent, minimumSize, false, spawningRequiredBranchesOnly, world);
        				} else {
        					// 2. During the second phase of a cycle branch groups with optional branches are spawned, the optional branches get a chance to spawn first, after that the
        					// required branches try to spawn, if that fails the branch is rolled back.
        					// 3. A branch was rolled back that was a requirement for another branch (mustbeinside/mustbebelowother), causing the other branch to be rolled back as well.

                			// Since we're not using SpawningRequiredBranchesOnly AddBranches should only traverse child branches for any branches that it spawns from the branch group its re-trying.
        					// Otherwise some branches may have the same children traversed multiple times in a single phase.
            				addBranches(branchData.parent, minimumSize, true, spawningRequiredBranchesOnly, world);
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
        				addBranches(branchData.parent, minimumSize, false, spawningRequiredBranchesOnly, world);
        				spawningRequiredChildrenForOptionalBranch = true;
        			}
    			}
    		}
		}

    	branchData.isBeingRolledBack = false;
    }

    private void deleteBranchChildren(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly, LocalWorld world)
    {
    	// Remove all children of this branch from AllBranchesBranchData
    	Stack<BranchDataItem> children = branchData.getChildren(true, world);
        for(BranchDataItem branchDataItem : children)
        {
        	branchDataItem.cannotSpawn = true;
        	branchDataItem.doneSpawning = true;
        	branchDataItem.wasDeleted = true;

        	if(branchDataItem.getChildren(true, world).size() > 0)
        	{
    			deleteBranchChildren(branchDataItem, minimumSize, spawningRequiredBranchesOnly, world);
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

	        	if(!((BO4)branchDataItem.branch.getObject()).getSettings().canOverride)
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
			    	    			if(((BO4)branchDataItem2.branch.getObject()).getSettings().mustBeBelowOther && branchDataItem2.chunkCoordinate.equals(branchDataItem.chunkCoordinate))
			    	    			{
			    	    				boolean branchAboveFound = false;
			    	    				for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(branchDataItem2.chunkCoordinate))
		    	    					{
			    	    					if(
			        							branchDataItem3 != branchDataItem &&
			        							!((BO4)branchDataItem3.branch.getObject()).getSettings().mustBeBelowOther &&
			        							!((BO4)branchDataItem3.branch.getObject()).getSettings().canOverride &&
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
			    	    					rollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world);
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
			    					((BO4)branchDataItem2.branch.getObject()).getSettings().mustBeInsideBranches.size() > 0 &&
			    					branchDataItem2.chunkCoordinate.equals(branchDataItem.chunkCoordinate)
			    				)
			        			{
									boolean currentBO3Found = false;
									for(String mustBeInsideBO3Name : ((BO4)branchDataItem2.branch.getObject()).getSettings().mustBeInsideBranches)
									{
										for(String branchName : ((BO4)branchDataItem.branch.getObject()).getSettings().getInheritedBO3s())
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
			    	    					rollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly, world);
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
		for(Entry<String, Integer> entry : bo3.getSettings().branchFrequencyGroups.entrySet())
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
			for(Entry<String, Integer> entry : ((BO4)branchDataItem.branch.getObject()).getSettings().branchFrequencyGroups.entrySet())
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

    private Stack<BranchDataItem> checkSpawnRequirementsAndCollisions(BranchDataItem branchData, boolean minimumSize, LocalWorld world)
    {
    	// collidingObjects are only used for size > 0 check and to see if this branch tried to spawn on top of its parent
    	Stack<BranchDataItem> collidingObjects = new Stack<BranchDataItem>();
    	boolean bFound = false;

    	CustomStructureCoordinate coordObject = branchData.branch;

    	if(!minimumSize)
    	{
		    // Check if any other structures in world are in this chunk
		    if(!bFound && (world.isInsidePregeneratedRegion(branchData.chunkCoordinate) || world.getStructureCache().bo4StructureCache.containsKey(branchData.chunkCoordinate)))
		    {
		    	collidingObjects.add(null);
		    	bFound = true;
		    }

		    // Check if the structure can spawn in this biome
		    if(!bFound && !isStructureAtSpawn)
		    {
		    	ArrayList<String> biomeStructures;

            	LocalBiome biome3 = world.getBiome(branchData.chunkCoordinate.getChunkX() * 16 + 8, branchData.chunkCoordinate.getChunkZ() * 16 + 8);
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
                	collidingObjects.add(null);
                	bFound = true;
				}
		    }

	    	int smoothRadius = ((BO4)this.start.getObject()).getSettings().smoothRadius; // For collision detection use Start's SmoothingRadius. TODO: Improve this and use smoothingradius of individual branches?
	    	if(smoothRadius == -1 || ((BO4)coordObject.getObject()).getSettings().smoothRadius == -1)
	    	{
	    		smoothRadius = 0;
	    	}
	    	if(smoothRadius > 0 && !bFound)
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
	            		    	collidingObjects.add(null);
	            		    	bFound = true;
	            		    	break;
	            		    }

	            			if(!isStructureAtSpawn)
	            			{
		            		    // Check if the structure can spawn in this biome
		            			ArrayList<String> biomeStructures;

	        	            	LocalBiome biome3 = world.getBiome(x * 16 + 8, z * 16 + 8);
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
		                        	collidingObjects.add(null);
		                        	bFound = true;
		                        	break;
		        				}
	            			}
	            		}
	            	}
	            	if(bFound)
	            	{
	            		break;
	            	}
	        	}
	        }
    	}

        if(!bFound && !((BO4) coordObject.getObject()).getSettings().canOverride)
        {
	        Stack<BranchDataItem> existingBranches = new Stack<BranchDataItem>();
	        if(AllBranchesBranchDataByChunk.containsKey(branchData.chunkCoordinate))
	        {
	        	for(BranchDataItem existingBranchData : AllBranchesBranchDataByChunk.get(branchData.chunkCoordinate))
		        {
		        	if(branchData.chunkCoordinate.equals(existingBranchData.chunkCoordinate) && !((BO4)existingBranchData.branch.getObject()).getSettings().canOverride)
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

    // TODO: return list with colliding structures instead of bool?
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

    	CustomStructureCoordinate branchData1BranchMinRotated = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(((BO4)branchData1Branch.getObject()).getSettings().getminX(), ((BO4)branchData1Branch.getObject()).getSettings().getminY(), ((BO4)branchData1Branch.getObject()).getSettings().getminZ(), branchData1Branch.getRotation());
    	CustomStructureCoordinate branchData1BranchMaxRotated = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(((BO4)branchData1Branch.getObject()).getSettings().getmaxX(),((BO4)branchData1Branch.getObject()).getSettings().getmaxY(), ((BO4)branchData1Branch.getObject()).getSettings().getmaxZ(), branchData1Branch.getRotation());

        int startX = branchData1Branch.getX() + Math.min(branchData1BranchMinRotated.getX(),branchData1BranchMaxRotated.getX());
        int endX = branchData1Branch.getX() + Math.max(branchData1BranchMinRotated.getX(),branchData1BranchMaxRotated.getX());
        int startY = branchData1Branch.getY() + Math.min(branchData1BranchMinRotated.getY(),branchData1BranchMaxRotated.getY());
        int endY = branchData1Branch.getY() + Math.max(branchData1BranchMinRotated.getY(),branchData1BranchMaxRotated.getY());
        int startZ = branchData1Branch.getZ() + Math.min(branchData1BranchMinRotated.getZ(),branchData1BranchMaxRotated.getZ());
        int endZ = branchData1Branch.getZ() + Math.max(branchData1BranchMinRotated.getZ(),branchData1BranchMaxRotated.getZ());

        CustomStructureCoordinate branchData2BranchMinRotated = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(((BO4)branchData2Branch.getObject()).getSettings().getminX(), ((BO4)branchData2Branch.getObject()).getSettings().getminY(), ((BO4)branchData2Branch.getObject()).getSettings().getminZ(), branchData2Branch.getRotation());
        CustomStructureCoordinate branchData2BranchMaxRotated = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(((BO4)branchData2Branch.getObject()).getSettings().getmaxX(), ((BO4) branchData2Branch.getObject()).getSettings().getmaxY(), ((BO4)branchData2Branch.getObject()).getSettings().getmaxZ(), branchData2Branch.getRotation());

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
     * Add the object to the list of BO3's to be spawned for this chunk
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

    // This method gets called by other chunks spawning their structures to
    // finish any branches going to this chunk
    /**
    * Checks if this structure or any of its branches are inside the given
    * chunk and spawns all objects that are including their smoothing areas (if any)
    *
    * @param chunkCoordinate
    */
    public boolean spawnForChunkOTGPlus(ChunkCoordinate chunkCoordinate, LocalWorld world)
    {
    	//OTG.log(LogMarker.INFO, "SpawnForChunk X" + chunkCoordinate.getChunkX() + " Z" + chunkCoordinate.getChunkZ() + " " + Start.BO3Name);

        // If this structure is not allowed to spawn because a structure
        // of the same type (this.Start BO3 filename) has already been
        // spawned nearby.
    	if(this.start == null)
    	{
			throw new RuntimeException();
    	}
    	if ((!objectsToSpawn.containsKey(chunkCoordinate) && !smoothingAreasToSpawn.containsKey(chunkCoordinate)))
        {
            return true;
        }

    	doStartChunkBlockChecks(world);

        // Get all BO3's that should spawn in the given chunk, if any
        // Note: The given chunk may not necessarily be the chunkCoordinate of this.Start
        Stack<BO4CustomStructureCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
        if (objectsInChunk != null)
        {
        	BO4Config config = ((BO4)this.start.getObject()).getSettings();
            LocalBiome biome = null;
            BiomeConfig biomeConfig = null;
            if(config.spawnUnderWater)
        	{
            	biome = world.getBiome(this.start.getX() + 8, this.start.getZ() + 7);
            	biomeConfig = biome.getBiomeConfig();
            	if(biomeConfig == null)
            	{
            		throw new RuntimeException(); // TODO: Remove after testing
            	}
        	}

            // Do ReplaceAbove / ReplaceBelow
            for (BO4CustomStructureCoordinate coordObject : objectsInChunk)
            {
                if (coordObject.isSpawned)
                {
                    continue;
                }

                BO4 bo3 = ((BO4)coordObject.getObject());
                if(bo3 == null)
                {
                	throw new RuntimeException(); // TODO: Remove after testing
                }

                BO4Config objectConfig = bo3.getSettings();

                if (!coordObject.spawnWithChecks(chunkCoordinate, world, random, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceAbove : objectConfig.replaceAbove, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceBelow : objectConfig.replaceBelow, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithBiomeBlocks : objectConfig.replaceWithBiomeBlocks, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithSurfaceBlock : objectConfig.replaceWithSurfaceBlock, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithGroundBlock : objectConfig.replaceWithGroundBlock, config.spawnUnderWater,  !config.spawnUnderWater ? -1 : (biomeConfig.useWorldWaterLevel ? world.getConfigs().getWorldConfig().waterLevelMax : biomeConfig.waterLevelMax), false, true))
                {
                	OTG.log(LogMarker.FATAL, "Could not spawn chunk " + coordObject.bo3Name + " for structure " + this.start.getObject().getName());
                	throw new RuntimeException("Could not spawn chunk " + coordObject.bo3Name + " for structure " + this.start.getObject().getName());
                }
            }

            // Spawn smooth areas in this chunk if any exist
            // If SpawnSmoothAreas returns false then spawning has
            // been delayed and should be tried again later.
        	if(!smoothingAreaManager.spawnSmoothAreas(chunkCoordinate, smoothingAreasToSpawn, this.start, world))
        	{
        		BO4.OriginalTopBlocks.clear(); // TODO: Make this prettier
        		return false;
    		}

            for (BO4CustomStructureCoordinate coordObject : objectsInChunk)
            {
                if (coordObject.isSpawned)
                {
                    continue;
                }

                BO4 bo3 = ((BO4)coordObject.getObject());
                if(bo3 == null)
                {
                	throw new RuntimeException(); // TODO: Remove this after testing
                }

                BO4Config objectConfig = bo3.getSettings();

                if (!coordObject.spawnWithChecks(chunkCoordinate, world, random, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceAbove : objectConfig.replaceAbove, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceBelow : objectConfig.replaceBelow, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithBiomeBlocks : objectConfig.replaceWithBiomeBlocks, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithSurfaceBlock : objectConfig.replaceWithSurfaceBlock, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithGroundBlock : objectConfig.replaceWithGroundBlock, config.spawnUnderWater,  !config.spawnUnderWater ? -1 : (biomeConfig.useWorldWaterLevel ? world.getConfigs().getWorldConfig().waterLevelMax : biomeConfig.waterLevelMax), false, false))
                //if(1 == 0)
                {
                	OTG.log(LogMarker.FATAL, "Could not spawn chunk " + coordObject.bo3Name + " for structure " + this.start.getObject().getName());
                	throw new RuntimeException("Could not spawn chunk " + coordObject.bo3Name + " for structure " + this.start.getObject().getName());
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
            // If SpawnSmoothAreas returns false then spawning has
            // been delayed and should be tried again later.
        	if(!smoothingAreaManager.spawnSmoothAreas(chunkCoordinate, smoothingAreasToSpawn, this.start, world))
        	{
        		BO4.OriginalTopBlocks.clear(); // TODO: Make this prettier
        		return false;
    		}
        }

		objectsToSpawn.remove(chunkCoordinate);
		smoothingAreasToSpawn.remove(chunkCoordinate);	
		BO4.OriginalTopBlocks.clear(); 	
        return true;
    }
}
