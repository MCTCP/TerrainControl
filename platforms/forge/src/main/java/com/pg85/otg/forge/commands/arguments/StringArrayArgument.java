package com.pg85.otg.forge.commands.arguments;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.ISuggestionProvider;

public class StringArrayArgument implements ArgumentType<String>
{
	private final String[] options;

	private StringArrayArgument(String[] options)
	{
		this.options = options;
	}

	public static StringArrayArgument with(String... options)
	{
		return new StringArrayArgument(options);
	}
	
	public static StringArrayArgument with(List<String> options)
	{
		return new StringArrayArgument(options.toArray(new String[0]));
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException
	{
		return reader.readString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(options, builder);
	}
}
