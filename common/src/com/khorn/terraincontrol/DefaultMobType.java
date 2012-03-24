package com.khorn.terraincontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public enum DefaultMobType
{
    // The first strings MUST match the strings in nms.EntityTypes and are case sensitive.
    CREEPER(50, "Creeper", "creeper"),
    SKELETON(51, "Skeleton", "skeleton"),
    SPIDER(52, "Spider", "spider"),
    GIANT(53, "Giant", "giant", "giantzombie", "zombiegiant"),
    ZOMBIE(54, "Zombie", "zombie"),
    SLIME(55, "Slime", "slime"),
    GHAST(56, "Ghast", "ghast"),
    PIG_ZOMBIE(57, "PigZombie", "pigzombie", "pig_zombie"),
    ENDERMAN(58, "Enderman", "enderman"),
    CAVE_SPIDER(59, "CaveSpider", "cavespider", "cave_spider"),
    SILVERFISH(60, "Silverfish", "silverfish", "silver_fish"),
    BLAZE(61, "Blaze", "blaze"),
    MAGMA_CUBE(62, "LavaSlime", "lavaslime", "lava_slime", "magmacube", "magma_cube"),
    ENDER_DRAGON(63, "EnderDragon", "enderdragon", "ender_dragon"),
    PIG(90, "Pig", "pig"),
    SHEEP(91, "Sheep", "sheep"),
    COW(92, "Cow", "cow"),
    CHICKEN(93, "Chicken", "chicken"),
    SQUID(94, "Squid", "squid"),
    WOLF(95, "Wolf", "wolf"),
    MUSHROOM_COW(96, "MushroomCow", "mushroomcow", "shroom", "mooshroom", "moshoom", "mcow", "shroomcow"),
    SNOWMAN(97, "SnowMan", "snowman"),
    OCELOT(98, "Ozelot", "ocelot", "ozelot"),
    IRON_GOLEM(99, "VillagerGolem", "irongolem", "iron_golem"),
    VILLAGER(120, "Villager", "villager");

    // INSTANCE FIELDS
    protected final short typeId;
    public short getTypeId() { return this.typeId; }
    
    protected final String formalName;
    public String getFormalName() { return this.formalName; }
    
    protected final String preferedName;
    public String getPreferedName() { return this.preferedName; }
    
    protected final Set<String> allNames;
    public Set<String> getAllNames() { return this.allNames; }

    // LOOKUP MAPS AND OTHER PRE COMPUTATIONS
    protected static final Map<Short, DefaultMobType> ID_MAP = new HashMap<Short, DefaultMobType>();
    protected static final Map<String, DefaultMobType> NAME_MAP = new TreeMap<String, DefaultMobType>(String.CASE_INSENSITIVE_ORDER);
    protected static final List<String> PREFERED_NAMES = new ArrayList<String>(); 

    static
    {
        // Build the lookup maps
        for (DefaultMobType type : EnumSet.allOf(DefaultMobType.class))
        {
            ID_MAP.put(type.typeId, type);
            for (String name : type.allNames)
            {
                NAME_MAP.put(name, type);
            }
            PREFERED_NAMES.add(type.getPreferedName());
        }
    }

    // The constructor
    private DefaultMobType(int typeId, String... names)
    {
        this.typeId = (short) typeId;
        this.formalName = names[0];
        this.preferedName = names[1];
        this.allNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        this.allNames.addAll(Arrays.asList(names));
    }

    public static DefaultMobType fromName(String name) 
    {
        return NAME_MAP.get(name);
    }

    public static DefaultMobType fromId(int id)
    {
        if (id > Short.MAX_VALUE)
        {
            return null;
        }
        return ID_MAP.get((short) id);
    }
    
    public static List<String> getPreferedNames()
    {
        return PREFERED_NAMES;
    }
}