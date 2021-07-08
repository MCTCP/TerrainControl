package com.pg85.otg.forge.commands;

import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;

import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class BiomeCommand
{
	protected static int showBiome(CommandSource source)
	{
		if (!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}

		String MCBiome = source.getLevel().getBiome(new BlockPos(source.getPosition().x, source.getPosition().y, source.getPosition().z)).getRegistryName().toString();
		String OTGBiome = ((OTGNoiseChunkGenerator)source.getLevel().getChunkSource().generator).getCachedBiomeProvider().getBiomeConfig((int)source.getPosition().x, (int)source.getPosition().z).getName();

		source.sendSuccess(new StringTextComponent("MC says: " + MCBiome + "\r\nOTG says:" + OTGBiome), false);
		return 0;
	}
}
