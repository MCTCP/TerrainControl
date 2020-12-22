package com.pg85.otg.spigot.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.gen.biome.layers.BiomeLayers;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.gen.biome.layers.util.CachingLayerSampler;
import com.pg85.otg.spigot.presets.SpigotPresetLoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.server.v1_16_R3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

// Spigot name: WorldChunkManager
// Forge name: BiomeProvider
public class OTGBiomeProvider extends WorldChunkManager implements LayerSource
{
	public static final Codec<OTGBiomeProvider> CODEC = RecordCodecBuilder.create(
			(instance) -> instance.group(
					Codec.STRING.fieldOf("preset_name").stable().forGetter((provider) -> provider.presetName),
					Codec.LONG.fieldOf("seed").stable().forGetter((provider) -> provider.seed),
					Codec.BOOL.optionalFieldOf("legacy_biome_init_layer", Boolean.FALSE, Lifecycle.stable()).forGetter((provider) -> provider.legacyBiomeInitLayer),
					Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter((provider) -> provider.largeBiomes),
					RegistryLookupCodec.a(IRegistry.ay).forGetter((provider) -> provider.registry)
			).apply(instance, instance.stable(OTGBiomeProvider::new))
	);

	private final long seed;
	private final boolean legacyBiomeInitLayer;
	private final boolean largeBiomes;
	private final IRegistry<BiomeBase> registry;
	private final CachingLayerSampler layer;
	public final BiomeConfig[] configLookup;
	private final Int2ObjectMap<ResourceKey<BiomeBase>> keyLookup;
	private final String presetName;

	public OTGBiomeProvider(String presetName, long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, IRegistry<BiomeBase> registry)
	{
		super(getAllBiomesByPreset(presetName, registry));
		this.presetName = presetName;
		this.seed = seed;
		this.legacyBiomeInitLayer = legacyBiomeInitLayer;
		this.largeBiomes = largeBiomes;
		this.registry = registry;
		this.layer = BiomeLayers.create(seed, ((SpigotPresetLoader) OTG.getEngine().getPresetLoader()).getPresetGenerationData().get(presetName), OTG.getEngine().getLogger());
		this.keyLookup = new Int2ObjectOpenHashMap<>();

		// Default to let us know if we did anything wrong
		this.keyLookup.defaultReturnValue(Biomes.OCEAN);

		this.configLookup = ((SpigotPresetLoader)OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetName);
		for (int biomeId = 0; biomeId < this.configLookup.length; biomeId++)
		{
			BiomeConfig config = this.configLookup[biomeId];
			// Forge method: RegistryKey.getOrCreateKey()
			// Spigot method: ResourceKey.a()
			ResourceKey<BiomeBase> key = ResourceKey.a(IRegistry.ay, new MinecraftKey(config.getRegistryKey().toResourceLocationString()));
			this.keyLookup.put(biomeId, key);
		}
	}

	private static Stream<Supplier<BiomeBase>> getAllBiomesByPreset(String presetName, IRegistry<BiomeBase> registry)
	{
		List<ResourceKey<BiomeBase>> biomesForPreset = ((SpigotPresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(presetName);
		if(biomesForPreset == null)
		{
			((SpigotPresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(OTG.getEngine().getPresetLoader().getDefaultPresetName());
		}
		if(biomesForPreset == null)
		{
			biomesForPreset = new ArrayList<>();
		}
		return biomesForPreset.stream().map(
				// Forge method: getOrThrow
				// Spigot method: d
				(p_242638_1_) -> () -> registry.d(p_242638_1_)
		);
	}

	// Forge name: getBiomeProviderCodec
	// Spigot name: a
	protected Codec<? extends WorldChunkManager> a()
	{
		return CODEC;
	}

	public ResourceKey<BiomeBase> getBiomeRegistryKey(int biomeX, int biomeY, int biomeZ)
	{
		return keyLookup.get(this.layer.sample(biomeX, biomeZ));
	}

	public ResourceKey<BiomeBase> lookupKey(int index)
	{
		return keyLookup.get(index);
	}

	@Override
	public BiomeBase getBiome(int biomeX, int biomeY, int biomeZ)
	{
		// Forge name: getValueForKey
		// Spigot name: a
		return registry.a(keyLookup.get(this.layer.sample(biomeX, biomeZ)));
	}

	@Override
	public CachingLayerSampler getSampler()
	{
		return this.layer;
	}

	@Override
	public BiomeConfig getConfig(int biomeX, int biomeZ)
	{
		int biomeId = this.layer.sample(biomeX, biomeZ);
		return this.configLookup.length > biomeId ? configLookup[this.layer.sample(biomeX, biomeZ)] : null;
	}
}
