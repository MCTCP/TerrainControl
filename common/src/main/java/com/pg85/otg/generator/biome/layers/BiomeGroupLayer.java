package com.pg85.otg.generator.biome.layers;

import static com.pg85.otg.generator.biome.layers.BiomeLayers.GROUP_SHIFT;

import java.util.List;

import com.pg85.otg.generator.biome.layers.type.ParentedLayer;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;
import com.pg85.otg.generator.biome.layers.util.LayerSampler;

public class BiomeGroupLayer implements ParentedLayer
{
	private BiomePicker picker;

	public BiomeGroupLayer(List<NewBiomeGroup> groups) {
		BiomePicker picker = new BiomePicker();

		for (NewBiomeGroup group : groups)
		{
			picker.addBiome(group.id, group.weight);
		}

		this.picker = picker;
	}

	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		if (BiomeLayers.isLand(sample))
		{
			int biomeGroup = this.picker.pickBiome(context);
			return sample | biomeGroup << GROUP_SHIFT;
		}

		return sample;
	}
}
