package com.pg85.otg.forge.event;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;

import net.minecraft.world.Dimension;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

//Only used to allow sleeping in OTG dimensions atm
@EventBusSubscriber(modid = Constants.MOD_ID_SHORT)
public class WorldHandler
{
	// Beds don't work in non-overworld dimensions since DerivedWorldInfo doesn't implement 
	// setDayTime (time is shared with the overworld, so can't tick more than one dim).
	// For non-overworld OTG dims, when players finish sleeping apply the new time to the 
	// overworld. 
	// TODO: Improve dimensions implementation, allow separate time/weather/gamerules per dim.
	@SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event)
    {
		if(event.getWorld() instanceof ServerWorld)
		{
			if(!((ServerWorld)event.getWorld()).dimension().location().equals(Dimension.OVERWORLD.location()))
			{
				ChunkGenerator chunkGenerator = ((ServerWorld)event.getWorld()).getChunkSource().generator;
				if(chunkGenerator instanceof OTGNoiseChunkGenerator)
				{
					((ServerWorld)event.getWorld()).getServer().overworld().setDayTime(event.getNewTime());
				}
			}
		}
    }
}
