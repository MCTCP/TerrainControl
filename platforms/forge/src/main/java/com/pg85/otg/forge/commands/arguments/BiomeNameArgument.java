package com.pg85.otg.forge.commands.arguments;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.constants.Constants;

import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeNameArgument implements ArgumentType<String>
{
	List<String> biomes = ForgeRegistries.BIOMES.getKeys().stream()
			.filter(key -> key.getNamespace().equals(Constants.MOD_ID_SHORT))
			.map(ResourceLocation::getPath).collect(Collectors.toList());
	
	public static BiomeNameArgument create() {
		return new BiomeNameArgument();
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException
	{
		return reader.readString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(biomes, builder);
	}
}
