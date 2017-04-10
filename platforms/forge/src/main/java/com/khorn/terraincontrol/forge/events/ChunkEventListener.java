package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import net.minecraftforge.event.world.ChunkEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChunkEventListener
{
	@SideOnly(Side.SERVER)
	@SubscribeEvent
	public void onUnloadChunk(Unload event)
	{
		boolean cartographerEnabled = ((ForgeEngine)TerrainControl.getEngine()).getCartographerEnabled();

		if(cartographerEnabled)
		{
			if(event.getWorld().provider.getDimension() == 0)
			{
				Cartographer.CreateBlockWorldMapAtSpawn(ChunkCoordinate.fromChunkCoords(event.getChunk().xPosition, event.getChunk().zPosition), true);
			}
		}
	}
}
