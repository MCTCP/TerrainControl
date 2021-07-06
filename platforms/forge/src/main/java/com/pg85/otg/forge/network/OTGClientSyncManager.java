package com.pg85.otg.forge.network;

import java.util.HashMap;
import java.util.Map;

import com.pg85.otg.constants.Constants;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.FMLHandshakeHandler;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class OTGClientSyncManager
{
	private static final Map<String, BiomeSettingSyncWrapper> syncedData = new HashMap<>();

	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel LOGIN = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Constants.MOD_ID_SHORT, "login"))
			.networkProtocolVersion(() -> PROTOCOL_VERSION).clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals).simpleChannel();

	public static void setup()
	{
		LOGIN.messageBuilder(PacketSyncBiomeSettings.class, 0, NetworkDirection.LOGIN_TO_CLIENT)
				.loginIndex(OTGLoginMessage::getLoginIndex, OTGLoginMessage::setLoginIndex)
				.encoder(PacketSyncBiomeSettings::encode).decoder(PacketSyncBiomeSettings::decode).markAsLoginPacket()
				.consumer(FMLHandshakeHandler.biConsumerFor((__, msg, ctx) -> PacketSyncBiomeSettings.handleLogin(msg, ctx)))
				.add();

		LOGIN.messageBuilder(AcknowledgeOTGMessage.class, 99, NetworkDirection.LOGIN_TO_SERVER)
				.loginIndex(OTGLoginMessage::getLoginIndex, OTGLoginMessage::setLoginIndex)
				.encoder(AcknowledgeOTGMessage::serialize)
				.decoder(AcknowledgeOTGMessage::deserialize)
				.consumer(FMLHandshakeHandler.indexFirst(AcknowledgeOTGMessage::handle)).add();
	}

	public static Map<String, BiomeSettingSyncWrapper> getSyncedData()
	{
		return syncedData;
	}
}
