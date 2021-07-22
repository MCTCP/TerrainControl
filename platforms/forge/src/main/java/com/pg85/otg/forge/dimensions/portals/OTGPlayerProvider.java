package com.pg85.otg.forge.dimensions.portals;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class OTGPlayerProvider implements ICapabilityProvider
{
	private final OTGPlayer otgPlayer;
	
	public OTGPlayerProvider(OTGPlayer otgPlayer)
	{
		this.otgPlayer = otgPlayer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == OTGCapabilities.OTG_PLAYER_CAPABILITY)
		{
			return LazyOptional.of(() -> (T) this.otgPlayer);
		}
		return LazyOptional.empty();
	}
}
