package com.pg85.otg.forge.network.server.packets;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFile;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.ForgeWorldSession;
import com.pg85.otg.forge.generator.Pregenerator;
import com.pg85.otg.forge.network.OTGPacket;
import com.pg85.otg.forge.network.client.AbstractClientMessageHandler;
import com.pg85.otg.logging.LogMarker;

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
	
	public static void WriteToStream(ByteBufOutputStream stream) throws IOException
	{
	    stream.writeInt(PluginStandardValues.ProtocolVersion);
	    stream.writeInt(0); // 0 = Normal packet
	    
	    ArrayList<Pregenerator> pregenerators = new ArrayList<Pregenerator>();
	    for(LocalWorld localWorld : ((ForgeEngine)OTG.getEngine()).getAllWorlds())
	    {
	    	if(localWorld.GetWorldSession() != null && ((ForgeWorldSession)localWorld.GetWorldSession()).getPregenerator() != null)
	    	{
	    		pregenerators.add(((ForgeWorldSession)localWorld.GetWorldSession()).getPregenerator());
	    	}
	    }
	    
	    stream.writeInt(pregenerators.size()); // Number of pregenerators
	    
	    for(Pregenerator pregenerator : pregenerators)
	    {
	    	ConfigFile.writeStringToStream(stream, pregenerator.pregenerationWorld);
	    	stream.writeBoolean(pregenerator.getPregeneratorIsRunning());
	    	stream.writeInt(pregenerator.progressScreenWorldSizeInBlocks);
	    	ConfigFile.writeStringToStream(stream, pregenerator.preGeneratorProgress);
	    	ConfigFile.writeStringToStream(stream, pregenerator.preGeneratorProgressStatus);
	    	ConfigFile.writeStringToStream(stream, pregenerator.progressScreenElapsedTime);
	    	ConfigFile.writeStringToStream(stream, pregenerator.progressScreenEstimatedTime);	 
	    	
	        long i = Runtime.getRuntime().maxMemory();
	        long j = Runtime.getRuntime().totalMemory();
	        long k = Runtime.getRuntime().freeMemory();
	        long l = j - k;
	    	
	    	stream.writeLong(Long.valueOf(BytesToMb(l)));
	    	stream.writeLong(Long.valueOf(BytesToMb(i)));
	    }
	}
	
    private static long BytesToMb(long bytes)
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
				    	String pregenerationWorld = ConfigFile.readStringFromStream(message.getStream());
				    	Boolean pregeneratorIsRunning = message.getStream().readBoolean();
				    	int progressScreenWorldSizeInBlocks = message.getStream().readInt();
				    	String preGeneratorProgress = ConfigFile.readStringFromStream(message.getStream());
				    	String preGeneratorProgressStatus = ConfigFile.readStringFromStream(message.getStream());
				    	String progressScreenElapsedTime = ConfigFile.readStringFromStream(message.getStream());
				    	String progressScreenEstimatedTime = ConfigFile.readStringFromStream(message.getStream());
				    	
				    	long mbUsed = message.getStream().readLong();
				    	long mbTotal = message.getStream().readLong();
				    	
				    	ForgeWorld forgeWorld = (ForgeWorld)OTG.getEngine().getWorld(pregenerationWorld);
				    	if(forgeWorld == null)
				    	{
				    		forgeWorld = (ForgeWorld)OTG.getEngine().getUnloadedWorld(pregenerationWorld);
				    	}				    
				    	
				    	// WorldSession can be null if MP client has not received a world instance for the world
				    	if(forgeWorld != null && forgeWorld.GetWorldSession() != null)
				    	{
				    		Pregenerator pregenerator = ((ForgeWorldSession)forgeWorld.GetWorldSession()).getPregenerator();
				    		pregenerator.SetPregeneratorIsRunning(pregeneratorIsRunning);
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