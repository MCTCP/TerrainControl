package com.pg85.otg.paper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.core.OTG;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class FlushCommand extends BaseCommand
{	
	public FlushCommand() 
	{
		super("flush");
		this.helpMessage = "Clears all loaded objects, forcing them to be reloaded from disk.";
		this.usage = "/otg flush";
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("flush")
			.executes(context -> flushCache(context.getSource()))
		);
	}

	@Override
	public String getPermission() {
		return "otg.cmd.flush";
	}
	
	protected int flushCache(CommandSourceStack source)
	{
		if (!source.hasPermission(2, getPermission())) {
			source.sendSuccess(new TextComponent("\u00a7cPermission denied!"), false);
			return 0;
		}
		if (!(source.getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new TextComponent("OTG is not enabled in this world"), false);
			return 0;
		}

		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Unloading BO2/BO3/BO4 files");
		OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles();
		source.sendSuccess(new TextComponent("Objects unloaded."), false);
		return 0;
	}
}
