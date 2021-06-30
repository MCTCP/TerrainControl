package com.pg85.otg.forge.presets;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.Set;

import com.mojang.serialization.Lifecycle;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.materials.ForgeMaterialReader;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.MCBiomeResourceLocation;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.EntityCategory;
import com.pg85.otg.gen.biome.layers.BiomeLayerData;
import com.pg85.otg.gen.biome.layers.NewBiomeGroup;
import com.pg85.otg.interfaces.IBiomeResourceLocation;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;

import com.pg85.otg.gen.biome.BiomeData;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgePresetLoader extends LocalPresetLoader
{
	private Map<String, List<RegistryKey<Biome>>> biomesByPresetFolderName = new LinkedHashMap<>();
	private HashMap<String, BiomeConfig[]> globalIdMapping = new HashMap<>();
	private HashMap<String, Reference2IntMap<BiomeConfig>> reverseIdMapping = new HashMap<>();	// Using a ref is much faster than using an object
	private Map<String, BiomeConfig> biomeConfigsByRegistryKey = new HashMap<>();
	private Map<String, BiomeLayerData> presetGenerationData = new HashMap<>();

	public ForgePresetLoader(Path otgRootFolder)
	{
		super(otgRootFolder);
	}

	@Override
	public IMaterialReader createMaterialReader()
	{
		return new ForgeMaterialReader();
	}
	
	@Override
	public BiomeConfig getBiomeConfig(String presetFolderName, int biomeId)
	{
		BiomeConfig[] biomes = this.globalIdMapping.get(presetFolderName);
		return biomes.length > biomeId ? biomes[biomeId] : null;
	}
	
	@Override
	public BiomeConfig getBiomeConfig(String resourceLocationString)
	{
		return this.biomeConfigsByRegistryKey.get(resourceLocationString);
	}
	
	public List<RegistryKey<Biome>> getBiomeRegistryKeys(String presetFolderName)
	{
		return this.biomesByPresetFolderName.get(presetFolderName);
	}

	public BiomeConfig[] getGlobalIdMapping(String presetFolderName)
	{
		return globalIdMapping.get(presetFolderName);
	}

	public Map<String, BiomeLayerData> getPresetGenerationData()
	{
		Map<String, BiomeLayerData> clonedData = new HashMap<>();
		for(Entry<String, BiomeLayerData> entry : this.presetGenerationData.entrySet())
		{
			clonedData.put(entry.getKey(), new BiomeLayerData(entry.getValue()));
		}
		return clonedData;
	}

	// Note: BiomeGen and ChunkGen cache some settings during a session, so they'll only update on world exit/rejoin.
	public void reloadPresetFromDisk(String presetFolderName, IConfigFunctionProvider biomeResourcesManager, ILogger logger, MutableRegistry<Biome> biomeRegistry)
	{
		if(this.presetsDir.exists() && this.presetsDir.isDirectory())
		{
			for(File presetDir : this.presetsDir.listFiles())
			{
				if(presetDir.isDirectory() && presetDir.getName().equals(presetFolderName))
				{
					for(File file : presetDir.listFiles())
					{
						if(file.getName().equals(Constants.WORLD_CONFIG_FILE))
						{
							this.materialReaderByPresetFolderName.put(presetFolderName, new ForgeMaterialReader());							
							Preset preset = loadPreset(presetDir.toPath(), biomeResourcesManager, logger);
							Preset existingPreset = this.presets.get(preset.getFolderName());
							existingPreset.update(preset);
							break;
						}
					}
				}
			}
		}
		refreshConfigs(biomeRegistry);
	}

	protected void refreshConfigs(MutableRegistry<Biome> biomeRegistry)
	{
		this.globalIdMapping = new HashMap<>();
		this.reverseIdMapping = new HashMap<>();
		this.biomeConfigsByRegistryKey = new HashMap<>();
		this.presetGenerationData = new HashMap<>();
		this.biomesByPresetFolderName = new LinkedHashMap<>();
		this.materialReaderByPresetFolderName = new LinkedHashMap<>();
		registerBiomes(true, biomeRegistry);
	}
	
	@Override
	public void registerBiomes()
	{
		registerBiomes(false, null);
	}

	private void registerBiomes(boolean refresh, MutableRegistry<Biome> biomeRegistry)
	{
		for(Preset preset : this.presets.values())
		{
			// Index BiomeColors for FromImageMode and /otg map
			HashMap<Integer, Integer> biomeColorMap = new HashMap<Integer, Integer>();
			
			// Start at 1, 0 is the fallback for the biome generator (the world's ocean biome).
			int currentId = 1;
			
			List<RegistryKey<Biome>> presetBiomes = new ArrayList<>();
			this.biomesByPresetFolderName.put(preset.getFolderName(), presetBiomes);

			WorldConfig worldConfig = preset.getWorldConfig();
			BiomeConfig oceanBiomeConfig = null;
			int[] oceanTemperatures = new int[]{0, 0, 0, 0};
			
			List<BiomeConfig> biomeConfigs = preset.getAllBiomeConfigs();
			BiomeConfig[] presetIdMapping = new BiomeConfig[biomeConfigs.size()];
			Reference2IntMap<BiomeConfig> presetReverseIdMapping = new Reference2IntLinkedOpenHashMap<>();

			Map<Integer, List<BiomeData>> isleBiomesAtDepth = new HashMap<>();
			Map<Integer, List<BiomeData>> borderBiomesAtDepth = new HashMap<>();
			
			Map<String, Integer> worldBiomes = new HashMap<>();

			Map<String, BiomeConfig> biomeConfigsByName = new HashMap<>();
			for(BiomeConfig biomeConfig : biomeConfigs)
			{				
				boolean isOceanBiome = false;
 				// Biome id 0 is reserved for ocean, used when a land column has 
 				// no biome assigned, which can happen due to biome group rarity.
 				if(biomeConfig.getName().equals(worldConfig.getDefaultOceanBiome()))
 				{
 					oceanBiomeConfig = biomeConfig;
 					isOceanBiome = true;
 				}

 				// When using TemplateForBiome, we'll fetch the non-OTG biome from the registry, including any settings registered to it.
 				// For normal biomes we create our own new OTG biome and apply settings from the biome config.
				ResourceLocation resourceLocation;
 				RegistryKey<Biome> registryKey;
				if(biomeConfig.getTemplateForBiome() != null && biomeConfig.getTemplateForBiome().trim().length() > 0)
				{
					resourceLocation = new ResourceLocation(biomeConfig.getTemplateForBiome().replace("minecraft:", "").replace(" ", "_").toLowerCase());
					registryKey = RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation);
					presetBiomes.add(registryKey);
					biomeConfig.setRegistryKey(new MCBiomeResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath(), preset.getFolderName()));
				} else {
					IBiomeResourceLocation otgLocation = new OTGBiomeResourceLocation(preset.getPresetFolder(), preset.getShortPresetName(), preset.getMajorVersion(), biomeConfig.getName());
					biomeConfig.setRegistryKey(otgLocation);
					resourceLocation = new ResourceLocation(otgLocation.toResourceLocationString());	
 	 				registryKey = RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation);
 					presetBiomes.add(registryKey);
	 				Biome biome = ForgeBiome.createOTGBiome(isOceanBiome, preset.getWorldConfig(), biomeConfig);
					if(!refresh)
					{
						ForgeRegistries.BIOMES.register(biome);
					} else {
						biomeRegistry.registerOrOverride(OptionalInt.empty(), registryKey, biome, Lifecycle.stable());
					}
 				}

				biomeConfigsByName.put(biomeConfig.getName(), biomeConfig);
				
 				// Store registry key (resourcelocation) so we can look up biomeconfigs via RegistryKey<Biome> later.
 				this.biomeConfigsByRegistryKey.put(resourceLocation.toString(), biomeConfig);
 				
 				int otgBiomeId = isOceanBiome ? 0 : currentId;
 				
 				// Ocean temperature mappings. Probably a better way to do this?
 				if (biomeConfig.getName().equals(worldConfig.getDefaultWarmOceanBiome()))
 				{
 					oceanTemperatures[0] = otgBiomeId;
				}
 				if (biomeConfig.getName().equals(worldConfig.getDefaultLukewarmOceanBiome()))
 				{
					oceanTemperatures[1] = otgBiomeId;
				}
 				if (biomeConfig.getName().equals(worldConfig.getDefaultColdOceanBiome()))
 				{
					oceanTemperatures[2] = otgBiomeId;
				}
 				if (biomeConfig.getName().equals(worldConfig.getDefaultFrozenOceanBiome()))
 				{
					oceanTemperatures[3] = otgBiomeId;
				} 			
				
				// Add biome dictionary tags for Forge
				biomeConfig.getBiomeDictTags().forEach(biomeDictId -> {
					if(biomeDictId != null && biomeDictId.trim().length() > 0)
					{
						BiomeDictionary.addTypes(registryKey, BiomeDictionary.Type.getType(biomeDictId.trim()));
					}
				});

				presetIdMapping[otgBiomeId] = biomeConfig;
				presetReverseIdMapping.put(biomeConfig, otgBiomeId);

				worldBiomes.put(biomeConfig.getName(), otgBiomeId);
 				
 				// Make a list of isle and border biomes per generation depth
 				if(biomeConfig.isIsleBiome())
 				{
					// Make or get a list for this group depth, then add
					List<BiomeData> biomesAtDepth = isleBiomesAtDepth.getOrDefault(worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(), new ArrayList<>());
					biomesAtDepth.add(
						new BiomeData(
							otgBiomeId, 
							biomeConfig.getName(), 
							worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getBiomeRarity() : biomeConfig.getBiomeRarityWhenIsle(),
							worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(), 
							biomeConfig.getBiomeTemperature(), 
							biomeConfig.getIsleInBiomes(), 
							biomeConfig.getBorderInBiomes(), 
							biomeConfig.getNotBorderNearBiomes()
						)
					);
					isleBiomesAtDepth.put(worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(), biomesAtDepth);
 				}

 				if(biomeConfig.isBorderBiome())
 				{
					// Make or get a list for this group depth, then add
					List<BiomeData> biomesAtDepth = borderBiomesAtDepth.getOrDefault(worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(), new ArrayList<>());
					biomesAtDepth.add(
						new BiomeData(
							otgBiomeId,
							biomeConfig.getName(), 
							biomeConfig.getBiomeRarity(),
							worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(), 
							biomeConfig.getBiomeTemperature(), 
							biomeConfig.getIsleInBiomes(), 
							biomeConfig.getBorderInBiomes(), 
							biomeConfig.getNotBorderNearBiomes()
						)
					);
					borderBiomesAtDepth.put(worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(), biomesAtDepth);
 				}
 				
 				// Index BiomeColor for FromImageMode and /otg map
				biomeColorMap.put(biomeConfig.getBiomeColor(), otgBiomeId);
 				
				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
				{
					OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Registered biome " + resourceLocation.toString() + " | " + biomeConfig.getName() + " with OTG id " + otgBiomeId);
				}
 				
 				currentId += isOceanBiome ? 0 : 1;
			}

			// If the ocean config is null, shift the array downwards to fill id 0
			if (oceanBiomeConfig == null)
			{
				System.arraycopy(presetIdMapping, 1, presetIdMapping, 0, presetIdMapping.length - 1);
			}
			
			this.globalIdMapping.put(preset.getFolderName(), presetIdMapping);
			this.reverseIdMapping.put(preset.getFolderName(), presetReverseIdMapping);

			// Set the base data
			BiomeLayerData data = new BiomeLayerData(preset.getPresetFolder(), worldConfig, oceanBiomeConfig, oceanTemperatures);
			
			Set<Integer> biomeDepths = new HashSet<>();
			Map<Integer, List<NewBiomeGroup>> groupDepths = new HashMap<>();

			int genDepth = worldConfig.getGenerationDepth();

			// Iterate through the groups and add it to the layer data
			for (BiomeGroup group : worldConfig.getBiomeGroupManager().getGroups())
			{
				// Initialize biome group data
				NewBiomeGroup bg = new NewBiomeGroup();
				bg.id = group.getGroupId();
				bg.rarity = group.getGroupRarity();

				// init to genDepth as it will have one value per depth
				bg.totalDepthRarity = new int[genDepth + 1];
				bg.maxRarityPerDepth = new int[genDepth + 1];

				float totalTemp = 0;
				
				// Add each biome to the group
				for (String biome : group.biomes.keySet())
				{					
					BiomeConfig config = biomeConfigsByName.get(biome);
					if(config != null)
					{
						// Make and add the generation data
						BiomeData newBiomeData = new BiomeData(
							presetReverseIdMapping.getInt(config),
							config.getName(),
							config.getBiomeRarity(),
							config.getBiomeSize(),
							config.getBiomeTemperature(),
							config.getIsleInBiomes(),
							config.getBorderInBiomes(),
							config.getNotBorderNearBiomes());
						bg.biomes.add(newBiomeData);
	
						// Add the biome size- if it's already there, nothing is done
						biomeDepths.add(config.getBiomeSize());
						
						totalTemp += config.getBiomeTemperature();
						bg.totalGroupRarity += config.getBiomeRarity();
	
						// Add this biome's rarity to the total for its depth in the group
						bg.totalDepthRarity[config.getBiomeSize()] += config.getBiomeRarity();
					}
				}

				// We have filled out the biome group's totalDepthRarity array, use it to fill the maxRarityPerDepth array
				for (int depth = 0; depth < bg.totalDepthRarity.length; depth++)
				{
					// maxRarityPerDepth is the sum of totalDepthRarity for this and subsequent depths
					for (int j = depth; j < bg.totalDepthRarity.length; j++)
					{
						bg.maxRarityPerDepth[depth] += bg.totalDepthRarity[j];
					}
				}

				bg.avgTemp = totalTemp / group.biomes.size();

				int groupSize = group.getGenerationDepth();

				// Make or get a list for this group depth, then add
				List<NewBiomeGroup> groupsAtDepth = groupDepths.getOrDefault(groupSize, new ArrayList<>());
				groupsAtDepth.add(bg);

				// Replace entry
				groupDepths.put(groupSize, groupsAtDepth);

				// Register group id
				data.groupRegistry.put(bg.id, bg);
			}

			// Add the data and process isle/border biomes
			data.init(biomeDepths, groupDepths, isleBiomesAtDepth, borderBiomesAtDepth, worldBiomes, biomeColorMap, presetIdMapping);
			
			// Set data for this preset
			this.presetGenerationData.put(preset.getFolderName(), data);
		}
	}
	
	@Override
	protected void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{		
		String[] resourceLocationArr = biomeResourceLocation.split(":");			
		String resourceDomain = resourceLocationArr.length > 1 ? resourceLocationArr[0] : null;
		String resourceLocation = resourceLocationArr.length > 1 ? resourceLocationArr[1] : resourceLocationArr[0];
			
		Biome biome = null;
		try
		{
			ResourceLocation location = new ResourceLocation(resourceDomain, resourceLocation);
			biome = ForgeRegistries.BIOMES.getValue(location);
		}
		catch(ResourceLocationException ex)
		{
			// Can happen when no biome is registered or input is otherwise invalid.
		}
		if(biome != null)
		{
			// Merge the vanilla biome's mob spawning lists with the mob spawning lists from the BiomeConfig.
			// Mob spawning settings for the same creature will not be inherited (so BiomeConfigs can override vanilla mob spawning settings).
			// We also inherit any mobs that have been added to vanilla biomes' mob spawning lists by other mods.
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, EntityClassification.MONSTER), EntityCategory.MONSTER);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, EntityClassification.AMBIENT), EntityCategory.AMBIENT_CREATURE);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, EntityClassification.CREATURE), EntityCategory.CREATURE);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, EntityClassification.WATER_AMBIENT), EntityCategory.WATER_AMBIENT);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, EntityClassification.WATER_CREATURE), EntityCategory.WATER_CREATURE);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, EntityClassification.MISC), EntityCategory.MISC);
		} else {
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MOBS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MOBS, "Could not inherit mobs for unrecognised biome \"" +  biomeResourceLocation + "\" in " + biomeConfigStub.getBiomeName() + Constants.BiomeConfigFileExtension);
			}
		}
	}

	private List<WeightedMobSpawnGroup> getListFromMinecraftBiome(Biome biome, EntityClassification type)
	{
		List<Spawners> mobList = biome.getMobSettings().getMobs(type);		
		List<WeightedMobSpawnGroup> result = new ArrayList<WeightedMobSpawnGroup>();
		for (Spawners spawner : mobList)
		{
			WeightedMobSpawnGroup wMSG = new WeightedMobSpawnGroup(spawner.type.getRegistryName().toString(), spawner.weight, spawner.minCount, spawner.maxCount);
			if(wMSG != null)
			{
				result.add(wMSG);
			}
		}
		return result;
	}	
}
