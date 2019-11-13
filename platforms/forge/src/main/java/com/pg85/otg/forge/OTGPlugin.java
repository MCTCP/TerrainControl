package com.pg85.otg.forge;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.events.EventPriority;
import com.pg85.otg.forge.events.*;
import com.pg85.otg.forge.events.client.ClientConnectionEventListener;
import com.pg85.otg.forge.events.client.ClientTickHandler;
import com.pg85.otg.forge.events.client.KeyBoardEventListener;
import com.pg85.otg.forge.events.dimensions.BlockTracker;
import com.pg85.otg.forge.events.dimensions.EntityTravelToDimensionListener;
import com.pg85.otg.forge.events.dimensions.RightClickListener;
import com.pg85.otg.forge.events.server.SaveServerHandler;
import com.pg85.otg.forge.events.server.ServerEventListener;
import com.pg85.otg.forge.events.server.ServerTickHandler;
import com.pg85.otg.forge.events.server.UnloadServerHandler;
import com.pg85.otg.forge.generator.ForgeVanillaBiomeGenerator;
import com.pg85.otg.forge.generator.structure.OTGRareBuildingStart;
import com.pg85.otg.forge.generator.structure.OTGVillageStart;
import com.pg85.otg.forge.gui.GuiHandler;
import com.pg85.otg.forge.network.CommonProxy;
import com.pg85.otg.forge.network.PacketDispatcher;
import com.pg85.otg.forge.network.client.BukkitClientNetworkEventListener;
import com.pg85.otg.forge.world.OTGWorldType;
import com.pg85.otg.generator.biome.VanillaBiomeGenerator;
import com.pg85.otg.util.minecraft.defaults.StructureNames;

import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
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

//@Mod(modid = "openterraingenerator", name = "Open Terrain Generator", acceptableRemoteVersions = "*", version = "v2")
@Mod(modid = "openterraingenerator", name = "Open Terrain Generator", version = "v8.3_r1", dependencies="required-after:otgcore@[1.12.2 - v8.3_r1]")
public class OTGPlugin
{	
	@SidedProxy(clientSide="com.pg85.otg.forge.network.client.ClientProxy", serverSide="com.pg85.otg.forge.network.server.ServerProxy")
	public static CommonProxy Proxy;

	@Instance("OTG")
    public static OTGPlugin Instance;

    public static OTGWorldType OtgWorldType;
        
    @EventHandler
    public void load(FMLInitializationEvent event)
    {    	       
        // Register World listener for tracking world unloads and loads.
        MinecraftForge.EVENT_BUS.register(new WorldListener());

        // Create the world type. WorldType registers itself in the constructor
        // - that is Mojang code, so don't blame me
        OtgWorldType = new OTGWorldType();

        // Start OpenTerrainGenerator engine
        OTG.setEngine(new ForgeEngine());

        // Register Default biome generator to OpenTerrainGenerator
        OTG.getEngine().getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, ForgeVanillaBiomeGenerator.class);

        // Register village and rare building starts
        MapGenStructureIO.registerStructure(OTGRareBuildingStart.class, StructureNames.RARE_BUILDING);
        MapGenStructureIO.registerStructure(OTGVillageStart.class, StructureNames.VILLAGE);
       
        // Register listening channel for listening to received configs. <- Spigot only?
        if (event.getSide() == Side.CLIENT)
        {
            BukkitClientNetworkEventListener networkHandler = new BukkitClientNetworkEventListener();
            FMLEventChannel eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PluginStandardValues.ChannelName);
            eventDrivenChannel.register(networkHandler);
            MinecraftForge.EVENT_BUS.register(networkHandler);
        }

        // Register packets
        PacketDispatcher.registerPackets();

        // Register player tracker, for detecting player login/logoff/teleport etc.
        MinecraftForge.EVENT_BUS.register(new PlayerTracker());

        // Register block tracker, for block protect etc.
        MinecraftForge.EVENT_BUS.register(new BlockTracker());

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener();
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        // Register save and unload handlers for saving data and unloading BO3's.
        MinecraftForge.EVENT_BUS.register(new SaveServerHandler());
        MinecraftForge.EVENT_BUS.register(new UnloadServerHandler());

        // Register biome colors listener, which listens to color events to apply custom colors.
        MinecraftForge.EVENT_BUS.register(new BiomeColorsListener());

        // Register server tick handler which is used for the pre-generator, dimAbove/dimBelow, mob/entity spawning etc.
        MinecraftForge.EVENT_BUS.register(new ServerTickHandler());

        // Register client tick handler for handling particles.
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());

        // Register gui handler for replacing MC's gui with OTG's
        MinecraftForge.EVENT_BUS.register(new GuiHandler());

        // Register KeyBoardEventListener for OTG's O menu.
        MinecraftForge.EVENT_BUS.register(new KeyBoardEventListener());

        // Register to our own events, so that they can be fired again as Forge events.
        OTG.getEngine().registerEventHandler(new ForgeEventHandler(), EventPriority.CANCELABLE);

        // Register RightClickBlockListener for detecting flint and tinder and creating portals
        MinecraftForge.EVENT_BUS.register(new RightClickListener());

    	// Register EntityTravelToDimensionListener for OTG portals that tp to other dimensions
    	MinecraftForge.EVENT_BUS.register(new EntityTravelToDimensionListener());

        // Register ClientConnectionEventListener for detecting disconnects on the client side and unloading worlds. 
        MinecraftForge.EVENT_BUS.register(new ClientConnectionEventListener());

        // Fix lava as light source not working when spawning lava as resource
        // TODO: This is a hack fix, lighting still needs to be fixed properly..
        Blocks.LAVA.setLightOpacity(255);
    }

    // TODO: Is this handler really necessary to make signing work?
    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        //logger.warning("Invalid fingerprint detected!");
    }
    
    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
    	ServerEventListener.serverAboutToStart(event);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
    	ServerEventListener.serverLoad(event);
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        Proxy.preInit(e);
    }

    @EventHandler
    public void init(FMLInitializationEvent e)
    {
        Proxy.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        Proxy.postInit(e);
    }
}