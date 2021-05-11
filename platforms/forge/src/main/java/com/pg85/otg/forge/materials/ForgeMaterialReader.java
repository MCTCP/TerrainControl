package com.pg85.otg.forge.materials;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;

public class ForgeMaterialReader implements IMaterialReader
{
	@Override
	public LocalMaterialData readMaterial(String string) throws InvalidConfigException
	{
		return ForgeMaterials.readMaterial(string);
	}
	
	@Override
	public LocalMaterialTag readTag(String string) throws InvalidConfigException
	{
		return ForgeMaterials.readTag(string);
	}
}
