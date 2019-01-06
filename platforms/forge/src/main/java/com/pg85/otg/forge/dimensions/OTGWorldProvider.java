package com.pg85.otg.forge.dimensions;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.OTGPlugin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OTGWorldProvider extends WorldProvider
{
	public DimensionConfig dimConfig = null;
	public WorldConfig worldConfig = null;
	public String worldName = null;

	public WorldConfig GetWorldConfig()
	{
		if(worldConfig == null)
		{
			ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(world);
			if(forgeWorld != null)
			{				
				worldConfig = forgeWorld.getConfigs().getWorldConfig();
			}			
		}
		return worldConfig;
	}

	long lastFetchTime = System.currentTimeMillis();
	public DimensionConfig GetDimensionConfig()
	{		
		// The config may be updated during a session, refresh it once per second
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastFetchTime > 1000l)
		{
			lastFetchTime = currentTime;
			if(worldName == null)
			{
				ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getWorld(world);
				if(forgeWorld != null)
				{
					worldName = forgeWorld.getName();
				}
			}
			if(worldName != null)
			{
				dimConfig = OTG.GetDimensionsConfig().GetDimensionConfig(worldName);				
			}
		}
		return dimConfig;
	}
	
	public OTGWorldProvider() { }

    // A message to display to the user when they transfer to this dimension.
    public String getWelcomeMessage()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
		return dimConfig != null ? dimConfig.Settings.WelcomeMessage : WorldStandardValues.welcomeMessage.getDefaultValue();
    }

	// A Message to display to the user when they transfer out of this dismension.
    public String getDepartMessage()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
		return dimConfig != null ? dimConfig.Settings.DepartMessage : WorldStandardValues.departMessage.getDefaultValue();
    }

	DimensionType dimType = null;
    public DimensionType getDimensionType()
    {
    	if(dimType == null)
    	{
    		dimType = DimensionManager.getProviderType(this.world.provider.getDimension());
    	}

    	// Some mods (like Optifine) crash if the dimensionType returned is not one of the default ones.
    	// We can't use DimensionType.OVERWORLD though or the ChunkProdivderServer.unloadQueuedChunks won't unload this dimension
    	// This seems to be called often so may cause client lag :(.
    	StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    	if(stackTrace.length > 2)
    	{
    		String className = stackTrace[2].getClassName().toLowerCase();
	    	if(className.contains("customcolors"))
	    	{
	    		return DimensionType.OVERWORLD;
	    	}
    	}

        return dimType;
    }

     // Creates a new {@link BiomeProvider} for the WorldProvider, and also sets the values of {@link #hasSkylight} and
     // {@link #hasNoSky} appropriately.
    protected void init()
    {
        // Creates a new world chunk manager for WorldProvider
    	this.hasSkyLight = true;
        this.biomeProvider = OTGPlugin.txWorldType.getBiomeProvider(world);
    }

    @Override
    public net.minecraft.world.gen.IChunkGenerator createChunkGenerator()
    {
    	return OTGPlugin.txWorldType.getChunkGenerator(world, "OpenTerrainGenerator");
    }

    // Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions.
    @Override
    public boolean isSurfaceWorld()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
        return dimConfig != null ? dimConfig.Settings.IsSurfaceWorld : WorldStandardValues.isSurfaceWorld.getDefaultValue();
    }

    // Will check if the x, z position specified is alright to be set as the map spawn point
    @Override
    public boolean canCoordinateBeSpawn(int x, int z)
    {
        return false; // TODO: Make spawn pos detection method? (make sure it doesn't screw up BO3AtSpawn and cartographer in TXChunkGenerator)
    }

    @Override
    public BlockPos getRandomizedSpawnPoint()
    {
    	return super.getRandomizedSpawnPoint();
    }

    // True if the player can respawn in this dimension (true = overworld, false = nether).
    @Override
    public boolean canRespawnHere()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
        return dimConfig != null ? dimConfig.Settings.CanRespawnHere : WorldStandardValues.canRespawnHere.getDefaultValue();
    }

    // Determine if the cursor on the map should 'spin' when rendered, like it does for the player in the nether.
    // @param entity The entity holding the map, playername, or frame-ENTITYID
    // @param x X Position
    // @param y Y Position
	// @param z Z Position
    // @return True to 'spin' the cursor
    @Override
    public boolean shouldMapSpin(String entity, double x, double y, double z)
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
    	return dimConfig != null ? dimConfig.Settings.ShouldMapSpin : super.shouldMapSpin(entity, x, y, z);
    }

    @Override
    public boolean isDaytime()
    {
    	return super.isDaytime();
    }

    @Override
    public int getHeight()
    {
        return super.getHeight();
    }

    @Override
    public int getActualHeight()
    {
        return super.getActualHeight();
    }

    // Return Vec3D with biome specific fog color
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_)
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
        return dimConfig != null && dimConfig.Settings.UseCustomFogColor ? new Vec3d(dimConfig.Settings.FogColorRed, dimConfig.Settings.FogColorGreen, dimConfig.Settings.FogColorBlue) : super.getFogColor(p_76562_1_, p_76562_2_);
    }

    // Returns true if the given X,Z coordinate should show environmental fog.
    // TODO: Is this really needed to make fog work?
    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(int x, int z)
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
        return dimConfig != null ? dimConfig.Settings.DoesXZShowFog : WorldStandardValues.doesXZShowFog.getDefaultValue();
    }

    /**
     * Returns a double value representing the Y value relative to the top of the map at which void fog is at its
     * maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at
     * (256*0.03125), or 8.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public double getVoidFogYFactor()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
    	return dimConfig != null ? dimConfig.Settings.VoidFogYFactor : WorldStandardValues.voidFogYFactor.getDefaultValue();
    }

    // TODO: Is this really needed to make sky colors work?
    @SideOnly(Side.CLIENT)
    @Override
    public boolean isSkyColored()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
        return dimConfig != null ? dimConfig.Settings.IsSkyColored : WorldStandardValues.isSkyColored.getDefaultValue();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks)
    {
    	return super.getSkyColor(cameraEntity, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getCloudColor(float partialTicks)
    {
        return super.getCloudColor(partialTicks);
    }

    // the y level at which clouds are rendered.
    @SideOnly(Side.CLIENT)
    @Override
    public float getCloudHeight()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
    	return dimConfig != null ? dimConfig.Settings.CloudHeight : WorldStandardValues.cloudHeight.getDefaultValue();
    }

    @Override
    public int getAverageGroundLevel()
    {
    	WorldConfig worldConfig = GetWorldConfig();
   		return worldConfig != null ? worldConfig.waterLevelMax + 1 : this.world.getSeaLevel() + 1; // Sea level + 1 by default
    }

    // Called to determine if the chunk at the given chunk coordinates within the provider's world can be dropped. Used
    // in WorldProviderSurface to prevent spawn chunks from being unloaded.
    @Override
    public boolean canDropChunk(int x, int z)
    {
    	if(this.getDimension() != 0) // Never unload Overworld
    	{
    		DimensionConfig dimConfig = GetDimensionConfig();
	        return dimConfig != null ? dimConfig.Settings.CanDropChunk : WorldStandardValues.canDropChunk.getDefaultValue();
    	} else {
    		return !this.world.isSpawnChunk(x, z) || !this.world.provider.getDimensionType().shouldLoadSpawn();
    	}
    }

    // Creates the light to brightness table
    @Override
    protected void generateLightBrightnessTable()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
		if(dimConfig != null && dimConfig.Settings.IsNightWorld)
    	{
	        for (int i = 0; i <= 15; ++i)
	        {
	            //float f1 = 1.0F - (float)i / 15.0F;
	        	float f1 = 0.0F;
	            this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 0.9F + 0.1F;
	        }
    	} else {
    		super.generateLightBrightnessTable();
    	}
    }


	@Override
    public WorldBorder createWorldBorder()
    {
		return super.createWorldBorder();
    }

    // Calculates the angle of sun and moon in the sky relative to a specified time (usually worldTime)
    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks)
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
    	if(dimConfig != null && dimConfig.Settings.IsNightWorld)
    	{
    		return 0.49837038f;
    	} else {
    		return super.calculateCelestialAngle(worldTime, partialTicks);
    	}
    }

    @Override
    public double getHorizon()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.waterLevelMax : this.world.getSeaLevel();
    }

    @Override
    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk)
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
        return dimConfig != null ? dimConfig.Settings.CanDoLightning : WorldStandardValues.canDoLightning.getDefaultValue();
    }

    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk)
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
    	return dimConfig != null ? dimConfig.Settings.CanDoRainSnowIce : WorldStandardValues.canDoRainSnowIce.getDefaultValue();
    }

    // This only affects lillies, glass bottles and a few other unimportant things?
    //@Override
    //public boolean canMineBlock(net.minecraft.entity.player.EntityPlayer player, BlockPos pos)
    //{
    	//WorldConfig worldConfig = GetWorldConfig();
    	//return worldConfig != null ? worldConfig.canMineBlock && super.canMineBlock(player, pos) : super.canMineBlock(player, pos);
    //}

    @Override
    public boolean doesWaterVaporize()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
    	return dimConfig != null ? dimConfig.Settings.DoesWaterVaporize : WorldStandardValues.doesWaterVaporize.getDefaultValue();
    }

    @Override
    public boolean hasSkyLight()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
    	return dimConfig != null ? dimConfig.Settings.HasSkyLight : WorldStandardValues.hasSkyLight.getDefaultValue();
    }

    @Override
    public void setWorldTime(long time)
    {
    	world.getWorldInfo().setWorldTime(time);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public net.minecraftforge.client.IRenderHandler getSkyRenderer()
    {
    	return super.getSkyRenderer(); // TODO: Replace this with custom sky renderer?
    }

    public double getGravityFactor()
    {
    	DimensionConfig dimConfig = GetDimensionConfig();
    	return dimConfig != null ? dimConfig.Settings.GravityFactor : WorldStandardValues.gravityFactor.getDefaultValue();
    }

    public double getFallDamageFactor(double y)
    {
    	double baseGravityFactor = WorldStandardValues.gravityFactor.getDefaultValue();
    	double gravityFactor = getGravityFactor();
    	return (y * (gravityFactor / baseGravityFactor));
    }

    /**
     * The dimension's movement factor.
     * Whenever a player or entity changes dimension from world A to world B, their coordinates are multiplied by
     * worldA.provider.getMovementFactor() / worldB.provider.getMovementFactor()
     * Example: Overworld factor is 1, nether factor is 8. Traveling from overworld to nether multiplies coordinates by 1/8.
     * @return The movement factor
     */
    @Override
    public double getMovementFactor()
    {
    	DimensionConfig worldConfig = GetDimensionConfig();
    	return worldConfig != null ? worldConfig.Settings.MovementFactor : WorldStandardValues.MOVEMENT_FACTOR.getDefaultValue();
    }

    /**
     * Determines the dimension the player will be respawned in, typically this brings them back to the overworld.
     *
     * @param player The player that is respawning
     * @return The dimension to respawn the player in
     */
    @Override
    public int getRespawnDimension(net.minecraft.entity.player.EntityPlayerMP player)
    {
    	DimensionConfig worldConfig = GetDimensionConfig();
    	return worldConfig != null ? !worldConfig.Settings.CanRespawnHere ? worldConfig.Settings.RespawnDimension : super.getRespawnDimension(player) : super.getRespawnDimension(player);
    }
}