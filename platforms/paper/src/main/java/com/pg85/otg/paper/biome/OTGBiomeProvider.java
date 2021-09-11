package com.pg85.otg.paper.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.gen.biome.layers.BiomeLayers;
import com.pg85.otg.gen.biome.layers.util.CachingLayerSampler;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.paper.presets.PaperPresetLoader;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;

public class OTGBiomeProvider extends BiomeSource implements ILayerSource
{
	public static final Codec<OTGBiomeProvider> CODEC = RecordCodecBuilder.create(
		(instance) -> instance.group(
			Codec.STRING.fieldOf("preset_name").stable().forGetter((provider) -> provider.presetFolderName),
			Codec.LONG.fieldOf("seed").stable().forGetter((provider) -> provider.seed),
			Codec.BOOL.optionalFieldOf("legacy_biome_init_layer", Boolean.FALSE, Lifecycle.stable()).forGetter((provider) -> provider.legacyBiomeInitLayer),
			Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter((provider) -> provider.largeBiomes),
			RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter((provider) -> provider.registry)
		).apply(instance, instance.stable(OTGBiomeProvider::new))
	);
	private final long seed;
	private final boolean legacyBiomeInitLayer;
	private final boolean largeBiomes;
	private final Registry<Biome> registry;
	private final ThreadLocal<CachingLayerSampler> layer;
	private final Int2ObjectMap<ResourceKey<Biome>> keyLookup;
	private final String presetFolderName;

	public OTGBiomeProvider (String presetFolderName, long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> registry)
	{
		super(getAllBiomesByPreset(presetFolderName, registry));
		this.presetFolderName = presetFolderName;
		this.seed = seed;
		this.legacyBiomeInitLayer = legacyBiomeInitLayer;
		this.largeBiomes = largeBiomes;
		this.registry = registry;
		this.layer = ThreadLocal.withInitial(() -> BiomeLayers.create(seed, ((PaperPresetLoader)OTG.getEngine().getPresetLoader()).getPresetGenerationData().get(presetFolderName), OTG.getEngine().getLogger()));
		this.keyLookup = new Int2ObjectOpenHashMap<>();

		// Default to let us know if we did anything wrong
		this.keyLookup.defaultReturnValue(Biomes.OCEAN);

		IBiome[] biomeLookup = ((PaperPresetLoader) OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName);
		for (int biomeId = 0; biomeId < biomeLookup.length; biomeId++)
		{
			IBiomeConfig config = biomeLookup[biomeId].getBiomeConfig();

			ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(config.getRegistryKey().toResourceLocationString()));
			this.keyLookup.put(biomeId, key);
		}
	}

	private static Stream<Supplier<Biome>> getAllBiomesByPreset (String presetFolderName, Registry<Biome> registry)
	{
		List<ResourceKey<Biome>> biomesForPreset = ((PaperPresetLoader) OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(presetFolderName);
		if (biomesForPreset == null)
		{
			((PaperPresetLoader) OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName());
		}
		if (biomesForPreset == null)
		{
			biomesForPreset = new ArrayList<>();
		}
		return biomesForPreset.stream().map(
			(p_242638_1_) -> () -> registry.getOrThrow(p_242638_1_)
		);
	}

	protected Codec<? extends BiomeSource> codec ()
	{
		return CODEC;
	}

	// TODO: This is only used by MC internally, OTG fetches all biomes via CachedBiomeProvider.
	// Could make this use the cache too?
	@Override
	public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ)
	{
		return registry.get(keyLookup.get(this.layer.get().sample(biomeX, biomeZ)));
	}

	@Override
	public CachingLayerSampler getSampler ()
	{
		return this.layer.get();
	}
	
	@Override
	public BiomeSource withSeed(long seed)
	{
		return new OTGBiomeProvider(this.presetFolderName, seed, this.legacyBiomeInitLayer, this.largeBiomes, this.registry);
	}

}
