package com.pg85.otg.forge.events.server;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;

import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SaveServerHandler
{
	@SubscribeEvent
	public void onSave(Save event)
	{		
		((ForgeEngine)OTG.getEngine()).onSave(event.getWorld());			
	}
}