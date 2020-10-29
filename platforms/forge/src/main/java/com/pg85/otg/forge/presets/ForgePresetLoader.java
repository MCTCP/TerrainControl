package com.pg85.otg.forge.presets;

import java.nio.file.Path;

import com.pg85.otg.common.presets.LocalPresetLoader;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.preset.Preset;
import com.pg85.otg.forge.OTGPlugin;

import net.minecraft.world.biome.BiomeMaker;

public class ForgePresetLoader extends LocalPresetLoader
{
    public ForgePresetLoader(Path otgRootFolder)
    {
		super(otgRootFolder);
	}

	@Override
	public void registerBiomes()
	{
		for(Preset preset : this.presets.values())
		{
			for(BiomeConfig biomeConfig : preset.getAllBiomeConfigs())
			{
				OTGPlugin.BIOMES.register(biomeConfig.getRegistryKey().getResourcePath(), () -> BiomeMaker.func_244226_a(false)); // Register to both mc and forge so forge doesn't complain when synching registries
			}
		}
	}
}
