package com.khorn.terraincontrol.util;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

// Some kind of format desription can be found here:
// 1. http://iamacamera.org/default.aspx?id=109
// 2. http://www.selapa.net/swatches/colors/fileformats.php

public final class AdobeSwatchesExchange
{
	private static final byte[] signature = new byte[] {'A', 'S', 'E', 'F'};
	private static final short[] version = new short[] { 0x0001, 0x0000 };
	private static final byte[] defaultColorMode = new byte[] { 'R', 'G', 'B', 0x20 };
	public static final HashMap<String, Integer> load(File file)
	{
		final HashMap<String, Integer> result = new HashMap<>();
		try(DataInputStream dis = new DataInputStream(new FileInputStream(file)))
		{
			byte signatureTest[] = new byte[4];
			// Signature
			dis.read(signatureTest);
			if(!Arrays.equals(signature, signatureTest))
				throw new IOException();
			// Version
			short versionMajor = dis.readShort();
			short versionMinor = dis.readShort();
			// Number of colors
			int chunkNumber = dis.readInt();
			for(int chunk = 0; chunk < chunkNumber; chunk += 1)
				readChunk(dis, result);
		} catch(FileNotFoundException ex) {
		} catch(IOException ex) {
		}
		return result;
	}
	public static final HashMap<String, Integer> load(String filename)
	{
		return load(new File(filename));
	}
	public static boolean save(File file, HashMap<String, Integer> palette, String world)
	{
		if("".equals(world))
			world = null;
		try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(file)))
		{
			dos.write(signature);
			dos.writeShort(version[0]);
			dos.writeShort(version[1]);
			// Group "world" start, palette, Group end
			dos.writeInt(((world != null) ? 2 : 0) + palette.size());
			// Write group start
			if(world != null)
			{
				dos.writeShort(0xC001);
				dos.writeInt(world.length() * 2 + 4);
				writeUTF16Z(dos, world);
			}
			String keys[] = palette.keySet().toArray(new String[palette.size()]);
			Arrays.sort(keys);
			// Write colors
			for(String color : keys)
				writeColorEntry(dos, color, palette.get(color));
			// Write group end
			if(world != null)
			{
				dos.writeShort(0xC002);
				dos.writeInt(0);
			}
			return true;
		} catch(FileNotFoundException ex) {
		} catch(IOException ex) {
		}
		return false;
	}
	public static boolean save(File file, HashMap<String, Integer> palette)
	{
		return save(file, palette, null);
	}
	public static boolean save(String filename, HashMap<String, Integer> palette, String world)
	{
		return save(new File(filename), palette, world);
	}
	public static boolean save(String filename, HashMap<String, Integer> palette)
	{
		return save(new File(filename), palette, null);
	}
	private static void readChunk(DataInputStream dis, HashMap<String, Integer> palette) throws IOException
	{
		final int chunkType = (int)dis.readShort();
		final int chunkLength = dis.readInt();
		switch(chunkType)
		{
			case 0xC001: // Group start
				final String groupName = readUTF16Z(dis);
				break;
			case 0xC002: // Group end
				dis.skip(chunkLength);
				break;
			case 0x0000: // Final chunk
				break;
			case 0x0001: // Color entry
				final String colorName = readUTF16Z(dis);
				final byte colorMode[] = new byte[4];
				dis.read(colorMode);
				final String colorModeName = new String(colorMode, "US-ASCII");
				switch(colorModeName.trim().toUpperCase())
				{
					case "RGB":
						float red   = dis.readFloat();
						float green = dis.readFloat();
						float blue  = dis.readFloat();
						palette.put(colorName, new Color(red, green, blue).getRGB());
						break;
					case "CMYK":
						// TO DO ?
						dis.skipBytes(4 * Float.SIZE);
						break;
					case "LAB":
						// TO DO ?
						dis.skipBytes(3 * Float.SIZE);
						break;
					case "GRAY":
						float gray = dis.readFloat();
						palette.put(colorName, new Color(gray, gray, gray).getRGB());
						break;
				}
				short colorTypeMarker = dis.readShort();
				break;
			default:
				throw new IOException();
		}
	}
	private static void writeColorEntry(DataOutputStream dos, String colorName, int colorValue) throws IOException
	{
		dos.writeShort(0x0001);
		int chunkLength = (2 + colorName.length() * 2 + 2) // Name
			+ 4 // Color model
			+ 3 * (Float.SIZE >> 3) // RGB data
			+ 2; // Marker
		dos.writeInt(chunkLength);
		writeUTF16Z(dos, colorName);
		dos.write(defaultColorMode);
		final Color color = new Color(colorValue);
		float R = color.getRed() / 255.0f;
		float G = color.getGreen()/ 255.0f;
		float B = color.getBlue()/ 255.0f;
		dos.writeFloat(R);
		dos.writeFloat(G);
		dos.writeFloat(B);
		dos.writeShort(0x0002);
	}
	private static String readUTF16Z(DataInputStream dis) throws IOException
	{
		short nameLength = dis.readShort();
		final StringBuilder sb = new StringBuilder(64);
		for(char c = dis.readChar(); c != 0; c = dis.readChar())
			sb.append(c);
		return sb.toString().trim();
	}
	private static void writeUTF16Z(DataOutputStream dos, String string) throws IOException
	{
		dos.writeShort(string.length() + 1);
		for(char c : string.toCharArray())
			dos.writeChar(c);
		dos.writeChar(0);
	}
}