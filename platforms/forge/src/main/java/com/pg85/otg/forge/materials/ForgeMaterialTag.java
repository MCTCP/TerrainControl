package com.pg85.otg.forge.materials;

import com.pg85.otg.util.materials.LocalMaterialTag;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

public class ForgeMaterialTag extends LocalMaterialTag
{
	public static LocalMaterialTag ofString(String name)
	{
		final ResourceLocation resourceLocation;
		try
		{		
			resourceLocation = new ResourceLocation(name.trim().toLowerCase());			
		} catch(ResourceLocationException ex) {
			return null;
		}
		ITag<Block> blockTag = BlockTags.getWrappers().stream().filter(n -> n.getName().equals(resourceLocation)).findFirst().orElse(null);
		return blockTag == null ? null : new ForgeMaterialTag(blockTag);
	}
	
	private ITag<Block> blockTag;
	
	private ForgeMaterialTag(ITag<Block> blockTag)
	{
		this.blockTag = blockTag;
	}

	public ITag<Block> getTag()
	{
		return this.blockTag;
	}
}
