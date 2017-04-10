package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.TCWorldType;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.forge.generator.TCBlockPortal;

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
			boolean cartographerEnabled = ((ForgeEngine)TerrainControl.getEngine()).getCartographerEnabled();
			boolean dimensionsEnabled = ((ForgeEngine)TerrainControl.getEngine()).getDimensionsEnabled();		
	
			if(cartographerEnabled || dimensionsEnabled)
			{		
				if(
					event.getEntity().dimension != 1 && 
					event.getEntity().dimension != -1 && 
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
		
					if(TCBlockPortal.trySpawnPortal(event.getWorld(), blockInFront, true))
					{
						event.setCanceled(true);
						// Register the portal to the world's portals list
				    	TCBlockPortal.placeInExistingPortal(blockInFront);
					}
					// Make sure obsidian portals work in custom dimensions even though custom dimensions do not have dimensionType = DimensionType.OVERWORLD
					else if(event.getWorld().provider.getDimension() > 1 && event.getWorld().getWorldType() instanceof TCWorldType)
					{
						if(TCBlockPortal.trySpawnPortal(event.getWorld(), blockInFront, false))
						{
							event.setCanceled(true);
							// Register the portal to the world's portals list
					    	TCBlockPortal.placeInExistingPortal(blockInFront);
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
}
