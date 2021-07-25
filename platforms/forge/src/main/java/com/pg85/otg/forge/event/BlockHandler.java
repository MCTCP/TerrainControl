package com.pg85.otg.forge.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.dimensions.portals.OTGPortalBlock;
import com.pg85.otg.forge.dimensions.portals.OTGPortalColors;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
			event.getWorld() instanceof ServerWorld &&
			event.getWorld().dimension() != World.END &&
			event.getWorld().dimension() != World.NETHER && 
			(
				event.getWorld().dimension() == World.OVERWORLD ||
				((ServerWorld)event.getWorld()).getChunkSource().generator instanceof OTGNoiseChunkGenerator
			)
		)
		{
			// TODO: Optimise this, may cause lag doing this for every right-click?
			BlockRayTraceResult hitVec = event.getHitVec();
			BlockPos pos = hitVec.getBlockPos().relative(hitVec.getDirection());
			Collection<ServerWorld> worlds = (Collection<ServerWorld>) event.getWorld().getServer().getAllLevels();
			worlds = worlds.stream().sorted((a,b) -> a.dimension().location().toString().compareTo(b.dimension().location().toString())).collect(Collectors.toList());
			ArrayList<String> usedColors = new ArrayList<>();
			for(ServerWorld world : worlds)
			{
				if(
					world.dimension() != World.END && 
					world.dimension() != World.NETHER && 
					world.dimension() != World.OVERWORLD &&
					world.getChunkSource().generator instanceof OTGNoiseChunkGenerator
				)
				{
					Preset preset = ((OTGNoiseChunkGenerator)world.getChunkSource().generator).getPreset();		
	
					Item ignitionItem = null;
					try
					{
						ignitionItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(preset.getWorldConfig().getPortalIgnitionSource()));
					}
					catch(ResourceLocationException ex) { }
					if(ignitionItem == null)
					{
						ignitionItem = Items.FLINT_AND_STEEL;
					}

					String portalColor = preset.getWorldConfig().getPortalColor().toLowerCase().trim();
					while(usedColors.contains(portalColor))
					{
						portalColor = OTGPortalColors.getNextPortalColor(portalColor);	
					}
					usedColors.add(portalColor);

					if (event.getItemStack().getItem() == ignitionItem)
					{
						RegistryObject<OTGPortalBlock> otgPortalBlock = OTGPortalColors.getPortalBlockByColor(portalColor);				
						List<LocalMaterialData> portalBlocks = preset.getWorldConfig().getPortalBlocks();
						if (OTGPortalBlock.checkForPortal((ServerWorld)event.getWorld(), pos, event.getPlayer(), event.getHand(), event.getItemStack(), otgPortalBlock, portalBlocks)) 
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
