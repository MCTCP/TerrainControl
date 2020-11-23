package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.gen.biome.layers.util.LayerRandomnessSource;

public class BiomePicker {
	private final List<Entry> biomeEntries = new ArrayList<>();
	private double weightTotal;

	public int pickBiome(LayerRandomnessSource rand) {
		double randVal = target(rand);
		int i = -1;

		while (randVal >= 0) {
			++i;
			randVal -= biomeEntries.get(i).weight;
		}

		return biomeEntries.get(i).biomeId;
	}

	public void addBiome(int biome, double weight) {
		this.biomeEntries.add(new Entry(biome, weight));
		weightTotal += weight;
	}

	private double target(LayerRandomnessSource random) {
		return (double) random.nextInt(Integer.MAX_VALUE) * weightTotal / Integer.MAX_VALUE;
	}

	private static class Entry {
		private final int biomeId;
		private final double weight;
		private Entry(int biome, double weight) {
			this.biomeId = biome;
			this.weight = weight;
		}

	}
}
