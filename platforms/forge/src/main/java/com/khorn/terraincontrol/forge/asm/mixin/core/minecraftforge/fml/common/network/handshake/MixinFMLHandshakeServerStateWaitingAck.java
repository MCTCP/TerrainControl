package com.khorn.terraincontrol.forge.asm.mixin.core.minecraftforge.fml.common.network.handshake;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(
        targets = "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$3",
        remap = false
)
public abstract class MixinFMLHandshakeServerStateWaitingAck {

    @Inject(
            method = "accept(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage;Ljava/util/function/Consumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;entrySet()Ljava/util/Set;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1
    )
    private void removeTerrainControlFromBiomeSnapshot(final ChannelHandlerContext ctx, final FMLHandshakeMessage msg, final Consumer<?> consumer, final CallbackInfo ci, final Map<ResourceLocation, ForgeRegistry.Snapshot> snapshot) {
        // Strip client requirement of having biomes match server
        snapshot.get(new ResourceLocation("minecraft", "biomes")).ids.keySet().removeIf(location -> location.getResourceDomain().equals("terraincontrol"));
    }
}
