package com.khorn.terraincontrol.customobjects.bo2;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;

public class ObjectCoordinate
{
    public int x;
    public int y;
    public int z;
    private int hash;
    public LocalMaterialData material;
    public int BranchDirection;
    public int BranchOdds;


    public ObjectCoordinate(int _x, int _y, int _z)
    {
        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.BranchDirection = -1;
        this.BranchOdds = -1;

        hash = x + z << 8 + y << 16;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ObjectCoordinate)
        {
            ObjectCoordinate object = (ObjectCoordinate) obj;
            return object.x == this.x && object.y == this.y && object.z == this.z;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    public ObjectCoordinate Rotate()
    {
        ObjectCoordinate newCoordinate = new ObjectCoordinate(this.z, this.y, (this.x * -1));
        newCoordinate.material = material.rotate();
        newCoordinate.BranchOdds = this.BranchOdds;

        if (this.BranchDirection != -1)
        {
            newCoordinate.BranchDirection = this.BranchDirection + 1;
            if (newCoordinate.BranchDirection > 3)
                newCoordinate.BranchDirection = 0;
        }

        return newCoordinate;

    }


    public static boolean isCoordinateString(String key)
    {
        String[] coordinates = key.split(",");
        return coordinates.length == 3;
    }

    public static ObjectCoordinate getCoordinateFromString(String key, String value)
    {
        String[] coordinates = key.split(",", 3);
        if (coordinates.length != 3)
            return null;

        try
        {

            int x = Integer.parseInt(coordinates[0]);
            int z = Integer.parseInt(coordinates[1]);
            int y = Integer.parseInt(coordinates[2]);

            ObjectCoordinate newCoordinate = new ObjectCoordinate(x, y, z);


            String workingDataString = value;
            if (workingDataString.contains("#"))
            {
                String stringSet[] = workingDataString.split("#");
                workingDataString = stringSet[0];
                String branchData[] = stringSet[1].split("@");
                newCoordinate.BranchDirection = Integer.parseInt(branchData[0]);
                newCoordinate.BranchOdds = Integer.parseInt(branchData[1]);

            }
            newCoordinate.material = TerrainControl.readMaterial(workingDataString);

            return newCoordinate;

        } catch (NumberFormatException e)
        {
            return null;

        } catch (InvalidConfigException e)
        {
            return null;
            
        }


    }


}
