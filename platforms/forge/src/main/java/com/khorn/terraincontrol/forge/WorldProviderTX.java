package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderTX extends WorldProvider
{
	public WorldConfig config = null;
	
	private WorldConfig GetWorldConfig()
	{
		if(config == null)
		{
			ForgeWorld forgeWorld = (ForgeWorld)((ForgeEngine)TerrainControl.getEngine()).getWorld(world);
			if(forgeWorld != null)
			{
				config = forgeWorld.getConfigs().getWorldConfig();
			}
		}
		return config;
	}
	
	public WorldProviderTX() { }
	
    // A message to display to the user when they transfer to this dimension.
	@Override
    public String getWelcomeMessage()
    {
		WorldConfig worldConfig = GetWorldConfig();
		return worldConfig != null ? worldConfig.welcomeMessage : WorldStandardValues.welcomeMessage.getDefaultValue();
    }
	
	// A Message to display to the user when they transfer out of this dismension.
	@Override
    public String getDepartMessage()
    {
		WorldConfig worldConfig = GetWorldConfig();
		return worldConfig != null ? worldConfig.departMessage : WorldStandardValues.departMessage.getDefaultValue();
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
        this.biomeProvider = TXPlugin.txWorldType.getBiomeProvider(world);
    }
    
    @Override
    public IChunkGenerator createChunkGenerator()
    {
    	return TXPlugin.txWorldType.getChunkGenerator(world, world.getWorldInfo().getGeneratorOptions());
    }

    // Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions.
    @Override
    public boolean isSurfaceWorld()
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.isSurfaceWorld : WorldStandardValues.isSurfaceWorld.getDefaultValue();
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
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.canRespawnHere : WorldStandardValues.canRespawnHere.getDefaultValue();
    }
    
    // Determines the dimension the player will be respawned in, typically this brings them back to the overworld.
    @Override
    public int getRespawnDimension(net.minecraft.entity.player.EntityPlayerMP player)
    {
        return super.getRespawnDimension(player);
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
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.shouldMapSpin : super.shouldMapSpin(entity, x, y, z);
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
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null && worldConfig.useCustomFogColor ? new Vec3d(worldConfig.fogColorRed, worldConfig.fogColorGreen, worldConfig.fogColorBlue) : super.getFogColor(p_76562_1_, p_76562_2_);
    }
    
    // Returns true if the given X,Z coordinate should show environmental fog.
    // TODO: Is this really needed to make fog work?
    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(int x, int z)
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.doesXZShowFog : WorldStandardValues.doesXZShowFog.getDefaultValue();
    }
    
    // Returns a double value representing the Y value relative to the top of the map at which void fog is at its
    // maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at
    // (256*0.03125), or 8.
    @SideOnly(Side.CLIENT)
    @Override
    public double getVoidFogYFactor()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.voidFogYFactor : WorldStandardValues.voidFogYFactor.getDefaultValue();
    }
    
    // TODO: Is this really needed to make sky colors work?
    @SideOnly(Side.CLIENT)
    @Override
    public boolean isSkyColored()
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.isSkyColored : WorldStandardValues.isSkyColored.getDefaultValue();
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
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.cloudHeight : WorldStandardValues.cloudHeight.getDefaultValue();    	
    }
    
    @Override
    public int getAverageGroundLevel()
    {
    	WorldConfig worldConfig = GetWorldConfig();
   		return worldConfig != null ? worldConfig.waterLevelMax + 1 : this.world.getSeaLevel() + 1;
    }
    
    // Called to determine if the chunk at the given chunk coordinates within the provider's world can be dropped. Used
    // in WorldProviderSurface to prevent spawn chunks from being unloaded.
    @Override
    public boolean canDropChunk(int x, int z)
    {
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.canDropChunk : WorldStandardValues.canDropChunk.getDefaultValue();
    }
    
    // Creates the light to brightness table
    @Override
    protected void generateLightBrightnessTable()
    {
    	WorldConfig worldConfig = GetWorldConfig();
   	
		if(worldConfig != null && worldConfig.isNightWorld)
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
    	WorldConfig worldConfig = GetWorldConfig();    	
    	if(worldConfig != null && worldConfig.isNightWorld)
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
    	WorldConfig worldConfig = GetWorldConfig();
        return worldConfig != null ? worldConfig.canDoLightning : WorldStandardValues.canDoLightning.getDefaultValue();
    }
        
    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk)
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.canDoRainSnowIce : WorldStandardValues.canDoRainSnowIce.getDefaultValue();
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
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.doesWaterVaporize : WorldStandardValues.doesWaterVaporize.getDefaultValue(); 
    }
    
    // For 1.10.2 hasNoSky and hasSkyLight were the same thing, controlled via hasNoSky
    
    @Override
    public boolean hasNoSky()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? !worldConfig.hasSkyLight : !WorldStandardValues.hasSkyLight.getDefaultValue();
    }   
    
    @Override
    public boolean hasSkyLight()
    {
    	WorldConfig worldConfig = GetWorldConfig();
    	return worldConfig != null ? worldConfig.hasSkyLight : WorldStandardValues.hasSkyLight.getDefaultValue();
    }    
    
    @SideOnly(Side.CLIENT)
    @Override
    public net.minecraftforge.client.IRenderHandler getSkyRenderer()
    {
    	return super.getSkyRenderer();
    }	
}