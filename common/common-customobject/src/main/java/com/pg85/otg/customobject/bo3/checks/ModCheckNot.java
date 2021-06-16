package com.pg85.otg.customobject.bo3.checks;

import com.pg85.otg.util.interfaces.IModLoadedChecker;

public class ModCheckNot extends ModCheck
{
	@Override
	public String makeString()
	{
		return makeString("ModCheckNot");
	}
	
	public boolean evaluate(IModLoadedChecker modLoadedChecker)
	{
		return !super.evaluate(modLoadedChecker);
	}
}
