package com.pg85.otg.forge.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.OTG;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

public class BiomeObjectArgument
{
	private static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ") ? "\"" + name + "\"" : name);

	public static CompletableFuture<Suggestions> suggest(CommandContext<CommandSource> context,
			SuggestionsBuilder builder)
	{
		String presetFolderName = context.getArgument("preset", String.class);
		List<String> list;
		// Get global objects if global, else fetch based on preset
		if (presetFolderName.equalsIgnoreCase("global"))
		{
			list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getGlobalObjectNames(OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
		}
		else
		{
			list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(presetFolderName, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
		}
		if (list == null)
		{
			list = new ArrayList<>();
		}
		list = list.stream().map(filterNamesWithSpaces).collect(Collectors.toList());
		return ISuggestionProvider.suggest(list, builder);
	}
}
