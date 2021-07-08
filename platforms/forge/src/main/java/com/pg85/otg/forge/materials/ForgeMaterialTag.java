package com.pg85.otg.forge.materials;

import com.pg85.otg.constants.Constants;
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
		// If no domain was supplied, first try OTG tags.
		final ResourceLocation otgResourceLocation;
		ITag<Block> blockTag;
		if(!name.contains(":"))
		{
			try
			{
				otgResourceLocation = new ResourceLocation(Constants.MOD_ID_SHORT + ":" + name.trim().toLowerCase());	
			} catch(ResourceLocationException ex) {
				return null;
			}
			blockTag = BlockTags.getAllTags().getTag(otgResourceLocation);
			if(blockTag != null)
			{
				return new ForgeMaterialTag(blockTag, name.trim().toLowerCase());
			}
		}
		final ResourceLocation resourceLocation;
		try
		{
			resourceLocation = new ResourceLocation(name.trim().toLowerCase());	
		} catch(ResourceLocationException ex) {
			return null;
		}
		blockTag = BlockTags.getAllTags().getTag(resourceLocation);
		return blockTag == null ? null : new ForgeMaterialTag(blockTag, name.trim().toLowerCase());
	}

	private final String name;
	private final ITag<Block> blockTag;

	private ForgeMaterialTag(ITag<Block> blockTag, String name)
	{
		this.blockTag = blockTag;
		this.name = name;
	}

	public ITag<Block> getTag()
	{
		return this.blockTag;
	}

	@Override
	public String toString()
	{
		// TODO: Fetch the registry name from the tag object for writing back to config.
		return this.name;
	}
}
