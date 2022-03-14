package com.pg85.otg.forge.util;

import com.pg85.otg.interfaces.IModLoadedChecker;
import net.minecraftforge.fml.ModList;

public class ForgeModLoadedChecker implements IModLoadedChecker
{
	@Override
	public boolean isModLoaded(String mod)
	{
		return ModList.get().isLoaded(mod);
	}
}
