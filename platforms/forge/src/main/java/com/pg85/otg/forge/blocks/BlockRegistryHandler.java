package com.pg85.otg.forge.blocks;

import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.blocks.portal.TileEntityPortal;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@EventBusSubscriber
public class BlockRegistryHandler
{
	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(ModBlocks.Blocks.toArray(new Block[0]));
		GameRegistry.registerTileEntity(TileEntityPortal.class, new ResourceLocation(PluginStandardValues.MOD_ID, "tile_entity_portal"));
	}
	
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent event)
	{
		for(Block block : ModBlocks.Blocks)
		{
			if(block instanceof IHasModel)
			{
				((IHasModel)block).registerModels();
			}
		}
	}
}
