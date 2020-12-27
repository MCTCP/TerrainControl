package com.pg85.otg.spigot.biome;

import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.interfaces.IBiome;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import net.minecraft.server.v1_16_R3.*;

import java.util.Optional;

public class SpigotBiome implements IBiome
{
	private final BiomeBase biomeBase;
	private final IBiomeConfig biomeConfig;

	public SpigotBiome (BiomeBase biomeBase, IBiomeConfig biomeConfig)
	{
		this.biomeBase = biomeBase;
		this.biomeConfig = biomeConfig;
	}

	public static BiomeBase createOTGBiome (boolean isOceanBiome, WorldConfig worldConfig, BiomeConfig biomeConfig)
	{
		// BiomeSettingsGeneration.a == BiomeGenerationSettings.Builder in forge
		BiomeSettingsGeneration.a biomeGenerationSettingsBuilder = new BiomeSettingsGeneration.a();

		BiomeSettingsMobs.a mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);

		// NOOP surface builder, surface/ground/stone blocks / sagc are done during base terrain gen.
		// a() == withSurfaceBuilder() in forge
		// WorldGenSurfaceComposites.j == ConfiguredSurfaceBuilders.field_244184_p in forge
		biomeGenerationSettingsBuilder.a(WorldGenSurfaceComposites.j);

		// * Carvers are handled by OTG

		// Default structures
		addVanillaStructures(biomeGenerationSettingsBuilder, worldConfig, biomeConfig);

		float safeTemperature = biomeConfig.getBiomeTemperature();
		if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
		{
			// Avoid temperatures between 0.1 and 0.2, Minecraft restriction
			safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
		}

		// BiomeFog == BiomeAmbient in forge
		BiomeFog.a biomeAmbienceBuilder =
				new BiomeFog.a() // fog, water, water fog, sky -> a, b, c, d
						.a(biomeConfig.getFogColor() != BiomeStandardValues.FOG_COLOR.getDefaultValue(null) ? biomeConfig.getFogColor() : worldConfig.getFogColor())
						.b(biomeConfig.getWaterColor() != BiomeStandardValues.WATER_COLOR.getDefaultValue() ? biomeConfig.getWaterColor() : 4159204)
						.c(biomeConfig.getWaterFogColor() != BiomeStandardValues.WATER_FOG_COLOR.getDefaultValue() ? biomeConfig.getWaterFogColor() : 329011)
						.d(biomeConfig.getSkyColor() != BiomeStandardValues.SKY_COLOR.getDefaultValue() ? biomeConfig.getSkyColor() : getSkyColorForTemp(safeTemperature))
						//.e() // TODO: Sky color is normally based on temp, make a setting for that?
						// TODO: Implement these
						// particle
						// .func_235244_a_()
						// ambient_sound
						// .func_235241_a_() // Sound event?
						// mood_sound
						.a(CaveSoundSettings.b)
				//additions_sound
				//.func_235242_a_()
				//music
				//.func_235240_a_()
				;

		if (biomeConfig.getFoliageColor() != 0xffffff)
		{
			biomeAmbienceBuilder.e(biomeConfig.getFoliageColor());
		}

		if (biomeConfig.getGrassColor() != 0xffffff)
		{
			biomeAmbienceBuilder.f(biomeConfig.getGrassColor());
		}

		switch (biomeConfig.getGrassColorModifier())
		{
			//TODO: Find the right NMS methods here
			case Swamp:
				//biomeAmbienceBuilder.withGrassColorModifier(BiomeAmbience.GrassColorModifier.SWAMP);
				break;
			case DarkForest:
				//biomeAmbienceBuilder.withGrassColorModifier(BiomeAmbience.GrassColorModifier.DARK_FOREST);
				break;
			default:
				break;
		}

		BiomeBase.a builder = new BiomeBase.a()
				// Precipitation
				.a(biomeConfig.getBiomeWetness() <= 0.0001 ? BiomeBase.Precipitation.NONE :
				   biomeConfig.getBiomeTemperature() > Constants.SNOW_AND_ICE_TEMP ? BiomeBase.Precipitation.RAIN :
				   BiomeBase.Precipitation.SNOW)
				// Biome Category TODO: Expand this beyond oceans
				.a(isOceanBiome ? BiomeBase.Geography.OCEAN : BiomeBase.Geography.PLAINS)
				// depth
				.a(biomeConfig.getBiomeHeight())
				.b(biomeConfig.getBiomeVolatility())
				.c(safeTemperature)
				.d(biomeConfig.getBiomeWetness())
				// Ambience (colours/sounds)
				.a(biomeAmbienceBuilder.a())
				// Mob spawning
				.a(mobSpawnInfoBuilder.b())
				// All other biome settings...
				.a(biomeGenerationSettingsBuilder.a());

		return builder.a();
	}

	private static int getSkyColorForTemp (float safeTemperature)
	{
		float lvt_1_1_ = safeTemperature / 3.0F;
		// a == clamp
		lvt_1_1_ = MathHelper.a(lvt_1_1_, -1.0F, 1.0F);
		// f == hsvToRGB
		return MathHelper.f(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}

	private static void addVanillaStructures (BiomeSettingsGeneration.a biomeGenerationSettingsBuilder, WorldConfig worldConfig, BiomeConfig biomeConfig)
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
		if (worldConfig.getVillagesEnabled() && biomeConfig.getVillageType() != SettingsEnums.VillageType.disabled)
		{
			int villageSize = biomeConfig.getVillageSize();
			SettingsEnums.VillageType villageType = biomeConfig.getVillageType();
			StructureFeature<WorldGenFeatureVillageConfiguration, ? extends StructureGenerator<WorldGenFeatureVillageConfiguration>> customVillage = register(
					biomeConfig.getRegistryKey().withBiomeResource("village").toResourceLocationString(),
					StructureGenerator.VILLAGE.a(
							new WorldGenFeatureVillageConfiguration(
									() ->
									{
										switch (villageType)
										{
											case sandstone:
												return WorldGenFeatureDesertVillage.a;
											case savanna:
												return WorldGenFeatureVillageSavanna.a;
											case taiga:
												return WorldGenFeatureVillageTaiga.a;
											case wood:
												return WorldGenFeatureVillagePlain.a;
											case snowy:
												return WorldGenFeatureVillageSnowy.a;
											case disabled: // Should never happen
												break;
										}
										return WorldGenFeatureVillagePlain.a;
									},
									villageSize
							)
					)
			);
			// a() == withStructure() in forge
			biomeGenerationSettingsBuilder.a(customVillage);
		}

		// Strongholds
		if (worldConfig.getStrongholdsEnabled() && biomeConfig.getStrongholdsEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.k);
		}

		// Ocean Monuments
		if (worldConfig.getOceanMonumentsEnabled() && biomeConfig.getOceanMonumentsEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.l);
		}

		// Rare buildings
		// TODO: Allow spawning multiple types in a single biome?
		if (worldConfig.getRareBuildingsEnabled() && biomeConfig.getRareBuildingType() != SettingsEnums.RareBuildingType.disabled)
		{
			switch (biomeConfig.getRareBuildingType())
			{
				case desertPyramid:
					biomeGenerationSettingsBuilder.a(StructureFeatures.f);
					break;
				case igloo:
					biomeGenerationSettingsBuilder.a(StructureFeatures.g);
					break;
				case jungleTemple:
					biomeGenerationSettingsBuilder.a(StructureFeatures.e);
					break;
				case swampHut:
					biomeGenerationSettingsBuilder.a(StructureFeatures.j);
					break;
				case disabled:
					break;
			}
		}

		// Woodland Mansions
		if (worldConfig.getWoodlandMansionsEnabled() && biomeConfig.getWoodlandMansionsEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.d);
		}

		// Nether Fortresses
		if (worldConfig.getNetherFortressesEnabled() && biomeConfig.getNetherFortressesEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.o);
		}

		// Mineshafts
		if (worldConfig.getMineshaftsEnabled() && biomeConfig.getMineShaftType() != SettingsEnums.MineshaftType.disabled)
		{
			float mineShaftProbability = biomeConfig.getMineShaftProbability();
			SettingsEnums.MineshaftType mineShaftType = biomeConfig.getMineShaftType();
			StructureFeature<WorldGenMineshaftConfiguration, ? extends StructureGenerator<WorldGenMineshaftConfiguration>> customMineShaft = register(
					biomeConfig.getRegistryKey().withBiomeResource("mineshaft").toResourceLocationString(),
					StructureGenerator.MINESHAFT.a(
							new WorldGenMineshaftConfiguration(
									mineShaftProbability,
									mineShaftType == SettingsEnums.MineshaftType.mesa ? WorldGenMineshaft.Type.MESA : WorldGenMineshaft.Type.NORMAL
							)
					)
			);
			biomeGenerationSettingsBuilder.a(customMineShaft);
		}

		// Buried Treasure
		if (worldConfig.getBuriedTreasureEnabled() && biomeConfig.getBuriedTreasureEnabled())
		{
			float buriedTreasureProbability = biomeConfig.getBuriedTreasureProbability();
			StructureFeature<WorldGenFeatureConfigurationChance, ? extends StructureGenerator<WorldGenFeatureConfigurationChance>> customBuriedTreasure = register(
					biomeConfig.getRegistryKey().withBiomeResource("buried_treasure").toResourceLocationString(),
					StructureGenerator.BURIED_TREASURE.a(new WorldGenFeatureConfigurationChance(buriedTreasureProbability))
			);
			biomeGenerationSettingsBuilder.a(customBuriedTreasure);
		}

		// Ocean Ruins
		if (worldConfig.getOceanRuinsEnabled() && biomeConfig.getOceanRuinsType() != SettingsEnums.OceanRuinsType.disabled)
		{
			float oceanRuinsLargeProbability = biomeConfig.getOceanRuinsLargeProbability();
			float oceanRuinsClusterProbability = biomeConfig.getOceanRuinsClusterProbability();
			SettingsEnums.OceanRuinsType oceanRuinsType = biomeConfig.getOceanRuinsType();
			StructureFeature<WorldGenFeatureOceanRuinConfiguration, ? extends StructureGenerator<WorldGenFeatureOceanRuinConfiguration>> customOceanRuins = register(
					biomeConfig.getRegistryKey().withBiomeResource("ocean_ruin").toResourceLocationString(),
					StructureGenerator.OCEAN_RUIN.a(
							new WorldGenFeatureOceanRuinConfiguration(
									oceanRuinsType == SettingsEnums.OceanRuinsType.cold ? WorldGenFeatureOceanRuin.Temperature.COLD : WorldGenFeatureOceanRuin.Temperature.WARM,
									oceanRuinsLargeProbability,
									oceanRuinsClusterProbability
							)
					)
			);
			biomeGenerationSettingsBuilder.a(customOceanRuins);
		}

		// Shipwrecks
		// TODO: Allowing both types in the same biome, make sure this won't cause problems.
		if (worldConfig.getShipWrecksEnabled())
		{
			if (biomeConfig.getShipWreckEnabled())
			{
				biomeGenerationSettingsBuilder.a(StructureFeatures.h);
			}
			if (biomeConfig.getShipWreckBeachedEnabled())
			{
				biomeGenerationSettingsBuilder.a(StructureFeatures.i);
			}
		}

		// Pillager Outpost
		if (worldConfig.getPillagerOutpostsEnabled() && biomeConfig.getPillagerOutpostEnabled())
		{
			int outpostSize = biomeConfig.getPillagerOutPostSize();
			StructureFeature<WorldGenFeatureVillageConfiguration, ? extends StructureGenerator<WorldGenFeatureVillageConfiguration>> customOutpost = register(
					biomeConfig.getRegistryKey().withBiomeResource("pillager_outpost").toResourceLocationString(),
					StructureGenerator.PILLAGER_OUTPOST.a(
							new WorldGenFeatureVillageConfiguration(
									() -> WorldGenFeaturePillagerOutpostPieces.a,
									outpostSize
							)
					)
			);
			biomeGenerationSettingsBuilder.a(customOutpost);
		}

		// Bastion Remnants
		if (worldConfig.getBastionRemnantsEnabled() && biomeConfig.getBastionRemnantEnabled())
		{
			int bastionRemnantSize = biomeConfig.getBastionRemnantSize();
			StructureFeature<WorldGenFeatureVillageConfiguration, ? extends StructureGenerator<WorldGenFeatureVillageConfiguration>> customBastionRemnant = register(
					biomeConfig.getRegistryKey().withBiomeResource("bastion_remnant").toResourceLocationString(),
					StructureGenerator.BASTION_REMNANT.a(
							new WorldGenFeatureVillageConfiguration(
									() -> WorldGenFeatureBastionPieces.a,
									bastionRemnantSize
							)
					)
			);
			biomeGenerationSettingsBuilder.a(customBastionRemnant);
		}

		// Nether Fossils
		if (worldConfig.getNetherFossilsEnabled() && biomeConfig.getNetherFossilEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.p);
		}

		// End Cities
		if (worldConfig.getEndCitiesEnabled() && biomeConfig.getEndCityEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.q);
		}

		// Ruined Portals
		if (worldConfig.getRuinedPortalsEnabled() && biomeConfig.getRuinedPortalType() != SettingsEnums.RuinedPortalType.disabled)
		{
			switch (biomeConfig.getRuinedPortalType())
			{
				case normal:
					biomeGenerationSettingsBuilder.a(StructureFeatures.y);
					break;
				case desert:
					biomeGenerationSettingsBuilder.a(StructureFeatures.z);
					break;
				case jungle:
					biomeGenerationSettingsBuilder.a(StructureFeatures.A);
					break;
				case swamp:
					biomeGenerationSettingsBuilder.a(StructureFeatures.B);
					break;
				case mountain:
					biomeGenerationSettingsBuilder.a(StructureFeatures.C);
					break;
				case ocean:
					biomeGenerationSettingsBuilder.a(StructureFeatures.D);
					break;
				case nether:
					biomeGenerationSettingsBuilder.a(StructureFeatures.E);
					break;
				case disabled:
					break;
			}
		}
	}

	// StructureFeatures.register()
	private static <FC extends WorldGenFeatureConfiguration, F extends StructureGenerator<FC>> StructureFeature<FC, F> register (String name, StructureFeature<FC, F> structure)
	{
		// RegistryGeneration == WorldGenRegistries
		// a() == register()
		// f == CONFIGURED_STRUCTURE_FEATURE
		return RegistryGeneration.a(RegistryGeneration.f, name, structure);
	}

	private static BiomeSettingsMobs.a createMobSpawnInfo (BiomeConfig biomeConfig)
	{
		// BiomeSettingsMobs.a == MobSpawnInfo.Builder() in forge
		BiomeSettingsMobs.a mobSpawnInfoBuilder = new BiomeSettingsMobs.a();
		for (WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.getMonsters())
		{
			// a() == byKey() in forge
			Optional<EntityTypes<?>> entityType = EntityTypes.a(mobSpawnGroup.getInternalName());
			if (entityType.isPresent())
			{
				// a() == withSpawner() in forge
				mobSpawnInfoBuilder.a(EnumCreatureType.MONSTER, new BiomeSettingsMobs.c(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			}
			else
			{
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		for (WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.getCreatures())
		{
			Optional<EntityTypes<?>> entityType = EntityTypes.a(mobSpawnGroup.getInternalName());
			if (entityType.isPresent())
			{
				mobSpawnInfoBuilder.a(EnumCreatureType.CREATURE, new BiomeSettingsMobs.c(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			}
			else
			{
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		for (WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.getWaterCreatures())
		{
			Optional<EntityTypes<?>> entityType = EntityTypes.a(mobSpawnGroup.getInternalName());
			if (entityType.isPresent())
			{
				mobSpawnInfoBuilder.a(EnumCreatureType.WATER_CREATURE, new BiomeSettingsMobs.c(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			}
			else
			{
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		for (WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.getAmbientCreatures())
		{
			Optional<EntityTypes<?>> entityType = EntityTypes.a(mobSpawnGroup.getInternalName());
			if (entityType.isPresent())
			{
				mobSpawnInfoBuilder.a(EnumCreatureType.AMBIENT, new BiomeSettingsMobs.c(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			}
			else
			{
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}

		// TODO: EnumCreatureType.WATER_AMBIENT / EnumCreatureType.MISC ?
		// a() == isValidSpawnBiomeForPlayer()
		mobSpawnInfoBuilder.a(); // Default biomes do this, not sure if needed. Does the opposite of disablePlayerSpawn?
		return mobSpawnInfoBuilder;
	}

	@Override
	public IBiomeConfig getBiomeConfig ()
	{
		return this.biomeConfig;
	}

	@Override
	public float getTemperatureAt (int x, int y, int z)
	{
		return this.biomeBase.getAdjustedTemperature(new BlockPosition(x, y, z));
	}
}
