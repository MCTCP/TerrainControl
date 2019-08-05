package com.pg85.otg.util.helpers;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;

public class StreamHelper
{

	public static void writeStringToStream(DataOutput stream, String value) throws IOException
	{
	    stream.writeBoolean(value == null);
	    if(value != null)
	    {
		    byte[] bytes = (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
		    stream.writeShort(bytes.length);
		    stream.write(bytes);
	    }
	}

	public static String readStringFromStream(DataInputStream stream) throws IOException
	{
		boolean isNull = stream.readBoolean();
		if(isNull)
		{
			return null;
		}
		
		short length = stream.readShort();
	    byte[] chars = new byte[length];
	    if(length > 0)
	    {
		    if (stream.read(chars, 0, chars.length) != chars.length)
		    {
		        throw new EOFException();
		    }
		    return new String(chars);
	    } else {
    		return "";
	    }
	}
	
	public static String readStringFromBuffer(MappedByteBuffer buffer) throws IOException
	{
		boolean isNull = buffer.get() != 0;
		if(isNull)
		{
			return null;
		}
		
		short length = buffer.getShort();
	    byte[] chars = new byte[length];
	    if(length > 0)
	    {
	    	try
	    	{
	    		buffer.get(chars, 0, chars.length);
	    	}
	    	catch(BufferUnderflowException ex)
	    	{
	    		throw new EOFException();
	    	}
		    return new String(chars);
	    } else {
    		return "";
	    }
	}
}
