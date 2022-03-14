package com.pg85.otg.forge.biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.biome.BiomeSource;

import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.core.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.gen.biome.layers.BiomeLayers;
import com.pg85.otg.gen.biome.layers.util.CachingLayerSampler;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ILayerSource;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OTGBiomeProvider extends BiomeSource implements ILayerSource
{
	public static final MapCodec<OTGBiomeProvider> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
		(instance) -> {
			return instance.group(
				Codec.STRING.fieldOf("preset_name").stable().forGetter((provider) -> provider.presetFolderName),
				ExtraCodecs.<Pair<Climate.ParameterPoint, Supplier<Biome>>>nonEmptyList(
					RecordCodecBuilder.<Pair<Climate.ParameterPoint, Supplier<Biome>>>create(
						(p_187078_) -> { return p_187078_.group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)).apply(p_187078_, Pair::of); }
					).listOf()
				).xmap(
					Climate.ParameterList::new, 
					(Function<Climate.ParameterList<Supplier<Biome>>, List<Pair<Climate.ParameterPoint, Supplier<Biome>>>>) Climate.ParameterList::values
				).fieldOf("biomes").forGetter(
					(p_187080_) -> { return p_187080_.parameters; }
				)
			).apply(instance, OTGBiomeProvider::new);
		}
	);
	public static final Codec<OTGBiomeProvider> CODEC = 
		Codec.mapEither(
			OTGBiomeProvider.PresetInstance.CODEC, 
			DIRECT_CODEC
		).xmap(
			(p_187068_) -> {
				return p_187068_.map(OTGBiomeProvider.PresetInstance::biomeSource, Function.identity());
			}, 
			(p_187066_) -> {
				return p_187066_.preset().map(Either::<OTGBiomeProvider.PresetInstance, OTGBiomeProvider>left).orElseGet(
					() -> { return Either.right(p_187066_); }
				);
			}
		).codec()
	;
	private final Climate.ParameterList<Supplier<Biome>> parameters;
	private final Optional<OTGBiomeProvider.PresetInstance> preset;

	private final Registry<Biome> registry;
	private final ThreadLocal<CachingLayerSampler> layer;
	private final Int2ObjectMap<ResourceKey<Biome>> keyLookup;
	private final String presetFolderName;

	private OTGBiomeProvider(String presetFolderName, Climate.ParameterList<Supplier<Biome>> parameters)
	{
		this(presetFolderName, parameters, Optional.empty());
	}

	public OTGBiomeProvider(String presetFolderName, Climate.ParameterList<Supplier<Biome>> parameters, Optional<OTGBiomeProvider.PresetInstance> preset)
	{
		super(getAllBiomesByPreset(presetFolderName, (WritableRegistry<Biome>)preset.get().biomes()));
		long seed = 12; // TODO Reimplement this for 1.18, where did seed go? :/
		this.preset = preset;
		this.parameters = parameters;
		this.presetFolderName = presetFolderName;
		this.registry = (WritableRegistry<Biome>)preset.get().biomes();
		this.layer = ThreadLocal.withInitial(() -> BiomeLayers.create(seed, ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getPresetGenerationData().get(presetFolderName), OTG.getEngine().getLogger()));
		this.keyLookup = new Int2ObjectOpenHashMap<>();

		// Default to let us know if we did anything wrong
		this.keyLookup.defaultReturnValue(Biomes.OCEAN);

		IBiome[] biomeLookup = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName);
		if(biomeLookup == null)
		{
			throw new RuntimeException("No OTG preset found with name \"" + presetFolderName + "\". Install the correct preset or update your server.properties.");
		}

		IBiome biome;
		ResourceKey<Biome> key;
		for (int biomeId = 0; biomeId < biomeLookup.length; biomeId++)
		{
			biome = biomeLookup[biomeId];
			if(biome != null)
			{
				key = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(biome.getBiomeConfig().getRegistryKey().toResourceLocationString()));
				this.keyLookup.put(biomeId, key);
			}
		}
	}
	
	private static Stream<Supplier<Biome>> getAllBiomesByPreset(String presetFolderName, WritableRegistry<Biome> registry)
	{
		if(OTG.getEngine().getPluginConfig().getDeveloperModeEnabled())
		{
			OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles();
			((ForgeEngine)OTG.getEngine()).reloadPreset(presetFolderName, registry);
		} else {
			// Recreate Biome objects and fire Forge BiomeLoadedEvent to allow other mods to enrich otg biomes 
			// with decoration features, structure features and mob spawns. Need to do this here to make sure 
			// modded features get registered on existing world load. 
			// TODO: Fix Forge biome registration so hopefully none of this is necessary, use deferredregister (wasn't working before)?
			((ForgePresetLoader)OTG.getEngine().getPresetLoader()).reRegisterBiomes(presetFolderName, registry);
		}

		List<ResourceKey<Biome>> biomesForPreset = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(presetFolderName);
		if(biomesForPreset == null)
		{
			biomesForPreset= ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName());
		}
		if(biomesForPreset == null)
		{
			biomesForPreset = new ArrayList<>();
		}

		return fixBiomeFeatureOrder(biomesForPreset, registry);
	}

	private static Stream<Supplier<Biome>> fixBiomeFeatureOrder(List<ResourceKey<Biome>> biomesForPreset, WritableRegistry<Biome> registry)
	{
		// For some reason Mojang thought it'd be funny to add a method (BiomeSource.buildFeaturesPerStep)
		// that validates the order of features registered to biomes, making sure that all biomes have
		// PlacedFeatures registered in the same order, otherwise it throws a "cycle order" exception.
		// This requirement is not defined or enforced anywhere else in the code, so you can register
		// features to biomes in any order without issue, but then it blows up on calling the BiomeSource
		// constructor. Fix any feature order inconsistencies for OTG biomes before handing them to MC.	
		
		List<Biome> biomes = biomesForPreset.stream().map((key) -> registry.getOrThrow(key)).collect(Collectors.toList());
		ArrayList<PlacedFeature> featureOrder = new ArrayList<>();		
		HashMap<PlacedFeature,List<PlacedFeature>> dependenciesPerFeature = new HashMap<>();

		// Fill the featureOrder list to establish the allowed order of PlacedFeatures per biome
		
		// For each feature, find all dependencies (other features that are listed before it)
		biomes.stream().filter(a -> !a.getRegistryName().getNamespace().equals(com.pg85.otg.constants.Constants.MOD_ID_SHORT)).forEach(biome -> {
			biome.getGenerationSettings().features().stream().forEach(listForDecorationStep -> {
				for(int i = listForDecorationStep.size() - 1; i >= 0; i--)
				{
					PlacedFeature feature2 = listForDecorationStep.get(i).get();
					List<PlacedFeature> dependenciesForFeature = dependenciesPerFeature.computeIfAbsent(feature2, g -> { return new ArrayList<PlacedFeature>(); });
					if(i > 0)
					{
						PlacedFeature feature3 = listForDecorationStep.get(i - 1).get();
						if(feature3 != feature2 && !dependenciesForFeature.contains(feature3))
						{
							dependenciesForFeature.add(feature3);
						}
					}
				}
			});
		});

		// Resolve dependencies in order: Go through all features and find any features that have no dependencies, add them to the 
		// featureOrder list and remove them from all dependency lists. Repeat until the features list is empty or there are no 
		// features that have no dependencies (cyclical dependency error).

		while(!dependenciesPerFeature.isEmpty())
		{
			int itemsRemoved = 0;
			for(Entry<PlacedFeature,List<PlacedFeature>> entry : new HashMap<PlacedFeature,List<PlacedFeature>>(dependenciesPerFeature).entrySet())
			{
				PlacedFeature feature = entry.getKey();
				List<PlacedFeature> dependencies = entry.getValue();
				if(dependencies.isEmpty())
				{
					itemsRemoved++;
					featureOrder.add(feature);
					dependenciesPerFeature.remove(feature);
					dependenciesPerFeature.entrySet().forEach(entry2 -> {
						List<PlacedFeature> dependencies2 = entry2.getValue();
						dependencies2.remove(feature);
					});
				}
			};
			if(itemsRemoved == 0)
			{
				// Detected a cyclical dependency
				throw new IllegalStateException("Feature order cycle found: Some of your (non-OTG) biomes have the same Features added in a different order, MC doesn't allow this. If you're using a datapack, you'll have to fix this yourself. The mod Cyanide can tell you which biomes/features are the problem.");
			}
		}

		// Now index any features added only to OTG biomes, we'll reorder these so will ignore order.
		
		// For each feature, find all the other features that are listed before it
		biomes.stream().filter(a -> a.getRegistryName().getNamespace().equals(com.pg85.otg.constants.Constants.MOD_ID_SHORT)).forEach(biome -> {
			biome.getGenerationSettings().features().stream().forEach(listForDecorationStep -> {
				for(int i = listForDecorationStep.size() - 1; i >= 0; i--)
				{
					PlacedFeature feature2 = listForDecorationStep.get(i).get();
					// Ignore any features that already have an order determined by non-otg biomes, 
					// or have already been found in other otg biomes
					if(!featureOrder.contains(feature2))
					{
						featureOrder.add(feature2);
					}
				}
			});
		});
		
		// For OTG biomes, reorder PlacedFeatures so MC doesn't blow up.
		biomes.stream().filter(a -> a.getRegistryName().getNamespace().equals(com.pg85.otg.constants.Constants.MOD_ID_SHORT)).forEach(biome -> {
			List<List<Supplier<PlacedFeature>>> orderedFeatures = new ArrayList<>();
			biome.getGenerationSettings().features().stream().forEach(listForDecorationStep -> {
				List<Supplier<PlacedFeature>> orderedFeaturesForDecorationStep = new ArrayList<>();
				featureOrder.forEach(orderedFeature -> {
					listForDecorationStep.forEach(supplier ->
					{
						if(orderedFeature == supplier.get())
						{
							orderedFeaturesForDecorationStep.add(supplier);
						}
					});
				});
				orderedFeatures.add(orderedFeaturesForDecorationStep);
			});
			biome.getGenerationSettings().features = orderedFeatures.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList());
		});

		return biomes.stream().map((biome) -> () -> biome);		
	}

	protected Codec<? extends BiomeSource> codec()
	{
		return CODEC;
	}

	@OnlyIn(Dist.CLIENT)
	public BiomeSource withSeed(long seed)
	{
		return this;
	}

	private Optional<OTGBiomeProvider.PresetInstance> preset()
	{
		return this.preset;
	}
	
	public boolean stable(OTGBiomeProvider.Preset p_187064_)
	{
		return this.preset.isPresent() && Objects.equals(this.preset.get().preset(), p_187064_);
	}
	
	// TODO: This is only used by MC internally, OTG fetches all biomes via CachedBiomeProvider.
	// Could make this use the cache too?
	@Override
	public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ, Sampler p_186738_)
	{
		return this.registry.get(this.keyLookup.get(this.layer.get().sample(biomeX, biomeZ)));
	}

	@Override
	public CachingLayerSampler getSampler()
	{
		return this.layer.get();
	}

	public static class Preset
	{
		public static final OTGBiomeProvider.Preset DEFAULT = new OTGBiomeProvider.Preset(
			new ResourceLocation("default"), 
			(biomeRegistry) -> { 
				// Dummy list
				return new Climate.ParameterList<>(
					ImmutableList.of(
						Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> {
							return biomeRegistry.getOrThrow(Biomes.PLAINS);
						})
					)
				);
			}
		);
		
		public final ResourceLocation name;
		private final Function<Registry<Biome>, Climate.ParameterList<Supplier<Biome>>> parameterSource;
	
		public Preset(ResourceLocation key, Function<Registry<Biome>, Climate.ParameterList<Supplier<Biome>>> parameterSource)
		{
			this.name = key;
			this.parameterSource = parameterSource;
		}

		OTGBiomeProvider biomeSource(OTGBiomeProvider.PresetInstance presetInstance, boolean withInstance)
		{
			Climate.ParameterList<Supplier<Biome>> parameterlist = this.parameterSource.apply(presetInstance.biomes());
			return new OTGBiomeProvider(presetInstance.presetFolderName, parameterlist, withInstance ? Optional.of(presetInstance) : Optional.empty());
		}
	}

	public static record PresetInstance(String presetFolderName, OTGBiomeProvider.Preset preset, Registry<Biome> biomes)
	{
		public static final MapCodec<OTGBiomeProvider.PresetInstance> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
			return instance.group(
			Codec.STRING.fieldOf("preset_name").stable().forGetter(OTGBiomeProvider.PresetInstance::presetFolderName),
			ResourceLocation.CODEC.flatXmap(
				(key) -> {
					return Optional.ofNullable(new OTGBiomeProvider.Preset(
						new ResourceLocation("otg"),
						(biomeRegistry) -> {
							// Dummy list
							return new Climate.ParameterList<>(
								ImmutableList.of(
									Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> {
										return biomeRegistry.getOrThrow(Biomes.PLAINS);
									})
								)
							);
						}
					)).map(DataResult::success).orElseGet(() -> {
						return DataResult.error("Unknown preset: " + key);
					});
				}, 
				(preset) -> {
					return DataResult.success(preset.name);
				}
			).fieldOf("preset").stable().forGetter(OTGBiomeProvider.PresetInstance::preset),
			RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(OTGBiomeProvider.PresetInstance::biomes)).apply(
				instance, 
				instance.stable(OTGBiomeProvider.PresetInstance::new)
			);
		});
	
		public OTGBiomeProvider biomeSource()
		{
			return this.preset.biomeSource(this, true);
		}
	}
}
