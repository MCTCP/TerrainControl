package com.pg85.otg.forge.events;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;
import com.pg85.otg.forge.gui.GuiHandler;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.forge.world.ForgeWorldSession;
import com.pg85.otg.forge.world.OTGWorldType;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldListener
{
	private void initOnWorldLoad()
	{
		GuiHandler.IsInMainMenu = false;
		OTGPlugin.BiomeColorsListener.reload();		
	}
	
	@SubscribeEvent
	@SideOnly(Side.SERVER)
	public void onWorldLoadServer(WorldEvent.Load event)
	{
		initOnWorldLoad();
		
        World world = event.getWorld();
        int dimension = world.provider.getDimension();

        //OTG.log(LogMarker.INFO, "WorldEvent.Load - DIM: {}", dimension);

        ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorld(world);
    	if(forgeWorld != null)
        {
            if (dimension == 0)
            {
           		overrideWorldProvider(world);
            }

            // The createworldspawn event was cancelled, since the worldprovider needed to be replaced first for the overworld.
            // Now that the worldprovider has been replaced, create the spawn point.
            // For dimensions, the createworldspawn event is never fired, so create the spawn point here as well.
            // Spawn point/border data for dims is not saved by MC (overworld only), so we recreate the spawn position & borders on each world load.
            DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName());
            createSpawnPosition(forgeWorld, dimConfig);
            
           	// Don't create bonus chest on each world load (bit of a hack using getPreGeneratorIsInitialised).
	        if(dimConfig.BonusChest && !forgeWorld.getWorldSession().getPreGeneratorIsInitialised())
	        {
	        	createBonusChest(forgeWorld.getWorld());
	        }
            
        	// Spawn position has been determined, set pregenerator center.
        	forgeWorld.getWorldSession().setPreGeneratorCenterPoint(ChunkCoordinate.fromBlockCoords(world.getWorldInfo().getSpawnX(), world.getWorldInfo().getSpawnZ()));

            // Spawn position has been determined, create world borders.
           	world.worldBorder = ((OTGWorldProvider)world.provider).createWorldBorderA(world.worldBorder);
           	
    		ServerPacketManager.sendDimensionLoadUnloadPacketToAllPlayers(true, forgeWorld.getName(), event.getWorld().getMinecraftServer());
    	}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldLoadClient(WorldEvent.Load event)
	{	
		initOnWorldLoad();
		
		// For single player only one world is loaded on the client, but forgeworlds exist for all dims		
		for(LocalWorld localWorld : ((ForgeEngine)OTG.getEngine()).getAllWorlds())
		{
			ForgeWorld forgeWorld = (ForgeWorld)localWorld;
			if(forgeWorld.getWorld() == null && forgeWorld.clientDimensionId == event.getWorld().provider.getDimension())
			{
				forgeWorld.provideClientWorld(event.getWorld());
			}
		}
		
        World world = event.getWorld();
        int dimension = world.provider.getDimension();

        //OTG.log(LogMarker.INFO, "WorldEvent.Load - DIM: {}", dimension);
       
        ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorld(world);
    	if(forgeWorld != null)
        {
            if (dimension == 0)
            {
           		overrideWorldProvider(world);
            }

            if(!world.isRemote)
            {
	            // The createworldspawn event was cancelled, since the worldprovider needed to be replaced first for the overworld.
	            // Now that the worldprovider has been replaced, create the spawn point.
	            // For dimensions, the createworldspawn event is never fired, so create the spawn point here as well.
	            // Spawn point/border data for dims is not saved by MC (overworld only), so we recreate the spawn position & borders on each world load.
	            DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName());
	            createSpawnPosition(forgeWorld, dimConfig);
	            
	           	// Don't create bonus chest on each world load (bit of a hack using getPreGeneratorIsInitialised).
		        if(dimConfig.BonusChest && !forgeWorld.getWorldSession().getPreGeneratorIsInitialised())
		        {
		        	createBonusChest(forgeWorld.getWorld());
		        }
	            
	        	// Spawn position has been determined, set pregenerator center.
	        	forgeWorld.getWorldSession().setPreGeneratorCenterPoint(ChunkCoordinate.fromBlockCoords(world.getWorldInfo().getSpawnX(), world.getWorldInfo().getSpawnZ()));
	
	            // Spawn position has been determined, create world borders.
	           	world.worldBorder = ((OTGWorldProvider)world.provider).createWorldBorderA(world.worldBorder);
            }
        }
	}
	
    private static void overrideWorldProvider(World world)
    {
        String newClassName = OTGWorldProvider.class.getName();
        Class<? extends WorldProvider> newProviderClass = OTGWorldProvider.class;

        if (newProviderClass != null && newProviderClass != world.provider.getClass())
        {
            final int dim = world.provider.getDimension();
            //OTG.log(LogMarker.INFO, "WorldUtils.overrideWorldProvider: Trying to override the WorldProvider of type '{}' in dimension {} with '{}'", oldName, dim, newClassName);

            try
            {
                Constructor <? extends WorldProvider> constructor = newProviderClass.getConstructor();
                WorldProvider newProvider = constructor.newInstance();               

                try
                {                	
                    WorldProvider oldProvider = world.provider;
                    world.provider = newProvider;
                    ((OTGWorldProvider)world.provider).isSPServerOverworld = !world.isRemote; // TODO: Why is this necessary?
                   	world.provider.setWorld(world);
                    world.provider.setDimension(dim);
                 
                    if(!world.isRemote)
                    {
                    	// TODO: Bit of a hack, need to override the worldprovider for SP server or gravity won't work properly ><.
                    	// Creating a new biomeprovider causes problems, re-using the existing one seems to work though,
                    	((OTGWorldProvider)world.provider).init(oldProvider.getBiomeProvider());
                    }
                    
                    //OTG.log(LogMarker.INFO, "WorldUtils.overrideWorldProvider: Overrode the WorldProvider in dimension {} with '{}'", dim, newClassName);
                }
                catch (Exception e) // TODO: Don't catch all
                {
                    OTG.log(LogMarker.ERROR, "WorldUtils.overrideWorldProvider: Failed to override the WorldProvider of dimension {}", dim);
                }

                return;
            }
            catch (Exception e)
            {
            	
            }
        }

        OTG.log(LogMarker.WARN, "WorldUtils.overrideWorldProvider: Failed to create a WorldProvider from name '{}', or it was already that type", newClassName);
    }
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		ForgeWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld());
		if(world != null)
		{
			((ForgeWorldSession)world.getWorldSession()).getPregenerator().savePregeneratorData();
		} else {
			// This is not an OTG world.
		}
	}
	
    // TODO: This method should not be called by DimensionManager when switching dimensions (main -> nether -> main). Find out why it is being called
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
		if(!event.getWorld().isRemote) // Server side only
		{			
	        World mcWorld = event.getWorld();
	        MinecraftServer mcServer = mcWorld.getMinecraftServer();
	        if(mcServer == null)
	        {
	        	mcServer = getClientServer();
	        }

	        boolean serverStopping = !mcServer.isServerRunning();

	        if(event.getWorld().getWorldType() instanceof OTGWorldType)
	        {
	        	int dimId = event.getWorld().provider.getDimension();
	        	
		        ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(mcWorld);
		        if(forgeWorld == null)
		        {
		        	// Can happen if this is dim -1 or 1 (or some other mod's dim?)
		        	return;
		        }		        	     		        			
		        		        
				if(dimId != -1 && dimId != 1)
		    	{		    		
	    			if((ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld()) != null)
	    			{    				
		    			//OTG.log(LogMarker.INFO, "Unloading world " + event.getWorld().getWorldInfo().getWorldName() + " at dim " + dimId);
		    			((ForgeEngine)OTG.getEngine()).getWorldLoader().unloadWorld(event.getWorld(), false);
	    			} else {
	    				// World has already been unloaded, only happens when shutting down server?
	    			}
	    			
	    			if(serverStopping)
	    			{
		        		OTGDimensionManager.UnloadCustomDimensionData(mcWorld.provider.getDimension());
		        		forgeWorld.unRegisterBiomes();
		        		
						((ForgeWorldSession)forgeWorld.getWorldSession()).getPregenerator().shutDown();
	    			}
		    	}
	        }
	        
        	if(serverStopping)
        	{    			
    			// Unregister any currently unloaded custom dimensions
				// Doesn't matter that this might happen multiple times on server shutdown
    			for(ForgeWorld unloadedWorld : ((ForgeEngine)OTG.getEngine()).getUnloadedWorlds())
    			{
    				if(unloadedWorld.getWorld() != mcWorld)
    				{
    	        		OTGDimensionManager.UnloadCustomDimensionData(unloadedWorld.getWorld().provider.getDimension());
        				unloadedWorld.unRegisterBiomes();
    				}
    			}
        	}
		}
    }
    
    @SideOnly(Side.CLIENT)
    private MinecraftServer getClientServer()
    {
    	return net.minecraft.client.Minecraft.getMinecraft().getIntegratedServer();
    }
    
    @SubscribeEvent
    public void onCreateWorldSpawn(WorldEvent.CreateSpawnPosition event)
    {
    	LocalWorld world = ((ForgeEngine) OTG.getEngine()).getWorld(event.getWorld());
    	if(world != null)
    	{
    		// The Spawn point is overridden on worldload, since for the overworld the 
    		// worldprovider hasn't been replaced yet at this point, and for dimensions
    		// CreateSpawnPosition never gets fired.
			event.setCanceled(true);
    	}
    }

    /**
     * creates a spawn position at random within 256 blocks of 0,0
     */
    private static void createSpawnPosition(ForgeWorld forgeWorld, DimensionConfig dimConfig)
    {
		if(dimConfig.Settings.SpawnPointSet)
		{
			BlockPos spawnPosition = new BlockPos(dimConfig.Settings.SpawnPointX, dimConfig.Settings.SpawnPointY, dimConfig.Settings.SpawnPointZ);
			forgeWorld.getWorld().getWorldInfo().setSpawn(spawnPosition);
		} else {
			forgeWorld.getWorld().findingSpawnPoint = true;
	        BiomeProvider biomeprovider = forgeWorld.getWorld().provider.getBiomeProvider();
	        List<Biome> list = biomeprovider.getBiomesToSpawnIn();
	        Random random = new Random(forgeWorld.getSeed());
	        int range = 1024;
	        BlockPos blockpos = biomeprovider.findBiomePosition(0, 0, range, list, random);
	        int i = 8;
	        int j = forgeWorld.getWorld().provider.getAverageGroundLevel();
	        int k = 8;
	
	        if (blockpos != null)
	        {
	            i = blockpos.getX();
	            k = blockpos.getZ();
	        } else {
	            OTG.log(LogMarker.INFO, "Unable to find spawn biome");
	        }
	
	        int l = 0;
	
	        while (!forgeWorld.getWorld().provider.canCoordinateBeSpawn(i, k))
	        {
	            i += random.nextInt(64) - random.nextInt(64);
	            k += random.nextInt(64) - random.nextInt(64);
	            ++l;
	
	            if (l == 1000)
	            {
	                break;
	            }
	        }
	
	        ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(i, k);
	        // When using a BO3 a spawn, set the spawn point in the center of the 32x32 area being populated, to hopefully put it above the BO3's blocks.
	        if(forgeWorld.getConfigs().getWorldConfig().bo3AtSpawn != null && forgeWorld.getConfigs().getWorldConfig().bo3AtSpawn.trim().length() > 0)
	        {
	        	i = chunkCoord.getBlockX() + 15;
	        	k = chunkCoord.getBlockZ() + 15;
	        }
	        
	        forgeWorld.getWorld().getWorldInfo().setSpawn(new BlockPos(i, j, k));
	        forgeWorld.getWorld().findingSpawnPoint = false;
	        
	        // Spawn point is only saved for overworld by MC, 
	        // so we have to save it ourselves for dimensions.
	        // Use the dimensionconfig
	        dimConfig.Settings.SpawnPointX = i;
	        dimConfig.Settings.SpawnPointY = j;
	        dimConfig.Settings.SpawnPointZ = k;
	        OTG.getDimensionsConfig().save();
		}
    }
    
    /**
     * Creates the bonus chest in the world.
     */
    protected static void createBonusChest(World world)
    {
        WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest();

        for (int i = 0; i < 10; ++i)
        {
            int j = world.getWorldInfo().getSpawnX() + world.rand.nextInt(6) - world.rand.nextInt(6);
            int k = world.getWorldInfo().getSpawnZ() + world.rand.nextInt(6) - world.rand.nextInt(6);
            BlockPos blockpos = world.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k)).up();

            if (worldgeneratorbonuschest.generate(world, world.rand, blockpos))
            {
                break;
            }
        }
    }
}
