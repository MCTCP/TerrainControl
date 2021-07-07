package com.pg85.otg.forge.client;

import com.pg85.otg.forge.network.BiomeSettingSyncWrapper;
import com.pg85.otg.forge.network.OTGClientSyncManager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColors;
import net.minecraft.world.level.ColorResolver;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class MultipleColorHandler
{

	@SuppressWarnings("resource")
	public static void setup()
	{
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ColorResolver grassColorResolver = BiomeColors.GRASS_COLOR_RESOLVER;
			ColorResolver foliageColorResolver = BiomeColors.FOLIAGE_COLOR_RESOLVER;
			ColorResolver waterColorResolver = BiomeColors.WATER_COLOR_RESOLVER;

			BiomeColors.GRASS_COLOR_RESOLVER = (biome, posX, posZ) ->
			{
				ResourceLocation key = Minecraft.getInstance().level.registryAccess()
						.registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);

				// This shouldn't be null, but sometimes it is?
				if (key == null)
				{
					return grassColorResolver.getColor(biome, posX, posZ);
				}

				BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

				if (wrapper == null)
				{
					return grassColorResolver.getColor(biome, posX, posZ);
				}

				int grassColor = wrapper.getGrassColor() == 0xFFFFFF ? biome.getGrassColor(posX, posZ)
						: wrapper.getGrassColor();
				double noise = Biome.BIOME_INFO_NOISE.getValue(posX * 0.0225D, posZ * 0.0225D, false);
				return wrapper.getGrassColorControl().getColor(noise, grassColor);

			};

			BiomeColors.FOLIAGE_COLOR_RESOLVER = (biome, posX, posZ) ->
			{
				ResourceLocation key = Minecraft.getInstance().level.registryAccess()
						.registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
				
				if (key == null)
				{
					return foliageColorResolver.getColor(biome, posX, posZ);
				}

				BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

				if (wrapper == null)
				{
					return foliageColorResolver.getColor(biome, posX, posZ);
				}

				int foliageColor = wrapper.getFoliageColor() == 0xFFFFFF ? biome.getFoliageColor()
						: wrapper.getFoliageColor();
				double noise = Biome.BIOME_INFO_NOISE.getValue(posX * 0.0225D, posZ * 0.0225D, false);
				return wrapper.getFoliageColorControl().getColor(noise, foliageColor);

			};

			BiomeColors.WATER_COLOR_RESOLVER = (biome, posX, posZ) ->
			{
				ResourceLocation key = Minecraft.getInstance().level.registryAccess()
						.registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
				
				if (key == null)
				{
					return waterColorResolver.getColor(biome, posX, posZ);
				}

				BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

				if (wrapper == null)
				{
					return waterColorResolver.getColor(biome, posX, posZ);
				}

				int waterColor = wrapper.getWaterColor() == 0xFFFFFF ? biome.getWaterColor() : wrapper.getWaterColor();
				double noise = Biome.BIOME_INFO_NOISE.getValue(posX * 0.0225D, posZ * 0.0225D, false);
				return wrapper.getWaterColorControl().getColor(noise, waterColor);

			};
		}
	}
}
