package com.pg85.otg.customobjects;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalMaterialData;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.BiomeConfig;
import com.pg85.otg.configuration.ConfigFunction;
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

    Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawnPerDiagonalLineOrigin = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();
    Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawnPerDiagonalLineDestination = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();
    Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawnPerLineOrigin = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();
    Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawnPerLineDestination = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

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
			if((((BO3)Start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestBlock || ((BO3)Start.getObject()).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock) && (World.getConfigs().getWorldConfig().disableBedrock || ((BO3)Start.getObject()).getSettings().minHeight > 1 || ((BO3)Start.getObject()).getSettings().maxHeight < 256))
			{
				if(!DoStartChunkBlockChecks()){ return; }
			}

			if(!((BO3)Start.getObject()).getSettings().CanSpawnOnWater)
			{
				if(!DoStartChunkBlockChecks()){ return; }
				int highestBlocky = world.getHighestBlockYAt(Start.getX() + 8, Start.getZ() + 7, true, true, false, true);;
				//if(Start.y - 1 > OTG.WORLD_DEPTH && Start.y - 1 < OTG.WORLD_HEIGHT && world.getMaterial(Start.getX() + 8, Start.y - 1, Start.getZ() + 7).isLiquid())
				if(Start.y - 1 > OTG.WORLD_DEPTH && Start.y - 1 < OTG.WORLD_HEIGHT && world.getMaterial(Start.getX() + 8, highestBlocky, Start.getZ() + 7, IsOTGPlus).isLiquid())
				{
					return;
				}
			}

			if(((BO3)Start.getObject()).getSettings().SpawnOnWaterOnly)
			{
				if(!DoStartChunkBlockChecks()){ return; }
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
				OTG.log(LogMarker.ERROR, "An unknown error occurred while calculating branches for BO3 " + Start.BO3Name + ". This is probably an error in the BO3's branch configuration, not a bug. If you can track this down, please tell me what caused it!");
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

			//TODO: Smoothing areas should count as must spawn/required branches!

	        // Calculate smoothing areas around the entire branching structure
	        // Smooth the terrain in all directions bordering the structure so
	        // that there is a smooth transition in height from the surrounding
	        // terrain to the BO3. This way BO3's won't float above the ground
	        // or spawn inside a hole with vertical walls.
			SmoothingAreasToSpawn = CalculateSmoothingAreas();

			SmoothingAreasToSpawnPerDiagonalLineOrigin.clear();
			SmoothingAreasToSpawnPerDiagonalLineDestination.clear();
			SmoothingAreasToSpawnPerLineOrigin.clear();
			SmoothingAreasToSpawnPerLineDestination.clear();

			for(Entry<ChunkCoordinate, ArrayList<Object[]>> derp : SmoothingAreasToSpawn.entrySet())
			{
				ArrayList<Object[]> values = derp.getValue();
				for(Object[] smoothingAreaLine : values)
				{
        			int originPointX2 = (Integer)smoothingAreaLine[6];
					int originPointZ2 = (Integer)smoothingAreaLine[8];

					ChunkCoordinate originChunk = ChunkCoordinate.fromBlockCoords(originPointX2, originPointZ2);
                	ArrayList<Object[]> lineInOriginChunkSaved2 = SmoothingAreasToSpawnPerLineOrigin.get(originChunk);
                	if(lineInOriginChunkSaved2 == null)
                	{
                    	ArrayList<Object[]> smoothingAreaLines = new ArrayList<Object[]>();
                    	smoothingAreaLines.add(smoothingAreaLine);
                    	SmoothingAreasToSpawnPerLineOrigin.put(ChunkCoordinate.fromChunkCoords(originPointX2, originPointZ2), smoothingAreaLines);
                    } else {
                    	lineInOriginChunkSaved2.add(smoothingAreaLine);
                    }

                	int finalDestinationPointX2 = (Integer)smoothingAreaLine[9];
                    int finalDestinationPointZ2 = (Integer)smoothingAreaLine[11];

					originChunk = ChunkCoordinate.fromBlockCoords(finalDestinationPointX2, finalDestinationPointZ2);
                	ArrayList<Object[]> lineInOriginChunkSaved3 = SmoothingAreasToSpawnPerLineDestination.get(originChunk);
                	if(lineInOriginChunkSaved3 == null)
                	{
                    	ArrayList<Object[]> smoothingAreaLines = new ArrayList<Object[]>();
                    	smoothingAreaLines.add(smoothingAreaLine);
                    	SmoothingAreasToSpawnPerLineDestination.put(ChunkCoordinate.fromChunkCoords(finalDestinationPointX2, finalDestinationPointZ2), smoothingAreaLines);
                    } else {
                    	lineInOriginChunkSaved3.add(smoothingAreaLine);
                    }

            		if(smoothingAreaLine.length > 17)
            		{
	                	int diagonalLineFinalOriginPointX2 = (Integer)smoothingAreaLine[12];
	                    int diagonalLineFinalOriginPointZ2 = (Integer)smoothingAreaLine[14];

	                	int diagonalLineFinalDestinationPointX2 = (Integer)smoothingAreaLine[15];
	                    int diagonalLineFinalDestinationPointZ2 = (Integer)smoothingAreaLine[17];

	                    originChunk = ChunkCoordinate.fromBlockCoords(diagonalLineFinalOriginPointX2, diagonalLineFinalOriginPointZ2);
	                    ArrayList<Object[]> lineInOriginChunkSaved4 = SmoothingAreasToSpawnPerDiagonalLineOrigin.get(originChunk);
	                    if(lineInOriginChunkSaved4 == null)
	                    {
	                    	ArrayList<Object[]> smoothingAreaLines = new ArrayList<Object[]>();
	                    	smoothingAreaLines.add(smoothingAreaLine);
	                    	SmoothingAreasToSpawnPerDiagonalLineOrigin.put(ChunkCoordinate.fromChunkCoords(diagonalLineFinalOriginPointX2, diagonalLineFinalOriginPointZ2), smoothingAreaLines);
	                    } else {
	                    	lineInOriginChunkSaved4.add(smoothingAreaLine);
	                    }

	                    originChunk = ChunkCoordinate.fromBlockCoords(diagonalLineFinalDestinationPointX2, diagonalLineFinalDestinationPointZ2);
	                    ArrayList<Object[]> lineInOriginChunkSaved = SmoothingAreasToSpawnPerDiagonalLineDestination.get(originChunk);
	                    if(lineInOriginChunkSaved == null)
	                    {
	                    	ArrayList<Object[]> smoothingAreaLines = new ArrayList<Object[]>();
	                    	smoothingAreaLines.add(smoothingAreaLine);
	                    	SmoothingAreasToSpawnPerDiagonalLineDestination.put(ChunkCoordinate.fromChunkCoords(diagonalLineFinalDestinationPointX2, diagonalLineFinalDestinationPointZ2), smoothingAreaLines);
	                    } else {
	                    	lineInOriginChunkSaved.add(smoothingAreaLine);
	                    }
                	}
				}
			}

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

    /**
     * Adds a smoothing area around the lowest layer of blocks in all BO3's within this branching structure that have smoothRadius set to a value higher than 0.
     * The smooth area is basicly a set of lines, each line being a set of start- and end-point coordinates. Each line starts from a block on the lowest
     * layer of blocks in the BO3 that has no neighbouring block on one of four sides horizontally (taking into account any neighbouring branches that connect seamlessly).
     * The line is drawn starting at that block and then goes outward in the direction where no neighbouring block was found, the length of the line being the smoothRadius.
     * Later, when the BO3 blocks and the smoothing areas are actually being spawned, the y-value of the endpoint is detected (since by then the terrain will have spawned
     * and we'll be able to detect the highest solid block in the landscape that we'll need to smooth to) and the lines of blocks we've plotted are spawned, creating a nice
     * linear slope from the highest solid block in the landscape to the lowest block in the BO3 (the block we started at when drawing the line).
    */
    private Map<ChunkCoordinate, ArrayList<Object[]>> CalculateSmoothingAreas()
    {
        // TODO: Don't check neighbouring BO3's with SmoothRadius -1

        // Get all solid blocks on the lowest layer of this BO3 that border an air block or have no neighbouring blocks
        // This may include blocks on the border of this BO3 that are supposed to seamlessly border another BO3, remove those later since they shouldnt be smoothed
        Map<ChunkCoordinate, ArrayList<Object[]>> smoothToBlocksPerChunk = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesn't exactly
        // make the code any easier to read..
        // Object[] { bO3, blockX, blockY, blockZ, smoothInDirection1, smoothInDirection2, smoothInDirection3, smoothInDirection4, smoothRadius }
        ArrayList<Object[]> smoothToBlocks;
        ChunkCoordinate chunkCoord;
        Stack<CustomObjectCoordinate> bO3sInChunk;
        boolean bFoundNeighbour1;
        boolean bFoundNeighbour2;
        boolean bFoundNeighbour3;
        boolean bFoundNeighbour4;
        CustomObjectCoordinate neighbouringBlockCoords;
        int normalizedNeigbouringBlockX;
        int normalizedNeigbouringBlockY;
        int normalizedNeigbouringBlockZ;
        ChunkCoordinate neighbouringBlockChunk;
        ChunkCoordinate searchTarget;
        Stack<CustomObjectCoordinate> bO3sInNeighbouringBlockChunk;
        CustomObjectCoordinate blockToCheckCoords;
        int normalizedBlockToCheckX;
        int normalizedBlockToCheckY;
        int normalizedBlockToCheckZ;

        // Get all BO3's that are a part of this branching structure
        for(Entry<ChunkCoordinate, Stack<CustomObjectCoordinate>> chunkCoordSet : ObjectsToSpawn.entrySet())
        {
            chunkCoord = chunkCoordSet.getKey();
            bO3sInChunk = chunkCoordSet.getValue();
            smoothToBlocks = new ArrayList<Object[]>();

            for(CustomObjectCoordinate objectInChunk : bO3sInChunk)
            {
            	if(objectInChunk.isSpawned)
            	{
            		continue;
            	}

            	BO3 bO3InChunk = ((BO3)objectInChunk.getObject());
            	boolean SmoothStartTop = ((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothStartTop : bO3InChunk.getSettings().smoothStartTop;

                //if((((BO3)Start.getObject(World.getName())).settings.overrideChildSettings && ((BO3)bO3InChunk.getObject(World.getName())).settings.overrideChildSettings && ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius != -1 ? ((BO3)Start.getObject(World.getName())).settings.smoothRadius : ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius) > 0)
                int smoothRadius = ((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothRadius : bO3InChunk.getSettings().smoothRadius;
                if(smoothRadius == -1 || bO3InChunk.getSettings().smoothRadius == -1)
                {
                	smoothRadius = 0;
                }
                if(smoothRadius > 0)
                {
        			Map<ChunkCoordinate, BlockFunction> heightMap = bO3InChunk.getSettings().getHeightMap((BO3)Start.getObject());

                    // if !SmoothStartTop then for each BO3 that has a smoothradius > 0 get the lowest layer of blocks and determine smooth area starting points
                	// if SmoothStartTop then for each BO3 that has a smoothradius > 0 get the highest blocks of the BO3 and determine smooth area starting points

                	for(int x = 0; x <= 15; x ++)
                	{
                		for(int z = 0; z <= 15; z ++)
                		{
                			BlockFunction block = heightMap.get(ChunkCoordinate.fromChunkCoords(x, z));

                			if(block != null)
                			{
                            	//if(1 == 1) { throw new RuntimeException();}

                                bFoundNeighbour1 = false;
                                bFoundNeighbour2 = false;
                                bFoundNeighbour3 = false;
                                bFoundNeighbour4 = false;

                                //Check if any neighbouring blocks are air or non-existent within this BO3
                                if(heightMap.get(ChunkCoordinate.fromChunkCoords(block.x - 1, block.z)) != null)
                                {
                                    bFoundNeighbour1 = true;
                                }
                                if(heightMap.get(ChunkCoordinate.fromChunkCoords(block.x + 1, block.z)) != null)
                                {
                                    bFoundNeighbour2 = true;
                                }
                                if(heightMap.get(ChunkCoordinate.fromChunkCoords(block.x, block.z - 1)) != null)
                                {
                                    bFoundNeighbour3 = true;
                                }
                                if(heightMap.get(ChunkCoordinate.fromChunkCoords(block.x, block.z + 1)) != null)
                                {
                                    bFoundNeighbour4 = true;
                                }

                                // If one of the neighbouring blocks has not been found in this BO3 and the block is on the edge of the BO3
                                // then check for other BO3's that may have blocks that border this block.
                                // If a solid neighbouring block is found then don't smooth in that direction.

                                if(!bFoundNeighbour1 && block.x - 1 < 0)
                                {
                                    // Check if the BO3 contains a block at the location of the neighbouring block
                                    // Normalize the coordinates of the neighbouring block taking into consideration rotation
                                    neighbouringBlockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x - 1, block.y, block.z, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    // Get the chunk that the neighbouring block is in
                                    neighbouringBlockChunk = null;
                                    searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
                                    for(ChunkCoordinate chunkInStructure : ObjectsToSpawn.keySet())
                                    {
                                        // Find the chunk that contains the coordinates were looking for
                                        if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
                                        {
                                            neighbouringBlockChunk = chunkInStructure;
                                            break;
                                        }
                                    }
                                    if(neighbouringBlockChunk != null)
                                    {
                                        // Found the neighbouring chunk
                                        bO3sInNeighbouringBlockChunk = ObjectsToSpawn.get(neighbouringBlockChunk);
                                        if(bO3sInNeighbouringBlockChunk != null)
                                        {
                                            for(CustomObjectCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                                            {
                                                if(bO3ToCheck != objectInChunk)
                                                {
                                                    // Now find the actual block
                                                	Map<ChunkCoordinate, BlockFunction> neighbouringBO3HeightMap = ((BO3)bO3ToCheck.getObject()).getSettings().getHeightMap((BO3)Start.getObject());

                                                	for(Entry<ChunkCoordinate, BlockFunction> blockToCheckEntry : neighbouringBO3HeightMap.entrySet())
                                                    {
                                                		BlockFunction blockToCheck = blockToCheckEntry.getValue();

                                                        blockToCheckCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(blockToCheck.x, blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
                                                        normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
                                                        normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
                                                        normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());

                                                        if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
                                                        {
                                                            // Neighbouring block found
                                                        	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck))
                                                            {
                                                            	bFoundNeighbour1 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if(bFoundNeighbour1)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(!bFoundNeighbour2 && block.x + 1 > 15)
                                {
                                    // Check if the BO3 contains a block at the location of the neighbouring block
                                    //Normalize the coordinates of the neigbouring block taking into consideration rotation
                                    neighbouringBlockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + 1, block.y, block.z, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    // Get the chunk that the neighbouring block is in
                                    neighbouringBlockChunk = null;
                                    searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
                                    for(ChunkCoordinate chunkInStructure : ObjectsToSpawn.keySet())
                                    {
                                        // Find the chunk that contains the coordinates being looked for
                                        if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
                                        {
                                            neighbouringBlockChunk = chunkInStructure;
                                            break;
                                        }
                                    }
                                    if(neighbouringBlockChunk != null)
                                    {
                                        //found the neighbouring chunk
                                        bO3sInNeighbouringBlockChunk = ObjectsToSpawn.get(neighbouringBlockChunk);
                                        if(bO3sInNeighbouringBlockChunk != null)
                                        {
                                            for(CustomObjectCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                                            {
                                                if(bO3ToCheck != objectInChunk)
                                                {
                                                    // Now find the actual block
                                                	Map<ChunkCoordinate, BlockFunction> neighbouringBO3HeightMap = ((BO3)bO3ToCheck.getObject()).getSettings().getHeightMap((BO3)Start.getObject());

                                                	for(Entry<ChunkCoordinate, BlockFunction> blockToCheckEntry : neighbouringBO3HeightMap.entrySet())
                                                	{
                                                		BlockFunction blockToCheck = blockToCheckEntry.getValue();

                                                        blockToCheckCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(blockToCheck.x, blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
                                                        normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
                                                        normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
                                                        normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());

                                                        if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
                                                        {
                                                            // Neighbouring block found
                                                        	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck))
                                                            {
                                                                bFoundNeighbour2 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if(bFoundNeighbour2)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(!bFoundNeighbour3 && block.z - 1 < 0)
                                {
                                    // Check if the BO3 contains a block at the location of the neighbouring block
                                    //Normalize the coordinates of the neigbouring block taking into consideration rotation
                                    neighbouringBlockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x, block.y, block.z - 1, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    // Get the chunk that the neighbouring block is in
                                    neighbouringBlockChunk = null;
                                    searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
                                    for(ChunkCoordinate chunkInStructure : ObjectsToSpawn.keySet())
                                    {
                                        // Find the chunk that contains the coordinates being looked for
                                        if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
                                        {
                                            neighbouringBlockChunk = chunkInStructure;
                                            break;
                                        }
                                    }
                                    if(neighbouringBlockChunk != null)
                                    {
                                        //found the neighbouring chunk
                                        bO3sInNeighbouringBlockChunk = ObjectsToSpawn.get(neighbouringBlockChunk);
                                        if(bO3sInNeighbouringBlockChunk != null)
                                        {
                                            for(CustomObjectCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                                            {
                                                if(bO3ToCheck != objectInChunk)
                                                {
                                                    // Now find the actual block
                                                	Map<ChunkCoordinate, BlockFunction> neighbouringBO3HeightMap = ((BO3)bO3ToCheck.getObject()).getSettings().getHeightMap((BO3)Start.getObject());

                                                	for(Entry<ChunkCoordinate, BlockFunction> blockToCheckEntry : neighbouringBO3HeightMap.entrySet())
                                                    {
                                                		BlockFunction blockToCheck = blockToCheckEntry.getValue();

                                                        blockToCheckCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(blockToCheck.x, blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
                                                        normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
                                                        normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
                                                        normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());

                                                        if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
                                                        {
                                                            // Neighbouring block found
                                                        	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck))
                                                            {
                                                            	bFoundNeighbour3 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if(bFoundNeighbour3)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(!bFoundNeighbour4 && block.z + 1 > 15)
                                {
                                    // Check if the BO3 contains a block at the location of the neighbouring block
                                    // Normalize the coordinates of the neighbouring block taking into consideration rotation
                                    neighbouringBlockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x, block.y, block.z + 1, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    // Get the chunk that the neighbouring block is in
                                    neighbouringBlockChunk = null;
                                    searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
                                    for(ChunkCoordinate chunkInStructure : ObjectsToSpawn.keySet())
                                    {
                                        // Find the chunk that contains the coordinates being looked for
                                        if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
                                        {
                                            neighbouringBlockChunk = chunkInStructure;
                                            break;
                                        }
                                    }
                                    if(neighbouringBlockChunk != null)
                                    {
                                        //found the neighbouring chunk
                                        bO3sInNeighbouringBlockChunk = ObjectsToSpawn.get(neighbouringBlockChunk);
                                        if(bO3sInNeighbouringBlockChunk != null)
                                        {
                                            for(CustomObjectCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                                            {
                                                if(bO3ToCheck != objectInChunk)
                                                {
                                                    // Now find the actual block
                                                	Map<ChunkCoordinate, BlockFunction> neighbouringBO3HeightMap = ((BO3)bO3ToCheck.getObject()).getSettings().getHeightMap((BO3)Start.getObject());

                                                	for(Entry<ChunkCoordinate, BlockFunction> blockToCheckEntry : neighbouringBO3HeightMap.entrySet())
                                                    {
                                                		BlockFunction blockToCheck = blockToCheckEntry.getValue();

                                                        blockToCheckCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(blockToCheck.x, blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
                                                        normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
                                                        normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
                                                        normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());

                                                        if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
                                                        {
                                                            // Neighbouring block found
                                                        	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck))
                                                            {
                                                                bFoundNeighbour4 = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if(bFoundNeighbour4)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Only blocks that have air blocks or no blocks as neighbours should be part of the smoothing area
                                if(!bFoundNeighbour1 || !bFoundNeighbour2 || !bFoundNeighbour3 || !bFoundNeighbour4)
                                {
                                    // The first block of the smoothing area is placed at a 1 block offset in the direction of the smoothing area so that it is not directly underneath or above the origin block
                                    // for outside corner blocks (blocks with no neighbouring block on 2 adjacent sides) that means they will be placed at x AND z offsets of plus or minus one.
                                    // smoothToBlocks is filled with Object[] { bO3, blockX, blockY, blockZ, smoothInDirection1, smoothInDirection2, smoothInDirection3, smoothInDirection4, smoothRadius }
                                    int xOffset = 0;
                                    int yOffset = 0;
                                    int zOffset = 0;

                            		int smoothHeightOffset = ((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothHeightOffset : bO3InChunk.getSettings().smoothHeightOffset;
                                	yOffset += smoothHeightOffset;

                                    // Shorten diagonal line to make circle x = sin(smoothradius)
                                    // 45 degrees == 0.7853981634 radians

                                    //int a = (((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1) * (((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1);
                                    //int b = (((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1) * (((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1);
                                    //int smoothRadiusRectangleCorner = (int)Math.ceil(Math.sqrt(a + b));
                                    //smoothRadiusRectangleCorner = ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1;
                                    //smoothRadiusRectangleCorner = 10;

                                    //test = true;

                                    // Circle / round corners
                                    int smoothRadius1 = bO3InChunk.getSettings().smoothRadius == -1 ? 0 : (((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothRadius : bO3InChunk.getSettings().smoothRadius) - 1;
                                    int smoothRadius2 = bO3InChunk.getSettings().smoothRadius == -1 ? 0 : (int)Math.ceil(((((BO3)Start.getObject()).getSettings().overrideChildSettings && bO3InChunk.getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothRadius : bO3InChunk.getSettings().smoothRadius) - 1) * Math.sin(0.7853981634));

                                    // Square / square corners
                                    //int smoothRadius1 = ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1;
                                    //int smoothRadius2 = ((BO3)bO3InChunk.getObject(World.getName())).settings.smoothRadius - 1;

                                    int xOffset1 = 0;
                                    int zOffset1 = 0;

                                    if(!bFoundNeighbour1)
                                    {
                                        xOffset = -1;
                                        CustomObjectCoordinate blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset1, objectInChunk.getRotation());

                                        Object[] smoothDirections = RotateSmoothDirections(true, false, false, false, objectInChunk.getRotation());

                                        smoothToBlocks.add(new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1 });

                                        if(!bFoundNeighbour3)
                                        {
                                            zOffset = -1;
                                            blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                            smoothDirections = RotateSmoothDirections(true, false, true, false, objectInChunk.getRotation());

                                            //PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ bO3InChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], 0,smoothRadius2 });
                                            PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1,smoothRadius2 });
                                        }
                                        if(!bFoundNeighbour4)
                                        {
                                            zOffset = 1;
                                            blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                            smoothDirections = RotateSmoothDirections(true, false, false, true, objectInChunk.getRotation());

                                            //PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ bO3InChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], 0,smoothRadius2 });
                                            PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1,smoothRadius2 });
                                        }
                                    }

                                    if(!bFoundNeighbour2)
                                    {
                                        xOffset = 1;
                                        CustomObjectCoordinate blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset1, objectInChunk.getRotation());
                                        Object[] smoothDirections = RotateSmoothDirections(false, true, false, false, objectInChunk.getRotation());

                                        smoothToBlocks.add(new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1 });

                                        if(!bFoundNeighbour3)
                                        {
                                            zOffset = -1;
                                            blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                            smoothDirections = RotateSmoothDirections(false, true, true, false, objectInChunk.getRotation());

                                            //PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ bO3InChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], 0, smoothRadius2 });
                                            PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1, smoothRadius2 });
                                        }
                                        if(!bFoundNeighbour4)
                                        {
                                            zOffset = 1;
                                            blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                            smoothDirections = RotateSmoothDirections(false, true, false, true, objectInChunk.getRotation());

                                            //PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ bO3InChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], 0, smoothRadius2 });
                                            PlotDiagonalLine(smoothToBlocksPerChunk, new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1, smoothRadius2 });
                                        }
                                    }

                                    if(!bFoundNeighbour3)
                                    {
                                        zOffset = -1;
                                        CustomObjectCoordinate blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                        Object[] smoothDirections = RotateSmoothDirections(false, false, true, false, objectInChunk.getRotation());

                                        smoothToBlocks.add(new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1 });
                                    }
                                    if(!bFoundNeighbour4)
                                    {
                                        zOffset = 1;
                                        CustomObjectCoordinate blockCoords = CustomObjectCoordinate.getRotatedSmoothingCoords(block.x + xOffset1, block.y + yOffset, block.z + zOffset + zOffset1, objectInChunk.getRotation());
                                        Object[] smoothDirections = RotateSmoothDirections(false, false, false, true, objectInChunk.getRotation());

                                        smoothToBlocks.add(new Object[]{ objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3], smoothRadius1 });
                                    }
                                }
                			}
                		}
            		}
                }
            }

            if(!smoothToBlocksPerChunk.containsKey(chunkCoord))
            {
            	smoothToBlocksPerChunk.put(chunkCoord, smoothToBlocks);
            } else {
                // only happens in chunks that have horizontal/vertical lines as well as diagonal ones
            	smoothToBlocksPerChunk.get(chunkCoord).addAll(smoothToBlocks);
            }
        }

        return CalculateBeginAndEndPointsPerChunk(smoothToBlocksPerChunk);
    }

    Object[] RotateSmoothDirections(Boolean smoothDirection1, Boolean smoothDirection2, Boolean smoothDirection3, Boolean smoothDirection4, Rotation rotation)
    {
    	// smoothDirection1 -1x WEST
    	// smoothDirection2 +1x EAST
    	// smoothDirection3 -1z NORTH
    	// smoothDirection4 +1z SOUTH
		if(rotation == Rotation.NORTH)
		{
			return new Object[] { smoothDirection1, smoothDirection2, smoothDirection3, smoothDirection4 };
		}
		else if(rotation == Rotation.EAST)
		{
			return new Object[] { smoothDirection4, smoothDirection3, smoothDirection1, smoothDirection2 };
		}
		else if(rotation == Rotation.SOUTH)
		{
			return new Object[] { smoothDirection2, smoothDirection1, smoothDirection4, smoothDirection3 };
		} else {
			return new Object[] { smoothDirection3, smoothDirection4, smoothDirection2, smoothDirection1 };
		}
    }

    private boolean isMaterialSmoothingAnchor(BlockFunction blockToCheck, CustomObjectCoordinate bO3ToCheck)
    {
		boolean isSmoothAreaAnchor = false;
		if(blockToCheck instanceof RandomBlockFunction)
		{
			for(LocalMaterialData material : ((RandomBlockFunction)blockToCheck).blocks)
			{
				// TODO: Material should never be null, fix the code in RandomBlockFunction.load() that causes this.
				if(material == null)
				{
					continue;
				}
				if(material.isSmoothAreaAnchor(((BO3)Start.getObject()).getSettings().overrideChildSettings && ((BO3)bO3ToCheck.getObject()).getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothStartWood : ((BO3)bO3ToCheck.getObject()).getSettings().smoothStartWood, ((BO3)Start.getObject()).getSettings().SpawnUnderWater))
				{
					isSmoothAreaAnchor = true;
					break;
				}
			}
		}

        // Neighbouring block found
    	if(
			isSmoothAreaAnchor ||
			(!(blockToCheck instanceof RandomBlockFunction) && blockToCheck.material.isSmoothAreaAnchor(((BO3)Start.getObject()).getSettings().overrideChildSettings && ((BO3)bO3ToCheck.getObject()).getSettings().overrideChildSettings ? ((BO3)Start.getObject()).getSettings().smoothStartWood : ((BO3)bO3ToCheck.getObject()).getSettings().smoothStartWood, ((BO3)Start.getObject()).getSettings().SpawnUnderWater))
		)
    	{
    		return true;
    	}
    	return false;
    }

    private void PlotDiagonalLine(Map<ChunkCoordinate, ArrayList<Object[]>> smoothToBlocksPerChunk, Object[] blockCoordsAndNeighbours)
    {
        Map<ChunkCoordinate, ArrayList<Object[]>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesn't exactly
        // make the code any easier to read..

        int normalizedSmoothFinalEndPointBlockX1;
        int normalizedSmoothFinalEndPointBlockY1 = -1;
        int normalizedSmoothFinalEndPointBlockZ1;
        int normalizedSmoothEndPointBlockX;
        int normalizedSmoothEndPointBlockZ;
        ChunkCoordinate destinationChunk;
        int beginPointX;
        int beginPointY;
        int beginPointZ;
        ArrayList<Object[]> beginningAndEndpoints;
        ChunkCoordinate chunkcontainingSmoothArea;
        ArrayList<Object[]> beginAndEndPoints;

        CustomObjectCoordinate bO3 = (CustomObjectCoordinate)blockCoordsAndNeighbours[0];
        int blockX = (Integer)blockCoordsAndNeighbours[1];
        int blockY = (Integer)blockCoordsAndNeighbours[2];
        int blockZ = (Integer)blockCoordsAndNeighbours[3];
        boolean smoothInDirection1 = (Boolean)blockCoordsAndNeighbours[4];
        boolean smoothInDirection2 = (Boolean)blockCoordsAndNeighbours[5];
        boolean smoothInDirection3 = (Boolean)blockCoordsAndNeighbours[6];
        boolean smoothInDirection4 = (Boolean)blockCoordsAndNeighbours[7];
        int smoothRadius = (Integer)blockCoordsAndNeighbours[8];
       	int smoothRadiusDiagonal = (Integer)blockCoordsAndNeighbours[9];

        // Find smooth end point and normalize coord
        // Add each chunk between the smooth-beginning and end points to a list along with the line-segment information (startcoords in chunk, endcoords in chunk, originCoords, finaldestinationCoords)
        // Later when a chunk is being spawned the list is consulted in order to merge all smoothing lines into 1 smoothing area for the chunk.
        // Note: Unfortunately we can only find x and z coordinates for the smoothing lines at this point. In order to find the Y endpoint
        // for a smoothing line we need the landscape to be spawned so that we can find the highest solid block in the landscape.
        // This problem is handled later during spawning, if the Y endpoint for a smoothing line in a chunk is not available when that chunk
        // is being spawned (because the endpoint is in a neighbouring chunk that has not yet been spawned) then all spawning for the chunk is paused until the Y endpoint is available (the neighbouring chunk has spawned).

        // If this block is an outer corner block (it has the smoothInDirection boolean set to true for 2 neighbouring sides)
        if(smoothInDirection1 && smoothInDirection3)// && (smoothRadiusDiagonal > 0 || test))
        {
        	normalizedSmoothFinalEndPointBlockX1 = blockX - smoothRadiusDiagonal + (bO3.getX());
        	normalizedSmoothFinalEndPointBlockZ1 = blockZ - smoothRadiusDiagonal + (bO3.getZ());
            // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas

            beginPointX = blockX + bO3.getX();
            beginPointY = blockY + bO3.getY();
            beginPointZ = blockZ + bO3.getZ();

            // First get all chunks between the beginning- and end-points
            for(int i = 0; i <= smoothRadiusDiagonal; i++)
            {
            	normalizedSmoothEndPointBlockX = blockX - i + (bO3.getX());
                normalizedSmoothEndPointBlockZ = blockZ - i + (bO3.getZ());
                destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

            	ChunkCoordinate nextBlocksChunkCoord = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX - 1, normalizedSmoothEndPointBlockZ - 1);

            	// only store the line once it's traversed an entire chunk or is at smoothRadiusDiagonal
            	if(!destinationChunk.equals(nextBlocksChunkCoord) || i == smoothRadiusDiagonal)
            	{
                    beginningAndEndpoints = new ArrayList<Object[]> ();
                    beginningAndEndpoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });

                    // Check if there are already start and endpoints for this chunk
                    for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                    {
                        chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                        if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                        {
                            beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                            beginAndEndPoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });
                            break;
                        }
                    }
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);

                    // Get the coordinates of the beginning point for the next entry
                    beginPointX = normalizedSmoothEndPointBlockX - 1;
                    beginPointZ = normalizedSmoothEndPointBlockZ - 1;
            	}
            }
        }
        if(smoothInDirection1 && smoothInDirection4)// && (smoothRadiusDiagonal > 0 || test))
        {
        	normalizedSmoothFinalEndPointBlockX1 = blockX - smoothRadiusDiagonal + (bO3.getX());
        	normalizedSmoothFinalEndPointBlockZ1 = blockZ + smoothRadiusDiagonal + (bO3.getZ());
            // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas

            beginPointX = blockX + bO3.getX();
            beginPointY = blockY + bO3.getY();
            beginPointZ = blockZ + bO3.getZ();

            // First get all chunks between the beginning- and end-points
            for(int i = 0; i <= smoothRadiusDiagonal; i++)
            {
            	normalizedSmoothEndPointBlockX = blockX - i + (bO3.getX());
                normalizedSmoothEndPointBlockZ = blockZ + i + (bO3.getZ());
                destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

            	ChunkCoordinate nextBlocksChunkCoord = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX - 1, normalizedSmoothEndPointBlockZ + 1);

            	// only store the line once it's traversed an entire chunk or is at smoothRadiusDiagonal
            	if(!destinationChunk.equals(nextBlocksChunkCoord) || i == smoothRadiusDiagonal)
            	{
                    beginningAndEndpoints = new ArrayList<Object[]> ();
                    beginningAndEndpoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });

                    // Check if there are already start and endpoints for this chunk
                    for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                    {
                        chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                        if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                        {
                            beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                            beginAndEndPoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });
                            break;
                        }
                    }
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);

                    // Get the coordinates of the beginning point for the next entry
                    beginPointX = normalizedSmoothEndPointBlockX - 1;
                    beginPointZ = normalizedSmoothEndPointBlockZ + 1;
            	}
            }
        }
        if(smoothInDirection2 && smoothInDirection3)// && (smoothRadiusDiagonal > 0 || test))
        {
        	normalizedSmoothFinalEndPointBlockX1 = blockX + smoothRadiusDiagonal + (bO3.getX());
        	normalizedSmoothFinalEndPointBlockZ1 = blockZ - smoothRadiusDiagonal + (bO3.getZ());
            // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas

            beginPointX = blockX + bO3.getX();
            beginPointY = blockY + bO3.getY();
            beginPointZ = blockZ + bO3.getZ();

            // First get all chunks between the beginning- and end-points
            for(int i = 0; i <= smoothRadiusDiagonal; i++)
            {
            	normalizedSmoothEndPointBlockX = blockX + i + (bO3.getX());
                normalizedSmoothEndPointBlockZ = blockZ - i + (bO3.getZ());
                destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

            	ChunkCoordinate nextBlocksChunkCoord = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX + 1, normalizedSmoothEndPointBlockZ - 1);

            	// only store the line once it's traversed an entire chunk or is at smoothRadiusDiagonal
            	if(!destinationChunk.equals(nextBlocksChunkCoord) || i == smoothRadiusDiagonal)
            	{
                    beginningAndEndpoints = new ArrayList<Object[]> ();
                    beginningAndEndpoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });

                    // Check if there are already start and endpoints for this chunk
                    for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                    {
                        chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                        if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                        {
                            beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                            beginAndEndPoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });
                            break;
                        }
                    }
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);

                    // Get the coordinates of the beginning point for the next entry
                    beginPointX = normalizedSmoothEndPointBlockX + 1;
                    beginPointZ = normalizedSmoothEndPointBlockZ - 1;
            	}
            }
        }
        if(smoothInDirection2 && smoothInDirection4)// && (smoothRadiusDiagonal > 0 || test))
        {
        	normalizedSmoothFinalEndPointBlockX1 = blockX + smoothRadiusDiagonal + (bO3.getX());
        	normalizedSmoothFinalEndPointBlockZ1 = blockZ + smoothRadiusDiagonal + (bO3.getZ());
            // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas

            beginPointX = blockX + bO3.getX();
            beginPointY = blockY + bO3.getY();
            beginPointZ = blockZ + bO3.getZ();

            // First get all chunks between the beginning- and end-points
            for(int i = 0; i <= smoothRadiusDiagonal; i++)
            {
            	normalizedSmoothEndPointBlockX = blockX + i + (bO3.getX());
                normalizedSmoothEndPointBlockZ = blockZ + i + (bO3.getZ());
                destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

            	ChunkCoordinate nextBlocksChunkCoord = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX + 1, normalizedSmoothEndPointBlockZ + 1);

            	// only store the line once it's traversed an entire chunk or is at smoothRadiusDiagonal
            	if(!destinationChunk.equals(nextBlocksChunkCoord) || i == smoothRadiusDiagonal)
            	{
                    beginningAndEndpoints = new ArrayList<Object[]> ();
                    beginningAndEndpoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });

                    // Check if there are already start and endpoints for this chunk
                    for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                    {
                        chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                        if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                        {
                            beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                            beginAndEndPoints.add(new Object[] { beginPointX, beginPointY, beginPointZ, normalizedSmoothEndPointBlockX, beginPointY, normalizedSmoothEndPointBlockZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 });
                            break;
                        }
                    }
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);

                    // Get the coordinates of the beginning point for the next entry
                    beginPointX = normalizedSmoothEndPointBlockX + 1;
                    beginPointZ = normalizedSmoothEndPointBlockZ + 1;
            	}
            }
        }

        // Now use each block we've just plotted as the start point for a new line

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesnt exactly
        // make the code any easier to read..
        int distanceFromStart;
        BlockFunction beginPoint;
        int originPointX;
        int originPointY;
        int originPointZ;
        int finalDestinationPointX;
        int finalDestinationPointY;
        int finalDestinationPointZ;
        BlockFunction endPoint;
        BlockFunction filler;
        ArrayList<Object[]> smoothToBlocks;

        int diagonalBlockSmoothRadius = 0;
        int diagonalBlockSmoothRadius2 = 0;

        for(Entry<ChunkCoordinate, ArrayList<Object[]>> smoothingAreaInChunk : smoothingAreasToSpawn.entrySet())
        {
	        for(Object[] smoothingBeginAndEndPoints : smoothingAreaInChunk.getValue())
	        {
	            distanceFromStart = 0;

	            beginPoint = new BlockFunction();
	            beginPoint.x = (Integer)smoothingBeginAndEndPoints[0];
	            beginPoint.y = (Integer)smoothingBeginAndEndPoints[1];
	            beginPoint.z = (Integer)smoothingBeginAndEndPoints[2];

                endPoint = new BlockFunction();
                endPoint.x = (Integer)smoothingBeginAndEndPoints[3];
                endPoint.y = (Integer)smoothingBeginAndEndPoints[4];
                endPoint.z = (Integer)smoothingBeginAndEndPoints[5];

	            originPointX = (Integer)smoothingBeginAndEndPoints[6];
	            originPointY = (Integer)smoothingBeginAndEndPoints[7];
	            originPointZ = (Integer)smoothingBeginAndEndPoints[8];

	            finalDestinationPointX = (Integer)smoothingBeginAndEndPoints[9];
	            finalDestinationPointY = (Integer)smoothingBeginAndEndPoints[10];
	            finalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[11];

                diagonalBlockSmoothRadius = smoothRadius;

	        	distanceFromStart = Math.abs(beginPoint.x - originPointX);

	            // Corners call this method for every diagonal block that makes up the corner, every diagonal block spawns
	        	// its own child blocks seperately in x and y directions which creates the shape of the corner
	            for(int i = 0; i <= Math.abs((beginPoint.z) - (endPoint.z)); i++)
	            {
	                filler = new BlockFunction();
	                if(smoothInDirection2)
	                {
	                    filler.x = beginPoint.x + i;
	                }
	                if(smoothInDirection1)
	                {
	                    filler.x = beginPoint.x - i;
	                }
	                if(smoothInDirection4)
	                {
	                    filler.z = beginPoint.z + i;
	                }
	                if(smoothInDirection3)
	                {
	                    filler.z = beginPoint.z - i;
	                }
	                filler.y = beginPoint.y;

	                smoothToBlocks = new ArrayList<Object[]>();
	                bO3 = new CustomObjectCoordinate(World, null, null, null, 0, 0, 0, false, 0, false, false, null);

	        		// While drawing a circle:
	        		// x^2 + y^2 = r^2
	        		// so y^2 = r^2 - x^2

	                // Circle / round corners
	                diagonalBlockSmoothRadius2 = (int)Math.round(Math.sqrt((diagonalBlockSmoothRadius * diagonalBlockSmoothRadius) - ((distanceFromStart + i) * (distanceFromStart + i))) - (distanceFromStart + i));

	                // Square / square corners
	                //diagonalBlockSmoothRadius2 = diagonalBlockSmoothRadius - (distanceFromStart + i);

	                destinationChunk = ChunkCoordinate.fromBlockCoords(beginPoint.x, endPoint.x);

	                //smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, false, false, false, false, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });

	                if(smoothInDirection1)
	                {
	                	smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, true, false, false, false, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });
	                }
	                if(smoothInDirection2)
	                {
	                	smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, false, true, false, false, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });
	                }
	                if(smoothInDirection3)
	                {
	                	smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, false, false, true, false, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });
	                }
	                if(smoothInDirection4)
	                {
	                	smoothToBlocks.add(new Object[]{ bO3, filler.x, filler.y, filler.z, false, false, false, true, diagonalBlockSmoothRadius2, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ });
	                }

                    if(!smoothToBlocksPerChunk.containsKey(destinationChunk))
                    {
                    	smoothToBlocksPerChunk.put(destinationChunk, smoothToBlocks);
                    } else {
                        // only happens in chunks that have horizontal/vertical lines as well as diagonal ones
                    	smoothToBlocksPerChunk.get(destinationChunk).addAll(smoothToBlocks);
                    }
	            }
	        }
        }
    }

    // We've determined starting points, smooth direction and smooth radius for lines that will form a smoothing area, now find the end point for each line depending on the smoothradius and smooth direction.
    // The lines we plot this way may traverse several chunks so divide them up into segments of one chunk and make a collection of line segments per chunk.
    // For each line-segment store the beginning and endpoints within the chunk as well as the origin coordinate of the line and the final destination coordinate of the line
    // we spawn the line-segments per chunk we can still see what the completed line looks like and how far along that line we are.
    private Map<ChunkCoordinate, ArrayList<Object[]>> CalculateBeginAndEndPointsPerChunk(Map<ChunkCoordinate, ArrayList<Object[]>> smoothToBlocksPerChunk)
    {
        Map<ChunkCoordinate, ArrayList<Object[]>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesn't exactly
        // make the code any easier to read..
        CustomObjectCoordinate bO3;
        int blockX;
        int blockY;
        int blockZ;
        boolean smoothInDirection1;
        boolean smoothInDirection2;
        boolean smoothInDirection3;
        boolean smoothInDirection4;
        int smoothRadius;
        int normalizedSmoothFinalEndPointBlockX1;
        int normalizedSmoothFinalEndPointBlockY1;
        int normalizedSmoothFinalEndPointBlockZ1;
        ChunkCoordinate finalDestinationChunk;
        ArrayList<ChunkCoordinate>smoothingAreasToSpawnForThisBlock;
        int normalizedSmoothEndPointBlockX;
        int normalizedSmoothEndPointBlockY;
        int normalizedSmoothEndPointBlockZ;
        ChunkCoordinate destinationChunk;
        boolean bFound;
        int beginPointX;
        int beginPointY;
        int beginPointZ;
        int endPointX;
        int endPointY;
        int endPointZ;
        ArrayList<Object[]> beginningAndEndpoints;
        ChunkCoordinate chunkcontainingSmoothArea;
        ArrayList<Object[]> beginAndEndPoints;

        int originPointX = 0;
		int originPointY = 0;
		int originPointZ = 0;
		int finalDestinationPointX = 0;
		int finalDestinationPointY = 0;
		int finalDestinationPointZ = 0;
        Object[] objectToAdd;

        // Loop through smooth-line starting blocks
        for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkCoordSet : smoothToBlocksPerChunk.entrySet())
        {
            for(Object[] blockCoordsAndNeighbours : chunkCoordSet.getValue())
            {
                bO3 = (CustomObjectCoordinate)blockCoordsAndNeighbours[0];
                blockX = (Integer)blockCoordsAndNeighbours[1];
                blockY = (Integer)blockCoordsAndNeighbours[2];
                blockZ = (Integer)blockCoordsAndNeighbours[3];
                smoothInDirection1 = (Boolean)blockCoordsAndNeighbours[4];
                smoothInDirection2 = (Boolean)blockCoordsAndNeighbours[5];
                smoothInDirection3 = (Boolean)blockCoordsAndNeighbours[6];
                smoothInDirection4 = (Boolean)blockCoordsAndNeighbours[7];
                smoothRadius = (Integer)blockCoordsAndNeighbours[8];

                // used for diagonal line child lines that make up corners
                if(blockCoordsAndNeighbours.length > 14)
                {
	                originPointX = (Integer)blockCoordsAndNeighbours[9];
	        		originPointY = (Integer)blockCoordsAndNeighbours[10];
					originPointZ = (Integer)blockCoordsAndNeighbours[11];
					finalDestinationPointX = (Integer)blockCoordsAndNeighbours[12];
					finalDestinationPointY = (Integer)blockCoordsAndNeighbours[13];
					finalDestinationPointZ = (Integer)blockCoordsAndNeighbours[14];
                }

                // Find smooth end point and normalize coord
                // Add each chunk between the smooth-beginning and end points to a list along with the line-segment information (startcoords in chunk, endcoords in chunk, originCoords, finaldestinationCoords)
                // Later when a chunk is being spawned the list is consulted in order to merge all smoothing lines into 1 smoothing area for the chunk.
                // Note: Unfortunately we can only find x and z coordinates for the smoothing lines at this point. In order to find the Y endpoint
                // for a smoothing line we need the landscape to be spawned so that we can find the highest solid block in the landscape.
                // This problem is handled later during spawning, if the Y endpoint for a smoothing line in a chunk is not available when that chunk
                // is being spawned (because the endpoint is in a neighbouring chunk that has not yet been spawned) then all spawning for the chunk is paused until the Y endpoint is available (the neighbouring chunk has spawned).

                if(smoothRadius == 0 && blockCoordsAndNeighbours.length < 15)
                {
                	//throw new RuntimeException();
                }

                // If this block is a non-outer-corner block (it does not have the smoothInDirection boolean set to true for 2 neighbouring sides)
                if(smoothInDirection1)// && (smoothRadius > 0 || test || blockCoordsAndNeighbours.length > 14))
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX - smoothRadius + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;

                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                    for(int i = 0; i <= smoothRadius; i++)
                    {
                    	normalizedSmoothEndPointBlockX = blockX - i + (bO3.getX());
                        normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                        normalizedSmoothEndPointBlockZ = blockZ + (bO3.getZ());
                        destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                        // Check if we havent handled this chunk yet for the current line
                        bFound = false;
                        for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                        {
                            if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                            beginPointX = normalizedSmoothEndPointBlockX;
                            beginPointY = normalizedSmoothEndPointBlockY;
                            beginPointZ = normalizedSmoothEndPointBlockZ;

                            endPointX = normalizedSmoothEndPointBlockX;
                            endPointY = normalizedSmoothEndPointBlockY;
                            endPointZ = normalizedSmoothEndPointBlockZ;

                            if(finalDestinationChunk.getChunkX() != destinationChunk.getChunkX() || finalDestinationChunk.getChunkZ() != destinationChunk.getChunkZ())
                            {
                                // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (0 because we're moving in the - direction)
                                endPointX = destinationChunk.getChunkX() * 16;
                            } else {
                                // Get the endpoint by adding the remaining smoothRadius
                                endPointX = normalizedSmoothEndPointBlockX -= (smoothRadius - i);
                            }

                            beginningAndEndpoints = new ArrayList<Object[]> ();
                            if(blockCoordsAndNeighbours.length > 14)
                            {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                            } else {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                            }

                        	beginningAndEndpoints.add(objectToAdd);

                            // Check if there are already start and endpoints for this chunk
                            for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                            {
                                chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                                if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                                {
                                    bFound = true;
                                    beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                    beginAndEndPoints.add(objectToAdd);
                                    break;
                                }
                            }
                            if(!bFound)
                            {
                                smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                            }
                            smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                        }
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }
                if(smoothInDirection2)// && (smoothRadius > 0 || test || blockCoordsAndNeighbours.length > 14))
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX + smoothRadius + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;
                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                    for(int i = 0; i <= smoothRadius; i++)
                    {
                    	normalizedSmoothEndPointBlockX = blockX + i + (bO3.getX());
                        normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                        normalizedSmoothEndPointBlockZ = blockZ + (bO3.getZ());
                        destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                        // Check if we havent handled this chunk yet for the current line
                        bFound = false;
                        for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                        {
                            if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                            beginPointX = normalizedSmoothEndPointBlockX;
                            beginPointY = normalizedSmoothEndPointBlockY;
                            beginPointZ = normalizedSmoothEndPointBlockZ;

                            endPointX = normalizedSmoothEndPointBlockX;
                            endPointY = normalizedSmoothEndPointBlockY;
                            endPointZ = normalizedSmoothEndPointBlockZ;

                            if(finalDestinationChunk.getChunkX() != destinationChunk.getChunkX() || finalDestinationChunk.getChunkZ() != destinationChunk.getChunkZ())
                            {
                                // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (15 because we're moving in the + direction)
                                endPointX = destinationChunk.getChunkX() * 16 + 15;
                            } else {
                                // Get the endpoint by adding the remaining smoothRadius
                                endPointX = normalizedSmoothEndPointBlockX += (smoothRadius - i);
                            }

                            beginningAndEndpoints = new ArrayList<Object[]> ();
                            if(blockCoordsAndNeighbours.length > 14)
                            {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                            } else {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                            }

                        	beginningAndEndpoints.add(objectToAdd);

                            // Check if there are already start and endpoints for this chunk
                            for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                            {
                                chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                                if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                                {
                                    bFound = true;
                                    beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                    beginAndEndPoints.add(objectToAdd);
                                    break;
                                }
                            }
                            if(!bFound)
                            {
                            	smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                            }
                            smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                        }
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }
                if(smoothInDirection3)// && (smoothRadius > 0 || test || blockCoordsAndNeighbours.length > 14))
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ - smoothRadius + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;
                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                    for(int i = 0; i <= smoothRadius; i++)
                    {
                    	normalizedSmoothEndPointBlockX = blockX + (bO3.getX());
                        normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                        normalizedSmoothEndPointBlockZ = blockZ - i + (bO3.getZ());
                        destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                        // Check if we havent handled this chunk yet for the current line
                        bFound = false;
                        for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                        {
                            if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                            beginPointX = normalizedSmoothEndPointBlockX;
                            beginPointY = normalizedSmoothEndPointBlockY;
                            beginPointZ = normalizedSmoothEndPointBlockZ;

                            endPointX = normalizedSmoothEndPointBlockX;
                            endPointY = normalizedSmoothEndPointBlockY;
                            endPointZ = normalizedSmoothEndPointBlockZ;

                            if(finalDestinationChunk.getChunkX() != destinationChunk.getChunkX() || finalDestinationChunk.getChunkZ() != destinationChunk.getChunkZ())
                            {
                                // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (0 because we're moving in the - direction)
                                endPointZ = destinationChunk.getChunkZ() * 16;
                            } else {
                                // Get the endpoint by adding the remaining smoothRadius
                                endPointZ = normalizedSmoothEndPointBlockZ -= (smoothRadius - i);
                            }

                            beginningAndEndpoints = new ArrayList<Object[]> ();
                            if(blockCoordsAndNeighbours.length > 14)
                            {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                            } else {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                            }

                        	beginningAndEndpoints.add(objectToAdd);

                            // Check if there are already start and endpoints for this chunk
                            for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                            {
                                chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                                if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                                {
                                    bFound = true;
                                    beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                    beginAndEndPoints.add(objectToAdd);
                                    break;
                                }
                            }
                            if(!bFound)
                            {
                            	smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                            }
                            smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                        }
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }
                if(smoothInDirection4)// && (smoothRadius > 0 || test || blockCoordsAndNeighbours.length > 14))
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ + smoothRadius + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;

                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                    for(int i = 0; i <= smoothRadius; i++)
                    {
                    	normalizedSmoothEndPointBlockX = blockX + (bO3.getX());
                        normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                        normalizedSmoothEndPointBlockZ = blockZ + i + (bO3.getZ());
                        destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                        // Check if we havent handled this chunk yet for the current line
                        bFound = false;
                        for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                        {
                            if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                            beginPointX = normalizedSmoothEndPointBlockX;
                            beginPointY = normalizedSmoothEndPointBlockY;
                            beginPointZ = normalizedSmoothEndPointBlockZ;

                            endPointX = normalizedSmoothEndPointBlockX;
                            endPointY = normalizedSmoothEndPointBlockY;
                            endPointZ = normalizedSmoothEndPointBlockZ;

                            if(finalDestinationChunk.getChunkX() != destinationChunk.getChunkX() || finalDestinationChunk.getChunkZ() != destinationChunk.getChunkZ())
                            {
                                // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (15 because we're moving in the + direction)
                                endPointZ = destinationChunk.getChunkZ() * 16 + 15;
                            } else {
                                // Get the endpoint by adding the remaining smoothRadius
                                endPointZ = normalizedSmoothEndPointBlockZ += (smoothRadius - i);
                            }

                            beginningAndEndpoints = new ArrayList<Object[]> ();
                            if(blockCoordsAndNeighbours.length > 14)
                            {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                            } else {
                            	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                            }

                        	beginningAndEndpoints.add(objectToAdd);

                            // Check if there are already start and endpoints for this chunk
                            for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                            {
                                chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                                if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                                {
                                    bFound = true;
                                    beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                    beginAndEndPoints.add(objectToAdd);
                                    break;
                                }
                            }
                            if(!bFound)
                            {
                                smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                            }
                            smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                        }
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }

                if(!smoothInDirection1 && !smoothInDirection2 && !smoothInDirection3 && !smoothInDirection4)
                {
                	normalizedSmoothFinalEndPointBlockX1 = blockX + (bO3.getX());
                	normalizedSmoothFinalEndPointBlockZ1 = blockZ + (bO3.getZ());
                    // normalizedSmoothFinalEndPointBlockY1 will be detected later while spawning smooth areas
                	normalizedSmoothFinalEndPointBlockY1 = -1;

                    finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockZ1);

                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();

                    // First get all chunks between the beginning- and end-points
                	normalizedSmoothEndPointBlockX = blockX + (bO3.getX());
                    normalizedSmoothEndPointBlockY = blockY + bO3.getY();
                    normalizedSmoothEndPointBlockZ = blockZ + (bO3.getZ());
                    destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothEndPointBlockX, normalizedSmoothEndPointBlockZ);

                    // Check if we havent handled this chunk yet for the current line
                    bFound = false;
                    for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
                    {
                        if(destinationChunk.getChunkX() == cCoord.getChunkX() && destinationChunk.getChunkZ() == cCoord.getChunkZ())
                        {
                            bFound = true;
                            break;
                        }
                    }
                    if(!bFound)
                    {
                        // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                        beginPointX = normalizedSmoothEndPointBlockX;
                        beginPointY = normalizedSmoothEndPointBlockY;
                        beginPointZ = normalizedSmoothEndPointBlockZ;

                        endPointX = normalizedSmoothEndPointBlockX;
                        endPointY = normalizedSmoothEndPointBlockY;
                        endPointZ = normalizedSmoothEndPointBlockZ;

                        beginningAndEndpoints = new ArrayList<Object[]> ();
                        if(blockCoordsAndNeighbours.length > 14)
                        {
                        	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), -1, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1, originPointX + (bO3.getX()), originPointY, originPointZ + (bO3.getZ()), finalDestinationPointX + (bO3.getX()), finalDestinationPointY, finalDestinationPointZ + (bO3.getZ()) };
                        } else {
                        	objectToAdd = new Object[] { beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, blockX + (bO3.getX()), beginPointY, blockZ + (bO3.getZ()), normalizedSmoothFinalEndPointBlockX1, normalizedSmoothFinalEndPointBlockY1, normalizedSmoothFinalEndPointBlockZ1 };
                        }

                        if(normalizedSmoothFinalEndPointBlockY1 != -1)
                        {
                        	throw new RuntimeException();
                        }

                    	beginningAndEndpoints.add(objectToAdd);

                        // Check if there are already start and endpoints for this chunk
                        for(Entry<ChunkCoordinate, ArrayList<Object[]>> chunkcontainingSmoothAreaSet : smoothingAreasToSpawn.entrySet())
                        {
                            chunkcontainingSmoothArea = chunkcontainingSmoothAreaSet.getKey();
                            if(chunkcontainingSmoothArea.getChunkX() == destinationChunk.getChunkX() && chunkcontainingSmoothArea.getChunkZ() == destinationChunk.getChunkZ())
                            {
                                bFound = true;
                                beginAndEndPoints = chunkcontainingSmoothAreaSet.getValue();
                                beginAndEndPoints.add(objectToAdd);
                                break;
                            }
                        }
                        if(!bFound)
                        {
                            smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                        }
                        smoothingAreasToSpawnForThisBlock.add(destinationChunk);
                    }
                    smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
                }
            }
        }
        return smoothingAreasToSpawn;
    }

    public static class BranchDataItemCounter
    {
    	private static int branchDataItemCounter = -1;
    }

    public class BranchDataItem
    {
    	boolean wasDeleted = false;

    	boolean isBeingRolledBack = false;

    	int branchNumber = -1;

    	boolean MinimumSize = false;

    	public CustomObjectCoordinate Branch;
    	public ChunkCoordinate ChunkCoordinate;

    	public BranchDataItem Parent;
    	public boolean DoneSpawning = false;
    	public boolean SpawnDelayed = false;
    	public boolean CannotSpawn = false;

    	int CurrentDepth = 0;
    	int MaxDepth = 0;

    	LocalWorld World;
    	Random Random;

    	private Stack<BranchDataItem> Children = new Stack<BranchDataItem>();
    	public Stack<BranchDataItem> getChildren(boolean dontSpawn)
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
    		    			if(OTG.getPluginConfig().SpawnLog)
    		    			{
    		    				OTG.log(LogMarker.INFO, "canOverride optional branches cannot be in a branch group, ignoring branch: " + childBO3.getName() + " in BO3: " + Branch.BO3Name);
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

    	public BranchDataItem()
    	{
    		throw new RuntimeException();
    	}

    	public BranchDataItem(LocalWorld world, Random random, BranchDataItem parent, CustomObjectCoordinate branch, String startBO3Name, int currentDepth, int maxDepth, boolean minimumSize)
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

    		BranchDataItemCounter.branchDataItemCounter += 1; // TODO: Reset this somewhere for each new world created?
    		branchNumber = BranchDataItemCounter.branchDataItemCounter;
    	}
    }

    public Stack<BranchDataItem> AllBranchesBranchData = new Stack<BranchDataItem>();
    public HashMap<ChunkCoordinate, Stack<BranchDataItem>> AllBranchesBranchDataByChunk = new HashMap<ChunkCoordinate, Stack<BranchDataItem>>();
    public HashSet<Integer> AllBranchesBranchDataHash = new HashSet<Integer>();
    private boolean ProcessingDone = false;
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
    	while(!ProcessingDone)
    	{
    		SpawnedBranchLastCycle = SpawnedBranchThisCycle;
    		SpawnedBranchThisCycle = false;

    		Cycle += 1;

    		if(OTG.getPluginConfig().SpawnLog)
    		{
    			OTG.log(LogMarker.INFO, "");
    			OTG.log(LogMarker.INFO, "---- Cycle " + Cycle + " ----");
    		}

    		TraverseAndSpawnChildBranches(branchData, minimumSize);

			if(OTG.getPluginConfig().SpawnLog)
			{
				OTG.log(LogMarker.INFO, "All branch groups with required branches only have been processed for cycle " + Cycle + ", plotting branch groups with optional branches.");
			}
			SpawningRequiredBranchesOnly = false;
			TraverseAndSpawnChildBranches(branchData, minimumSize);
			SpawningRequiredBranchesOnly = true;

            ProcessingDone = true;
            for(BranchDataItem branchDataItem3 : AllBranchesBranchData)
            {
            	if(!branchDataItem3.DoneSpawning)
            	{
            		ProcessingDone = false;
            		break;
            	}
            }

        	// CanOverride optional branches are spawned only after the main structure has spawned.
        	// This is useful for knocking out walls between rooms and adding interiors.
            if(ProcessingDone && !canOverrideBranchesSpawned)
            {
            	canOverrideBranchesSpawned = true;
            	SpawningCanOverrideBranches = true;
	            ProcessingDone = false;
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
    				OTG.log(LogMarker.INFO, "Error: Branching BO3 " + Start.BO3Name + " could not be spawned in minimum configuration (isRequiredBranch branches only).");
            		throw new InvalidConfigException("");
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
    boolean SpawningRequiredBranchesOnly = true;
    boolean SpawnedBranchThisCycle = false;
    boolean SpawnedBranchLastCycle = false;
    private void TraverseAndSpawnChildBranches(BranchDataItem branchData, boolean minimumSize)
    {
    	if(!branchData.DoneSpawning)
    	{
    		AddBranches(branchData, minimumSize, false);
    	} else {
    		if(!branchData.CannotSpawn)
    		{
    			for(BranchDataItem branchDataItem2 : branchData.getChildren(false))
    			{
    				// BranchData.DoneSpawning can be set to true by a child branch
    				// that tried to spawn but couldnt
    				if(!branchDataItem2.CannotSpawn && branchData.DoneSpawning)
    				{
    					TraverseAndSpawnChildBranches(branchDataItem2, minimumSize);
    				}
    			}
    		}
    	}
    }

    private void AddBranches(BranchDataItem branchDataItem, boolean minimumSize, boolean traverseOnlySpawnedChildren)
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
    	if(!SpawningRequiredBranchesOnly)// || isRollBack)
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
		        			OTG.log(LogMarker.ERROR, "Error: Could not find BO3 file: " + childBranchDataItem.Branch.BO3Name + ".BO3 which is a branch of " + branchDataItem.Branch.BO3Name + ".BO3");
		        		}
        			}

	        		if(childBranchDataItem.DoneSpawning || childBranchDataItem.CannotSpawn)
	        		{
	        			continue;
	        		}

	        		// Before spawning any required branch make sure there are no optional branches in its branch group that haven't tried to spawn yet.
    	        	if(SpawningRequiredBranchesOnly)// && !isRollBack)
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
		        			boolean spawningRequiredBranchesOnly = SpawningRequiredBranchesOnly;
			        		SpawningRequiredBranchesOnly = true;
			        		TraverseAndSpawnChildBranches(childBranchDataItem, minimumSize);
			        		SpawningRequiredBranchesOnly = spawningRequiredBranchesOnly;
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
		        			boolean spawningRequiredBranchesOnly = SpawningRequiredBranchesOnly;
			        		SpawningRequiredBranchesOnly = true;
			        		TraverseAndSpawnChildBranches(childBranchDataItem, minimumSize);
			        		SpawningRequiredBranchesOnly = spawningRequiredBranchesOnly;
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

		            			RollBackBranch(branchDataItem, minimumSize);
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
						            			RollBackBranch(branchDataItem, minimumSize);
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
        		!SpawningRequiredBranchesOnly &&
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
							TraverseAndSpawnChildBranches(childBranchDataItem, minimumSize);
						}
	        		}
	        	}
	        }

	        // When calling AddBranches during a rollback to continue spawning a branch group don't traverse already spawned children (otherwise the branch could spawn children more than once per cycle).
	        if(
        		!traverseOnlySpawnedChildren &&
        		SpawningRequiredBranchesOnly &&
        		!branchDataItem.CannotSpawn
    		)
	        {
	        	for(BranchDataItem childBranchDataItem : branchDataItem.getChildren(false))
	        	{
	        		if(AllBranchesBranchDataHash.contains(childBranchDataItem.branchNumber))
	        		{
						if(childBranchDataItem.Branch.isRequiredBranch)
						{
							TraverseAndSpawnChildBranches(childBranchDataItem, minimumSize);
						}
	        		}
	        	}
	        }
    	}
    }

    private void RollBackBranch(BranchDataItem branchData, boolean minimumSize)
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
    	DeleteBranchChildren(branchData,minimumSize);

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
			    					RollBackBranch(branchDataItem2, minimumSize);
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
			    					RollBackBranch(branchDataItem2, minimumSize);
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
    			RollBackBranch(branchData.Parent, minimumSize);
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
        				if(SpawningRequiredBranchesOnly)
        				{
                			// 1. The branch being rolled back has spawned all its required-only branch groups but not yet its optional branches and one of the required child branches
                			// (that spawn in the same cycle) failed to spawn one of its required children and is rolled back.
            				// AddBranches should be called for the parent of the branch being rolled back and its parent if a branch group failed to spawn (and so on).

                			// Since we're using SpawningRequiredBranchesOnly AddBranches can traverse all child branches without problems.
            				AddBranches(branchData.Parent, minimumSize, false);
        				} else {
        					// 2. During the second phase of a cycle branch groups with optional branches are spawned, the optional branches get a chance to spawn first, after that the
        					// required branches try to spawn, if that fails the branch is rolled back.
        					// 3. A branch was rolled back that was a requirement for another branch (mustbeinside/mustbebelowother), causing the other branch to be rolled back as well.

                			// Since we're not using SpawningRequiredBranchesOnly AddBranches should only traverse child branches for any branches that it spawns from the branch group its re-trying.
        					// Otherwise some branches may have the same children traversed multiple times in a single phase.
            				AddBranches(branchData.Parent, minimumSize, true);
        				}
        			} else {

        				// 4. While spawning required children for an optional branch (SpawningRequiredChildrenForOptionalBranch == true).
            			// AddBranches should be called only for children of the optional branch since they may have multiple required branches in the same branch group.

            			// In this case AddBranches should not set DoneSpawning to true on the branches (unless they have only required branches) to make sure that any optional
            			// branches are spawned in the second phase of the cycle.
        				if(!SpawningRequiredBranchesOnly)
        				{
        					throw new RuntimeException();
        				}

        				SpawningRequiredChildrenForOptionalBranch = false;
            			// Since we're using SpawningRequiredBranchesOnly AddBranches can traverse all child branches without problems.
        				AddBranches(branchData.Parent, minimumSize, false);
        				SpawningRequiredChildrenForOptionalBranch = true;
        			}
    			}
    		}
		}

    	branchData.isBeingRolledBack = false;
    }

    private void DeleteBranchChildren(BranchDataItem branchData, boolean minimumSize)
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
    			DeleteBranchChildren(branchDataItem, minimumSize);
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
			    	    					RollBackBranch(branchDataItem2, minimumSize);
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
											RollBackBranch(branchDataItem2, minimumSize);
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
                	throw new RuntimeException();
                }
            }

            // Spawn smooth areas in this chunk if any exist
            // If SpawnSmoothAreas returns false then spawning has
            // been delayed and should be tried again later.
        	if(!SpawnSmoothAreas(chunkCoordinate)) { return false; }

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
                	throw new RuntimeException();
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
        	if(!SpawnSmoothAreas(chunkCoordinate)) { return false; }
        }

		ObjectsToSpawn.remove(chunkCoordinate);
		SmoothingAreasToSpawn.remove(chunkCoordinate);

        return true;
    }

    /**
     * Merges all the smoothing lines that were plotted earlier into one
     * smoothing area per chunk and then spawns the smoothing area.
     * Returns false if a smoothing area could not be finalised
     * and spawning has to be delayed until other chunks have spawned
     * @param chunkCoordinate
    */
    private boolean SpawnSmoothAreas(ChunkCoordinate chunkCoordinate)
    {
        // Get all smoothing areas (lines) that should spawn in this chunk for this branching structure
        Entry<ChunkCoordinate, ArrayList<Object[]>> smoothingAreaInChunk = null;
        for(Entry<ChunkCoordinate, ArrayList<Object[]>> smoothingAreaToSpawn : SmoothingAreasToSpawn.entrySet())
        {
            if(smoothingAreaToSpawn.getKey().getChunkX() == chunkCoordinate.getChunkX() && smoothingAreaToSpawn.getKey().getChunkZ() == chunkCoordinate.getChunkZ())
            {
                smoothingAreaInChunk = smoothingAreaToSpawn;
                break;
            }
        }

        if(smoothingAreaInChunk != null && smoothingAreaInChunk.getValue() != null)
        {
            // Merge all smooth areas (lines) so that in one x + z coordinate there can be a maximum of 2 smoothing area blocks, 1 going up and 1 going down (first pass and second pass)
            ArrayList<Object[]> blocksToSpawn = MergeSmoothingAreas(chunkCoordinate, smoothingAreaInChunk.getValue());

            // blocksToSpawn can be null if a smoothing line's endpoint Y coordinate could not be found. This can happen if
            // the chunk that the endpoint is located in has not yet been spawned. Return false so that the calling method (SpawnForChunk()) knows
            // that it should delay spawning for this chunk and try again later.
            if(blocksToSpawn == null) { return false; }

        	boolean isOnBiomeBorder = false;

        	LocalBiome biome = World.getBiome(chunkCoordinate.getChunkX() * 16, chunkCoordinate.getChunkZ() * 16);
        	LocalBiome biome2 = World.getBiome(chunkCoordinate.getChunkX() * 16 + 15, chunkCoordinate.getChunkZ() * 16);
        	LocalBiome biome3 = World.getBiome(chunkCoordinate.getChunkX() * 16, chunkCoordinate.getChunkZ() * 16 + 15);
        	LocalBiome biome4 = World.getBiome(chunkCoordinate.getChunkX() * 16 + 15, chunkCoordinate.getChunkZ() * 16 + 15);

            if(!(biome == biome2 && biome == biome3 && biome == biome4))
            {
            	isOnBiomeBorder = true;
            }

            BiomeConfig biomeConfig = biome.getBiomeConfig();

            DefaultMaterial surfaceBlockMaterial = biomeConfig.surfaceBlock.toDefaultMaterial();
            byte surfaceBlockMaterialBlockData = biomeConfig.surfaceBlock.getBlockData();
            DefaultMaterial groundBlockMaterial = biomeConfig.groundBlock.toDefaultMaterial();
            byte groundBlockMaterialBlockData = biomeConfig.groundBlock.getBlockData();

            boolean surfaceBlockSet = false;
			if(((BO3)Start.getObject()).getSettings().smoothingSurfaceBlock != null && ((BO3)Start.getObject()).getSettings().smoothingSurfaceBlock.trim().length() > 0)
			{
				try {
					LocalMaterialData material = OTG.readMaterial(((BO3)Start.getObject()).getSettings().smoothingSurfaceBlock);
					surfaceBlockSet = true;
					surfaceBlockMaterial = material.toDefaultMaterial();
					surfaceBlockMaterialBlockData = material.getBlockData();
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
				}
			}
            boolean groundBlockSet = false;
			if(((BO3)Start.getObject()).getSettings().smoothingGroundBlock != null && ((BO3)Start.getObject()).getSettings().smoothingGroundBlock.trim().length() > 0)
			{
				try
				{
					LocalMaterialData material = OTG.readMaterial(((BO3)Start.getObject()).getSettings().smoothingGroundBlock);
					groundBlockSet = true;
					groundBlockMaterial = material.toDefaultMaterial();
					groundBlockMaterialBlockData = material.getBlockData();
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
				}
			}

            if(surfaceBlockMaterial == null || surfaceBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	surfaceBlockMaterial = DefaultMaterial.GRASS;
            	surfaceBlockMaterialBlockData = 0;
            }

            if(groundBlockMaterial == null || groundBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	groundBlockMaterial = DefaultMaterial.DIRT;
            	groundBlockMaterialBlockData = 0;
            }

            DefaultMaterial replaceAboveMaterial = null;
            byte replaceAboveMaterialBlockData = 0;
            DefaultMaterial replaceBelowMaterial = null;
			if(((BO3)Start.getObject()).getSettings().replaceAbove != null && ((BO3)Start.getObject()).getSettings().replaceAbove.trim().length() > 0)
			{
				try
				{
					LocalMaterialData material = OTG.readMaterial(((BO3)Start.getObject()).getSettings().replaceAbove);
					replaceAboveMaterial = material.toDefaultMaterial();
					replaceAboveMaterialBlockData = material.getBlockData();
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
				}
			}
			if(((BO3)Start.getObject()).getSettings().replaceBelow != null && ((BO3)Start.getObject()).getSettings().replaceBelow.trim().length() > 0)
			{
				try
				{
					LocalMaterialData material = OTG.readMaterial(((BO3)Start.getObject()).getSettings().replaceBelow);
					replaceBelowMaterial = material.toDefaultMaterial();
				}
				catch (InvalidConfigException e)
				{
					e.printStackTrace();
				}
			}

            if(replaceAboveMaterial == null || replaceAboveMaterial == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	replaceAboveMaterial = null;
            	replaceAboveMaterialBlockData = 0;
            }

            if(replaceBelowMaterial == null || replaceBelowMaterial == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	replaceBelowMaterial = null;
            }

            // Declare these here instead of inside for loops to help the GC (good for memory usage)
            // TODO: Find out if this actually makes any noticeable difference, it doesnt exactly
            // make the code any easier to read..
            BlockFunction blockToSpawn;
            boolean goingUp;
            boolean secondPass;
            LocalMaterialData sourceBlockMaterial;
            DefaultMaterial sourceBlockMaterialAbove;
            DefaultMaterial materialToSet = null;
            Byte blockDataToSet = 0;
            boolean bBreak;
            int yStart;
            int yEnd;
            BlockFunction blockToQueueForSpawn = new BlockFunction();


            HashMap<ChunkCoordinate, LocalMaterialData> originalTopBlocks = new HashMap<ChunkCoordinate, LocalMaterialData>();

            // Spawn blocks
            // For each block in the smoothing area replace blocks above and/or below it
            for(Object[] blockItemToSpawn : blocksToSpawn)
            {
                blockToSpawn = (BlockFunction)blockItemToSpawn[0];
                goingUp = (Boolean)blockItemToSpawn[1];
                secondPass =  (Boolean)blockItemToSpawn[3];

                if(blockToSpawn.y > 255)
                {
                	continue; // TODO: prevent this from ever happening!
                }

            	if(!originalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToSpawn.x, blockToSpawn.z)))
            	{
        			int highestBlockY = World.getHighestBlockYAt(blockToSpawn.x, blockToSpawn.z, true, true, false, false);
        			if(highestBlockY > OTG.WORLD_DEPTH)
        			{
        				originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToSpawn.x, blockToSpawn.z), World.getMaterial(blockToSpawn.x, highestBlockY, blockToSpawn.z, IsOTGPlus));
        			} else {
        				originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToSpawn.x, blockToSpawn.z), null);
        			}
            	}

                if(isOnBiomeBorder && (!surfaceBlockSet || !groundBlockSet))
                {
	                biome = World.getBiome(blockToSpawn.x, blockToSpawn.z);
	                biomeConfig = biome.getBiomeConfig();

	                if(!surfaceBlockSet)
	                {
		                surfaceBlockMaterial = biomeConfig.surfaceBlock.toDefaultMaterial();
		                surfaceBlockMaterialBlockData = biomeConfig.surfaceBlock.getBlockData();

		                if(surfaceBlockMaterial == null || surfaceBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
		                {
		                	surfaceBlockMaterial = DefaultMaterial.GRASS;
		                	surfaceBlockMaterialBlockData = 0;
		                }
	                }

	                if(!groundBlockSet)
	                {
		                groundBlockMaterial = biomeConfig.groundBlock.toDefaultMaterial();
		                groundBlockMaterialBlockData = biomeConfig.groundBlock.getBlockData();

		                if(groundBlockMaterial == null || groundBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
		                {
		                	groundBlockMaterial = DefaultMaterial.DIRT;
		                	groundBlockMaterialBlockData = 0;
		                }
	                }
                }

                // If using the biome's surfaceblock then take what was previously the top
                // block and use it's material as the surface block (solves no podzol problem in mega spruce taiga)
                if(
            		!surfaceBlockSet &&
					!(biomeConfig.surfaceAndGroundControl instanceof MesaSurfaceGenerator)
        		)
            	{
        			LocalMaterialData originalSurfaceBlock = originalTopBlocks.get(ChunkCoordinate.fromChunkCoords(blockToSpawn.x, blockToSpawn.z));
        			if(originalSurfaceBlock == null || originalSurfaceBlock.isLiquid() || originalSurfaceBlock.isAir())
        			{
    	                surfaceBlockMaterial = biomeConfig.surfaceBlock.toDefaultMaterial();
    	                surfaceBlockMaterialBlockData = biomeConfig.surfaceBlock.getBlockData();
        			} else {
        				surfaceBlockMaterial = originalSurfaceBlock.toDefaultMaterial();
        				surfaceBlockMaterialBlockData = originalSurfaceBlock.getBlockData();
        			}

                    if(surfaceBlockMaterial == null || surfaceBlockMaterial == DefaultMaterial.UNKNOWN_BLOCK)
                    {
                    	surfaceBlockMaterial = DefaultMaterial.GRASS;
                    	surfaceBlockMaterialBlockData = 0;
                    }
            	}

                bBreak = false;
                // When going down make a hill for the BO3 to stand on
				if(!goingUp)
				{
					yStart = blockToSpawn.y;
					yEnd = 0;
					for(int y = yStart; y > yEnd; y--)
					{
						if(y >= 255){ continue;}

						sourceBlockMaterial = World.getMaterial(blockToSpawn.x, y, blockToSpawn.z, IsOTGPlus);
	                    // When going down don't go lower than the highest solid block
	                    if(sourceBlockMaterial.isSolid() && y < blockToSpawn.y)
	                    {
	                        // Place the current block but abort spawning after that
	                        bBreak = true;
	                    }

	                    if(y == blockToSpawn.y)
	                    {
	                		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, IsOTGPlus).toDefaultMaterial();
	                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
	                		{
	                			materialToSet = surfaceBlockMaterial;
	                			blockDataToSet = surfaceBlockMaterialBlockData;
	                		} else {
	                        	materialToSet = groundBlockMaterial;
	                        	blockDataToSet = groundBlockMaterialBlockData;
	                		}
	                    }
	                    else if(y < blockToSpawn.y)
	                    {
	                    	materialToSet = groundBlockMaterial;
	                    	blockDataToSet = groundBlockMaterialBlockData;
	                    } else {
	                    	throw new RuntimeException();
	                    }

	                    if(materialToSet != null && materialToSet != DefaultMaterial.UNKNOWN_BLOCK)
	                    {
	                        blockToQueueForSpawn = new BlockFunction();
	                        blockToQueueForSpawn.x = blockToSpawn.x;
	                        blockToQueueForSpawn.y = y;
	                        blockToQueueForSpawn.z = blockToSpawn.z;
	                        blockToQueueForSpawn.material = OTG.toLocalMaterialData(materialToSet,blockDataToSet);

	                        // Apply mesa blocks if needed
	                        if(
                        		!blockToQueueForSpawn.material.isAir() &&
                        		!blockToQueueForSpawn.material.isLiquid() &&
                        		biomeConfig.surfaceAndGroundControl != null &&
                				biomeConfig.surfaceAndGroundControl instanceof MesaSurfaceGenerator &&
                        		(
                    				(
                						blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeConfig.groundBlock.toDefaultMaterial()) &&
                						blockToQueueForSpawn.material.getBlockData() == biomeConfig.groundBlock.getBlockData()
            						)
            						||
            						(
        								blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeConfig.surfaceBlock.toDefaultMaterial()) &&
        								blockToQueueForSpawn.material.getBlockData() == biomeConfig.surfaceBlock.getBlockData()
    								)
								)
							)
	                        {
            		        	LocalMaterialData customBlockData = biomeConfig.surfaceAndGroundControl.getCustomBlockData(World, biomeConfig, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
            		        	if(customBlockData != null)
            		        	{
            		        		blockToQueueForSpawn.material = customBlockData;
            		        	}
        		        		setBlock(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag);
	                        } else {
	                        	if (!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
	                        	{
	                        		setBlock(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag);
	                        	}
	                        }
	                    } else {
	                    	throw new RuntimeException();
	                    }
	                    if(bBreak)
	                    {
	                        break;
	                    }
					}
				}
				else if(goingUp)
				{
	                // ReplaceAbove should really be three option setting:
	                // Clear nothing above BO3
	                // Clear all except water/liquid (below or at water level) and fill up with liquid if below water level
	                // Clear all including water/liquid

					if(replaceAboveMaterial == null)
					{
						continue;
					}

					yStart = World.getHighestBlockYAt(blockToSpawn.x,blockToSpawn.z, true, true, false, false);
					yEnd = 0;
					for(int y = yStart; y >= yEnd; y--)
					{
						if(y >= 255){ continue;}

						sourceBlockMaterial = World.getMaterial(blockToSpawn.x, y, blockToSpawn.z, IsOTGPlus);

                    	materialToSet = replaceAboveMaterial;
                    	blockDataToSet = replaceAboveMaterialBlockData;

	                    if(y < blockToSpawn.y)
                    	{
	                    	if(!sourceBlockMaterial.isLiquid() || (secondPass && !((BO3)Start.getObject()).getSettings().SpawnUnderWater))  // If this is the second pass then the first pass went down and we don't have to make a dam, otherwise we do
	                    	{
                    			break;
                    		}
	                    	else if(((BO3)Start.getObject()).getSettings().SpawnUnderWater)
	                    	{
	                    		materialToSet = replaceAboveMaterial; // Replace liquid with replaceAboveMaterial
	                    		blockDataToSet = replaceAboveMaterialBlockData;
	                    	} else {
	                    		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, IsOTGPlus).toDefaultMaterial();
		                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
		                		{
		                			materialToSet = surfaceBlockMaterial;
		                			blockDataToSet = surfaceBlockMaterialBlockData;
		                		} else {
		                        	materialToSet = groundBlockMaterial;
		                        	blockDataToSet = groundBlockMaterialBlockData;
		                		}
	                    	}
	                    }

	                    if(y == blockToSpawn.y)
	                    {
	                    	if(sourceBlockMaterial.isSolid() || (!secondPass && sourceBlockMaterial.isLiquid() && !((BO3)Start.getObject()).getSettings().SpawnUnderWater))
	                    	{
		                		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, IsOTGPlus).toDefaultMaterial();
		                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
		                		{
			                		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, IsOTGPlus).toDefaultMaterial();
			                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
			                		{
			                			materialToSet = surfaceBlockMaterial;
			                			blockDataToSet = surfaceBlockMaterialBlockData;
			                		} else {
			                        	materialToSet = groundBlockMaterial;
			                        	blockDataToSet = groundBlockMaterialBlockData;
			                		}
		                		} else {
		                        	materialToSet = groundBlockMaterial;
		                        	blockDataToSet = groundBlockMaterialBlockData;
		                		}
	                    	} else {
	                    		if(((BO3)Start.getObject()).getSettings().SpawnUnderWater)
		                    	{
		                    		materialToSet = replaceAboveMaterial; // Replace liquid with replaceAboveMaterial
		                    		blockDataToSet = replaceAboveMaterialBlockData;
	                    		} else {
	                    			// After removing layers of blocks replace the heighest block left with the surfaceBlockMaterial
	                    			if(!sourceBlockMaterial.isLiquid() && !sourceBlockMaterial.equals(DefaultMaterial.AIR))
	                    			{
	        	                		sourceBlockMaterialAbove = World.getMaterial(blockToSpawn.x, y + 1, blockToSpawn.z, IsOTGPlus).toDefaultMaterial();
	        	                		if(sourceBlockMaterialAbove == null || sourceBlockMaterialAbove == DefaultMaterial.AIR)
	        	                		{
	        	                			materialToSet = DefaultMaterial.AIR; // Make sure that canyons/caves etc aren't covered
        		                			blockDataToSet = surfaceBlockMaterialBlockData;
	        	                		} else {
	        	                        	materialToSet = groundBlockMaterial;
	        	                        	blockDataToSet = groundBlockMaterialBlockData;
	        	                		}
	                    				bBreak = true;
	                    			} else {
	                    				break;
	                    			}
		                    	}
	                    	}
	                    }

                    	if(materialToSet.isLiquid() && ((BO3)Start.getObject()).getSettings().SpawnUnderWater && y >= (biomeConfig.useWorldWaterLevel ? World.getConfigs().getWorldConfig().waterLevelMax : biomeConfig.waterLevelMax))
                    	{
                    		materialToSet = DefaultMaterial.AIR;
                    		blockDataToSet = 0;
                    	}

	                    if(materialToSet != null && materialToSet != DefaultMaterial.UNKNOWN_BLOCK)
	                    {
	                        blockToQueueForSpawn = new BlockFunction();
	                        blockToQueueForSpawn.x = blockToSpawn.x;
	                        blockToQueueForSpawn.y = y;
	                        blockToQueueForSpawn.z = blockToSpawn.z;
	                        blockToQueueForSpawn.material = OTG.toLocalMaterialData(materialToSet, blockDataToSet);

	                        // Apply mesa blocks if needed
	                        if(
                        		!blockToQueueForSpawn.material.isAir() &&
                        		!blockToQueueForSpawn.material.isLiquid() &&
                        		biomeConfig.surfaceAndGroundControl != null &&
                				biomeConfig.surfaceAndGroundControl instanceof MesaSurfaceGenerator &&
                        		(
                    				(
                						blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeConfig.groundBlock.toDefaultMaterial()) &&
                						blockToQueueForSpawn.material.getBlockData() == biomeConfig.groundBlock.getBlockData()
            						)
            						||
            						(
        								blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeConfig.surfaceBlock.toDefaultMaterial()) &&
        								blockToQueueForSpawn.material.getBlockData() == biomeConfig.surfaceBlock.getBlockData()
    								)
								)
							)
	                        {
            		        	LocalMaterialData customBlockData = biomeConfig.surfaceAndGroundControl.getCustomBlockData(World, biomeConfig, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
            		        	if(customBlockData != null)
            		        	{
            		        		blockToQueueForSpawn.material = customBlockData;
            		        	}
        		        		setBlock(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag);
	                        } else {
	                        	if (!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
	                        	{
	                        		setBlock(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag);
	                        	}
	                        }
	                    } else {
	                    	throw new RuntimeException();
	                    }
	                    if(bBreak)
	                    {
	                        break;
	                    }
					}
				}
            }

            // We'll still be using the chunks that smoothing areas
            // spawn in for chunk based collision detection so keep them
            // but empty them of blocks
            smoothingAreaInChunk.setValue(null);
        }
        return true;
    }

    private void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag)
    {
	    HashMap<DefaultMaterial,LocalMaterialData> blocksToReplace = this.World.getConfigs().getWorldConfig().getReplaceBlocksDict();
	    if(blocksToReplace != null && blocksToReplace.size() > 0)
	    {
	    	LocalMaterialData targetBlock = blocksToReplace.get(material);
	    	if(targetBlock != null)
	    	{
	    		material = targetBlock;
	    	}
	    }
	    World.setBlock(x, y, z, material, metaDataTag, IsOTGPlus);
    }

    private ArrayList<Object[]> MergeSmoothingAreas(ChunkCoordinate chunkCoordinate, ArrayList<Object[]> smoothingAreas)
    {
        ArrayList<Object[]> blocksToSpawn = new ArrayList<Object[]>();

        // Declare these here instead of inside for loops to help the GC (good for memory usage)
        // TODO: Find out if this actually makes any noticeable difference, it doesnt exactly
        // make the code any easier to read..
        boolean goingUp;
        boolean goingDown;

        boolean diagonalLinegoingUp;
        boolean diagonalLinegoingDown;

        int distanceFromStart;
        BlockFunction beginPoint;
        int originPointX;
        int originPointY;
        int originPointZ;
        int finalDestinationPointX;
        int finalDestinationPointY;
        int finalDestinationPointZ;

        int diagonalLineOriginPointX;
        int diagonalLineoriginPointY;
        int diagonalLineOriginPointZ;
        int diagonalLineFinalDestinationPointX;
        int diagonalLineFinalDestinationPointY;
        int diagonalLineFinalDestinationPointZ;

        LocalMaterialData material;
        boolean dontAdd;
        BlockFunction existingBlock;
        BlockFunction endPoint;
        int surfaceBlockHeight;
        BlockFunction filler;
        ArrayList<Object[]> blocksToRemove;

        BlockFunction[] blockColumn = null;
        BlockFunction[] blockColumn2 = null;
        int prevfinalDestinationPointX = 0;
        int prevfinalDestinationPointZ = 0;
        int prevDiagonalLineFinalDestinationPointX = 0;
        int prevDiagonalLineFinalDestinationPointZ = 0;
        boolean isInitialised = false;

        // Check if all smooth areas have been finalized (endpoint y set) for this chunk
        // if so then merge them down to a single smooth area. Otherwise queue them and
        // spawn them later
        for(Object[] smoothingBeginAndEndPoints : smoothingAreas)
        {
        	beginPoint = new BlockFunction();
            beginPoint.x = (Integer)smoothingBeginAndEndPoints[0];
            beginPoint.y = (Integer)smoothingBeginAndEndPoints[1];
            beginPoint.z = (Integer)smoothingBeginAndEndPoints[2];

            originPointX = (Integer)smoothingBeginAndEndPoints[6];
            originPointY = (Integer)smoothingBeginAndEndPoints[7];
            originPointZ = (Integer)smoothingBeginAndEndPoints[8];

        	finalDestinationPointX = (Integer)smoothingBeginAndEndPoints[9];
            finalDestinationPointY = (Integer)smoothingBeginAndEndPoints[10];
            finalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[11];

            diagonalLineOriginPointX = -1;
            diagonalLineoriginPointY = -1;
            diagonalLineOriginPointZ = -1;
            diagonalLineFinalDestinationPointX = -1;
            diagonalLineFinalDestinationPointY = -1;
            diagonalLineFinalDestinationPointZ = -1;

            // if this line is a child line of a diagonal line
        	if(smoothingBeginAndEndPoints.length > 17)
        	{
	            diagonalLineOriginPointX = (Integer)smoothingBeginAndEndPoints[12];
	            diagonalLineoriginPointY = (Integer)smoothingBeginAndEndPoints[13];
	            diagonalLineOriginPointZ = (Integer)smoothingBeginAndEndPoints[14];
	            diagonalLineFinalDestinationPointX = (Integer)smoothingBeginAndEndPoints[15];
	            diagonalLineFinalDestinationPointY = (Integer)smoothingBeginAndEndPoints[16];
	            diagonalLineFinalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[17];

	            // Line has been marked as do not spawn because it crossed another line
	            if(diagonalLineFinalDestinationPointY == -2)
	            {
	            	continue;
	            }
        	}

            if(!isInitialised || (prevfinalDestinationPointX != finalDestinationPointX || prevfinalDestinationPointZ != finalDestinationPointZ))
            {
               	blockColumn = World.getBlockColumn(finalDestinationPointX, finalDestinationPointZ);

            	prevfinalDestinationPointX = finalDestinationPointX;
            	prevfinalDestinationPointX = finalDestinationPointZ;
            }
            if(smoothingBeginAndEndPoints.length > 17)
            {
	            if(!isInitialised || (prevDiagonalLineFinalDestinationPointX != diagonalLineFinalDestinationPointX || prevDiagonalLineFinalDestinationPointZ != diagonalLineFinalDestinationPointZ))
	            {
            		blockColumn2 = World.getBlockColumn(diagonalLineFinalDestinationPointX, diagonalLineFinalDestinationPointZ);
	            	prevDiagonalLineFinalDestinationPointX = diagonalLineFinalDestinationPointX;
	            	prevDiagonalLineFinalDestinationPointZ = diagonalLineFinalDestinationPointZ;
	            }
            }
            isInitialised = true;

            // This is a line plotted as a child line of a diagonal line of which the endpointy has not yet been determined
            if(originPointY == -1 && smoothingBeginAndEndPoints.length > 17)
            {
	            if(diagonalLineFinalDestinationPointY == -1)
	            {
		            material = null;
		            for(int i = 255; i > -1; i--)
		            {
		                // when going down dont stop at the waterline
		                // when going up stop at the waterline
		            	BlockFunction block = blockColumn2[i];
		            	material = block.material;
		                if(
		                    !material.getName().toLowerCase().equals("air") &&
		                    (
		                        (block.y <= diagonalLineoriginPointY && !material.isLiquid()) ||
		                        (block.y > diagonalLineoriginPointY && (!((BO3)Start.getObject()).getSettings().SpawnUnderWater || !material.isLiquid()))
		                    )
		                )
		                {
		                	// TODO: get all blocks using this endpoint and set this Y
		                	diagonalLineFinalDestinationPointY = block.y;
		                    break;
		                }
		            }
	            }

	        	if(diagonalLineFinalDestinationPointY == -1)
	        	{
	        		// TODO: Could this be the cause of the mystery bug that places smoothing areas at y 0?
	        		//OTG.log(LogMarker.INFO, "A smoothing area tried to spawn at Y -1 for structure " + Start.BO3Name + ". If you are creating empty chunks intentionally (for a sky-world for instance) you may wish to disable smoothing areas in your BO3s!");
	        		// Something is wrong!
	        		//throw new RuntimeException();
	        		diagonalLineFinalDestinationPointY = 0;
	        	}

	        	//{
        		diagonalLinegoingDown = false;
        		diagonalLinegoingUp = false;
        		if(diagonalLineoriginPointY >= diagonalLineFinalDestinationPointY)
        		{
        			diagonalLinegoingDown = true;
        		}
        		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
        		{
        			diagonalLinegoingUp = true;
        		}

                // Set diagonal-y-endpoint for all other smoothing area lines that are children of this diagonal line.
                ArrayList<Object[]> smoothingAreasToSpawnPerLineDestination = SmoothingAreasToSpawnPerDiagonalLineDestination.get(ChunkCoordinate.fromChunkCoords(diagonalLineFinalDestinationPointX, diagonalLineFinalDestinationPointZ));
                if(smoothingAreasToSpawnPerLineDestination != null)
                {
	                for(Object[] smoothingBeginAndEndPoints2 : smoothingAreasToSpawnPerLineDestination)
	                {
	                	int diagonalLineFinalOriginPointX2 = (Integer)smoothingBeginAndEndPoints2[12];
	                	int diagonalLineFinalOriginPointZ2 = (Integer)smoothingBeginAndEndPoints2[14];

	                    int diagonalLineFinalDestinationPointY2 = (Integer)smoothingBeginAndEndPoints2[16];

	            		if(
            				diagonalLineOriginPointX == diagonalLineFinalOriginPointX2 && diagonalLineOriginPointZ == diagonalLineFinalOriginPointZ2
        				)
	            		{
	            			if(diagonalLineFinalDestinationPointY2 != -2)
	            			{
		            			smoothingBeginAndEndPoints2[16] = diagonalLineFinalDestinationPointY;
		            		}
	        			}
	                }
                }

                if((Integer)smoothingBeginAndEndPoints[16] != -2)
                {
                	smoothingBeginAndEndPoints[16] = diagonalLineFinalDestinationPointY;
                } else {
                	continue;
                }
            }

        	if(finalDestinationPointY == -1)
        	{
	            material = null;
	            for(int i = 255; i > -1; i--)
	            {
	                // when going down dont stop at the waterline
	                // when going up stop at the waterline
	            	BlockFunction block = blockColumn[i];
	            	material = block.material;

	                if(
	                    !material.getName().toLowerCase().equals("air") &&
	                    (
	                        (
                        		block.y <= (diagonalLineoriginPointY > -1 ? diagonalLineoriginPointY : originPointY) &&
                        		!material.isLiquid()
                    		) ||
	                        (block.y > (diagonalLineoriginPointY > -1 ? diagonalLineoriginPointY : originPointY) && (!((BO3)Start.getObject()).getSettings().SpawnUnderWater || !material.isLiquid()))
	                    )
	                )
	                {
	                	finalDestinationPointY = block.y;

	                	smoothingBeginAndEndPoints[10] = finalDestinationPointY;

    	                // Set y-endpoint for all other smoothing area line-parts that are part of this line

            			ArrayList<Object[]> smoothingAreasForLine = SmoothingAreasToSpawnPerLineOrigin.get(ChunkCoordinate.fromChunkCoords(originPointX, originPointZ));
            			if(smoothingAreasForLine != null)
            			{
		                	for(Object[] smoothingBeginAndEndPoints2 : smoothingAreasForLine)
		                	{
			                	int finalDestinationPointX2 = (Integer)smoothingBeginAndEndPoints2[9];
			                    int finalDestinationPointZ2 = (Integer)smoothingBeginAndEndPoints2[11];

			            		if(finalDestinationPointX == finalDestinationPointX2 && finalDestinationPointZ == finalDestinationPointZ2)
			            		{
		            				smoothingBeginAndEndPoints2[10] = finalDestinationPointY; // - 1; // <-- -1 is a hack because the spawning area endpoints would always spawn 1 block too high
			            		}
	    	                }
            			}
	                    break;
	                }
	            }
        	}

        	// this should no longer be necessary since ForgeWorld has been changed to force chunk
        	// population when height is requested for a block in an unpopulated chunk. TODO: will that work for bukkit too?
            if(finalDestinationPointY == -1)
            {
            	finalDestinationPointY = 0;
            }

            // This is a line plotted as a child line of a diagonal line of which the diagonalendpointy has been determined
            // but the originPointY hasnt
            if((Integer)smoothingBeginAndEndPoints[7] == -1)
            {
	            diagonalLineOriginPointX = (Integer)smoothingBeginAndEndPoints[12];
	            diagonalLineoriginPointY = (Integer)smoothingBeginAndEndPoints[13];
	            diagonalLineFinalDestinationPointX = (Integer)smoothingBeginAndEndPoints[15];
	            diagonalLineFinalDestinationPointY = (Integer)smoothingBeginAndEndPoints[16];

        		originPointY = (int)Math.round(
                    (double)
                    (
                        (double)Math.abs(diagonalLineoriginPointY - diagonalLineFinalDestinationPointY)
                        *
                        (double)((double)Math.abs(diagonalLineOriginPointX - originPointX) / (double)Math.abs(diagonalLineOriginPointX - diagonalLineFinalDestinationPointX))
                    )
                );

        		if(diagonalLineoriginPointY > diagonalLineFinalDestinationPointY)
        		{
        			originPointY = diagonalLineoriginPointY - originPointY;
        		}
        		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
        		{
        			originPointY = diagonalLineoriginPointY + originPointY;
        		} else {
        			originPointY = diagonalLineoriginPointY;
        		}

        		smoothingBeginAndEndPoints[7] = originPointY;
            }
        }

        for(Object[] smoothingBeginAndEndPoints : smoothingAreas)
        {
        	// if this line was set as do not spawn then skip it
        	if(smoothingBeginAndEndPoints.length > 17 && (Integer)smoothingBeginAndEndPoints[16] == -2)
        	{
	            continue;
        	}

            diagonalLinegoingUp = false;
            diagonalLinegoingDown = false;

            goingUp = false;
            goingDown = false;
            distanceFromStart = 0;

            beginPoint = new BlockFunction();
            beginPoint.x = (Integer)smoothingBeginAndEndPoints[0];
            beginPoint.y = (Integer)smoothingBeginAndEndPoints[1];
            beginPoint.z = (Integer)smoothingBeginAndEndPoints[2];

            originPointX = (Integer)smoothingBeginAndEndPoints[6];
            originPointY = (Integer)smoothingBeginAndEndPoints[7];
            originPointZ = (Integer)smoothingBeginAndEndPoints[8];

            finalDestinationPointX = (Integer)smoothingBeginAndEndPoints[9];
            finalDestinationPointY = (Integer)smoothingBeginAndEndPoints[10];
            finalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[11];

            diagonalLineOriginPointX = -1;
            diagonalLineoriginPointY = -1;
            diagonalLineOriginPointZ = -1;
            diagonalLineFinalDestinationPointX = -1;
            diagonalLineFinalDestinationPointY = -1;
            diagonalLineFinalDestinationPointZ = -1;

            if(smoothingBeginAndEndPoints.length > 17)
            {
	            diagonalLineOriginPointX = (Integer)smoothingBeginAndEndPoints[12];
	            diagonalLineoriginPointY = (Integer)smoothingBeginAndEndPoints[13];
	            diagonalLineOriginPointZ = (Integer)smoothingBeginAndEndPoints[14];
	            diagonalLineFinalDestinationPointX = (Integer)smoothingBeginAndEndPoints[15];
	            diagonalLineFinalDestinationPointY = (Integer)smoothingBeginAndEndPoints[16];
	            diagonalLineFinalDestinationPointZ = (Integer)smoothingBeginAndEndPoints[17];
            }

            if(smoothingBeginAndEndPoints.length > 17)
            {
        		if((Integer)smoothingBeginAndEndPoints[13] >= (Integer)smoothingBeginAndEndPoints[16])
        		{
        			diagonalLinegoingDown = true;
        		}
        		else if((Integer)smoothingBeginAndEndPoints[13] < (Integer)smoothingBeginAndEndPoints[16])
        		{
        			diagonalLinegoingUp = true;
        		}
            }

            int highestBlock = -1;

            // TODO: Check if this is really still needed
            // finalDestinationPointY may have been found so the chunk is loaded, however it might still be the wrong coordinate
            // check again, taking into account water and lava and originPointY
            material = null;
            highestBlock = finalDestinationPointY;

            if((prevfinalDestinationPointX != finalDestinationPointX || prevfinalDestinationPointZ != finalDestinationPointZ))
            {
            	blockColumn = World.getBlockColumn(finalDestinationPointX,finalDestinationPointZ);
            	prevfinalDestinationPointX = finalDestinationPointX;
            	prevfinalDestinationPointZ = finalDestinationPointZ;
            }

            for(int i = highestBlock; i > -1; i--)
            {
                // when going down dont stop at the waterline
                // when going up stop at the waterline
            	BlockFunction block = blockColumn[i];
            	material = block.material;
                if(
                    !material.getName().toLowerCase().equals("air") &&
                    (
                        (block.y <= originPointY && !material.isLiquid()) ||
                        (block.y > originPointY && ((!((BO3)Start.getObject()).getSettings().SpawnUnderWater || !material.isLiquid())))
                    )
                )
                {
                    finalDestinationPointY = block.y;
                    break;
                }
            }

            if(finalDestinationPointY > beginPoint.y)
            {
                goingUp = true;
            }
            if(finalDestinationPointY <= beginPoint.y)
            {
                goingDown = true;
            }

            // Diagonal line child lines can only spawn in the same vertical direction
            // as their parent
    		if(diagonalLinegoingUp && !goingUp)
    		{
    			finalDestinationPointY = originPointY + 75 < 256 ? originPointY + 75 : 255;
                goingUp = true;
                goingDown = false;
    		}
    		else if(diagonalLinegoingDown && !goingDown)
    		{
                goingUp = false;
                goingDown = true;

                int distanceFromOrigin = -1;
                int firstSolidBlock = -1;

                // Since this is the second pass and the first pass went up we'll have to detect
                // the closest suitable block to smooth to without using getHeighestBlock()
                // this means we might accidentally detect a cave beneath the surface as the
                // smooth to point.
                // set a limit of -30 y to reduce the chance that we target a cave underneath the surface
                // if we do hit a cave then it will be used as the base for the dirt ramp we're making,
                // the cave will be filled with the dirt ramp and the dirt ramp may look oddly steep
                // when seen from above. Limiting this to 30 should reduce this effect to acceptable levels?

                // Look for a solid destination block that has air/water/lava above it below the originBlock
                // If we cant find one within range (30 blocks) then use the first solid block without air/water/lava above it

                for(int i = originPointY; i > -1; i--)
                {
                	BlockFunction block = blockColumn[i];
                    distanceFromOrigin = Math.abs(originPointY - block.y);
                    LocalMaterialData materialAbove = blockColumn[i + 1].material;
                    material = blockColumn[block.y].material;
                    if(
                        firstSolidBlock == -1 &&
                        !material.getName().toLowerCase().equals("air") &&
                        !material.isLiquid()
                    )
                    {
                        firstSolidBlock = block.y;
                    }

                    if(
                        distanceFromOrigin <= 30 &&
                        !material.getName().toLowerCase().equals("air") &&
                        !material.isLiquid() &&
                        (
                            materialAbove.getName().toLowerCase().equals("air") ||
                            materialAbove.isLiquid()
                        )
                    )
                    {
                        finalDestinationPointY = block.y;
                        break;
                    }
                    if(distanceFromOrigin > 30 && firstSolidBlock > -1)
                    {
                        finalDestinationPointY = firstSolidBlock;
                        break;
                    }
                }

                // No block found
                if(distanceFromOrigin > 30 && firstSolidBlock == -1)
                {
                    finalDestinationPointY = originPointY;
                }
    		}

            // TODO: Make checks for situations where we can predict that a second pass won't be needed?
            int repeats = 1;

            // Do two passes, one up and one down, for each smoothing begin and endpoint
            // to make both an evenly sloped hole above and a hill below the BO3
            for(int pass2 = 0; pass2 <= repeats; pass2++)
            {
            	//if(pass2 == 1) { break; }

                // If this is a corner then on the second pass move the diagonal line
                if(smoothingBeginAndEndPoints.length > 17 && pass2 == 1)
                {
    	            // Recalculate the diagonal line y endpoint

                    if(diagonalLinegoingDown)
                    {
                        // TODO: replace 75 with... configurable value? or some kinda block-detection routine?
                    	diagonalLineFinalDestinationPointY = diagonalLineoriginPointY + 75 < 256 ? diagonalLineoriginPointY + 75 : 255;
                    	//diagonalLineFinalDestinationPointY = diagonalLineoriginPointY;
                    }
                    else if(diagonalLinegoingUp)// && !goingUp)
                    {
                        int distanceFromOrigin = -1;
                        int firstSolidBlock = -1;
                        // Since this is the second pass and the first pass went up we'll have to detect
                        // the closest suitable block to smooth to without using getHeighestBlock()
                        // this means we might accidentally detect a cave beneath the surface as the
                        // smooth to point.
                        // set a limit of -30 y to reduce the chance that we target a cave underneath the surface
                        // if we do hit a cave then it will be used as the base for the dirt ramp we're making,
                        // the cave will be filled with the dirt ramp and the dirt ramp may look oddly steep
                        // when seen from above. Limiting this to 30 should reduce this effect to acceptable levels?

                        // Look for a solid destination block that has air/water/lava above it below the originBlock
                        // If we cant find one within range (30 blocks) then use the first solid block without air/water/lava above it
                        diagonalLineFinalDestinationPointY = diagonalLineoriginPointY;

        	            if((prevDiagonalLineFinalDestinationPointX != diagonalLineFinalDestinationPointX || prevDiagonalLineFinalDestinationPointZ != diagonalLineFinalDestinationPointZ))
        	            {
        	            	blockColumn2 = World.getBlockColumn(diagonalLineFinalDestinationPointX,diagonalLineFinalDestinationPointZ);
        	            	prevDiagonalLineFinalDestinationPointX = diagonalLineFinalDestinationPointX;
        	            	prevDiagonalLineFinalDestinationPointZ = diagonalLineFinalDestinationPointZ;
        	            }

        	            for(int i = diagonalLineoriginPointY; i > 0; i--)
                        {
                        	BlockFunction block = blockColumn2[i];
                            distanceFromOrigin = Math.abs(diagonalLineoriginPointY - block.y);
                            LocalMaterialData materialAbove = blockColumn2[i + 1].material;
                            material = block.material;
                            if(
                                firstSolidBlock == -1 &&
                                !material.getName().toLowerCase().equals("air") &&
                                !material.isLiquid()
                            )
                            {
                                firstSolidBlock = block.y;
                            }

                            if(
                                distanceFromOrigin <= 30 &&
                                !material.getName().toLowerCase().equals("air") &&
                                !material.isLiquid() &&
                                (
                                    materialAbove.getName().toLowerCase().equals("air") ||
                                    materialAbove.isLiquid()
                                )
                            )
                            {
                            	diagonalLineFinalDestinationPointY = block.y;
                                break;
                            }
                            if(distanceFromOrigin > 30 && firstSolidBlock > -1)
                            {
                            	diagonalLineFinalDestinationPointY = firstSolidBlock;
                                break;
                            }
                        }
                    }

	        		originPointY = (int)Math.ceil(
                        (double)
                        (
                            (double)Math.abs(diagonalLineoriginPointY - diagonalLineFinalDestinationPointY)
                            *
                            (double)((Math.abs(diagonalLineOriginPointX - originPointX)) / (double)Math.abs(diagonalLineOriginPointX - diagonalLineFinalDestinationPointX))
                        )
                    );
	        		if(diagonalLineoriginPointY > diagonalLineFinalDestinationPointY)
	        		{
	        			originPointY = diagonalLineoriginPointY - originPointY;
	        		}
	        		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
	        		{
	        			originPointY = diagonalLineoriginPointY + originPointY;
	        		} else {
	        			originPointY = diagonalLineoriginPointY;
	        		}

	        		// Line has been switched so really this line is going down
	        		if(diagonalLinegoingUp)
	        		{
		                material = null;
		                highestBlock = originPointY;

		                for(int i = highestBlock; i > -1; i--)
		                {
		                    // when going down dont stop at the waterline
		                    // when going up stop at the waterline
		                	BlockFunction block = blockColumn[i];
		                	material = block.material;
		                    if(
		                        !material.getName().toLowerCase().equals("air") &&
		                        (
		                            (block.y <= originPointY && !material.isLiquid()) ||
		                            (block.y > originPointY && (!((BO3)Start.getObject()).getSettings().SpawnUnderWater || !material.isLiquid()))
		                        )
		                    )
		                    {
		                        finalDestinationPointY = block.y;
		                        break;
		                    }
		                }
                        goingUp = false;
                        goingDown = true;
	        		}
	        		// Line has been switched so really this line is going up
	        		else if(diagonalLinegoingDown)
	        		{
	        			finalDestinationPointY = World.getHighestBlockYAt(finalDestinationPointX, finalDestinationPointZ, true, true, false, true);
                    	if(finalDestinationPointY < diagonalLineoriginPointY)
                    	{
		        			finalDestinationPointY = diagonalLineoriginPointY + 75 < 256 ? diagonalLineoriginPointY + 75 : 255;
                    	}

                        goingUp = true;
                        goingDown = false;
	        		}
                }

                if(pass2 == 1 && smoothingBeginAndEndPoints.length < 18)
                {
                    if(!goingUp)
                    {
                        // TODO: replace 75 with... configurable value? or some kinda block-detection routine?
                        finalDestinationPointY = originPointY + 75 < 256 ? originPointY + 75 : 255;
                        goingUp = true;
                        goingDown = false;
                    }
                    else if(goingUp)
                    {
                        goingUp = false;
                        goingDown = true;

                        int distanceFromOrigin = -1;
                        int firstSolidBlock = -1;
                        // Since this is the second pass and the first pass went up we'll have to detect
                        // the closest suitable block to smooth to without using getHeighestBlock()
                        // this means we might accidentally detect a cave beneath the surface as the
                        // smooth to point.
                        // set a limit of -30 y to reduce the chance that we target a cave underneath the surface
                        // if we do hit a cave then it will be used as the base for the dirt ramp we're making,
                        // the cave will be filled with the dirt ramp and the dirt ramp may look oddly steep
                        // when seen from above. Limiting this to 30 should reduce this effect to acceptable levels?

                        // Look for a solid destination block that has air/water/lava above it below the originBlock
                        // If we cant find one within range (30 blocks) then use the first solid block without air/water/lava above it
                        finalDestinationPointY = originPointY;
                        for(int i = originPointY; i > -1; i--)
                        {
                            BlockFunction block = blockColumn[i];
                            distanceFromOrigin = Math.abs(originPointY - block.y);
                            LocalMaterialData materialAbove = blockColumn[i + 1].material;
                            material = block.material;
                            if(
                                firstSolidBlock == -1 &&
                                !material.getName().toLowerCase().equals("air") &&
                                !material.isLiquid()
                            )
                            {
                                firstSolidBlock = block.y;
                            }

                            if(
                                distanceFromOrigin <= 30 &&
                                !material.getName().toLowerCase().equals("air") &&
                                !material.isLiquid() &&
                                (
                                    materialAbove.getName().toLowerCase().equals("air") ||
                                    materialAbove.isLiquid()
                                )
                            )
                            {
                                finalDestinationPointY = block.y;
                                break;
                            }
                            if(distanceFromOrigin > 30 && firstSolidBlock > -1)
                            {
                                finalDestinationPointY = firstSolidBlock;
                                break;
                            }
                        }

                        // No block found
                        if(distanceFromOrigin > 30 && firstSolidBlock == -1)
                        {
                            finalDestinationPointY = originPointY;
                        }
                    }

                    material = null;
                }

                // Get the coordinates for the last block in this chunk for this line
                endPoint = new BlockFunction();
                endPoint.x = (Integer)smoothingBeginAndEndPoints[3];
                endPoint.y = finalDestinationPointY;
                endPoint.z = (Integer)smoothingBeginAndEndPoints[5];

                // Add to spawn list all the blocks in between the first and last block in this chunk for this line

                if(originPointX != finalDestinationPointX && originPointZ == finalDestinationPointZ)
                {
                	double adjustedOriginPointY = 0;
                	if(smoothingBeginAndEndPoints.length > 17)
                	{
	            		double originPointY2 =
                        (
                            (double)Math.abs(diagonalLineoriginPointY - diagonalLineFinalDestinationPointY)
                            *
                            (double)((double)Math.abs(diagonalLineOriginPointX - originPointX) / (double)Math.abs(diagonalLineOriginPointX - diagonalLineFinalDestinationPointX))
                        );

                		if(diagonalLineoriginPointY > diagonalLineFinalDestinationPointY)
                		{
                			originPointY2 = diagonalLineoriginPointY - originPointY2;
                		}
                		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
                		{
                			originPointY2 = diagonalLineoriginPointY + originPointY2;
                		} else {
                			originPointY2 = diagonalLineoriginPointY;
                		}

                        adjustedOriginPointY =
                        (
                            (double)Math.abs(originPointY2 - finalDestinationPointY)
                            *
                            (double)((double)Math.abs(originPointX - diagonalLineOriginPointX) / (double)Math.abs(originPointX - finalDestinationPointX))
                        );

                        if(originPointY2 > finalDestinationPointY)
                        {
                        	adjustedOriginPointY =  originPointY2 + adjustedOriginPointY;
                        } else {
                        	adjustedOriginPointY =  originPointY2 - adjustedOriginPointY;
                        }
                	}

                    for(int i = 0; i <= Math.abs(beginPoint.x - endPoint.x); i++)
                    {
	    	            //X difference
	    	            distanceFromStart = Math.abs(beginPoint.x - originPointX) + i;

	    	            // (diagonalLineOriginPointX != originPointX) is to ignore any lines of blocks that use the very first block
	    	            // in a diagonal line as an origin point because those lines can be treated like normal (non-corner/diagonal lines)
	    	            if(smoothingBeginAndEndPoints.length > 17 && (diagonalLineOriginPointX != originPointX))
	    	            {
    	    	            //X difference
    	    	            distanceFromStart = Math.abs(beginPoint.x - diagonalLineOriginPointX) + i;

	                        double surfaceBlockHeight2 =
                            (
                                (double)Math.abs(adjustedOriginPointY - finalDestinationPointY)
                                *
                                (double)((double)distanceFromStart / (double)Math.abs(diagonalLineOriginPointX - finalDestinationPointX))
                            );

	                        if(adjustedOriginPointY > finalDestinationPointY)
	                        {
	                            // Moving down
	                        	surfaceBlockHeight = (int)Math.round(adjustedOriginPointY - surfaceBlockHeight2);
	                        } else {
	                        	surfaceBlockHeight = (int)Math.round(adjustedOriginPointY + surfaceBlockHeight2);
	                        }
	    	            } else {
	                        //surfaceBlockHeight = (int)Math.ceil(
	    	            	surfaceBlockHeight = (int)Math.round(
	                            (double)
	                            (
	                                (double)Math.abs(originPointY - finalDestinationPointY)
	                                *
	                                (double)((double)distanceFromStart / (double)Math.abs(originPointX - finalDestinationPointX))
	                            )
	                        );

	                        if(originPointY > finalDestinationPointY)
	                        {
	                            // Moving down
	                            surfaceBlockHeight = originPointY - surfaceBlockHeight;
	                        } else {
	                            surfaceBlockHeight = originPointY + surfaceBlockHeight;
	                        }
	    	            }

                        filler = new BlockFunction();
                        if(originPointX < finalDestinationPointX)
                        {
                            filler.x = beginPoint.x + i;
                            filler.y = surfaceBlockHeight;
                            filler.z = beginPoint.z;
                        }
                        if(originPointX > finalDestinationPointX)
                        {
                            filler.x = beginPoint.x - i;
                            filler.y = surfaceBlockHeight;
                            filler.z = beginPoint.z;
                        }

                        // For each block to spawn find out if it is above or below a smooth-area beginning point
                        // if it is above a smooth-area beginning point and this line is going up then don't spawn the block
                        // and abort spawning for this line of blocks
                        // this is done to make sure that smoothing-areas going down can cover lower-lying smooth areas
                        // but lower-lying smooth-areas going up do not replace higher smoothing areas going down
                        boolean abort = false;
                        // get smoothing blocks
                        for(Object[] smoothingBeginAndEndPoints2 : smoothingAreas)
                        {
                        	// TODO: Find out if this doesnt skip the block at
                        	// diagonal line index 0, it shouldnt!
                        	if(smoothingBeginAndEndPoints2.length < 18)
                        	{
	                        	int originPointX2 = (Integer)smoothingBeginAndEndPoints2[6];
	                        	int originPointZ2 = (Integer)smoothingBeginAndEndPoints2[8];

	                            if((originPointX2 != filler.x || originPointZ2 != filler.z) || (originPointX == originPointX2 && originPointZ == originPointZ2))
	                            {
	                                continue;
	                            }

	                            if(goingUp)
	                            {
	                                abort = true;
	                                break;
	                            }
                        	}
                        }

                        if(abort)
                        {
                            break;
                        }

                        blocksToRemove = new ArrayList<Object[]>();
                        dontAdd = false;
                        for(Object[] existingBlockItem : blocksToSpawn)
                        {
                            existingBlock = (BlockFunction)existingBlockItem[0];

                            //Don't always override higher blocks when going down, instead do a second pass going up
                            if (existingBlock.x == filler.x && existingBlock.z == filler.z)
                            {
                                // When this block is lower than existingblock and this block is going up and existingblock is going up
                                if (filler.y < existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                                // When this block is higher than or equal to existingblock and this block is going up and existingblock is going up
                                else if (filler.y >= existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                                {
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is lower than existingblock and this block is not going up and existingblock is going up
                                else if (filler.y < existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    // since goingDown does not remove higher blocks allow both blocks
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                                else if (filler.y > existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                                else if (filler.y == existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    //Allow both
                                }

                                // When this block is lower than existingblock and this block is going up and existingblock is not going up
                                if (filler.y < existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    //if the other block is higher and smoothing downwards then let it cover (smother) any smooth area below it (namely this one)
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is higher than or equal to existingblock and this block is going up and existingblock is not going up
                                else if (filler.y >= existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    // if this block is above another block that is going down and this block is going up then both are allowed
                                }
                                // When this block is lower than existingblock and this block is not going up and existingblock is not going up
                                else if (filler.y < existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is not going up
                                else if (filler.y >= existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                            }
                        }

                        if(!dontAdd)
                        {
                        	/*
	                        // Use this to debug smoothing areas, this shows origin points!
	                        if(filler.x == originPointX && filler.z == originPointZ)
	                        {
	            	    		try {
	            					World.setBlock(originPointX, originPointY, originPointZ,OTG.readMaterial("STONE"), null, true);
	            				} catch (InvalidConfigException e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}
	                        }
	                        */

	                        if(blocksToRemove.size() > 0)
	                        {
	                            for(Object[] blockToRemove : blocksToRemove)
	                            {
	                                blocksToSpawn.remove(blockToRemove);
	                            }
	                        }

                            blocksToSpawn.add(new Object[] { filler, goingUp, goingDown, pass2 == 1 });
                        }
                    }
                }
                if(originPointX == finalDestinationPointX && originPointZ != finalDestinationPointZ)
                {
                	double adjustedOriginPointY = 0;
                	if(smoothingBeginAndEndPoints.length > 17)
                	{
	            		double originPointY2 =
                        (
                            (double)Math.abs(diagonalLineoriginPointY - diagonalLineFinalDestinationPointY)
                            *
                            (double)((double)Math.abs(diagonalLineOriginPointX - originPointX) / (double)Math.abs(diagonalLineOriginPointX - diagonalLineFinalDestinationPointX))
                        );

                		if(diagonalLineoriginPointY > diagonalLineFinalDestinationPointY)
                		{
                			originPointY2 = diagonalLineoriginPointY - originPointY2;
                		}
                		else if(diagonalLineoriginPointY < diagonalLineFinalDestinationPointY)
                		{
                			originPointY2 = diagonalLineoriginPointY + originPointY2;
                		} else {
                			originPointY2 = diagonalLineoriginPointY;
                		}

                        adjustedOriginPointY =
                        (
                            (double)Math.abs(originPointY2 - finalDestinationPointY)
                            *
                            (double)((double)Math.abs(originPointZ - diagonalLineOriginPointZ) / (double)Math.abs(originPointZ - finalDestinationPointZ))
                        );

                        if(originPointY2 > finalDestinationPointY)
                        {
                        	adjustedOriginPointY =  originPointY2 + adjustedOriginPointY;
                        } else {
                        	adjustedOriginPointY =  originPointY2 - adjustedOriginPointY;
                        }
                	}

                    for(int i = 0; i <= Math.abs(beginPoint.z - endPoint.z); i++)
                    {
	    	            //Z difference
	    	            distanceFromStart = Math.abs(beginPoint.z - originPointZ) + i;

	    	            // (diagonalLineOriginPointZ != originPointZ) is to ignore any lines of blocks that use the very first block
	    	            // in a diagonal line as an origin point because those lines can be treated like normal (non-corner/diagonal lines)
	    	            if(smoothingBeginAndEndPoints.length > 17 && (diagonalLineOriginPointZ != originPointZ))
	    	            {
    	    	            //Z difference
    	    	            distanceFromStart = Math.abs(beginPoint.z - diagonalLineOriginPointZ) + i;

	                        double surfaceBlockHeight2 =
                            (
                                (double)Math.abs(adjustedOriginPointY - finalDestinationPointY)
                                *
                                (double)((double)distanceFromStart / (double)Math.abs(diagonalLineOriginPointZ - finalDestinationPointZ))
                            );

	                        if(adjustedOriginPointY > finalDestinationPointY)
	                        {
	                            // Moving down
	                        	surfaceBlockHeight = (int)Math.round(adjustedOriginPointY - surfaceBlockHeight2);
	                        } else {
	                        	surfaceBlockHeight = (int)Math.round(adjustedOriginPointY + surfaceBlockHeight2);
	                        }
	    	            } else {
                    		surfaceBlockHeight = (int)Math.round(
	                            (double)
	                            (
	                                (double)Math.abs(originPointY - finalDestinationPointY)
	                                *
	                                (double)((double)distanceFromStart / (double)Math.abs(originPointZ - finalDestinationPointZ))
	                            )
	                        );

	                        if(originPointY > finalDestinationPointY)
	                        {
	                            // Moving down
	                            surfaceBlockHeight = originPointY - surfaceBlockHeight;
	                        } else {
	                            surfaceBlockHeight = originPointY + surfaceBlockHeight;
	                        }
	    	            }

                        filler = new BlockFunction();
                        if(originPointZ < finalDestinationPointZ)
                        {
                            filler.x = beginPoint.x;
                            filler.y = surfaceBlockHeight;
                            filler.z = beginPoint.z + i;
                        }
                        if(originPointZ > finalDestinationPointZ)
                        {
                            filler.x = beginPoint.x;
                            filler.y = surfaceBlockHeight;
                            filler.z = beginPoint.z - i;
                        }

                        // For each block to spawn find out if it is above or below a smooth-area beginning point
                        // if it is above a smooth-area beginning point and this line is going up then don't spawn the block
                        // and abort spawning for this line of blocks
                        // this is done to make sure that smoothing-areas going down can cover lower-lying smooth areas
                        // but lower-lying smooth-areas going up do not replace higher smoothing areas going down
                        boolean abort = false;
                        // get smoothing blocks
                        for(Object[] smoothingBeginAndEndPoints2 : smoothingAreas)
                        {
                        	// TODO: Find out if this doesnt skip the block at
                        	// diagonal line index 0, it shouldnt!
                        	if(smoothingBeginAndEndPoints2.length < 18)
                        	{
	                        	// TODO: Even diagonal block child line smooth origin points are included
	                        	// here, find out if that doesn't cause bugs..
	                        	int originPointX2 = (Integer)smoothingBeginAndEndPoints2[6];
	                        	int originPointZ2 = (Integer)smoothingBeginAndEndPoints2[8];

	                            if((originPointX2 != filler.x || originPointZ2 != filler.z) || (originPointX == originPointX2 && originPointZ == originPointZ2))
	                            {
	                                continue;
	                            }

	                            if(goingUp)
	                            {
	                                abort = true;
	                                break;
	                            }
                        	}
                        }

                        if(abort)
                        {
                            break;
                        }

                        dontAdd = false;
                        blocksToRemove = new ArrayList<Object[]>();
                        for(Object[] existingBlockItem : blocksToSpawn)
                        {
                            existingBlock = (BlockFunction)existingBlockItem[0];
                            //Don't always override higher blocks when going down, instead do a second pass going up
                            if (existingBlock.x == filler.x && existingBlock.z == filler.z)
                            {
                                // When this block is lower than existingblock and this block is going up and existingblock is going up
                                if (filler.y < existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                                // When this block is higher than or equal to existingblock and this block is going up and existingblock is going up
                                else if (filler.y >= existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                                {
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is lower than existingblock and this block is not going up and existingblock is going up
                                else if (filler.y < existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    // since goingDown does not remove higher blocks allow both blocks
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                                else if (filler.y > existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                                else if (filler.y == existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                                {
                                    //Allow both
                                }

                                // When this block is lower than existingblock and this block is going up and existingblock is not going up
                                if (filler.y < existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    //if the other block is higher and smoothing downwards then let it cover (smother) any smooth area below it (namely this one)
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is higher than or equal to existingblock and this block is going up and existingblock is not going up
                                else if (filler.y >= existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    // if this block is above another block that is going down and this block is going up then both are allowed
                                }
                                // When this block is lower than existingblock and this block is not going up and existingblock is not going up
                                else if (filler.y < existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    dontAdd = true;
                                    break;
                                }
                                // When this block is higher than or equal to existingblock and this block is not going up and existingblock is not going up
                                else if (filler.y >= existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                                {
                                    blocksToRemove.add(existingBlockItem);
                                }
                            }
                        }

                        if(!dontAdd)
                        {
                        	/*
	                        // Use this to debug smoothing areas, this shows origin points!
	                        if(filler.x == originPointX && filler.z == originPointZ)
	                        {
	            	    		try {
	            					World.setBlock(originPointX, originPointY, originPointZ,OTG.readMaterial("STONE"), null, true);
	            				} catch (InvalidConfigException e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}
	                        }
                        	*/

	                        if(blocksToRemove.size() > 0)
	                        {
	                            for(Object[] blockToRemove : blocksToRemove)
	                            {
	                                blocksToSpawn.remove(blockToRemove);
	                            }
	                        }

                            blocksToSpawn.add(new Object[] { filler, goingUp, goingDown, pass2 == 1 });
                        }
                    }
                }

                if(originPointX == finalDestinationPointX && originPointZ == finalDestinationPointZ)
                {
                    filler = new BlockFunction();
                    filler.x = finalDestinationPointX;
                    filler.y = finalDestinationPointY;
                    filler.z = finalDestinationPointZ;

                    if(!goingUp && !goingDown)
                    {
                    	goingDown = true;
                    }

                    // For each block to spawn find out if it is above or below a smooth-area beginning point
                    // if it is above a smooth-area beginning point and this line is going up then don't spawn the block
                    // and abort spawning for this line of blocks
                    // this is done to make sure that smoothing-areas going down can cover lower-lying smooth areas
                    // but lower-lying smooth-areas going up do not replace higher smoothing areas going down
                    boolean abort = false;
                    // get smoothing blocks
                    for(Object[] smoothingBeginAndEndPoints2 : smoothingAreas)
                    {
                    	// TODO: Find out if this doesnt skip the block at
                    	// diagonal line index 0, it shouldnt!
                    	if(smoothingBeginAndEndPoints2.length < 18)
                    	{
                        	int originPointX2 = (Integer)smoothingBeginAndEndPoints2[6];
                        	int originPointZ2 = (Integer)smoothingBeginAndEndPoints2[8];

                            if((originPointX2 != filler.x || originPointZ2 != filler.z) || (originPointX == originPointX2 && originPointZ == originPointZ2))
                            {
                                continue;
                            }

                            if(goingUp)
                            {
                                abort = true;
                                break;
                            }
                    	}
                    }

                    if(abort)
                    {
                    	break;
                    }

                    dontAdd = false;
                    blocksToRemove = new ArrayList<Object[]>();
                    for(Object[] existingBlockItem : blocksToSpawn)
                    {
                        existingBlock = (BlockFunction)existingBlockItem[0];
                        //Don't always override higher blocks when going down, instead do a second pass going up
                        if (existingBlock.x == filler.x && existingBlock.z == filler.z)
                        {
                            // When this block is lower than existingblock and this block is going up and existingblock is going up
                            if (filler.y < existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                            {
                                blocksToRemove.add(existingBlockItem);
                            }
                            // When this block is higher than or equal to existingblock and this block is going up and existingblock is going up
                            else if (filler.y >= existingBlock.y && goingUp && (Boolean)existingBlockItem[1])
                            {
                                dontAdd = true;
                                break;
                            }
                            // When this block is lower than existingblock and this block is not going up and existingblock is going up
                            else if (filler.y < existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                            {
                                // since goingDown does not remove higher blocks allow both blocks
                            }
                            // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                            else if (filler.y > existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                            {
                                blocksToRemove.add(existingBlockItem);
                            }
                            // When this block is higher than or equal to existingblock and this block is not going up and existingblock is going up
                            else if (filler.y == existingBlock.y && !goingUp && (Boolean)existingBlockItem[1])
                            {
                                //Allow both
                            }

                            // When this block is lower than existingblock and this block is going up and existingblock is not going up
                            if (filler.y < existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                            {
                                //if the other block is higher and smoothing downwards then let it cover (smother) any smooth area below it (namely this one)
                                dontAdd = true;
                                break;
                            }
                            // When this block is higher than or equal to existingblock and this block is going up and existingblock is not going up
                            else if (filler.y >= existingBlock.y && goingUp && !(Boolean)existingBlockItem[1])
                            {
                                // if this block is above another block that is going down and this block is going up then both are allowed
                            }
                            // When this block is lower than existingblock and this block is not going up and existingblock is not going up
                            else if (filler.y < existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                            {
                                dontAdd = true;
                                break;
                            }
                            // When this block is higher than or equal to existingblock and this block is not going up and existingblock is not going up
                            else if (filler.y >= existingBlock.y && !goingUp && !(Boolean)existingBlockItem[1])
                            {
                                blocksToRemove.add(existingBlockItem);
                            }
                        }
                    }

                    if(!dontAdd)
                    {
                    	/*
                        // Use this to debug smoothing areas, this shows origin points!
                        if(filler.x == originPointX && filler.z == originPointZ)
                        {
            	    		try {
            					World.setBlock(originPointX, originPointY, originPointZ,OTG.readMaterial("STONE"), null, true);
            				} catch (InvalidConfigException e) {
            					// TODO Auto-generated catch block
            					e.printStackTrace();
            				}
                        }
                    	*/

                        if(blocksToRemove.size() > 0)
                        {
                            for(Object[] blockToRemove : blocksToRemove)
                            {
                                blocksToSpawn.remove(blockToRemove);
                            }
                        }

                        blocksToSpawn.add(new Object[] { filler, goingUp, goingDown, pass2 == 1 });
                    }
                }
            }
        }
        return blocksToSpawn;
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
