package com.pg85.otg.forge.network.server.packets;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.forge.network.client.AbstractClientMessageHandler;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StreamHelper;

public class DimensionLoadUnloadPacket extends OTGPacket
{
	public DimensionLoadUnloadPacket()
	{
		super();
	}
	
	public DimensionLoadUnloadPacket(ByteBuf nettyBuffer)
	{
		super(nettyBuffer);
	}
	
	public static void writeToStream(boolean dimensionLoaded, String worldName, ByteBufOutputStream stream) throws IOException
	{
	    stream.writeInt(PluginStandardValues.ProtocolVersion);
	    stream.writeInt(dimensionLoaded ? 1 : 0); // 1 == World loaded packet 0 == world unloaded packet
	    StreamHelper.writeStringToStream(stream, worldName);
	}
	
	public static class Handler extends AbstractClientMessageHandler<DimensionLoadUnloadPacket>
	{
		@Override
		public IMessage handleClientMessage(EntityPlayer player, DimensionLoadUnloadPacket message, MessageContext ctx)
		{
			// For SP clients data is shared between client and server
			if(Minecraft.getMinecraft().isSingleplayer())
			{
				return null;
			}
			
	        try
	        {
	        	int packetType = message.getStream().readInt();
	        	String worldName = StreamHelper.readStringFromStream(message.getStream());
	    		if(packetType == 0 || packetType == 1) // 1 World loaded packet, 0 World unloaded packet
	        	{
	    			// On MP client no forgeworlds should ever be unloaded.
	    			// A Forge World exists on the client for each dimension 
	    			// on the server, though only the active ForgeWorld will
	    			// have a world attached.
	    			if(((ForgeEngine)OTG.getEngine()).getUnloadedWorlds().size() > 0)
	    			{
	    				throw new RuntimeException();
	    			}
	    			
	    			ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(worldName);
	    			if(forgeWorld != null)
	    			{
	    				forgeWorld.isLoadedOnServer = packetType == 1;
	    			}
	        	} else {
	        		throw new RuntimeException();
	        	}
	        }
	        catch (Exception e)
	        {
	            OTG.log(LogMarker.FATAL, "Failed to receive packet");
	            OTG.printStackTrace(LogMarker.FATAL, e);
	        } finally {
	        	// Finally is executed even if we return inside try
				message.getData().release();
			}

			return null;
		}
	}
}