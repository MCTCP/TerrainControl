package com.khorn.terraincontrol.forge;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderTC extends WorldProvider
{
	//private CartographerSkyRenderer cartographerSkyRenderer = new CartographerSkyRenderer();
	
	public WorldProviderTC()
	{
	    //this.isHellWorld = true;
	    //this.hasNoSky = true;
		//this.setSkyRenderer(new CartographerSkyRenderer());
	}
	
    /**
     * A message to display to the user when they transfer to this dimension.
     *
     * @return The message to be displayed
     */
	@Override
    public String getWelcomeMessage()
    {
		if(IsCartographer())
		{
			return "Welcome to the Cartographer, traveller. This place is a hub between worlds, from here you can observe the world and transport living things and items to any place you desire.";
		} else {
			return "Entering dimension " + dimType.getName();
		}
    }
	
	boolean isCartographer;
	boolean IsCartographer()
	{
    	if(dimType == null)
    	{
    		dimType = DimensionManager.getProviderType(this.worldObj.provider.getDimension());
    		isCartographer = dimType.getName().equals("DIM-Cartographer");
    	}
    	return isCartographer;
	}
	
	DimensionType dimType = null;
    public DimensionType getDimensionType()
    {
    	if(dimType == null)
    	{
    		dimType = DimensionManager.getProviderType(this.worldObj.provider.getDimension());
    		isCartographer = dimType.getName().equals("DIM-Cartographer");
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
    
    /**
     * creates a new world chunk manager for WorldProvider
     */
    @Override
    protected void createBiomeProvider()
    {
    	this.biomeProvider = TCPlugin.tcWorldType.getBiomeProvider(worldObj);
    }
    
    @Override
    public IChunkGenerator createChunkGenerator()
    {
    	return TCPlugin.tcWorldType.getChunkGenerator(worldObj, worldObj.getWorldInfo().getGeneratorOptions());
    }

    /**
     * Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions.
     */
    public boolean isSurfaceWorld()
    {
        return true;
    }
    
    /**
     * Will check if the x, z position specified is alright to be set as the map spawn point
     */
    public boolean canCoordinateBeSpawn(int x, int z)
    {
        return false;
    }
    
    /**
     * True if the player can respawn in this dimension (true = overworld, false = nether).
     */
    public boolean canRespawnHere()
    {
    	return true;
    }
    
    /**
     * Return Vec3D with biome specific fog color
     */
    //@SideOnly(Side.CLIENT)
    //public Vec3d getFogColor(float p_76562_1_, float p_76562_2_)
    {
    	//if(IsCartographer())
    	{
    		//return new Vec3d(0.029999999329447746D, 0.029999999329447746D, 0.029999999329447746D);
    	//} else {
    		//return super.getFogColor(p_76562_1_, p_76562_2_);
    	}
    }
    
    /**
     * Returns true if the given X,Z coordinate should show environmental fog.
     */
    //@SideOnly(Side.CLIENT)
    //public boolean doesXZShowFog(int x, int z)
    {
    	//return false;
        //return IsCartographer();
    }
    
    //@SideOnly(Side.CLIENT)
    //public boolean isSkyColored()
    {
    	//return IsCartographer() ? false : super.isSkyColored();
    	//return super.isSkyColored();
    }
    
    /**
     * the y level at which clouds are rendered.
     */
    @SideOnly(Side.CLIENT)
    public float getCloudHeight()
    {
    	if(IsCartographer())
    	{
    		return -8.0F;
    	} else {
    		return super.getCloudHeight();
    	}
    }
    
    //public int getAverageGroundLevel()
    {
    	//if(IsCartographer())
    	{
    		//return 8;
    	//} else {
    		//return super.getAverageGroundLevel();
    	}
    }
    
    /**
     * The current sun brightness factor for this dimension.
     * 0.0f means no light at all, and 1.0f means maximum sunlight.
     * This will be used for the "calculateSkylightSubtracted"
     * which is for Sky light value calculation.
     *
     * @return The current brightness factor
     * */
    /*
    public float getSunBrightnessFactor(float par1)
    {
    	if(IsCartographer())
    	{
	        float f = 0.49837038f;
	        float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F);
	        f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
	        f1 = 1.0F - f1;
	        f1 = (float)((double)f1 * (1.0D - (double)(0.0d * 5.0F) / 16.0D));
	        f1 = (float)((double)f1 * (1.0D - (double)(0.0d * 5.0F) / 16.0D));
	        return f1;
    	} else {
    		return super.getSunBrightnessFactor(par1);
    	}
    }
    */
    
    /**
     * Gets the Sun Brightness for rendering sky.
     * */
    /*
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float par1)
    {
    	if(IsCartographer())
    	{
            float f = 0.49837038f;
            float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
            f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
            f1 = 1.0F - f1;
            f1 = (float)((double)f1 * (1.0D - (double)(0.0d * 5.0F) / 16.0D));
            f1 = (float)((double)f1 * (1.0D - (double)(0.0d * 5.0F) / 16.0D));
            return f1 * 0.8F + 0.2F;
    	} else {
    		return super.getSunBrightness(par1);
    	}
    }
    */

    /**
     * Gets the Star Brightness for rendering sky.
     * */
    //@SideOnly(Side.CLIENT)
    //public float getStarBrightness(float par1)
    {
        //return worldObj.getStarBrightnessBody(par1);
    }
    
    /**
     * Creates the light to brightness table
     */
    protected void generateLightBrightnessTable()
    {
    	if(IsCartographer())
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
    
    /*
    public WorldBorder createWorldBorder()
    {
        return new WorldBorder()
        {
            public double getCenterX()
            {
                return super.getCenterX() / 8.0D;
            }
            public double getCenterZ()
            {
                return super.getCenterZ() / 8.0D;
            }
        };
    }
    */
    
    /**
     * Returns array with sunrise/sunset colors
     */
    /*
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks)
    {
    	if(IsCartographer())
    	{
    		return super.calcSunriseSunsetColors(0.49837038f, partialTicks);
    	} else {
    		return super.calcSunriseSunsetColors(celestialAngle, partialTicks);
    	}
    }
    */
    
    /**
     * Calculates the angle of sun and moon in the sky relative to a specified time (usually worldTime)
     */
    public float calculateCelestialAngle(long worldTime, float partialTicks)
    {
    	if(IsCartographer())
    	{
    		return 0.49837038f;
    	} else {
    		return super.calculateCelestialAngle(worldTime, partialTicks);
    	}
    }      
    
    @Override
    public double getHorizon()
    {
   		return IsCartographer() ? 4.0d : super.getHorizon();
    	//return super.getHorizon();
    }
    
    @Override
    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk)
    {
        return IsCartographer() ? false : super.canDoLightning(chunk);
    }
    
    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk)
    {
    	return IsCartographer() ? false : super.canDoRainSnowIce(chunk);
    }
    
    @Override
    public boolean canMineBlock(net.minecraft.entity.player.EntityPlayer player, BlockPos pos)
    {
        return IsCartographer() ? false : super.canMineBlock(player, pos);
    }    
    
    //@Override
    //public boolean getHasNoSky()
    {
    	//if(IsCartographer())
    	{
    		//this.hasNoSky = true;
    	}
    	//return  super.getHasNoSky();
    }    
    
    //@SideOnly(Side.CLIENT)
    //public net.minecraftforge.client.IRenderHandler getSkyRenderer()
    {
    	//return IsCartographer() ? cartographerSkyRenderer : super.getSkyRenderer();
    }
}