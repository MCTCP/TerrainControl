package com.pg85.otg.forge.biome;

import java.util.Optional;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeGenerationSettings.Builder;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MoodSoundAmbience;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
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

	public static Biome createOTGBiome(IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder = new BiomeGenerationSettings.Builder();

		// Mob spawning
		MobSpawnInfo.Builder mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);
		
		// NOOP surface builder, surface/ground/stone blocks / sagc are done during base terrain gen.
		biomeGenerationSettingsBuilder.withSurfaceBuilder(ConfiguredSurfaceBuilders.field_244184_p);

		// * Carvers are handled by OTG
		
		// Default structures
		addDefaultStructures(biomeGenerationSettingsBuilder, worldConfig, biomeConfig);	

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
			.category(Biome.Category.PLAINS) // TODO: Find out what category is used for.
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
	
	private static void addDefaultStructures(Builder biomeGenerationSettingsBuilder, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		// TODO: Village size, distance.
		if(worldConfig.getVillagesEnabled())
		{
			if(biomeConfig.getVillageType() == VillageType.sandstone)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.VILLAGE_DESERT);
			}
			else if(biomeConfig.getVillageType() == VillageType.savanna)
	        {
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.VILLAGE_SAVANNA);
	        }
			else if(biomeConfig.getVillageType() == VillageType.taiga)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.VILLAGE_TAIGA);
			}
			else if(biomeConfig.getVillageType() == VillageType.wood)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.VILLAGE_PLAINS);
			}
			else if(biomeConfig.getVillageType() == VillageType.snowy)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.VILLAGE_SNOWY);
			}
		}
		
		// TODO: Stronghold count, distance, spread.
		if(worldConfig.getStrongholdsEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.STRONGHOLD);
		}
				
		// TODO: Ocean monument gridsize, offset.
		if(worldConfig.getOceanMonumentsEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.MONUMENT);
		}
		
		// TODO: Min/max distance for rare buildings.
		if(worldConfig.getRareBuildingsEnabled())
		{
			if(biomeConfig.getRareBuildingType() == RareBuildingType.desertPyramid)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.DESERT_PYRAMID);
			}		
			if(biomeConfig.getRareBuildingType() == RareBuildingType.igloo)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.IGLOO);
			}		
			if(biomeConfig.getRareBuildingType() == RareBuildingType.jungleTemple)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.JUNGLE_PYRAMID);
			}
			if(biomeConfig.getRareBuildingType() == RareBuildingType.swampHut)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.SWAMP_HUT);
			}
		}
		
		if(biomeConfig.getWoodlandMansionsEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.MANSION);
		}
		
		if(biomeConfig.getNetherFortressesEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.FORTRESS);
		}

		// TODO: Mineshaft rarity.
		if(worldConfig.getMineshaftsEnabled())
		{
			if(biomeConfig.getMineShaftType() == MineshaftType.normal)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.MINESHAFT);
			}
			else if(biomeConfig.getMineShaftType() == MineshaftType.mesa)
			{
				biomeGenerationSettingsBuilder.withStructure(StructureFeatures.MINESHAFT_BADLANDS);
			}
		}
		
		// Buried Treasure
		if(biomeConfig.getBuriedTreasureEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.BURIED_TREASURE); // buried_treasure
		}
		
		// Ocean Ruins
		if(biomeConfig.getOceanRuinsColdEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.OCEAN_RUIN_COLD); // ocean_ruin_cold
		}		
		if(biomeConfig.getOceanRuinsWarmEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.OCEAN_RUIN_WARM); // ocean_ruin_warm
		}		
				
		// Shipwreck
		if(biomeConfig.getShipWreckEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.SHIPWRECK); // shipwreck
		}
		if(biomeConfig.getShipWreckBeachedEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.SHIPWRECK_BEACHED); // shipwreck_beached
		}
		
		// Pillager Outpost
		if(biomeConfig.getPillagerOutpostEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.PILLAGER_OUTPOST);
		}
		
		// Bastion Remnant
		if(biomeConfig.getBastionRemnantEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.BASTION_REMNANT);
		}
		
		// Nether Fossil
		if(biomeConfig.getNetherFossilEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.NETHER_FOSSIL);
		}
		
		// End City
		if(biomeConfig.getEndCityEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.END_CITY);
		}
		
		// Ruined Portal
		if(biomeConfig.getRuinedPortalEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL); // ruined_portal
		}
		if(biomeConfig.getRuinedPortalDesertEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_DESERT); // ruined_portal_desert
		}
		if(biomeConfig.getRuinedPortalJungleEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_JUNGLE); // ruined_portal_jungle
		}
		if(biomeConfig.getRuinedPortalSwampEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_SWAMP); // ruined_portal_swamp
		}
		if(biomeConfig.getRuinedPortalMountainEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_MOUNTAIN); // ruined_portal_mountain
		}
		if(biomeConfig.getRuinedPortalOceanEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_OCEAN); // ruined_portal_ocean
		}
		if(biomeConfig.getRuinedPortalNetherEnabled())
		{
			biomeGenerationSettingsBuilder.withStructure(StructureFeatures.RUINED_PORTAL_NETHER); // ruined_portal_nether
		}
		
		// TODO: Fossil
		// TODO: Amethyst Geode (1.17?)
		
		// Misc structures: These structures generate even when the "Generate structures" world option is disabled, and also cannot be located with the /locate command.
		// TODO: Dungeon
		// TODO: Desert Well
	}
	
	private static int getSkyColorForTemp(float p_244206_0_)
	{
		float lvt_1_1_ = p_244206_0_ / 3.0F;
		lvt_1_1_ = MathHelper.clamp(lvt_1_1_, -1.0F, 1.0F);
		return MathHelper.hsvToRGB(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}
}
