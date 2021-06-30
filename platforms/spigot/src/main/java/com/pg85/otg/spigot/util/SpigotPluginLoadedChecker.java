package com.pg85.otg.spigot.util;

import org.bukkit.Bukkit;

import com.pg85.otg.interfaces.IModLoadedChecker;

public class SpigotPluginLoadedChecker implements IModLoadedChecker
{
	@Override
	public boolean isModLoaded (String mod)
	{
		return Bukkit.getServer().getPluginManager().isPluginEnabled(mod);
	}
}
