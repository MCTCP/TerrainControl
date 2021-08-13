package com.pg85.otg.util.materials;

/**
 * Represents one of Minecraft's materials.
 * Immutable.
 */
public abstract class LocalMaterialBase
{
	public abstract boolean isTag();
	
	public abstract String toString();

	public abstract boolean matches(LocalMaterialData material);
}
