package com.khorn.terraincontrol.forge.events;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.TXWorldType;
import com.khorn.terraincontrol.forge.dimensions.TXTeleporter;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.util.ChunkCoordinate;

public class ServerEventListener
{	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{		
		if(event.phase == Phase.END)
		{			
			((ForgeEngine)TerrainControl.getEngine()).getPregenerator().ProcessTick();
			
			if(((ForgeEngine)TerrainControl.getEngine()).getOverWorld() != null) // If overworld is null then the overworld is not an OTG world
			{
				boolean cartographerEnabled = ((ForgeEngine)TerrainControl.getEngine()).getCartographerEnabled();	
	
				if(cartographerEnabled)
				{			
					Cartographer.UpdateWorldMap();
				}
			}
			
			// When players are at Y < 0 or Y > 256 teleport them to the dimension above or below this world (configured via worldconfig)
			TeleportPlayers();
		}
	}
	
	private void TeleportPlayers()
	{
		MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();			
		for(WorldServer worldServer : mcServer.worlds)
		{
			if(worldServer.getWorldInfo().getTerrainType() instanceof TXWorldType)
			{
				ArrayList<EntityPlayer> players = new ArrayList<EntityPlayer>(worldServer.playerEntities);
				for(EntityPlayer player : players)
				{
					tryTeleportPlayer(player);
				}
			}
		}
	}
	
    public void tryTeleportPlayer(EntityPlayer player)
	{
		ForgeWorld playerWorld = (ForgeWorld)((ForgeEngine)TerrainControl.getEngine()).getWorld(player.world);
		WorldConfig worldConfig = playerWorld.getConfigs().getWorldConfig();
		
		// DimensionBelow
		if(playerWorld != null && worldConfig.dimensionBelow != null && worldConfig.dimensionBelow.trim().length() > 0)
		{
			if(player.getPosition().getY() < worldConfig.dimensionBelowHeight)
			{
				ForgeWorld destinationWorld = (ForgeWorld)((ForgeEngine)TerrainControl.getEngine()).getWorld(worldConfig.dimensionBelow);
				if(destinationWorld == null)
				{
					destinationWorld = (ForgeWorld)((ForgeEngine)TerrainControl.getEngine()).getUnloadedWorld(worldConfig.dimensionBelow);						
				}
				
				if(destinationWorld != null) // Dimension does not exist
				{
					if(destinationWorld == playerWorld)
					{
						player.world.setBlockToAir(new BlockPos(player.getPosition().getX(), 254, player.getPosition().getZ()));
						player.world.setBlockToAir(new BlockPos(player.getPosition().getX(), 255, player.getPosition().getZ()));
						player.setPositionAndUpdate(player.getPosition().getX(), 254, player.getPosition().getZ());
					} else {
						TeleportPlayerToDimension(playerWorld.getWorld().provider.getDimension(), destinationWorld.getWorld().provider.getDimension(), player);
						return;
					}
				}
			}
		}
		
		// DimensionAbove
		if(playerWorld != null && worldConfig.dimensionAbove != null && worldConfig.dimensionAbove.trim().length() > 0)
		{
			if(player.getPosition().getY() > worldConfig.dimensionAboveHeight)
			{
				ForgeWorld destinationWorld = (ForgeWorld)((ForgeEngine)TerrainControl.getEngine()).getWorld(worldConfig.dimensionAbove);
				if(destinationWorld == null)
				{
					destinationWorld = (ForgeWorld)((ForgeEngine)TerrainControl.getEngine()).getUnloadedWorld(worldConfig.dimensionAbove);						
				}
				
				if(destinationWorld != null) // Dimension does not exist
				{
					if(destinationWorld != playerWorld)
					{
						TeleportPlayerToDimension(playerWorld.getWorld().provider.getDimension(), destinationWorld.getWorld().provider.getDimension(), player);	
					}
				}
			}
		}
	}
	
    private void TeleportPlayerToDimension(int originDimension, int newDimension, EntityPlayer e)
    {   	
    	boolean cartographerEnabled = ((ForgeEngine)TerrainControl.getEngine()).getCartographerEnabled();
    	
		if(e instanceof EntityPlayerMP)
		{
			TXTeleporter.changeDimension(newDimension, (EntityPlayerMP)e, false);
		}
		
    	// If coming from main world then update Cartographer map at last player position (should remove head+banner from Cartographer map)
		if(originDimension == 0 && cartographerEnabled)
		{
			//LocalWorld localWorld = TerrainControl.getEngine().getWorld(world.getWorldInfo().getWorldName());
			//if(localWorld != null)
			{
				Cartographer.CreateBlockWorldMapAtSpawn(ChunkCoordinate.fromBlockCoords(e.getPosition().getX(), e.getPosition().getZ()), true);
			}
		}
    }	
}