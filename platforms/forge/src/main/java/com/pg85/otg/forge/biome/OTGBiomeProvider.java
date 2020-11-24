package com.pg85.otg.forge.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;

import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.gen.biome.layers.BiomeLayers;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.gen.biome.layers.util.CachingLayerSampler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OTGBiomeProvider extends BiomeProvider implements LayerSource
{
 	public static final Codec<OTGBiomeProvider> CODEC = RecordCodecBuilder.create(
		(instance) -> instance.group(
			Codec.STRING.fieldOf("preset_name").stable().forGetter((provider) -> provider.presetName),
			Codec.LONG.fieldOf("seed").stable().forGetter((provider) -> provider.seed),
			Codec.BOOL.optionalFieldOf("legacy_biome_init_layer", Boolean.FALSE, Lifecycle.stable()).forGetter((provider) -> provider.legacyBiomeInitLayer),
			Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter((provider) -> provider.largeBiomes),
			RegistryLookupCodec.func_244331_a(Registry.field_239720_u_).forGetter((provider) -> provider.registry)
		).apply(instance, instance.stable(OTGBiomeProvider::new))
	);
 	
	private final long seed;
	private final boolean legacyBiomeInitLayer;
	private final boolean largeBiomes;
	private final Registry<Biome> registry;
	private final CachingLayerSampler layer;
	public final Int2ObjectMap<BiomeConfig> configLookup;
	private final Int2ObjectMap<RegistryKey<Biome>> keyLookup;
	private final String presetName;
	
	public OTGBiomeProvider(String presetName, long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> registry)
	{
		super(getAllBiomesByPreset(presetName, registry));
		this.presetName = presetName;
		this.seed = seed;
		this.legacyBiomeInitLayer = legacyBiomeInitLayer;
		this.largeBiomes = largeBiomes;
		this.registry = registry;
		this.layer = BiomeLayers.create(seed, ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getPresetGenerationData().get(presetName));
		this.keyLookup = new Int2ObjectOpenHashMap<>();

		// Default to let us know if we did anything wrong
		this.keyLookup.defaultReturnValue(Biomes.OCEAN);

		this.configLookup = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetName);

		for (int biomeId : this.configLookup.keySet())
		{
			BiomeConfig config = this.configLookup.get(biomeId);

			RegistryKey<Biome> key = RegistryKey.func_240903_a_(Registry.field_239720_u_, new ResourceLocation(config.getRegistryKey().toResourceLocationString()));
			this.keyLookup.put(biomeId, key);
		}
	}
	
	private static Stream<Supplier<Biome>> getAllBiomesByPreset(String presetName, Registry<Biome> registry)
	{
		List<RegistryKey<Biome>> biomesForPreset = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(presetName);
		if(biomesForPreset == null)
		{
			// TODO: Generate Default preset on install
			((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(Constants.DEFAULT_PRESET_NAME);
		}
		if(biomesForPreset == null)
		{
			biomesForPreset = new ArrayList<>();
		}		
		return biomesForPreset.stream().map(
			(p_242638_1_) -> () -> registry.func_243576_d(p_242638_1_)
		);
	}

	protected Codec<? extends BiomeProvider> func_230319_a_()
	{
		return CODEC;
	}

	@OnlyIn(Dist.CLIENT)
	public BiomeProvider func_230320_a_(long seed)
	{
		return new OTGBiomeProvider(this.presetName, seed, this.legacyBiomeInitLayer, this.largeBiomes, this.registry);
	}

	public RegistryKey<Biome> getBiomeRegistryKey(int x, int y, int z)
	{
		// TODO: this is hardcoded for now until layer generation is fixed
		return keyLookup.get(this.layer.sample(x, z));
	}

	@Override
	public Biome getNoiseBiome(int x, int y, int z)
	{
		// TODO: this is hardcoded for now until layer generation is fixed
		return registry.func_230516_a_(keyLookup.get(this.layer.sample(x, z)));
	}

	@Override
	public CachingLayerSampler getSampler()
	{
		return this.layer;
	}

	@Override
	public BiomeConfig getConfig(int x, int z)
	{
		return configLookup.get(this.layer.sample(x, z));
	}
}
