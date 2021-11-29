package com.pg85.otg.config.biome;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.BiomeRegistryNames;

import java.text.MessageFormat;
import java.util.*;

/**
 * Biomes are spawned in groups so that biomes of the same type are near each
 * other, and completely different biomes (desert vs taiga) don't spawn next
 * to each other.
 *
 * <p>This class represents such a biome group.
 */
public final class BiomeGroup extends ConfigFunction<IWorldConfig>
{
	private int groupId;
	private String name;
	private int groupRarity;
	private int generationDepth = 0;
	private double minTemp = 0;
	private double maxTemp = 0;
	private final List<String> biomes = new ArrayList<String>();

	/**
	 * Loads the biome group using the provided settings.
	 * @param config The world config.
	 * @param args	The settings to be parsed.
	 * @throws InvalidConfigException When the config is invalid.
	 * @see #BiomeGroup(IWorldConfig, String, int, int, List) Constructor to
	 * properly initialize this biome group manually.
	 */
	public BiomeGroup(IWorldConfig config, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		// Must have at least a GroupName and a Biome that belongs to it
		assureSize(4, args);
		this.name = args.get(0);
		this.generationDepth = readInt(args.get(1), 0, config.getGenerationDepth());
		this.groupRarity = readInt(args.get(2), 1, Integer.MAX_VALUE);
		
		try
		{
			this.minTemp = readDouble(args.get(args.size() - 2), Integer.MIN_VALUE, Integer.MAX_VALUE);
			this.maxTemp = readDouble(args.get(args.size() - 1), Integer.MIN_VALUE, Integer.MAX_VALUE);
			args = args.subList(0, args.size() - 2);
		}
		catch(InvalidConfigException ex)
		{
			this.minTemp = 0;
			this.maxTemp = 0;
		}
		
		for (String biome : readBiomes(args, 3))
		{
			this.biomes.add(biome);
		}
	}

	/**
	 * Creates a new <code>BiomeGroup</code>.
	 * @param config	WorldConfig this biome group is part of.
	 * @param groupName The name of this group.
	 * @param size	  Size value of this biome group.
	 * @param rarity	Rarity value of this biome group.
	 * @param biomes	List of names of the biomes that spawn in this group.
	 */
	public BiomeGroup(IWorldConfig config, String groupName, int size, int rarity, List<String> biomes)
	{
		this.name = groupName;
		this.generationDepth = size;
		this.groupRarity = rarity;
		this.minTemp = 0;
		this.maxTemp = 0;		
		for (String biome : biomes)
		{
			this.biomes.add(biome);
		}
	}

	public List<String> getBiomes()
	{
		return this.biomes;
	}

	public boolean temperatureAllowed(float baseTemperature)
	{
		if(this.minTemp != 0 || this.maxTemp != 0)
		{
			return baseTemperature >= this.minTemp && baseTemperature <= this.maxTemp;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return "BiomeGroup(" + name + ", " + generationDepth + ", " + groupRarity + ", " + StringHelper.join(biomes, ", ") + (this.minTemp == 0 ? "" : ", " + this.minTemp)  + (this.maxTemp == 0 ? "" : ", " + this.maxTemp) + ")";
	}

	/**
	 * Reads all biomes from the start position until the end of the
	 * list.
	 * @param strings The input strings.
	 * @param start	The position to start. The first element in the list has index 0, the last one size() - 1.
	 * @return All biome names.
	 * @throws InvalidConfigException If one of the elements in the list is not a valid block id.
	 */
	private List<String> readBiomes(List<String> strings, int start) throws InvalidConfigException
	{
		return new ArrayList<String>(strings.subList(start, strings.size()));
	}

	/**
	 * Gets the name of this biome group.
	 * @return The name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Filters the biomes in this group, removing all biomes that have an
	 * unrecognized name.
	 * @param customBiomeNames Set of known custom biomes.
	 */
	void filterBiomes(ArrayList<String> customBiomeNames, ILogger logger)
	{
		for (Iterator<String> it = this.biomes.iterator(); it.hasNext();)
		{
			String biomeName = it.next();
			if(biomeName != null && biomeName.trim().length() > 0)
			{
				if (
					BiomeRegistryNames.Contain(biomeName) || 
					customBiomeNames.contains(biomeName) ||
					biomeName.contains(":") || // Non-otg biome registry name
					biomeName.toLowerCase().startsWith(Constants.MOD_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.MOD_BIOME_CATEGORY_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL) ||					
					biomeName.toLowerCase().startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) ||
					biomeName.toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL)
				)
				{
					continue;
				}
				// Invalid biome name, remove
				if(logger.getLogCategoryEnabled(LogCategory.CONFIGS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CONFIGS, MessageFormat.format("Invalid biome name {0} in biome group {1}", biomeName, this.name));
				}
			}
			it.remove();
		}
	}

	/**
	 * Gets whether this group contains the given biome. Biome name is case
	 * sensitive.
	 * @param name Name of the biome.
	 * @return True if this group contains the given biome, false otherwise.
	 */
	public boolean containsBiome(String name)
	{
		return this.biomes.contains(name);
	}

	/**
	 * Sets the group id to the given value. Don't use this if the group is
	 * already registered in a collection.
	 * @param groupId The new group id.
	 * @throws IllegalArgumentException If the group id is larger than
	 * {@link BiomeGroupManager#MAX_BIOME_GROUP_COUNT}.
	 */
	void setGroupId(int groupId)
	{
		if (groupId > BiomeGroupManager.MAX_BIOME_GROUP_COUNT)
		{
			throw new IllegalArgumentException("Tried to set group id to " + groupId
					+ ", max allowed is " + BiomeGroupManager.MAX_BIOME_GROUP_COUNT);
		}

		this.groupId = groupId;
	}

	/**
	 * Gets the numerical id for this group. Group ids are sequential and
	 * based on the order they are placed in the configuration files.
	 * @return The numerical id.
	 */
	public int getGroupId()
	{
		return this.groupId;
	}

	public int getGroupRarity()
	{
		return groupRarity;
	}

	public int getGenerationDepth()
	{
		return generationDepth;
	}

	/**
	 * Gets whether this group has any biomes.
	 * @return True if the group has no biomes and is thus empty, false
	 * if the group has biomes.
	 */
	boolean hasNoBiomes()
	{
		return biomes.isEmpty();
	}
}
