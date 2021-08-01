package com.pg85.otg.forge.gui.screens;

import java.util.OptionalLong;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pg85.otg.config.dimensions.DimensionConfig;

import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OTGCreateWorldScreen extends CreateWorldScreen
{
	private final CreateOTGDimensionsScreen parent;
	private final DimensionConfig dimConfig;
	private Button createButton;

	public static OTGCreateWorldScreen create(@Nullable CreateOTGDimensionsScreen parent , DimensionConfig dimConfig)
	{
		return new OTGCreateWorldScreen(
			parent,
			new OTGWorldOptionsScreen(
				parent.parent.worldGenSettingsComponent.registryHolder(),
				net.minecraftforge.client.ForgeHooksClient.getDefaultWorldType().map(
					type -> type.create(
						parent.parent.worldGenSettingsComponent.registryHolder(), 
						new java.util.Random().nextLong(), 
						true, 
						false
					)
				).orElseGet(
					() -> DimensionGeneratorSettings.makeDefault(
						parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY),
						parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY),
						parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
					)
				),
				net.minecraftforge.client.ForgeHooksClient.getDefaultWorldType(), OptionalLong.empty()
			),
			dimConfig
		);
	}

	public OTGCreateWorldScreen(@Nullable CreateOTGDimensionsScreen parent, OTGWorldOptionsScreen p_i242063_3_, DimensionConfig dimConfig)
	{
		super(parent, DatapackCodec.DEFAULT, p_i242063_3_);
		this.parent = parent;
		this.dimConfig = dimConfig;
	}

	@Override
	public void tick()
	{
		 ((OTGWorldOptionsScreen)this.worldGenSettingsComponent).tick();
	}
	
	@Override
	protected void init()
	{
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		int i = this.width / 2 - 155;
		int j = this.width / 2 + 5;
		 ((OTGWorldOptionsScreen)this.worldGenSettingsComponent).init(this, this.minecraft, this.font);
		this.createButton = this.addButton(new Button(i, this.height - 28, 150, 20, DialogTexts.GUI_DONE, (p_214318_1_) ->
		{
			this.dimConfig.Overworld.NonOTGWorldType = ((OTGWorldOptionsScreen)this.worldGenSettingsComponent).preset.get().description().getString().replace("generator.", "");
			this.parent.dimGenSettings = ((OTGWorldOptionsScreen)this.worldGenSettingsComponent).settings;
			this.popScreen();
		}));
		this.createButton.active = true;
		this.addButton(new Button(j, this.height - 28, 150, 20, DialogTexts.GUI_CANCEL, (p_214317_1_) ->
		{
			this.popScreen();
		}));
		 ((OTGWorldOptionsScreen)this.worldGenSettingsComponent).setDisplayOptions(true);
	}	

	@Override
	public void removed()
	{
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void updateDisplayOptions()
	{
		 ((OTGWorldOptionsScreen)this.worldGenSettingsComponent).setDisplayOptions(true);
	}
	
	@Override
	public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_)
	{
		if (super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_))
		{
			return true;
		}
		else if (p_231046_1_ != 257 && p_231046_1_ != 335)
		{
			return false;
		} else {
			//this.onCreate();
			return true;
		}
	}

	@Override
	public void onClose()
	{
		this.popScreen();
	}

	@Override
	public void popScreen()
	{
		this.minecraft.setScreen(this.parent);
	}

	@Override
	public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		this.renderBackground(p_230430_1_);
		drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, 20, -1);

		for(int i = 0; i < this.buttons.size(); ++i)
		{
			this.buttons.get(i).render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		}
	}

	@Override
	protected <T extends IGuiEventListener> T addWidget(T p_230481_1_)
	{
		return super.addWidget(p_230481_1_);
	}

	@Override
	protected <T extends Widget> T addButton(T p_230480_1_)
	{
		return super.addButton(p_230480_1_);
	}
}
