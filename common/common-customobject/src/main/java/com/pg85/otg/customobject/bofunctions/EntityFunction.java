package com.pg85.otg.customobject.bofunctions;

import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.interfaces.IEntityFunction;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.minecraft.EntityNames;

import java.io.*;
import java.util.List;

/**
 * Represents an entity in a BO3.
 */
public abstract class EntityFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T> implements IEntityFunction<T>
{
	public int y;

	public String name = "";
	public int groupSize = 1;
	public String nameTagOrNBTFileName = "";
	public String originalNameTagOrNBTFileName = "";
	public String resourceLocation = "";
	public NamedBinaryTag namedBinaryTag = null;
	public int rotation = 0;

	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		assureSize(5, args);
		// Those limits are arbitrary, LocalWorld.setBlock will limit it
		// correctly based on what chunks can be accessed
		x = readInt(args.get(0), -100, 100);
		y = readInt(args.get(1), -1000, 1000);
		z = readInt(args.get(2), -100, 100);
		processEntityName(args.get(3), logger);
		groupSize = readInt(args.get(4), 0, Integer.MAX_VALUE);

		if(args.size() > 5)
		{
			processNameTagOrFileName(args.get(5), logger);
		}
	}

	public void processEntityName(String name, ILogger logger)
	{
		// When loading from file, it will contain either a mob name or a resource location.
		// If a mob name, we get the mob's vanilla resource location
		// If a resource location, we store it and extract a mob name from it
		if (name == null)
		{
			return;
		}
		if (name.contains(":"))
		{
			resourceLocation = name.toLowerCase().trim();
		} else {
			resourceLocation = EntityNames.toInternalName(name);
			if (!resourceLocation.contains(":"))
			{
				logger.log(LogMarker.ERROR, "Could not find entity '"+name+"', are you sure you spelled it correctly?");
			}
		}
		this.name = resourceLocation.split(":")[1];
	}

	public void processNameTagOrFileName(String s, ILogger logger)
	{
		originalNameTagOrNBTFileName = s;

		if(originalNameTagOrNBTFileName != null && originalNameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt"))
		{
			nameTagOrNBTFileName = getHolder().getFile().getParentFile().getAbsolutePath() + File.separator + originalNameTagOrNBTFileName;
		}
		else if(originalNameTagOrNBTFileName != null && originalNameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt"))
		{
			nameTagOrNBTFileName = getHolder().getFile().getParentFile().getAbsolutePath() + File.separator + originalNameTagOrNBTFileName;
			if (namedBinaryTag == null)
			{
				// load NBT data from .nbt file
				try {
					FileInputStream stream = new FileInputStream(nameTagOrNBTFileName);
					namedBinaryTag = NamedBinaryTag.readFrom(stream, true);
				} catch (FileNotFoundException e) {
					if(logger.getSpawnLogEnabled())
					{
						logger.log(LogMarker.WARN, "Could not find file: "+nameTagOrNBTFileName);
					}
					// Set it to null so we don't go looking for this later
					nameTagOrNBTFileName = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			// It's a name tag
			nameTagOrNBTFileName = originalNameTagOrNBTFileName;
		}
	}

	@Override
	public String makeString()
	{
		return "Entity(" + x + ',' + y + ',' + z + ',' + resourceLocation + ',' + groupSize + (originalNameTagOrNBTFileName != null && originalNameTagOrNBTFileName.length() > 0 ? ',' + originalNameTagOrNBTFileName : "") + ')';
	}

	private String metaDataTag;
	public String getMetaData()
	{
		if(nameTagOrNBTFileName != null && nameTagOrNBTFileName.length() > 0 && metaDataTag == null)
		{
			File metaDataFile = new File(nameTagOrNBTFileName);
			StringBuilder stringbuilder = new StringBuilder();
			if(metaDataFile.exists())
			{
				try {
					BufferedReader reader = new BufferedReader(new FileReader(metaDataFile));
					try {
						String line = reader.readLine();

						while (line != null) {
							stringbuilder.append(line);
							//sb.append(System.lineSeparator());
							line = reader.readLine();
						}
					} finally {
						reader.close();
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			metaDataTag = stringbuilder.toString();
		}
		return metaDataTag;
	}

	@Override
	public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
	{
		if(!getClass().equals(other.getClass()))
		{
			return false;
		}
		EntityFunction<T> block = (EntityFunction<T>) other;
		return block.x == x && block.y == y && block.z == z && block.resourceLocation.equalsIgnoreCase(resourceLocation) && block.groupSize == groupSize && block.originalNameTagOrNBTFileName.equalsIgnoreCase(originalNameTagOrNBTFileName);
	}

	public abstract EntityFunction<T> createNewInstance();
}
