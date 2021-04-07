package com.pg85.otg.forge.materials;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.materials.LegacyMaterials;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.material.Material;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;

import com.pg85.otg.util.materials.MaterialProperty;
import com.pg85.otg.util.materials.MaterialProperties;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 *
 */
public class ForgeMaterialData extends LocalMaterialData
{
	private BlockState blockData;

	private ForgeMaterialData(BlockState blockData)
	{
		this.blockData = blockData;
	}
	
	private ForgeMaterialData(BlockState blockData, String raw)
	{
		this.blockData = blockData;
		this.rawEntry = raw;
	}
	
	private ForgeMaterialData(String raw)
	{
		this.blockData = null;
		this.rawEntry = raw;
	}
	
	private static ForgeMaterialData getBlank()
	{
		// TODO: this null should probably be replaced with air
		ForgeMaterialData material = new ForgeMaterialData((BlockState)null, null);
		material.isBlank = true;
		return material;
	}

	public static ForgeMaterialData ofString(String input) throws InvalidConfigException
	{
		if(input == null || input.trim().isEmpty())
		{
			return null;
		}
		
		// Try parsing as an internal Minecraft name
		// This is so that things like "minecraft:stone" aren't parsed
		// as the block "minecraft" with data "stone", but instead as the
		// block "minecraft:stone" with no block data.
		
		// Used in BO4's as placeholder/detector block.
		if(input.toLowerCase().equals("blank"))
		{
			return ForgeMaterialData.getBlank();
		}

		BlockState blockState = null;
		String blockNameCorrected = input.trim().toLowerCase();
		// Try parsing as legacy block name / id
		if(!blockNameCorrected.contains(":"))
		{
			blockState = ForgeLegacyMaterials.fromLegacyBlockName(blockNameCorrected);
			if(blockState != null)
			{
				return ofMinecraftBlockState(blockState, input);
			}
			try
			{
				// Deal with pesky accidental floats that parseInt won't recognize
				if (blockNameCorrected.endsWith(".0"))
				{
					blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.length()-2);
				}
				int blockId = Integer.parseInt(blockNameCorrected);
				String fromLegacyIdName = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
				if(fromLegacyIdName != null)
				{
					blockNameCorrected = fromLegacyIdName;
				}
			} catch(NumberFormatException ex) { }
		}
		
		// Try blockname[blockdata] / minecraft:blockname[blockdata] syntax
		
		// Use mc /setblock command logic to parse block string for us <3
		BlockState state = null;
		try {
			String newInput = blockNameCorrected.contains(":") ? blockNameCorrected : "minecraft:" + blockNameCorrected;
			state = new BlockStateParser(new StringReader(newInput), true).parse(true).getState();
		} catch (CommandSyntaxException ignored) { }
		if(state != null)
		{
			// For leaves, add DISTANCE 1 to make them not decay.
			if(state.getMaterial().equals(Material.LEAVES))
			{
				return new ForgeMaterialData(state.with(LeavesBlock.DISTANCE, 1), input);
			}
			return new ForgeMaterialData(state, input);
		}
		
		// Try legacy block with data (fe SAND:1 or 12:1)
		if(blockNameCorrected.contains(":"))
		{
			// Try parsing data argument as int.
			String blockNameOrId = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));
			try
			{
				int blockId = Integer.parseInt(blockNameOrId);
				blockNameOrId = LegacyMaterials.blockNameFromLegacyBlockId(blockId);
			} catch(NumberFormatException ex) { }			
			try
			{
				int data = Integer.parseInt(blockNameCorrected.substring(blockNameCorrected.indexOf(":") + 1));
				blockState = ForgeLegacyMaterials.fromLegacyBlockNameOrIdWithData(blockNameOrId, data);
				if(blockState != null)
				{
					return ofMinecraftBlockState(blockState, input);
				}
				// Failed to parse data, remove. fe STONE:0 or STONE:1 -> STONE
				blockNameCorrected = blockNameCorrected.substring(0, blockNameCorrected.indexOf(":"));				
			} catch(NumberFormatException ex) { }			
		}

		// Try without data
		Block block = null;
		try
		{
			// This returns AIR if block is not found ><.
			block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockNameCorrected));
			if(block != null && (block != Blocks.AIR || blockNameCorrected.toLowerCase().endsWith("air")))
			{
				// For leaves, add DISTANCE 1 to make them not decay.
				if(block.getDefaultState().getMaterial().equals(Material.LEAVES))
				{
					return new ForgeMaterialData(block.getDefaultState().with(LeavesBlock.DISTANCE, 1), input);	
				}
				return ofMinecraftBlock(block, input);
			}
		} catch(net.minecraft.util.ResourceLocationException ex) { }

		// Try legacy name again, without data.
		blockState = ForgeLegacyMaterials.fromLegacyBlockName(blockNameCorrected.replace("minecraft:", ""));
		if(blockState != null)
		{
			return ofMinecraftBlockState(blockState, input);
		}
		
		OTG.log(LogMarker.INFO, "Could not parse block: " + input + ", substituting AIR.");
		
		return ofMinecraftBlock(Blocks.AIR, input);
	}
		
	/**
	 * Gets a {@code BukkitMaterialData} of the given Minecraft block. The
	 * default block data (usually 0) will be used.
	 * @param block The material.
	 * @return The {@code BukkitMateialData} instance.
	 */
	private static ForgeMaterialData ofMinecraftBlock(Block block, String raw)
	{
		return ofMinecraftBlockState(block.getDefaultState(), raw);
	}

	/**
	 * Gets a {@code ForgeMaterialData} of the given Minecraft blockData.
	 * @param blockData The material an data.
	 * @return The {@code BukkitMateialData} instance.
	 */
	public static ForgeMaterialData ofMinecraftBlockState(BlockState blockData)
	{
		return new ForgeMaterialData(blockData, null);
	}
	
	/**
	 * Gets a {@code ForgeMaterialData} of the given Minecraft blockData.
	 * @param blockData The material an data.
	 * @return The {@code BukkitMateialData} instance.
	 */
	private static ForgeMaterialData ofMinecraftBlockState(BlockState blockData, String raw)
	{
		return new ForgeMaterialData(blockData, raw);
	}	

	@Override
	public LocalMaterialData withDefaultBlockData()
	{
		if(this.blockData == null)
		{
			return this;
		}
		Block block = this.blockData.getBlock();
		return ofMinecraftBlock(block, this.rawEntry);
	}
	
	@Override
	public String getName()
	{
		if(isBlank)
		{
			return "BLANK";
		}
		else if(this.blockData == null)
		{
			if(this.rawEntry != null)
			{
				return this.rawEntry;
			} else {
				return "Unknown";
			}
		} else {
			return this.blockData.toString().replace("Block{", "").replace("}", "");
			//Block block = this.blockData.getBlock();
			//byte data = getBlockData();
			//boolean noData = this.blockData.getPropertyKeys().isEmpty();
			// Note that the above line is not equivalent to data != 0, as for
			// example pumpkins have a default data value of 2
	
			//boolean nonDefaultData = !block.getDefaultState().equals(this.blockData);
			
			// Use Minecraft's name
			//if (nonDefaultData)
			//{
				//return Block.REGISTRY.getNameForObject(block) + (noData ? "" : ":" + data);
			//} else {
				//return block.getRegistryName().toString();
			//}
		}
	}
	
	public BlockState internalBlock()
	{
		return this.blockData;
	}

	@Override
	public boolean isMaterial(LocalMaterialData material)
	{
		// TODO: Compare registry names?
		return 
			(this.isBlank && ((ForgeMaterialData)material).isBlank) || 
			(
				!this.isBlank && 
				!((ForgeMaterialData)material).isBlank &&
				Objects.equals(this.blockData.getBlock().getRegistryName(), ((ForgeMaterialData) material).internalBlock().getBlock().getRegistryName())
			)
		;
	}
	
	@Override
	public boolean isLiquid()
	{
		return this.blockData != null && this.blockData.getMaterial().isLiquid();
	}

	@Override
	public boolean isSolid()
	{		
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}
	
	@Override
	public boolean isEmptyOrAir()
	{
		return this.blockData == null || this.blockData.isAir();
	}
	
	@Override
	public boolean isAir()
	{
		return this.blockData != null && this.blockData.isAir();
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.blockData == null;
	}

	@Override
	public boolean canFall()
	{
		return this.blockData != null && this.blockData.getBlock() instanceof FallingBlock;
	}

	@Override
	public boolean canSnowFallOn()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}
	
	@Override
	public boolean hasData()
	{
		// TODO: Implement this for 1.16		
		return false;
	}

	@Override
	public <T extends Comparable<T>> LocalMaterialData withProperty(MaterialProperty<T> materialProperty, T value)
	{
		Property<T> property = null;

		// TODO: This is really bad. We need a way to append properties onto the MaterialProperty
		if (materialProperty == MaterialProperties.AGE_0_25) {
			property = (Property<T>) BlockStateProperties.AGE_0_25;
		} else if (materialProperty == MaterialProperties.PICKLES_1_4) {
			property = (Property<T>) BlockStateProperties.PICKLES_1_4;
		} else {
			throw new IllegalArgumentException("Bad property: " + materialProperty);
		}

		return ForgeMaterialData.ofMinecraftBlockState(this.blockData.with(property, value));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof ForgeMaterialData))
		{
			return false;
		}
		ForgeMaterialData other = (ForgeMaterialData) obj;
		
		// TODO: Compare registry names?
		return 
			(this.isBlank && other.isBlank) || 
			(
				!this.isBlank && 
				!other.isBlank &&
				this.blockData.getBlock().equals(other.internalBlock().getBlock())
			)
		;
	}
	
	/**
	 * Gets the hashCode of the material, based on the block id and block data.
	 * The hashCode must be unique, which is possible considering that there are
	 * only 4096 * 16 possible materials.
	 * 
	 * @return The unique hashCode.
	 */
	@Override
	public int hashCode()
	{
		// TODO: Implement this for 1.16
		return this.blockData == null ? -1 : this.blockData.hashCode();
	}
}
