package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.generator.biome.layers.type.ParentedLayer;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;
import com.pg85.otg.generator.biome.layers.util.LayerSampler;

public class PlaceBiomesLayer implements ParentedLayer
{
	private final BiomePicker picker;
	public PlaceBiomesLayer() {
		this.picker = new BiomePicker();
		this.picker.addBiome(1, 0.65);
		this.picker.addBiome(2, 0.55);
		this.picker.addBiome(3, 0.125);
	}

	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		if (BiomeLayers.isLand(sample)) {
			int biome = this.picker.pickBiome(context);

			return sample | biome;
		}

		return sample;
	}
}
