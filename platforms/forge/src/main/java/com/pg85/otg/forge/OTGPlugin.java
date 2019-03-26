package com.pg85.otg.forge;

import com.google.common.base.Function;
import com.pg85.otg.LocalBiome;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.events.EventPriority;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.events.*;
import com.pg85.otg.forge.events.client.ClientTickHandler;
import com.pg85.otg.forge.events.client.KeyBoardEventListener;
import com.pg85.otg.forge.events.dimensions.BlockTracker;
import com.pg85.otg.forge.events.dimensions.EntityTravelToDimensionListener;
import com.pg85.otg.forge.events.dimensions.RightClickListener;
import com.pg85.otg.forge.events.server.OTGCommandHandler;
import com.pg85.otg.forge.events.server.SaveServerHandler;
import com.pg85.otg.forge.events.server.ServerEventListener;
import com.pg85.otg.forge.events.server.UnloadServerHandler;
import com.pg85.otg.forge.generator.ForgeVanillaBiomeGenerator;
import com.pg85.otg.forge.generator.structure.OTGRareBuildingStart;
import com.pg85.otg.forge.generator.structure.OTGVillageStart;
import com.pg85.otg.forge.gui.GuiHandler;
import com.pg85.otg.forge.network.CommonProxy;
import com.pg85.otg.forge.network.PacketDispatcher;
import com.pg85.otg.forge.network.client.BukkitClientNetworkEventListener;
import com.pg85.otg.generator.biome.VanillaBiomeGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraftTypes.StructureNames;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

//@Mod(modid = "openterraingenerator", name = "Open Terrain Generator", acceptableRemoteVersions = "*", version = "v2", certificateFingerprint = "e9f7847a78c5342af5b0a9e04e5abc0b554d69e0")
@Mod(modid = "openterraingenerator", name = "Open Terrain Generator", version = "v7", certificateFingerprint = "e9f7847a78c5342af5b0a9e04e5abc0b554d69e0")
public class OTGPlugin
{	
	@SidedProxy(clientSide="com.pg85.otg.forge.network.client.ClientProxy", serverSide="com.pg85.otg.forge.network.server.ServerProxy")
	public static CommonProxy proxy;

	@Instance("OTG")
    public static OTGPlugin instance;

    private WorldLoader worldLoader;
    public static OTGWorldType txWorldType;
    
    // TODO: Is this handler really necessary to make signing work?
    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        //logger.warning("Invalid fingerprint detected!");
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        File configsDir = new File(Loader.instance().getConfigDir(), "OpenTerrainGenerator");
        this.worldLoader = new WorldLoader(configsDir);
        // Register World listener for tracking world unloads and loads.
        MinecraftForge.EVENT_BUS.register(new WorldListener());

        // Create the world type. WorldType registers itself in the constructor
        // - that is Mojang code, so don't blame me
        txWorldType = new OTGWorldType(this.worldLoader);

        // Start OpenTerrainGenerator engine
        final ForgeEngine engine = new ForgeEngine(this.worldLoader);
        OTG.setEngine(engine);

        // Register Default biome generator to OpenTerrainGenerator
        engine.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, ForgeVanillaBiomeGenerator.class);

        // Register village and rare building starts
        MapGenStructureIO.registerStructure(OTGRareBuildingStart.class, StructureNames.RARE_BUILDING);
        MapGenStructureIO.registerStructure(OTGVillageStart.class, StructureNames.VILLAGE);

        // Register listening channel for listening to received configs. <- Spigot only?
        if (event.getSide() == Side.CLIENT)
        {
            BukkitClientNetworkEventListener networkHandler = new BukkitClientNetworkEventListener(this.worldLoader);
            FMLEventChannel eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PluginStandardValues.ChannelName);
            eventDrivenChannel.register(networkHandler);
            MinecraftForge.EVENT_BUS.register(networkHandler);
        }

        PacketDispatcher.registerPackets();

        // Register player tracker, for sending configs.
        MinecraftForge.EVENT_BUS.register(new PlayerTracker());

        // Register block tracker, for block protect
        MinecraftForge.EVENT_BUS.register(new BlockTracker());

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener();
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        MinecraftForge.EVENT_BUS.register(new SaveServerHandler());
        MinecraftForge.EVENT_BUS.register(new UnloadServerHandler());

        // Register colorizer, for biome colors
        Function<Biome, BiomeConfig> getBiomeConfig = new Function<Biome, BiomeConfig>()
        {
            @Override
            public BiomeConfig apply(Biome input)
            {
                LocalBiome biome = null;
                try
                {
                	// Get world name from resourcelocation
                	// TODO: Get world name from somewhere sensical...
                	biome = OTG.getBiome(input.getBiomeName(), input.getRegistryName().getResourcePath().split("_")[0]);
                }
                catch (BiomeNotFoundException e)
                {
                    // Ignored, try in next world
                }

                if (biome == null)
                {
                    return null;
                }

                return biome.getBiomeConfig();
            }
        };

        MinecraftForge.EVENT_BUS.register(new BiomeColorsListener(getBiomeConfig));

        // Register server tick handler for pre-generation of worlds
        MinecraftForge.EVENT_BUS.register(new ServerEventListener());

        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());

        MinecraftForge.EVENT_BUS.register(new GuiHandler());

        // Register KeyBoardEventListener for toggling pre-generator in-game UI using F3
        MinecraftForge.EVENT_BUS.register(new KeyBoardEventListener());

        // Register to our own events, so that they can be fired again as Forge events.
        engine.registerEventHandler(new OTGToForgeEventConverter(), EventPriority.CANCELABLE);

        // Register RightClickBlockListener for detecting fire and creating portals
        MinecraftForge.EVENT_BUS.register(new RightClickListener());

    	// Register EntityTravelToDimensionListener for quartz portals that tp to other dimensions
    	MinecraftForge.EVENT_BUS.register(new EntityTravelToDimensionListener());

        // Register ChunkLoadListener for updating Cartographer map
        MinecraftForge.EVENT_BUS.register(new ChunkEventListener());

        // Fix lava as light source not working when spawning lava as resource
        Blocks.LAVA.setLightOpacity(255);
    }

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
    	// Default settings are not restored on world unload / server quit 
    	// because this was causing problems (unloading dimensions while 
    	// their worlds were still ticking etc).
    	// Unload all world and biomes on server start / connect instead, 
    	// for SP client where data is kept when leaving the game.
 
    	((ForgeEngine)OTG.getEngine()).UnloadAndUnregisterAllWorlds();
    	ForgeEngine.loadPresets();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new OTGCommandHandler());

        World overWorld = DimensionManager.getWorld(0);

        if(overWorld.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator") && !(overWorld.getWorldInfo().getTerrainType() instanceof OTGWorldType))
        {
            ISaveHandler isavehandler = overWorld.getSaveHandler();
            WorldInfo worldInfo = isavehandler.loadWorldInfo();

            if(worldInfo != null)
            {
            	overWorld.getWorldInfo().setTerrainType(txWorldType);
            	worldInfo.setTerrainType(txWorldType);
            	isavehandler.saveWorldInfo(worldInfo);
            }
            throw new RuntimeException("OTG has detected that you are loading an OTG world that has been used without OTG installed. OTG has fixed and saved the world data, you can now restart the game and enter the world.");
        }

		if(!overWorld.isRemote) // Server side only
		{
			// This is a vanilla overworld, a new OTG world or a legacy OTG world without a dimensionconfig
		    if(OTG.GetDimensionsConfig() == null)
		    {
				// Check if there is a dimensionsConfig saved for this world
				DimensionsConfig dimsConfig = DimensionsConfig.LoadFromFile(overWorld.getSaveHandler().getWorldDirectory());
				if(dimsConfig == null)
				{
					// If there is no DimensionsConfig saved for this world, create one
					// LoadCustomDimensionData will add dimensions
					dimsConfig = new DimensionsConfig(overWorld.getSaveHandler().getWorldDirectory());
					// If this is a vanilla overworld then create a dummy overworld config
					if(!overWorld.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator"))
					{
						dimsConfig.Overworld = new DimensionConfig();
					}
					dimsConfig.Save();
				}
				OTG.SetDimensionsConfig(dimsConfig);
			}
		    			
		    OTGDimensionManager.ReAddOTGDims();
	
		    // Load any saved dimensions.
		    OTGDimensionManager.LoadCustomDimensionData();
	
		    // Create Cartographer dimension if it doesn't yet exist
			//Cartographer.CreateCartographerDimension();

		    for(DimensionConfig dimConfig : OTG.GetDimensionsConfig().Dimensions)
		    {
		    	if(!OTGDimensionManager.isDimensionNameRegistered(dimConfig.PresetName))
	    		{
		    		File worldConfigFile = new File(OTG.getEngine().getOTGDataFolder().getAbsolutePath() + "/" + PluginStandardValues.PresetsDirectoryName + "/" + dimConfig.PresetName + "/WorldConfig.ini");
		    		if(!worldConfigFile.exists())
		    		{
		    			OTG.log(LogMarker.ERROR, "Could not create dimension \"" + dimConfig.PresetName + "\", OTG preset " + dimConfig.PresetName + " could not be found or does not contain a WorldConfig.ini file.");
		    		} else {
		    			OTG.isNewWorldBeingCreated = true;
		    			OTGDimensionManager.createDimension(dimConfig.PresetName, false, true, false);
		    			OTG.isNewWorldBeingCreated = false;
		    		}
	    		}
		    }

		    OTGDimensionManager.SaveDimensionData();
		}
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        proxy.preInit(e);
    }

    @EventHandler
    public void init(FMLInitializationEvent e)
    {
        proxy.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        proxy.postInit(e);
    }
}