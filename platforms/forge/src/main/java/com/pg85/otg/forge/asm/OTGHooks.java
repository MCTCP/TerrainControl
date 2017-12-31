package com.pg85.otg.forge.asm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

public class OTGHooks
{
	public static int getIDForObject(Biome biome)
	{
		//System.out.println("getIDForObject");
		return ((IOTGASMBiome)biome).getSavedId();
	}

	public static int countMissingRegistryEntries(LinkedHashMap<ResourceLocation, Map<ResourceLocation, Integer>> missing)
	{
		//System.out.println("countMissingRegistryEntries");

		// Exclude OTG Biomes.
		//int otgBiomesCount = 0;
		if(missing.containsKey(new ResourceLocation("minecraft", "biomes")))
		{
			Gson gson = new Gson();
			ArrayList<ResourceLocation> biomesToRemove = new ArrayList();
			Map<ResourceLocation, Integer> biomes = missing.get(new ResourceLocation("minecraft", "biomes"));
			for(ResourceLocation biomeResourceLocation : biomes.keySet())
			{
				if(biomeResourceLocation != null)
				{
					// Can't use biomeResourceLocation.getResourceDomain()
					String jsonInString = gson.toJson(biomeResourceLocation);
					if(jsonInString.contains(":\"openterraingenerator\","))
					{
						//System.out.println("OTG Biome found: \"" + jsonInString + "\". otgBiomesCount: " + otgBiomesCount);
						//otgBiomesCount++;
						biomesToRemove.add(biomeResourceLocation);
					} else {
						//System.out.println("Non-OTG Biome found: " + jsonInString);
					}
				}
			}
			for(ResourceLocation biomeResourceLocation : biomesToRemove)
			{
				biomes.remove(biomeResourceLocation);
			}
		}

		int count = missing.values().stream().mapToInt(Map::size).sum();//) - otgBiomesCount;

		if(count > 0)
		{
			System.out.println("Items/Blocks/Biomes appear to be missing from the registry. Forge will show an error message about this and you will not be able to join the world. Forge will display a list of missing registry entries, you can ignore any otg biomes on that list as they do not actually need to be registered. The missing registries error is caused by other mods, not OTG.");
		}
		return count;
	}
}
