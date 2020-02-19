package com.pg85.otg.forge.world;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public abstract class WorldHelper
{
    private WorldHelper()
    {
    }

    public static String getName(World world)
    {
    	if(
			//world.provider.getDimension() > 1 &&
			//(
				//(
					//world.getWorldInfo() instanceof DerivedWorldInfo &&
					//((DerivedWorldInfo)world.getWorldInfo()).delegate.getGeneratorOptions().equals("OpenTerrainGenerator")
				//) || (
					world.getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator")
				//)
			//)
		)
    	{
        	// This dimension was created by OTG, use the given dimension name
        	return DimensionManager.getProviderType(world.provider.getDimension()).getName();
        }

        World defaultWorld = DimensionManager.getWorld(0);
        // If vanilla or we are dealing with an implementation that supports
        // multiple save handlers, return the world name
        if (isVanillaDimension(world) || (defaultWorld != null && world.getWorldInfo() != null && world.getSaveHandler() != defaultWorld.getSaveHandler()))
        {
            return world.getWorldInfo().getWorldName();
        }

        // This is the best we can do for Forge
        return world.provider.getSaveFolder();
    }

    private static boolean isVanillaDimension(World world)
    {
        int dimensionId = world.provider.getDimension();
        // If vanilla or OTG, return overworld
        if (dimensionId == 0 || dimensionId == 1 || dimensionId == -1)
        {
            return true;
        }

        return false;
    }   
}
