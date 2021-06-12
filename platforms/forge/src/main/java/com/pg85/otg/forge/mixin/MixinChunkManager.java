package com.pg85.otg.forge.mixin;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkPrimerWrapper;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(ChunkManager.class)
public class MixinChunkManager
{
    @ModifyArg(method = "saveAllChunks(Z)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 0))
    private Predicate<ChunkHolder> alwaysAccessibleFlush(Predicate<ChunkHolder> chunk)
    {
        return c -> true;
    }
	
    @ModifyArg(method = "saveAllChunks(Z)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 1))
    private Predicate<IChunk> allowChunkPrimerFlush(Predicate<IChunk> chunk)
    {
        return c -> c instanceof ChunkPrimer || c instanceof ChunkPrimerWrapper || c instanceof Chunk;
    }   
}
