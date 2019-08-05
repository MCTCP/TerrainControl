package com.pg85.otg.forge.events.dimensions;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.forge.util.WorldHelper;

public class BlockTracker
{
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
    	DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(WorldHelper.getName(event.getWorld()));
    	if(dimConfig != null && !dimConfig.Settings.PlayersCanBreakBlocks)
    	{
    		event.setCanceled(true);
    	}
    }

	@SubscribeEvent
	public void onBlockPlace(BlockEvent.PlaceEvent event)
	{
		DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(WorldHelper.getName(event.getWorld()));
		if(dimConfig != null && !dimConfig.Settings.PlayersCanPlaceBlocks)
		{
			event.setCanceled(true);
		}	
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Start event)
	{
		DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(WorldHelper.getName(event.getWorld()));
		if(dimConfig != null && !dimConfig.Settings.ExplosionsCanBreakBlocks)
		{
			event.setCanceled(true);
		}
	}
}
