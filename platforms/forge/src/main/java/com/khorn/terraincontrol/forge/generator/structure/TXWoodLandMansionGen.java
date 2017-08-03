package com.khorn.terraincontrol.forge.generator.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.generator.TXChunkGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import net.minecraft.init.Blocks;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.WoodlandMansionPieces;

public class TXWoodLandMansionGen extends MapGenStructure
{
	static
	{
		MapGenStructureIO.registerStructure(Start.class, "MansionOTG");
	}
	
    private int spacing = 80;
    private int separation = 20;
	
    private final List<Biome> woodLandMansionSpawnBiomes;
	
    public TXWoodLandMansionGen(ConfigProvider settings)
    {
        this.woodLandMansionSpawnBiomes = new ArrayList<Biome>();
        
        for (LocalBiome biome : settings.getBiomeArray())
        {
            if (biome == null || !biome.getBiomeConfig().woodLandMansionsEnabled)
            {
                continue;
            }

            this.woodLandMansionSpawnBiomes.add(((ForgeBiome) biome).getHandle());
        }
    }
    
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0)
        {
            i = chunkX - 79;
        }

        if (chunkZ < 0)
        {
            j = chunkZ - 79;
        }

        int k = i / 80;
        int l = j / 80;
        Random random = this.world.setRandomSeed(k, l, 10387319);
        k = k * 80;
        l = l * 80;
        k = k + (random.nextInt(60) + random.nextInt(60)) / 2;
        l = l + (random.nextInt(60) + random.nextInt(60)) / 2;

        if (chunkX == k && chunkZ == l)
        {
        	boolean flag = this.world.getBiomeProvider().areBiomesViable(chunkX * 16 + 8, chunkZ * 16 + 8, 32, woodLandMansionSpawnBiomes);

            if (flag)
            {
            	TerrainControl.log(LogMarker.INFO, "SPAWNING MANSION AT " + (chunkX * 16 + 8) + " 100 " + (chunkZ * 16 + 8));
                return true;
            }
        }

        return false;
    }    
    
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
    {
        this.world = worldIn;
        BiomeProvider biomeprovider = worldIn.getBiomeProvider();              
        return biomeprovider.isFixedBiome() && !woodLandMansionSpawnBiomes.contains(biomeprovider.getFixedBiome()) ? null : findNearestStructurePosBySpacing(worldIn, this, pos, this.spacing, this.separation, 10387319, true, 100, findUnexplored);        	
        //return biomeprovider.isFixedBiome() && biomeprovider.getFixedBiome() != Biomes.ROOFED_FOREST ? null : findNearestStructurePosBySpacing(worldIn, this, pos, this.spacing, this.separation, 10387319, true, 100, findUnexplored);
    }
	
    @Override
    public String getStructureName()
    {
        return StructureNames.WOODLAND_MANSION;
    }
    
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
    	//if(this.world.getChunkProvider() instanceof ChunkProviderOverworld)
    	{
    		// TODO: This only allows WoodlandMansion in the overworld
    		return new Start(this.world, this.rand, chunkX, chunkZ); 
    	}
    	//return null;
    }
    
    public static class Start extends StructureStart
    {
        private boolean isValid;

        public Start()
        {
        }

        //public Start(World p_i47235_1_, ChunkProviderOverworld p_i47235_2_, Random p_i47235_3_, int p_i47235_4_, int p_i47235_5_)
        public Start(World p_i47235_1_, Random p_i47235_3_, int p_i47235_4_, int p_i47235_5_)
        {
            super(p_i47235_4_, p_i47235_5_);
            //this.create(p_i47235_1_, p_i47235_2_, p_i47235_3_, p_i47235_4_, p_i47235_5_);
            this.create(p_i47235_1_, p_i47235_3_, p_i47235_4_, p_i47235_5_);
        }

        
        //private void create(World p_191092_1_, ChunkProviderOverworld p_191092_2_, Random p_191092_3_, int p_191092_4_, int p_191092_5_)
        private void create(World world, Random p_191092_3_, int p_191092_4_, int p_191092_5_)
        {       	
            Rotation rotation = Rotation.values()[p_191092_3_.nextInt(Rotation.values().length)];

            TXChunkGenerator chunkProvider = ((ForgeWorld)((ForgeEngine)TerrainControl.getEngine()).getWorld(world)).getChunkGenerator();
                      
            // TODO: The vanilla spawn requirements for Mansions are checked a bit differently, make sure this implementation is adequate.
            
            int i = 5;
            int j = 5;

            if (rotation == Rotation.CLOCKWISE_90)
            {
                i = -5;
            }
            else if (rotation == Rotation.CLOCKWISE_180)
            {
                i = -5;
                j = -5;
            }
            else if (rotation == Rotation.COUNTERCLOCKWISE_90)
            {
                j = -5;
            }

            // Checking for ground at x/z 2 and 13?
            int k = chunkProvider.getHighestBlockInCurrentlyGeneratingChunk(7, 7);
            int l = chunkProvider.getHighestBlockInCurrentlyGeneratingChunk(7, 7 + j);
            int i1 = chunkProvider.getHighestBlockInCurrentlyGeneratingChunk(7 + i, 7);
            int j1 = chunkProvider.getHighestBlockInCurrentlyGeneratingChunk(7 + i, 7 + j);
            int k1 = Math.min(Math.min(k, l), Math.min(i1, j1));            
  
            if (k1 <= 0 || k1 > 255)
            {
                this.isValid = false;
            } else {
                BlockPos blockpos = new BlockPos(p_191092_4_ * 16 + 8, k1 + 1, p_191092_5_ * 16 + 8);
                List<WoodlandMansionPieces.MansionTemplate> list = Lists.<WoodlandMansionPieces.MansionTemplate>newLinkedList();
                WoodlandMansionPieces.generateMansion(world.getSaveHandler().getStructureTemplateManager(), blockpos, rotation, list, p_191092_3_);
                this.components.addAll(list);
                this.updateBoundingBox();
                this.isValid = true;
            }
        }
        
        /**
         * Keeps iterating Structure Pieces and spawning them until the checks tell it to stop
         */
        public void generateStructure(World worldIn, Random rand, StructureBoundingBox structurebb)
        {
            super.generateStructure(worldIn, rand, structurebb);
            int i = this.boundingBox.minY;

            for (int j = structurebb.minX; j <= structurebb.maxX; ++j)
            {
                for (int k = structurebb.minZ; k <= structurebb.maxZ; ++k)
                {
                    BlockPos blockpos = new BlockPos(j, i, k);

                    if (!worldIn.isAirBlock(blockpos) && this.boundingBox.isVecInside(blockpos))
                    {
                        boolean flag = false;

                        for (StructureComponent structurecomponent : this.components)
                        {
                            if (structurecomponent.getBoundingBox().isVecInside(blockpos))
                            {
                                flag = true;
                                break;
                            }
                        }

                        if (flag)
                        {
                            for (int l = i - 1; l > 1; --l)
                            {
                                BlockPos blockpos1 = new BlockPos(j, l, k);

                                if (!worldIn.isAirBlock(blockpos1) && !worldIn.getBlockState(blockpos1).getMaterial().isLiquid())
                                {
                                    break;
                                }

                                worldIn.setBlockState(blockpos1, Blocks.COBBLESTONE.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }
        }

        /**
         * currently only defined for Villages, returns true if Village has more than 2 non-road components
         */
        public boolean isSizeableStructure()
        {
            return this.isValid;
        }
    }
}