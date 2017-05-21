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
    BAT("bat", "Bat"),
    BLAZE("blaze", "Blaze"),
    CAVE_SPIDER("cave_spider", "CaveSpider", "cavespider"),
    CHICKEN("chicken", "Chicken"),
    COW("cow", "Cow"),
    CREEPER("creeper", "Creeper"),
    DONKEY("donkey", "Donkey"),
    ELDER_GUARDIAN("elder_guardian", "ElderGuardian", "elderguardian"),
    ENDER_DRAGON("ender_dragon", "EnderDragon", "enderdragon"),
    ENDERMAN("enderman", "Enderman", "ender_man", "EnderMan"),
    ENDERMITE("endermite", "Endermite", "ender_mite", "EnderMite"),
    EVOKER("evocation_illager", "EvocationIllager", "evocationillager", "Evoker", "evoker"),
    GHAST("ghast", "Ghast"),
    GIANT("giant", "Giant", "giantzombie", "zombiegiant"),
    GUARDIAN("guardian", "Guardian"),
    HORSE("horse", "EntityHorse", "Horse"),
    HUSK("husk", "Husk"),
    ILLUSIONER("illusion_illager", "IllusionIllager", "illusionillager", "Illusioner", "illusioner"),
    IRON_GOLEM("villager_golem", "VillagerGolem", "villagergolem", "IronGolem", "irongolem", "iron_golem"),
    LLAMA("llama", "Llama", "LLama"),
    MAGMA_CUBE("magma_cube", "LavaSlime", "lavaslime", "lava_slime", "magmacube"),
    MULE("mule", "Mule"),
    MUSHROOM_COW("mooshroom", "MushroomCow", "mushroomcow", "shroom", "moshoom", "mcow", "shroomcow"),
    OCELOT("ocelot", "Ozelot", "ozelot", "Ocelot"),
    PARROT("parrot", "Parrot"),
    PIG("pig", "Pig"),
    PIG_ZOMBIE("zombie_pigman", "ZombiePigman", "zombiepigman", "PigZombie", "pigzombie", "pig_zombie"),
    POLAR_BEAR("polar_bear", "PolarBear", "polarbear"),
    RABBIT("rabbit", "Rabbit"),
    SHEEP("sheep", "Sheep"),
    SILVERFISH("silverfish", "Silverfish", "silver_fish"),
    SHULKER("shulker", "Shulker"),
    SKELETON("skeleton", "Skeleton"),
    SKELETON_HORSE("skeleton_horse", "SkeletonHorse", "skeletonhorse"),
    SLIME("slime", "Slime"),
    SNOWMAN("snowman", "Snowman", "SnowMan", "snow_man"),
    SPIDER("spider", "Spider"),
    SQUID("squid", "Squid"),
    STRAY("stray", "Stray"),
    VEX("vex", "Vex"),
    VINDICATOR("vindication_illager", "Vindicator", "vindicator"),
    VILLAGER("villager", "Villager"),
    WITCH("witch", "Witch"),
    WITHER("wither", "WitherBoss", "witherboss", "wither_boss", "Wither"),
    WITHER_SKELETON("wither_skeleton", "WitherSkeleton", "witherskeleton"),
    WOLF("wolf", "Wolf"),
    ZOMBIE("zombie", "Zombie"),
    ZOMBIE_HORSE("zombie_horse", "ZombieHorse", "zombiehorse"),
    ZOMBIE_VILLAGER("zombie_villager", "ZombieVillager", "zombievillager");

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