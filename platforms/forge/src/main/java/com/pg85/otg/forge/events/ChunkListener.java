package com.pg85.otg.forge.events;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChunkListener
{
	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload unloadEvent)
	{
		ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorld(unloadEvent.getWorld());
		if(forgeWorld != null)
		{
			forgeWorld.getChunkGenerator().clearChunkFromCache(ChunkCoordinate.fromChunkCoords(unloadEvent.getChunk().getPos().x, unloadEvent.getChunk().getPos().z));
		}
	}
}
