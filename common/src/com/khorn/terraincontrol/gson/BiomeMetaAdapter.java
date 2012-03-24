package com.khorn.terraincontrol.gson;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import net.minecraft.server.BiomeMeta;
import net.minecraft.server.WeightedRandomChoice;

import com.khorn.terraincontrol.lib.gson.JsonDeserializationContext;
import com.khorn.terraincontrol.lib.gson.JsonDeserializer;
import com.khorn.terraincontrol.lib.gson.JsonElement;
import com.khorn.terraincontrol.lib.gson.JsonObject;
import com.khorn.terraincontrol.lib.gson.JsonParseException;
import com.khorn.terraincontrol.lib.gson.JsonSerializationContext;
import com.khorn.terraincontrol.lib.gson.JsonSerializer;

public class BiomeMetaAdapter implements JsonDeserializer<BiomeMeta>, JsonSerializer<BiomeMeta>
{
    private static final String MOB = "mob";
    private static final String WEIGHT = "weight";
    private static final String MIN = "min";
    private static final String MAX = "max";
    
    @Override
    public JsonElement serialize(BiomeMeta src, Type typeOfSrc, JsonSerializationContext context)
    {
        // Create an empty json object to fill and return
        JsonObject ret = new JsonObject();
        
        // Decide the fields
        String entityClass = livingEntityClass2String(obfuGetEntityClass(src));
        int weight = obfuGetWeight(src);
        int min = obfuGetMin(src);
        int max = obfuGetMax(src);
        
        // Set the fields
        ret.addProperty(MOB, entityClass);
        ret.addProperty(WEIGHT, weight);
        ret.addProperty(MIN, min);
        ret.addProperty(MAX, max);
        
        return ret;
    }

    @Override
    public BiomeMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        // This must be a json object
        JsonObject jsonObject = (JsonObject)json;
        
        // Read the values from it
        Class<?> entityClass = string2LivingEntityClass(jsonObject.get(MOB).getAsString());
        int weight = jsonObject.get(WEIGHT).getAsInt();
        int min = jsonObject.get(MIN).getAsInt();
        int max = jsonObject.get(MAX).getAsInt();
        
        // Create a BiomeMeta using non informative/default constructor.
        BiomeMeta ret = new BiomeMeta(null, 0, 0, 0);
        
        // Set the values we read from the json object
        obfuSetEntityClass(ret, entityClass);
        obfuSetWeight(ret, weight);
        obfuSetMin(ret, min);
        obfuSetMax(ret, max);
        
        return ret;
    }
    
    // -------------------------------------------- //
    // LIVING ENTITY CLASS <-> STRING CONVERTION
    // -------------------------------------------- //
    
    public static String livingEntityClass2String(Class<?> livingEntityClass)
    {
        // EntityEnderman, EntityCreeper, EntityOcelot etc...
        String simpleName = livingEntityClass.getSimpleName();
        
        // Return without the "Entity" part
        return simpleName.substring(6);
    }
    
    public static Class<?> string2LivingEntityClass(String string)
    {
        try
        {
            return Class.forName("net.minecraft.server.Entity"+string);
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    // -------------------------------------------- //
    // OBFUSCATION GETTERS AND SETTERS
    // -------------------------------------------- //
    
    /**
     * Note that the allowed entity classes are those extending EntityLiving.
     */
    public static Class<?> obfuGetEntityClass(BiomeMeta biomeMeta)
    {
        return biomeMeta.a;
    }
    public static void obfuSetEntityClass(BiomeMeta biomeMeta, Class<?> val)
    {
        biomeMeta.a = val;
    }
    
    protected static Field weightField;
    public static int obfuGetWeight(BiomeMeta biomeMeta)
    {
        try
        {
            return weightField.getInt(biomeMeta);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
    public static void obfuSetWeight(BiomeMeta biomeMeta, int val)
    {
        try
        {
            weightField.setInt(biomeMeta, val);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static int obfuGetMin(BiomeMeta biomeMeta)
    {
        return biomeMeta.b;
    }
    public static void obfuSetMin(BiomeMeta biomeMeta, int val)
    {
        biomeMeta.b = val;
    }
    
    public static int obfuGetMax(BiomeMeta biomeMeta)
    {
        return biomeMeta.c;
    }
    public static void obfuSetMax(BiomeMeta biomeMeta, int val)
    {
        biomeMeta.c = val;
    }
    
    static
    {  
        try
        {
            weightField = WeightedRandomChoice.class.getDeclaredField("d");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        weightField.setAccessible(true);
    }
}
