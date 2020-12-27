package com.pg85.otg.spigot;

import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import com.pg85.otg.spigot.commands.OTGCommandExecutor;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.spigot.gen.SpigotChunkGenerator;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.generator.CustomChunkGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;


public class OTGPlugin extends JavaPlugin implements Listener
{
	private static HashMap<String, String> worlds = new HashMap<>();

	@Override
	public void onDisable ()
	{
		OTG.stopEngine();
	}

	@Override
	public void onEnable ()
	{
		IRegistry.a(IRegistry.BIOME_SOURCE, new MinecraftKey(Constants.MOD_ID_SHORT, "default"), OTGBiomeProvider.CODEC);
		IRegistry.a(IRegistry.CHUNK_GENERATOR, new MinecraftKey(Constants.MOD_ID_SHORT, "default"), OTGNoiseChunkGenerator.CODEC);

		OTG.startEngine(new SpigotEngine(this));
		Bukkit.getPluginCommand("OTG").setExecutor(new OTGCommandExecutor());
		// Does this go here?
		OTG.getEngine().getPresetLoader().registerBiomes();

		/*
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByName("biome_bundle");

		OTGNoiseChunkGenerator OTGChunkGen = new OTGNoiseChunkGenerator(
				new DimensionConfig(preset.getName()),
				new OTGBiomeProvider(preset.getName(), 0, false, false,
						((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(IRegistry.ay)),
				0,
				GeneratorSettingBase::i);
		 */
		IRegistryWritable<BiomeBase> biome_registry = ((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(IRegistry.ay);
		int i = 0;

		OTG.log(LogMarker.TRACE, "-----------------");
		OTG.log(LogMarker.TRACE, "Registered biomes:");
		for (BiomeBase biomeBase : biome_registry)
		{
			OTG.log(LogMarker.TRACE, (i++) + ": " + biomeBase.toString());
		}
		OTG.log(LogMarker.TRACE, "-----------------");
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator (String worldName, String id)
	{
		worlds.put(worldName, id);
		return new SpigotChunkGenerator(worldName, id);
	}

	@EventHandler
	public void onWorldEnable(WorldInitEvent event)
	{
		if (worlds.containsKey(event.getWorld().getName()))
		{
			OTG.log(LogMarker.INFO, "Taking over world "+event.getWorld().getName());

			net.minecraft.server.v1_16_R3.ChunkGenerator generator = ((CraftWorld) event.getWorld()).getHandle().getChunkProvider().getChunkGenerator();
			if (!(generator instanceof CustomChunkGenerator))
			{
				OTG.log(LogMarker.INFO, "Mission failed, we'll get them next time");
				return;
			}

			OTGNoiseChunkGenerator infiltrator = ((SpigotChunkGenerator)event.getWorld().getGenerator()).generator;

			Field field = null;
			Field modifiers = null;
			try
			{
				// Get the delegate chunkgen
				field = CustomChunkGenerator.class.getDeclaredField("delegate");
				// Set it to public
				field.setAccessible(true);
				// Make it not final
				modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			}
			catch (NoSuchFieldException | IllegalAccessException e)
			{
				e.printStackTrace();
				return;
			}

			/*
			Preset preset = OTG.getEngine().getPresetLoader().getPresetByName(worlds.get(event.getWorld().getName()));
			OTGNoiseChunkGenerator OTGChunkGen = new OTGNoiseChunkGenerator(
					new DimensionConfig(preset.getName()),
					new OTGBiomeProvider(preset.getName(), 0, false, false,
							((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(IRegistry.ay)),
					0,
					GeneratorSettingBase::i);

			 */
			try
			{
				field.set(generator, infiltrator);

			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
				return;
			}
			OTG.log(LogMarker.INFO, "Success!");
		}
	}
}
