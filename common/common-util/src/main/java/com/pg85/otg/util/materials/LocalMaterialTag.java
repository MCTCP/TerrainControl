package com.pg85.otg.util.materials;

/**
 * Represents one of Minecraft's material tags.
 * Immutable.
 */
public abstract class LocalMaterialTag extends LocalMaterialBase
{
	@Override
	public boolean isTag()
	{
		return true;
	}
	
	@Override
	public boolean matches(LocalMaterialData material)
	{
		return material.isBlockTag(this);
	}
}
