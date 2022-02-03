package com.pg85.otg.paper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import com.pg85.otg.paper.util.ObfuscationHelper;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.generator.CustomChunkGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.paper.biome.OTGBiomeProvider;
import com.pg85.otg.paper.commands.OTGCommandExecutor;
import com.pg85.otg.paper.events.OTGHandler;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.paper.gen.OTGPaperChunkGen;
import com.pg85.otg.paper.networking.NetworkingListener;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;


public class OTGPlugin extends JavaPlugin implements Listener
{
	private static final ReentrantLock initLock = new ReentrantLock();
	private static final HashMap<String, String> worlds = new HashMap<>();
	private static final HashSet<String> processedWorlds = new HashSet<>();

	@SuppressWarnings("unused")
	private OTGHandler handler;
	private static Field field;

	public static OTGPlugin plugin;

	static
	{
		try
		{
			field = CustomChunkGenerator.class.getDeclaredField("delegate");
			field.setAccessible(true);
		} catch (ReflectiveOperationException ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void onDisable ()
	{
		// Experimental test to stop crash on server stop for spigot
		// OTG.stopEngine();
	}

	@Override
	public void onEnable ()
	{
		plugin = this;
		Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(Constants.MOD_ID_SHORT, "default"), OTGBiomeProvider.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(Constants.MOD_ID_SHORT, "default"), OTGNoiseChunkGenerator.CODEC);

		OTG.startEngine(new PaperEngine(this));
		
		OTGCommandExecutor.registerArguments();
		OTGCommandExecutor.register(((CraftServer)Bukkit.getServer()).getServer().vanillaCommandDispatcher.getDispatcher());
		// Does this go here?
		OTG.getEngine().getPresetLoader().registerBiomes();

		WritableRegistry<Biome> biome_registry = ((CraftServer) Bukkit.getServer()).getServer().registryAccess().ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
		int i = 0;

		if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
		{
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "-----------------");
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Registered biomes:");
			for (Biome biomeBase : biome_registry)
			{
				OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, (i++) + ": " + biomeBase.toString());
			}
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "-----------------");
		}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		this.handler = new OTGHandler(this);	
		Bukkit.getPluginManager().registerEvents(new NetworkingListener(this), this);
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator (String worldName, String id)
	{
		if (id == null || id.equals(""))
		{
			id = "Default";
		}
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(id);
		if (preset == null)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Could not find preset '" + id + "', did you install it correctly?");
			return null;
		}
		worlds.put(worldName, id);
		return new OTGPaperChunkGen(preset);
	}

	@EventHandler
	public void onWorldEnable (WorldInitEvent event)
	{
		if (worlds.containsKey(event.getWorld().getName()))
		{
			// Most likely no longer needed, but keeping it just in case. The lock keeps it from doing it double anyway.
			injectInternalGenerator(event.getWorld());
		}
	}

	public void injectInternalGenerator(World world)
	{
		initLock.lock();
		if (processedWorlds.contains(world.getName()))
		{
			// We have already processed this world, return
			return;
		}

		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Taking over world " + world.getName());
		ServerLevel serverWorld = ((CraftWorld) world).getHandle();

		net.minecraft.world.level.chunk.ChunkGenerator generator = serverWorld.getChunkSource().getGenerator();
		if (!(generator instanceof CustomChunkGenerator))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Mission failed, we'll get them next time");
			return;
		}
		if (!(world.getGenerator() instanceof OTGPaperChunkGen))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "World generator was not an OTG generator, cannot take over, something has gone wrong");
			return;
		}
		// We have a CustomChunkGenerator and a NoiseChunkGenerator
		OTGPaperChunkGen OTGGen = (OTGPaperChunkGen) world.getGenerator();
		OTGNoiseChunkGenerator OTGDelegate;
		// If generator is null, it has not been initialized yet. Initialize it.
		// The lock is used to avoid the accidental creation of two separate objects, in case
		// of a race condition.
		if (OTGGen.generator == null)
		{
			OTGDelegate = new OTGNoiseChunkGenerator(
				OTGGen.getPreset().getFolderName(),
				new OTGBiomeProvider(OTGGen.getPreset().getFolderName(), world.getSeed(), false, false, ((CraftServer) Bukkit.getServer()).getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)),
				world.getSeed(),
				NoiseGeneratorSettings::bootstrap
			);
		} else {
			OTGDelegate = OTGGen.generator;
		}

		try
		{
			
			Field finalGenerator = ObfuscationHelper.getField(ChunkMap.class, "generator", "t");
			finalGenerator.setAccessible(true);

			finalGenerator.set(serverWorld.getChunkSource().chunkMap, OTGDelegate);

			/*Field pcmGen = ObfuscationHelper.getField(ChunkMap.class, "generator", "r");
			pcmGen.setAccessible(true);

			pcmGen.set(serverWorld.getChunkSource().chunkMap, OTGDelegate);*/
		} catch (ReflectiveOperationException ex)
		{
			ex.printStackTrace();
		}

		if (OTGGen.generator == null)
		{
			OTGGen.generator = OTGDelegate;
		}

		// Spigot may have started generating - we gotta regen if so

		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Success!");

		processedWorlds.add(world.getName());

		initLock.unlock();
	}
}
