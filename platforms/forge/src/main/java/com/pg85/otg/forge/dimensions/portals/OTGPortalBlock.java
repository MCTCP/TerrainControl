package com.pg85.otg.forge.dimensions.portals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.RegistryObject;

// TODO: Customisable portal particles, requires preset lookup on client.
public class OTGPortalBlock extends NetherPortalBlock
{
	private final String portalColor;

	public OTGPortalBlock(BlockBehaviour.Properties properties, String portalColor)
	{
		super(properties);
		this.portalColor = portalColor;
	}
	
	public static boolean checkForPortal(ServerLevel serverworld, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, RegistryObject<OTGPortalBlock> otgPortalBlock, List<LocalMaterialData> portalBlocks)
	{
		if(
			serverworld.dimension() == Level.OVERWORLD ||
			serverworld.getChunkSource().generator instanceof OTGNoiseChunkGenerator
		)
		{
			boolean tryPortal = false;
			for (Direction direction : Direction.values())
			{
				Block blockAtPos = serverworld.getBlockState(pos.relative(direction)).getBlock();
				boolean isFound = false;
				for(LocalMaterialData portalBlock : portalBlocks)
				{
					if(((ForgeMaterialData)portalBlock).internalBlock().getBlock() == blockAtPos)
					{
						isFound = true;
						break;
					}
				}
				if (isFound)
				{
					if (otgPortalBlock.get().isPortal(serverworld, pos, otgPortalBlock, portalBlocks) != null)
					{
						tryPortal = true;
						break;
					}
				}
			}
			if (tryPortal)
			{
				if (otgPortalBlock.get().trySpawnPortal(serverworld, pos, otgPortalBlock, portalBlocks))
				{
					player.playSound(SoundEvents.BUCKET_EMPTY, 1.0F, 1.0F);
					player.swing(hand);
					if (!player.isCreative())
					{
						if (stack.getCount() > 1)
						{
							stack.shrink(1);
							player.addItem(stack.hasContainerItem() ? stack.getContainerItem() : ItemStack.EMPTY);
						}
						else if (stack.isDamageableItem())
						{
							stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
						} else {
							player.setItemInHand(hand, stack.hasContainerItem() ? stack.getContainerItem() : ItemStack.EMPTY);
						}
					}
					return true;
				}
			}
		}
		return false;
	}	

	@Nullable
	public OTGPortalBlock.Size isPortal(LevelAccessor world, BlockPos pos, RegistryObject<OTGPortalBlock> portalBlock, List<LocalMaterialData> portalBlocks)
	{
		OTGPortalBlock.Size otgPortalSizeX = new OTGPortalBlock.Size(world, pos, Axis.X, portalBlock, portalBlocks);
		if (otgPortalSizeX.isValid() && otgPortalSizeX.portalBlockCount == 0)
		{
			return otgPortalSizeX;
		} else {
			OTGPortalBlock.Size otgPortalSizeZ = new OTGPortalBlock.Size(world, pos, Axis.Z, portalBlock, portalBlocks);
			return otgPortalSizeZ.isValid() && otgPortalSizeZ.portalBlockCount == 0? otgPortalSizeZ : null;
		}
	}
	
	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity)
	{
		if (!entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions())
		{
			if (entity.isOnPortalCooldown())
			{
				entity.setPortalCooldown();
			} else {
				if (!entity.level.isClientSide && !pos.equals(entity.portalEntrancePos))
				{
					entity.portalEntrancePos = pos.immutable();
				}
				LazyOptional<OTGPlayer> otgPlayer = entity.getCapability(OTGCapabilities.OTG_PLAYER_CAPABILITY);
				if (!otgPlayer.isPresent())
				{
					doTeleport(entity);
				} else {
					otgPlayer.ifPresent(handler ->
					{
						handler.setPortal(true, this.portalColor);
						int waitTime = handler.getPortalTime();
						if (waitTime >= entity.getPortalWaitTime())
						{
							doTeleport(entity);
							handler.setPortalTime(0);
						}
					});
				}
			}
		}
	}

	public boolean trySpawnPortal(LevelAccessor worldIn, BlockPos pos, RegistryObject<OTGPortalBlock> portalBlock, List<LocalMaterialData> portalBlocks)
	{
		OTGPortalBlock.Size otgPortalSize = this.isPortal(worldIn, pos, portalBlock, portalBlocks);
		if (otgPortalSize != null)
		{
			otgPortalSize.placePortalBlocks(portalBlock);
			return true;
		} else {
			return false;
		}
	}

	public void randomTick(BlockState block, ServerLevel serverWorld, BlockPos pos, Random rand)
	{
		if (serverWorld.dimensionType().natural() && serverWorld.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && rand.nextInt(2000) < serverWorld.getDifficulty().getId())
		{
			while(serverWorld.getBlockState(pos).is(this))
			{
				pos = pos.below();
			}

			EntityType<?> entityType = null;
			Collection<ServerLevel> worlds = (Collection<ServerLevel>)serverWorld.getServer().getAllLevels();
			worlds = worlds.stream().sorted((a,b) -> a.dimension().location().toString().compareTo(b.dimension().location().toString())).collect(Collectors.toList());			
			ArrayList<String> usedColors = new ArrayList<>();
			for(ServerLevel world : worlds)
			{
				if(
					world.dimension() != Level.OVERWORLD &&
					world.dimension() != Level.END &&
					world.dimension() != Level.NETHER &&
					world.getChunkSource().generator instanceof OTGNoiseChunkGenerator
				)
				{
					OTGNoiseChunkGenerator generator = ((OTGNoiseChunkGenerator)world.getChunkSource().generator);
					String portalColor = generator.getPortalColor().toLowerCase().trim();
					while(usedColors.contains(portalColor))
					{
						portalColor = OTGPortalColors.getNextPortalColor(portalColor);
					}
					usedColors.add(portalColor);
					
					if(this.portalColor.equals(portalColor))
					{
						Optional<EntityType<?>> optEntityType = EntityType.byString(generator.getPortalMob());
						if(optEntityType.isPresent())
						{
							entityType = optEntityType.get();
						}
						break;
					}
				}
			}
			if(entityType != null)
			{
				if (serverWorld.getBlockState(pos).isValidSpawn(serverWorld, pos, entityType))
				{
					Entity entity = entityType.spawn(serverWorld, (CompoundTag)null, (Component)null, (Player)null, pos.above(), MobSpawnType.STRUCTURE, false, false);
					if (entity != null)
					{
						entity.setPortalCooldown();
					}
				}
			}
		}
	}

	private void doTeleport(Entity entity)
	{
		if (entity.level != null && entity.level instanceof ServerLevel)
		{
			ServerLevel serverWorld = (ServerLevel) entity.level;
			MinecraftServer minecraftServer = serverWorld.getServer();
			ResourceKey<Level> destinationDim = null;
			if(
				serverWorld.dimension() != Level.END &&
				serverWorld.dimension() != Level.NETHER &&
				(
					serverWorld.dimension() == Level.OVERWORLD ||
					serverWorld.getChunkSource().generator instanceof OTGNoiseChunkGenerator
				)
			)
			{
				Collection<ServerLevel> worlds = (Collection<ServerLevel>)entity.getServer().getAllLevels();
				worlds = worlds.stream().sorted((a,b) -> a.dimension().location().toString().compareTo(b.dimension().location().toString())).collect(Collectors.toList());				
				ArrayList<String> usedColors = new ArrayList<>();
				for(ServerLevel world : worlds)
				{
					if(
						world.dimension() != Level.OVERWORLD &&
						world.dimension() != Level.END &&
						world.dimension() != Level.NETHER &&
						world.getChunkSource().generator instanceof OTGNoiseChunkGenerator
					)
					{
						String portalColor = ((OTGNoiseChunkGenerator)world.getChunkSource().generator).getPortalColor().toLowerCase().trim();
						while(usedColors.contains(portalColor))
						{
							portalColor = OTGPortalColors.getNextPortalColor(portalColor);	
						}
						usedColors.add(portalColor);
						
						if(this.portalColor.equals(portalColor))
						{
							if(world.dimension() == serverWorld.dimension())
							{
								destinationDim = Level.OVERWORLD;
							} else {
								destinationDim = world.dimension();
							}
							break;
						}
					}
				}
			}
			if (minecraftServer != null)
			{
				ServerLevel destination = minecraftServer.getLevel(destinationDim);
				if (destination != null && minecraftServer.isNetherEnabled() && !entity.isPassenger())
				{
					entity.level.getProfiler().push("otg_portal");
					entity.setPortalCooldown();
					entity.changeDimension(destination, new OTGTeleporter(destination));
					entity.level.getProfiler().pop();
				}
			}
		}
	}
	
	public static class Size
	{
		protected final LevelAccessor world;
		public final Direction.Axis axis;
		public final Direction rightDir;
		public final Direction leftDir;
		public int portalBlockCount;
		@Nullable
		public BlockPos bottomLeft;
		public int height;
		public int width;
		
		public Size(LevelAccessor worldIn, BlockPos pos, Direction.Axis axisIn, RegistryObject<OTGPortalBlock> portalBlock, List<LocalMaterialData> portalBlocks)
		{
			this.world = worldIn;
			this.axis = axisIn;
			if (axisIn == Direction.Axis.X)
			{
				this.leftDir = Direction.EAST;
				this.rightDir = Direction.WEST;
			} else {
				this.leftDir = Direction.NORTH;
				this.rightDir = Direction.SOUTH;
			}
			
			for (BlockPos blockpos = pos; pos.getY() > blockpos.getY() - 21 && pos.getY() > 0 && this.isEmptyBlock(worldIn.getBlockState(pos.below())); pos = pos.below())
			{
				;
			}
			
			int i = this.getDistanceToEdge(pos, this.leftDir, portalBlocks) - 1;
			if (i >= 0)
			{
				this.bottomLeft = pos.relative(this.leftDir, i);
				this.width = this.getDistanceToEdge(this.bottomLeft, this.rightDir, portalBlocks);
				if (this.width < 2 || this.width > 21)
				{
					this.bottomLeft = null;
					this.width = 0;
				}
			}
			
			if (this.bottomLeft != null)
			{
				this.height = this.getPortalHeight(portalBlock, portalBlocks);
			}
		}
			
		protected int getDistanceToEdge(BlockPos pos, Direction directionIn, List<LocalMaterialData> portalBlocks)
		{
			int i;
			for (i = 0; i < 22; ++i)
			{
				BlockPos blockpos = pos.relative(directionIn, i);
				Block blockAtPos = this.world.getBlockState(blockpos.below()).getBlock();
				boolean isFound = false;
				for(LocalMaterialData portalBlock : portalBlocks)
				{
					if(((ForgeMaterialData)portalBlock).internalBlock().getBlock() == blockAtPos)
					{
						isFound = true;
						break;
					}
				}				
				if (
					!this.isEmptyBlock(this.world.getBlockState(blockpos)) || 
					!isFound
				)
				{
					break;
				}
			}
		
			BlockPos framePos = pos.relative(directionIn, i);
			Block blockAtPos = this.world.getBlockState(framePos).getBlock();
			boolean isFound = false;
			for(LocalMaterialData portalBlock : portalBlocks)
			{
				if(((ForgeMaterialData)portalBlock).internalBlock().getBlock() == blockAtPos)
				{
					isFound = true;
					break;
				}
			}			
			return isFound ? i : 0;
		}
		
		public int getHeight()
		{
			return this.height;
		}
		
		public int getWidth()
		{
			return this.width;
		}
		
		protected int getPortalHeight(RegistryObject<OTGPortalBlock> portalBlock, List<LocalMaterialData> portalBlocks)
		{
			lblLoopOuter:
				for (this.height = 0; this.height < 21; ++this.height)
				{
					for (int i = 0; i < this.width; ++i)
					{
						BlockPos blockpos = this.bottomLeft.relative(this.rightDir, i).above(this.height);
						BlockState blockstate = this.world.getBlockState(blockpos);
						if (!this.isEmptyBlock(blockstate))
						{
							break lblLoopOuter;
						}
		
						Block block = blockstate.getBlock();
						if (block == portalBlock.get())
						{
							++this.portalBlockCount;
						}
		
						if (i == 0)
						{
							BlockPos framePos = blockpos.relative(this.leftDir);
							Block blockAtPos = this.world.getBlockState(framePos).getBlock();
							boolean isFound = false;
							for(LocalMaterialData portalBlock2 : portalBlocks)
							{
								if(((ForgeMaterialData)portalBlock2).internalBlock().getBlock() == blockAtPos)
								{
									isFound = true;
									break;
								}
							}							
							if (!isFound)
							{
								break lblLoopOuter;
							}
						}
						else if (i == this.width - 1)
						{
							BlockPos framePos = blockpos.relative(this.rightDir);
							Block blockAtPos = this.world.getBlockState(framePos).getBlock();
							boolean isFound = false;
							for(LocalMaterialData portalBlock2 : portalBlocks)
							{
								if(((ForgeMaterialData)portalBlock2).internalBlock().getBlock() == blockAtPos)
								{
									isFound = true;
									break;
								}
							}							
							if (!isFound)
							{
								break lblLoopOuter;
							}
						}
					}
				}
		
			for (int j = 0; j < this.width; ++j)
			{
				BlockPos framePos = this.bottomLeft.relative(this.rightDir, j).above(this.height);
				Block blockAtPos = this.world.getBlockState(framePos).getBlock();
				boolean isFound = false;
				for(LocalMaterialData portalBlock2 : portalBlocks)
				{
					if(((ForgeMaterialData)portalBlock2).internalBlock().getBlock() == blockAtPos)
					{
						isFound = true;
						break;
					}
				}
				if (!isFound)
				{
					this.height = 0;
					break;
				}
			}

			if (this.height <= 21 && this.height >= 3)
			{
				return this.height;
			} else {
				this.bottomLeft = null;
				this.width = 0;
				this.height = 0;
				return 0;
			}
		}
		
		protected boolean isEmptyBlock(BlockState pos)
		{
			Block block = pos.getBlock();
			// TODO: In updating, changed block.getBlock to just block here. Might have broken something --auth
			return pos.isAir() || block == Blocks.WATER || block instanceof OTGPortalBlock;
		}

		public boolean isValid()
		{
			return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
		}

		public void placePortalBlocks(RegistryObject<OTGPortalBlock> portalBlock)
		{
			for (int i = 0; i < this.width; ++i)
			{
				BlockPos blockpos = this.bottomLeft.relative(this.rightDir, i);
				
				for (int j = 0; j < this.height; ++j)
				{
					if (this.world instanceof Level)
					{
						Level world = (Level) this.world;
						world.setBlockAndUpdate(blockpos.above(j), portalBlock.get().defaultBlockState().setValue(OTGPortalBlock.AXIS, this.axis));
					}
				}
			}
		}

		private boolean isLargeEnough()
		{
			return this.portalBlockCount >= this.width * this.height;
		}
		
		public boolean canCreatePortal()
		{
			return this.isValid() && this.isLargeEnough();
		}
	}
}
