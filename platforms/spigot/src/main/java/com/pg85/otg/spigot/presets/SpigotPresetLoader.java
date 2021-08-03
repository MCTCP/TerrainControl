package com.pg85.otg.spigot.presets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;

import com.mojang.serialization.Lifecycle;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfigFinder;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.gen.biome.layers.BiomeLayerData;
import com.pg85.otg.gen.biome.layers.NewBiomeGroup;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IBiomeResourceLocation;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.SpigotBiome;
import com.pg85.otg.spigot.materials.SpigotMaterialReader;
import com.pg85.otg.spigot.networking.BiomeSettingSyncWrapper;
import com.pg85.otg.spigot.networking.OTGClientSyncManager;
import com.pg85.otg.spigot.util.MobSpawnGroupHelper;
import com.pg85.otg.util.biome.MCBiomeResourceLocation;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.EntityCategory;

import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.EnumCreatureType;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.IRegistryWritable;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.RegistryGeneration;
import net.minecraft.server.v1_16_R3.ResourceKey;

public class SpigotPresetLoader extends LocalPresetLoader
{
	private final Map<String, List<ResourceKey<BiomeBase>>> biomesByPresetFolderName = new LinkedHashMap<>();
	private final HashMap<String, IBiome[]> globalIdMapping = new HashMap<>();	
	private final Map<String, BiomeLayerData> presetGenerationData = new HashMap<>();	
	// We have to store biomes, since Spigot doesn't expose registry key on BiomeBase.
	private final Map<BiomeBase, IBiomeConfig> biomeConfigsByBiome = new HashMap<>();	

	private final ResourceKey<IRegistry<BiomeBase>> BIOME_KEY = IRegistry.ay;

	public SpigotPresetLoader (File otgRootFolder)
	{
		super(otgRootFolder.toPath());
	}

	// Creates a preset-specific materialreader, have to do this
	// only when loading each preset since each preset may have
	// its own block fallbacks / block dictionaries.
	@Override
	public IMaterialReader createMaterialReader()
	{
		return new SpigotMaterialReader();
	}
	
	@Override
	public void registerBiomes()
	{
		for(Preset preset : this.presets.values())
		{
			registerBiomesForPreset(false, preset);
		}
	}

	private void registerBiomesForPreset(boolean refresh, Preset preset)
	{
		IRegistryWritable<BiomeBase> biomeRegistry = ((CraftServer)Bukkit.getServer()).getServer().customRegistry.b(BIOME_KEY);
		// Index BiomeColors for FromImageMode and /otg map
		HashMap<Integer, Integer> biomeColorMap = new HashMap<Integer, Integer>();
		
		// Start at 1, 0 is the fallback for the biome generator (the world's ocean biome).
		int currentId = 1;
		
		List<ResourceKey<BiomeBase>> presetBiomes = new ArrayList<>();
		this.biomesByPresetFolderName.put(preset.getFolderName(), presetBiomes);

		IWorldConfig worldConfig = preset.getWorldConfig();
		IBiomeConfig oceanBiomeConfig = null;
		int[] oceanTemperatures = new int[]{0, 0, 0, 0};
		
		List<IBiomeConfig> biomeConfigs = preset.getAllBiomeConfigs();		

		Map<Integer, List<BiomeData>> isleBiomesAtDepth = new HashMap<>();
		Map<Integer, List<BiomeData>> borderBiomesAtDepth = new HashMap<>();
		
		Map<String, List<Integer>> worldBiomes = new HashMap<>();
		Map<String, IBiomeConfig> biomeConfigsByName = new HashMap<>();
		
		// Create registry keys for each biomeconfig, create template 
		// biome configs for any modded biomes using TemplateForBiome.
		Map<IBiomeResourceLocation, IBiomeConfig> biomeConfigsByResourceLocation = new LinkedHashMap<>();
		List<String> blackListedBiomes = worldConfig.getBlackListedBiomes();
		for(IBiomeConfig biomeConfig : biomeConfigs)
		{
			if(biomeConfig.getTemplateForBiome() != null && biomeConfig.getTemplateForBiome().trim().length() > 0)
			{
				if(
					biomeConfig.getTemplateForBiome().toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL) ||
					biomeConfig.getTemplateForBiome().toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL)
				)
				{					
					String[] tagStrings = biomeConfig.getTemplateForBiome().split(",");
					for(String tagString : tagStrings)
					{
						Set<ResourceKey<BiomeBase>> biomesForTags = new HashSet<>();
						String[] tagSubStrings = tagString.split(" ");
						for(String tagSubString : tagSubStrings)
						{
							if(
								tagSubString.trim().toLowerCase().toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL) ||
								tagSubString.trim().toLowerCase().toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL)
							)
							{
								BiomeBase.Geography category = 	BiomeBase.Geography.a(tagSubString.trim().toLowerCase().toLowerCase().replace(Constants.MC_BIOME_CATEGORY_LABEL, "").replace(Constants.BIOME_CATEGORY_LABEL, ""));
								if(category != null)
								{
									biomesForTags.addAll(
											biomeRegistry.g()
											.filter(a -> 
												a.t() == category &&
												!blackListedBiomes.contains(biomeRegistry.getKey(a).toString()) &&
												!biomeRegistry.getKey(a).getNamespace().equals(Constants.MOD_ID_SHORT) &&
												(tagSubString.trim().toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) || !biomeRegistry.getKey(a).getNamespace().equals("minecraft"))
											).map(
												b -> ResourceKey.a(BIOME_KEY, biomeRegistry.getKey(b))
											).collect(Collectors.toList())
									);
								} else {
									if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
									{
										OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "TemplateForBiome biome category " + tagSubString +  " for biomeconfig " + biomeConfig.getName() + " could not be found.");
									}
								}
							}
						}
						if(biomesForTags != null)
						{
							for(ResourceKey<BiomeBase> biomeForTag : biomesForTags)
							{
								BiomeBase biome = biomeRegistry.get(biomeForTag.a());
								if(biomeConfig.isWithinTemplateForBiomeTemperatureRange(biome.k()))
								{
									IBiomeResourceLocation otgLocation = new MCBiomeResourceLocation(biomeForTag.a().getNamespace(), biomeForTag.a().getKey(), preset.getFolderName());
									if(!biomeConfigsByResourceLocation.containsKey(otgLocation))
									{
										biomeConfigsByResourceLocation.put(otgLocation, biomeConfig.createTemplateBiome());
										biomeConfigsByName.put(biomeConfig.getName(), biomeConfig);
									}
								}
							}
						} else {
							if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
							{
								OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "No tags or categories found for TemplateForBiome: " + biomeConfig.getTemplateForBiome() + " in biome config " + biomeConfig.getName());
							}
						}
					}
				} else {
					MinecraftKey resourceLocation = new MinecraftKey(biomeConfig.getTemplateForBiome().replace("minecraft:", "").replace(" ", "_").toLowerCase());
					BiomeBase biome = biomeRegistry.get(resourceLocation);
					if(biome != null)
					{
						biomeConfigsByResourceLocation.put(new MCBiomeResourceLocation(biomeRegistry.getKey(biome).getNamespace(), biomeRegistry.getKey(biome).getKey(), preset.getFolderName()), biomeConfig);
						biomeConfigsByName.put(biomeConfig.getName(), biomeConfig);
					} else {
						if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
						{
							OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "No biome found for TemplateForBiome " + biomeConfig.getTemplateForBiome().replace("minecraft:", "").replace(" ", "_").toLowerCase() + " in biome config " + biomeConfig.getName());
						}
					}
				}
			} else {
				IBiomeResourceLocation otgLocation = new OTGBiomeResourceLocation(preset.getPresetFolder(), preset.getShortPresetName(), preset.getMajorVersion(), biomeConfig.getName());
				biomeConfigsByResourceLocation.put(otgLocation, biomeConfig);
				biomeConfigsByName.put(biomeConfig.getName(), biomeConfig);
			}
		}
		
		IBiome[] presetIdMapping = new IBiome[biomeConfigsByResourceLocation.entrySet().size()];
		for(Entry<IBiomeResourceLocation, IBiomeConfig> biomeConfig : biomeConfigsByResourceLocation.entrySet())
		{
			boolean isOceanBiome = false;
			// Biome id 0 is reserved for ocean, used when a land column has 
			// no biome assigned, which can happen due to biome group rarity.
			if(biomeConfig.getValue().getName().equals(worldConfig.getDefaultOceanBiome()))
			{
				oceanBiomeConfig = biomeConfig.getValue();
				isOceanBiome = true;
			}

			int otgBiomeId = isOceanBiome ? 0 : currentId;

			// When using TemplateForBiome, we'll fetch the non-OTG biome from the registry, including any settings registered to it.
			// For normal biomes we create our own new OTG biome and apply settings from the biome config.
			MinecraftKey resourceLocation = new MinecraftKey(biomeConfig.getKey().toResourceLocationString());
			ResourceKey<BiomeBase> registryKey;
			BiomeBase biome;
			if(biomeConfig.getValue().getTemplateForBiome() != null && biomeConfig.getValue().getTemplateForBiome().trim().length() > 0)
			{
				biome = biomeRegistry.get(resourceLocation);
				if(biome == null)
				{
					if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
					{
						OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Could not find biome " + resourceLocation.toString() + " for template biomeconfig " + biomeConfig.getValue().getName());
					}
					continue;
				}
				registryKey = ResourceKey.a(BIOME_KEY, resourceLocation);
				presetBiomes.add(registryKey);
				biomeConfig.getValue().setRegistryKey(biomeConfig.getKey());
				biomeConfig.getValue().setOTGBiomeId(otgBiomeId);
			} else {
				biomeConfig.getValue().setRegistryKey(biomeConfig.getKey());
				biomeConfig.getValue().setOTGBiomeId(otgBiomeId);
 				registryKey = ResourceKey.a(BIOME_KEY, resourceLocation);
				presetBiomes.add(registryKey);
 				biome = SpigotBiome.createOTGBiome(isOceanBiome, preset.getWorldConfig(), biomeConfig.getValue());	 			

				if(!refresh)
				{
					biomeRegistry.a(registryKey, biome, Lifecycle.experimental());
				} else {
					biomeRegistry.a(OptionalInt.empty(), registryKey, biome, Lifecycle.experimental());
				}
			}

			// Populate our map for syncing
			OTGClientSyncManager.getSyncedData().put(resourceLocation.toString(), new BiomeSettingSyncWrapper(biomeConfig.getValue()));

			// Ocean temperature mappings. Probably a better way to do this?
			if (biomeConfig.getValue().getName().equals(worldConfig.getDefaultWarmOceanBiome()))
			{
				oceanTemperatures[0] = otgBiomeId;
			}
			if (biomeConfig.getValue().getName().equals(worldConfig.getDefaultLukewarmOceanBiome()))
			{
				oceanTemperatures[1] = otgBiomeId;
			}
			if (biomeConfig.getValue().getName().equals(worldConfig.getDefaultColdOceanBiome()))
			{
				oceanTemperatures[2] = otgBiomeId;
			}
			if (biomeConfig.getValue().getName().equals(worldConfig.getDefaultFrozenOceanBiome()))
			{
				oceanTemperatures[3] = otgBiomeId;
			}

//			if(biomeConfig.getKey() instanceof OTGBiomeResourceLocation)
//			{
//				// Add biome dictionary tags for Forge
//				biomeConfig.getValue().getBiomeDictTags().forEach(biomeDictId -> {
//					if(biomeDictId != null && biomeDictId.trim().length() > 0)
//					{
//						BiomeDictionary.addTypes(registryKey, BiomeDictionary.Type.getType(biomeDictId.trim()));
//					}
//				});
//			}

			IBiome otgBiome = new SpigotBiome(biome, biomeConfig.getValue());
			if(otgBiomeId >= presetIdMapping.length)
			{
				OTG.getEngine().getLogger().log(LogLevel.FATAL, LogCategory.CONFIGS, "Fatal error while registering OTG biome id's for preset " + preset.getFolderName() + ", most likely you've assigned a DefaultOceanBiome that doesn't exist.");
				throw new RuntimeException("Fatal error while registering OTG biome id's for preset " + preset.getFolderName() + ", most likely you've assigned a DefaultOceanBiome that doesn't exist.");
			}
			presetIdMapping[otgBiomeId] = otgBiome;

			List<Integer> idsForBiome = worldBiomes.get(biomeConfig.getValue().getName());
			if(idsForBiome == null)
			{
				idsForBiome = new ArrayList<Integer>();
				worldBiomes.put(biomeConfig.getValue().getName(), idsForBiome);
			}
			idsForBiome.add(otgBiomeId);
			
			// Make a list of isle and border biomes per generation depth
			if(biomeConfig.getValue().isIsleBiome())
			{
				// Make or get a list for this group depth, then add
				List<BiomeData> biomesAtDepth = isleBiomesAtDepth.getOrDefault(worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getValue().getBiomeSize() : biomeConfig.getValue().getBiomeSizeWhenIsle(), new ArrayList<>());
				biomesAtDepth.add(
					new BiomeData(
						otgBiomeId,
						worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getValue().getBiomeRarity() : biomeConfig.getValue().getBiomeRarityWhenIsle(),
						worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getValue().getBiomeSize() : biomeConfig.getValue().getBiomeSizeWhenIsle(), 
						biomeConfig.getValue().getBiomeTemperature(), 
						biomeConfig.getValue().getIsleInBiomes(), 
						biomeConfig.getValue().getBorderInBiomes(),
						biomeConfig.getValue().getOnlyBorderNearBiomes(),
						biomeConfig.getValue().getNotBorderNearBiomes()
					)
				);
				isleBiomesAtDepth.put(worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getValue().getBiomeSize() : biomeConfig.getValue().getBiomeSizeWhenIsle(), biomesAtDepth);
			}

			if(biomeConfig.getValue().isBorderBiome())
			{
				// Make or get a list for this group depth, then add
				List<BiomeData> biomesAtDepth = borderBiomesAtDepth.getOrDefault(worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getValue().getBiomeSize() : biomeConfig.getValue().getBiomeSizeWhenBorder(), new ArrayList<>());
				biomesAtDepth.add(
					new BiomeData(
						otgBiomeId,
						biomeConfig.getValue().getBiomeRarity(),
						worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getValue().getBiomeSize() : biomeConfig.getValue().getBiomeSizeWhenBorder(), 
						biomeConfig.getValue().getBiomeTemperature(), 
						biomeConfig.getValue().getIsleInBiomes(), 
						biomeConfig.getValue().getBorderInBiomes(),
						biomeConfig.getValue().getOnlyBorderNearBiomes(),
						biomeConfig.getValue().getNotBorderNearBiomes()
					)
				);
				borderBiomesAtDepth.put(worldConfig.getBiomeMode() == BiomeMode.NoGroups ? biomeConfig.getValue().getBiomeSize() : biomeConfig.getValue().getBiomeSizeWhenBorder(), biomesAtDepth);
			}
			
			// Index BiomeColor for FromImageMode and /otg map
			biomeColorMap.put(biomeConfig.getValue().getBiomeColor(), otgBiomeId);
			
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
			{
				OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Registered biome " + resourceLocation.toString() + " | " + biomeConfig.getValue().getName() + " with OTG id " + otgBiomeId);
			}
			
			currentId += isOceanBiome ? 0 : 1;
		}

		// If the ocean config is null, shift the array downwards to fill id 0
		if (oceanBiomeConfig == null)
		{
			System.arraycopy(presetIdMapping, 1, presetIdMapping, 0, presetIdMapping.length - 1);
		}
		
		this.globalIdMapping.put(preset.getFolderName(), presetIdMapping);

		// Set the base data
		BiomeLayerData data = new BiomeLayerData(preset.getPresetFolder(), worldConfig, oceanBiomeConfig, oceanTemperatures);
		
		Set<Integer> biomeDepths = new HashSet<>();
		Map<Integer, List<NewBiomeGroup>> groupDepths = new HashMap<>();

		int genDepth = worldConfig.getGenerationDepth();

		// Iterate through the groups and add it to the layer data
		// TODO: Refactor BiomeGroupManager to IBiomeGroupManager/IBiomeGroup to avoid WorldConfig cast?
		for (BiomeGroup group : ((WorldConfig)worldConfig).getBiomeGroupManager().getGroups())
		{
			// Initialize biome group data
			NewBiomeGroup bg = new NewBiomeGroup();
			bg.id = group.getGroupId();
			bg.rarity = group.getGroupRarity();

			// init to genDepth as it will have one value per depth
			bg.totalDepthRarity = new int[genDepth + 1];
			bg.maxRarityPerDepth = new int[genDepth + 1];

			float totalTemp = 0;

			HashMap<String, IBiomeConfig> groupBiomes = new LinkedHashMap<String, IBiomeConfig>();
			for (String biomeEntry : group.getBiomes())
			{
				Set<ResourceKey<BiomeBase>> biomesForTags = new HashSet<>();
				if(
					biomeEntry.toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL) ||
					biomeEntry.toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL) ||
					biomeEntry.toLowerCase().startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
					biomeEntry.toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL)
				)
				{					
					String[] tagStrings = biomeEntry.split(" ");
					for(String tagString : tagStrings)
					{
						if(
							tagString.trim().toLowerCase().toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL) ||
							tagString.trim().toLowerCase().toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL)
						)
						{
							BiomeBase.Geography category = 	BiomeBase.Geography.a(tagString.trim().toLowerCase().toLowerCase().replace(Constants.MC_BIOME_CATEGORY_LABEL, "").replace(Constants.BIOME_CATEGORY_LABEL, ""));
							if(category != null)
							{
								biomesForTags.addAll(
										biomeRegistry.g()
										.filter(a -> 
											a.t() == category &&
											!blackListedBiomes.contains(biomeRegistry.getKey(a).toString()) &&
											!biomeRegistry.getKey(a).getNamespace().equals(Constants.MOD_ID_SHORT) &&
											(tagString.trim().toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) || !biomeRegistry.getKey(a).getNamespace().equals("minecraft"))
										).map(
											b -> ResourceKey.a(BIOME_KEY, biomeRegistry.getKey(b))
										).collect(Collectors.toList())
								);
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
								{
									OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "No biome category found for " + tagString + " in world config for preset " + preset.getFolderName());
								}
							}
						}
					}
					if(biomesForTags != null)
					{
						for(ResourceKey<BiomeBase> biomeForTag : biomesForTags)
						{
							BiomeBase biome = biomeRegistry.a(biomeForTag);
							if(group.temperatureAllowed(biome.k()))
							{
								String otgBiomeName = biomeRegistry.getKey(biome).getNamespace() + "." + biomeRegistry.getKey(biome).getKey();
								for(IBiomeConfig biomeConfig : biomeConfigsByResourceLocation.values())
								{
									if(
										biomeConfig.getRegistryKey().toResourceLocationString().equals(biomeForTag.a().toString()) &&
										!groupBiomes.containsKey(otgBiomeName)
									)
									{
										groupBiomes.put(otgBiomeName, biomeConfig);
									}
								}
							}
						}
					} else {
						if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
						{
							OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "No tags/categories found for TemplateForBiome: " + biomeEntry + " in world config for preset " + preset.getFolderName());
						}
					}
				} else {
					IBiomeConfig config = biomeConfigsByName.get(biomeEntry);
					if(config != null)
					{
						groupBiomes.put(biomeEntry, config);	
					}
				}
			}

			// Add each biome to the group
			for (Entry<String, IBiomeConfig> biome : groupBiomes.entrySet())
			{
				if(biome.getValue() != null)
				{
					IBiomeConfig config = biome.getValue();
					// Make and add the generation data
					BiomeData newBiomeData = new BiomeData(
						config.getOTGBiomeId(),
						config.getBiomeRarity(),
						config.getBiomeSize(),
						config.getBiomeTemperature(),
						config.getIsleInBiomes(),
						config.getBorderInBiomes(),
						config.getOnlyBorderNearBiomes(),
						config.getNotBorderNearBiomes()
					);
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

			bg.avgTemp = totalTemp / group.getBiomes().size();

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

	public IBiomeConfig getBiomeConfig(BiomeBase biome)
	{
		return this.biomeConfigsByBiome.get(biome);
	}	

	public List<ResourceKey<BiomeBase>> getBiomeRegistryKeys (String presetFolderName)
	{
		return this.biomesByPresetFolderName.get(presetFolderName);
	}

	public IBiome[] getGlobalIdMapping (String presetFolderName)
	{
		return globalIdMapping.get(presetFolderName);
	}

	public Map<String, BiomeLayerData> getPresetGenerationData ()
	{
		Map<String, BiomeLayerData> clonedData = new HashMap<>();
		for (Map.Entry<String, BiomeLayerData> entry : this.presetGenerationData.entrySet())
		{
			clonedData.put(entry.getKey(), new BiomeLayerData(entry.getValue()));
		}
		return clonedData;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void mergeVanillaBiomeMobSpawnSettings (BiomeConfigFinder.BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{
		String[] resourceLocationArr = biomeResourceLocation.split(":");			
		String resourceDomain = resourceLocationArr.length > 1 ? resourceLocationArr[0] : null;
		String resourceLocation = resourceLocationArr.length > 1 ? resourceLocationArr[1] : resourceLocationArr[0];		
		
		NamespacedKey location = null;
		try
		{
			location = new NamespacedKey(resourceDomain, resourceLocation);
		}
		catch(IllegalArgumentException ex)
		{
			// Can happen when input is invalid.
		}
		
		if(location != null)
		{
			Biome biome = Registry.BIOME.get(location);
			BiomeBase biomeBase = null;
			if(biome != null)
			{
				biomeBase = RegistryGeneration.WORLDGEN_BIOME.get(new MinecraftKey(biome.getKey().toString()));		
			}
			if(biomeBase != null)
			{
				// Merge the vanilla biome's mob spawning lists with the mob spawning lists from the BiomeConfig.
				// Mob spawning settings for the same creature will not be inherited (so BiomeConfigs can override vanilla mob spawning settings).
				// We also inherit any mobs that have been added to vanilla biomes' mob spawning lists by other mods.
				
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.MONSTER), EntityCategory.MONSTER);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.AMBIENT), EntityCategory.AMBIENT_CREATURE);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.CREATURE), EntityCategory.CREATURE);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.WATER_AMBIENT), EntityCategory.WATER_AMBIENT);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.WATER_CREATURE), EntityCategory.WATER_CREATURE);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.MISC), EntityCategory.MISC);
				return;
			}
		}
		if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MOBS))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MOBS, "Could not inherit mobs for unrecognised biome \"" +  biomeResourceLocation + "\" in " + biomeConfigStub.getBiomeName() + Constants.BiomeConfigFileExtension);
		}
	}
}
