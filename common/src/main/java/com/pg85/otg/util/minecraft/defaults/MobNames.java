package com.pg85.otg.util.minecraft.defaults;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a lot of alternative mob names. The implementation should support
 * this names, along with the other names that are available on the current
 * platform.
 */
public enum MobNames
{
	//See: net.minecraft.entity.EntityList for internal mob names list
	
    BAT("bat", "bat"),
    BLAZE("blaze", "blaze"),
    CAVE_SPIDER("cave_spider", "cavespider"),
    CHICKEN("chicken", "chicken"),
    COW("cow", "cow"),
    CREEPER("creeper", "creeper"),
    DONKEY("donkey", "donkey"),
    ELDER_GUARDIAN("elder_guardian", "elderguardian"),
    ENDER_DRAGON("ender_dragon", "enderdragon"),
    ENDERMAN("enderman", "enderman"),
    ENDERMITE("endermite", "endermite"),
    EVOCATION_ILLAGER("evocation_illager", "evocationillager", "evoker"),
    GHAST("ghast", "ghast"),
    GIANT("giant", "giant", "giantzombie", "zombiegiant"),
    GUARDIAN("guardian", "guardian"),
    HORSE("horse", "horse"),
    HUSK("husk", "husk"),
    LLAMA("llama", "llama"),
    MAGMA_CUBE("magma_cube", "magmaslime", "lavaslime", "magmacube"),
    MULE("mule", "mule"),
    MOOSHROOM("mooshroom", "mushroomcow", "mooshroom"),
    OCELOT("ocelot", "ozelot", "ocelot"),
    PIG("pig", "pig"),
    POLAR_BEAR("polar_bear", "polarbear"),
    RABBIT("rabbit", "rabbit"),
    SHEEP("sheep", "sheep"),
    SHULKER("shulker", "shulker"),
    SILVERFISH("silverfish", "silverfish"),
    SKELETON("skeleton", "skeleton"),
    SKELETON_HORSE("skeleton_horse", "skeletonhorse"),
    SLIME("slime", "slime"),
    SNOWMAN("snowman", "snowman"),
    SPIDER("spider", "spider"),    
    SQUID("squid", "squid"),
    STRAY("stray", "stray"),
    VEX("vex", "vex"),    
    VILLAGER("villager", "villager"),
    VILLAGER_GOLEM("villager_golem", "villagergolem", "irongolem"),
    VINDICATION_ILLAGER("vindication_illager", "vindicationillager", "vindicator"),
    WITCH("witch", "witch"),
    WITHER("wither", "witherboss", "wither"),
    WITHER_SKELETON("wither_skeleton", "witherskeleton"),
    WOLF("wolf", "wolf"),
    ZOMBIE("zombie", "zombie"),
    ZOMBIE_HORSE("zombie_horse", "zombiehorse"),
    ZOMBIE_PIGMAN("zombie_pigman", "zombiepigman", "pig_zombie", "pigzombie"),
    ZOMBIE_VILLAGER("zombie_villager", "zombievillager");
   
    // Contains all aliases (alias, internalName)
    private static Map<String, String> MobAliases = new HashMap<String, String>();

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
    	for(String key : MobAliases.keySet())
    	{
    		if(
				key.toLowerCase().trim().replace("minecraft:","").replace("entity","").trim().replace("_","").equals(
					alias.toLowerCase().trim().replace("minecraft:","").replace("entity","").trim().replace("_","")
				)
			)
    		{
    			return MobAliases.get(key);
    		}
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
            MobAliases.put("minecraft:" + alias, "minecraft:" + internalMinecraftName);
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
        return "minecraft:" + this.internalMinecraftName;
    }
}