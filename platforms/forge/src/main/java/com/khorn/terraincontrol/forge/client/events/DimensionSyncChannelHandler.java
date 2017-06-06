package com.khorn.terraincontrol.forge.client.events;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;

public class DimensionSyncChannelHandler extends FMLIndexedMessageToMessageCodec<DimensionSyncPacket>
{
    public static DimensionSyncChannelHandler instance = new DimensionSyncChannelHandler();

    public DimensionSyncChannelHandler() {
        addDiscriminator(0, DimensionSyncPacket.class);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, DimensionSyncPacket msg, ByteBuf target) throws Exception {
        target.writeBytes(msg.getData());
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, DimensionSyncPacket msg) {
        msg.consumePacket(source);
        switch(FMLCommonHandler.instance().getEffectiveSide()) {
            case CLIENT:
                msg.execute();
                break;
            case SERVER:
                break;
        }
    }
}
