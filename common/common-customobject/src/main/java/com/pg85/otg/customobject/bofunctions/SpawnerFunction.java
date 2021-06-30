package com.pg85.otg.customobject.bofunctions;

import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogCategory;
import com.pg85.otg.logging.LogLevel;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Represents a block in a BO3.
 */
public abstract class SpawnerFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T>
{
	public int y;

	public Boolean firstSpawn = true;
	public String mobName = "";
	public String nbtFileName = "";
	public String originalnbtFileName = "";
	public int groupSize = 1;
	public int interval = 40;
	public int intervalOffset = 0;
	public int spawnChance = 100;
	public int maxCount = 0;

	public int despawnTime = 0;

	public double velocityX = 0;
	public double velocityY = 0;
	public double velocityZ = 0;

	public float yaw = 0.0F;
	public float pitch = 0.0F;

	public boolean velocityXSet = false;
	public boolean velocityYSet = false;
	public boolean velocityZSet = false;

	protected String metaDataTag;
	protected boolean metaDataProcessed = false;
	
	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		assureSize(8, args);
		// Those limits are arbitrary, LocalWorld.setBlock will limit it
		// correctly based on what chunks can be accessed
		x = readInt(args.get(0), -100, 100);
		y = readInt(args.get(1), -1000, 1000);
		z = readInt(args.get(2), -100, 100);
		mobName = args.get(3);

		boolean param4isNBT = false;
		try
		{
			groupSize = (int) Double.parseDouble(args.get(4));
		}
		catch(NumberFormatException ex)
		{
			// TODO: Get relative path or saves can't be copied to a different location
			originalnbtFileName = args.get(4);
			param4isNBT = true;
		}

		if(originalnbtFileName != null && originalnbtFileName.toLowerCase().trim().endsWith(".txt"))
		{
			nbtFileName = getHolder().getFile().getParentFile().getAbsolutePath() + File.separator + originalnbtFileName;
		}

		if(param4isNBT)
		{
			groupSize = readInt(args.get(5), 0, Integer.MAX_VALUE);
		}

		if(!param4isNBT)
		{
			interval = readInt(args.get(5), 0, Integer.MAX_VALUE);
		} else {
			interval = readInt(args.get(6), 0, Integer.MAX_VALUE);
		}
		if(!param4isNBT)
		{
			spawnChance = readInt(args.get(6), 0, Integer.MAX_VALUE);
		} else {
			spawnChance = readInt(args.get(7), 0, Integer.MAX_VALUE);
		}
		if(!param4isNBT)
		{
			maxCount = readInt(args.get(7), 0, Integer.MAX_VALUE);
		} else {
			maxCount = readInt(args.get(8), 0, Integer.MAX_VALUE);
		}

		if(!param4isNBT)
		{
			if(args.size() > 8)
			{
				despawnTime = readInt(args.get(8), 0, Integer.MAX_VALUE);
			}
		} else {
			if(args.size() > 9)
			{
				despawnTime = readInt(args.get(9), 0, Integer.MAX_VALUE);
			}
		}
		if(!param4isNBT)
		{
			if(args.size() > 9)
			{
				velocityX = readDouble(args.get(9), Integer.MIN_VALUE, Integer.MAX_VALUE);
				velocityXSet = true;
			}
		} else {
			if(args.size() > 10)
			{
				velocityX = readDouble(args.get(10), Integer.MIN_VALUE, Integer.MAX_VALUE);
				velocityXSet = true;
			}
		}
		if(!param4isNBT)
		{
			if(args.size() > 10)
			{
				velocityY = readDouble(args.get(10), Integer.MIN_VALUE, Integer.MAX_VALUE);
				velocityYSet = true;
			}
		} else {
			if(args.size() > 11)
			{
				velocityY = readDouble(args.get(11), Integer.MIN_VALUE, Integer.MAX_VALUE);
				velocityYSet = true;
			}
		}
		if(!param4isNBT)
		{
			if(args.size() > 11)
			{
				velocityZ = readDouble(args.get(11), Integer.MIN_VALUE, Integer.MAX_VALUE);
				velocityZSet = true;
			}
		} else {
			if(args.size() > 12)
			{
				velocityZ = readDouble(args.get(12), Integer.MIN_VALUE, Integer.MAX_VALUE);
				velocityZSet = true;
			}
		}
		if(!param4isNBT)
		{
			if(args.size() > 12)
			{
				yaw = (float)readDouble(args.get(12), 0, Integer.MAX_VALUE);
			}
		} else {
			if(args.size() > 13)
			{
				yaw = (float)readDouble(args.get(13), 0, Integer.MAX_VALUE);
			}
		}
		if(!param4isNBT)
		{
			if(args.size() > 13)
			{
				this.pitch = (float)readDouble(args.get(13), 0, Integer.MAX_VALUE);
			}
		} else {
			if(args.size() > 14)
			{
				this.pitch = (float)readDouble(args.get(14), 0, Integer.MAX_VALUE);
			}
		}
	}

	public String getMetaData(ILogger logger)
	{
		if(this.nbtFileName != null && this.nbtFileName.length() > 0 && this.metaDataTag == null && !this.metaDataProcessed)
		{
			this.metaDataProcessed = true;
			File metaDataFile = new File(this.nbtFileName);
			StringBuilder stringbuilder = new StringBuilder();
			if(metaDataFile.exists())
			{
				try {
					BufferedReader reader = new BufferedReader(new FileReader(metaDataFile));
					try {
						String line = reader.readLine();

						while (line != null)
						{
							stringbuilder.append(line);
							line = reader.readLine();
						}
					} finally {
						reader.close();
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not find file \"" + this.nbtFileName  + "\" for Spawner: " + this.makeString());
				}
			}

			this.metaDataTag = stringbuilder.length() > 0 ? stringbuilder.toString() : null;
		}
		
		this.metaDataProcessed = true;		
		return this.metaDataTag;
	}

	@Override
	public String makeString()
	{
		return "Spawner(" + x + ',' + y + ',' + z + ',' + this.mobName + (this.originalnbtFileName != null && this.originalnbtFileName.length() > 0 ? "," + this.originalnbtFileName : "") + ',' + this.groupSize + ',' + this.interval + ',' + this.spawnChance + ',' + this.maxCount + ',' + this.despawnTime + ',' + this.velocityX + ',' + this.velocityY + ',' + this.velocityZ + ',' + this.yaw + ',' + this.pitch + ')';
	}

	@Override
	public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
	{
		if(!getClass().equals(other.getClass()))
		{
			return false;
		}
		SpawnerFunction<T> block = (SpawnerFunction<T>) other;
		return block.x == this.x && block.y == this.y && block.z == this.z && block.mobName.equalsIgnoreCase(this.mobName) && block.originalnbtFileName.equalsIgnoreCase(this.originalnbtFileName) && block.groupSize == this.groupSize && block.interval == this.interval && block.spawnChance == this.spawnChance && block.maxCount == this.maxCount && block.despawnTime == this.despawnTime && block.velocityX == this.velocityX && block.velocityY == this.velocityY && block.velocityZ == this.velocityZ && block.yaw == this.yaw && block.pitch == this.pitch;
	}
	
	public abstract SpawnerFunction<T> getNewInstance();
}
