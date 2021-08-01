package com.pg85.otg.forge.gui.screens;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import org.apache.commons.lang3.StringUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.pg85.otg.forge.gui.OTGGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public class OTGWorldOptionsScreen extends WorldOptionsScreen
{
	private static final ITextComponent CUSTOM_WORLD_DESCRIPTION = new TranslationTextComponent("generator.custom");
	private static final ITextComponent AMPLIFIED_HELP_TEXT = new TranslationTextComponent("generator.amplified.info");
	private IBidiRenderer amplifiedWorldInfo = IBidiRenderer.EMPTY;
	private FontRenderer font;
	private int width;
	private TextFieldWidget seedEdit;
	private Button typeButton;
	private Button customizeTypeButton;
	private DynamicRegistries.Impl registryHolder;
	public DimensionGeneratorSettings settings;
	protected Optional<BiomeGeneratorTypeScreens> preset;
	private OptionalLong seed;

	public OTGWorldOptionsScreen(DynamicRegistries.Impl p_i242065_1_, DimensionGeneratorSettings p_i242065_2_, Optional<BiomeGeneratorTypeScreens> p_i242065_3_, OptionalLong p_i242065_4_)
	{
		super(p_i242065_1_, p_i242065_2_, p_i242065_3_, p_i242065_4_);
		this.registryHolder = p_i242065_1_;
		this.settings = p_i242065_2_;
		this.preset = p_i242065_3_;
		this.seed = p_i242065_4_;
	}

	@Override
	public void init(final CreateWorldScreen p_239048_1_, Minecraft p_239048_2_, FontRenderer p_239048_3_)
	{
		this.font = p_239048_3_;
		this.width = p_239048_1_.width;
		this.seedEdit = new TextFieldWidget(this.font, this.width / 2 - 100, 60, 200, 20, new TranslationTextComponent("selectWorld.enterSeed"));
		this.seedEdit.setValue(toString(this.seed));
		this.seedEdit.setResponder((p_239058_1_) ->
		{
			this.seed = this.parseSeed();
		});
		((OTGCreateWorldScreen)p_239048_1_).addWidget(this.seedEdit);
		int j = this.width / 2 + 5;
		this.typeButton = ((OTGCreateWorldScreen)p_239048_1_).addButton(new Button(j, 100, 150, 20, new TranslationTextComponent("selectWorld.mapType"), (p_239050_2_) ->
		{
			while(true)
			{
				if (this.preset.isPresent())
				{
					int k = BiomeGeneratorTypeScreens.PRESETS.indexOf(this.preset.get()) + 1;
					if (k >= BiomeGeneratorTypeScreens.PRESETS.size())
					{
						k = 0;
					}
	
					BiomeGeneratorTypeScreens biomegeneratortypescreens = BiomeGeneratorTypeScreens.PRESETS.get(k);
					if(biomegeneratortypescreens == OTGGui.OTG_WORLD_TYPE)
					{
						k++;
						if (k >= BiomeGeneratorTypeScreens.PRESETS.size())
						{
							k = 0;
						}
						biomegeneratortypescreens = BiomeGeneratorTypeScreens.PRESETS.get(k);
					}
					this.preset = Optional.of(biomegeneratortypescreens);
					this.settings = biomegeneratortypescreens.create(this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
					if (this.settings.isDebug() && !Screen.hasShiftDown())
					{
						continue;
					}
				}
	
				p_239048_1_.updateDisplayOptions();
				p_239050_2_.queueNarration(250);
				return;
			}
		}) {
			@Override
			public ITextComponent getMessage()
			{
				return super.getMessage().copy().append(" ").append(OTGWorldOptionsScreen.this.preset.map(BiomeGeneratorTypeScreens::description).orElse(OTGWorldOptionsScreen.CUSTOM_WORLD_DESCRIPTION));
			}
	
			@Override
			protected IFormattableTextComponent createNarrationMessage()
			{
				return Objects.equals(OTGWorldOptionsScreen.this.preset, Optional.of(BiomeGeneratorTypeScreens.AMPLIFIED)) ? super.createNarrationMessage().append(". ").append(OTGWorldOptionsScreen.AMPLIFIED_HELP_TEXT) : super.createNarrationMessage();
			}
		});
		this.typeButton.visible = false;
		this.typeButton.active = this.preset.isPresent();
		this.customizeTypeButton = ((OTGCreateWorldScreen)p_239048_1_).addButton(new Button(j, 120, 150, 20, new TranslationTextComponent("selectWorld.customizeType"), (p_239044_3_) ->
		{
			BiomeGeneratorTypeScreens.IFactory biomegeneratortypescreens$ifactory = BiomeGeneratorTypeScreens.EDITORS.get(this.preset);
			biomegeneratortypescreens$ifactory = net.minecraftforge.client.ForgeHooksClient.getBiomeGeneratorTypeScreenFactory(this.preset, biomegeneratortypescreens$ifactory);
			if (biomegeneratortypescreens$ifactory != null)
			{
				p_239048_2_.setScreen(biomegeneratortypescreens$ifactory.createEditScreen(p_239048_1_, this.settings));
			}
		}));
		this.customizeTypeButton.visible = false;
		this.amplifiedWorldInfo = IBidiRenderer.create(p_239048_3_, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
	}

	@Override
	public void tick()
	{
		this.seedEdit.tick();
	}

	@Override
	public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		this.seedEdit.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		if (this.preset.equals(Optional.of(BiomeGeneratorTypeScreens.AMPLIFIED)))
		{
			this.amplifiedWorldInfo.renderLeftAligned(p_230430_1_, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
		}
	}

	@Override
	public void updateSettings(DimensionGeneratorSettings p_239043_1_)
	{
		this.settings = p_239043_1_;
	}

	private static String toString(OptionalLong p_243445_0_)
	{
		return p_243445_0_.isPresent() ? Long.toString(p_243445_0_.getAsLong()) : "";
	}

	private static OptionalLong parseLong(String p_239053_0_)
	{
		try {
			return OptionalLong.of(Long.parseLong(p_239053_0_));
		} catch (NumberFormatException numberformatexception) {
			return OptionalLong.empty();
		}
	}

	@Override
	public DimensionGeneratorSettings makeSettings(boolean p_239054_1_)
	{
		OptionalLong optionallong = this.parseSeed();
		return this.settings.withSeed(p_239054_1_, optionallong);
	}

	private OptionalLong parseSeed()
	{
		String s = this.seedEdit.getValue();
		OptionalLong optionallong;
		if (StringUtils.isEmpty(s))
		{
			optionallong = OptionalLong.empty();
		} else {
			OptionalLong optionallong1 = parseLong(s);
			if (optionallong1.isPresent() && optionallong1.getAsLong() != 0L)
			{
				optionallong = optionallong1;
			} else {
				optionallong = OptionalLong.of((long)s.hashCode());
			}
		}
		return optionallong;
	}

	@Override
	public boolean isDebug()
	{
		return this.settings.isDebug();
	}

	@Override
	public void setDisplayOptions(boolean p_239059_1_)
	{
		this.typeButton.visible = p_239059_1_;
		if (this.settings.isDebug())
		{
			this.customizeTypeButton.visible = false;
		} else {
			this.customizeTypeButton.visible = p_239059_1_ && (BiomeGeneratorTypeScreens.EDITORS.containsKey(this.preset) || net.minecraftforge.client.ForgeHooksClient.hasBiomeGeneratorSettingsOptionsScreen(this.preset));
		}
		
		this.seedEdit.setVisible(p_239059_1_);
	}

	@Override
	public DynamicRegistries.Impl registryHolder()
	{
		return this.registryHolder;
	}	
}
