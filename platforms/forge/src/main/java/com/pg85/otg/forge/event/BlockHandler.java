package com.pg85.otg.forge.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.dimensions.portals.OTGPortalBlock;
import com.pg85.otg.forge.dimensions.portals.OTGPortalColors;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

// Only used for portal ignition logic atm.
@EventBusSubscriber(modid = Constants.MOD_ID_SHORT)
public class BlockHandler
{
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		if(
			event.getWorld().getServer() != null &&
			event.getWorld() instanceof ServerLevel &&
			event.getWorld().dimension() != Level.END &&
			event.getWorld().dimension() != Level.NETHER && 
			(
				event.getWorld().dimension() == Level.OVERWORLD ||
				((ServerLevel)event.getWorld()).getChunkSource().generator instanceof OTGNoiseChunkGenerator
			)
		)
		{
			// TODO: Optimise this, may cause lag doing this for every right-click?
			BlockHitResult hitVec = event.getHitVec();
			BlockPos pos = hitVec.getBlockPos().relative(hitVec.getDirection());
			Collection<ServerLevel> worlds = (Collection<ServerLevel>) event.getWorld().getServer().getAllLevels();
			worlds = worlds.stream().sorted((a,b) -> a.dimension().location().toString().compareTo(b.dimension().location().toString())).collect(Collectors.toList());
			ArrayList<String> usedColors = new ArrayList<>();
			for(ServerLevel world : worlds)
			{
				if(
					world.dimension() != Level.END && 
					world.dimension() != Level.NETHER && 
					world.dimension() != Level.OVERWORLD &&
					world.getChunkSource().generator instanceof OTGNoiseChunkGenerator
				)
				{
					OTGNoiseChunkGenerator generator = ((OTGNoiseChunkGenerator)world.getChunkSource().generator);	
					Item ignitionItem = null;
					try
					{
						ignitionItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(generator.getPortalIgnitionSource()));
					}
					catch(ResourceLocationException ex) { }
					if(ignitionItem == null)
					{
						ignitionItem = Items.FLINT_AND_STEEL;
					}

					String portalColor = generator.getPortalColor().toLowerCase().trim();
					while(usedColors.contains(portalColor))
					{
						portalColor = OTGPortalColors.getNextPortalColor(portalColor);	
					}
					usedColors.add(portalColor);

					if (event.getItemStack().getItem() == ignitionItem)
					{
						RegistryObject<OTGPortalBlock> otgPortalBlock = OTGPortalColors.getPortalBlockByColor(portalColor);				
						List<LocalMaterialData> portalBlocks = generator.getPortalBlocks();
						if (OTGPortalBlock.checkForPortal((ServerLevel)event.getWorld(), pos, event.getPlayer(), event.getHand(), event.getItemStack(), otgPortalBlock, portalBlocks)) 
						{
							event.setCanceled(true);
							return;
						}
					}
				}
			}
		}
	}
}
