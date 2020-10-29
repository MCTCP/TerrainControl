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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.commands.OTGCommand;
import com.pg85.otg.forge.generator.OTGNoiseChunkGenerator;

// The value here should match an entry in the META-INF/mods.toml files
@Mod(PluginStandardValues.MOD_ID)
@Mod.EventBusSubscriber(modid = PluginStandardValues.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OTGPlugin
{   	
	// TODO: Use custom DimensionSettings?
	private static final RegistryKey<DimensionSettings> OVERWORLD = RegistryKey.func_240903_a_(Registry.field_243549_ar, new ResourceLocation("overworld"));

	// Register the otg worldtype for the world creation screen
	private static final BiomeGeneratorTypeScreens otgWorldType = new BiomeGeneratorTypeScreens("otg")
	{
		protected ChunkGenerator func_241869_a(Registry<Biome> biomes, Registry<DimensionSettings> dimensionSettings, long seed)
		{
			return new OTGNoiseChunkGenerator(
				new OTGBiomeProvider(seed, false, false, biomes),
					seed,
				() -> dimensionSettings.func_243576_d(OVERWORLD)
			);
		}
	};
	
   	public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, PluginStandardValues.MOD_ID);

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
