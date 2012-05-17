package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.DefaultMaterial;

public class Coordinate
{

    private int x;
    private int y;
    private int z;
    public int workingData = 0;
    public int workingExtra = 0;
    private String dataString;
    private int branchOdds = -1;
    public int branchDirection = -1;
    public boolean Digs;

    public Coordinate(int initX, int initY, int initZ, String initData, boolean digs)
    {
        x = initX;
        y = initY;
        z = initZ;
        dataString = initData;
        Digs = digs;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }


    @SuppressWarnings("PointlessBitwiseExpression")
    public static int RotateData(int type, int data)
    {
        DefaultMaterial mat = DefaultMaterial.getMaterial(type);
        if(mat == null)
            return data;

        switch (mat) {
            case TORCH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
                switch (data) {
                    case 3: return 1;
                    case 4: return 2;
                    case 2: return 3;
                    case 1: return 4;
                }
                break;

            case RAILS:
                switch (data) {
                    case 7: return 6;
                    case 8: return 7;
                    case 9: return 8;
                    case 6: return 9;
                }

            case POWERED_RAIL:
            case DETECTOR_RAIL:
                int power = data & ~0x7;
                switch (data & 0x7) {
                    case 1: return 0 | power;
                    case 0: return 1 | power;
                    case 5: return 2 | power;
                    case 4: return 3 | power;
                    case 2: return 4 | power;
                    case 3: return 5 | power;
                }
                break;

            case WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
                switch (data) {
                    case 2: return 0;
                    case 3: return 1;
                    case 1: return 2;
                    case 0: return 3;
                    case 6: return 4;
                    case 7: return 5;
                    case 5: return 6;
                    case 4: return 7;
                }
                break;

            case LEVER:
            case STONE_BUTTON:
                int thrown = data & 0x8;
                int withoutThrown = data & ~0x8;
                switch (withoutThrown) {
                    case 3: return 1 | thrown;
                    case 4: return 2 | thrown;
                    case 2: return 3 | thrown;
                    case 1: return 4 | thrown;
                }
                break;

            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                int topHalf = data & 0x8;
                int swung = data & 0x4;
                int withoutFlags = data & ~(0x8 | 0x4);
                switch (withoutFlags) {
                    case 1: return 0 | topHalf | swung;
                    case 2: return 1 | topHalf | swung;
                    case 3: return 2 | topHalf | swung;
                    case 0: return 3 | topHalf | swung;
                }
                break;

            case SIGN_POST:
                return (data + 12) % 16;

            case LADDER:
            case WALL_SIGN:
            case CHEST:
            case FURNACE:
            case BURNING_FURNACE:
            case DISPENSER:
                switch (data) {
                    case 5: return 2;
                    case 4: return 3;
                    case 2: return 4;
                    case 3: return 5;
                }
                break;

            case PUMPKIN:
            case JACK_O_LANTERN:
                switch (data) {
                    case 1: return 0;
                    case 2: return 1;
                    case 3: return 2;
                    case 0: return 3;
                }
                break;

            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
                int dir = data & 0x03;
                int delay = data - dir;
                switch (dir) {
                    case 1: return 0 | delay;
                    case 2: return 1 | delay;
                    case 3: return 2 | delay;
                    case 0: return 3 | delay;
                }
                break;

            case TRAP_DOOR:
                int withoutOrientation = data & ~0x3;
                int orientation = data & 0x3;
                switch (orientation) {
                    case 3: return 0 | withoutOrientation;
                    case 2: return 1 | withoutOrientation;
                    case 0: return 2 | withoutOrientation;
                    case 1: return 3 | withoutOrientation;
                }

            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case PISTON_EXTENSION:
                final int rest = data & ~0x7;
                switch (data & 0x7) {
                    case 5: return 2 | rest;
                    case 4: return 3 | rest;
                    case 2: return 4 | rest;
                    case 3: return 5 | rest;
                }
                break;

            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
                if (data >= 10) return data;
                return (data * 7) % 10;

            case VINE:
                return ((data >> 1) | (data << 3)) & 0xf;

            case FENCE_GATE:
                return ((data + 3) & 0x3) | (data & ~0x3);
        }

        return data;
    }


    public void Rotate()
    {
        this.workingExtra = RotateData(this.workingData,this.workingExtra);
        if (branchDirection != -1)
        {
            branchDirection = branchDirection + 1;
            if (branchDirection > 3)
            {
                branchDirection = 0;
            }
        }
        int tempx = x;
        x = z;
        z = (tempx * (-1));
    }

    public void RegisterData()
    {
        String workingDataString = dataString;
        String workingExtraString;
        String branchDataString = null;
        if (workingDataString.contains("#"))
        {
            String stringSet[] = workingDataString.split("#");
            workingDataString = stringSet[0];
            branchDataString = stringSet[1];

        }
        if (workingDataString.contains("."))
        {
            String stringSet[] = workingDataString.split("\\.");
            workingDataString = stringSet[0];
            workingExtraString = stringSet[1];
            workingExtra = Integer.parseInt(workingExtraString);
        }
        workingData = Integer.parseInt(workingDataString);
        if (branchDataString != null)
        {
            String stringSet[] = branchDataString.split("@");
            branchDirection = Integer.parseInt(stringSet[0]);
            branchOdds = Integer.parseInt(stringSet[1]);
        }
    }

    Coordinate GetCopy(int initX, int initY, int initZ, String initData, boolean digs)
    {
        Coordinate copy = new Coordinate(initX, initY, initZ, initData, digs);

        copy.workingData = this.workingData;
        copy.workingExtra = this.workingExtra;
        copy.branchDirection = this.branchDirection;
        copy.branchOdds = this.branchOdds;
        return copy;
    }

    public Coordinate GetCopy()
    {
        return this.GetCopy(x, y, z, dataString, Digs);
    }

    public Coordinate GetSumm(Coordinate workCoord)
    {
        return this.GetCopy(x + workCoord.getX(), y + workCoord.getY(), z + workCoord.getZ(), dataString, Digs);
    }
}