package com.pg85.otg.util.interfaces;

public interface IChunkPopulator
{
	Object getLockingObject();

	boolean isPopulating();

	public void beginSave();

	public void endSave();

	boolean getIsSaveRequired();
}
