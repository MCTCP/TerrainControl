package com.pg85.otg.forge.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.pg85.otg.constants.Constants;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketSyncBiomeSettings implements OTGLoginMessage
{
	private Map<String, BiomeSettingSyncWrapper> syncMap = new HashMap<>();
	private int loginIndex;

	public PacketSyncBiomeSettings() { 
		this.syncMap = OTGClientSyncManager.getSyncedData();
	}

	public PacketSyncBiomeSettings(Map<String, BiomeSettingSyncWrapper> syncMap)
	{
		this.syncMap = syncMap;
	}

	public static void encode(PacketSyncBiomeSettings packet, FriendlyByteBuf buffer)
	{
		buffer.writeInt(packet.syncMap.size());
		for (Entry<String, BiomeSettingSyncWrapper> entry : packet.syncMap.entrySet())
		{
			buffer.writeUtf(entry.getKey());
			entry.getValue().encode(buffer);
		}
	}

	public static PacketSyncBiomeSettings decode(FriendlyByteBuf buffer)
	{
		PacketSyncBiomeSettings packet = new PacketSyncBiomeSettings();
		int size = buffer.readInt();
		for (int i = size; i > 0; i--)
		{
			String key = buffer.readUtf();
			BiomeSettingSyncWrapper wrapper = new BiomeSettingSyncWrapper(buffer);
			packet.syncMap.putIfAbsent(key, wrapper);
		}
		return packet;
	}
	
	public static PacketSyncBiomeSettings decodeSpigot(FriendlyByteBuf buffer)
	{
		PacketSyncBiomeSettings packet = new PacketSyncBiomeSettings();
		int size = buffer.readInt();
		String preset = buffer.readUtf();
		for (int i = size; i > 0; i--)
		{
			String biomeName = buffer.readUtf();
			String key = Constants.MOD_ID_SHORT + ":" + preset + "." + biomeName;
			BiomeSettingSyncWrapper wrapper = new BiomeSettingSyncWrapper(buffer);
			packet.syncMap.putIfAbsent(key, wrapper);
		}
		return packet;
	}

	public static void handleLogin(PacketSyncBiomeSettings packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> OTGClientSyncManager.getSyncedData().putAll(packet.syncMap));

		// We need to send something as a reply, or the client will hang on login
		// forever.
		OTGClientSyncManager.LOGIN.reply(new AcknowledgeOTGMessage(), context.get());
		context.get().setPacketHandled(true);
		context.get().enqueueWork(() -> Minecraft.getInstance().levelRenderer.allChanged());
	}

	@Override
	public int getLoginIndex()
	{
		return loginIndex;
	}

	@Override
	public void setLoginIndex(int loginIndex)
	{
		this.loginIndex = loginIndex;
	}
}
