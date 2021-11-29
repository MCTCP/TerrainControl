package com.pg85.otg.forge.mixin;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(ChunkMap.class)
public class MixinChunkManager
{
	@ModifyArg(method = "saveAllChunks(Z)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 0))
	private Predicate<ChunkHolder> alwaysAccessibleFlush(Predicate<ChunkHolder> chunk)
	{
		return c -> true;
	}
	
	@ModifyArg(method = "saveAllChunks(Z)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 1))
	private Predicate<ChunkAccess> allowChunkPrimerFlush(Predicate<ChunkAccess> chunk)
	{
		return c -> c instanceof ProtoChunk || c instanceof ImposterProtoChunk || c instanceof LevelChunk;
	}	
}
