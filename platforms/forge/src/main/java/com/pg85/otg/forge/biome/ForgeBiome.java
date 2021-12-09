package com.pg85.otg.forge.biome;

import java.util.List;
import java.util.Optional;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.biome.BiomeConfig;
import com.pg85.otg.gen.resource.RegistryResource;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import com.pg85.otg.util.minecraft.EntityCategory;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.world.level.biome.Biome.TemperatureModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

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
		MobSpawnSettings.Builder mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);

		// Surface/ground/stone blocks / sagc are done during base terrain gen.
		// Spawn point detection checks for surfacebuilder blocks, so using ConfiguredSurfaceBuilders.GRASS.
		// TODO: What if there's no grass around spawn?
		//biomeGenerationSettingsBuilder.surfaceBuilder(SurfaceBuilders.GRASS);

		// Register default carvers, we won't actually use these since we have
		// our own carvers, but if they're replaced we'll know there are modded carvers.
		//BiomeDefaultFeatures.addDefaultCarvers(biomeGenerationSettingsBuilder);

		// Register any Registry() resources to the biome, to be handled by MC.
		for (ConfigFunction<IBiomeConfig> res : ((BiomeConfig)biomeConfig).getResourceQueue())
		{
			if (res instanceof RegistryResource)
			{
				RegistryResource registryResource = (RegistryResource)res;
				Decoration stage = GenerationStep.Decoration.valueOf(registryResource.getDecorationStage());
				// This changed from CONFIGURED_FEATURES to PLACED_FEATURE, also registry names changed, so presets will need to be updated.
				PlacedFeature registry = BuiltinRegistries.PLACED_FEATURE.get(new ResourceLocation(registryResource.getFeatureKey()));
				if(registry != null)
				{
					biomeGenerationSettingsBuilder.addFeature(stage, registry);
				} else {
					if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.DECORATION))
					{
						OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.DECORATION, "Registry() " + registryResource.getFeatureKey() + " could not be found for biomeconfig " + biomeConfig.getName());
					}
				}
			}
		}

		float safeTemperature = biomeConfig.getBiomeTemperature();
		if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
		{
			// Avoid temperatures between 0.1 and 0.2, Minecraft restriction
			safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
		}

		BiomeSpecialEffects.Builder biomeAmbienceBuilder =
			new BiomeSpecialEffects.Builder()			
				.fogColor(biomeConfig.getFogColor() != BiomeStandardValues.FOG_COLOR.getDefaultValue() ? biomeConfig.getFogColor() : worldConfig.getFogColor())
				.waterFogColor(biomeConfig.getWaterFogColor() != BiomeStandardValues.WATER_FOG_COLOR.getDefaultValue() ? biomeConfig.getWaterFogColor() : 329011)
				.waterColor(biomeConfig.getWaterColor() != BiomeStandardValues.WATER_COLOR.getDefaultValue() ? biomeConfig.getWaterColor() : 4159204)
				.skyColor(biomeConfig.getSkyColor() != BiomeStandardValues.SKY_COLOR.getDefaultValue() ? biomeConfig.getSkyColor() : getSkyColorForTemp(safeTemperature)) // TODO: Sky color is normally based on temp, make a setting for that?
		;

		@SuppressWarnings("deprecation")
		Optional<ParticleType<?>> particleType = Registry.PARTICLE_TYPE.getOptional(new ResourceLocation(biomeConfig.getParticleType()));
		if(particleType.isPresent() && particleType.get() instanceof ParticleOptions)
		{
			biomeAmbienceBuilder.ambientParticle(new AmbientParticleSettings((ParticleOptions)particleType.get(), biomeConfig.getParticleProbability()));	
		}

		SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(biomeConfig.getMusic()));
		if (event != null)
		{
			biomeAmbienceBuilder.backgroundMusic(new Music(event,
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
			biomeAmbienceBuilder.ambientMoodSound(new AmbientMoodSettings(event,
				biomeConfig.getMoodSoundDelay(),
				biomeConfig.getMoodSearchRange(),
				biomeConfig.getMoodOffset()));
		}

		event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(biomeConfig.getAdditionsSound()));
		if (event != null)
		{
			biomeAmbienceBuilder.ambientAdditionsSound(new AmbientAdditionsSettings(event, biomeConfig.getAdditionsTickChance()));
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
				biomeAmbienceBuilder.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP);
				break;
			case DarkForest:
				biomeAmbienceBuilder.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST);
				break;
			default:
				break;
		}
		
		ResourceLocation registryName = new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString());
		Biome.BiomeCategory category = Biome.BiomeCategory.byName(biomeConfig.getBiomeCategory());
		if (category == null)
		{
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Could not parse biome category " + biomeConfig.getBiomeCategory());
			}
			category = isOceanBiome ? Biome.BiomeCategory.OCEAN : Biome.BiomeCategory.NONE;
		}
		Biome.Precipitation rainType = 
			biomeConfig.getBiomeWetness() <= 0.0001 ? Biome.Precipitation.NONE : 
			biomeConfig.getBiomeTemperature() > Constants.SNOW_AND_ICE_TEMP ? Biome.Precipitation.RAIN : 
			Biome.Precipitation.SNOW
		;

		// Fire Forge BiomeLoadingEvent to allow other mods to enrich otg biomes with decoration features, structure features and mob spawns.
		BiomeGenerationSettingsBuilder genBuilder = new BiomeGenerationSettingsBuilder(biomeGenerationSettingsBuilder.build());
		MobSpawnSettingsBuilder spawnBuilder = new MobSpawnSettingsBuilder(mobSpawnInfoBuilder.build());
		BiomeLoadingEvent event1 = new BiomeLoadingEvent(registryName, new Biome.ClimateSettings(rainType, safeTemperature, TemperatureModifier.NONE, biomeConfig.getBiomeWetness()), category, biomeAmbienceBuilder.build(), genBuilder, spawnBuilder);
		MinecraftForge.EVENT_BUS.post(event1);
		BiomeSpecialEffects biomeAmbienceBuilder2 = event1.getEffects();
		BiomeGenerationSettingsBuilder biomeGenerationSettingsBuilder2 = event1.getGeneration();
		MobSpawnSettingsBuilder mobSpawnInfoBuilder2 = event1.getSpawns();
		//

		Biome.BiomeBuilder biomeBuilder = 
			new Biome.BiomeBuilder()
			.precipitation(rainType)
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

		biomeBuilder.biomeCategory(category != null ? category : isOceanBiome ? Biome.BiomeCategory.OCEAN : Biome.BiomeCategory.PLAINS);
		
		return biomeBuilder.build().setRegistryName(registryName);
	}

	private static MobSpawnSettings.Builder createMobSpawnInfo(IBiomeConfig biomeConfig)
	{
		MobSpawnSettings.Builder mobSpawnInfoBuilder = new MobSpawnSettings.Builder();
		addMobGroup(MobCategory.MONSTER, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.MONSTER), biomeConfig.getName());
		addMobGroup(MobCategory.CREATURE, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.CREATURE), biomeConfig.getName());
		addMobGroup(MobCategory.AMBIENT, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.AMBIENT), biomeConfig.getName());
		addMobGroup(MobCategory.UNDERGROUND_WATER_CREATURE, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.UNDERGROUND_WATER_CREATURE), biomeConfig.getName());
		addMobGroup(MobCategory.WATER_CREATURE, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.WATER_CREATURE), biomeConfig.getName());
		addMobGroup(MobCategory.WATER_AMBIENT, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.WATER_AMBIENT), biomeConfig.getName());
		addMobGroup(MobCategory.MISC, mobSpawnInfoBuilder, biomeConfig.getSpawnList(EntityCategory.MISC), biomeConfig.getName());
		//mobSpawnInfoBuilder.setPlayerCanSpawn();
		return mobSpawnInfoBuilder;
	}

	private static void addMobGroup(MobCategory entitiClassification, MobSpawnSettings.Builder mobSpawnInfoBuilder, List<WeightedMobSpawnGroup> mobSpawnGroupList, String biomeName)
	{
		for(WeightedMobSpawnGroup mobSpawnGroup : mobSpawnGroupList)
		{
			Optional<EntityType<?>> entityType = EntityType.byString(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.addSpawn(entitiClassification, new MobSpawnSettings.SpawnerData(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MOBS))
				{
					OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MOBS, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeName);
				}
			}
		}
	}
	
	private static int getSkyColorForTemp(float p_244206_0_)
	{
		float lvt_1_1_ = p_244206_0_ / 3.0F;
		lvt_1_1_ = Mth.clamp(lvt_1_1_, -1.0F, 1.0F);
		return Mth.hsvToRgb(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}
}
