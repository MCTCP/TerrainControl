package com.pg85.otg.util.helpers;

public class MathHelper
{
	private static float[] A = new float[65536];

	private MathHelper() { }
	
	public static float sqrt(float paramFloat)
	{
		return (float) Math.sqrt(paramFloat);
	}

	public static float sin(float paramFloat)
	{
		return A[((int) (paramFloat * 10430.378F) & 0xFFFF)];
	}

	public static float cos(float paramFloat)
	{
		return A[((int) (paramFloat * 10430.378F + 16384.0F) & 0xFFFF)];
	}

	public static int floor(double d0)
	{
		int i = (int) d0;
		return d0 < i ? i - 1 : i;
	}

	public static long lfloor(double d)
	{
		long l = (long) d;
		return d >= l ? l : l - 1L;
	}

	public static int abs(int number)
	{
		if (number > 0)
		{
			return number;
		} else {
			return -number;
		}
	}
	
	public static double smoothstep(double d)
	{
		return d * d * d * (d * (d * 6.0D - 15.0D) + 10.0D);
	}

	public static double lerp(double delta, double start, double end)
	{
		return start + delta * (end - start);
	}

	private static double lerp2(double deltaX, double deltaY, double val00, double val10, double val01, double val11)
	{
		return lerp(deltaY, lerp(deltaX, val00, val10), lerp(deltaX, val01, val11));
	}

	public static double lerp3(double deltaX, double deltaY, double deltaZ, double val000, double val100, double val010, double val110, double val001, double val101, double val011, double val111)
	{
		return lerp(deltaZ, lerp2(deltaX, deltaY, val000, val100, val010, val110), lerp2(deltaX, deltaY, val001, val101, val011, val111));
	}	

	static
	{
		for (int i = 0; i < 65536; i++)
		{
			A[i] = (float) Math.sin(i * 3.141592653589793D * 2.0D / 65536.0D);
		}
	}

	public static int ceil(float floatNumber)
	{
		int truncated = (int) floatNumber;
		return floatNumber > truncated ? truncated + 1 : truncated;
	}

	public static int clamp(int check, int min, int max)
	{
		return check > max ? max : (check < min ? min : check);
	}
	
	public static double clamp(double check, double min, double max)
	{
		return check > max ? max : (check < min ? min : check);
	}

	public static double clampedLerp(double start, double end, double delta)
	{
		if (delta < 0.0D)
		{
			return start;
		} else {
			return delta > 1.0D ? end : lerp(delta, start, end);
		}
	}

	public static int smallestEncompassingPowerOfTwo(int value)
	{
		int i = value - 1;
		i |= i >> 1;
		i |= i >> 2;
		i |= i >> 4;
		i |= i >> 8;
		i |= i >> 16;
		return i + 1;
	}

	public static long toLong(int x, int z)
	{
		return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
	}

	public static int getXFromLong(long val)
	{
		return (int)(val & 4294967295L);
	}

	public static int getZFromLong(long val)
	{
		return (int)(val >>> 32 & 4294967295L);
	}

	public static double fastInverseSqrt(double x) {
		double d = 0.5D * x;
		long l = Double.doubleToRawLongBits(x);
		l = 6910469410427058090L - (l >> 1);
		x = Double.longBitsToDouble(l);
		x *= 1.5D - d * x * x;
		return x;
	}

	public static long mixSeed(long seed, long salt)
	{
		seed *= seed * 6364136223846793005L + 1442695040888963407L;
		seed += salt;
		return seed;
	}	
	
	/*
	 * Modulus, rather than java's modulo (%)
	 * which does a remainder operation.
	 */
	public static int mod(int x, int y)
	{
		int result = x % y;
		if (result < 0)
		{
			result += y;
		}
		return result;
	}
	
	public static boolean tryParseInt(String value)
	{  
		try {  
			Integer.parseInt(value);  
			return true;  
		 } catch (NumberFormatException e) {  
			return false;  
		 }  
	}
}
