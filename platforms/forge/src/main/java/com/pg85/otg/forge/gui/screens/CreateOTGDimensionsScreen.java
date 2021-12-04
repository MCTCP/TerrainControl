package com.pg85.otg.forge.gui.screens;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.config.dimensions.DimensionConfig;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGOverWorld;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateOTGDimensionsScreen extends Screen
{
	public final CreateWorldScreen parent;
	public WorldGenSettings dimGenSettings;
	private final Consumer<OTGDimensionSettingsContainer> dimensionConfigConsumer;
	private Component columnType;
	private Component columnHeight;
	private CreateOTGDimensionsScreen.DetailsList list;
	private final DimensionConfig currentSelection;
	private Button addDimButton;
	private Button removeDimButton;
	private Button editDimButton;
	
	private boolean uiLocked = false;
	private static DimensionConfig modpackConfig;
	
	public CreateOTGDimensionsScreen(CreateWorldScreen parent, Consumer<OTGDimensionSettingsContainer> dimensionConfigConsumer)
	{
		super(setTitle());
		this.parent = parent;
		this.dimensionConfigConsumer = dimensionConfigConsumer;
		
		if(modpackConfig == null)
		{
			this.currentSelection = new DimensionConfig();
			this.currentSelection.Overworld = new OTGOverWorld(null, -1, null, null);
			this.currentSelection.Nether = new OTGDimension(null, -1);
			this.currentSelection.End = new OTGDimension(null, -1);
		} else {
			this.currentSelection = modpackConfig;
			this.uiLocked = true;
		}
	}

	private static BaseComponent setTitle()
	{
		// If there is a dimensionconfig for the generatorsettings, use that. Otherwise find a preset by name.
		modpackConfig = DimensionConfig.fromDisk(Constants.MODPACK_CONFIG_NAME);

		if(modpackConfig == null || modpackConfig.ModpackName == null)
		{
			return new TranslatableComponent("otg.createDimensions.customize.title");
		} else {
			return new TextComponent(modpackConfig.ModpackName);
		}
	}
	
	protected void init()
	{
		this.columnType = new TranslatableComponent("otg.createDimensions.customize.dimension");
		this.columnHeight = new TranslatableComponent("otg.createDimensions.customize.preset");
		this.list = new CreateOTGDimensionsScreen.DetailsList();
		this.addWidget(this.list);

		if(!this.uiLocked)
		{
			this.addDimButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 52, 95, 20, new TranslatableComponent("otg.createDimensions.customize.dimension.addDimension"), (p_213007_1_) -> {
				this.currentSelection.Dimensions.add(new OTGDimension(null, -1l));
				this.list.resetRows();
				this.updateButtonValidity();
			}));
	
			this.removeDimButton = this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 52, 95, 20, new TranslatableComponent("otg.createDimensions.customize.dimension.removeDimension"), (p_213007_1_) -> {
				if (this.hasValidSelection() && this.currentSelection.Dimensions.size() > this.list.getSelected().dimId - 3)
				{
					this.currentSelection.Dimensions.remove(this.list.getSelected().dimId - 3);
					this.list.resetRows();
					this.updateButtonValidity();
				}
			}));
			
			this.editDimButton = this.addRenderableWidget(new Button(this.width / 2 + 53, this.height - 52, 95, 20, new TranslatableComponent("otg.createDimensions.customize.dimension.editDimension"), (p_213007_1_) -> {
				if (this.hasValidSelection())
				{
					this.minecraft.setScreen(new SelectOTGPresetScreen(CreateOTGDimensionsScreen.this, CreateOTGDimensionsScreen.this.currentSelection, this.list.getSelected().dimId));
				}
			}));
		}

		// Done
		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, 
			(p_213010_1_) -> {
				if(!(this.parent instanceof ModpackCreateWorldScreen))
				{
					this.dimensionConfigConsumer.accept(new OTGDimensionSettingsContainer(this.currentSelection, this.dimGenSettings));
				}
				this.minecraft.setScreen(this.parent);
			}
		));

		// Cancel
		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, (p_213009_1_) -> {
			this.minecraft.setScreen(this.parent);
		}));

		this.updateButtonValidity();
	}
	
	public void applySettings()
	{
		this.dimensionConfigConsumer.accept(new OTGDimensionSettingsContainer(this.currentSelection, this.dimGenSettings));
	}

	private void updateButtonValidity()
	{
		if(!this.uiLocked)
		{
			this.removeDimButton.active = this.hasValidSelection();
			this.editDimButton.active = this.hasValidSelection();
			this.addDimButton.active = this.list.children().size() <= 13;
		}
	}

	private boolean hasValidSelection()
	{
		return this.list.getSelected() != null;
   	}	
	
	public void onClose()
	{
		this.minecraft.setScreen(this.parent);
	}
	
	public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		this.renderBackground(p_230430_1_);
		this.list.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, 8, 16777215);
		int i = this.width / 2 - 92 - 16;
		drawString(p_230430_1_, this.font, this.columnType, i, 32, 16777215);
		drawString(p_230430_1_, this.font, this.columnHeight, i + 2 + 213 - this.font.width(this.columnHeight), 32, 16777215);
		super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	}

	@OnlyIn(Dist.CLIENT)
	class DetailsList extends ObjectSelectionList<CreateOTGDimensionsScreen.DetailsList.LayerEntry>
	{
		public DetailsList()
		{
			super(CreateOTGDimensionsScreen.this.minecraft, CreateOTGDimensionsScreen.this.width, CreateOTGDimensionsScreen.this.height, 43, CreateOTGDimensionsScreen.this.height - 60, 24);

			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("Overworld", CreateOTGDimensionsScreen.this.currentSelection.Overworld.PresetFolderName == null ? CreateOTGDimensionsScreen.this.currentSelection.Overworld.NonOTGWorldType != null ? CreateOTGDimensionsScreen.this.currentSelection.Overworld.NonOTGWorldType : "Non-OTG": CreateOTGDimensionsScreen.this.currentSelection.Overworld.PresetFolderName, 0));
			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("Nether", CreateOTGDimensionsScreen.this.currentSelection.Nether.PresetFolderName == null ? "Vanilla" : CreateOTGDimensionsScreen.this.currentSelection.Nether.PresetFolderName, 1));
			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("End", CreateOTGDimensionsScreen.this.currentSelection.End.PresetFolderName == null ? "Vanilla" : CreateOTGDimensionsScreen.this.currentSelection.End.PresetFolderName, 2));
			int dimId = 3;
			for(OTGDimension dim : currentSelection.Dimensions)
			{
				this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("OTG " + (dimId - 2), dim.PresetFolderName == null ? "None" : dim.PresetFolderName, dimId));
				dimId++;
			}
		}
	
		public void setSelected(@Nullable CreateOTGDimensionsScreen.DetailsList.LayerEntry p_241215_1_)
		{
			super.setSelected(p_241215_1_);
			CreateOTGDimensionsScreen.this.updateButtonValidity();
		}
		
		protected boolean isFocused()
		{
			return CreateOTGDimensionsScreen.this.getFocused() == this;
		}
		
		protected int getScrollbarPosition()
		{
			return this.width - 70;
		}
		
		public void resetRows()
		{
			int i = this.children().indexOf(this.getSelected());
			this.clearEntries();

			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("Overworld", currentSelection.Overworld.PresetFolderName == null ? currentSelection.Overworld.NonOTGWorldType != null ? currentSelection.Overworld.NonOTGWorldType : "Non-OTG" : currentSelection.Overworld.PresetFolderName, 0));
			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("Nether", currentSelection.Nether.PresetFolderName == null ? "Vanilla" : currentSelection.Nether.PresetFolderName, 1));
			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("End", currentSelection.End.PresetFolderName == null ? "Vanilla" : currentSelection.End.PresetFolderName, 2));
			int dimId = 3;
			for(OTGDimension dim : currentSelection.Dimensions)
			{
				this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("OTG " + (dimId - 2), dim.PresetFolderName == null ? "None" : dim.PresetFolderName, dimId));
				dimId++;
			}

			List<CreateOTGDimensionsScreen.DetailsList.LayerEntry> list = this.children();
			if (i >= 0 && i < list.size())
			{
				this.setSelected(list.get(i));
			}
		}
	
		@OnlyIn(Dist.CLIENT)
		class LayerEntry extends ObjectSelectionList.Entry<CreateOTGDimensionsScreen.DetailsList.LayerEntry>
		{
			private final String dimensionName;
			private String presetFolderName;
			private int dimId;

			private LayerEntry(String dimensionName, String presetFolderName, int dimId)
			{
				this.dimensionName = dimensionName;
				this.presetFolderName = presetFolderName;
				this.dimId = dimId;
			}

			public void render(PoseStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_)
			{
				String s = I18n.get(this.dimensionName, 32);
				CreateOTGDimensionsScreen.this.font.draw(p_230432_1_, s, (float)(p_230432_4_), (float)(p_230432_3_ + 3), 16777215);
				String s1 = I18n.get(this.presetFolderName, 32);
				CreateOTGDimensionsScreen.this.font.draw(p_230432_1_, s1, (float)(p_230432_4_ + 2 + 213 - CreateOTGDimensionsScreen.this.font.width(s1)), (float)(p_230432_3_ + 3), 16777215);
			}

			public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_)
			{
				if (p_231044_5_ == 0 && !CreateOTGDimensionsScreen.this.uiLocked)
				{
					DetailsList.this.setSelected(this);
					if(this.dimId <= 2) // We need to be able to select custom dims, so we can delete them.
					{
						DetailsList.this.minecraft.setScreen(new SelectOTGPresetScreen(CreateOTGDimensionsScreen.this, CreateOTGDimensionsScreen.this.currentSelection, this.dimId));
					}
					return true;
				} else {
					return false;
				}
			}

			@Override
			public Component getNarration()
			{
				return new TextComponent("dimension " + dimensionName + " preset " + presetFolderName);
			}
		}
	}
}
