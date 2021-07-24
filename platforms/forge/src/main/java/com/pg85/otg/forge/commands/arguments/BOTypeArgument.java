package com.pg85.otg.forge.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.customobject.creator.ObjectType;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BOTypeArgument implements ArgumentType<ObjectType>
{
	private static final SimpleCommandExceptionType NOT_RECOGNIZED = new SimpleCommandExceptionType(new StringTextComponent("Could not recognize object type"));
	private final boolean includeBO2;

	public BOTypeArgument(boolean includeBO2)
	{

		this.includeBO2 = includeBO2;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		Set<String> set = Stream.of(ObjectType.values())
			.map(ObjectType::getType)
			.collect(Collectors.toSet());
		if (!includeBO2)
		{
			set.remove("BO2");
		}
		return ISuggestionProvider.suggest(set, builder);
	}

	/** Parses the string to object type
	 *
	 * @param reader
	 * @return The object type
	 * @throws CommandSyntaxException
	 */
	@Override
	public ObjectType parse(StringReader reader) throws CommandSyntaxException
	{
		String raw = reader.readString();
		try
		{
			return ObjectType.valueOf(raw.toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex)
		{
			throw NOT_RECOGNIZED.create();
		}
	}
}
