package com.pg85.otg.forge.gui.screens;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public class OTGWorldOptionsScreen extends WorldGenSettingsComponent
{
	private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
	private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
	private int width;
	private CycleButton<WorldPreset> typeButton;
	private Button customizeTypeButton;
	private RegistryAccess.RegistryHolder registryHolder;
	public WorldGenSettings settings;
	protected Optional<WorldPreset> preset;
	private OptionalLong seed;

	public OTGWorldOptionsScreen(RegistryAccess.RegistryHolder p_i242065_1_, WorldGenSettings p_i242065_2_, Optional<WorldPreset> p_i242065_3_, OptionalLong p_i242065_4_)
	{
		super(p_i242065_1_, p_i242065_2_, p_i242065_3_, p_i242065_4_);
		this.registryHolder = p_i242065_1_;
		this.settings = p_i242065_2_;
		this.preset = p_i242065_3_;
		this.seed = p_i242065_4_;
	}

	@Override
	public void init(CreateWorldScreen p_239048_1_, Minecraft p_239048_2_, Font p_239048_3_)
	{
		this.width = p_239048_1_.width;
		int j = this.width / 2 + 5;
		this.typeButton = ((OTGCustomiseOverworldScreen)p_239048_1_).addRenderableWidget(
			CycleButton.builder(WorldPreset::description).withValues(
				WorldPreset.PRESETS.stream().filter(WorldPreset::isVisibleByDefault).collect(Collectors.toList()), 
				WorldPreset.PRESETS
			).withCustomNarration(
				(p_170264_) -> {
					return p_170264_.getValue() == WorldPreset.AMPLIFIED ? CommonComponents.joinForNarration(p_170264_.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT) : p_170264_.createDefaultNarrationMessage();
				}
			).create(
				j, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), 
				(p_170274_, p_170275_) ->
				{
					this.preset = Optional.of(p_170275_);
					this.settings = p_170275_.create(this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
					setVisibility(true);
				}
			)
		);
		this.preset.ifPresent(this.typeButton::setValue);		
		this.typeButton.visible = true;
		this.typeButton.active = this.preset.isPresent();
		this.customizeTypeButton = ((OTGCustomiseOverworldScreen)p_239048_1_).addRenderableWidget(new Button(j, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), (p_239044_3_) ->
		{
			WorldPreset.PresetEditor biomegeneratortypescreens$ifactory = WorldPreset.EDITORS.get(this.preset);
			biomegeneratortypescreens$ifactory = net.minecraftforge.client.ForgeHooksClient.getBiomeGeneratorTypeScreenFactory(this.preset, biomegeneratortypescreens$ifactory);
			if (biomegeneratortypescreens$ifactory != null)
			{
				p_239048_2_.setScreen(biomegeneratortypescreens$ifactory.createEditScreen(p_239048_1_, this.settings));
			}
		}));
		this.customizeTypeButton.visible = false;
		this.amplifiedWorldInfo = MultiLineLabel.create(p_239048_3_, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
	}

	@Override
	public void tick() { }

	@Override
	public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		if (this.preset.equals(Optional.of(WorldPreset.AMPLIFIED)))
		{
			this.amplifiedWorldInfo.renderLeftAligned(p_230430_1_, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
		}
	}

	@Override
	public void updateSettings(WorldGenSettings p_239043_1_)
	{
		this.settings = p_239043_1_;
	}

	@Override
	public WorldGenSettings makeSettings(boolean p_239054_1_)
	{
		return this.settings.withSeed(p_239054_1_, this.seed);
	}

	@Override
	public boolean isDebug()
	{
		return this.settings.isDebug();
	}

	@Override
	public void setVisibility(boolean p_239059_1_)
	{
		if (this.settings.isDebug())
		{
			this.customizeTypeButton.visible = false;
		} else {
			this.customizeTypeButton.visible = p_239059_1_ && (WorldPreset.EDITORS.containsKey(this.preset) || net.minecraftforge.client.ForgeHooksClient.hasBiomeGeneratorSettingsOptionsScreen(this.preset));
		}
	}

	@Override
	public RegistryAccess.RegistryHolder registryHolder()
	{
		return this.registryHolder;
	}	
}
