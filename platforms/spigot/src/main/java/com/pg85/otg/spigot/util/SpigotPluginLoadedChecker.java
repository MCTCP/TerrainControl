package com.pg85.otg.spigot.util;

import com.pg85.otg.util.interfaces.IModLoadedChecker;
import org.bukkit.Bukkit;

public class SpigotPluginLoadedChecker implements IModLoadedChecker
{
	@Override
	public boolean isModLoaded (String mod)
	{
		return Bukkit.getServer().getPluginManager().isPluginEnabled(mod);
	}
}
