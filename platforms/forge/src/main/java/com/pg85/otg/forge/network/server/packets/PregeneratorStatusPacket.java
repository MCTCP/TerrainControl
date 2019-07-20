package com.pg85.otg.forge.network.server.packets;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.forge.network.client.AbstractClientMessageHandler;
import com.pg85.otg.forge.pregenerator.Pregenerator;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.forge.world.ForgeWorldSession;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StreamHelper;

public class PregeneratorStatusPacket extends OTGPacket
{
	public PregeneratorStatusPacket()
	{
		super();
	}
	
	public PregeneratorStatusPacket(ByteBuf nettyBuffer)
	{
		super(nettyBuffer);
	}
	
	public static void writeToStream(ByteBufOutputStream stream) throws IOException
	{
	    stream.writeInt(PluginStandardValues.ProtocolVersion);
	    stream.writeInt(0); // 0 = Normal packet
	    
	    ArrayList<Pregenerator> pregenerators = new ArrayList<Pregenerator>();
	    for(LocalWorld localWorld : ((ForgeEngine)OTG.getEngine()).getAllWorlds())
	    {
	    	if(localWorld.getWorldSession() != null && ((ForgeWorldSession)localWorld.getWorldSession()).getPregenerator() != null)
	    	{
	    		pregenerators.add(((ForgeWorldSession)localWorld.getWorldSession()).getPregenerator());
	    	}
	    }
	    
	    stream.writeInt(pregenerators.size()); // Number of pregenerators
	    
	    for(Pregenerator pregenerator : pregenerators)
	    {
	    	StreamHelper.writeStringToStream(stream, pregenerator.pregenerationWorld);
	    	stream.writeBoolean(pregenerator.getPregeneratorIsRunning());
	    	stream.writeInt(pregenerator.progressScreenWorldSizeInBlocks);
	    	StreamHelper.writeStringToStream(stream, pregenerator.preGeneratorProgress);
	    	StreamHelper.writeStringToStream(stream, pregenerator.preGeneratorProgressStatus);
	    	StreamHelper.writeStringToStream(stream, pregenerator.progressScreenElapsedTime);
	    	StreamHelper.writeStringToStream(stream, pregenerator.progressScreenEstimatedTime);	 
	    	
	        long i = Runtime.getRuntime().maxMemory();
	        long j = Runtime.getRuntime().totalMemory();
	        long k = Runtime.getRuntime().freeMemory();
	        long l = j - k;
	    	
	    	stream.writeLong(Long.valueOf(bytesToMb(l)));
	    	stream.writeLong(Long.valueOf(bytesToMb(i)));
	    }
	}
	
    private static long bytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
    }
	
	public static class Handler extends AbstractClientMessageHandler<PregeneratorStatusPacket>
	{
		@Override
		public IMessage handleClientMessage(EntityPlayer player, PregeneratorStatusPacket message, MessageContext ctx)
		{
			// For SP clients data is shared between client and server
			if(Minecraft.getMinecraft().isSingleplayer())
			{
				return null;
			}
			
	        try
	        {
	        	int packetType = message.getStream().readInt();
	    		if(packetType == 0) // 0 = Normal packet
	        	{
	    			int listSize = message.getStream().readInt();
	    			for(int i = 0; i < listSize; i++)
	    			{		        	
				    	String pregenerationWorld = StreamHelper.readStringFromStream(message.getStream());
				    	Boolean pregeneratorIsRunning = message.getStream().readBoolean();
				    	int progressScreenWorldSizeInBlocks = message.getStream().readInt();
				    	String preGeneratorProgress = StreamHelper.readStringFromStream(message.getStream());
				    	String preGeneratorProgressStatus = StreamHelper.readStringFromStream(message.getStream());
				    	String progressScreenElapsedTime = StreamHelper.readStringFromStream(message.getStream());
				    	String progressScreenEstimatedTime = StreamHelper.readStringFromStream(message.getStream());
				    	
				    	long mbUsed = message.getStream().readLong();
				    	long mbTotal = message.getStream().readLong();
				    	
				    	ForgeWorld forgeWorld = (ForgeWorld)OTG.getEngine().getWorld(pregenerationWorld);
				    	if(forgeWorld == null)
				    	{
				    		forgeWorld = (ForgeWorld)OTG.getEngine().getUnloadedWorld(pregenerationWorld);
				    	}				    
				    	
				    	// WorldSession can be null if MP client has not received a world instance for the world
				    	if(forgeWorld != null && forgeWorld.getWorldSession() != null)
				    	{
				    		Pregenerator pregenerator = ((ForgeWorldSession)forgeWorld.getWorldSession()).getPregenerator();
				    		pregenerator.setPregeneratorIsRunning(pregeneratorIsRunning);
				    		pregenerator.progressScreenWorldSizeInBlocks = progressScreenWorldSizeInBlocks;
				    		pregenerator.preGeneratorProgress = preGeneratorProgress;
				    		pregenerator.preGeneratorProgressStatus = preGeneratorProgressStatus;
				    		pregenerator.progressScreenElapsedTime = progressScreenElapsedTime;
				    		pregenerator.progressScreenEstimatedTime = progressScreenEstimatedTime;
				    		pregenerator.progressScreenServerUsedMbs = mbUsed;
				    		pregenerator.progressScreenServerTotalMbs = mbTotal;
				    	}
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