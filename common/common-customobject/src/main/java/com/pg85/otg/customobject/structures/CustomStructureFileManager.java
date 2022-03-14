package com.pg85.otg.customobject.structures;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructure;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.bo4.CustomStructurePlaceHolder;
import com.pg85.otg.customobject.structures.bo4.smoothing.SmoothingAreaLine;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.StreamHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

public class CustomStructureFileManager
{
	// Plotted chunks
	
	public static void savePlottedChunksData(Path worldSaveDir, String presetFolderName, Map<ChunkCoordinate, PlottedChunksRegion> decoratedChunks, ILogger logger)
	{
		int regionsSaved = 0;
		if(decoratedChunks.size() > 0)
		{
			for(Entry<ChunkCoordinate, PlottedChunksRegion> chunkPerRegionEntry : decoratedChunks.entrySet())
			{
				if(!chunkPerRegionEntry.getValue().requiresSave())
				{
					continue;
				}
				chunkPerRegionEntry.getValue().markSaved();
				regionsSaved++;
				
				File occupiedChunksFile = new File(
					worldSaveDir + File.separator + 
					Constants.MOD_ID + File.separator + 
					presetFolderName + File.separator +
					Constants.PlottedChunksDataFolderName + File.separator +
					chunkPerRegionEntry.getKey().getChunkX() + "_" +
					chunkPerRegionEntry.getKey().getChunkZ() +
					Constants.StructureDataFileExtension
				);
				File occupiedChunksBackupFile = new File(
					worldSaveDir + File.separator + 
					Constants.MOD_ID + File.separator + 
					presetFolderName + File.separator +
					Constants.PlottedChunksDataFolderName + File.separator +
					chunkPerRegionEntry.getKey().getChunkX() + "_" +
					chunkPerRegionEntry.getKey().getChunkZ() +					
					Constants.StructureDataBackupFileExtension
				);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				
				boolean[][] entriesByStructureName = chunkPerRegionEntry.getValue().getArray();
				try
				{
					int version = 1;
					dos.writeInt(version);
					dos.writeInt(Constants.REGION_SIZE);

					for(int x = 0; x < Constants.REGION_SIZE; x++)
					{
						boolean[] structureArr = entriesByStructureName[x];
						for(int z = 0; z < Constants.REGION_SIZE; z++)
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
					byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray(), logger);
					dos.close();
					fos = new FileOutputStream(occupiedChunksFile);
					dos2 = new DataOutputStream(fos);
					dos2.write(compressedBytes, 0, compressedBytes.length);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					logger.log(LogLevel.ERROR, LogCategory.MAIN, "OTG encountered an error writing " + occupiedChunksFile.getAbsolutePath() + ", skipping.");
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
		
		if(logger.getLogCategoryEnabled(LogCategory.STRUCTURE_PLOTTING))
		{
			logger.log(LogLevel.INFO, LogCategory.STRUCTURE_PLOTTING, regionsSaved + " plotted chunk regions saved.");
		}
	}
	
	public static Map<ChunkCoordinate, PlottedChunksRegion> loadPlottedChunksData(Path worldSaveDir, String presetFolderName, ILogger logger)
	{
		HashMap<ChunkCoordinate, PlottedChunksRegion> output = new HashMap<ChunkCoordinate, PlottedChunksRegion>();
		
		File occupiedChunksFolder = new File(
			worldSaveDir + File.separator + 
			Constants.MOD_ID + File.separator + 
			presetFolderName + File.separator +
			Constants.PlottedChunksDataFolderName + File.separator
		);

		HashMap<File, File> saveFiles = new HashMap<File, File>();
		ArrayList<File> mainFiles = new ArrayList<File>();
		ArrayList<File> backupFiles = new ArrayList<File>();
		if(occupiedChunksFolder.exists())
		{
			for(File file : occupiedChunksFolder.listFiles())
			{
				if(
					file.getPath().endsWith(Constants.StructureDataFileExtension) && 
					file.getName().replace(Constants.StructureDataFileExtension, "").split("_").length == 2 &&
					MathHelper.tryParseInt(file.getName().replace(Constants.StructureDataFileExtension, "").split("_")[0]) &&
					MathHelper.tryParseInt(file.getName().replace(Constants.StructureDataFileExtension, "").split("_")[1])
				) {
					mainFiles.add(file);
				}
				else if(
					file.getPath().endsWith(Constants.StructureDataBackupFileExtension) &&
					file.getName().replace(Constants.BackupFileSuffix, "").split("_").length == 2 &&
					MathHelper.tryParseInt(file.getName().replace(Constants.BackupFileSuffix, "").split("_")[0]) &&
					MathHelper.tryParseInt(file.getName().replace(Constants.BackupFileSuffix, "").split("_")[1])
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
					if(file.getPath().replace(Constants.StructureDataFileExtension, "").equals(backupFile.getPath().replace(Constants.StructureDataBackupFileExtension, "")))
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
					String[] chunkCoords = occupiedChunksFile.getName().replace(Constants.StructureDataFileExtension, "").split("_");
					int regionX = Integer.parseInt(chunkCoords[0]);
					int regionZ = Integer.parseInt(chunkCoords[1]);
					regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);
					
					fis = new FileInputStream(occupiedChunksFile);			
					ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
					buffer = ByteBuffer.wrap(decompressedBytes);
					result = parsePlottedChunksFileFromStream(buffer, logger);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					logger.log(LogLevel.WARN, LogCategory.MAIN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
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
					String[] chunkCoords = occupiedChunksFile.getName().replace(Constants.StructureDataBackupFileExtension, "").split("_");
					int regionX = Integer.parseInt(chunkCoords[0]);
					int regionZ = Integer.parseInt(chunkCoords[1]);
					regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);
					
					fis = new FileInputStream(occupiedChunksBackupFile);			
					ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
					buffer = ByteBuffer.wrap(decompressedBytes);
					result = parsePlottedChunksFileFromStream(buffer, logger);
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
					logger.log(LogLevel.ERROR, LogCategory.MAIN,
						"OTG encountered an error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, substituting a default filled region. "
						+ "This may result in areas with missing BO4's, smoothing areas, /otg structure info and spawners/particles/moddata."
					);
				} else {
					logger.log(LogLevel.FATAL, LogCategory.MAIN,
						"OTG encountered a critical error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
						+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
						+ "If your dimension's structure data files and backups have been corrupted, you can delete them,"
						+ "at the risk of losing data for unspawned structure parts."
					);					
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
	
	private static PlottedChunksRegion parsePlottedChunksFileFromStream(ByteBuffer buffer, ILogger logger) throws IOException
	{
		buffer.getInt(); // Version, not used atm.		
		int regionSize = buffer.getInt();
		boolean[][] chunksMatrix = new boolean[Constants.REGION_SIZE][Constants.REGION_SIZE];
		if(regionSize == Constants.REGION_SIZE)
		{		
			for(int x = 0; x < regionSize; x++)
			{
				for(int z = 0; z < regionSize; z++)
				{
					chunksMatrix[x][z] = buffer.get() != 0;
				}
			}
		} else {
			logger.log(LogLevel.ERROR, LogCategory.MAIN, "PlottedChunks region files were corrupted or exported with an incompatible version of OTG, ignoring.");
			return PlottedChunksRegion.getFilledRegion();
		}
		return new PlottedChunksRegion(chunksMatrix);
	}
	
	// Structure cache

	// TODO: Since we're using regions, use short/byte for (internal) coords?
	static void saveStructureData(Map<ChunkCoordinate, StructureDataRegion> worldInfoChunks, String presetFolderName, Path worldSaveDir, ILogger logger)
	{		
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
				for(int internalX = 0; internalX < Constants.REGION_SIZE; internalX++)
				{
					for(int internalZ = 0; internalZ < Constants.REGION_SIZE; internalZ++)
					{
						ChunkCoordinate worldChunkCoord = ChunkCoordinate.fromChunkCoords(
							(cachedRegion.getKey().getChunkX() * Constants.REGION_SIZE) + internalX, 
							(cachedRegion.getKey().getChunkZ() * Constants.REGION_SIZE) + internalZ
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
				saveStructuresRegionFile(worldSaveDir, presetFolderName, cachedRegion.getKey(), structuresPerRegion, logger);
			}
		}
		if(logger.getLogCategoryEnabled(LogCategory.STRUCTURE_PLOTTING))
		{
			logger.log(LogLevel.INFO, LogCategory.STRUCTURE_PLOTTING, regionsSaved + " structure data regions saved.");
		}
	}

	private static void saveStructuresRegionFile(Path worldSaveDir, String presetFolderName, ChunkCoordinate regionCoord, HashMap<String, HashMap<CustomStructure, ArrayList<ChunkCoordinate>>> structuresPerRegion, ILogger logger)
	{
		File structuresRegionFile = new File(
			worldSaveDir + File.separator + 
			Constants.MOD_ID + File.separator + 
			presetFolderName + File.separator +
			Constants.StructureDataFolderName + File.separator +
			regionCoord.getChunkX() + "_" +
			regionCoord.getChunkZ() +
			Constants.StructureDataFileExtension
		);
		File structuresRegionBackupFile = new File(
			worldSaveDir + File.separator + 
			Constants.MOD_ID + File.separator + 
			presetFolderName + File.separator +
			Constants.StructureDataFolderName + File.separator +
			regionCoord.getChunkX() + "_" +
			regionCoord.getChunkZ() +					
			Constants.StructureDataBackupFileExtension
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
						((BO4CustomStructure)structure).getObjectsToSpawn().entrySet().size() > 0
					)
					{
						dos.writeBoolean(true);
												
						Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsInRegion = new HashMap<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>>();
						int size = 0;
						for(Entry<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectToSpawn : ((BO4CustomStructure)structure).getObjectsToSpawn().entrySet())
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
						((BO4CustomStructure)structure).getSmoothingAreaManager().smoothingAreasToSpawn.entrySet().size() > 0 
					)
					{
						ArrayList<SmoothingAreaLine> coords2;
						dos.writeBoolean(true);
						
						Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasPerRegion = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
						int size = 0;
						for(Entry<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreaToSpawn : ((BO4CustomStructure)structure).getSmoothingAreaManager().smoothingAreasToSpawn.entrySet())
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
			byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray(), logger);
			dos.close();
			fos = new FileOutputStream(structuresRegionFile);
			dos2 = new DataOutputStream(fos);
			dos2.write(compressedBytes, 0, compressedBytes.length);
		}
		catch (IOException e)
		{
			logger.log(LogLevel.ERROR, LogCategory.MAIN, "OTG encountered an error writing " + structuresRegionFile.getAbsolutePath() + ", skipping. Exception:");
			e.printStackTrace();			
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
	static HashMap<CustomStructure, ArrayList<ChunkCoordinate>> loadStructureData(String presetFolderName, Path worldSaveDir, long worldSeed, boolean isBO4Enabled, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		HashMap<CustomStructure, ArrayList<ChunkCoordinate>> output = new HashMap<CustomStructure, ArrayList<ChunkCoordinate>>();
		
		File structureDataFolder = new File(
			worldSaveDir + File.separator + 
			Constants.MOD_ID + File.separator + 
			presetFolderName + File.separator +
			Constants.StructureDataFolderName + File.separator
		);
		
		HashMap<File, File> saveFiles = new HashMap<File, File>();
		ArrayList<File> mainFiles = new ArrayList<File>();
		ArrayList<File> backupFiles = new ArrayList<File>();
		if(structureDataFolder.exists())
		{
			for(File file : structureDataFolder.listFiles())
			{
				if(
					file.getPath().endsWith(Constants.StructureDataFileExtension) && 
					file.getName().replace(Constants.StructureDataFileExtension, "").split("_").length == 2 &&
					MathHelper.tryParseInt(file.getName().replace(Constants.StructureDataFileExtension, "").split("_")[0]) &&
					MathHelper.tryParseInt(file.getName().replace(Constants.StructureDataFileExtension, "").split("_")[1])
				) {
					mainFiles.add(file);
				}
				else if(
					file.getPath().endsWith(Constants.StructureDataBackupFileExtension) &&
					file.getName().replace(Constants.BackupFileSuffix, "").split("_").length == 2 &&
					MathHelper.tryParseInt(file.getName().replace(Constants.BackupFileSuffix, "").split("_")[0]) &&
					MathHelper.tryParseInt(file.getName().replace(Constants.BackupFileSuffix, "").split("_")[1])
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
					if(file.getPath().replace(Constants.StructureDataFileExtension, "").equals(backupFile.getPath().replace(Constants.StructureDataBackupFileExtension, "")))
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
					
					int regionX = Integer.parseInt(structureDataFile.getName().replace(Constants.StructureDataFileExtension, "").split("_")[0]);
					int regionZ = Integer.parseInt(structureDataFile.getName().replace(Constants.StructureDataFileExtension, "").split("_")[1]);
					regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);
					
					fis = new FileInputStream(structureDataFile);			
					ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
					buffer = ByteBuffer.wrap(decompressedBytes);
										
					result = parseStructuresFileFromStream(buffer, regionCoord, presetFolderName, worldSeed, isBO4Enabled, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					logger.log(LogLevel.WARN, LogCategory.MAIN, "Failed to load " + structureDataFile.getAbsolutePath() + ", trying to load backup.");
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
					mergeRegionData(result, output);
				}
			}
			
			if(!bSuccess && structureDataBackupFile != null && structureDataBackupFile.exists())
			{
				FileInputStream fis = null;
				HashMap<CustomStructure, ArrayList<ChunkCoordinate>> result = null;
				try {
					int regionX = Integer.parseInt(structureDataBackupFile.getName().replace(Constants.BackupFileSuffix, "").split("_")[0]);
					int regionZ = Integer.parseInt(structureDataBackupFile.getName().replace(Constants.BackupFileSuffix, "").split("_")[1]);				
					regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);					
					
					fis = new FileInputStream(structureDataBackupFile);			
					ByteBuffer buffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fis.getChannel().size());				
					
					byte[] compressedBytes = new byte[(int) fis.getChannel().size()];
					buffer.get(compressedBytes);
					byte[] decompressedBytes = com.pg85.otg.util.CompressionUtils.decompress(compressedBytes);
					buffer = ByteBuffer.wrap(decompressedBytes);
										
					result = parseStructuresFileFromStream(buffer, regionCoord, presetFolderName, worldSeed, isBO4Enabled, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
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
					mergeRegionData(result, output);
				}
			}
			if(!bSuccess)
			{
				logger.log(LogLevel.ERROR, LogCategory.MAIN,
					"OTG encountered an error loading " + structureDataFile.getAbsolutePath() + " and could not load a backup, ignoring. "
					+ "This may result in areas with missing BO4's, smoothing areas, /otg structure info and spawners/particles/moddata."
				);
			}
		}
		
		return output.size() > 0 ? output : null;
	}
	
	private static void mergeRegionData(HashMap<CustomStructure, ArrayList<ChunkCoordinate>> result, HashMap<CustomStructure, ArrayList<ChunkCoordinate>> output)
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
							((CustomStructurePlaceHolder)entryResult.getKey()).mergeWithCustomStructure((BO4CustomStructure)entryOutput.getKey());
							ArrayList<ChunkCoordinate> coords = entryOutput.getValue();
							coords.addAll(entryResult.getValue());							
						}
						else if(entryOutput.getKey() instanceof CustomStructurePlaceHolder)
						{
							((CustomStructurePlaceHolder)entryOutput.getKey()).mergeWithCustomStructure((BO4CustomStructure)entryResult.getKey());
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
	private static HashMap<CustomStructure, ArrayList<ChunkCoordinate>> parseStructuresFileFromStream(ByteBuffer buffer, ChunkCoordinate regionCoord, String presetFolderName, long worldSeed, boolean isBO4Enabled, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws IOException
	{
		buffer.getInt(); // Version, not used atm.
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

					if(isBO4Enabled)
					{
						structureStart = new BO4CustomStructureCoordinate(presetFolderName, null, structureName, startRotationId, startX, (short)startY, startZ, 0, false, false, null);
					} else {
						structureStart = new BO3CustomStructureCoordinate(presetFolderName, null, structureName, startRotationId, startX, (short)startY, startZ);
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
							coords.add(new BO4CustomStructureCoordinate(presetFolderName, null, bo3Name, coordRotation, coordX, (short)coordY, coordZ, 0, false, false, null));
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

				CustomStructure structure;
				if(isBO4Enabled)
				{
					// If the structure start is outside the current region, it's a placeholder.
					// We'll replace the placeholder in worldInfoChunks as soon as the region data
					// containing the "real" structure start is loaded.
					ChunkCoordinate startChunkCoord = ChunkCoordinate.fromChunkCoords(structureStart.getChunkX(), structureStart.getChunkZ());
					if(!startChunkCoord.toRegionCoord().equals(regionCoord))
					{
						structure = new CustomStructurePlaceHolder(worldSeed, (BO4CustomStructureCoordinate)structureStart, objectsToSpawn, smoothingAreasToSpawn, 0, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
					} else {
						structure = new BO4CustomStructure(worldSeed, (BO4CustomStructureCoordinate)structureStart, objectsToSpawn, smoothingAreasToSpawn, 0, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
					}
					((BO4CustomStructure)structure).setStartChunkBlockChecksDone();
				} else {
					structure = new BO3CustomStructure((BO3CustomStructureCoordinate)structureStart);
				}
				structuresFile.put(structure, chunkCoords);
			}
		}

		return structuresFile;
	}

	public static void saveChunksMapFile(Path worldSaveDir, String presetFolderName, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup, ILogger logger)
	{
		File occupiedChunksFile = new File(worldSaveDir + File.separator + Constants.MOD_ID + File.separator + presetFolderName + File.separator + Constants.SpawnedStructuresFileName);
		File occupiedChunksBackupFile = new File(worldSaveDir + File.separator + Constants.MOD_ID + File.separator + presetFolderName + File.separator + Constants.SpawnedStructuresBackupFileName);

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
				byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray(), logger);
				fos = new FileOutputStream(occupiedChunksFile);
				dos2 = new DataOutputStream(fos);
				dos2.write(compressedBytes, 0, compressedBytes.length);
			} catch (IOException e) {
				logger.log(LogLevel.ERROR, LogCategory.MAIN, "OTG encountered an error writing " + occupiedChunksFile.getAbsolutePath() + ", skipping. Exception: ");
				e.printStackTrace();				
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

	public static void loadChunksMapFile(Path worldSaveDir, String presetFolderName, boolean isBO4Enabled, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup, ILogger logger)
	{
		File occupiedChunksFile = new File(worldSaveDir + File.separator + Constants.MOD_ID + File.separator + presetFolderName + File.separator + Constants.SpawnedStructuresFileName);
		File occupiedChunksBackupFile = new File(worldSaveDir + File.separator + Constants.MOD_ID + File.separator + presetFolderName + File.separator + Constants.SpawnedStructuresBackupFileName);

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
				parseChunksMapFileFromStream(buffer, spawnedStructuresByName, spawnedStructuresByGroup);
				return;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				logger.log(LogLevel.WARN, LogCategory.MAIN, "Failed to load " + occupiedChunksFile.getAbsolutePath() + ", trying to load backup.");
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
				parseChunksMapFileFromStream(buffer, spawnedStructuresByName, spawnedStructuresByGroup);
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
		
		logger.log(LogLevel.ERROR, LogCategory.MAIN, "OTG encountered an error loading " + occupiedChunksFile.getAbsolutePath() + " and could not load a backup, skipping. ");
	}

	private static void parseChunksMapFileFromStream(ByteBuffer buffer, HashMap<String, ArrayList<ChunkCoordinate>> spawnedStructuresByName, HashMap<String, HashMap<ChunkCoordinate, Integer>> spawnedStructuresByGroup) throws IOException
	{
		HashMap<String, ArrayList<ChunkCoordinate>> chunksByName = new HashMap<String, ArrayList<ChunkCoordinate>>();
		HashMap<String, HashMap<ChunkCoordinate, Integer>> chunksByGroup = new HashMap<String, HashMap<ChunkCoordinate, Integer>>();		

		buffer.getInt(); // Version, not used atm.
		
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
