package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.gen.biome.NewBiomeData;

/**
 * Class to hold biome layer data until world configs are working. This class will eventually be removed.
 */
public class BiomeLayerData
{
	public BiomeMode biomeMode = BiomeMode.Normal;
	public int generationDepth = 10;
	public int landSize = 0;
	public int landFuzzy = 5;
	public int landRarity = 99;
	public double frozenOceanTemperature;
	public NewBiomeData oceanBiomeData;
	public Map<Integer, List<NewBiomeGroup>> groups = new HashMap<>();
	public List<Integer> biomeDepths = new ArrayList<>(); // Depths with biomes
	public Map<Integer, NewBiomeGroup> groupRegistry = new HashMap<>();
	public boolean freezeGroups;
}
