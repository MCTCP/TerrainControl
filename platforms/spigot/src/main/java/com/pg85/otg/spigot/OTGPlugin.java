package com.pg85.otg.spigot;

import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import com.pg85.otg.spigot.commands.OTGCommandExecutor;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.spigot.gen.OTGSpigotChunkGen;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.GeneratorSettingBase;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.IRegistryWritable;
import net.minecraft.server.v1_16_R3.MinecraftKey;

import com.pg85.otg.spigot.util.UnsafeUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.generator.CustomChunkGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;


public class OTGPlugin extends JavaPlugin implements Listener
{
	private static HashMap<String, String> worlds = new HashMap<>();

	@Override
	public void onDisable ()
	{
		// Experimental test to stop crash on server stop for spigot
//		OTG.stopEngine();
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
		if (id == null || id.equals(""))
		{
			id = "Default";
		}
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByName(id);
		if (preset == null)
		{
			OTG.log(LogMarker.WARN, "Could not find preset '"+id+"', did you install it correctly?");
			return null;
		}
		worlds.put(worldName, id);
		return new OTGSpigotChunkGen(preset);
	}

	@EventHandler
	public void onWorldEnable (WorldInitEvent event)
	{
		if (worlds.containsKey(event.getWorld().getName()))
		{
			OTG.log(LogMarker.INFO, "Taking over world " + event.getWorld().getName());

			net.minecraft.server.v1_16_R3.ChunkGenerator generator = ((CraftWorld) event.getWorld()).getHandle().getChunkProvider().getChunkGenerator();
			if (!(generator instanceof CustomChunkGenerator))
			{
				OTG.log(LogMarker.INFO, "Mission failed, we'll get them next time");
				return;
			}
			if (!(event.getWorld().getGenerator() instanceof OTGSpigotChunkGen))
			{
				OTG.log(LogMarker.WARN, "World generator was not an OTG generator, cannot take over, something has gone wrong");
				return;
			}
			// We have a CustomChunkGenerator and a NoiseChunkGenerator
			OTGSpigotChunkGen OTGGen = (OTGSpigotChunkGen) event.getWorld().getGenerator();
			OTGNoiseChunkGenerator OTGDelegate;
			// If generator is null, it has not been initialized yet. Initialize it.
			// The lock is used to avoid the accidental creation of two separate objects, in case
			// of a race condition.
			if (OTGGen.generator == null)
			{
				OTGGen.initLock.lock();
				if (OTGGen.generator == null)
				{
					OTGDelegate = new OTGNoiseChunkGenerator(
							new DimensionConfig(OTGGen.preset.getName()),
							new OTGBiomeProvider(OTGGen.preset.getName(), event.getWorld().getSeed(), false, false, ((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(IRegistry.ay)),
							event.getWorld().getSeed(),
							GeneratorSettingBase::i
					);
					OTGGen.generator = OTGDelegate;
				} else {
					OTGDelegate = OTGGen.generator;
				}
				OTGGen.initLock.unlock();
			} else {
				OTGDelegate = OTGGen.generator;
			}

			UnsafeUtil.setDelegate(generator, OTGDelegate);

			OTG.log(LogMarker.INFO, "Success!");
		}
	}
}
