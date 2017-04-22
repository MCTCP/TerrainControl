package com.khorn.terraincontrol.bukkit;

public class NBTException extends Exception
{
	static final long serialVersionUID = 0; // TODO: Why is declaring this necessary?!
	
    public NBTException(String message)
    {
        super(message);
    }
}