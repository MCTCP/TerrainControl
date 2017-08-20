package com.pg85.otg.forge.events;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.ForgeWorldSession;
import com.pg85.otg.forge.OTGWorldType;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldListener
{
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldLoad(WorldEvent.Load event)
	{		
		// For single player only one world is loaded on the client.		
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
		        	// Can happen if this is dim -1 or 1 (or some other mod's dim??)
		        	return;
		        }		        
		        
		        MinecraftServer mcServer = mcWorld.getMinecraftServer();
		        if(mcServer == null)
		        {
		        	mcServer = getClientServer();
		        }
		     
		        boolean serverStopping = !mcServer.isServerRunning();
		        
				// TODO: TC should only unload the world and dimension when the server is closed or a dimension is deleted. 
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
		        		
		        		if(mcWorld.provider.getDimension() == 0)
		        		{
		        			((ForgeWorldSession)forgeWorld.GetWorldSession()).getPregenerator().shutDown();
		        			
		        			// Unregister any currently unloaded custom dimensions	        			
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
