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
    BAT("Bat", "bat"),
    BLAZE("Blaze", "blaze"),
    CAVE_SPIDER("CaveSpider", "cavespider", "cave_spider"),
    CHICKEN("Chicken", "chicken"),
    COW("Cow", "cow"),
    CREEPER("Creeper", "creeper"),
    ENDER_DRAGON("EnderDragon", "enderdragon", "ender_dragon"),
    ENDERMAN("Enderman", "enderman"),
    ENDERMITE("Endermite", "endermite"),
    GHAST("Ghast", "ghast"),
    GIANT("Giant", "giant", "giantzombie", "zombiegiant"),
    GUARDIAN("Guardian", "guardian"),
    HORSE("EntityHorse", "Horse", "horse"),
    IRON_GOLEM("VillagerGolem", "villagergolem", "villager_golem", "IronGolem", "irongolem", "iron_golem"),
    MAGMA_CUBE("LavaSlime", "lavaslime", "lava_slime", "magmacube", "magma_cube"),
    MUSHROOM_COW("MushroomCow", "mushroomcow", "shroom", "mooshroom", "moshoom", "mcow", "shroomcow"),
    OCELOT("Ozelot", "ozelot", "Ocelot", "ocelot"),
    PIG("Pig", "pig"),
    PIG_ZOMBIE("PigZombie", "pigzombie", "pig_zombie"),
    RABBIT("Rabbit", "rabbit"),
    SHEEP("Sheep", "sheep"),
    SILVERFISH("Silverfish", "silverfish", "silver_fish"),
    SKELETON("Skeleton", "skeleton"),
    SLIME("Slime", "slime"),
    SNOWMAN("SnowMan", "snowman"),
    SPIDER("Spider", "spider"),
    SQUID("Squid", "squid"),
    VILLAGER("Villager", "villager"),
    WITCH("Witch", "witch"),
    WITHER("WitherBoss", "witherboss", "wither_boss", "Wither", "wither"),
    WOLF("Wolf", "wolf"),
    ZOMBIE("Zombie", "zombie");

    // Contains all aliases (alias, internalName)
    private static Map<String, String> mobAliases = new HashMap<String, String>();

    // Auto-register all aliases in the enum
    static
    {
        for (MobNames alt : MobNames.values())
        {
            register(alt.internalMinecraftName, alt.aliases);
        }
    }

    /**
     * Returns the internal name of the mob. If it can't be found, it returns
     * the alias.
     *
     * @param alias The alias.
     * @return The internal name, or if it can't be found, the alias.
     */
    public static String toInternalName(String alias)
    {
        if (mobAliases.containsKey(alias))
        {
            return mobAliases.get(alias);
        }
        return alias;
    }

    /**
     * Register aliases here
     *
     * @param internalMinecraftName The internal Minecraft mob id, for example Ozelot
     * @param aliases               The alias, for example Ocelot
     */
    private static void register(String internalMinecraftName, String... aliases)
    {
        for (String alias : aliases)
        {
            mobAliases.put(alias, internalMinecraftName);
        }
    }


    private String[] aliases;
    private String internalMinecraftName;

    private MobNames(String internalMinecraftName, String... aliases)
    {
        this.internalMinecraftName = internalMinecraftName;
        this.aliases = aliases;
    }

    /**
     * Gets the internal Minecraft name of this mob.
     * @return The internal Minecraft name.
     */
    public String getInternalName()
    {
        return this.internalMinecraftName;
    }
}