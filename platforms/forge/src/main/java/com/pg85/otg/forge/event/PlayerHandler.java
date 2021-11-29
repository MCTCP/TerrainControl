package com.pg85.otg.forge.event;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.dimensions.portals.OTGPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// Only used to track players in otg portals atm.
@EventBusSubscriber(modid = Constants.MOD_ID_SHORT)
public class PlayerHandler
{
	@SubscribeEvent
	public static void onPlayerUpdate(LivingUpdateEvent event)
	{
		if (event.getEntityLiving() instanceof Player)
		{
			OTGPlayer.get((Player) event.getEntityLiving()).ifPresent(OTGPlayer::onUpdate);
		}
	}
}
