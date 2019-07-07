package com.pg85.otg.util.minecraft.defaults;

public class BiomeRegistryNames
{
	public static String getRegistryNameForDefaultBiome(String biomeName)
	{
		String registryName = null;
		switch(biomeName)
		{
			// TODO: Always keep this biome list up to date
			// TODO: Put everything that needs to be updated per MC version in one place?
			case "Beach":
				registryName = "minecraft:beaches";
			break;
			case "Birch Forest Hills M":
				registryName = "minecraft:mutated_birch_forest_hills";
			break;
			case "Birch Forest Hills":
				registryName = "minecraft:birch_forest_hills";
			break;
			case "Birch Forest M":
				registryName = "minecraft:mutated_birch_forest";
			break;
			case "Birch Forest":
				registryName = "minecraft:birch_forest";
			break;
			case "Cold Beach":
				registryName = "minecraft:cold_beach";
			break;
			case "Cold Taiga Hills":
				registryName = "minecraft:taiga_cold_hills";
			break;
			case "Cold Taiga M":
				registryName = "minecraft:mutated_taiga_cold";
			break;
			case "Cold Taiga":
				registryName = "minecraft:taiga_cold";
			break;
			case "Deep Ocean":
				registryName = "minecraft:deep_ocean";
			break;
			case "Desert M":
				registryName = "minecraft:mutated_desert";
			break;
			case "Desert":
				registryName = "minecraft:desert";
			break;
			case "DesertHills":
				registryName = "minecraft:desert_hills";
			break;
			case "Extreme Hills Edge":
				registryName = "minecraft:smaller_extreme_hills";
			break;
			case "Extreme Hills M":
				registryName = "minecraft:mutated_extreme_hills";
			break;
			case "Extreme Hills":
				registryName = "minecraft:extreme_hills";
			break;
			case "Extreme Hills+ M":
				registryName = "minecraft:mutated_extreme_hills_with_trees";
			break;
			case "Extreme Hills+":
				registryName = "minecraft:extreme_hills_with_trees";
			break;
			case "Flower Forest":
				registryName = "minecraft:mutated_forest";
			break;
			case "Forest":
				registryName = "minecraft:forest";
			break;
			case "ForestHills":
				registryName = "minecraft:forest_hills";
			break;
			case "FrozenOcean":
				registryName = "minecraft:frozen_ocean";
			break;
			case "FrozenRiver":
				registryName = "minecraft:frozen_river";
			break;
			case "Hell":
				registryName = "minecraft:hell";
			break;
			case "Ice Mountains":
				registryName = "minecraft:ice_mountains";
			break;
			case "Ice Plains Spikes":
				registryName = "minecraft:mutated_ice_flats";
			break;
			case "Ice Plains":
				registryName = "minecraft:ice_flats";
			break;
			case "Jungle M":
				registryName = "minecraft:mutated_jungle";
			break;
			case "Jungle":
				registryName = "minecraft:jungle";
			break;
			case "JungleEdge M":
				registryName = "minecraft:mutated_jungle_edge";
			break;
			case "JungleEdge":
				registryName = "minecraft:jungle_edge";
			break;
			case "JungleHills":
				registryName = "minecraft:jungle_hills";
			break;
			case "Mega Spruce Taiga Hills":
				registryName = "minecraft:mutated_redwood_taiga_hills";
			break;
			case "Mega Spruce Taiga":
				registryName = "minecraft:mutated_redwood_taiga";
			break;
			case "Mega Taiga Hills":
				registryName = "minecraft:redwood_taiga_hills";
			break;
			case "Mega Taiga":
				registryName = "minecraft:redwood_taiga";
			break;
			case "Mesa (Bryce)":
				registryName = "minecraft:mutated_mesa";
			break;
			case "Mesa Plateau F M":
				registryName = "minecraft:mutated_mesa_rock";
			break;
			case "Mesa Plateau F":
				registryName = "minecraft:mesa_rock";
			break;
			case "Mesa Plateau M":
				registryName = "minecraft:mutated_mesa_clear_rock";
			break;
			case "Mesa Plateau":
				registryName = "minecraft:mesa_clear_rock";
			break;
			case "Mesa":
				registryName = "minecraft:mesa";
			break;
			case "MushroomIsland":
				registryName = "minecraft:mushroom_island";
			break;
			case "MushroomIslandShore":
				registryName = "minecraft:mushroom_island_shore";
			break;
			case "Ocean":
				registryName = "minecraft:ocean";
			break;
			case "Plains":
				registryName = "minecraft:plains";
			break;
			case "River":
				registryName = "minecraft:river";
			break;
			case "Roofed Forest M":
				registryName = "minecraft:mutated_roofed_forest";
			break;
			case "Roofed Forest":
				registryName = "minecraft:roofed_forest";
			break;
			case "Savanna M":
				registryName = "minecraft:mutated_savanna";
			break;
			case "Savanna Plateau M":
				registryName = "minecraft:mutated_savanna_rock";
			break;
			case "Savanna Plateau":
				registryName = "minecraft:savanna_rock";
			break;
			case "Savanna":
				registryName = "minecraft:savanna";
			break;
			case "Sky":
				registryName = "minecraft:sky";
			break;
			case "Stone Beach":
				registryName = "minecraft:stone_beach";
			break;
			case "Sunflower Plains":
				registryName = "minecraft:mutated_plains";
			break;
			case "Swampland M":
				registryName = "minecraft:mutated_swampland";
			break;
			case "Swampland":
				registryName = "minecraft:swampland";
			break;
			case "Taiga M":
				registryName = "minecraft:mutated_taiga";
			break;
			case "Taiga":
				registryName = "minecraft:taiga";
			break;
			case "TaigaHills":
				registryName = "minecraft:taiga_hills";
			break;
			case "The Void":
				registryName = "minecraft:void";
			break;
		}
		return registryName;
	}
}
