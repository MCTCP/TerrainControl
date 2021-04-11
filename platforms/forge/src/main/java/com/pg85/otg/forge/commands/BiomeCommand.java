package com.pg85.otg.forge.commands;

import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class BiomeCommand
{
	protected static int showBiome(CommandSource source)
	{
		if (!(source.getWorld().getChunkProvider().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendFeedback(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}
		source.sendFeedback(new StringTextComponent(
				source.getWorld().getBiome(
					new BlockPos(source.getPos().x, source.getPos().y, source.getPos().z)).toString())
			, false);
		return 0;
	}
}
