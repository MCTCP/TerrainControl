package com.pg85.otg.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;

public class CompressionUtils
{
	public static byte[] compress(byte[] data, ILogger logger) throws IOException
	{  
		Deflater deflater = new Deflater();  
		deflater.setInput(data);  
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);	
		deflater.finish();  
		byte[] buffer = new byte[1024];	
		while (!deflater.finished())
		{  
			int count = deflater.deflate(buffer); // returns the generated code... index  
			outputStream.write(buffer, 0, count);	
		}  
		outputStream.close();  
		byte[] output = outputStream.toByteArray();
		if(logger.getSpawnLogEnabled())
		{
			logger.log(LogMarker.INFO, "Original: " + data.length / 1024 + " Kb");  
			logger.log(LogMarker.INFO, "Compressed: " + output.length / 1024 + " Kb");
		}
		return output;  
	}

	public static byte[] decompress(byte[] data) throws IOException, DataFormatException
	{  
		Inflater inflater = new Inflater();	
		inflater.setInput(data);  
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);  
		byte[] buffer = new byte[1024];  
		while (!inflater.finished())
		{  
			int count = inflater.inflate(buffer);  
			outputStream.write(buffer, 0, count);  
		}  
		outputStream.close();  
		byte[] output = outputStream.toByteArray();
		//OTG.log(LogMarker.INFO, "Original: " + data.length);  
		//OTG.log(LogMarker.INFO, "Decompressed: " + output.length);
		return output;  
	}
}
