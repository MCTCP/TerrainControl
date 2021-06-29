package com.pg85.otg.spigot;

import com.pg85.otg.OTGEngine;
import com.pg85.otg.spigot.materials.SpigotMaterials;
import com.pg85.otg.spigot.presets.SpigotPresetLoader;
import com.pg85.otg.spigot.util.SpigotLogger;
import com.pg85.otg.spigot.util.SpigotPluginLoadedChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SpigotEngine extends OTGEngine
{
	private final JavaPlugin plugin;

	protected SpigotEngine (JavaPlugin plugin)
	{
		super(
			new SpigotLogger(),
			plugin.getDataFolder().toPath(),
			new SpigotPluginLoadedChecker(),
			new SpigotPresetLoader(plugin.getDataFolder())
		);
		this.plugin = plugin;
	}

	@Override
	public void onStart()
	{
		SpigotMaterials.init();
		super.onStart();
	}

	@Override
	public File getJarFile()
	{
		String fileName = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
		// URLEncoded string, decode.
		try {
			fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) { }
		
		if(fileName != null)
		{
			File modFile = new File(fileName);
			if(modFile.isFile())
			{
				return modFile;
			}
		}
		return null;
	}
}
