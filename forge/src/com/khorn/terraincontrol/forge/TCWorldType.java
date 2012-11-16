package com.khorn.terraincontrol.forge;

import java.io.File;
import java.util.Random;

import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.ObjectsStore;

import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ModLoader;
import net.minecraft.src.SaveHandler;
import net.minecraft.src.World;
import net.minecraft.src.WorldChunkManager;
import net.minecraft.src.WorldClient;
import net.minecraft.src.WorldType;
import cpw.mods.fml.common.IWorldGenerator;

public class TCWorldType extends WorldType
{
	public SingleWorld TCWorld;
	private TCPlugin plugin;

	public TCWorldType(TCPlugin plugin, int paramInt, String paramString)
	{
		super(paramInt, paramString);
		this.plugin = plugin;
	}

	@Override
	public WorldChunkManager getChunkManager(World world)
	{
		if (world instanceof WorldClient)
			return super.getChunkManager(world);

		this.TCWorld = new SingleWorld(world.getSaveHandler().getSaveDirectoryName());

		File worldDirectory = new File(plugin.terrainControlDirectory, "worlds" + File.separator + world.getSaveHandler().getSaveDirectoryName());

		if (!worldDirectory.exists())
		{
			System.out.println("TerrainControl: settings does not exist, creating defaults");

			if (!worldDirectory.mkdirs())
				System.out.println("TerrainControl: cant create folder " + worldDirectory.getAbsolutePath());
		}

		

		WorldConfig config = new WorldConfig(worldDirectory, TCWorld, false);
		this.TCWorld.setSettings(config);
		this.TCWorld.Init(world);

		WorldChunkManager ChunkManager = null;

		switch (this.TCWorld.getSettings().ModeBiome)
		{
			case FromImage:
			case Normal:
				ChunkManager = new BiomeManager(this.TCWorld);
				this.TCWorld.setBiomeManager((BiomeManager) ChunkManager);
				break;
			case OldGenerator:
				ChunkManager = new BiomeManagerOld(this.TCWorld);
				this.TCWorld.setOldBiomeManager((BiomeManagerOld) ChunkManager);
				break;
			case Default:
				ChunkManager = super.getChunkManager(world);
				break;
		}

		return ChunkManager;
	}

	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions)
	{
		if (this.TCWorld.getSettings().ModeTerrain != WorldConfig.TerrainMode.Default)
		{
			return this.TCWorld.getChunkGenerator();
		} else
			return super.getChunkGenerator(world, generatorOptions);
	}

}
