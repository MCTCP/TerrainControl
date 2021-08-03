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
import java.util.stream.Collectors;

import com.mojang.serialization.Lifecycle;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
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
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.MCBiomeResourceLocation;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.EntityCategory;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgePresetLoader extends LocalPresetLoader
{
	private Map<String, List<RegistryKey<Biome>>> biomesByPresetFolderName = new LinkedHashMap<>();
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

	public List<RegistryKey<Biome>> getBiomeRegistryKeys(String presetFolderName)
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
	public void reloadPresetFromDisk(String presetFolderName, IConfigFunctionProvider biomeResourcesManager, ILogger logger, MutableRegistry<Biome> biomeRegistry)
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

	public void reRegisterBiomes(String presetFolderName, MutableRegistry<Biome> biomeRegistry)
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

	private void registerBiomes(boolean refresh, MutableRegistry<Biome> biomeRegistry)
	{
		for(Preset preset : this.presets.values())
		{
			registerBiomesForPreset(refresh, preset, biomeRegistry);
		}
	}
	
	private void registerBiomesForPreset(boolean refresh, Preset preset, MutableRegistry<Biome> biomeRegistry)
	{
		// Index BiomeColors for FromImageMode and /otg map
		HashMap<Integer, Integer> biomeColorMap = new HashMap<Integer, Integer>();
		
		// Start at 1, 0 is the fallback for the biome generator (the world's ocean biome).
		int currentId = 1;
		
		List<RegistryKey<Biome>> presetBiomes = new ArrayList<>();
		this.biomesByPresetFolderName.put(preset.getFolderName(), presetBiomes);

		IWorldConfig worldConfig = preset.getWorldConfig();
		IBiomeConfig oceanBiomeConfig = null;
		int[] oceanTemperatures = new int[]{0, 0, 0, 0};
		
		List<IBiomeConfig> biomeConfigs = preset.getAllBiomeConfigs();		

		Map<Integer, List<BiomeData>> isleBiomesAtDepth = new HashMap<>();
		Map<Integer, List<BiomeData>> borderBiomesAtDepth = new HashMap<>();
		
		Map<String, Integer> worldBiomes = new HashMap<>();
		Map<String, IBiomeConfig> biomeConfigsByName = new HashMap<>();
		
		// Create registry keys for each biomeconfig, create template 
		// biome configs for any non-otg biomes targeted via TemplateForBiome.
		Map<IBiomeResourceLocation, IBiomeConfig> biomeConfigsByResourceLocation = new LinkedHashMap<>();
		List<String> blackListedBiomes = worldConfig.getBlackListedBiomes();
		for(IBiomeConfig biomeConfig : biomeConfigs)
		{
			if(biomeConfig.getTemplateForBiome() != null && biomeConfig.getTemplateForBiome().trim().length() > 0)
			{
				String[] tagStrings = biomeConfig.getTemplateForBiome().split(",");
				for(String tagString : tagStrings)
				{
					// Handle biome registry names: minecraft:plains
					if(
						!tagString.trim().toLowerCase().toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL) &&
						!tagString.trim().toLowerCase().toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL) &&
						!tagString.trim().toLowerCase().startsWith(Constants.BIOME_DICT_TAG_LABEL) &&
						!tagString.trim().toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL)
					) {
						ResourceLocation resourceLocation = new ResourceLocation(tagString.replace("minecraft:", "").trim().replace(" ", "_").toLowerCase());
						Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
						if(biome != null)
						{
							IBiomeResourceLocation otgLocation = new MCBiomeResourceLocation(biome.getRegistryName().getNamespace(), biome.getRegistryName().getPath(), preset.getFolderName());
							if(!biomeConfigsByResourceLocation.containsKey(otgLocation))
							{
								biomeConfigsByResourceLocation.put(otgLocation, biomeConfig.createTemplateBiome());
								biomeConfigsByName.put(biomeConfig.getName(), biomeConfig);
							}
						} else {
							if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
							{
								OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "No biome found for TemplateForBiome " + tagString.replace("minecraft:", "").trim().replace(" ", "_").toLowerCase() + " in biome config " + biomeConfig.getName());
							}
						}
					} else {					
						Set<RegistryKey<Biome>> biomesForTags = new HashSet<>();
						String[] tagSubStrings = tagString.split(" ");
						// Handle biome category
						for(String tagSubString : tagSubStrings)
						{
							if(
								tagSubString.trim().toLowerCase().toLowerCase().startsWith(Constants.BIOME_CATEGORY_LABEL) ||
								tagSubString.trim().toLowerCase().toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL)
							)
							{
								Biome.Category category = Biome.Category.byName(tagSubString.trim().toLowerCase().toLowerCase().replace(Constants.MC_BIOME_CATEGORY_LABEL, "").replace(Constants.BIOME_CATEGORY_LABEL, ""));
								if(category != null)
								{
									biomesForTags.addAll(
										ForgeRegistries.BIOMES.getValues().stream()
											.filter(a -> 
												a.getBiomeCategory() == category &&
												!blackListedBiomes.contains(a.getRegistryName().toString()) &&
												!a.getRegistryName().getNamespace().equals(Constants.MOD_ID_SHORT) &&
												(tagSubString.trim().toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL) || !a.getRegistryName().getNamespace().equals("minecraft"))
											).map(
												b -> RegistryKey.create(Registry.BIOME_REGISTRY, b.getRegistryName())
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
						// Handle biome dictionary tags
						List<BiomeDictionary.Type> tags = new ArrayList<>();
						List<Boolean> tagsMC = new ArrayList<>();
						for(String tagSubString : tagSubStrings)
						{
							if(
								tagSubString.trim().toLowerCase().startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
								tagSubString.trim().toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL)
							)
							{
								BiomeDictionary.Type tag = BiomeDictionary.Type.getType(tagSubString.trim().toLowerCase().replace(Constants.MC_BIOME_DICT_TAG_LABEL, "").replace(Constants.BIOME_DICT_TAG_LABEL, ""));
								if(tag != null)
								{
									tags.add(tag);
									tagsMC.add(tagSubString.trim().toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL));
								} else {
									if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
									{
										OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "TemplateForBiome biome tag " + tagSubString +  " for biomeconfig " + biomeConfig.getName() + " could not be found.");
									}
								}
							}
						}
						if(tags.size() > 0)
						{
							// When using a combination of category/tags, filter for biomes that match all.
							biomesForTags.addAll(BiomeDictionary.getBiomes(tags.get(0)));
							biomesForTags = biomesForTags.stream()
								.filter(a ->
									!blackListedBiomes.contains(a.location().toString()) &&
									!a.location().getNamespace().equals(Constants.MOD_ID_SHORT) &&							
									(tagsMC.get(0) || !a.location().getNamespace().equals("minecraft"))
								).collect(Collectors.toSet());
							
							for(int i = 1; i < tags.size(); i++)
							{
								BiomeDictionary.Type tag = tags.get(i);
								boolean allowMCBiomes = tagsMC.get(i);
								biomesForTags = biomesForTags.stream()
									.filter(
										a -> BiomeDictionary.hasType(a, tag) && 
										!blackListedBiomes.contains(a.location().toString()) &&
										!a.location().getNamespace().equals(Constants.MOD_ID_SHORT) &&
										(allowMCBiomes || !a.location().getNamespace().equals("minecraft"))
									).collect(Collectors.toSet());
							}
						}
						if(biomesForTags != null)
						{
							for(RegistryKey<Biome> biomeForTag : biomesForTags)
							{
								Biome biome = ForgeRegistries.BIOMES.getValue(biomeForTag.location());
								// Check for temperature range and add biome, if it hasn't already been added by a previous entry.
								if(biomeConfig.isWithinTemplateForBiomeTemperatureRange(biome.getBaseTemperature()))
								{
									IBiomeResourceLocation otgLocation = new MCBiomeResourceLocation(biomeForTag.location().getNamespace(), biomeForTag.location().getPath(), preset.getFolderName());
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
				}
			} else {
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
			RegistryKey<Biome> registryKey;
			Biome biome;
			if(biomeConfig.getValue().getTemplateForBiome() != null && biomeConfig.getValue().getTemplateForBiome().trim().length() > 0)
			{
				biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
				if(biome == null)
				{
					if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
					{
						OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Could not find biome " + resourceLocation.toString() + " for template biomeconfig " + biomeConfig.getValue().getName());
					}
					continue;
				}
				registryKey = RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation);
				presetBiomes.add(registryKey);
				biomeConfig.getValue().setRegistryKey(biomeConfig.getKey());
				biomeConfig.getValue().setOTGBiomeId(otgBiomeId);
			} else {
				biomeConfig.getValue().setRegistryKey(biomeConfig.getKey());
				biomeConfig.getValue().setOTGBiomeId(otgBiomeId);
 				registryKey = RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation);
				presetBiomes.add(registryKey);
 				biome = ForgeBiome.createOTGBiome(isOceanBiome, preset.getWorldConfig(), biomeConfig.getValue());	 			

				if(!refresh)
				{
					ForgeRegistries.BIOMES.register(biome);
				} else {
					biomeRegistry.registerOrOverride(OptionalInt.empty(), registryKey, biome, Lifecycle.stable());
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

			// Note: BiomeConfigs used with TemplateForBiome may represent
			// multiple otg biome id's, this simply overrides. worldbiomes
			// is used for River/Isle/Border biome settings, so this may 
			// cause unexpected behaviours when using template biomes as 
			// rivers/isles/borders.
			worldBiomes.put(biomeConfig.getValue().getName(), otgBiomeId);
			
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
				Set<RegistryKey<Biome>> biomesForTags = new HashSet<>();
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
							Biome.Category category = Biome.Category.byName(tagString.trim().toLowerCase().toLowerCase().replace(Constants.MC_BIOME_CATEGORY_LABEL, "").replace(Constants.BIOME_CATEGORY_LABEL, ""));
							if(category != null)
							{
								biomesForTags.addAll(
									ForgeRegistries.BIOMES.getValues().stream()
									.filter(
										a -> a.getBiomeCategory() == category &&
										!a.getRegistryName().getNamespace().equals(Constants.MOD_ID_SHORT) &&
										!blackListedBiomes.contains(a.getRegistryName().toString()) &&
										(tagString.trim().toLowerCase().toLowerCase().startsWith(Constants.MC_BIOME_CATEGORY_LABEL) || !a.getRegistryName().getNamespace().equals("minecraft"))
									).map(
										b -> RegistryKey.create(Registry.BIOME_REGISTRY, b.getRegistryName())
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

					List<BiomeDictionary.Type> tags = new ArrayList<>();
					List<Boolean> tagsMC = new ArrayList<>();
					for(String tagString : tagStrings)
					{
						if(
							tagString.trim().toLowerCase().startsWith(Constants.BIOME_DICT_TAG_LABEL) ||
							tagString.trim().toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL)
						)
						{
							BiomeDictionary.Type tag = BiomeDictionary.Type.getType(tagString.trim().toLowerCase().replace(Constants.MC_BIOME_DICT_TAG_LABEL, "").replace(Constants.BIOME_DICT_TAG_LABEL, ""));
							if(tag != null)
							{
								tags.add(tag);
								tagsMC.add(tagString.trim().toLowerCase().startsWith(Constants.MC_BIOME_DICT_TAG_LABEL));
							} else {
								if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
								{
									OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "No biome dictionary tag found for " + tagString + " in world config for preset " + preset.getFolderName());
								}
							}
						}
					}
					if(tags.size() > 0)
					{
						if(biomesForTags.size() == 0)
						{
							biomesForTags.addAll(BiomeDictionary.getBiomes(tags.get(0)));
						}					
						biomesForTags = biomesForTags.stream()
							.filter(a ->
								!blackListedBiomes.contains(a.location().toString()) &&
								!a.location().getNamespace().equals(Constants.MOD_ID_SHORT) &&
								(tagsMC.get(0) || !a.location().getNamespace().equals("minecraft")) 								
							).collect(Collectors.toSet());
						
						for(int i = 1; i < tags.size(); i++)
						{
							BiomeDictionary.Type tag = tags.get(i);
							boolean allowMCBiomes = tagsMC.get(i);
							biomesForTags = biomesForTags.stream()
								.filter(
									a -> BiomeDictionary.hasType(a, tag) &&
									!blackListedBiomes.contains(a.location().toString()) &&
									!a.location().getNamespace().equals(Constants.MOD_ID_SHORT) &&
									(allowMCBiomes || !a.location().getNamespace().equals("minecraft"))
								).collect(Collectors.toSet());
						}
					}
					if(biomesForTags != null)
					{
						for(RegistryKey<Biome> biomeForTag : biomesForTags)
						{
							Biome biome = ForgeRegistries.BIOMES.getValue(biomeForTag.location());
							if(group.temperatureAllowed(biome.getBaseTemperature()))
							{
								String otgBiomeName = biome.getRegistryName().getNamespace() + "." + biome.getRegistryName().getPath();
								for(IBiomeConfig biomeConfig : biomeConfigsByResourceLocation.values())
								{
									if(
										biomeConfig.getRegistryKey().toResourceLocationString().equals(biomeForTag.location().toString()) &&
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
					// Check for biomeconfig name, if none then use  
					// resourcelocation to look up template biome config.
					IBiomeConfig biomeConfig = biomeConfigsByName.get(biomeEntry);
					if(biomeConfig != null)
					{
						if(!groupBiomes.containsKey(biomeEntry))
						{
							groupBiomes.put(biomeEntry, biomeConfig);
						}
					}
					else if(biomeEntry.contains(":"))
					{
						String[] resourceLocationParts = biomeEntry.split(":");
						if(resourceLocationParts.length == 2)
						{
							biomeConfig = biomeConfigsByResourceLocation.get(new MCBiomeResourceLocation(resourceLocationParts[0], resourceLocationParts[1], preset.getFolderName()));
							if(biomeConfig != null && !groupBiomes.containsKey(biomeEntry))
							{
								groupBiomes.put(biomeEntry, biomeConfig);
							}
						}
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
