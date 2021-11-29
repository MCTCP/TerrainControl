package com.pg85.otg.forge.dimensions.portals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

// Used only for tracking players in OTG portals.
public class OTGPlayer
{
	private final PlayerEntity player;
	private boolean isInOtgPortal = false;
	private String portalColor;
	private int otgPortalTime = 0;
	private float portalAnimTime = 0.0F;
	
	public OTGPlayer(PlayerEntity player)
	{
		this.player = player;
	}	
	
	public static LazyOptional<OTGPlayer> get(PlayerEntity player)
	{
		return player.getCapability(OTGCapabilities.OTG_PLAYER_CAPABILITY);
	}	
	
	public PlayerEntity getPlayer()
	{
		return this.player;
	}

	public void onUpdate()
	{
		handleOTGPortal();
	}

	/**
	 * Updates the portal timer when a player is inside a portal.
	 */
	private void handleOTGPortal()
	{
		if (player.level.isClientSide)
		{
			Minecraft mc = Minecraft.getInstance();
			if (this.isInOtgPortal)
			{
				if (mc.screen != null && !mc.screen.isPauseScreen())
				{
					if (mc.screen instanceof ContainerScreen)
					{
						player.closeContainer();
					}
					mc.setScreen(null);
				}

				if (this.portalAnimTime == 0.0F)
				{
					playPortalSound(mc);
				}
			}
		}

		if (this.isInOtgPortal)
		{
			++this.otgPortalTime;
			if (player.level.isClientSide)
			{
				this.portalAnimTime += 0.0125F;
				if(this.portalAnimTime > 1.0F)
				{
					this.portalAnimTime = 1.0F;
				}
			}
			this.isInOtgPortal = false;
			this.portalColor = null;
		} else {
			if (player.level.isClientSide)
			{
				if (this.portalAnimTime > 0.0F)
				{
					this.portalAnimTime -= 0.05F;
				}

				if (this.portalAnimTime < 0.0F)
				{
					this.portalAnimTime = 0.0F;
				}
			}
			if (this.otgPortalTime > 0)
			{
				this.otgPortalTime -= 4;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void playPortalSound(Minecraft mc)
	{
		mc.getSoundManager().play(SimpleSound.forLocalAmbience(SoundEvents.PORTAL_TRIGGER, this.getPlayer().getRandom().nextFloat() * 0.4F + 0.8F, 0.25F));
	}

	public void setPortal(boolean inPortal, String portalColor)
	{
		this.isInOtgPortal = inPortal;
		this.portalColor = portalColor;
	}
	
	public String getPortalColor()
	{
		return this.portalColor;
	}

	public void setPortalTime(int time)
	{
		this.otgPortalTime = time;
	}

	public int getPortalTime()
	{
		return this.otgPortalTime;
	}
}
