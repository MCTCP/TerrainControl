package com.pg85.otg.forge;

import java.nio.file.Path;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.client.MultipleColorHandler;
import com.pg85.otg.forge.commands.OTGCommand;
import com.pg85.otg.forge.dimensions.OTGDimensionTypeHelper;
import com.pg85.otg.forge.dimensions.OTGWorldType;
import com.pg85.otg.forge.dimensions.portals.OTGPortalBlocks;
import com.pg85.otg.forge.dimensions.portals.OTGCapabilities;
import com.pg85.otg.forge.dimensions.portals.OTGPortalPois;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.gui.OTGGui;
import com.pg85.otg.forge.network.OTGClientSyncManager;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(Constants.MOD_ID_SHORT) // Should match META-INF/mods.toml
@Mod.EventBusSubscriber(modid = Constants.MOD_ID_SHORT, bus = Mod.EventBusSubscriber.Bus.MOD) 
public class OTGPlugin
{
	public OTGPlugin()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Register the clientSetup method for client-side initialisation logic (GUI etc).
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::commonSetup);

		// Register self for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		// Let MC know about our chunk generator and biome provider. 
		// If they're not added, we get errors and MC does not save properly.
		Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(Constants.MOD_ID_SHORT, "default"), OTGBiomeProvider.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(Constants.MOD_ID_SHORT, "default"), OTGNoiseChunkGenerator.CODEC);
		ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, "default"));

		// Deferred registers
		OTGPortalPois.poi.register(modEventBus);
		OTGPortalBlocks.blocks.register(modEventBus);
		
		OTGCommand.registerArguments();
	}

	// Register player capabilities for dimension portal timer.
	public void commonSetup(FMLCommonSetupEvent event)
	{
		OTGCapabilities.register();
	}

	// OTG World Type SP: We use our own world type registration logic so we can add a "customise"
	// button to the world creation gui that shows OTG preset selection and customisation screens.
	private void clientSetup(final FMLClientSetupEvent event)
	{
		// Register the OTG world type and any OTG GUI's for the world creation screen.
		OTGGui.init();
	}

	// OTG World Type MP: Register the OTG world type.
	// For MP we use server.properties level-type:otg + generatorSettings:presetFolderName
	@SubscribeEvent
	@OnlyIn(Dist.DEDICATED_SERVER)
	public static void registerWorldType(RegistryEvent.Register<ForgeWorldType> event)
	{
		ForgeRegistries.WORLD_TYPES.register(new OTGWorldType());
	}
	
	@SubscribeEvent
	public static void registerBiomes(RegistryEvent.Register<Biome> event)
	{
		// Start OpenTerrainGenerator engine, loads all presets.
		// Done here so that file indexing happens after presetpacker has unpacked its preset
		OTG.startEngine(new ForgeEngine());

		// Register all biomes
		// TODO: Use proper Forge way of registering biomes, we're not using
		// deferredregister (wasn't working before) or event.getRegistry().register atm.
		OTG.getEngine().getPresetLoader().registerBiomes();

		// Fog & colors networking/handlers
		OTGClientSyncManager.setup();
		MultipleColorHandler.setup();
	}

	@SubscribeEvent
	public void onCommandRegister(RegisterCommandsEvent event)
	{
		OTGCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void onSave(Save event)
	{
		// Save OTG DimensionTypes to world save folder as datapack json files so they're picked up on world load.
		// Unfortunately there doesn't appear to be a way to persist them via code(?)
		if(!event.getWorld().isClientSide())
		{
			if(((ServerLevel)event.getWorld()).getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator)
			{
				Path datapackDir = ((ServerLevel)event.getWorld()).getLevel().getServer().getWorldPath(LevelResource.DATAPACK_DIR);
				Preset preset = ((OTGNoiseChunkGenerator)((ServerLevel)event.getWorld()).getLevel().getChunkSource().generator).getPreset();
				String dimName = ((ServerLevel)event.getWorld()).dimension().location().getPath();
				OTGDimensionTypeHelper.saveDataPackFile(datapackDir, dimName, preset.getWorldConfig(), preset.getFolderName());
			}
		}
		((ForgeEngine)OTG.getEngine()).onSave(event.getWorld());
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event)
	{
		((ForgeEngine)OTG.getEngine()).onUnload(event.getWorld());
	}
}
