package com.pg85.otg.spigot;

import com.pg85.otg.OTG;
import org.bukkit.plugin.java.JavaPlugin;

public class OTGPlugin extends JavaPlugin
{
	@Override
	public void onDisable ()
	{
		OTG.stopEngine();
	}

	@Override
	public void onEnable ()
	{
		OTG.startEngine(new SpigotEngine(this));
	}
}
