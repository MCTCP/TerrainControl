package com.pg85.otg.util.bo3;

import java.util.Random;

import com.pg85.otg.exceptions.InvalidConfigException;

/**
 * An enum to help with CustomObject rotation.
 *
 */
public enum Rotation
{
	NORTH(0),
	WEST(1),
	SOUTH(2),
	EAST(3);

	private final int ROTATION_ID;

	private Rotation(int id)
	{
		this.ROTATION_ID = id;
	}

	/**
	 * Returns the id of the rotation. Can be 0, 1, 2 or 3.
	 * 
	 * @return The id of the rotation.
	 */
	public int getRotationId()
	{
		return ROTATION_ID;
	}

	public static Rotation FromString(String rotation)
	{
		if(rotation.toLowerCase().equals("north"))
		{
			return NORTH;
		}
		if(rotation.toLowerCase().equals("east"))
		{
			return EAST;
		}
		if(rotation.toLowerCase().equals("south"))
		{
			return SOUTH;
		}
		if(rotation.toLowerCase().equals("west"))
		{
			return WEST;
		} else {
			return WEST; // WEST is the default
		}		
	}	 
	
	/**
	 * Get the rotation with the given id. Returns null if the
	 * rotation id isn't found.
	 * 
	 * @param id The rotation id.
	 * @return The rotation with the given id, or null if it isn't found.
	 */
	public static Rotation getRotation(int id)
	{
		for (Rotation rotation : values())
		{
			if (rotation.ROTATION_ID == id)
			{
				return rotation;
			}
		}

		return null;
	}

	/**
	 * Returns a random rotation.
	 * 
	 * @param random The random number generator.
	 * @return One of the four directions.
	 */
	public static Rotation getRandomRotation(Random random)
	{
		return values()[random.nextInt(values().length)];
	}

	/**
	 * Returns the next rotation. NORTH -> WEST -> SOUTH -> EAST
	 * @return The next rotation.
	 */
	public Rotation next()
	{
		int id = getRotationId();
		id++;
		if (id >= values().length)
		{
			id = 0;
		}

		return Rotation.getRotation(id);
	}

	public static Rotation getRotation(String string) throws InvalidConfigException
	{
		Rotation rotation = null;
		// Try to parse it as a number
		try
		{
			rotation = getRotation(Integer.parseInt(string));
		} catch (NumberFormatException e)
		{
		}

		if (rotation != null)
		{
			return rotation;
		}

		// Try to parse it as a String
		try
		{
			rotation = Rotation.valueOf(string.toUpperCase());
		} catch (IllegalArgumentException e)
		{
		}

		if (rotation != null)
		{
			return rotation;
		}

		// Failed
		throw new InvalidConfigException("Unknown rotation: " + string);
	}
}
