package com.khorn.terraincontrol.forge;

import com.google.common.base.Function;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.client.events.ClientNetworkEventListener;
import com.khorn.terraincontrol.forge.dimensions.DimensionSyncChannelHandler;
import com.khorn.terraincontrol.forge.dimensions.TXDimensionManager;
import com.khorn.terraincontrol.forge.events.*;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.forge.generator.ForgeVanillaBiomeGenerator;
import com.khorn.terraincontrol.forge.generator.structure.TXRareBuildingStart;
import com.khorn.terraincontrol.forge.generator.structure.TXVillageStart;
import com.khorn.terraincontrol.forge.gui.GuiHandler;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.lang.reflect.Field;
import java.util.EnumMap;

@Mod(modid = "openterraingenerator", name = "Open Terrain Generator", acceptableRemoteVersions = "*", version = "v7")
public class TXPlugin
{
	public TXPlugin()
	{
		TerrainControl.isForge = true;
	}
	
    private WorldLoader worldLoader;
    public static TXWorldType txWorldType;    
    
    public static EnumMap<Side, FMLEmbeddedChannel> channels;

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        File configsDir = new File(Loader.instance().getConfigDir(), "OpenTerrainGenerator");
        this.worldLoader = new WorldLoader(configsDir);
        // Register World listener for tracking world unloads and loads.
        MinecraftForge.EVENT_BUS.register(new WorldListener());

        // Create the world type. WorldType registers itself in the constructor
        // - that is Mojang code, so don't blame me
        txWorldType = new TXWorldType(this.worldLoader);

        // Start TerrainControl engine
        final ForgeEngine engine = new ForgeEngine(this.worldLoader);
        TerrainControl.setEngine(engine);

        // Register Default biome generator to TerrainControl
        engine.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, ForgeVanillaBiomeGenerator.class);

        // Register village and rare building starts
        MapGenStructureIO.registerStructure(TXRareBuildingStart.class, StructureNames.RARE_BUILDING);
        MapGenStructureIO.registerStructure(TXVillageStart.class, StructureNames.VILLAGE);

        // Register listening channel for listening to received configs.
        if (event.getSide() == Side.CLIENT)
        {
            ClientNetworkEventListener networkHandler = new ClientNetworkEventListener(this.worldLoader);
            FMLEventChannel eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PluginStandardValues.ChannelName);
            eventDrivenChannel.register(networkHandler);
            MinecraftForge.EVENT_BUS.register(networkHandler);
        }        
        
        channels = NetworkRegistry.INSTANCE.newChannel("OTGChannel", DimensionSyncChannelHandler.instance);
        
        // Register player tracker, for sending configs.
        MinecraftForge.EVENT_BUS.register(new PlayerTracker());

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener();
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        // Register colorizer, for biome colors
        Function<Biome, BiomeConfig> getBiomeConfig = new Function<Biome, BiomeConfig>()
        {
            @Override
            public BiomeConfig apply(Biome input)
            {
                LocalBiome biome = null;
                try
                {
                	biome = TerrainControl.getBiomeAllWorlds(input.getBiomeName());
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

        MinecraftForge.EVENT_BUS.register(new GuiHandler());

        // Register KeyBoardEventListener for toggling pre-generator in-game UI using F3
        MinecraftForge.EVENT_BUS.register(new KeyBoardEventListener());

        // Register to our own events, so that they can be fired again as Forge events.
        engine.registerEventHandler(new TCToForgeEventConverter(), EventPriority.CANCELABLE);
        
        // Register RightClickBlockListener for detecting fire and creating portals
        MinecraftForge.EVENT_BUS.register(new RightClickBlockListener());
    	
    	// Register EntityTravelToDimensionListener for quartz portals that tp to other dimensions
    	MinecraftForge.EVENT_BUS.register(new EntityTravelToDimensionListener());
    	
        // Register ChunkLoadListener for updating Cartographer map
        MinecraftForge.EVENT_BUS.register(new ChunkEventListener());
        
        FixWorlds();
    }
    
    // If a world was created with OTG, then used without OTG and then used with OTG again then the WorldType 
    // will have been set back to vanilla, put it back to TXWorldType.
    private void FixWorlds()    
    {  	
        File savesFolder;
        try
        {
            Field minecraftDir = Loader.class.getDeclaredField("minecraftDir");
            minecraftDir.setAccessible(true);
            savesFolder = new File((File) minecraftDir.get(null), "saves");
        }
        catch (Throwable e)
        {
            System.out.println("Could not reflect the Minecraft directory, save location may be unpredicatble.");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
            return;
        }
    	
    	ISaveFormat saveLoader = new AnvilSaveConverter(savesFolder, DataFixesManager.createFixer());
    	
    	for(String folderName : savesFolder.list())
    	{
	        ISaveHandler isavehandler = saveLoader.getSaveLoader(folderName, false);
	        WorldInfo worldInfo = isavehandler.loadWorldInfo();
	        
	        if(worldInfo != null && !(worldInfo.getTerrainType() instanceof TXWorldType))
	        {
		        worldInfo.setTerrainType(txWorldType);
	            isavehandler.saveWorldInfo(worldInfo);
	        }
    	}
    }    
    
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {    	
        event.registerServerCommand(new TXCommandHandler());
        
        World overWorld = DimensionManager.getWorld(0);
      
        if(overWorld.getWorldInfo().getTerrainType() instanceof TXWorldType)
        {			
			if(!overWorld.isRemote) // Server side only
			{	
	        	TXDimensionManager.ReAddTCDims();
				
	        	// Load any saved dimensions.
	        	TXDimensionManager.LoadCustomDimensionData();
	        	
	        	// Create Cartographer dimension if it doesn't yet exist
				Cartographer.CreateCartographerDimension();
	
	            // Create dimensions defined in worldconfig if they don't yet exist
				ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)TerrainControl.getEngine()).getWorld(overWorld);
				WorldConfig worldConfig = forgeWorld.getConfigs().getWorldConfig();
				
	            for(String dimName : worldConfig.Dimensions)
	            {
	    	    	if(!TXDimensionManager.isDimensionNameRegistered(dimName))
	    	    	{
	    				File worldConfigFile = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds/" + dimName + "/WorldConfig.ini");
	    				if(!worldConfigFile.exists())
	    				{
	    					TerrainControl.log(LogMarker.ERROR, "Could not create dimension \"" + dimName + "\", mods/OpenTerrainGenerator/worlds/" + dimName + " could not be found or does not contain a WorldConfig.ini file.");
	    				} else {
	    		    		TXDimensionManager.createDimension(dimName, false, true, false);
	    				}
	    	    	}
	            }	 
	                       
	            TXDimensionManager.SaveDimensionData();
			}
        }
    }
}