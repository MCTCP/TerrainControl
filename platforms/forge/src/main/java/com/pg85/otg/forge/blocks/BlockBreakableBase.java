package com.pg85.otg.forge.blocks;

import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.OTGPlugin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public class BlockBreakableBase extends BlockBreakable implements IHasModel 
{
	public BlockBreakableBase(String name, Material material)
	{
		super(material, false);
		this.setRegistryName(new ResourceLocation(PluginStandardValues.MOD_ID, name));
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		
		ModBlocks.Blocks.add(this);
	}
	
	@Override
	public void registerModels()
	{
		//OTGPlugin.Proxy.registerItemRender(Item.getItemFromBlock(this), 0, "inventory");
	}
}
