package com.pg85.otg.util.interfaces;

public interface IChunkDecorator
{
	Object getLockingObject();

	boolean isPopulating();

	public void beginSave();

	public void endSave();

	boolean getIsSaveRequired();
}
