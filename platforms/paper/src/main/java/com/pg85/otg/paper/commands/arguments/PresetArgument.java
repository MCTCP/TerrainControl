package com.pg85.otg.paper.commands.arguments;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.core.OTG;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class PresetArgument
{
	private static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ")
			? "\"" + name + "\""
			: name);

	public static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder, boolean global)
	{
		// TODO: Should this include shortnames?
		Set<String> set = OTG.getEngine().getPresetLoader().getAllPresetFolderNames().stream()
				.map(filterNamesWithSpaces).collect(Collectors.toSet());
		if (global)
			set.add("global");
		return SharedSuggestionProvider.suggest(set, builder);
	}
}
