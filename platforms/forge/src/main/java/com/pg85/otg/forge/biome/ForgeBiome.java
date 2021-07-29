package com.pg85.otg.forge.biome;

import java.util.List;
import java.util.Optional;
import com.pg85.otg.OTG;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.OceanRuinsType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.RuinedPortalType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.client.audio.BackgroundMusicSelector;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.*;
import net.minecraft.world.biome.Biome.TemperatureModifier;
import net.minecraft.world.biome.BiomeGenerationSettings.Builder;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.BastionRemnantsPieces;
import net.minecraft.world.gen.feature.structure.DesertVillagePools;
import net.minecraft.world.gen.feature.structure.MineshaftConfig;
import net.minecraft.world.gen.feature.structure.MineshaftStructure;
import net.minecraft.world.gen.feature.structure.OceanRuinConfig;
import net.minecraft.world.gen.feature.structure.OceanRuinStructure;
import net.minecraft.world.gen.feature.structure.PillagerOutpostPools;
import net.minecraft.world.gen.feature.structure.PlainsVillagePools;
import net.minecraft.world.gen.feature.structure.SavannaVillagePools;
import net.minecraft.world.gen.feature.structure.SnowyVillagePools;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.feature.structure.TaigaVillagePools;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilders;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeBiome implements IBiome
{
	private final Biome biomeBase;
	private final IBiomeConfig biomeConfig;
	
	public ForgeBiome(Biome biomeBase, IBiomeConfig biomeConfig)
	{
		this.biomeBase = biomeBase;
		this.biomeConfig = biomeConfig;
	}

	@Override
	public float getTemperatureAt(int x, int y, int z)
	{
		return this.biomeBase.getTemperature(new BlockPos(x, y, z));
	}

	@Override
	public IBiomeConfig getBiomeConfig()
	{
		return this.biomeConfig;
	}
	
	public Biome getBiomeBase()
	{
		return biomeBase;
	}

	public static Biome createOTGBiome(boolean isOceanBiome, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder = new BiomeGenerationSettings.Builder();

		// Mob spawning
		MobSpawnInfo.Builder mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);
		
		// Surface/ground/stone blocks / sagc are done during base terrain gen.
		// Spawn point detection checks for surfacebuilder blocks, so using ConfiguredSurfaceBuilders.GRASS.
		// TODO: What if there's no grass around spawn?
		biomeGenerationSettingsBuilder.surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);

		// * Carvers are handled by OTG
		
		// Default structures
		addVanillaStructures(biomeGenerationSettingsBuilder, worldConfig, biomeConfig);	

		float safeTemperature = biomeConfig.getBiomeTemperature();
		if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
		{
			// Avoid temperatures between 0.1 and 0.2, Minecraft restriction
			safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
		}

		BiomeAmbience.Builder biomeAmbienceBuilder =
			new BiomeAmbience.Builder()			
				.fogColor(biomeConfig.getFogColor() != BiomeStandardValues.FOG_COLOR.getDefaultValue() ? biomeConfig.getFogColor() : worldConfig.getFogColor())
				.waterFogColor(biomeConfig.getWaterFogColor() != BiomeStandardValues.WATER_FOG_COLOR.getDefaultValue() ? biomeConfig.getWaterFogColor() : 329011)
				.waterColor(biomeConfig.getWaterColor() != BiomeStandardValues.WATER_COLOR.getDefaultValue() ? biomeConfig.getWaterColor() : 4159204)
				.skyColor(biomeConfig.getSkyColor() != BiomeStandardValues.SKY_COLOR.getDefaultValue() ? biomeConfig.getSkyColor() : getSkyColorForTemp(safeTemperature)) // TODO: Sky color is normally based on temp, make a setting for that?
		;

		@SuppressWarnings("deprecation")
		Optional<ParticleType<?>> particleType = Registry.PARTICLE_TYPE.getOptional(new ResourceLocation(biomeConfig.getParticleType()));
		if(particleType.isPresent() && particleType.get() instanceof IParticleData)
		{
			biomeAmbienceBuilder.ambientParticle(new ParticleEffectAmbience((IParticleData)particleType.get(), biomeConfig.getParticleProbability()));	
		}

		SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(biomeConfig.getMusic()));
		if (event != null)
		{
			biomeAmbienceBuilder.backgroundMusic(new BackgroundMusicSelector(event,
				biomeConfig.getMusicMinDelay(),
				biomeConfig.getMusicMaxDelay(),
				biomeConfig.isReplaceCurrentMusic()));
		}

		event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(biomeConfig.getAmbientSound()));
		if (event != null)
		{
			biomeAmbienceBuilder.ambientLoopSound(event);
		}

		event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(biomeConfig.getMoodSound()));
		if (event != null)
		{
			biomeAmbienceBuilder.ambientMoodSound(new MoodSoundAmbience(event,
				biomeConfig.getMoodSoundDelay(),
				biomeConfig.getMoodSearchRange(),
				biomeConfig.getMoodOffset()));
		}

		event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(biomeConfig.getAdditionsSound()));
		if (event != null)
		{
			biomeAmbienceBuilder.ambientAdditionsSound(new SoundAdditionsAmbience(event, biomeConfig.getAdditionsTickChance()));
		}

		if(biomeConfig.getFoliageColor() != 0xffffff)
		{
			biomeAmbienceBuilder.foliageColorOverride(biomeConfig.getFoliageColor());
		}

		if(biomeConfig.getGrassColor() != 0xffffff)
		{
			biomeAmbienceBuilder.grassColorOverride(biomeConfig.getGrassColor());
		}
		
		switch(biomeConfig.getGrassColorModifier())
		{
			case Swamp:
				biomeAmbienceBuilder.grassColorModifier(BiomeAmbience.GrassColorModifier.SWAMP);
				break;
			case DarkForest:
				biomeAmbienceBuilder.grassColorModifier(BiomeAmbience.GrassColorModifier.DARK_FOREST);
				break;
			default:
				break;
		}
		
		ResourceLocation registryName = new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString());
		Biome.Category category = Biome.Category.byName(biomeConfig.getBiomeCategory());
		if (category == null)
		{
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Could not parse biome category " + biomeConfig.getBiomeCategory());
			}
		}
        Biome.RainType rainType = 
    		biomeConfig.getBiomeWetness() <= 0.0001 ? Biome.RainType.NONE : 
			biomeConfig.getBiomeTemperature() > Constants.SNOW_AND_ICE_TEMP ? Biome.RainType.RAIN : 
			Biome.RainType.SNOW
		;

		// Fire Forge BiomeLoadingEvent to allow other mods to enrich otg biomes with decoration features, structure features and mob spawns.
        BiomeGenerationSettingsBuilder genBuilder = new BiomeGenerationSettingsBuilder(biomeGenerationSettingsBuilder.build());
        MobSpawnInfoBuilder spawnBuilder = new MobSpawnInfoBuilder(mobSpawnInfoBuilder.build());
        BiomeLoadingEvent event1 = new BiomeLoadingEvent(registryName, new Biome.Climate(rainType, safeTemperature, TemperatureModifier.NONE, biomeConfig.getBiomeWetness()), category, biomeConfig.getBiomeHeight(), biomeConfig.getBiomeVolatility(), biomeAmbienceBuilder.build(), genBuilder, spawnBuilder);
        MinecraftForge.EVENT_BUS.post(event1);
        BiomeAmbience biomeAmbienceBuilder2 = event1.getEffects();
        BiomeGenerationSettingsBuilder biomeGenerationSettingsBuilder2 = event1.getGeneration();
        MobSpawnInfoBuilder mobSpawnInfoBuilder2 = event1.getSpawns();
        //

		Biome.Builder biomeBuilder = 
			new Biome.Builder()
			.precipitation(rainType)
			.depth(biomeConfig.getBiomeHeight())
			.scale(biomeConfig.getBiomeVolatility())
			.temperature(safeTemperature)
			.downfall(biomeConfig.getBiomeWetness())
			.specialEffects(biomeAmbienceBuilder2)
			.mobSpawnSettings(mobSpawnInfoBuilder2.build())
			.generationSettings(biomeGenerationSettingsBuilder2.build())
		;
		
		if(biomeConfig.useFrozenOceanTemperature())
		{
			biomeBuilder.temperatureAdjustment(Biome.TemperatureModifier.FROZEN);
		}

		biomeBuilder.biomeCategory(category != null ? category : isOceanBiome ? Biome.Category.OCEAN : Biome.Category.PLAINS);
		
		return biomeBuilder.build().setRegistryName(registryName);
	}

	private static MobSpawnInfo.Builder createMobSpawnInfo(IBiomeConfig biomeConfig)
	{
		MobSpawnInfo.Builder mobSpawnInfoBuilder = new MobSpawnInfo.Builder();
		addMobGroup(EntityClassification.MONSTER, mobSpawnInfoBuilder, biomeConfig.getMonsters(), biomeConfig.getName());
		addMobGroup(EntityClassification.CREATURE, mobSpawnInfoBuilder, biomeConfig.getCreatures(), biomeConfig.getName());
		addMobGroup(EntityClassification.WATER_CREATURE, mobSpawnInfoBuilder, biomeConfig.getWaterCreatures(), biomeConfig.getName());
		addMobGroup(EntityClassification.AMBIENT, mobSpawnInfoBuilder, biomeConfig.getAmbientCreatures(), biomeConfig.getName());
		addMobGroup(EntityClassification.WATER_AMBIENT, mobSpawnInfoBuilder, biomeConfig.getWaterAmbientCreatures(), biomeConfig.getName());
		addMobGroup(EntityClassification.MISC, mobSpawnInfoBuilder, biomeConfig.getMiscCreatures(), biomeConfig.getName());
		mobSpawnInfoBuilder.setPlayerCanSpawn();
		return mobSpawnInfoBuilder;
	}

	private static void addMobGroup(EntityClassification entitiClassification, MobSpawnInfo.Builder mobSpawnInfoBuilder, List<WeightedMobSpawnGroup> mobSpawnGroupList, String biomeName)
	{
		for(WeightedMobSpawnGroup mobSpawnGroup : mobSpawnGroupList)
		{
			Optional<EntityType<?>> entityType = EntityType.byString(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.addSpawn(entitiClassification, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MOBS))
				{
					OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MOBS, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeName);
				}
			}
		}
	}	
	
	private static void addVanillaStructures(Builder biomeGenerationSettingsBuilder, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		// TODO: Currently we can only enable/disable structures per biome and use any configuration options exposed by the vanilla structure 
		// classes (size for villages fe). If we want to be able to customise more, we'll need to implement our own structure classes.
		// TODO: Allow users to create their own jigsaw patterns (for villages, end cities, pillager outposts etc)?
		// TODO: Amethyst Geodes (1.17?)	

		// Villages
		// TODO: Allow spawning multiple types in a single biome?
		if(worldConfig.getVillagesEnabled() && biomeConfig.getVillageType() != VillageType.disabled)
		{
			int villageSize = biomeConfig.getVillageSize();
			VillageType villageType = biomeConfig.getVillageType();
			StructureFeature<VillageConfig, ? extends Structure<VillageConfig>> customVillage = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("village").toResourceLocationString(),
				Structure.VILLAGE.configured(
					new VillageConfig(
						() -> {
							switch(villageType)
							{
								case sandstone:
									return DesertVillagePools.START;
								case savanna:
									return SavannaVillagePools.START;
								case taiga:
									return TaigaVillagePools.START;
								case wood:
									return PlainsVillagePools.START;
								case snowy:
									return SnowyVillagePools.START;
								case disabled: // Should never happen
									break;
							}
							return PlainsVillagePools.START;
						},
						villageSize
					)
				)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customVillage);
		}
		
		// Strongholds
		if(worldConfig.getStrongholdsEnabled() && biomeConfig.getStrongholdsEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.STRONGHOLD);
		}

		// Ocean Monuments
		if(worldConfig.getOceanMonumentsEnabled() && biomeConfig.getOceanMonumentsEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
		}
		
		// Rare buildings
		// TODO: Allow spawning multiple types in a single biome?
		if(worldConfig.getRareBuildingsEnabled() && biomeConfig.getRareBuildingType() != RareBuildingType.disabled)
		{
			switch(biomeConfig.getRareBuildingType())
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
		if(worldConfig.getWoodlandMansionsEnabled() && biomeConfig.getWoodlandMansionsEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.WOODLAND_MANSION);
		}
		
		// Nether Fortresses
		if(worldConfig.getNetherFortressesEnabled() && biomeConfig.getNetherFortressesEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.NETHER_BRIDGE);
		}

		// Mineshafts
		if(worldConfig.getMineshaftsEnabled() && biomeConfig.getMineShaftType() != MineshaftType.disabled)
		{		
			float mineShaftProbability = biomeConfig.getMineShaftProbability();
			MineshaftType mineShaftType = biomeConfig.getMineShaftType();
			StructureFeature<MineshaftConfig, ? extends Structure<MineshaftConfig>> customMineShaft = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("mineshaft").toResourceLocationString(),
				Structure.MINESHAFT.configured(
					new MineshaftConfig(
						mineShaftProbability,
						mineShaftType == MineshaftType.mesa ? MineshaftStructure.Type.MESA : MineshaftStructure.Type.NORMAL
					)
				)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customMineShaft);
		}
		
		// Buried Treasure
		if(worldConfig.getBuriedTreasureEnabled() && biomeConfig.getBuriedTreasureEnabled())
		{
			float buriedTreasureProbability = biomeConfig.getBuriedTreasureProbability();
			StructureFeature<ProbabilityConfig, ? extends Structure<ProbabilityConfig>> customBuriedTreasure = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("buried_treasure").toResourceLocationString(),
				Structure.BURIED_TREASURE.configured(new ProbabilityConfig(buriedTreasureProbability))
			);
			biomeGenerationSettingsBuilder.addStructureStart(customBuriedTreasure);
		}
		
		// Ocean Ruins
		if(worldConfig.getOceanRuinsEnabled() && biomeConfig.getOceanRuinsType() != OceanRuinsType.disabled)
		{
			float oceanRuinsLargeProbability = biomeConfig.getOceanRuinsLargeProbability();
			float oceanRuinsClusterProbability = biomeConfig.getOceanRuinsClusterProbability();
			OceanRuinsType oceanRuinsType = biomeConfig.getOceanRuinsType();
			StructureFeature<OceanRuinConfig, ? extends Structure<OceanRuinConfig>> customOceanRuins = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("ocean_ruin").toResourceLocationString(),
				Structure.OCEAN_RUIN.configured(
					new OceanRuinConfig(
						oceanRuinsType == OceanRuinsType.cold ? OceanRuinStructure.Type.COLD : OceanRuinStructure.Type.WARM,
						oceanRuinsLargeProbability,
						oceanRuinsClusterProbability
					)
				)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customOceanRuins);
		}

		// Shipwrecks
		// TODO: Allowing both types in the same biome, make sure this won't cause problems.
		if(worldConfig.getShipWrecksEnabled())
		{
			if(biomeConfig.getShipWreckEnabled())
			{
				biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.SHIPWRECK);
			}
			if(biomeConfig.getShipWreckBeachedEnabled())
			{
				biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
			}			
		}
		
		// Pillager Outpost
		if(worldConfig.getPillagerOutpostsEnabled() && biomeConfig.getPillagerOutpostEnabled())
		{
			int outpostSize = biomeConfig.getPillagerOutPostSize();
			StructureFeature<VillageConfig, ? extends Structure<VillageConfig>> customOutpost = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("pillager_outpost").toResourceLocationString(), 
				Structure.PILLAGER_OUTPOST.configured(
					new VillageConfig(
						() -> {
							return PillagerOutpostPools.START;
						},
						outpostSize
					)
				)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customOutpost);			
		}
		
		// Bastion Remnants
		if(worldConfig.getBastionRemnantsEnabled() && biomeConfig.getBastionRemnantEnabled())
		{
			int bastionRemnantSize = biomeConfig.getBastionRemnantSize();
			StructureFeature<VillageConfig, ? extends Structure<VillageConfig>> customBastionRemnant = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("bastion_remnant").toResourceLocationString(), 
				Structure.BASTION_REMNANT.configured(
					new VillageConfig(
						() -> {
							return BastionRemnantsPieces.START;
						},
						bastionRemnantSize
					)
				)
			);
			biomeGenerationSettingsBuilder.addStructureStart(customBastionRemnant);
		}
		
		// Nether Fossils
		if(worldConfig.getNetherFossilsEnabled() && biomeConfig.getNetherFossilEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.NETHER_FOSSIL);
		}
		
		// End Cities
		if(worldConfig.getEndCitiesEnabled() && biomeConfig.getEndCityEnabled())
		{
			biomeGenerationSettingsBuilder.addStructureStart(StructureFeatures.END_CITY);
		}
		
		// Ruined Portals
		if(worldConfig.getRuinedPortalsEnabled() && biomeConfig.getRuinedPortalType() != RuinedPortalType.disabled)
		{
			switch(biomeConfig.getRuinedPortalType())
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
		}
	}

	// StructureFeatures.register()
	private static <FC extends IFeatureConfig, F extends Structure<FC>> StructureFeature<FC, F> register(String name, StructureFeature<FC, F> structure)
	{
		return WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, name, structure);
	}
	
	private static int getSkyColorForTemp(float p_244206_0_)
	{
		float lvt_1_1_ = p_244206_0_ / 3.0F;
		lvt_1_1_ = MathHelper.clamp(lvt_1_1_, -1.0F, 1.0F);
		return MathHelper.hsvToRgb(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}
}
