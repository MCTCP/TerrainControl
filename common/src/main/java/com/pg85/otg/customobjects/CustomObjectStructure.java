package com.pg85.otg.customobjects;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.customobjects.bo3.BlockFunction;
import com.pg85.otg.customobjects.bo3.EntityFunction;
import com.pg85.otg.customobjects.bo3.ModDataFunction;
import com.pg85.otg.customobjects.bo3.ParticleFunction;
import com.pg85.otg.customobjects.bo3.RandomBlockFunction;
import com.pg85.otg.customobjects.bo3.SpawnerFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.generator.surface.MesaSurfaceGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.LocalMaterialData;
import com.pg85.otg.util.NamedBinaryTag;
import com.pg85.otg.util.Rotation;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

import java.util.*;
import java.util.Map.Entry;

/**
 * Represents a collection of all {@link CustomObject}s in a structure. It is
 * calculated by finding the branches of one object, then finding the branches
 * of those branches, etc., until
 * {@link CustomObject#getMaxBranchDepth()} is reached.
 *
 */
public class CustomObjectStructure
{
	// OTG+

	public SmoothingArea smoothingAreaManager = new SmoothingArea();
	
	public HashSet<ModDataFunction> modData = new HashSet<ModDataFunction>();
	public HashSet<SpawnerFunction> spawnerData = new HashSet<SpawnerFunction>();
	public HashSet<ParticleFunction> particleData = new HashSet<ParticleFunction>();

	public boolean saveRequired = true;

    protected LocalWorld World;
    protected Random Random;

    // The origin BO3 for this branching structure
    public CustomObjectCoordinate Start;

    // Stores all the branches of this branching structure that should spawn along with the chunkcoordinates they should spawn in
    public Map<ChunkCoordinate, Stack<CustomObjectCoordinate>> ObjectsToSpawn = new HashMap<ChunkCoordinate, Stack<CustomObjectCoordinate>>();
    public Map<ChunkCoordinate, String> ObjectsToSpawnInfo = new HashMap<ChunkCoordinate, String>();

    public boolean IsSpawned;
    // If the origin structure of this branching structure has tried to spawn but could not not and never will.
    public boolean CannotSpawn;
    public boolean IsStructureAtSpawn = false;

    int MinY;

    boolean IsOTGPlus = false;

    // A smoothing area is drawn around all outer blocks (or blocks neighbouring air) on the lowest layer of blocks in each BO3 of this branching structure that has a SmoothRadius set greater than 0.
    // Object[] { int startpoint, int endpoint, int distance from real startpoint }
    Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

    public CustomObjectStructure(LocalWorld world, CustomObjectCoordinate structureStart, Map<ChunkCoordinate, Stack<CustomObjectCoordinate>> objectsToSpawn, Map<ChunkCoordinate, ArrayList<Object[]>> smoothingAreasToSpawn, int minY)
    {
    	this(world, structureStart, false, false);
    	ObjectsToSpawn = objectsToSpawn;
    	SmoothingAreasToSpawn = smoothingAreasToSpawn;
    	MinY = minY;
    }

    public boolean startChunkBlockChecksDone = false;
    private boolean DoStartChunkBlockChecks()
    {
    	if(!startChunkBlockChecksDone)
    	{
    		saveRequired = true;
	    	startChunkBlockChecksDone = true;

	    	//OTG.log(LogMarker.INFO, "DoStartChunkBlockChecks");

			// Requesting the Y position or material of a block in an unpopulated chunk causes some of that chunk's blocks to be calculated, this is expensive and should be kept at a minimum.

			// Y checks:
			// If BO3's have a minimum and maximum Y configured by the player then we don't really need
	    	// to check if the BO3 fits in the Y direction, that is the player's responsibility!

			// Material checks:
			// A BO3 may need to perform material checks to when using !CanSpawnOnWater or SpawnOnWaterOnly

	    	int startY = 0;

			if(((BO3)Start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestBlock || ((BO3)Start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock)
			{
				if(((BO3)Start.getObject()).getSettings().SpawnAtWaterLevel)
				{
					LocalBiome biome = World.getBiome(Start.getX() + 8, Start.getZ() + 7);
					startY = biome.getBiomeConfig().useWorldWaterLevel ? World.getConfigs().getWorldConfig().waterLevelMax : biome.getBiomeConfig().waterLevelMax;
				} else {
					// OTG.log(LogMarker.INFO, "Request height for chunk X" + ChunkCoordinate.fromBlockCoords(Start.getX(), Start.getZ()).getChunkX() + " Z" + ChunkCoordinate.fromBlockCoords(Start.getX(), Start.getZ()).getChunkZ());
					// If this chunk has not yet been populated then this will cause it to be! (ObjectSpawner.Populate() is called)

					int highestBlock = 0;

					if(!((BO3)Start.getObject()).getSettings().SpawnUnderWater)
					{
						highestBlock = World.getHighestBlockYAt(Start.getX() + 8, Start.getZ() + 7, true, true, false, true);
					} else {
						highestBlock = World.getHighestBlockYAt(Start.getX() + 8, Start.getZ() + 7, true, false, true, true);
					}

					if(highestBlock < 1)
					{
						//OTG.log(LogMarker.INFO, "Structure " + Start.BO3Name + " could not be plotted at Y < 1. If you are creating empty chunks intentionally (for a sky world for instance) then make sure you don't use the highestBlock setting for your BO3's");
						if(((BO3)Start.getObject()).getSettings().heightOffset > 0) // Allow floating structures that use highestblock + heightoffset
						{
							highestBlock = ((BO3)Start.getObject()).getSettings().heightOffset;
						} else {
							return false;
						}
					} else {
						startY  = highestBlock + 1;
					}
				}
			} else {
				if(((BO3)Start.getObject()).getSettings().maxHeight != ((BO3)Start.getObject()).getSettings().minHeight)
				{
					startY = ((BO3)Start.getObject()).getSettings().minHeight + new Random().nextInt(((BO3)Start.getObject()).getSettings().maxHeight - ((BO3)Start.getObject()).getSettings().minHeight);
				} else {
					startY = ((BO3)Start.getObject()).getSettings().minHeight;
				}
			}

			//if((MinY + startY) < 1 || (startY) < ((BO3)Start.getObject(World.getName())).settings.minHeight || (startY) > ((BO3)Start.getObject(World.getName())).settings.maxHeight)
			if(startY < ((BO3)Start.getObject()).getSettings().minHeight || startY > ((BO3)Start.getObject()).getSettings().maxHeight)
			{
				return false;
				//throw new IllegalArgumentException("Structure could not be plotted at these coordinates, it does not fit in the Y direction. " + ((BO3)Start.getObject(World.getName())).getName() + " at Y " + startY);
			}

			startY += ((BO3)Start.getObject()).getSettings().heightOffset;

			if(startY < OTG.WORLD_DEPTH || startY >= OTG.WORLD_HEIGHT)
			{
				return false;
			}

			for(ChunkCoordinate chunkCoord : ObjectsToSpawn.keySet())
			{
				for(CustomObjectCoordinate BO3 : ObjectsToSpawn.get(chunkCoord))
				{
					BO3.y += startY;
				}
			}

			Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawn2 = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();
			SmoothingAreasToSpawn2.putAll(SmoothingAreasToSpawn);
			SmoothingAreasToSpawn.clear();
			for(ChunkCoordinate chunkCoord2 : SmoothingAreasToSpawn2.keySet())
			{
				ArrayList<Object[]> coords = new ArrayList<Object[]>();
				Object[] coordToAdd;
				for(Object[] coord : SmoothingAreasToSpawn2.get(chunkCoord2))
				{
					if(coord.length == 18)
					{
						coordToAdd = new Object[]{ ((Integer)coord[0]), ((Integer)coord[1]) + Start.getY(), ((Integer)coord[2]), ((Integer)coord[3]), ((Integer)coord[4]) + Start.getY(), ((Integer)coord[5]), ((Integer)coord[6]), -1, ((Integer)coord[8]), ((Integer)coord[9]), -1, ((Integer)coord[11]), ((Integer)coord[12]), ((Integer)coord[13]) + Start.getY(), ((Integer)coord[14]), ((Integer)coord[15]), -1, ((Integer)coord[17]) };
						coords.add(coordToAdd);
					}
					else if(coord.length == 12)
					{
						coordToAdd = new Object[]{ ((Integer)coord[0]), ((Integer)coord[1]) + Start.getY(), ((Integer)coord[2]), ((Integer)coord[3]), ((Integer)coord[4]) + Start.getY(), ((Integer)coord[5]), ((Integer)coord[6]), ((Integer)coord[7]) + Start.getY(), ((Integer)coord[8]), ((Integer)coord[9]), -1, ((Integer)coord[11]) };
						coords.add(coordToAdd);
					} else {
						throw new RuntimeException();
					}
				}
				SmoothingAreasToSpawn.put(ChunkCoordinate.fromChunkCoords(chunkCoord2.getChunkX(), chunkCoord2.getChunkZ()), coords);
			}

			Start.y = startY;
    	}
    	return true;
    }

    int branchesTried = 0;

    public CustomObjectStructure(LocalWorld world, CustomObjectCoordinate start, boolean spawn, boolean isStructureAtSpawn)
    {
        World = world;
        IsStructureAtSpawn = isStructureAtSpawn;
        IsOTGPlus = true;

        if(start == null)
        {
        	return;
        }
        if (!(start.getObject() instanceof StructuredCustomObject))
        {
            throw new IllegalArgumentException("Start object must be a structure!");
        }

        Start = start;
        Random = RandomHelper.getRandomForCoords(start.getX() + 8, start.getY(), start.getZ() + 7, world.getSeed());

		if(spawn)
		{
			branchesTried = 0;

			long startTime = System.currentTimeMillis();

			// Structure at spawn can't hurt to query source blocks, structures with randomY don't need to do any block checks so don't hurt either.
			//if(isStructureAtSpawn || ((BO3)Start.getObject(World.getName())).settings.spawnHeight == SpawnHeightEnum.randomY)
			{
				if(!DoStartChunkBlockChecks()){ return; } // Just do the damn checks to get the height right....
			}

			// Only detect Y or material of source block if necessary to prevent chunk loading
			// if this BO3 is being plotted in a chunk that has not yet been populated.

			// Need to know the height if this structure can only spawn at a certain height
			//if((((BO3)Start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestBlock || ((BO3)Start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock) && (World.getConfigs().getWorldConfig().disableBedrock || ((BO3)Start.getObject()).getSettings().minHeight > 1 || ((BO3)Start.getObject()).getSettings().maxHeight < 256))
			{
				//if(!DoStartChunkBlockChecks()){ return; }
			}

			if(!((BO3)Start.getObject()).getSettings().CanSpawnOnWater)
			{
				//if(!DoStartChunkBlockChecks()){ return; }
				int highestBlocky = world.getHighestBlockYAt(Start.getX() + 8, Start.getZ() + 7, true, true, false, true);;
				//if(Start.y - 1 > OTG.WORLD_DEPTH && Start.y - 1 < OTG.WORLD_HEIGHT && world.getMaterial(Start.getX() + 8, Start.y - 1, Start.getZ() + 7).isLiquid())
				if(Start.y - 1 > OTG.WORLD_DEPTH && Start.y - 1 < OTG.WORLD_HEIGHT && world.getMaterial(Start.getX() + 8, highestBlocky, Start.getZ() + 7, IsOTGPlus).isLiquid())
				{
					return;
				}
			}

			if(((BO3)Start.getObject()).getSettings().SpawnOnWaterOnly)
			{
				//if(!DoStartChunkBlockChecks()){ return; }
				if(
					!(
						world.getMaterial(Start.getX(), Start.y - 1, Start.getZ(), IsOTGPlus).isLiquid() &&
						world.getMaterial(Start.getX(), Start.y - 1, Start.getZ() + 15, IsOTGPlus).isLiquid() &&
						world.getMaterial(Start.getX() + 15, Start.y - 1, Start.getZ(), IsOTGPlus).isLiquid() &&
						world.getMaterial(Start.getX() + 15, Start.y - 1, Start.getZ() + 15, IsOTGPlus).isLiquid()
					)
				)
				{
					return;
				}
			}

			try
			{
				CalculateBranches(false);
			} catch (InvalidConfigException ex) {
				OTG.log(LogMarker.FATAL, "An unknown error occurred while calculating branches for BO3 " + Start.BO3Name + ". This is probably an error in the BO3's branch configuration, not a bug. If you can track this down, please tell me what caused it!");
				throw new RuntimeException();
			}

			for(Entry<ChunkCoordinate, Stack<CustomObjectCoordinate>> chunkCoordSet : ObjectsToSpawn.entrySet())
			{
				String structureInfo = "";
				for(CustomObjectCoordinate customObjectCoord : chunkCoordSet.getValue())
				{
					structureInfo += customObjectCoord.getObject().getName() + ":" + customObjectCoord.getRotation() + ", ";
				}
				if(structureInfo.length() > 0)
				{
					structureInfo = structureInfo.substring(0,  structureInfo.length() - 2);
					ObjectsToSpawnInfo.put(chunkCoordSet.getKey(), "Branches in chunk X" + chunkCoordSet.getKey().getChunkX() + " Z" + chunkCoordSet.getKey().getChunkZ() + " : " + structureInfo);
				}
			}

			for(Entry<ChunkCoordinate, Stack<CustomObjectCoordinate>> chunkCoordSet : ObjectsToSpawn.entrySet())
			{
	        	// Don't spawn BO3's that have been overriden because of replacesBO3
	        	for (CustomObjectCoordinate coordObject : chunkCoordSet.getValue())
	        	{
	        		BO3Config objectConfig = ((BO3)coordObject.getObject()).getSettings();
	        		if(objectConfig.replacesBO3 != null && objectConfig.replacesBO3.length() > 0)
	        		{
	        			String[] BO3sToReplace = objectConfig.replacesBO3.split(",");
	        			for(String BO3ToReplace : BO3sToReplace)
	        			{
	        				for (CustomObjectCoordinate coordObjectToReplace : chunkCoordSet.getValue())
	        				{
	        					if(((BO3)coordObjectToReplace.getObject()).getName().trim().equals(BO3ToReplace.trim()))
	        					{
	        						if(CheckCollision(coordObject, coordObjectToReplace))
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
			SmoothingAreasToSpawn = smoothingAreaManager.CalculateSmoothingAreas(ObjectsToSpawn, Start, World);
			smoothingAreaManager.CustomObjectStructureSpawn(SmoothingAreasToSpawn);			
			
			for(ChunkCoordinate chunkCoord : ObjectsToSpawn.keySet())
			{
				World.getStructureCache().structureCache.put(chunkCoord, this);
				World.getStructureCache().structuresPerChunk.put(chunkCoord, new ArrayList<String>());
				// Make sure not to override any ModData/Spawner/Particle data added by CustomObjects
				if(World.getStructureCache().worldInfoChunks.containsKey(chunkCoord))
				{
					CustomObjectStructure existingObject = World.getStructureCache().worldInfoChunks.get(chunkCoord);
					this.modData.addAll(existingObject.modData);
					this.particleData.addAll(existingObject.particleData);
					this.spawnerData.addAll(existingObject.spawnerData);
				}
				World.getStructureCache().worldInfoChunks.put(chunkCoord, this);
			}

			for(ChunkCoordinate chunkCoord : SmoothingAreasToSpawn.keySet())
			{
				World.getStructureCache().structureCache.put(chunkCoord, this);
				World.getStructureCache().structuresPerChunk.put(chunkCoord, new ArrayList<String>());
				// Make sure not to override any ModData/Spawner/Particle data added by CustomObjects
				if(World.getStructureCache().worldInfoChunks.containsKey(chunkCoord))
				{
					CustomObjectStructure existingObject = World.getStructureCache().worldInfoChunks.get(chunkCoord);
					this.modData.addAll(existingObject.modData);
					this.particleData.addAll(existingObject.particleData);
					this.spawnerData.addAll(existingObject.spawnerData);
				}
				World.getStructureCache().worldInfoChunks.put(chunkCoord, this);
			}

			if(ObjectsToSpawn.size() > 0)
			{
				IsSpawned = true;
				if(OTG.getPluginConfig().SpawnLog)
				{
					int totalBO3sSpawned = 0;
					for(ChunkCoordinate entry : ObjectsToSpawn.keySet())
					{
						totalBO3sSpawned += ObjectsToSpawn.get(entry).size();
					}

					OTG.log(LogMarker.INFO, Start.getObject().getName() + " " + totalBO3sSpawned + " object(s) plotted in " + (System.currentTimeMillis() - startTime) + " Ms and " + Cycle + " cycle(s), " + (branchesTried + 1) + " object(s) tried.");
				}
			}
		}
    }
    
    /**
     * Gets an Object[] { ChunkCoordinate, ChunkCoordinate } containing the top left and bottom right chunk
     * If this structure were spawned as small as possible (with branchDepth 0)
     * @param world
     * @param start
     * @return
     * @throws InvalidConfigException
     */
    public Object[] GetMinimumSize() throws InvalidConfigException
    {
    	if(
			((BO3)Start.getObject()).getSettings().MinimumSizeTop != -1 &&
			((BO3)Start.getObject()).getSettings().MinimumSizeBottom != -1 &&
			((BO3)Start.getObject()).getSettings().MinimumSizeLeft != -1 &&
			((BO3)Start.getObject()).getSettings().MinimumSizeRight != -1)
    	{
    		Object[] returnValue = { ((BO3)Start.getObject()).getSettings().MinimumSizeTop, ((BO3)Start.getObject()).getSettings().MinimumSizeRight, ((BO3)Start.getObject()).getSettings().MinimumSizeBottom, ((BO3)Start.getObject()).getSettings().MinimumSizeLeft };
    		return returnValue;
    	}
    	
    	CalculateBranches(true);

        // Calculate smoothing areas around the entire branching structure
        // Smooth the terrain in all directions bordering the structure so
        // that there is a smooth transition in height from the surrounding
        // terrain to the BO3. This way BO3's won't float above the ground
        // or spawn inside a hole with vertical walls.

		// Don't calculate smoothing areas for minimumSize, instead just add smoothradius / 16 to each side

		ChunkCoordinate startChunk = ChunkCoordinate.fromBlockCoords(Start.getX(), Start.getZ());

		ChunkCoordinate top = startChunk;
		ChunkCoordinate left = startChunk;
		ChunkCoordinate bottom = startChunk;
		ChunkCoordinate right = startChunk;

		for(ChunkCoordinate chunkCoord : ObjectsToSpawn.keySet())
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
			for(CustomObjectCoordinate struct : ObjectsToSpawn.get(chunkCoord))
			{
				if(struct.getY() < MinY)
				{
					MinY = struct.getY();
				}
			}
		}

		MinY += ((BO3)Start.getObject()).getSettings().heightOffset;

		int smoothingRadiusInChunks = (int)Math.ceil(((BO3)Start.getObject()).getSettings().smoothRadius / (double)16);  // TODO: this assumes that smoothradius is the same for every BO3 within this structure, child branches may have overriden their own smoothradius! This may cause problems if a child branch has a larger smoothradius than the starting structure
    	((BO3)Start.getObject()).getSettings().MinimumSizeTop = Math.abs(startChunk.getChunkZ() - top.getChunkZ()) + smoothingRadiusInChunks;
    	((BO3)Start.getObject()).getSettings().MinimumSizeRight = Math.abs(startChunk.getChunkX() - right.getChunkX()) + smoothingRadiusInChunks;
    	((BO3)Start.getObject()).getSettings().MinimumSizeBottom = Math.abs(startChunk.getChunkZ() - bottom.getChunkZ()) + smoothingRadiusInChunks;
    	((BO3)Start.getObject()).getSettings().MinimumSizeLeft = Math.abs(startChunk.getChunkX() - left.getChunkX()) + smoothingRadiusInChunks;

    	Object[] returnValue = { ((BO3)Start.getObject()).getSettings().MinimumSizeTop, ((BO3)Start.getObject()).getSettings().MinimumSizeRight, ((BO3)Start.getObject()).getSettings().MinimumSizeBottom, ((BO3)Start.getObject()).getSettings().MinimumSizeLeft };

    	if(OTG.getPluginConfig().SpawnLog)
    	{
    		OTG.log(LogMarker.INFO, "");
        	OTG.log(LogMarker.INFO, Start.getObject().getName() + " minimum size: Width " + ((Integer)returnValue[1] + (Integer)returnValue[3] + 1) + " Length " + ((Integer)returnValue[0] + (Integer)returnValue[2] + 1) + " top " + (Integer)returnValue[0] + " right " + (Integer)returnValue[1] + " bottom " + (Integer)returnValue[2] + " left " + (Integer)returnValue[3]);
    	}

    	ObjectsToSpawn.clear();

    	return returnValue;
    }

    public Stack<BranchDataItem> AllBranchesBranchData = new Stack<BranchDataItem>();
    public HashMap<ChunkCoordinate, Stack<BranchDataItem>> AllBranchesBranchDataByChunk = new HashMap<ChunkCoordinate, Stack<BranchDataItem>>();
    public HashSet<Integer> AllBranchesBranchDataHash = new HashSet<Integer>();
    private boolean SpawningCanOverrideBranches = false;
    int Cycle = 0;

    // TODO: Make sure that canOverride optional branches cannot be in the same branch group as required branches.
    // This makes sure that when the first spawn phase is complete and all required branches and non-canOverride optional branches have spawned
    // those can never be rolled back because of canOverride optional branches that are unable to spawn.
    // canOverride required branches: things that need to be spawned in the same cycle as their parent branches, for instance door/wall markers for rooms
    // canOverride optional branches: things that should be spawned after the base of the structure has spawned, for instance room interiors, adapter/modifier pieces that knock out walls/floors between rooms etc.

    public void CalculateBranches(boolean minimumSize) throws InvalidConfigException
    {
    	if(OTG.getPluginConfig().SpawnLog)
    	{
	    	String sminimumSize = minimumSize ? " (minimumSize)" : "";
	    	OTG.log(LogMarker.INFO, "");
	    	OTG.log(LogMarker.INFO, "-------- CalculateBranches " + Start.BO3Name + sminimumSize +" --------");
    	}

        BranchDataItem branchData = new BranchDataItem(World, Random, null, Start, null, 0, 0, minimumSize);

        if(OTG.getPluginConfig().SpawnLog)
        {
        	OTG.log(LogMarker.INFO, "");
	        OTG.log(LogMarker.INFO, "---- Cycle 0 ----");
	        OTG.log(LogMarker.INFO, "Plotted X" + branchData.ChunkCoordinate.getChunkX() + " Z" + branchData.ChunkCoordinate.getChunkZ() + " - " + branchData.Branch.getObject().getName());
        }

    	AllBranchesBranchData.add(branchData);
    	AllBranchesBranchDataHash.add(branchData.branchNumber);
		Stack<BranchDataItem> branchDataItemStack = AllBranchesBranchDataByChunk.get(branchData.ChunkCoordinate);
		if(branchDataItemStack != null)
		{
			branchDataItemStack.add(branchData);
		} else {
			branchDataItemStack = new Stack<BranchDataItem>();
			branchDataItemStack.add(branchData);
			AllBranchesBranchDataByChunk.put(branchData.ChunkCoordinate, branchDataItemStack);
		}

    	Cycle = 0;
    	boolean canOverrideBranchesSpawned = false;
    	SpawningCanOverrideBranches = false;
    	boolean processingDone = false;
    	while(!processingDone)
    	{
    		SpawnedBranchLastCycle = SpawnedBranchThisCycle;
    		SpawnedBranchThisCycle = false;

    		Cycle += 1;

    		if(OTG.getPluginConfig().SpawnLog)
    		{
    			OTG.log(LogMarker.INFO, "");
    			OTG.log(LogMarker.INFO, "---- Cycle " + Cycle + " ----");
    		}

    		TraverseAndSpawnChildBranches(branchData, minimumSize, true);

			if(OTG.getPluginConfig().SpawnLog)
			{
				OTG.log(LogMarker.INFO, "All branch groups with required branches only have been processed for cycle " + Cycle + ", plotting branch groups with optional branches.");
			}
			TraverseAndSpawnChildBranches(branchData, minimumSize, false);

			processingDone = true;
            for(BranchDataItem branchDataItem3 : AllBranchesBranchData)
            {
            	if(!branchDataItem3.DoneSpawning)
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
	            	for(BranchDataItem childBranch : branchDataItem3.getChildren(false))
	            	{
	            		if(
            				!childBranch.Branch.isRequiredBranch &&
            				((BO3)childBranch.Branch.getObject()).getSettings().canOverride
        				)
	            		{
	            			branchDataItem3.DoneSpawning = false;
	            			childBranch.DoneSpawning = false;
	            			childBranch.CannotSpawn = false;

	            			if(branchDataItem3.wasDeleted)
	            			{
	            				throw new RuntimeException();
	            			}

	            			if(childBranch.wasDeleted)
	            			{
	            				throw new RuntimeException();
	            			}
	            		}
	            	}
	            }
            }

    		if(branchData.CannotSpawn)
    		{
    			if(minimumSize)
    			{
    				if(OTG.getPluginConfig().SpawnLog)
    				{
    					OTG.log(LogMarker.WARN, "Error: Branching BO3 " + Start.BO3Name + " could not be spawned in minimum configuration (isRequiredBranch branches only).");
    				}
            		throw new InvalidConfigException("Error: Branching BO3 " + Start.BO3Name + " could not be spawned in minimum configuration (isRequiredBranch branches only).");
    			}
    			return;
    		}
    	}

        for(BranchDataItem branchToAdd : AllBranchesBranchData)
        {
        	if(!branchToAdd.CannotSpawn)
        	{
        		if(branchToAdd.Branch == null)
        		{
        			throw new RuntimeException();
        		}
        		AddToChunk(branchToAdd.Branch, branchToAdd.ChunkCoordinate, ObjectsToSpawn);
        	}
        }
    }

    BranchDataItem currentSpawningRequiredChildrenForOptionalBranch;
    boolean SpawningRequiredChildrenForOptionalBranch = false;
    boolean SpawnedBranchThisCycle = false;
    boolean SpawnedBranchLastCycle = false;
    private void TraverseAndSpawnChildBranches(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly)
    {
    	if(!branchData.DoneSpawning)
    	{
    		AddBranches(branchData, minimumSize, false, spawningRequiredBranchesOnly);
    	} else {
    		if(!branchData.CannotSpawn)
    		{
    			for(BranchDataItem branchDataItem2 : branchData.getChildren(false))
    			{
    				// BranchData.DoneSpawning can be set to true by a child branch
    				// that tried to spawn but couldnt
    				if(!branchDataItem2.CannotSpawn && branchData.DoneSpawning)
    				{
    					TraverseAndSpawnChildBranches(branchDataItem2, minimumSize, spawningRequiredBranchesOnly);
    				}
    			}
    		}
    	}
    }

    private void AddBranches(BranchDataItem branchDataItem, boolean minimumSize, boolean traverseOnlySpawnedChildren, boolean spawningRequiredBranchesOnly)
    {
    	// CanOverride optional branches are spawned only after the main structure has spawned.
    	// This is useful for adding interiors and knocking out walls between rooms
    	if(!SpawningCanOverrideBranches)
    	{
	    	for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false))
	    	{
	    		if(
    				(
						!branchDataItem3.CannotSpawn ||
						!branchDataItem3.DoneSpawning
					) && (
						((BO3)branchDataItem3.Branch.getObject()).getSettings().canOverride &&
						!branchDataItem3.Branch.isRequiredBranch
					)
				)
	    		{
	    			branchDataItem3.CannotSpawn = true;
	    			branchDataItem3.DoneSpawning = true;
	    		}
	    	}
    	}

    	// TODO: Remove these
    	if(SpawningRequiredChildrenForOptionalBranch && traverseOnlySpawnedChildren)
    	{
    		throw new RuntimeException();
    	}

    	// If we are spawning optional branches then we know this branch will be done spawning when this method returns
    	// (all optional branches will try to spawn, then if none have spawned any leftover required branches will try to spawn)
    	// and won't try to spawn anything in the second phase of this branch spawning cycle
    	if(!spawningRequiredBranchesOnly)// || isRollBack)
    	{
    		branchDataItem.DoneSpawning = true;
    	} else {
    		// If we are spawning required branches then there might also
    		// be optional branches, which will not have had a chance to spawn when this method returns
    		// The second (optional branches) phase of this branch spawning cycle will call AddBranches on the branch for the
    		// second time to try to spawn them and will set DoneSpawning to true.
			boolean hasOnlyRequiredBranches = true;
			for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false))
			{
				if(!branchDataItem3.Branch.isRequiredBranch && !branchDataItem3.DoneSpawning && !branchDataItem3.CannotSpawn)
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
				branchDataItem.DoneSpawning = true;
			}
    	}

    	if(!branchDataItem.CannotSpawn)
    	{
	        for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false))
	        {
	        	if(!AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber) && !childBranchDataItem.SpawnDelayed)
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
        			boolean branchFrequencyGroupNotPassed = false;

        			BO3 bo3 = ((BO3)childBranchDataItem.Branch.getObject());

        			if(bo3 == null || bo3.isInvalidConfig)
        			{
		        		childBranchDataItem.DoneSpawning = true;
		        		childBranchDataItem.CannotSpawn = true;
		        		if(bo3 == null)
		        		{
		        			if(OTG.getPluginConfig().SpawnLog)
		        			{
		        				OTG.log(LogMarker.WARN, "Error: Could not find BO3 file: " + childBranchDataItem.Branch.BO3Name + ".BO3 which is a branch of " + branchDataItem.Branch.BO3Name + ".BO3");
		        			}
		        		}
        			}

	        		if(childBranchDataItem.DoneSpawning || childBranchDataItem.CannotSpawn)
	        		{
	        			continue;
	        		}

	        		// Before spawning any required branch make sure there are no optional branches in its branch group that haven't tried to spawn yet.
    	        	if(spawningRequiredBranchesOnly)// && !isRollBack)
    	        	{
    	    			if(childBranchDataItem.Branch.isRequiredBranch)
    	    			{
    		    			boolean hasOnlyRequiredBranches = true;
    		    			if(childBranchDataItem.Branch.branchGroup != null && childBranchDataItem.Branch.branchGroup.length() > 0)
    		    			{
	    		    			for(BranchDataItem branchDataItem3 : branchDataItem.getChildren(false))
	    		    			{
	    		    				if(
    		    						!branchDataItem3.Branch.isRequiredBranch &&
    		    						branchDataItem3.Branch.branchGroup != null &&
    		    						branchDataItem3.Branch.branchGroup.length() > 0 &&
    		    						childBranchDataItem.Branch.branchGroup.equals(branchDataItem3.Branch.branchGroup) &&
    		    						!branchDataItem3.wasDeleted &&
	    								!branchDataItem3.CannotSpawn &&
		    							!branchDataItem3.DoneSpawning
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

	        		if(canSpawn && (childBranchDataItem.MaxDepth == 0 || childBranchDataItem.CurrentDepth > childBranchDataItem.MaxDepth) && !childBranchDataItem.Branch.isRequiredBranch)
	        		{
	        			canSpawn = false;
	        		}

	        		branchesTried += 1;

	        		// Ignore weightedbranches when measuring
	        		if(minimumSize && childBranchDataItem.Branch.isWeightedBranch)
	        		{
	        			childBranchDataItem.DoneSpawning = true;
        				childBranchDataItem.CannotSpawn = true;
        				continue;
	        		}

	        		int smoothRadius = ((BO3)Start.getObject()).getSettings().overrideChildSettings && bo3.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothRadius : bo3.getSettings().smoothRadius;
	        		if(smoothRadius == -1 || bo3.getSettings().smoothRadius == -1)
	        		{
	        			smoothRadius = 0;
	        		}

	        		ChunkCoordinate worldBorderCenterPoint = World.GetWorldSession().getWorldBorderCenterPoint();

	        		if(
        				canSpawn &&
        				!minimumSize &&
        				World.GetWorldSession().getWorldBorderRadius() > 0 &&
        				(
    						(
								smoothRadius == 0 &&
								!World.IsInsideWorldBorder(ChunkCoordinate.fromChunkCoords(childBranchDataItem.Branch.getChunkX(), childBranchDataItem.Branch.getChunkZ()), true)
							)
    						||
    						(
								smoothRadius > 0 &&
								(
									childBranchDataItem.Branch.getChunkX() - Math.ceil(smoothRadius / (double)16) < worldBorderCenterPoint.getChunkX() - (World.GetWorldSession().getWorldBorderRadius() - 1) ||
									childBranchDataItem.Branch.getChunkX() + Math.ceil(smoothRadius / (double)16) > worldBorderCenterPoint.getChunkX() + (World.GetWorldSession().getWorldBorderRadius() - 1) - 1 || // Resources are spawned at an offset of + half a chunk so stop 1 chunk short of the border
									childBranchDataItem.Branch.getChunkZ() - Math.ceil(smoothRadius / (double)16) < worldBorderCenterPoint.getChunkZ() - (World.GetWorldSession().getWorldBorderRadius() - 1) ||
									childBranchDataItem.Branch.getChunkZ() + Math.ceil(smoothRadius / (double)16) > worldBorderCenterPoint.getChunkZ() + (World.GetWorldSession().getWorldBorderRadius() - 1) - 1 // Resources are spawned at an offset of + half a chunk so stop 1 chunk short of the border
								)
							)
						)
    				)
	        		{
	        			canSpawn = false;
	        			isInsideWorldBorder = false;
	        		}

        			if(!DoStartChunkBlockChecks())
        			{
        				canSpawn = false;
        				startChunkBlockChecksPassed = false;
        			} else {
		        	    if(childBranchDataItem.Branch.getY() < 0 && !minimumSize)
		        	    {
		    		    	canSpawn = false;
		        	    }
        			}

	        		Stack<BranchDataItem> collidingObjects = null;
	        		if(canSpawn)
	        		{
		        		if(bo3.getSettings().SpawnOnWaterOnly && !minimumSize)
		    			{
		    				if(
		    					!(
		    						World.getMaterial(childBranchDataItem.ChunkCoordinate.getBlockX(), World.getHighestBlockYAt(childBranchDataItem.ChunkCoordinate.getBlockX(), childBranchDataItem.ChunkCoordinate.getBlockZ(), true, true, false, true), childBranchDataItem.ChunkCoordinate.getBlockZ(), IsOTGPlus).isLiquid() &&
		    						World.getMaterial(childBranchDataItem.ChunkCoordinate.getBlockX(), World.getHighestBlockYAt(childBranchDataItem.ChunkCoordinate.getBlockX(), childBranchDataItem.ChunkCoordinate.getBlockZ() + 15, true, true, false, true), childBranchDataItem.ChunkCoordinate.getBlockZ() + 15, IsOTGPlus).isLiquid() &&
		    						World.getMaterial(childBranchDataItem.ChunkCoordinate.getBlockX() + 15, World.getHighestBlockYAt(childBranchDataItem.ChunkCoordinate.getBlockX() + 15, childBranchDataItem.ChunkCoordinate.getBlockZ(), true, true, false, true), childBranchDataItem.ChunkCoordinate.getBlockZ(), IsOTGPlus).isLiquid() &&
		    						World.getMaterial(childBranchDataItem.ChunkCoordinate.getBlockX() + 15, World.getHighestBlockYAt(childBranchDataItem.ChunkCoordinate.getBlockX() + 15, childBranchDataItem.ChunkCoordinate.getBlockZ() + 15, true, true, false, true), childBranchDataItem.ChunkCoordinate.getBlockZ() + 15, IsOTGPlus).isLiquid()
		    					)
		    				)
		    				{
		    					wasntOnWater = true;
		    					canSpawn = false;
		    				}
		    			}
		        		if(!bo3.getSettings().CanSpawnOnWater && !minimumSize)
		    			{
		    				if(
	    						(World.getMaterial(childBranchDataItem.ChunkCoordinate.getBlockX() + 8, World.getHighestBlockYAt(childBranchDataItem.ChunkCoordinate.getBlockX() + 8, childBranchDataItem.ChunkCoordinate.getBlockZ() + 7, true, true, false, true), childBranchDataItem.ChunkCoordinate.getBlockZ() + 7, IsOTGPlus).isLiquid())
		    				)
		    				{
		    					wasOnWater = true;
		    					canSpawn = false;
		    				}
		    			}

	        			if(canSpawn && bo3.getSettings().mustBeBelowOther)
	        			{
	        				// Check for mustBeBelowOther
	        				boolean bFound = false;
	        				if(AllBranchesBranchDataByChunk.containsKey(childBranchDataItem.ChunkCoordinate))
	        				{
		        				for(BranchDataItem branchDataItem2 : AllBranchesBranchDataByChunk.get(childBranchDataItem.ChunkCoordinate))
		        				{
		        					if(
	        							branchDataItem2.ChunkCoordinate.equals(childBranchDataItem.ChunkCoordinate) &&
	        							!((BO3) branchDataItem2.Branch.getObject()).getSettings().canOverride &&
        								branchDataItem2.Branch.getY() >= childBranchDataItem.Branch.getY()
									)
		        					{
		        						bFound = true;
		        						break;
		        					}
		        				}
	        				}
	        				if(!bFound)
	        				{
	        					wasntBelowOther = true;
	        					canSpawn = false;
	        				}
	        			}

	        			if(canSpawn && bo3.getSettings().mustBeInside != null && bo3.getSettings().mustBeInside.length() > 0)
	        			{
	        				// Check for mustBeInside
	        				String[] mustBeInsideBO3s = bo3.getSettings().mustBeInside.split(",");
        					boolean foundSpwanRequirement = false;
		    				for(String mustBeInsideBO3Group : mustBeInsideBO3s)
		    				{
		    					String[] mustBeInsideBO3sByGroup = mustBeInsideBO3Group.trim().split(" ");
		    					boolean foundAllSpwanRequirementParts = true;
		    					for(String mustBeInsideBO3 : mustBeInsideBO3sByGroup)
		    					{
		    						String[] mustBeInsideBO3NameAndRotation = mustBeInsideBO3.split(":");
		    						String mustBeInsideBO3Name = mustBeInsideBO3NameAndRotation[0];
		    						String mustBeInsideBO3Rotation = mustBeInsideBO3NameAndRotation.length > 1 ? mustBeInsideBO3NameAndRotation[1] : null;
				    	    		boolean bFoundPart = false;
				    	    		if(AllBranchesBranchDataByChunk.containsKey(childBranchDataItem.ChunkCoordinate))
				    	    		{
				    	    			for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(childBranchDataItem.ChunkCoordinate))
										{
				   							if(branchDataItem3 != childBranchDataItem && branchDataItem3 != childBranchDataItem.Parent)
				   							{
				   								for(String branchName : ((BO3)branchDataItem3.Branch.getObject()).getSettings().getInheritedBO3s())
				   								{
				   									if(branchName.equals(mustBeInsideBO3Name.trim()))
				   									{
				   										int rotation = (branchDataItem3.Branch.getRotation().getRotationId() - childBranchDataItem.Branch.getRotation().getRotationId());
				   										if(rotation < 0)
				   										{
				   											rotation += 4; // TODO: What is this? <- Always keeping rotation positive?
				   										}

				   										if(mustBeInsideBO3Rotation == null || rotation == Rotation.FromString(mustBeInsideBO3Rotation).getRotationId())
				   										{
						   	   	    						if(CheckCollision(childBranchDataItem.Branch, branchDataItem3.Branch))
						   	   	    						{
						   	   	    							bFoundPart = true;
						   	   	    							break;
						   	   	    						}
				   										}
				   									}
				   								}
				    	   						if(bFoundPart)
				    	   						{
				    	   							break;
				    	   						}
				   							}
										}
				    	    		}
		   							if(!bFoundPart)
		   							{
		   								foundAllSpwanRequirementParts = false;
		   								break;
		   							}
		    					}
		    					if(foundAllSpwanRequirementParts)
		    					{
		    						foundSpwanRequirement = true;
		    						break;
		    					}
		    				}
    	    				if(!foundSpwanRequirement)
    	    				{
	        					wasntInsideOther = true;
	        					canSpawn = false;
    	    				}
	        			}

	        			if(canSpawn && bo3.getSettings().cannotBeInside != null && bo3.getSettings().cannotBeInside.length() > 0)
	        			{
	        				// Check for cannotSpawnInside
	        				String[] mustBeInsideBO3s = bo3.getSettings().cannotBeInside.split(",");
        					boolean foundSpwanBlocker = false;
    	    				for(String mustBeInsideBO3 : mustBeInsideBO3s) // Check if the branch can remain spawned without the branch we're rolling back
    	    				{
	    						String[] mustBeInsideBO3NameAndRotation = mustBeInsideBO3.split(":");
	    						String mustBeInsideBO3Name = mustBeInsideBO3NameAndRotation[0];
	    						String mustBeInsideBO3Rotation = mustBeInsideBO3NameAndRotation.length > 1 ? mustBeInsideBO3NameAndRotation[1] : null;
	    						if(AllBranchesBranchDataByChunk.containsKey(childBranchDataItem.ChunkCoordinate))
	    						{
			    	    			for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(childBranchDataItem.ChunkCoordinate))
									{
			   							if(branchDataItem3 != childBranchDataItem && branchDataItem3 != childBranchDataItem.Parent)
			   							{
			   								for(String branchName : ((BO3)branchDataItem3.Branch.getObject()).getSettings().getInheritedBO3s())
			   								{
			   									if(branchName.equals(mustBeInsideBO3Name.trim()))
			   									{
			   										int rotation = (branchDataItem3.Branch.getRotation().getRotationId() - childBranchDataItem.Branch.getRotation().getRotationId());
			   										if(rotation < 0)
			   										{
			   											rotation += 4;
			   										}

			   										if(mustBeInsideBO3Rotation == null || rotation == Rotation.FromString(mustBeInsideBO3Rotation).getRotationId())
			   										{
					   	   	    						if(CheckCollision(childBranchDataItem.Branch, branchDataItem3.Branch))
					   	   	    						{
				    	   	     	        				if(OTG.getPluginConfig().SpawnLog)
				    	   	    	        				{
				    	   	     	        					OTG.log(LogMarker.INFO, "CannotBeInside branch " + childBranchDataItem.Branch.BO3Name + " was blocked by " + branchDataItem3.Branch.BO3Name);
				    	   	    	        				}

			    	   	   	    							foundSpwanBlocker = true;
			    	   	   	    							break;
					   	   	    						}
			   										}
			   									}
			   								}
			    	   						if(foundSpwanBlocker)
			    	   						{
			    	   							break;
			    	   						}
			   							}
									}
	    	   						if(foundSpwanBlocker)
	    	   						{
	    	   							break;
	    	   						}
	    						}
    	    				}
    	    				if(foundSpwanBlocker)
    	    				{
    	    					cannotSpawnInsideOther = true;
	        					canSpawn = false;
    	    				}
	        			}

	        		    if(canSpawn && (bo3.getSettings().branchFrequency > 0 || (bo3.getSettings().branchFrequencyGroup != null && bo3.getSettings().branchFrequencyGroup.length() > 0)))
	        		    {
	        	    		int radius = bo3.getSettings().branchFrequency;

	        	            // Check if no other structure of the same type (filename) is within the minimum radius (BO3 frequency)
	        	    		// Check if no other structures that are a member of the same group as this BO3 are within the minimum radius (BO3Group frequency)
	        	            String[] groupStrings = bo3.getSettings().branchFrequencyGroup.trim().length() > 0 ? bo3.getSettings().branchFrequencyGroup.split(",") : null;
	        	            ArrayList<String> groupNames = new ArrayList<String>();
	        	            ArrayList<Integer> groupFrequencies = new ArrayList<Integer>();
	        	            int largestBranchFrequency = radius;
	        	            if(groupStrings != null && groupStrings.length > 0)
	        	            {
	        	            	for(int i = 0; i < groupStrings.length; i++)
	        	            	{
	        	                	String[] groupString = groupStrings[i].trim().length() > 0 ? groupStrings[i].split(":") : null;
	        	                	if(groupString != null && groupString.length == 2)
	        	                	{
	        	                		groupNames.add(groupString[0].trim());
	        	                		int groupFrequency = Integer.parseInt(groupString[1].trim());
	        	                		groupFrequencies.add(groupFrequency);
	        	                		if(groupFrequency > largestBranchFrequency)
	        	                		{
	        	                			largestBranchFrequency = groupFrequency;
	        	                		}
	        	                	}
	        	            	}
	        	            }
        	            	// Check branch frequency
        	    			boolean bFound = false;
        	    	    	for(int x = -largestBranchFrequency; x <= largestBranchFrequency; x++)
        	    	    	{
        	    	    		for(int z = -largestBranchFrequency; z <= largestBranchFrequency; z++)
        	    	    		{
        	    	    			ChunkCoordinate targetChunk = ChunkCoordinate.fromChunkCoords(childBranchDataItem.Branch.getChunkX() + x, childBranchDataItem.Branch.getChunkZ() + z);

        	    	    			Stack<BranchDataItem> branches = AllBranchesBranchDataByChunk.get(targetChunk);
        		    	    		if(branches != null)
        		    	    		{
        		    	    			for(BranchDataItem a : branches)
        		    	    			{
        		    	    				float distanceBetweenStructures = (int)Math.floor(Math.sqrt(Math.pow(childBranchDataItem.Branch.getChunkX() - targetChunk.getChunkX(), 2) + Math.pow(childBranchDataItem.Branch.getChunkZ() - targetChunk.getChunkZ(), 2)));
        		    	    				if(a.Branch.BO3Name.equals(childBranchDataItem.Branch.BO3Name))
        		    	    				{
        		    	    					if(a.Branch == childBranchDataItem.Branch)
        		    	    					{
        		    	    						throw new RuntimeException();
        		    	    					}

        		    	    					//OTG.log(LogMarker.INFO, "A: " + a.Branch.BO3Name + " B: " + childBranchDataItem.Branch.BO3Name);
        		    	                        // Find distance between two points
        		    	                        if (distanceBetweenStructures <= radius)
        		    	                        {
        		    	                        	// Other branch of the same type is too nearby, cannot spawn here!
        			    					    	bFound = true;
        			    		        			branchFrequencyNotPassed = true;
        			    		        			break;
        		    	                        }
        		    	    				}
    		    	        	            if(groupStrings != null && groupStrings.length > 0)
    		    	        	            {
    			    	    					BO3 targetBO3 = ((BO3)a.Branch.getObject());
    			    	        	            String[] targetGroupStrings = targetBO3.getSettings().branchFrequencyGroup.trim().length() > 0 ? targetBO3.getSettings().branchFrequencyGroup.split(",") : null;
    			    	        	            ArrayList<String> targetGroupNames = new ArrayList<String>();
    			    	        	            ArrayList<Integer> targetGroupFrequencies = new ArrayList<Integer>();

    			    	        	            if(targetGroupStrings != null && targetGroupStrings.length > 0)
    			    	        	            {
    		    	            	            	for(int t = 0; t < targetGroupStrings.length; t++)
    		    	            	            	{
    		    	            	                	String[] groupString = targetGroupStrings[t].trim().length() > 0 ? targetGroupStrings[t].split(":") : null;
    		    	            	                	if(groupString != null && groupString.length == 2)
    		    	            	                	{
    		    	            	                		targetGroupNames.add(groupString[0].trim());
    		    	            	                		int groupFrequency = Integer.parseInt(groupString[1].trim());
    		    	            	                		targetGroupFrequencies.add(groupFrequency);
    		    	            	                	}
    		    	            	            	}

    			    	        	            	for(int i = 0; i < groupNames.size(); i++)
    			    	        	            	{
    			    	        	            		for(int t = 0; t < targetGroupNames.size(); t++)
    			    	        	            		{
    			    	        	            			if(groupNames.get(i).equals(targetGroupNames.get(t)))
    			    	        	            			{
    			    	        	            				if(distanceBetweenStructures <= groupFrequencies.get(i))
    			    	        	            				{
    					    		    	    					// Branch with same branchFrequencyGroup was closer than branchFrequencyGroup's frequency in chunks, don't spawn
    			    	    		    					    	bFound = true;
    			        			    		        			branchFrequencyGroupNotPassed = true;
    			    	    		    					    	break;
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
    	        	            			if(bFound)
    	        	            			{
    	        	            				break;
    	        	            			}
        		    	    			}
        		    	    		}
        	            			if(bFound)
        	            			{
        	            				break;
        	            			}
        	    	    		}
    	            			if(bFound)
    	            			{
    	            				break;
    	            			}
        	    	    	}
	            			if(bFound)
	            			{
	            				canSpawn = false;
	            			}
	        		    }

	        			if(canSpawn)
	        			{
	        				// Returns collidingObject == null if if the branch cannot spawn in the given biome or if the given chunk is occupied by another structure
	    					collidingObjects = CheckSpawnRequirementsAndCollisions(childBranchDataItem, minimumSize);
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
        									branchDataItem.Parent == null ||
        									collidingObject.Branch != branchDataItem.Parent.Branch
    									) &&
    									!((BO3) collidingObject.Branch.getObject()).getSettings().canOverride
									)
		        					{
		        						boolean siblingFound = false;
		        						if(branchDataItem.Parent != null)
		        						{
			        						for(BranchDataItem parentSibling : branchDataItem.Parent.getChildren(false))
			        						{
			        							if(collidingObject.Branch == parentSibling.Branch)
			        							{
				        							siblingFound = true;
				        							break;
			        							}
			        						}
		        						}
		        						if(!siblingFound)
		        						{
			        						for(BranchDataItem sibling : branchDataItem.getChildren(false))
			        						{
			        							if(collidingObject.Branch == sibling.Branch)
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
		        		if(OTG.getPluginConfig().SpawnLog)
		        		{

			        		String allParentsString = "";
			        		BranchDataItem tempBranch = childBranchDataItem;
			        		while(tempBranch.Parent != null)
			        		{
			        			allParentsString += " <-- X" + tempBranch.Parent.Branch.getChunkX() + " Z" + tempBranch.Parent.Branch.getChunkZ() + " Y" + tempBranch.Parent.Branch.getY() + " " + tempBranch.Parent.Branch.BO3Name + ":" + tempBranch.Parent.Branch.getRotation();
			        			tempBranch = tempBranch.Parent;
			        		}

			        		OTG.log(LogMarker.INFO, "Plotted X" + childBranchDataItem.ChunkCoordinate.getChunkX() + " Z" + childBranchDataItem.ChunkCoordinate.getChunkZ() + (minimumSize ? "" : " Y" + (childBranchDataItem.Branch.getY())) + " " +  childBranchDataItem.Branch.BO3Name + ":" + childBranchDataItem.Branch.getRotation() + (childBranchDataItem.Branch.isRequiredBranch ? " required" : " optional") + " cycle " + Cycle + allParentsString);
		        		}

	        	    	if(childBranchDataItem.getChildren(false).size() == 0)
	        	    	{
	        	    		childBranchDataItem.DoneSpawning = true;
	        	    	}

	        	    	// Mark any required branches in the same branch group so they wont try to spawn
		        		for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false))
		        		{
		        			if(
	        					childBranchDataItem2 != childBranchDataItem &&
	    						(childBranchDataItem.Branch.branchGroup != null && childBranchDataItem.Branch.branchGroup.length() >= 0) &&
	        					childBranchDataItem.Branch.branchGroup.equals(childBranchDataItem2.Branch.branchGroup) &&
    							childBranchDataItem2.Branch.isRequiredBranch
        					)
		        			{
		        				childBranchDataItem2.DoneSpawning = true;
		        				childBranchDataItem2.CannotSpawn = true;
        					}
		        		}

		        		SpawnedBranchThisCycle = true;

		        		AllBranchesBranchData.add(childBranchDataItem);
		        		AllBranchesBranchDataHash.add(childBranchDataItem.branchNumber);
		        		Stack<BranchDataItem> branchDataItemStack = AllBranchesBranchDataByChunk.get(childBranchDataItem.ChunkCoordinate);
		        		if(branchDataItemStack != null)
		        		{
		        			branchDataItemStack.add(childBranchDataItem);
		        		} else {
		        			branchDataItemStack = new Stack<BranchDataItem>();
		        			branchDataItemStack.add(childBranchDataItem);
		        			AllBranchesBranchDataByChunk.put(childBranchDataItem.ChunkCoordinate, branchDataItemStack);
		        		}

		        		// If an optional branch spawns then immediately spawn its required branches as well (if any)
		        		// If this causes a rollback the rollback will stopped at this branch and we can resume spawning
		        		// the current branch's children as if it was unable to spawn.
		        		if(
	        				!SpawningRequiredChildrenForOptionalBranch &&
	        				!childBranchDataItem.Branch.isRequiredBranch
        				)
		        		{
		        			if(OTG.getPluginConfig().SpawnLog)
		        			{
		        				OTG.log(LogMarker.INFO, "Plotting all required child branches that are not in a branch group with optional branches.");
		        			}

		        			SpawningRequiredChildrenForOptionalBranch = true;
        					currentSpawningRequiredChildrenForOptionalBranch = childBranchDataItem;
			        		TraverseAndSpawnChildBranches(childBranchDataItem, minimumSize, true);
			        		SpawningRequiredChildrenForOptionalBranch = false;

			        		// Make sure the branch wasn't rolled back because the required branches couldn't spawn.
			        		boolean bFound = false;
			        		branchDataItemStack = AllBranchesBranchDataByChunk.get(childBranchDataItem.ChunkCoordinate);
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

		        			if(OTG.getPluginConfig().SpawnLog)
		        			{
		        				OTG.log(LogMarker.INFO, "Done spawning required children for optional branch X" + childBranchDataItem.ChunkCoordinate.getChunkX() + " Z" + childBranchDataItem.ChunkCoordinate.getChunkZ() + (minimumSize ? "" : " Y" + (childBranchDataItem.Branch.getY())) + " " +  childBranchDataItem.Branch.BO3Name + ":" + childBranchDataItem.Branch.getRotation());
		        			}
		        		}
		        		// If AddBranches was called during a rollback then only traverse branches for children that spawn during this call
		        		// Otherwise existing branches could have their children spawn more than once per cycle
		        		else if(
	        				traverseOnlySpawnedChildren &&
	        				!SpawningRequiredChildrenForOptionalBranch &&
	        				childBranchDataItem.Branch.isRequiredBranch
        				)
		        		{
			        		TraverseAndSpawnChildBranches(childBranchDataItem, minimumSize, true);
		        		}
		        	}

		        	if(!canSpawn)
		        	{
		        		if(!childBranchDataItem.DoneSpawning && !childBranchDataItem.CannotSpawn)
		        		{
		        			// WasntBelowOther branches that cannot spawn get to retry
		        			// each cycle unless no branch spawned last cycle
		        			// TODO: Won't this cause problems?
		        			if(!wasntBelowOther || !SpawnedBranchLastCycle)
		        			{
				        		childBranchDataItem.DoneSpawning = true;
				        		childBranchDataItem.CannotSpawn = true;
		        			} else {
		        				branchDataItem.DoneSpawning = false;
		        				if(branchDataItem.wasDeleted)
		        				{
		        					throw new RuntimeException();
		        				}
		        			}

			        		boolean bBreak = false;

			        		boolean branchGroupFailedSpawning = false;
			        		if(childBranchDataItem.Branch.isRequiredBranch)
			        		{
			        			branchGroupFailedSpawning = true;

			        	    	// Check if there are any more required branches in this group that haven't tried to spawn yet.
				        		for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false))
				        		{
				        			if(
			        					childBranchDataItem2 != childBranchDataItem &&
			    						(childBranchDataItem.Branch.branchGroup != null && childBranchDataItem.Branch.branchGroup.length() >= 0) &&
			        					childBranchDataItem.Branch.branchGroup.equals(childBranchDataItem2.Branch.branchGroup) &&
		    							childBranchDataItem2.Branch.isRequiredBranch &&
				        				!childBranchDataItem2.DoneSpawning &&
				        				!childBranchDataItem2.CannotSpawn
		        					)
				        			{
				        				branchGroupFailedSpawning = false;
				        				break;
		        					}
				        		}
			        		}

			        		if(!collidedWithParentOrSibling && (!wasntBelowOther || !SpawnedBranchLastCycle) && branchGroupFailedSpawning)
			        		{
			            		// Branch could not spawn
			            		// abort this branch because it has a branch group that could not be spawned

			            		if(OTG.getPluginConfig().SpawnLog)
			            		{
			        	    		String allParentsString = "";
			        	    		BranchDataItem tempBranch = branchDataItem;
			        	    		while(tempBranch.Parent != null)
			        	    		{
			        	    			allParentsString += " <-- X" + tempBranch.Parent.Branch.getChunkX() + " Z" + tempBranch.Parent.Branch.getChunkZ() + " Y" + tempBranch.Parent.Branch.getY() + " " + tempBranch.Parent.Branch.BO3Name + ":" + tempBranch.Parent.Branch.getRotation();
			        	    			tempBranch = tempBranch.Parent;
			        	    		}

			        	    		String occupiedByObjectsString = "";
			        	    		if(spaceIsOccupied)
			        	    		{
			        	    			for(BranchDataItem collidingObject : collidingObjects)
			        	    			{
			        	    				String occupiedByObjectString = collidingObject.Branch.BO3Name + ":" + collidingObject.Branch.getRotation() + " X" + collidingObject.Branch.getChunkX() + " Z" + collidingObject.Branch.getChunkZ() + " Y" + collidingObject.Branch.getY();
					        	    		tempBranch = collidingObject;
					        	    		while(tempBranch.Parent != null)
					        	    		{
					        	    			occupiedByObjectString += " <-- X" + tempBranch.Parent.Branch.getChunkX() + " Z" + tempBranch.Parent.Branch.getChunkZ() + " Y" + tempBranch.Parent.Branch.getY() + " " + tempBranch.Parent.Branch.BO3Name + ":" + tempBranch.Parent.Branch.getRotation();
					        	    			tempBranch = tempBranch.Parent;
					        	    		}
					        	    		occupiedByObjectsString += " " + occupiedByObjectString;
			        	    			}
			        	    		}

			        	    		String reason = (branchFrequencyGroupNotPassed ? "BranchFrequencyGroupNotPassed " : "") + (branchFrequencyNotPassed ? "BranchFrequencyNotPassed " : "") + (!isInsideWorldBorder ? "IsOutsideWorldBorder " : "") + (!startChunkBlockChecksPassed ? "StartChunkBlockChecksNotPassed " : "") + (collidedWithParentOrSibling ? "CollidedWithParentOrSibling " : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (wasntInsideOther ? "WasntInsideOther " : "") + (cannotSpawnInsideOther ? "CannotSpawnInsideOther " : "") + (wasntOnWater ? "WasntOnWater " : "") + (wasOnWater ? "WasOnWater " : "") + (!branchFrequencyGroupNotPassed && !branchFrequencyNotPassed && isInsideWorldBorder && startChunkBlockChecksPassed && !wasntBelowOther && !cannotSpawnInsideOther && !wasntOnWater && !wasOnWater && !wasntBelowOther && !chunkIsIneligible && spaceIsOccupied ? "SpaceIsOccupied by" + occupiedByObjectsString : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (chunkIsIneligible ? "TerrainIsUnsuitable (StartChunkBlockChecks (height or material) not passed or Y < 0 or Frequency/BO3Group checks not passed or BO3 collided with other CustomStructure or smoothing area collided with other CustomStructure or BO3 not in allowed Biome or Smoothing area not in allowed Biome)" : "");
			        	    		OTG.log(LogMarker.INFO, "Rolling back X" + branchDataItem.Branch.getChunkX() + " Z" + branchDataItem.Branch.getChunkZ() + " Y" + branchDataItem.Branch.getY() + " " + branchDataItem.Branch.BO3Name + ":" + branchDataItem.Branch.getRotation() + allParentsString + " because required branch "+ childBranchDataItem.Branch.BO3Name + " couldn't spawn. Reason: " + reason);
			            		}

		            			RollBackBranch(branchDataItem, minimumSize, spawningRequiredBranchesOnly);
		            			bBreak = true;
			        		} else {
				        		// if this child branch could not spawn then in some cases other child branches won't be able to either
				        		// mark those child branches so they dont try to spawn and roll back the whole branch if a required branch can't spawn
				        		for(BranchDataItem childBranchDataItem2 : branchDataItem.getChildren(false))
				        		{
				        			if(!wasntBelowOther || !SpawnedBranchLastCycle)
				        			{
					        			if(
				        					childBranchDataItem == childBranchDataItem2 ||
					        				(
				        						!(childBranchDataItem2.CannotSpawn || childBranchDataItem2.DoneSpawning) &&
				        						(
			        								(
		        										childBranchDataItem.Branch.getY() < 0 ||
		        										chunkIsIneligible ||
		        										(wasntBelowOther && ((BO3)childBranchDataItem2.Branch.getObject()).getSettings().mustBeBelowOther) ||
		        										(wasntOnWater && ((BO3)childBranchDataItem2.Branch.getObject()).getSettings().SpawnOnWaterOnly) ||
		        										(wasOnWater && !((BO3)childBranchDataItem2.Branch.getObject()).getSettings().CanSpawnOnWater)
			        								) &&
			        								childBranchDataItem.Branch.getX() == childBranchDataItem2.Branch.getX() &&
			        								childBranchDataItem.Branch.getY() == childBranchDataItem2.Branch.getY() &&
			        								childBranchDataItem.Branch.getZ() == childBranchDataItem2.Branch.getZ()
					        					)
				        					)
			        					)
					        			{
					        				childBranchDataItem2.DoneSpawning = true;
					        				childBranchDataItem2.CannotSpawn = true;

							        		branchGroupFailedSpawning = false;
							        		if(childBranchDataItem2.Branch.isRequiredBranch)
							        		{
							        			branchGroupFailedSpawning = true;

							        	    	// Check if there are any more required branches in this group that haven't tried to spawn yet.
								        		for(BranchDataItem childBranchDataItem3 : branchDataItem.getChildren(false))
								        		{
								        			if(
							        					childBranchDataItem3 != childBranchDataItem2 &&
							    						(childBranchDataItem2.Branch.branchGroup != null && childBranchDataItem2.Branch.branchGroup.length() >= 0) &&
							    						childBranchDataItem2.Branch.branchGroup.equals(childBranchDataItem3.Branch.branchGroup) &&
							    						childBranchDataItem3.Branch.isRequiredBranch &&
								        				!childBranchDataItem3.DoneSpawning &&
								        				!childBranchDataItem3.CannotSpawn
						        					)
								        			{
								        				branchGroupFailedSpawning = false;
								        				break;
						        					}
								        		}
							        		}

					        				if(branchGroupFailedSpawning && !collidedWithParentOrSibling)
					        				{
							            		if(OTG.getPluginConfig().SpawnLog)
							            		{
							        	    		String allParentsString = "";
							        	    		BranchDataItem tempBranch = branchDataItem;
							        	    		while(tempBranch.Parent != null)
							        	    		{
							        	    			allParentsString += " <-- X" + tempBranch.Parent.Branch.getChunkX() + " Z" + tempBranch.Parent.Branch.getChunkZ() + " Y" + tempBranch.Parent.Branch.getY() + " " + tempBranch.Parent.Branch.BO3Name + ":" + tempBranch.Parent.Branch.getRotation();
							        	    			tempBranch = tempBranch.Parent;
							        	    		}

							        	    		String occupiedByObjectsString = "";
							        	    		if(spaceIsOccupied)
							        	    		{
							        	    			for(BranchDataItem collidingObject : collidingObjects)
							        	    			{
							        	    				String occupiedByObjectString = collidingObject.Branch.BO3Name + ":" + collidingObject.Branch.getRotation() + " X" + collidingObject.Branch.getChunkX() + " Z" + collidingObject.Branch.getChunkZ() + " Y" + collidingObject.Branch.getY();
									        	    		tempBranch = collidingObject;
									        	    		while(tempBranch.Parent != null)
									        	    		{
									        	    			occupiedByObjectString += " <-- X" + tempBranch.Parent.Branch.getChunkX() + " Z" + tempBranch.Parent.Branch.getChunkZ()+ " Y" + tempBranch.Parent.Branch.getY() + " " + tempBranch.Parent.Branch.BO3Name + ":" + tempBranch.Parent.Branch.getRotation();
									        	    			tempBranch = tempBranch.Parent;
									        	    		}
									        	    		occupiedByObjectsString += " " + occupiedByObjectString;
							        	    			}
							        	    		}

							        	    		String reason =
							        	    				(branchFrequencyGroupNotPassed ? "BranchFrequencyGroupNotPassed " : "") +
							        	    				(branchFrequencyNotPassed ? "BranchFrequencyNotPassed " : "") +
							        	    				(!isInsideWorldBorder ? "IsOutsideWorldBorder " : "") +
							        	    				(!startChunkBlockChecksPassed ? "StartChunkBlockChecksNotPassed " : "") +
							        	    				(collidedWithParentOrSibling ? "CollidedWithParentOrSibling " : "") +
							        	    				(wasntBelowOther ? "WasntBelowOther " : "") +
							        	    				(wasntInsideOther ? "WasntInsideOther " : "") +
							        	    				(cannotSpawnInsideOther ? "CannotSpawnInsideOther " : "") +
							        	    				(wasntOnWater ? "WasntOnWater " : "") +
							        	    				(wasOnWater ? "WasOnWater " : "") +
							        	    				(childBranchDataItem.Branch.getY() < 0 ? " WasBelowY0 " : "") +
							        	    				(!branchFrequencyGroupNotPassed && !branchFrequencyNotPassed && isInsideWorldBorder && startChunkBlockChecksPassed && !wasntBelowOther && !cannotSpawnInsideOther && !wasntOnWater && !wasOnWater && !wasntBelowOther && !chunkIsIneligible && spaceIsOccupied ? "SpaceIsOccupied by" + occupiedByObjectsString : "") + (wasntBelowOther ? "WasntBelowOther " : "") + (chunkIsIneligible ? "ChunkIsIneligible: Either the chunk is occupied by another structure or the BO3/smoothing area is not allowed in the Biome)" : "");
							        	    		OTG.log(LogMarker.INFO, "Rolling back X" + branchDataItem.Branch.getChunkX() + " Z" + branchDataItem.Branch.getChunkZ() + " Y" + branchDataItem.Branch.getY() + " " + branchDataItem.Branch.BO3Name + ":" + branchDataItem.Branch.getRotation() + allParentsString + " because required branch "+ childBranchDataItem.Branch.BO3Name + " couldn't spawn. Reason: " + reason);
							            		}
						            			RollBackBranch(branchDataItem, minimumSize, spawningRequiredBranchesOnly);
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
	        	else if(childBranchDataItem.SpawnDelayed)
	        	{
	        		childBranchDataItem.SpawnDelayed = false;
	        	}
	        }

    		// when spawning optional branches spawn them first then traverse any previously spawned required branches
	        // When calling AddBranches during a rollback to continue spawning a branch group don't traverse already spawned children (otherwise the branch could spawn children more than once per cycle).
	        if(
        		!traverseOnlySpawnedChildren &&
        		!spawningRequiredBranchesOnly &&
        		!branchDataItem.CannotSpawn
    		)
	        {
	        	for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false))
	        	{
	        		if(AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber))
	        		{
						if(
							(
								childBranchDataItem.Branch.isRequiredBranch ||
								(
									SpawningCanOverrideBranches &&
									!((BO3)childBranchDataItem.Branch.getObject()).getSettings().canOverride
								)
							) &&
							!childBranchDataItem.CannotSpawn &&
							(
								!childBranchDataItem.SpawnDelayed ||
								!SpawnedBranchLastCycle
							)
						)
						{
							TraverseAndSpawnChildBranches(childBranchDataItem, minimumSize, spawningRequiredBranchesOnly);
						}
	        		}
	        	}
	        }

	        // When calling AddBranches during a rollback to continue spawning a branch group don't traverse already spawned children (otherwise the branch could spawn children more than once per cycle).
	        if(
        		!traverseOnlySpawnedChildren &&
        		spawningRequiredBranchesOnly &&
        		!branchDataItem.CannotSpawn
    		)
	        {
	        	for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false))
	        	{
	        		if(AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber))
	        		{
						if(childBranchDataItem.Branch.isRequiredBranch)
						{
							TraverseAndSpawnChildBranches(childBranchDataItem, minimumSize, spawningRequiredBranchesOnly);
						}
	        		}
	        	}
	        }
    	}
    }

    private void RollBackBranch(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly)
    {
    	// When spawning an optional branch its required branches are spawned immediately as well (if there are no optional branches in the same branchGroup)
    	// This can cause a rollback if the required branches cannot spawn. Make sure that the parent branch of the optional branch isn't rolled back since it
    	// is currently still being processed and is spawning its optional branches.
    	if(SpawningRequiredChildrenForOptionalBranch && currentSpawningRequiredChildrenForOptionalBranch.Parent == branchData)
    	{
    		return;
    	}

    	// Remove all children of this branch from AllBranchesBranchData
    	// And set this branches' CannotSpawn to true
    	// check if the parent has any required branches that cannot spawn
    	// and roll back until there is a viable branch pattern

    	branchData.CannotSpawn = true;
    	branchData.DoneSpawning = true;

    	branchData.wasDeleted = true;

    	branchData.isBeingRolledBack = true;
    	DeleteBranchChildren(branchData,minimumSize, spawningRequiredBranchesOnly);

    	if(AllBranchesBranchDataHash.contains(branchData.branchNumber))
    	{
    		if(OTG.getPluginConfig().SpawnLog)
    		{
	    		String allParentsString = "";
	    		BranchDataItem tempBranch = branchData;
	    		while(tempBranch.Parent != null)
	    		{
	    			allParentsString += " <-- X" + tempBranch.Parent.Branch.getChunkX() + " Z" + tempBranch.Parent.Branch.getChunkZ() + " Y" + tempBranch.Parent.Branch.getY() + " " + tempBranch.Parent.Branch.BO3Name + ":" + tempBranch.Parent.Branch.getRotation();
	    			tempBranch = tempBranch.Parent;
	    		}
	    		OTG.log(LogMarker.INFO, "Deleted X" + branchData.Branch.getChunkX() + " Z" + branchData.Branch.getChunkZ() + " Y" + branchData.Branch.getY() + " " + branchData.Branch.BO3Name + ":" + branchData.Branch.getRotation()  + (branchData.Branch.isRequiredBranch ? " required" : " optional") + " cycle " + Cycle + allParentsString);
    		}

    		AllBranchesBranchData.remove(branchData);
    		AllBranchesBranchDataHash.remove(branchData.branchNumber);
    		Stack<BranchDataItem> branchDataItemStack = AllBranchesBranchDataByChunk.get(branchData.ChunkCoordinate);
    		if(branchDataItemStack != null)
    		{
    			branchDataItemStack.remove(branchData);
    			if(branchDataItemStack.size() == 0)
    			{
    				AllBranchesBranchDataByChunk.remove(branchData.ChunkCoordinate);
    			}
    		}
    	}

    	if(!((BO3)branchData.Branch.getObject()).getSettings().canOverride)
    	{
	    	// If this branch is allowing lower-lying .mustBeBelowOther branches to spawn then roll those back as well

    		Stack<BranchDataItem> allBranchesBranchData2 = new Stack<BranchDataItem>();
    		Stack<BranchDataItem> branchDataByChunk = AllBranchesBranchDataByChunk.get(branchData.ChunkCoordinate);
    		if(branchDataByChunk != null)
    		{
	    		allBranchesBranchData2.addAll(branchDataByChunk);
	    		for(BranchDataItem branchDataItem2 : allBranchesBranchData2)
	    		{
	    			if(AllBranchesBranchDataHash.contains(branchDataItem2.branchNumber))
	    			{
		    			if(branchDataItem2 != branchData)
		    			{
			    			if(((BO3)branchDataItem2.Branch.getObject()).getSettings().mustBeBelowOther && branchDataItem2.ChunkCoordinate.equals(branchData.ChunkCoordinate))
			    			{
			    				boolean branchAboveFound = false;
			    				for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(branchDataItem2.ChunkCoordinate))
		    					{
			    					if(
		    							branchDataItem3 != branchData &&
		    							!((BO3)branchDataItem3.Branch.getObject()).getSettings().mustBeBelowOther &&
		    							!((BO3)branchDataItem3.Branch.getObject()).getSettings().canOverride &&
		    							branchDataItem3.ChunkCoordinate.equals(branchDataItem2.ChunkCoordinate)
									)
			    					{
			    						if(branchDataItem3.Branch.getY() >= branchDataItem2.Branch.getY())
			    						{
			    							branchAboveFound = true;
			    							break;
			    						}
			    					}
		    					}
			    				if(!branchAboveFound)
			    				{
			    					RollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly);
			    				}
			    			}
		    			}
	    			}
	    		}
    		}
    	}

    	// If this branch is allowing mustBeInside branches to spawn then roll those back as well
		Stack<BranchDataItem> allBranchesBranchData2 = new Stack<BranchDataItem>();
		Stack<BranchDataItem> branchDataByChunk = AllBranchesBranchDataByChunk.get(branchData.ChunkCoordinate);
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
							((BO3)branchDataItem2.Branch.getObject()).getSettings().mustBeInside != null &&
							((BO3)branchDataItem2.Branch.getObject()).getSettings().mustBeInside.length() > 0 &&
							branchDataItem2.ChunkCoordinate.equals(branchData.ChunkCoordinate)
						)
		    			{
		    				String[] mustBeInsideBO3s = ((BO3)branchDataItem2.Branch.getObject()).getSettings().mustBeInside.split(",");
		    				boolean currentBO3Found = false;
		    				for(String mustBeInsideBO3Group : mustBeInsideBO3s)
		    				{
		    					String[] mustBeInsideBO3sByGroup = mustBeInsideBO3Group.trim().split(" ");
		    					for(String mustBeInsideBO3 : mustBeInsideBO3sByGroup)
		    					{
		    						String[] mustBeInsideBO3NameAndRotation = mustBeInsideBO3.split(":");
		    						String mustBeInsideBO3Name = mustBeInsideBO3NameAndRotation[0];
		    						String mustBeInsideBO3Rotation = mustBeInsideBO3NameAndRotation.length > 1 ? mustBeInsideBO3NameAndRotation[1] : null;
	   								for(String branchName : ((BO3)branchData.Branch.getObject()).getSettings().getInheritedBO3s())
	   								{
	   									if(branchName.equals(mustBeInsideBO3Name.trim()))
	   									{
	   										int rotation = (branchData.Branch.getRotation().getRotationId() - branchDataItem2.Branch.getRotation().getRotationId());
	   										if(rotation < 0)
	   										{
	   											rotation += 4;
	   										}

	   										if(mustBeInsideBO3Rotation == null || rotation == Rotation.FromString(mustBeInsideBO3Rotation).getRotationId())
	   										{
		   	   	    							currentBO3Found = true;
		   	   	    							break;
	   										}
	   									}
	   								}
			    					if(currentBO3Found)
			    					{
			    						break;
			    					}
		    					}
		    					if(currentBO3Found)
		    					{
		    						break;
		    					}
		    				}
		    				if(currentBO3Found) // The BO3 that is currently being rolled back may have been allowing this mustBeInside branch to spawn
		    				{
		    					// Check if the branch can remain spawned without the branch we're rolling back
		    					boolean foundSpwanRequirement = false;
			    				for(String mustBeInsideBO3Group : mustBeInsideBO3s)
			    				{
			    					String[] mustBeInsideBO3sByGroup = mustBeInsideBO3Group.trim().split(" ");
			    					boolean foundAllSpwanRequirementParts = true;
			    					for(String mustBeInsideBO3 : mustBeInsideBO3sByGroup)
			    					{
					    	    		boolean bFoundPart = false;
					    	    		for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(branchDataItem2.ChunkCoordinate))
										{
				   							if(branchDataItem3 != branchData && branchDataItem3 != branchDataItem2 && branchDataItem3 != branchDataItem2.Parent)
				   							{
					    						String[] mustBeInsideBO3NameAndRotation = mustBeInsideBO3.split(":");
					    						String mustBeInsideBO3Name = mustBeInsideBO3NameAndRotation[0];
					    						String mustBeInsideBO3Rotation = mustBeInsideBO3NameAndRotation.length > 1 ? mustBeInsideBO3NameAndRotation[1] : null;

				   								for(String branchName : ((BO3)branchDataItem3.Branch.getObject()).getSettings().getInheritedBO3s())
				   								{
				   									if(branchName.equals(mustBeInsideBO3Name.trim()))
				   									{
				   										int rotation = (branchDataItem3.Branch.getRotation().getRotationId() - branchDataItem2.Branch.getRotation().getRotationId());
				   										if(rotation < 0)
				   										{
				   											rotation += 4;
				   										}

				   										if(mustBeInsideBO3Rotation == null || rotation == Rotation.FromString(mustBeInsideBO3Rotation).getRotationId())
				   										{
				   											if(CheckCollision(branchDataItem2.Branch, branchDataItem3.Branch))
				   											{
				   												bFoundPart = true;
						   	   	    							break;
				   											}
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
			   								foundAllSpwanRequirementParts = false;
			   								break;
			   							}
			    					}
			    					if(foundAllSpwanRequirementParts)
			    					{
			    						foundSpwanRequirement = true;
			    					}
			    				}
			    				if(!foundSpwanRequirement)
			    				{
			    					RollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly);
			    				}
		    				}
		    			}
					}
	    		}
	    	}
		}
		// if this branch is a required branch
		// then roll back the parent as well
		if(branchData.Parent != null && !branchData.Parent.isBeingRolledBack)
		{
    		if(branchData.Branch.isRequiredBranch)
    		{
    			//OTG.log(LogMarker.INFO, "RollBackBranch 4: " + branchData.Parent.Branch.BO3Name + " <> " + branchData.Branch.BO3Name);
    			RollBackBranch(branchData.Parent, minimumSize, spawningRequiredBranchesOnly);
    		} else {

    			// Mark for spawning the parent and all other branches in the same branch group that spawn after this branch (unless they have already been spawned successfully)
    			boolean parentDoneSpawning = true;
    			boolean currentBranchFound = false;
        		for (BranchDataItem branchDataItem2 : branchData.Parent.getChildren(false))
        		{
        			if(currentBranchFound)
        			{
        				if(
    						branchData.Branch.branchGroup != null && branchData.Branch.branchGroup.length() >= 0 &&
    						branchData.Branch.branchGroup.equals(branchDataItem2.Branch.branchGroup)
						)
        				{
	            			if(
            					!branchDataItem2.wasDeleted &&
            					!AllBranchesBranchDataHash.contains(branchDataItem2.branchNumber))
	            			{
	        					branchDataItem2.CannotSpawn = false;
	        					branchDataItem2.DoneSpawning = false;
	            			}
        				}
        			}
        			if(branchDataItem2 == branchData)
        			{
        				currentBranchFound = true;
        			}
        			if(!branchDataItem2.DoneSpawning && !branchDataItem2.CannotSpawn)
        			{
        				parentDoneSpawning = false;
        			}
        		}

        		// When rolling back after failing to spawn the required branches for an optional branch that just spawned don't roll back all the way to the optional
        		// branch's parent and continue spawning there. Instead only rollback up to the optional branch, then let the normal spawn cycle continue spawning the parent.
        		if(
    				!parentDoneSpawning &&
    				!(
						SpawningRequiredChildrenForOptionalBranch &&
						currentSpawningRequiredChildrenForOptionalBranch == branchData
					)
				)
    			{
        			branchData.Parent.DoneSpawning = false;

	        		// Rollbacks only happen when:

        			if(!SpawningRequiredChildrenForOptionalBranch)
        			{
        				if(spawningRequiredBranchesOnly)
        				{
                			// 1. The branch being rolled back has spawned all its required-only branch groups but not yet its optional branches and one of the required child branches
                			// (that spawn in the same cycle) failed to spawn one of its required children and is rolled back.
            				// AddBranches should be called for the parent of the branch being rolled back and its parent if a branch group failed to spawn (and so on).

                			// Since we're using SpawningRequiredBranchesOnly AddBranches can traverse all child branches without problems.
            				AddBranches(branchData.Parent, minimumSize, false, spawningRequiredBranchesOnly);
        				} else {
        					// 2. During the second phase of a cycle branch groups with optional branches are spawned, the optional branches get a chance to spawn first, after that the
        					// required branches try to spawn, if that fails the branch is rolled back.
        					// 3. A branch was rolled back that was a requirement for another branch (mustbeinside/mustbebelowother), causing the other branch to be rolled back as well.

                			// Since we're not using SpawningRequiredBranchesOnly AddBranches should only traverse child branches for any branches that it spawns from the branch group its re-trying.
        					// Otherwise some branches may have the same children traversed multiple times in a single phase.
            				AddBranches(branchData.Parent, minimumSize, true, spawningRequiredBranchesOnly);
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

        				SpawningRequiredChildrenForOptionalBranch = false;
            			// Since we're using SpawningRequiredBranchesOnly AddBranches can traverse all child branches without problems.
        				AddBranches(branchData.Parent, minimumSize, false, spawningRequiredBranchesOnly);
        				SpawningRequiredChildrenForOptionalBranch = true;
        			}
    			}
    		}
		}

    	branchData.isBeingRolledBack = false;
    }

    private void DeleteBranchChildren(BranchDataItem branchData, boolean minimumSize, boolean spawningRequiredBranchesOnly)
    {
    	// Remove all children of this branch from AllBranchesBranchData
    	Stack<BranchDataItem> children = branchData.getChildren(true);
        for(BranchDataItem branchDataItem : children)
        {
        	branchDataItem.CannotSpawn = true;
        	branchDataItem.DoneSpawning = true;
        	branchDataItem.wasDeleted = true;

        	if(branchDataItem.getChildren(true).size() > 0)
        	{
    			DeleteBranchChildren(branchDataItem, minimumSize, spawningRequiredBranchesOnly);
        	}
        	if(AllBranchesBranchDataHash.contains(branchDataItem.branchNumber))
        	{
        		if(OTG.getPluginConfig().SpawnLog)
        		{
	        		String allParentsString = "";
	        		BranchDataItem tempBranch = branchDataItem;
	        		while(tempBranch.Parent != null)
	        		{
	        			allParentsString += " <-- X" + tempBranch.Parent.Branch.getChunkX() + " Z" + tempBranch.Parent.Branch.getChunkZ() + " Y" + tempBranch.Parent.Branch.getY() + " " + tempBranch.Parent.Branch.BO3Name + ":" + tempBranch.Parent.Branch.getRotation();
	        			tempBranch = tempBranch.Parent;
	        		}

	        		OTG.log(LogMarker.INFO, "Deleted X" + branchDataItem.Branch.getChunkX() + " Z" + branchDataItem.Branch.getChunkZ() + " Y" + branchDataItem.Branch.getY() + " " + branchDataItem.Branch.BO3Name + ":" + branchDataItem.Branch.getRotation() + (branchDataItem.Branch.isRequiredBranch ? " required" : " optional") + " cycle " + Cycle + allParentsString);
        		}

	        	AllBranchesBranchData.remove(branchDataItem);
	        	AllBranchesBranchDataHash.remove(branchDataItem.branchNumber);
	    		Stack<BranchDataItem> branchDataItemStack = AllBranchesBranchDataByChunk.get(branchDataItem.ChunkCoordinate);
	    		if(branchDataItemStack != null)
	    		{
	    			branchDataItemStack.remove(branchDataItem);
	    			if(branchDataItemStack.size() == 0)
	    			{
	    				AllBranchesBranchDataByChunk.remove(branchDataItem.ChunkCoordinate);
	    			}
	    		}

	        	if(!((BO3)branchDataItem.Branch.getObject()).getSettings().canOverride)
	        	{
	    	    	// If this branch is allowing lower-lying .mustBeBelowOther branches to spawn then roll those back as well
	        		Stack<BranchDataItem> allBranchesBranchData2 = new Stack<BranchDataItem>();
	        		Stack<BranchDataItem> branchDataByChunk = AllBranchesBranchDataByChunk.get(branchDataItem.ChunkCoordinate);
	        		if(branchDataByChunk != null)
	        		{
		        		allBranchesBranchData2.addAll(branchDataByChunk);
		        		for(BranchDataItem branchDataItem2 : allBranchesBranchData2)
		        		{
		        			if(AllBranchesBranchDataHash.contains(branchDataItem2.branchNumber))
		        			{
			        			if(branchDataItem2 != branchDataItem)
			        			{
			    	    			if(((BO3)branchDataItem2.Branch.getObject()).getSettings().mustBeBelowOther && branchDataItem2.ChunkCoordinate.equals(branchDataItem.ChunkCoordinate))
			    	    			{
			    	    				boolean branchAboveFound = false;
			    	    				for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(branchDataItem2.ChunkCoordinate))
		    	    					{
			    	    					if(
			        							branchDataItem3 != branchDataItem &&
			        							!((BO3)branchDataItem3.Branch.getObject()).getSettings().mustBeBelowOther &&
			        							!((BO3)branchDataItem3.Branch.getObject()).getSettings().canOverride &&
			        							branchDataItem3.ChunkCoordinate.equals(branchDataItem2.ChunkCoordinate)
			    							)
			    	    					{
			    	    						if(branchDataItem3.Branch.getY() >= branchDataItem2.Branch.getY())
			    	    						{
			    	    							branchAboveFound = true;
			    	    							break;
			    	    						}
			    	    					}
		    	    					}
			    	    				if(!branchAboveFound)
			    	    				{
			    	    					RollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly);
			    	    				}
			    	    			}
			        			}
		        			}
		        		}
	        		}
	        	}

        		Stack<BranchDataItem> allBranchesBranchData2 = new Stack<BranchDataItem>();
        		Stack<BranchDataItem> branchDataByChunk = AllBranchesBranchDataByChunk.get(branchDataItem.ChunkCoordinate);
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
			    					((BO3)branchDataItem2.Branch.getObject()).getSettings().mustBeInside != null &&
			    					((BO3)branchDataItem2.Branch.getObject()).getSettings().mustBeInside.length() > 0 &&
			    					branchDataItem2.ChunkCoordinate.equals(branchDataItem.ChunkCoordinate)
			    				)
			        			{
									String[] mustBeInsideBO3s = ((BO3)branchDataItem2.Branch.getObject()).getSettings().mustBeInside.split(",");
									boolean currentBO3Found = false;
									for(String mustBeInsideBO3Group : mustBeInsideBO3s)
									{
										String[] mustBeInsideBO3sByGroup = mustBeInsideBO3Group.trim().split(" ");
										for(String mustBeInsideBO3 : mustBeInsideBO3sByGroup)
										{
											String[] mustBeInsideBO3NameAndRotation = mustBeInsideBO3.split(":");
											String mustBeInsideBO3Name = mustBeInsideBO3NameAndRotation[0];
											String mustBeInsideBO3Rotation = mustBeInsideBO3NameAndRotation.length > 1 ? mustBeInsideBO3NameAndRotation[1] : null;

											for(String branchName : ((BO3)branchDataItem.Branch.getObject()).getSettings().getInheritedBO3s())
											{
												if(branchName.equals(mustBeInsideBO3Name.trim()))
												{
			   										int rotation = (branchDataItem.Branch.getRotation().getRotationId() - branchDataItem2.Branch.getRotation().getRotationId());
			   										if(rotation < 0)
			   										{
			   											rotation += 4;
			   										}

			   										if(mustBeInsideBO3Rotation == null || rotation == Rotation.FromString(mustBeInsideBO3Rotation).getRotationId())
													{
														currentBO3Found = true;
														break;
													}
												}
											}
											if(currentBO3Found)
											{
												break;
											}
										}
										if(currentBO3Found)
										{
											break;
										}
									}
									if(currentBO3Found) // The BO3 that is currently being rolled back may have been allowing this mustBeInside branch to spawn
									{
										boolean foundSpwanRequirement = false;
										for(String mustBeInsideBO3Group : mustBeInsideBO3s) // Check if the branch can remain spawned without the branch we're rolling back
										{
											String[] mustBeInsideBO3sByGroup = mustBeInsideBO3Group.trim().split(" ");
											boolean foundAllSpwanRequirementParts = true;
											for(String mustBeInsideBO3 : mustBeInsideBO3sByGroup)
											{
												boolean bFoundPart = false;
												for(BranchDataItem branchDataItem3 : AllBranchesBranchDataByChunk.get(branchDataItem2.ChunkCoordinate))
												{
													if(branchDataItem3 != branchDataItem && branchDataItem3 != branchDataItem2 && branchDataItem3 != branchDataItem2.Parent)
													{
														String[] mustBeInsideBO3NameAndRotation = mustBeInsideBO3.split(":");
														String mustBeInsideBO3Name = mustBeInsideBO3NameAndRotation[0];
														String mustBeInsideBO3Rotation = mustBeInsideBO3NameAndRotation.length > 1 ? mustBeInsideBO3NameAndRotation[1] : null;

														for(String branchName : ((BO3)branchDataItem3.Branch.getObject()).getSettings().getInheritedBO3s())
														{
															if(branchName.equals(mustBeInsideBO3Name.trim()))
															{
						   										int rotation = (branchDataItem3.Branch.getRotation().getRotationId() - branchDataItem2.Branch.getRotation().getRotationId()) % 4;
						   										if(rotation < 0)
						   										{
						   											rotation += 4;
						   										}

						   										if(mustBeInsideBO3Rotation == null || rotation == Rotation.FromString(mustBeInsideBO3Rotation).getRotationId())
																{
																	if(CheckCollision(branchDataItem2.Branch, branchDataItem3.Branch))
																	{
																		bFoundPart = true;
																		break;
																	}
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
													foundAllSpwanRequirementParts = false;
													break;
												}
											}
											if(foundAllSpwanRequirementParts)
											{
												foundSpwanRequirement = true;
											}
										}
										if(!foundSpwanRequirement)
										{
											RollBackBranch(branchDataItem2, minimumSize, spawningRequiredBranchesOnly);
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

    private Stack<BranchDataItem> CheckSpawnRequirementsAndCollisions(BranchDataItem branchData, boolean minimumSize)
    {
    	// collidingObjects are only used for size > 0 check and to see if this branch tried to spawn on top of its parent
    	Stack<BranchDataItem> collidingObjects = new Stack<BranchDataItem>();
    	boolean bFound = false;

    	CustomObjectCoordinate coordObject = branchData.Branch;

    	if(!minimumSize)
    	{
		    // Check if any other structures in world are in this chunk
		    if(!bFound && (World.IsInsidePregeneratedRegion(branchData.ChunkCoordinate, true) || World.getStructureCache().structureCache.containsKey(branchData.ChunkCoordinate)))
		    {
		    	collidingObjects.add(null);
		    	bFound = true;
		    }

		    // Check if the structure can spawn in this biome
		    if(!bFound && !IsStructureAtSpawn)
		    {
		    	ArrayList<String> biomeStructures;

            	LocalBiome biome3 = World.getBiome(branchData.ChunkCoordinate.getChunkX() * 16 + 8, branchData.ChunkCoordinate.getChunkZ() * 16 + 8);
                BiomeConfig biomeConfig3 = biome3.getBiomeConfig();
                // Get Bo3's for this biome
                ArrayList<String> structuresToSpawn = new ArrayList<String>();
                for (ConfigFunction<BiomeConfig> res : biomeConfig3.resourceSequence)
                {
                	if(res instanceof CustomStructureGen)
                	{
                		for(String bo3Name : ((CustomStructureGen)res).objectNames)
                		{
                			structuresToSpawn.add(bo3Name);
                		}
                	}
                }

                biomeStructures = structuresToSpawn;

                boolean canSpawnHere = false;
                for(String structureToSpawn : biomeStructures)
                {
                	if(structureToSpawn.equals(Start.getObject().getName()))
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

	    	int smoothRadius = ((BO3)Start.getObject()).getSettings().smoothRadius; // For collision detection use Start's SmoothingRadius. TODO: Improve this and use smoothingradius of individual branches?
	    	if(smoothRadius == -1 || ((BO3)coordObject.getObject()).getSettings().smoothRadius == -1)
	    	{
	    		smoothRadius = 0;
	    	}
	    	if(smoothRadius > 0 && !bFound)
	        {
	        	// get all chunks within smoothRadius and check structureCache for collisions
	    		double radiusInChunks = Math.ceil((smoothRadius) / (double)16);
	        	for(int x = branchData.ChunkCoordinate.getChunkX() - (int)radiusInChunks; x <= branchData.ChunkCoordinate.getChunkX() + radiusInChunks; x++)
	        	{
	            	for(int z = branchData.ChunkCoordinate.getChunkZ() - (int)radiusInChunks; z <= branchData.ChunkCoordinate.getChunkZ() + radiusInChunks; z++)
	            	{
	            		double distanceBetweenStructures = Math.floor((float) Math.sqrt(Math.pow(branchData.ChunkCoordinate.getChunkX() - x, 2) + Math.pow(branchData.ChunkCoordinate.getChunkZ() - z, 2)));
	            		if(distanceBetweenStructures <= radiusInChunks)
	            		{
	            		    // Check if any other structures in world are in this chunk
	            			if(World.IsInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(x,z), true) || World.getStructureCache().structureCache.containsKey(ChunkCoordinate.fromChunkCoords(x,z)))
	            		    {
	            		        // Structures' bounding boxes are overlapping, don't add this branch.
	            		    	collidingObjects.add(null);
	            		    	bFound = true;
	            		    	break;
	            		    }

	            			if(!IsStructureAtSpawn)
	            			{
		            		    // Check if the structure can spawn in this biome
		            			ArrayList<String> biomeStructures;

	        	            	LocalBiome biome3 = World.getBiome(x * 16 + 8, z * 16 + 8);
	        	                BiomeConfig biomeConfig3 = biome3.getBiomeConfig();
	        	                // Get Bo3's for this biome
	        	                ArrayList<String> structuresToSpawn = new ArrayList<String>();
	        	                for (ConfigFunction<BiomeConfig> res : biomeConfig3.resourceSequence)
	        	                {
	        	                	if(res instanceof CustomStructureGen)
	        	                	{
	        	                		for(String bo3Name : ((CustomStructureGen)res).objectNames)
	        	                		{
	        	                			structuresToSpawn.add(bo3Name);
	        	                		}
	        	                	}
	        	                }

	        	                biomeStructures = structuresToSpawn;

		                        boolean canSpawnHere = false;
		                        for(String structureToSpawn : biomeStructures)
		                        {
		                        	if(structureToSpawn.equals(Start.getObject().getName()))
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

        if(!bFound && !((BO3) coordObject.getObject()).getSettings().canOverride)
        {
	        Stack<BranchDataItem> existingBranches = new Stack<BranchDataItem>();
	        if(AllBranchesBranchDataByChunk.containsKey(branchData.ChunkCoordinate))
	        {
	        	for(BranchDataItem existingBranchData : AllBranchesBranchDataByChunk.get(branchData.ChunkCoordinate))
		        {
		        	if(branchData.ChunkCoordinate.equals(existingBranchData.ChunkCoordinate) && !((BO3)existingBranchData.Branch.getObject()).getSettings().canOverride)
		        	{
		        		existingBranches.add(existingBranchData);
		        	}
		        }
	        }

	        if (existingBranches.size() > 0)
	        {
	        	for (BranchDataItem cachedBranch : existingBranches)
	        	{
	        		if(CheckCollision(coordObject, cachedBranch.Branch))
	        		{
	        			collidingObjects.add(cachedBranch);
	        		}
	        	}
	        }
        }

    	return collidingObjects;
    }

    // TODO: return list with colliding structures instead of bool?
    private boolean CheckCollision(CustomObjectCoordinate branchData1Branch, CustomObjectCoordinate branchData2Branch)
    {
    	if(
			!((BO3)branchData1Branch.getObject()).isCollidable() ||
			!((BO3)branchData2Branch.getObject()).isCollidable()
		)
    	{
    		return false;
    	}

    	// minX/maxX/minZ/maxZ are always positive.

        CustomObjectCoordinate branchData1BranchMinRotated = CustomObjectCoordinate.getRotatedBO3CoordsJustified(((BO3)branchData1Branch.getObject()).getSettings().getminX(), ((BO3)branchData1Branch.getObject()).getSettings().getminY(), ((BO3)branchData1Branch.getObject()).getSettings().getminZ(), branchData1Branch.getRotation());
        CustomObjectCoordinate branchData1BranchMaxRotated = CustomObjectCoordinate.getRotatedBO3CoordsJustified(((BO3)branchData1Branch.getObject()).getSettings().getmaxX(),((BO3)branchData1Branch.getObject()).getSettings().getmaxY(), ((BO3)branchData1Branch.getObject()).getSettings().getmaxZ(), branchData1Branch.getRotation());

        int startX = branchData1Branch.getX() + Math.min(branchData1BranchMinRotated.getX(),branchData1BranchMaxRotated.getX());
        int endX = branchData1Branch.getX() + Math.max(branchData1BranchMinRotated.getX(),branchData1BranchMaxRotated.getX());
        int startY = branchData1Branch.getY() + Math.min(branchData1BranchMinRotated.getY(),branchData1BranchMaxRotated.getY());
        int endY = branchData1Branch.getY() + Math.max(branchData1BranchMinRotated.getY(),branchData1BranchMaxRotated.getY());
        int startZ = branchData1Branch.getZ() + Math.min(branchData1BranchMinRotated.getZ(),branchData1BranchMaxRotated.getZ());
        int endZ = branchData1Branch.getZ() + Math.max(branchData1BranchMinRotated.getZ(),branchData1BranchMaxRotated.getZ());

        CustomObjectCoordinate branchData2BranchMinRotated = CustomObjectCoordinate.getRotatedBO3CoordsJustified(((BO3)branchData2Branch.getObject()).getSettings().getminX(), ((BO3)branchData2Branch.getObject()).getSettings().getminY(), ((BO3)branchData2Branch.getObject()).getSettings().getminZ(), branchData2Branch.getRotation());
        CustomObjectCoordinate branchData2BranchMaxRotated = CustomObjectCoordinate.getRotatedBO3CoordsJustified(((BO3)branchData2Branch.getObject()).getSettings().getmaxX(), ((BO3) branchData2Branch.getObject()).getSettings().getmaxY(), ((BO3)branchData2Branch.getObject()).getSettings().getmaxZ(), branchData2Branch.getRotation());

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
    private void AddToChunk(CustomObjectCoordinate coordObject, ChunkCoordinate chunkCoordinate, Map<ChunkCoordinate, Stack<CustomObjectCoordinate>> objectList)
    {
    	//OTG.log(LogMarker.INFO, "AddToChunk X" + chunkCoordinate.getChunkX() + " Z" + chunkCoordinate.getChunkZ());

        // Get the set of structures to spawn that is currently being stored
        // for the target chunk or create a new one if none exists
        Stack<CustomObjectCoordinate> objectsInChunk = objectList.get(chunkCoordinate);
        if (objectsInChunk == null)
        {
            objectsInChunk = new Stack<CustomObjectCoordinate>();
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
    public boolean SpawnForChunk(ChunkCoordinate chunkCoordinate)
    {
    	//OTG.log(LogMarker.INFO, "SpawnForChunk X" + chunkCoordinate.getChunkX() + " Z" + chunkCoordinate.getChunkZ() + " " + Start.BO3Name);

        // If this structure is not allowed to spawn because a structure
        // of the same type (this.Start BO3 filename) has already been
        // spawned nearby.
    	if(Start == null)
    	{
			throw new RuntimeException();
    	}
    	if ((!ObjectsToSpawn.containsKey(chunkCoordinate) && !SmoothingAreasToSpawn.containsKey(chunkCoordinate)))
        {
            return true;
        }

    	saveRequired = true;

    	DoStartChunkBlockChecks();

        // Get all BO3's that should spawn in the given chunk, if any
        // Note: The given chunk may not necessarily be the chunkCoordinate of this.Start
        Stack<CustomObjectCoordinate> objectsInChunk = ObjectsToSpawn.get(chunkCoordinate);
        if (objectsInChunk != null)
        {
        	BO3Config config = ((BO3)Start.getObject()).getSettings();
            LocalBiome biome = null;
            BiomeConfig biomeConfig = null;
            if(config.SpawnUnderWater)
        	{
            	biome = World.getBiome(Start.getX() + 8, Start.getZ() + 7);
            	biomeConfig = biome.getBiomeConfig();
            	if(biomeConfig == null)
            	{
            		throw new RuntimeException();
            	}
        	}

            BO3.originalTopBlocks.clear(); // TODO: Lol ugly hack fix!

            // Do ReplaceAbove / ReplaceBelow
            for (CustomObjectCoordinate coordObject : objectsInChunk)
            {
                if (coordObject.isSpawned)
                {
                    continue;
                }

                BO3 bo3 = ((BO3)coordObject.getObject());
                if(bo3 == null)
                {
                	throw new RuntimeException();
                }

                BO3Config objectConfig = bo3.getSettings();

                if (!coordObject.spawnWithChecks(chunkCoordinate, World, Random, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceAbove : objectConfig.replaceAbove, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceBelow : objectConfig.replaceBelow, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithBiomeBlocks : objectConfig.replaceWithBiomeBlocks, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithSurfaceBlock : objectConfig.replaceWithSurfaceBlock, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithGroundBlock : objectConfig.replaceWithGroundBlock, config.SpawnUnderWater,  !config.SpawnUnderWater ? -1 : (biomeConfig.useWorldWaterLevel ? World.getConfigs().getWorldConfig().waterLevelMax : biomeConfig.waterLevelMax), false, true))
                {
                	OTG.log(LogMarker.FATAL, "Could not spawn chunk " + coordObject.BO3Name + " for structure " + Start.getObject().getName());
                	throw new RuntimeException("Could not spawn chunk " + coordObject.BO3Name + " for structure " + Start.getObject().getName());
                }
            }

            // Spawn smooth areas in this chunk if any exist
            // If SpawnSmoothAreas returns false then spawning has
            // been delayed and should be tried again later.
        	if(!smoothingAreaManager.SpawnSmoothAreas(chunkCoordinate, SmoothingAreasToSpawn, Start, World)) { return false; }

            for (CustomObjectCoordinate coordObject : objectsInChunk)
            {
                if (coordObject.isSpawned)
                {
                    continue;
                }

                BO3 bo3 = ((BO3)coordObject.getObject());
                if(bo3 == null)
                {
                	throw new RuntimeException();
                }

                BO3Config objectConfig = bo3.getSettings();

                if (!coordObject.spawnWithChecks(chunkCoordinate, World, Random, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceAbove : objectConfig.replaceAbove, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceBelow : objectConfig.replaceBelow, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithBiomeBlocks : objectConfig.replaceWithBiomeBlocks, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithSurfaceBlock : objectConfig.replaceWithSurfaceBlock, config.overrideChildSettings && objectConfig.overrideChildSettings ? config.replaceWithGroundBlock : objectConfig.replaceWithGroundBlock, config.SpawnUnderWater,  !config.SpawnUnderWater ? -1 : (biomeConfig.useWorldWaterLevel ? World.getConfigs().getWorldConfig().waterLevelMax : biomeConfig.waterLevelMax), false, false))
                //if(1 == 0)
                {
                	OTG.log(LogMarker.FATAL, "Could not spawn chunk " + coordObject.BO3Name + " for structure " + Start.getObject().getName());
                	throw new RuntimeException("Could not spawn chunk " + coordObject.BO3Name + " for structure " + Start.getObject().getName());
                } else {

                	ModDataFunction[] blockDataInObject = objectConfig.getModData();
                	for(int i = 0; i < blockDataInObject.length; i++)
                	{
                		ModDataFunction newModData = new ModDataFunction();
                    	if(coordObject.getRotation() != Rotation.NORTH)
                    	{
                        	int rotations = 0;
                        	// How many counter-clockwise rotations have to be applied?
                    		if(coordObject.getRotation() == Rotation.WEST)
                    		{
                    			rotations = 1;
                    		}
                    		else if(coordObject.getRotation() == Rotation.SOUTH)
                    		{
                    			rotations = 2;
                    		}
                    		else if(coordObject.getRotation() == Rotation.EAST)
                    		{
                    			rotations = 3;
                    		}

                            // Apply rotation
                        	if(rotations == 0)
                        	{
                        		newModData.x = blockDataInObject[i].x;
                        		newModData.z = blockDataInObject[i].z;
                        	}
                        	if(rotations == 1)
                        	{
                        		newModData.x = blockDataInObject[i].z;
                        		newModData.z = -blockDataInObject[i].x + 15;
                        	}
                        	if(rotations == 2)
                        	{
                        		newModData.x = -blockDataInObject[i].x + 15;
                        		newModData.z = -blockDataInObject[i].z + 15;
                        	}
                        	if(rotations == 3)
                        	{
                        		newModData.x = -blockDataInObject[i].z + 15;
                        		newModData.z = blockDataInObject[i].x;
                        	}
                        	newModData.y = coordObject.getY() + blockDataInObject[i].y;

                        	newModData.x = coordObject.getX() + newModData.x;
                        	newModData.z = coordObject.getZ() + newModData.z;

                        	newModData.modData = blockDataInObject[i].modData;
                        	newModData.modId = blockDataInObject[i].modId;

                    		modData.add(newModData);

                    		if(!ChunkCoordinate.fromBlockCoords(newModData.x, newModData.z).equals(chunkCoordinate))
                    		{
                    			throw new RuntimeException();
                    		}
                    	} else {

                        	newModData.y = coordObject.getY() + blockDataInObject[i].y;

                        	newModData.x = coordObject.getX() + blockDataInObject[i].x;
                        	newModData.z = coordObject.getZ() + blockDataInObject[i].z;

                        	newModData.modData = blockDataInObject[i].modData;
                        	newModData.modId = blockDataInObject[i].modId;

                    		modData.add(newModData);

                    		if(!ChunkCoordinate.fromBlockCoords(newModData.x, newModData.z).equals(chunkCoordinate))
                    		{
                    			throw new RuntimeException();
                    		}
                    	}
                	}

                	SpawnerFunction[] spawnerDataInObject = objectConfig.getSpawnerData();
                	for(int i = 0; i < spawnerDataInObject.length; i++)
                	{
                		SpawnerFunction newSpawnerData = new SpawnerFunction();
                    	if(coordObject.getRotation() != Rotation.NORTH)
                    	{
                        	int rotations = 0;
                        	// How many counter-clockwise rotations have to be applied?
                    		if(coordObject.getRotation() == Rotation.WEST)
                    		{
                    			rotations = 1;
                    		}
                    		else if(coordObject.getRotation() == Rotation.SOUTH)
                    		{
                    			rotations = 2;
                    		}
                    		else if(coordObject.getRotation() == Rotation.EAST)
                    		{
                    			rotations = 3;
                    		}

                            // Apply rotation
                        	if(rotations == 0)
                        	{
                        		newSpawnerData.x = spawnerDataInObject[i].x;
                        		newSpawnerData.velocityX = spawnerDataInObject[i].velocityX;
                        		newSpawnerData.z = spawnerDataInObject[i].z;
                        		newSpawnerData.velocityZ = spawnerDataInObject[i].velocityZ;
                        		newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityXSet;
                        		newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityZSet;
                        	}
                        	if(rotations == 1)
                        	{
                        		newSpawnerData.x = spawnerDataInObject[i].z;
                        		newSpawnerData.velocityX = spawnerDataInObject[i].velocityZ;
                        		newSpawnerData.z = -spawnerDataInObject[i].x + 15;
                        		newSpawnerData.velocityZ = -spawnerDataInObject[i].velocityX;
                        		newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityZSet;
                        		newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityXSet;
                        	}
                        	if(rotations == 2)
                        	{
                        		newSpawnerData.x = -spawnerDataInObject[i].x + 15;
                        		newSpawnerData.velocityX = -spawnerDataInObject[i].velocityX;
                        		newSpawnerData.z = -spawnerDataInObject[i].z + 15;
                        		newSpawnerData.velocityZ = -spawnerDataInObject[i].velocityZ;
                        		newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityXSet;
                        		newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityZSet;
                        	}
                        	if(rotations == 3)
                        	{
                        		newSpawnerData.x = -spawnerDataInObject[i].z + 15;
                        		newSpawnerData.velocityX = -spawnerDataInObject[i].velocityZ;
                        		newSpawnerData.z = spawnerDataInObject[i].x;
                        		newSpawnerData.velocityZ = spawnerDataInObject[i].velocityX;
                        		newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityZSet;
                        		newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityXSet;
                        	}
                        	newSpawnerData.y = coordObject.getY() + spawnerDataInObject[i].y;

                        	newSpawnerData.x = coordObject.getX() + newSpawnerData.x;
                        	newSpawnerData.z = coordObject.getZ() + newSpawnerData.z;

                        	newSpawnerData.mobName = spawnerDataInObject[i].mobName;
                        	newSpawnerData.originalnbtFileName = spawnerDataInObject[i].originalnbtFileName;
                        	newSpawnerData.nbtFileName = spawnerDataInObject[i].nbtFileName;
                        	newSpawnerData.groupSize = spawnerDataInObject[i].groupSize;
                        	newSpawnerData.interval = spawnerDataInObject[i].interval;
                        	newSpawnerData.spawnChance = spawnerDataInObject[i].spawnChance;
                        	newSpawnerData.maxCount= spawnerDataInObject[i].maxCount;

                        	newSpawnerData.despawnTime = spawnerDataInObject[i].despawnTime;

                        	newSpawnerData.velocityY = spawnerDataInObject[i].velocityY;
                        	newSpawnerData.velocityYSet = spawnerDataInObject[i].velocityYSet;

                        	newSpawnerData.yaw = spawnerDataInObject[i].yaw;
                        	newSpawnerData.pitch = spawnerDataInObject[i].pitch;

                    		spawnerData.add(newSpawnerData);

                    		if(!ChunkCoordinate.fromBlockCoords(newSpawnerData.x, newSpawnerData.z).equals(chunkCoordinate))
                    		{
                    			throw new RuntimeException();
                    		}
                    	} else {

                        	newSpawnerData.y = coordObject.getY() + spawnerDataInObject[i].y;

                        	newSpawnerData.x = coordObject.getX() + spawnerDataInObject[i].x;
                        	newSpawnerData.z = coordObject.getZ() + spawnerDataInObject[i].z;

                        	newSpawnerData.mobName = spawnerDataInObject[i].mobName;
                        	newSpawnerData.originalnbtFileName = spawnerDataInObject[i].originalnbtFileName;
                        	newSpawnerData.nbtFileName = spawnerDataInObject[i].nbtFileName;
                        	newSpawnerData.groupSize = spawnerDataInObject[i].groupSize;
                        	newSpawnerData.interval = spawnerDataInObject[i].interval;
                        	newSpawnerData.spawnChance = spawnerDataInObject[i].spawnChance;
                        	newSpawnerData.maxCount= spawnerDataInObject[i].maxCount;

                        	newSpawnerData.despawnTime = spawnerDataInObject[i].despawnTime;

                        	newSpawnerData.velocityX = spawnerDataInObject[i].velocityX;
                        	newSpawnerData.velocityY = spawnerDataInObject[i].velocityY;
                        	newSpawnerData.velocityZ = spawnerDataInObject[i].velocityZ;

                        	newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityXSet;
                        	newSpawnerData.velocityYSet = spawnerDataInObject[i].velocityYSet;
                        	newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityZSet;

                        	newSpawnerData.yaw = spawnerDataInObject[i].yaw;
                        	newSpawnerData.pitch = spawnerDataInObject[i].pitch;

                    		spawnerData.add(newSpawnerData);

                    		if(!ChunkCoordinate.fromBlockCoords(newSpawnerData.x, newSpawnerData.z).equals(chunkCoordinate))
                    		{
                    			throw new RuntimeException();
                    		}
                    	}
                	}

                	ParticleFunction[] particleDataInObject = objectConfig.getParticleData();
                	for(int i = 0; i < particleDataInObject.length; i++)
                	{
                		ParticleFunction newParticleData = new ParticleFunction();
                    	if(coordObject.getRotation() != Rotation.NORTH)
                    	{
                        	int rotations = 0;
                        	// How many counter-clockwise rotations have to be applied?
                    		if(coordObject.getRotation() == Rotation.WEST)
                    		{
                    			rotations = 1;
                    		}
                    		else if(coordObject.getRotation() == Rotation.SOUTH)
                    		{
                    			rotations = 2;
                    		}
                    		else if(coordObject.getRotation() == Rotation.EAST)
                    		{
                    			rotations = 3;
                    		}

                            // Apply rotation
                        	if(rotations == 0)
                        	{
                        		newParticleData.x = particleDataInObject[i].x;
                        		newParticleData.velocityX = particleDataInObject[i].velocityX;
                        		newParticleData.z = particleDataInObject[i].z;
                        		newParticleData.velocityZ = particleDataInObject[i].velocityZ;
                        		newParticleData.velocityXSet = particleDataInObject[i].velocityXSet;
                        		newParticleData.velocityZSet = particleDataInObject[i].velocityZSet;
                        	}
                        	if(rotations == 1)
                        	{
                        		newParticleData.x = particleDataInObject[i].z;
                        		newParticleData.velocityX = particleDataInObject[i].velocityZ;
                        		newParticleData.z = -particleDataInObject[i].x + 15;
                        		newParticleData.velocityZ = -particleDataInObject[i].velocityX;
                        		newParticleData.velocityXSet = particleDataInObject[i].velocityZSet;
                        		newParticleData.velocityZSet = particleDataInObject[i].velocityXSet;
                        	}
                        	if(rotations == 2)
                        	{
                        		newParticleData.x = -particleDataInObject[i].x + 15;
                        		newParticleData.velocityX = -particleDataInObject[i].velocityX;
                        		newParticleData.z = -particleDataInObject[i].z + 15;
                        		newParticleData.velocityZ = -particleDataInObject[i].velocityZ;
                        		newParticleData.velocityXSet = particleDataInObject[i].velocityXSet;
                        		newParticleData.velocityZSet = particleDataInObject[i].velocityZSet;
                        	}
                        	if(rotations == 3)
                        	{
                        		newParticleData.x = -particleDataInObject[i].z + 15;
                        		newParticleData.velocityX = -particleDataInObject[i].velocityZ;
                        		newParticleData.z = particleDataInObject[i].x;
                        		newParticleData.velocityZ = particleDataInObject[i].velocityX;
                        		newParticleData.velocityXSet = particleDataInObject[i].velocityZSet;
                        		newParticleData.velocityZSet = particleDataInObject[i].velocityXSet;
                        	}
                        	newParticleData.y = coordObject.getY() + particleDataInObject[i].y;

                        	newParticleData.x = coordObject.getX() + newParticleData.x;
                        	newParticleData.z = coordObject.getZ() + newParticleData.z;

                        	newParticleData.particleName = particleDataInObject[i].particleName;

                        	newParticleData.interval = particleDataInObject[i].interval;

                        	newParticleData.velocityY = particleDataInObject[i].velocityY;
                        	newParticleData.velocityYSet = particleDataInObject[i].velocityYSet;

                        	particleData.add(newParticleData);

                    		if(!ChunkCoordinate.fromBlockCoords(newParticleData.x, newParticleData.z).equals(chunkCoordinate))
                    		{
                    			throw new RuntimeException();
                    		}
                    	} else {

                    		newParticleData.y = coordObject.getY() + particleDataInObject[i].y;

                    		newParticleData.x = coordObject.getX() + particleDataInObject[i].x;
                    		newParticleData.z = coordObject.getZ() + particleDataInObject[i].z;

                    		newParticleData.particleName = particleDataInObject[i].particleName;

                    		newParticleData.interval = particleDataInObject[i].interval;

                    		newParticleData.velocityX = particleDataInObject[i].velocityX;
                    		newParticleData.velocityY = particleDataInObject[i].velocityY;
                        	newParticleData.velocityZ = particleDataInObject[i].velocityZ;

                        	newParticleData.velocityXSet = particleDataInObject[i].velocityXSet;
                        	newParticleData.velocityYSet = particleDataInObject[i].velocityYSet;
                        	newParticleData.velocityZSet = particleDataInObject[i].velocityZSet;

                    		particleData.add(newParticleData);

                    		if(!ChunkCoordinate.fromBlockCoords(newParticleData.x, newParticleData.z).equals(chunkCoordinate))
                    		{
                    			throw new RuntimeException();
                    		}
                    	}
                	}

                	EntityFunction[] entityDataInObject = objectConfig.getEntityData();
                	for(int i = 0; i < entityDataInObject.length; i++)
                	{
                		EntityFunction newEntityData = new EntityFunction();

                    	if(coordObject.getRotation() != Rotation.NORTH)
                    	{
                        	int rotations = 0;
                        	// How many counter-clockwise rotations have to be applied?
                    		if(coordObject.getRotation() == Rotation.WEST)
                    		{
                    			rotations = 1;
                    		}
                    		else if(coordObject.getRotation() == Rotation.SOUTH)
                    		{
                    			rotations = 2;
                    		}
                    		else if(coordObject.getRotation() == Rotation.EAST)
                    		{
                    			rotations = 3;
                    		}

                            // Apply rotation
                        	if(rotations == 0)
                        	{
                        		newEntityData.x = entityDataInObject[i].x;
                        		newEntityData.z = entityDataInObject[i].z;
                        	}
                        	if(rotations == 1)
                        	{
                        		newEntityData.x = entityDataInObject[i].z;
                        		newEntityData.z = -entityDataInObject[i].x + 15;
                        	}
                        	if(rotations == 2)
                        	{
                        		newEntityData.x = -entityDataInObject[i].x + 15;
                        		newEntityData.z = -entityDataInObject[i].z + 15;
                        	}
                        	if(rotations == 3)
                        	{
                        		newEntityData.x = -entityDataInObject[i].z + 15;
                        		newEntityData.z = entityDataInObject[i].x;
                        	}
                        	newEntityData.y = coordObject.getY() + entityDataInObject[i].y;

                        	newEntityData.x = coordObject.getX() + newEntityData.x;
                        	newEntityData.z = coordObject.getZ() + newEntityData.z;

                        	newEntityData.mobName = entityDataInObject[i].mobName;
                        	newEntityData.groupSize = entityDataInObject[i].groupSize;
                        	newEntityData.nameTagOrNBTFileName = entityDataInObject[i].nameTagOrNBTFileName;

                    		World.SpawnEntity(newEntityData);

                    		if(!ChunkCoordinate.fromBlockCoords(newEntityData.x, newEntityData.z).equals(chunkCoordinate))
                    		{
                    			throw new RuntimeException();
                    		}
                    	} else {

                        	newEntityData.y = coordObject.getY() + entityDataInObject[i].y;

                        	newEntityData.x = coordObject.getX() + entityDataInObject[i].x;
                        	newEntityData.z = coordObject.getZ() + entityDataInObject[i].z;

                        	newEntityData.mobName = entityDataInObject[i].mobName;
                        	newEntityData.groupSize = entityDataInObject[i].groupSize;
                        	newEntityData.nameTagOrNBTFileName = entityDataInObject[i].nameTagOrNBTFileName;

                    		World.SpawnEntity(newEntityData);

                    		if(!ChunkCoordinate.fromBlockCoords(newEntityData.x, newEntityData.z).equals(chunkCoordinate))
                    		{
                    			throw new RuntimeException();
                    		}
                    	}
                	}

                    coordObject.isSpawned = true;
                }
            }
            BO3.originalTopBlocks.clear(); // TODO: Lol ugly hack fix!
        } else {
            // Spawn smooth areas in this chunk if any exist
            // If SpawnSmoothAreas returns false then spawning has
            // been delayed and should be tried again later.
        	if(!smoothingAreaManager.SpawnSmoothAreas(chunkCoordinate, SmoothingAreasToSpawn, Start, World)) { return false; }
        }

		ObjectsToSpawn.remove(chunkCoordinate);
		SmoothingAreasToSpawn.remove(chunkCoordinate);

        return true;
    }

    public CustomObjectStructure(CustomObjectCoordinate start)
    {
    	IsOTGPlus = false;
    	Start = start;
    }

	//

    protected StructurePartSpawnHeight height;
    private Map<ChunkCoordinate, Set<CustomObjectCoordinate>> objectsToSpawn;
    private int maxBranchDepth;

    CustomObjectStructure(LocalWorld world, CustomObjectCoordinate start)
    {
    	IsOTGPlus = false;
        StructuredCustomObject object = (StructuredCustomObject)start.getObject(); // TODO: Turned CustomObject into StructuredCustomObject, check if that doesn't cause problems. Can a non-StructuredCustomObject be passed here?

        this.World = world;
        this.Start = start;
        this.height = object.getStructurePartSpawnHeight();
        this.maxBranchDepth = object.getMaxBranchDepth();
        this.Random = RandomHelper.getRandomForCoords(start.getX(), start.getY(), start.getZ(), world.getSeed());

        // Calculate all branches and add them to a list
        objectsToSpawn = new LinkedHashMap<ChunkCoordinate, Set<CustomObjectCoordinate>>();

        addToSpawnList(start, object); // Add the object itself
        addBranches(start, 1);
    }

    private void addBranches(CustomObjectCoordinate coordObject, int depth)
    {
    	// This should never happen for OTG+

    	CustomObject object = coordObject.getObject();

    	if(object != null)
    	{
	        for (Branch branch : getBranches(object, coordObject.getRotation()))
	        {
	        	// TODO: Does passing null as startbo3name work?
	            CustomObjectCoordinate childCoordObject = branch.toCustomObjectCoordinate(World, Random, coordObject.getRotation(), coordObject.getX(), coordObject.getY(), coordObject.getZ(), null);

	            // Don't add null objects
	            if (childCoordObject == null)
	            {
	                continue;
	            }

	            // Add this object to the chunk
	            addToSpawnList(childCoordObject, object);

	            // Also add the branches of this object
	            if (depth < maxBranchDepth)
	            {
	                addBranches(childCoordObject, depth + 1);
	            }
	        }
    	}
    }

    private Branch[] getBranches(CustomObject customObject, Rotation rotation)
    {
        return ((BO3)customObject).getBranches(rotation);
    }

    /**
     * Adds the object to the spawn list of each chunk that the object
     * touches.
     * @param coordObject The object.
     */
    private void addToSpawnList(CustomObjectCoordinate coordObject, CustomObject parent)
    {
        ChunkCoordinate chunkCoordinate = coordObject.getPopulatingChunk();
        if(chunkCoordinate != null)
        {
	        Set<CustomObjectCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
	        if (objectsInChunk == null)
	        {
	            objectsInChunk = new LinkedHashSet<CustomObjectCoordinate>();
	            objectsToSpawn.put(chunkCoordinate, objectsInChunk);
	        }
	        objectsInChunk.add(coordObject);
        } else {
    		if(OTG.getPluginConfig().SpawnLog)
    		{
	    		OTG.log(LogMarker.WARN, "Error reading branch in BO3 " + parent.getName()  + " Could not find BO3: " + coordObject.BO3Name);
    		}
        }
    }

    // Only used for OTG CustomStructure
    public void spawnForChunk(ChunkCoordinate chunkCoordinate)
    {
        Set<CustomObjectCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
        if (objectsInChunk != null)
        {
            for (CustomObjectCoordinate coordObject : objectsInChunk)
            {
                coordObject.spawnWithChecks(this, World, height, Random);
            }
        }
    }
}
