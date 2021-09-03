package com.pg85.otg.paper.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;

import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;

import net.minecraft.server.level.ServerLevel;

public class PresetCommand extends BaseCommand
{
	public PresetCommand()
	{
		super("preset");
		this.helpMessage = "Displays information about the current world's preset.";
		this.usage = "/otg preset";
	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Only players can execute this command");
			return true;
		}
		Player player = (Player) sender;
		ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();

		if (!(world.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			sender.sendMessage("OTG is not enabled in this world");
			return true;
		}

		Preset preset = ((OTGNoiseChunkGenerator) world.getChunkSource().getGenerator() ).getPreset();
		sender.sendMessage("Preset: " + preset.getFolderName() + 
				"\nDescription: " + preset.getDescription() + 
				"\nMajor version: " + preset.getMajorVersion());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return Collections.emptyList();
	}
}
