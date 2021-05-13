package com.pg85.otg.constants;

import java.io.File;

import com.pg85.otg.util.ChunkCoordinate;

public class Constants
{
	// Plugin constants
	
	// Files
	
	// Main Plugin Config
	public static final String PluginConfigFilename = "OTG.ini";
	
	// Folders
	
	public static final String PRESETS_FOLDER = "Presets";
	public static final String MODPACK_CONFIGS_FOLDER = "Modpacks";
	public static final String GLOBAL_OBJECTS_FOLDER = "GlobalObjects";
	public static final String DEFAULT_PRESET_NAME = "Default";
	
	// Network
	
	public static final String ChannelName = "OpenTerrainGenerator";
	public static final int ProtocolVersion = 6;
	
	// Plugin Defaults
	
	public static final String MOD_ID = "OpenTerrainGenerator";
	public static final String MOD_ID_LOWER_CASE = "openterraingenerator";
	public static final String MOD_ID_SHORT = "otg";
	
	/**
	 * The world depth that the engine supports. Not the actual depth the
	 * world is capped at. 0 in Minecraft.
	 */
	public static final int WORLD_DEPTH = 0;
	
	/**
	 * The world height that the engine supports. Not the actual height the
	 * world is capped at. 256 in Minecraft.
	 */
	public static final int WORLD_HEIGHT = 256;		
	
	// Region size for BO3/BO4 structure data files
	public static final int REGION_SIZE = 100;
	
	// World constants
	
	// Files and folders
	public static final String WORLD_CONFIG_FILE = "WorldConfig.ini";
	public static final String FALLBACK_FILE = "Fallbacks.ini";
	public static final String WORLD_BIOMES_FOLDER = "Biomes";
	public static final String LEGACY_WORLD_BIOMES_FOLDER = "WorldBiomes";
	public static final String WORLD_OBJECTS_FOLDER = "Objects";
	public static final String LEGACY_WORLD_OBJECTS_FOLDER = "WorldObjects";
	
	public static final String BackupFileSuffix = "-backup";
	public static final String StructureDataFileExtension = ".dat";
	public static final String BiomeConfigFileExtension = ".bc";
	
	// Contains the O menu settings for a world/dims.
	public static final String DimensionsConfigFileName = "Config.yaml";
	public static final String DimensionsConfigBackupFileName = "Config" + BackupFileSuffix + ".yaml";
	// Contains the biome id's for a world/dim, generated on world/dim creation
	public static final String BiomeIdDataFileName = "BiomeIds.txt";
	public static final String BiomeIdDataBackupFileName = "BiomeIds" + BackupFileSuffix + ".txt";
	// Data about dimensions, used to load dims on server start.
	public static final String DimensionsDataFileName = "Dimensions.txt";
	public static final String DimensionsDataBackupFileName = "Dimensions" + BackupFileSuffix + ".txt";
	// Data on all chunks containing structures or spawners/particles/moddata
	public static final String StructureDataFolderName = "StructureData";	
	public static final String StructureDataBackupFileExtension = BackupFileSuffix + StructureDataFileExtension;
	public static final String PlottedChunksDataFolderName = StructureDataFolderName + File.separator + "PlottedChunks";
	// Data about structure start points and bo4 groups, used for distance.
	public static final String SpawnedStructuresFileName = StructureDataFolderName + File.separator + "SpawnedStructures" + StructureDataFileExtension;
	public static final String SpawnedStructuresBackupFileName = StructureDataFolderName + File.separator + "SpawnedStructures" + StructureDataBackupFileExtension;
	// Data about pregenerator progress
	public static final String PregeneratedChunksFileName = "PregeneratedChunks.txt";
	public static final String PregeneratedChunksBackupFileName = "PregeneratedChunks" + BackupFileSuffix + ".txt";
	// Misc data that should be saved with the world. Currently only contains version, 
	// can be used for backwards compatibility when updating configs.
	public static final String WorldSaveDataFileName = "WorldSave.txt";
	public static final String WorldSaveDataBackupFileName = "WorldSave" + BackupFileSuffix + ".txt";
	
	/**
	 * Temperatures below this temperature will cause the biome to be covered
	 * by snow.
	 */
	// OTG biome temperature is between 0.0 and 2.0, snow should appear below 0.15.
	// According to the configs, snow and ice should appear between 0.2 (at y > 90) and 0.1 (entire biome covered in ice).
	// Make sure that at 0.2, snow layers start with thickness 0 at y 90 and thickness 7 around y255.
	// In a 0.2 temp biome, y90 temp is 0.156, y255 temp is -0.12
	public static final float SNOW_AND_ICE_TEMP = 0.15F;
	public static final float SNOW_AND_ICE_MAX_TEMP = -0.115f;
	public static final float ICE_GROUP_MAX_TEMP = 0.33F;
	
	// MesaSurfaceGenerator types
	
	public static final String MESA_NAME_NORMAL = "Mesa";
	public static final String MESA_NAME_FOREST = "MesaForest";
	public static final String MESA_NAME_BRYCE = "MesaBryce";
	
	// Some constants on the terrain shape
	
	/**
	 * The size in blocks of a noise piece in the y direction.
	 */
	public static final int PIECE_Y_SIZE = 8;
	
	/**
	 * The amount of noise pieces that fit inside a chunk on the y axis.
	 */
	public static final int PIECES_PER_CHUNK_Y = ChunkCoordinate.CHUNK_Y_SIZE / PIECE_Y_SIZE;
}
