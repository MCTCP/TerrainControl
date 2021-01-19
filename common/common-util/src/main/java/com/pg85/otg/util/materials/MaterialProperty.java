package com.pg85.otg.util.materials;

public class MaterialProperty<T>
{
	public final String name;

	public MaterialProperty(String name) {
		this.name = name;
	}

	@Override
	public String toString()
	{
		return "MaterialProperty(" + this.name + ")";
	}
}
