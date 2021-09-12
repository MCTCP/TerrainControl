package com.pg85.otg.paper.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.OTG;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class DataCommand extends BaseCommand
{
	private static final String[] DATA_TYPES = new String[]
			{ "biome", "block", "entity", "sound", "particle", "configured_feature" };
	
	public DataCommand() 
	{
		super("data");
		this.helpMessage = "Dumps various types of game data to files for preset development.";
		this.usage = "/otg data <type>";
		this.detailedHelp = new String[] { 
				"<type>: The type of data to dump.",
				" - biome: All registered biomes.",
				" - block: All registered blocks.",
				" - entity: All registered entities.",
				" - sound: All registered sounds.",
				" - particle: All registered particles.",
				" - configured_feature: All registered configured features (Used to decorate biomes during worldgen)."
			};
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("data")
			.executes(context -> execute(context.getSource(), ""))
				.then(Commands.argument("type", StringArgumentType.word())
					.suggests(this::suggestTypes)
					.executes((context -> execute(context.getSource(), context.getArgument("type", String.class)))
				)
			)
		);
	}

	public int execute(CommandSourceStack source, String type)
	{
		// /otg data music

		Registry<?> registry;

		switch (type.toLowerCase())
		{
			case "biome":
				registry = ((CraftServer) Bukkit.getServer()).getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
				break;
			case "block":
				registry = Registry.BLOCK;
				break;
			case "entity":
				registry = Registry.ENTITY_TYPE;
				break;
			case "sound":
				registry = Registry.SOUND_EVENT;
				break;
			case "particle":
				registry = Registry.PARTICLE_TYPE;
				break;
			case "configured_feature":
				registry = BuiltinRegistries.CONFIGURED_FEATURE;
				break;
			default:
				source.sendSuccess(new TextComponent(getUsage()), false);
				return 0;
		}

		Set<ResourceLocation> set = registry.keySet();
		new Thread(() -> {
			try
			{
				Path root = OTG.getEngine().getOTGRootFolder();
				File folder = new File(root.toString() + File.separator + "output");
				if (!folder.exists())
				{
					folder.mkdirs();
				}

				String fileName = "data-output-" + type + ".txt".toLowerCase();
				File output = new File(folder, fileName);
				FileWriter writer = new FileWriter(output);
				for (ResourceLocation key : set)
				{
					writer.write(key.toString() + "\n");
				}
				writer.close();
				source.sendSuccess(new TextComponent("File exported as " + output.getPath()), true);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}).start();
		return 0;
	}
	
	private CompletableFuture<Suggestions> suggestTypes(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(DATA_TYPES, builder);
	}
}
