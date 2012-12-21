package com.khorn.terraincontrol.bukkit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_4_6.BiomeBase;
import net.minecraft.server.v1_4_6.BiomeMeta;
import net.minecraft.server.v1_4_6.EntityTypes;

import com.khorn.terraincontrol.DefaultMobType;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.util.Txt;

/**
 * This static utility is for obfuscation mappings.
 */
public class Obfu
{
    // -------------------------------------------- //
    // GETTERS AND SETTERS
    // -------------------------------------------- //
    
    @SuppressWarnings("unchecked")
    public static Map<Integer, Class<?>> getEntityTypes_Id2Class()
    {
        return (Map<Integer, Class<?>>) getAsObject(null, fieldEntityTypes_Id2Class);
    }
    
    // -------------------------------------------- //
    // OBFUSCATION
    // -------------------------------------------- //
    
    @SuppressWarnings("unchecked")
    public static List<BiomeMeta> getBiomeBase_MonsterBiomeMetas(BiomeBase obj)
    {
        return (List<BiomeMeta>) getAsObject(obj, fieldBiomeBase_MonsterBiomeMetas);
    }
    
    @SuppressWarnings("unchecked")
    public static List<BiomeMeta> getBiomeBase_CreatureBiomeMetas(BiomeBase obj)
    {
        return (List<BiomeMeta>) getAsObject(obj, fieldBiomeBase_CreatureBiomeMetas);
    }
    
    @SuppressWarnings("unchecked")
    public static List<BiomeMeta> getBiomeBase_WaterCreatureBiomeMetas(BiomeBase obj)
    {
        return (List<BiomeMeta>) getAsObject(obj, fieldBiomeBase_WaterCreatureBiomeMetas);
    }
    
    // -------------------------------------------- //
    // CONVERTERS
    // -------------------------------------------- //
    
    public static Class<?> convertDefaultCreatureType2Class(DefaultMobType type)
    {
        int typeId = type.getTypeId();
        Map<Integer, Class<?>> id2class = Obfu.getEntityTypes_Id2Class();
        return id2class.get(typeId);
    }
    
    public static List<BiomeMeta> convertToBiomeMetaList(List<WeightedMobSpawnGroup> wmsgs)
    {
        List<BiomeMeta> ret = new ArrayList<BiomeMeta>();
        for (WeightedMobSpawnGroup wmsg : wmsgs)
        {
            ret.add(constructBiomeMeta(wmsg));
        }
        return ret;
    }
    
    // -------------------------------------------- //
    // CONSTRUCTORS
    // -------------------------------------------- //
    
    public static BiomeMeta constructBiomeMeta(Class<?> entityClass, int weight, int min, int max)
    {
        return new BiomeMeta(entityClass, weight, min, max);
    }
    public static BiomeMeta constructBiomeMeta(WeightedMobSpawnGroup wmsg)
    {
        Class<?> clazz = convertDefaultCreatureType2Class(wmsg.getDefaultMobType());
        return constructBiomeMeta(clazz, wmsg.getWeight(), wmsg.getMin(), wmsg.getMax());
    }
    
    // -------------------------------------------- //
    // REFLECTION
    // -------------------------------------------- //
    
    protected static Field fieldEntityTypes_Id2Class;
    protected static Field fieldBiomeBase_MonsterBiomeMetas;
    protected static Field fieldBiomeBase_CreatureBiomeMetas;
    protected static Field fieldBiomeBase_WaterCreatureBiomeMetas;
    
    static
    {
        fieldEntityTypes_Id2Class = getDeclaredField(EntityTypes.class, "d");
        
        fieldBiomeBase_MonsterBiomeMetas = getDeclaredField(BiomeBase.class, "J");
        
        fieldBiomeBase_CreatureBiomeMetas = getDeclaredField(BiomeBase.class, "K");
        
        fieldBiomeBase_WaterCreatureBiomeMetas = getDeclaredField(BiomeBase.class, "L");
    }
    
    public static void probeClass(Class<?> clazz)
    {
        Field[] fields = clazz.getDeclaredFields();
        List<String> names = new ArrayList<String>();
        for (Field field : fields)
        {
            names.add(field.getName());
        }
        String className = clazz.getName().replace("net.minecraft.server.", "nms.");
        System.out.println("DECLARED FIELDS FOR \""+className+"\" is "+Txt.implode(names, ", "));
    }
    
    public static Field getDeclaredField(Class<?> clazz, String fieldName)
    {
        Field ret = null;
        try
        {
            ret = clazz.getDeclaredField(fieldName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ret.setAccessible(true);
        return ret;
    }
    
    public static Object getAsObject(Object instance, Field field)
    {
        try
        {
            return field.get(instance);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
}
