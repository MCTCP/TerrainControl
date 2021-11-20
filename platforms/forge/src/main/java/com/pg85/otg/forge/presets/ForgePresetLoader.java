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
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.serialization.Lifecycle;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.biome.TemplateBiome;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.world.WorldConfig;
import com.pg85.otg.core.presets.LocalPresetLoader;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.materials.ForgeMaterialReader;
import com.pg85.otg.forge.network.BiomeSettingSyncWrapper;
import com.pg85.otg.forge.network.OTGClientSyncManager;
import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.gen.biome.layers.BiomeLayerData;
import com.pg85.otg.gen.biome.layers.NewBiomeGroup;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IBiomeResourceLocation;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.biome.MCBiomeResourceLocation;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.EntityCategory;

import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgePresetLoader extends LocalPresetLoader
{
	private Map<String, List<ResourceKey<Biome>>> biomesByPresetFolderName = new LinkedHashMap<>();
	private HashMap<String, IBiome[]> globalIdMapping = new HashMap<>();
	private Map<String, BiomeLayerData> presetGenerationData = new HashMap<>();

	public ForgePresetLoader(Path otgRootFolder)
	{
		super(otgRootFolder);
	}

	// Creates a preset-specific materialreader, have to do this
	// only when loading each preset since each preset may have
	// its own block fallbacks / block dictionaries.
	@Override
	public IMaterialReader createMaterialReader()
	{
		return new ForgeMaterialReader();
	}

	public List<ResourceKey<Biome>> getBiomeRegistryKeys(String presetFolderName)
	{
		return this.biomesByPresetFolderName.get(presetFolderName);
	}

	public IBiome[] getGlobalIdMapping(String presetFolderName)
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
	public void reloadPresetFromDisk(String presetFolderName, IConfigFunctionProvider biomeResourcesManager, ILogger logger, WritableRegistry<Biome> biomeRegistry)
	{
		clearCaches();
		
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
							Preset preset = loadPreset(presetDir.toPath(), biomeResourcesManager, logger);
							Preset existingPreset = this.presets.get(preset.getFolderName());
							existingPreset.update(preset);
							break;
						}
					}
				}
			}
		}
		registerBiomes(true, biomeRegistry);
	}

	protected void clearCaches()
	{
		this.globalIdMapping = new HashMap<>();
		this.presetGenerationData = new HashMap<>();
		this.biomesByPresetFolderName = new LinkedHashMap<>();
		this.materialReaderByPresetFolderName = new HashMap<>();
	}

	public void reRegisterBiomes(String presetFolderName, WritableRegistry<Biome> biomeRegistry)
	{
		this.globalIdMapping.remove(presetFolderName);
		this.presetGenerationData.remove(presetFolderName);
		this.biomesByPresetFolderName.remove(presetFolderName);
		
		registerBiomes(true, biomeRegistry);
	}
	
	@Override
	public void registerBiomes()
	{
		registerBiomes(false, null);
	}

	private void registerBiomes(boolean refresh, WritableRegistry<Biome> biomeRegistry)
	{
		for(Preset preset : this.presets.values())
		{
			registerBiomesForPreset(refresh, preset, biomeRegistry);
		}
	}
	
	private void registerBiomesForPreset(boolean refresh, Preset preset, WritableRegistry<Biome> biomeRegistry)
	{
		// Index BiomeColors for FromImageMode and /otg map
		HashMap<Integer, Integer> biomeColorMap = new HashMap<Integer, Integer>();
		
		// Start at 1, 0 is the fallback for the biome generator (the world's ocean biome).
		int currentId = 1;
		
		List<ResourceKey<Biome>> presetBiomes = new ArrayList<>();
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
		// biome configs for any non-otg biomes targeted via TemplateForBiome.
		Map<IBiomeResourceLocation, IBiomeConfig> biomeConfigsByResourceLocation = new LinkedHashMap<>();
		List<String> blackListedBiomes = worldConfig.getBlackListedBiomes();

		processTemplateBiomes(preset.getFolderName(), worldConfig, biomeConfigs, biomeConfigsByResourceLocation, biomeConfigsByName, blackListedBiomes);
		
		for(IBiomeConfig biomeConfig : biomeConfigs)
		{
			if(!biomeConfig.getIsTemplateForBiome())
			{
				// Normal OTG biome, not a template biome.
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
			ResourceLocation resourceLocation = new ResourceLocation(biomeConfig.getKey().toResourceLocationString());
			ResourceKey<Biome> registryKey;
			Biome biome;
			if(biomeConfig.getValue().getIsTemplateForBiome())
			{
				if(refresh)
				{
					biome = biomeRegistry.get(resourceLocation);
					Optional<RegistryKey<Biome>> key = biomeRegistry.getResourceKey(biome);
					registryKey = key.isPresent() ? key.get() : null;
				} else {
					biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
					// TODO: Can we not fetch an existing key?
					registryKey = RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation);
				}
			} else {
				if(!(biomeConfig.getKey() instanceof OTGBiomeResourceLocation))
				{
					if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
					{
						OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.BIOME_REGISTRY, "Could not process template biomeconfig " + biomeConfig.getValue().getName() + ", did you set TemplateForBiome:true in the BiomeConfig?");
					}
					continue;
				}
				if(OTG.getEngine().getPluginConfig().getDeveloperModeEnabled())
				{
					// For developer-mode, always re-create OTG biomes, to pick up any config changes.
					// This does break any kind of datapack support we might implement for OTG biomes.
					biome = ForgeBiome.createOTGBiome(isOceanBiome, preset.getWorldConfig(), biomeConfig.getValue());
					registryKey = RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation);		
					if(refresh)
					{
						biomeRegistry.registerOrOverride(OptionalInt.empty(), registryKey, biome, Lifecycle.stable());
					} else {	 			
						ForgeRegistries.BIOMES.register(biome);
					}
				} else {
					if(refresh)
					{
						biome = biomeRegistry.get(resourceLocation);
						Optional<RegistryKey<Biome>> key = biomeRegistry.getResourceKey(biome);
						registryKey = key.isPresent() ? key.get() : null;
					} else {
						biome = ForgeBiome.createOTGBiome(isOceanBiome, preset.getWorldConfig(), biomeConfig.getValue());
						registryKey = RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation);
						ForgeRegistries.BIOMES.register(biome);
					}
				}
			}
			if(biome == null || registryKey == null)
			{
				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
				{
					OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.BIOME_REGISTRY, "Could not find biome " + resourceLocation.toString() + " for biomeconfig " + biomeConfig.getValue().getName());
				}
				continue;
			}
			presetBiomes.add(registryKey);
			biomeConfig.getValue().setRegistryKey(biomeConfig.getKey());
			biomeConfig.getValue().setOTGBiomeId(otgBiomeId);

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

			if(biomeConfig.getKey() instanceof OTGBiomeResourceLocation)
			{
				// For OTG biomes, add Forge biome dictionary tags.
				biomeConfig.getValue().getBiomeDictTags().forEach(biomeDictId -> {
					if(biomeDictId != null && biomeDictId.trim().length() > 0)
					{
						BiomeDictionary.addTypes(registryKey, BiomeDictionary.Type.getType(biomeDictId.trim()));
					}
				});
			}

			IBiome otgBiome = new ForgeBiome(biome, biomeConfig.getValue());
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

		// Iterate through the groups and add it to the layer data
		processBiomeGroups(preset.getFolderName(), worldConfig, biomeConfigsByResourceLocation, biomeConfigsByName, blackListedBiomes, biomeDepths, groupDepths, data);
		
		// Add the data and process isle/border biomes
		data.init(biomeDepths, groupDepths, isleBiomesAtDepth, borderBiomesAtDepth, worldBiomes, biomeColorMap, presetIdMapping);

		// Set data for this preset
		this.presetGenerationData.put(preset.getFolderName(), data);
	}

	private void processTemplateBiomes(String presetFolderName, IWorldConfig worldConfig, List<IBiomeConfig> biomeConfigs, Map<IBiomeResourceLocation, IBiomeConfig> biomeConfigsByResourceLocation, Map<String, IBiomeConfig> biomeConfigsByName, List<String> blackListedBiomes)
	{
		for (TemplateBiome templateBiome : ((WorldConfig)worldConfig).getTemplateBiomes())
		{
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
			{
				OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Processing " + templateBiome.toString());
			}
			
			List<String> templateBiomeTagStrings = templateBiome.getTags();
			IBiomeConfig biomeConfig = biomeConfigs.stream().filter(a -> a.getName().toLowerCase().trim().equals(templateBiome.getName().toLowerCase().trim())).findFirst().orElse(null);
			if(biomeConfig == null)
			{
				continue;
			}
			List<String> allowedMods = new ArrayList<>();
			List<Biome> excludedBiomes = new ArrayList<>();
			List<Biome.BiomeCategory> excludedCategories = new ArrayList<>();
			List<BiomeDictionary.Type> excludedTags = new ArrayList<>();
			List<String> excludedMods = new ArrayList<>();
			List<String> tagStrings = new ArrayList<>();
			for(String tagString : templateBiomeTagStrings)
			{
				String tagString2 = tagString.trim().toLowerCase();
				String[] tagSubStrings = tagString2.split(" ");
				if(tagSubStrings.length == 1)
				{
					if(!tagString2.startsWith(Constants.LABEL_EXCLUDE))
					{
						// Handle biome registry names: minecraft:plains
						if(
							!tagString2.startsWith(Constants.MOD_LABEL) &&
							!tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL) &&
							!tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL) &&
							!tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL) &&
							!tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL) &&
							!tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) &&
							!tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) &&
							!tagString2.startsWith(Constants.MOD_LABEL_EXCLUDE) &&
							!tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) &&
							!tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) &&
							!tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE) &&
							!tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) &&
							!tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) &&
							!tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)					
						) {
							// Add biome, don't care about excludes.
							ResourceLocation resourceLocation = new ResourceLocation(tagString2.replace("minecraft:", "").replace(" ", "_"));
							Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
							if(biome != null)
							{
								IBiomeResourceLocation otgLocation = new MCBiomeResourceLocation(biome.getRegistryName().getNamespace(), biome.getRegistryName().getPath(), presetFolderName);
								if(!biomeConfigsByResourceLocation.containsKey(otgLocation))
								{
									if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
									{
										OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome " + biome.getRegistryName().toString() + " found for " + templateBiome.toString() + " with entry " + tagString);
									}
									biomeConfigsByResourceLocation.put(otgLocation, biomeConfig.createTemplateBiome());
									biomeConfigsByName.put(biomeConfig.getName(), biomeConfig);
								}
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
								{
									OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "No biome found for TemplateBiome() " + templateBiome.toString() + " with entry " + tagString);
								}
							}
						}
						else if(tagString2.startsWith(Constants.MOD_LABEL))
						{
							allowedMods.add(tagString2.replace(Constants.MOD_LABEL, ""));
						} else {
							tagStrings.add(tagString);
						}
					} else  {
						if(
							tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)
						)
						{
							// Exclude biome
							ResourceLocation resourceLocation = new ResourceLocation(tagString2.replace("minecraft:", "").replace(" ", "_").substring(1));
							Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
							if(biome != null)
							{
								excludedBiomes.add(biome);
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
								{
									OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome " + biome.getRegistryName().toString() + " excluded for " + templateBiome.toString());
								}
							}
						} else if(
							tagString2.toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagString2.toLowerCase().startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagString2.toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE)
						)
						{
							// Exclude biome category
							Biome.BiomeCategory category = Biome.BiomeCategory.byName(tagString2.toLowerCase().replace(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE, "").replace(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE, "").replace(Constants.BIOME_CATEGORY_LABEL_EXCLUDE, ""));
							if(category != null)
							{
								excludedCategories.add(category);
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
								{
									OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "Biome category " + tagString +  " for " + templateBiome.toString() + " could not be found.");
								}
							}
						} else if(
							tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)
						)
						{
							// Exclude tag
							BiomeDictionary.Type tag = BiomeDictionary.Type.getType(tagString2.replace(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE, "").replace(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE, "").replace(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE, ""));
							if(tag != null)
							{
								excludedTags.add(tag);
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
								{
									OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Biome tag " + tagString +  " for " + templateBiome.toString() + " could not be found.");
								}
							}
						}					
						else if(tagString2.startsWith(Constants.MOD_LABEL_EXCLUDE))
						{
							// Exclude mod
							excludedMods.add(tagString2.replace(Constants.MOD_LABEL_EXCLUDE, ""));
						}
					}
				} else {
					tagStrings.add(tagString);
				}
			}
			for(String tagString : tagStrings)
			{
				String tagString2 = tagString.trim().toLowerCase();
				String[] tagSubStrings = tagString.split(" ");
				if(
					tagString2.startsWith(Constants.MOD_LABEL) ||
					tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL) ||
					tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL) ||
					tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL) ||
					tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
					tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) ||
					tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) ||
					tagString2.startsWith(Constants.MOD_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)					
				) {
					Set<ResourceKey<Biome>> biomesForTags = new HashSet<>();
					List<Biome.BiomeCategory> innerExcludedCategories = new ArrayList<>();
					List<BiomeDictionary.Type> innerExcludedTags = new ArrayList<>();
					List<String> innerExcludedMods = new ArrayList<>();
					// Handle biome category
					for(String tagSubString : tagSubStrings)
					{
						String tagSubString2 = tagSubString.trim().toLowerCase().toLowerCase();
						if(
							tagSubString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagSubString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagSubString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE)
						)
						{
							Biome.BiomeCategory category = Biome.BiomeCategory.byName(tagSubString2.replace(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE, "").replace(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE, "").replace(Constants.BIOME_CATEGORY_LABEL_EXCLUDE, ""));
							if(category != null)
							{								
								innerExcludedCategories.add(category);
								biomesForTags.stream().filter(a -> ForgeRegistries.BIOMES.getValue(a.location()) != null && ForgeRegistries.BIOMES.getValue(a.location()).getBiomeCategory() != category).collect(Collectors.toList());
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
								{
									OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "Biome category " + tagSubString +  " for " + templateBiome.toString() + " could not be found.");
								}
							}
						} else if(
							tagSubString2.startsWith(Constants.BIOME_CATEGORY_LABEL) ||
							tagSubString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL) ||
							tagSubString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL)
						)
						{
							Biome.BiomeCategory category = Biome.BiomeCategory.byName(tagSubString2.replace(Constants.MOD_BIOME_CATEGORY_LABEL, "").replace(Constants.MC_BIOME_CATEGORY_LABEL, "").replace(Constants.BIOME_CATEGORY_LABEL, ""));
							if(category != null)
							{
								biomesForTags.addAll(
									ForgeRegistries.BIOMES.getValues().stream()
										.filter(biome -> 
											biome.getBiomeCategory() == category &&
											!excludedBiomes.contains(biome) &&
											!excludedCategories.contains(biome.getBiomeCategory()) &&
											!innerExcludedCategories.contains(biome.getBiomeCategory()) &&
											excludedTags.stream().allMatch(type -> !BiomeDictionary.hasType(ResourceKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName()), type)) &&
											!blackListedBiomes.contains(biome.getRegistryName().toString()) &&
											!biome.getRegistryName().getNamespace().equals(Constants.MOD_ID_SHORT) &&
											(
												allowedMods.size() == 0 ||
												allowedMods.stream().anyMatch(mod -> biome.getRegistryName().getNamespace().equals(mod))
											) && (
												excludedMods.size() == 0 ||
												!excludedMods.stream().anyMatch(mod -> biome.getRegistryName().getNamespace().equals(mod))
											)
										).map(
											b -> ResourceKey.create(Registry.BIOME_REGISTRY, b.getRegistryName())
										).collect(Collectors.toList())
								);
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
								{
									OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "Biome category " + tagSubString +  " could not be found for " + templateBiome.toString() + " .");
								}
							}
						}
					}
					// Handle biome dictionary tags
					List<BiomeDictionary.Type> tags = new ArrayList<>();
					List<String> tagsStrings = new ArrayList<>();
					for(String tagSubString : tagSubStrings)
					{
						String tagSubString2 = tagSubString.trim().toLowerCase().toLowerCase();
						if(
							tagSubString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagSubString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagSubString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)
						)
						{
							BiomeDictionary.Type tag = BiomeDictionary.Type.getType(tagSubString2.replace(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE, "").replace(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE, "").replace(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE, ""));
							if(tag != null)
							{
								innerExcludedTags.add(tag);
								biomesForTags = biomesForTags.stream().filter(key -> !BiomeDictionary.hasType(key, tag)).collect(Collectors.toSet());
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
								{
									OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "Biome tag " + tagSubString +  " could not be found for " + templateBiome.toString() + " .");
								}
							}
						} else if(
							tagSubString2.startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
							tagSubString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) ||
							tagSubString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL)
						)
						{
							BiomeDictionary.Type tag = BiomeDictionary.Type.getType(tagSubString2.replace(Constants.MOD_BIOME_DICT_TAG_LABEL, "").replace(Constants.MC_BIOME_DICT_TAG_LABEL, "").replace(Constants.BIOME_DICT_TAG_LABEL, ""));
							if(tag != null)
							{
								tags.add(tag);
								tagsStrings.add(tagSubString2);
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
								{
									OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "Biome tag " + tagSubString +  " could not be found for " + templateBiome.toString() + " .");
								}
							}
						}
					}
					List<String> innerMods = new ArrayList<>();
					for(String tagSubString : tagSubStrings)
					{
						String tagSubString2 = tagSubString.trim().toLowerCase();
						if(tagSubString2.startsWith(Constants.MOD_LABEL_EXCLUDE))
						{
							innerExcludedMods.add(tagSubString2.replace(Constants.MOD_LABEL_EXCLUDE, ""));
							biomesForTags = biomesForTags.stream().filter(key -> !key.location().getNamespace().equals(tagSubString2.replace(Constants.MOD_LABEL_EXCLUDE, ""))).collect(Collectors.toSet());							
						}
						else if(tagSubString2.startsWith(Constants.MOD_LABEL))
						{
							innerMods.add(tagSubString2.replace(Constants.MOD_LABEL, ""));							
						}
					}
					if(innerMods.size() > 0)
					{
						biomesForTags = biomesForTags.stream().filter(key -> innerMods.stream().anyMatch(a -> a.equals(key.location().getNamespace()))).collect(Collectors.toSet());
					}					
					if(tags.size() > 0)
					{
						String tagType = tagsStrings.get(0);
						biomesForTags.addAll(
							BiomeDictionary.getBiomes(tags.get(0)).stream()
							.filter(key -> 
								tags.stream().allMatch(tag -> BiomeDictionary.hasType(key, tag)) &&
								ForgeRegistries.BIOMES.getValue(key.location()) != null &&
								!excludedBiomes.contains(ForgeRegistries.BIOMES.getValue(key.location())) &&
								!excludedCategories.contains(ForgeRegistries.BIOMES.getValue(key.location()).getBiomeCategory()) &&
								!innerExcludedCategories.contains(ForgeRegistries.BIOMES.getValue(key.location()).getBiomeCategory()) &&
								excludedTags.stream().allMatch(type -> !BiomeDictionary.hasType(ResourceKey.create(Registry.BIOME_REGISTRY, key.location()), type)) &&
								innerExcludedTags.stream().allMatch(type -> !BiomeDictionary.hasType(ResourceKey.create(Registry.BIOME_REGISTRY, key.location()), type)) &&
								!blackListedBiomes.contains(key.location().toString()) &&
								!key.location().getNamespace().equals(Constants.MOD_ID_SHORT) &&
								(
									allowedMods.size() == 0 ||
									allowedMods.stream().anyMatch(mod -> key.location().getNamespace().equals(mod))
								) && (
									excludedMods.size() == 0 ||
									!excludedMods.stream().anyMatch(mod -> key.location().getNamespace().equals(mod))
								) && (
									innerMods.size() == 0 ||
									innerMods.stream().anyMatch(mod -> key.location().getNamespace().equals(mod))									
								) && (
									innerExcludedMods.size() == 0 ||
									!innerExcludedMods.stream().anyMatch(mod -> key.location().getNamespace().equals(mod))											
								) && (
									tagType.startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
									(
										(
											tagType.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) && !key.location().getNamespace().equals("minecraft")
										) || (
											tagType.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) && key.location().getNamespace().equals("minecraft")	
										)
									)
								)
							).collect(Collectors.toList())
						);
					}
					if(biomesForTags.size() > 0)
					{
						for(ResourceKey<Biome> biomeForTag : biomesForTags)
						{
							Biome biome = ForgeRegistries.BIOMES.getValue(biomeForTag.location());
							// Check for temperature range and add biome, if it hasn't already been added by a previous entry.
							if(biome != null && templateBiome.temperatureAllowed(biome.getBaseTemperature()))
							{
								IBiomeResourceLocation otgLocation = new MCBiomeResourceLocation(biomeForTag.location().getNamespace(), biomeForTag.location().getPath(), presetFolderName);
								if(!biomeConfigsByResourceLocation.containsKey(otgLocation))
								{
									if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
									{
										OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome " + biomeForTag.location().toString() + " found for " + templateBiome.toString() + " with temperature: " + biome.getBaseTemperature() + " Category: " + biome.getBiomeCategory() + " Tags: " + String.join(",", BiomeDictionary.getTypes(RegistryKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName())).stream().map(a -> a.getName()).collect(Collectors.toList())));
									}										
									biomeConfigsByResourceLocation.put(otgLocation, biomeConfig.createTemplateBiome());
									biomeConfigsByName.put(biomeConfig.getName(), biomeConfig);
								}
							}
						}
					} else {
						if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
						{
							OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "No tags or categories found for " + templateBiome.toString());
						}
					}
				} else {
					if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
					{
						OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "No tags or categories found for " + templateBiome.toString());
					}
				}
			}
		}
	}

	private void processBiomeGroups(String presetFolderName, IWorldConfig worldConfig, Map<IBiomeResourceLocation, IBiomeConfig> biomeConfigsByResourceLocation, Map<String, IBiomeConfig> biomeConfigsByName, List<String> blackListedBiomes, Set<Integer> biomeDepths, Map<Integer, List<NewBiomeGroup>> groupDepths, BiomeLayerData data)
	{
		int genDepth = worldConfig.getGenerationDepth();
		// TODO: Refactor BiomeGroupManager to IBiomeGroupManager/IBiomeGroup to avoid WorldConfig cast?
		for (BiomeGroup group : ((WorldConfig)worldConfig).getBiomeGroupManager().getGroups())
		{
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
			{
				OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Processing " + group.toString());
			}
			
			// Initialize biome group data
			NewBiomeGroup bg = new NewBiomeGroup();
			bg.id = group.getGroupId();
			bg.rarity = group.getGroupRarity();

			// init to genDepth as it will have one value per depth
			bg.totalDepthRarity = new int[genDepth + 1];
			bg.maxRarityPerDepth = new int[genDepth + 1];

			float totalTemp = 0;

			HashMap<String, IBiomeConfig> groupBiomes = new LinkedHashMap<String, IBiomeConfig>();
			List<String> templateBiomeTagStrings = group.getBiomes();
			List<String> allowedMods = new ArrayList<>();
			List<Biome> excludedBiomes = new ArrayList<>();
			List<Biome.BiomeCategory> excludedCategories = new ArrayList<>();
			List<BiomeDictionary.Type> excludedTags = new ArrayList<>();
			List<String> excludedMods = new ArrayList<>();
			List<String> tagStrings = new ArrayList<>();
			for(String tagString : templateBiomeTagStrings)
			{
				String tagString2 = tagString.trim().toLowerCase();
				String[] tagSubStrings = tagString2.split(" ");
				if(!tagString2.startsWith(Constants.LABEL_EXCLUDE))
				{
					// Handle biome registry names: minecraft:plains
					if(
						!tagString2.startsWith(Constants.MOD_LABEL) &&
						!tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL) &&
						!tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL) &&
						!tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL) &&
						!tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL) &&
						!tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) &&
						!tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) &&
						!tagString2.startsWith(Constants.MOD_LABEL_EXCLUDE) &&
						!tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) &&
						!tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) &&
						!tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE) &&
						!tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) &&
						!tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) &&
						!tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)					
					) {
						// Add biome, don't care about excludes.
						// Check for biomeconfig name, if none then use  
						// resourcelocation to look up template biome config.
						IBiomeConfig biomeConfig = biomeConfigsByName.get(tagString.trim());
						if(biomeConfig != null)
						{
							if(biomeConfig.getIsTemplateForBiome())
							{
								// For template biome configs, fetch all associated biomes and add them.
								for(Entry<IBiomeResourceLocation, IBiomeConfig> entry : biomeConfigsByResourceLocation.entrySet())
								{
									if(entry.getValue().getName().equals(biomeConfig.getName()))
									{
										if(!groupBiomes.containsKey(entry.getKey().toResourceLocationString()))
										{
											if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
											{
												OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "BiomeConfig " + biomeConfig.getName() + " found for entry " + tagString + " in group " + group.getName());
											}
											groupBiomes.put(entry.getKey().toResourceLocationString(), entry.getValue());
										}
									}
								}
							} else {
								if(!groupBiomes.containsKey(tagString.trim()))
								{
									if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
									{
										OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "BiomeConfig " + biomeConfig.getName() + " found for entry " + tagString + " in group " + group.getName());
									}
									groupBiomes.put(tagString.trim(), biomeConfig);
								}
							}
						}
						else if(tagString.trim().contains(":") && tagSubStrings.length == 1)
						{
							String[] resourceLocationParts = tagString.trim().split(":");
							if(resourceLocationParts.length == 2)
							{
								biomeConfig = biomeConfigsByResourceLocation.get(new MCBiomeResourceLocation(resourceLocationParts[0], resourceLocationParts[1], presetFolderName));
								if(biomeConfig != null && !groupBiomes.containsKey(tagString.trim()))
								{
									if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
									{
										OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "BiomeConfig " + biomeConfig.getName() + " found for entry " + tagString + " in group " + group.getName());											
									}
									groupBiomes.put(tagString.trim(), biomeConfig);
								}
							}
						} else {
							if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
							{
								OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "No biomeConfig found for entry " + tagString + " in group " + group.getName());
							}
						}							
					}
					else if(tagString2.startsWith(Constants.MOD_LABEL) && tagSubStrings.length == 1)
					{
						allowedMods.add(tagString2.replace(Constants.MOD_LABEL, ""));
					} else {
						tagStrings.add(tagString);
					}
				} else {
					IBiomeConfig biomeConfig = biomeConfigsByName.get(tagString.trim().replace(Constants.LABEL_EXCLUDE, ""));
					if(biomeConfig != null)
					{
						if(biomeConfig.getIsTemplateForBiome())
						{
							// For template biome configs, fetch all associated biomes and add them.
							for(Entry<IBiomeResourceLocation, IBiomeConfig> entry : biomeConfigsByResourceLocation.entrySet())
							{
								if(entry.getValue().getName().equals(biomeConfig.getName()))
								{
									Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(entry.getKey().toResourceLocationString()));
									if(biome != null)
									{
										excludedBiomes.add(biome);
									}
								}
							}
						} else {
							Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString()));
							if(biome != null)
							{
								excludedBiomes.add(biome);
							}
						}
					}
					else if(tagString.trim().contains(":") && tagSubStrings.length == 1)
					{						
						// Exclude biome
						ResourceLocation resourceLocation = new ResourceLocation(tagString2.replace("minecraft:", "").replace(" ", "_").substring(1));
						Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
						if(biome != null)
						{
							excludedBiomes.add(biome);
						}							
					} else if(
						tagSubStrings.length == 1 &&
						tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
						tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
						tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE) ||
						tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
						tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
						tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)
					)
					{
						// Exclude biome
						ResourceLocation resourceLocation = new ResourceLocation(tagString2.replace("minecraft:", "").replace(" ", "_").substring(1));
						Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
						if(biome != null)
						{
							excludedBiomes.add(biome);
							if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
							{
								OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome " + biome.getRegistryName().toString() + " excluded for entry " + tagString + " in group " + group.getName());
							}
						}
					} else if(
						tagSubStrings.length == 1 &&
						tagString2.toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
						tagString2.toLowerCase().startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
						tagString2.toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE)
					)
					{
						// Exclude biome category
						Biome.BiomeCategory category = Biome.BiomeCategory.byName(tagString2.toLowerCase().replace(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE, "").replace(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE, "").replace(Constants.BIOME_CATEGORY_LABEL_EXCLUDE, ""));
						if(category != null)
						{
							excludedCategories.add(category);
						} else {
							if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
							{
								OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome category " + tagString + " for entry " + tagString + " in group " + group.getName() + " could not be found.");
							}
						}
					} else if(
						tagSubStrings.length == 1 &&
						tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
						tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
						tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)
					)
					{
						// Exclude tag
						BiomeDictionary.Type tag = BiomeDictionary.Type.getType(tagString2.replace(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE, "").replace(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE, "").replace(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE, ""));
						if(tag != null)
						{
							excludedTags.add(tag);
						} else {
							if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
							{
								OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome tag " + tagString + " for entry " + tagString + " in group " + group.getName() + " could not be found.");
							}
						}
					} else if(
						tagSubStrings.length == 1 &&
						tagString2.startsWith(Constants.MOD_LABEL_EXCLUDE)
					)
					{
						// Exclude mod
						excludedMods.add(tagString2.replace(Constants.MOD_LABEL_EXCLUDE, ""));
					} else {
						if(
							tagString2.startsWith(Constants.MOD_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)
						)
						{
							tagStrings.add(tagString);
						}
					}
				}
			}
			for(String tagString : tagStrings)
			{
				String tagString2 = tagString.trim().toLowerCase();
				String[] tagSubStrings = tagString.split(" ");
				if(
					tagString2.startsWith(Constants.MOD_LABEL) ||
					tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL) ||
					tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL) ||
					tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL) ||
					tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
					tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) ||
					tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) ||
					tagString2.startsWith(Constants.MOD_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
					tagString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)					
				) {
					Set<ResourceKey<Biome>> biomesForTags = new HashSet<>();
					List<Biome.BiomeCategory> innerExcludedCategories = new ArrayList<>();
					List<BiomeDictionary.Type> innerExcludedTags = new ArrayList<>();
					List<String> innerExcludedMods = new ArrayList<>();
					// Handle biome category
					for(String tagSubString : tagSubStrings)
					{
						String tagSubString2 = tagSubString.trim().toLowerCase().toLowerCase();
						if(
							tagSubString2.startsWith(Constants.BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagSubString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE) ||
							tagSubString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE)
						)
						{
							Biome.BiomeCategory category = Biome.BiomeCategory.byName(tagSubString2.replace(Constants.MOD_BIOME_CATEGORY_LABEL_EXCLUDE, "").replace(Constants.MC_BIOME_CATEGORY_LABEL_EXCLUDE, "").replace(Constants.BIOME_CATEGORY_LABEL_EXCLUDE, ""));
							if(category != null)
							{
								innerExcludedCategories.add(category);
								biomesForTags.stream().filter(a -> ForgeRegistries.BIOMES.getValue(a.location()) != null && ForgeRegistries.BIOMES.getValue(a.location()).getBiomeCategory() != category).collect(Collectors.toList());
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
								{
									OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome category " + tagString + " in group " + group.getName() + " could not be found.");
								}
							}
						} else if(
							tagSubString2.startsWith(Constants.BIOME_CATEGORY_LABEL) ||
							tagSubString2.startsWith(Constants.MOD_BIOME_CATEGORY_LABEL) ||
							tagSubString2.startsWith(Constants.MC_BIOME_CATEGORY_LABEL)
						)
						{
							Biome.BiomeCategory category = Biome.BiomeCategory.byName(tagSubString2.replace(Constants.MOD_BIOME_CATEGORY_LABEL, "").replace(Constants.MC_BIOME_CATEGORY_LABEL, "").replace(Constants.BIOME_CATEGORY_LABEL, ""));
							if(category != null)
							{
								biomesForTags.addAll(
									ForgeRegistries.BIOMES.getValues().stream()
										.filter(biome -> 
											biome.getBiomeCategory() == category &&
											!excludedBiomes.contains(biome) &&
											!excludedCategories.contains(biome.getBiomeCategory()) &&
											!innerExcludedCategories.contains(biome.getBiomeCategory()) &&
											excludedTags.stream().allMatch(type -> !BiomeDictionary.hasType(ResourceKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName()), type)) &&
											!blackListedBiomes.contains(biome.getRegistryName().toString()) &&
											!biome.getRegistryName().getNamespace().equals(Constants.MOD_ID_SHORT) &&
											(
												allowedMods.size() == 0 ||
												allowedMods.stream().anyMatch(mod -> biome.getRegistryName().getNamespace().equals(mod))
											) && (
												excludedMods.size() == 0 ||
												!excludedMods.stream().anyMatch(mod -> biome.getRegistryName().getNamespace().equals(mod))
											)
										).map(
											b -> ResourceKey.create(Registry.BIOME_REGISTRY, b.getRegistryName())
										).collect(Collectors.toList())
								);
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
								{
									OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome category " + tagSubString + " in group " + group.getName() + " could not be found.");
								}
							}
						}
					}
					// Handle biome dictionary tags
					List<BiomeDictionary.Type> tags = new ArrayList<>();
					List<String> tagsStrings = new ArrayList<>();
					for(String tagSubString : tagSubStrings)
					{
						String tagSubString2 = tagSubString.trim().toLowerCase().toLowerCase();
						if(
							tagSubString2.startsWith(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagSubString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE) ||
							tagSubString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE)
						)
						{
							BiomeDictionary.Type tag = BiomeDictionary.Type.getType(tagSubString2.replace(Constants.MOD_BIOME_DICT_TAG_LABEL_EXCLUDE, "").replace(Constants.MC_BIOME_DICT_TAG_LABEL_EXCLUDE, "").replace(Constants.BIOME_DICT_TAG_LABEL_EXCLUDE, ""));
							if(tag != null)
							{
								innerExcludedTags.add(tag);
								biomesForTags = biomesForTags.stream().filter(key -> !BiomeDictionary.hasType(key, tag)).collect(Collectors.toSet());
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
								{
									OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome tag " + tagSubString + " in group " + group.getName() + " could not be found.");
								}
							}
						} else if(
							tagSubString2.startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
							tagSubString2.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) ||
							tagSubString2.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL)
						)
						{
							BiomeDictionary.Type tag = BiomeDictionary.Type.getType(tagSubString2.replace(Constants.MOD_BIOME_DICT_TAG_LABEL, "").replace(Constants.MC_BIOME_DICT_TAG_LABEL, "").replace(Constants.BIOME_DICT_TAG_LABEL, ""));
							if(tag != null)
							{
								tags.add(tag);
								tagsStrings.add(tagSubString2);
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
								{
									OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Biome tag " + tagSubString + " in group " + group.getName() + " could not be found.");
								}
							}
						}
					}
					List<String> innerMods = new ArrayList<>();
					for(String tagSubString : tagSubStrings)
					{
						String tagSubString2 = tagSubString.trim().toLowerCase();
						if(tagSubString2.startsWith(Constants.MOD_LABEL_EXCLUDE))
						{
							innerExcludedMods.add(tagSubString2.replace(Constants.MOD_LABEL_EXCLUDE, ""));
							biomesForTags = biomesForTags.stream().filter(key -> !key.location().getNamespace().equals(tagSubString2.replace(Constants.MOD_LABEL_EXCLUDE, ""))).collect(Collectors.toSet());
						}						
						else if(tagSubString2.startsWith(Constants.MOD_LABEL))
						{
							innerMods.add(tagSubString2.replace(Constants.MOD_LABEL, ""));
						}
					}
					if(innerMods.size() > 0)
					{
						biomesForTags = biomesForTags.stream().filter(key -> innerMods.stream().anyMatch(a -> a.equals(key.location().getNamespace()))).collect(Collectors.toSet());
					}
					if(tags.size() > 0)
					{
						String tagType = tagsStrings.get(0);
						biomesForTags.addAll(
							BiomeDictionary.getBiomes(tags.get(0)).stream()
							.filter(key -> 
								tags.stream().allMatch(tag -> BiomeDictionary.hasType(key, tag)) &&
								ForgeRegistries.BIOMES.getValue(key.location()) != null &&
								!excludedBiomes.contains(ForgeRegistries.BIOMES.getValue(key.location())) &&
								!excludedCategories.contains(ForgeRegistries.BIOMES.getValue(key.location()).getBiomeCategory()) &&
								!innerExcludedCategories.contains(ForgeRegistries.BIOMES.getValue(key.location()).getBiomeCategory()) &&
								excludedTags.stream().allMatch(type -> !BiomeDictionary.hasType(ResourceKey.create(Registry.BIOME_REGISTRY, key.location()), type)) &&
								innerExcludedTags.stream().allMatch(type -> !BiomeDictionary.hasType(ResourceKey.create(Registry.BIOME_REGISTRY, key.location()), type)) &&
								!blackListedBiomes.contains(key.location().toString()) &&
								!key.location().getNamespace().equals(Constants.MOD_ID_SHORT) &&
								(
									allowedMods.size() == 0 ||
									allowedMods.stream().anyMatch(mod -> key.location().getNamespace().equals(mod))
								) && (
									excludedMods.size() == 0 ||
									!excludedMods.stream().anyMatch(mod -> key.location().getNamespace().equals(mod))
								) && (
									innerMods.size() == 0 ||
									innerMods.stream().anyMatch(mod -> key.location().getNamespace().equals(mod))								
								) && (
									innerExcludedMods.size() == 0 ||
									!innerExcludedMods.stream().anyMatch(mod -> key.location().getNamespace().equals(mod))											
								) && (
									tagType.startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
									(
										(
											tagType.startsWith(Constants.MOD_BIOME_DICT_TAG_LABEL) && !key.location().getNamespace().equals("minecraft")
										) || (
											tagType.startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) && key.location().getNamespace().equals("minecraft")	
										)
									)
								)
							).collect(Collectors.toList())
						);
					}
					if(biomesForTags.size() > 0)
					{
						for(ResourceKey<Biome> biomeForTag : biomesForTags)
						{
							Biome biome = ForgeRegistries.BIOMES.getValue(biomeForTag.location());
							// Check for temperature range and add biome, if it hasn't already been added by a previous entry.
							if(biome != null && group.temperatureAllowed(biome.getBaseTemperature()))
							{
								// Check for biomeconfig name, if none then use  
								// resourcelocation to look up template biome config.
								IBiomeResourceLocation otgLocation = new MCBiomeResourceLocation(biomeForTag.location().getNamespace(), biomeForTag.location().getPath(), presetFolderName);
								IBiomeConfig biomeConfig = biomeConfigsByResourceLocation.get(otgLocation);
								if(biomeConfig != null && !groupBiomes.containsKey(biomeForTag.location().toString()))
								{
									if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
									{
										OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "BiomeConfig " + biomeConfig.getName() + " found for biome " + biome.getRegistryName().toString() + " in group " + group.getName() + " with entry " + tagString);
									}
									groupBiomes.put(biomeForTag.location().toString(), biomeConfig);
								} else if(biomeConfig == null) {
									if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
									{
										OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "No BiomeConfig found for biome " + biome.getRegistryName().toString() + " in group " + group.getName() + " with entry " + tagString);
									}
								}
							}
						}
					} else {
						if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
						{
							OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "No tags or categories found for group " + group.getName() + " with entry " + tagString);
						}
					}
				} else {
					if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
					{
						OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "No tags or categories found for group " + group.getName() + " with entry " + tagString);
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
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, MobCategory.MONSTER), EntityCategory.MONSTER);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, MobCategory.AMBIENT), EntityCategory.AMBIENT);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, MobCategory.CREATURE), EntityCategory.CREATURE);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, MobCategory.UNDERGROUND_WATER_CREATURE), EntityCategory.UNDERGROUND_WATER_CREATURE);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, MobCategory.WATER_AMBIENT), EntityCategory.WATER_AMBIENT);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, MobCategory.WATER_CREATURE), EntityCategory.WATER_CREATURE);
			biomeConfigStub.mergeMobs(getListFromMinecraftBiome(biome, MobCategory.MISC), EntityCategory.MISC);
		} else {
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MOBS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MOBS, "Could not inherit mobs for unrecognised biome \"" +  biomeResourceLocation + "\" in " + biomeConfigStub.getBiomeName() + Constants.BiomeConfigFileExtension);
			}
		}
	}

	private List<WeightedMobSpawnGroup> getListFromMinecraftBiome(Biome biome, MobCategory type)
	{
		WeightedRandomList<SpawnerData> mobList = biome.getMobSettings().getMobs(type);
		List<WeightedMobSpawnGroup> result = new ArrayList<WeightedMobSpawnGroup>();
		for (SpawnerData spawner : mobList.unwrap())
		{
			WeightedMobSpawnGroup wMSG = new WeightedMobSpawnGroup(spawner.type.getRegistryName().toString(), spawner.getWeight().asInt(), spawner.minCount, spawner.maxCount);
			if(wMSG != null)
			{
				result.add(wMSG);
			}
		}
		return result;
	}
}
