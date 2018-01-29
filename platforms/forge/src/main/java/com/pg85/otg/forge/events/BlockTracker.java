package com.pg85.otg.forge.events;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;

public class BlockTracker
{
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
    	ForgeWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld());
    	if(world != null && !world.getConfigs().getWorldConfig().playersCanBreakBlocks)
    	{
    		event.setCanceled(true);
    	}
    }

	@SubscribeEvent
	public void onBlockPlace(BlockEvent.PlaceEvent event)
	{
		ForgeWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld());
		if(world != null && !world.getConfigs().getWorldConfig().playersCanPlaceBlocks)
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Start event)
	{
		ForgeWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld());
		if(world != null && !world.getConfigs().getWorldConfig().explosionsCanBreakBlocks)
		{
			event.setCanceled(true);
		}
	}
}
