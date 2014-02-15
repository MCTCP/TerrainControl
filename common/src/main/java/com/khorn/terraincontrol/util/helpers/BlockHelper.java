package com.khorn.terraincontrol.util.helpers;

import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

public class BlockHelper
{

    /**
     * Rotate the block. North -> west - > south - > east
     * 
     * @param type
     * @param data
     * @return
     */
    @SuppressWarnings({"PointlessBitwiseExpression", "incomplete-switch"})
    public static int rotateData(DefaultMaterial mat, int data)
    {
        switch (mat)
        {
            case TORCH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
                switch (data)
                {
                    case 3:
                        return 1;
                    case 4:
                        return 2;
                    case 2:
                        return 3;
                    case 1:
                        return 4;
                }
                break;

            case RAILS:
                switch (data)
                {
                    case 7:
                        return 6;
                    case 8:
                        return 7;
                    case 9:
                        return 8;
                    case 6:
                        return 9;
                }

            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case ACTIVATOR_RAIL:
                int power = data & ~0x7;
                switch (data & 0x7)
                {
                    case 1:
                        return 0 | power;
                    case 0:
                        return 1 | power;
                    case 5:
                        return 2 | power;
                    case 4:
                        return 3 | power;
                    case 2:
                        return 4 | power;
                    case 3:
                        return 5 | power;
                }
                break;

            case LOG:
            case LOG_2:
            case HAY_BLOCK:
                switch (data / 4)
                {
                    case 1:
                        return data + 4; // East/West-->North/South horizontal log
                    case 2:
                        return data - 4; // North/South-->East/West horizontal log
                    // Default: vertical or all-bark log
                }
                break;

            case WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case QUARTZ_STAIRS:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
                switch (data)
                {
                    case 2:
                        return 0;
                    case 3:
                        return 1;
                    case 1:
                        return 2;
                    case 0:
                        return 3;
                    case 6:
                        return 4;
                    case 7:
                        return 5;
                    case 5:
                        return 6;
                    case 4:
                        return 7;
                }
                break;

            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
                int thrown = data & 0x8;
                int withoutThrown = data & ~0x8;
                switch (withoutThrown)
                {
                    case 3:
                        return 1 | thrown;
                    case 4:
                        return 2 | thrown;
                    case 2:
                        return 3 | thrown;
                    case 1:
                        return 4 | thrown;
                }
                break;

            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                int topHalf = data & 0x8;
                int swung = data & 0x4;
                int withoutFlags = data & ~(0x8 | 0x4);
                switch (withoutFlags)
                {
                    case 1:
                        return 0 | topHalf | swung;
                    case 2:
                        return 1 | topHalf | swung;
                    case 3:
                        return 2 | topHalf | swung;
                    case 0:
                        return 3 | topHalf | swung;
                }
                break;

            case SIGN_POST:
                return (data + 12) % 16;

            case LADDER:
            case WALL_SIGN:
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case FURNACE:
            case BURNING_FURNACE:
                switch (data)
                {
                    case 5:
                        return 2;
                    case 4:
                        return 3;
                    case 2:
                        return 4;
                    case 3:
                        return 5;
                }
                break;

            case DISPENSER:
            case DROPPER:
            case HOPPER:
                int dispPower = data & 0x8;
                switch (data & ~0x8)
                {
                    case 5:
                        return 2 | dispPower;
                    case 4:
                        return 3 | dispPower;
                    case 2:
                        return 4 | dispPower;
                    case 3:
                        return 5 | dispPower;
                }
                break;

            case PUMPKIN:
            case JACK_O_LANTERN:
                switch (data)
                {
                    case 1:
                        return 0;
                    case 2:
                        return 1;
                    case 3:
                        return 2;
                    case 0:
                        return 3;
                }
                break;

            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
            case BED_BLOCK:
                int dir = data & 0x03;
                int withoutDir = data - dir;
                switch (dir)
                {
                    case 1:
                        return 0 | withoutDir;
                    case 2:
                        return 1 | withoutDir;
                    case 3:
                        return 2 | withoutDir;
                    case 0:
                        return 3 | withoutDir;
                }
                break;

            case TRAP_DOOR:
                int withoutOrientation = data & ~0x3;
                int orientation = data & 0x3;
                switch (orientation)
                {
                    case 3:
                        return 0 | withoutOrientation;
                    case 2:
                        return 1 | withoutOrientation;
                    case 0:
                        return 2 | withoutOrientation;
                    case 1:
                        return 3 | withoutOrientation;
                }

            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case PISTON_EXTENSION:
                final int rest = data & ~0x7;
                switch (data & 0x7)
                {
                    case 5:
                        return 2 | rest;
                    case 4:
                        return 3 | rest;
                    case 2:
                        return 4 | rest;
                    case 3:
                        return 5 | rest;
                }
                break;

            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
                if (data >= 10)
                    return data;
                return (data * 7) % 10;

            case VINE:
                return ((data >> 1) | (data << 3)) & 0xf;

            case FENCE_GATE:
                return ((data + 3) & 0x3) | (data & ~0x3);

            case COCOA:
            case TRIPWIRE_HOOK:
                int rotationData = data % 4;
                if (rotationData == 0)
                {
                    return data + 3;
                } else
                {
                    return data - 1;
                }

            case ANVIL:
                if (data % 2 == 0)
                {
                    // North-south --> west-east
                    return data + 1;
                } else
                {
                    // West-east --> north-south
                    return data - 1;
                }

            case QUARTZ_BLOCK:
                if (data == 3)
                    return 4;
                if (data == 4)
                    return 3;
        }

        return data;
    }

    private BlockHelper()
    {
    }

}
