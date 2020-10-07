package com.pg85.otg.util.minecraft.defaults;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a lot of alternative mob names. The implementation should support
 * this names, along with the other names that are available on the current
 * platform.
 */
public enum EntityNames
{
	//See: net.minecraft.entity.EntityList for internal mob names list

    // Aliases don't need to contain underscores - the toInternalName()
    // function removes those when looking up anyway
	
	// Mobs

    BAT("bat", "bat"),
    BLAZE("blaze", "blaze"),
    CAVE_SPIDER("cave_spider", "cavespider"),
    CHICKEN("chicken", "chicken"),
    COW("cow", "cow"),
    CREEPER("creeper", "creeper"),
    DONKEY("donkey", "donkey"),
    ELDER_GUARDIAN("elder_guardian", "elderguardian"),
    ENDER_DRAGON("ender_dragon", "enderdragon", "dragon"),
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
	ILLUSION_ILLAGER("illusion_illager", "illusionillager", "illusioner"),
    PARROT("parrot", "parrot"),
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
    ZOMBIE_HORSE("zombie_horse", "zombiehorse", "horsezombie"),
    ZOMBIE_PIGMAN("zombie_pigman", "zombiepigman", "pigzombie"),
    ZOMBIE_VILLAGER("zombie_villager", "zombievillager", "villagerzombie");

	// Projectiles
	
	ARROW("arrow", "arrow"),
    DRAGON_FIREBALL("dragon_fireball", "dragonfireball"),
    EGG("egg", "egg", "thrownegg"),
    ENDER_PEARL("ender_pearl", "enderpearl", "thrownenderpearl"),
    EVOKATION_FANGS("evocation_fangs", "evocationfangs", "evokerfangs"),
    FIREBALL("fireball", "fireball"),
    FIREWORKS_ROCKET("fireworks_rocket", "fireworksrocket", "firework", "fireworksrocketentity"),
    LLAMA_SPIT("llama_spit","llamaspit"),
    SHULKER_BULLET("shulker_bullet", "shulkerbullet"),
    SMALL_FIREBALL("small_fireball","smallfireball"),
    SNOWBALL("snowball", "snowball"),
    SPECTRAL_ARROW("spectral_arrow", "spectralarrow"),
    XP_BOTTLE("xp_bottle","xpbottle", "thrownexpbottle"),
    XP_ORB("xp_orb", "xp_orb", "experienceorb"),

	// Entities
	
	AREA_EFFECT_CLOUD("area_effect_cloud", "areaeffectcloud"),
    ARMOR_STAND("armor_stand", "armorstand"),
    BOAT("boat", "boat"),
    CHEST_MINECART("chest_minecart", "chestminecart", "minecartchest"),
    COMMANDBLOCK_MINECART("commandblock_minecart", "commandblockminecart", "minecartcommand"),
    ENDER_CRYSTAL("ender_crystal", "endercrystal"),
    EYE_OF_ENDER_SIGNAL("eye_of_ender_signal", "eye_of_ender_signal","endersignal"),
    FALLING_BLOCK("falling_block", "fallingblock", "fallingsand"),
    FURNACE_MINECART("furnace_minecart","furnaceminecart","minecartfurnace"),
    HOPPER_MINECART("hopper_minecart", "hopperminecart", "minecarthopper"),
    ITEM("item", "item", "droppeditem"),
    ITEM_FRAME("item_frame", "itemframe"),
    LEASH_KNOT("leash_knot", "leashknot", "leashhitch"),
    MINECART("minecart", "minecart"),
    PAINTING("painting", "painting"),
    POTION("potion", "potion", "splashpotion", "thrownpotion"),
    SPAWNER_MINECART("spawner_minecart", "spawnerminecart", "minecartmobspawner"),
    TNT("tnt", "tnt", "primedtnt"),
    TNT_MINECART("tnt_minecart", "tntminecart", "minecarttnt"),
    WITHER_SKULL("wither_skull", "witherskull"),
   
    // Contains all aliases (alias, internalName)
    private static Map<String, String> MobAliases = new HashMap<String, String>();

    // Auto-register all aliases in the enum
    static
    {
        for (EntityNames alt : EntityNames.values())
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
				key.toLowerCase().trim().replace("minecraft:","").replace("entity","").trim().replace("_","").equalsIgnoreCase(
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

    private EntityNames(String internalMinecraftName, String... aliases)
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