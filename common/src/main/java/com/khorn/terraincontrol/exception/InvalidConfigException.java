package com.khorn.terraincontrol.exception;

@SuppressWarnings("serial") // No need to serialize this
public class InvalidConfigException extends Exception
{

    public InvalidConfigException(String string)
    {
        super(string);
    }

}
