package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.logging.LogMarker;

import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class UnloadServerHandler
{
	@SubscribeEvent
	public void onUnload(Unload event)
	{
		if(!event.getWorld().isRemote)
		{
			((ForgeEngine)TerrainControl.getEngine()).onSave(event.getWorld());
			if(TerrainControl.getPluginConfig().DeveloperMode)
			{
	    		TerrainControl.log(LogMarker.INFO, "Unloading BO2's/BO3's");
	    		TerrainControl.getEngine().ReloadCustomObjectFiles();
	    		TerrainControl.log(LogMarker.INFO, "BO2's/BO3's unloaded");
			}
		}
	}
}