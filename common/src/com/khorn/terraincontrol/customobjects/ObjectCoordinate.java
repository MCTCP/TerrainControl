package com.khorn.terraincontrol.customobjects;

public class ObjectCoordinate
{
    public int x;
    public int y;
    public int z;
    private int hash;

    public ObjectCoordinate(int _x, int _y, int _z)
    {
        this.x = _x;
        this.y = _y;
        this.z = _z;

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
}
