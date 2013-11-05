package com.khorn.terraincontrol.util.minecraftTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a lot of alternative mob names. The implementation should support
 * this names, along with the other names that are available on the current
 * platform.
 */
public enum MobNames
{
    // All entities sorted by the network ID
    CREEPER("Creeper", "creeper"),
    SKELETON("Skeleton", "skeleton"),
    SPIDER("Spider", "spider"),
    GIANT("Giant", "giant", "giantzombie", "zombiegiant"),
    ZOMBIE("Zombie", "zombie"),
    SLIME("Slime", "slime"),
    GHAST("Ghast", "ghast"),
    PIG_ZOMBIE("PigZombie", "pigzombie", "pig_zombie"),
    ENDERMAN("Enderman", "enderman"),
    CAVE_SPIDER("CaveSpider", "cavespider", "cave_spider"),
    SILVERFISH("Silverfish", "silverfish", "silver_fish"),
    BLAZE("Blaze", "blaze"),
    MAGMA_CUBE("LavaSlime", "lavaslime", "lava_slime", "magmacube", "magma_cube"),
    ENDER_DRAGON("EnderDragon", "enderdragon", "ender_dragon"),
    WITHER_BOSS("WitherBoss", "witherboss", "wither_boss", "Wither", "wither"),
    BAT("Bat", "bat"),
    WITCH("Witch", "witch"),
    PIG("Pig", "pig"),
    SHEEP("Sheep", "sheep"),
    COW("Cow", "cow"),
    CHICKEN("Chicken", "chicken"),
    SQUID("Squid", "squid"),
    WOLF("Wolf", "wolf"),
    MUSHROOM_COW("MushroomCow", "mushroomcow", "shroom", "mooshroom", "moshoom", "mcow", "shroomcow"),
    SNOWMAN("SnowMan", "snowman"),
    OCELOT("Ozelot", "ozelot", "Ocelot", "ocelot"),
    IRON_GOLEM("VillagerGolem", "villagergolem", "villager_golem", "IronGolem", "irongolem", "iron_golem"),
    VILLAGER("Villager", "villager");

    // Contains all aliases (alias, internalName)
    private static Map<String, String> mobAliases = new HashMap<String, String>();

    // Fields and constructor
    private String internalMinecraftName;
    private String[] aliases;

    private MobNames(String internalMinecraftName, String... aliases)
    {
        this.internalMinecraftName = internalMinecraftName;
        this.aliases = aliases;
    }

    // Auto-register all aliases in the enum
    static
    {
        for (MobNames alt : MobNames.values())
        {
            register(alt.internalMinecraftName, alt.aliases);
        }
    }

    /**
     * Register aliases here
     *
     * @param internalMinecraftName The internal Minecraft mob id, for example Ozelot
     * @param aliases               The alias, for example Ocelot
     */
    public static void register(String internalMinecraftName, String... aliases)
    {
        for (String alias : aliases)
        {
            mobAliases.put(alias, internalMinecraftName);
        }
    }

    /**
     * Returns the internal name of the mob. If it can't be found, it returns
     * the alias.
     *
     * @param alias The alias.
     * @return The internal name, or if it can't be found, the alias.
     */
    public static String getInternalMinecraftName(String alias)
    {
        if (mobAliases.containsKey(alias))
        {
            return mobAliases.get(alias);
        }
        return alias;
    }
}