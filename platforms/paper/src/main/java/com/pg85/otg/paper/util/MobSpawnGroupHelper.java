package com.pg85.otg.paper.util;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.util.biome.WeightedMobSpawnGroup;

import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

public class MobSpawnGroupHelper
{
	
	public static List<WeightedMobSpawnGroup> getListFromMinecraftBiome(Biome biome, MobCategory type)
	{
		WeightedRandomList<SpawnerData> mobList = biome.getMobSettings().getMobs(type);
		List<WeightedMobSpawnGroup> result = new ArrayList<WeightedMobSpawnGroup>();
		for (SpawnerData spawner : mobList.unwrap())
		{
			// Removing "entities/" since the key returned is "minecraft:entities/chicken" for vanilla biomes/mobs.
			// TODO: Make sure this works for all mobs.
			WeightedMobSpawnGroup wMSG = new WeightedMobSpawnGroup(spawner.type.getDescriptionId().toString().replace("entities/", ""), spawner.getWeight().asInt(), spawner.minCount, spawner.maxCount);
			if(wMSG != null)
			{
				result.add(wMSG);
			}
		}
		return result;
	}
}
