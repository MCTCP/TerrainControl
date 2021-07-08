package com.pg85.otg.forge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;

public interface BaseCommand
{
	public void build(LiteralArgumentBuilder<CommandSource> builder);
}
