package com.pg85.otg.forge.biome;

import java.util.Optional;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.OceanRuinsType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.RuinedPortalType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.interfaces.IBiome;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IWorldConfig;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeGenerationSettings.Builder;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MoodSoundAmbience;
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

	public static Biome createOTGBiome(boolean isOceanBiome, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder = new BiomeGenerationSettings.Builder();

		// Mob spawning
		MobSpawnInfo.Builder mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);
		
		// NOOP surface builder, surface/ground/stone blocks / sagc are done during base terrain gen.
		biomeGenerationSettingsBuilder.withSurfaceBuilder(ConfiguredSurfaceBuilders.field_244184_p);

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
				.setFogColor(biomeConfig.getFogColor() != 0x000000 ? biomeConfig.getFogColor() : 12638463)
				.setWaterFogColor(biomeConfig.getFogColor() != 0x000000 ? biomeConfig.getFogColor() : 329011) // TODO: Add a setting for Water fog color.
				.setWaterColor(biomeConfig.getWaterColor() != 0xffffff ? biomeConfig.getWaterColor() : 4159204)
				.withSkyColor(biomeConfig.getSkyColor() != 0x7BA5FF ? biomeConfig.getSkyColor() : getSkyColorForTemp(safeTemperature)) // TODO: Sky color is normally based on temp, make a setting for that?
				// TODO: Implement these
				//particle
				//.func_235244_a_()
				//ambient_sound
				//.func_235241_a_() // Sound event?
				//mood_sound
				.setMoodSound(MoodSoundAmbience.DEFAULT_CAVE) // TODO: Find out what this is, a sound?
				//additions_sound
				//.func_235242_a_()
				//music
				//.func_235240_a_()				
		;

	    if(biomeConfig.getFoliageColor() != 0xffffff)
	    {
			biomeAmbienceBuilder.withFoliageColor(biomeConfig.getFoliageColor());
	    }

	    if(biomeConfig.getGrassColor() != 0xffffff)
	    {
	    	if(!biomeConfig.getGrassColorIsMultiplier())
	    	{
				biomeAmbienceBuilder.withGrassColor(biomeConfig.getGrassColor());
	    	} else {
	    		// TODO: grass color multiplier
	    		//int multipliedGrassColor = (defaultGrassColor + biomeConfig.grassColor) / 2;
				//biomeAmbienceBuilder.func_242537_a(biomeConfig.grassColor);
	    	}
	    }

		Biome.Builder biomeBuilder = 
			new Biome.Builder()
			.precipitation(
				biomeConfig.getBiomeWetness() <= 0.0001 ? Biome.RainType.NONE : 
				biomeConfig.getBiomeTemperature() > Constants.SNOW_AND_ICE_TEMP ? Biome.RainType.RAIN : 
				Biome.RainType.SNOW
			)
			.category(isOceanBiome ? Biome.Category.OCEAN : Biome.Category.PLAINS) // TODO: Find out what category is used for.
			.depth(biomeConfig.getBiomeHeight())
			.scale(biomeConfig.getBiomeVolatility())
			.temperature(safeTemperature)
			.downfall(biomeConfig.getBiomeWetness())
			// Ambience (colours/sounds)
			.setEffects(
				biomeAmbienceBuilder.build()
			// Mob spawning
			).withMobSpawnSettings(
				mobSpawnInfoBuilder.copy() // Validate & build
			// All other biome settings...
			).withGenerationSettings(
				biomeGenerationSettingsBuilder.build() // Validate & build
			)
		;

		return
			biomeBuilder
				// Finalise
				.build() // Validate & build
				.setRegistryName(new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString()))
		;
	}

	private static MobSpawnInfo.Builder createMobSpawnInfo(IBiomeConfig biomeConfig)
	{
		MobSpawnInfo.Builder mobSpawnInfoBuilder = new MobSpawnInfo.Builder();
		for(WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.getMonsters())
		{
			Optional<EntityType<?>> entityType = EntityType.byKey(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		for(WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.getCreatures())
		{
			Optional<EntityType<?>> entityType = EntityType.byKey(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		for(WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.getWaterCreatures())
		{
			Optional<EntityType<?>> entityType = EntityType.byKey(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.withSpawner(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}		
		for(WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.getAmbientCreatures())
		{
			Optional<EntityType<?>> entityType = EntityType.byKey(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.withSpawner(EntityClassification.AMBIENT, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		
		// TODO: EntityClassification.WATER_AMBIENT / EntityClassification.MISC ?
		
		mobSpawnInfoBuilder.isValidSpawnBiomeForPlayer(); // Default biomes do this, not sure if needed. Does the opposite of disablePlayerSpawn?
		return mobSpawnInfoBuilder;
	}

	private static void addVanillaStructures(Builder biomeGenerationSettingsBuilder, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
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
		if(worldConfig.getVillagesEnabled() && biomeConfig.getVillageType() != VillageType.disabled)
		{
			int villageSize = biomeConfig.getVillageSize();
			VillageType villageType = biomeConfig.getVillageType();
			StructureFeature<VillageConfig, ? extends Structure<VillageConfig>> customVillage = register(
				biomeConfig.getRegistryKey().withBiomeResource("village").toResourceLocationString(), 
				Structure.VILLAGE.withConfiguration(
					new VillageConfig(
						() -> {
							switch(villageType)
							{ 
								case sandstone:
									return DesertVillagePools.field_243774_a;
								case savanna:
									return SavannaVillagePools.field_244128_a;
								case taiga:
									return TaigaVillagePools.field_244193_a;
								case wood:
									return PlainsVillagePools.field_244090_a;
								case snowy:
									return SnowyVillagePools.field_244129_a;
								case disabled: // Should never happen
									break;
							}
							return PlainsVillagePools.field_244090_a;
						},
						villageSize
					)
				)
			);
			biomeGenerationSettingsBuilder.withStructure(customVillage);
		}
		
		// Strongholds
		if(worldConfig.getStrongholdsEnabled() && biomeConfig.getStrongholdsEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.STRONGHOLD);
		}

		// Ocean Monuments
		if(worldConfig.getOceanMonumentsEnabled() && biomeConfig.getOceanMonumentsEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.MONUMENT);
		}
		
		// Rare buildings
		// TODO: Allow spawning multiple types in a single biome?
		if(worldConfig.getRareBuildingsEnabled() && biomeConfig.getRareBuildingType() != RareBuildingType.disabled)
		{
			switch(biomeConfig.getRareBuildingType())
			{
				case desertPyramid:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.DESERT_PYRAMID);
					break;
				case igloo:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.IGLOO);
					break;
				case jungleTemple:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.JUNGLE_PYRAMID);				
					break;
				case swampHut:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.SWAMP_HUT);
					break;
				case disabled:
					break;					
			}
		}
		
		// Woodland Mansions
		if(worldConfig.getWoodlandMansionsEnabled() && biomeConfig.getWoodlandMansionsEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.MANSION);
		}
		
		// Nether Fortresses
		if(worldConfig.getNetherFortressesEnabled() && biomeConfig.getNetherFortressesEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.FORTRESS);
		}

		// Mineshafts
		if(worldConfig.getMineshaftsEnabled() && biomeConfig.getMineShaftType() != MineshaftType.disabled)
		{		
			float mineShaftProbability = biomeConfig.getMineShaftProbability();
			MineshaftType mineShaftType = biomeConfig.getMineShaftType();
			StructureFeature<MineshaftConfig, ? extends Structure<MineshaftConfig>> customMineShaft = register(
				biomeConfig.getRegistryKey().withBiomeResource("mineshaft").toResourceLocationString(),
				Structure.MINESHAFT.withConfiguration(
					new MineshaftConfig(
						mineShaftProbability,
						mineShaftType == MineshaftType.mesa ? MineshaftStructure.Type.MESA : MineshaftStructure.Type.NORMAL
					)
				)
			);
			biomeGenerationSettingsBuilder.withStructure(customMineShaft);
		}
		
		// Buried Treasure
		if(worldConfig.getBuriedTreasureEnabled() && biomeConfig.getBuriedTreasureEnabled())
		{
			float buriedTreasureProbability = biomeConfig.getBuriedTreasureProbability();
			StructureFeature<ProbabilityConfig, ? extends Structure<ProbabilityConfig>> customBuriedTreasure = register(
				biomeConfig.getRegistryKey().withBiomeResource("buried_treasure").toResourceLocationString(),
				Structure.BURIED_TREASURE.withConfiguration(new ProbabilityConfig(buriedTreasureProbability))
			);
			biomeGenerationSettingsBuilder.withStructure(customBuriedTreasure);
		}
		
		// Ocean Ruins
		if(worldConfig.getOceanRuinsEnabled() && biomeConfig.getOceanRuinsType() != OceanRuinsType.disabled)
		{
			float oceanRuinsLargeProbability = biomeConfig.getOceanRuinsLargeProbability();
			float oceanRuinsClusterProbability = biomeConfig.getOceanRuinsClusterProbability();
			OceanRuinsType oceanRuinsType = biomeConfig.getOceanRuinsType();
			StructureFeature<OceanRuinConfig, ? extends Structure<OceanRuinConfig>> customOceanRuins = register(
				biomeConfig.getRegistryKey().withBiomeResource("ocean_ruin").toResourceLocationString(),
				Structure.OCEAN_RUIN.withConfiguration(
					new OceanRuinConfig(
						oceanRuinsType == OceanRuinsType.cold ? OceanRuinStructure.Type.COLD : OceanRuinStructure.Type.WARM,
						oceanRuinsLargeProbability,
						oceanRuinsClusterProbability
					)
				)
			);
			biomeGenerationSettingsBuilder.withStructure(customOceanRuins);
		}

		// Shipwrecks
		// TODO: Allowing both types in the same biome, make sure this won't cause problems.
		if(worldConfig.getShipWrecksEnabled())
		{
			if(biomeConfig.getShipWreckEnabled())
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.SHIPWRECK);
			}
			if(biomeConfig.getShipWreckBeachedEnabled())
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.SHIPWRECK_BEACHED);
			}			
		}
		
		// Pillager Outpost
		if(worldConfig.getPillagerOutpostsEnabled() && biomeConfig.getPillagerOutpostEnabled())
		{
			int outpostSize = biomeConfig.getPillagerOutPostSize();
			StructureFeature<VillageConfig, ? extends Structure<VillageConfig>> customOutpost = register(
				biomeConfig.getRegistryKey().withBiomeResource("pillager_outpost").toResourceLocationString(), 
				Structure.PILLAGER_OUTPOST.withConfiguration(
					new VillageConfig(
						() -> {
							return PillagerOutpostPools.field_244088_a;
						},
						outpostSize
					)
				)
			);
			biomeGenerationSettingsBuilder.withStructure(customOutpost);			
		}
		
		// Bastion Remnants
		if(worldConfig.getBastionRemnantsEnabled() && biomeConfig.getBastionRemnantEnabled())
		{
			int bastionRemnantSize = biomeConfig.getBastionRemnantSize();
			StructureFeature<VillageConfig, ? extends Structure<VillageConfig>> customBastionRemnant = register(
				biomeConfig.getRegistryKey().withBiomeResource("bastion_remnant").toResourceLocationString(), 
				Structure.BASTION_REMNANT.withConfiguration(
					new VillageConfig(
						() -> {
							return BastionRemnantsPieces.field_243686_a;
						},
						bastionRemnantSize
					)
				)
			);
			biomeGenerationSettingsBuilder.withStructure(customBastionRemnant);
		}
		
		// Nether Fossils
		if(worldConfig.getNetherFossilsEnabled() && biomeConfig.getNetherFossilEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.NETHER_FOSSIL);
		}
		
		// End Cities
		if(worldConfig.getEndCitiesEnabled() && biomeConfig.getEndCityEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.END_CITY);
		}
		
		// Ruined Portals
		if(worldConfig.getRuinedPortalsEnabled() && biomeConfig.getRuinedPortalType() != RuinedPortalType.disabled)
		{
			switch(biomeConfig.getRuinedPortalType())
			{
				case normal:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL);
					break;
				case desert:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_DESERT);
					break;
				case jungle:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_JUNGLE);
					break;
				case swamp:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_SWAMP);
					break;
				case mountain:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
					break;
				case ocean:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_OCEAN);
					break;
				case nether:
					biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_NETHER);
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
		return MathHelper.hsvToRGB(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}
}
