package com.pg85.otg.forge.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.customobjects.bo3.ParticleFunction;
import com.pg85.otg.forge.client.events.ClientTickHandler;
import com.pg85.otg.logging.LogMarker;

public class ParticlesPacket implements IMessage
{
    ByteBuf data = Unpooled.buffer();
    DataInputStream wrappedStream;

	public ParticlesPacket()
	{
		// Create an empty packet
        ByteBuf nettyBuffer = Unpooled.buffer();
        ByteBufOutputStream stream = new ByteBufOutputStream(nettyBuffer);

		try
		{
		    stream.writeInt(PluginStandardValues.ProtocolVersion);
		    stream.writeInt(1); // 0 == Normal packet, 1 == Particles packet
		}
		catch (IOException e)
		{
		    OTG.printStackTrace(LogMarker.FATAL, e);
		}

		StringBuilder sb = new StringBuilder();

		try
		{
			String value = sb.toString();
	        byte[] bytes = value.getBytes();
	        stream.writeShort(bytes.length);
	        stream.write(bytes);
		}
		catch (IOException e)
		{
		    OTG.printStackTrace(LogMarker.FATAL, e);
		}

		try
		{
			stream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		this.data = nettyBuffer;
	}

	public ParticlesPacket(ByteBuf data)
	{
		this.data = data;
	}

	@Override
	public void fromBytes(ByteBuf data)
	{
        int serverProtocolVersion = data.readInt();
        int clientProtocolVersion = PluginStandardValues.ProtocolVersion;
        if (serverProtocolVersion == clientProtocolVersion)
        {
        	data.retain();
        	wrappedStream = new DataInputStream(new ByteBufInputStream(data));
        } else {
        	// Wrong version!
        	throw new RuntimeException("Client is using a different version of OTG than server!");
        }
	}

	@Override
	public void toBytes(ByteBuf data)
	{
		data.writeBytes(this.data);
	}

	public static class Handler extends AbstractClientMessageHandler<ParticlesPacket>
	{
		@Override
		public IMessage handleClientMessage(EntityPlayer player, ParticlesPacket message, MessageContext ctx)
		{
	        try
	        {
	        	int packetType = message.wrappedStream.readInt(); // 0 == normal packet, 1 == Particles packet
	    		if(packetType == 1)
	        	{
	        		ArrayList<ParticleFunction> particleFunctions = new ArrayList<ParticleFunction>();
	        		int msgLength = message.wrappedStream.readShort();
	                byte[] chars = new byte[msgLength];
	                if (!(message.wrappedStream.read(chars, 0, chars.length) != chars.length))
	        		{
	            		// Server sent particles
	            		String particleFunctionString = new String(chars);
	            		if(particleFunctionString != null && particleFunctionString.length() > 0 && particleFunctionString.startsWith("Particle("))
	            		{
	            			//OTG.log(LogMarker.INFO, "Received Particle packet: " + particleFunctionString);
	            			String[] particleStrings = particleFunctionString.replace(")", "").replace("Particle(", "'").split("'");
	            			for(String particleString : particleStrings)
	            			{
	            				String[] parameters = particleString.split(",");
		            			if(parameters.length == 11)
		            			{
		            				//OTG.log(LogMarker.INFO, "Processing Particle packet: " + particleString);

			            			ParticleFunction particle = new ParticleFunction();
		            				particle.x = Integer.parseInt(parameters[0]);
		            				particle.y = Integer.parseInt(parameters[1]);
		            				particle.z = Integer.parseInt(parameters[2]);
		            				particle.particleName = parameters[3];
		            				particle.interval = Double.parseDouble(parameters[4]);
		            				particle.velocityX = Double.parseDouble(parameters[5]);
		            				particle.velocityY = Double.parseDouble(parameters[6]);
		            				particle.velocityZ = Double.parseDouble(parameters[7]);
		            				particle.velocityXSet = Boolean.parseBoolean(parameters[8]);
		            				particle.velocityYSet = Boolean.parseBoolean(parameters[9]);
		            				particle.velocityZSet = Boolean.parseBoolean(parameters[10]);
		            				particleFunctions.add(particle);

		            				//sendMessage(EnumChatFormatting.RED, "Received: " + particle.makeString());
		            			}
	            			}
	            		}
	        		}
	        		ArrayList<ParticleFunction> existingParticleFunctions = new ArrayList<ParticleFunction>();
	        		ArrayList<ParticleFunction> newParticleFunctions = new ArrayList<ParticleFunction>();
	        		// Keep existing ParticleFunctions instead of overriding with duplicates so as not to reset any timers
	                synchronized(ClientTickHandler.ClientParticleFunctions)
	                {
	                	existingParticleFunctions.addAll(ClientTickHandler.ClientParticleFunctions);
	                	ClientTickHandler.ClientParticleFunctions.clear();
	                	for(ParticleFunction particleFunction : particleFunctions)
	                	{
	                		boolean bFound = false;
	                		for(ParticleFunction existingParticleFunction : existingParticleFunctions)
	                		{
	                			if(
	            					particleFunction.x == existingParticleFunction.x &&
	            					particleFunction.y == existingParticleFunction.y &&
	            					particleFunction.z == existingParticleFunction.z
	        					)
	                			{
	                				bFound = true;
	                				if(!newParticleFunctions.contains(existingParticleFunction))
	                				{
	                					newParticleFunctions.add(existingParticleFunction);
	                				}
	                			}
	                		}
	                		if(!bFound)
	                		{
	        					newParticleFunctions.add(particleFunction);
	                		}
	                	}

	                	ClientTickHandler.ClientParticleFunctions.addAll(newParticleFunctions);
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
				message.data.release();
			}

			return null;
		}
	}
}