package com.pg85.otg.spigot.materials;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.materials.LocalMaterialTag;

import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Tag;
import net.minecraft.server.v1_16_R3.TagsBlock;

public class SpigotMaterialTag extends LocalMaterialTag
{
	public static LocalMaterialTag ofString(String name)
	{
		// If otg: or no domain was supplied, try OTG tags.
		// If no domain was supplied, first try OTG tags.
		if(!name.contains(":") || name.startsWith(Constants.MOD_ID_SHORT + ":"))
		{
			Block[] blockTag = SpigotMaterials.OTG_BLOCK_TAGS.get(name.trim().toLowerCase().replace(Constants.MOD_ID_SHORT + ":", "")); 
			if(blockTag != null)
			{
				return new SpigotMaterialTag(blockTag, Constants.MOD_ID_SHORT + ":" + name.trim().toLowerCase().replace(Constants.MOD_ID_SHORT + ":", ""));
			}
		}
		final MinecraftKey resourceLocation;
		try
		{
			resourceLocation = new MinecraftKey(name.trim().toLowerCase());	
		} catch(Exception ex) {
			return null;
		}
		Tag<Block> blockTag = TagsBlock.b().stream().filter(n -> n.a().equals(resourceLocation)).findFirst().orElse(null);
		return blockTag == null ? null : new SpigotMaterialTag(blockTag, resourceLocation.toString());
	}
	
	private final String name;
	private final Tag<Block> blockTag;
	private final Block[] otgBlockTag;

	private SpigotMaterialTag(Tag<Block> blockTag, String name)
	{
		this.otgBlockTag = null;
		this.blockTag = blockTag;
		this.name = name;
	}
	
	private SpigotMaterialTag(Block[] otgBlockTag, String name)
	{
		this.otgBlockTag = otgBlockTag;
		this.blockTag = null;
		this.name = name;
	}

	public boolean isOTGTag(Block block)
	{
		if(this.otgBlockTag != null)
		{
			for(Block otgTagBlock : this.otgBlockTag)
			{
				if(otgTagBlock == block)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public Tag<Block> getTag()
	{
		return this.blockTag;
	}
	
	@Override
	public String toString()
	{
		// TODO: Fetch the registry name from the tag object.
		return this.name;
	}
}