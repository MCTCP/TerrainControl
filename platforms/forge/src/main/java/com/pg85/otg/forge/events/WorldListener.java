package com.pg85.otg.forge.events;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.ForgeWorldSession;
import com.pg85.otg.forge.OTGWorldType;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldListener
{
    private static Field field_World_provider = null;
    private static Field field_ChunkProviderServer_chunkGenerator = null;

    static
    {
        try
        {
            field_World_provider = ReflectionHelper.findField(World.class, "field_73011_w", "provider");
            field_ChunkProviderServer_chunkGenerator = ReflectionHelper.findField(ChunkProviderServer.class, "field_186029_c", "chunkGenerator");
        }
        catch (UnableToFindFieldException e)
        {
            OTG.log(LogMarker.ERROR, "WorldUtils: Reflection failed!!", e);
        }
    }
	
    @SubscribeEvent(priority = EventPriority.HIGH)
	public void onWorldLoad(WorldEvent.Load event)
	{
        World world = event.getWorld();
        int dimension = world.provider.getDimension();

        //OTG.log(LogMarker.INFO, "WorldEvent.Load - DIM: {}", dimension);

        if (dimension == 0)
        {
        	ForgeWorld overworld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
        	if(overworld != null)
        	{
        		overrideWorldProvider(world);	
        	}
        }
	}
	
    public static void overrideWorldProvider(World world)
    {
        String newClassName = OTGWorldProvider.class.getName();
        Class<? extends WorldProvider> newProviderClass = OTGWorldProvider.class;

        if (newProviderClass != null && newProviderClass != world.provider.getClass())
        {
            final int dim = world.provider.getDimension();
            //OTG.log(LogMarker.INFO, "WorldUtils.overrideWorldProvider: Trying to override the WorldProvider of type '{}' in dimension {} with '{}'", oldName, dim, newClassName);

            try
            {
                Constructor <? extends WorldProvider> constructor = newProviderClass.getConstructor();
                WorldProvider newProvider = constructor.newInstance();               

                try
                {                	
                    WorldProvider oldProvider = world.provider;
                    field_World_provider.set(world, newProvider);
                    ((OTGWorldProvider)world.provider).isSPServerOverworld = !world.isRemote;
                   	world.provider.setWorld(world);
                    world.provider.setDimension(dim);
                 
                    if(!world.isRemote)
                    {
                    	// TODO: Bit of a hack, need to override the worldprovider for SP server or gravity won't work properly ><.
                    	// Creating a new biomeprovider causes problems, re-using the existing one seems to work though,
                    	((OTGWorldProvider)world.provider).init(oldProvider.getBiomeProvider());
                    }
                    
                    //OTG.log(LogMarker.INFO, "WorldUtils.overrideWorldProvider: Overrode the WorldProvider in dimension {} with '{}'", dim, newClassName);
                }
                catch (Exception e)
                {
                    OTG.log(LogMarker.ERROR, "WorldUtils.overrideWorldProvider: Failed to override the WorldProvider of dimension {}", dim);
                }

                return;
            }
            catch (Exception e)
            {
            	
            }
        }

        OTG.log(LogMarker.WARN, "WorldUtils.overrideWorldProvider: Failed to create a WorldProvider from name '{}', or it was already that type", newClassName);
    }
    
	@SubscribeEvent
	@SideOnly(Side.SERVER)
	public void onWorldLoadServer(WorldEvent.Load event)
	{
    	ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld());
    	if(forgeWorld != null)
    	{
    		ServerPacketManager.SendDimensionLoadUnloadPacketToAllPlayers(true, forgeWorld.getName(), event.getWorld().getMinecraftServer());
    	}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldLoadClient(WorldEvent.Load event)
	{		
		// For single player only one world is loaded on the client, but forgeworlds exist for all dims		
		for(LocalWorld localWorld : ((ForgeEngine)OTG.getEngine()).getAllWorlds())
		{
			ForgeWorld forgeWorld = (ForgeWorld)localWorld;
			if(forgeWorld.getWorld() == null && forgeWorld.clientDimensionId == event.getWorld().provider.getDimension())
			{
				forgeWorld.provideClientWorld(event.getWorld());
			}
		}
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		ForgeWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld());
		if(world != null)
		{
			((ForgeWorldSession)world.GetWorldSession()).getPregenerator().SavePregeneratorData();
		} else {
			// This is not an OTG world.
		}
	}
	
    // TODO: This method should not be called by DimensionManager when switching dimensions (main -> nether -> main). Find out why it is being called
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
		if(!event.getWorld().isRemote) // Server side only
		{			
	        World mcWorld = event.getWorld();
	        if(event.getWorld().getWorldType() instanceof OTGWorldType)
	        {	        	
		        ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(mcWorld);
		        if(forgeWorld == null)
		        {
		        	// Can happen if this is dim -1 or 1 (or some other mod's dim?)
		        	return;
		        }		        
		        
		        MinecraftServer mcServer = mcWorld.getMinecraftServer();
		        if(mcServer == null)
		        {
		        	mcServer = getClientServer();
		        }
		     
		        boolean serverStopping = !mcServer.isServerRunning();
		        
				int dimId = event.getWorld().provider.getDimension();
		        		        
				if(dimId != -1 && dimId != 1)
		    	{		    		
	    			if((ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld()) != null)
	    			{    				
		    			//OTG.log(LogMarker.INFO, "Unloading world " + event.getWorld().getWorldInfo().getWorldName() + " at dim " + dimId);
		    			((OTGWorldType)event.getWorld().getWorldType()).worldLoader.unloadWorld(event.getWorld(), false);
	    			} else {
	    				// World has already been unloaded, only happens when shutting down server?
	    			}

		        	if(serverStopping)
		        	{
		        		OTGDimensionManager.UnloadCustomDimensionData(mcWorld.provider.getDimension());
		        		forgeWorld.unRegisterBiomes();
		        		
        				((ForgeWorldSession)forgeWorld.GetWorldSession()).getPregenerator().shutDown();
	        			
	        			// Unregister any currently unloaded custom dimensions
        				// Doesn't matter that this might happen multiple times on server shutdown
	        			for(ForgeWorld unloadedWorld : ((ForgeEngine)OTG.getEngine()).getUnloadedWorlds())
	        			{
	        				if(unloadedWorld.getWorld() != mcWorld)
	        				{
		    	        		OTGDimensionManager.UnloadCustomDimensionData(unloadedWorld.getWorld().provider.getDimension());
		        				unloadedWorld.unRegisterBiomes();
	        				}
	        			}
		        	}
		    	}
	        }
		}
    }
    
    @SideOnly(Side.CLIENT)
    private MinecraftServer getClientServer()
    {
    	return net.minecraft.client.Minecraft.getMinecraft().getIntegratedServer();
    }
    
    @SubscribeEvent
    public void onCreateWorldSpawn(WorldEvent.CreateSpawnPosition event)
    {    
    	// Make sure the world spawn doesn't get moved after the first chunks have been spawned  
    	LocalWorld world = ((ForgeEngine) OTG.getEngine()).getWorld(event.getWorld());
    	if(world != null)
    	{
	        if(((ForgeEngine)OTG.getEngine()).getCartographerEnabled() || world.GetWorldSession().getWorldBorderRadius() > 0 || (world.getConfigs().getWorldConfig().BO3AtSpawn != null && world.getConfigs().getWorldConfig().BO3AtSpawn.trim().length() > 0))
	        {
	        	event.setCanceled(true);
	        }        
    	}
    }    
}
