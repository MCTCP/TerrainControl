package com.pg85.otg.forge.events.dimensions;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.dimensions.OTGBlockPortal;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.util.ForgeMaterialData;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServerMulti;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityTravelToDimensionListener
{
	@SubscribeEvent
	public void entityTravelToDimension(EntityTravelToDimensionEvent e)
	{
		if(e.getDimension() == -1)
		{
			World entityWorld = e.getEntity().getEntityWorld();
			
			// Find the nearest portal to the player.
			// Hopefully the search radius is large enough to work for fast moving players, could improve that.
			boolean portalFound = false;
			BlockPos entityPos = e.getEntity().getPosition();
			BlockPos closestPortalPos = null;
			for(int x = -2; x < 3; x++)
			{
				for(int z = -2; z < 3; z++)
				{
					for(int y = -2; y < 4; y++)
					{
						if(ForgeMaterialData.ofMinecraftBlockState(entityWorld.getBlockState(new BlockPos(entityPos.getX() + x, entityPos.getY() + y, entityPos.getZ() + z))).toDefaultMaterial().equals(DefaultMaterial.PORTAL))
						{
							if(closestPortalPos == null || Math.abs(entityPos.getX() + x) + Math.abs(entityPos.getY() + y) + Math.abs(entityPos.getZ() + z) < Math.abs(entityPos.getX() - closestPortalPos.getX()) + Math.abs(entityPos.getY() - closestPortalPos.getY()) + Math.abs(entityPos.getZ() - closestPortalPos.getZ()))
							{
								closestPortalPos = new BlockPos(entityPos.getX() + x, entityPos.getY() + y, entityPos.getZ() + z);								
							} 
							portalFound = true;
						}
					}
				}
			}
			
			OTG.log(LogMarker.INFO, "PortalFound: " + portalFound);
			if(!portalFound)
			{
				return;
			}		

			// Find portal material
			BlockPos playerPortalMaterialBlockPos = new BlockPos(closestPortalPos);
			IBlockState blockState = entityWorld.getBlockState(playerPortalMaterialBlockPos);
			while(ForgeMaterialData.ofMinecraftBlockState(blockState).toDefaultMaterial() == DefaultMaterial.PORTAL && playerPortalMaterialBlockPos.getY() > 0)
			{
				playerPortalMaterialBlockPos = new BlockPos(playerPortalMaterialBlockPos.getX(), playerPortalMaterialBlockPos.getY() - 1, playerPortalMaterialBlockPos.getZ());
				blockState = entityWorld.getBlockState(playerPortalMaterialBlockPos);
			}
			
			OTG.log(LogMarker.INFO, "PortalMaterial: " + ForgeMaterialData.ofMinecraftBlockState(blockState).toDefaultMaterial().toString());
			
			// Find portal material for OTG dimensions and see if they match
			ArrayList<LocalWorld> forgeWorlds = ((ForgeEngine)OTG.getEngine()).getAllWorlds();
			int destinationDim = 0;
			boolean bOTGPortalFound = false;

			ForgeMaterialData playerPortalMaterial = ForgeMaterialData.ofMinecraftBlockState(blockState);

			ForgeWorld overWorld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
			boolean bFound = false;
			if(overWorld == null) // If overworld is null then it's a vanilla overworld
			{
				DimensionConfig dimConfig = OTG.getDimensionsConfig().Overworld;
				ArrayList<LocalMaterialData> portalMaterials = dimConfig.Settings.GetDimensionPortalMaterials();

				boolean bIsPortalMaterial = false;
				for(LocalMaterialData portalMaterial : portalMaterials)
				{
					if(playerPortalMaterial.toDefaultMaterial().equals(portalMaterial.toDefaultMaterial()) && playerPortalMaterial.getBlockData() == portalMaterial.getBlockData())
					{
						bIsPortalMaterial = true;
						bOTGPortalFound = true;
						OTG.log(LogMarker.INFO, "OTG PortalMaterial found: " + portalMaterial.toDefaultMaterial().toString());
						break;
					}
				}
				if(bIsPortalMaterial)
				{
					if(e.getEntity().dimension != 0)
					{
						destinationDim = 0;
						bFound = true;
					} else {
						// Portal material is the same as this dim's own material, default to OverWorld.
						destinationDim = 0;
					}
				}
			}
			if(!bFound)
			{
				for(LocalWorld localWorld : forgeWorlds)
				{
					ForgeWorld forgeWorld = (ForgeWorld)localWorld;
					DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName());
					ArrayList<LocalMaterialData> portalMaterials = dimConfig.Settings.GetDimensionPortalMaterials();
	
					boolean bIsPortalMaterial = false;
					for(LocalMaterialData portalMaterial : portalMaterials)
					{
						if(playerPortalMaterial.toDefaultMaterial().equals(portalMaterial.toDefaultMaterial()) && playerPortalMaterial.getBlockData() == portalMaterial.getBlockData())
						{
							bIsPortalMaterial = true;
							bOTGPortalFound = true;
							break;
						}
					}
					if(bIsPortalMaterial)
					{
						if(forgeWorld.getWorld().provider.getDimension() != e.getEntity().dimension)
						{
							destinationDim = forgeWorld.getWorld().provider.getDimension();
							break;
						}
					}
				}
			}

			if(!bOTGPortalFound)
			{
				// No custom OTG dimensions exists with this material.
				OTG.log(LogMarker.INFO, "No OTG portal material found for portal.");
				return;
			}

			if(playerPortalMaterialBlockPos.getY() > 0 && bOTGPortalFound)
			{
				e.setCanceled(true); // Don't tp to nether

				int originDimension = e.getEntity().dimension;
				int newDimension = destinationDim;

				if(newDimension == 0 && e.getEntity().dimension == 0)
				{
					// No custom dimensions exist, destroy the portal
					entityWorld.setBlockToAir(entityPos);
					return;
				}

				// Register the portal to the world's portals list
		    	OTGBlockPortal.placeInExistingPortal(originDimension, entityPos);

				if(e.getEntity() instanceof EntityPlayerMP)
				{
					OTGTeleporter.changeDimension(newDimension, (EntityPlayerMP)e.getEntity(), true, false);
				} else {
					OTGTeleporter.changeDimension(newDimension, e.getEntity());
				}
			}
		}
	}
}
