package com.pg85.otg.forge.presets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.BiomeResourceLocation;
import com.pg85.otg.gen.biome.layers.BiomeLayerData;
import com.pg85.otg.gen.biome.layers.NewBiomeGroup;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import com.pg85.otg.gen.biome.NewBiomeData;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgePresetLoader extends LocalPresetLoader
{
	private final Int2ObjectMap<BiomeConfig> globalIdMapping = new Int2ObjectLinkedOpenHashMap<>();
	// Using a ref is much faster than using an object
	private final Reference2IntMap<BiomeConfig> reverseIdMapping = new Reference2IntLinkedOpenHashMap<>();
	private final Map<ResourceLocation, BiomeConfig> biomeConfigsByRegistryKey = new HashMap<>();
	private final Map<String, List<RegistryKey<Biome>>> biomesByPresetName = new LinkedHashMap<>();
	private final Map<String, BiomeLayerData> presetGenerationData = new HashMap<>();
	
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

	public Int2ObjectMap<BiomeConfig> getGlobalIdMapping()
	{
		return globalIdMapping;
	}

	public Map<String, BiomeLayerData> getPresetGenerationData()
	{
		return presetGenerationData;
	}

	@Override
	public void registerBiomes()
	{
		int currentId = 0;
		for(Preset preset : this.presets.values())
		{
			List<RegistryKey<Biome>> presetBiomes = new ArrayList<>();
			this.biomesByPresetName.put(preset.getName(), presetBiomes);
			WorldConfig worldConfig = preset.getWorldConfig();

			for(BiomeConfig biomeConfig : preset.getAllBiomeConfigs())
			{
				// DeferredRegister for Biomes doesn't appear to be working atm, biomes are never registered :(
				//RegistryObject<Biome> registryObject = OTGPlugin.BIOMES.register(biomeConfig.getRegistryKey().getResourcePath(), () -> createOTGBiome(biomeConfig));
				
				Biome biome = ForgeBiome.createOTGBiome(preset.getWorldConfig(), biomeConfig);
 				ForgeRegistries.BIOMES.register(biome);
 				
 				// Store registry key (resourcelocation) so we can look up biomeconfigs via RegistryKey<Biome> later.
 				ResourceLocation resourceLocation = new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString());
				System.out.println(resourceLocation);
 				this.biomeConfigsByRegistryKey.put(resourceLocation, biomeConfig);
 				
 				presetBiomes.add(RegistryKey.func_240903_a_(Registry.field_239720_u_, resourceLocation));

 				this.globalIdMapping.put(currentId, biomeConfig);
 				this.reverseIdMapping.put(biomeConfig, currentId);

 				currentId++;
			}

			// Set the base data
			BiomeLayerData data = new BiomeLayerData();
			data.generationDepth = worldConfig.getGenerationDepth();
			data.landSize = worldConfig.getLandSize();
			data.landFuzzy = worldConfig.getLandFuzzy();
			data.landRarity = worldConfig.getLandRarity();

			// Get and set the ocean
			BiomeConfig ocean = this.biomeConfigsByRegistryKey.get(new ResourceLocation(new BiomeResourceLocation(preset.getName(), worldConfig.getDefaultOceanBiome()).toResourceLocationString()));
			data.oceanId = this.reverseIdMapping.getInt(ocean);

			Set<Integer> biomeDepths = new HashSet<>();
			Map<Integer, List<NewBiomeGroup>> groupDepths = new HashMap<>();

			// Iterate through the groups and add it to the layer data
			for (BiomeGroup group : worldConfig.biomeGroupManager.getGroups())
			{
				// Initialize biome group data
				NewBiomeGroup bg = new NewBiomeGroup();
				bg.id = group.getGroupId();
				bg.rarity = group.getGroupRarity();

				// Add each biome to the group
				for (String biome : group.biomes.keySet())
				{
					ResourceLocation location = new ResourceLocation(new BiomeResourceLocation(preset.getName(), biome).toResourceLocationString());
					BiomeConfig config = this.biomeConfigsByRegistryKey.get(location);

					// Make and add the generation data
					NewBiomeData newBiomeData = new NewBiomeData(this.reverseIdMapping.getInt(config), config.getBiomeRarity(), config.getBiomeSize());
					bg.biomes.add(newBiomeData);

					// Add the biome size- if it's already there, nothing is done
					biomeDepths.add(config.getBiomeSize());
				}

				int groupSize = group.getGenerationDepth();

				// Make or get a list for this group depth, then add
				List<NewBiomeGroup> groupsAtDepth = groupDepths.getOrDefault(groupSize, new ArrayList<>());
				groupsAtDepth.add(bg);

				// Replace entry
				groupDepths.put(groupSize, groupsAtDepth);

				// Register group id
				data.groupRegistry.put(bg.id, bg);
			}

			// Add the data
			data.biomeDepths.addAll(biomeDepths);
			data.groups = groupDepths;

			// Set data for this preset
			this.presetGenerationData.put(preset.getName(), data);
		}
	}
}
