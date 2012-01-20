package com.Khorn.TerrainControl.CustomObjects;

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

    public int getChunkX(int _x)
    {
        return (_x + x) >> 4;
    }

    public int getChunkZ(int _z)
    {
        return (_z + z) >> 4;
    }

    public static int RotateData(int type, int data)
    {

        switch (type)
        {
            case 50:
            case 75:
            case 76:
                switch (data)
                {
                    case 1:
                        return 3;
                    case 2:
                        return 4;
                    case 3:
                        return 2;
                    case 4:
                        return 1;
                }
                break;
            case 66:
                switch (data)
                {
                    case 6:
                        return 7;
                    case 7:
                        return 8;
                    case 8:
                        return 9;
                    case 9:
                        return 6;
                }

            case 27:
            case 28:
                switch (data & 0x7)
                {
                    case 0:
                        return 0x1 | data & 0xFFFFFFF8;
                    case 1:
                        return 0x0 | data & 0xFFFFFFF8;
                    case 2:
                        return 0x5 | data & 0xFFFFFFF8;
                    case 3:
                        return 0x4 | data & 0xFFFFFFF8;
                    case 4:
                        return 0x2 | data & 0xFFFFFFF8;
                    case 5:
                        return 0x3 | data & 0xFFFFFFF8;
                }
                break;
            case 53:
            case 67:
            case 108:
            case 109:
            case 114:
                switch (data)
                {
                    case 0:
                        return 2;
                    case 1:
                        return 3;
                    case 2:
                        return 1;
                    case 3:
                        return 0;
                }
                break;
            case 69:
            case 77:
                int thrown = data & 0x8;
                int withoutThrown = data & 0xFFFFFFF7;
                switch (withoutThrown)
                {
                    case 1:
                        return 0x3 | thrown;
                    case 2:
                        return 0x4 | thrown;
                    case 3:
                        return 0x2 | thrown;
                    case 4:
                        return 0x1 | thrown;
                }
                break;
            case 64:
            case 71:
                int topHalf = data & 0x8;
                int swung = data & 0x4;
                int withoutFlags = data & 0xFFFFFFF3;
                switch (withoutFlags)
                {
                    case 0:
                        return 0x1 | topHalf | swung;
                    case 1:
                        return 0x2 | topHalf | swung;
                    case 2:
                        return 0x3 | topHalf | swung;
                    case 3:
                        return 0x0 | topHalf | swung;
                }
                break;
            case 63:
                return (data + 4) % 16;
            case 23:
            case 54:
            case 61:
            case 62:
            case 65:
            case 68:
                switch (data)
                {
                    case 2:
                        return 5;
                    case 3:
                        return 4;
                    case 4:
                        return 2;
                    case 5:
                        return 3;
                }
                break;
            case 86:
            case 91:
                switch (data)
                {
                    case 0:
                        return 1;
                    case 1:
                        return 2;
                    case 2:
                        return 3;
                    case 3:
                        return 0;
                }
                break;
            case 93:
            case 94:
                int dir = data & 0x3;
                int delay = data - dir;
                switch (dir)
                {
                    case 0:
                        return 0x1 | delay;
                    case 1:
                        return 0x2 | delay;
                    case 2:
                        return 0x3 | delay;
                    case 3:
                        return 0x0 | delay;
                }
                break;
            case 96:
                int withoutOrientation = data & 0xFFFFFFFC;
                int orientation = data & 0x3;
                switch (orientation)
                {
                    case 0:
                        return 0x3 | withoutOrientation;
                    case 1:
                        return 0x2 | withoutOrientation;
                    case 2:
                        return 0x0 | withoutOrientation;
                    case 3:
                        return 0x1 | withoutOrientation;
                }
                break;
            case 29:
            case 33:
            case 34:
                int rest = data & 0xFFFFFFF8;
                switch (data & 0x7)
                {
                    case 2:
                        return 0x5 | rest;
                    case 3:
                        return 0x4 | rest;
                    case 4:
                        return 0x2 | rest;
                    case 5:
                        return 0x3 | rest;
                }
                break;
            case 99:
            case 100:
                if (data >= 10)
                    return data;
                return data * 3 % 10;
            case 106:
                return (data << 1 | data >> 3) & 0xF;
            case 107:
                return data + 1 & 0x3 | data & 0xFFFFFFFC;

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