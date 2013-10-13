package com.khorn.terraincontrol;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
public enum DefaultMaterial
{

    AIR(0, false),
    STONE(1),
    GRASS(2),
    DIRT(3),
    COBBLESTONE(4),
    WOOD(5),
    SAPLING(6, false),
    BEDROCK(7),
    WATER(8, false),
    STATIONARY_WATER(9, false),
    LAVA(10, false),
    STATIONARY_LAVA(11, false),
    SAND(12),
    GRAVEL(13),
    GOLD_ORE(14),
    IRON_ORE(15),
    COAL_ORE(16),
    LOG(17),
    LEAVES(18),
    SPONGE(19),
    GLASS(20),
    LAPIS_ORE(21),
    LAPIS_BLOCK(22),
    DISPENSER(23),
    SANDSTONE(24),
    NOTE_BLOCK(25),
    BED_BLOCK(26, false),
    POWERED_RAIL(27, false),
    DETECTOR_RAIL(28, false),
    PISTON_STICKY_BASE(29, false),
    WEB(30, false),
    LONG_GRASS(31, false),
    DEAD_BUSH(32, false),
    PISTON_BASE(33),
    PISTON_EXTENSION(34, false),
    WOOL(35),
    PISTON_MOVING_PIECE(36, false),
    YELLOW_FLOWER(37, false),
    RED_ROSE(38, false),
    BROWN_MUSHROOM(39, false),
    RED_MUSHROOM(40, false),
    GOLD_BLOCK(41),
    IRON_BLOCK(42),
    DOUBLE_STEP(43),
    STEP(44, false),
    BRICK(45),
    TNT(46),
    BOOKSHELF(47),
    MOSSY_COBBLESTONE(48),
    OBSIDIAN(49),
    TORCH(50, false),
    FIRE(51, false),
    MOB_SPAWNER(52, false),
    WOOD_STAIRS(53, false),
    CHEST(54, false),
    REDSTONE_WIRE(55, false),
    DIAMOND_ORE(56),
    DIAMOND_BLOCK(57),
    WORKBENCH(58, false),
    CROPS(59, false),
    SOIL(60, false),
    FURNACE(61),
    BURNING_FURNACE(62),
    SIGN_POST(63, false),
    WOODEN_DOOR(64, false),
    LADDER(65, false),
    RAILS(66, false),
    COBBLESTONE_STAIRS(67, false),
    WALL_SIGN(68, false),
    LEVER(69, false),
    STONE_PLATE(70, false),
    IRON_DOOR_BLOCK(71, false),
    WOOD_PLATE(72, false),
    REDSTONE_ORE(73),
    GLOWING_REDSTONE_ORE(74),
    REDSTONE_TORCH_OFF(75, false),
    REDSTONE_TORCH_ON(76, false),
    STONE_BUTTON(77, false),
    SNOW(78, false),
    ICE(79, false),
    SNOW_BLOCK(80),
    CACTUS(81),
    CLAY(82),
    SUGAR_CANE_BLOCK(83, false),
    JUKEBOX(84, false),
    FENCE(85, false),
    PUMPKIN(86),
    NETHERRACK(87),
    SOUL_SAND(88),
    GLOWSTONE(89),
    PORTAL(90, false),
    JACK_O_LANTERN(91),
    CAKE_BLOCK(92),
    DIODE_BLOCK_OFF(93, false),
    DIODE_BLOCK_ON(94, false),
    LOCKED_CHEST(95, false),
    TRAP_DOOR(96, false),
    MONSTER_EGGS(97),
    SMOOTH_BRICK(98),
    HUGE_MUSHROOM_1(99),
    HUGE_MUSHROOM_2(100),
    IRON_FENCE(101, false),
    THIN_GLASS(102, false),
    MELON_BLOCK(103),
    PUMPKIN_STEM(104, false),
    MELON_STEM(105, false),
    VINE(106, false),
    FENCE_GATE(107, false),
    BRICK_STAIRS(108, false),
    SMOOTH_STAIRS(109, false),
    MYCEL(110, false),
    WATER_LILY(111, false),
    NETHER_BRICK(112),
    NETHER_FENCE(113, false),
    NETHER_BRICK_STAIRS(114, false),
    NETHER_WARTS(115, false),
    ENCHANTMENT_TABLE(116, false),
    BREWING_STAND(117, false),
    CAULDRON(118, false),
    ENDER_PORTAL(119, false),
    ENDER_PORTAL_FRAME(120, false),
    ENDER_STONE(121),
    DRAGON_EGG(122),
    REDSTONE_LAMP_OFF(123, false),
    REDSTONE_LAMP_ON(124, false),
    WOOD_DOUBLE_STEP(125),
    WOOD_STEP(126),
    COCOA(127, false),
    SANDSTONE_STAIRS(128),
    EMERALD_ORE(129),
    ENDER_CHEST(130),
    TRIPWIRE_HOOK(131, false),
    TRIPWIRE(132, false),
    EMERALD_BLOCK(133),
    SPRUCE_WOOD_STAIRS(134),
    BIRCH_WOOD_STAIRS(135),
    JUNGLE_WOOD_STAIRS(136),
    COMMAND(137),
    BEACON(138),
    COBBLE_WALL(139, false),
    FLOWER_POT(140, false),
    CARROT(141, false),
    POTATO(142, false),
    WOOD_BUTTON(143, false),
    SKULL(144, false),
    ANVIL(145, false),
    TRAPPED_CHEST(146),
    GOLD_PLATE(147, false),
    IRON_PLATE(148, false),
    REDSTONE_COMPARATOR_OFF(149, false),
    REDSTONE_COMPARATOR_ON(150, false),
    DAYLIGHT_DETECTOR(151, false),
    REDSTONE_BLOCK(152),
    QUARTZ_ORE(153),
    HOPPER(154),
    QUARTZ_BLOCK(155),
    QUARTZ_STAIRS(156),
    ACTIVATOR_RAIL(157, false),
    DROPPER(158),
    STAINED_CLAY(159),
    HAY_BLOCK(170),
    CARPET(171, false),
    HARD_CLAY(172),
    COAL_BLOCK(173),
    UNKNOWN_BLOCK(255);
    /**
     * The ID of the material
     */
    public final int id;
    /**
     * Whether or not a material is solid. If set to false, it will prevent
     * snowfall.
     * Note: this isn't always equal to what Minecraft calls solid.
     */
    private final boolean solid;

    /**
     * Creates a new material.
     * <p/>
     * @param id    Id of the material.
     * @param solid Whether the material is solid. If set to false, it will
     *              prevent snowfall.
     *              Note: this isn't always equal to what Minecraft calls solid.
     */
    private DefaultMaterial(int id, boolean solid)
    {
        this.id = id;
        this.solid = solid;
    }

    /**
     * Creates a new solid material where snow will fall on.
     * <p/>
     * @param id Id of the material.
     */
    private DefaultMaterial(int id)
    {
        this.id = id;
        this.solid = true;
    }

    /**
     * Returns true only if this material is flowing or stationary Water
     * <p/>
     * @return boolean whether or not this material is flowing or
     *         stationary Water
     */
    public boolean isLiquid()
    {
        return this == WATER || this == STATIONARY_WATER;
    }

    /**
     * Returns the solid value of the material. When returns false, it will
     * prevent snowfall.
     * Note: this isn't always equal to what Minecraft calls solid.
     * <p/>
     * @return boolean Whether or not the material is considered solid
     */
    public boolean isSolid()
    {
        return this.solid;
    }
    /**
     * A DefaultMaterial lookup table with the material ID as the index
     */
    private static DefaultMaterial[] lookupID;
    /**
     * A DefaultMaterial lookup table with the material name as the index
     */
    private static Map<String, DefaultMaterial> lookupName;

    static
    {
        lookupID = new DefaultMaterial[256];
        lookupName = new HashMap<String, DefaultMaterial>();

        for (DefaultMaterial material : DefaultMaterial.values())
        {
            lookupID[material.id] = material;
            lookupName.put(material.name(), material);
        }
    }

    /**
     * Returns a DefaultMaterial object with the given material name
     * <p/>
     * @param name the Name of the DefaultMaterial that is to be returned
     * <p/>
     * @return A DefaultMaterial with the given name
     */
    public static DefaultMaterial getMaterial(String name)
    {
        return lookupName.get(name);
    }

    /**
     * Returns true or false depending on if this DefaultMaterial has the
     * given name
     * <p/>
     * @param name The string name to test this.Name against
     * <p/>
     * @return Boolean whether or not this DefaultMaterial has the given name
     */
    public static boolean contains(String name)
    {
        return lookupName.containsKey(name);
    }

    /**
     * Returns a DefaultMaterial object with the given material ID
     * <p/>
     * @param id the ID of the DefaultMaterial that is to be returned
     * <p/>
     * @return A DefaultMaterial with the given ID
     */
    public static DefaultMaterial getMaterial(int id)
    {
        if (id < 256 && lookupID[id] != null)
        {
            return lookupID[id];
        }
        return UNKNOWN_BLOCK;
    }

    /**
     * Returns true or false depending on if this DefaultMaterial has the
     * given ID
     * <p/>
     * @param id The integer ID to test this.id against
     * <p/>
     * @return Boolean whether or not this DefaultMaterial has the given id
     */
    public static boolean contains(int id)
    {
        return id < 256 && lookupID[id] != null;
    }
}
