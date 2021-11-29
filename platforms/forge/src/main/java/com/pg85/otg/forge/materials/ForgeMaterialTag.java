package com.pg85.otg.forge.materials;

import java.util.Optional;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.materials.LocalMaterialTag;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

public class ForgeMaterialTag extends LocalMaterialTag
{
	public static LocalMaterialTag ofString(String name)
	{ 
		// If otg: or no domain was supplied, try OTG tags.
		Optional<? extends INamedTag<Block>> optTag;
		if(!name.contains(":") || name.startsWith(Constants.MOD_ID_SHORT + ":"))
		{
			final ResourceLocation otgResourceLocation;
			try
			{
				otgResourceLocation = new ResourceLocation(Constants.MOD_ID_SHORT + ":" + name.trim().toLowerCase().replace(Constants.MOD_ID_SHORT + ":", ""));
				optTag = BlockTags.getWrappers().stream().filter(a -> a.getName().equals(otgResourceLocation)).findFirst();
				if(optTag.isPresent())
				{
					return new ForgeMaterialTag(optTag.get(), otgResourceLocation.toString());
				}
			} catch(ResourceLocationException ex) { }
		}
		
		final ResourceLocation resourceLocation;
		try
		{
			resourceLocation = new ResourceLocation(name.trim().toLowerCase());
			optTag = BlockTags.getWrappers().stream().filter(a -> a.getName().equals(resourceLocation)).findFirst();
			if(optTag.isPresent())
			{
				return new ForgeMaterialTag(optTag.get(), resourceLocation.toString());
			}
		} catch(ResourceLocationException ex) { }
		
		return null;
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
		return this.name;
	}
}
