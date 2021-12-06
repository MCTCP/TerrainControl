package com.pg85.otg.forge.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.BiomeSource;

import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.core.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.gen.biome.layers.BiomeLayers;
import com.pg85.otg.gen.biome.layers.util.CachingLayerSampler;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.presets.Preset;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	private final IWorldConfig worldConfig;
	
	public OTGBiomeProvider(String presetFolderName, long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> registry)
	{
		super(getAllBiomesByPreset(presetFolderName, (WritableRegistry<Biome>)registry));
		this.presetFolderName = presetFolderName;
		this.seed = seed;
		this.legacyBiomeInitLayer = legacyBiomeInitLayer;
		this.largeBiomes = largeBiomes;
		this.registry = registry;
		this.layer = ThreadLocal.withInitial(() -> BiomeLayers.create(seed, ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getPresetGenerationData().get(presetFolderName), OTG.getEngine().getLogger()));
		this.keyLookup = new Int2ObjectOpenHashMap<>();

		// Default to let us know if we did anything wrong
		this.keyLookup.defaultReturnValue(Biomes.OCEAN);

		IBiome[] biomeLookup = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName);
		Preset preset = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getPresetByFolderName(presetFolderName);
		if(biomeLookup == null || preset == null)
		{
			throw new RuntimeException("No OTG preset found with name \"" + presetFolderName + "\". Install the correct preset or update your server.properties.");
		}
		this.worldConfig = preset.getWorldConfig();
				
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
		return new OTGBiomeProvider(this.presetFolderName, seed, this.legacyBiomeInitLayer, this.largeBiomes, this.registry);
	}

	// TODO: This is only used by MC internally, OTG fetches all biomes via CachedBiomeProvider.
	// Could make this use the cache too?
	@Override
	public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ)
	{
		return this.registry.get(this.keyLookup.get(this.layer.get().sample(biomeX, biomeZ)));
	}

	@Override
	public CachingLayerSampler getSampler()
	{
		return this.layer.get();
	}
	
	// TODO: May have to override this for spawn?
	@Override
	public Set<BlockState> getSurfaceBlocks()
	{
		return super.getSurfaceBlocks();
	}
	
	@Override
	public boolean canGenerateStructure(Structure<?> structure)
	{
		return isWorldConfigAllowedStructure(structure) && this.supportedStructures.computeIfAbsent(structure, (structure2) ->
		{
			return this.possibleBiomes.stream().anyMatch((biome) ->
			{
				return biome.getGenerationSettings().isValidStart(structure2);
			});
		});	
	}
	
	private boolean isWorldConfigAllowedStructure(Structure<?> structure)
	{
		// This doesn't catch modded structures, modded structures don't appear to have a type so we can't filter except by name.
		// We don't have to check biomeconfig toggles here, as that would only apply to non-template biomes and for those we
		// register structures ourselves, so we just don't register them in the first place.
		if(
			(this.worldConfig.getStrongholdsEnabled() || !(structure instanceof StrongholdStructure)) &&
			(this.worldConfig.getVillagesEnabled() || !(structure instanceof VillageStructure)) &&
			(this.worldConfig.getRareBuildingsEnabled() || !(structure instanceof SwampHutStructure)) &&
			(this.worldConfig.getRareBuildingsEnabled() || !(structure instanceof IglooStructure)) &&
			(this.worldConfig.getRareBuildingsEnabled() || !(structure instanceof JunglePyramidStructure)) &&
			(this.worldConfig.getRareBuildingsEnabled() || !(structure instanceof DesertPyramidStructure)) &&
			(this.worldConfig.getMineshaftsEnabled() || !(structure instanceof MineshaftStructure)) &&
			(this.worldConfig.getRuinedPortalsEnabled() || !(structure instanceof RuinedPortalStructure)) &&
			(this.worldConfig.getOceanRuinsEnabled() || !(structure instanceof OceanRuinStructure)) &&
			(this.worldConfig.getShipWrecksEnabled() || !(structure instanceof ShipwreckStructure)) &&
			(this.worldConfig.getOceanMonumentsEnabled() || !(structure instanceof OceanMonumentStructure)) &&
			(this.worldConfig.getBastionRemnantsEnabled() || !(structure instanceof BastionRemantsStructure)) &&
			(this.worldConfig.getBuriedTreasureEnabled() || !(structure instanceof BuriedTreasureStructure)) &&
			(this.worldConfig.getEndCitiesEnabled() || !(structure instanceof EndCityStructure)) &&
			(this.worldConfig.getNetherFortressesEnabled() || !(structure instanceof FortressStructure)) &&
			(this.worldConfig.getNetherFossilsEnabled() || !(structure instanceof NetherFossilStructure)) &&
			(this.worldConfig.getPillagerOutpostsEnabled() || !(structure instanceof PillagerOutpostStructure)) &&
			(this.worldConfig.getWoodlandMansionsEnabled() || !(structure instanceof WoodlandMansionStructure))
		)
		{
			return true;
		}
		return false;
	}
}
