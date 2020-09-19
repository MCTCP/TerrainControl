package com.pg85.otg.customobjects.structures.bo4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.structures.CustomStructure;
import com.pg85.otg.customobjects.structures.CustomStructureFileManager;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructure;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.bo3.Rotation;

public class CustomStructurePlotter
{
	private Object processingLock = new Object();
    private boolean processing = false;
	private FifoMap<ChunkCoordinate, ArrayList<String>> structuresPerChunk; // Used as a cache by the plotting code
	
	// Used to find distance between structures and structure groups, only stores 1 chunk per structure in the 
	// calculated center of the structure. Does not clean itself when used with the pre-generator and will become 
	// slower as it fills up, use as little as possible! (can't clean itself because max radius for BO4 groups cannot be known)	
	private HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName;  // group name -> start chunk coords
	private HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup; // group name -> chunkCoord, radius 
	
	public CustomStructurePlotter()
	{
		this.structuresPerChunk = new FifoMap<ChunkCoordinate, ArrayList<String>>(2048);
        this.spawnedStructuresByName = new HashMap<String, ArrayList<ChunkCoordinate>>();
        this.spawnedStructuresByGroup = new HashMap<String, HashMap<ChunkCoordinate, Integer>>();
	}
	
	public int getStructureCount()
	{
		return spawnedStructuresByName.entrySet().size();
	}
	
	public void saveSpawnedStructures(LocalWorld world)
	{
		CustomStructureFileManager.saveChunksMapFile(world, this.spawnedStructuresByName, spawnedStructuresByGroup);
	}
	
	public void loadSpawnedStructures(LocalWorld world)
	{		
		CustomStructureFileManager.loadChunksMapFile(world, this.spawnedStructuresByName, this.spawnedStructuresByGroup);		
	}
	
	public void invalidateChunkInStructuresPerChunkCache(ChunkCoordinate chunkCoord)
	{
		this.structuresPerChunk.put(chunkCoord, null);
	}	
	
	public void plotStructures(LocalWorld world, Random rand, ChunkCoordinate chunkCoord, boolean spawningStructureAtSpawn, Map<ChunkCoordinate, BO4CustomStructure> structureCache, Map<ChunkCoordinate, CustomStructure> worldInfoChunks)
	{
		plotStructures(null, null, world, rand, chunkCoord, spawningStructureAtSpawn, structureCache, worldInfoChunks);
	}
	
    public boolean plotStructures(BO4 targetStructure, ArrayList<String> targetBiomes, LocalWorld world, Random rand, ChunkCoordinate chunkCoord, boolean spawningStructureAtSpawn, Map<ChunkCoordinate, BO4CustomStructure> structureCache, Map<ChunkCoordinate, CustomStructure> worldInfoChunks)
    {
    	// This method can be called by /otg spawn and during chunkgeneration.
    	// When called during chunkgeneration, the chunk must be filled or invalidated before returning, so never cancel.
    	// When called by /otg spawn, skip this attempt to spawn and let chunk generation complete first.
    	// This method should never be called recursively.
    	// TODO: Make sure this never gets stuck, or abort and log a warning.
    	// TODO: Can this thread be paused without issues, or for how long?
    	while(true)
    	{
        	synchronized(processingLock)
        	{
        		if(!processing)
        		{
        			processing = true;
        			break;
        		}        		
        		else if(targetStructure != null)
    	    	{
    	    		return false;
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
    	
    	// Try to spawn a structure that has a branch in this chunk.
        if (!world.isInsidePregeneratedRegion(chunkCoord) && !structureCache.containsKey(chunkCoord))
        {
            LocalBiome biome = world.getBiome(chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 7);
            BiomeConfig biomeConfig = biome.getBiomeConfig();
            
            ArrayList<CustomStructureGen> customStructureGens = new ArrayList<CustomStructureGen>();
        	if(targetStructure == null && !world.chunkHasDefaultStructure(rand, chunkCoord))
        	{
	            // Get Bo4's for this biome
                for (CustomStructureGen res : biomeConfig.getCustomStructures())
                {
                	//TODO: Check for res.error?
                	customStructureGens.add(res);
                }
        	}
            if(targetStructure != null || customStructureGens.size() > 0)
            {
            	Map<StructuredCustomObject, Double> structuredCustomObjects = new HashMap<StructuredCustomObject, Double>();
            	if(targetStructure != null)
            	{
            		if(targetBiomes.size() == 0 || targetBiomes.contains(biome.getName()))
            		{
            			structuredCustomObjects.put(targetStructure, 100.0);
            		}
            	} else {
	        		for(CustomStructureGen structureGen : customStructureGens)
	        		{
	        			int i = 0;
	        			for(StructuredCustomObject structure : structureGen.getObjects(world.getName()))
	        			{
	        				if(structure != null) // Structure was in resource list but file could not be found. TODO: Make this prettier!
	        				{
	        					if(structure instanceof BO4)
	        					{
	        						structuredCustomObjects.put(structure, structureGen.objectChances.get(i));
	        						i += 1;
	        					} else if(OTG.getPluginConfig().spawnLog) {
	        						OTG.log(LogMarker.WARN, "CustomStructure " + structure.getName() + " in biome " + biome.getName() + " has IsOTGPlus:false, ignoring.");
	        					}
	        				}
	        			}
	        		}
            	}

        		if(structuredCustomObjects.size() > 0)
        		{
	            	BO4CustomStructureCoordinate structureCoord = null;
	            	BO4CustomStructure structureStart2 = null;

	            	ArrayList<Object[]> BO3sBySize = new ArrayList<Object[]>();
	            	ArrayList<String> structuresToSpawn1 = new ArrayList<String>();

	            	// Get list of BO3's that should spawn at the spawn point
	            	if(targetStructure == null && spawningStructureAtSpawn)
	            	{
		            	for(Map.Entry<StructuredCustomObject, Double> bo3AndRarity : structuredCustomObjects.entrySet())
		            	{
		            		if(!((BO4)bo3AndRarity.getKey()).isInvalidConfig && ((BO4)bo3AndRarity.getKey()).getConfig().isSpawnPoint)
		            		{
			            		structuresToSpawn1.add(bo3AndRarity.getKey().getName());
			                	structureCoord = new BO4CustomStructureCoordinate(world, bo3AndRarity.getKey(), null, Rotation.NORTH, chunkCoord.getBlockX(), (short)0, chunkCoord.getBlockZ(), 0, false, false, null);
			                	structureStart2 = new BO4CustomStructure(world, structureCoord);
			                	// Get minimum size (size if spawned with branchDepth 0)

			                	try {
			                		Object[] topLeftAndLowerRightChunkCoordinates = structureStart2.getMinimumSize(world);
				                	double BO3size = Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[0] - -(Integer)topLeftAndLowerRightChunkCoordinates[2]) * Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[1] - -(Integer)topLeftAndLowerRightChunkCoordinates[3]);
			                		BO3sBySize.add(new Object[]{ bo3AndRarity.getKey(), topLeftAndLowerRightChunkCoordinates, BO3size, bo3AndRarity.getValue() });
								}
			                	catch (InvalidConfigException e)
			                	{
									((BO4)bo3AndRarity.getKey()).isInvalidConfig = true;
								}
		            		}
		            	}
	            	}
	            	if(!spawningStructureAtSpawn || BO3sBySize.size() == 0)
	            	{
		            	// Get list of BO3's able to spawn in this chunk
		            	for(Map.Entry<StructuredCustomObject, Double> bo3AndRarity : structuredCustomObjects.entrySet())
		            	{
		            		if(!((BO4)bo3AndRarity.getKey()).isInvalidConfig && (int)Math.round(bo3AndRarity.getValue()) > 0)
		            		{
		            			// TODO: avoid calling IsBO3AllowedToSpawnAt so much, cache and reuse any nearest group members found

		            			if(isBO4AllowedToSpawnAtByFrequency(chunkCoord, ((BO4)bo3AndRarity.getKey())))
		            			{
				            		structuresToSpawn1.add(bo3AndRarity.getKey().getName());
				                	structureCoord = new BO4CustomStructureCoordinate(world, bo3AndRarity.getKey(), null, Rotation.NORTH, chunkCoord.getBlockX(), (short)0, chunkCoord.getBlockZ(), 0, false, false, null);
				                	structureStart2 = new BO4CustomStructure(world, structureCoord);
				                	// Get minimum size (size if spawned with branchDepth 0)

				                	try {
				                		Object[] topLeftAndLowerRightChunkCoordinates = structureStart2.getMinimumSize(world);
					                	double BO3size = Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[0] - -(Integer)topLeftAndLowerRightChunkCoordinates[2]) * Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[1] - -(Integer)topLeftAndLowerRightChunkCoordinates[3]);
					            		int insertAtIndex = BO3sBySize.size();
					            		int i = 0;
					                	for(Object[] entry : BO3sBySize)
					                	{
					                		if(((BO4)bo3AndRarity.getKey()).getConfig().timesSpawned < ((BO4)entry[0]).getConfig().timesSpawned || (BO3size > (Double)entry[2] && ((BO4)bo3AndRarity.getKey()).getConfig().timesSpawned == ((BO4)entry[0]).getConfig().timesSpawned))
					                		{
					                			insertAtIndex = i;
					                			break;
					                		}
					                		i += 1;
					                	}
				                		BO3sBySize.add(insertAtIndex, new Object[]{ bo3AndRarity.getKey(), topLeftAndLowerRightChunkCoordinates, BO3size, bo3AndRarity.getValue() });
									}
				                	catch (InvalidConfigException e)
				                	{
										((BO4)bo3AndRarity.getKey()).isInvalidConfig = true;
									}
		            			}
		            		}
		            	}
	            	}

	            	if(BO3sBySize.size() > 0)
	            	{
		            	// Go over the list and try to spawn each structure, from largest to smallest.
		        		for(Object[] currentStructureSpawning : BO3sBySize)
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
			            			LocalBiome biome3;
			            			BiomeConfig biomeConfig3;
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
					            					if(!world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + scanDistance, chunkCoord.getChunkZ() + i)))
			            							{
					            						if(targetStructure != null)
					            						{
					            							if(!world.chunkHasDefaultStructure(rand, chunkCoord))
					            							{
					            								if(targetBiomes.size() == 0)
					            								{
					            									canSpawnHere = true;
					            								} else {
					            									biome3 = world.getBiome((chunkCoord.getChunkX() + scanDistance) * 16 + 8, (chunkCoord.getChunkZ() + i) * 16 + 7);
					            									if(targetBiomes.contains(biome3.getName()))
					            									{
					            										canSpawnHere = true;
					            									}
					            								}
					            							}
					            						} else {
					            							biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + scanDistance), (chunkCoord.getChunkZ() + i)));
								        	                
					            							// StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here
								        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
								            				if(biomeStructures == null && !structuresPerChunk.containsKey(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + scanDistance), (chunkCoord.getChunkZ() + i))))
								        	            	{
							            						if(!world.chunkHasDefaultStructure(rand, chunkCoord))
							            						{
									            					biome3 = world.getBiome((chunkCoord.getChunkX() + scanDistance) * 16 + 8, (chunkCoord.getChunkZ() + i) * 16 + 7);
									            					// Get cached data if available
									            					if(!biome3.getName().equals(biome.getName()))
										            				{
											        	                biomeConfig3 = biome3.getBiomeConfig();
											        	                structuresToSpawn = new ArrayList<String>();

											        	                // Get Bo3's for this biome
											        	                for (CustomStructureGen res : biomeConfig3.getCustomStructures())
											        	                {
										        	                		for(String bo3Name : res.objectNames)
										        	                		{
										        	                			structuresToSpawn.add(bo3Name);
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
						            							structuresPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + scanDistance), (chunkCoord.getChunkZ() + i)),biomeStructures);
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
						        	            	} else {
						        	            		structuresPerChunk.remove(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + scanDistance), (chunkCoord.getChunkZ() + i)));
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
					            					if(!world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() - scanDistance, chunkCoord.getChunkZ() + i)))
					            					{
					            						if(targetStructure != null)
					            						{
					            							if(!world.chunkHasDefaultStructure(rand, chunkCoord))
					            							{
					            								if(targetBiomes.size() == 0)
					            								{
					            									canSpawnHere = true;
					            								} else {
					            									biome3 = world.getBiome((chunkCoord.getChunkX() - scanDistance) * 16 + 8, (chunkCoord.getChunkZ() + i) * 16 + 7);
					            									if(targetBiomes.contains(biome3.getName()))
					            									{
					            										canSpawnHere = true;
					            									}
					            								}
					            							}
					            						} else {
								        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here
								        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
								        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - scanDistance), (chunkCoord.getChunkZ() + i)));
							            					if(biomeStructures == null && !structuresPerChunk.containsKey(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - scanDistance), (chunkCoord.getChunkZ() + i))))
								        	            	{
								            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
								            					{
									            					biome3 = world.getBiome((chunkCoord.getChunkX() - scanDistance) * 16 + 8, (chunkCoord.getChunkZ() + i) * 16 + 7);
									            					if(!biome3.getName().equals(biome.getName()))
										            				{
											        	                biomeConfig3 = biome3.getBiomeConfig();
											        	                structuresToSpawn = new ArrayList<String>();

											        	                // Get Bo3's for this biome
											        	                for (CustomStructureGen res : biomeConfig3.getCustomStructures())
											        	                {
										        	                		for(String bo3Name : res.objectNames)
										        	                		{
										        	                			structuresToSpawn.add(bo3Name);
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
								            					structuresPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - scanDistance), (chunkCoord.getChunkZ() + i)),biomeStructures);
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
						        	            	} else {
						        	            		structuresPerChunk.remove(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - scanDistance), (chunkCoord.getChunkZ() + i)));
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
					            					if(!world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + i, chunkCoord.getChunkZ() + scanDistance)))
					            					{
					            						if(targetStructure != null)
					            						{
					            							if(!world.chunkHasDefaultStructure(rand, chunkCoord))
					            							{
					            								if(targetBiomes.size() == 0)
					            								{
					            									canSpawnHere = true;
					            								} else {
					            									biome3 = world.getBiome((chunkCoord.getChunkX() + i) * 16 + 8, (chunkCoord.getChunkZ() + scanDistance) * 16 + 7);
					            									if(targetBiomes.contains(biome3.getName()))
					            									{
					            										canSpawnHere = true;
					            									}
					            								}
					            							}
					            						} else {
								        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here
								        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
								        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + scanDistance)));
							            					if(biomeStructures == null && !structuresPerChunk.containsKey(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + scanDistance))))
								        	            	{
								            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
								            					{
									            					biome3 = world.getBiome((chunkCoord.getChunkX() + i) * 16 + 8, (chunkCoord.getChunkZ() + scanDistance) * 16 + 7);
									            					if(!biome3.getName().equals(biome.getName()))
										            				{
											        	                biomeConfig3 = biome3.getBiomeConfig();
											        	                structuresToSpawn = new ArrayList<String>();

											        	                // Get Bo3's for this biome
											        	                for (CustomStructureGen res : biomeConfig3.getCustomStructures())
											        	                {
										        	                		for(String bo3Name : res.objectNames)
										        	                		{
										        	                			structuresToSpawn.add(bo3Name);
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
								            					structuresPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + scanDistance)),biomeStructures);
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
						        	            	} else {
						        	            		structuresPerChunk.remove(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + scanDistance)));
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
					            					if(!world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + i, chunkCoord.getChunkZ() - scanDistance)))
					            					{
					            						if(targetStructure != null)
					            						{
					            							if(!world.chunkHasDefaultStructure(rand, chunkCoord))
					            							{
					            								if(targetBiomes.size() == 0)
					            								{
					            									canSpawnHere = true;
					            								} else {
					            									biome3 = world.getBiome((chunkCoord.getChunkX() + i) * 16 + 8, (chunkCoord.getChunkZ() - scanDistance) * 16 + 7);
					            									if(targetBiomes.contains(biome3.getName()))
					            									{
					            										canSpawnHere = true;
					            									}
					            								}
					            							}
					            						} else {
								        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here
								        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
								        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - scanDistance)));
							            					if(biomeStructures == null && !structuresPerChunk.containsKey(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - scanDistance))))
								        	            	{
								            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
								            					{
									            					biome3 = world.getBiome((chunkCoord.getChunkX() + i) * 16 + 8, (chunkCoord.getChunkZ() - scanDistance) * 16 + 7);
									            					if(!biome3.getName().equals(biome.getName()))
										            				{
											        	                biomeConfig3 = biome3.getBiomeConfig();
											        	                structuresToSpawn = new ArrayList<String>();

											        	                // Get Bo3's for this biome
											        	                for (CustomStructureGen res : biomeConfig3.getCustomStructures())
											        	                {
										        	                		for(String bo3Name : res.objectNames)
										        	                		{
										        	                			structuresToSpawn.add(bo3Name);
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
								            					structuresPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - scanDistance)),biomeStructures);
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
						        	            	} else {
						        	            		structuresPerChunk.remove(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - scanDistance)));
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
				            			// TODO: Shouldn't it just put the start BO4 at the spawn point? 
				            			// TODO: Does the spawn structure ignore biome checks completely (it should)?
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
					            		// so we may have to try to spawn more structures.
					            		
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
						                	structureCoord = new BO4CustomStructureCoordinate(world, ((BO4)currentStructureSpawning[0]), null, rotation, spawnCoordX * 16, (short)0, spawnCoordZ * 16, 0, false, false, null);
						                	structureStart2 = new BO4CustomStructure(world, structureCoord, spawningStructureAtSpawn, targetBiomes, chunkCoord);

				            	        	if(structureStart2.IsSpawned)
						                	{
				            	        		structureCache.put(spawnChunk, structureStart2);
				            	    			this.structuresPerChunk.put(spawnChunk, null);
				            	    			worldInfoChunks.put(spawnChunk, structureStart2);

						                		((BO4)structureCoord.getObject()).getConfig().timesSpawned += 1;
						                		if(OTG.getPluginConfig().spawnLog)
						                		{
						                			OTG.log(LogMarker.INFO, "Plotted structure " + structureCoord.getObject().getName() + " at chunk X" + spawnCoordX + " Z" + spawnCoordZ + " ("+ (spawnCoordX * 16) + " 100 " + (spawnCoordZ * 16) + ")");// + " biome " + biome3.getName());
						                		}

						                		if(((BO4)currentStructureSpawning[0]).getConfig().frequency > 0 || ((BO4)currentStructureSpawning[0]).getConfig().bo3Groups.size() > 0)
						                		{
						                			String bO3Name = ((BO4)currentStructureSpawning[0]).getName();
						                			ChunkCoordinate bo4SpawnCoord = ChunkCoordinate.fromChunkCoords(spawnCoordX, spawnCoordZ);

						                			ArrayList<ChunkCoordinate> chunkCoords = this.spawnedStructuresByName.get(bO3Name);
					                				if(chunkCoords == null)
							                		{
							                			chunkCoords = new ArrayList<ChunkCoordinate>();
							                			spawnedStructuresByName.put(bO3Name, chunkCoords);
							                		}
					                				chunkCoords.add(bo4SpawnCoord);

						                			if(((BO4)currentStructureSpawning[0]).getConfig().bo3Groups.size() > 0)
						                			{
									            		int structureCenterX = structureBBInsideAreaX + (int)Math.floor(((rotation == Rotation.NORTH || rotation == Rotation.SOUTH ? structureLeft + structureRight + 1 : structureBottom + structureTop + 1) / 2d));
									            		int structureCenterZ = structureBBInsideAreaZ + (int)Math.floor(((rotation == Rotation.NORTH || rotation == Rotation.SOUTH ? structureTop + structureBottom + 1 : structureLeft + structureRight + 1) / 2d));
									                	ChunkCoordinate bo4CenterSpawnCoord = ChunkCoordinate.fromChunkCoords(structureCenterX, structureCenterZ);
						                				
						                				for(Entry<String, Integer> entry : ((BO4)currentStructureSpawning[0]).getConfig().bo3Groups.entrySet())
						                				{
					                						String bo3GroupName = entry.getKey();
					                						int bo3GroupFrequency = entry.getValue().intValue();
					                						if(bo3GroupFrequency > 0)
					                						{
					                							HashMap<ChunkCoordinate, Integer> spawnedStructures = this.spawnedStructuresByGroup.get(bo3GroupName);
					                							if(spawnedStructures == null)
					                							{
					                								spawnedStructures = new HashMap<ChunkCoordinate, Integer>();
					                								spawnedStructures.put(bo4CenterSpawnCoord, entry.getValue());
					                								this.spawnedStructuresByGroup.put(bo3GroupName, spawnedStructures);
					                							} else {
						                							Integer frequency = spawnedStructures.get(bo4CenterSpawnCoord);
						                							if(frequency != null)
						                							{
						                								if(frequency.intValue() < bo3GroupFrequency)
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
						                		// If we're plotting a target structure via /otg spawn, then the chunk isn't being populated
						                		// so it's okay if the structure didn't get plotted on this chunk.
						                		if(structureCache.containsKey(chunkCoord) || targetStructure != null)
						                		{
						                	        processing = false;
						                			return true;
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
        if(!structureCache.containsKey(chunkCoord))
        {
        	structureCache.put(chunkCoord, null);
        }
		structuresPerChunk.put(chunkCoord, null);
		processing = false;
		
    	return false;
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
		if(BO3ToSpawn.getConfig().bo3Groups.size() > 0)
		{
        	float distanceBetweenStructures = 0;
        	int cachedChunkRadius = 0;
        	ChunkCoordinate cachedChunk = null;
        	for(Entry<String, Integer> entry : BO3ToSpawn.getConfig().bo3Groups.entrySet())
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
}
