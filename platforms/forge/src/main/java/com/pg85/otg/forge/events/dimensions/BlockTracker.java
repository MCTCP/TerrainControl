package com.pg85.otg.forge.events.dimensions;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.forge.world.WorldHelper;

public class BlockTracker
{
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
    	DimensionsConfig dimsConfig = OTG.getDimensionsConfig();
    	// TODO: DimsConfig and event should never be null, but we've had crash reports when using OTG with other mods, reproduce and confirm.
    	if(dimsConfig != null && event != null)
    	{
	    	DimensionConfig dimConfig = dimsConfig.getDimensionConfig(WorldHelper.getName(event.getWorld()));
	    	if(dimConfig != null && !dimConfig.Settings.PlayersCanBreakBlocks)
	    	{
	    		event.setCanceled(true);
	    	}
    	}
    }

	@SubscribeEvent
	public void onBlockPlace(BlockEvent.PlaceEvent event)
	{
    	DimensionsConfig dimsConfig = OTG.getDimensionsConfig();
    	// TODO: DimsConfig and event should never be null, but we've had crash reports when using OTG with other mods, reproduce and confirm.
    	if(dimsConfig != null && event != null)
    	{		
			DimensionConfig dimConfig = dimsConfig.getDimensionConfig(WorldHelper.getName(event.getWorld()));
			if(dimConfig != null && !dimConfig.Settings.PlayersCanPlaceBlocks)
			{
				event.setCanceled(true);
			}	
    	}
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Start event)
	{
    	DimensionsConfig dimsConfig = OTG.getDimensionsConfig();
    	// TODO: DimsConfig and event should never be null, but we've had crash reports when using OTG with other mods, reproduce and confirm.    	
    	if(dimsConfig != null && event != null)
    	{
			DimensionConfig dimConfig = dimsConfig.getDimensionConfig(WorldHelper.getName(event.getWorld()));
			if(dimConfig != null && !dimConfig.Settings.ExplosionsCanBreakBlocks)
			{
				event.setCanceled(true);
			}
    	}
	}
}
