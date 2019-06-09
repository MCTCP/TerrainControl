package com.pg85.otg.forge.network;

import java.lang.reflect.Field;
import java.util.EnumMap;

import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.network.client.packets.CreateDeleteDimensionPacket;
import com.pg85.otg.forge.network.client.packets.UpdateDimensionSettingsPacket;
import com.pg85.otg.forge.network.client.packets.TeleportPlayerPacket;
import com.pg85.otg.forge.network.server.packets.DimensionLoadUnloadPacket;
import com.pg85.otg.forge.network.server.packets.DimensionSyncPacket;
import com.pg85.otg.forge.network.server.packets.ParticlesPacket;
import com.pg85.otg.forge.network.server.packets.PregeneratorStatusPacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketDispatcher
{
	// a simple counter will allow us to get rid of 'magic' numbers used during packet registration
	private static byte packetId = 0;
	 
	/**
	* The SimpleNetworkWrapper instance is used both to register and send packets.
	*/
	private static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(PluginStandardValues.MOD_ID);
	
	/**
	* Call this during pre-init or loading and register all of your packets (messages) here
	*/
	public static final void registerPackets()
	{
		PacketDispatcher.registerMessage(DimensionSyncPacket.Handler.class, DimensionSyncPacket.class, Side.CLIENT);
		PacketDispatcher.registerMessage(ParticlesPacket.Handler.class, ParticlesPacket.class, Side.CLIENT);
		PacketDispatcher.registerMessage(DimensionLoadUnloadPacket.Handler.class, DimensionLoadUnloadPacket.class, Side.CLIENT);
		PacketDispatcher.registerMessage(PregeneratorStatusPacket.Handler.class, PregeneratorStatusPacket.class, Side.CLIENT);
		PacketDispatcher.registerMessage(CreateDeleteDimensionPacket.Handler.class, CreateDeleteDimensionPacket.class, Side.SERVER);
		PacketDispatcher.registerMessage(UpdateDimensionSettingsPacket.Handler.class, UpdateDimensionSettingsPacket.class, Side.SERVER);
		PacketDispatcher.registerMessage(TeleportPlayerPacket.Handler.class, TeleportPlayerPacket.class, Side.SERVER);
	}
	
	private static final void registerMessage(Class handlerClass, Class messageClass, Side side)
	{
		PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, side);
	}
	
	public static final void sendTo(IMessage message, NetworkManager manager)
	{
		EnumMap<Side, FMLEmbeddedChannel> channels = null;       	
    	
		try {
			Field[] fields = SimpleNetworkWrapper.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();  				
				if(fieldClass.equals(EnumMap.class))
				{
			        field.setAccessible(true);			        
			        channels = (EnumMap<Side, FMLEmbeddedChannel>) field.get(PacketDispatcher.dispatcher);
			        break;
				}    				
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		if(channels == null)
		{
			throw new RuntimeException();
		}
		
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DISPATCHER);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(manager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get());
        channels.get(Side.SERVER).writeOutbound(message);
	}
	
	public static final void sendTo(IMessage message, EntityPlayerMP player)
	{
		PacketDispatcher.dispatcher.sendTo(message, player);
	}
	
	public static final void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point)
	{
		PacketDispatcher.dispatcher.sendToAllAround(message, point);
	}
	
	public static final void sendToAllAround(IMessage message, int dimension, double x, double y, double z, double range)
	{
		PacketDispatcher.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
	}
	
	public static final void sendToAllAround(IMessage message, EntityPlayer player, double range)
	{
		PacketDispatcher.sendToAllAround(message, player.world.provider.getDimension(), player.posX, player.posY, player.posZ, range);
	}
	
	public static final void sendToDimension(IMessage message, int dimensionId)
	{
		PacketDispatcher.dispatcher.sendToDimension(message, dimensionId);
	}
	
	public static final void sendToServer(IMessage message)
	{
		PacketDispatcher.dispatcher.sendToServer(message);
	}
}