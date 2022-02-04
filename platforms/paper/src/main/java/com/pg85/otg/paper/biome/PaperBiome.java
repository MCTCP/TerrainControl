package com.pg85.otg.paper.biome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.biome.BiomeConfig;
import com.pg85.otg.gen.resource.GlowLichenResource;
import com.pg85.otg.gen.resource.RegistryResource;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.paper.materials.PaperMaterialData;
import com.pg85.otg.paper.materials.PaperMaterialTag;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import com.pg85.otg.util.materials.LocalMaterialBase;
import com.pg85.otg.util.minecraft.EntityCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.*;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.Biome.TemperatureModifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class PaperBiome implements IBiome
{
	private final Biome biomeBase;
	private final IBiomeConfig biomeConfig;

	public PaperBiome(Biome biomeBase, IBiomeConfig biomeConfig)
	{
		this.biomeBase = biomeBase;
		this.biomeConfig = biomeConfig;
	}

	public Biome getBiome()
	{
		return this.biomeBase;
	}

	@Override
	public IBiomeConfig getBiomeConfig()
	{
		return this.biomeConfig;
	}

	public static Biome createOTGBiome (boolean isOceanBiome, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder = new BiomeGenerationSettings.Builder();

		MobSpawnSettings.Builder mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);

		// Surface/ground/stone blocks / sagc are done during base terrain gen.
		// Spawn point detection checks for surfacebuilder blocks, so using ConfiguredSurfaceBuilders.GRASS.
		// TODO: What if there's no grass around spawn?
		// Commenting out for the time being - Frank
		//biomeGenerationSettingsBuilder.surfaceBuilder(SurfaceBuilders.GRASS);

		// * Carvers are handled by OTG

		// Register any Registry() resources to the biome, to be handled by MC.
		for (ConfigFunction<IBiomeConfig> res : ((BiomeConfig)biomeConfig).getResourceQueue())
		{
			if (res instanceof RegistryResource registryResource)
			{
				GenerationStep.Decoration stage = GenerationStep.Decoration.valueOf(registryResource.getDecorationStage());
				System.out.println(">>> Configured features:\n");
				BuiltinRegistries.CONFIGURED_FEATURE.stream().forEach(new Consumer(){

					@Override
					public void accept(Object o) {
						if (o instanceof ConfiguredFeature) {
							System.out.println("- " + (ConfiguredFeature)o);
						} else {
							System.out.println("Obj not instance of ConfiguredFeature");
						}
					}
				});
				ConfiguredFeature<?, ?> registry = BuiltinRegistries.CONFIGURED_FEATURE.get(new ResourceLocation(registryResource.getFeatureKey()));
				if (registry == null)
				{
					OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.BIOME_REGISTRY, "Could not find feature "+registryResource.getFeatureKey()+" in the registry, please check spelling");
				} else {
					PlacedFeature feature = registry.placed(PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT);
					biomeGenerationSettingsBuilder.addFeature(stage, feature);
				}
			}
			if (res instanceof GlowLichenResource glow)
			{
				List<BlockState> list = new ArrayList<>();
				for (LocalMaterialBase base : glow.canBePlacedOn)
				{
					if (base instanceof PaperMaterialTag tag)
					{
						if (tag.getTag() == null)
						{
							list.addAll(Arrays.stream(tag.getOtgBlockTag()).map(Block::defaultBlockState).collect(Collectors.toList()));
						} else {
							list.addAll(tag.getTag().getValues().stream().map(Block::defaultBlockState).collect(Collectors.toList()));
						}
					}
					else if (base instanceof PaperMaterialData data)
					{
						list.add(data.internalBlock());
					}
				}
				// Commenting out for now - Frank
				/*GlowLichenConfiguration config = new GlowLichenConfiguration(glow.nearbyAttempts, glow.canPlaceOnFloor, glow.canPlaceOnCeiling, glow.canPlaceOnWall, glow.chanceOfSpreading, list);
				biomeGenerationSettingsBuilder
					.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Feature.GLOW_LICHEN.configured(config).squared()
					.rangeUniform(VerticalAnchor.aboveBottom(glow.minX), VerticalAnchor.absolute(glow.maxX))
					.count(UniformInt.of(glow.countMin, glow.countMax)));*/
			}
		}

		// Default structures
		addVanillaStructures(biomeGenerationSettingsBuilder, worldConfig, biomeConfig);

		float safeTemperature = biomeConfig.getBiomeTemperature();
		if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
		{
			// Avoid temperatures between 0.1 and 0.2, Minecraft restriction
			safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
		}

		// BiomeFog == BiomeAmbient in forge
		BiomeSpecialEffects.Builder biomeAmbienceBuilder =
				new BiomeSpecialEffects.Builder()
						.fogColor(biomeConfig.getFogColor() != BiomeStandardValues.FOG_COLOR.getDefaultValue(null) ? biomeConfig.getFogColor() : worldConfig.getFogColor())
						.waterColor(biomeConfig.getWaterColor() != BiomeStandardValues.WATER_COLOR.getDefaultValue() ? biomeConfig.getWaterColor() : 4159204)
						.waterFogColor(biomeConfig.getWaterFogColor() != BiomeStandardValues.WATER_FOG_COLOR.getDefaultValue() ? biomeConfig.getWaterFogColor() : 329011)
						.skyColor(biomeConfig.getSkyColor() != BiomeStandardValues.SKY_COLOR.getDefaultValue() ? biomeConfig.getSkyColor() : getSkyColorForTemp(safeTemperature))
				//.e() // TODO: Sky color is normally based on temp, make a setting for that?
				;


		Optional<ParticleType<?>> particleType = Registry.PARTICLE_TYPE.getOptional(new ResourceLocation(biomeConfig.getParticleType()));
		if (particleType.isPresent() && particleType.get() instanceof ParticleOptions)
		{
			biomeAmbienceBuilder.ambientParticle(new AmbientParticleSettings((ParticleOptions) particleType.get(), biomeConfig.getParticleProbability()));
		}

		Optional<SoundEvent> music = Registry.SOUND_EVENT.getOptional(new ResourceLocation(biomeConfig.getMusic()));
		music.ifPresent(soundEffect ->
				biomeAmbienceBuilder.backgroundMusic(
						new Music(
								soundEffect,
								biomeConfig.getMusicMinDelay(),
								biomeConfig.getMusicMaxDelay(),
								biomeConfig.isReplaceCurrentMusic()
						)
				)
		);

		Optional<SoundEvent> ambientSound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(biomeConfig.getAmbientSound()));
		ambientSound.ifPresent(soundEffect -> biomeAmbienceBuilder.ambientLoopSound(ambientSound.get()));

		Optional<SoundEvent> moodSound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(biomeConfig.getMoodSound()));
		moodSound.ifPresent(soundEffect ->
				biomeAmbienceBuilder.ambientMoodSound(
						new AmbientMoodSettings(
								moodSound.get(),
								biomeConfig.getMoodSoundDelay(),
								biomeConfig.getMoodSearchRange(),
								biomeConfig.getMoodOffset()
						)
				)
		);

		Optional<SoundEvent> additionsSound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(biomeConfig.getAdditionsSound()));
		additionsSound.ifPresent(soundEffect -> biomeAmbienceBuilder.ambientAdditionsSound(new AmbientAdditionsSettings(additionsSound.get(), biomeConfig.getAdditionsTickChance())));

		if (biomeConfig.getFoliageColor() != 0xffffff)
		{
			biomeAmbienceBuilder.foliageColorOverride(biomeConfig.getFoliageColor());
		}

		if (biomeConfig.getGrassColor() != 0xffffff)
		{
			biomeAmbienceBuilder.grassColorOverride(biomeConfig.getGrassColor());
		}

		switch (biomeConfig.getGrassColorModifier())
		{
			case Swamp -> biomeAmbienceBuilder.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP);
			case DarkForest -> biomeAmbienceBuilder.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST);
			case None -> {}
		}

		Biome.BiomeBuilder builder = new Biome.BiomeBuilder()
				.precipitation(biomeConfig.getBiomeWetness() <= 0.0001 ? Biome.Precipitation.NONE :
						biomeConfig.getBiomeTemperature() > Constants.SNOW_AND_ICE_TEMP ? Biome.Precipitation.RAIN :
								Biome.Precipitation.SNOW)
				//.depth(biomeConfig.getBiomeHeight())
				//.scale(biomeConfig.getBiomeVolatility())
				.temperature(safeTemperature)
				.downfall(biomeConfig.getBiomeWetness())
				.specialEffects(biomeAmbienceBuilder.build())
				.mobSpawnSettings(mobSpawnInfoBuilder.build())
				// All other biome settings...
				.generationSettings(biomeGenerationSettingsBuilder.build());

		if(biomeConfig.useFrozenOceanTemperature())
		{
			builder.temperatureAdjustment(TemperatureModifier.FROZEN);
		}

		builder.biomeCategory(Biome.BiomeCategory.byName(biomeConfig.getBiomeCategory()));

		return builder.build();
	}

	private static int getSkyColorForTemp (float safeTemperature)
	{
		float lvt_1_1_ = safeTemperature / 3.0F;
		lvt_1_1_ = Mth.clamp(lvt_1_1_, -1.0F, 1.0F);
		return Mth.hsvToRgb(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}

	private static void addVanillaStructures (BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		// TODO: Currently we can only enable/disable structures per biome and use any configuration options exposed by the vanilla structure
		// classes (size for villages fe). If we want to be able to customise more, we'll need to implement our own structure classes.
		// TODO: Allow users to create their own jigsaw patterns (for villages, end cities, pillager outposts etc)?
		// TODO: Fossils?
		// TODO: Amethyst Geodes (1.17?)
		// TODO: Misc structures: These structures generate even when the "Generate structures" world option is disabled, and also cannot be located with the /locate command.
		// - Dungeons
		// - Desert Wells

		// Villages
		// TODO: Allow spawning multiple types in a single biome?
		// Cutting this out for the time being - Frank
		/*if (worldConfig.getVillagesEnabled() && biomeConfig.getVillageType() != SettingsEnums.VillageType.disabled)
		{
			int villageSize = biomeConfig.getVillageSize();
			SettingsEnums.VillageType villageType = biomeConfig.getVillageType();
			ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> customVillage = register(
					((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("village").toResourceLocationString(),
					StructureFeature.VILLAGE.configured(
							new JigsawConfiguration(
									() -> {
										switch (villageType)
										{
											case sandstone:
												return DesertVillagePools.START;
											case savanna:
												return SavannaVillagePools.START;
											case taiga:
												return TaigaVillagePools.START;
											case wood:
												return PlainVillagePools.START;
											case snowy:
												return SnowyVillagePools.START;
											case disabled: // Should never happen
												break;
										}
										return PlainVillagePools.START;
									},
									villageSize
							)
					)
			);
			biomeGenerationSettingsBuilder.(GenerationStep.Decoration.SURFACE_STRUCTURES, );
		}

		// Strongholds
		if (worldConfig.getStrongholdsEnabled() && biomeConfig.getStrongholdsEnabled())
		{
			biomeGenerationSettingsBuilder.addFeature(GenerationStep.Decoration.STRONGHOLDS, StructureFeatures.STRONGHOLD);
		}

		// Ocean Monuments
		if (worldConfig.getOceanMonumentsEnabled() && biomeConfig.getOceanMonumentsEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
		}

		// Rare buildings
		// TODO: Allow spawning multiple types in a single biome?
		if (worldConfig.getRareBuildingsEnabled() && biomeConfig.getRareBuildingType() != SettingsEnums.RareBuildingType.disabled)
		{
			switch (biomeConfig.getRareBuildingType())
			{
				case desertPyramid:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.DESERT_PYRAMID);
					break;
				case igloo:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.IGLOO);
					break;
				case jungleTemple:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.JUNGLE_TEMPLE);
					break;
				case swampHut:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.SWAMP_HUT);
					break;
				case disabled:
					break;
			}
		}

		// Woodland Mansions
		if (worldConfig.getWoodlandMansionsEnabled() && biomeConfig.getWoodlandMansionsEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.WOODLAND_MANSION);
		}

		// Nether Fortresses
		if (worldConfig.getNetherFortressesEnabled() && biomeConfig.getNetherFortressesEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.NETHER_FOSSIL);
		}

		// Mineshafts
		if (worldConfig.getMineshaftsEnabled() && biomeConfig.getMineShaftType() != SettingsEnums.MineshaftType.disabled)
		{
			float mineShaftProbability = biomeConfig.getMineShaftProbability();
			SettingsEnums.MineshaftType mineShaftType = biomeConfig.getMineShaftType();
			ConfiguredStructureFeature<MineshaftConfiguration, ? extends StructureFeature<MineshaftConfiguration>> customMineShaft = register(
					((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("mineshaft").toResourceLocationString(),
					StructureFeature.MINESHAFT.configured(
							new MineshaftConfiguration(
									mineShaftProbability,
									mineShaftType == SettingsEnums.MineshaftType.mesa ? MineshaftFeature.Type.MESA : MineshaftFeature.Type.NORMAL
							)
					)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customMineShaft);
		}

		// Buried Treasure
		if (worldConfig.getBuriedTreasureEnabled() && biomeConfig.getBuriedTreasureEnabled())
		{
			float buriedTreasureProbability = biomeConfig.getBuriedTreasureProbability();
			ConfiguredStructureFeature<ProbabilityFeatureConfiguration, ? extends StructureFeature<ProbabilityFeatureConfiguration>> customBuriedTreasure = register(
					((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("buried_treasure").toResourceLocationString(),
					StructureFeature.BURIED_TREASURE.configured(new ProbabilityFeatureConfiguration(buriedTreasureProbability))
			);
			biomeGenerationSettingsBuilder.addStructureStart(customBuriedTreasure);
		}

		// Ocean Ruins
		if (worldConfig.getOceanRuinsEnabled() && biomeConfig.getOceanRuinsType() != SettingsEnums.OceanRuinsType.disabled)
		{
			float oceanRuinsLargeProbability = biomeConfig.getOceanRuinsLargeProbability();
			float oceanRuinsClusterProbability = biomeConfig.getOceanRuinsClusterProbability();
			SettingsEnums.OceanRuinsType oceanRuinsType = biomeConfig.getOceanRuinsType();
			ConfiguredStructureFeature<OceanRuinConfiguration, ? extends StructureFeature<OceanRuinConfiguration>> customOceanRuins = register(
					((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("ocean_ruin").toResourceLocationString(),
					StructureFeature.OCEAN_RUIN.configured(
							new OceanRuinConfiguration(
									oceanRuinsType == SettingsEnums.OceanRuinsType.cold ? OceanRuinFeature.Type.COLD : OceanRuinFeature.Type.WARM,
									oceanRuinsLargeProbability,
									oceanRuinsClusterProbability
							)
					)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customOceanRuins);
		}

		// Shipwrecks
		// TODO: Allowing both types in the same biome, make sure this won't cause problems.
		if (worldConfig.getShipWrecksEnabled())
		{
			if (biomeConfig.getShipWreckEnabled())
			{
				biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.SHIPWRECK);
			}
			if (biomeConfig.getShipWreckBeachedEnabled())
			{
				biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
			}
		}

		// Pillager Outpost
		if (worldConfig.getPillagerOutpostsEnabled() && biomeConfig.getPillagerOutpostEnabled())
		{
			int outpostSize = biomeConfig.getPillagerOutPostSize();
			ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> customOutpost = register(
					((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("pillager_outpost").toResourceLocationString(),
					StructureFeature.PILLAGER_OUTPOST.configured(
							new JigsawConfiguration(
									() -> PillagerOutpostPools.START,
									outpostSize
							)
					)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customOutpost);
		}

		// Bastion Remnants
		if (worldConfig.getBastionRemnantsEnabled() && biomeConfig.getBastionRemnantEnabled())
		{
			int bastionRemnantSize = biomeConfig.getBastionRemnantSize();
			ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> customBastionRemnant = register(
					((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("bastion_remnant").toResourceLocationString(),
					StructureFeature.BASTION_REMNANT.configured(
							new JigsawConfiguration(
									() -> BastionPieces.START,
									bastionRemnantSize
							)
					)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customBastionRemnant);
		}

		// Nether Fossils
		if (worldConfig.getNetherFossilsEnabled() && biomeConfig.getNetherFossilEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.NETHER_FOSSIL);
		}

		// End Cities
		if (worldConfig.getEndCitiesEnabled() && biomeConfig.getEndCityEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.END_CITY);
		}

		// Ruined Portals
		if (worldConfig.getRuinedPortalsEnabled() && biomeConfig.getRuinedPortalType() != SettingsEnums.RuinedPortalType.disabled)
		{
			switch (biomeConfig.getRuinedPortalType())
			{
				case normal:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
					break;
				case desert:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_DESERT);
					break;
				case jungle:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_JUNGLE);
					break;
				case swamp:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
					break;
				case mountain:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
					break;
				case ocean:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
					break;
				case nether:
					biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
					break;
				case disabled:
					break;
			}
		}*/
	}

	// StructureFeatures.register()
	private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> ConfiguredStructureFeature<FC, F> register (String name, ConfiguredStructureFeature<FC, F> structure)
	{
		return Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, name, structure);
	}

	private static MobSpawnSettings.Builder createMobSpawnInfo (IBiomeConfig biomeConfig)
	{
		MobSpawnSettings.Builder mobSpawnInfoBuilder = new MobSpawnSettings.Builder();
		addMobGroup(MobCategory.MONSTER, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.MONSTER), biomeConfig.getName());
		addMobGroup(MobCategory.CREATURE, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.CREATURE), biomeConfig.getName());
		addMobGroup(MobCategory.AMBIENT, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.AMBIENT), biomeConfig.getName());
		addMobGroup(MobCategory.UNDERGROUND_WATER_CREATURE, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.UNDERGROUND_WATER_CREATURE), biomeConfig.getName());
		addMobGroup(MobCategory.WATER_CREATURE, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.WATER_CREATURE), biomeConfig.getName());
		addMobGroup(MobCategory.WATER_AMBIENT, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.WATER_AMBIENT), biomeConfig.getName());
		addMobGroup(MobCategory.MISC, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.MISC), biomeConfig.getName());

		// This functionality must've been removed? - Frank
		//mobSpawnInfoBuilder.setPlayerCanSpawn();
		return mobSpawnInfoBuilder;
	}

	private static void addMobGroup(MobCategory creatureType, MobSpawnSettings.Builder mobSpawnInfoBuilder, List<WeightedMobSpawnGroup> mobSpawnGroupList, String biomeName)
	{
		for (WeightedMobSpawnGroup mobSpawnGroup : mobSpawnGroupList)
		{
			Optional<EntityType<?>> entityType = EntityType.byString(mobSpawnGroup.getInternalName());
			if (entityType.isPresent())
			{
				mobSpawnInfoBuilder.addSpawn(creatureType, new MobSpawnSettings.SpawnerData(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MOBS))
				{
					OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MOBS, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeName);
				}
			}
		}
	}

	@Override
	public float getTemperatureAt (int x, int y, int z)
	{
		return this.biomeBase.getTemperature(new BlockPos(x, y, z));
	}
}
