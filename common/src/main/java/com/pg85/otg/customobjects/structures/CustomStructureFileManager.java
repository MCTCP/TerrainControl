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
import com.pg85.otg.customobjects.structures.bo4.CustomStructurePlaceHolder;
import com.pg85.otg.customobjects.structures.bo4.smoothing.SmoothingAreaLine;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.StreamHelper;

public class CustomStructureFileManager
{
	// Plotted chunks
	
	public static void savePlottedChunksData(LocalWorld world, Map<ChunkCoordinate, PlottedChunksRegion> populatedChunks)
	{
		int dimensionId = world.getDimensionId();

		int regionsSaved = 0;
    	if(populatedChunks.size() > 0)
    	{
    		for(Entry<ChunkCoordinate, PlottedChunksRegion> chunkPerRegionEntry : populatedChunks.entrySet())
    		{
    			if(!chunkPerRegionEntry.getValue().requiresSave())
    			{
    				continue;
    			}
				chunkPerRegionEntry.getValue().markSaved();
    			regionsSaved++;
    			
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
    			
        		boolean[][] entriesByStructureName = chunkPerRegionEntry.getValue().getArray();
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
					OTG.log(LogMarker.INFO, "OTG encountered an error writing " + occupiedChunksFile.getAbsolutePath() + ", skipping.");
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
    	
    	OTG.log(LogMarker.INFO, regionsSaved + " plotted chunk regions saved.");
    }
	
	public static Map<ChunkCoordinate, PlottedChunksRegion> loadPlottedChunksData(LocalWorld world)
	{
		int dimensionId = world.getDimensionId();
		
		HashMap<ChunkCoordinate, PlottedChunksRegion> output = new HashMap<ChunkCoordinate, PlottedChunksRegion>();
		
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

	    	ChunkCoordinate regionCoord = null;
		    
		    if(occupiedChunksFile != null && occupiedChunksFile.exists())
		    {			
		    	FileInputStream fis = null;
		    	PlottedChunksRegion result = null;
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
					OTG.log(LogMarker.INFO, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
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
		    	PlottedChunksRegion result = null;
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
		    	if(regionCoord != null)
		    	{
		    		output.put(regionCoord, PlottedChunksRegion.getFilledRegion());
					OTG.log(LogMarker.INFO,
						"OTG encountered an error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, substituting a default filled region. "
						+ "This may result in areas with missing BO4's, smoothing areas, /otg structure info and spawners/particles/moddata."
					);
		    	} else {
					throw new RuntimeException(
						"OTG encountered a critical error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
						+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
						+ "If your dimension's structure data files and backups have been corrupted, you can delete them,"
						+ "at the risk of losing data for unspawned structure parts."
					);
		    	}
		    }
		}
		
		return output.size() > 0 ? output : null;
	}
	
	private static PlottedChunksRegion parsePlottedChunksFileFromStream(ByteBuffer buffer, LocalWorld world) throws IOException
	{
		int version = buffer.getInt();		
		int regionSize = buffer.getInt();
		boolean[][] chunksMatrix = new boolean[CustomStructureCache.REGION_SIZE][CustomStructureCache.REGION_SIZE];
		if(regionSize == CustomStructureCache.REGION_SIZE)
		{		
			for(int x = 0; x < regionSize; x++)
			{
				for(int z = 0; z < regionSize; z++)
				{
					chunksMatrix[x][z] = buffer.get() != 0;
				}
			}
		} else {
			OTG.log(LogMarker.INFO, "PlottedChunks region files were corrupted or exported with an incompatible version of OTG, ignoring.");
			return PlottedChunksRegion.getFilledRegion();
		}
		return new PlottedChunksRegion(chunksMatrix);
	}
	
	
	// Structure cache

	// TODO: Since we're using regions, use short/byte for (internal) coords?
	static void saveStructureData(Map<ChunkCoordinate, StructureDataRegion> worldInfoChunks, LocalWorld world)
	{
		int dimensionId = world.getDimensionId();
		
		// Collect all structure start points (and chunks that have bo3's with spawners/moddata/particles in them)
		// and group them by BO name (or "NULL" for bo3's with spawners/moddata/particles).
		// Structure starts are saved per region, if a BO4 structure has chunk data in multiple regions, each region gets 
		// its own BO4CustomStructure containing only the chunk data for that region. When loading, structures that have 
		// their structure start in a different region are loaded as CustomStructurePlaceHolder instead of BO4CustomStructure.
		// When loading regions, we'll reconstitute/update worldInfoChunks by replacing any CustomStructurePlaceHolders with 
		// BO4CustomStructures as soon as they're loaded from disk. Fully spawned chunks that are part of structures are saved 
		// to disk inside their structure start/placeholder, but are only cached/kept in memory in worldInfoChunks.
		// (BO4CustomStructures only cache data for unspawned structure parts and spawners/moddata/particles, worldInfoChunks 
		// caches data about fully spawned structure chunks, plottedChunks caches/persists info about plotted chunks etc).
		int regionsSaved = 0;
		for (Entry<ChunkCoordinate, StructureDataRegion> cachedRegion : worldInfoChunks.entrySet())
		{
			if(cachedRegion.getValue().requiresSave())
			{
				cachedRegion.getValue().markSaved();
				regionsSaved++;
				
				HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>> structuresPerRegion = new HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>>();			
				for(int internalX = 0; internalX < CustomStructureCache.REGION_SIZE; internalX++)
				{
					for(int internalZ = 0; internalZ < CustomStructureCache.REGION_SIZE; internalZ++)
					{
						ChunkCoordinate worldChunkCoord = ChunkCoordinate.fromChunkCoords(
							(cachedRegion.getKey().getChunkX() * CustomStructureCache.REGION_SIZE) + internalX, 
							(cachedRegion.getKey().getChunkZ() * CustomStructureCache.REGION_SIZE) + internalZ
						);
						CustomStructure structureInChunk = cachedRegion.getValue().getStructure(internalX, internalZ);
						if(structureInChunk != null)
						{
			    			// BO3's that add spawners/particles/moddata are saved as null structures
			    			String startBoName = "NULL"; 
			    			if(structureInChunk.start != null)
			    			{
			    				startBoName = structureInChunk.start.bo3Name;
			    			}

							HashMap<CustomStructure, ArrayList<ChunkCoordinate>> entryByStructureName = structuresPerRegion.get(startBoName);
							ArrayList<ChunkCoordinate> structureChunks = new ArrayList<ChunkCoordinate>();
			    			if(entryByStructureName == null)
			    			{
			    				entryByStructureName = new HashMap<CustomStructure, ArrayList<ChunkCoordinate>>();
			    				entryByStructureName.put(structureInChunk, structureChunks);
			    				structuresPerRegion.put(startBoName, entryByStructureName);
			    			} else {
			    				structureChunks = entryByStructureName.get(structureInChunk);
			    				if(structureChunks == null)
			    				{
			    					structureChunks = new ArrayList<ChunkCoordinate>();
			    					entryByStructureName.put(structureInChunk, structureChunks);
			    				}
			    			}
			    			structureChunks.add(worldChunkCoord);
						}
					}
				}
				saveStructuresRegionFile(world, dimensionId, cachedRegion.getKey(), structuresPerRegion);
			}
		}
		OTG.log(LogMarker.INFO, regionsSaved + " structure data regions saved.");
	}

	private static void saveStructuresRegionFile(LocalWorld world, int dimensionId, ChunkCoordinate regionCoord, HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>> structuresPerRegion)
	{
		File structuresRegionFile = new File(
			world.getWorldSaveDir().getAbsolutePath() + File.separator + 
			PluginStandardValues.PLUGIN_NAME + File.separator + 
			(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
			WorldStandardValues.StructureDataFolderName + File.separator +
			regionCoord.getChunkX() + "_" +
			regionCoord.getChunkZ() +
			WorldStandardValues.StructureDataFileExtension
		);
		File structuresRegionBackupFile = new File(
			world.getWorldSaveDir().getAbsolutePath() + File.separator + 
			PluginStandardValues.PLUGIN_NAME + File.separator + 
			(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
			WorldStandardValues.StructureDataFolderName + File.separator +
			regionCoord.getChunkX() + "_" +
			regionCoord.getChunkZ() +    				
			WorldStandardValues.StructureDataBackupFileExtension
		);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try
		{
			int version = 1;
			dos.writeInt(version);
			dos.writeInt(structuresPerRegion.entrySet().size());
			for(Entry<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>> entry : structuresPerRegion.entrySet())
			{
				StreamHelper.writeStringToStream(dos, entry.getKey());
				dos.writeInt(entry.getValue().entrySet().size());
				// Structures have been de-duplicated, should be only one entry per structure start
				for(Entry<CustomStructure, ArrayList<ChunkCoordinate>> entry1 : entry.getValue().entrySet())
				{
					CustomStructure structure = entry1.getKey();

					// No need to write to file whether this is a CustomStructurePlaceHolder or not.
					// If the structure start is outside the current region, it's a placeholder.
					
					// Write structure start data (if any)
					// If name is "NULL", we'll know not to look for these when reading.
					if(entry1.getKey().start != null)
					{
    					dos.writeInt(structure.start.rotation.getRotationId());
    					dos.writeInt(structure.start.getX());
    					dos.writeInt(structure.start.getY());
    					dos.writeInt(structure.start.getZ());
					}

					// Write all chunks used for structure
					dos.writeInt(entry1.getValue().size());
					for(ChunkCoordinate chunkCoord : entry1.getValue())
					{
						// TODO: Use internal coords so we can use byte/short 
			    		dos.writeInt(chunkCoord.getChunkX());
			    		dos.writeInt(chunkCoord.getChunkZ());
					}

					if(
						structure instanceof BO4CustomStructure &&
						((BO4CustomStructure)structure).objectsToSpawn.entrySet().size() > 0
					)
					{
						dos.writeBoolean(true);
												
						Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsInRegion = new HashMap<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>>();
						int size = 0;
						for(Entry<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectToSpawn : ((BO4CustomStructure)structure).objectsToSpawn.entrySet())
						{
							if(objectToSpawn.getKey().toRegionCoord().equals(regionCoord))
							{
								objectsInRegion.put(objectToSpawn.getKey(), objectToSpawn.getValue());
								size++;
							}
						}
						
						dos.writeInt(size);
						for(Entry<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectToSpawn : objectsInRegion.entrySet())
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
						
						Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasPerRegion = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
						int size = 0;
						for(Entry<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreaToSpawn : ((BO4CustomStructure)structure).smoothingAreaManager.smoothingAreasToSpawn.entrySet())
						{
							if(smoothingAreaToSpawn.getKey().toRegionCoord().equals(regionCoord))
							{
								smoothingAreasPerRegion.put(smoothingAreaToSpawn.getKey(), smoothingAreaToSpawn.getValue());
								size++;
							}
						}
						
						dos.writeInt(size);
						for(Entry<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreaToSpawn : smoothingAreasPerRegion.entrySet())
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
						
						HashSet<ModDataFunction<?>> modDataPerRegion = new HashSet<ModDataFunction<?>>();
						int size = 0;
						for(ModDataFunction<?> modData : structure.modDataManager.modData)
						{
							if(ChunkCoordinate.fromBlockCoords(modData.x, modData.z).toRegionCoord().equals(regionCoord))
							{
								modDataPerRegion.add(modData);
								size++;
							}
						}
						
						dos.writeInt(size);
						for(ModDataFunction<?> modData : modDataPerRegion)
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
						
						HashSet<SpawnerFunction<?>> spawnerDataPerRegion = new HashSet<SpawnerFunction<?>>();
						int size = 0;
						for(SpawnerFunction<?> spawnerData : structure.spawnerManager.spawnerData)
						{
							if(ChunkCoordinate.fromBlockCoords(spawnerData.x, spawnerData.z).toRegionCoord().equals(regionCoord))
							{
								spawnerDataPerRegion.add(spawnerData);
								size++;
							}
						}
						
						dos.writeInt(size);
						for(SpawnerFunction<?> spawnerData : spawnerDataPerRegion)
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
						
						HashSet<ParticleFunction<?>> particleDataPerRegion = new HashSet<ParticleFunction<?>>();
						int size = 0;
						for(ParticleFunction<?> particleData : structure.particlesManager.particleData)
						{
							if(ChunkCoordinate.fromBlockCoords(particleData.x, particleData.z).toRegionCoord().equals(regionCoord))
							{
								particleDataPerRegion.add(particleData);
								size++;
							}
						}
						
						dos.writeInt(size);
						for(ParticleFunction<?> particleData : particleDataPerRegion)
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
    		if(!structuresRegionFile.exists())
    		{
    			structuresRegionFile.getParentFile().mkdirs();
    		} else {
    			Files.move(structuresRegionFile.toPath(), structuresRegionBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    		}
			byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray());
			dos.close();
			fos = new FileOutputStream(structuresRegionFile);
			dos2 = new DataOutputStream(fos);
			dos2.write(compressedBytes, 0, compressedBytes.length);
        }
        catch (IOException e)
        {
			e.printStackTrace();
			OTG.log(LogMarker.INFO, "OTG encountered an error writing " + structuresRegionFile.getAbsolutePath() + ", skipping.");
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
	
	// TODO: Load one region file at a time, on-demand, rather than loading all region files at once.
	// Almost everything should be set up for it, auto-replacing CustomStructurePlaceHolders take care of most things?
	public static HashMap<CustomStructure, ArrayList<ChunkCoordinate>> loadStructureData(LocalWorld world)
	{
		int dimensionId = world.getDimensionId();

		HashMap<CustomStructure, ArrayList<ChunkCoordinate>> output = new HashMap<CustomStructure, ArrayList<ChunkCoordinate>>();
		
		File structureDataFolder = new File(
			world.getWorldSaveDir().getAbsolutePath() + File.separator + 
			PluginStandardValues.PLUGIN_NAME + File.separator + 
			(dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") +
			WorldStandardValues.StructureDataFolderName + File.separator
		);
		
		HashMap<File, File> saveFiles = new HashMap<File, File>();
		ArrayList<File> mainFiles = new ArrayList<File>();
		ArrayList<File> backupFiles = new ArrayList<File>();
		if(structureDataFolder.exists())
		{
			for(File file : structureDataFolder.listFiles())
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
			ChunkCoordinate regionCoord = null;
			boolean bSuccess = false;
			File structureDataFile = saveFile.getKey();
			File structureDataBackupFile = saveFile.getValue();
				
		    if(
	    		(structureDataFile == null || !structureDataFile.exists()) &&
				(structureDataBackupFile == null || !structureDataBackupFile.exists())
    		)
		    {
		    	continue;
		    }

		    if(structureDataFile != null && structureDataFile.exists())
		    {			
		    	FileInputStream fis = null;
		    	HashMap<CustomStructure, ArrayList<ChunkCoordinate>> result = null;
				try {
					
					int regionX = Integer.parseInt(structureDataFile.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_")[0]);
					int regionZ = Integer.parseInt(structureDataFile.getName().replace(WorldStandardValues.StructureDataFileExtension, "").split("_")[1]);
		    		regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);
					
			    	fis = new FileInputStream(structureDataFile);			
		    		ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
		    		buffer = ByteBuffer.wrap(decompressedBytes);
		    							
		    		result = parseStructuresFileFromStream(buffer, regionCoord, world);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					OTG.log(LogMarker.INFO, "Failed to load " + structureDataFile.getAbsolutePath() + ", trying to load backup.");
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
					mergeRegionData(world, result, output);
				}
		    }
		    
		    if(!bSuccess && structureDataBackupFile != null && structureDataBackupFile.exists())
		    {
		    	FileInputStream fis = null;
		    	HashMap<CustomStructure, ArrayList<ChunkCoordinate>> result = null;
				try {
		    		int regionX = Integer.parseInt(structureDataBackupFile.getName().replace(WorldStandardValues.BackupFileSuffix, "").split("_")[0]);
		    		int regionZ = Integer.parseInt(structureDataBackupFile.getName().replace(WorldStandardValues.BackupFileSuffix, "").split("_")[1]);	    		
		    		regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);					
					
			    	fis = new FileInputStream(structureDataBackupFile);			
		    		ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
		    		buffer = ByteBuffer.wrap(decompressedBytes);
		    				    		
		    		result = parseStructuresFileFromStream(buffer, regionCoord, world);
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
					mergeRegionData(world, result, output);
				}
		    }
		    if(!bSuccess)
		    {
				OTG.log(LogMarker.INFO,
					"OTG encountered an error loading " + structureDataFile.getAbsolutePath() + " and could not load a backup, ignoring. "
					+ "This may result in areas with missing BO4's, smoothing areas, /otg structure info and spawners/particles/moddata."
				);
		    }
		}
		
		return output.size() > 0 ? output : null;
	}
	
	private static void mergeRegionData(LocalWorld world, HashMap<CustomStructure, ArrayList<ChunkCoordinate>> result, HashMap<CustomStructure, ArrayList<ChunkCoordinate>> output)
	{
		// When parsing structures per region, merge all placeholder structures 
		// into their real structure starts as soon as their regions are loaded.
		// TODO: Load on-demand, not all regions at once.
		for(Entry<CustomStructure, ArrayList<ChunkCoordinate>> entryResult : result.entrySet())
		{
			if(output.containsKey(entryResult.getKey()))
			{
				for(Entry<CustomStructure, ArrayList<ChunkCoordinate>> entryOutput : new HashSet<Entry<CustomStructure, ArrayList<ChunkCoordinate>>>(output.entrySet()))
				{				
					// Returns true if structure starts are equal
					if(entryResult.getKey().equals(entryOutput.getKey()))
					{
						if(entryResult.getKey() instanceof CustomStructurePlaceHolder)
						{
							((CustomStructurePlaceHolder)entryResult.getKey()).mergeWithCustomStructure(world, (BO4CustomStructure)entryOutput.getKey());
							ArrayList<ChunkCoordinate> coords = entryOutput.getValue();
							coords.addAll(entryResult.getValue());							
						}
						else if(entryOutput.getKey() instanceof CustomStructurePlaceHolder)
						{
							((CustomStructurePlaceHolder)entryOutput.getKey()).mergeWithCustomStructure(world, (BO4CustomStructure)entryResult.getKey());
							ArrayList<ChunkCoordinate> coords = entryResult.getValue();
							coords.addAll(entryOutput.getValue());
							
							// Be sure to remove before putting, or only the value gets replaced.
							output.remove(entryResult.getKey());
							output.put(entryResult.getKey(), entryResult.getValue());							
						}
						break;
					}
				}
			} else {
				output.put(entryResult.getKey(), entryResult.getValue());
			}
		}
	}

	// TODO: Since we're using regions now, can use byte/short for internal coords instead of int.
	// TODO: Dev versions of v9 used region size 100, not 250, this may cause problems.
	private static HashMap<CustomStructure, ArrayList<ChunkCoordinate>> parseStructuresFileFromStream(ByteBuffer buffer, ChunkCoordinate regionCoord, LocalWorld world) throws IOException
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
					int chunkX = buffer.getInt();
					int chunkZ = buffer.getInt();
					chunkCoords.add(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));
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
					// If the structure start is outside the current region, it's a placeholder.
			    	// We'll replace the placeholder in worldInfoChunks as soon as the region data
			    	// containing the "real" structure start is loaded.
			    	ChunkCoordinate startChunkCoord = ChunkCoordinate.fromChunkCoords(structureStart.getChunkX(), structureStart.getChunkZ());
			    	if(!startChunkCoord.toRegionCoord().equals(regionCoord))
			    	{
			    		structure = new CustomStructurePlaceHolder(world, (BO4CustomStructureCoordinate)structureStart, objectsToSpawn, smoothingAreasToSpawn, 0);		    		
			    	} else {
				    	structure = new BO4CustomStructure(world, (BO4CustomStructureCoordinate)structureStart, objectsToSpawn, smoothingAreasToSpawn, 0);
			    	}
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
				OTG.log(LogMarker.INFO, "OTG encountered an error writing " + occupiedChunksFile.getAbsolutePath() + ", skipping.");
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
				OTG.log(LogMarker.INFO, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
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
		
		OTG.log(LogMarker.INFO, "OTG encountered an error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, skipping. ");
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
