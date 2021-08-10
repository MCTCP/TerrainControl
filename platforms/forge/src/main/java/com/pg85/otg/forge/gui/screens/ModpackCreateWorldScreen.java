package com.pg85.otg.forge.gui.screens;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.gui.OTGGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.util.FileUtil;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.FolderName;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModpackCreateWorldScreen extends CreateWorldScreen
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ITextComponent GAME_MODEL_LABEL = new TranslationTextComponent("selectWorld.gameMode");
	private static final ITextComponent NAME_LABEL = new TranslationTextComponent("selectWorld.enterName");
	private static final ITextComponent OUTPUT_DIR_INFO = new TranslationTextComponent("selectWorld.resultFolder");
	private static final ITextComponent COMMANDS_INFO = new TranslationTextComponent("selectWorld.allowCommands.info");
	private TextFieldWidget seedEdit;
	private ModpackCreateWorldScreen.GameMode gameMode = ModpackCreateWorldScreen.GameMode.SURVIVAL;
	@Nullable
	private ModpackCreateWorldScreen.GameMode oldGameMode;
	private Difficulty selectedDifficulty = Difficulty.NORMAL;
	private boolean commandsChanged;
	@Nullable
	private Path tempDataPackDir;
	@Nullable
	private ResourcePackList tempDataPackRepository;
	private Button createButton;
	private Button difficultyButton;
	private Button commandsButton;
	private ITextComponent gameModeHelp1;
	private ITextComponent gameModeHelp2;
	private String initName;
	private String initSeed = "";
	private ITextComponent title2;

	public static ModpackCreateWorldScreen create(@Nullable Screen screen)
	{
		DynamicRegistries.Impl dynamicregistry = DynamicRegistries.builtin();
		DimensionGeneratorSettings dimGenSettings = net.minecraftforge.client.ForgeHooksClient.getDefaultWorldType().map(
			type -> type.create(
					dynamicregistry, 
					new java.util.Random().nextLong(), 
					true, 
					false
				)
			).orElseGet(() -> 
				DimensionGeneratorSettings.makeDefault(
					dynamicregistry.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY), 
					dynamicregistry.registryOrThrow(Registry.BIOME_REGISTRY), 
					dynamicregistry.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
				)
			);
		
		return new ModpackCreateWorldScreen(
			screen,
			DatapackCodec.DEFAULT, 
			new WorldOptionsScreen(
				dynamicregistry, 
				dimGenSettings, 
				net.minecraftforge.client.ForgeHooksClient.getDefaultWorldType(), 
				OptionalLong.empty()
			),
			dimGenSettings
		);
	}

	public ModpackCreateWorldScreen(@Nullable Screen screen, DatapackCodec datapackCodec, WorldOptionsScreen worldGenSettingsComponent, DimensionGeneratorSettings dimGenSettings)
	{
		super(screen, datapackCodec, worldGenSettingsComponent);
		this.initName = I18n.get("selectWorld.newWorld");

		// Does the same as opening the customisation/dimensions menu and applying, registering the dimensions from the modpack config.
		Optional<BiomeGeneratorTypeScreens> preset = Optional.of(OTGGui.OTG_WORLD_TYPE);
		BiomeGeneratorTypeScreens.IFactory biomegeneratortypescreens$ifactory = BiomeGeneratorTypeScreens.EDITORS.get(preset);
		biomegeneratortypescreens$ifactory = net.minecraftforge.client.ForgeHooksClient.getBiomeGeneratorTypeScreenFactory(preset, biomegeneratortypescreens$ifactory);
		if (biomegeneratortypescreens$ifactory != null)
		{
			((CreateOTGDimensionsScreen)biomegeneratortypescreens$ifactory.createEditScreen(this, dimGenSettings)).applySettings();
		}
	}
	
	@Override
	public void tick()
	{
		this.nameEdit.tick();
		this.seedEdit.tick();
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	protected void init()
	{
		int i = this.width / 2 - 155;
		int j = this.width / 2 + 5;

		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.nameEdit = new TextFieldWidget(this.font, i, 60, 150, 20, new TranslationTextComponent("selectWorld.enterName"))
		{
			protected IFormattableTextComponent createNarrationMessage()
			{
				return super.createNarrationMessage().append(". ").append(new TranslationTextComponent("selectWorld.resultFolder")).append(" ").append(ModpackCreateWorldScreen.this.resultFolder);
			}
		};
		this.seedEdit = new TextFieldWidget(this.font, j, 60, 150, 20, new TranslationTextComponent("selectWorld.enterName"))
		{
			protected IFormattableTextComponent createNarrationMessage()
			{
				return super.createNarrationMessage().append(". ").append(new TranslationTextComponent("selectWorld.resultFolder")).append(" ").append(ModpackCreateWorldScreen.this.resultFolder);
			}
		};		
		this.nameEdit.setValue(this.initName);
		this.seedEdit.setValue(this.initSeed);
		this.nameEdit.setResponder((p_214319_1_) ->
		{
			this.initName = p_214319_1_;
			this.createButton.active = !this.nameEdit.getValue().isEmpty();
			this.updateResultFolder();
		});
		this.children.add(this.nameEdit);
		this.seedEdit.setResponder((p_214319_1_) ->
		{
			this.initSeed = p_214319_1_;
		});
		this.children.add(this.seedEdit);
		this.addButton(
			new Button(i, 110, 150, 20, StringTextComponent.EMPTY, 
				(p_214316_1_) ->
				{
					switch(this.gameMode)
					{
						case SURVIVAL:
							this.setGameMode(ModpackCreateWorldScreen.GameMode.HARDCORE);
							break;
						case HARDCORE:
							this.setGameMode(ModpackCreateWorldScreen.GameMode.CREATIVE);
							break;
						case CREATIVE:
							this.setGameMode(ModpackCreateWorldScreen.GameMode.SURVIVAL);
					}
				
					p_214316_1_.queueNarration(250);
				}
			) {
				public ITextComponent getMessage()
				{
					return new TranslationTextComponent("options.generic_value", ModpackCreateWorldScreen.GAME_MODEL_LABEL, new TranslationTextComponent("selectWorld.gameMode." + ModpackCreateWorldScreen.this.gameMode.name));
				}
			
				protected IFormattableTextComponent createNarrationMessage()
				{
					return super.createNarrationMessage().append(". ").append(ModpackCreateWorldScreen.this.gameModeHelp1).append(" ").append(ModpackCreateWorldScreen.this.gameModeHelp2);
				}
			}
		);
		this.difficultyButton = this.addButton(
			new Button(j, 110, 150, 20, new TranslationTextComponent("options.difficulty"),
			(p_238956_1_) -> {
				this.selectedDifficulty = this.selectedDifficulty.nextById();
				this.effectiveDifficulty = this.selectedDifficulty;
				p_238956_1_.queueNarration(250);
			}) {
				public ITextComponent getMessage()
				{
					return (new TranslationTextComponent("options.difficulty")).append(": ").append(ModpackCreateWorldScreen.this.effectiveDifficulty.getDisplayName());
				}
			}
		);
		this.commandsButton = this.addButton(new Button(i, 161, 150, 20, new TranslationTextComponent("selectWorld.allowCommands"), 
			(p_214322_1_) -> {
				this.commandsChanged = true;
				this.commands = !this.commands;
				p_214322_1_.queueNarration(250);
			}) {
				public ITextComponent getMessage()
				{
					return DialogTexts.optionStatus(super.getMessage(), ModpackCreateWorldScreen.this.commands && !ModpackCreateWorldScreen.this.hardCore);
				}
		
				protected IFormattableTextComponent createNarrationMessage()
				{
					return super.createNarrationMessage().append(". ").append(new TranslationTextComponent("selectWorld.allowCommands.info"));
				}
			}
		);
		this.addButton(new Button(j, 161, 150, 20, new StringTextComponent("Dimensions"), // TODO: Use translationtext
			(p_214322_1_) -> {
				Optional<BiomeGeneratorTypeScreens> preset = Optional.of(OTGGui.OTG_WORLD_TYPE);
				BiomeGeneratorTypeScreens.IFactory biomegeneratortypescreens$ifactory = BiomeGeneratorTypeScreens.EDITORS.get(preset);
				biomegeneratortypescreens$ifactory = net.minecraftforge.client.ForgeHooksClient.getBiomeGeneratorTypeScreenFactory(preset, biomegeneratortypescreens$ifactory);
				if (biomegeneratortypescreens$ifactory != null)
				{
					this.minecraft.setScreen(biomegeneratortypescreens$ifactory.createEditScreen(this, this.worldGenSettingsComponent.makeSettings(this.hardCore)));
				}
			}) {
				public ITextComponent getMessage()
				{
					// TODO: Add translations/narration
					//return DialogTexts.optionStatus(super.getMessage(), ModpackCreateWorldScreen.this.commands && !ModpackCreateWorldScreen.this.hardCore);
					return new StringTextComponent("Dimensions");
				}

				protected IFormattableTextComponent createNarrationMessage()
				{
					// TODO: Add translations/narration
					//return super.createNarrationMessage().append(". ").append(new TranslationTextComponent("selectWorld.allowCommands.info"));
					return super.createNarrationMessage().append(". ").append(new StringTextComponent("Dimensions"));
				}
			}
		);
		this.createButton = this.addButton(
			new Button(i, this.height - 28, 150, 20, new TranslationTextComponent("selectWorld.create"),
				(p_214318_1_) -> {
					this.onCreate();
				}
			)
		);
		this.createButton.active = !this.initName.isEmpty();
		this.addButton(
			new Button(j, this.height - 28, 150, 20, DialogTexts.GUI_CANCEL, 
				(p_214317_1_) -> {
					this.popScreen();
				}
			)
		);

		this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
		
		this.updateDisplayOptions();
		this.setInitialFocus(this.nameEdit);
		this.setGameMode(this.gameMode);
		this.updateResultFolder();

		DimensionConfig modPackConfig = DimensionConfig.fromDisk(Constants.MODPACK_CONFIG_NAME);
		if(modPackConfig == null || modPackConfig.ModpackName == null)
		{
			this.title2 = new TranslationTextComponent("otg.createDimensions.customize.title");
		} else {
			this.title2 = new StringTextComponent(modPackConfig.ModpackName);
		}
	}

	private void updateGameModeHelp()
	{
		this.gameModeHelp1 = new TranslationTextComponent("selectWorld.gameMode." + this.gameMode.name + ".line1");
		this.gameModeHelp2 = new TranslationTextComponent("selectWorld.gameMode." + this.gameMode.name + ".line2");
	}

	private void updateResultFolder()
	{
		this.resultFolder = this.nameEdit.getValue().trim();
		if (this.resultFolder.isEmpty())
		{
			this.resultFolder = "World";
		}

		try {
			this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
		} catch (Exception exception1) {
			this.resultFolder = "World";
			try {
				this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
			} catch (Exception exception) {
				throw new RuntimeException("Could not create save folder", exception);
			}
		}
	}

	@Override
	public void removed()
	{
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	private void onCreate()
	{
		this.minecraft.forceSetScreen(new DirtMessageScreen(new TranslationTextComponent("createWorld.preparing")));
		if (this.copyTempDataPackDirToNewWorld())
		{
			this.cleanupTempResources();
			WorldSettings worldsettings = new WorldSettings(this.nameEdit.getValue().trim(), this.gameMode.gameType, this.hardCore, this.effectiveDifficulty, this.commands && !this.hardCore, this.gameRules, this.dataPacks);
			this.minecraft.createLevel(this.resultFolder, worldsettings, this.worldGenSettingsComponent.registryHolder(), this.worldGenSettingsComponent.makeSettings(worldsettings.hardcore()).withSeed(worldsettings.hardcore(), parseSeed()));
		}
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

	private static OptionalLong parseLong(String p_239053_0_)
	{
		try {
			return OptionalLong.of(Long.parseLong(p_239053_0_));
		} catch (NumberFormatException numberformatexception) {
			return OptionalLong.empty();
		}
	}
	
	private void setGameMode(ModpackCreateWorldScreen.GameMode p_228200_1_)
	{
		if (!this.commandsChanged)
		{
			this.commands = p_228200_1_ == ModpackCreateWorldScreen.GameMode.CREATIVE;
		}

		if (p_228200_1_ == ModpackCreateWorldScreen.GameMode.HARDCORE)
		{
			this.hardCore = true;
			this.commandsButton.active = false;
			this.effectiveDifficulty = Difficulty.HARD;
			this.difficultyButton.active = false;
		} else {
			this.hardCore = false;
			this.commandsButton.active = true;
			this.effectiveDifficulty = this.selectedDifficulty;
			this.difficultyButton.active = true;
		}

		this.gameMode = p_228200_1_;
		this.updateGameModeHelp();
	}

	@Override
	public void updateDisplayOptions() { }

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
			this.onCreate();
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
		this.minecraft.setScreen(this.lastScreen);
		this.cleanupTempResources();
	}

	private void cleanupTempResources()
	{
		if (this.tempDataPackRepository != null)
		{
			this.tempDataPackRepository.close();
		}
		this.removeTempDataPackDir();
	}

	@Override
	public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		this.renderBackground(p_230430_1_);
		drawCenteredString(p_230430_1_, this.font, this.title2, this.width / 2, 20, -1);
		drawString(p_230430_1_, this.font, NAME_LABEL, this.width / 2 - 150, 47, -6250336);
		drawString(p_230430_1_, this.font, (new StringTextComponent("")).append(OUTPUT_DIR_INFO).append(" ").append(this.resultFolder), this.width / 2 - 150, 85, -6250336);
		drawString(p_230430_1_, this.font, "Seed", this.width / 2 + 10, 47, -6250336);
		this.nameEdit.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		this.seedEdit.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		drawString(p_230430_1_, this.font, this.gameModeHelp1, this.width / 2 - 150, 132, -6250336);
		drawString(p_230430_1_, this.font, this.gameModeHelp2, this.width / 2 - 150, 144, -6250336);
		if (this.commandsButton.visible)
		{
			drawString(p_230430_1_, this.font, COMMANDS_INFO, this.width / 2 - 150, 182, -6250336);
		}

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

	@Override
	@Nullable
	protected Path getTempDataPackDir()
	{
		if (this.tempDataPackDir == null)
		{
			try {
				this.tempDataPackDir = Files.createTempDirectory("mcworld-");
			} catch (IOException ioexception) {
				LOGGER.warn("Failed to create temporary dir", (Throwable)ioexception);
				SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
				this.popScreen();
			}
		}
		return this.tempDataPackDir;
	}

	private void removeTempDataPackDir()
	{
		if (this.tempDataPackDir != null)
		{
			try (Stream<Path> stream = Files.walk(this.tempDataPackDir))
			{
				stream.sorted(Comparator.reverseOrder()).forEach(
					(p_238948_0_) -> {
						try {
							Files.delete(p_238948_0_);
						} catch (IOException ioexception1) {
							LOGGER.warn("Failed to remove temporary file {}", p_238948_0_, ioexception1);
						}
		
					}
				);
			} catch (IOException ioexception) {
				LOGGER.warn("Failed to list temporary dir {}", (Object)this.tempDataPackDir);
			}
			this.tempDataPackDir = null;
		}
	}

	private static void copyBetweenDirs(Path p_238945_0_, Path p_238945_1_, Path p_238945_2_)
	{
		try {
			Util.copyBetweenDirs(p_238945_0_, p_238945_1_, p_238945_2_);
		} catch (IOException ioexception) {
			LOGGER.warn("Failed to copy datapack file from {} to {}", p_238945_2_, p_238945_1_);
			throw new ModpackCreateWorldScreen.DatapackException(ioexception);
		}
	}

	private boolean copyTempDataPackDirToNewWorld()
	{
		if (this.tempDataPackDir != null)
		{
			try (
					SaveFormat.LevelSave saveformat$levelsave = this.minecraft.getLevelSource().createAccess(this.resultFolder);
					Stream<Path> stream = Files.walk(this.tempDataPackDir);
			) {
				Path path = saveformat$levelsave.getLevelPath(FolderName.DATAPACK_DIR);
				Files.createDirectories(path);
				stream.filter(
					(p_238942_1_) -> {
						return !p_238942_1_.equals(this.tempDataPackDir);
					}).forEach((p_238949_2_) -> {
						copyBetweenDirs(this.tempDataPackDir, path, p_238949_2_);
					});
			} catch (ModpackCreateWorldScreen.DatapackException | IOException ioexception) {
				LOGGER.warn("Failed to copy datapacks to world {}", this.resultFolder, ioexception);
				SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
				this.popScreen();
				return false;
			}
		}
	
		return true;
	}

	@Nullable
	public static Path createTempDataPackDirFromExistingWorld(Path p_238943_0_, Minecraft p_238943_1_)
	{
		MutableObject<Path> mutableobject = new MutableObject<>();

		try (Stream<Path> stream = Files.walk(p_238943_0_))
		{
			stream.filter(
				(p_238944_1_) -> {
					return !p_238944_1_.equals(p_238943_0_);
				}).forEach((p_238947_2_) -> {
					Path path = mutableobject.getValue();
					if (path == null)
					{
						try {
							path = Files.createTempDirectory("mcworld-");
						} catch (IOException ioexception1) {
							LOGGER.warn("Failed to create temporary dir");
							throw new ModpackCreateWorldScreen.DatapackException(ioexception1);
						}
		
						mutableobject.setValue(path);
					}
					copyBetweenDirs(p_238943_0_, path, p_238947_2_);
				});
		} catch (ModpackCreateWorldScreen.DatapackException | IOException ioexception) {
			LOGGER.warn("Failed to copy datapacks from world {}", p_238943_0_, ioexception);
			SystemToast.onPackCopyFailure(p_238943_1_, p_238943_0_.toString());
			return null;
		}
	
		return mutableobject.getValue();
	}

	@SuppressWarnings("serial")
	@OnlyIn(Dist.CLIENT)
	static class DatapackException extends RuntimeException
	{
		public DatapackException(Throwable p_i232309_1_)
		{
			super(p_i232309_1_);
		}
	}

	@OnlyIn(Dist.CLIENT)
	static enum GameMode
	{
		SURVIVAL("survival", GameType.SURVIVAL),
		HARDCORE("hardcore", GameType.SURVIVAL),
		CREATIVE("creative", GameType.CREATIVE),
		DEBUG("spectator", GameType.SPECTATOR);
	
		private final String name;
		private final GameType gameType;
	
		private GameMode(String p_i225940_3_, GameType p_i225940_4_)
		{
			this.name = p_i225940_3_;
			this.gameType = p_i225940_4_;
		}
	}	
}
