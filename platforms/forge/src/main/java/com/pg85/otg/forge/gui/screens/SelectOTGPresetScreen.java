package com.pg85.otg.forge.gui.screens;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.config.dimensions.DimensionConfig.OTGOverWorld;
import com.pg85.otg.presets.Preset;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SelectOTGPresetScreen extends Screen
{
	private static final ITextComponent SELECT_PRESET = new TranslationTextComponent("otg.createWorld.customize.preset");
	private final CreateOTGDimensionsScreen parent;
	private final ArrayList<Preset> presetList = new ArrayList<>();
	private SelectOTGPresetScreen.PresetList guiPresetList;
	private final int dimId;
	private final DimensionConfig currentSelection;
	private Preset selectedPreset;

	public SelectOTGPresetScreen(CreateOTGDimensionsScreen parent, DimensionConfig currentSelection, int dimId)
	{
		super(new TranslationTextComponent("otg.createWorld.customize.title"));
		this.parent = parent;
		ArrayList<Preset> presetList = OTG.getEngine().getPresetLoader().getAllPresets();
		this.presetList.add(0, null);
		for(Preset preset : presetList)
		{
			if(
				(dimId == 0 || preset.getFolderName() != currentSelection.Overworld.PresetFolderName) &&
				(dimId == 1 || preset.getFolderName() != currentSelection.Nether.PresetFolderName) &&
				(dimId == 2 || preset.getFolderName() != currentSelection.End.PresetFolderName)
			)
			{
				boolean bFound = false;
				int dimId2 = 3;
				for(OTGDimension otgDim : currentSelection.Dimensions)
				{
					if(!(dimId == dimId2 || preset.getFolderName() != otgDim.PresetFolderName))
					{
						bFound = true;
						break;
					}
					dimId2++;
				}
				if(!bFound)
				{
					this.presetList.add(preset);
				}
			}
		}
		this.dimId = dimId;
		this.currentSelection = currentSelection;
	}

	@Override
	public void onClose()
	{
		this.minecraft.setScreen(this.parent);
	}

	@Override
	protected void init()
	{
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.guiPresetList = new SelectOTGPresetScreen.PresetList();
		this.children.add(this.guiPresetList);
		
		this.guiPresetList.setSelected(
			this.guiPresetList.children().stream().filter(
				entry -> entry.preset != null && this.selectedPreset != null && Objects.equals(entry.preset.getFolderName(), this.selectedPreset.getFolderName())
			).findFirst().orElse(null)
		);
	}

	@Override
	public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		this.renderDirtBackground(0);
		this.guiPresetList.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, 8, 16777215);
		drawCenteredString(p_230430_1_, this.font, SELECT_PRESET, this.width / 2, 28, 10526880);
		super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	}
			
	@OnlyIn(Dist.CLIENT)
	class PresetList extends ExtendedList<SelectOTGPresetScreen.PresetList.PresetEntry>
	{
		private PresetList()
		{
			super(
				SelectOTGPresetScreen.this.minecraft,
				SelectOTGPresetScreen.this.width,
				SelectOTGPresetScreen.this.height,
				40,
				SelectOTGPresetScreen.this.height - 37,
				16
			);
			SelectOTGPresetScreen.this.presetList.forEach(preset -> this.addEntry(new PresetEntry(preset)));
		}

		@Override
		protected boolean isFocused()
		{
			return SelectOTGPresetScreen.this.getFocused() == this;
		}

		@Override
		public void setSelected(@Nullable SelectOTGPresetScreen.PresetList.PresetEntry p_241215_1_)
		{
			super.setSelected(p_241215_1_);
			if (p_241215_1_ != null)
			{
				SelectOTGPresetScreen.this.selectedPreset = p_241215_1_.preset;
				NarratorChatListener.INSTANCE.sayNow(p_241215_1_.preset.getFolderName());
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		class PresetEntry extends ExtendedList.AbstractListEntry<SelectOTGPresetScreen.PresetList.PresetEntry>
		{
			private final Preset preset;
			private final ITextComponent field_243282_c;
		
			public PresetEntry(Preset p_i232272_2_)
			{
				this.preset = p_i232272_2_;
				this.field_243282_c = new StringTextComponent(p_i232272_2_ == null ? SelectOTGPresetScreen.this.dimId == 0 ? "Non-OTG (Customize)" : SelectOTGPresetScreen.this.dimId < 3 ? "Vanilla" : "None" : p_i232272_2_.getFolderName());		
			}

			@Override
			public void render(MatrixStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_)
			{
				AbstractGui.drawString(p_230432_1_, SelectOTGPresetScreen.this.font, this.field_243282_c, p_230432_4_ + 5, p_230432_3_ + 2, 16777215);
			}

			@Override
			public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_)
			{
				if (p_231044_5_ == 0)
				{
					if(this.preset == null && SelectOTGPresetScreen.this.dimId == 0)
					{
						SelectOTGPresetScreen.this.currentSelection.Overworld = new OTGOverWorld(null, -1l, null, null);
						SelectOTGPresetScreen.this.minecraft.setScreen(OTGCreateWorldScreen.create(SelectOTGPresetScreen.this.parent, SelectOTGPresetScreen.this.currentSelection));
					} else {
						OTGDimension otgDim = new OTGDimension(this.preset == null ? null : this.preset.getFolderName(), -1l);
						switch(SelectOTGPresetScreen.this.dimId)
						{
							case 0:
								SelectOTGPresetScreen.this.currentSelection.Overworld = new OTGOverWorld(this.preset == null ? null : this.preset.getFolderName(), -1l, null, null);
								break;
							case 1:
								SelectOTGPresetScreen.this.currentSelection.Nether = otgDim;
								break;							
							case 2:
								SelectOTGPresetScreen.this.currentSelection.End = otgDim;
								break;
							default:
								SelectOTGPresetScreen.this.currentSelection.Dimensions.set(SelectOTGPresetScreen.this.dimId - 3, otgDim);
								break;
						}	
						SelectOTGPresetScreen.this.minecraft.setScreen(SelectOTGPresetScreen.this.parent);
					}
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
