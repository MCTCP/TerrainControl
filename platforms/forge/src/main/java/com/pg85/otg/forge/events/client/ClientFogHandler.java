package com.pg85.otg.forge.events.client;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;
import com.pg85.otg.forge.world.ForgeWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
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
public class ClientFogHandler
{
	// Max blend distance in ForgeModContainer.blendRanges
	private final int MAX_BLEND_DISTANCE = 34;
	private short[][] biomeCache = new short[(MAX_BLEND_DISTANCE * 2) + 1][(MAX_BLEND_DISTANCE * 2) + 1];
	private double lastX, lastZ;	

	public ClientFogHandler()
	{
		for (short[] row : biomeCache)
		{
			Arrays.fill(row, (short) -1);
		}
	}

	// Handle the fog color
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGetFogColor(EntityViewRenderEvent.FogColors event)
	{
		if (!(event.getEntity() instanceof EntityPlayer) || !(event.getEntity().getEntityWorld().provider instanceof OTGWorldProvider))
		{
			// Not a player or OTG world
			return;
		}
		
		ForgeWorld forgeWorld = ((ForgeEngine) OTG.getEngine()).getWorld(event.getEntity().world);
		if (forgeWorld == null)
		{
			return;
		}
		
		int blockX = (int) Math.floor(event.getEntity().posX);
		int blockZ = (int) Math.floor(event.getEntity().posZ);

		boolean hasMoved = event.getEntity().posX != lastX || event.getEntity().posZ != lastZ;
				
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(blockX, 0, blockZ);
		BiomeConfig biomeConfig = getBiomeConfig(forgeWorld, 0, 0, blockPos, hasMoved);
		if(biomeConfig != null)
		{
			Vec3d fogColor = blendFogColors(forgeWorld, biomeConfig, (EntityLivingBase) event.getEntity(), event.getRed(), event.getGreen(), event.getBlue(), event.getRenderPartialTicks());
			
			if(fogColor != null)
			{
				event.setRed((float) fogColor.x);
				event.setGreen((float) fogColor.y);
				event.setBlue((float) fogColor.z);
			}
		}
		
		lastX = event.getEntity().posX;
		lastZ = event.getEntity().posZ;
	}

	@SideOnly(Side.CLIENT)
	private void resetFogDistance(Minecraft mc, int fogMode)
	{
		if(otgDidLastFogRender)
		{
			// Non-OTG dims and OTG dims without fog settings don't properly reset 
			// the fog start and end when players teleport between dimensions.
			// Reset the fog distance here.
			otgDidLastFogRender = false;
			float farPlaneDistance = (float)(mc.gameSettings.renderDistanceChunks * 16);
			
            if (fogMode < 0)
            {
    			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
    			GL11.glFogf(GL11.GL_FOG_END, farPlaneDistance);
            } else {
    			GL11.glFogf(GL11.GL_FOG_START, farPlaneDistance * 0.75F);
    			GL11.glFogf(GL11.GL_FOG_END, farPlaneDistance);
            }
            
    		for (short[] row : biomeCache)
    		{
    			Arrays.fill(row, (short) -1);
    		}
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void clearBiomeCacheOnWorldChanged(Minecraft mc, int fogMode, ForgeWorld forgeWorld)
	{
		// If the player switched worlds, clear the biome id cache
		if(!lastWorldName.equals(forgeWorld.getName()))
		{
			lastWorldName = forgeWorld.getName();
			for (short[] row : biomeCache)
			{
				Arrays.fill(row, (short) -1);
			}
			resetFogDistance(mc, fogMode);
		}
	}
	
	boolean otgDidLastFogRender = false;
	// Handle the fog distance blending
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderFog(EntityViewRenderEvent.RenderFogEvent event)
	{
		if (!(event.getEntity().getEntityWorld().provider instanceof OTGWorldProvider))
		{
			resetFogDistance(event.getRenderer().mc, event.getFogMode());
			return;
		}
		
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		int[] ranges = ForgeModContainer.blendRanges;
		int blendDistance = 6;

		if (
			settings.fancyGraphics && settings.renderDistanceChunks >= 0
			&& settings.renderDistanceChunks < ranges.length
		)
		{
			blendDistance = ranges[settings.renderDistanceChunks];
		}

		Entity entity = event.getEntity();

		int blockX = MathHelper.floor(entity.posX);
		int blockZ = MathHelper.floor(entity.posZ);

		ForgeWorld forgeWorld = ((ForgeEngine) OTG.getEngine()).getWorld(entity.getEntityWorld());

		if (forgeWorld == null)
		{
			// Not an OTG world
			resetFogDistance(event.getRenderer().mc, event.getFogMode());		
			return;
		}

		clearBiomeCacheOnWorldChanged(event.getRenderer().mc, event.getFogMode(), forgeWorld);
		
		float biomeFogDistance = 0.0F;
		float weightBiomeFog = 0.0f;
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(0, 0, 0);
		boolean hasMoved = entity.posX != lastX || entity.posZ != lastZ;
		float fogDensity;
		float densityWeight;
		double differenceX;
		double differenceZ;
		BiomeConfig config;
		boolean bFound = false;
		
		for (int x = -blendDistance; x <= blendDistance; ++x)
		{
			for (int z = -blendDistance; z <= blendDistance; ++z)
			{
				blockPos.setPos(blockX + x, 0, blockZ + z);
				config = getBiomeConfig(forgeWorld, x + blendDistance, z + blendDistance, blockPos, hasMoved);

				if(config == null)
				{
					return;
				}
				
				if(config.fogColor != 0x000000)
				{		
					bFound = true;
					fogDensity = 1.0f - config.fogDensity;
					densityWeight = 1.0f;

					differenceX = getDifference(entity.posX, blockX, x, blendDistance);
					differenceZ = getDifference(entity.posZ, blockZ, z, blendDistance);

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
		}
		
		if(!bFound)
		{
			// OTG world with default fog settings.
			resetFogDistance(event.getRenderer().mc, event.getFogMode());		
			return;
		}

		float weightMixed = (blendDistance * 2) * (blendDistance * 2);
		float weightDefault = weightMixed - weightBiomeFog;

		if(weightDefault < 0.0f)
		{
			weightDefault = 0.0f;
		}
		
		float fogDistanceAvg = weightBiomeFog == 0.0f ? 0.0f : biomeFogDistance / weightBiomeFog;

		float fogDistance = (biomeFogDistance * 240.0f + event.getFarPlaneDistance() * weightDefault) / weightMixed;
		float fogDistanceScaleBiome = (0.1f * (1.0f - fogDistanceAvg) + 0.75f * fogDistanceAvg);
		float fogDistanceScale = (fogDistanceScaleBiome * weightBiomeFog + 0.75f * weightDefault) / weightMixed;
	
		float finalFogDistance = Math.min(fogDistance, event.getFarPlaneDistance());
		
		lastX = entity.posX;
		lastZ = entity.posZ;

		otgDidLastFogRender = true;
		// Render the fog
		if (event.getFogMode() < 0)
		{
			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, finalFogDistance);
		} else {
			GL11.glFogf(GL11.GL_FOG_START, finalFogDistance * fogDistanceScale);
			GL11.glFogf(GL11.GL_FOG_END, finalFogDistance);
		}
	}

	// Get the difference between the raw coordinate and block coordinate
	private double getDifference(double rawCoord, int blockCoord, int pos, int distance)
	{
		if (pos == -distance)
		{
			return 1.0f - (rawCoord - blockCoord);
		}
		else if (pos == distance)
		{
			return (rawCoord - blockCoord);
		}
		return -1.0f;
	}

	// Blend the fog color
	@SideOnly(Side.CLIENT)
	private Vec3d blendFogColors(ForgeWorld forgeWorld, BiomeConfig biomeConfig, EntityLivingBase entity, float red, float green, float blue, double renderPartialTicks)
	{
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		int[] ranges = ForgeModContainer.blendRanges;
		int blendDistance = 6;

		if (settings.fancyGraphics && settings.renderDistanceChunks >= 0
				&& settings.renderDistanceChunks < ranges.length)
		{
			blendDistance = ranges[settings.renderDistanceChunks];
		}

		double biomeFogRed = 0.0D;
		double biomeFogGreen = 0.0D;
		double biomeFogBlue = 0.0D;
		double biomeFogWeight = 0.0D;

		int blockX = (int) Math.floor(entity.posX);
		int blockZ = (int) Math.floor(entity.posZ);
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(blockX, 0, blockZ);
		
		boolean hasMoved = entity.posX != lastX || entity.posZ != lastZ;
		BiomeConfig config;
		double fogRed;
		double fogGreen;
		double fogBlue;
		float fogWeight;

		double differenceX;
		double differenceZ;	
		
		for (int x = -blendDistance; x <= blendDistance; ++x)
		{
			for (int z = -blendDistance; z <= blendDistance; ++z)
			{
				blockPos.setPos(blockX + x, 0, blockZ + z);
				config = getBiomeConfig(forgeWorld, x + blendDistance, z + blendDistance, blockPos, hasMoved);
				if(config == null)
				{
					return null;
				}
				if(config.fogColor != 0x000000)
				{
					fogRed = (config.fogColor & 0xFF0000) >> 16;
					fogGreen = (config.fogColor & 0x00FF00) >> 8;
					fogBlue = config.fogColor & 0x0000FF;
					fogWeight = 1.0f;

					differenceX = getDifference(entity.posX, blockX, x, blendDistance);
					differenceZ = getDifference(entity.posZ, blockZ, z, blendDistance);

					if (differenceX >= 0.0f)
					{
						fogRed *= differenceX;
						fogGreen *= differenceX;
						fogBlue *= differenceX;
						fogWeight *= differenceX;
					}

					if (differenceZ >= 0.0f)
					{
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

		if (biomeFogWeight <= 0.0f || blendDistance <= 0.0f)
		{
			return new Vec3d(red, green, blue);
		}

		// Convert integer to float from 0-1
		biomeFogRed /= 255.0f;
		biomeFogGreen /= 255.0f;
		biomeFogBlue /= 255.0f;

		// Scale color based on world time
		float baseScale = 1.0f;

		float time = MathHelper.clamp(
			MathHelper.cos(forgeWorld.getWorld().getCelestialAngle((float) renderPartialTicks) * (float) Math.PI * 2.0F) * 2.0F + 0.5F, 0.0f, 1.0f
		);

		baseScale *= 1.0f - (1.0f - time) * biomeConfig.fogTimeWeight;

		// Adjust based on weather
		float rainStrength = forgeWorld.getWorld().getRainStrength((float) renderPartialTicks);
		float thunderStrength = forgeWorld.getWorld().getThunderStrength((float) renderPartialTicks);

		if (thunderStrength >= 0.0f)
		{
			baseScale *= Math.min(1.0f - thunderStrength * biomeConfig.fogThunderWeight, 1.0f - rainStrength * biomeConfig.fogRainWeight);
		}
		else if (rainStrength >= 0.0f)
		{
			baseScale *= 1.0f - rainStrength * biomeConfig.fogRainWeight;
		}

		biomeFogRed *= baseScale / biomeFogWeight;
		biomeFogGreen *= baseScale / biomeFogWeight;
		biomeFogBlue *= baseScale / biomeFogWeight;

		// Mix default fog and our fog
		double weightMixed = (blendDistance * 2) * (blendDistance * 2);
		double weightDefault = weightMixed - biomeFogWeight;

		fogRed = (biomeFogRed * biomeFogWeight + red * weightDefault) / weightMixed;
		fogGreen = (biomeFogGreen * biomeFogWeight + green * weightDefault) / weightMixed;
		fogBlue = (biomeFogBlue * biomeFogWeight + blue * weightDefault) / weightMixed;

		return new Vec3d(fogRed, fogGreen, fogBlue);
	}

	String lastWorldName = "";
	// Get the biome config from the cache or freshly from the world if needed
	private BiomeConfig getBiomeConfig(ForgeWorld world, int x, int z, MutableBlockPos blockPos, boolean hasMoved)
	{		
		short cachedId = biomeCache[x][z];
		if (cachedId != -1 && !hasMoved)
		{
			return OTG.getEngine().getOTGBiomeIds(world.getName())[cachedId];
		} else {
			Biome biome = world.getBiomeFromChunk(blockPos.getX(), blockPos.getZ());
			LocalBiome localBiome = biome != null ? world.getBiomeByNameOrNull(biome.getBiomeName()) : null;
            if (localBiome == null || localBiome.getBiomeConfig() == null)
            {
            	biomeCache[x][z] = (short) -1;
                return null;
            }

			biomeCache[x][z] = (short) localBiome.getIds().getOTGBiomeId();
			return localBiome.getBiomeConfig();
		}
	}
}
