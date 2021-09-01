package com.pg85.otg.paper.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.util.biome.WeightedMobSpawnGroup;

import net.minecraft.server.v1_17_R1.BiomeBase;
import net.minecraft.server.v1_17_R1.EnumCreatureType;
import net.minecraft.server.v1_17_R1.WeightedRandom.WeightedRandomChoice;
import net.minecraft.server.v1_17_R1.BiomeSettingsMobs.c;

public class MobSpawnGroupHelper
{
	private static final Field WEIGHT_FIELD;

	static
	{
		try
		{
			WEIGHT_FIELD = WeightedRandomChoice.class.getDeclaredField("a");
			WEIGHT_FIELD.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Reflection error", e);
		}
	}
	
	public static List<WeightedMobSpawnGroup> getListFromMinecraftBiome(BiomeBase biome, EnumCreatureType type)
	{
		List<c> mobList = biome.b().a(type);		
		List<WeightedMobSpawnGroup> result = new ArrayList<WeightedMobSpawnGroup>();
		for (c spawner : mobList)
		{
			// Removing "entities/" since the key returned is "minecraft:entities/chicken" for vanilla biomes/mobs.
			// TODO: Make sure this works for all mobs.
			WeightedMobSpawnGroup wMSG = new WeightedMobSpawnGroup(spawner.c.i().toString().replace("entities/", ""), getWeight(spawner), spawner.d, spawner.e);
			if(wMSG != null)
			{
				result.add(wMSG);
			}
		}
		return result;
	}
	
	/**
	 * For some reason, the weight field in the BiomeMeta class is protected
	 * and has no getter. This method uses reflection to get around that.
	 */
	public static int getWeight(net.minecraft.server.v1_17_R1.BiomeSettingsMobs.c biomeMeta)
	{
		try
		{
			return WEIGHT_FIELD.getInt(biomeMeta);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
