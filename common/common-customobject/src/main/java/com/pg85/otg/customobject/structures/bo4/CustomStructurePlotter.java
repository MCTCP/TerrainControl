package com.pg85.otg.customobject.structures.bo4;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructure;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.customobject.structures.CustomStructureFileManager;
import com.pg85.otg.customobject.structures.PlottedChunksRegion;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructure;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.ICustomStructureGen;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IStructuredCustomObject;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public class CustomStructurePlotter
{
	// Structurecache holds plotted structures/branches/smoothing areas in undecorated chunks.
	private final Map<ChunkCoordinate, BO4CustomStructure[][]> bo4StructureCache; // Per region
	
	// plottedChunks holds a chunkcoord for every chunk outside the 
	// pregenerated region that has had its decorate method called.
	// We unfortunately need this because MC can't tell use whether a chunk
	// has been decorated, only whether is has had terraingen done, or if it
	// is completely done being decorated and lit (its neighbours have all spawned).
	private final Map<ChunkCoordinate, PlottedChunksRegion> plottedChunks; // Per region

	// Used to find distance between structures and structure groups, only stores 1 chunk per structure in the 
	// calculated center of the structure. Does not clean itself when used with the pre-generator and will become 
	// slower as it fills up, use as little as possible! (can't clean itself because max radius for BO4 groups cannot be known)	
	private final HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName;  // structure name -> start chunk coords. Saved to disk.
	private final HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup; // group name -> Map<ChunkCoord, Radius>. Saved to disk.	

	// Locking objects to ensure plotting code can
	// never run multiple times asynchronously, or recursively.
	private final Object processingLock = new Object();
	private boolean processing = false;
	//
	
	// Non-persistent caches (optimisations)
	private final FifoMap<ChunkCoordinate, ArrayList<String>> structureNamesPerChunk;
	private final FifoMap<ChunkCoordinate, Object> plottedChunksFastCache; // TODO: Technically we don't need a map, we need a FIFO list with unique entries.
	
	public CustomStructurePlotter()
	{
		// Non-persistent caches
		this.structureNamesPerChunk = new FifoMap<ChunkCoordinate, ArrayList<String>>(2048);
		this.plottedChunksFastCache = new FifoMap<ChunkCoordinate, Object>(2048);
		
		// Persistent caches
		this.spawnedStructuresByName = new HashMap<String, ArrayList<ChunkCoordinate>>();
		this.spawnedStructuresByGroup = new HashMap<String, HashMap<ChunkCoordinate, Integer>>();
		this.bo4StructureCache = new HashMap<ChunkCoordinate, BO4CustomStructure[][]>();
		this.plottedChunks = new HashMap<ChunkCoordinate, PlottedChunksRegion>(); 
	}

	// Structure cache
	
	private boolean structureCacheContainsKey(ChunkCoordinate chunkCoordinate)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();				
		BO4CustomStructure[][] chunkRegion = bo4StructureCache.get(regionCoord);
		return chunkRegion != null && chunkRegion[chunkCoordinate.getRegionInternalX()][chunkCoordinate.getRegionInternalZ()] != null;
	}
	
	private void addToStructureCache(ChunkCoordinate chunkCoordinate, BO4CustomStructure structure)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();
		
		BO4CustomStructure[][] chunkRegion = this.bo4StructureCache.get(regionCoord);
		if(chunkRegion == null)
		{
			chunkRegion = new BO4CustomStructure[Constants.REGION_SIZE][Constants.REGION_SIZE];
			this.bo4StructureCache.put(regionCoord, chunkRegion);
		}
		chunkRegion[chunkCoordinate.getRegionInternalX()][chunkCoordinate.getRegionInternalZ()] = structure;
	}
	
	private void removeFromStructureCache(ChunkCoordinate chunkCoordinate)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();
		
		BO4CustomStructure[][] chunkRegion = this.bo4StructureCache.get(regionCoord);
		if(chunkRegion != null)
		{
			chunkRegion[chunkCoordinate.getRegionInternalX()][chunkCoordinate.getRegionInternalZ()] = null;
		}
		
		// TODO: Remove region from bo4StructureCache if it's empty? Shouldn't matter too much, region shouldn't be saved if it's empty.
	}
	
	private BO4CustomStructure getFromStructureCache(ChunkCoordinate chunkCoordinate)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();		
		BO4CustomStructure[][] chunkRegion = this.bo4StructureCache.get(regionCoord);
		if(chunkRegion != null)
		{
			return chunkRegion[chunkCoordinate.getRegionInternalX()][chunkCoordinate.getRegionInternalZ()];
		}
		return null;
	}

	// Populated chunks cache
	
	private boolean plottedChunksContainsKey(ChunkCoordinate chunkCoordinate)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();
		PlottedChunksRegion chunkRegion = plottedChunks.get(regionCoord);
		return chunkRegion != null && chunkRegion.getChunk(chunkCoordinate.getRegionInternalX(), chunkCoordinate.getRegionInternalZ());
	}
	
	private void addToPlottedChunks(ChunkCoordinate chunkCoordinate)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();				
		PlottedChunksRegion chunkRegion = this.plottedChunks.get(regionCoord);
		if(chunkRegion == null)
		{
			chunkRegion = new PlottedChunksRegion();
			this.plottedChunks.put(regionCoord, chunkRegion);
		}
		chunkRegion.setChunk(chunkCoordinate.getRegionInternalX(), chunkCoordinate.getRegionInternalZ());
	}	
	
	// Used while calculating branches
	public boolean isBo4ChunkPlotted(ChunkCoordinate chunkCoordinate)
	{
		// Check if any other structures are in this chunk
		boolean bFound =
			// TODO: Optimise this using regions, mark regions when completed so we can skip checks.
			this.plottedChunksFastCache.containsKey(chunkCoordinate) || // Has been plotted recently, still cached (fast cache).
			plottedChunksContainsKey(chunkCoordinate) // Has been plotted (slow cache).
		;
		if(bFound)
		{
			this.plottedChunksFastCache.put(chunkCoordinate, null);
		}
		return bFound;
	}

	// Called for each structure start plotted, and each ObjectToSpawn / 
	// SmoothingArea chunk after branches have been calculated	
	public void addBo4ToStructureCache(ChunkCoordinate chunkCoordinate, BO4CustomStructure structure)
	{
		// Add to structure cache so we can spawn parts later
		addToStructureCache(chunkCoordinate, structure);

		// Add to decorated chunks so we won't override
		addToPlottedChunks(chunkCoordinate);
		
		// Let plotter know the chunk is taken (fast cache, optimisation)
		setChunkOccupied(chunkCoordinate);
	}

	// Only used by ChunkDecorator
	public void spawnBO4Chunk(ChunkCoordinate chunkCoordinate, CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Path otgRootFolder, boolean developerMode, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		BO4CustomStructure structureStart = getFromStructureCache(chunkCoordinate);
		if (structureStart != null && structureStart.start != null)
		{
			structureStart.spawnInChunk(chunkCoordinate, structureCache, worldGenRegion, otgRootFolder, developerMode, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		} else {
			// TODO: When can structure.start be null? Should only be possible for bo3 structures?
			if(structureStart != null && structureStart.start == null)
			{
				throw new RuntimeException("This shouldn't happen, please contact Team OTG about this crash.");
			}
			// Nothing plotted in this chunk
		}

		// Safe to remove chunk from bo4StructureCache now, 
		// it has been decorated by ChunkDecorator (not just 
		// plotted/spawned while decorating a neighbouring chunk).
		removeFromStructureCache(chunkCoordinate);
		
		// Let plotter know the chunk is taken (fast cache, optimisation)
		setChunkOccupied(chunkCoordinate);
	}

	// Only used by ChunkDecorator during decoration
	public ChunkCoordinate plotStructures(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random rand, ChunkCoordinate chunkCoord, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return plotStructures(null, null, structureCache, worldGenRegion, rand, chunkCoord, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker, false);
	}
	
	// Used by ChunkDecorator during decoration and /otg spawn. targetStructure and targetBiomes only used for /spawn (make that prettier?)
	public ChunkCoordinate plotStructures(BO4 targetStructure, ArrayList<String> targetBiomes, CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random rand, ChunkCoordinate chunkCoord, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker, boolean force)
	{
		return plotStructures(targetStructure, targetBiomes, structureCache, worldGenRegion, rand, chunkCoord, false, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker, force);
	}

	private void setChunkOccupied(ChunkCoordinate chunkCoord)
	{
		// Add empty list as an optimisation, so null means not handled, emtpy means done
		this.structureNamesPerChunk.put(chunkCoord, new ArrayList<String>());
		// Use separate cache for faster isChunkPopulated lookups, 
		// no need to do containsKey + get == null, can just do containsKey
		this.plottedChunksFastCache.put(chunkCoord, null);
	}
	
	private ChunkCoordinate plotStructures(BO4 targetStructure, ArrayList<String> targetBiomes, CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random rand, ChunkCoordinate chunkCoord, boolean spawningStructureAtSpawn, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker, boolean force)
	{
		// This method can be called by /otg spawn and during chunkgeneration.
		// When called during chunkgeneration, the chunk must be filled or invalidated before returning, so never cancel.
		// When called by /otg spawn, skip this attempt to spawn and let chunk generation complete first.
		// This method should never be called recursively.
		// TODO: Make sure this never gets stuck, or abort and log a warning.
		// TODO: Can this thread be paused without issues, or for how long?
		while(true)
		{
			synchronized(this.processingLock)
			{
				if(!this.processing)
				{				
					this.processing = true;
					break;
				}				
				else if(targetStructure != null)
				{
					return null;
				}
			}  		
			try {
				Thread.sleep(20);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				throw new RuntimeException("This shouldn't happen, please ask for help on the OTG Discord or file an issue on the OTG github.");
			}
		}			
		
		if (!isBo4ChunkPlotted(chunkCoord))
		{
			IBiomeConfig biomeConfig = worldGenRegion.getBiomeConfig(chunkCoord.getBlockX() + DecorationArea.BO_CHUNK_CENTER_X, chunkCoord.getBlockZ() + DecorationArea.BO_CHUNK_CENTER_Z);		  
			List<ICustomStructureGen> customStructureGens = new ArrayList<>();
			if(targetStructure == null && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
			{
				// Get Bo4's for this biome
				for (ICustomStructureGen res : biomeConfig.getCustomStructures())
				{
					//TODO: Check for res.error?
					customStructureGens.add(res);
				}
			}
			if(targetStructure != null || customStructureGens.size() > 0)
			{
				Map<IStructuredCustomObject, Double> structuredCustomObjects = new HashMap<>();
				if(targetStructure != null)
				{
					if(targetBiomes.size() == 0 || targetBiomes.contains(biomeConfig.getName()))
					{
						structuredCustomObjects.put(targetStructure, 100.0);
					}
				} else {
					for(ICustomStructureGen structureGen : customStructureGens)
					{
						int i = 0;
						for(IStructuredCustomObject structure : structureGen.getObjects(worldGenRegion.getPresetFolderName(), otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker))
						{
							if(structure != null) // Structure was in resource list but file could not be found. TODO: Make this prettier!
							{
								if(structure instanceof BO4)
								{
									structuredCustomObjects.put(structure, structureGen.getObjectChance(i));
									i += 1;
								}
								else if(logger.getSpawnLogEnabled())
								{
									logger.log(LogMarker.WARN, "CustomStructure " + structure.getName() + " in biome " + biomeConfig.getName() + " has IsOTGPlus:false, ignoring.");
								}
							}
						}
					}
				}
				if(structuredCustomObjects.size() > 0)
				{
					BO4CustomStructureCoordinate structureCoord = null;
					BO4CustomStructure structureStart2 = null;

					ArrayList<Object[]> bo4sBySize = new ArrayList<Object[]>();
					ArrayList<String> structuresToSpawn1 = new ArrayList<String>();

					// Get list of BO3's that should spawn at the spawn point
					if(targetStructure == null && spawningStructureAtSpawn)
					{
						for(Map.Entry<IStructuredCustomObject, Double> bo4AndRarity : structuredCustomObjects.entrySet())
						{
							if(!((BO4)bo4AndRarity.getKey()).isInvalidConfig && ((BO4)bo4AndRarity.getKey()).getConfig().isSpawnPoint)
							{
								structuresToSpawn1.add(bo4AndRarity.getKey().getName());
								structureCoord = new BO4CustomStructureCoordinate(worldGenRegion.getPresetFolderName(), bo4AndRarity.getKey(), null, Rotation.NORTH, chunkCoord.getBlockX(), (short)0, chunkCoord.getBlockZ(), 0, false, false, null);
								structureStart2 = new BO4CustomStructure(worldGenRegion.getSeed(), structureCoord, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
								// Get minimum size (size if spawned with branchDepth 0)
								try {
									Object[] topLeftAndLowerRightChunkCoordinates = structureStart2.getMinimumSize(structureCache, worldGenRegion, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
									double BO3size = Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[0] - -(Integer)topLeftAndLowerRightChunkCoordinates[2]) * Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[1] - -(Integer)topLeftAndLowerRightChunkCoordinates[3]);
									bo4sBySize.add(new Object[]{ bo4AndRarity.getKey(), topLeftAndLowerRightChunkCoordinates, BO3size, bo4AndRarity.getValue() });
								}
								catch (InvalidConfigException e)
								{
									((BO4)bo4AndRarity.getKey()).isInvalidConfig = true;
								}
							}
						}
					}

					if(!spawningStructureAtSpawn || bo4sBySize.size() == 0)
					{
						if(spawningStructureAtSpawn && bo4sBySize.size() == 0)
						{
							spawningStructureAtSpawn = false;
						}
						
						// Get list of BO4's able to spawn in this chunk
						for(Map.Entry<IStructuredCustomObject, Double> bo4AndRarity : structuredCustomObjects.entrySet())
						{
							if(!((BO4)bo4AndRarity.getKey()).isInvalidConfig && (int)Math.round(bo4AndRarity.getValue()) > 0)
							{
								// TODO: avoid calling IsBO4AllowedToSpawnAt so much, cache and reuse any nearest group members found

								if(isBO4AllowedToSpawnAtByFrequency(chunkCoord, ((BO4)bo4AndRarity.getKey())))
								{
									structuresToSpawn1.add(bo4AndRarity.getKey().getName());
									structureCoord = new BO4CustomStructureCoordinate(worldGenRegion.getPresetFolderName(), bo4AndRarity.getKey(), null, Rotation.NORTH, chunkCoord.getBlockX(), (short)0, chunkCoord.getBlockZ(), 0, false, false, null);
									structureStart2 = new BO4CustomStructure(worldGenRegion.getSeed(), structureCoord, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
									// Get minimum size (size if spawned with branchDepth 0)
									try {
										Object[] topLeftAndLowerRightChunkCoordinates = structureStart2.getMinimumSize(structureCache, worldGenRegion, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
										double BO3size = Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[0] - -(Integer)topLeftAndLowerRightChunkCoordinates[2]) * Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[1] - -(Integer)topLeftAndLowerRightChunkCoordinates[3]);
										int insertAtIndex = bo4sBySize.size();
										int i = 0;
										for(Object[] entry : bo4sBySize)
										{
											if(((BO4)bo4AndRarity.getKey()).getConfig().timesSpawned < ((BO4)entry[0]).getConfig().timesSpawned || (BO3size > (Double)entry[2] && ((BO4)bo4AndRarity.getKey()).getConfig().timesSpawned == ((BO4)entry[0]).getConfig().timesSpawned))
											{
												insertAtIndex = i;
												break;
											}
											i += 1;
										}
										bo4sBySize.add(insertAtIndex, new Object[]{ bo4AndRarity.getKey(), topLeftAndLowerRightChunkCoordinates, BO3size, bo4AndRarity.getValue() });
									}
									catch (InvalidConfigException e)
									{
										((BO4)bo4AndRarity.getKey()).isInvalidConfig = true;
									}
								}
							}
						}
					}

					if(bo4sBySize.size() > 0)
					{
						// Go over the list and try to spawn each structure, from largest to smallest.
						// We have to spawn large structures first, since small structures would 
						// clutter the landscape and make it impossible to find a large open space.
						for(Object[] currentStructureSpawning : bo4sBySize)
						{
							int pass = 0;

							int left = 0;
							int right = 0;
							int top = 0;
							int bottom = 0;

							int areaLength = 0;
							int areaWidth = 0;
							
							Object[] topLeftAndLowerRightChunkCoordinates = (Object[])currentStructureSpawning[1];
							int structureLength = (Integer)topLeftAndLowerRightChunkCoordinates[1] + (Integer)topLeftAndLowerRightChunkCoordinates[3] + 1;
							int structureWidth = (Integer)topLeftAndLowerRightChunkCoordinates[0] + (Integer)topLeftAndLowerRightChunkCoordinates[2] + 1;
							int structureTop = (Integer)topLeftAndLowerRightChunkCoordinates[0];
							int structureRight = (Integer)topLeftAndLowerRightChunkCoordinates[1];
							int structureBottom = (Integer)topLeftAndLowerRightChunkCoordinates[2];
							int structureLeft = (Integer)topLeftAndLowerRightChunkCoordinates[3];
							
							while(pass <= 4)
							{
								pass++;

								// Disabled rarity because it was basically meaningless, 
								// Rarity 0 is used to allow spawning of branches but not structure start in biomes.
								// Any value > 0 means spawn in this biome.
								// TODO: Replace rarity with a boolean (but keep it backwards compatible)?
								if ((int)Math.round((Double)currentStructureSpawning[3]) > 0 || (spawningStructureAtSpawn && ((BO4)currentStructureSpawning[0]).getConfig().isSpawnPoint))
								{
									left = 0;
									right = 0;
									top = 0;
									bottom = 0;

									// Find out how large the available area is

									boolean leftEdgeFound = false;
									boolean rightEdgeFound = false;
									boolean topEdgeFound = false;
									boolean bottomEdgeFound = false;

									// Pass 1 expands to left and bottom first, right and top second
									if(pass == 1)
									{
										topEdgeFound = true;
										rightEdgeFound = true;
									}
									// Pass 2 expands to right and bottom first, left and top second
									if(pass == 2)
									{
										topEdgeFound = true;
										leftEdgeFound = true;
									}
									// Pass 3 expands to left and top first, right and bottom second
									if(pass == 3)
									{
										bottomEdgeFound = true;
										rightEdgeFound = true;
									}
									// Pass 4 expands to right and top first, left and bottom second
									if(pass == 4)
									{
										bottomEdgeFound = true;
										leftEdgeFound = true;
									}

									ArrayList<String> biomeStructures;
									IBiomeConfig biomeConfig3;
									ArrayList<String> structuresToSpawn;
									boolean canSpawnHere;

									int scanDistance = 0;
									
									if(!spawningStructureAtSpawn)
									{
										while(!(leftEdgeFound && rightEdgeFound && topEdgeFound && bottomEdgeFound))
										{
											scanDistance += 1;

											if(!rightEdgeFound)
											{
												// Find more free chunks until both height and width fit, just in case
												// we can't fit length or width in the y direction.
												if(right + left + 1 >= structureLength && right + left + 1 >= structureWidth)
												{
													rightEdgeFound = true; // leftEdgeFound is already true, since left and right will never spawn in the same pass.
												}
											}
											if(!rightEdgeFound)
											{
												// Check Right
												for(int i = -top; i <= bottom; i++)
												{
													canSpawnHere = false;
													if(targetStructure != null)
													{
														if(!isBo4ChunkPlotted(chunkCoord) && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
														{
															if(targetBiomes.size() == 0)
															{
																canSpawnHere = true;
															} else {
																biomeConfig3 = worldGenRegion.getBiomeConfig((chunkCoord.getChunkX() + scanDistance) * 16 + DecorationArea.BO_CHUNK_CENTER_X, (chunkCoord.getChunkZ() + i) * 16 + DecorationArea.BO_CHUNK_CENTER_Z);
																if(targetBiomes.contains(biomeConfig3.getName()))
																{
																	canSpawnHere = true;
																}
															}
														}
													} else {
														// When we get biomestructures here, size() == 0 means the chunk has been handled, null means it hasnt yet been cached at all
														biomeStructures = this.structureNamesPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + scanDistance), (chunkCoord.getChunkZ() + i)));															
														if(biomeStructures == null)
														{
															if(!isBo4ChunkPlotted(chunkCoord) && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
															{
																biomeConfig3 = worldGenRegion.getBiomeConfig((chunkCoord.getChunkX() + scanDistance) * 16 + DecorationArea.BO_CHUNK_CENTER_X, (chunkCoord.getChunkZ() + i) * 16 + DecorationArea.BO_CHUNK_CENTER_Z);
																// Get cached data if available
																if(!biomeConfig3.getName().equals(biomeConfig.getName()))
																{
																	structuresToSpawn = new ArrayList<String>();

																	// Get Bo3's for this biome
																	for (List<String> res : biomeConfig3.getCustomStructureNames())
																	{
																		for(String bo4Name : res)
																		{
																			structuresToSpawn.add(bo4Name);
																		}
																	}																		
																	biomeStructures = structuresToSpawn;
																} else {
																	canSpawnHere = true;
																	biomeStructures = structuresToSpawn1;
																}
															} else {
																biomeStructures = new ArrayList<String>(); // Don't spawn anything here, there is a default structure.
															}
															this.structureNamesPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + scanDistance), (chunkCoord.getChunkZ() + i)),biomeStructures);
														}
														if(biomeStructures != null)
														{
															for(String structureToSpawn : biomeStructures)
															{
																if(structureToSpawn.equals(((BO4)currentStructureSpawning[0]).getName()))
																{
																	canSpawnHere = true;
																	break;
																}
															}
														}
													}
													if(!canSpawnHere)
													{
														rightEdgeFound = true;
													}
												}
												if(!rightEdgeFound)
												{
													right += 1;
												}
											}

											if(!leftEdgeFound)
											{
												// Find more free chunks until both height and width fit, just in case
												// we can't fit length or width in the y direction.
												if(right + left + 1 >= structureLength && right + left + 1 >= structureWidth)
												{
													leftEdgeFound = true; // rightEdgeFound is already true, since left and right will never spawn in the same pass.
												}
											}
											if(!leftEdgeFound)
											{
												// Check Left
												for(int i = -top; i <= bottom; i++)
												{
													canSpawnHere = false;
													if(targetStructure != null)
													{
														if(!isBo4ChunkPlotted(chunkCoord) && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
														{
															if(targetBiomes.size() == 0)
															{
																canSpawnHere = true;
															} else {
																biomeConfig3 = worldGenRegion.getBiomeConfig((chunkCoord.getChunkX() - scanDistance) * 16 + DecorationArea.BO_CHUNK_CENTER_X, (chunkCoord.getChunkZ() + i) * 16 + DecorationArea.BO_CHUNK_CENTER_Z);
																if(targetBiomes.contains(biomeConfig3.getName()))
																{
																	canSpawnHere = true;
																}
															}
														}
													} else {
														// When we get biomestructures here, size() == 0 means the chunk has been handled, null means it hasnt yet been cached at all
														biomeStructures = this.structureNamesPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - scanDistance), (chunkCoord.getChunkZ() + i)));
														if(biomeStructures == null)
														{
															if(!isBo4ChunkPlotted(chunkCoord) && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
															{
																biomeConfig3 = worldGenRegion.getBiomeConfig((chunkCoord.getChunkX() - scanDistance) * 16 + DecorationArea.BO_CHUNK_CENTER_X, (chunkCoord.getChunkZ() + i) * 16 + DecorationArea.BO_CHUNK_CENTER_Z);
																if(!biomeConfig3.getName().equals(biomeConfig.getName()))
																{
																	structuresToSpawn = new ArrayList<String>();

																	// Get Bo3's for this biome
																	for (List<String> res : biomeConfig3.getCustomStructureNames())
																	{
																		for(String bo4Name : res)
																		{
																			structuresToSpawn.add(bo4Name);
																		}
																	}																		
																	biomeStructures = structuresToSpawn;
																} else {
																	canSpawnHere = true;
																	biomeStructures = structuresToSpawn1;
																}
															} else {
																biomeStructures = new ArrayList<String>();
															}
															this.structureNamesPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - scanDistance), (chunkCoord.getChunkZ() + i)),biomeStructures);
														}
														if(biomeStructures != null)
														{
															for(String structureToSpawn : biomeStructures)
															{
																if(structureToSpawn.equals(((BO4)currentStructureSpawning[0]).getName()))
																{
																	canSpawnHere = true;
																	break;
																}
															}
														}
													}
													if(!canSpawnHere)
													{
														leftEdgeFound = true;
													}
												}
												if(!leftEdgeFound)
												{
													left += 1;
												}
											}

											if(!bottomEdgeFound)
											{
												// Find more free chunks until both height and width fit, just in case
												// we can't fit length or width in the x direction.
												if(bottom + top + 1 >= structureLength && bottom + top + 1 >= structureWidth)
												{
													bottomEdgeFound = true; // topEdgeFound is already true, since left and right will never spawn in the same pass.
												}
											}
											if(!bottomEdgeFound)
											{
												// Check Bottom
												for(int i = -left; i <= right; i++)
												{
													canSpawnHere = false;
													if(targetStructure != null)
													{
														if(!isBo4ChunkPlotted(chunkCoord) && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
														{
															if(targetBiomes.size() == 0)
															{
																canSpawnHere = true;
															} else {
																biomeConfig3 = worldGenRegion.getBiomeConfig((chunkCoord.getChunkX() + i) * 16 + DecorationArea.BO_CHUNK_CENTER_X, (chunkCoord.getChunkZ() + scanDistance) * 16 + DecorationArea.BO_CHUNK_CENTER_Z);
																if(targetBiomes.contains(biomeConfig3.getName()))
																{
																	canSpawnHere = true;
																}
															}
														}
													} else {
														// When we get biomestructures here, size() == 0 means the chunk has been handled, null means it hasnt yet been cached at all
														biomeStructures = this.structureNamesPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + scanDistance)));
														if(biomeStructures == null)
														{
															if(!isBo4ChunkPlotted(chunkCoord) && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
															{
																biomeConfig3 = worldGenRegion.getBiomeConfig((chunkCoord.getChunkX() + i) * 16 + DecorationArea.BO_CHUNK_CENTER_X, (chunkCoord.getChunkZ() + scanDistance) * 16 + DecorationArea.BO_CHUNK_CENTER_Z);
																if(!biomeConfig3.getName().equals(biomeConfig.getName()))
																{
																	structuresToSpawn = new ArrayList<String>();

																	// Get Bo3's for this biome
																	for (List<String> res : biomeConfig3.getCustomStructureNames())
																	{
																		for(String bo4Name : res)
																		{
																			structuresToSpawn.add(bo4Name);
																		}
																	}

																	biomeStructures = structuresToSpawn;
																} else {
																	canSpawnHere = true;
																	biomeStructures = structuresToSpawn1;
																}
															} else {
																biomeStructures = new ArrayList<String>();
															}
															this.structureNamesPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + scanDistance)),biomeStructures);
														}
														if(biomeStructures != null)
														{
															for(String structureToSpawn : biomeStructures)
															{
																if(structureToSpawn.equals(((BO4)currentStructureSpawning[0]).getName()))
																{
																	canSpawnHere = true;
																	break;
																}
															}
														}
													}
													if(!canSpawnHere)
													{
														bottomEdgeFound = true;
													}
												}
												if(!bottomEdgeFound)
												{
													bottom += 1;
												}
											}

											if(!topEdgeFound)
											{
												// Find more free chunks until both height and width fit, just in case
												// we can't fit length or width in the x direction.
												if(bottom + top + 1 >= structureLength && bottom + top + 1 >= structureWidth)
												{
													topEdgeFound = true; // bottomEdgeFound is already true, since left and right will never spawn in the same pass.
												}
											}
											if(!topEdgeFound)
											{
												// Check Top
												for(int i = -left; i <= right; i++)
												{
													canSpawnHere = false;
													if(targetStructure != null)
													{
														if(!isBo4ChunkPlotted(chunkCoord) && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
														{
															if(targetBiomes.size() == 0)
															{
																canSpawnHere = true;
															} else {
																biomeConfig3 = worldGenRegion.getBiomeConfig((chunkCoord.getChunkX() + i) * 16 + DecorationArea.BO_CHUNK_CENTER_X, (chunkCoord.getChunkZ() - scanDistance) * 16 + DecorationArea.BO_CHUNK_CENTER_Z);
																if(targetBiomes.contains(biomeConfig3.getName()))
																{
																	canSpawnHere = true;
																}
															}
														}
													} else {
														// When we get biomestructures here, size() == 0 means the chunk has been handled, null means it hasnt yet been cached at all
														biomeStructures = this.structureNamesPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - scanDistance)));
														if(biomeStructures == null)
														{
															if(!isBo4ChunkPlotted(chunkCoord) && !worldGenRegion.chunkHasDefaultStructure(rand, chunkCoord))
															{
																biomeConfig3 = worldGenRegion.getBiomeConfig((chunkCoord.getChunkX() + i) * 16 + DecorationArea.BO_CHUNK_CENTER_X, (chunkCoord.getChunkZ() - scanDistance) * 16 + DecorationArea.BO_CHUNK_CENTER_Z);
																if(!biomeConfig3.getName().equals(biomeConfig.getName()))
																{
																	structuresToSpawn = new ArrayList<String>();

																	// Get Bo4's for this biome
																	for (List<String> res : biomeConfig3.getCustomStructureNames())
																	{
																		for(String bo4Name : res)
																		{
																			structuresToSpawn.add(bo4Name);
																		}
																	}																			
																	biomeStructures = structuresToSpawn;
																} else {
																	canSpawnHere = true;
																	biomeStructures = structuresToSpawn1;
																}
															} else {
																biomeStructures = new ArrayList<String>();
															}
															this.structureNamesPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - scanDistance)),biomeStructures);
														}
														if(biomeStructures != null)
														{
															for(String structureToSpawn : biomeStructures)
															{
																if(structureToSpawn.equals(((BO4)currentStructureSpawning[0]).getName()))
																{
																	canSpawnHere = true;
																	break;
																}
															}
														}
													}
													if(!canSpawnHere)
													{
														topEdgeFound = true;
													}
												}
												if(!topEdgeFound)
												{
													top += 1;
												}
											}
										}
									} else {
										// When spawning structure at spawn, spawn the structure centered around the spawn point, all chunks should be available.
										// TODO: Put the start BO4 at the spawn point. 
										left = (int) Math.ceil(((Integer)topLeftAndLowerRightChunkCoordinates[3] + (Integer)topLeftAndLowerRightChunkCoordinates[1]) / 2d);
										right = (int) Math.floor(((Integer)topLeftAndLowerRightChunkCoordinates[3] + (Integer)topLeftAndLowerRightChunkCoordinates[1]) / 2d);
										top = (int) Math.ceil(((Integer)topLeftAndLowerRightChunkCoordinates[0] + (Integer)topLeftAndLowerRightChunkCoordinates[2]) / 2d);
										bottom = (int) Math.floor(((Integer)topLeftAndLowerRightChunkCoordinates[0] + (Integer)topLeftAndLowerRightChunkCoordinates[2]) / 2d);
									}

									areaLength = left + right + 1;
									areaWidth = top + bottom + 1;

									// See if the structure will fit
									if(
										(structureLength <= areaLength && structureWidth <= areaWidth) ||
										(structureLength <= areaWidth && structureWidth <= areaLength)
									)
									{
										// Determine the coordinates of the start BO4 and spawn the structure.
										// Make sure the current chunk is inside the structure's bounding box
										// and take into account rotation. Center the structure in the available 
										// area as much as possible, this should give structures with randomised 
										// branches room to expand in all directions. Hopefully this won't cause 
										// large structures to spawn less frequently because of space (placing one 
										// structure in the middle of an area vs spawning two structures side by side). 
										// This won't guarantee that the structure will spawn a branch in this chunk,
										// so we may have to try to spawn more structures afterwards.
										
										// Get rotated width/length and start chunk.
										
										boolean canSpawnUnrotated = false;
										boolean canSpawnRotated = false;
										if(structureLength <= areaLength && structureWidth <= areaWidth)
										{
											canSpawnUnrotated = true;
										}
										if(structureLength <= areaWidth && structureWidth <= areaLength)
										{
											canSpawnRotated = true;	
										}
																				
										Rotation rotation = rand.nextBoolean() ? Rotation.NORTH : Rotation.SOUTH;
										if(canSpawnUnrotated && canSpawnRotated)
										{
											rotation = Rotation.getRandomRotation(rand);
										}
										else if(canSpawnRotated)
										{
											rotation = rand.nextBoolean() ? Rotation.EAST : Rotation.WEST;
										}
										
										int structureLengthRotated = 0;
										int structureWidthRotated = 0;											
										if(rotation == Rotation.NORTH || rotation == Rotation.SOUTH)
										{
											structureLengthRotated = structureLength;
											structureWidthRotated = structureWidth;
										} else {
											structureLengthRotated = structureWidth;
											structureWidthRotated = structureLength;
										}
										
										// Fit the bounding box of the structure in the center of the available area (as much as possible),
										// making sure that the current chunk is inside the structure

										int structureBBInsideAreaX = (int)Math.floor(chunkCoord.getChunkX() - left + ((left + right + 1) / 2d) - (structureLengthRotated / 2d));
										if(structureBBInsideAreaX > chunkCoord.getChunkX())
										{
											structureBBInsideAreaX = chunkCoord.getChunkX();
										}
										else if(structureBBInsideAreaX + structureLengthRotated < chunkCoord.getChunkX())
										{
											structureBBInsideAreaX = chunkCoord.getChunkX() - structureLengthRotated + 1;
										}
										
										int structureBBInsideAreaZ = (int)Math.floor(chunkCoord.getChunkZ() - top + ((top + bottom + 1) / 2d) - (structureWidthRotated / 2d));
										if(structureBBInsideAreaZ > chunkCoord.getChunkZ())
										{
											structureBBInsideAreaZ = chunkCoord.getChunkZ();
										}
										else if(structureBBInsideAreaZ + structureWidthRotated < chunkCoord.getChunkZ())
										{
											structureBBInsideAreaZ = chunkCoord.getChunkZ() - structureWidthRotated + 1;
										}

										// Find the world coords of the structure start inside the rotated bounding box.											
										int spawnCoordX = structureBBInsideAreaX + (rotation == Rotation.NORTH ? structureLeft : rotation == Rotation.EAST ? structureBottom : rotation == Rotation.SOUTH ? structureRight : structureTop);
										int spawnCoordZ = structureBBInsideAreaZ + (rotation == Rotation.NORTH ? structureTop : rotation == Rotation.EAST ? structureLeft : rotation == Rotation.SOUTH ? structureBottom : structureRight);
										ChunkCoordinate spawnChunk = ChunkCoordinate.fromChunkCoords(spawnCoordX, spawnCoordZ);

										if(isBO4AllowedToSpawnAtByFrequency(spawnChunk, (BO4)currentStructureSpawning[0]))
										{
											structureCoord = new BO4CustomStructureCoordinate(worldGenRegion.getPresetFolderName(), ((BO4)currentStructureSpawning[0]), null, rotation, spawnCoordX * 16, (short)0, spawnCoordZ * 16, 0, false, false, null);
											structureStart2 = new BO4CustomStructure(structureCache, worldGenRegion, structureCoord, spawningStructureAtSpawn, force, targetBiomes, chunkCoord, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);

											if(structureStart2.isSpawned())
											{
												structureCache.addBo4ToStructureCache(spawnChunk, structureStart2);

												BO4 structureCoordConfig = ((BO4)structureCoord.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker));
												
												structureCoordConfig.getConfig().timesSpawned += 1;
												if(logger.getSpawnLogEnabled())
												{
													logger.log(LogMarker.INFO, "Plotted structure " + structureCoordConfig.getName() + " at chunk " + spawnCoordX + " " + spawnCoordZ + " ("+ (spawnCoordX * 16) + " 100 " + (spawnCoordZ * 16) + ")");// + " biome " + biome3.getName());
												}

												if(((BO4)currentStructureSpawning[0]).getConfig().frequency > 0 || ((BO4)currentStructureSpawning[0]).getConfig().bo4Groups.size() > 0)
												{
													String bO3Name = ((BO4)currentStructureSpawning[0]).getName();
													ChunkCoordinate bo4SpawnCoord = ChunkCoordinate.fromChunkCoords(spawnCoordX, spawnCoordZ);

													ArrayList<ChunkCoordinate> chunkCoords = this.spawnedStructuresByName.get(bO3Name);
													if(chunkCoords == null)
													{
														chunkCoords = new ArrayList<ChunkCoordinate>();
														this.spawnedStructuresByName.put(bO3Name, chunkCoords);
													}
													chunkCoords.add(bo4SpawnCoord);

													if(((BO4)currentStructureSpawning[0]).getConfig().bo4Groups.size() > 0)
													{
														int structureCenterX = structureBBInsideAreaX + (int)Math.floor(((rotation == Rotation.NORTH || rotation == Rotation.SOUTH ? structureLeft + structureRight + 1 : structureBottom + structureTop + 1) / 2d));
														int structureCenterZ = structureBBInsideAreaZ + (int)Math.floor(((rotation == Rotation.NORTH || rotation == Rotation.SOUTH ? structureTop + structureBottom + 1 : structureLeft + structureRight + 1) / 2d));
														ChunkCoordinate bo4CenterSpawnCoord = ChunkCoordinate.fromChunkCoords(structureCenterX, structureCenterZ);
														
														for(Entry<String, Integer> entry : ((BO4)currentStructureSpawning[0]).getConfig().bo4Groups.entrySet())
														{
															String bo4GroupName = entry.getKey();
															int bo4GroupFrequency = entry.getValue().intValue();
															if(bo4GroupFrequency > 0)
															{
																HashMap<ChunkCoordinate, Integer> spawnedStructures = this.spawnedStructuresByGroup.get(bo4GroupName);
																if(spawnedStructures == null)
																{
																	spawnedStructures = new HashMap<ChunkCoordinate, Integer>();
																	spawnedStructures.put(bo4CenterSpawnCoord, entry.getValue());
																	this.spawnedStructuresByGroup.put(bo4GroupName, spawnedStructures);
																} else {
																	Integer frequency = spawnedStructures.get(bo4CenterSpawnCoord);
																	if(frequency != null)
																	{
																		if(frequency.intValue() < bo4GroupFrequency)
																		{
																			spawnedStructures.put(bo4CenterSpawnCoord, entry.getValue().intValue());
																		}
																	} else {
																		spawnedStructures.put(bo4CenterSpawnCoord, entry.getValue().intValue());
																	}
																}
															}
														}
													}
												}

												// Even though we made sure the structure's bounding box contained the current chunk,
												// the structure may not have spawned a branch on the current chunk. If so, try to 
												// spawn more structures.
												// If we're plotting a target structure via /otg spawn, then the chunk isn't being decorated
												// so it's okay if the structure didn't get plotted on this chunk.
												if(structureCacheContainsKey(chunkCoord) || targetStructure != null)
												{
													this.processing = false;
													return spawnChunk;
												}
												break;
											}
										}
										// We've found an area big enough and tried to spawn the structure, so stop 
										// scanning regardless of whether the structure actually spawned.
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		
		// Set the chunk to plotted so we know not to override it.
		this.addToPlottedChunks(chunkCoord);
		// Let plotter know the chunk is taken (fast cache, optimisation)
		setChunkOccupied(chunkCoord);
		
		this.processing = false;
		
		return null;
	}

	private boolean isBO4AllowedToSpawnAtByFrequency(ChunkCoordinate chunkCoord, BO4 BO3ToSpawn)
	{
		// Check if no other structure of the same type (filename) is within the minimum radius (BO3 frequency)
		int radius = BO3ToSpawn.getConfig().frequency;
		String bO3Name = BO3ToSpawn.getName();
		if(radius > 0)
		{
			float distanceBetweenStructures = 0;
			
			ArrayList<ChunkCoordinate> chunkCoords = spawnedStructuresByName.get(bO3Name);
			if(chunkCoords != null)
			{
				// Check BO3 frequency
					for(ChunkCoordinate cachedChunk : chunkCoords)
				{
					// Find distance between two points
					distanceBetweenStructures = (int)Math.floor(Math.sqrt(Math.pow(chunkCoord.getChunkX() - cachedChunk.getChunkX(), 2) + Math.pow(chunkCoord.getChunkZ() - cachedChunk.getChunkZ(), 2)));
					if (distanceBetweenStructures <= radius)
					{
						// Other BO3 of the same type is too nearby, cannot spawn here!
						return false;
					}
				}				
			}
		}
		
		// Check if no other structures that are a member of the same group as this BO3 are within the minimum radius (BO3Group frequency)
		if(BO3ToSpawn.getConfig().bo4Groups.size() > 0)
		{
			float distanceBetweenStructures = 0;
			int cachedChunkRadius = 0;
			ChunkCoordinate cachedChunk = null;
			for(Entry<String, Integer> entry : BO3ToSpawn.getConfig().bo4Groups.entrySet())
			{
				HashMap<ChunkCoordinate, Integer> spawnedStructure = spawnedStructuresByGroup.get(entry.getKey());
				if(spawnedStructure != null)
				{
					for(Entry<ChunkCoordinate, Integer> cachedChunkEntry : spawnedStructure.entrySet())
					{
						cachedChunk = cachedChunkEntry.getKey();
						cachedChunkRadius = cachedChunkEntry.getValue().intValue();
						radius = entry.getValue().intValue() >= cachedChunkRadius ? entry.getValue().intValue() : cachedChunkRadius;
						// Find distance between two points
						distanceBetweenStructures = (int)Math.floor(Math.sqrt(Math.pow(chunkCoord.getChunkX() - cachedChunk.getChunkX(), 2) + Math.pow(chunkCoord.getChunkZ() - cachedChunk.getChunkZ(), 2)));
						if (distanceBetweenStructures <= radius)
						{
							// Other BO3 using a shared BO3Group is too nearby, cannot spawn here!
							return false;
						}
					}
				}
			}
		}

		return true;
	}
	
	// Persistence

	private void savePlottedChunks(Path worldSaveDir, int dimensionId, ILogger logger)
	{
		CustomStructureFileManager.savePlottedChunksData(worldSaveDir, dimensionId, this.plottedChunks, logger);
	}

	private void loadPlottedChunks(Path worldSaveDir, int dimensionId, ILogger logger)
	{
		this.plottedChunks.clear();
		this.plottedChunks.putAll(CustomStructureFileManager.loadPlottedChunksData(worldSaveDir, dimensionId, logger));
	}
	
	private void saveSpawnedStructures(Path worldSaveDir, int dimensionId, ILogger logger)
	{
		CustomStructureFileManager.saveChunksMapFile(worldSaveDir, dimensionId, this.spawnedStructuresByName, this.spawnedStructuresByGroup, logger);
	}
	
	private void loadSpawnedStructures(Path worldSaveDir, int dimensionId, boolean isBO4Enabled, ILogger logger)
	{
		CustomStructureFileManager.loadChunksMapFile(worldSaveDir, dimensionId, isBO4Enabled, this.spawnedStructuresByName, this.spawnedStructuresByGroup, logger);		
	}
	
	public void saveStructureCache(Path worldSaveDir, int dimensionId, boolean isBO4Enabled, ILogger logger)
	{
		if(isBO4Enabled)
		{
			savePlottedChunks(worldSaveDir, dimensionId, logger);
			saveSpawnedStructures(worldSaveDir, dimensionId, logger);
		}
	}

	public void loadStructureCache(Path worldSaveDir, int dimensionId, boolean isBO4Enabled, Map<CustomStructure, ArrayList<ChunkCoordinate>> loadedStructures, ILogger logger)
	{
		this.bo4StructureCache.clear();
		
		if(isBO4Enabled)
		{
			if(loadedStructures != null)
			{
				for(Entry<CustomStructure, ArrayList<ChunkCoordinate>> loadedStructure : loadedStructures.entrySet())
				{
					if(loadedStructure == null)
					{
						throw new RuntimeException("This shouldn't happen, please ask for help on the OTG Discord and/or file an issue on the OTG github.");
					}
				
					// loadedStructures contains chunkcoords for every chunk ever plotted.
					// We only need chunks plotted but not yet decorated that contain structure parts.
					// objectsToSpawn and smoothingAreasToSpawn contain all unspawned branches and 
					// smoothing areas. Any chunks that have had their bo4's spawned while decorating a neighbouring 
					// chunk, but have not yet been fully decorated themselves are kept in decoratedChunks, along with all
					// fully decorated chunks (they are considered fully decorated for the purposes of bo4 plotting).
					for(ChunkCoordinate chunkCoord : ((BO4CustomStructure)loadedStructure.getKey()).getObjectsToSpawn().keySet())
					{
						this.addToStructureCache(chunkCoord, (BO4CustomStructure)loadedStructure.getKey()); // This structure has blocks that need to be spawned
					}
					for(ChunkCoordinate chunkCoord : ((BO4CustomStructure)loadedStructure.getKey()).getSmoothingAreaManager().getSmoothingAreaChunkCoords())
					{
						this.addToStructureCache(chunkCoord, (BO4CustomStructure)loadedStructure.getKey()); // This structure has smoothing area blocks that need to be spawned
					}
				}
			}

			loadPlottedChunks(worldSaveDir, dimensionId, logger);
			loadSpawnedStructures(worldSaveDir, dimensionId, isBO4Enabled, logger);

			for(ChunkCoordinate chunkCoord : this.bo4StructureCache.keySet())
			{
				// This is an optimisation so that PlotStructures knows not to plot anything in this chunk
				// TODO: This could easily exceed the capacity of the StructuresPerChunkCache, mostly defeating the point?
				setChunkOccupied(chunkCoord);
			}
		}
		logger.log(LogMarker.DEBUG, "Loading done");
	}	
}
