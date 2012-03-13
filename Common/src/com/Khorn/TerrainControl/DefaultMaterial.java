package com.Khorn.TerrainControl;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
public enum DefaultMaterial
{
    AIR(0),
    STONE(1),
    GRASS(2),
    DIRT(3),
    COBBLESTONE(4),
    WOOD(5),
    SAPLING(6),
    BEDROCK(7),
    WATER(8),
    STATIONARY_WATER(9),
    LAVA(10),
    STATIONARY_LAVA(11),
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
    BED_BLOCK(26),
    POWERED_RAIL(27),
    DETECTOR_RAIL(28),
    PISTON_STICKY_BASE(29),
    WEB(30),
    LONG_GRASS(31),
    DEAD_BUSH(32),
    PISTON_BASE(33),
    PISTON_EXTENSION(34),
    WOOL(35),
    PISTON_MOVING_PIECE(36),
    YELLOW_FLOWER(37),
    RED_ROSE(38),
    BROWN_MUSHROOM(39),
    RED_MUSHROOM(40),
    GOLD_BLOCK(41),
    IRON_BLOCK(42),
    DOUBLE_STEP(43),
    STEP(44),
    BRICK(45),
    TNT(46),
    BOOKSHELF(47),
    MOSSY_COBBLESTONE(48),
    OBSIDIAN(49),
    TORCH(50),
    FIRE(51),
    MOB_SPAWNER(52),
    WOOD_STAIRS(53),
    CHEST(54),
    REDSTONE_WIRE(55),
    DIAMOND_ORE(56),
    DIAMOND_BLOCK(57),
    WORKBENCH(58),
    CROPS(59),
    SOIL(60),
    FURNACE(61),
    BURNING_FURNACE(62),
    SIGN_POST(63),
    WOODEN_DOOR(64),
    LADDER(65),
    RAILS(66),
    COBBLESTONE_STAIRS(67),
    WALL_SIGN(68),
    LEVER(69),
    STONE_PLATE(70),
    IRON_DOOR_BLOCK(71),
    WOOD_PLATE(72),
    REDSTONE_ORE(73),
    GLOWING_REDSTONE_ORE(74),
    REDSTONE_TORCH_OFF(75),
    REDSTONE_TORCH_ON(76),
    STONE_BUTTON(77),
    SNOW(78),
    ICE(79),
    SNOW_BLOCK(80),
    CACTUS(81),
    CLAY(82),
    SUGAR_CANE_BLOCK(83),
    JUKEBOX(84),
    FENCE(85),
    PUMPKIN(86),
    NETHERRACK(87),
    SOUL_SAND(88),
    GLOWSTONE(89),
    PORTAL(90),
    JACK_O_LANTERN(91),
    CAKE_BLOCK(92),
    DIODE_BLOCK_OFF(93),
    DIODE_BLOCK_ON(94),
    LOCKED_CHEST(95),
    TRAP_DOOR(96),
    MONSTER_EGGS(97),
    SMOOTH_BRICK(98),
    HUGE_MUSHROOM_1(99),
    HUGE_MUSHROOM_2(100),
    IRON_FENCE(101),
    THIN_GLASS(102),
    MELON_BLOCK(103),
    PUMPKIN_STEM(104),
    MELON_STEM(105),
    VINE(106),
    FENCE_GATE(107),
    BRICK_STAIRS(108),
    SMOOTH_STAIRS(109),
    MYCEL(110),
    WATER_LILY(111),
    NETHER_BRICK(112),
    NETHER_FENCE(113),
    NETHER_BRICK_STAIRS(114),
    NETHER_WARTS(115),
    ENCHANTMENT_TABLE(116),
    BREWING_STAND(117),
    CAULDRON(118),
    ENDER_PORTAL(119),
    ENDER_PORTAL_FRAME(120),
    ENDER_STONE(121),
    DRAGON_EGG(122),
    REDSTONE_LAMP_OFF(123),
    REDSTONE_LAMP_ON(124),
    ;

    public final int id;
    private DefaultMaterial(int Id)
    {
        this.id = Id;
    }
    public boolean isLiquid()
    {
        return this == WATER || this == STATIONARY_WATER;
    }
    
    private static DefaultMaterial[] lookupID;
    private static Map<String, DefaultMaterial> lookupName;
    static
    {
        lookupID = new DefaultMaterial[256];
        lookupName = new HashMap<String, DefaultMaterial>();

        for(DefaultMaterial material : DefaultMaterial.values())
        {
            lookupID[material.id] = material;
            lookupName.put(material.name(),material);
        }
    }
    
    public static DefaultMaterial getMaterial(String name)
    {
        return lookupName.get(name);
    }

    public static DefaultMaterial getMaterial(int id)
    {
        if(id < 256 && lookupID[id]!= null)
        {
            return lookupID[id];
        }
        return null;
    }
}
