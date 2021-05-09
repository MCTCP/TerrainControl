package com.pg85.otg.spigot.materials;

import com.pg85.otg.util.materials.LocalMaterialTag;

import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Tag;
import net.minecraft.server.v1_16_R3.TagsBlock;

public class SpigotMaterialTag extends LocalMaterialTag
{
	public static LocalMaterialTag ofString(String name)
	{
		final MinecraftKey resourceLocation;
		try
		{		
			resourceLocation = new MinecraftKey(name.trim().toLowerCase());			
		} catch(Exception ex) {
			// TODO: Find out which exception spigot throws when resourcelocation can't be parsed.
			return null;
		}
		Tag<Block> blockTag = TagsBlock.b().stream().filter(n -> n.a().equals(resourceLocation)).findFirst().orElse(null);
		return blockTag == null ? null : new SpigotMaterialTag(blockTag);
	}
	
	private Tag<Block> blockTag;
	
	private SpigotMaterialTag(Tag<Block> blockTag)
	{
		this.blockTag = blockTag;
	}

	public Tag<Block> getTag()
	{
		return this.blockTag;
	}
}