package com.khorn.terraincontrol.forge;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraftforge.common.ForgeHooks;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PlayerTracker implements IPlayerTracker
{

	TCPlugin plugin;

	public PlayerTracker(TCPlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
		// Send config to player
		WorldConfig config = plugin.getWorld().getSettings();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(outputStream);
		try
		{
			stream.writeInt(TCDefaultValues.ProtocolVersion.intValue());
			config.Serialize(stream);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = TCDefaultValues.ChannelName.stringValue();
		packet.data = outputStream.toByteArray();
		packet.length = outputStream.size();

		((EntityPlayerMP) player).playerNetServerHandler.sendPacketToPlayer(packet);
		System.out.println("TerrainControl: sent config");
	}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
		// Stub method
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		// Stub method
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player)
	{
		// Stub method
	}

}
