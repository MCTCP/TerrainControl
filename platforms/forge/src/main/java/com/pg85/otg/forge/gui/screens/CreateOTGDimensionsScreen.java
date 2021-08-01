package com.pg85.otg.forge.gui.screens;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.config.dimensions.DimensionConfig.OTGOverWorld;

import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateOTGDimensionsScreen extends Screen
{	
	public final CreateWorldScreen parent;
	public DimensionGeneratorSettings dimGenSettings;
	private final Consumer<OTGDimensionSettingsContainer> dimensionConfigConsumer;
	private ITextComponent columnType;
	private ITextComponent columnHeight;
	private CreateOTGDimensionsScreen.DetailsList list;
	private final DimensionConfig currentSelection;
	private Button addDimButton;
	private Button removeDimButton;
	private Button editDimButton;
	
	public CreateOTGDimensionsScreen(CreateWorldScreen parent, Consumer<OTGDimensionSettingsContainer> dimensionConfigConsumer)
	{
		super(new TranslationTextComponent("otg.createDimensions.customize.title"));
		this.parent = parent;
		this.dimensionConfigConsumer = dimensionConfigConsumer;
		this.currentSelection = new DimensionConfig();
		this.currentSelection.Overworld = new OTGOverWorld(null, -1, null, null);
		this.currentSelection.Nether = new OTGDimension(null, -1);
		this.currentSelection.End = new OTGDimension(null, -1);
	}

	protected void init()
	{
		this.columnType = new TranslationTextComponent("otg.createDimensions.customize.dimension");
		this.columnHeight = new TranslationTextComponent("otg.createDimensions.customize.preset");
		this.list = new CreateOTGDimensionsScreen.DetailsList();
		this.children.add(this.list);
		
		this.addDimButton = this.addButton(new Button(this.width / 2 - 155, this.height - 52, 95, 20, new TranslationTextComponent("otg.createDimensions.customize.dimension.addDimension"), (p_213007_1_) -> {
			this.currentSelection.Dimensions.add(new OTGDimension(null, -1l));
			this.list.resetRows();
			this.updateButtonValidity();
		}));

		this.removeDimButton = this.addButton(new Button(this.width / 2 - 50, this.height - 52, 95, 20, new TranslationTextComponent("otg.createDimensions.customize.dimension.removeDimension"), (p_213007_1_) -> {
			if (this.hasValidSelection() && this.currentSelection.Dimensions.size() > this.list.getSelected().dimId - 3)
			{
				this.currentSelection.Dimensions.remove(this.list.getSelected().dimId - 3);
				this.list.resetRows();
				this.updateButtonValidity();
			}
		}));
		
		this.editDimButton = this.addButton(new Button(this.width / 2 + 53, this.height - 52, 95, 20, new TranslationTextComponent("otg.createDimensions.customize.dimension.editDimension"), (p_213007_1_) -> {
			if (this.hasValidSelection())
			{
				this.minecraft.setScreen(new SelectOTGPresetScreen(CreateOTGDimensionsScreen.this, CreateOTGDimensionsScreen.this.currentSelection, this.list.getSelected().dimId));
			}
		}));
		
		// Done
		this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, DialogTexts.GUI_DONE, (p_213010_1_) -> {
			this.dimensionConfigConsumer.accept(new OTGDimensionSettingsContainer(this.currentSelection, this.dimGenSettings));			
			this.minecraft.setScreen(this.parent);
		}));

		// Cancel
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, DialogTexts.GUI_CANCEL, (p_213009_1_) -> {
			this.minecraft.setScreen(this.parent);
		}));

		this.updateButtonValidity();
	}

	private void updateButtonValidity()
	{
		this.removeDimButton.active = this.hasValidSelection();
		this.editDimButton.active = this.hasValidSelection();
		this.addDimButton.active = this.list.children().size() <= 13;
	}

	private boolean hasValidSelection()
	{
		return this.list.getSelected() != null;
   	}	
	
	public void onClose()
	{
		this.minecraft.setScreen(this.parent);
	}
	
	public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
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
	class DetailsList extends ExtendedList<CreateOTGDimensionsScreen.DetailsList.LayerEntry>
	{
		public DetailsList()
		{
			super(CreateOTGDimensionsScreen.this.minecraft, CreateOTGDimensionsScreen.this.width, CreateOTGDimensionsScreen.this.height, 43, CreateOTGDimensionsScreen.this.height - 60, 24);

			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("Overworld", currentSelection.Overworld.PresetFolderName == null ? "Non-OTG": currentSelection.Overworld.PresetFolderName, 0));
			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("Nether", currentSelection.Nether.PresetFolderName == null ? "Vanilla" : currentSelection.Nether.PresetFolderName, 1));
			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("End", currentSelection.End.PresetFolderName == null ? "Vanilla" : currentSelection.End.PresetFolderName, 2));
			int dimId = 3;
			for(OTGDimension dim : currentSelection.Dimensions)
			{
				this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry(dim.PresetFolderName == null ? "DIM-" + dimId : dim.PresetFolderName, dim.PresetFolderName == null ? "None" : dim.PresetFolderName, dimId));
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

			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("Overworld", currentSelection.Overworld.PresetFolderName == null ? "Non-OTG" : currentSelection.Overworld.PresetFolderName, 0));
			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("Nether", currentSelection.Nether.PresetFolderName == null ? "Vanilla" : currentSelection.Nether.PresetFolderName, 1));
			this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry("End", currentSelection.End.PresetFolderName == null ? "Vanilla" : currentSelection.End.PresetFolderName, 2));
			int dimId = 3;
			for(OTGDimension dim : currentSelection.Dimensions)
			{
				this.addEntry(new CreateOTGDimensionsScreen.DetailsList.LayerEntry(dim.PresetFolderName == null ? "DIM-" + dimId : dim.PresetFolderName, dim.PresetFolderName == null ? "None" : dim.PresetFolderName, dimId));
				dimId++;
			}

			List<CreateOTGDimensionsScreen.DetailsList.LayerEntry> list = this.children();
			if (i >= 0 && i < list.size())
			{
				this.setSelected(list.get(i));
			}
		}
	
		@OnlyIn(Dist.CLIENT)
		class LayerEntry extends ExtendedList.AbstractListEntry<CreateOTGDimensionsScreen.DetailsList.LayerEntry>
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

			public void render(MatrixStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_)
			{
				String s = I18n.get(this.dimensionName, 32);
				CreateOTGDimensionsScreen.this.font.draw(p_230432_1_, s, (float)(p_230432_4_), (float)(p_230432_3_ + 3), 16777215);
				String s1 = I18n.get(this.presetFolderName, 32);
				CreateOTGDimensionsScreen.this.font.draw(p_230432_1_, s1, (float)(p_230432_4_ + 2 + 213 - CreateOTGDimensionsScreen.this.font.width(s1)), (float)(p_230432_3_ + 3), 16777215);
			}

			public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_)
			{
				if (p_231044_5_ == 0)
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
		}
	}
}
