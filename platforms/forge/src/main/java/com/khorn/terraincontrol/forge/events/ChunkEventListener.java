package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.ChunkEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChunkEventListener
{
	@SubscribeEvent
	public void onUnloadChunk(Unload event)
	{
		if(!event.getWorld().isRemote)
		{
			boolean cartographerEnabled = ((ForgeEngine)TerrainControl.getEngine()).getCartographerEnabled();
	
			if(cartographerEnabled)
			{
				if(event.getWorld().provider.getDimension() == 0)
				{
					Cartographer.CreateBlockWorldMapAtSpawn(ChunkCoordinate.fromChunkCoords(event.getChunk().x, event.getChunk().z), true);
				}
			}
		}
	}
	
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void clientDisconnectionFromServerEvent(ClientDisconnectionFromServerEvent event)
    {
    	if(!Minecraft.getMinecraft().isSingleplayer()) // Don't do this for Forge SP client, it will save and unload all worlds when it shuts down the server
    	{
    		((ForgeEngine)TerrainControl.getEngine()).getWorldLoader().onServerStopped();
    	}
    }
}
