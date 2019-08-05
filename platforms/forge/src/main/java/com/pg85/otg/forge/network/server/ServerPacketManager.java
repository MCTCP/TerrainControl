package com.pg85.otg.forge.network.server;

import java.io.IOException;
import java.util.ArrayList;

import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.forge.network.PacketDispatcher;
import com.pg85.otg.forge.network.server.packets.DimensionLoadUnloadPacket;
import com.pg85.otg.forge.network.server.packets.DimensionSyncPacket;
import com.pg85.otg.forge.network.server.packets.ParticlesPacket;
import com.pg85.otg.forge.network.server.packets.PregeneratorStatusPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;

public class ServerPacketManager
{
	// Server to client
	
    // Used when creating / deleting dimensions
    public static void sendDimensionSynchPacketToAllPlayers(MinecraftServer server)
    {
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);
    	
        try
        {
    		DimensionSyncPacket.writeToStream(stream);
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
		if(nettyBuffer != null)
		{
	    	for(EntityPlayerMP player : server.getPlayerList().getPlayers())
	    	{
	        	PacketDispatcher.sendTo(new DimensionSyncPacket(nettyBuffer.copy()), (EntityPlayerMP) player);
	    	}
		}
    }

    public static void sendPacketsOnConnect(ServerConnectionFromClientEvent event)
	{
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);
		
        try
        {
    		DimensionSyncPacket.writeToStream(stream);
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(nettyBuffer != null)
		{
			PacketDispatcher.sendTo(new DimensionSyncPacket(nettyBuffer), event.getManager());
	    	// Reset particles in case the player just switched worlds. This also happens when players log on.
	    	PacketDispatcher.sendTo(ParticlesPacket.createEmptyPacket(), event.getManager());
		}
	}	
	
	public static void sendParticlesPacket(ArrayList<ParticleFunction<?>> particleDataForOTGPerPlayer, EntityPlayerMP player)
	{
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

        try
        {
        	ParticlesPacket.writeToStream(particleDataForOTGPerPlayer, stream);
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(nettyBuffer != null)
		{		
        	PacketDispatcher.sendTo(new ParticlesPacket(nettyBuffer), player);
		}
	}   
	
	// Sent to the client to update the UI when a world loads/unloads on the server
	public static void sendDimensionLoadUnloadPacketToAllPlayers(boolean dimensionLoaded, String worldName, MinecraftServer server)
	{
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

        try
        {        	
        	DimensionLoadUnloadPacket.writeToStream(dimensionLoaded, worldName, stream);
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(nettyBuffer != null)
		{
	    	for(EntityPlayerMP player : server.getPlayerList().getPlayers())
	    	{
	        	PacketDispatcher.sendTo(new DimensionLoadUnloadPacket(nettyBuffer.copy()), (EntityPlayerMP) player);
	    	}
		}
	} 
	
	// Sent to the client to update the pregenerator UI
	public static void sendPregeneratorStatusPacketToAllPlayers(MinecraftServer server)
	{
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

        try
        {        	
        	PregeneratorStatusPacket.writeToStream(stream);
		}
        catch (IOException e1)
        {
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(nettyBuffer != null)
		{
	    	for(EntityPlayerMP player : server.getPlayerList().getPlayers())
	    	{
    			PacketDispatcher.sendTo(new PregeneratorStatusPacket(nettyBuffer.copy()), (EntityPlayerMP) player);
	    	}
		}
	}
}
