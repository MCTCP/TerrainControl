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
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.structures.CustomStructure;
import com.pg85.otg.customobjects.structures.CustomStructureFileManager;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructure;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class CustomStructurePlotter
{
    private boolean processing = false;
	private HashMap<ChunkCoordinate, ArrayList<String>> structuresPerChunk; // Used as a cache by the plotting code
	private HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName; // Used to find distance between structures and structure groups, only stores 1 chunk per structure in the calculated center of the structure. Does not clean itself when used with the pre-generator and will become slower as it fills up, use as little as possible! (can't clean itself because max radius for BO3 groups cannot be known)
	private HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup; // Used to find distance between structures and structure groups, only stores 1 chunk per structure in the calculated center of the structure. Does not clean itself when used with the pre-generator and will become slower as it fills up, use as little as possible! (can't clean itself because max radius for BO3 groups cannot be known)
	
	public CustomStructurePlotter()
	{
		this.structuresPerChunk = new HashMap<ChunkCoordinate, ArrayList<String>>();
        this.spawnedStructuresByName = new HashMap<String, ArrayList<ChunkCoordinate>>();
        this.spawnedStructuresByGroup = new HashMap<String, HashMap<ChunkCoordinate, Integer>>();
	}
	
	public int getStructureCount()
	{
		return spawnedStructuresByName.entrySet().size();	
	}
	
	public void saveSpawnedStructures(LocalWorld world)
	{
		CustomStructureFileManager.saveChunksMapFile(WorldStandardValues.SpawnedStructuresFileName, world, this.spawnedStructuresByName, spawnedStructuresByGroup);
	}
	
	public void loadSpawnedStructures(LocalWorld world)
	{		
		CustomStructureFileManager.loadChunksMapFile(WorldStandardValues.SpawnedStructuresFileName, world, this.spawnedStructuresByName, this.spawnedStructuresByGroup);		
	}
	
	public void addToStructuresPerChunkCache(ChunkCoordinate chunkCoord, ArrayList<String> BO3Names)
	{
		this.structuresPerChunk.put(chunkCoord, BO3Names);
	}
		
    public void plotStructures(LocalWorld world, Random rand, ChunkCoordinate chunkCoord, boolean spawningStructureAtSpawn, Map<ChunkCoordinate, BO4CustomStructure> structureCache, Map<ChunkCoordinate, CustomStructure> worldInfoChunks)
    {
    	if(!processing)
    	{
	    	processing = true;
	        if (!world.isInsidePregeneratedRegion(chunkCoord) && !structureCache.containsKey(chunkCoord))
	        {
	            LocalBiome biome = world.getBiome(chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 7);
	            BiomeConfig biomeConfig = biome.getBiomeConfig();
	            ArrayList<CustomStructureGen> customStructureGens = new ArrayList<CustomStructureGen>();

	        	if(!world.chunkHasDefaultStructure(rand, chunkCoord))
	        	{
		            // Get Bo3's for this biome
	                for (CustomStructureGen res : biomeConfig.getCustomStructures())
	                {
	                	//TODO: Check for res.error?
	                	customStructureGens.add(res);
	                }
	        	}

	            if(customStructureGens.size() > 0)
	            {
	            	Random random = new Random();

	            	Map<StructuredCustomObject, Double> structuredCustomObjects = new HashMap<StructuredCustomObject, Double>();
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

	        		if(structuredCustomObjects.size() > 0)
	        		{
		            	Object[] topLeftAndLowerRightChunkCoordinates = null;
		            	BO4CustomStructureCoordinate structureCoord = null;
		            	BO4CustomStructure structureStart2 = null;

		            	ArrayList<Object[]> BO3sBySize = new ArrayList<Object[]>();
		            	ArrayList<String> structuresToSpawn1 = new ArrayList<String>();

		            	// Get list of BO3's that should spawn at the spawn point
		            	if(spawningStructureAtSpawn)
		            	{
			            	for(Map.Entry<StructuredCustomObject, Double> bo3AndRarity : structuredCustomObjects.entrySet())
			            	{
			            		if(!((BO4)bo3AndRarity.getKey()).isInvalidConfig && ((BO4)bo3AndRarity.getKey()).getSettings().isSpawnPoint)
			            		{
				            		structuresToSpawn1.add(bo3AndRarity.getKey().getName());
				                	structureCoord = new BO4CustomStructureCoordinate(world, bo3AndRarity.getKey(), null, Rotation.NORTH, chunkCoord.getBlockX(), (short)0, chunkCoord.getBlockZ(), 0, false, false, null);
				                	structureStart2 = new BO4CustomStructure(world, structureCoord, false, false);
				                	// Get minimum size (size if spawned with branchDepth 0)

				                	try {
										topLeftAndLowerRightChunkCoordinates = structureStart2.getMinimumSize(world);
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

			            			if(isBO3AllowedToSpawnAt(chunkCoord, ((BO4)bo3AndRarity.getKey())))
			            			{
					            		structuresToSpawn1.add(bo3AndRarity.getKey().getName());
					                	structureCoord = new BO4CustomStructureCoordinate(world, bo3AndRarity.getKey(), null, Rotation.NORTH, chunkCoord.getBlockX(), (short)0, chunkCoord.getBlockZ(), 0, false, false, null);
					                	structureStart2 = new BO4CustomStructure(world, structureCoord, false, false);
					                	// Get minimum size (size if spawned with branchDepth 0)

					                	try {
											topLeftAndLowerRightChunkCoordinates = structureStart2.getMinimumSize(world);
						                	double BO3size = Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[0] - -(Integer)topLeftAndLowerRightChunkCoordinates[2]) * Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[1] - -(Integer)topLeftAndLowerRightChunkCoordinates[3]);
						            		int insertAtIndex = BO3sBySize.size();
						            		int i = 0;
						                	for(Object[] entry : BO3sBySize)
						                	{
						                		if(((BO4)bo3AndRarity.getKey()).getSettings().timesSpawned < ((BO4)entry[0]).getSettings().timesSpawned || (BO3size > (Double)entry[2] && ((BO4)bo3AndRarity.getKey()).getSettings().timesSpawned == ((BO4)entry[0]).getSettings().timesSpawned))
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
			            	int pass = 1;
			            	Object[] currentStructureSpawning = BO3sBySize.size() > 0 ? (spawningStructureAtSpawn ? BO3sBySize.get(random.nextInt(BO3sBySize.size()))  : BO3sBySize.get(0)) : null;

		            		int left = 0;
		            		int right = 0;
		            		int top = 0;
		            		int bottom = 0;

		        			int areaSizeX = 0;
		        			int areaSizeZ = 0;

		            		int structureSizeX = 0;
		            		int structureSizeZ = 0;

		        			int maxPass = 4;
		        			boolean spawned;

			        		while(pass <= maxPass && currentStructureSpawning != null)
			        		{
			        			spawned = false;

			            		topLeftAndLowerRightChunkCoordinates = (Object[])currentStructureSpawning[1];
			            		structureSizeX = (Integer)topLeftAndLowerRightChunkCoordinates[1] + (Integer)topLeftAndLowerRightChunkCoordinates[3] + 1;
			            		structureSizeZ = (Integer)topLeftAndLowerRightChunkCoordinates[0] + (Integer)topLeftAndLowerRightChunkCoordinates[2] + 1;

		        		        if ((int)Math.round((Double)currentStructureSpawning[3]) > 0 || (spawningStructureAtSpawn && ((BO4)currentStructureSpawning[0]).getSettings().isSpawnPoint))// && (Double)currentStructureSpawning[3] >= random.nextDouble() * 100.0) <-- disabled rarity because it was basically meaningless (now rarity 0 means never, anything else means always)
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

				        			boolean passHandled = false;
				            		int j = 0;

				            		if(!spawningStructureAtSpawn)
				            		{
						            	while(!(leftEdgeFound && rightEdgeFound && topEdgeFound && bottomEdgeFound))
						            	{
						            		j += 1;

						            		if(right >= structureSizeX - 1 || left >= structureSizeX - 1 || right + left + 1 >= structureSizeX)
						            		{
						            			rightEdgeFound = true;
						            		}
						            		if(!rightEdgeFound)
						            		{
						            			// Check Right
						            			for(int i = -top; i <= bottom; i++)
						            			{
						        	                canSpawnHere = false;
					            					if(!world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + j, chunkCoord.getChunkZ() + i)))
			            							{
							        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + j), (chunkCoord.getChunkZ() + i)));

							        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here

							        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
							            				if(biomeStructures == null)
							        	            	{
						            						if(!world.chunkHasDefaultStructure(rand, chunkCoord))
						            						{
								            					biome3 = world.getBiome((chunkCoord.getChunkX() + j) * 16 + 8, (chunkCoord.getChunkZ() + i) * 16 + 7);
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
							            					structuresPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + j), (chunkCoord.getChunkZ() + i)),biomeStructures);
						            					}
							        	                for(String structureToSpawn : biomeStructures)
							        	                {
							        	                	if(structureToSpawn.equals(((BO4)currentStructureSpawning[0]).getName()))
							        	                	{
							        	                		canSpawnHere = true;
							        	                		break;
							        	                	}
							        	                }
						        	            	} else {
						        	            		structuresPerChunk.remove(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + j), (chunkCoord.getChunkZ() + i)));
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

						            		if(right >= structureSizeX - 1 || left >= structureSizeX - 1 || right + left + 1 >= structureSizeX)
						            		{
						            			leftEdgeFound = true;
						            		}
						            		if(!leftEdgeFound)
						            		{
							            		// Check Left
						            			for(int i = -top; i <= bottom; i++)
						            			{
						        	                canSpawnHere = false;
					            					if(!world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() - j, chunkCoord.getChunkZ() + i)))
					            					{
							        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here

							        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
							        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - j), (chunkCoord.getChunkZ() + i)));
							            				if(biomeStructures == null)
							        	            	{
							            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
							            					{
								            					biome3 = world.getBiome((chunkCoord.getChunkX() - j) * 16 + 8, (chunkCoord.getChunkZ() + i) * 16 + 7);
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
							            					structuresPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - j), (chunkCoord.getChunkZ() + i)),biomeStructures);
						            					}
							        	                for(String structureToSpawn : biomeStructures)
							        	                {
							        	                	if(structureToSpawn.equals(((BO4)currentStructureSpawning[0]).getName()))
							        	                	{
							        	                		canSpawnHere = true;
							        	                		break;
							        	                	}
							        	                }
						        	            	} else {
						        	            		structuresPerChunk.remove(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - j), (chunkCoord.getChunkZ() + i)));
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

						            		if(bottom >= structureSizeZ - 1 || top >= structureSizeZ - 1 || bottom + top + 1 >= structureSizeZ)
						            		{
						            			bottomEdgeFound = true;
						            		}
						            		if(!bottomEdgeFound)
						            		{
							            		// Check Bottom
						            			for(int i = -left; i <= right; i++)
						            			{
						        	                canSpawnHere = false;
					            					if(!world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + i, chunkCoord.getChunkZ() + j)))
					            					{
							        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here

							        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
							        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + j)));
							            				if(biomeStructures == null)
							        	            	{
							            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
							            					{
								            					biome3 = world.getBiome((chunkCoord.getChunkX() + i) * 16 + 8, (chunkCoord.getChunkZ() + j) * 16 + 7);
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
							            					structuresPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + j)),biomeStructures);
						            					}
							        	                for(String structureToSpawn : biomeStructures)
							        	                {
							        	                	if(structureToSpawn.equals(((BO4)currentStructureSpawning[0]).getName()))
							        	                	{
							        	                		canSpawnHere = true;
							        	                		break;
							        	                	}
							        	                }
						        	            	} else {
						        	            		structuresPerChunk.remove(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + j)));
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

						            		if(top >= structureSizeZ - 1 || bottom >= structureSizeZ - 1 || bottom + top >= structureSizeZ + 1)
						            		{
						            			topEdgeFound = true;
						            		}
						            		if(!topEdgeFound)
						            		{
							            		// Check Top
						            			for(int i = -left; i <= right; i++)
						            			{
						        	                canSpawnHere = false;
					            					if(!world.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + i, chunkCoord.getChunkZ() - j)))
					            					{
							        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here

							        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
							        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - j)));
							            				if(biomeStructures == null)
							        	            	{
							            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
							            					{
								            					biome3 = world.getBiome((chunkCoord.getChunkX() + i) * 16 + 8, (chunkCoord.getChunkZ() - j) * 16 + 7);
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
							            					structuresPerChunk.put(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - j)),biomeStructures);
						            					}
							        	                for(String structureToSpawn : biomeStructures)
							        	                {
							        	                	if(structureToSpawn.equals(((BO4)currentStructureSpawning[0]).getName()))
							        	                	{
							        	                		canSpawnHere = true;
							        	                		break;
							        	                	}
							        	                }
						        	            	} else {
						        	            		structuresPerChunk.remove(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - j)));
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

						            		if(!passHandled)
						            		{
							            		// Pass 1 expands to left and bottom first, right and top second
							            		if(pass == 1 && bottomEdgeFound && leftEdgeFound)
							            		{
							            			passHandled = true;
							            			j = 0;
							            			topEdgeFound = false;
							            			rightEdgeFound = false;
							            		}
							            		// Pass 2 expands to right and bottom first, left and top second
							            		if(pass == 2 && bottomEdgeFound && rightEdgeFound)
							            		{
							            			passHandled = true;
							            			j = 0;
							            			topEdgeFound = false;
							            			leftEdgeFound = false;
							            		}
							            		// Pass 3 expands to left and top first, right and bottom second
							            		if(pass == 3 && topEdgeFound && leftEdgeFound)
							            		{
							            			passHandled = true;
							            			j = 0;
							            			bottomEdgeFound = false;
							            			rightEdgeFound = false;
							            		}
							            		// Pass 4 expands to right and top first, left and bottom second
							            		if(pass == 4 && topEdgeFound && rightEdgeFound)
							            		{
							            			passHandled = true;
							            			j = 0;
							            			bottomEdgeFound = false;
							            			leftEdgeFound = false;
							            		}
						            		}
						            	}
				            		} else {
				            			left = (int) Math.ceil(((Integer)topLeftAndLowerRightChunkCoordinates[3] + (Integer)topLeftAndLowerRightChunkCoordinates[1]) / 2d);
				            			right = (int) Math.floor(((Integer)topLeftAndLowerRightChunkCoordinates[3] + (Integer)topLeftAndLowerRightChunkCoordinates[1]) / 2d);
				            			top = (int) Math.ceil(((Integer)topLeftAndLowerRightChunkCoordinates[0] + (Integer)topLeftAndLowerRightChunkCoordinates[2]) / 2d);
				            			bottom = (int) Math.floor(((Integer)topLeftAndLowerRightChunkCoordinates[0] + (Integer)topLeftAndLowerRightChunkCoordinates[2]) / 2d);

				            			if(
			            					world.getWorldSession().getWorldBorderRadius() > 0 &&
			            					(
		            							world.getWorldSession().getWorldBorderRadius() < left ||
		            							world.getWorldSession().getWorldBorderRadius() < right ||
		            							world.getWorldSession().getWorldBorderRadius() < top ||
		            							world.getWorldSession().getWorldBorderRadius() < bottom
			            					)
		            					)
				            			{
				            				left = 0;
				            				right = 0;
				            				top = 0;
				            				bottom = 0;
				            			}
				            		}

				        			areaSizeX = left + right + 1;
				        			areaSizeZ = top + bottom + 1;

					            	// See if the structure will fit
					            	if(
				            			structureSizeX <= areaSizeX &&
				    					structureSizeZ <= areaSizeZ
				        			)
					            	{
					            		int spawnCoordX = 0;
					            		int spawnCoordZ = 0;

					            		// Always spawn in the opposite corner for each pass to make optimal use of detected area
					            		// TODO: Is this still necessary? Will the "found" area dimensions ever be larger than the structure?

					            		// Make sure to always spawn on the chunk that called this method! (area length/width may be larger than structure length/width if the structure has uneven length/width)

					            		// Pass 1 expands to left and bottom first, right and top second
					            		if(pass == 1)
					            		{
					            			// Spawn on left border of available area
						            		spawnCoordX = chunkCoord.getChunkX() - (left > 0 && left - (Integer)topLeftAndLowerRightChunkCoordinates[3] > (Integer)topLeftAndLowerRightChunkCoordinates[1] ? (structureSizeX - 1) : left) + (Integer)topLeftAndLowerRightChunkCoordinates[3]; // left + (Integer)topLeftAndLowerRightChunkCoordinates[3];
						            		// Spawn on bottom border of available area
						            		spawnCoordZ = chunkCoord.getChunkZ() + (bottom > 0 && bottom - (Integer)topLeftAndLowerRightChunkCoordinates[2] > (Integer)topLeftAndLowerRightChunkCoordinates[0] ? (structureSizeZ - 1) : bottom) - (Integer)topLeftAndLowerRightChunkCoordinates[2]; // bottom - (Integer)topLeftAndLowerRightChunkCoordinates[2];
					            		}
					            		// Pass 2 expands to right and bottom first, left and top second
					            		if(pass == 2)
					            		{
					            			// Spawn on right border of available area
						            		spawnCoordX = chunkCoord.getChunkX() + (right > 0 && right - (Integer)topLeftAndLowerRightChunkCoordinates[1] > (Integer)topLeftAndLowerRightChunkCoordinates[3] ? (structureSizeX - 1) : right) - (Integer)topLeftAndLowerRightChunkCoordinates[1]; // right - (Integer)topLeftAndLowerRightChunkCoordinates[1];
						            		// Spawn on bottom border of available area
						            		spawnCoordZ = chunkCoord.getChunkZ() + (bottom > 0 && bottom - (Integer)topLeftAndLowerRightChunkCoordinates[2] > (Integer)topLeftAndLowerRightChunkCoordinates[0] ? (structureSizeZ - 1) : bottom) - (Integer)topLeftAndLowerRightChunkCoordinates[2]; // bottom - (Integer)topLeftAndLowerRightChunkCoordinates[2];
					            		}
					            		// Pass 3 expands to left and top first, right and bottom second
					            		if(pass == 3)
					            		{
					            			// Spawn on left border of available area
						            		spawnCoordX = chunkCoord.getChunkX() - (left > 0 && left - (Integer)topLeftAndLowerRightChunkCoordinates[3] > (Integer)topLeftAndLowerRightChunkCoordinates[1] ?  (structureSizeX - 1) : left) + (Integer)topLeftAndLowerRightChunkCoordinates[3];// left + (Integer)topLeftAndLowerRightChunkCoordinates[3];
						            		// Spawn on top border of available area
						            		spawnCoordZ = chunkCoord.getChunkZ() - (top > 0 && top - (Integer)topLeftAndLowerRightChunkCoordinates[0] > (Integer)topLeftAndLowerRightChunkCoordinates[2] ? (structureSizeZ - 1) : top) + (Integer)topLeftAndLowerRightChunkCoordinates[0]; // top + (Integer)topLeftAndLowerRightChunkCoordinates[0];
					            		}
					            		// Pass 4 expands to right and top first, left and bottom second
					            		if(pass == 4)
					            		{
					            			// Spawn on right border of available area
						            		spawnCoordX = chunkCoord.getChunkX() + (right > 0 && right - (Integer)topLeftAndLowerRightChunkCoordinates[1] > (Integer)topLeftAndLowerRightChunkCoordinates[3] ?  (structureSizeX - 1) : right) - (Integer)topLeftAndLowerRightChunkCoordinates[1]; //right - (Integer)topLeftAndLowerRightChunkCoordinates[1];
						            		// Spawn on top border of available area
						            		spawnCoordZ = chunkCoord.getChunkZ() - (top > 0 && top - (Integer)topLeftAndLowerRightChunkCoordinates[0] > (Integer)topLeftAndLowerRightChunkCoordinates[2] ?  (structureSizeZ - 1) : top) + (Integer)topLeftAndLowerRightChunkCoordinates[0]; //top + (Integer)topLeftAndLowerRightChunkCoordinates[0];
					            		}

				                		if(isBO3AllowedToSpawnAt(ChunkCoordinate.fromChunkCoords((int)Math.round(spawnCoordX - ((Integer)topLeftAndLowerRightChunkCoordinates[3] / 2d) + ((Integer)topLeftAndLowerRightChunkCoordinates[1] / 2d)), (int)Math.round(spawnCoordZ - ((Integer)topLeftAndLowerRightChunkCoordinates[0] / 2d) + ((Integer)topLeftAndLowerRightChunkCoordinates[2] / 2d))), (BO4)currentStructureSpawning[0]))
				                		{
						                	structureCoord = new BO4CustomStructureCoordinate(world, ((BO4)currentStructureSpawning[0]), null, Rotation.NORTH, spawnCoordX * 16, (short)0, spawnCoordZ * 16, 0, false, false, null);
						                	structureStart2 = new BO4CustomStructure(world, structureCoord, true, spawningStructureAtSpawn);

				            	        	if(structureStart2.IsSpawned)
						                	{
				            	    			// Always add the Start chunk to the structureCache etc even if it doesnt have any blocks. 
				            	        		// This is done to make sure that Start will get saved correctly when the server saves to disk.
				            	    			// TODO: This means that the start chunk can be empty and cannot be populated by another structure :(. It will also show /otg BO3Info in the apparently empty chunk

				            	        		structureCache.put(chunkCoord, structureStart2);
				            	    			this.structuresPerChunk.put(chunkCoord, new ArrayList<String>());
				            	    			worldInfoChunks.put(chunkCoord, structureStart2);

						                		((BO4)structureCoord.getObject()).getSettings().timesSpawned += 1;
							                	//biome3 = world.getBiome(spawnCoordX * 16 + 8, spawnCoordZ * 16 + 7);
						                		if(OTG.getPluginConfig().spawnLog)
						                		{
						                			OTG.log(LogMarker.INFO, "Plotted structure " + structureCoord.getObject().getName() + " at chunk X" + spawnCoordX + " Z" + spawnCoordZ + " ("+ (spawnCoordX * 16) + " 100 " + (spawnCoordZ * 16) + ")");// + " biome " + biome3.getName());
						                		}

						                		if(((BO4)currentStructureSpawning[0]).getSettings().frequency > 0 || ((BO4)currentStructureSpawning[0]).getSettings().bo3Groups.size() > 0)
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

						                			if(((BO4)currentStructureSpawning[0]).getSettings().bo3Groups.size() > 0)
						                			{
							                			ChunkCoordinate bo4CenterSpawnCoord = ChunkCoordinate.fromChunkCoords(
				                							(int)Math.round(spawnCoordX - ((Integer)topLeftAndLowerRightChunkCoordinates[3] / 2d) + ((Integer)topLeftAndLowerRightChunkCoordinates[1] / 2d)),
				                							(int)Math.round(spawnCoordZ - ((Integer)topLeftAndLowerRightChunkCoordinates[0] / 2d) + ((Integer)topLeftAndLowerRightChunkCoordinates[2] / 2d))
			                							);
						                				
						                				for(Entry<String, Integer> entry : ((BO4)currentStructureSpawning[0]).getSettings().bo3Groups.entrySet())
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

						                		spawned = true;
						                		// If the structure has spawned then get its actual size, don't use its minimum size
					                			random = new Random();
					                		}

				            	        	// if the target chunk had a part of the branching structure spawned on it // <-- this should always be the case when spawned == true
			                				if(spawned)
				                			{
				                				break;
				                			}
				                		}
					            	}
		        		        } else {
		        		        	pass = maxPass;
		        		        }

			                	if(!spawned)
			                	{
			                		//OTG.log(LogMarker.INFO, "Cannot spawn pass" + pass + " size" + BO3sBySize.size());
			                		pass += 1;

			                		// Tried spawning this structure 4 times, move on to next structure
				                	if(pass > maxPass)
				                	{
				                		// If we've tried 4 passes for all structures then give up
				                		if(BO3sBySize.size() == 1)
				                		{
			                        		structureCache.put(chunkCoord, new BO4CustomStructure(world, null, false, false));
				                		} else {
				                			// Try 4 passes for next structure
				                			// Find next structure
				                			boolean bFound = false;
				                			int i = 0;
				                			for(Object[] nextStructure : BO3sBySize)
				                			{
				                				if(bFound)
				                				{
				                					BO3sBySize.remove(currentStructureSpawning);
				                					currentStructureSpawning = nextStructure;
				                					break;
				                				}
				                				if(nextStructure == currentStructureSpawning)
				                				{
				                					if(i == BO3sBySize.size() - 1)
				                					{
				                						BO3sBySize.remove(currentStructureSpawning);
				                						currentStructureSpawning = BO3sBySize.get(0);
				                						break;
				                					}
				                					bFound = true;
				                				}
				                				i += 1;
				                			}
				                			pass = 1;
				                		}
				                	}
			                	} else {
		                			// Try 4 passes for next structure
		                			// Find next structure
		                			boolean bFound = false;
		                			int i = 0;
		                			for(Object[] nextStructure : BO3sBySize)
		                			{
		                				if(bFound)
		                				{
		                					currentStructureSpawning = nextStructure;
		                					break;
		                				}
		                				if(nextStructure == currentStructureSpawning)
		                				{
		                					if(i == BO3sBySize.size() - 1)
		                					{
		                						currentStructureSpawning = BO3sBySize.get(0);
		                						break;
		                					}
		                					bFound = true;
		                				}
		                				i += 1;
		                			}
	                				pass = 1;
		                		}
			        		}
		            	} else {
		            		structureCache.put(chunkCoord, new BO4CustomStructure(world, null, false, false));
		            	}
		            } else {
	            		structureCache.put(chunkCoord, new BO4CustomStructure(world, null, false, false));
		            }
	            } else {
            		structureCache.put(chunkCoord, new BO4CustomStructure(world, null, false, false));
	            }
	    	}
	        processing = false;

    		structuresPerChunk.put(chunkCoord, new ArrayList<String>());
    	} else {
    		OTG.log(LogMarker.FATAL, "Illegal double spawn detected, aborting...");
    		throw new RuntimeException("Illegal double spawn detected, aborting...");
    	}
    }

    private boolean isBO3AllowedToSpawnAt(ChunkCoordinate chunkCoord, BO4 BO3ToSpawn)
    {
        // Check if no other structure of the same type (filename) is within the minimum radius (BO3 frequency)
		int radius = BO3ToSpawn.getSettings().frequency;
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
		if(BO3ToSpawn.getSettings().bo3Groups.size() > 0)
		{
        	float distanceBetweenStructures = 0;
        	int cachedChunkRadius = 0;
        	ChunkCoordinate cachedChunk = null;
        	for(Entry<String, Integer> entry : BO3ToSpawn.getSettings().bo3Groups.entrySet())
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
