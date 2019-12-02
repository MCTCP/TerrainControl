package com.pg85.otg.forge.events.client;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.forge.ForgeEngine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Adapted from Minecraft and Biomes O' Plenty.
 * 
 * @see net.minecraft.client.renderer.RenderGlobal
 * @see <a href=
 *      "https://github.com/Glitchfiend/BiomesOPlenty/blob/BOP-1.12.x-7.0.x/src/main/java/biomesoplenty/common/handler/FogEventHandler.java">https://github.com/Glitchfiend/BiomesOPlenty/blob/BOP-1.12.x-7.0.x/src/main/java/biomesoplenty/common/handler/FogEventHandler.java</a>
 */
public class ClientFogHandler {

	// Max blend distance in ForgeModContainer.blendRanges
	private final int MAX_BLEND_DISTANCE = 34;
	private short[][] biomeCache = new short[(MAX_BLEND_DISTANCE * 2) + 1][(MAX_BLEND_DISTANCE * 2) + 1];
	private double lastX, lastZ;

	public ClientFogHandler() {
		for (short[] row : biomeCache) {
			Arrays.fill(row, (short) -1);
		}
	}

	// Handle the fog color
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGetFogColor(EntityViewRenderEvent.FogColors event) {
		if (!(event.getEntity() instanceof EntityPlayer))
			return;

		LocalWorld world = ((ForgeEngine) OTG.getEngine()).getWorld(event.getEntity().world);

		// Not an OTG world
		if (world == null)
			return;

		Vec3d fogColor = blendFogColors(event.getEntity().world, (EntityLivingBase) event.getEntity(), event.getRed(),
				event.getGreen(), event.getBlue(), event.getRenderPartialTicks());

		event.setRed((float) fogColor.x);
		event.setGreen((float) fogColor.y);
		event.setBlue((float) fogColor.z);
	}

	// Handle the fog distance blending
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderFog(EntityViewRenderEvent.RenderFogEvent event) {
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		int[] ranges = ForgeModContainer.blendRanges;
		int blendDistance = 6;

		if (settings.fancyGraphics && settings.renderDistanceChunks >= 0
				&& settings.renderDistanceChunks < ranges.length) {
			blendDistance = ranges[settings.renderDistanceChunks];
		}

		Entity entity = event.getEntity();
		World mcworld = entity.world;

		int blockX = MathHelper.floor(entity.posX);
		int blockZ = MathHelper.floor(entity.posZ);

		boolean hasMoved = entity.posX != lastX || entity.posZ != lastZ;

		LocalWorld world = ((ForgeEngine) OTG.getEngine()).getWorld(mcworld);

		if (world == null)
			return;

		float biomeFogDistance = 0F;
		float weightBiomeFog = 0;
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(0, 0, 0);

		for (int x = -blendDistance; x <= blendDistance; ++x) {
			for (int z = -blendDistance; z <= blendDistance; ++z) {
				blockPos.setPos(blockX + x, 0, blockZ + z);
				BiomeConfig config = getBiomeConfig(world, x + blendDistance, z + blendDistance, blockPos, hasMoved);

				if (config.fogColor != 0x000000) {
					float fogDensity = 1f - config.fogDensity;
					float densityWeight = 1;

					double differenceX = getDifference(entity.posX, blockX, x, blendDistance);

					double differenceZ = getDifference(entity.posZ, blockZ, z, blendDistance);

					if (differenceX != -1) {
						fogDensity *= differenceX;
						densityWeight *= differenceX;
					}

					if (differenceZ != -1) {
						fogDensity *= differenceZ;
						densityWeight *= differenceZ;
					}

					biomeFogDistance += fogDensity;
					weightBiomeFog += densityWeight;
				}
			}
		}

		float weightMixed = (blendDistance * 2) * (blendDistance * 2);
		float weightDefault = weightMixed - weightBiomeFog;

		float fogDistanceAvg = (weightBiomeFog == 0) ? 0 : biomeFogDistance / weightBiomeFog;

		float fogDistance = (biomeFogDistance * 240 + event.getFarPlaneDistance() * weightDefault) / weightMixed;
		float fogDistanceScaleBiome = (0.1f * (1 - fogDistanceAvg) + 0.75f * fogDistanceAvg);
		float fogDistanceScale = (fogDistanceScaleBiome * weightBiomeFog + 0.75f * weightDefault) / weightMixed;

		float finalFogDistance = Math.min(fogDistance, event.getFarPlaneDistance());

		lastX = entity.posX;
		lastZ = entity.posZ;

		// Render the fog
		if (event.getFogMode() < 0) {
			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, finalFogDistance);
		} else {
			GL11.glFogf(GL11.GL_FOG_START, finalFogDistance * fogDistanceScale);
			GL11.glFogf(GL11.GL_FOG_END, finalFogDistance);
		}
	}

	// Get the difference between the raw coordinate and block coordinate
	private double getDifference(double rawCoord, int blockCoord, int pos, int distance) {
		if (pos == -distance) {
			return 1 - (rawCoord - blockCoord);
		} else if (pos == distance) {
			return (rawCoord - blockCoord);
		}
		return -1;
	}

	// Blend the fog color
	private Vec3d blendFogColors(World mcworld, EntityLivingBase entity, float red, float green, float blue,
			double renderPartialTicks) {

		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		int[] ranges = ForgeModContainer.blendRanges;
		int blendDistance = 6;

		if (settings.fancyGraphics && settings.renderDistanceChunks >= 0
				&& settings.renderDistanceChunks < ranges.length) {
			blendDistance = ranges[settings.renderDistanceChunks];
		}

		LocalWorld world = ((ForgeEngine) OTG.getEngine()).getWorld(mcworld);
		int blockX = (int) Math.floor(entity.posX);
		int blockZ = (int) Math.floor(entity.posZ);

		boolean hasMoved = entity.posX != lastX || entity.posZ != lastZ;

		double biomeFogRed = 0;
		double biomeFogGreen = 0;
		double biomeFogBlue = 0;
		double biomeFogWeight = 0;

		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(blockX, 0, blockZ);

		BiomeConfig biomeConfig = getBiomeConfig(world, 0, 0, blockPos, hasMoved);

		for (int x = -blendDistance; x <= blendDistance; ++x) {
			for (int z = -blendDistance; z <= blendDistance; ++z) {
				blockPos.setPos(blockX + x, 0, blockZ + z);
				BiomeConfig config = getBiomeConfig(world, x + blendDistance, z + blendDistance, blockPos, hasMoved);
				int fogColour = config.fogColor;

				if (fogColour != 0x000000) {

					double fogRed = (fogColour & 0xFF0000) >> 16;
					double fogGreen = (fogColour & 0x00FF00) >> 8;
					double fogBlue = fogColour & 0x0000FF;
					float fogWeight = 1;

					double differenceX = getDifference(entity.posX, blockX, x, blendDistance);

					double differenceZ = getDifference(entity.posZ, blockZ, z, blendDistance);

					if (differenceX != -1) {
						fogRed *= differenceX;
						fogGreen *= differenceX;
						fogBlue *= differenceX;
						fogWeight *= differenceX;
					}

					if (differenceZ != -1) {
						fogRed *= differenceZ;
						fogGreen *= differenceZ;
						fogBlue *= differenceZ;
						fogWeight *= differenceZ;
					}

					biomeFogRed += fogRed;
					biomeFogGreen += fogGreen;
					biomeFogBlue += fogBlue;
					biomeFogWeight += fogWeight;

				}
			}
		}

		if (biomeFogWeight == 0 || blendDistance == 0) {
			return new Vec3d(red, green, blue);
		}

		// Convert integer to float from 0-1
		biomeFogRed /= 255f;
		biomeFogGreen /= 255f;
		biomeFogBlue /= 255f;

		// Scale color based on world time
		float baseScale = 1f;

		float time = MathHelper.clamp(
				MathHelper.cos(mcworld.getCelestialAngle((float) renderPartialTicks) * (float) Math.PI * 2.0F) * 2.0F
						+ 0.5F,
				0, 1);

		baseScale *= 1 - (1 - time) * biomeConfig.fogTimeWeight;

		// Adjust based on weather
		float rainStrength = mcworld.getRainStrength((float) renderPartialTicks);
		float thunderStrength = mcworld.getThunderStrength((float) renderPartialTicks);

		if (thunderStrength > 0) {
			baseScale *= Math.min(1 - thunderStrength * biomeConfig.fogThunderWeight,
					1 - rainStrength * biomeConfig.fogRainWeight);
		} else if (rainStrength > 0) {
			baseScale *= 1 - rainStrength * biomeConfig.fogRainWeight;
		}

		biomeFogRed *= baseScale / biomeFogWeight;
		biomeFogGreen *= baseScale / biomeFogWeight;
		biomeFogBlue *= baseScale / biomeFogWeight;

		// Mix default fog and our fog
		double weightMixed = (blendDistance * 2) * (blendDistance * 2);
		double weightDefault = weightMixed - biomeFogWeight;

		double fogRed = (biomeFogRed * biomeFogWeight + red * weightDefault) / weightMixed;
		double fogGreen = (biomeFogGreen * biomeFogWeight + green * weightDefault) / weightMixed;
		double fogBlue = (biomeFogBlue * biomeFogWeight + blue * weightDefault) / weightMixed;

		return new Vec3d(fogRed, fogGreen, fogBlue);
	}

	// Get the biome config from the cache or freshly from the world if needed
	private BiomeConfig getBiomeConfig(LocalWorld world, int x, int z, MutableBlockPos blockPos, boolean hasMoved) {
		short cachedId = biomeCache[x][z];
		if (cachedId != -1 && !hasMoved) {
			return OTG.getEngine().getOTGBiomeIds(world.getName())[cachedId];
		} else {
			LocalBiome biome = world.getBiome(blockPos.getX(), blockPos.getZ());
			biomeCache[x][z] = (short) biome.getIds().getOTGBiomeId();
			return biome.getBiomeConfig();
		}
	}
}
