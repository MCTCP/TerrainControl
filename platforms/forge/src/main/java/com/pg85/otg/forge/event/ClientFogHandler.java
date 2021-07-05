package com.pg85.otg.forge.event;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.network.BiomeSettingSyncWrapper;
import com.pg85.otg.forge.network.OTGClientSyncManager;
import com.pg85.otg.util.helpers.MathHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID_SHORT, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientFogHandler
{

	private static final int BLEND_DISTANCE = 6;

	private static double lastX = Double.MIN_VALUE;
	private static double lastZ = Double.MIN_VALUE;

	private static float[][] fogDensityCache = new float[(BLEND_DISTANCE * 2) + 1][(BLEND_DISTANCE * 2) + 1];

	public ClientFogHandler()
	{
		for (float[] row : fogDensityCache)
		{
			Arrays.fill(row, -1f);
		}
	}

	@SubscribeEvent
	public static void onRenderFog(EntityViewRenderEvent.RenderFogEvent event)
	{
		Entity entity = event.getInfo().getEntity();

		double posX = entity.getX();
		double posZ = entity.getZ();

		int blockX = MathHelper.floor(posX);
		int blockZ = MathHelper.floor(posZ);

		boolean hasMoved = posX != lastX || posZ != lastZ;
		float biomeFogDistance = 0.0F;
		float weightBiomeFog = 0.0f;
		BlockPos.Mutable blockPos = new BlockPos.Mutable(0, 0, 0);
		float fogDensity;
		float densityWeight;
		double differenceX;
		double differenceZ;

		for (int x = -BLEND_DISTANCE; x <= BLEND_DISTANCE; ++x)
		{
			for (int z = -BLEND_DISTANCE; z <= BLEND_DISTANCE; ++z)
			{
				blockPos.set(blockX + x, 0, blockZ + z);

				fogDensity = 1.0f - getFogDensity(x + BLEND_DISTANCE, z + BLEND_DISTANCE, blockPos, hasMoved);
				densityWeight = 1.0f;

				differenceX = getDifference(entity.getX(), blockX, x, BLEND_DISTANCE);
				differenceZ = getDifference(entity.getZ(), blockZ, z, BLEND_DISTANCE);

				if (differenceX >= 0.0f)
				{
					fogDensity *= differenceX;
					densityWeight *= differenceX;
				}

				if (differenceZ >= 0.0f)
				{
					fogDensity *= differenceZ;
					densityWeight *= differenceZ;
				}

				biomeFogDistance += fogDensity;
				weightBiomeFog += densityWeight;

			}
		}

		float weightMixed = (BLEND_DISTANCE * 2) * (BLEND_DISTANCE * 2);
		float weightDefault = weightMixed - weightBiomeFog;

		if (weightDefault < 0.0f)
		{
			weightDefault = 0.0f;
		}

		float fogDistanceAvg = weightBiomeFog == 0.0f ? 0.0f : biomeFogDistance / weightBiomeFog;

		float fogDistance = (biomeFogDistance * 240.0f + event.getFarPlaneDistance() * weightDefault) / weightMixed;
		float fogDistanceScaleBiome = (0.1f * (1.0f - fogDistanceAvg) + 0.75f * fogDistanceAvg);

		float fogDistanceScale = (fogDistanceScaleBiome * weightBiomeFog + 0.75f * weightDefault) / weightMixed;
		float finalFogDistance = Math.min(fogDistance, event.getFarPlaneDistance());

		// set cache values
		lastX = posX;
		lastZ = posZ;

		GL11.glFogf(GL11.GL_FOG_START, finalFogDistance * fogDistanceScale);
		GL11.glFogf(GL11.GL_FOG_END, finalFogDistance);
	}

	// Get the difference between the raw coordinate and block coordinate
	private static double getDifference(double rawCoord, int blockCoord, int pos, int distance)
	{
		if (pos == -distance)
		{
			return 1.0f - (rawCoord - blockCoord);
		} else if (pos == distance)
		{
			return (rawCoord - blockCoord);
		}
		return -1.0f;
	}

	@SuppressWarnings("resource")
	private static float getFogDensity(int x, int z, BlockPos.Mutable blockpos, boolean hasMoved)
	{
		float density = fogDensityCache[x][z];

		if (density != -1f && !hasMoved)
		{
			return density;
		}

		ResourceLocation key = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)
				.getKey(Minecraft.getInstance().level.getBiome(blockpos));

		BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedmap().get(key.toString());

		if (wrapper == null)
			return 0;

		fogDensityCache[x][z] = wrapper.getFogDensity();
		return wrapper.getFogDensity();
	}
}
