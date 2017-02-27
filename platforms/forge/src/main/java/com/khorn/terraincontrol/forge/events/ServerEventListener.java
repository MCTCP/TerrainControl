package com.khorn.terraincontrol.forge.events;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;

public class ServerEventListener
{	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{	
		if(event.phase == Phase.END)
		{
			((ForgeEngine)TerrainControl.getEngine()).getPregenerator().ProcessTick();
		}	
	}	
}