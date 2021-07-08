package com.pg85.otg.forge.commands.arguments;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.ISuggestionProvider;

public class FlagsArgument implements ArgumentType<String>
{
	private final String[] options;

	private FlagsArgument(String[] options)
	{
		this.options = options;
	}

	public static FlagsArgument with(String... options)
	{
		return new FlagsArgument(options);
	}

	@Override
	public String parse(StringReader reader)
	{
		final String text = reader.getRemaining();
		reader.setCursor(reader.getTotalLength());
		return text;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(options, builder);
	}
}
