package com.pg85.otg.customobject.bofunctions;

import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IEntityFunction;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.EntityNames;

import java.io.*;
import java.util.List;

/**
 * Represents an entity in a BO3.
 */
public abstract class EntityFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T> implements IEntityFunction
{
	public int y;
	public String name = "";
	public int groupSize = 1;
	public String nameTagOrNBTFileName = "";
	public String originalNameTagOrNBTFileName = "";
	public String resourceLocation = "";
	public NamedBinaryTag namedBinaryTag = null;
	public int rotation = 0;
	private String metaDataTag;
	
	@Override
	public double getX()
	{
		return this.x;
	}
	@Override
	public int getY()
	{
		return this.y;
	}	
	@Override
	public double getZ()
	{
		return this.z;
	}	
	@Override
	public int getGroupSize()
	{
		return this.groupSize;
	}	
	@Override
	public String getNameTagOrNBTFileName()
	{
		return this.nameTagOrNBTFileName;
	}	
	@Override
	public String getResourceLocation()
	{
		return this.resourceLocation;
	}
	@Override
	public NamedBinaryTag getNBTTag()
	{
		return this.namedBinaryTag;
	}
	
	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		assureSize(5, args);
		// Those limits are arbitrary, LocalWorld.setBlock will limit it
		// correctly based on what chunks can be accessed
		this.x = readInt(args.get(0), -100, 100);
		this.y = readInt(args.get(1), -1000, 1000);
		this.z = readInt(args.get(2), -100, 100);
		processEntityName(args.get(3), logger);
		this.groupSize = readInt(args.get(4), 0, Integer.MAX_VALUE);

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
			this.resourceLocation = name.toLowerCase().trim();
		} else {
			this.resourceLocation = EntityNames.toInternalName(name);
			if (!this.resourceLocation.contains(":"))
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not find entity '" + name + "', are you sure you spelled it correctly?");
				}
			}
		}
		this.name = this.resourceLocation.split(":")[1];
	}

	public void processNameTagOrFileName(String s, ILogger logger)
	{
		this.originalNameTagOrNBTFileName = s;

		if(this.originalNameTagOrNBTFileName != null && this.originalNameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt"))
		{
			this.nameTagOrNBTFileName = getHolder().getFile().getParentFile().getAbsolutePath() + File.separator + this.originalNameTagOrNBTFileName;
		}
		else if(this.originalNameTagOrNBTFileName != null && this.originalNameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt"))
		{
			this.nameTagOrNBTFileName = getHolder().getFile().getParentFile().getAbsolutePath() + File.separator + this.originalNameTagOrNBTFileName;
			if (this.namedBinaryTag == null)
			{
				// load NBT data from .nbt file
				try {
					FileInputStream stream = new FileInputStream(this.nameTagOrNBTFileName);
					this.namedBinaryTag = NamedBinaryTag.readFrom(stream, true);
				} catch (FileNotFoundException e) {
					if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not find file: " + this.nameTagOrNBTFileName);
					}
					// Set it to null so we don't go looking for this later
					this.nameTagOrNBTFileName = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			// It's a name tag
			this.nameTagOrNBTFileName = this.originalNameTagOrNBTFileName;
		}
	}

	@Override
	public String makeString()
	{
		return "Entity(" + x + ',' + y + ',' + z + ',' + this.resourceLocation + ',' + this.groupSize + (this.originalNameTagOrNBTFileName != null && this.originalNameTagOrNBTFileName.length() > 0 ? ',' + this.originalNameTagOrNBTFileName : "") + ')';
	}

	public String getMetaData()
	{
		if(this.nameTagOrNBTFileName != null && this.nameTagOrNBTFileName.length() > 0 && this.metaDataTag == null)
		{
			File metaDataFile = new File(this.nameTagOrNBTFileName);
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

			this.metaDataTag = stringbuilder.toString();
		}
		return this.metaDataTag;
	}

	@Override
	public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
	{
		if(!getClass().equals(other.getClass()))
		{
			return false;
		}
		EntityFunction<T> block = (EntityFunction<T>) other;
		return block.x == this.x && block.y == this.y && block.z == this.z && block.resourceLocation.equalsIgnoreCase(this.resourceLocation) && block.groupSize == this.groupSize && block.originalNameTagOrNBTFileName.equalsIgnoreCase(this.originalNameTagOrNBTFileName);
	}

	public abstract EntityFunction<T> createNewInstance();
}
