package com.pg85.otg.customobject.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

public class NBTHelper
{
	// A list of already loaded meta Tags. The path is the key, a NBT Tag is the value.
	private static Map<String, NamedBinaryTag> LoadedTags = new HashMap<String, NamedBinaryTag>();

	private static NamedBinaryTag loadTileEntityFromNBT(String path, ILogger logger)
	{
		// Load from file
		NamedBinaryTag metadata;
		FileInputStream stream = null;
		try
		{
			// Read it from a file next to the BO3
			stream = new FileInputStream(path);
			// Get the tag
			metadata = NamedBinaryTag.readFrom(stream, true);
		} catch (FileNotFoundException e) {
			// File not found
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, MessageFormat.format("NBT file {0} not found", path));
			}
			return null;
		} catch (IOException e)
		{
			tryToClose(stream);

			// Not a compressed NBT file, try uncompressed
			FileInputStream streamForUncompressed = null;
			try
			{
				// Read it from a file next to the BO3
				streamForUncompressed = new FileInputStream(path);
				// Get the tag
				metadata = NamedBinaryTag.readFrom(streamForUncompressed, false);
			}			 
			catch (java.lang.ArrayIndexOutOfBoundsException corruptFile)
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Failed to read NBT meta file: " + e.getMessage());
					logger.printStackTrace(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, corruptFile);
				}
				return null;
			}
			catch (IOException corruptFile)
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Failed to read NBT meta file: " + e.getMessage());
					logger.printStackTrace(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, corruptFile);
				}
				return null;
			} finally
			{
				tryToClose(streamForUncompressed);
			}
		} finally {
			tryToClose(stream);
		}

		if(metadata != null)
		{

			// The file can be structured in two ways:
			// 1. chest.nbt with all the contents directly in it
			// 2. chest.nbt with a Compound tag in it with all the data
	
			// Check for type 1 by searching for an id tag
			NamedBinaryTag idTag = metadata.getTag("id");
			if (idTag != null)
			{
				// Found id tag, so return the root tag
				return metadata;
			}
			// No id tag found, so check for type 2
			if (metadata.getValue() instanceof NamedBinaryTag[])
			{
				NamedBinaryTag[] subtag = (NamedBinaryTag[]) metadata.getValue();
				if (subtag.length != 0)
				{
					return subtag[0];
				}
			}
		}
		// Unknown/bad structure
		if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
		{
			logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Structure of NBT file is incorrect: " + path);
		}
		return null;
	}	
	
	private static void tryToClose(InputStream stream)
	{
		if (stream != null)
		{
			try
			{
				stream.close();
			} catch (IOException ignored)
			{
				// Ignore
			}
		}
	}

	public static NamedBinaryTag loadMetadata(String name, File bo3Folder, ILogger logger)
	{
		String path = bo3Folder.getParent() + File.separator + name;

		if (LoadedTags.containsKey(path))
		{
			// Found a cached one
			return LoadedTags.get(path);
		}

		NamedBinaryTag tag = loadTileEntityFromNBT(path, logger);
		registerMetadata(path, tag);
		return tag;
	}

	/**
	 * Caches and returns the provided Meta data
	 * @param pathOnDisk The path of the meta data
	 * @param metadata	The Tag object to be cached
	 * @return the meta data that was cached
	 */
	private static NamedBinaryTag registerMetadata(String pathOnDisk, NamedBinaryTag metadata)
	{
		// Add it to the cache
		LoadedTags.put(pathOnDisk, metadata);
		// Return it
		return metadata;
	}

	public static void clearCache()
	{
		// Clean up the cache
		LoadedTags.clear();
	}	
}
