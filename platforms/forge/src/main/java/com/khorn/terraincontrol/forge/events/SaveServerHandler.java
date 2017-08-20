package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;

import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SaveServerHandler
{
	//long lastSaveTime = 0;
	//int saveIntervalInMs = 60000 * 5;
	@SubscribeEvent
	public void onSave(Save event)
	{		
		//TerrainControl.log(LogMarker.INFO, "SaveServerHandler onSave");
		//if(event.world.provider.isSurfaceWorld() && System.currentTimeMillis() - lastSaveTime > saveIntervalInMs)
		{
			((ForgeEngine)TerrainControl.getEngine()).onSave(event.getWorld());			
			//lastSaveTime = System.currentTimeMillis();
		}
	}
}