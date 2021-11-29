package com.pg85.otg.forge.materials;

import java.util.Optional;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.materials.LocalMaterialTag;

import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ResourceLocationException;

public class ForgeMaterialTag extends LocalMaterialTag
{
	public static LocalMaterialTag ofString(String name)
	{ 
		// If otg: or no domain was supplied, try OTG tags.
		Optional<? extends Named<Block>> optTag;
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
	private final Tag<Block> blockTag;

	private ForgeMaterialTag(Tag<Block> blockTag, String name)
	{
		this.blockTag = blockTag;
		this.name = name;
	}

	public Tag<Block> getTag()
	{
		return this.blockTag;
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
