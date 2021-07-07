package com.pg85.otg.forge.event;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.network.BiomeSettingSyncWrapper;
import com.pg85.otg.forge.network.OTGClientSyncManager;
import com.pg85.otg.util.helpers.MathHelper;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FogRenderer.FogType;
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

	private static double lastX = Double.MIN_VALUE;
	private static double lastZ = Double.MIN_VALUE;

	private static float[][] fogDensityCache = new float[0][0];

	private static boolean otgDidLastFogRender = false;

	private ClientFogHandler()
	{
		for (float[] row : fogDensityCache)
		{
			Arrays.fill(row, -1f);
		}
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onRenderFog(EntityViewRenderEvent.RenderFogEvent event)
	{
		Entity entity = event.getInfo().getEntity();
		GameSettings settings = Minecraft.getInstance().options;

		if (!(entity instanceof ClientPlayerEntity))
		{
			resetFogDistance(Minecraft.getInstance());
			return;
		}

		ResourceLocation key = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)
				.getKey(Minecraft.getInstance().level.getBiome(event.getInfo().getBlockPosition()));

		BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

		if (wrapper == null)
		{
			if (otgDidLastFogRender)
			{
				resetFogDistance(Minecraft.getInstance());
			}
			return;
		}

		double posX = entity.getX();
		double posZ = entity.getZ();

		int blockX = MathHelper.floor(posX);
		int blockZ = MathHelper.floor(posZ);

		int blendDistance = settings.biomeBlendRadius;
		blendDistance = blendDistance == 0 ? 1 : blendDistance;

		if (fogDensityCache.length < blendDistance * 2 + 1)
		{
			fogDensityCache = new float[(blendDistance * 2) + 1][(blendDistance * 2) + 1];
			for (float[] row : fogDensityCache)
			{
				Arrays.fill(row, -1f);
			}
		}

		boolean hasMoved = posX != lastX || posZ != lastZ;
		float biomeFogDistance = 0.0F;
		float weightBiomeFog = 0.0f;
		BlockPos.Mutable blockPos = new BlockPos.Mutable(0, 0, 0);
		float fogDensity;
		float densityWeight;
		double differenceX;
		double differenceZ;

		for (int x = -blendDistance; x <= blendDistance; ++x)
		{
			for (int z = -blendDistance; z <= blendDistance; ++z)
			{
				blockPos.set(blockX + x, 0, blockZ + z);

				fogDensity = 1.0f - getFogDensity(x + blendDistance, z + blendDistance, blockPos, hasMoved);
				densityWeight = 1.0f;

				differenceX = getDifference(entity.getX(), blockX, x, blendDistance);
				differenceZ = getDifference(entity.getZ(), blockZ, z, blendDistance);

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

		float weightMixed = (blendDistance * 2f) * (blendDistance * 2f);
		float weightDefault = weightMixed - weightBiomeFog;

		if (weightDefault < 0.0f)
		{
			weightDefault = 0.0f;
		}

		float fogDistanceAvg = weightBiomeFog == 0.0f ? 0.0f : biomeFogDistance / weightBiomeFog;

		float fogDistance = (biomeFogDistance * 520f + event.getFarPlaneDistance() * weightDefault) / weightMixed;
		float fogDistanceScaleBiome = (0.1f * (1.0f - fogDistanceAvg) + 0.75f * fogDistanceAvg);
		float fogDistanceScale = (fogDistanceScaleBiome * weightBiomeFog + 1f * weightDefault) / weightMixed;

		float finalFogDistance = Math.min(fogDistance, event.getFarPlaneDistance());
		float fogStart = event.getType() == FogType.FOG_SKY ? 0.0f : finalFogDistance * fogDistanceScale;
		
		// set cache values
		lastX = posX;
		lastZ = posZ;

		otgDidLastFogRender = true;
		
		GL11.glFogf(GL11.GL_FOG_START, fogStart);
		GL11.glFogf(GL11.GL_FOG_END, finalFogDistance);
	}

	private static void resetFogDistance(Minecraft minecraft)
	{
		if (otgDidLastFogRender)
		{
			// Non-OTG dims and OTG dims without fog settings don't properly reset
			// the fog start and end when players teleport between dimensions.
			// Reset the fog distance here.
			otgDidLastFogRender = false;
			float farPlaneDistance = (float) (minecraft.options.renderDistance * 16);

			GL11.glFogf(GL11.GL_FOG_START, farPlaneDistance * 0.75F);
			GL11.glFogf(GL11.GL_FOG_END, farPlaneDistance);

			for (float[] row : fogDensityCache)
			{
				Arrays.fill(row, -1f);
			}
		}
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

		BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

		if (wrapper == null)
			return 0;

		fogDensityCache[x][z] = wrapper.getFogDensity();
		return wrapper.getFogDensity();
	}
}
