package com.khorn.terraincontrol.util;

public class MathHelper
{
    private static float[] a = new float[65536];

    public static float sqrt(float paramFloat) {
        return (float) Math.sqrt(paramFloat);
    }

    public static float sin(float paramFloat)
    {
        return a[((int)(paramFloat * 10430.378F) & 0xFFFF)];
    }

    public static float cos(float paramFloat) {
        return a[((int)(paramFloat * 10430.378F + 16384.0F) & 0xFFFF)];
    }
    public static int floor(double d0) {
        int i = (int) d0;

        return d0 < (double) i ? i - 1 : i;
    }

    public static long floor_double_long(double d)
    {
        long l = (long)d;
        return d >= (double)l ? l : l - 1L;
    }


    static
    {
        for (int i = 0; i < 65536; i++)
            a[i] = (float)Math.sin(i * 3.141592653589793D * 2.0D / 65536.0D);
    }
}
