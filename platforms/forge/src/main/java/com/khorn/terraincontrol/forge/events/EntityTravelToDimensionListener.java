package com.khorn.terraincontrol.forge.events;

import java.util.ArrayList;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.ForgeMaterialData;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.forge.generator.TXBlockPortal;
import com.khorn.terraincontrol.forge.generator.TXTeleporter;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityTravelToDimensionListener
{
	@SubscribeEvent
	public void entityTravelToDimension(EntityTravelToDimensionEvent e)
	{	
		boolean cartographerEnabled = ((ForgeEngine)TerrainControl.getEngine()).getCartographerEnabled();

		if(e.getDimension() == -1)
		{							
			// If going to the nether
			//if(e.getEntity() instanceof EntityPlayer)
			{					
				// Make sure the player is above a portal block so we can check if this is a Quartz portal
				if(!ForgeMaterialData.ofMinecraftBlockState(e.getEntity().getEntityWorld().getBlockState(e.getEntity().getPosition())).toDefaultMaterial().equals(DefaultMaterial.PORTAL))
				{
					e.getEntity().timeUntilPortal = 0;
					e.setCanceled(true);
					return;
				}

				Entity sender = e.getEntity();
				BlockPos playerPos = new BlockPos(sender.getPosition());
				World world = sender.getEntityWorld();

				BlockPos pos = new BlockPos(sender.getPosition());
				IBlockState blockState = world.getBlockState(pos);
				while(!blockState.getMaterial().isSolid() && pos.getY() > 0)
				{
					pos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
					blockState = world.getBlockState(pos);
				}

				ArrayList<LocalWorld> forgeWorlds = ((ForgeEngine)TerrainControl.getEngine()).getAllWorlds();
				int destinationDim = 0;
				boolean bPortalMaterialFound = false;
				
				ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(blockState);
									
				for(LocalWorld localWorld : forgeWorlds)
				{
					ForgeWorld forgeWorld = (ForgeWorld)localWorld;
					ArrayList<LocalMaterialData> portalMaterials = forgeWorld.getConfigs().getWorldConfig().DimensionPortalMaterials;
				
					boolean bIsPortalMaterial = false;	
					for(LocalMaterialData portalMaterial : portalMaterials)
					{
						if(material.toDefaultMaterial().equals(portalMaterial.toDefaultMaterial()) && material.getBlockData() == portalMaterial.getBlockData())
						{
							bIsPortalMaterial = true;
							bPortalMaterialFound = true;
							break;
						}
					}
					if(bIsPortalMaterial)
					{
						if(forgeWorld.getWorld().provider.getDimension() != e.getEntity().dimension)
						{
							destinationDim = forgeWorld.getWorld().provider.getDimension();
							break;
						} else {
							// Portal material is the same as this dim's own material, default to OverWorld.
							destinationDim = 0;
						}
					}
				}
				
				if(!bPortalMaterialFound && !material.toDefaultMaterial().equals(DefaultMaterial.OBSIDIAN))
				{
					// No custom dimensions exist, destroy the portal
					e.getEntity().getEntityWorld().setBlockToAir(e.getEntity().getPosition());
					e.setCanceled(true); // Don't tp to nether
					return;
				}

				if(pos.getY() > 0 && bPortalMaterialFound)
				{
					e.setCanceled(true); // Don't tp to nether

					int originDimension = e.getEntity().dimension;
					int newDimension = destinationDim;

					// If the Cartographer dimension is enabled and this is a chiseled quartz block then tp to Cartographer
					boolean isCartographerPortal = cartographerEnabled && blockState.getBlock() == Blocks.QUARTZ_BLOCK && (byte) blockState.getBlock().getMetaFromState(blockState) == 1;
					if(isCartographerPortal)
					{
						newDimension = Cartographer.CartographerDimension;
					}

					if(newDimension == 0 && e.getEntity().dimension == 0)
					{
						// No custom dimensions exist, destroy the portal
						e.getEntity().getEntityWorld().setBlockToAir(e.getEntity().getPosition());
						return;
					}
									
					// Register the portal to the world's portals list
			    	TXBlockPortal.placeInExistingPortal(originDimension, playerPos);
			    		
    				if(e.getEntity() instanceof EntityPlayerMP)
    				{
    					TXTeleporter.changeDimension(newDimension, (EntityPlayerMP)e.getEntity());
					} else {
						TXTeleporter.changeDimension(newDimension, e.getEntity());
    				}
					
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
