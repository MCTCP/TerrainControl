package com.pg85.otg.spigot.presets;

import com.mojang.serialization.Lifecycle;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeConfigFinder;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.gen.biome.layers.BiomeLayerData;
import com.pg85.otg.gen.biome.layers.NewBiomeGroup;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.SpigotBiome;
import com.pg85.otg.spigot.materials.SpigotMaterialReader;
import com.pg85.otg.spigot.util.MobSpawnGroupHelper;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.EntityCategory;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;

import java.io.File;
import java.util.*;

public class SpigotPresetLoader extends LocalPresetLoader
{
	private final Map<String, List<ResourceKey<BiomeBase>>> biomesByPresetFolderName = new LinkedHashMap<>();
	private final HashMap<String, BiomeConfig[]> globalIdMapping = new HashMap<>();	
	private final HashMap<String, Object2IntMap<BiomeConfig>> reverseIdMapping = new HashMap<>(); // Using a ref is much faster than using an object
	private final Map<String, BiomeConfig> biomeConfigsByRegistryKey = new HashMap<>();
	private final Map<String, BiomeLayerData> presetGenerationData = new HashMap<>();	
	// We have to store biomes, since Spigot doesn't expose registry key on BiomeBase.
	private final Map<BiomeBase, BiomeConfig> biomeConfigsByBiome = new HashMap<>();	

	private final ResourceKey<IRegistry<BiomeBase>> BIOME_KEY = IRegistry.ay;

	public SpigotPresetLoader (File otgRootFolder)
	{
		super(otgRootFolder.toPath());
	}

	@Override
	public IMaterialReader createMaterialReader()
	{
		return new SpigotMaterialReader();
	}

	@Override
	public void registerBiomes ()
	{
		for (Preset preset : this.presets.values())
		{
			// Index BiomeColors for FromImageMode and /otg map
			HashMap<Integer, Integer> biomeColorMap = new HashMap<>();

			// Start at 1, 0 is the fallback for the biome generator (the world's ocean biome).
			int currentId = 1;

			List<ResourceKey<BiomeBase>> presetBiomes = new ArrayList<>();
			this.biomesByPresetFolderName.put(preset.getFolderName(), presetBiomes);
			WorldConfig worldConfig = preset.getWorldConfig();
			BiomeConfig oceanBiomeConfig = null;
			int[] oceanTemperatures = new int[]{0, 0, 0, 0};

			List<BiomeConfig> biomeConfigs = preset.getAllBiomeConfigs();
			BiomeConfig[] presetIdMapping = new BiomeConfig[biomeConfigs.size()];
			Object2IntMap<BiomeConfig> presetReverseIdMapping = new Object2IntArrayMap<>();

			Map<Integer, List<BiomeData>> isleBiomesAtDepth = new HashMap<>();
			Map<Integer, List<BiomeData>> borderBiomesAtDepth = new HashMap<>();

			Map<String, Integer> worldBiomes = new HashMap<>();

			IRegistryWritable<BiomeBase> biome_registry = ((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(BIOME_KEY);

			for (BiomeConfig biomeConfig : biomeConfigs)
			{
				boolean isOceanBiome = false;
				// Biome id 0 is reserved for ocean, used when a land column has
				// no biome assigned, which can happen due to biome group rarity.
				if (biomeConfig.getName().equals(worldConfig.getDefaultOceanBiome()))
				{
					oceanBiomeConfig = biomeConfig;
					isOceanBiome = true;
				}
				
				int otgBiomeId = isOceanBiome ? 0 : currentId;

				// Ocean temperature mappings. Probably a better way to do this?
				if (biomeConfig.getName().equals(worldConfig.getDefaultWarmOceanBiome())) {
					oceanTemperatures[0] = otgBiomeId;
				}
				if (biomeConfig.getName().equals(worldConfig.getDefaultLukewarmOceanBiome())) {
					oceanTemperatures[1] = otgBiomeId;
				}
				if (biomeConfig.getName().equals(worldConfig.getDefaultColdOceanBiome())) {
					oceanTemperatures[2] = otgBiomeId;
				}
				if (biomeConfig.getName().equals(worldConfig.getDefaultFrozenOceanBiome())) {
					oceanTemperatures[3] = otgBiomeId;
				}

				BiomeBase biome = SpigotBiome.createOTGBiome(isOceanBiome, preset.getWorldConfig(), biomeConfig);

				MinecraftKey resourceLocation = new MinecraftKey(biomeConfig.getRegistryKey().toResourceLocationString());
				//System.out.println(resourceLocation);

				// Create a registry key
				ResourceKey<BiomeBase> registryKey = ResourceKey.a(IRegistry.ay, resourceLocation);
				// Store the biome in the registry
				biome_registry.a(registryKey, biome, Lifecycle.experimental());
				
				// Store registry key (resourcelocation) so we can look up biomeconfigs via RegistryKey<Biome> later.
				this.biomeConfigsByRegistryKey.put(resourceLocation.toString(), biomeConfig);
				this.biomeConfigsByBiome.put(biome,  biomeConfig);
				
				presetBiomes.add(ResourceKey.a(BIOME_KEY, resourceLocation));

				presetIdMapping[otgBiomeId] = biomeConfig;
				presetReverseIdMapping.put(biomeConfig, otgBiomeId);

				worldBiomes.put(biomeConfig.getName(), otgBiomeId);

				// Make a list of isle and border biomes per generation depth
				if (biomeConfig.isIsleBiome())
				{
					// Make or get a list for this group depth, then add
					List<BiomeData> biomesAtDepth = isleBiomesAtDepth.getOrDefault(worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(), new ArrayList<>());
					biomesAtDepth.add(
						new BiomeData(
							otgBiomeId,
							biomeConfig.getName(),
							worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeRarity() : biomeConfig.getBiomeRarityWhenIsle(),
							worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(),
							biomeConfig.getBiomeTemperature(),
							biomeConfig.getIsleInBiomes(),
							biomeConfig.getBorderInBiomes(),
							biomeConfig.getBorderNearBiomes(),
							biomeConfig.getNotBorderNearBiomes()
						)
					);

					isleBiomesAtDepth.put(worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(), biomesAtDepth);
				}

				if (biomeConfig.isBorderBiome())
				{
					// Make or get a list for this group depth, then add
					List<BiomeData> biomesAtDepth = borderBiomesAtDepth.getOrDefault(worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(), new ArrayList<>());
					biomesAtDepth.add(
						new BiomeData(
							otgBiomeId,
							biomeConfig.getName(),
							biomeConfig.getBiomeRarity(),
							worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(),
							biomeConfig.getBiomeTemperature(),
							biomeConfig.getIsleInBiomes(),
							biomeConfig.getBorderInBiomes(),
							biomeConfig.getBorderNearBiomes(),
							biomeConfig.getNotBorderNearBiomes()
						)
					);

					borderBiomesAtDepth.put(worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(), biomesAtDepth);
				}

				// Index BiomeColor for FromImageMode and /otg map
				biomeColorMap.put(biomeConfig.getBiomeColor(), otgBiomeId);

				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
				{
					OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Registered biome " + biomeConfig.getName() + " with OTG id " + otgBiomeId);
				}

				currentId += isOceanBiome ? 0 : 1;
			}

			// If the ocean config is null, shift the array downwards to fill id 0
			if (oceanBiomeConfig == null) {
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
				bg.totalDepthRarity = new int[genDepth+1];
				bg.maxRarityPerDepth = new int[genDepth+1];

				float totalTemp = 0;

				// Add each biome to the group
				for (String biome : group.biomes.keySet())
				{
					MinecraftKey location = new MinecraftKey(new OTGBiomeResourceLocation(preset.getPresetFolder(), preset.getShortPresetName(), preset.getMajorVersion(), biome).toResourceLocationString());
					BiomeConfig config = this.biomeConfigsByRegistryKey.get(location.toString());
					if (config == null)
					{
						throw new NullPointerException("Failed to fetch biome config using ResourceLocation '"+location+"'");
					}

					// Make and add the generation data
					BiomeData newBiomeData = new BiomeData(
						presetReverseIdMapping.getInt(config),
						config.getName(),
						config.getBiomeRarity(),
						config.getBiomeSize(),
						config.getBiomeTemperature(),
						config.getIsleInBiomes(),
						config.getBorderInBiomes(),
						config.getBorderNearBiomes(),
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
	public BiomeConfig getBiomeConfig (String resourceLocationString)
	{
		return this.biomeConfigsByRegistryKey.get(resourceLocationString);
	}
	
	public IBiomeConfig getBiomeConfig(BiomeBase biome)
	{
		return this.biomeConfigsByBiome.get(biome);
	}	

	@Override
	public BiomeConfig getBiomeConfig (String presetFolderName, int biomeId)
	{
		BiomeConfig[] biomes = this.globalIdMapping.get(presetFolderName);
		return biomes.length > biomeId ? biomes[biomeId] : null;
	}

	public List<ResourceKey<BiomeBase>> getBiomeRegistryKeys (String presetFolderName)
	{
		return this.biomesByPresetFolderName.get(presetFolderName);
	}

	public BiomeConfig[] getGlobalIdMapping (String presetFolderName)
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
