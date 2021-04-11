package com.pg85.otg.util.minecraft;

// Recurring TODO: Update this when MC is updated. Last updated for 1.16.5. 
public class BiomeRegistryNames
{
	public static String getRegistryNameForDefaultBiome(String biomeName)
	{
		String registryName = null;
		switch(biomeName)
		{
			case "badlands": // Overworld 37
			case "Badlands":
			case "Mesa":
			case "minecraft:mesa":
				registryName = "minecraft:badlands";
			break;
			case "badlands_plateau": // Overworld 39
			case "Badlands Plateau":
			case "Mesa Plateau":
			case "minecraft:mesa_clear_rock":
				registryName = "minecraft:badlands_plateau";
			break;
			case "bamboo_jungle": // Overworld 168
			case "Bamboo Jungle":
				registryName = "minecraft:bamboo_jungle";
			break;				
			case "bamboo_jungle_hills": // Overworld 169
			case "Bamboo Jungle Hills":
				registryName = "minecraft:bamboo_jungle_hills";
			break;				
			case "basalt_deltas": // Nether 173
			case "Basalt Deltas":
				registryName = "minecraft:basalt_deltas";
			break;				
			case "beach": // Overworld 16
			case "Beach":
			case "minecraft:beaches":
				registryName = "minecraft:beach";
			break;
			case "birch_forest": // Overworld 27
			case "Birch Forest":
				registryName = "minecraft:birch_forest";
			break;
			case "birch_forest_hills": // Overworld 28
			case "Birch Forest Hills":
				registryName = "minecraft:birch_forest_hills";
			break;
			case "cold_ocean": // Overworld 46
			case "Cold Ocean":
				registryName = "minecraft:cold_ocean";
			break;				
			case "crimson_forest": // Nether 171
			case "Crimson Forest":
				registryName = "minecraft:crimson_forest";
			break;				
			case "dark_forest": // Overworld 29
			case "Dark Forest":
			case "Roofed Forest":
			case "minecraft:roofed_forest":
				registryName = "minecraft:dark_forest";
			break;
			case "dark_forest_hills": // Overworld 157
			case "Dark Forest Hills":
			case "Roofed Forest M":
			case "minecraft:mutated_roofed_forest":
				registryName = "minecraft:dark_forest_hills";
			break;
			case "deep_cold_ocean": // Overworld 49
			case "Deep Cold Ocean":
				registryName = "minecraft:deep_cold_ocean";
			break;
			case "deep_frozen_ocean": // Overworld 50
			case "Deep Frozen Ocean":
				registryName = "minecraft:deep_frozen_ocean";
			break;				
			case "deep_lukewarm_ocean": // Overworld 48
			case "Deep Lukewarm Ocean":
				registryName = "minecraft:deep_lukewarm_ocean";
			break;
			case "deep_ocean": // Overworld 24
			case "Deep Ocean":
				registryName = "minecraft:deep_ocean";
			break;
			case "deep_warm_ocean": // Overworld 47
			case "Deep Warm Ocean":
				registryName = "minecraft:deep_warm_ocean";
			break;
			case "desert": // Overworld 2
			case "Desert":
				registryName = "minecraft:desert";
			break;
			case "desert_hills": // Overworld 17
			case "Desert Hills":
			case "DesertHills":
				registryName = "minecraft:desert_hills";
			break;
			case "desert_lakes": // Overworld 130
			case "Desert Lakes":
			case "Desert M":
			case "minecraft:mutated_desert":
				registryName = "minecraft:desert_lakes";
			break;
			case "end_barrens": // End 43
			case "End Barrens":
				registryName = "minecraft:end_barrens";
			break;
			case "end_highlands": // End 42
			case "End Highlands":
				registryName = "minecraft:end_highlands";
			break;
			case "end_midlands": // End 41
			case "End Midlands":
				registryName = "minecraft:end_midlands";
			break;
			case "eroded_badlands": // Overworld 165
			case "Eroded Badlands":
			case "Mesa (Bryce)":
			case "minecraft:mutated_mesa":
				registryName = "minecraft:eroded_badlands";
			break;
			case "flower_forest": // Overworld 132
			case "Flower Forest":
			case "minecraft:mutated_forest":
				registryName = "minecraft:flower_forest";
			break;
			case "forest": // Overworld 4
			case "Forest":
				registryName = "minecraft:forest";
			break;
			case "frozen_ocean": // Overworld 10
			case "FrozenOcean":
			case "Frozen Ocean":
				registryName = "minecraft:frozen_ocean";
			break;
			case "frozen_river": // Overworld 11
			case "FrozenRiver":
			case "Frozen River":
				registryName = "minecraft:frozen_river";
			break;
			case "giant_spruce_taiga": // Overworld 160
			case "Giant Spruce Taiga":
			case "Mega Spruce Taiga":
			case "minecraft:mutated_redwood_taiga":
				registryName = "minecraft:giant_spruce_taiga";
			break;
			case "giant_spruce_taiga_hills": // Overworld 161
			case "Giant Spruce Taiga Hills":
			case "Mega Spruce Taiga Hills":
			case "minecraft:mutated_redwood_taiga_hills":
				registryName = "minecraft:giant_spruce_taiga_hills";
			break;
			case "giant_tree_taiga": // Overworld 32
			case "Giant Tree Taiga":
			case "Mega Taiga":
			case "minecraft:redwood_taiga":
				registryName = "minecraft:giant_tree_taiga";
			break;
			case "giant_tree_taiga_hills": // Overworld 33
			case "Giant Tree Taiga Hills":
			case "Mega Taiga Hills":
			case "minecraft:redwood_taiga_hills":
				registryName = "minecraft:giant_tree_taiga_hills";
			break;
			case "gravelly_mountains": // Overworld 131
			case "Gravelly Mountains":
			case "Extreme Hills M":
			case "minecraft:mutated_extreme_hills":
				registryName = "minecraft:gravelly_mountains";
			break;
			case "ice_spikes": // Overworld 140
			case "Ice Spikes":
			case "Ice Plains Spikes":
			case "minecraft:mutated_ice_flats":
				registryName = "minecraft:ice_spikes";
			break;
			case "jungle": // Overworld 21
			case "Jungle":
				registryName = "minecraft:jungle";
			break;
			case "jungle_edge": // Overworld 23
			case "JungleEdge":
			case "Jungle Edge":
				registryName = "minecraft:jungle_edge";
			break;
			case "jungle_hills": // Overworld 22
			case "JungleHills":
			case "Jungle Hills":
				registryName = "minecraft:jungle_hills";
			break;
			case "lukewarm_ocean": // Overworld 45
			case "Lukewarm Ocean":
				registryName = "minecraft:lukewarm_ocean";
			break;
			case "modified_badlands_plateau": // Overworld 167
			case "Modified Badlands Plateau":
			case "Mesa Plateau M":
			case "minecraft:mutated_mesa_clear_rock":
				registryName = "minecraft:modified_badlands_plateau";
			break;
			case "modified_gravelly_mountains": // Overworld 162
			case "Gravelly Mountains+":
			case "Extreme Hills+ M":
			case "minecraft:mutated_extreme_hills_with_trees":
			case "Roofed Extreme Hills M": // TODO: This should be invalid input, don't accept?
			case "Birch Extreme Hills M": // TODO: This should be invalid input, don't accept?				
				registryName = "minecraft:modified_gravelly_mountains";
			break;
			case "modified_jungle": // Overworld 149
			case "Modified Jungle":
			case "Jungle M":
			case "minecraft:mutated_jungle":
				registryName = "minecraft:modified_jungle";
			break;
			case "modified_jungle_edge": // Overworld 151
			case "Modified Jungle Edge":
			case "JungleEdge M":
			case "minecraft:mutated_jungle_edge":
				registryName = "minecraft:modified_jungle_edge";
			break;
			case "modified_wooded_badlands_plateau": // Overworld 166
			case "Modified Wooded Badlands Plateau":
			case "Mesa Plateau F M":
			case "minecraft:mutated_mesa_rock":
				registryName = "minecraft:modified_wooded_badlands_plateau";
			break;
			case "mountain_edge": // Overworld 20
			case "Mountain Edge":
			case "Extreme Hills Edge":
			case "minecraft:smaller_extreme_hills":
				registryName = "minecraft:mountain_edge";
			break;
			case "mountains": // Overworld 3
			case "Mountains":
			case "Extreme Hills":
			case "minecraft:extreme_hills":
				registryName = "minecraft:mountains";
			break;
			case "mushroom_field_shore": // Overworld 15
			case "Mushroom Field Shore":
			case "MushroomIslandShore":
			case "minecraft:mushroom_island_shore":
				registryName = "minecraft:mushroom_field_shore";
			break;		
			case "mushroom_fields": // Overworld 14
			case "Mushroom Fields":
			case "MushroomIsland":
			case "minecraft:mushroom_island":
				registryName = "minecraft:mushroom_fields";
			break;
			case "nether_wastes": // Nether 8
			case "Nether Wastes":
			case "Hell":
			case "minecraft:hell":
				registryName = "minecraft:nether_wastes";
			break;
			case "ocean": // Overworld 0
			case "Ocean":
				registryName = "minecraft:ocean";
			break;
			case "plains": // Overworld 1
			case "Plains":
				registryName = "minecraft:plains";
			break;
			case "river": // Overworld 7
			case "River":
				registryName = "minecraft:river";
			break;
			case "savanna": // Overworld 35
			case "Savanna":
				registryName = "minecraft:savanna";
			break;
			case "savanna_plateau": // Overworld 36
			case "Savanna Plateau":
			case "minecraft:savanna_rock":
				registryName = "minecraft:savanna_plateau";
			break;
			case "shattered_savanna": // Overworld 163
			case "Shattered Savanna":
			case "Savanna M":
			case "minecraft:mutated_savanna":
				registryName = "minecraft:shattered_savanna";
			break;
			case "shattered_savanna_plateau": // Overworld 164
			case "Shattered Savanna Plateau":
			case "Savanna Plateau M":
			case "minecraft:mutated_savanna_rock":
				registryName = "minecraft:shattered_savanna_plateau";
			break;
			case "small_end_islands": // End 40
			case "Small End Islands":
				registryName = "minecraft:small_end_islands";
			break;
			case "snowy_beach": // Overworld 26
			case "Snowy Beach":
			case "Cold Beach":
			case "minecraft:cold_beach":
				registryName = "minecraft:snowy_beach";
			break;
			case "snowy_mountains": // Overworld 13
			case "Snowy Mountains":
			case "Ice Mountains":
			case "minecraft:ice_mountains":
				registryName = "minecraft:snowy_mountains";
			break;
			case "snowy_taiga": // Overworld 30
			case "Snowy Taiga":
			case "Cold Taiga":
			case "minecraft:taiga_cold":				
				registryName = "minecraft:snowy_taiga";
			break;
			case "snowy_taiga_hills": // Overworld 31
			case "Snowy Taiga Hills":
			case "Cold Taiga Hills":
			case "minecraft:taiga_cold_hills":
				registryName = "minecraft:snowy_taiga_hills";
			break;
			case "snowy_taiga_mountains": // Overworld 158
			case "Snowy Taiga Mountains":
			case "Cold Taiga M":
			case "minecraft:mutated_taiga_cold":
				registryName = "minecraft:snowy_taiga_mountains";
			break;
			case "snowy_tundra": // Overworld 12
			case "Snowy Tundra":
			case "Ice Plains":
			case "minecraft:ice_flats":
				registryName = "minecraft:snowy_tundra";
			break;
			case "soul_sand_valley": // Nether 170
			case "Soul Sand Valley":
				registryName = "minecraft:soul_sand_valley";
			break;
			case "stone_shore": // Overworld 25
			case "Stone Shore":
			case "Stone Beach":
			case "minecraft:stone_beach":
				registryName = "minecraft:stone_shore";
			break;
			case "sunflower_plains": // Overworld 129
			case "Sunflower Plains":
			case "minecraft:mutated_plains":
				registryName = "minecraft:sunflower_plains";
			break;
			case "swamp": // Overworld 6
			case "Swamp":
			case "Swampland":
			case "minecraft:swampland":
				registryName = "minecraft:swamp";
			break;
			case "swamp_hills": // Overworld 134
			case "Swamp Hills":
			case "Swampland M":
			case "minecraft:mutated_swampland":
				registryName = "minecraft:swamp_hills";
			break;
			case "taiga": // Overworld 5
			case "Taiga":
				registryName = "minecraft:taiga";
			break;
			case "taiga_hills": // Overworld 19
			case "Taiga Hills":
			case "TaigaHills":
				registryName = "minecraft:taiga_hills";
			break;
			case "taiga_mountains": // Overworld 133
			case "Taiga Mountains":
			case "Taiga M":
			case "minecraft:mutated_taiga":				
				registryName = "minecraft:taiga_mountains";
			break;
			case "tall_birch_forest": // Overworld 155
			case "Tall Birch Forest":
			case "Birch Forest M":
			case "minecraft:mutated_birch_forest":
				registryName = "minecraft:tall_birch_forest";
			break;
			case "tall_birch_hills": // Overworld 156
			case "Tall Birch Hills":
			case "Birch Forest Hills M":
			case "minecraft:mutated_birch_forest_hills":
				registryName = "minecraft:tall_birch_hills";
			break;
			case "the_end": // End 9
			case "The End":
			case "Sky":
			case "minecraft:sky":
				registryName = "minecraft:the_end";
			break;
			case "the_void": // End 127
			case "The Void":
			case "minecraft:void":
				registryName = "minecraft:the_void";
			break;
			case "warm_ocean": // Overworld 44
			case "Warm Ocean":
				registryName = "minecraft:warm_ocean";
			break;
			case "warped_forest": // Nether 172
			case "Warped Forest":
				registryName = "minecraft:warped_forest";
			break;
			case "wooded_badlands_plateau": // Overworld 38
			case "Wooded Badlands Plateau":
			case "Mesa Plateau F":
			case "minecraft:mesa_rock":
				registryName = "minecraft:wooded_badlands_plateau";
			break;
			case "wooded_hills": // Overworld 18
			case "Wooded Hills":
			case "ForestHills":
			case "minecraft:forest_hills":
				registryName = "minecraft:wooded_hills";
			break;
			case "wooded_mountains": // Overworld 34
			case "Wooded Mountains":
			case "Extreme Hills+":
			case "minecraft:extreme_hills_with_trees":
				registryName = "minecraft:wooded_mountains";
			break;
		}
		return registryName;
	}
}
