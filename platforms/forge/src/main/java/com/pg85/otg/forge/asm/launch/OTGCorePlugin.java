package com.pg85.otg.forge.asm.launch;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@TransformerExclusions(value = { "com.pg85.otg.forge.asm" })
public class OTGCorePlugin implements IFMLLoadingPlugin
{
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{ "com.pg85.otg.forge.asm.OTGClassTransformer"};
	}

	@Override
	public String getModContainerClass()
	{
		return "com.pg85.otg.forge.asm.launch.OTGASMModContainer";
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) { }

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}