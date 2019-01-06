package com.pg85.otg.forge.events.server;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;

import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SaveServerHandler
{
	//long lastSaveTime = 0;
	//int saveIntervalInMs = 60000 * 5;
	@SubscribeEvent
	public void onSave(Save event)
	{		
		//OTG.log(LogMarker.INFO, "SaveServerHandler onSave");
		//if(event.world.provider.isSurfaceWorld() && System.currentTimeMillis() - lastSaveTime > saveIntervalInMs)
		{
			((ForgeEngine)OTG.getEngine()).onSave(event.getWorld());			
			//lastSaveTime = System.currentTimeMillis();
		}
	}
}