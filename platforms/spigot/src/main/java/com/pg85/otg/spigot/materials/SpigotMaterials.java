package com.pg85.otg.spigot.materials;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

public class SpigotMaterials extends LocalMaterials
{
	private static final FifoMap<String, LocalMaterialData> CachedMaterials = new FifoMap<>(4096); // TODO: Smaller cache should be ok, only most frequently used should be cached?

	static LocalMaterialData readMaterial (String name) throws InvalidConfigException
	{
		if (name == null)
		{
			return null;
		}

		LocalMaterialData material = CachedMaterials.get(name);
		if (material != null)
		{
			return material;
		}
		else if (CachedMaterials.containsKey(name))
		{
			throw new InvalidConfigException("Cannot read block: " + name);
		}

		try
		{
			material = SpigotMaterialData.ofString(name);
		}
		catch (InvalidConfigException ex)
		{
			// Happens when a non existing block name is used.
			String breakpoint = "";
		}

		CachedMaterials.put(name, material);

		return material;
	}
}
