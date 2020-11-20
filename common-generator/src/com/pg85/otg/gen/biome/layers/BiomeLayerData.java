package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.pg85.otg.gen.biome.NewBiomeData;

/**
 * Class to hold biome layer data until world configs are working. This class will eventually be removed.
 */
public class BiomeLayerData
{
	public int generationDepth = 10;
	public int landSize = 0;
	public int landFuzzy = 5;
	public int landRarity = 99;
	public Map<Integer, List<NewBiomeGroup>> groups = new HashMap<>();
	public List<Integer> biomeDepths = new ArrayList<>(); // Depths with biomes
	public Map<Integer, NewBiomeGroup> groupRegistry = new HashMap<>();

	public static final BiomeLayerData INSTANCE = init();

	private static BiomeLayerData init() {
		BiomeLayerData data = new BiomeLayerData();
		NewBiomeGroup group1 = new NewBiomeGroup();
		group1.id = 1;
		group1.rarity = 97;
		group1.biomes.add(new NewBiomeData(1, 30, 4));
		group1.biomes.add(new NewBiomeData(2, 70, 4));

		NewBiomeGroup group2 = new NewBiomeGroup();
		group2.id = 2;
		group2.rarity = 30;
		group1.biomes.add(new NewBiomeData(3, 20, 4));

		data.groups.put(2, ImmutableList.of(group1, group2));

		data.groupRegistry.put(1, group1);
		data.groupRegistry.put(2, group2);

		data.biomeDepths.add(4);

		return data;
	}
}
