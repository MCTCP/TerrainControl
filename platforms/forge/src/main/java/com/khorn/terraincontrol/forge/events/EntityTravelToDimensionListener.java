package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.ForgeMaterialData;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.forge.generator.TCBlockPortal;
import com.khorn.terraincontrol.forge.generator.TCTeleporter;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityTravelToDimensionListener
{
	@SubscribeEvent
	public void entityTravelToDimension(EntityTravelToDimensionEvent e)
	{	
		boolean cartographerEnabled = ((ForgeEngine)TerrainControl.getEngine()).getCartographerEnabled();
		boolean dimensionsEnabled = ((ForgeEngine)TerrainControl.getEngine()).getDimensionsEnabled();		

		if(cartographerEnabled || dimensionsEnabled)
		{
			// TODO: This will disallow any dimension travelling by entities?
			if(!(e.getEntity() instanceof EntityPlayer))
			{
				e.setCanceled(true); // Don't tp to nether
				return;
			}
			
			// If going to the nether
			if(e.getDimension() == -1 && e.getEntity() instanceof EntityPlayer)
			{
				// Make sure the player is above a portal block so we can check if this is a Quartz portal
				if(!ForgeMaterialData.ofMinecraftBlockState(e.getEntity().getEntityWorld().getBlockState(e.getEntity().getPosition())).toDefaultMaterial().equals(DefaultMaterial.PORTAL))
				{
					e.getEntity().timeUntilPortal = 0;
					e.setCanceled(true);
					return;
				}
				
				EntityPlayer sender = ((EntityPlayer)e.getEntity());
				BlockPos playerPos = new BlockPos(sender.getPosition());
				World world = sender.getEntityWorld();
				
				BlockPos pos = new BlockPos(sender.getPosition());
				IBlockState blockState = world.getBlockState(pos);
				while(!blockState.getMaterial().isSolid() && pos.getY() > 0)
				{
					pos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
					blockState = world.getBlockState(pos);
				}
				
				if(
					pos.getY() > 0 && 
					(
						blockState.getBlock() == Blocks.QUARTZ_BLOCK || 
						blockState.getBlock() == Blocks.QUARTZ_STAIRS || 
						blockState.getBlock() == Blocks.GLOWSTONE || 
						(
							(
								blockState.getBlock() == Blocks.STONE_SLAB2 || 
								blockState.getBlock() == Blocks.STONE_SLAB || 
								blockState.getBlock() == Blocks.DOUBLE_STONE_SLAB2 || 
								blockState.getBlock() == Blocks.DOUBLE_STONE_SLAB
							) && 
							(byte) blockState.getBlock().getMetaFromState(blockState) == 7
						)
					)
				)
				{	
					e.setCanceled(true); // Don't tp to nether								
					
					int originDimension = e.getEntity().dimension;
					int newDimension = e.getEntity().dimension + 1;
					
					// If the Cartographer dimension is enabled and this is a chiseled quartz block then tp to Cartographer
					boolean isCartographerPortal = cartographerEnabled && blockState.getBlock() == Blocks.QUARTZ_BLOCK && (byte) blockState.getBlock().getMetaFromState(blockState) == 1;
					if(isCartographerPortal)
					{
						newDimension = Cartographer.CartographerDimension;
					} else {									
						while(newDimension == 1 || !DimensionManager.isDimensionRegistered(newDimension) || (cartographerEnabled && newDimension == Cartographer.CartographerDimension))
						{
							newDimension++;
							if(newDimension > Long.SIZE << 4)
							{
								newDimension = 0;
							}
						}
					}
	
					if(newDimension == 0 && e.getEntity().dimension == 0)
					{
						// No custom dimensions exist, destroy the portal
						e.getEntity().getEntityWorld().setBlockToAir(e.getEntity().getPosition());
						return;
					}
									
					// Register the portal to the world's portals list
			    	TCBlockPortal.placeInExistingPortal(playerPos);
			    		
			    	TCTeleporter.changeDimension(newDimension, ((EntityPlayerMP)e.getEntity()));
			    	//((EntityPlayerMP)e.getEntity()).changeDimension(newDimension);
					
			    	// If coming from main world then update Cartographer map at last player position (should remove head+banner from Cartographer map)
					if(originDimension == 0 && cartographerEnabled)
					{
						//LocalWorld localWorld = TerrainControl.getEngine().getWorld(world.getWorldInfo().getWorldName());
						//if(localWorld != null)
						{
							Cartographer.CreateBlockWorldMapAtSpawn(ChunkCoordinate.fromBlockCoords(playerPos.getX(), playerPos.getZ()), true);
						}
					}
				}
			}
		}
	}	
}
