package com.pg85.otg.forge.gui.screens;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.presets.Preset;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateOTGWorldScreen extends Screen
{
	private static final ITextComponent SELECT_PRESET = new TranslationTextComponent("otg.createWorld.customize.preset");
	private final Screen parent;
	private final Consumer<DimensionConfig> dimensionConfig;
	private final ArrayList<Preset> presetList;
	private CreateOTGWorldScreen.PresetList guiPresetList;
	private DimensionConfig selectedPreset;
	private Button btnClose;

	public CreateOTGWorldScreen(Screen p_i242054_1_, DynamicRegistries p_i242054_2_, Consumer<DimensionConfig> dimensionConfig)
	{
		super(new TranslationTextComponent("otg.createWorld.customize.title"));
		this.parent = p_i242054_1_;
		this.dimensionConfig = dimensionConfig;
		this.presetList = OTG.getEngine().getPresetLoader().getAllPresets();
		this.selectedPreset = new DimensionConfig(this.presetList.get(0).getFolderName());
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
		this.guiPresetList = new CreateOTGWorldScreen.PresetList();
		this.children.add(this.guiPresetList);
		this.btnClose = this.addButton(
			new Button(
				this.width / 2 - 155,
				this.height - 28,
				150,
				20,
				DialogTexts.GUI_DONE,
				(p_241579_1_) -> {
					this.dimensionConfig.accept(this.selectedPreset);
					this.minecraft.setScreen(this.parent);
				}
			)
		);

		this.addButton(
			new Button(
				this.width / 2 + 5, 
				this.height - 28, 
				150, 
				20, 
				DialogTexts.GUI_CANCEL,
				(p_213015_1_) -> this.minecraft.setScreen(this.parent)
			)
		);
		
		this.guiPresetList.setSelected(this.guiPresetList.children().stream().filter(entry -> Objects.equals(entry.field_238599_b_.getFolderName(), this.selectedPreset.PresetFolderName))
				.findFirst().orElse(null));
	}

	private void func_205306_h()
	{
		this.btnClose.active = this.guiPresetList.getSelected() != null;
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
	class PresetList extends ExtendedList<CreateOTGWorldScreen.PresetList.PresetEntry>
	{
		private PresetList()
		{
			super(
				CreateOTGWorldScreen.this.minecraft,
				CreateOTGWorldScreen.this.width,
				CreateOTGWorldScreen.this.height,
				40,
				CreateOTGWorldScreen.this.height - 37,
				16
			);
			CreateOTGWorldScreen.this.presetList.forEach(preset -> this.addEntry(new PresetEntry(preset)));
		}

		@Override
		protected boolean isFocused()
		{
			return CreateOTGWorldScreen.this.getFocused() == this;
		}

		@Override
		public void setSelected(@Nullable CreateOTGWorldScreen.PresetList.PresetEntry p_241215_1_)
		{
			super.setSelected(p_241215_1_);
			if (p_241215_1_ != null)
			{
				CreateOTGWorldScreen.this.selectedPreset = new DimensionConfig(p_241215_1_.field_238599_b_.getFolderName());
				NarratorChatListener.INSTANCE.sayNow(p_241215_1_.field_238599_b_.getFolderName());
			}
			CreateOTGWorldScreen.this.func_205306_h();
		}
		
		@OnlyIn(Dist.CLIENT)
		class PresetEntry extends ExtendedList.AbstractListEntry<CreateOTGWorldScreen.PresetList.PresetEntry>
		{
			private final Preset field_238599_b_;
			private final ITextComponent field_243282_c;
		
			public PresetEntry(Preset p_i232272_2_)
			{
				this.field_238599_b_ = p_i232272_2_;
				this.field_243282_c = new StringTextComponent(p_i232272_2_.getFolderName());		
			}

			@Override
			public void render(MatrixStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_)
			{
				AbstractGui.drawString(p_230432_1_, CreateOTGWorldScreen.this.font, this.field_243282_c, p_230432_4_ + 5, p_230432_3_ + 2, 16777215);
			}

			@Override
			public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_)
			{
				if (p_231044_5_ == 0)
				{
					PresetList.this.setSelected(this);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
