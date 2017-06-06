package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.forge.TXWorldType;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.forge.generator.TXBlockPortal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RightClickBlockListener
{	
	@SubscribeEvent
	public void onRightClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event)
	{
		if(!event.getWorld().isRemote) // Server side only
		{
			if(
				(
					event.getEntity().dimension == 0 ||
					DimensionManager.getProviderType(event.getEntity().dimension).getSuffix().equals("OTG")					
				) &&
				event.getItemStack() != null && 
				event.getItemStack().getItem() != null && 
				event.getItemStack().getItem() instanceof ItemFlintAndSteel
			)
			{			
				BlockPos blockInFront = new BlockPos(event.getPos());
				if(event.getFace() == EnumFacing.DOWN)
				{
					blockInFront = new BlockPos(event.getPos().getX(), event.getPos().getY() - 1, event.getPos().getZ());
				}
				if(event.getFace() == EnumFacing.UP)
				{
					blockInFront = new BlockPos(event.getPos().getX(), event.getPos().getY() + 1, event.getPos().getZ());
				}
				if(event.getFace() == EnumFacing.NORTH)
				{
					blockInFront = new BlockPos(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ() - 1);
				}
				if(event.getFace() == EnumFacing.SOUTH)
				{
					blockInFront = new BlockPos(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ() + 1);
				}
				if(event.getFace() == EnumFacing.EAST)
				{
					blockInFront = new BlockPos(event.getPos().getX() + 1, event.getPos().getY(), event.getPos().getZ());
				}
				if(event.getFace() == EnumFacing.WEST)
				{
					blockInFront = new BlockPos(event.getPos().getX() - 1, event.getPos().getY(), event.getPos().getZ());
				}
	
				if(TXBlockPortal.trySpawnPortal(event.getWorld(), blockInFront, true))
				{
					event.setCanceled(true);
					// Register the portal to the world's portals list
			    	TXBlockPortal.placeInExistingPortal(event.getEntity().dimension, blockInFront);
				}
				// Make sure obsidian portals work in custom dimensions even though custom dimensions do not have dimensionType = DimensionType.OVERWORLD
				else if(event.getWorld().provider.getDimension() > 1 && event.getWorld().getWorldType() instanceof TXWorldType)
				{
					if(TXBlockPortal.trySpawnPortal(event.getWorld(), blockInFront, false))
					{
						event.setCanceled(true);
						// Register the portal to the world's portals list
				    	TXBlockPortal.placeInExistingPortal(event.getEntity().dimension, blockInFront);
					}
				}
			}
			else if(event.getEntity().dimension > 1 && event.getEntity().dimension == Cartographer.CartographerDimension)
			{
	        	// TP player to the location they are standing on on the map
	        	Cartographer.TeleportPlayerFromMap((EntityPlayer)event.getEntityPlayer());
			}
		}
	}	
}
