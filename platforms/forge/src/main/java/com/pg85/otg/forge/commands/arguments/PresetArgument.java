package com.pg85.otg.forge.commands.arguments;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.OTG;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

public class PresetArgument
{
	private static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ")
			? "\"" + name + "\""
			: name);

	public static CompletableFuture<Suggestions> suggest(CommandContext<CommandSource> context,
			SuggestionsBuilder builder, boolean global)
	{
		// TODO: Should this include shortnames?
		Set<String> set = OTG.getEngine().getPresetLoader().getAllPresetFolderNames().stream()
				.map(filterNamesWithSpaces).collect(Collectors.toSet());
		if (global)
			set.add("global");
		return ISuggestionProvider.suggest(set, builder);
	}
}
