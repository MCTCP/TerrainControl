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
	public int oceanId = 0;
	public Map<Integer, List<NewBiomeGroup>> groups = new HashMap<>();
	public List<Integer> biomeDepths = new ArrayList<>(); // Depths with biomes
	public Map<Integer, NewBiomeGroup> groupRegistry = new HashMap<>();
}
