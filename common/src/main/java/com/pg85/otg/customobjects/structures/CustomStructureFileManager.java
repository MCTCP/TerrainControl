package com.pg85.otg.customobjects.structures;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
import com.pg85.otg.customobjects.structures.bo4.smoothing.SmoothingAreaLine;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.StreamHelper;

public class CustomStructureFileManager
{	
	// Plotted chunks
	
	public static void savePlottedChunksData(LocalWorld world, Map<ChunkCoordinate, boolean[][]> populatedChunks)
	{
		int dimensionId = world.getDimensionId();

    	if(populatedChunks.size() > 0)
    	{
    		for(Entry<ChunkCoordinate, boolean[][]> chunkPerRegionEntry : populatedChunks.entrySet())
    		{
        		File occupiedChunksFile = new File(
    				world.getWorldSaveDir().getAbsolutePath() + File.separator + 
    				PluginStandardValues.PLUGIN_NAME + File.separator + 
    				(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
    				WorldStandardValues.PlottedChunksDataFolderName + File.separator +
    				chunkPerRegionEntry.getKey().getChunkX() + "_" +
    				chunkPerRegionEntry.getKey().getChunkZ() +
    				WorldStandardValues.StructureDataFileExtension
				);
        		File occupiedChunksBackupFile = new File(
    				world.getWorldSaveDir().getAbsolutePath() + File.separator + 
    				PluginStandardValues.PLUGIN_NAME + File.separator + 
    				(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
    				WorldStandardValues.PlottedChunksDataFolderName + File.separator +
    				chunkPerRegionEntry.getKey().getChunkX() + "_" +
    				chunkPerRegionEntry.getKey().getChunkZ() +    				
    				WorldStandardValues.StructureDataBackupFileExtension
				);
        		
        		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        		DataOutputStream dos = new DataOutputStream(bos);
    			
        		boolean[][] entriesByStructureName = chunkPerRegionEntry.getValue();
	    		try
	    		{
	    			int version = 1;
	    			dos.writeInt(version);
					dos.writeInt(CustomStructureCache.REGION_SIZE);

		    		for(int x = 0; x < CustomStructureCache.REGION_SIZE; x++)
		    		{
		    			boolean[] structureArr = entriesByStructureName[x];
		    			for(int z = 0; z < CustomStructureCache.REGION_SIZE; z++)
		    			{
		    				dos.writeBoolean(structureArr[z]);
		    			}
		    		}
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}

				DataOutputStream dos2 = null;
				FileOutputStream fos = null;
		        try {
		    		if(!occupiedChunksFile.exists())
		    		{
		    			occupiedChunksFile.getParentFile().mkdirs();
		    		} else {
		    			Files.move(occupiedChunksFile.toPath(), occupiedChunksBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		    		}
					byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray());
					dos.close();
					fos = new FileOutputStream(occupiedChunksFile);
					dos2 = new DataOutputStream(fos);
					dos2.write(compressedBytes, 0, compressedBytes.length);
		        }
		        catch (IOException e)
		        {
					e.printStackTrace();
					throw new RuntimeException(
						"OTG encountered a critical error writing " + occupiedChunksFile.getAbsolutePath() + ", exiting. "
						+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
						+ "If your dimension's structure data files and backups have been corrupted, you can delete them,"
						+ "at the risk of losing data for unspawned structure parts."
					);
		        } finally {
		            try {
		                if(dos != null)
		                {
		                	dos.close();
		                }
		            } catch (Exception e) { }
		            try {
		                if(dos2 != null)
		                {
		                	dos2.close();
		                }
		            } catch (Exception e) { }
		            try {
		                if(fos != null)
		                {
		                	fos.close();
		                }
		            } catch (Exception e) { }
		        }
    		}
    	}
    }
	
	public static Map<ChunkCoordinate, boolean[][]> loadPlottedChunksData(LocalWorld world)
	{
		int dimensionId = world.getDimensionId();
		
		HashMap<ChunkCoordinate, boolean[][]> output = new HashMap<ChunkCoordinate, boolean[][]>();
		
		File occupiedChunksFolder = new File(
			world.getWorldSaveDir().getAbsolutePath() + File.separator + 
			PluginStandardValues.PLUGIN_NAME + File.separator + 
			(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
			WorldStandardValues.PlottedChunksDataFolderName + File.separator
		);
		
		HashMap<File, File> saveFiles = new HashMap<File, File>();
		ArrayList<File> mainFiles = new ArrayList<File>();
		ArrayList<File> backupFiles = new ArrayList<File>();
		if(occupiedChunksFolder.exists())
		{
			for(File file : occupiedChunksFolder.listFiles())
			{
				if(
					file.getPath().endsWith(WorldStandardValues.StructureDataFileExtension) && 
					file.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_").length == 2 &&
					MathHelper.tryParseInt(file.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_")[0]) &&
					MathHelper.tryParseInt(file.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_")[1])
				) {
					mainFiles.add(file);
				}
				else if(
					file.getPath().endsWith(WorldStandardValues.StructureDataBackupFileExtension) &&
					file.getName().replace(WorldStandardValues.BackupFileSuffix, "").split("_").length == 2 &&
					MathHelper.tryParseInt(file.getName().replace(WorldStandardValues.BackupFileSuffix, "").split("_")[0]) &&
					MathHelper.tryParseInt(file.getName().replace(WorldStandardValues.BackupFileSuffix, "").split("_")[1])
				)
				{
					backupFiles.add(file);
				}
			}

			for(File file : mainFiles)
			{
				boolean bFound = false;
				for(File backupFile : backupFiles)
				{
					if(file.getPath().replace(WorldStandardValues.StructureDataFileExtension, "").equals(backupFile.getPath().replace(WorldStandardValues.StructureDataBackupFileExtension, "")))
					{
						saveFiles.put(file, backupFile);
						bFound = true;
						break;
					}
				}
				if(!bFound)
				{
					saveFiles.put(file, null);
				}
			}
		}
				
		for(Entry<File, File> saveFile : saveFiles.entrySet())
		{
			boolean bSuccess = false;
			File occupiedChunksFile = saveFile.getKey();
			File occupiedChunksBackupFile = saveFile.getValue();
				
		    if(
	    		(occupiedChunksFile == null || !occupiedChunksFile.exists()) &&
				(occupiedChunksBackupFile == null || !occupiedChunksBackupFile.exists())
    		)
		    {
		    	continue;
		    }

		    if(occupiedChunksFile != null && occupiedChunksFile.exists())
		    {			
		    	FileInputStream fis = null;
		    	boolean[][] result = null;
		    	ChunkCoordinate regionCoord = null;
				try {
			    	String[] chunkCoords = occupiedChunksFile.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_");
			    	int regionX = Integer.parseInt(chunkCoords[0]);
			    	int regionZ = Integer.parseInt(chunkCoords[1]);
			    	regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);
			    	
			    	fis = new FileInputStream(occupiedChunksFile);			
		    		ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
		    		buffer = ByteBuffer.wrap(decompressedBytes);
		    		result = parsePlottedChunksFileFromStream(buffer, world);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					OTG.log(LogMarker.WARN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
				} finally {
					if(fis != null)
					{
						try {
							fis.getChannel().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				if(result != null)
				{
					bSuccess = true;
					output.put(regionCoord, result);
				}
		    }
		    
		    if(!bSuccess && occupiedChunksBackupFile != null && occupiedChunksBackupFile.exists())
		    {			
		    	FileInputStream fis = null;
		    	boolean[][] result = null;
		    	ChunkCoordinate regionCoord = null;
				try {
			    	String[] chunkCoords = occupiedChunksFile.getName().replace(WorldStandardValues.StructureDataBackupFileExtension, "").split("_");
			    	int regionX = Integer.parseInt(chunkCoords[0]);
			    	int regionZ = Integer.parseInt(chunkCoords[1]);
			    	regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);
			    	
			    	fis = new FileInputStream(occupiedChunksBackupFile);			
		    		ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
		    		buffer = ByteBuffer.wrap(decompressedBytes);
		    		result = parsePlottedChunksFileFromStream(buffer, world);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				} finally {
					if(fis != null)
					{
						try {
							fis.getChannel().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				if(result != null)
				{
					bSuccess = true;
					output.put(regionCoord, result);
				}				
		    }
		    
		    if(!bSuccess)
		    {
				throw new RuntimeException(
					"OTG encountered a critical error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
					+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
					+ "If your dimension's structure data files and backups have been corrupted, you can delete them,"
					+ "at the risk of losing data for unspawned structure parts."
				);
		    }
		}
		
		return output.size() > 0 ? output : null;
	}
	
	private static boolean[][] parsePlottedChunksFileFromStream(ByteBuffer buffer, LocalWorld world) throws IOException
	{
		int version = buffer.getInt();		
		int regionSize = buffer.getInt();
		boolean[][] chunksMatrix = new boolean[regionSize][regionSize];
		
		for(int x = 0; x < regionSize; x++)
		{		
			for(int z = 0; z < regionSize; z++)
			{
				chunksMatrix[x][z] = buffer.get() != 0;
			}
		}
		
		return chunksMatrix;
	}
	
	
	// Structure cache

	// TODO: Since we're using regions now, can use byte/short for internal coords instead of int.
	static void saveStructureData(Map<ChunkCoordinate, CustomStructure> structures, LocalWorld world)
	{
		int dimensionId = world.getDimensionId();

		// Sort structures start bo4 name, then split them into regions
		
		// For the start of each structure save its ObjectsToSpawn and SmoothingAreasToSpawn
		// save chunk coords for branches that have already spawned (not in ObjectsToSpawn/SmoothingAreasToSpawn)
    	if(structures.size() > 0)
    	{
    		HashMap<ChunkCoordinate, HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>>> structuresPerRegion = new HashMap<ChunkCoordinate, HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>>>();
    		for(Entry<ChunkCoordinate, CustomStructure> entry : structures.entrySet())
    		{
    			ChunkCoordinate regionCoord = ChunkCoordinate.fromChunkCoords(
					MathHelper.floor((double)entry.getKey().getChunkX() / (double)CustomStructureCache.REGION_SIZE), 
					MathHelper.floor((double)entry.getKey().getChunkZ() / (double)CustomStructureCache.REGION_SIZE)
				);
    		
        		HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>> entriesByStructureName = structuresPerRegion.get(regionCoord);
        		if(entriesByStructureName == null)
        		{
    				entriesByStructureName = new HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>>();
    				structuresPerRegion.put(regionCoord, entriesByStructureName);
    			}
    			
    			// BO3's that add spawners/particles/moddata are saved as null structures
    			String startBo3Name = "NULL"; 
    			if(entry.getValue().start != null)
    			{
    				startBo3Name = entry.getValue().start.bo3Name;
    			}

				HashMap<CustomStructure, ArrayList<ChunkCoordinate>> entryByStructureName = entriesByStructureName.get(startBo3Name);
				ArrayList<ChunkCoordinate> structureChunks = new ArrayList<ChunkCoordinate>();
    			if(entryByStructureName == null)
    			{
    				entryByStructureName = new HashMap<CustomStructure, ArrayList<ChunkCoordinate>>();
    				entryByStructureName.put(entry.getValue(), structureChunks);
    				entriesByStructureName.put(startBo3Name, entryByStructureName);
    			} else {
    				structureChunks = entryByStructureName.get(entry.getValue());
    				if(structureChunks == null)
    				{
    					structureChunks = new ArrayList<ChunkCoordinate>();
    					entryByStructureName.put(entry.getValue(), structureChunks);
    				}
    			}
    			structureChunks.add(entry.getKey());
    		}
    		
    		for(Entry<ChunkCoordinate, HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>>> structuresPerRegionEntry : structuresPerRegion.entrySet())
    		{
        		File occupiedChunksFile = new File(
    				world.getWorldSaveDir().getAbsolutePath() + File.separator + 
    				PluginStandardValues.PLUGIN_NAME + File.separator + 
    				(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
    				WorldStandardValues.StructureDataFolderName + File.separator +
    				structuresPerRegionEntry.getKey().getChunkX() + "_" +
    				structuresPerRegionEntry.getKey().getChunkZ() +
    				WorldStandardValues.StructureDataFileExtension
				);
        		File occupiedChunksBackupFile = new File(
    				world.getWorldSaveDir().getAbsolutePath() + File.separator + 
    				PluginStandardValues.PLUGIN_NAME + File.separator + 
    				(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
    				WorldStandardValues.StructureDataFolderName + File.separator +
    				structuresPerRegionEntry.getKey().getChunkX() + "_" +
    				structuresPerRegionEntry.getKey().getChunkZ() +    				
    				WorldStandardValues.StructureDataBackupFileExtension
				);
    			
        		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        		DataOutputStream dos = new DataOutputStream(bos);
        		
    			HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>> entriesByStructureName = structuresPerRegionEntry.getValue();
	    		try
	    		{
	    			int version = 1;
	    			dos.writeInt(version);
					dos.writeInt(entriesByStructureName.entrySet().size());
					for(Entry<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>> entry : entriesByStructureName.entrySet())
					{
						StreamHelper.writeStringToStream(dos, entry.getKey());
						dos.writeInt(entry.getValue().entrySet().size());
						// Structures have been de-duplicated, should be only one entry per structure start
						for(Entry<CustomStructure, ArrayList<ChunkCoordinate>> entry1 : entry.getValue().entrySet())
						{
							CustomStructure structure = entry1.getKey();
	
							// Write structure start data (if any)
							// If name is "NULL", we'll know not to look for these when reading.
							if(entry1.getKey().start != null)
							{
		    					dos.writeInt(entry1.getKey().start.rotation.getRotationId());
		    					dos.writeInt(entry1.getKey().start.getX());
		    					dos.writeInt(entry1.getKey().start.getY());
		    					dos.writeInt(entry1.getKey().start.getZ());
							}
	
							// Write all chunks used for structure
							int a = entry1.getValue().size();
							int b = 0;
	    					dos.writeInt(entry1.getValue().size());
	    					for(ChunkCoordinate chunkCoord : entry1.getValue())
	    					{
	    						b++;
								// TODO: Use byte/short for internal coords
					    		dos.writeInt(chunkCoord.getChunkX());
					    		dos.writeInt(chunkCoord.getChunkZ());
	    					}
	
							if(
								structure instanceof BO4CustomStructure &&
								((BO4CustomStructure)structure).objectsToSpawn.entrySet().size() > 0
							)
							{
								dos.writeBoolean(true);
								dos.writeInt(((BO4CustomStructure)structure).objectsToSpawn.entrySet().size());
								for(Entry<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectToSpawn : ((BO4CustomStructure)structure).objectsToSpawn.entrySet())
								{
									ChunkCoordinate key = objectToSpawn.getKey();
									dos.writeInt(key.getChunkX());
									dos.writeInt(key.getChunkZ());
	
									Stack<BO4CustomStructureCoordinate> coords = objectToSpawn.getValue();
									dos.writeInt(coords.size());
									for(CustomStructureCoordinate coord : coords)
									{
										StreamHelper.writeStringToStream(dos, coord.bo3Name); 
										dos.writeInt(coord.rotation.getRotationId());
										dos.writeInt(coord.getX());
										dos.writeInt(coord.getY());
										dos.writeInt(coord.getZ());
									}
								}
							} else {
								dos.writeBoolean(false);
							}
			
							if(
								structure instanceof BO4CustomStructure && 
								((BO4CustomStructure)structure).smoothingAreaManager.smoothingAreasToSpawn.entrySet().size() > 0 
							)
							{								
								ArrayList<SmoothingAreaLine> coords2;
								dos.writeBoolean(true);
								dos.writeInt(((BO4CustomStructure)structure).smoothingAreaManager.smoothingAreasToSpawn.entrySet().size());
								for(Entry<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreaToSpawn : ((BO4CustomStructure)structure).smoothingAreaManager.smoothingAreasToSpawn.entrySet())
								{
									ChunkCoordinate key = smoothingAreaToSpawn.getKey();
									dos.writeInt(key.getChunkX());
									dos.writeInt(key.getChunkZ());
			
									coords2 = smoothingAreaToSpawn.getValue();
									dos.writeInt(coords2.size());
									for(SmoothingAreaLine coord : coords2)
									{
										// TODO: Should only need origin and destination?
										dos.writeInt(coord.beginPointX);
										dos.writeInt(coord.beginPointY);
										dos.writeInt(coord.beginPointZ);
			
										dos.writeInt(coord.endPointX);
										dos.writeInt(coord.endPointY);
										dos.writeInt(coord.endPointZ);
			
										dos.writeInt(coord.originPointX);
										dos.writeInt(coord.originPointY);
										dos.writeInt(coord.originPointZ);
			
										dos.writeInt(coord.finalDestinationPointX);
										dos.writeInt(coord.finalDestinationPointY);
										dos.writeInt(coord.finalDestinationPointZ);										
									}
								}
							} else {
								dos.writeBoolean(false);
							}
	
							// Save moddata/particles/spawner data
							// Bo3 objects/structures have start == null
							// For Bo4's, only save for the start bo4, data will be reconstituted when the file is loaded.
							
							if(structure.modDataManager.modData.size() > 0)
							{
								dos.writeBoolean(true);
								dos.writeInt(structure.modDataManager.modData.size());
								for(ModDataFunction<?> modData : structure.modDataManager.modData)
								{
									dos.writeInt(modData.x);
									dos.writeInt(modData.y);
									dos.writeInt(modData.z);
									StreamHelper.writeStringToStream(dos, modData.modId.replace(":", "&#58;").replace(" ", "&nbsp;"));
									StreamHelper.writeStringToStream(dos, modData.modData.replace(":", "&#58;").replace(" ", "&nbsp;"));
								}
							} else {
								dos.writeBoolean(false);
							}
	
							if(structure.spawnerManager.spawnerData.size() > 0)
							{
								dos.writeBoolean(true);
								dos.writeInt(structure.spawnerManager.spawnerData.size());
								for(SpawnerFunction<?> spawnerData : structure.spawnerManager.spawnerData)
								{
									dos.writeInt(spawnerData.x); 
									dos.writeInt(spawnerData.y); 
									dos.writeInt(spawnerData.z);
									StreamHelper.writeStringToStream(dos, spawnerData.mobName.replace(":", "&#58;").replace(" ", "&nbsp;")); 
									StreamHelper.writeStringToStream(dos, spawnerData.originalnbtFileName.replace(":", "&#58;").replace(" ", "&nbsp;")); 
									StreamHelper.writeStringToStream(dos, spawnerData.nbtFileName.replace(":", "&#58;").replace(" ", "&nbsp;"));
									dos.writeInt(spawnerData.groupSize);
									dos.writeInt(spawnerData.interval);
									dos.writeInt(spawnerData.spawnChance); 
									dos.writeInt(spawnerData.maxCount); 
									dos.writeInt(spawnerData.despawnTime); 
									dos.writeDouble(spawnerData.velocityX); 
									dos.writeDouble(spawnerData.velocityY); 
									dos.writeDouble(spawnerData.velocityZ); 
									dos.writeBoolean(spawnerData.velocityXSet); 
									dos.writeBoolean(spawnerData.velocityYSet); 
									dos.writeBoolean(spawnerData.velocityZSet); 
									dos.writeFloat(spawnerData.yaw); 
									dos.writeFloat(spawnerData.pitch);
								}
							} else {
								dos.writeBoolean(false);
							}
	
							if(structure.particlesManager.particleData.size() > 0)
							{
								dos.writeBoolean(true);
								dos.writeInt(structure.particlesManager.particleData.size());
								for(ParticleFunction<?> particleData : structure.particlesManager.particleData)
								{
									dos.writeInt(particleData.x); 
									dos.writeInt(particleData.y);
									dos.writeInt(particleData.z); 
									StreamHelper.writeStringToStream(dos, particleData.particleName.replace(":", "&#58;").replace(" ", "&nbsp;")); 
									dos.writeDouble(particleData.interval); 
									dos.writeDouble(particleData.velocityX); 
									dos.writeDouble(particleData.velocityY); 
									dos.writeDouble(particleData.velocityZ); 
									dos.writeBoolean(particleData.velocityXSet); 
									dos.writeBoolean(particleData.velocityYSet); 
									dos.writeBoolean(particleData.velocityZSet);
								}
							} else {
								dos.writeBoolean(false);
							}					
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}
	    			
				DataOutputStream dos2 = null;
				FileOutputStream fos = null;
		        try {
		    		if(!occupiedChunksFile.exists())
		    		{
		    			occupiedChunksFile.getParentFile().mkdirs();
		    		} else {
		    			Files.move(occupiedChunksFile.toPath(), occupiedChunksBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		    		}
					byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray());
					dos.close();
					fos = new FileOutputStream(occupiedChunksFile);
					dos2 = new DataOutputStream(fos);
					dos2.write(compressedBytes, 0, compressedBytes.length);
		        }
		        catch (IOException e)
		        {
					e.printStackTrace();
					throw new RuntimeException(
						"OTG encountered a critical error writing " + occupiedChunksFile.getAbsolutePath() + ", exiting. "
						+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
						+ "If your dimension's structure data files and backups have been corrupted, you can delete them,"
						+ "at the risk of losing data for unspawned structure parts."
					);
		        } finally {
		            try {
		                if(dos != null)
		                {
		                	dos.close();
		                }
		            } catch (Exception e) { }
		            try {
		                if(dos2 != null)
		                {
		                	dos2.close();
		                }
		            } catch (Exception e) { }
		            try {
		                if(fos != null)
		                {
		                	fos.close();
		                }
		            } catch (Exception e) { }
		        }
    		}
    	}
    }

	public static HashMap<CustomStructure, ArrayList<ChunkCoordinate>> loadStructureData(LocalWorld world)
	{		
		int dimensionId = world.getDimensionId();
		
		HashMap<CustomStructure, ArrayList<ChunkCoordinate>> output = new HashMap<CustomStructure, ArrayList<ChunkCoordinate>>();
		
		File occupiedChunksFolder = new File(
			world.getWorldSaveDir().getAbsolutePath() + File.separator + 
			PluginStandardValues.PLUGIN_NAME + File.separator + 
			(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
			WorldStandardValues.StructureDataFolderName + File.separator
		);
		
		HashMap<File, File> saveFiles = new HashMap<File, File>();
		ArrayList<File> mainFiles = new ArrayList<File>();
		ArrayList<File> backupFiles = new ArrayList<File>();
		if(occupiedChunksFolder.exists())
		{
			for(File file : occupiedChunksFolder.listFiles())
			{
				if(
					file.getPath().endsWith(WorldStandardValues.StructureDataFileExtension) && 
					file.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_").length == 2 &&
					MathHelper.tryParseInt(file.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_")[0]) &&
					MathHelper.tryParseInt(file.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_")[1])
				) {
					mainFiles.add(file);
				}
				else if(
					file.getPath().endsWith(WorldStandardValues.StructureDataBackupFileExtension) &&
					file.getName().replace(WorldStandardValues.BackupFileSuffix, "").split("_").length == 2 &&
					MathHelper.tryParseInt(file.getName().replace(WorldStandardValues.BackupFileSuffix, "").split("_")[0]) &&
					MathHelper.tryParseInt(file.getName().replace(WorldStandardValues.BackupFileSuffix, "").split("_")[1])
				)
				{
					backupFiles.add(file);
				}
			}
			for(File file : mainFiles)
			{
				boolean bFound = false;
				for(File backupFile : backupFiles)
				{
					if(file.getPath().replace(WorldStandardValues.StructureDataFileExtension, "").equals(backupFile.getPath().replace(WorldStandardValues.StructureDataBackupFileExtension, "")))
					{
						saveFiles.put(file, backupFile);
						bFound = true;
						break;
					}
				}
				if(!bFound)
				{
					saveFiles.put(file, null);
				}
			}
		}
				
		for(Entry<File, File> saveFile : saveFiles.entrySet())
		{
			boolean bSuccess = false;
			File occupiedChunksFile = saveFile.getKey();
			File occupiedChunksBackupFile = saveFile.getValue();
				
		    if(
	    		(occupiedChunksFile == null || !occupiedChunksFile.exists()) &&
				(occupiedChunksBackupFile == null || !occupiedChunksBackupFile.exists())
    		)
		    {
		    	continue;
		    }

		    if(occupiedChunksFile != null && occupiedChunksFile.exists())
		    {			
		    	FileInputStream fis = null;
		    	HashMap<CustomStructure, ArrayList<ChunkCoordinate>> result = null;
				try {			
			    	fis = new FileInputStream(occupiedChunksFile);			
		    		ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
		    		buffer = ByteBuffer.wrap(decompressedBytes);
		    		result = parseStructuresFileFromStream(buffer, world);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					OTG.log(LogMarker.WARN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
				} finally {
					if(fis != null)
					{
						try {
							fis.getChannel().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				if(result != null)
				{
					bSuccess = true;
					for(Entry<CustomStructure, ArrayList<ChunkCoordinate>> entry : result.entrySet())
					{
						ArrayList<ChunkCoordinate> coords = output.get(entry.getKey());
						if(coords != null)
						{
							coords.addAll(entry.getValue());
						} else {
							output.put(entry.getKey(), entry.getValue());
						}
					}
				}
		    }
		    
		    if(!bSuccess && occupiedChunksBackupFile != null && occupiedChunksBackupFile.exists())
		    {			
		    	FileInputStream fis = null;
		    	HashMap<CustomStructure, ArrayList<ChunkCoordinate>> result = null;
				try {			
			    	fis = new FileInputStream(occupiedChunksBackupFile);			
		    		ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
		    		buffer = ByteBuffer.wrap(decompressedBytes);
		    		result = parseStructuresFileFromStream(buffer, world);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				} finally {
					if(fis != null)
					{
						try {
							fis.getChannel().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				if(result != null)
				{
					bSuccess = true;
					for(Entry<CustomStructure, ArrayList<ChunkCoordinate>> entry : result.entrySet())
					{
						ArrayList<ChunkCoordinate> coords = output.get(entry.getKey());
						if(coords != null)
						{
							coords.addAll(entry.getValue());
						} else {
							output.put(entry.getKey(), entry.getValue());
						}
					}
				}
		    }
		    if(!bSuccess)
		    {
				throw new RuntimeException(
					"OTG encountered a critical error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
					+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
					+ "If your dimension's structure data files and backups have been corrupted, you can delete them,"
					+ "at the risk of losing data for unspawned structure parts."
				);
		    }
		}
		
		return output.size() > 0 ? output : null;
	}

	// TODO: Since we're using regions now, can use byte/short for internal coords instead of int.
	private static HashMap<CustomStructure, ArrayList<ChunkCoordinate>> parseStructuresFileFromStream(ByteBuffer buffer, LocalWorld world) throws IOException
	{
		int version = buffer.getInt();		
		HashMap<CustomStructure, ArrayList<ChunkCoordinate>> structuresFile = new HashMap<CustomStructure, ArrayList<ChunkCoordinate>>();
		int structureNamesSize = buffer.getInt();
		for(int i = 0; i < structureNamesSize; i++)
		{			
			String structureName = StreamHelper.readStringFromBuffer(buffer);
			Rotation startRotationId;
			int startX;
			int startY;
			int startZ;
			int structuresSize = buffer.getInt();
			for(int j = 0; j < structuresSize; j++)
			{
				CustomStructureCoordinate structureStart = null;

				// Check if this is a structure start
				if(!structureName.equals("NULL"))
				{
					startRotationId = Rotation.getRotation(buffer.getInt());
					startX = buffer.getInt();
					startY = buffer.getInt();
					startZ = buffer.getInt();
					
			    	if(world.isBo4Enabled())
			    	{
			    		structureStart = new BO4CustomStructureCoordinate(world, null, structureName, startRotationId, startX, (short)startY, startZ, 0, false, false, null);
			    	} else {
			    		structureStart = new BO3CustomStructureCoordinate(world, null, structureName, startRotationId, startX, (short)startY, startZ);
			    	}					
				}

				// Get all chunks used for structure
				int chunksSize = buffer.getInt();
				ArrayList<ChunkCoordinate> chunkCoords = new ArrayList<ChunkCoordinate>(); 
				for(int k = 0; k < chunksSize; k++)
				{
					chunkCoords.add(ChunkCoordinate.fromChunkCoords(buffer.getInt(), buffer.getInt()));
				}

				Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn = new HashMap<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>>();	
				if(buffer.get() != 0)
				{
					int objectsToSpawnSize = buffer.getInt();
					for(int l = 0; l < objectsToSpawnSize; l++)
					{
						ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(buffer.getInt(), buffer.getInt());
						Stack<BO4CustomStructureCoordinate> coords = new Stack<BO4CustomStructureCoordinate>();								
						int coordsSize = buffer.getInt();
						for(int m = 0; m < coordsSize; m++)
						{
							String bo3Name = StreamHelper.readStringFromBuffer(buffer); 
							Rotation coordRotation = Rotation.getRotation(buffer.getInt());
							int coordX = buffer.getInt();
							int coordY = buffer.getInt();
							int coordZ = buffer.getInt();
					    	coords.add(new BO4CustomStructureCoordinate(world, null, bo3Name, coordRotation, coordX, (short)coordY, coordZ, 0, false, false, null));
						}
						objectsToSpawn.put(chunkCoord, coords);
					}
				}

			    Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
				if(buffer.get() != 0)
				{
					int smoothingAreasToSpawnSize = buffer.getInt();
					for(int l = 0; l < smoothingAreasToSpawnSize; l++)
					{
						ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(buffer.getInt(), buffer.getInt());
						int coordsSize = buffer.getInt();
						ArrayList<SmoothingAreaLine> smoothingAreaLines = new ArrayList<SmoothingAreaLine>();
						for(int m = 0; m < coordsSize; m++)
						{
							SmoothingAreaLine smoothingAreaLine;
							
							int beginPointX = buffer.getInt();
							int beginPointY = buffer.getInt();
							int beginPointZ = buffer.getInt();

							int endPointX = buffer.getInt();
							int endPointY = buffer.getInt();
							int endPointZ = buffer.getInt();

							int originPointX = buffer.getInt();
							int originPointY = buffer.getInt();
							int originPointZ = buffer.getInt();

							int finalDestinationPointX = buffer.getInt();
							int finalDestinationPointY = buffer.getInt();
							int finalDestinationPointZ = buffer.getInt();
							
							smoothingAreaLine = new SmoothingAreaLine(beginPointX, (short)beginPointY, beginPointZ, endPointX, (short)endPointY, endPointZ, originPointX, (short)originPointY, originPointZ, finalDestinationPointX, (short)finalDestinationPointY, finalDestinationPointZ);
							smoothingAreaLines.add(smoothingAreaLine);
						}
						smoothingAreasToSpawn.put(chunkCoord, smoothingAreaLines);
					}
				}
				
				// Save moddata/particles/spawner data
				// Bo3 objects/structures have start == null
				// For Bo4's, only save for the start bo4, data will be reconstituted when the file is loaded.
				
			    HashSet<ModDataFunction<?>> modData = new HashSet<ModDataFunction<?>>();			
				if(buffer.get() != 0)
				{
					int modDataSize = buffer.getInt();
					for(int l = 0; l < modDataSize; l++)						
					{
			    		ModDataFunction<?> modDataFunction;
				    	if(world.isBo4Enabled())
				    	{
				    		modDataFunction = new BO4ModDataFunction();
				    	} else {
				    		modDataFunction = new BO3ModDataFunction();
				    	}
				    	
				    	modDataFunction.x = buffer.getInt();
				    	modDataFunction.y = buffer.getInt();
				    	modDataFunction.z = buffer.getInt();
				    	modDataFunction.modId = StreamHelper.readStringFromBuffer(buffer);
				    	modDataFunction.modData = StreamHelper.readStringFromBuffer(buffer);
						modData.add(modDataFunction);
					}
				}

			    HashSet<SpawnerFunction<?>> spawnerData = new HashSet<SpawnerFunction<?>>();				
				if(buffer.get() != 0)
				{
					int spawnerDataSize = buffer.getInt();
					for(int l = 0; l < spawnerDataSize; l++)
					{
				    	SpawnerFunction<?> spawnerFunction;
				    	if(world.isBo4Enabled())
				    	{
				    		spawnerFunction = new BO4SpawnerFunction();
				    	} else {
				    		spawnerFunction = new BO3SpawnerFunction();
				    	}
						
				    	spawnerFunction.x = buffer.getInt();
				    	spawnerFunction.y = buffer.getInt();
				    	spawnerFunction.z = buffer.getInt();
				    	spawnerFunction.mobName = StreamHelper.readStringFromBuffer(buffer);
				    	spawnerFunction.originalnbtFileName = StreamHelper.readStringFromBuffer(buffer);
				    	spawnerFunction.nbtFileName = StreamHelper.readStringFromBuffer(buffer);
				    	spawnerFunction.groupSize = buffer.getInt();
				    	spawnerFunction.interval = buffer.getInt();
				    	spawnerFunction.spawnChance = buffer.getInt();
				    	spawnerFunction.maxCount = buffer.getInt();
				    	spawnerFunction.despawnTime = buffer.getInt(); 
				    	spawnerFunction.velocityX = buffer.getDouble();
				    	spawnerFunction.velocityY = buffer.getDouble(); 
				    	spawnerFunction.velocityZ = buffer.getDouble(); 
				    	spawnerFunction.velocityXSet = buffer.get() != 0; 
				    	spawnerFunction.velocityYSet = buffer.get() != 0; 
				    	spawnerFunction.velocityZSet = buffer.get() != 0;
				    	spawnerFunction.yaw = buffer.getFloat();
				    	spawnerFunction.pitch = buffer.getFloat();						
						spawnerData.add(spawnerFunction);
					}
				}

			    HashSet<ParticleFunction<?>> particleData = new HashSet<ParticleFunction<?>>();				
				if(buffer.get() != 0)
				{
					int particleDataSize = buffer.getInt();
					for(int l = 0; l < particleDataSize; l++)
					{
				    	ParticleFunction<?> particleFunction;
				    	if(world.isBo4Enabled())
				    	{
				    		particleFunction = new BO4ParticleFunction();
				    	} else {
				    		particleFunction = new BO3ParticleFunction();
				    	}
						
				    	particleFunction.x = buffer.getInt();
				    	particleFunction.y = buffer.getInt();
				    	particleFunction.z = buffer.getInt(); 
						particleFunction.particleName = StreamHelper.readStringFromBuffer(buffer);
				    	particleFunction.interval = buffer.getDouble(); 
				    	particleFunction.velocityX = buffer.getDouble(); 
				    	particleFunction.velocityY = buffer.getDouble();
				    	particleFunction.velocityZ = buffer.getDouble();
				    	particleFunction.velocityXSet = buffer.get() != 0;
				    	particleFunction.velocityYSet = buffer.get() != 0; 
				    	particleFunction.velocityZSet = buffer.get() != 0;
						
						particleData.add(particleFunction);
					}
				}
				
			    CustomStructure structure;
			    if(world.isBo4Enabled())
			    {
			    	structure = new BO4CustomStructure(world, (BO4CustomStructureCoordinate)structureStart, objectsToSpawn, smoothingAreasToSpawn, 0);
				    ((BO4CustomStructure)structure).startChunkBlockChecksDone = true;
			    } else {
			    	structure = new BO3CustomStructure((BO3CustomStructureCoordinate)structureStart);
			    }
			    structure.modDataManager.modData = modData;
			    structure.spawnerManager.spawnerData = spawnerData;
			    structure.particlesManager.particleData = particleData;
			    
		    	structuresFile.put(structure, chunkCoords);
			}
		}
		
		return structuresFile;
	}

	public static void saveChunksMapFile(LocalWorld world, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup)
	{
		int dimensionId = world.getDimensionId();
		File occupiedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.SpawnedStructuresFileName);
		File occupiedChunksBackupFile = new File(world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.SpawnedStructuresBackupFileName);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		if(spawnedStructuresByName.size() > 0)
		{
    		try {
    			int version = 1;
    			dos.writeInt(version);

    			dos.writeInt(spawnedStructuresByName.entrySet().size());
				for(Map.Entry<String, ArrayList<ChunkCoordinate>> entry : spawnedStructuresByName.entrySet())
				{
					StreamHelper.writeStringToStream(dos,  entry.getKey());
					dos.writeInt(entry.getValue().size());
					for(ChunkCoordinate chunkCoord : entry.getValue())
					{
						dos.writeInt(chunkCoord.getChunkX());
						dos.writeInt(chunkCoord.getChunkZ());
					}
				}
				
				dos.writeInt(spawnedStructuresByGroup.entrySet().size());
				for(Entry<String, HashMap<ChunkCoordinate, Integer>> entry : spawnedStructuresByGroup.entrySet())
				{
					StreamHelper.writeStringToStream(dos,  entry.getKey());
					dos.writeInt(entry.getValue().entrySet().size());
					for(Entry<ChunkCoordinate, Integer> valueEntry : entry.getValue().entrySet())
					{					
						dos.writeInt(valueEntry.getKey().getChunkX());
						dos.writeInt(valueEntry.getKey().getChunkZ());
						dos.writeInt(valueEntry.getValue().intValue());
					}
				}
    		} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}			
			
			DataOutputStream dos2 = null;
			FileOutputStream fos = null;
	        try
	        {				
	    		if(!occupiedChunksFile.exists())
	    		{
	    			occupiedChunksFile.getParentFile().mkdirs();
	    		} else {
	    			Files.move(occupiedChunksFile.toPath(), occupiedChunksBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    		}
				byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray());
				fos = new FileOutputStream(occupiedChunksFile);
				dos2 = new DataOutputStream(fos);
				dos2.write(compressedBytes, 0, compressedBytes.length);
	        } catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(
					"OTG encountered a critical error writing " + occupiedChunksFile.getAbsolutePath() + ", exiting. "
					+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
					+ "If your dimension's " + WorldStandardValues.SpawnedStructuresFileName + " and its backup have been corrupted, "
					+ "you can replace it with your own backup.");
	        } finally {
	            try {
	                if(dos != null)
	                {
	                	dos.close();
	                }
	            } catch (Exception e) { }	        	
	            try {
	                if(dos2 != null)
	                {
	                	dos2.close();
	                }
	            } catch (Exception e) { }
	            try {
	                if(fos != null)
	                {
	                	fos.close();
	                }
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
	    	FileInputStream fis = null;
			try {			
		    	fis = new FileInputStream(occupiedChunksFile);			
	    		ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
				
				byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
				buffer.get(compressedBytes);
				byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
	    		buffer = ByteBuffer.wrap(decompressedBytes);
	    		parseChunksMapFileFromStream(buffer, world, spawnedStructuresByName, spawnedStructuresByGroup);
	    		return;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
			} finally {
				if(fis != null)
				{
					try {
						fis.getChannel().close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	    }
	    
	    if(occupiedChunksBackupFile.exists())
	    {			
	    	FileInputStream fis = null;
			try {			
		    	fis = new FileInputStream(occupiedChunksBackupFile);			
	    		ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
				
				byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
				buffer.get(compressedBytes);
				byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
	    		buffer = ByteBuffer.wrap(decompressedBytes);
	    		parseChunksMapFileFromStream(buffer, world, spawnedStructuresByName, spawnedStructuresByGroup);
	    		return;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			} finally {
				if(fis != null)
				{
					try {
						fis.getChannel().close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	    }
		
		throw new RuntimeException(
			"OTG encountered a critical error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
			+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
			+ "If your dimension's " + WorldStandardValues.SpawnedStructuresFileName + " and its backup have been corrupted, you can "
			+ "replace it with a backup.");
	}

	private static void parseChunksMapFileFromStream(ByteBuffer buffer, LocalWorld world, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup) throws IOException
	{
		HashMap<String, ArrayList<ChunkCoordinate>> chunksByName = new HashMap<String, ArrayList<ChunkCoordinate>>();
		HashMap<String, HashMap<ChunkCoordinate, Integer>> chunksByGroup = new HashMap<String, HashMap<ChunkCoordinate, Integer>>();		

		int version = buffer.getInt();
		
		int spawnedStructuresByNameSize = buffer.getInt();
		for(int i = 0; i < spawnedStructuresByNameSize; i++)
		{
			String name = StreamHelper.readStringFromBuffer(buffer);
			int coordsSize = buffer.getInt();
			ArrayList<ChunkCoordinate> coords = new ArrayList<ChunkCoordinate>();
			for(int j = 0; j < coordsSize; j++)
			{				
				coords.add(ChunkCoordinate.fromChunkCoords(buffer.getInt(), buffer.getInt()));
			}
			chunksByName.put(name, coords);
		}
		
		int spawnedStructuresByGroupSize = buffer.getInt();
		for(int k = 0; k < spawnedStructuresByGroupSize; k++)
		{
			String name = StreamHelper.readStringFromBuffer(buffer);
			HashMap<ChunkCoordinate, Integer> coords = new HashMap<ChunkCoordinate, Integer>();
			int coordsSize = buffer.getInt();
			for(int l = 0; l < coordsSize; l++)
			{
				coords.put(ChunkCoordinate.fromChunkCoords(buffer.getInt(), buffer.getInt()), Integer.valueOf(buffer.getInt()));
			}
			chunksByGroup.put(name, coords);
		}
		
		spawnedStructuresByName.clear();
		spawnedStructuresByName.putAll(chunksByName);
		
		spawnedStructuresByGroup.clear();
		spawnedStructuresByGroup.putAll(chunksByGroup);
	}
}
