package com.pg85.otg.generator.surface;

import java.util.Arrays;
import java.util.Random;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.settings.ReplacedBlocksMatrix;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.GeneratingChunk;
import com.pg85.otg.generator.noise.NoiseGeneratorPerlinMesaBlocks;
import com.pg85.otg.util.materials.MaterialHelper;

public class MesaSurfaceGenerator implements SurfaceGenerator
{
    public static final String NAME_NORMAL = "Mesa";
    public static final String NAME_FOREST = "MesaForest";
    public static final String NAME_BRYCE = "MesaBryce";

    private LocalMaterialData[] clayBands;
    private long worldSeed;
    private boolean hasForest;
    private boolean brycePillars;
    private NoiseGeneratorPerlinMesaBlocks pillarNoise;
    private NoiseGeneratorPerlinMesaBlocks pillarRoofNoise;
    private NoiseGeneratorPerlinMesaBlocks clayBandsOffsetNoise;
    
    private LocalMaterialData hardClay = MaterialHelper.HARDENED_CLAY;
    private LocalMaterialData orangeClay = MaterialHelper.ORANGE_STAINED_CLAY;
    private LocalMaterialData yellowClay = MaterialHelper.YELLOW_STAINED_CLAY;
    private LocalMaterialData brownClay = MaterialHelper.BROWN_STAINED_CLAY;
    private LocalMaterialData redClay = MaterialHelper.RED_STAINED_CLAY;
    private LocalMaterialData whiteClay = MaterialHelper.WHITE_STAINED_CLAY;
    private LocalMaterialData silverClay = MaterialHelper.SILVER_STAINED_CLAY;
    private LocalMaterialData redSand = MaterialHelper.RED_SAND;
    
    boolean initialized;
	private boolean hardClayIsReplaced;
	private boolean orangeClayIsReplaced;
	private boolean yellowClayIsReplaced;
	private boolean brownClayIsReplaced;
	private boolean redClayIsReplaced;
	private boolean whiteClayIsReplaced;
	private boolean silverClayIsReplaced;
	private boolean redSandIsReplaced;
    
    private MesaSurfaceGenerator(boolean mountainMesa, boolean forestMesa)
    {
        this.brycePillars = mountainMesa;
        this.hasForest = forestMesa;
    }

    /**
     * Returns the mesa surface generator representing the setting value. If
     * no mes surface generator represents this setting value, null is
     * returned.
     * 
     * @param settingValue The setting value.
     * @return The mesa surface generator, or null if not found.
     */
    public static MesaSurfaceGenerator getFor(String settingValue)
    {
        if (NAME_NORMAL.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(false, false);
        }
        if (NAME_FOREST.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(false, true);
        }
        if (NAME_BRYCE.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(true, false);
        }
        return null;
    }

    public void Init(ReplacedBlocksMatrix replacedBlocks)
    {
    	if(!this.initialized)
    	{
    		this.initialized = true;
    		this.hardClayIsReplaced = replacedBlocks.replacesBlock(this.hardClay);
    		this.orangeClayIsReplaced = replacedBlocks.replacesBlock(this.orangeClay);
    		this.yellowClayIsReplaced = replacedBlocks.replacesBlock(this.yellowClay);
    		this.brownClayIsReplaced = replacedBlocks.replacesBlock(this.brownClay);
    		this.redClayIsReplaced = replacedBlocks.replacesBlock(this.redClay);
    		this.whiteClayIsReplaced = replacedBlocks.replacesBlock(this.whiteClay);
    		this.silverClayIsReplaced = replacedBlocks.replacesBlock(this.silverClay);
    		this.redSandIsReplaced = replacedBlocks.replacesBlock(this.redSand);
    	}
    }
    
    private int lastX = Integer.MAX_VALUE;
    private int lastZ = Integer.MAX_VALUE;
    private int lastNoise = 0;
    private LocalMaterialData getBand(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	int noise = this.lastNoise;
    	if(this.lastX != xInWorld || this.lastZ != zInWorld)
    	{
    		noise = (int) Math.round(this.clayBandsOffsetNoise.getValue((double) xInWorld / 512.0D, (double) zInWorld / 512.0D) * 2.0D);
    		this.lastX = xInWorld;
    		this.lastZ = zInWorld;
    		this.lastNoise = noise;
    	}
    	//int l = 0; // TODO: Fix the mesa noise pattern (it's broken for vanilla too).
    	
    	// TODO: BiomeConfig should always be the same, this layer should only be used in a single biome,
    	// Make this prettier?
    	LocalMaterialData material = this.clayBands[(yInWorld + noise + 64) % 64];
    	if(material == this.hardClay)
    	{
    		return !this.hardClayIsReplaced ? this.hardClay : this.hardClay.parseWithBiomeAndHeight(world, biomeConfig, yInWorld);
    	}
    	else if(material == this.orangeClay)
    	{
    		return !this.orangeClayIsReplaced ? this.orangeClay : this.orangeClay.parseWithBiomeAndHeight(world, biomeConfig, yInWorld);
    	}
    	else if(material == this.yellowClay)
    	{
    		return !this.yellowClayIsReplaced ? this.yellowClay : this.yellowClay.parseWithBiomeAndHeight(world, biomeConfig, yInWorld);
    	}
    	else if(material == this.brownClay)
    	{
    		return !this.brownClayIsReplaced ? this.brownClay : this.brownClay.parseWithBiomeAndHeight(world, biomeConfig, yInWorld);
    	}
    	else if(material == this.redClay)
    	{
    		return !this.redClayIsReplaced ? this.redClay : this.redClay.parseWithBiomeAndHeight(world, biomeConfig, yInWorld);
    	}
    	else if(material == this.whiteClay)
    	{
    		return !this.whiteClayIsReplaced ? this.whiteClay : this.whiteClay.parseWithBiomeAndHeight(world, biomeConfig, yInWorld);
    	}
    	else if(material == this.silverClay)
    	{
    		return !this.silverClayIsReplaced ? this.silverClay : this.silverClay.parseWithBiomeAndHeight(world, biomeConfig, yInWorld);
    	}
    	else if(material == this.redSand)
    	{
    		return !this.redSandIsReplaced ? this.redSand : this.redSand.parseWithBiomeAndHeight(world, biomeConfig, yInWorld);
    	}
    	return null;
    }

    @Override
    public LocalMaterialData getSurfaceBlockAtHeight(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return getBand(world, biomeConfig, xInWorld, yInWorld, zInWorld);
    }
    
    @Override
    public LocalMaterialData getGroundBlockAtHeight(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return getBand(world, biomeConfig, xInWorld, yInWorld, zInWorld);
    }
    
    // net.minecraft.world.biome.BiomeMesa.generateBands
    private void generateBands(long p_150619_1_)
    {
        this.clayBands = new LocalMaterialData[64];
        Arrays.fill(this.clayBands, this.hardClay);
        Random random = new Random(p_150619_1_);

        this.clayBandsOffsetNoise = new NoiseGeneratorPerlinMesaBlocks(random, 1);

        for (int l1 = 0; l1 < 64; ++l1)
        {
            l1 += random.nextInt(5) + 1;

            if (l1 < 64)
            {
                this.clayBands[l1] = orangeClay;
            }
        }

        int i2 = random.nextInt(4) + 2;

        for (int i = 0; i < i2; ++i)
        {
            int j = random.nextInt(3) + 1;
            int k = random.nextInt(64);

            for (int l = 0; k + l < 64 && l < j; ++l)
            {
                this.clayBands[k + l] = yellowClay;
            }
        }

        int j2 = random.nextInt(4) + 2;

        for (int k2 = 0; k2 < j2; ++k2)
        {
            int i3 = random.nextInt(3) + 2;
            int l3 = random.nextInt(64);

            for (int i1 = 0; l3 + i1 < 64 && i1 < i3; ++i1)
            {
                this.clayBands[l3 + i1] = brownClay;
            }
        }

        int l2 = random.nextInt(4) + 2;

        for (int j3 = 0; j3 < l2; ++j3)
        {
            int i4 = random.nextInt(3) + 1;
            int k4 = random.nextInt(64);

            for (int j1 = 0; k4 + j1 < 64 && j1 < i4; ++j1)
            {
                this.clayBands[k4 + j1] = redClay;
            }
        }

        int k3 = random.nextInt(3) + 3;
        int j4 = 0;

        for (int l4 = 0; l4 < k3; ++l4)
        {
            j4 += random.nextInt(16) + 4;

            for (int k1 = 0; j4 + k1 < 64 && k1 < 1; ++k1)
            {
                this.clayBands[j4 + k1] = whiteClay;

                if (j4 + k1 > 1 && random.nextBoolean())
                {
                    this.clayBands[j4 + k1 - 1] = silverClay;
                }

                if (j4 + k1 < 63 && random.nextBoolean())
                {
                    this.clayBands[j4 + k1 + 1] = silverClay;
                }
            }
        }
    }

    boolean clayBandsGenerated = false;
    // net.minecraft.world.biome.BiomeMesa.genTerrainBlocks
    @Override
    public void spawn(LocalWorld world, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int xInWorld, int zInWorld)
    {    	    	
        long worldSeed = world.getSeed();
        if (this.clayBands == null || !this.clayBandsGenerated)
        {
            this.generateBands(worldSeed);
        }

    	// TODO: BiomeConfig should always be the same, this layer should only be used in a single biome,
    	// Make this prettier?
    	Init(biomeConfig.replacedBlocks);        
        
        if (this.pillarNoise == null || this.pillarRoofNoise == null || !this.clayBandsGenerated)
        {
            Random random = new Random(this.worldSeed);
            this.pillarNoise = new NoiseGeneratorPerlinMesaBlocks(random, 4);
            this.pillarRoofNoise = new NoiseGeneratorPerlinMesaBlocks(random, 1);
        }
        
        this.clayBandsGenerated = true;
        
        int x = xInWorld & 15;
        int z = zInWorld & 15;
        double noise = generatingChunk.getNoise(x, z);
        
    	this.worldSeed = worldSeed;
        // Bryce spike calculations
        double bryceHeight = 0.0D;
        if (this.brycePillars)
        {
            int k = (xInWorld & -16) + (zInWorld & 15);
            int l = (zInWorld & -16) + (xInWorld & 15);
            double bryceNoiseValue = Math.min(Math.abs(noise), this.pillarNoise.getValue((double)k * 0.25D, (double)l * 0.25D));

            if (bryceNoiseValue > 0.0D)
            {
                double d3 = 0.001953125D;
                double d4 = Math.abs(this.pillarRoofNoise.getValue((double)k * d3, (double)l * d3));
                bryceHeight = bryceNoiseValue * bryceNoiseValue * 2.5D;
                double d5 = Math.ceil(d4 * 50.0D) + 14.0D;

                if (bryceHeight > d5)
                {
                    bryceHeight = d5;
                }

                bryceHeight += 64.0D;
            }
        }
        
        int waterLevel = generatingChunk.getWaterLevel(x, z);
        
        boolean useDefaultGroundBlock = true;
        LocalMaterialData currentGroundBlock = null;
        boolean groundIsStainedClay = true;
        
        int noisePlusRandomFactor = (int) (noise / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);
        
        int k1 = -1;
        boolean belowSand = false;
    	boolean useGroundBlockGround = false;
    	boolean useGroundBlockStone = false;
        int i1 = 0;

        // Bedrock on the ceiling
        if (biomeConfig.worldConfig.ceilingBedrock)
        {
            // Moved one block lower to fix lighting issues
            chunkBuffer.setBlock(x, generatingChunk.heightCap - 2, z, biomeConfig.worldConfig.getBedrockBlockReplaced(world, biomeConfig, generatingChunk.heightCap - 2));
        }
        
        int highestBlockInColumn = chunkBuffer.getHighestBlockForColumn(x, z);
        int maxHeight = highestBlockInColumn < (int)bryceHeight ? (int)bryceHeight : highestBlockInColumn;
        if(maxHeight < bryceHeight)
        {
        	maxHeight = (int)bryceHeight;
        }
        
        int minHeight = 0;
        LocalMaterialData worldMaterial = null;
        
        for (int y = maxHeight; y >= minHeight; y--)
        {
        	if (
    			(y < (int) bryceHeight) ||
    			(i1 < 15 || this.brycePillars)
			)
        	{
        		worldMaterial = chunkBuffer.getBlock(x, y, z);
        	}

            if (y < (int) bryceHeight && worldMaterial.isAir())
            {
                chunkBuffer.setBlock(x, y, z, biomeConfig.getStoneBlockReplaced(world, y));
            }

            if (generatingChunk.mustCreateBedrockAt(biomeConfig.worldConfig, y))
            {
                chunkBuffer.setBlock(x, y, z, biomeConfig.worldConfig.getBedrockBlockReplaced(world, biomeConfig, y));
            }
            else if (i1 < 15 || this.brycePillars)
            {
            	if(worldMaterial.isEmptyOrAir())
                {
                    k1 = -1;
                }
                // The water block is much less likely to be replaced so lookups should be quicker,
                // do a != waterblockreplaced rather than an == stoneblockreplaced. Since we know
                // there can be only air, water and stone in the chunk atm (unless some other mod
                // did funky magic, which might cause problems).
            	// TODO: This'll cause issues with surfaceandgroundcontrol if users configure the 
            	// same biome water block as surface/ground/stone block.
                // TODO: If other mods have problems bc of replacedblocks in the chunk during ReplaceBiomeBlocks, 
                // do replaceblock for stone/water here instead of when initially filling the chunk.            	
                else if(!worldMaterial.equals(biomeConfig.getWaterBlockReplaced(world, y)))
                {
                    if (k1 == -1)
                    {
                        belowSand = false;

                        if (noisePlusRandomFactor <= 0)
                        {
                        	useDefaultGroundBlock = false;
                        	useGroundBlockGround = false;
                        	useGroundBlockStone = true;
                        }
                        else if (y >= waterLevel - 4 && y <= waterLevel + 1)
                        {
                        	useDefaultGroundBlock = false;
                        	useGroundBlockGround = true;
                        	useGroundBlockStone = false;
                        }
                        
                        k1 = noisePlusRandomFactor + Math.max(0, y - waterLevel);
                        if (y >= waterLevel - 1)
                        {
                            if (this.hasForest && y > 86 + noisePlusRandomFactor * 2)
                            {
                                chunkBuffer.setBlock(x, y, z, biomeConfig.getSurfaceBlockReplaced(world, y));
                            }
                            else if (y > waterLevel + 3 + noisePlusRandomFactor)
                            {
                                if (y >= 64 && y <= 127)
                                {
                                	worldMaterial = this.getBand(world, biomeConfig, xInWorld, y, zInWorld);
                                } else {
                                	worldMaterial = !this.orangeClayIsReplaced ? this.orangeClay : this.orangeClay.parseWithBiomeAndHeight(world, biomeConfig, y);
                                }

                                chunkBuffer.setBlock(x, y, z, worldMaterial);
                            } else {
                                chunkBuffer.setBlock(x, y, z, !this.redSandIsReplaced ? this.redSand : this.redSand.parseWithBiomeAndHeight(world, biomeConfig, y));
                                belowSand = true;
                            }
                        } else {                        	
                            if (useGroundBlockGround)
                            {
                            	// block should already be the replaced stoneblock
                                ++i1;
                            	continue;
                            }
                            else if (useGroundBlockStone)
                            {
                                currentGroundBlock = biomeConfig.getGroundBlockReplaced(world, y);
                            }                        	
                            else if(useDefaultGroundBlock)
                        	{
                        		currentGroundBlock = !this.whiteClayIsReplaced ? this.whiteClay : this.whiteClay.parseWithBiomeAndHeight(world, biomeConfig, y);
                        	}
                            
                            if (groundIsStainedClay)
                            {
                                chunkBuffer.setBlock(x, y, z, !this.orangeClayIsReplaced ? this.orangeClay : this.orangeClay.parseWithBiomeAndHeight(world, biomeConfig, y));
                            } else {
                            	chunkBuffer.setBlock(x, y, z, currentGroundBlock);
                            }
                        }
                    }
                    else if (k1 > 0)
                    {
                        --k1;
                        if (belowSand)
                        {
                            chunkBuffer.setBlock(x, y, z, !this.orangeClayIsReplaced ? this.orangeClay : this.orangeClay.parseWithBiomeAndHeight(world, biomeConfig, y));
                        } else {
                        	worldMaterial = this.getBand(world, biomeConfig, xInWorld, y, zInWorld);
                            chunkBuffer.setBlock(x, y, z, worldMaterial);
                        }
                    }
                    ++i1;
                }
            }
        }
    }

    @Override
    public String toString()
    {
        if (this.hasForest)
        {
            return NAME_FOREST;
        }
        if (this.brycePillars)
        {
            return NAME_BRYCE;
        }
        return NAME_NORMAL;
    }
}
