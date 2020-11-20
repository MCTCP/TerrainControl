package com.pg85.otg.forge.presets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.Preset;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgePresetLoader extends LocalPresetLoader
{
	private Map<ResourceLocation, BiomeConfig> biomeConfigsByRegistryKey = new HashMap<>();
	private Map<String,List<RegistryKey<Biome>>> biomesByPresetName = new LinkedHashMap<>();
	
	public ForgePresetLoader(Path otgRootFolder)
	{
		super(otgRootFolder);
	}

	@Override
	public BiomeConfig getBiomeConfig(String resourceLocationString)
	{
		return this.biomeConfigsByRegistryKey.get(new ResourceLocation(resourceLocationString));
	}
	
	public List<RegistryKey<Biome>> getBiomeRegistryKeys(String presetName)
	{
		return this.biomesByPresetName.get(presetName);
	}
	
	@Override
	public void registerBiomes()
	{
		for(Preset preset : this.presets.values())
		{
			List<RegistryKey<Biome>> presetBiomes = new ArrayList<>();
			this.biomesByPresetName.put(preset.getName(), presetBiomes);
			
			for(BiomeConfig biomeConfig : preset.getAllBiomeConfigs())
			{
				// DeferredRegister for Biomes doesn't appear to be working atm, biomes are never registered :(
				//RegistryObject<Biome> registryObject = OTGPlugin.BIOMES.register(biomeConfig.getRegistryKey().getResourcePath(), () -> createOTGBiome(biomeConfig));
				
				Biome biome = ForgeBiome.createOTGBiome(preset.getWorldConfig(), biomeConfig);
 				ForgeRegistries.BIOMES.register(biome);
 				
 				// Store registry key (resourcelocation) so we can look up biomeconfigs via RegistryKey<Biome> later.
 				ResourceLocation resourceLocation = new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString());
 				this.biomeConfigsByRegistryKey.put(resourceLocation, biomeConfig);
 				
 				presetBiomes.add(RegistryKey.func_240903_a_(Registry.field_239720_u_, resourceLocation));

 				if(biomeConfig.getRegistryKey().toResourceLocationString().equals("otg:default.ocean"))
 				{
 					OTGBiomeProvider.LOOKUP[0] = biomeConfig;
 				}
 				if(biomeConfig.getRegistryKey().toResourceLocationString().equals("otg:default.plains"))
 				{
 					OTGBiomeProvider.LOOKUP[1] = biomeConfig;
 				}
 				if(biomeConfig.getRegistryKey().toResourceLocationString().equals("otg:default.forest"))
 				{
 					OTGBiomeProvider.LOOKUP[2] = biomeConfig;
 				}
 				if(biomeConfig.getRegistryKey().toResourceLocationString().equals("otg:default.desert"))
 				{
 					OTGBiomeProvider.LOOKUP[3] = biomeConfig;
 				}
			}
		}
	}	
}
