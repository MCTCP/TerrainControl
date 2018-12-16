package com.pg85.otg.customobjects;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.BiomeConfig;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.ModDataFunction;
import com.pg85.otg.customobjects.bo3.ParticleFunction;
import com.pg85.otg.customobjects.bo3.SpawnerFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.Rotation;
import com.pg85.otg.util.helpers.RandomHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;

/**
 * Each world has a cache of unfinished structures. This class is the cache.
 *
 */
public class CustomObjectStructureCache
{
	// OTG+

	// Key not present in structurecache == was never populated or plotted
	// Key is present and Value is CustomObjectStructure with null as Start == plotted as emtpy chunk (this chunk was populated but no BO3 was plotted on it so only add trees, lakes, ores etc)
	// Key is present and Value is CustomObjectStructure with non-null as Start == plotted with BO3
	// Key is present and Value is null == plotted and spawned
	// If a chunk of a CustomObjectStructure has been spawned then the CustomObjectStructure's SmoothingAreasToSpawn and ObjectsToSpawn entries for that chunk
	// have been removed (cleans up cache and makes sure nothing is ever spawned twice, although a second spawn call should never be made in the first place ofc ><).

	public Map<ChunkCoordinate, CustomObjectStructure> worldInfoChunks; // Used for the /WorldInfo command, stores information about every BO3 that has been spawned so that author and description information can be requested by chunk.
	public Map<String, Stack<ChunkCoordinate>> spawnedStructures; // Used to find distance between structures and structure groups, only stores 1 chunk per structure in the calculated center of the structure. Does not clean itself when used with the pre-generator and will become slower as it fills up, use as little as possible! (can't clean itself because max radius for BO3 groups cannot be known)
	public HashMap<ChunkCoordinate, ArrayList<String>> structuresPerChunk; // Used as a cache by the plotting code

	//

    public Map<ChunkCoordinate, CustomObjectStructure> structureCache;
    private LocalWorld world;

    public CustomObjectStructureCache(LocalWorld world)
    {
        this.world = world;
        this.structureCache = new HashMap<ChunkCoordinate, CustomObjectStructure>();

        this.spawnedStructures = new HashMap<String, Stack<ChunkCoordinate>>();
        this.worldInfoChunks = new HashMap<ChunkCoordinate, CustomObjectStructure>();
        this.structuresPerChunk = new HashMap<ChunkCoordinate, ArrayList<String>>();

        LoadStructureCache();
    }

    public void reload(LocalWorld world)
    {
    	// Only used for Bukkit?
        this.world = world;
        structureCache.clear();
    }

    public CustomObjectStructure getStructureStart(int chunkX, int chunkZ)
    {
    	if(world.getConfigs().getWorldConfig().IsOTGPlus)
    	{
    		throw new RuntimeException();
    	} else {
	        ChunkCoordinate coord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
	        CustomObjectStructure structureStart = structureCache.get(coord);

	        // Clear cache if needed
	        if (structureCache.size() > 400)
	        {
	            structureCache.clear();
	        }

	        if (structureStart != null)
	        {
	            return structureStart;
	        }
	        // No structure found, create one
	        Random random = RandomHelper.getRandomForCoords(chunkX ^ 2, (chunkZ + 1) * 2, world.getSeed());
	        BiomeConfig biomeConfig = world.getBiome(chunkX * 16 + 15, chunkZ * 16 + 15).getBiomeConfig();
	        CustomStructureGen structureGen = biomeConfig.structureGen;
	        if (structureGen != null)
	        {
	            CustomObjectCoordinate customObject = structureGen.getRandomObjectCoordinate(world, random, chunkX, chunkZ);
	            if (customObject != null)
	            {
	                structureStart = new CustomObjectStructure(world, customObject);
	                structureCache.put(coord, structureStart);
	                return structureStart;
	            }	        }

	        return null;
    	}
    }

    public boolean processing = false;
    public void PlotStructures(Random rand, ChunkCoordinate chunkCoord, boolean spawningStructureAtSpawn)
    {
    	if(!processing)
    	{
	    	processing = true;
	        if (!world.IsInsidePregeneratedRegion(chunkCoord, true) && world.IsInsideWorldBorder(chunkCoord, false) && !this.structureCache.containsKey(chunkCoord))
	        {
	            LocalBiome biome = world.getBiome(chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8);
	            BiomeConfig biomeConfig = biome.getBiomeConfig();
	            ArrayList<CustomStructureGen> customStructureGens = new ArrayList<CustomStructureGen>();

	        	if(!world.chunkHasDefaultStructure(rand, chunkCoord))
	        	{
		            // Get Bo3's for this biome
		            for (ConfigFunction<BiomeConfig> res : biomeConfig.resourceSequence)
		            {
		            	// TODO: Find out if not checking for error could cause problems
		            	if(res instanceof CustomStructureGen)// && ((CustomStructureGen)res).error == null)
		            	{
		            		customStructureGens.add((CustomStructureGen)res);
		            	}
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
	        				if(structure != null && ((BO3)structure).getSettings().isOTGPlus) // Structure was in resource list but file could not be found. TODO: Make this prettier!
	        				{
		        				structuredCustomObjects.put(structure, structureGen.objectChances.get(i));
		        				i += 1;
	        				} else {
	        					if(structure != null && !((BO3)structure).getSettings().isOTGPlus)
	        					{
		        					if(OTG.getPluginConfig().SpawnLog)
		        					{
		        						OTG.log(LogMarker.WARN, "Tried to spawn non-OTG+ enabled BO3 as CustomStructure in a OTG+ enabled world. BO3: " + ((BO3)structure).getSettings().getName());
		        					}
	        					}
	        				}
	        			}
	        		}

	        		if(structuredCustomObjects.size() > 0)
	        		{
		            	Object[] topLeftAndLowerRightChunkCoordinates = null;
		            	CustomObjectCoordinate structureCoord = null;
		            	CustomObjectStructure structureStart2 = null;

		            	ArrayList<Object[]> BO3sBySize = new ArrayList<Object[]>();
		            	ArrayList<String> structuresToSpawn1 = new ArrayList<String>();

		            	// Get list of BO3's that should spawn at the spawn point
		            	if(spawningStructureAtSpawn)
		            	{
			            	for(Map.Entry<StructuredCustomObject, Double> bo3AndRarity : structuredCustomObjects.entrySet())
			            	{
			            		if(!((BO3)bo3AndRarity.getKey()).isInvalidConfig && ((BO3)bo3AndRarity.getKey()).getSettings().isSpawnPoint)
			            		{
				            		structuresToSpawn1.add(bo3AndRarity.getKey().getName());
				                	structureCoord = new CustomObjectCoordinate(world, bo3AndRarity.getKey(), null, Rotation.NORTH, chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ(), false, 0, false, false, null);
				                	structureStart2 = new CustomObjectStructure(world, structureCoord, false, false);
				                	// Get minimum size (size if spawned with branchDepth 0)

				                	try {
										topLeftAndLowerRightChunkCoordinates = structureStart2.GetMinimumSize();
					                	double BO3size = Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[0] - -(Integer)topLeftAndLowerRightChunkCoordinates[2]) * Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[1] - -(Integer)topLeftAndLowerRightChunkCoordinates[3]);
				                		BO3sBySize.add(new Object[]{ bo3AndRarity.getKey(), topLeftAndLowerRightChunkCoordinates, BO3size, bo3AndRarity.getValue() });
									}
				                	catch (InvalidConfigException e)
				                	{
										((BO3)bo3AndRarity.getKey()).isInvalidConfig = true;
									}
			            		}
			            	}
		            	}
		            	if(!spawningStructureAtSpawn || BO3sBySize.size() == 0)
		            	{
			            	// Get list of BO3's able to spawn in this chunk
			            	for(Map.Entry<StructuredCustomObject, Double> bo3AndRarity : structuredCustomObjects.entrySet())
			            	{
			            		if(!((BO3)bo3AndRarity.getKey()).isInvalidConfig && (int)Math.round(bo3AndRarity.getValue()) > 0)
			            		{
			            			// TODO: avoid calling IsBO3AllowedToSpawnAt so much, cache and reuse any nearest group members found

			            			if(IsBO3AllowedToSpawnAt(chunkCoord, ((BO3)bo3AndRarity.getKey())))
			            			{
					            		structuresToSpawn1.add(bo3AndRarity.getKey().getName());
					                	structureCoord = new CustomObjectCoordinate(world, bo3AndRarity.getKey(), null, Rotation.NORTH, chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ(), false, 0, false, false, null);
					                	structureStart2 = new CustomObjectStructure(world, structureCoord, false, false);
					                	// Get minimum size (size if spawned with branchDepth 0)

					                	try {
											topLeftAndLowerRightChunkCoordinates = structureStart2.GetMinimumSize();
						                	double BO3size = Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[0] - -(Integer)topLeftAndLowerRightChunkCoordinates[2]) * Math.abs((Integer)topLeftAndLowerRightChunkCoordinates[1] - -(Integer)topLeftAndLowerRightChunkCoordinates[3]);
						            		int insertAtIndex = BO3sBySize.size();
						            		int i = 0;
						                	for(Object[] entry : BO3sBySize)
						                	{
						                		if(((BO3)bo3AndRarity.getKey()).getSettings().timesSpawned < ((BO3)entry[0]).getSettings().timesSpawned || (BO3size > (Double)entry[2] && ((BO3)bo3AndRarity.getKey()).getSettings().timesSpawned == ((BO3)entry[0]).getSettings().timesSpawned))
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
											((BO3)bo3AndRarity.getKey()).isInvalidConfig = true;
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

		        		        if ((int)Math.round((Double)currentStructureSpawning[3]) > 0 || (spawningStructureAtSpawn && ((BO3)currentStructureSpawning[0]).getSettings().isSpawnPoint))// && (Double)currentStructureSpawning[3] >= random.nextDouble() * 100.0) <-- disabled rarity because it was basically meaningless (now rarity 0 means never, anything else means always)
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
					            					if(!world.IsInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + j, chunkCoord.getChunkZ() + i), true) && world.IsInsideWorldBorder(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + j, chunkCoord.getChunkZ() + i), true))
			            							{
							        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + j), (chunkCoord.getChunkZ() + i)));

							        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here

							        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
							            				if(biomeStructures == null)
							        	            	{
						            						if(!world.chunkHasDefaultStructure(rand, chunkCoord))
						            						{
								            					biome3 = world.getBiome((chunkCoord.getChunkX() + j) * 16 + 8, (chunkCoord.getChunkZ() + i) * 16 + 8);
								            					// Get cached data if available
								            					if(!biome3.getName().equals(biome.getName()))
									            				{
										        	                biomeConfig3 = biome3.getBiomeConfig();
										        	                structuresToSpawn = new ArrayList<String>();

										        	                // Get Bo3's for this biome
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
							        	                	if(structureToSpawn.equals(((BO3)currentStructureSpawning[0]).getName()))
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
					            					if(!world.IsInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() - j, chunkCoord.getChunkZ() + i), true) && world.IsInsideWorldBorder(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() - j, chunkCoord.getChunkZ() + i), true))
					            					{
							        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here

							        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
							        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() - j), (chunkCoord.getChunkZ() + i)));
							            				if(biomeStructures == null)
							        	            	{
							            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
							            					{
								            					biome3 = world.getBiome((chunkCoord.getChunkX() - j) * 16 + 8, (chunkCoord.getChunkZ() + i) * 16 + 8);
								            					if(!biome3.getName().equals(biome.getName()))
									            				{
										        	                biomeConfig3 = biome3.getBiomeConfig();
										        	                structuresToSpawn = new ArrayList<String>();

										        	                // Get Bo3's for this biome
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
							        	                	if(structureToSpawn.equals(((BO3)currentStructureSpawning[0]).getName()))
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
					            					if(!world.IsInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + i, chunkCoord.getChunkZ() + j), true) && world.IsInsideWorldBorder(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + i, chunkCoord.getChunkZ() + j), true))
					            					{
							        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here

							        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
							        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() + j)));
							            				if(biomeStructures == null)
							        	            	{
							            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
							            					{
								            					biome3 = world.getBiome((chunkCoord.getChunkX() + i) * 16 + 8, (chunkCoord.getChunkZ() + j) * 16 + 8);
								            					if(!biome3.getName().equals(biome.getName()))
									            				{
										        	                biomeConfig3 = biome3.getBiomeConfig();
										        	                structuresToSpawn = new ArrayList<String>();

										        	                // Get Bo3's for this biome
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
							        	                	if(structureToSpawn.equals(((BO3)currentStructureSpawning[0]).getName()))
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
					            					if(!world.IsInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + i, chunkCoord.getChunkZ() - j), true) && world.IsInsideWorldBorder(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + i, chunkCoord.getChunkZ() - j), true))
					            					{
							        	                // StructureCache.put's also add an empty list to biomestructures so don't need to check structurecache here

							        	                // When we get biomestructures here we can check, size() == 0 means the chunk is in structurecache, null means it hasnt yet been cached at all
							        	                biomeStructures = structuresPerChunk.get(ChunkCoordinate.fromChunkCoords((chunkCoord.getChunkX() + i), (chunkCoord.getChunkZ() - j)));
							            				if(biomeStructures == null)
							        	            	{
							            					if(!world.chunkHasDefaultStructure(rand, chunkCoord))
							            					{
								            					biome3 = world.getBiome((chunkCoord.getChunkX() + i) * 16 + 8, (chunkCoord.getChunkZ() - j) * 16 + 8);
								            					if(!biome3.getName().equals(biome.getName()))
									            				{
										        	                biomeConfig3 = biome3.getBiomeConfig();
										        	                structuresToSpawn = new ArrayList<String>();

										        	                // Get Bo3's for this biome
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
							        	                	if(structureToSpawn.equals(((BO3)currentStructureSpawning[0]).getName()))
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
				            					world.GetWorldSession().getWorldBorderRadius() > 0 &&
			            					(
		            							world.GetWorldSession().getWorldBorderRadius() < left ||
		            							world.GetWorldSession().getWorldBorderRadius() < right ||
		            							world.GetWorldSession().getWorldBorderRadius() < top ||
		            							world.GetWorldSession().getWorldBorderRadius() < bottom
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

				                		if(IsBO3AllowedToSpawnAt(ChunkCoordinate.fromChunkCoords((int)Math.round(spawnCoordX - ((Integer)topLeftAndLowerRightChunkCoordinates[3] / 2d) + ((Integer)topLeftAndLowerRightChunkCoordinates[1] / 2d)), (int)Math.round(spawnCoordZ - ((Integer)topLeftAndLowerRightChunkCoordinates[0] / 2d) + ((Integer)topLeftAndLowerRightChunkCoordinates[2] / 2d))), (BO3)currentStructureSpawning[0]))
				                		{
						                	structureCoord = new CustomObjectCoordinate(world, ((BO3)currentStructureSpawning[0]), null, Rotation.NORTH, spawnCoordX * 16, 0, spawnCoordZ * 16, false, 0, false, false, null);
						                	structureStart2 = new CustomObjectStructure(world, structureCoord, true, spawningStructureAtSpawn);

				            	        	if(structureStart2.IsSpawned)
						                	{
				            	    			// Always add the Start chunk to the structureCache etc even if it doesnt have any blocks. This is done to make sure that Start will get saved correctly when the server saves to disk.
				            	    			// TODO: This means that the start chunk can be empty and cannot be populated by another structure :(. It will also show /mcw BO3Info in the apparently empty chunk

				            	        		structureCache.put(chunkCoord, structureStart2);
				            	    			structuresPerChunk.put(chunkCoord, new ArrayList<String>());
				            	    			worldInfoChunks.put(chunkCoord, structureStart2);

						                		((BO3)structureCoord.getObject()).getSettings().timesSpawned += 1;
							                	//biome3 = world.getBiome(spawnCoordX * 16 + 8, spawnCoordZ * 16 + 8);
						                		if(OTG.getPluginConfig().SpawnLog)
						                		{
						                			OTG.log(LogMarker.INFO, "Plotted structure " + structureCoord.getObject().getName() + " at chunk X" + spawnCoordX + " Z" + spawnCoordZ + " ("+ (spawnCoordX * 16) + " 100 " + (spawnCoordZ * 16) + ")");// + " biome " + biome3.getName());
						                		}

						                		if(((BO3)currentStructureSpawning[0]).getSettings().frequency > 0 || ((BO3)currentStructureSpawning[0]).getSettings().bo3Group.length() > 0)
						                		{
						                			String bO3NameAndGroupString = ((BO3)currentStructureSpawning[0]).getName() + ";" + ((BO3)currentStructureSpawning[0]).getSettings().bo3Group;
							                		if(spawnedStructures.containsKey(bO3NameAndGroupString))
							                		{
							                			spawnedStructures.get(bO3NameAndGroupString).add(ChunkCoordinate.fromChunkCoords(spawnCoordX, spawnCoordZ));
							                		} else {
							                			Stack<ChunkCoordinate> chunks = new Stack<ChunkCoordinate>();
							                			chunks.add(
						                					ChunkCoordinate.fromChunkCoords(
					                							(int)Math.round(spawnCoordX - ((Integer)topLeftAndLowerRightChunkCoordinates[3] / 2d) + ((Integer)topLeftAndLowerRightChunkCoordinates[1] / 2d)),
					                							(int)Math.round(spawnCoordZ - ((Integer)topLeftAndLowerRightChunkCoordinates[0] / 2d) + ((Integer)topLeftAndLowerRightChunkCoordinates[2] / 2d))
				                							)
					                					);
							                			spawnedStructures.put(bO3NameAndGroupString, chunks);
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
			                        		structureCache.put(chunkCoord, new CustomObjectStructure(world, null, false, false));
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
		            		this.structureCache.put(chunkCoord, new CustomObjectStructure(world, null, false, false));
		            	}
		            } else {
	            		this.structureCache.put(chunkCoord, new CustomObjectStructure(world, null, false, false));
		            }
	            } else {
            		this.structureCache.put(chunkCoord, new CustomObjectStructure(world, null, false, false));
	            }
	    	}
	        processing = false;

    		structuresPerChunk.put(chunkCoord, new ArrayList<String>());
    	} else {
    		OTG.log(LogMarker.ERROR, "Illegal double spawn detected, aborting...");
    		throw new RuntimeException();
    	}
    }

    private boolean IsBO3AllowedToSpawnAt(ChunkCoordinate chunkCoord, BO3 BO3ToSpawn)
    {
		int radius = BO3ToSpawn.getSettings().frequency;

        // Check if no other structure of the same type (filename) is within the minimum radius (BO3 frequency)
		// Check if no other structures that are a member of the same group as this BO3 are within the minimum radius (BO3Group frequency)
        String bO3NameAndGroupString = BO3ToSpawn.getName() + ";" + BO3ToSpawn.getSettings().bo3Group;
        String[] groupStrings = BO3ToSpawn.getSettings().bo3Group.trim().length() > 0 ? BO3ToSpawn.getSettings().bo3Group.split(",") : null;
        ArrayList<String> groupNames = new ArrayList<String>();
        ArrayList<Integer> groupFrequencies = new ArrayList<Integer>();
        if(groupStrings != null && groupStrings.length > 0)
        {
        	for(int i = 0; i < groupStrings.length; i++)
        	{
            	String[] groupString = groupStrings[i].trim().length() > 0 ? groupStrings[i].split(":") : null;
            	if(groupString != null && groupString.length == 2)
            	{
            		groupNames.add(groupString[0].trim());
            		groupFrequencies.add(Integer.parseInt(groupString[1].trim()));
            	}
        	}
        }
		if(radius > 0 || groupNames.size() > 0)
		{
			float distanceBetweenStructures = 0;
            for(String key : spawnedStructures.keySet())
            {
            	// Check BO3 frequency
				if(radius > 0 && key.equals(bO3NameAndGroupString))
				{
            		ArrayList<ChunkCoordinate> spawnedCoords = new ArrayList<ChunkCoordinate>();
        			spawnedCoords.addAll(spawnedStructures.get(key));
        			for(ChunkCoordinate cachedChunk : spawnedCoords)
        			{
        				radius = BO3ToSpawn.getSettings().frequency;
                        // Find distance between two points
                        distanceBetweenStructures = (int)Math.floor(Math.sqrt(Math.pow(chunkCoord.getChunkX() - cachedChunk.getChunkX(), 2) + Math.pow(chunkCoord.getChunkZ() - cachedChunk.getChunkZ(), 2)));
                        if (distanceBetweenStructures <= radius)
                        {
                        	// Other BO3 of the same type is too nearby, cannot spawn here!
                            return false;
                        }
        			}
				}

				if(groupNames.size() > 0)
				{
    				// Check groups
                    ArrayList<String> spawnedStructureGroupNames = new ArrayList<String>();
                    ArrayList<Integer> spawnedStructureGroupFrequencies = new ArrayList<Integer>();
        			String[] spawnedStructureNameAndGroupString = key.split(";");
        			if(spawnedStructureNameAndGroupString.length > 1)
        			{
                        String[] spawnedStructureGroupStrings = spawnedStructureNameAndGroupString[1].split(",");
                        if(spawnedStructureGroupStrings.length > 0)
                        {
                        	for(int i = 0; i < spawnedStructureGroupStrings.length; i++)
                        	{
	                        	String[] spawnedStructureGroupString = spawnedStructureGroupStrings[i].trim().length() > 0 ? spawnedStructureGroupStrings[i].split(":") : null;
	                        	if(spawnedStructureGroupString != null && spawnedStructureGroupString.length == 2)
	                        	{
	                        		spawnedStructureGroupNames.add(spawnedStructureGroupString[0].trim());
	                        		spawnedStructureGroupFrequencies.add(Integer.parseInt(spawnedStructureGroupString[1].trim()));
	                        	}
                        	}
                        }
        			}

        			if(spawnedStructureGroupNames.size() > 0)
        			{
        				for(int i = 0; i < groupNames.size(); i++)
        				{
        					for(int j = 0; j < spawnedStructureGroupNames.size(); j++)
        					{
        						if(groupNames.get(i).equals(spawnedStructureGroupNames.get(j)))
        						{
                        			for(ChunkCoordinate cachedChunk : spawnedStructures.get(key))
                        			{
                        				radius = groupFrequencies.get(i) >= spawnedStructureGroupFrequencies.get(j) ? groupFrequencies.get(i) : spawnedStructureGroupFrequencies.get(j);
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
        			}
				}
            }
		}
		return true;
    }

    // persistence stuff

    public void CompressCache()
    {
    	OTG.log(LogMarker.INFO, "Compressing structure-cache and pre-generator data");

    	// If a chunk in the structurecache is inside the outermost ring of
    	// chunks in the pre-generated area then it can be safely removed

    	int structuresRemoved = 0;

    	// Fill a new structureCache based on the  existing one, remove all the chunks inside the pregenerated region that we know will no longer be used
    	HashMap<ChunkCoordinate, CustomObjectStructure> newStructureCache = new HashMap<ChunkCoordinate,CustomObjectStructure>();
    	for (Map.Entry<ChunkCoordinate, CustomObjectStructure> cachedChunk : structureCache.entrySet())
    	{
			// If this structure is not done spawning or on/outside the border of the pre-generated area then keep it
			if(!world.IsInsidePregeneratedRegion(cachedChunk.getKey(), true))
			{
				newStructureCache.put(cachedChunk.getKey(), cachedChunk.getValue());
			} else {

				structuresRemoved += 1;

				// Null means fully populated, plotted and spawned
				if(cachedChunk.getValue() != null)
				{
					OTG.log(LogMarker.INFO, "Running " + world.GetWorldSession().getPreGeneratorIsRunning() +  " L" + world.GetWorldSession().getPregeneratedBorderLeft() + " R" + world.GetWorldSession().getPregeneratedBorderRight() + " T" + world.GetWorldSession().getPregeneratedBorderTop() + " B" + world.GetWorldSession().getPregeneratedBorderBottom());
					OTG.log(LogMarker.INFO, "Error at Chunk X" + cachedChunk.getKey().getChunkX() + " Z" + cachedChunk.getKey().getChunkZ() + ". " + (!this.structureCache.containsKey(cachedChunk.getKey()) ? (world.IsInsidePregeneratedRegion(cachedChunk.getKey(), true) ? "Inside pregenned region" : "Not plotted") : this.structureCache.get(cachedChunk.getKey()) == null ? "Plotted and spawned" : this.structureCache.get(cachedChunk.getKey()).Start != null ? this.structureCache.get(cachedChunk.getKey()).Start.BO3Name : "Trees"));

					throw new RuntimeException();
				}
			}
    	}

    	structureCache = newStructureCache;

    	OTG.log(LogMarker.INFO, "Removed " + structuresRemoved + " cached chunks");
    }

    public void SaveToDisk()
    {
    	OTG.log(LogMarker.INFO, "Saving structure data");
    	int i = 0;
    	long starTime = System.currentTimeMillis();
		while(true)
		{
			synchronized(world.getObjectSpawner().lockingObject)
			{
				if(!world.getObjectSpawner().populating)
				{
					world.getObjectSpawner().saving = true;
					break;
				}
			}
			if(i == 0 || i == 100)
			{
				OTG.log(LogMarker.INFO, "SaveToDisk waiting on Populate. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
				i = 0;
			}
			i += 1;
			if(System.currentTimeMillis() - starTime > (300 * 1000))
			{
				OTG.log(LogMarker.INFO, "SaveToDisk waited on populate longer than 300 seconds, something went wrong!");
				throw new RuntimeException();
			}
		}

		if(world.getConfigs().getWorldConfig().IsOTGPlus)
		{
			CompressCache();
		}
		SaveStructureCache();

		synchronized(world.getObjectSpawner().lockingObject)
		{
	    	world.getObjectSpawner().saveRequired = false;
	    	world.getObjectSpawner().saving = false;
		}
    }

    private void SaveStructureCache()
    {
    	OTG.log(LogMarker.INFO, "Saving structures and pre-generator data");

	    Map<ChunkCoordinate, CustomObjectStructure> worldInfoChunksToSave = new HashMap<ChunkCoordinate, CustomObjectStructure>();

	    for (Map.Entry<ChunkCoordinate, CustomObjectStructure> cachedChunk : worldInfoChunks.entrySet()) // WorldInfo holds info on all BO3's ever spawned for this world, structurecache only holds those outside the pregenerated area and sets spawned chunks to null!
	    {
	    	if(cachedChunk.getValue() != null)
	    	{
	    		worldInfoChunksToSave.put(cachedChunk.getKey(), cachedChunk.getValue());
	    	} else {
	    		throw new RuntimeException();
	    	}
	    }

	    SaveStructuresFile(worldInfoChunksToSave);

	    for (Map.Entry<ChunkCoordinate, CustomObjectStructure> cachedChunk : worldInfoChunks.entrySet())
	    {
	    	if(cachedChunk.getValue() != null)
	    	{
	    		cachedChunk.getValue().saveRequired = false;
	    	}
	    }

	    if(world.getConfigs().getWorldConfig().IsOTGPlus)
	    {
		    ArrayList<ChunkCoordinate> nullChunks = new ArrayList<ChunkCoordinate>();
	    	for (Map.Entry<ChunkCoordinate, CustomObjectStructure> cachedChunk : structureCache.entrySet()) // Save null chunks from structurecache so that when loading we can reconstitute it based on worldInfoChunks, null chunks and the pregenerator border
	    	{
	    		if(cachedChunk.getValue() == null)
	    		{
	    			if(!world.IsInsidePregeneratedRegion(cachedChunk.getKey(),true))
	    			{
	    				nullChunks.add(cachedChunk.getKey());
					}
	    		}
	    	}

	    	SaveChunksFile(nullChunks, "NullChunks.txt");

	    	SaveChunksMapFile(spawnedStructures, "SpawnedStructures.txt");
	    }

		OTG.log(LogMarker.INFO, "Saving done");
    }

	private void SaveStructuresFile(Map<ChunkCoordinate, CustomObjectStructure> structures)
	{
		// When loading files we first load all the structure files and put them in worldInfoChunks and structurecache (if they are outside the pregenerated region),
		// then from any structures that have ObjectsToSpawn or SmoothingAreas to spawn we create structures in the structure cache for each of those (overriding some of the structures we added earlier)
		// Then we load all the null chunks and add them to the structurecache (if they are outside the pregenerated region), potentially overriding some of the structures we added earlier.

		// So don't worry about saving structure files for structures that have already been spawned, they won't be added to the structure cache when loading

		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + "StructureData.txt");
    	if(occupiedChunksFile.exists())
    	{
    		occupiedChunksFile.delete();
    	}

    	StringBuilder stringbuilder = new StringBuilder();
    	if(structures.size() > 0)
    	{
	    	stringbuilder.append("[");

			for(Entry<ChunkCoordinate, CustomObjectStructure> entry : structures.entrySet())
			{
				ChunkCoordinate chunkCoord = entry.getKey();
				CustomObjectStructure structure = entry.getValue();

				if(stringbuilder.length() > 1)
				{
					stringbuilder.append(" ");
				}

		    	if(structure.Start != null)
		    	{
		    		//stringbuilder.append("[" + entry.getKey().getChunkX() + "," + entry.getKey().getChunkZ() + "][" + structure.MinY + "," + structure.Start.isSpawned + "," + structure.Start.isBranch + "," + structure.Start.branchDepth + "," + structure.Start.isRequiredBranch + "," + structure.Start.BO3Name + "," + structure.Start.rotation.toString() + "," + structure.Start.getX() + "," + structure.Start.getY() + "," + structure.Start.getZ() + "," + structure.startChunkBlockChecksDone + "]");
		    		stringbuilder.append("[" + entry.getKey().getChunkX() + "," + entry.getKey().getChunkZ() + "][" + structure.Start.BO3Name + "," + structure.Start.rotation.toString() + "," + structure.Start.getX() + "," + structure.Start.getY() + "," + structure.Start.getZ() + "]");
		    	} else {
		    		stringbuilder.append("[" + entry.getKey().getChunkX() + "," + entry.getKey().getChunkZ() + "][Null structure]");
		    	}

				ChunkCoordinate key;
				Stack<CustomObjectCoordinate> coords;

				// If this is the origin of this structure then save its ObjectsToSpawn and SmoothingAreasToSpawn
				// All the chunks belonging to this structure will be reconstituted when this file is loaded

				stringbuilder.append("[");
				if(structure.ObjectsToSpawn.entrySet().size() > 0 && chunkCoord.getChunkX() == structure.Start.getChunkX() && chunkCoord.getChunkZ() == structure.Start.getChunkZ())
				{
					boolean added = false;
					for(Entry<ChunkCoordinate, Stack<CustomObjectCoordinate>> objectToSpawn : structure.ObjectsToSpawn.entrySet())
					{
						if(added)
						{
							stringbuilder.append(";");
						}
						key = objectToSpawn.getKey();
						stringbuilder.append(key.getChunkX() + "," + key.getChunkZ());
						added = true;

						coords = objectToSpawn.getValue();
						for(CustomObjectCoordinate coord : coords)
						{
							//stringbuilder.append("," + coord.isSpawned + "," + coord.isBranch + "," + coord.branchDepth + "," + coord.isRequiredBranch + "," + coord.BO3Name + "," + coord.rotation.toString() + "," + coord.getX() + "," + coord.getY() + "," + coord.getZ());
							stringbuilder.append("," + coord.BO3Name + "," + coord.rotation.toString() + "," + coord.getX() + "," + coord.getY() + "," + coord.getZ());
						}
					}
				}

				stringbuilder.append("][");

				if(structure.SmoothingAreasToSpawn.entrySet().size() > 0 && chunkCoord.getChunkX() == structure.Start.getChunkX() && chunkCoord.getChunkZ() == structure.Start.getChunkZ())
				{
					boolean added = false;
					ArrayList<Object[]> coords2;
					String append;
					for(Entry<ChunkCoordinate, ArrayList<Object[]>> smoothingAreaToSpawn : structure.SmoothingAreasToSpawn.entrySet())
					{
						if(added)
						{
							stringbuilder.append(";");
						}
						key = smoothingAreaToSpawn.getKey();
						stringbuilder.append(key.getChunkX() + "," + key.getChunkZ());
						added = true;

						coords2 = smoothingAreaToSpawn.getValue();
						for(Object[] coord : coords2)
						{
							append = ":";
							for(int i = 0; i < coord.length; i++)
							{
								if(append.length() == 1)
								{
									append += coord[i];
								} else {
									append += "," + coord[i];
								}
							}
							stringbuilder.append(append);
						}
					}
				}

				stringbuilder.append("][");

				if(structure.modData.size() > 0 && chunkCoord.getChunkX() == structure.Start.getChunkX() && chunkCoord.getChunkZ() == structure.Start.getChunkZ())
				{
					boolean added = false;
					for(ModDataFunction modData : structure.modData)
					{
						if(added)
						{
							stringbuilder.append(":");
						}
						stringbuilder.append(modData.x + "," + modData.y + "," + modData.z + "," + modData.modId.replace(":", "&#58;").replace(" ", "&nbsp;") + "," + modData.modData.replace(":", "&#58;").replace(" ", "&nbsp;"));
						added = true;
					}
				}

				stringbuilder.append("][");

				if(structure.spawnerData.size() > 0 && chunkCoord.getChunkX() == structure.Start.getChunkX() && chunkCoord.getChunkZ() == structure.Start.getChunkZ())
				{
					boolean added = false;
					for(SpawnerFunction spawnerData : structure.spawnerData)
					{
						if(added)
						{
							stringbuilder.append(":");
						}
						stringbuilder.append(spawnerData.x + "," + spawnerData.y + "," + spawnerData.z + "," + spawnerData.mobName.replace(":", "&#58;").replace(" ", "&nbsp;") + "," + spawnerData.originalnbtFileName.replace(":", "&#58;").replace(" ", "&nbsp;") + "," + spawnerData.nbtFileName.replace(":", "&#58;").replace(" ", "&nbsp;") + "," + spawnerData.groupSize + "," + spawnerData.interval + "," + spawnerData.spawnChance + "," + spawnerData.maxCount + "," + spawnerData.despawnTime + "," + spawnerData.velocityX + "," + spawnerData.velocityY + "," + spawnerData.velocityZ + "," + spawnerData.velocityXSet + "," + spawnerData.velocityYSet + "," + spawnerData.velocityZSet + "," + spawnerData.yaw + "," + spawnerData.pitch);
						added = true;
					}
				}

				stringbuilder.append("][");

				if(structure.particleData.size() > 0 && chunkCoord.getChunkX() == structure.Start.getChunkX() && chunkCoord.getChunkZ() == structure.Start.getChunkZ())
				{
					boolean added = false;
					for(ParticleFunction particleData : structure.particleData)
					{
						if(added)
						{
							stringbuilder.append(":");
						}
						stringbuilder.append(particleData.x + "," + particleData.y + "," + particleData.z + "," + particleData.particleName.replace(":", "&#58;").replace(" ", "&nbsp;")+ "," + particleData.interval + "," + particleData.velocityX + "," + particleData.velocityY + "," + particleData.velocityZ + "," + particleData.velocityXSet + "," + particleData.velocityYSet + "," + particleData.velocityZSet);
						added = true;
					}
				}

				stringbuilder.append("]");
			}

			stringbuilder.append("]");

			BufferedWriter writer = null;
	        try
	        {
	        	if(occupiedChunksFile.exists())
	        	{
	        		occupiedChunksFile.delete();
	        	}
	        	occupiedChunksFile.getParentFile().mkdirs();
	        	writer = new BufferedWriter(new FileWriter(occupiedChunksFile));
	            writer.write(stringbuilder.toString());
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
	        finally
	        {
	            try
	            {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) { }
	        }
    	}
    }

	private void LoadStructureCache()
	{
		OTG.log(LogMarker.INFO, "Loading structures and pre-generator data");

    	int structuresLoaded = 0;

		Map<ChunkCoordinate, CustomObjectStructure> loadedStructures = LoadStructuresFile();

		for(Map.Entry<ChunkCoordinate, CustomObjectStructure> loadedStructure : loadedStructures.entrySet())
		{
			structuresLoaded += 1;

			if(loadedStructure == null)
			{
				throw new RuntimeException();
			}

			worldInfoChunks.put(loadedStructure.getKey(), loadedStructure.getValue());

			if(world.getConfigs().getWorldConfig().IsOTGPlus)
			{
				if(!world.IsInsidePregeneratedRegion(loadedStructure.getKey(), true) && !structureCache.containsKey(loadedStructure.getKey())) // Dont override any loaded structures that have been added to the structure cache
				{
					// This chunk is either
					// A. outside the border and has no objects to spawn (empty chunk) but has not yet been populated
					// B. Part of but not the starting point of a branching structure, therefore the structure's ObjectsToSpawn and SmoothingAreasToSpawn were not saved with this file.
					structureCache.put(loadedStructure.getKey(), loadedStructure.getValue());
				}

				// The starting structure in a branching structure is saved with the ObjectsToSpawn, SmoothingAreasToSpawn & modData of all its branches.
				// All branches are saved as individual structures but without any ObjectsToSpawn/SmoothingAreasToSpawn/modData (only essential data for structure placement remains).
				// The starting structure overrides any empty branches that were added as structures here if it has any ObjectsToSpawn/SmoothingAreasToSpawn/modData in their chunks.

				for(ChunkCoordinate chunkCoord : loadedStructure.getValue().ObjectsToSpawn.keySet())
				{
					if(!world.IsInsidePregeneratedRegion(chunkCoord, true))
					{
						structureCache.put(chunkCoord, loadedStructure.getValue()); // This structure has BO3 blocks that need to be spawned
					} else {
						throw new RuntimeException();
					}
				}
				for(ChunkCoordinate chunkCoord : loadedStructure.getValue().SmoothingAreasToSpawn.keySet())
				{
					if(!world.IsInsidePregeneratedRegion(chunkCoord, true))
					{
						structureCache.put(chunkCoord, loadedStructure.getValue()); // This structure has smoothing area blocks that need to be spawned
					} else {
						throw new RuntimeException();
					}
				}
			}

			for(ModDataFunction modDataFunc : loadedStructure.getValue().modData)
			{
				worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(modDataFunc.x, modDataFunc.z), loadedStructure.getValue());
			}

			for(SpawnerFunction spawnerFunc : loadedStructure.getValue().spawnerData)
			{
				worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(spawnerFunc.x, spawnerFunc.z), loadedStructure.getValue());
			}

			for(ParticleFunction particleFunc : loadedStructure.getValue().particleData)
			{
				worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(particleFunc.x, particleFunc.z), loadedStructure.getValue());
			}
		}

		OTG.log(LogMarker.INFO, "Loaded " + structuresLoaded + " structure chunks");

		if(world.getConfigs().getWorldConfig().IsOTGPlus)
		{
			ArrayList<ChunkCoordinate> nullChunks = LoadChunksFile("NullChunks.txt");
			for(ChunkCoordinate chunkCoord : nullChunks)
			{
				structureCache.remove(chunkCoord);
				if(!world.IsInsidePregeneratedRegion(chunkCoord, true))
				{
					structureCache.put(chunkCoord, null); // This chunk has been completely populated and spawned
				} else {

					// This should only happen when a world is loaded that was generated with a PregenerationRadius of 0 and then had its PregenerationRadius increased

					OTG.log(LogMarker.INFO, "Running " + world.GetWorldSession().getPreGeneratorIsRunning() +  " L" + world.GetWorldSession().getPregeneratedBorderLeft() + " R" + world.GetWorldSession().getPregeneratedBorderRight() + " T" + world.GetWorldSession().getPregeneratedBorderTop() + " B" + world.GetWorldSession().getPregeneratedBorderBottom());
					OTG.log(LogMarker.INFO, "Error at Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
					throw new RuntimeException();
				}
			}

			spawnedStructures = LoadChunksMapFile("SpawnedStructures.txt");

			for(ChunkCoordinate chunkCoord : structureCache.keySet())
			{
				structuresPerChunk.put(chunkCoord, new ArrayList<String>()); // This is an optimisation so that PlotStructures knows not to plot anything in this chunk
			}

			if(loadedStructures.size() > 0 || nullChunks.size() > 0 || spawnedStructures.size() > 0)
			{
				world.getObjectSpawner().StructurePlottedAtSpawn = true;
			}
		}

		OTG.log(LogMarker.INFO, "Loading done");
	}

	private Map<ChunkCoordinate, CustomObjectStructure> LoadStructuresFile()
	{
	    Map<ChunkCoordinate, CustomObjectStructure> structuresFile = new HashMap<ChunkCoordinate, CustomObjectStructure>();

		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + "StructureData.txt");

		StringBuilder stringbuilder = new StringBuilder();
	    if(occupiedChunksFile.exists())
	    {
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(occupiedChunksFile));
				try
				{
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				} finally {
					reader.close();
				}
			}
			catch (FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
	    } else {
	    	return structuresFile;
	    }

	    String[] structuresString = stringbuilder.toString().substring(1, stringbuilder.length() - 1).split(" ");

	    for(int i = 0; i < structuresString.length; i++)
	    {
	    	String[] structureStringArray = structuresString[i].substring(1, structuresString[i].length() - 1).split("\\]\\[");
	    	String structureString = structureStringArray[1];

		    int minY = 0;
		    CustomObjectCoordinate structureStart = null;
		    Map<ChunkCoordinate, Stack<CustomObjectCoordinate>> ObjectsToSpawn = new HashMap<ChunkCoordinate, Stack<CustomObjectCoordinate>>();
		    Map<ChunkCoordinate, ArrayList<Object[]>> SmoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<Object[]>>();
		    HashSet<ModDataFunction> ModData = new HashSet<ModDataFunction>();
		    HashSet<SpawnerFunction> SpawnerData = new HashSet<SpawnerFunction>();
		    HashSet<ParticleFunction> ParticleData = new HashSet<ParticleFunction>();

		    String[] chunkCoordString = structureStringArray[0].split(",");
		    int chunkX = Integer.parseInt(chunkCoordString[0]);
		    int chunkZ = Integer.parseInt(chunkCoordString[1]);
		    ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);

		    if(!structureString.equals("Null structure"))
		    {
		    	structureStart = new CustomObjectCoordinate(world, null, null, null, 0, 0, 0, false, 0, false, false, null);

			    stringbuilder = null;
			    String[] structureStartString = structureStringArray[1].split(",");
			    String[] objectsToSpawnString = {};
			    if(structureStringArray.length > 2 && !structureStringArray[2].equals(""))
			    {
			    	objectsToSpawnString = structureStringArray[2].split(";");
			    }
			    String[] smoothingAreasToSpawnString = {};
			    if(structureStringArray.length > 3 && !structureStringArray[3].equals(""))
		    	{
			    	smoothingAreasToSpawnString = structureStringArray[3].split(";");
		    	}
			    String[] modDataString = {};
			    if(structureStringArray.length > 4 && !structureStringArray[4].equals(""))
			    {
			    	modDataString = structureStringArray[4].split(":");
			    }
			    String[] spawnerDataString = {};
			    if(structureStringArray.length > 5 && !structureStringArray[5].equals(""))
			    {
			    	spawnerDataString = structureStringArray[5].split(":");
			    }
			    String[] particleDataString = {};
			    if(structureStringArray.length > 6 && !structureStringArray[6].equals(""))
			    {
			    	particleDataString = structureStringArray[6].split(":");
			    }

			    structureStart.BO3Name = structureStartString[0];
			    structureStart.rotation = Rotation.FromString(structureStartString[1]);
			    structureStart.x = Integer.parseInt(structureStartString[2]);
			    structureStart.y = Integer.parseInt(structureStartString[3]);
			    structureStart.z = Integer.parseInt(structureStartString[4]);

			    ChunkCoordinate chunk;
			    Stack<CustomObjectCoordinate> coords;
			    CustomObjectCoordinate coord;
			    String[] objectAsString;

			    for(String objectToSpawn : objectsToSpawnString)
			    {
			    	objectAsString = objectToSpawn.split(",");

			    	chunk = ChunkCoordinate.fromChunkCoords(Integer.parseInt(objectAsString[0]),Integer.parseInt(objectAsString[1]));
			    	coords = new Stack<CustomObjectCoordinate>();
			    	for(int j = 2; j < objectAsString.length; j += 5)//9)
			    	{
					    coord = new CustomObjectCoordinate(world, null, null, null, 0, 0, 0, false, 0, false, false, null);

					    coord.BO3Name = objectAsString[j];
					    coord.rotation = Rotation.FromString(objectAsString[j + 1]);
					    coord.x = Integer.parseInt(objectAsString[j + 2]);
					    coord.y = Integer.parseInt(objectAsString[j + 3]);
					    coord.z = Integer.parseInt(objectAsString[j + 4]);
					    coords.add(coord);
			    	}
			    	ObjectsToSpawn.put(chunk, coords);
			    }

			    ArrayList<Object[]> coords2;
			    ArrayList<Object> objects;
			    for(String smoothingAreaToSpawn : smoothingAreasToSpawnString)
			    {
			    	objectAsString = smoothingAreaToSpawn.split(":");

			    	chunk = ChunkCoordinate.fromChunkCoords(Integer.parseInt(objectAsString[0].split(",")[0]),Integer.parseInt(objectAsString[0].split(",")[1]));

			    	coords2 = new ArrayList<Object[]>();

			    	for(String objectArray : objectAsString)
			    	{
			    		if(objectArray != objectAsString[0])
			    		{
			    			objects = new ArrayList<Object>();
			    			for(String object : objectArray.split(","))
			    			{
			    				objects.add(Integer.parseInt(object));
			    			}
			    			coords2.add(objects.toArray());
			    		}
			    	}

			    	SmoothingAreasToSpawn.put(chunk, coords2);
			    }

			    for(String modData : modDataString)
			    {
			    	objectAsString = modData.split(",");

			    	for(int j = 0; j < objectAsString.length; j += 5)
			    	{
				    	ModDataFunction modDataFunction = new ModDataFunction();
	    				modDataFunction.x = Integer.parseInt(objectAsString[j]);
			    		modDataFunction.y = Integer.parseInt(objectAsString[j + 1]);
			    		modDataFunction.z = Integer.parseInt(objectAsString[j + 2]);
			    		modDataFunction.modId = objectAsString[j + 3].replace("&#58;",":").replace("&nbsp;", " ");
			    		modDataFunction.modData = objectAsString[j + 4].replace("&#58;",":").replace("&nbsp;", " ");
				    	ModData.add(modDataFunction);
			    	}
			    }

			    for(String spawnerData : spawnerDataString)
			    {
			    	objectAsString = spawnerData.split(",");

			    	for(int j = 0; j < objectAsString.length; j += 19)
			    	{
				    	SpawnerFunction spawnerFunction = new SpawnerFunction();

				    	spawnerFunction.x = Integer.parseInt(objectAsString[j]);
				    	spawnerFunction.y = Integer.parseInt(objectAsString[j + 1]);
				    	spawnerFunction.z = Integer.parseInt(objectAsString[j + 2]);
				    	spawnerFunction.mobName = objectAsString[j + 3].replace("&#58;",":").replace("&nbsp;", " ");
				    	spawnerFunction.originalnbtFileName = objectAsString[j + 4].replace("&#58;",":").replace("&nbsp;", " ");
				    	spawnerFunction.nbtFileName = objectAsString[j + 5].replace("&#58;",":").replace("&nbsp;", " ");
				    	spawnerFunction.groupSize = Integer.parseInt(objectAsString[j + 6]);
				    	spawnerFunction.interval =  Integer.parseInt(objectAsString[j + 7]);
				    	spawnerFunction.spawnChance =  Integer.parseInt(objectAsString[j + 8]);
				    	spawnerFunction.maxCount =  Integer.parseInt(objectAsString[j + 9]);
				    	spawnerFunction.despawnTime =  Integer.parseInt(objectAsString[j + 10]);
				    	spawnerFunction.velocityX =  Double.parseDouble(objectAsString[j + 11]);
				    	spawnerFunction.velocityY =  Double.parseDouble(objectAsString[j + 12]);
				    	spawnerFunction.velocityZ =  Double.parseDouble(objectAsString[j + 13]);
				    	spawnerFunction.velocityXSet =  Boolean.parseBoolean(objectAsString[j + 14]);
				    	spawnerFunction.velocityYSet =  Boolean.parseBoolean(objectAsString[j + 15]);
				    	spawnerFunction.velocityZSet =  Boolean.parseBoolean(objectAsString[j + 16]);
				    	spawnerFunction.yaw =  Float.parseFloat(objectAsString[j + 17]);
				    	spawnerFunction.pitch =  Float.parseFloat(objectAsString[j + 18]);

				    	SpawnerData.add(spawnerFunction);
			    	}
			    }

			    for(String particleData : particleDataString)
			    {
			    	objectAsString = particleData.split(",");

			    	for(int j = 0; j < objectAsString.length; j += 11)
			    	{
				    	ParticleFunction particleFunction = new ParticleFunction();

				    	particleFunction.x = Integer.parseInt(objectAsString[j]);
				    	particleFunction.y = Integer.parseInt(objectAsString[j + 1]);
				    	particleFunction.z = Integer.parseInt(objectAsString[j + 2]);
				    	particleFunction.particleName = objectAsString[j + 3].replace("&#58;",":").replace("&nbsp;", " ");

				    	particleFunction.interval = Double.parseDouble(objectAsString[j + 4]);

				    	particleFunction.velocityX =  Double.parseDouble(objectAsString[j + 5]);
				    	particleFunction.velocityY =  Double.parseDouble(objectAsString[j + 6]);
				    	particleFunction.velocityZ =  Double.parseDouble(objectAsString[j + 7]);
				    	particleFunction.velocityXSet =  Boolean.parseBoolean(objectAsString[j + 8]);
				    	particleFunction.velocityYSet =  Boolean.parseBoolean(objectAsString[j + 9]);
				    	particleFunction.velocityZSet =  Boolean.parseBoolean(objectAsString[j + 10]);

				    	ParticleData.add(particleFunction);
			    	}
			    }
		    }

		    CustomObjectStructure structure;
		    if(world.getConfigs().getWorldConfig().IsOTGPlus)
		    {
		    	structure = new CustomObjectStructure(world, structureStart, ObjectsToSpawn, SmoothingAreasToSpawn, minY);
		    } else {
		    	structure = new CustomObjectStructure(structureStart);
		    }
		    structure.startChunkBlockChecksDone = true;
		    structure.saveRequired = false;
		    structure.modData = ModData;
		    structure.spawnerData = SpawnerData;
		    structure.particleData = ParticleData;

		    structuresFile.put(chunkCoord, structure);
	    }
	    return structuresFile;
	}

	public void SaveChunksFile(ArrayList<ChunkCoordinate> chunks, String fileName)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + fileName);
		if(occupiedChunksFile.exists())
		{
			occupiedChunksFile.delete();
		}

		if(chunks.size() > 0)
		{
			StringBuilder stringbuilder = new StringBuilder();
			for(ChunkCoordinate chunkCoord : chunks)
			{
				if(stringbuilder.length() > 0)
				{
					stringbuilder.append("," + chunkCoord.getChunkX() + "," + chunkCoord.getChunkZ());
				} else {
					stringbuilder.append(chunkCoord.getChunkX() + "," + chunkCoord.getChunkZ());
				}
			}

			BufferedWriter writer = null;
	        try
	        {
	        	occupiedChunksFile.getParentFile().mkdirs();
	        	writer = new BufferedWriter(new FileWriter(occupiedChunksFile));
	            writer.write(stringbuilder.toString());
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
	        finally
	        {
	            try
	            {
	                // Close the writer regardless of what happens...
	                writer.close();
	            }
	            catch (Exception e) { }
	        }
		}
	}

	public ArrayList<ChunkCoordinate> LoadChunksFile(String fileName)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + fileName);

		StringBuilder stringbuilder = new StringBuilder();
		String[] occupiedChunkCoords = {};
		if(occupiedChunksFile.exists())
		{
			try {
				BufferedReader reader = new BufferedReader(new FileReader(occupiedChunksFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	occupiedChunkCoords = stringbuilder.toString().split(",");
				    }
				} finally {
					reader.close();
				}
			}
			catch (FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}

		ArrayList<ChunkCoordinate> chunks = new ArrayList<ChunkCoordinate>();
		if(occupiedChunkCoords.length > 0)
		{
			for(int i = 0; i < occupiedChunkCoords.length; i += 2)
			{
				chunks.add(ChunkCoordinate.fromChunkCoords(Integer.parseInt(occupiedChunkCoords[i]),Integer.parseInt(occupiedChunkCoords[i + 1])));
			}
		}
		return chunks;
	}

	private void SaveChunksMapFile(Map<String, Stack<ChunkCoordinate>> chunksMap, String fileName)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + fileName);

		if(occupiedChunksFile.exists())
		{
			occupiedChunksFile.delete();
		}

		if(chunksMap.size() > 0)
		{
			StringBuilder stringbuilder = new StringBuilder();
			for(Map.Entry<String, Stack<ChunkCoordinate>> entry : chunksMap.entrySet())
			{
				if(stringbuilder.length() == 0)
				{
					stringbuilder.append(entry.getKey().replace(",", "\\"));
				} else {
					stringbuilder.append("/" + entry.getKey().replace(",", "\\"));
				}
				for(ChunkCoordinate chunkCoord : entry.getValue())
				{
					stringbuilder.append("," + chunkCoord.getChunkX() + "," + chunkCoord.getChunkZ());
				}
			}

			BufferedWriter writer = null;
	        try
	        {
	        	occupiedChunksFile.getParentFile().mkdirs();
	        	writer = new BufferedWriter(new FileWriter(occupiedChunksFile));
	            writer.write(stringbuilder.toString());
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
	        finally
	        {
	            try
	            {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) { }
	        }
		}
	}

	public Map<String, Stack<ChunkCoordinate>> LoadChunksMapFile(String fileName)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + fileName);

		Map<String, Stack<ChunkCoordinate>> chunks = new HashMap<String, Stack<ChunkCoordinate>>();

		StringBuilder stringbuilder = new StringBuilder();
		String[] occupiedChunks = {};
		if(occupiedChunksFile.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(occupiedChunksFile));
				try
				{
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        //sb.append(System.lineSeparator());
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	occupiedChunks = stringbuilder.toString().split("/");
				    }
				} finally {
					reader.close();
				}

			}
			catch (FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}

		String[] occupiedChunkCoords = {};
		for(String entry : occupiedChunks)
		{
			occupiedChunkCoords = entry.split(",");
			String key = occupiedChunkCoords[0].replace("\\", ",");
			Stack<ChunkCoordinate> value = new Stack<ChunkCoordinate>();

			if(occupiedChunkCoords.length > 0)
			{
				for(int i = 1; i < occupiedChunkCoords.length; i += 2)
				{
					value.add(ChunkCoordinate.fromChunkCoords(Integer.parseInt(occupiedChunkCoords[i]),Integer.parseInt(occupiedChunkCoords[i + 1])));
				}
			}

			chunks.put(key, value);
		}
		return chunks;
	}
}
