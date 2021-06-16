package com.pg85.otg.util.materials;

/**
 * Represents one of Minecraft's materials.
 * Immutable.
 */
public abstract class LocalMaterialBase
{
	// Used instead of instanceof to check between materials/tags
	// TODO: Make this prettier, avoid requiring type checks altogether.
	public abstract boolean isTag();
	
	public abstract String toString();
}
