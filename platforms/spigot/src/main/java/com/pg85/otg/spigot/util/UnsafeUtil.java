package com.pg85.otg.spigot.util;

import java.lang.reflect.Field;

import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;
import org.bukkit.craftbukkit.v1_16_R3.generator.CustomChunkGenerator;
import sun.misc.Unsafe;

import net.minecraft.server.v1_16_R3.ChunkGenerator;

public class UnsafeUtil
{
	private static Unsafe UNSAFE;

	static
	{
		try
		{
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			UNSAFE = (Unsafe) f.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	public static void setDelegate(ChunkGenerator generator, OTGNoiseChunkGenerator delegate) {
		try
		{
			Field field = CustomChunkGenerator.class.getDeclaredField("delegate");
			long pointer = UNSAFE.objectFieldOffset(field);
			UNSAFE.getAndSetObject(generator, pointer, delegate);
		} catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
	}
}
