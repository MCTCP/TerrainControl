package com.pg85.otg.customobject.bofunctions;

import java.util.List;
import java.util.Random;

import com.pg85.otg.customobject.bo3.BO3Loader;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.materials.LocalMaterialData;

/**
 * Represents a block in a BO3.
 */
public abstract class BlockFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T>
{
	public LocalMaterialData material;
	public short y;
	public NamedBinaryTag nbt;
	public String nbtName;

	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		assureSize(4, args);
		// Those limits are arbitrary, LocalWorld.setBlock will limit it
		// correctly based on what chunks can be accessed
		x = readInt(args.get(0), -100, 100);
		y = (short) readInt(args.get(1), -1000, 1000);
		z = readInt(args.get(2), -100, 100);

		material = readMaterial(args.get(3), materialReader);

		if(material == null)
		{
			throw new InvalidConfigException("Material \"" + args.get(3) + "\" could not be read.");
		}

		if (args.size() == 5)
		{
			nbt = BO3Loader.loadMetadata(args.get(4), getHolder().getFile(), logger);
			if (nbt != null)
			{
				nbtName = args.get(4);
			}
		}
	}

	@Override
	public String makeString()
	{
		String start = "B(" + x + ',' + y + ',' + z + ',' + material;
		if (nbt != null)
		{
			start += ',' + nbtName;
		}
		return start + ')';
	}

	public abstract void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z);
	
	public abstract void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, ReplaceBlockMatrix replaceBlocks);
	
	@Override
	public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
	{
		if (!getClass().equals(other.getClass()))
		{
			return false;
		}
		BlockFunction<T> block = (BlockFunction<T>) other;
		return block.x == x && block.y == y && block.z == z;
	}
}