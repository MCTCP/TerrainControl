package com.pg85.otg.forge.network;

import java.util.HashMap;
import java.util.Map;

import com.pg85.otg.constants.Constants;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.FMLHandshakeHandler;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class OTGPacketHandler
{
	private static final Map<String, BiomeSettingWrapper> syncedMap = new HashMap<>();

	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel LOGIN = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Constants.MOD_ID_LOWER_CASE, "login"))
			.networkProtocolVersion(() -> PROTOCOL_VERSION).clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals).simpleChannel();

	public static void setup()
	{
		LOGIN.messageBuilder(PacketSyncSettings.class, 0, NetworkDirection.LOGIN_TO_CLIENT)
				.loginIndex(OTGLoginMessage::getLoginIndex, OTGLoginMessage::setLoginIndex)
				.encoder(PacketSyncSettings::encode).decoder(PacketSyncSettings::decode).markAsLoginPacket()
				.consumer(FMLHandshakeHandler.biConsumerFor((__, msg, ctx) -> PacketSyncSettings.handleLogin(msg, ctx)))
				.add();

		LOGIN.messageBuilder(AcknowledgeOTGMessage.class, 99, NetworkDirection.LOGIN_TO_SERVER)
				.loginIndex(OTGLoginMessage::getLoginIndex, OTGLoginMessage::setLoginIndex)
				.encoder(AcknowledgeOTGMessage::serialize)
				.decoder(AcknowledgeOTGMessage::deserialize)
				.consumer(FMLHandshakeHandler.indexFirst(AcknowledgeOTGMessage::handle)).add();
	}

	public static Map<String, BiomeSettingWrapper> getSyncedmap()
	{
		return syncedMap;
	}
}
