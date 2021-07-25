package com.pg85.otg.forge.network;

import java.util.function.IntSupplier;

public interface OTGLoginMessage extends IntSupplier
{
	int getLoginIndex();
	void setLoginIndex(int loginIndex);

	@Override
	default int getAsInt()
	{
		return this.getLoginIndex();
	}
}
