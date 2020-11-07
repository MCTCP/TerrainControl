package com.pg85.otg.forge.biome;

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
	// TODO: This needs to be better
	public static final BiomeConfig[] LOOKUP = new BiomeConfig[128];
 	public static final Codec<OTGBiomeProvider> CODEC = RecordCodecBuilder.create(
		(instance) -> instance.group(
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
	private final Int2ObjectMap<RegistryKey<Biome>> lookup;

	public OTGBiomeProvider(long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> registry)
	{
		// TODO: Hardcoded preset name, world creation gui / config.yaml should have preset name.
		super(
			((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys("Default").stream().map(
				(p_242638_1_) -> () -> registry.func_243576_d(p_242638_1_)
			)
		);
		this.seed = seed;
		this.legacyBiomeInitLayer = legacyBiomeInitLayer;
		this.largeBiomes = largeBiomes;
		this.registry = registry;
		this.layer = BiomeLayers.create(seed);
		this.lookup = new Int2ObjectOpenHashMap<>();
		
		// TODO: this is hardcoded for now until layer generation is fixed.
		// TODO: Get the RegistryKey when registering biomes and reuse it?
		Biome biome = registry.getOrDefault(new ResourceLocation("openterraingenerator:default.ocean"));
		if(biome != null)
		{
			this.lookup.put(0, RegistryKey.func_240903_a_(Registry.field_239720_u_, new ResourceLocation("openterraingenerator:default.ocean")));
		} else {
			this.lookup.put(0, Biomes.OCEAN);
		}
		biome = registry.getOrDefault(new ResourceLocation("openterraingenerator:default.plains"));
		if(biome != null)
		{
			this.lookup.put(1, RegistryKey.func_240903_a_(Registry.field_239720_u_, new ResourceLocation("openterraingenerator:default.plains")));
		} else {
			this.lookup.put(1, Biomes.PLAINS);
		}
		biome = registry.getOrDefault(new ResourceLocation("openterraingenerator:default.forest"));
		if(biome != null)
		{
			this.lookup.put(2, RegistryKey.func_240903_a_(Registry.field_239720_u_, new ResourceLocation("openterraingenerator:default.forest")));
		} else {
			this.lookup.put(2, Biomes.FOREST);
		}
		biome = registry.getOrDefault(new ResourceLocation("openterraingenerator:default.desert"));
		if(biome != null)
		{
			this.lookup.put(3, RegistryKey.func_240903_a_(Registry.field_239720_u_, new ResourceLocation("openterraingenerator:default.desert")));
		} else {
			this.lookup.put(3, Biomes.DESERT);
		}
		//
	}

	protected Codec<? extends BiomeProvider> func_230319_a_()
	{
		return CODEC;
	}

	@OnlyIn(Dist.CLIENT)
	public BiomeProvider func_230320_a_(long seed)
	{
		return new OTGBiomeProvider(seed, this.legacyBiomeInitLayer, this.largeBiomes, this.registry);
	}

	public RegistryKey<Biome> getBiomeRegistryKey(int x, int y, int z)
	{
		// TODO: this is hardcoded for now until layer generation is fixed
		return lookup.get(this.layer.sample(x, z));
	}

	@Override
	public Biome getNoiseBiome(int x, int y, int z)
	{
		// TODO: this is hardcoded for now until layer generation is fixed
		return registry.func_230516_a_(lookup.get(this.layer.sample(x, z)));
	}

	@Override
	public CachingLayerSampler getSampler()
	{
		return this.layer;
	}

	@Override
	public BiomeConfig getConfig(int x, int z)
	{
		return LOOKUP[this.layer.sample(x, z)];
	}
}
