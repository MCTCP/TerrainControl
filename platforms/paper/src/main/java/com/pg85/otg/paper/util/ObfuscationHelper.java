package com.pg85.otg.paper.util;

import java.lang.reflect.Field;

public final class ObfuscationHelper
{
    private static final boolean IS_DEV;

    static {
        boolean dev;
        try {
            Class.forName("net.minecraft.world.level.biome.BiomeSource");
            dev = true;
        } catch (ClassNotFoundException e) {
            dev = false;
        }

        IS_DEV = dev;
    }

    public static Field getField(Class<?> clazz, String devName, String obfName) throws NoSuchFieldException
    {
        return clazz.getDeclaredField(IS_DEV ? devName : obfName);
    }
}