package com.pg85.otg.customobject.bo3.checks;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;

public class BlockCheckAbsolute extends BO3Check
{
	MaterialSet toCheck;

	@Override
	public boolean preventsSpawn(IWorldGenRegion worldGenregion, int x, int y, int z)
	{
		return !this.toCheck.contains(worldGenregion.getMaterial(x, this.y, z));
	}

	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		assureSize(4, args);
		this.x = readInt(args.get(0), -100, 100);
		this.y = readInt(args.get(1), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT);
		this.z = readInt(args.get(2), -100, 100);
		this.toCheck = readMaterials(args, 3, materialReader);
	}

	// The normal constructor, as used by CustomObjectConfigFunction::create
	public BlockCheckAbsolute() {}

	// A specific constructor used when converting BO2's to BO3's
	public BlockCheckAbsolute (int x, int y, int z, MaterialSet toCheck)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.toCheck = toCheck;
	}

	@Override
	public String makeString()
	{
		return makeString("BlockCheckAbsolute");
	}

	/**
	 * Gets the string representation with the given check name.
	 *
	 * @param name Name of the check, like BlockCheck.
	 * @return The string representation.
	 */
	protected String makeString(String name)
	{
		return name + '(' + x + ',' + y + ',' + z + makeMaterials(toCheck) + ')';
	}

	@Override
	public BO3Check rotate()
	{
		BlockCheckAbsolute rotatedCheck = new BlockCheckAbsolute();
		rotatedCheck.x = z;
		rotatedCheck.y = y;
		rotatedCheck.z = -x;
		rotatedCheck.toCheck = this.toCheck.rotate();
		return rotatedCheck;
	}
	
	@Override
	public Class<BO3Config> getHolderType()
	{
		return BO3Config.class;
	}
}
