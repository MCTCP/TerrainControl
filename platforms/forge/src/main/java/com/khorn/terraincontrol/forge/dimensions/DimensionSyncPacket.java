package com.khorn.terraincontrol.forge.dimensions;

import java.io.DataInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.logging.LogMarker;

public class DimensionSyncPacket
{
    private ByteBuf data = Unpooled.buffer();

    DataInputStream wrappedStream;
       
    public void consumePacket(ByteBuf data)
    {    	
        int serverProtocolVersion = data.readInt();
        int clientProtocolVersion = PluginStandardValues.ProtocolVersion;
        if (serverProtocolVersion == clientProtocolVersion)
        {
        	wrappedStream = new DataInputStream(new ByteBufInputStream(data));
        } else {
        	// Wrong version!
        	throw new RuntimeException("Client is using a different version of OTG than server!");
        }
    }

    public ByteBuf getData()
    {
        return data;
    }
    
    public void setData(ByteBuf data)
    {
        this.data = data;
    }

    public void execute()
    {
        // Only do this on client side.
    	        
        try
        {
			((ForgeEngine)TerrainControl.getEngine()).getWorldLoader().registerClientWorld(wrappedStream);
        }
        catch (Exception e)
        {
            TerrainControl.log(LogMarker.FATAL, "Failed to receive packet");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }
    }
}