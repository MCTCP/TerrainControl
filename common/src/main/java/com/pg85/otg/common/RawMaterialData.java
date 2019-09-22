package com.pg85.otg.common;

import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

public class RawMaterialData implements LocalMaterialData
{
    
    private String rawString;

    public RawMaterialData(String data) {
        this.rawString = data;
    }

    @Override
    public boolean isSmoothAreaAnchor(boolean allowWood, boolean ignoreWater)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName()
    {
        return rawString;
    }

    @Override
    public int getBlockId()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte getBlockData()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isLiquid()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSolid()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAir()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DefaultMaterial toDefaultMaterial()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canSnowFallOn()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isMaterial(DefaultMaterial material)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public LocalMaterialData withBlockData(int newData)
    {
        return this;
    }

    @Override
    public LocalMaterialData withDefaultBlockData()
    {
        return this;
    }

    @Override
    public int hashCodeWithoutBlockData()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public LocalMaterialData rotate()
    {
        return this;
    }

    @Override
    public LocalMaterialData rotate(int rotateTimes)
    {
        return this;
    }

    @Override
    public boolean canFall()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public LocalMaterialData parseForWorld(LocalWorld world)
    {
        return world.getConfigs().getWorldConfig().parseFallback(this.rawString);
    }
    
    @Override
    public String toString()
    {
        return getName();
    }

}
