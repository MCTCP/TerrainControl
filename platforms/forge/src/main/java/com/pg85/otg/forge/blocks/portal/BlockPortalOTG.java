package com.pg85.otg.forge.blocks.portal;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.forge.blocks.BlockBreakableBase;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPortalOTG extends BlockBreakableBase
{
    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.<EnumFacing.Axis>create("axis", EnumFacing.Axis.class, EnumFacing.Axis.X, EnumFacing.Axis.Z);
    protected static final AxisAlignedBB X_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D);
    protected static final AxisAlignedBB Z_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D);
    protected static final AxisAlignedBB Y_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);

    public BlockPortalOTG(String name)
    {
        super(name, Material.PORTAL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X));
        this.setTickRandomly(true);

		this.setHardness(-1.0F);
		this.setSoundType(SoundType.GLASS);
		this.setLightLevel(0.75F);
    }

    public static boolean trySpawnPortal(World worldIn, BlockPos pos)
    {
    	// Only create a portal when a OTG custom dimension exists
    	boolean bFound = false;
    	for(int i : OTGDimensionManager.GetOTGDimensions())
    	{
			if(DimensionManager.isDimensionRegistered(i))
			{
				bFound = true;
				break;
			}
		}	
		if(!bFound)
		{
			return false;
		}
    	
        OTGBlockPortalSize blockportal$size = new OTGBlockPortalSize(worldIn, pos, EnumFacing.Axis.X);

        if (blockportal$size.isValid() && blockportal$size.portalBlockCount == 0)
        {
            blockportal$size.placePortalBlocks();
            return true;
        } else {
            OTGBlockPortalSize blockportal$size1 = new OTGBlockPortalSize(worldIn, pos, EnumFacing.Axis.Z);

            if (blockportal$size1.isValid() && blockportal$size1.portalBlockCount == 0)
            {
                blockportal$size1.placePortalBlocks();
                return true;
            } else {
                return false;
            }
        }
    }    
    
    //
        
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        switch ((EnumFacing.Axis)state.getValue(AXIS))
        {
            case X:
                return X_AABB;
            case Y:
            default:
                return Y_AABB;
            case Z:
                return Z_AABB;
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);

        String portalColor = PortalColors.getPortalColorByPortalBlock(this);        
        DimensionsConfig dimsConfig = OTG.getDimensionsConfig();
        DimensionConfig dimConfig = null;
        if(dimsConfig.Overworld.Settings.PortalColor.equals(portalColor))
        {
        	dimConfig = dimsConfig.Overworld;
        } else {
	        if(dimsConfig.Dimensions != null)
	        {
	        	for(DimensionConfig dimConfig2 : dimsConfig.Dimensions)
	        	{
	        		if(portalColor.equals(dimConfig2.Settings.PortalColor.toLowerCase()))
	        		{
	        			dimConfig = dimConfig2;
	        			break;
	        		}
	        	}
	        }
        }
        
    	ResourceLocation entityType = dimConfig == null ? new ResourceLocation("zombie_pigman") : new ResourceLocation(dimConfig.Settings.PortalMobType);

        if (worldIn.provider.isSurfaceWorld() && worldIn.getGameRules().getBoolean("doMobSpawning") && rand.nextInt(dimConfig == null ? 2000 : dimConfig.Settings.PortalMobSpawnChance) < worldIn.getDifficulty().getId())
        {
            int i = pos.getY();
            BlockPos blockpos;

            for (blockpos = pos; !worldIn.getBlockState(blockpos).isTopSolid() && blockpos.getY() > 0; blockpos = blockpos.down())
            {
                ;
            }

            if (i > 0 && !worldIn.getBlockState(blockpos.up()).isNormalCube())
            {
                if(entityType != null)
                {
	                Entity entity = ItemMonsterPlacer.spawnCreature(worldIn, entityType, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 1.1D, (double)blockpos.getZ() + 0.5D);	
	                if (entity != null)
	                {
	                    entity.timeUntilPortal = entity.getPortalCooldown();
	                }
            	}
            }
        }
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    public static int getMetaForAxis(EnumFacing.Axis axis)
    {
        if (axis == EnumFacing.Axis.X)
        {
            return 1;
        } else {
            return axis == EnumFacing.Axis.Z ? 2 : 0;
        }
    }
    
    // Convert the given metadata into a BlockState for this Block
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(AXIS, (meta & 3) == 2 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
    }

    // Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	// change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
    // block, etc.
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        EnumFacing.Axis enumfacing$axis = (EnumFacing.Axis)state.getValue(AXIS);

        if (enumfacing$axis == EnumFacing.Axis.X)
        {
        	OTGBlockPortalSize blockportal$size = new OTGBlockPortalSize(worldIn, pos, EnumFacing.Axis.X);

            if (!blockportal$size.isValid() || blockportal$size.portalBlockCount < blockportal$size.width * blockportal$size.height)
            {
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        else if (enumfacing$axis == EnumFacing.Axis.Z)
        {
        	OTGBlockPortalSize blockportal$size1 = new OTGBlockPortalSize(worldIn, pos, EnumFacing.Axis.Z);

            if (!blockportal$size1.isValid() || blockportal$size1.portalBlockCount < blockportal$size1.width * blockportal$size1.height)
            {
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        pos = pos.offset(side);
        EnumFacing.Axis enumfacing$axis = null;

        if (blockState.getBlock() == this)
        {
            enumfacing$axis = (EnumFacing.Axis)blockState.getValue(AXIS);

            if (enumfacing$axis == null)
            {
                return false;
            }

            if (enumfacing$axis == EnumFacing.Axis.Z && side != EnumFacing.EAST && side != EnumFacing.WEST)
            {
                return false;
            }

            if (enumfacing$axis == EnumFacing.Axis.X && side != EnumFacing.SOUTH && side != EnumFacing.NORTH)
            {
                return false;
            }
        }

        boolean flag = blockAccess.getBlockState(pos.west()).getBlock() == this && blockAccess.getBlockState(pos.west(2)).getBlock() != this;
        boolean flag1 = blockAccess.getBlockState(pos.east()).getBlock() == this && blockAccess.getBlockState(pos.east(2)).getBlock() != this;
        boolean flag2 = blockAccess.getBlockState(pos.north()).getBlock() == this && blockAccess.getBlockState(pos.north(2)).getBlock() != this;
        boolean flag3 = blockAccess.getBlockState(pos.south()).getBlock() == this && blockAccess.getBlockState(pos.south(2)).getBlock() != this;
        boolean flag4 = flag || flag1 || enumfacing$axis == EnumFacing.Axis.X;
        boolean flag5 = flag2 || flag3 || enumfacing$axis == EnumFacing.Axis.Z;

        if (flag4 && side == EnumFacing.WEST)
        {
            return true;
        }
        else if (flag4 && side == EnumFacing.EAST)
        {
            return true;
        }
        else if (flag5 && side == EnumFacing.NORTH)
        {
            return true;
        } else {
            return flag5 && side == EnumFacing.SOUTH;
        }
    }

    // Returns the quantity of items to drop on block destruction.
    public int quantityDropped(Random random)
    {
        return 0;
    }

    // Called When an Entity Collided with the Block
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
    	if (!entityIn.isBeingRidden() && entityIn.isNonBoss())
        {
    		if(entityIn.isRiding())
    		{
    			// Not dismounting doesn't seem to be a problem, player auto-dismounts when cart is teleported?
    			Entity entityRiding = entityIn.getRidingEntity();
    			setPortal(pos, entityRiding);
    		}
    		setPortal(pos, entityIn);
        }
    }
    
    /**
     * Marks the entity as being inside a portal, activating teleportation logic in onEntityUpdate() in the following
     * tick(s).
     */
    public void setPortal(BlockPos pos, Entity _this)
    {
        if (_this.timeUntilPortal > 0)
        {
        	_this.timeUntilPortal = _this.getPortalCooldown();
        } else {
            if (!_this.world.isRemote && !pos.equals(_this.lastPortalPos))
            {
            	_this.lastPortalPos = new BlockPos(pos);
            	BlockPattern.PatternHelper blockpattern$patternhelper = OTGBlockPatternHelper.createPatternHelper(_this.world, _this.lastPortalPos != null ? _this.lastPortalPos : _this.getPosition());
                double d0 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? (double)blockpattern$patternhelper.getFrontTopLeft().getZ() : (double)blockpattern$patternhelper.getFrontTopLeft().getX();
                double d1 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? _this.posZ : _this.posX;
                d1 = Math.abs(MathHelper.pct(d1 - (double)(blockpattern$patternhelper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - (double)blockpattern$patternhelper.getWidth()));
                double d2 = MathHelper.pct(_this.posY - 1.0D, (double)blockpattern$patternhelper.getFrontTopLeft().getY(), (double)(blockpattern$patternhelper.getFrontTopLeft().getY() - blockpattern$patternhelper.getHeight()));
                _this.lastPortalVec = new Vec3d(d1, d2, 0.0D);
                _this.teleportDirection = blockpattern$patternhelper.getForwards();
            }
            _this.inPortal = true;
        }
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return ItemStack.EMPTY;
    }

    // Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
	// transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    // Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    // this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    // of whether the block can receive random update ticks
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (rand.nextInt(100) == 0)
        {
            worldIn.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
        }
               
        double d0;
        double d1;
        double d2;
        double d3;
        double d4;
        double d5;
        int j;

        String portalColor = PortalColors.getPortalColorByPortalBlock(this);        
        DimensionsConfig dimsConfig = OTG.getDimensionsConfig();
        DimensionConfig dimConfig = null;
        if(dimsConfig.Overworld.Settings.PortalColor.equals(portalColor))
        {
        	dimConfig = dimsConfig.Overworld;
        } else {
	        if(dimsConfig.Dimensions != null)
	        {
	        	for(DimensionConfig dimConfig2 : dimsConfig.Dimensions)
	        	{
	        		if(portalColor.equals(dimConfig2.Settings.PortalColor.toLowerCase()))
	        		{
	        			dimConfig = dimConfig2;
	        			break;
	        		}
	        	}
	        }
        }        

        EnumParticleTypes particleType = dimConfig == null ? EnumParticleTypes.PORTAL : EnumParticleTypes.getByName(dimConfig.Settings.PortalParticleType);
        
    	if(particleType != null)
    	{
	        for (int i = 0; i < 4; ++i)
	        {
	            d0 = (double)((float)pos.getX() + rand.nextFloat());
	            d1 = (double)((float)pos.getY() + rand.nextFloat());
	            d2 = (double)((float)pos.getZ() + rand.nextFloat());
	            d3 = ((double)rand.nextFloat() - 0.5D) * 0.5D;
	            d4 = ((double)rand.nextFloat() - 0.5D) * 0.5D;
	            d5 = ((double)rand.nextFloat() - 0.5D) * 0.5D;
	            j = rand.nextInt(2) * 2 - 1;
	
	            if (worldIn.getBlockState(pos.west()).getBlock() != this && worldIn.getBlockState(pos.east()).getBlock() != this)
	            {
	                d0 = (double)pos.getX() + 0.5D + 0.25D * (double)j;
	                d3 = (double)(rand.nextFloat() * 2.0F * (float)j);
	            } else {
	                d2 = (double)pos.getZ() + 0.5D + 0.25D * (double)j;
	                d5 = (double)(rand.nextFloat() * 2.0F * (float)j);
	            }
	            
	           	worldIn.spawnParticle(particleType, d0, d1, d2, d3, d4, d5);
	        }
    	}
    }

    // Convert the BlockState into the correct metadata value
    public int getMetaFromState(IBlockState state)
    {
        return getMetaForAxis((EnumFacing.Axis)state.getValue(AXIS));
    }

	// Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	// blockstate.
	// @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
	// fine.
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        switch (rot)
        {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:

                switch ((EnumFacing.Axis)state.getValue(AXIS))
                {
                    case X:
                        return state.withProperty(AXIS, EnumFacing.Axis.Z);
                    case Z:
                        return state.withProperty(AXIS, EnumFacing.Axis.X);
                    default:
                        return state;
                }

            default:
                return state;
        }
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {AXIS});
    }

	// Get the geometry of the queried face at the given position and state. This is used to decide whether things like
	// buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
	// <p>
	// Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that
	// does not fit the other descriptions and will generally cause other things not to connect to the face.
 
	// @return an approximation of the form of the given face
	// @deprecated call via {@link IBlockState#getBlockFaceShape(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
	// Implementing/overriding is fine.
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }
}