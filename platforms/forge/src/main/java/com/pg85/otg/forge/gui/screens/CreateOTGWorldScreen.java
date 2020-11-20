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
		this.selectedPreset = new DimensionConfig(this.presetList.get(0).getName(), 0, true, this.presetList.get(0).getWorldConfig());
	}
	
	public CreateOTGWorldScreen(Screen p_i242054_1_, DynamicRegistries p_i242054_2_, Consumer<DimensionConfig> dimensionConfig, DimensionConfig selectedPreset)
	{
		super(new TranslationTextComponent("otg.createWorld.customize.title"));
		this.parent = p_i242054_1_;
		this.dimensionConfig = dimensionConfig;
		this.selectedPreset = selectedPreset;
		this.presetList = OTG.getEngine().getPresetLoader().getAllPresets();
	}

	public void func_231175_as__()
	{
		this.field_230706_i_.displayGuiScreen(this.parent);
	}

	protected void func_231160_c_()
	{
		this.field_230706_i_.keyboardListener.enableRepeatEvents(true);
		this.guiPresetList = new CreateOTGWorldScreen.PresetList();
		this.field_230705_e_.add(this.guiPresetList);
		this.btnClose = this.func_230480_a_(
			new Button(
				this.field_230708_k_ / 2 - 155,
				this.field_230709_l_ - 28,
				150,
				20,
				DialogTexts.field_240632_c_, 
				(p_241579_1_) -> {
					this.dimensionConfig.accept(this.selectedPreset);
					this.field_230706_i_.displayGuiScreen(this.parent);
				}
			)
		);

		this.func_230480_a_(
			new Button(
				this.field_230708_k_ / 2 + 5, 
				this.field_230709_l_ - 28, 
				150, 
				20, 
				DialogTexts.field_240633_d_, 
				(p_213015_1_) ->
				{
					this.field_230706_i_.displayGuiScreen(this.parent);
				}
			)
		);
		
		this.guiPresetList.func_241215_a_(this.guiPresetList.func_231039_at__().stream().filter((p_241578_1_) ->
		{
			return Objects.equals(p_241578_1_.field_238599_b_.getName(), this.selectedPreset.PresetName);
		}).findFirst().orElse(
			(CreateOTGWorldScreen.PresetList.PresetEntry)null)
		);
	}

	private void func_205306_h()
	{
		this.btnClose.field_230693_o_ = this.guiPresetList.func_230958_g_() != null;
	}	
	
	public void func_230430_a_(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		this.func_231165_f_(0);
		this.guiPresetList.func_230430_a_(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		func_238472_a_(p_230430_1_, this.field_230712_o_, this.field_230704_d_, this.field_230708_k_ / 2, 8, 16777215);
		func_238472_a_(p_230430_1_, this.field_230712_o_, SELECT_PRESET, this.field_230708_k_ / 2, 28, 10526880);
		super.func_230430_a_(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	}
			
	@OnlyIn(Dist.CLIENT)
	class PresetList extends ExtendedList<CreateOTGWorldScreen.PresetList.PresetEntry>
	{
		private PresetList()
		{
			super(
				CreateOTGWorldScreen.this.field_230706_i_,
				CreateOTGWorldScreen.this.field_230708_k_,
				CreateOTGWorldScreen.this.field_230709_l_,
				40,
				CreateOTGWorldScreen.this.field_230709_l_ - 37,
				16
			);
			CreateOTGWorldScreen.this.presetList.forEach((p_238597_1_) -> {
				this.func_230513_b_(new CreateOTGWorldScreen.PresetList.PresetEntry(p_238597_1_));
			});
		}
		
		protected boolean func_230971_aw__()
		{
			return CreateOTGWorldScreen.this.func_241217_q_() == this;
		}
		
		public void func_241215_a_(@Nullable CreateOTGWorldScreen.PresetList.PresetEntry p_241215_1_)
		{
			super.func_241215_a_(p_241215_1_);
			if (p_241215_1_ != null)
			{
				CreateOTGWorldScreen.this.selectedPreset = new DimensionConfig(p_241215_1_.field_238599_b_.getName(), 0, true,  p_241215_1_.field_238599_b_.getWorldConfig());
				NarratorChatListener.INSTANCE.say(p_241215_1_.field_238599_b_.getName());
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
				this.field_243282_c = new StringTextComponent(p_i232272_2_.getName());		
			}
		
			public void func_230432_a_(MatrixStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_)
			{
				AbstractGui.func_238475_b_(p_230432_1_, CreateOTGWorldScreen.this.field_230712_o_, this.field_243282_c, p_230432_4_ + 5, p_230432_3_ + 2, 16777215);
			}
		
			public boolean func_231044_a_(double p_231044_1_, double p_231044_3_, int p_231044_5_)
			{
				if (p_231044_5_ == 0)
				{
					PresetList.this.func_241215_a_(this);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
