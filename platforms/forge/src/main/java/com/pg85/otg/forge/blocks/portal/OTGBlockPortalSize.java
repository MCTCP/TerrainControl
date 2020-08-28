package com.pg85.otg.forge.blocks.portal;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.blocks.ModBlocks;
import com.pg85.otg.forge.blocks.PortalColors;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.world.ForgeWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OTGBlockPortalSize
{
    private final World world;
    private final EnumFacing.Axis axis;
    final EnumFacing rightDir;
    private final EnumFacing leftDir;
    public int portalBlockCount;
    BlockPos bottomLeft;
    public int height;
    public int width;
    private Block portalBlock;
    
    // Used for checking for portals in the destination world
    OTGBlockPortalSize(World sourceWorld, World destinationWorld, BlockPos spawnPos, EnumFacing.Axis p_i45694_3_)
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
        	portalMaterials = OTG.getDimensionsConfig().Overworld.Settings.GetDimensionPortalMaterials();
        } else {            
            ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorld(sourceWorld);            
			portalMaterials = OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName()).Settings.GetDimensionPortalMaterials();
        }
		
		if(getDistanceUntilEdgeForPortalMaterials(portalMaterials, spawnPos))
		{
			return;
		}
    }
    
    // Used when creating portals in the source world
    public OTGBlockPortalSize(World sourceWorld, BlockPos spawnPos, EnumFacing.Axis p_i45694_3_)
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
        
		ForgeWorld overworld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
		ArrayList<LocalWorld> forgeWorlds = ((ForgeEngine)OTG.getEngine()).getAllWorlds();
		
        /* TODO: May still need this, remove after testing (make sure mc loads/unloads nether at the proper times now that we've switched to OTG portal blocks).
        // Try portal materials for each world. Also try nether if no world uses obsidian.
		boolean worldUsesObsidian = false;		
		if(overworld == null) // This is a vanilla overworld
		{
			ArrayList<LocalMaterialData> portalMaterials = OTG.getDimensionsConfig().Overworld.Settings.GetDimensionPortalMaterials();
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
				ArrayList<LocalMaterialData> portalMaterials = OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName()).Settings.GetDimensionPortalMaterials();
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
			portalMaterials.add(MaterialHelper.toLocalMaterialData(DefaultMaterial.OBSIDIAN, 0));
			// Only allow portals to nether from overworld
			if(sourceWorld.provider.getDimension() == 0 && getDistanceUntilEdgeForPortalMaterials(portalMaterials, spawnPos))
			{
				return;
			}
		}
		*/
		
		if(overworld == null && sourceWorld.provider.getDimension() != 0) // This is a vanilla overworld, player is not in overworld so may want to portal to it
		{ 
			// TODO: Does this still happen?
			ArrayList<LocalMaterialData> portalMaterials = OTG.getDimensionsConfig().Overworld.Settings.GetDimensionPortalMaterials();
			this.portalBlock = PortalColors.getPortalBlockByColor(OTG.getDimensionsConfig().Overworld.Settings.PortalColor);
			if(getDistanceUntilEdgeForPortalMaterials(portalMaterials, spawnPos))
			{				
				return;
			}
		}
		for(LocalWorld localWorld : forgeWorlds)
		{
			ForgeWorld forgeWorld = (ForgeWorld)localWorld;
			ArrayList<LocalMaterialData> portalMaterials = OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName()).Settings.GetDimensionPortalMaterials();
			this.portalBlock = PortalColors.getPortalBlockByColor(OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName()).Settings.PortalColor);
			
			if(forgeWorld.getDimensionId() != sourceWorld.provider.getDimension())
			{
				if(getDistanceUntilEdgeForPortalMaterials(portalMaterials, spawnPos))
				{					
					return;
				}
			}
		}
    }
    
    private boolean getDistanceUntilEdgeForPortalMaterials(ArrayList<LocalMaterialData> portalMaterials, BlockPos p_i45694_2_)
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
    
    private int getDistanceUntilEdge(ArrayList<LocalMaterialData> portalMaterials, BlockPos p_180120_1_, EnumFacing p_180120_2_)
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

    private int calculatePortalHeight(ArrayList<LocalMaterialData> portalMaterials)
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

                if (block instanceof BlockPortalOTG) // TODO: avoid using instanceof so much?
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

    private boolean isEmptyBlock(Block blockIn)
    {
        return blockIn.getMaterial(null) == Material.AIR || blockIn == Blocks.FIRE || blockIn == Blocks.PORTAL || blockIn instanceof BlockPortalOTG;  // TODO: avoid using instanceof so much?
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
                this.world.setBlockState(blockpos.up(j), this.portalBlock.getDefaultState().withProperty(BlockPortal.AXIS, this.axis), 2);
            }
        }
    }
}