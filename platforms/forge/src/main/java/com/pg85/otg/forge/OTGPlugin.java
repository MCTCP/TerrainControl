package com.pg85.otg.forge;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.NoiseSettings;
import net.minecraft.world.gen.settings.ScalingSettings;
import net.minecraft.world.gen.settings.SlideSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.standard.PluginStandardValues;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.commands.OTGCommand;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.gui.screens.CreateOTGWorldScreen;

@Mod(PluginStandardValues.MOD_ID_SHORT) // Should match META-INF/mods.toml
@Mod.EventBusSubscriber(modid = PluginStandardValues.MOD_ID_SHORT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OTGPlugin
{
	// Generate registry key for OTG DimensionSettings
	private static final RegistryKey<DimensionSettings> OTG_DIMENSION_SETTINGS = RegistryKey.func_240903_a_(
		Registry.field_243549_ar, 
		new ResourceLocation(PluginStandardValues.MOD_ID_SHORT, "otg_dimension")
	);
	
	// Define a new world type for the world creation screen
	private static final BiomeGeneratorTypeScreens OTG_WORLD_TYPE = new BiomeGeneratorTypeScreens(PluginStandardValues.MOD_ID_SHORT)
	{
		protected ChunkGenerator func_241869_a(Registry<Biome> biomes, Registry<DimensionSettings> dimensionSettings, long seed)
		{
			// Provide our custom chunk generator, biome provider and dimension settings.
			return new OTGNoiseChunkGenerator(new OTGBiomeProvider(null, seed, false, false, biomes), seed, () -> dimensionSettings.func_243576_d(OTG_DIMENSION_SETTINGS));
		}
	};
	
	static
	{
		// Register the otg worldtype for the world creation screen
		BiomeGeneratorTypeScreens.field_239068_c_.add(OTG_WORLD_TYPE);
		
		// Register OTG DimensionSettings for OTG worlds/dims
		// TODO: Taken from vanilla overworld dim settings, adapt.
		WorldGenRegistries.func_243664_a(
			WorldGenRegistries.field_243658_j, 
			OTGPlugin.OTG_DIMENSION_SETTINGS.func_240901_a_(), 
			new DimensionSettings(
				new DimensionStructuresSettings(true), 
				new NoiseSettings(
					256, 
					new ScalingSettings(0.9999999814507745D, 0.9999999814507745D, 80.0D, 160.0D), 
					new SlideSettings(-10, 3, 0), 
					new SlideSettings(-30, 0, 0), 1, 2, 1.0D, -0.46875D, true, true, false, false
				), 
				Blocks.STONE.getDefaultState(), 
				Blocks.WATER.getDefaultState(), 
				-10, 
				0, 
				63, 
				false
			)
		);
		
		// Register world type customisation button / screens

		Map<Optional<BiomeGeneratorTypeScreens>, BiomeGeneratorTypeScreens.IFactory> otgWorldOptionsScreen =
			ImmutableMap.of(
				Optional.of(OTG_WORLD_TYPE),
				(createWorldScreen, dimensionGeneratorSettings) ->
				{
					return new CreateOTGWorldScreen(
						createWorldScreen,
						createWorldScreen.field_238934_c_.func_239055_b_(),
						// Define apply function, generates updated 
						// settings when leaving customisation menu.
						(dimensionConfig) ->
						{
							createWorldScreen.field_238934_c_.func_239043_a_(
								OTGPlugin.createOTGDimensionGeneratorSettings(
									createWorldScreen.field_238934_c_.func_239055_b_(),
									dimensionGeneratorSettings,
									OTG_WORLD_TYPE,
									dimensionConfig
								)
							);
						}
					);
				}
			)
		;
		
		BiomeGeneratorTypeScreens.field_239069_d_ = ImmutableMap.<Optional<BiomeGeneratorTypeScreens>, BiomeGeneratorTypeScreens.IFactory>builder()
		    .putAll(BiomeGeneratorTypeScreens.field_239069_d_)
		    .putAll(otgWorldOptionsScreen)
		    .build()
	    ;
	}

	public static DimensionGeneratorSettings createOTGDimensionGeneratorSettings(DynamicRegistries dynamicRegistries, DimensionGeneratorSettings dimensionGeneratorSettings, BiomeGeneratorTypeScreens biomeGeneratorTypeScreens, DimensionConfig dimensionConfig)
	{
		Registry<DimensionType> dimensionTypesRegistry = dynamicRegistries.func_243612_b(Registry.field_239698_ad_);
		Registry<Biome> biomesRegistry = dynamicRegistries.func_243612_b(Registry.field_239720_u_);
		Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.func_243612_b(Registry.field_243549_ar);

		return new DimensionGeneratorSettings(
			dimensionGeneratorSettings.func_236221_b_(),
			dimensionGeneratorSettings.func_236222_c_(),
			dimensionGeneratorSettings.func_236223_d_(),
			DimensionGeneratorSettings.func_242749_a(
				dimensionTypesRegistry,
				dimensionGeneratorSettings.func_236224_e_(),
				new OTGNoiseChunkGenerator(
					dimensionConfig,
					new OTGBiomeProvider(
						dimensionConfig.PresetName,
						dimensionGeneratorSettings.func_236221_b_(),
						false,
						false,
						biomesRegistry
					),
					dimensionGeneratorSettings.func_236221_b_(),
					() -> { return dimensionSettingsRegistry.func_243576_d(OTG_DIMENSION_SETTINGS); }
				)
			)
		);
	}
	
	//

	// DeferredRegister for Biomes doesn't appear to be working atm, biomes are never registered :(
   	//public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, PluginStandardValues.MOD_ID);

	public OTGPlugin()
	{	
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		
		// TODO: Document why we need these, they don't appear to be used for anything atm?
		Registry.register(Registry.field_239689_aA_, new ResourceLocation(PluginStandardValues.MOD_ID_SHORT, "default"), OTGBiomeProvider.CODEC);
		Registry.register(Registry.field_239690_aB_, new ResourceLocation(PluginStandardValues.MOD_ID_SHORT, "default"), OTGNoiseChunkGenerator.CODEC);
		
        // Start OpenTerrainGenerator engine, loads all presets.
        OTG.setEngine(new ForgeEngine());
	}
	
    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event)
    {
    	OTG.getEngine().getPresetLoader().registerBiomes();
    }

	@SubscribeEvent
	public void onCommandRegister(RegisterCommandsEvent event)
	{
		OTGCommand.register(event.getDispatcher());
	}
}
