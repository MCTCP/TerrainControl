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
    public int getChunkX(int _x )
    {
        return (_x + x )>>4;
    }
    public  int getChunkZ(int _z)
    {
        return (_z + z )>>4;
    }

    public void rotateSliceC()
    {
        // torches
        if ((workingData == 50) || (workingData == 75) || (workingData == 76))
        {
            if (workingExtra == 1)
            {
                workingExtra = 3;
            } else if (workingExtra == 2)
            {
                workingExtra = 4;
            } else if (workingExtra == 3)
            {
                workingExtra = 2;
            } else if (workingExtra == 4)
            {
                workingExtra = 1;
            }
        }
        // rails
        else if ((workingData == 66) || (workingData == 27) || (workingData == 28))
        {
            if (workingExtra < 2)
            {
                if (workingExtra == 1)
                {
                    workingExtra = 0;
                } else
                {
                    workingExtra = 1;
                }
            } else
            {
                if (workingExtra == 2)
                {
                    workingExtra = 4;
                } else if (workingExtra == 3)
                {
                    workingExtra = 5;
                } else if (workingExtra == 4)
                {
                    workingExtra = 3;
                } else if (workingExtra == 5)
                {
                    workingExtra = 2;
                }
            }
        }
        // ladder
        else if (workingData == 65)
        {
            if (workingExtra == 2)
            {
                workingExtra = 5;
            } else if (workingExtra == 3)
            {
                workingExtra = 4;
            } else if (workingExtra == 4)
            {
                workingExtra = 2;
            } else if (workingExtra == 5)
            {
                workingExtra = 3;
            }
        }
        // stairs
        else if ((workingData == 53) || (workingData == 67))
        {
            if (workingExtra == 0)
            {
                workingExtra = 2;
            } else if (workingExtra == 1)
            {
                workingExtra = 3;
            } else if (workingExtra == 2)
            {
                workingExtra = 1;
            } else if (workingExtra == 3)
            {
                workingExtra = 0;
            }
        }
        // levers
        else if (workingData == 69)
        {
            if (workingExtra > 8)
            {
                if (workingExtra == 9)
                {
                    workingExtra = 11;
                } else if (workingExtra == 2)
                {
                    workingExtra = 12;
                } else if (workingExtra == 3)
                {
                    workingExtra = 10;
                } else if (workingExtra == 4)
                {
                    workingExtra = 9;
                }
            } else
            {
                if (workingExtra == 1)
                {
                    workingExtra = 3;
                } else if (workingExtra == 2)
                {
                    workingExtra = 4;
                } else if (workingExtra == 3)
                {
                    workingExtra = 2;
                } else if (workingExtra == 4)
                {
                    workingExtra = 1;
                }
            }
        }
        // doors
        else if ((workingData == 64) || (workingData == 71))
        {
            if (workingExtra < 8)
            {
                workingExtra = workingExtra + 1;
                if (workingExtra > 3)
                {
                    workingExtra = 0;
                }
            } else
            {
                workingExtra = workingExtra + 1;
                if (workingExtra > 11)
                {
                    workingExtra = 8;
                }
            }
        }
        // sign post
        else if (workingData == 63)
        {
            workingExtra = workingExtra + 1;
            if (workingExtra > 15)
            {
                workingExtra = 0;
            }
        }
        // wall signs
        else if (workingData == 68)
        {
            if (workingExtra == 2)
            {
                workingExtra = 5;
            } else if (workingExtra == 3)
            {
                workingExtra = 4;
            } else if (workingExtra == 4)
            {
                workingExtra = 2;
            } else if (workingExtra == 5)
            {
                workingExtra = 3;
            }
        }
        // furnaces and dispensrs
        else if ((workingData == 61) || (workingData == 62) || (workingData == 23))
        {
            if (workingExtra == 2)
            {
                workingExtra = 5;
            } else if (workingExtra == 3)
            {
                workingExtra = 4;
            } else if (workingExtra == 4)
            {
                workingExtra = 2;
            } else if (workingExtra == 5)
            {
                workingExtra = 3;
            }
        }
        // pumpkins and JOLs
        else if ((workingData == 86) || (workingData == 91))
        {
            workingExtra = workingExtra + 1;
        }
        // beds
        else if (workingData == 26)
        {
            if (workingExtra > 7)
            {
                workingExtra = workingExtra + 1;
                if (workingExtra > 11)
                {
                    workingExtra = 8;
                }
            } else
            {
                workingExtra = workingExtra + 1;
                if (workingExtra > 3)
                {
                    workingExtra = 0;
                }
            }
        }
        // redstone repeater/delayer
        else if ((workingData == 93) || (workingData == 94))
        {
            if ((workingExtra < 4) && (workingExtra > -1))
            {
                workingExtra = workingExtra + 1;
                if (workingExtra > 3)
                {
                    workingExtra = 0;
                }
            } else if ((workingExtra < 8) && (workingExtra > 3))
            {
                workingExtra = workingExtra + 1;
                if (workingExtra > 7)
                {
                    workingExtra = 4;
                }
            } else if ((workingExtra < 12) && (workingExtra > 7))
            {
                workingExtra = workingExtra + 1;
                if (workingExtra > 11)
                {
                    workingExtra = 8;
                }
            } else if ((workingExtra < 16) && (workingExtra > 11))
            {
                workingExtra = workingExtra + 1;
                if (workingExtra > 15)
                {
                    workingExtra = 12;
                }
            }
        }
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