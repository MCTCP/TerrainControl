package com.pg85.otg.forge.presets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pg85.otg.common.presets.LocalPresetLoader;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.preset.Preset;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.gen.BiomeGenData;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MoodSoundAmbience;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.DefaultSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgePresetLoader extends LocalPresetLoader
{
	private Map<ResourceLocation, BiomeConfig> biomeConfigsByRegistryKey = new HashMap<ResourceLocation, BiomeConfig>();
	// TODO: Store per preset
	private ArrayList<RegistryKey<Biome>> biomes = new ArrayList<RegistryKey<Biome>>();
	
	public ForgePresetLoader(Path otgRootFolder)
	{
		super(otgRootFolder);
	}

	@Override
	public BiomeConfig getBiomeConfig(String resourceLocationString)
	{
		return this.biomeConfigsByRegistryKey.get(new ResourceLocation(resourceLocationString));
	}
	
	// TODO: Hardcoded to use 1 preset atm (all biomes), make this work per preset
	public List<RegistryKey<Biome>> getBiomeRegistryKeys(String presetName)
	{
		return this.biomes;
	}
	
	@Override
	public void registerBiomes()
	{
		for(Preset preset : this.presets.values())
		{
			for(BiomeConfig biomeConfig : preset.getAllBiomeConfigs())
			{
				// DeferredRegister for Biomes doesn't appear to be working atm, biomes are never registered :(
				//RegistryObject<Biome> registryObject = OTGPlugin.BIOMES.register(biomeConfig.getRegistryKey().getResourcePath(), () -> createOTGBiome(biomeConfig));
				
				Biome biome = createOTGBiome(biomeConfig);
 				ForgeRegistries.BIOMES.register(biome);
 				
 				// Store registry key (resourcelocation) so we can look up biomeconfigs via RegistryKey<Biome> later.
 				ResourceLocation resourceLocation = new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString());
 				this.biomeConfigsByRegistryKey.put(resourceLocation, biomeConfig);
 				this.biomes.add(RegistryKey.func_240903_a_(Registry.field_239720_u_, resourceLocation));

 				// TODO: this is hardcoded for now until layer generation is fixed.
				BiomeGenData data = new BiomeGenData();
				data.biomeHeight = biomeConfig.biomeHeight;
				data.biomeVolatility = biomeConfig.biomeVolatility;
				data.horizontalFracture = biomeConfig.worldConfig.getFractureHorizontal();
				data.verticalFracture = biomeConfig.worldConfig.getFractureVertical();
				data.volatility1 = biomeConfig.volatility1;
				data.volatility2 = biomeConfig.volatility2;
				data.smoothRadius = biomeConfig.smoothRadius;
				data.color = biomeConfig.biomeColor;

 				if(biomeConfig.getRegistryKey().toResourceLocationString().equals("openterraingenerator:default.ocean"))
 				{
 					BiomeGenData.LOOKUP[0] = data;
 				}
 				if(biomeConfig.getRegistryKey().toResourceLocationString().equals("openterraingenerator:default.plains"))
 				{
 					BiomeGenData.LOOKUP[1] = data;
 				}
 				if(biomeConfig.getRegistryKey().toResourceLocationString().equals("openterraingenerator:default.forest"))
 				{
 					BiomeGenData.LOOKUP[2] = data;
 				}
 				if(biomeConfig.getRegistryKey().toResourceLocationString().equals("openterraingenerator:default.desert"))
 				{
 					BiomeGenData.LOOKUP[3] = data;
 				}
 				//
			}
		}
	}
	
	private static Biome createOTGBiome(BiomeConfig biomeConfig)
	{		
		BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder();
		
		// Mob spawning
		MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();	
		//mobspawninfo$builder.func_242575_a(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.SHEEP, 12, 4, 4));	      
		//mobspawninfo$builder.func_242571_a();
		
		// Each biomeconfig has its own surfacebuilder, so use biome registry key as name.
		
		// Surface builder
		// TODO: Register surfacebuilder properly, create a surfacebuilder that
		// looks like 1.12.2 (same ground layer depth etc).
		SurfaceBuilder<SurfaceBuilderConfig> surfaceBuilder = Registry.register(
			Registry.SURFACE_BUILDER, 
			"surfacebuilder." + biomeConfig.getRegistryKey().getResourcePath(), 
			new DefaultSurfaceBuilder(SurfaceBuilderConfig.field_237203_a_)
		);
		
		// Taken from default grassy surface config, grass/gravel/dirt.
		ConfiguredSurfaceBuilder<SurfaceBuilderConfig> configuredSurfaceBuilder = WorldGenRegistries.func_243663_a(
			WorldGenRegistries.field_243651_c,
			"surfacebuilder." + biomeConfig.getRegistryKey().getResourcePath(),
			surfaceBuilder.func_242929_a(
				new SurfaceBuilderConfig(
					((ForgeMaterialData)biomeConfig.getDefaultSurfaceBlock()).internalBlock(),
					((ForgeMaterialData)biomeConfig.getDefaultGroundBlock()).internalBlock(),
					((ForgeMaterialData)biomeConfig.getDefaultSurfaceBlock()).internalBlock() // TODO: Add UnderwaterSurfaceBlock to BiomeConfig
				)
			)
		);
		biomegenerationsettings$builder.func_242517_a(configuredSurfaceBuilder);

		// Villages 
		//biomegenerationsettings$builder.func_242516_a(StructureFeatures.field_244154_t).func_242516_a(StructureFeatures.field_244135_a);		
		
		// Mineshaft
		//DefaultBiomeFeatures.func_243733_b(biomegenerationsettings$builder);
		
		// Ruined portal
		//biomegenerationsettings$builder.func_242516_a(StructureFeatures.field_244159_y);
		
		// Carvers
		//DefaultBiomeFeatures.func_243738_d(biomegenerationsettings$builder);
		
		// Lakes
		//DefaultBiomeFeatures.func_243742_f(biomegenerationsettings$builder);
		
		// Underground structures
		//DefaultBiomeFeatures.func_243746_h(biomegenerationsettings$builder);
		
		// Vegetation 
		//DefaultBiomeFeatures.func_243711_Y(biomegenerationsettings$builder);
		
		// Underground ores
		//DefaultBiomeFeatures.func_243748_i(biomegenerationsettings$builder);
		
		// Top-layer modification
		//DefaultBiomeFeatures.func_243730_an(biomegenerationsettings$builder);
		
        float safeTemperature = biomeConfig.biomeTemperature;
        if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
        {
            // Avoid temperatures between 0.1 and 0.2, Minecraft restriction
            safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
        }
        
		return
			(new Biome.Builder())
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
				// Ambience: foliage/grass/water/sky/fog colors, particles, sounds, music.
				// TODO: Add particles and music.				
				.func_235097_a_( 
					(new BiomeAmbience.Builder())
						.func_235246_b_(biomeConfig.waterColor)
						.func_235248_c_(biomeConfig.fogColor) // TODO: Add a setting for Water fog color.
						.func_235239_a_(biomeConfig.fogColor)
						.func_242539_d(biomeConfig.skyColor) //getSkyColorForTemp(safeTemperature)) // TODO: Sky color is normally based on temp, make a setting for that?
						.func_235243_a_(MoodSoundAmbience.field_235027_b_) // TODO: Find out what this is, a sound?
						.func_235238_a_() // Validate & build
				// Mob spawning
				).func_242458_a(
					mobspawninfo$builder.func_242577_b()
				// All other biome settings...
				).func_242457_a(
					biomegenerationsettings$builder.func_242508_a()
				// Validate & build
				).func_242455_a()
				.setRegistryName(new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString()))
		;
	}

	private static int getSkyColorForTemp(float p_244206_0_)
	{
		float lvt_1_1_ = p_244206_0_ / 3.0F;
		lvt_1_1_ = MathHelper.clamp(lvt_1_1_, -1.0F, 1.0F);
		return MathHelper.hsvToRGB(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}
}
