package com.pg85.otg.customobject.bo2;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.materials.LocalMaterialData;

class ObjectCoordinate
{
    int x;
    int y;
    int z;
    private int hash;
    LocalMaterialData material;
    private int branchDirection;
    private int branchOdds;

    private ObjectCoordinate(int _x, int _y, int _z)
    {
        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.branchDirection = -1;
        this.branchOdds = -1;

        this.hash = this.x + this.z << 8 + this.y << 16;
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

    ObjectCoordinate rotate()
    {
        ObjectCoordinate newCoordinate = new ObjectCoordinate(this.z, this.y, (this.x * -1));
        newCoordinate.material = this.material.rotate();
        newCoordinate.branchOdds = this.branchOdds;

        if (this.branchDirection != -1)
        {
            newCoordinate.branchDirection = this.branchDirection + 1;
            if (newCoordinate.branchDirection > 3)
                newCoordinate.branchDirection = 0;
        }

        return newCoordinate;

    }

    static ObjectCoordinate getCoordinateFromString(String key, String value, IMaterialReader materialReader)
    {
        String[] coordinates = key.split(",", 3);
        if (coordinates.length != 3)
        {
            return null;
        }

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
                newCoordinate.branchDirection = Integer.parseInt(branchData[0]);
                newCoordinate.branchOdds = Integer.parseInt(branchData[1]);
            }
            newCoordinate.material = materialReader.readMaterial(workingDataString);

            return newCoordinate;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        catch (InvalidConfigException e)
        {
            return null;            
        }
    }
}
