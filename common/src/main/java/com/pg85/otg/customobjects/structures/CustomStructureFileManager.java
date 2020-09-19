package com.pg85.otg.customobjects.structures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3SpawnerFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4ModDataFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4ParticleFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4SpawnerFunction;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.customobjects.bofunctions.SpawnerFunction;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructure;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.bo4.SmoothingAreaLine;
import com.pg85.otg.customobjects.structures.bo4.SmoothingAreaLineDiagonal;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class CustomStructureFileManager
{
	static void saveStructuresFile(Map<ChunkCoordinate, CustomStructure> structures, LocalWorld world)
	{
		// When loading files we first load all the structure files and put them in worldInfoChunks and structurecache 
		// (if they are outside the pregenerated region), then from any structures that have ObjectsToSpawn or 
		// SmoothingAreas to spawn we create structures in the structure cache for each of those (overriding some of 
		// the structures we added earlier). Then we load all the null chunks and add them to the structurecache (if 
		// they are outside the pregenerated region), potentially overriding some of the structures we added earlier.

		// So don't worry about saving structure files for structures that have already been spawned, they won't be added to the structure cache when loading

		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.StructureDataFileName);
		File occupiedChunksBackupFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.StructureDataBackupFileName);

    	StringBuilder stringbuilder = new StringBuilder();
    	if(structures.size() > 0)
    	{
	    	stringbuilder.append("[");

			for(Entry<ChunkCoordinate, CustomStructure> entry : structures.entrySet())
			{
				ChunkCoordinate chunkCoord = entry.getKey();
				CustomStructure structure = entry.getValue();

				if(stringbuilder.length() > 1)
				{
					stringbuilder.append(" ");
				}

		    	if(structure.start != null)
		    	{
		    		//stringbuilder.append("[" + entry.getKey().getChunkX() + "," + entry.getKey().getChunkZ() + "][" + structure.MinY + "," + structure.Start.isSpawned + "," + structure.Start.isBranch + "," + structure.Start.branchDepth + "," + structure.Start.isRequiredBranch + "," + structure.Start.BO3Name + "," + structure.Start.rotation.toString() + "," + structure.Start.getX() + "," + structure.Start.getY() + "," + structure.Start.getZ() + "," + structure.startChunkBlockChecksDone + "]");
		    		stringbuilder.append("[" + entry.getKey().getChunkX() + "," + entry.getKey().getChunkZ() + "][" + structure.start.bo3Name + "," + structure.start.rotation.toString() + "," + structure.start.getX() + "," + structure.start.getY() + "," + structure.start.getZ() + "]");
		    	} else {
		    		stringbuilder.append("[" + entry.getKey().getChunkX() + "," + entry.getKey().getChunkZ() + "][Null structure]");
		    	}

				ChunkCoordinate key;
				Stack<BO4CustomStructureCoordinate> coords;

				// If this is the origin of this structure then save its ObjectsToSpawn and SmoothingAreasToSpawn
				// All the chunks belonging to this structure will be reconstituted when this file is loaded				
				stringbuilder.append("[");
				if(structure instanceof BO4CustomStructure && ((BO4CustomStructure)structure).objectsToSpawn.entrySet().size() > 0 && chunkCoord.getChunkX() == ((BO4CustomStructureCoordinate)structure.start).getChunkX() && chunkCoord.getChunkZ() == ((BO4CustomStructureCoordinate)structure.start).getChunkZ())
				{
					boolean added = false;
					for(Entry<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectToSpawn : ((BO4CustomStructure)structure).objectsToSpawn.entrySet())
					{
						if(added)
						{
							stringbuilder.append(";");
						}
						key = objectToSpawn.getKey();
						stringbuilder.append(key.getChunkX() + "," + key.getChunkZ());
						added = true;

						coords = objectToSpawn.getValue();
						for(CustomStructureCoordinate coord : coords)
						{
							//stringbuilder.append("," + coord.isSpawned + "," + coord.isBranch + "," + coord.branchDepth + "," + coord.isRequiredBranch + "," + coord.BO3Name + "," + coord.rotation.toString() + "," + coord.getX() + "," + coord.getY() + "," + coord.getZ());
							stringbuilder.append("," + coord.bo3Name + "," + coord.rotation.toString() + "," + coord.getX() + "," + coord.getY() + "," + coord.getZ());
						}
					}
				}

				stringbuilder.append("][");

				if(structure instanceof BO4CustomStructure && ((BO4CustomStructure)structure).smoothingAreasToSpawn.entrySet().size() > 0 && chunkCoord.getChunkX() == ((BO4CustomStructureCoordinate)structure.start).getChunkX() && chunkCoord.getChunkZ() == ((BO4CustomStructureCoordinate)structure.start).getChunkZ())
				{
					boolean added = false;
					ArrayList<SmoothingAreaLine> coords2;
					String append;
					for(Entry<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreaToSpawn : ((BO4CustomStructure)structure).smoothingAreasToSpawn.entrySet())
					{
						if(added)
						{
							stringbuilder.append(";");
						}
						key = smoothingAreaToSpawn.getKey();
						stringbuilder.append(key.getChunkX() + "," + key.getChunkZ());
						added = true;

						coords2 = smoothingAreaToSpawn.getValue();
						for(SmoothingAreaLine coord : coords2)
						{
							append = ":";
							
							append += coord.beginPointX;
							append += "," + coord.beginPointY;
							append += "," + coord.beginPointZ;

							append += "," + coord.endPointX;
							append += "," + coord.endPointY;
							append += "," + coord.endPointZ;

							append += "," + coord.originPointX;
							append += "," + coord.originPointY;
							append += "," + coord.originPointZ;

							append += "," + coord.finalDestinationPointX;
							append += "," + coord.finalDestinationPointY;
							append += "," + coord.finalDestinationPointZ;
							
							if(coord instanceof SmoothingAreaLineDiagonal)
							{
								append += "," + ((SmoothingAreaLineDiagonal)coord).diagonalLineOriginPointX; // 12;
								append += "," + ((SmoothingAreaLineDiagonal)coord).diagonalLineoriginPointY; // 13;
								append += "," + ((SmoothingAreaLineDiagonal)coord).diagonalLineOriginPointZ; // 14;
								append += "," + ((SmoothingAreaLineDiagonal)coord).diagonalLineFinalDestinationPointX; // 15;
								append += "," + ((SmoothingAreaLineDiagonal)coord).diagonalLineFinalDestinationPointY; // 16;
								append += "," + ((SmoothingAreaLineDiagonal)coord).diagonalLineFinalDestinationPointZ; // 17;								
							}
							
							stringbuilder.append(append);
						}
					}
				}

				stringbuilder.append("][");

				if(structure.modDataManager.modData.size() > 0 && chunkCoord.getChunkX() == structure.start.getChunkX() && chunkCoord.getChunkZ() == structure.start.getChunkZ())
				{
					boolean added = false;
					for(ModDataFunction<?> modData : structure.modDataManager.modData)
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

				if(structure.spawnerManager.spawnerData.size() > 0 && chunkCoord.getChunkX() == structure.start.getChunkX() && chunkCoord.getChunkZ() == structure.start.getChunkZ())
				{
					boolean added = false;
					for(SpawnerFunction<?> spawnerData : structure.spawnerManager.spawnerData)
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

				if(structure.particlesManager.particleData.size() > 0 && chunkCoord.getChunkX() == structure.start.getChunkX() && chunkCoord.getChunkZ() == structure.start.getChunkZ())
				{
					boolean added = false;
					for(ParticleFunction<?> particleData : structure.particlesManager.particleData)
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
	    		if(!occupiedChunksFile.exists())
	    		{
	    			occupiedChunksFile.getParentFile().mkdirs();
	    		} else {
	    			Files.move(occupiedChunksFile.toPath(), occupiedChunksBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    		}

	        	writer = new BufferedWriter(new FileWriter(occupiedChunksFile));
	            writer.write(stringbuilder.toString());
	        }
	        catch (IOException e)
	        {
				e.printStackTrace();
				throw new RuntimeException(
					"OTG encountered a critical error writing " + occupiedChunksFile.getAbsolutePath() + ", exiting. "
					+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
					+ "If your dimension's " + WorldStandardValues.StructureDataFileName + " and its backup have been corrupted, "
					+ "you can replace it with your own backup.");
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
	
	public static Map<ChunkCoordinate, CustomStructure> loadStructuresFile(LocalWorld world)
	{		
		// When loading files 
		// 1. Load all the structure files and put them in worldInfoChunks and structurecache (if they are outside the pregenerated region), 
		// 2. For any structures that have ObjectsToSpawn or SmoothingAreas we create structures in the structurecache 
		// 3. Then we load all the null chunks and add them to the structurecache (if they are outside the pregenerated region
		// Step 2 and 3 may override structures added by the previous steps.
		
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.StructureDataFileName);
		File occupiedChunksBackupFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.StructureDataBackupFileName);

	    if(!occupiedChunksFile.exists() && !occupiedChunksBackupFile.exists())
	    {
	    	return null;
	    }
				
	    if(occupiedChunksFile.exists())
	    {
	    	StringBuilder stringbuilder = new StringBuilder();
			boolean bSuccess = false;
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
			    	bSuccess = true;
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
			}
			
		    if(bSuccess)
		    {
		    	try
		    	{
		    		String[] structuresString = stringbuilder.toString().substring(1, stringbuilder.length() - 1).split(" ");	    
			    	return parseStructuresFile(structuresString, world);
		    	}
			    catch(Exception ex)
				{
			    	ex.printStackTrace();
			    	OTG.log(LogMarker.WARN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
				}
		    }			
	    }
	    
	    if(occupiedChunksBackupFile.exists())
	    {
	    	StringBuilder stringbuilder = new StringBuilder();
			boolean bSuccess = false;
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(occupiedChunksBackupFile));
				try
				{
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
			    	bSuccess = true;
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		    if(bSuccess)
		    {
		    	try
		    	{
		    		String[] structuresString = stringbuilder.toString().substring(1, stringbuilder.length() - 1).split(" ");	    
			    	return parseStructuresFile(structuresString, world);
		    	}
			    catch(Exception ex)
				{
			    	ex.printStackTrace();
				}
		    }
	    }
	    
		throw new RuntimeException(
			"OTG encountered a critical error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
			+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
			+ "If your dimension's " + WorldStandardValues.StructureDataFileName + " and its backup have been corrupted, you can "
			+ "replace it with a backup."
		);
	}
	
	private static Map<ChunkCoordinate, CustomStructure> parseStructuresFile(String[] structuresString, LocalWorld world)
	{
	    Map<ChunkCoordinate, CustomStructure> structuresFile = new HashMap<ChunkCoordinate, CustomStructure>();
	    for(int i = 0; i < structuresString.length; i++)
	    {
	    	String[] structureStringArray = structuresString[i].substring(1, structuresString[i].length() - 1).split("\\]\\[");
	    	String structureString = structureStringArray[1];

		    int minY = 0;
		    CustomStructureCoordinate structureStart = null;
		    Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn = new HashMap<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>>();
		    Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
		    HashSet<ModDataFunction<?>> modData = new HashSet<ModDataFunction<?>>();
		    HashSet<SpawnerFunction<?>> spawnerData = new HashSet<SpawnerFunction<?>>();
		    HashSet<ParticleFunction<?>> particleData = new HashSet<ParticleFunction<?>>();

		    String[] chunkCoordString = structureStringArray[0].split(",");
		    int chunkX = Integer.parseInt(chunkCoordString[0]);
		    int chunkZ = Integer.parseInt(chunkCoordString[1]);
		    ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);

		    if(!structureString.equals("Null structure"))
		    {
		    	if(world.isBo4Enabled())
		    	{
		    		structureStart = new BO4CustomStructureCoordinate(world, null, null, null, 0, (short)0, 0, 0, false, false, null);
		    	} else {
		    		structureStart = new BO3CustomStructureCoordinate(world, null, null, null, 0, (short)0, 0);
		    	}

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

			    structureStart.bo3Name = structureStartString[0];
			    structureStart.rotation = Rotation.FromString(structureStartString[1]);
			    structureStart.x = Integer.parseInt(structureStartString[2]);
			    structureStart.y = Short.parseShort(structureStartString[3]);
			    structureStart.z = Integer.parseInt(structureStartString[4]);

			    ChunkCoordinate chunk;
			    Stack<BO4CustomStructureCoordinate> coords;
			    BO4CustomStructureCoordinate coord;
			    String[] objectAsString;

			    for(String objectToSpawn : objectsToSpawnString)
			    {
			    	objectAsString = objectToSpawn.split(",");

			    	chunk = ChunkCoordinate.fromChunkCoords(Integer.parseInt(objectAsString[0]),Integer.parseInt(objectAsString[1]));
			    	coords = new Stack<BO4CustomStructureCoordinate>();
			    	for(int j = 2; j < objectAsString.length; j += 5)//9)
			    	{
					    coord = new BO4CustomStructureCoordinate(world, null, null, null, 0, (short)0, 0, 0, false, false, null);

					    coord.bo3Name = objectAsString[j];
					    coord.rotation = Rotation.FromString(objectAsString[j + 1]);
					    coord.x = Integer.parseInt(objectAsString[j + 2]);
					    coord.y = Short.parseShort(objectAsString[j + 3]);
					    coord.z = Integer.parseInt(objectAsString[j + 4]);
					    coords.add(coord);
			    	}
			    	objectsToSpawn.put(chunk, coords);
			    }

			    ArrayList<SmoothingAreaLine> coords2;
			    SmoothingAreaLine object;
			    for(String smoothingAreaToSpawn : smoothingAreasToSpawnString)
			    {
			    	objectAsString = smoothingAreaToSpawn.split(":");

			    	chunk = ChunkCoordinate.fromChunkCoords(Integer.parseInt(objectAsString[0].split(",")[0]),Integer.parseInt(objectAsString[0].split(",")[1]));

			    	coords2 = new ArrayList<SmoothingAreaLine>();

			    	for(String objectArray : objectAsString)
			    	{
			    		if(objectArray != objectAsString[0])
			    		{
			    			
			    			String params[] = objectArray.split(",");
		    						    				
		    				if(params.length > 12)
		    				{
		    					object = new SmoothingAreaLineDiagonal();
		    				} else {
		    					object = new SmoothingAreaLine();
		    				}
		    				
		    				object.beginPointX = Integer.parseInt(params[0]);
		    				object.beginPointY = Short.parseShort(params[1]);
		    				object.beginPointZ = Integer.parseInt(params[2]);
		    				object.endPointX = Integer.parseInt(params[3]);
		    				object.endPointY = Short.parseShort(params[4]);
		    				object.endPointZ = Integer.parseInt(params[5]);
		    				object.originPointX = Integer.parseInt(params[6]);
		    				object.originPointY = Short.parseShort(params[7]);
		    				object.originPointZ = Integer.parseInt(params[8]);
		    				object.finalDestinationPointX = Integer.parseInt(params[9]);
		    				object.finalDestinationPointY = Short.parseShort(params[10]);
		    				object.finalDestinationPointZ = Integer.parseInt(params[11]);

		    				if(params.length > 12)
		    				{
		    					((SmoothingAreaLineDiagonal)object).diagonalLineOriginPointX = Integer.parseInt(params[12]);
		    					((SmoothingAreaLineDiagonal)object).diagonalLineoriginPointY = Short.parseShort(params[13]);
		    					((SmoothingAreaLineDiagonal)object).diagonalLineOriginPointZ = Integer.parseInt(params[14]);
		    					((SmoothingAreaLineDiagonal)object).diagonalLineFinalDestinationPointX = Integer.parseInt(params[15]);
		    					((SmoothingAreaLineDiagonal)object).diagonalLineFinalDestinationPointY = Short.parseShort(params[16]);
		    					((SmoothingAreaLineDiagonal)object).diagonalLineFinalDestinationPointZ = Integer.parseInt(params[17]);		    					
		    				}
		    				
			    			coords2.add(object);
			    		}
			    	}

			    	smoothingAreasToSpawn.put(chunk, coords2);
			    }

			    for(String modData1 : modDataString)
			    {
			    	objectAsString = modData1.split(",");

			    	for(int j = 0; j < objectAsString.length; j += 5)
			    	{
			    		ModDataFunction<?> modDataFunction;
				    	if(world.isBo4Enabled())
				    	{
				    		modDataFunction = new BO4ModDataFunction();
				    	} else {
				    		modDataFunction = new BO3ModDataFunction();
				    	}
				    	
	    				modDataFunction.x = Integer.parseInt(objectAsString[j]);
			    		modDataFunction.y = Integer.parseInt(objectAsString[j + 1]);
			    		modDataFunction.z = Integer.parseInt(objectAsString[j + 2]);
			    		modDataFunction.modId = objectAsString[j + 3].replace("&#58;",":").replace("&nbsp;", " ");
			    		modDataFunction.modData = objectAsString[j + 4].replace("&#58;",":").replace("&nbsp;", " ");
				    	modData.add(modDataFunction);
			    	}
			    }

			    for(String spawnerData1 : spawnerDataString)
			    {
			    	objectAsString = spawnerData1.split(",");

			    	for(int j = 0; j < objectAsString.length; j += 19)
			    	{
				    	SpawnerFunction<?> spawnerFunction;
				    	if(world.isBo4Enabled())
				    	{
				    		spawnerFunction = new BO4SpawnerFunction();
				    	} else {
				    		spawnerFunction = new BO3SpawnerFunction();
				    	}
				    	
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

				    	spawnerData.add(spawnerFunction);
			    	}
			    }

			    for(String particleData1 : particleDataString)
			    {
			    	objectAsString = particleData1.split(",");

			    	for(int j = 0; j < objectAsString.length; j += 11)
			    	{
				    	ParticleFunction<?> particleFunction;
				    	if(world.isBo4Enabled())
				    	{
				    		particleFunction = new BO4ParticleFunction();
				    	} else {
				    		particleFunction = new BO3ParticleFunction();
				    	}
				    	
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

				    	particleData.add(particleFunction);
			    	}
			    }
		    }

		    CustomStructure structure;
		    if(world.isBo4Enabled())
		    {
		    	structure = new BO4CustomStructure(world, (BO4CustomStructureCoordinate)structureStart, objectsToSpawn, smoothingAreasToSpawn, minY);
			    ((BO4CustomStructure)structure).startChunkBlockChecksDone = true;
		    } else {
		    	structure = new BO3CustomStructure((BO3CustomStructureCoordinate)structureStart);
		    }
		    structure.modDataManager.modData = modData;
		    structure.spawnerManager.spawnerData = spawnerData;
		    structure.particlesManager.particleData = particleData;

		    structuresFile.put(chunkCoord, structure);
	    }    
	    return structuresFile;
	}

	public static void saveNullChunksFile(ArrayList<ChunkCoordinate> chunks, LocalWorld world)
	{
		int dimensionId = world.getDimensionId();
		File nullChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.NullChunksFileName);
		File nullChunksBackupFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.NullChunksBackupFileName);

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
	    		if(!nullChunksFile.exists())
	    		{
	    			nullChunksFile.getParentFile().mkdirs();
	    		} else {
	    			Files.move(nullChunksFile.toPath(), nullChunksBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    		}
	    		
	        	writer = new BufferedWriter(new FileWriter(nullChunksFile));
	            writer.write(stringbuilder.toString());
	        }
	        catch (IOException e)
	        {
				e.printStackTrace();
				throw new RuntimeException(
					"OTG encountered a critical error writing " + nullChunksFile.getAbsolutePath() + ", exiting. "
					+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
					+ "If your dimension's " + WorldStandardValues.NullChunksFileName + " and its backup have been corrupted, "
					+ "you can replace it with your own backup.");
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

	public static ArrayList<ChunkCoordinate> loadNullChunksFile(LocalWorld world)
	{
		int dimensionId = world.getDimensionId();
		File nullChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.NullChunksFileName);
		File nullChunksBackupFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.NullChunksBackupFileName);

		if(!nullChunksFile.exists() && !nullChunksBackupFile.exists())
		{
			return null;
		}
		
		if(nullChunksFile.exists())
		{
			StringBuilder stringbuilder = new StringBuilder();
			String[] nullChunkCoords = {};
			boolean bSuccess = false;			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(nullChunksFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	nullChunkCoords = stringbuilder.toString().split(",");
				    }
				    bSuccess = true;				    
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + nullChunksFile.getAbsolutePath() + ", trying to load backup.");
			}
			
			if(bSuccess)
			{
				try
				{
					return parseNullChunks(nullChunkCoords);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					OTG.log(LogMarker.WARN, "Failed to load " + nullChunksFile.getAbsolutePath() + ", trying to load backup.");
				}
			}
		}
		
		if(nullChunksBackupFile.exists())
		{
			StringBuilder stringbuilder = new StringBuilder();
			String[] nullChunkCoords = {};
			boolean bSuccess = false;			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(nullChunksBackupFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	nullChunkCoords = stringbuilder.toString().split(",");
					    bSuccess = true;
				    }
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if(bSuccess)
			{
				try
				{
					return parseNullChunks(nullChunkCoords);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}			
		}
		
		throw new RuntimeException(
			"OTG encountered a critical error loading " + nullChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
			+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
			+ "If your dimension's " + WorldStandardValues.NullChunksFileName + " and its backup have been corrupted, you can "
			+ "replace it with a backup.");
	}
	
	private static ArrayList<ChunkCoordinate> parseNullChunks(String[] nullChunkCoords)
	{
		ArrayList<ChunkCoordinate> chunks = new ArrayList<ChunkCoordinate>();
		if(nullChunkCoords.length > 0)
		{
			for(int i = 0; i < nullChunkCoords.length; i += 2)
			{
				chunks.add(ChunkCoordinate.fromChunkCoords(Integer.parseInt(nullChunkCoords[i]),Integer.parseInt(nullChunkCoords[i + 1])));
			}
		}
		return chunks;
	}

	public static void saveChunksMapFile(LocalWorld world, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.SpawnedStructuresFileName);
		File occupiedChunksBackupFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.SpawnedStructuresBackupFileName);

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
					stringbuilder.append("," + chunkCoord.getChunkX() + "," + chunkCoord.getChunkZ());
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
	    		if(!occupiedChunksFile.exists())
	    		{
	    			occupiedChunksFile.getParentFile().mkdirs();
	    		} else {
	    			Files.move(occupiedChunksFile.toPath(), occupiedChunksBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    		}
	    		
	        	writer = new BufferedWriter(new FileWriter(occupiedChunksFile));
	            writer.write(stringbuilder.toString());
	        }
	        catch (IOException e)
	        {
				e.printStackTrace();
				throw new RuntimeException(
					"OTG encountered a critical error writing " + occupiedChunksFile.getAbsolutePath() + ", exiting. "
					+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
					+ "If your dimension's " + WorldStandardValues.SpawnedStructuresFileName + " and its backup have been corrupted, "
					+ "you can replace it with your own backup.");
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

	public static void loadChunksMapFile(LocalWorld world, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.SpawnedStructuresFileName);
		File occupiedChunksBackupFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.SpawnedStructuresBackupFileName);

		if(!occupiedChunksFile.exists() && !occupiedChunksBackupFile.exists())
		{
			return;
		}	

		if(occupiedChunksFile.exists())
		{
			StringBuilder stringbuilder = new StringBuilder();
			String[] occupiedChunksByName = {};
			String[] occupiedChunksByGroup = {};
			boolean bSuccess = false;			
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
				    	String[] allData = stringbuilder.toString().split("\\|");				    	
				    	occupiedChunksByName = allData[0].split("/");
				    	if(allData.length > 1) // Legacy files may not have occupiedChunksByGroup
				    	{
				    		occupiedChunksByGroup = allData[1].split("/");
				    	}					    
				    }
				    bSuccess = true;				    
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
			}
			
			if(bSuccess)
			{
				try
				{
					parseChunksMapFile(occupiedChunksByName, occupiedChunksByGroup, spawnedStructuresByName, spawnedStructuresByGroup);
					return;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					OTG.log(LogMarker.WARN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
				}
			}
		}
		
		if(occupiedChunksBackupFile.exists())
		{
			StringBuilder stringbuilder = new StringBuilder();
			String[] occupiedChunksByName = {};
			String[] occupiedChunksByGroup = {};
			boolean bSuccess = false;			
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
				    	String[] allData = stringbuilder.toString().split("\\|");				    	
				    	occupiedChunksByName = allData[0].split("/");
				    	if(allData.length > 1) // Legacy files may not have occupiedChunksByGroup
				    	{
				    		occupiedChunksByGroup = allData[1].split("/");
				    	}
				    }
				    bSuccess = true;				    
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if(bSuccess)
			{
				try
				{
					parseChunksMapFile(occupiedChunksByName, occupiedChunksByGroup, spawnedStructuresByName, spawnedStructuresByGroup);
					return;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}			
		}
		
		throw new RuntimeException(
			"OTG encountered a critical error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
			+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
			+ "If your dimension's " + WorldStandardValues.SpawnedStructuresFileName + " and its backup have been corrupted, you can "
			+ "replace it with a backup.");
	}
	
	private static void parseChunksMapFile(String[] occupiedChunksByName, String[] occupiedChunksByGroup, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup)
	{
		HashMap<String, ArrayList<ChunkCoordinate>> chunksByName = new HashMap<String, ArrayList<ChunkCoordinate>>();
		HashMap<String, HashMap<ChunkCoordinate, Integer>> chunksByGroup = new HashMap<String, HashMap<ChunkCoordinate, Integer>>();		
		
		String[] occupiedChunkByNameCoords = {};
		for(String entry : occupiedChunksByName)
		{
			entry = entry.replace(",,", ","); // Legacy configs could have ,, in them.
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
				
		String[] occupiedChunkByGroupCoords = {};
		for(String entry : occupiedChunksByGroup)
		{
			entry = entry.replace(",,", ","); // Legacy configs could have ,, in them.
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
		
		spawnedStructuresByName.clear();
		spawnedStructuresByName.putAll(chunksByName);
		
		spawnedStructuresByGroup.clear();
		spawnedStructuresByGroup.putAll(chunksByGroup);
	}
}
