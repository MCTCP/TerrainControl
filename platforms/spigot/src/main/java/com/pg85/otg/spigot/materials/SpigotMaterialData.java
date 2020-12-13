package com.pg85.otg.spigot.materials;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.materials.LocalMaterialData;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

public class SpigotMaterialData extends LocalMaterialData
{
	private BlockData blockData;

	private SpigotMaterialData (BlockData blockData)
	{

		this.blockData = blockData;
	}

	private SpigotMaterialData (BlockData blockData, String raw)
	{
		this.blockData = blockData;
		this.rawEntry = raw;
	}

	private SpigotMaterialData (String raw)
	{
		this.blockData = null;
		this.rawEntry = raw;
	}

	public static LocalMaterialData ofString (String name) throws InvalidConfigException
	{
		return null;
	}

	@Override
	public String getName ()
	{
		return null;
	}

	@Override
	public boolean isLiquid ()
	{
		return this.blockData != null
			   && (this.blockData.getMaterial() == Material.WATER
				   || this.blockData.getMaterial() == Material.LAVA);
	}

	@Override
	public boolean isSolid ()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}

	@Override
	public boolean isEmptyOrAir ()
	{
		return this.blockData == null || this.blockData.getMaterial() == Material.AIR;
	}

	@Override
	public boolean isAir ()
	{
		return this.blockData != null && this.blockData.getMaterial() == Material.AIR;
	}

	@Override
	public boolean isEmpty ()
	{
		return this.blockData == null;
	}

	@Override
	public boolean canSnowFallOn ()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}

	@Override
	public boolean isMaterial (LocalMaterialData material)
	{
		return (this.isBlank && ((SpigotMaterialData) material).isBlank) ||
			   (
					   !this.isBlank &&
					   !((SpigotMaterialData) material).isBlank &&
					   this.blockData.getMaterial() == ((SpigotMaterialData) material).blockData.getMaterial());
	}

	@Override
	public LocalMaterialData withDefaultBlockData ()
	{
		if (this.blockData == null)
		{
			return this;
		}
		return fromMaterial(this.blockData.getMaterial(), rawEntry);
	}

	private LocalMaterialData fromMaterial (Material material, String rawEntry)
	{
		return new SpigotMaterialData(material.createBlockData(), rawEntry);
	}

	@Override
	public boolean equals (Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof SpigotMaterialData))
		{
			return false;
		}
		SpigotMaterialData other = (SpigotMaterialData) obj;

		// TODO: Compare registry names?
		return
				(this.isBlank && other.isBlank) ||
				(
						!this.isBlank &&
						!other.isBlank &&
						this.blockData.equals(other.blockData)
				);
	}

	@Override
	public int hashCode ()
	{
		// TODO: Implement this for 1.16
		return this.blockData == null ? -1 : this.blockData.hashCode();
	}

	@Override
	public boolean canFall ()
	{
		return this.blockData != null && this.blockData.getMaterial().hasGravity();
	}

	@Override
	public boolean hasData ()
	{
		// TODO: Implement this for 1.16
		return false;
	}
}
