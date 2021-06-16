package com.pg85.otg.util.materials;

class MaterialSetEntry
{
	private LocalMaterialBase material;

	MaterialSetEntry(LocalMaterialBase material)
	{
		this.material = material;
	}

	@Override
	public boolean equals(Object other)
	{
		// Uses hashCode, as it is guaranteed to be unique for this class
		if (other instanceof MaterialSetEntry)
		{
			return other.hashCode() == hashCode();
		}
		return false;
	}

	/**
	 * Gets the hashCode of this entry, which is equal to either
	 * {@link LocalMaterialData#hashCode()} or
	 * {@link LocalMaterialData#hashCodeWithoutBlockData()}. This means that
	 * the hashCode is unique.
	 *
	 * @return The unique hashCode.
	 */
	@Override
	public int hashCode()
	{
		return this.material.hashCode();
	}

	@Override
	public String toString()
	{
		return this.material.toString();
	}

	public LocalMaterialBase getMaterial()
	{
		return this.material;
	}
	
	/**
	 * Rotates this check 90 degrees. If block data was ignored in this check,
	 * it will still be ignored, otherwise the block data will be rotated too.
	 * 
	 * @return The rotated check.
	 */
	MaterialSetEntry rotate()
	{
		if (this.material.isTag())
		{
			return new MaterialSetEntry(this.material);
		} else {
			// Rotate block data, to maintain check correctness
			return new MaterialSetEntry(((LocalMaterialData)this.material).rotate());
		}
	}
}
