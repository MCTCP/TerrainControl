package com.pg85.otg.util;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFile;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.customobjects.bo3.BO3Loader;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import java.util.List;
import java.util.Random;

public class OTGBlock
{
    public LocalMaterialData material;
    public int x;
    public int y;
    public int z;
    public NamedBinaryTag metaDataTag;
    public String metaDataName;

    public OTGBlock() {}
    
    public OTGBlock(LocalMaterialData material, int x, int y, int z)
    {
    	this.material = material;
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.metaDataTag = null;
    	this.metaDataName = null;
    }
    
    public OTGBlock(LocalMaterialData material, int x, int y, int z, NamedBinaryTag metaDataTag, String metaDataName)
    {
    	this.material = material;
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.metaDataTag = metaDataTag;
    	this.metaDataName = metaDataName;
    }
}