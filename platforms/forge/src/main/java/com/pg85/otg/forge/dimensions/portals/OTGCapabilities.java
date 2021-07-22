package com.pg85.otg.forge.dimensions.portals;

import com.pg85.otg.constants.Constants;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

public class OTGCapabilities
{
	@CapabilityInject(OTGPlayer.class)
	public static final Capability<OTGPlayer> OTG_PLAYER_CAPABILITY = null;
	
	public static void register()
	{
		CapabilityManager.INSTANCE.register(
			OTGPlayer.class, 
			// Dummy storage, since we're not actually persisting anything.
			new Capability.IStorage<OTGPlayer>()
	        {
				@Override
				public INBT writeNBT(Capability<OTGPlayer> capability, OTGPlayer instance, Direction side) { return new CompoundNBT(); }
				
				@Override
				public void readNBT(Capability<OTGPlayer> capability, OTGPlayer instance, Direction side, INBT nbt) { }
	        }, 
	        () -> null
		);
	}

	@EventBusSubscriber(modid = Constants.MOD_ID_SHORT)
	public static class Registration
	{
		@SubscribeEvent
		public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
		{
			if (event.getObject() instanceof PlayerEntity)
			{
				event.addCapability(new ResourceLocation(Constants.MOD_ID_SHORT, "otg_player"), new OTGPlayerProvider(new OTGPlayer((PlayerEntity) event.getObject())));
			}
		}
	}
}
