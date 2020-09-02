package com.pg85.otg.forge.events.dimensions;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.blocks.portal.BlockPortalOTG;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.world.ForgeWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
						if(entityWorld.getBlockState(new BlockPos(entityPos.getX() + x, entityPos.getY() + y, entityPos.getZ() + z)).getBlock() instanceof BlockPortalOTG) // TODO: avoid using instanceof so much?
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
			
			if(!portalFound)
			{
				return;
			}		

			// Find portal material
			BlockPos playerPortalMaterialBlockPos = new BlockPos(closestPortalPos);
			IBlockState blockState = entityWorld.getBlockState(playerPortalMaterialBlockPos);
			while(blockState.getBlock() instanceof BlockPortalOTG && playerPortalMaterialBlockPos.getY() > 0) // TODO: Don't use instanceof so much?
			{
				playerPortalMaterialBlockPos = new BlockPos(playerPortalMaterialBlockPos.getX(), playerPortalMaterialBlockPos.getY() - 1, playerPortalMaterialBlockPos.getZ());
				blockState = entityWorld.getBlockState(playerPortalMaterialBlockPos);
			}
					
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
					if(playerPortalMaterial.equals(portalMaterial))
					{
						bIsPortalMaterial = true;
						bOTGPortalFound = true;
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
						if(playerPortalMaterial.equals(portalMaterial))
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
				return;
			}

			if(playerPortalMaterialBlockPos.getY() > 0 && bOTGPortalFound)
			{
				e.setCanceled(true); // Don't tp to nether

				int newDimension = destinationDim;

				if(newDimension == 0 && e.getEntity().dimension == 0)
				{
					// No custom dimensions exist, destroy the portal
					entityWorld.setBlockToAir(entityPos);
					return;
				}

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
