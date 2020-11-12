package com.pg85.otg.forge.biome;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.standard.PluginStandardValues;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.util.BiomeIds;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeGenerationSettings.Builder;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MoodSoundAmbience;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.DefaultSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;

public class ForgeBiome implements LocalBiome
{
	private final Biome biomeBase;
	private final BiomeConfig biomeConfig;
    
    public ForgeBiome(Biome biomeBase, BiomeConfig biomeConfig)
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
	public BiomeConfig getBiomeConfig()
	{
		return this.biomeConfig;
	}    
    
	@Override
	public boolean isCustom()
	{
		// TODO
		return false;
	}

	@Override
	public String getName()
	{
		// TODO
		return null;
	}

	@Override
	public BiomeIds getIds()
	{
		// TODO
		return null;
	}

	public static Biome createOTGBiome(BiomeConfig biomeConfig)
	{
		// TODO: These were taken from BiomeMaker Plains biome, there may still be resources missing.

		BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder = new BiomeGenerationSettings.Builder();

		// Mob spawning
		MobSpawnInfo.Builder mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);
		
		// Surface builder
		// TODO: Should we need this, surface builders in the common project should handle this?		
		addSurfaceBuilder(biomeGenerationSettingsBuilder, biomeConfig);

		// Default structures
		// TODO: Add missing structure types
		
		// Villages 
		addVillages(biomeGenerationSettingsBuilder, biomeConfig);	

		// Mineshafts
		addMineShafts(biomeGenerationSettingsBuilder, biomeConfig);

		// Ruined portals
		addRuinedPortals(biomeGenerationSettingsBuilder, biomeConfig);

		//
		
		// Carvers
		addCarvers(biomeGenerationSettingsBuilder, biomeConfig);

		// Underground structures
		// TODO: What are these?
		addUnderGroundStructures(biomeGenerationSettingsBuilder, biomeConfig);

	    float safeTemperature = biomeConfig.biomeTemperature;
	    if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
	    {
	        // Avoid temperatures between 0.1 and 0.2, Minecraft restriction
	        safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
	    }

	    BiomeAmbience.Builder biomeAmbienceBuilder =
			new BiomeAmbience.Builder()
				.func_235239_a_(biomeConfig.fogColor != 0x000000 ? biomeConfig.fogColor : 12638463)
				.func_235248_c_(biomeConfig.fogColor != 0x000000 ? biomeConfig.fogColor : 329011) // TODO: Add a setting for Water fog color.						
				.func_235246_b_(biomeConfig.waterColor != 0xffffff ? biomeConfig.waterColor : 4159204)
				.func_242539_d(biomeConfig.skyColor != 0x7BA5FF ? biomeConfig.skyColor : getSkyColorForTemp(safeTemperature)) // TODO: Sky color is normally based on temp, make a setting for that?
				// TODO: Implement these
				//particle
				//.func_235244_a_()
				//ambient_sound
				//.func_235241_a_() // Sound event?
				//mood_sound
				.func_235243_a_(MoodSoundAmbience.field_235027_b_) // TODO: Find out what this is, a sound?
				//additions_sound
				//.func_235242_a_()
				//music
				//.func_235240_a_()				
		;

	    if(biomeConfig.foliageColor != 0xffffff)
	    {
			biomeAmbienceBuilder.func_242540_e(biomeConfig.foliageColor);
	    }

	    if(biomeConfig.grassColor != 0xffffff)
	    {
	    	if(!biomeConfig.grassColorIsMultiplier)
	    	{
				biomeAmbienceBuilder.func_242541_f(biomeConfig.grassColor);
	    	} else {
	    		// TODO: grass color multiplier
	    		//int multipliedGrassColor = (defaultGrassColor + biomeConfig.grassColor) / 2;
				//biomeAmbienceBuilder.func_242537_a(biomeConfig.grassColor);
	    	}
	    }

		Biome.Builder biomeBuilder = 
			new Biome.Builder()
			.precipitation(
				biomeConfig.biomeWetness <= 0.0001 ? Biome.RainType.NONE : 
				biomeConfig.biomeTemperature > WorldStandardValues.SNOW_AND_ICE_TEMP ? Biome.RainType.RAIN : 
				Biome.RainType.SNOW
			)
			.category(Biome.Category.PLAINS) // TODO: Find out what category is used for.
			.depth(biomeConfig.biomeHeight)
			.scale(biomeConfig.biomeVolatility)
			.temperature(safeTemperature)
			.downfall(biomeConfig.biomeWetness)
			// Ambience (colours/sounds)
			.func_235097_a_(
				biomeAmbienceBuilder.func_235238_a_()
			// Mob spawning
			).func_242458_a(
				mobSpawnInfoBuilder.func_242577_b() // Validate & build
			// All other biome settings...
			).func_242457_a(
				biomeGenerationSettingsBuilder.func_242508_a() // Validate & build
			)
		;

		return
			biomeBuilder
				// Finalise
				.func_242455_a() // Validate & build
				.setRegistryName(new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString()))
		;
	}

	private static MobSpawnInfo.Builder createMobSpawnInfo(BiomeConfig biomeConfig)
	{
		MobSpawnInfo.Builder mobSpawnInfoBuilder = new MobSpawnInfo.Builder();
		//mobSpawnInfoBuilder.func_242575_a(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.SHEEP, 12, 4, 4));
		//mobSpawnInfoBuilder.func_242571_a();
		return mobSpawnInfoBuilder;
	}
	
	// TODO: Should we need this, surface builders in the common project should handle this?
	private static void addSurfaceBuilder(BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder, BiomeConfig biomeConfig)
	{
		// Each biomeconfig has its own surfacebuilder, so use biome registry key as name.
		// TODO: Create a surfacebuilder that looks like 1.12.2 (same ground layer depth etc).
		
		SurfaceBuilder<SurfaceBuilderConfig> surfaceBuilder = Registry.register(
			Registry.SURFACE_BUILDER,
			new ResourceLocation(PluginStandardValues.MOD_ID_SHORT, "surfacebuilder." + biomeConfig.getRegistryKey().getResourcePath()), 
			new DefaultSurfaceBuilder(SurfaceBuilderConfig.field_237203_a_)
		);
		
		// Taken from default grassy surface config, grass/gravel/dirt.
		ConfiguredSurfaceBuilder<SurfaceBuilderConfig> configuredSurfaceBuilder = WorldGenRegistries.func_243664_a(
			WorldGenRegistries.field_243651_c,
			new ResourceLocation(PluginStandardValues.MOD_ID_SHORT, "surfacebuilder." + biomeConfig.getRegistryKey().getResourcePath()),
			surfaceBuilder.func_242929_a(
				new SurfaceBuilderConfig(
					((ForgeMaterialData)biomeConfig.getDefaultSurfaceBlock()).internalBlock(),
					((ForgeMaterialData)biomeConfig.getDefaultGroundBlock()).internalBlock(),
					((ForgeMaterialData)biomeConfig.getDefaultSurfaceBlock()).internalBlock() // TODO: Add UnderwaterSurfaceBlock to BiomeConfig
				)
			)
		);
		biomeGenerationSettingsBuilder.func_242517_a(configuredSurfaceBuilder);
	}
	
	private static void addCarvers(Builder biomeGenerationSettingsBuilder, BiomeConfig biomeConfig2)
	{
		//DefaultBiomeFeatures.func_243738_d(biomegenerationsettings$builder);
	}

	private static void addUnderGroundStructures(Builder biomeGenerationSettingsBuilder, BiomeConfig biomeConfig2)
	{
		//DefaultBiomeFeatures.func_243746_h(biomegenerationsettings$builder);
	}
	
	private static void addRuinedPortals(Builder biomeGenerationSettingsBuilder, BiomeConfig biomeConfig2)
	{
		//biomegenerationsettings$builder.func_242516_a(StructureFeatures.field_244159_y);
	}

	private static void addMineShafts(Builder biomeGenerationSettingsBuilder, BiomeConfig biomeConfig2)
	{
		//DefaultBiomeFeatures.func_243733_b(biomegenerationsettings$builder);
	}

	private static void addVillages(Builder biomeGenerationSettingsBuilder, BiomeConfig biomeConfig2)
	{
		//biomegenerationsettings$builder.func_242516_a(StructureFeatures.field_244154_t).func_242516_a(StructureFeatures.field_244135_a);
	}
	
	private static int getSkyColorForTemp(float p_244206_0_)
	{
		float lvt_1_1_ = p_244206_0_ / 3.0F;
		lvt_1_1_ = MathHelper.clamp(lvt_1_1_, -1.0F, 1.0F);
		return MathHelper.hsvToRGB(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}
}
