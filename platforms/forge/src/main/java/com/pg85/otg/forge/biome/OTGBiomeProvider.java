package com.pg85.otg.forge.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
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
			((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName());
		}
		if(biomesForPreset == null)
		{
			biomesForPreset = new ArrayList<>();
		}
		return biomesForPreset.stream().map(
			(p_242638_1_) -> () -> registry.getOrThrow(p_242638_1_)
		);
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
