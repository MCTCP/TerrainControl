package com.pg85.otg.forge.events.server;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.logging.LogMarker;

import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class UnloadServerHandler
{
	@SubscribeEvent
	public void onUnload(Unload event)
	{
		if(!event.getWorld().isRemote)
		{
			((ForgeEngine)OTG.getEngine()).onSave(event.getWorld());
			if(OTG.getPluginConfig().developerMode)
			{
	    		OTG.log(LogMarker.DEBUG, "Unloading BO2's/BO3's");
	    		OTG.getEngine().getCustomObjectManager().ReloadCustomObjectFiles();
	    		OTG.log(LogMarker.DEBUG, "BO2's/BO3's unloaded");
			}
		}
	}
}