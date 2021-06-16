package com.pg85.otg.customobject.bofunctions;

import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.minecraft.DefaultStructurePart;

import java.util.List;
import java.util.Random;

/**
 * Represents a block in a BO3.
 */
public abstract class MinecraftObjectFunction<T extends CustomObjectConfigFile> extends BlockFunction<T>
{
	private DefaultStructurePart structurePart;
	protected Rotation rotation = Rotation.NORTH;

	public MinecraftObjectFunction()
	{
		super();
	}

	public MinecraftObjectFunction(BO3Config config, List<String> args) throws InvalidConfigException
	{
		assureSize(4, args);
		// Those limits are arbitrary, LocalWorld.setBlock will limit it
		// correctly based on what chunks can be accessed
		x = readInt(args.get(0), -100, 100);
		y = (short) readInt(args.get(1), -1000, 1000);
		z = readInt(args.get(2), -100, 100);
		structurePart = DefaultStructurePart.getDefaultStructurePart(args.get(3));
	}

	@Override
	public String makeString()
	{
		return "MinecraftObject(" + x + ',' + y + ',' + z + ',' + structurePart + ')';
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, ChunkCoordinate chunkBeingDecorated, boolean replaceBlocks)
	{
		// TODO: May cause cascading chunkgen.
		// TODO: Implement this?
		//SpawnableObject object = worldGenRegion.getMojangStructurePart(structurePart.getPath());
		//object.spawnForced(null, worldGenRegion, random, rotation, x, y, z);
	}

	@Override
	public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
	{
		if(!getClass().equals(other.getClass()))
		{
			return false;
		}
		MinecraftObjectFunction<T> block = (MinecraftObjectFunction<T>) other;
		return block.x == x && block.y == y && block.z == z;
	}
}
