package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.TCDimensionManager;
import com.khorn.terraincontrol.forge.TCWorldType;
import com.khorn.terraincontrol.logging.LogMarker;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldListener
{
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		((ForgeEngine)TerrainControl.getEngine()).getPregenerator().SavePreGeneratorData(event.getWorld());
	}
    
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			//int dimId = event.world.provider.getDimension();
			//TerrainControl.log(LogMarker.INFO, event.world.isRemote ? "Client" : "Server" + " ticking world " + event.world.getWorldInfo().getWorldName() + " at dim " + dimId);
		}
	}

	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event)
	{	

	}
	
    // TODO: This method should not be called by DimensionManager when switching dimensions (main -> nether -> main). Find out why it is being called
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
		if(!event.getWorld().isRemote) // Server side only
		{			
	        World mcWorld = event.getWorld();
	        if(event.getWorld().getWorldType() instanceof TCWorldType)
	        {	        	
		        ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)TerrainControl.getEngine()).getWorld(mcWorld);
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
				TerrainControl.log(LogMarker.DEBUG, "onWorldUnload serverstopping: " + !mcServer.isServerRunning() + " " + event.getWorld().getWorldInfo().getWorldName() + " at dim " + dimId);
		        		        
		    	//if(dimId > 1)
		    	{		    		
	    			if((ForgeWorld) ((ForgeEngine)TerrainControl.getEngine()).getWorld(event.getWorld()) != null)
	    			{    				
		    			//TerrainControl.log(LogMarker.INFO, "Unloading world " + event.getWorld().getWorldInfo().getWorldName() + " at dim " + dimId);
		    			// DimensionManager.setWorld(dimId, null, server);
		    			((TCWorldType)event.getWorld().getWorldType()).worldLoader.unloadWorld(event.getWorld(), false);
	    			} else {
	    				// World has already been unloaded, only happens when shutting down server?
	    			}
		    	}
	        	if(serverStopping)
	        	{
	        		TCDimensionManager.UnloadCustomDimensionData(mcWorld.provider.getDimension());
	        		forgeWorld.unRegisterBiomes();
	        		
	        		if(mcWorld.provider.getDimension() == 0)
	        		{
	        			// Unregister any currently unloaded custom dimensions	        			
	        			for(ForgeWorld unloadedWorld : ((ForgeEngine)TerrainControl.getEngine()).getUnloadedWorlds())
	        			{
	        				if(unloadedWorld.getWorld() != mcWorld)
	        				{
		    	        		TCDimensionManager.UnloadCustomDimensionData(unloadedWorld.getWorld().provider.getDimension());
		        				unloadedWorld.unRegisterBiomes();
	        				}
	        			}
	        		}
	        	}
	        }

	        //if(event.getWorld().provider.getDimension() != 0) // Temporary fix, this may break multi-world support (I assume it uses dimensions to load other worlds?) 
	        //{
	            //World mcWorld = event.getWorld();           
	            //ForgeWorld forgeWorld = this.worldLoader.getWorld(WorldHelper.getName(mcWorld));
	            //if (forgeWorld == null)
	            //{
	                //return;
	            //}
	            
	        	//((ForgeEngine)TerrainControl.getEngine()).getPregenerator().shutDown(mcWorld);
	        	//this.worldLoader.unloadWorld(forgeWorld);
	        //}
		}
    }
    
    @SideOnly(Side.CLIENT)
    private MinecraftServer getClientServer()
    {
    	return net.minecraft.client.Minecraft.getMinecraft().getIntegratedServer();
    }
    		
}
