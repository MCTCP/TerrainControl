package com.pg85.otg.forge.dimensions;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.google.common.cache.LoadingCache;
import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.generator.Cartographer;
import com.pg85.otg.forge.util.ForgeMaterialData;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class OTGBlockPortal
{
    public static boolean trySpawnPortal(World worldIn, BlockPos pos)
    {
    	boolean cartographerEnabled = ((ForgeEngine)OTG.getEngine()).getCartographerEnabled();
    	
    	// Only create a portal when a OTG custom dimension exists
    	boolean bFound = false;
    	boolean bFoundOtherThanCartographer = false;
    	for(int i : OTGDimensionManager.GetOTGDimensions())
    	{
			if(DimensionManager.isDimensionRegistered(i))
			{
				bFound = true;
				if(cartographerEnabled)
				{
					if(i != Cartographer.CartographerDimension)
					{
						bFoundOtherThanCartographer = true;
						break;
					}
				} else {
					break;
				}
			}
		}
		
		// If Cartographer is on and this is not a Cartographer portal and no other dimensions were found don't spawn a portal.
		// TODO: Clean up all this cartographer crap
		IBlockState blockState = worldIn.getBlockState(pos);
		BlockPos firstSolidBlockPos = new BlockPos(pos);
		while(!blockState.getMaterial().isSolid() && firstSolidBlockPos.getY() > 0)
		{
			firstSolidBlockPos = new BlockPos(firstSolidBlockPos.getX(), firstSolidBlockPos.getY() - 1, firstSolidBlockPos.getZ());
			blockState = worldIn.getBlockState(firstSolidBlockPos);
		}
		// Cartographer looks for chiseled quartz (?)
		boolean isCartographerPortal = blockState.getBlock() == Blocks.QUARTZ_BLOCK && (byte) blockState.getBlock().getMetaFromState(blockState) == 1;
		if(!(!cartographerEnabled && bFound) && (cartographerEnabled && !bFoundOtherThanCartographer && !isCartographerPortal))
		{
			return false;
		}
    	
        OTGBlockPortal.Size blockportal$size = new OTGBlockPortal.Size(worldIn, pos, EnumFacing.Axis.X);

        if (blockportal$size.isValid() && blockportal$size.portalBlockCount == 0)
        {
            blockportal$size.placePortalBlocks();
            return true;
        } else {
            OTGBlockPortal.Size blockportal$size1 = new OTGBlockPortal.Size(worldIn, pos, EnumFacing.Axis.Z);

            if (blockportal$size1.isValid() && blockportal$size1.portalBlockCount == 0)
            {
                blockportal$size1.placePortalBlocks();
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static class Size
    {
        private final World world;
        private final EnumFacing.Axis axis;
        private final EnumFacing rightDir;
        private final EnumFacing leftDir;
        private int portalBlockCount;
        private BlockPos bottomLeft;
        private int height;
        private int width;
        
        // Used for checking for portals in the destination world
        public Size(World sourceWorld, World destinationWorld, BlockPos spawnPos, EnumFacing.Axis p_i45694_3_)
        {
            this.world = destinationWorld;
            this.axis = p_i45694_3_;

            if (p_i45694_3_ == EnumFacing.Axis.X)
            {
                this.leftDir = EnumFacing.EAST;
                this.rightDir = EnumFacing.WEST;
            } else {
                this.leftDir = EnumFacing.NORTH;
                this.rightDir = EnumFacing.SOUTH;
            }

            for (BlockPos blockpos = spawnPos; spawnPos.getY() > blockpos.getY() - 21 && spawnPos.getY() > 0 && this.isEmptyBlock(world.getBlockState(spawnPos.down()).getBlock()); spawnPos = spawnPos.down())
            {
                ;
            }
           
            ArrayList<LocalMaterialData> portalMaterials = null;
            if(sourceWorld.provider.getDimension() == 0)
            {
            	portalMaterials = OTG.GetDimensionsConfig().Overworld.Settings.GetDimensionPortalMaterials();
            } else {            
	            ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorld(sourceWorld);            
				portalMaterials = OTG.GetDimensionsConfig().GetDimensionConfig(forgeWorld.getName()).Settings.GetDimensionPortalMaterials();
            }
			
			if(getDistanceUntilEdgeForPortalMaterials(portalMaterials, spawnPos))
			{
				return;
			}
        }
        
        // Used when creating portals in the source world
        public Size(World sourceWorld, BlockPos spawnPos, EnumFacing.Axis p_i45694_3_)
        {
            this.world = sourceWorld;
            this.axis = p_i45694_3_;

            if (p_i45694_3_ == EnumFacing.Axis.X)
            {
                this.leftDir = EnumFacing.EAST;
                this.rightDir = EnumFacing.WEST;
            } else {
                this.leftDir = EnumFacing.NORTH;
                this.rightDir = EnumFacing.SOUTH;
            }
            
            for (BlockPos blockpos = spawnPos; spawnPos.getY() > blockpos.getY() - 21 && spawnPos.getY() > 0 && this.isEmptyBlock(sourceWorld.getBlockState(spawnPos.down()).getBlock()); spawnPos = spawnPos.down())
            {
                ;
            }
           
            // Try portal materials for each world. Also try nether if no world uses obsidian.
			boolean worldUsesObsidian = false;
			ArrayList<LocalWorld> forgeWorlds = ((ForgeEngine)OTG.getEngine()).getAllWorlds();
			ForgeWorld overworld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
			if(overworld == null) // This is a vanilla overworld
			{
				ArrayList<LocalMaterialData> portalMaterials = OTG.GetDimensionsConfig().Overworld.Settings.GetDimensionPortalMaterials();
				for(LocalMaterialData portalMaterial : portalMaterials)
				{
					if(portalMaterial.toDefaultMaterial().equals(DefaultMaterial.OBSIDIAN))
					{
						worldUsesObsidian = true;
						break;						
					}
				}
			}
			if(!worldUsesObsidian)
			{
				for(LocalWorld localWorld : forgeWorlds)
				{
					ForgeWorld forgeWorld = (ForgeWorld)localWorld;
					ArrayList<LocalMaterialData> portalMaterials = OTG.GetDimensionsConfig().GetDimensionConfig(forgeWorld.getName()).Settings.GetDimensionPortalMaterials();
					for(LocalMaterialData portalMaterial : portalMaterials)
					{
						if(portalMaterial.toDefaultMaterial().equals(DefaultMaterial.OBSIDIAN))
						{
							worldUsesObsidian = true;
							break;						
						}
					}
					if(worldUsesObsidian)
					{
						break;
					}
				}
			}
			
			// If no world uses obsidian try nether.
			if(!worldUsesObsidian)
			{				
				ArrayList<LocalMaterialData> portalMaterials = new ArrayList<LocalMaterialData>();
				portalMaterials.add(OTG.toLocalMaterialData(DefaultMaterial.OBSIDIAN, 0));
				// Only allow portals to nether from overworld
				if(sourceWorld.provider.getDimension() == 0 && getDistanceUntilEdgeForPortalMaterials(portalMaterials, spawnPos))
				{
					return;
				}
			}
			
			if(overworld == null && sourceWorld.provider.getDimension() != 0) // This is a vanilla overworld, player is not in overworld so may want to portal to it
			{ // TODO: Does this still happen?
				ArrayList<LocalMaterialData> portalMaterials = OTG.GetDimensionsConfig().Overworld.Settings.GetDimensionPortalMaterials();
				if(getDistanceUntilEdgeForPortalMaterials(portalMaterials, spawnPos))
				{
					return;
				}
			}
			for(LocalWorld localWorld : forgeWorlds)
			{
				ForgeWorld forgeWorld = (ForgeWorld)localWorld;
				ArrayList<LocalMaterialData> portalMaterials = OTG.GetDimensionsConfig().GetDimensionConfig(forgeWorld.getName()).Settings.GetDimensionPortalMaterials();
				
				if(forgeWorld.getDimensionId() != sourceWorld.provider.getDimension())
				{
					if(getDistanceUntilEdgeForPortalMaterials(portalMaterials, spawnPos))
					{
						return;
					}
				}
			}
        }
        
        boolean getDistanceUntilEdgeForPortalMaterials(ArrayList<LocalMaterialData> portalMaterials, BlockPos p_i45694_2_)
        {
            int i = this.getDistanceUntilEdge(portalMaterials, p_i45694_2_, this.leftDir) - 1;
    		
            if (i >= 0)
            {
                this.bottomLeft = p_i45694_2_.offset(this.leftDir, i);
                this.width = this.getDistanceUntilEdge(portalMaterials, this.bottomLeft, this.rightDir);

                if (this.width < 2 || this.width > 21)
                {
                    this.bottomLeft = null;
                    this.width = 0;
                }
            }

            if (this.bottomLeft != null)
            {
                this.height = this.calculatePortalHeight(portalMaterials);
            }
            
            if(height > 0 && width > 0)
            {
            	return true;
            }
            
            return false;
        }
        
        protected int getDistanceUntilEdge(ArrayList<LocalMaterialData> portalMaterials, BlockPos p_180120_1_, EnumFacing p_180120_2_)
        {
            int i;
            
            for (i = 0; i < 22; ++i)
            {
                BlockPos blockpos = p_180120_1_.offset(p_180120_2_, i);
                
                Block block = this.world.getBlockState(blockpos).getBlock();
                IBlockState blockStateDown = this.world.getBlockState(blockpos.down());

				ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(blockStateDown);
				boolean isPortalMaterial = false;
				for(LocalMaterialData portalMaterial : portalMaterials)
				{
					if(material.toDefaultMaterial().equals(portalMaterial.toDefaultMaterial()) && material.getBlockData() == portalMaterial.getBlockData())
					{
						isPortalMaterial = true;
					}
				}
                
				if(!this.isEmptyBlock(block) || !isPortalMaterial)
				{
					break;
				}
            }

            IBlockState blockState = this.world.getBlockState(p_180120_1_.offset(p_180120_2_, i));
            
			ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(blockState);
			boolean isPortalMaterial = false;
			for(LocalMaterialData portalMaterial : portalMaterials)
			{
				if(material.toDefaultMaterial().equals(portalMaterial.toDefaultMaterial()) && material.getBlockData() == portalMaterial.getBlockData())
				{
					isPortalMaterial = true;
				}
			}
            
            return isPortalMaterial ? i : 0;
        }

        public int getHeight()
        {
            return this.height;
        }

        public int getWidth()
        {
            return this.width;
        }

        protected int calculatePortalHeight(ArrayList<LocalMaterialData> portalMaterials)
        {        	
        	label24:
        	
            for (this.height = 0; this.height < 21; ++this.height)
            {
                for (int i = 0; i < this.width; ++i)
                {
                    BlockPos blockpos = this.bottomLeft.offset(this.rightDir, i).up(this.height);
                    Block block = this.world.getBlockState(blockpos).getBlock();

                    if (!this.isEmptyBlock(block))
                    {
                        break label24;
                    }

                    if (block == Blocks.PORTAL)
                    {
                        ++this.portalBlockCount;
                    }

                    if (i == 0)
                    {
                    	IBlockState blockState = this.world.getBlockState(blockpos.offset(this.leftDir));
                        block = blockState.getBlock();

        				ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(blockState);
        				boolean isPortalMaterial = false;
        				for(LocalMaterialData portalMaterial : portalMaterials)
        				{
        					if(material.toDefaultMaterial().equals(portalMaterial.toDefaultMaterial()) && material.getBlockData() == portalMaterial.getBlockData())
        					{
        						isPortalMaterial = true;
        					}
        				}

                        if (!isPortalMaterial)
                        {
                            break label24;
                        }
                    }
                    else if (i == this.width - 1)
                    {
                    	IBlockState blockState = this.world.getBlockState(blockpos.offset(this.rightDir));
                        block = blockState.getBlock();

        				ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(blockState);
        				boolean isPortalMaterial = false;
        				for(LocalMaterialData portalMaterial : portalMaterials)
        				{
        					if(material.toDefaultMaterial().equals(portalMaterial.toDefaultMaterial()) && material.getBlockData() == portalMaterial.getBlockData())
        					{
        						isPortalMaterial = true;
        					}
        				}
                        
                        if (!isPortalMaterial)
                        {
                            break label24;
                        }
                    }
                }
            }

            for (int j = 0; j < this.width; ++j)
            {
            	IBlockState blockState = this.world.getBlockState(this.bottomLeft.offset(this.rightDir, j).up(this.height));
            	
				ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(blockState);
				boolean isPortalMaterial = false;
				for(LocalMaterialData portalMaterial : portalMaterials)
				{
					if(material.toDefaultMaterial().equals(portalMaterial.toDefaultMaterial()) && material.getBlockData() == portalMaterial.getBlockData())
					{
						isPortalMaterial = true;
					}
				}

				if (!isPortalMaterial)
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

        protected boolean isEmptyBlock(Block blockIn)
        {
            return blockIn.getMaterial(null) == Material.AIR || blockIn == Blocks.FIRE || blockIn == Blocks.PORTAL;
        }

        public boolean isValid()
        {
            return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
        }

        public void placePortalBlocks()
        {
            for (int i = 0; i < this.width; ++i)
            {
                BlockPos blockpos = this.bottomLeft.offset(this.rightDir, i);

                for (int j = 0; j < this.height; ++j)
                {
                    this.world.setBlockState(blockpos.up(j), Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, this.axis), 2);
                }
            }
        }
    }
    
    public static void placeInExistingPortal(int dimensionId, BlockPos pos)
    {    
    	MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
    	WorldServer worldServerInstance = mcServer.getWorld(dimensionId);
    	Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache = null;
    	Teleporter _this = worldServerInstance.getDefaultTeleporter();
		try {
			Field[] fields = _this.getClass().getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(Long2ObjectMap.class))
				{
					field.setAccessible(true);
					destinationCoordinateCache = (Long2ObjectMap<Teleporter.PortalPosition>) field.get(_this);
			        break;
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    	
        int j = MathHelper.floor(pos.getX());
        int k = MathHelper.floor(pos.getZ());
        long l = ChunkPos.asLong(j, k);
        
        if (destinationCoordinateCache.containsKey(l))
        {
            Teleporter.PortalPosition teleporter$portalposition = (Teleporter.PortalPosition)destinationCoordinateCache.get(l);
            teleporter$portalposition.lastUpdateTime = worldServerInstance.getTotalWorldTime();
        } else {
        	destinationCoordinateCache.put(l, _this.new PortalPosition(pos, worldServerInstance.getTotalWorldTime()));
        }
    }
    
    // Used to check if a portal exists in the destination world at the given coordinates,
    // portal must be made of the source world's portal materials.
    public static BlockPattern.PatternHelper createPatternHelper(World sourceWorld, World destinationWorld, BlockPos p_181089_2_)
    {
        EnumFacing.Axis enumfacing$axis = EnumFacing.Axis.Z;
        OTGBlockPortal.Size blockportal$size = new OTGBlockPortal.Size(sourceWorld, destinationWorld, p_181089_2_, EnumFacing.Axis.X);
        LoadingCache<BlockPos, BlockWorldState> loadingcache = BlockPattern.createLoadingCache(destinationWorld, true);

        if (!blockportal$size.isValid())
        {
            enumfacing$axis = EnumFacing.Axis.X;
            blockportal$size = new OTGBlockPortal.Size(sourceWorld, destinationWorld, p_181089_2_, EnumFacing.Axis.Z);
        }

        if (!blockportal$size.isValid())
        {
            return new BlockPattern.PatternHelper(p_181089_2_, EnumFacing.NORTH, EnumFacing.UP, loadingcache, 1, 1, 1);
        } else {
            int[] aint = new int[EnumFacing.AxisDirection.values().length];
            EnumFacing enumfacing = blockportal$size.rightDir.rotateYCCW();
            BlockPos blockpos = blockportal$size.bottomLeft.up(blockportal$size.getHeight() - 1);

            for (EnumFacing.AxisDirection enumfacing$axisdirection : EnumFacing.AxisDirection.values())
            {
                BlockPattern.PatternHelper blockpattern$patternhelper = new BlockPattern.PatternHelper(enumfacing.getAxisDirection() == enumfacing$axisdirection ? blockpos : blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1), EnumFacing.getFacingFromAxis(enumfacing$axisdirection, enumfacing$axis), EnumFacing.UP, loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);

                for (int i = 0; i < blockportal$size.getWidth(); ++i)
                {
                    for (int j = 0; j < blockportal$size.getHeight(); ++j)
                    {
                        BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(i, j, 1);

                        if (blockworldstate.getBlockState() != null && blockworldstate.getBlockState().getMaterial() != Material.AIR)
                        {
                            ++aint[enumfacing$axisdirection.ordinal()];
                        }
                    }
                }
            }

            EnumFacing.AxisDirection enumfacing$axisdirection1 = EnumFacing.AxisDirection.POSITIVE;

            for (EnumFacing.AxisDirection enumfacing$axisdirection2 : EnumFacing.AxisDirection.values())
            {
                if (aint[enumfacing$axisdirection2.ordinal()] < aint[enumfacing$axisdirection1.ordinal()])
                {
                    enumfacing$axisdirection1 = enumfacing$axisdirection2;
                }
            }

            return new BlockPattern.PatternHelper(enumfacing.getAxisDirection() == enumfacing$axisdirection1 ? blockpos : blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1), EnumFacing.getFacingFromAxis(enumfacing$axisdirection1, enumfacing$axis), EnumFacing.UP, loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);
        }
    }    
    
    // Used to check if any portal exists in the given world at the given coordinates
    public static BlockPattern.PatternHelper createPatternHelper(World worldIn, BlockPos p_181089_2_)
    {
        EnumFacing.Axis enumfacing$axis = EnumFacing.Axis.Z;
        OTGBlockPortal.Size blockportal$size = new OTGBlockPortal.Size(worldIn, p_181089_2_, EnumFacing.Axis.X);
        LoadingCache<BlockPos, BlockWorldState> loadingcache = BlockPattern.createLoadingCache(worldIn, true);

        if (!blockportal$size.isValid())
        {
            enumfacing$axis = EnumFacing.Axis.X;
            blockportal$size = new OTGBlockPortal.Size(worldIn, p_181089_2_, EnumFacing.Axis.Z);
        }

        if (!blockportal$size.isValid())
        {
            return new BlockPattern.PatternHelper(p_181089_2_, EnumFacing.NORTH, EnumFacing.UP, loadingcache, 1, 1, 1);
        } else {
            int[] aint = new int[EnumFacing.AxisDirection.values().length];
            EnumFacing enumfacing = blockportal$size.rightDir.rotateYCCW();
            BlockPos blockpos = blockportal$size.bottomLeft.up(blockportal$size.getHeight() - 1);

            for (EnumFacing.AxisDirection enumfacing$axisdirection : EnumFacing.AxisDirection.values())
            {
                BlockPattern.PatternHelper blockpattern$patternhelper = new BlockPattern.PatternHelper(enumfacing.getAxisDirection() == enumfacing$axisdirection ? blockpos : blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1), EnumFacing.getFacingFromAxis(enumfacing$axisdirection, enumfacing$axis), EnumFacing.UP, loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);

                for (int i = 0; i < blockportal$size.getWidth(); ++i)
                {
                    for (int j = 0; j < blockportal$size.getHeight(); ++j)
                    {
                        BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(i, j, 1);

                        if (blockworldstate.getBlockState() != null && blockworldstate.getBlockState().getMaterial() != Material.AIR)
                        {
                            ++aint[enumfacing$axisdirection.ordinal()];
                        }
                    }
                }
            }

            EnumFacing.AxisDirection enumfacing$axisdirection1 = EnumFacing.AxisDirection.POSITIVE;

            for (EnumFacing.AxisDirection enumfacing$axisdirection2 : EnumFacing.AxisDirection.values())
            {
                if (aint[enumfacing$axisdirection2.ordinal()] < aint[enumfacing$axisdirection1.ordinal()])
                {
                    enumfacing$axisdirection1 = enumfacing$axisdirection2;
                }
            }

            return new BlockPattern.PatternHelper(enumfacing.getAxisDirection() == enumfacing$axisdirection1 ? blockpos : blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1), EnumFacing.getFacingFromAxis(enumfacing$axisdirection1, enumfacing$axis), EnumFacing.UP, loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);
        }
    }
}