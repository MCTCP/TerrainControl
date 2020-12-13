package com.pg85.otg.spigot.presets;

import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.gen.biome.NewBiomeData;
import com.pg85.otg.gen.biome.layers.BiomeLayerData;
import com.pg85.otg.gen.biome.layers.NewBiomeGroup;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.SpigotBiome;
import com.pg85.otg.util.biome.BiomeResourceLocation;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;

import java.io.File;
import java.util.*;

public class SpigotPresetLoader extends LocalPresetLoader
{
	private final HashMap<String, BiomeConfig[]> globalIdMapping = new HashMap<>();
	// Using a ref is much faster than using an object
	private final HashMap<String, Reference2IntMap<BiomeConfig>> reverseIdMapping = new HashMap<>();
	private final Map<String, BiomeLayerData> presetGenerationData = new HashMap<>();

	private final Map<MinecraftKey, BiomeConfig> biomeConfigsByRegistryKey = new HashMap<>();
	private final Map<String, List<ResourceKey<BiomeBase>>> biomesByPresetName = new LinkedHashMap<>();

	private final ResourceKey<IRegistry<BiomeBase>> BIOME_KEY = IRegistry.ay;

	public SpigotPresetLoader (File otgRootFolder)
	{
		super(otgRootFolder.toPath());
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
			this.biomesByPresetName.put(preset.getName(), presetBiomes);
			WorldConfig worldConfig = preset.getWorldConfig();
			BiomeConfig oceanBiomeConfig = null;

			List<BiomeConfig> biomeConfigs = preset.getAllBiomeConfigs();
			BiomeConfig[] presetIdMapping = new BiomeConfig[biomeConfigs.size() + 1]; // +1 for default ocean biome, which is registered twice.
			Reference2IntMap<BiomeConfig> presetReverseIdMapping = new Reference2IntLinkedOpenHashMap<>();

			Map<Integer, List<NewBiomeData>> isleBiomesAtDepth = new HashMap<>();
			Map<Integer, List<NewBiomeData>> borderBiomesAtDepth = new HashMap<>();

			Map<String, Integer> worldBiomes = new HashMap<>();

			IRegistryWritable<BiomeBase> biome_registry = ((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(BIOME_KEY);

			for (BiomeConfig biomeConfig : biomeConfigs)
			{
				// DeferredRegister for Biomes doesn't appear to be working atm, biomes are never registered :(
				//RegistryObject<Biome> registryObject = OTGPlugin.BIOMES.register(biomeConfig.getRegistryKey().getResourcePath(), () -> createOTGBiome(biomeConfig));

				boolean isOceanBiome = false;
				// Biome id 0 is reserved for ocean, used when a land column has
				// no biome assigned, which can happen due to biome group rarity.
				if (biomeConfig.getName().equals(preset.getWorldConfig().getDefaultOceanBiome()))
				{
					// TODO: Can't map the same biome to 2 int keys for the reverse map
					// make sure this doesn't cause problems :/.
					oceanBiomeConfig = biomeConfig;
					presetIdMapping[0] = biomeConfig;
					isOceanBiome = true;
				}

				BiomeBase biome = SpigotBiome.createOTGBiome(isOceanBiome, preset.getWorldConfig(), biomeConfig);
				// Get a modifiable biome registry
				biome_registry.a(biome);

				// Store registry key (resourcelocation) so we can look up biomeconfigs via RegistryKey<Biome> later.
				MinecraftKey resourceLocation = new MinecraftKey(biomeConfig.getRegistryKey().toResourceLocationString());
				System.out.println(resourceLocation);
				this.biomeConfigsByRegistryKey.put(resourceLocation, biomeConfig);

				presetBiomes.add(ResourceKey.a(BIOME_KEY, resourceLocation));

				presetIdMapping[currentId] = biomeConfig;
				presetReverseIdMapping.put(biomeConfig, currentId);

				worldBiomes.put(biomeConfig.getName(), currentId);

				// Make a list of isle and border biomes per generation depth
				if (biomeConfig.isIsleBiome())
				{
					// Make or get a list for this group depth, then add
					List<NewBiomeData> biomesAtDepth = isleBiomesAtDepth.getOrDefault(worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(), new ArrayList<>());
					biomesAtDepth.add(
							new NewBiomeData(
									currentId,
									biomeConfig.getName(),
									worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeRarity() : biomeConfig.getBiomeRarityWhenIsle(),
									worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(),
									biomeConfig.getBiomeTemperature(),
									biomeConfig.getIsleInBiomes(),
									biomeConfig.getBorderInBiomes(),
									biomeConfig.getNotBorderNearBiomes()
							)
					);
					isleBiomesAtDepth.put(worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenIsle(), biomesAtDepth);
				}

				if (biomeConfig.isBorderBiome())
				{
					// Make or get a list for this group depth, then add
					List<NewBiomeData> biomesAtDepth = borderBiomesAtDepth.getOrDefault(worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(), new ArrayList<>());
					biomesAtDepth.add(
							new NewBiomeData(
									currentId,
									biomeConfig.getName(),
									biomeConfig.getBiomeRarity(),
									worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(),
									biomeConfig.getBiomeTemperature(),
									biomeConfig.getIsleInBiomes(),
									biomeConfig.getBorderInBiomes(),
									biomeConfig.getNotBorderNearBiomes()
							)
					);
					borderBiomesAtDepth.put(worldConfig.getBiomeMode() == SettingsEnums.BiomeMode.BeforeGroups ? biomeConfig.getBiomeSize() : biomeConfig.getBiomeSizeWhenBorder(), biomesAtDepth);
				}

				// Index BiomeColor for FromImageMode and /otg map
				biomeColorMap.put(biomeConfig.getBiomeColor(), currentId);

				OTG.log(LogMarker.INFO, "Registered biome " + biomeConfig.getName() + " with OTG id " + currentId);

				currentId++;
			}

			this.globalIdMapping.put(preset.getName(), presetIdMapping);
			this.reverseIdMapping.put(preset.getName(), presetReverseIdMapping);

			// Set the base data
			BiomeLayerData data = new BiomeLayerData(preset.getPresetDir(), worldConfig, oceanBiomeConfig);

			Set<Integer> biomeDepths = new HashSet<>();
			Map<Integer, List<NewBiomeGroup>> groupDepths = new HashMap<>();

			// Iterate through the groups and add it to the layer data
			for (BiomeGroup group : worldConfig.getBiomeGroupManager().getGroups())
			{
				// Initialize biome group data
				NewBiomeGroup bg = new NewBiomeGroup();
				bg.id = group.getGroupId();
				bg.rarity = group.getGroupRarity();

				float totalTemp = 0;

				// Add each biome to the group
				for (String biome : group.biomes.keySet())
				{
					MinecraftKey location = new MinecraftKey(new BiomeResourceLocation(preset.getName(), biome).toResourceLocationString());
					BiomeConfig config = this.biomeConfigsByRegistryKey.get(location);

					// Make and add the generation data
					NewBiomeData newBiomeData = new NewBiomeData(presetReverseIdMapping.getInt(config), config.getName(), config.getBiomeRarity(), config.getBiomeSize(), config.getBiomeTemperature(), config.getIsleInBiomes(), config.getBorderInBiomes(), config.getNotBorderNearBiomes());
					bg.biomes.add(newBiomeData);

					// Add the biome size- if it's already there, nothing is done
					biomeDepths.add(config.getBiomeSize());

					totalTemp += config.getBiomeTemperature();
					bg.totalGroupRarity += config.getBiomeRarity();
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
			this.presetGenerationData.put(preset.getName(), data);
		}
	}

	@Override
	public BiomeConfig getBiomeConfig (String resourceLocationString)
	{
		return this.biomeConfigsByRegistryKey.get(new MinecraftKey(resourceLocationString));
	}

	@Override
	public BiomeConfig getBiomeConfig (String presetName, int biomeId)
	{
		BiomeConfig[] biomes = this.globalIdMapping.get(presetName);
		return biomes.length > biomeId ? biomes[biomeId] : null;
	}
}
