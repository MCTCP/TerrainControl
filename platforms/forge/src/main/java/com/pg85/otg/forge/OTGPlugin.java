package com.pg85.otg.forge;

import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.pg85.otg.OTG;
import com.pg85.otg.config.standard.PluginStandardValues;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.commands.OTGCommand;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.gui.screens.CreateOTGWorldScreen;

// The value here should match an entry in the META-INF/mods.toml files
@Mod(PluginStandardValues.MOD_ID_SHORT)
@Mod.EventBusSubscriber(modid = PluginStandardValues.MOD_ID_SHORT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OTGPlugin
{
	// TODO: Use custom DimensionSettings?
	private static final RegistryKey<DimensionSettings> OVERWORLD = RegistryKey.func_240903_a_(Registry.field_243549_ar, new ResourceLocation("overworld"));

	// Register the otg worldtype for the world creation screen
	private static final BiomeGeneratorTypeScreens otgWorldType = new BiomeGeneratorTypeScreens("otg")
	{
		protected ChunkGenerator func_241869_a(Registry<Biome> biomes, Registry<DimensionSettings> dimensionSettings, long seed)
		{
			// If the OTG world options screens haven't been registered yet, do so now.
			registerOTGWorldCreationOptionsScreen();
			
			return new OTGNoiseChunkGenerator(new OTGBiomeProvider(seed, false, false, biomes), seed, () -> dimensionSettings.func_243576_d(OVERWORLD));
		}
		
		private void registerOTGWorldCreationOptionsScreen()
		{
			if(!BiomeGeneratorTypeScreens.field_239069_d_.containsKey(Optional.of(this)))
			{
				Map<Optional<BiomeGeneratorTypeScreens>, BiomeGeneratorTypeScreens.IFactory> otgWorldOptionsScreen =
					ImmutableMap.of(
						Optional.of(otgWorldType), (p_239087_0_, p_239087_1_) -> 
						{
							// TODO: Uses a copy of the floating islands world options screen atm, replace.
							return new CreateOTGWorldScreen(
								p_239087_0_, 
								p_239087_0_.field_238934_c_.func_239055_b_(), 
								(p_239088_2_) ->
								{
									// Uses SingleBiomeProvider to create a single biome world, disabled.
									//p_239087_0_.field_238934_c_.func_239043_a_(
										//func_243452_a(
											//p_239087_0_.field_238934_c_.func_239055_b_(), 
											//p_239087_1_, 
											//otgWorldType, 
											//p_239088_2_
										//)
									//);
								},
								BiomeGeneratorTypeScreens.func_243451_a(
									p_239087_0_.field_238934_c_.func_239055_b_(), 
									p_239087_1_
								)
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
		}
	};
		
	// DeferredRegister for Biomes doesn't appear to be working atm, biomes are never registered :(
   	//public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, PluginStandardValues.MOD_ID);

	public OTGPlugin()
	{	
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		Registry.register(Registry.field_239689_aA_, new ResourceLocation("otg", "otg"), OTGBiomeProvider.CODEC);
		Registry.register(Registry.field_239690_aB_, new ResourceLocation("otg", "otg"), OTGNoiseChunkGenerator.CODEC);

		// Register the otg worldtype for the world creation screen
		BiomeGeneratorTypeScreens.field_239068_c_.add(otgWorldType);
		
        // Start OpenTerrainGenerator engine, loads all presets.
        OTG.setEngine(new ForgeEngine());
	}
	
    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event)
    {
    	OTG.getEngine().getPresetLoader().registerBiomes();
    }

	private void commonSetup(final FMLCommonSetupEvent event) { }

	private void clientSetup(final FMLClientSetupEvent event) { }	

	@SubscribeEvent
	public void onCommandRegister(RegisterCommandsEvent event)
	{
		OTGCommand.register(event.getDispatcher());
	}
	
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) { }
}
