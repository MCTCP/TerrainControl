package com.pg85.otg.gen.surface;

import java.util.Arrays;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.gen.noise.legacy.NoiseGeneratorPerlinMesaBlocks;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

class MesaSurfaceGenerator implements SurfaceGenerator
{
    private LocalMaterialData[] clayBands;
    private long worldSeed;
    private boolean hasForest;
    private boolean brycePillars;
    private NoiseGeneratorPerlinMesaBlocks pillarNoise;
    private NoiseGeneratorPerlinMesaBlocks pillarRoofNoise;
    private NoiseGeneratorPerlinMesaBlocks clayBandsOffsetNoise;
    
    private LocalMaterialData hardClay = LocalMaterials.TERRACOTTA;
    private LocalMaterialData orangeClay = LocalMaterials.ORANGE_TERRACOTTA;
    private LocalMaterialData yellowClay = LocalMaterials.YELLOW_TERRACOTTA;
    private LocalMaterialData brownClay = LocalMaterials.BROWN_TERRACOTTA;
    private LocalMaterialData redClay = LocalMaterials.RED_TERRACOTTA;
    private LocalMaterialData whiteClay = LocalMaterials.WHITE_TERRACOTTA;
    private LocalMaterialData silverClay = LocalMaterials.SILVER_TERRACOTTA;
    private LocalMaterialData redSand = LocalMaterials.RED_SAND;
    
    private boolean initialized;
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
        if (Constants.MESA_NAME_NORMAL.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(false, false);
        }
        if (Constants.MESA_NAME_FOREST.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(false, true);
        }
        if (Constants.MESA_NAME_BRYCE.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(true, false);
        }
        return null;
    }

    private void Init(ReplacedBlocksMatrix replacedBlocks)
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
    private LocalMaterialData getBand(IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
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
    		return !this.hardClayIsReplaced ? this.hardClay : this.hardClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
    	}
    	else if(material == this.orangeClay)
    	{
    		return !this.orangeClayIsReplaced ? this.orangeClay : this.orangeClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
    	}
    	else if(material == this.yellowClay)
    	{
    		return !this.yellowClayIsReplaced ? this.yellowClay : this.yellowClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
    	}
    	else if(material == this.brownClay)
    	{
    		return !this.brownClayIsReplaced ? this.brownClay : this.brownClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
    	}
    	else if(material == this.redClay)
    	{
    		return !this.redClayIsReplaced ? this.redClay : this.redClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
    	}
    	else if(material == this.whiteClay)
    	{
    		return !this.whiteClayIsReplaced ? this.whiteClay : this.whiteClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
    	}
    	else if(material == this.silverClay)
    	{
    		return !this.silverClayIsReplaced ? this.silverClay : this.silverClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
    	}
    	else if(material == this.redSand)
    	{
    		return !this.redSandIsReplaced ? this.redSand : this.redSand.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
    	}
    	return null;
    }

    @Override
    public LocalMaterialData getSurfaceBlockAtHeight(IWorldGenRegion worldGenRegion, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return getBand(biomeConfig, xInWorld, yInWorld, zInWorld);
    }
    
    @Override
    public LocalMaterialData getGroundBlockAtHeight(IWorldGenRegion worldGenRegion, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return getBand(biomeConfig, xInWorld, yInWorld, zInWorld);
    }
    
    // net.minecraft.world.biome.BiomeMesa.generateBands
    private void generateBands(long seed)
    {
        this.clayBands = new LocalMaterialData[64];
        Arrays.fill(this.clayBands, this.hardClay);
        Random random = new Random(seed);

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

    private boolean clayBandsGenerated = false;
    // net.minecraft.world.biome.BiomeMesa.genTerrainBlocks
    @Override
    public void spawn(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiomeConfig biomeConfig, int xInWorld, int zInWorld)
    {    	    	
        if (this.clayBands == null || !this.clayBandsGenerated)
        {
            this.generateBands(worldSeed);
        }

    	// TODO: BiomeConfig should always be the same, this layer should only be used in a single biome,
    	// Make this prettier?
    	Init(biomeConfig.getReplaceBlocks());        
        
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
            int localX = (xInWorld & -16) + (zInWorld & 15);
            int localZ = (zInWorld & -16) + (xInWorld & 15);
            double bryceNoiseValue = Math.min(Math.abs(noise), this.pillarNoise.getValue((double) localX * 0.25D, (double) localZ * 0.25D));

            if (bryceNoiseValue > 0.0D)
            {
                double maxHeightScale = 0.001953125D;
                double maxHeightNoise = Math.abs(this.pillarRoofNoise.getValue((double) localX * maxHeightScale, (double) localZ * maxHeightScale));
                bryceHeight = bryceNoiseValue * bryceNoiseValue * 2.5D;
                double maxHeight = Math.ceil(maxHeightNoise * 50.0D) + 14.0D;

                if (bryceHeight > maxHeight)
                {
                    bryceHeight = maxHeight;
                }

                bryceHeight += 64.0D;
            }
        }
        
        int waterLevel = generatingChunk.getWaterLevel(x, z);
        
        boolean useDefaultGroundBlock = true;
        LocalMaterialData currentGroundBlock = null;
        boolean groundIsStainedClay = true;
        
        int noisePlusRandomFactor = (int) (noise / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);
        
        int groundLayerDepth = -1;
        boolean belowSand = false;
    	boolean useGroundBlockGround = false;
    	boolean useGroundBlockStone = false;
        int generatedDepth = 0;

        // Bedrock on the ceiling
        if (biomeConfig.isCeilingBedrock())
        {
            // Moved one block lower to fix lighting issues
            chunkBuffer.setBlock(x, generatingChunk.heightCap - 2, z, biomeConfig.getBedrockBlockReplaced(generatingChunk.heightCap - 2));
        }
        
        int highestBlockInColumn = chunkBuffer.getHighestBlockForColumn(x, z);
        int maxHeight = Math.max(highestBlockInColumn, (int) bryceHeight);

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
    			(generatedDepth < 15 || this.brycePillars)
			)
        	{
        		worldMaterial = chunkBuffer.getBlock(x, y, z);
        	}

            if (y < (int) bryceHeight && worldMaterial.isAir())
            {
                chunkBuffer.setBlock(x, y, z, getBand(biomeConfig, x, y, z));
            }

            if (generatingChunk.mustCreateBedrockAt(biomeConfig.isFlatBedrock(), biomeConfig.isBedrockDisabled(), biomeConfig.isCeilingBedrock(), y))
            {
                chunkBuffer.setBlock(x, y, z, biomeConfig.getBedrockBlockReplaced(y));
            }
            else if (generatedDepth < 15 || this.brycePillars)
            {
            	if(worldMaterial.isEmptyOrAir())
                {
                    groundLayerDepth = -1;
                }

                // The water block is much less likely to be replaced so lookups should be quicker,
                // do a != waterblockreplaced rather than an == stoneblockreplaced. Since we know
                // there can be only air, water and stone in the chunk atm (unless some other mod
                // did funky magic, which might cause problems).
            	// TODO: This'll cause issues with surfaceandgroundcontrol if users configure the 
            	// same biome water block as surface/ground/stone block.
                // TODO: If other mods have problems bc of replacedblocks in the chunk during ReplaceBiomeBlocks, 
                // do replaceblock for stone/water here instead of when initially filling the chunk.            	
                else if(!worldMaterial.equals(biomeConfig.getWaterBlockReplaced(y)))
                {
                    if (groundLayerDepth == -1)
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
                        
                        groundLayerDepth = noisePlusRandomFactor + Math.max(0, y - waterLevel);
                        if (y >= waterLevel - 1)
                        {
                            if (this.hasForest && y > 86 + noisePlusRandomFactor * 2)
                            {
                                chunkBuffer.setBlock(x, y, z, biomeConfig.getSurfaceBlockReplaced(y));
                            }
                            else if (y > waterLevel + 3 + noisePlusRandomFactor)
                            {
                                if (y >= 64 && y <= 127)
                                {
                                	worldMaterial = this.getBand(biomeConfig, xInWorld, y, zInWorld);
                                } else {
                                	worldMaterial = !this.orangeClayIsReplaced ? this.orangeClay : this.orangeClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), y);
                                }

                                chunkBuffer.setBlock(x, y, z, worldMaterial);
                            } else {
                                chunkBuffer.setBlock(x, y, z, !this.redSandIsReplaced ? this.redSand : this.redSand.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), y));
                                belowSand = true;
                            }
                        } else {                        	
                            if (useGroundBlockGround)
                            {
                            	// block should already be the replaced stoneblock
                                ++generatedDepth;
                            	continue;
                            }
                            else if (useGroundBlockStone)
                            {
                                currentGroundBlock = biomeConfig.getGroundBlockReplaced(y);
                            }                        	
                            else if(useDefaultGroundBlock)
                        	{
                        		currentGroundBlock = !this.whiteClayIsReplaced ? this.whiteClay : this.whiteClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), y);
                        	}
                            
                            if (groundIsStainedClay)
                            {
                                chunkBuffer.setBlock(x, y, z, !this.orangeClayIsReplaced ? this.orangeClay : this.orangeClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), y));
                            } else {
                            	chunkBuffer.setBlock(x, y, z, currentGroundBlock);
                            }
                        }
                    }
                    else if (groundLayerDepth > 0)
                    {
                        --groundLayerDepth;
                        if (belowSand)
                        {
                            chunkBuffer.setBlock(x, y, z, !this.orangeClayIsReplaced ? this.orangeClay : this.orangeClay.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), y));
                        } else {
                        	worldMaterial = this.getBand(biomeConfig, xInWorld, y, zInWorld);
                            chunkBuffer.setBlock(x, y, z, worldMaterial);
                        }
                    }
                    ++generatedDepth;
                }
            }
        }
    }

    @Override
    public String toString()
    {
        if (this.hasForest)
        {
            return Constants.MESA_NAME_FOREST;
        }
        if (this.brycePillars)
        {
            return Constants.MESA_NAME_BRYCE;
        }
        return Constants.MESA_NAME_NORMAL;
    }
}
