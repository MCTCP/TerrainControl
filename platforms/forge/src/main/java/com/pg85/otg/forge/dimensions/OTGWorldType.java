package com.pg85.otg.forge.dimensions;

import com.pg85.otg.constants.Constants;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.world.ForgeWorldType;

public class OTGWorldType extends ForgeWorldType
{
	public OTGWorldType()
	{
		super(new OTGChunkGeneratorFactory());
		this.setRegistryName(new ResourceLocation(Constants.MOD_ID_SHORT));
	}
}
