package com.pg85.otg.forge.client;

import com.pg85.otg.forge.network.BiomeSettingSyncWrapper;
import com.pg85.otg.forge.network.OTGClientSyncManager;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.client.renderer.BiomeColors;
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
					return grassColorResolver.m_130045_(biome, posX, posZ); // todo ?
				}

				BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

				if (wrapper == null)
				{
					return grassColorResolver.m_130045_(biome, posX, posZ); // todo ?
				}

				double noise = Biome.BIOME_INFO_NOISE.getValue(posX * 0.0225D, posZ * 0.0225D, false);
				return wrapper.getGrassColorControl().getColor(noise, biome.getGrassColor(posX, posZ));

			};

			BiomeColors.FOLIAGE_COLOR_RESOLVER = (biome, posX, posZ) ->
			{
				ResourceLocation key = Minecraft.getInstance().level.registryAccess()
						.registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
				
				if (key == null)
				{
					return foliageColorResolver.m_130045_(biome, posX, posZ); // todo ?
				}

				BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

				if (wrapper == null)
				{
					return foliageColorResolver.m_130045_(biome, posX, posZ); // todo ?
				}

				double noise = Biome.BIOME_INFO_NOISE.getValue(posX * 0.0225D, posZ * 0.0225D, false);
				return wrapper.getFoliageColorControl().getColor(noise, biome.getFoliageColor());

			};

			BiomeColors.WATER_COLOR_RESOLVER = (biome, posX, posZ) ->
			{
				ResourceLocation key = Minecraft.getInstance().level.registryAccess()
						.registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
				
				if (key == null)
				{
					return waterColorResolver.m_130045_(biome, posX, posZ); // todo ?
				}

				BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

				if (wrapper == null)
				{
					return waterColorResolver.m_130045_(biome, posX, posZ); // todo ?
				}

				double noise = Biome.BIOME_INFO_NOISE.getValue(posX * 0.0225D, posZ * 0.0225D, false);
				return wrapper.getWaterColorControl().getColor(noise, biome.getWaterColor());

			};
		}
	}
}
