package com.pg85.otg.customobjects.customstructure;

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
import java.util.Stack;
import java.util.Map.Entry;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.customobjects.bo3.bo3function.ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.SpawnerFunction;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class CustomObjectStructureFileManager
{
	public static Map<ChunkCoordinate, CustomObjectStructure> loadStructuresFile(LocalWorld world)
	{
	    Map<ChunkCoordinate, CustomObjectStructure> structuresFile = new HashMap<ChunkCoordinate, CustomObjectStructure>();

		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + WorldStandardValues.StructureDataFileName);

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
		    if(world.getConfigs().getWorldConfig().isOTGPlus)
		    {
		    	structure = new CustomObjectStructure(world, structureStart, ObjectsToSpawn, SmoothingAreasToSpawn, minY);
		    } else {
		    	structure = new CustomObjectStructure(structureStart);
		    }
		    structure.startChunkBlockChecksDone = true;
		    structure.saveRequired = false;
		    structure.modDataManager.modData = ModData;
		    structure.spawnerManager.spawnerData = SpawnerData;
		    structure.particlesManager.particleData = ParticleData;

		    structuresFile.put(chunkCoord, structure);
	    }
	    return structuresFile;
	}

	public static void saveChunksFile(ArrayList<ChunkCoordinate> chunks, String fileName, LocalWorld world)
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

	public static ArrayList<ChunkCoordinate> loadChunksFile(String fileName, LocalWorld world)
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

	public static void saveChunksMapFile(String fileName, LocalWorld world, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + fileName);

		if(occupiedChunksFile.exists())
		{
			occupiedChunksFile.delete();
		}

		if(spawnedStructuresByName.size() > 0)
		{
			StringBuilder stringbuilder = new StringBuilder();
			for(Map.Entry<String, ArrayList<ChunkCoordinate>> entry : spawnedStructuresByName.entrySet())
			{
				if(stringbuilder.length() == 0)
				{
					stringbuilder.append(entry.getKey().replace(",", "\\"));
				} else {
					stringbuilder.append("/" + entry.getKey().replace(",", "\\"));
				}
				for(ChunkCoordinate chunkCoord : entry.getValue())
				{
					stringbuilder.append("," + chunkCoord.getChunkX() + "," + chunkCoord.getChunkZ() + ",");
				}
			}
			
			stringbuilder.append("|");
			
			for(Entry<String, HashMap<ChunkCoordinate, Integer>> entry : spawnedStructuresByGroup.entrySet())
			{
				if(stringbuilder.length() == 0)
				{
					stringbuilder.append(entry.getKey().replace(",", "\\"));
				} else {
					stringbuilder.append("/" + entry.getKey().replace(",", "\\"));
				}
				for(Entry<ChunkCoordinate, Integer> valueEntry : entry.getValue().entrySet())
				{					
					stringbuilder.append("," + valueEntry.getKey().getChunkX() + "," + valueEntry.getKey().getChunkZ() + "," + valueEntry.getValue().intValue() + ",");
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

	public static void loadChunksMapFile(String fileName, LocalWorld world, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + fileName);

		HashMap<String, ArrayList<ChunkCoordinate>> chunksByName = new HashMap<String, ArrayList<ChunkCoordinate>>();
		HashMap<String, HashMap<ChunkCoordinate, Integer>> chunksByGroup = new HashMap<String, HashMap<ChunkCoordinate, Integer>>();

		StringBuilder stringbuilder = new StringBuilder();
		String[] occupiedChunksByName = {};
		String[] occupiedChunksByGroup = {};
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
				    	String[] allData = stringbuilder.toString().split("|");
				    	occupiedChunksByName = allData[0].split("/");
				    	if(allData.length > 1) // Legacy files may not have occupiedChunksByGroup
				    	{
				    		occupiedChunksByGroup = allData[1].split("/");
				    	}
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

		String[] occupiedChunkByNameCoords = {};
		for(String entry : occupiedChunksByName)
		{
			occupiedChunkByNameCoords = entry.split(",");
			String key = occupiedChunkByNameCoords[0].replace("\\", ",");
			ArrayList<ChunkCoordinate> value = new ArrayList<ChunkCoordinate>();

			if(occupiedChunkByNameCoords.length > 0)
			{
				for(int i = 1; i < occupiedChunkByNameCoords.length; i += 2)
				{
					value.add(ChunkCoordinate.fromChunkCoords(Integer.parseInt(occupiedChunkByNameCoords[i]),Integer.parseInt(occupiedChunkByNameCoords[i + 1])));
				}
			}

			chunksByName.put(key, value);
		}
		
		spawnedStructuresByName.clear();
		spawnedStructuresByName.putAll(chunksByName);
		
		String[] occupiedChunkByGroupCoords = {};
		for(String entry : occupiedChunksByGroup)
		{
			occupiedChunkByGroupCoords = entry.split(",");
			String key = occupiedChunkByGroupCoords[0].replace("\\", ",");
			HashMap<ChunkCoordinate, Integer> value = new HashMap<ChunkCoordinate, Integer>();

			if(occupiedChunkByGroupCoords.length > 0)
			{
				for(int i = 1; i < occupiedChunkByGroupCoords.length; i += 3)
				{
					value.put(ChunkCoordinate.fromChunkCoords(Integer.parseInt(occupiedChunkByGroupCoords[i]),Integer.parseInt(occupiedChunkByGroupCoords[i + 2])), Integer.valueOf(Integer.parseInt(occupiedChunkByGroupCoords[i + 1])));
				}
			}

			chunksByGroup.put(key, value);
		}
		
		spawnedStructuresByGroup.clear();
		spawnedStructuresByGroup.putAll(chunksByGroup);
	}
	

	public static void saveStructuresFile(Map<ChunkCoordinate, CustomObjectStructure> structures, LocalWorld world)
	{
		// When loading files we first load all the structure files and put them in worldInfoChunks and structurecache (if they are outside the pregenerated region),
		// then from any structures that have ObjectsToSpawn or SmoothingAreas to spawn we create structures in the structure cache for each of those (overriding some of the structures we added earlier)
		// Then we load all the null chunks and add them to the structurecache (if they are outside the pregenerated region), potentially overriding some of the structures we added earlier.

		// So don't worry about saving structure files for structures that have already been spawned, they won't be added to the structure cache when loading

		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + WorldStandardValues.StructureDataFileName);
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

				if(structure.modDataManager.modData.size() > 0 && chunkCoord.getChunkX() == structure.Start.getChunkX() && chunkCoord.getChunkZ() == structure.Start.getChunkZ())
				{
					boolean added = false;
					for(ModDataFunction modData : structure.modDataManager.modData)
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

				if(structure.spawnerManager.spawnerData.size() > 0 && chunkCoord.getChunkX() == structure.Start.getChunkX() && chunkCoord.getChunkZ() == structure.Start.getChunkZ())
				{
					boolean added = false;
					for(SpawnerFunction spawnerData : structure.spawnerManager.spawnerData)
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

				if(structure.particlesManager.particleData.size() > 0 && chunkCoord.getChunkX() == structure.Start.getChunkX() && chunkCoord.getChunkZ() == structure.Start.getChunkZ())
				{
					boolean added = false;
					for(ParticleFunction particleData : structure.particlesManager.particleData)
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
}
