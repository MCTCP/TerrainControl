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
		source.sendSuccess(new StringTextComponent(
				source.getLevel().getBiome(
					new BlockPos(source.getPosition().x, source.getPosition().y, source.getPosition().z)).toString())
			, false);
		return 0;
	}
}
