package com.pg85.otg.forge.events.dimensions;

import com.pg85.otg.forge.dimensions.OTGBlockPortal;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.generator.Cartographer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
					// Allow portals from non-otg overworlds
					event.getWorld().provider.getDimension() == 0 ||
					OTGDimensionManager.IsOTGDimension(event.getWorld().provider.getDimension())
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
	
				if(OTGBlockPortal.trySpawnPortal(event.getWorld(), blockInFront))
				{
					event.setCanceled(true);
					// Register the portal to the world's portals list
			    	OTGBlockPortal.placeInExistingPortal(event.getEntity().dimension, blockInFront);
				}
			}
			else if(event.getEntity().dimension > 0 && event.getEntity().dimension == Cartographer.CartographerDimension)
			{
	        	// TP player to the location they are standing on on the map
	        	Cartographer.TeleportPlayerFromMap((EntityPlayer)event.getEntityPlayer());
			}
		}
	}	
}
