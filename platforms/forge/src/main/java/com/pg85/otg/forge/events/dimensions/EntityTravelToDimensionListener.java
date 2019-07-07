package com.pg85.otg.forge.events.dimensions;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.dimensions.OTGBlockPortal;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.util.ForgeMaterialData;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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

			ArrayList<LocalWorld> forgeWorlds = ((ForgeEngine)OTG.getEngine()).getAllWorlds();
			int destinationDim = 0;
			boolean bPortalMaterialFound = false;

			ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(blockState);

			ForgeWorld overWorld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
			boolean bFound = false;
			if(overWorld == null) // If overworld is null then it's a vanilla overworld
			{
				DimensionConfig dimConfig = OTG.getDimensionsConfig().Overworld;
				ArrayList<LocalMaterialData> portalMaterials = dimConfig.Settings.GetDimensionPortalMaterials();

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
						}
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

				if(newDimension == 0 && e.getEntity().dimension == 0)
				{
					// No custom dimensions exist, destroy the portal
					e.getEntity().getEntityWorld().setBlockToAir(e.getEntity().getPosition());
					return;
				}

				// Register the portal to the world's portals list
		    	OTGBlockPortal.placeInExistingPortal(originDimension, playerPos);

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
