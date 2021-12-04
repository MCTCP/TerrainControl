
package com.pg85.otg.forge.event;

import java.nio.file.Path;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.resource.SaplingResource;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.ISaplingSpawner;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.minecraft.SaplingType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Allows users to configure different trees that spawn
// from saplings, including modded saplings and BO2/BO3 trees.
@EventBusSubscriber(modid = Constants.MOD_ID_SHORT)
public class SaplingHandler
{
	@SubscribeEvent
	public void onSaplingGrowTree(SaplingGrowTreeEvent event)
	{
    	if(!(event.getWorld() instanceof ServerLevel))
    	{
    		return;
    	}
		
        BlockPos blockPos = event.getPos();
		ForgeWorldGenRegion worldGenRegion;	
		Preset preset;
		if(((ServerLevel)event.getWorld()).getChunkSource().generator instanceof OTGNoiseChunkGenerator)
		{
			preset = ((OTGNoiseChunkGenerator)((ServerLevel)event.getWorld()).getChunkSource().generator).getPreset();
			worldGenRegion = new ForgeWorldGenRegion(
				preset.getFolderName(), 
				preset.getWorldConfig(), 
				(ServerLevel)event.getWorld(), 
				((OTGNoiseChunkGenerator)((ServerLevel)event.getWorld()).getChunkSource().generator)
			);
		} else { 
			return;
		}

		CustomObjectManager customObjectManager = OTG.getEngine().getCustomObjectManager();
		CustomObjectResourcesManager customObjectResourcesManager = OTG.getEngine().getCustomObjectResourcesManager();
		ILogger logger = OTG.getEngine().getLogger();
		Path otgRootFolder = OTG.getEngine().getOTGRootFolder();
		IModLoadedChecker modLoadedChecker = OTG.getEngine().getModLoadedChecker();		
		IMaterialReader materialReader = OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName());

        IBiomeConfig biomeConfig = worldGenRegion.getCachedBiomeProvider().getBiomeConfig(blockPos.getX(), blockPos.getZ());
        ForgeMaterialData material = (ForgeMaterialData)worldGenRegion.getMaterial(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        BlockPos result = findFourSaplings(blockPos, material, worldGenRegion);
        boolean wideTrunk = false;
        if (result != null)
        {
            blockPos = result;
            wideTrunk = true;
        } else {
            // Not a wide trunk
            wideTrunk = false;
        }        
        
        // Get the sapling generator
        ISaplingSpawner sapling = biomeConfig.getCustomSaplingGen(ForgeMaterialData.ofBlockState(material.internalBlock().getBlock().defaultBlockState()), wideTrunk);
        if(sapling != null && sapling.hasWideTrunk() && !wideTrunk)
        {
        	return;
        }
        if(sapling == null)
        {
            // Check whether block is a sapling. If not, assume custom sapling block.
        	SaplingType saplingType = null;
        	if(wideTrunk)
        	{
        		// Try to find big (2x2) sapling
        		saplingType = getBigSaplingType(ForgeMaterialData.ofBlockState(material.internalBlock().getBlock().defaultBlockState()));
        	}
            // If not big sapling, try to find small sapling
            if (saplingType == null)
            {
                saplingType = getSmallSaplingType(ForgeMaterialData.ofBlockState(material.internalBlock().getBlock().defaultBlockState()));
            }
            if(saplingType != null)
            {
            	sapling = biomeConfig.getSaplingGen(saplingType);
            }
        }
        if (sapling == null)
        {
            // No sapling generator set for this sapling
            return;
        }
        
        // When we have reached this point, we know that we have to handle the
        // event ourselves
        // So cancel it
        event.setResult(Result.DENY);

        // Remove saplings

        if (sapling.hasWideTrunk())
        {
        	worldGenRegion.setBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ(), LocalMaterials.AIR);
        	worldGenRegion.setBlock(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ(), LocalMaterials.AIR);
        	worldGenRegion.setBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 1, LocalMaterials.AIR);
        	worldGenRegion.setBlock(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ() + 1, LocalMaterials.AIR);
        } else {
        	worldGenRegion.setBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ(), LocalMaterials.AIR);
        }

        // Try ten times to grow sapling
        boolean saplingGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (((SaplingResource)sapling).growSapling(worldGenRegion, random, wideTrunk, blockPos.getX(), blockPos.getY(), blockPos.getZ(), preset.getFolderName(), otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker))
            {
                saplingGrown = true;
                break;
            }
        }

        if (!saplingGrown)
        {
            // Restore sapling
            int saplingX = blockPos.getX();
            int saplingY = blockPos.getY();
            int saplingZ = blockPos.getZ();
            if (sapling.hasWideTrunk())
            {
            	worldGenRegion.setBlock(saplingX, saplingY, saplingZ, material);
            	worldGenRegion.setBlock(saplingX + 1, saplingY, saplingZ, material);
            	worldGenRegion.setBlock(saplingX, saplingY, saplingZ + 1, material);
            	worldGenRegion.setBlock(saplingX + 1, saplingY, saplingZ + 1, material);
            } else {
            	worldGenRegion.setBlock(saplingX, saplingY, saplingZ, material);
            }
        }
	}

    @SubscribeEvent
    public void onBonemealUse(BonemealEvent event)
    {
    	if(!(event.getWorld() instanceof ServerLevel))
    	{
    		return;
    	}

        BlockPos blockPos = event.getPos();        
		IWorldGenRegion worldGenRegion;	
		Preset preset;
		if(((ServerLevel)event.getWorld()).getChunkSource().generator instanceof OTGNoiseChunkGenerator)
		{
			preset = ((OTGNoiseChunkGenerator)((ServerLevel)event.getWorld()).getChunkSource().generator).getPreset();
			worldGenRegion = new ForgeWorldGenRegion(
				preset.getFolderName(), 
				preset.getWorldConfig(), 
				(ServerLevel)event.getWorld(), 
				((OTGNoiseChunkGenerator)((ServerLevel)event.getWorld()).getChunkSource().generator)
			);
		} else {
			return;
		}
		
		CustomObjectManager customObjectManager = OTG.getEngine().getCustomObjectManager();
		CustomObjectResourcesManager customObjectResourcesManager = OTG.getEngine().getCustomObjectResourcesManager();
		ILogger logger = OTG.getEngine().getLogger();
		Path otgRootFolder = OTG.getEngine().getOTGRootFolder();
		IModLoadedChecker modLoadedChecker = OTG.getEngine().getModLoadedChecker();		
		IMaterialReader materialReader = OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName());		
		
        IBiomeConfig biomeConfig = worldGenRegion.getCachedBiomeProvider().getBiomeConfig(blockPos.getX(), blockPos.getZ());	

        // Get sapling gen
        ISaplingSpawner sapling = null;
        SaplingType type = null;
        if (ForgeMaterialData.ofBlockState(event.getBlock()).isMaterial(LocalMaterials.RED_MUSHROOM_BLOCK))
        {
            type = SaplingType.RedMushroom;
        }
        else if (ForgeMaterialData.ofBlockState(event.getBlock()).isMaterial(LocalMaterials.BROWN_MUSHROOM_BLOCK))
        {
            type = SaplingType.BrownMushroom;
        } else {
            return;
        }
        if(type != null)
        {
        	sapling = biomeConfig.getSaplingGen(type);
        }
        // No matching saplingType, return;
        if (sapling == null)
        {
            return;
        }
        // Generate mushroom
        event.setResult(Result.ALLOW);
        worldGenRegion.setBlock(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), LocalMaterials.AIR);

        boolean mushroomGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
        	if (((SaplingResource)sapling).growSapling(worldGenRegion, random, false, blockPos.getX(), blockPos.getY(), blockPos.getZ(), preset.getFolderName(), otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker))
            {
                mushroomGrown = true;
                break;
            }
        }
        if (!mushroomGrown)
        {
            // Restore mushroom
        	worldGenRegion.setBlock(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), ForgeMaterialData.ofBlockState(event.getBlock()));
        }
    }
        
    /**
     * Gets the sapling type, based on the assumption that the sapling is
     * not placed in a 2x2 pattern.
     *
     * @param data The block data of the sapling block.
     * @return The sapling type, or null if not found.
     */
    private SaplingType getSmallSaplingType(LocalMaterialData saplingMaterial)
    {
        if(saplingMaterial == LocalMaterials.OAK_SAPLING)
        {
            return SaplingType.Oak;
        }
        else if(saplingMaterial == LocalMaterials.SPRUCE_SAPLING)
        {
            return SaplingType.Redwood;
        }
        else if(saplingMaterial == LocalMaterials.BIRCH_SAPLING)
        {                
            return SaplingType.Birch;
        }
        else if(saplingMaterial == LocalMaterials.JUNGLE_SAPLING)
        {                
            return SaplingType.SmallJungle;
        }
        else if(saplingMaterial == LocalMaterials.ACACIA_SAPLING)
        {                
            return SaplingType.Acacia;
        }
        else if(saplingMaterial == LocalMaterials.BAMBOO_SAPLING)
        {                
            return SaplingType.Bamboo;
        }
        return null;
    }

    /**
     * Gets the sapling type, based on the assumption that the saplings must
     * be placed in a 2x2 pattern. Will never return one of the smaller
     * sapling types.
     *
     * @param data The block data of the sapling block.
     * @return The sapling type, or null if not found.
     */
    private SaplingType getBigSaplingType(LocalMaterialData saplingMaterial)
    {
        if(saplingMaterial == LocalMaterials.DARK_OAK_SAPLING)
        {
        	return SaplingType.DarkOak;
        }
        else if(saplingMaterial == LocalMaterials.SPRUCE_SAPLING)
        {
        	return SaplingType.HugeRedwood;
        }
        else if(saplingMaterial == LocalMaterials.JUNGLE_SAPLING)
        {
        	return SaplingType.BigJungle;
        }
        return null;
    }

    /**
     * Gets whether the saplings are placed in a 2x2 pattern. If successful,
     * it returns a BlockPos that represents the top left
     * sapling (with the lowest x and z). If not, it returns null.
     *
     * @return BlockPos of sapling with lowest X and Z, or null if not four saplings
     */
    private BlockPos findFourSaplings(BlockPos blockPos, LocalMaterialData material, IWorldGenRegion worldGenRegion)
    {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        for (int treeOffsetX = 0; treeOffsetX >= -1; --treeOffsetX)
        {
            for (int treeOffsetZ = 0; treeOffsetZ >= -1; --treeOffsetZ)
            {
                if (
                	material.isMaterial(worldGenRegion.getMaterial(x + treeOffsetX, y, z + treeOffsetZ))
                    && material.isMaterial(worldGenRegion.getMaterial(x + treeOffsetX + 1, y, z + treeOffsetZ))
                    && material.isMaterial(worldGenRegion.getMaterial(x + treeOffsetX, y, z + treeOffsetZ + 1))
                    && material.isMaterial(worldGenRegion.getMaterial(x + treeOffsetX + 1, y, z + treeOffsetZ + 1))
        		)
                {
                    // Found! Adjust internal position
                    return blockPos.offset(treeOffsetX, 0, treeOffsetZ);
                }
            }
        }
        return null;
    }
}
