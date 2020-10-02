package com.pg85.otg.util.helpers;

public class MathHelper
{
    private static float[] A = new float[65536];

    private MathHelper()
    {
    }
    
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

    public static long floor_double_long(double d)
    {
        long l = (long) d;
        return d >= l ? l : l - 1L;
    }

    public static int abs(int number)
    {
        if (number > 0)
        {
            return number;
        } else
        {
            return -number;
        }
    }

    static
    {
        for (int i = 0; i < 65536; i++)
            A[i] = (float) Math.sin(i * 3.141592653589793D * 2.0D / 65536.0D);
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
