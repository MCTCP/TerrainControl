package com.pg85.otg.paper;

import com.pg85.otg.OTGEngine;
import com.pg85.otg.paper.materials.PaperMaterials;
import com.pg85.otg.paper.presets.PaperPresetLoader;
import com.pg85.otg.paper.util.PaperLogger;
import com.pg85.otg.paper.util.PaperPluginLoadedChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class PaperEngine extends OTGEngine
{
	private final JavaPlugin plugin;

	protected PaperEngine(JavaPlugin plugin)
	{
		super(
			new PaperLogger(),
			plugin.getDataFolder().toPath(),
			new PaperPluginLoadedChecker(),
			new PaperPresetLoader(plugin.getDataFolder())
		);
		this.plugin = plugin;
	}

	@Override
	public void onStart()
	{
		PaperMaterials.init();
		super.onStart();
	}

	public JavaPlugin getPlugin()
	{
		return this.plugin;
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
