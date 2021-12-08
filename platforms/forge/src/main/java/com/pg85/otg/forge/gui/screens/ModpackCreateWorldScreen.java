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

import com.mojang.blaze3d.vertex.PoseStack;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.config.dimensions.DimensionConfig;
import com.pg85.otg.forge.gui.OTGGui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModpackCreateWorldScreen extends CreateWorldScreen
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Component GAME_MODEL_LABEL = new TranslatableComponent("selectWorld.gameMode");
	private static final Component NAME_LABEL = new TranslatableComponent("selectWorld.enterName");
	private static final Component OUTPUT_DIR_INFO = new TranslatableComponent("selectWorld.resultFolder");
	private static final Component COMMANDS_INFO = new TranslatableComponent("selectWorld.allowCommands.info");
	private EditBox seedEdit;
	private ModpackCreateWorldScreen.GameMode gameMode = ModpackCreateWorldScreen.GameMode.SURVIVAL;
	@Nullable
	private ModpackCreateWorldScreen.GameMode oldGameMode;
	private Difficulty difficulty = Difficulty.NORMAL;
	private boolean commandsChanged;
	@Nullable
	private Path tempDataPackDir;
	@Nullable
	private PackRepository tempDataPackRepository;
	private Button createButton;
	private CycleButton<Difficulty> difficultyButton;
	private CycleButton<Boolean> commandsButton;
	private Component gameModeHelp1;
	private Component gameModeHelp2;
	private String initName;
	private String initSeed = "";
	private Component title2;

	public static ModpackCreateWorldScreen create(@Nullable Screen screen)
	{
		RegistryAccess.RegistryHolder dynamicregistry = RegistryAccess.builtin();
		WorldGenSettings dimGenSettings = net.minecraftforge.client.ForgeHooksClient.getDefaultWorldPreset().map(
			type -> type.create(
					dynamicregistry, 
					new java.util.Random().nextLong(), 
					true, 
					false
				)
			).orElseGet(() -> 
				WorldGenSettings.makeDefault(dynamicregistry)
			);
		
		return new ModpackCreateWorldScreen(
			screen,
			DataPackConfig.DEFAULT, 
			new WorldGenSettingsComponent(
				dynamicregistry, 
				dimGenSettings, 
				net.minecraftforge.client.ForgeHooksClient.getDefaultWorldPreset(), 
				OptionalLong.empty()
			),
			dimGenSettings
		);
	}

	public ModpackCreateWorldScreen(@Nullable Screen screen, DataPackConfig datapackCodec, WorldGenSettingsComponent worldGenSettingsComponent, WorldGenSettings dimGenSettings)
	{
		super(screen, datapackCodec, worldGenSettingsComponent);
		this.initName = I18n.get("selectWorld.newWorld");

		// Does the same as opening the customisation/dimensions menu and applying, registering the dimensions from the modpack config.
		Optional<WorldPreset> preset = Optional.of(OTGGui.OTG_WORLD_TYPE);
		WorldPreset.PresetEditor biomegeneratortypescreens$ifactory = WorldPreset.EDITORS.get(preset);
		biomegeneratortypescreens$ifactory = net.minecraftforge.client.ForgeHooksClient.getPresetEditor(preset, biomegeneratortypescreens$ifactory);
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
		this.nameEdit = new EditBox(this.font, i, 60, 150, 20, new TranslatableComponent("selectWorld.enterName"))
		{
			protected MutableComponent createNarrationMessage()
			{
				return CommonComponents.joinForNarration(super.createNarrationMessage(), new TranslatableComponent("selectWorld.resultFolder")).append(" ").append(ModpackCreateWorldScreen.this.resultFolder);
			}
		};
		this.nameEdit.setValue(this.initName);
		this.nameEdit.setResponder(
			(p_100932_) -> {
				this.initName = p_100932_;
				this.createButton.active = !this.nameEdit.getValue().isEmpty();
				this.updateResultFolder();
			}
		);
		this.addWidget(this.nameEdit);
	      
		this.seedEdit = new EditBox(this.font, j, 60, 150, 20, new TranslatableComponent("selectWorld.enterName"))
		{
			protected MutableComponent createNarrationMessage()
			{
				return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.resultFolder")).append(" ").append(ModpackCreateWorldScreen.this.resultFolder);
			}
		};		
		this.seedEdit.setValue(this.initSeed);
		this.seedEdit.setResponder((p_214319_1_) ->
		{
			this.initSeed = p_214319_1_;
		});
		this.addWidget(this.seedEdit);
		
		this.addRenderableWidget(
			new Button(i, 110, 150, 20, TextComponent.EMPTY, 
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
				
					//p_214316_1_.queueNarration(250);
				}
			) {
				public Component getMessage()
				{
					return new TranslatableComponent("options.generic_value", ModpackCreateWorldScreen.GAME_MODEL_LABEL, new TranslatableComponent("selectWorld.gameMode." + ModpackCreateWorldScreen.this.gameMode.name));
				}
			
				protected MutableComponent createNarrationMessage()
				{
					return super.createNarrationMessage().append(". ").append(ModpackCreateWorldScreen.this.gameModeHelp1).append(" ").append(ModpackCreateWorldScreen.this.gameModeHelp2);
				}
			}
		);
		this.difficultyButton = this.addRenderableWidget(CycleButton.builder(Difficulty::getDisplayName).withValues(Difficulty.values()).withInitialValue((this.gameMode == GameMode.HARDCORE ? Difficulty.HARD : this.difficulty)).create(j, 110, 150, 20, new TranslatableComponent("options.difficulty"), (p_170162_, p_170163_) -> {
			this.difficulty = p_170163_;
		}));
		this.commandsButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.commands && !this.hardCore).withCustomNarration((p_170160_) -> {
			return CommonComponents.joinForNarration(p_170160_.createDefaultNarrationMessage(), new TranslatableComponent("selectWorld.allowCommands.info"));
		}).create(i, 161, 150, 20, new TranslatableComponent("selectWorld.allowCommands"), (p_170168_, p_170169_) -> {
			this.commandsChanged = true;
			this.commands = p_170169_;
		}));		
		this.addRenderableWidget(new Button(j, 161, 150, 20, new TextComponent("Dimensions"), // TODO: Use translationtext
			(p_214322_1_) -> {
				Optional<WorldPreset> preset = Optional.of(OTGGui.OTG_WORLD_TYPE);
				WorldPreset.PresetEditor biomegeneratortypescreens$ifactory = WorldPreset.EDITORS.get(preset);
				biomegeneratortypescreens$ifactory = net.minecraftforge.client.ForgeHooksClient.getPresetEditor(preset, biomegeneratortypescreens$ifactory);
				if (biomegeneratortypescreens$ifactory != null)
				{
					this.minecraft.setScreen(biomegeneratortypescreens$ifactory.createEditScreen(this, this.worldGenSettingsComponent.makeSettings(this.hardCore)));
				}
			}) {
				public Component getMessage()
				{
					// TODO: Add translations/narration
					//return DialogTexts.optionStatus(super.getMessage(), ModpackCreateWorldScreen.this.commands && !ModpackCreateWorldScreen.this.hardCore);
					return new TextComponent("Dimensions");
				}

				protected MutableComponent createNarrationMessage()
				{
					// TODO: Add translations/narration
					//return super.createNarrationMessage().append(". ").append(new TranslationTextComponent("selectWorld.allowCommands.info"));
					return super.createNarrationMessage().append(". ").append(new TextComponent("Dimensions"));
				}
			}
		);
		this.createButton = this.addRenderableWidget(
			new Button(i, this.height - 28, 150, 20, new TranslatableComponent("selectWorld.create"),
				(p_214318_1_) -> {
					this.onCreate();
				}
			)
		);
		this.createButton.active = !this.initName.isEmpty();
		this.addRenderableWidget(
			new Button(j, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, 
				(p_214317_1_) -> {
					this.popScreen();
				}
			)
		);

		this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
		
		this.setInitialFocus(this.nameEdit);
		this.setGameMode(this.gameMode);
		this.updateResultFolder();

		DimensionConfig modPackConfig = DimensionConfig.fromDisk(Constants.MODPACK_CONFIG_NAME);
		if(modPackConfig == null || modPackConfig.ModpackName == null)
		{
			this.title2 = new TranslatableComponent("otg.createDimensions.customize.title");
		} else {
			this.title2 = new TextComponent(modPackConfig.ModpackName);
		}
	}

	private void updateGameModeHelp()
	{
		this.gameModeHelp1 = new TranslatableComponent("selectWorld.gameMode." + this.gameMode.name + ".line1");
		this.gameModeHelp2 = new TranslatableComponent("selectWorld.gameMode." + this.gameMode.name + ".line2");
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
		this.minecraft.forceSetScreen(new GenericDirtMessageScreen(new TranslatableComponent("createWorld.preparing")));
		if (this.copyTempDataPackDirToNewWorld())
		{
			this.cleanupTempResources();
			LevelSettings worldsettings = new LevelSettings(this.nameEdit.getValue().trim(), this.gameMode.gameType, this.hardCore, (this.gameMode == GameMode.HARDCORE ? Difficulty.HARD : this.difficulty), this.commands && !this.hardCore, this.gameRules, this.dataPacks);
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
			this.commandsButton.setValue(this.commands);			
		}

		if (p_228200_1_ == ModpackCreateWorldScreen.GameMode.HARDCORE)
		{
			this.hardCore = true;
			this.commandsButton.active = false;
			this.commandsButton.setValue(false);
			this.worldGenSettingsComponent.switchToHardcore();
			this.difficultyButton.setValue(Difficulty.HARD);
			this.difficultyButton.active = false;
		} else {
			this.hardCore = false;
			this.commandsButton.active = true;
			this.commandsButton.setValue(this.commands);
			this.worldGenSettingsComponent.switchOutOfHardcode();
			this.difficultyButton.setValue(this.difficulty);
			this.difficultyButton.active = true;
		}

		this.gameMode = p_228200_1_;
		this.updateGameModeHelp();
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
	public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		this.renderBackground(p_230430_1_);
		drawCenteredString(p_230430_1_, this.font, this.title2, this.width / 2, 20, -1);
		drawString(p_230430_1_, this.font, NAME_LABEL, this.width / 2 - 150, 47, -6250336);
		drawString(p_230430_1_, this.font, (new TextComponent("")).append(OUTPUT_DIR_INFO).append(" ").append(this.resultFolder), this.width / 2 - 150, 85, -6250336);
		drawString(p_230430_1_, this.font, "Seed", this.width / 2 + 10, 47, -6250336);
		this.nameEdit.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		this.seedEdit.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		drawString(p_230430_1_, this.font, this.gameModeHelp1, this.width / 2 - 150, 132, -6250336);
		drawString(p_230430_1_, this.font, this.gameModeHelp2, this.width / 2 - 150, 144, -6250336);
		if (this.commandsButton.visible)
		{
			drawString(p_230430_1_, this.font, COMMANDS_INFO, this.width / 2 - 150, 182, -6250336);
		}

		for(Widget widget : this.renderables)
		{
			widget.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		}
	}

	@Override
	protected <T extends GuiEventListener & NarratableEntry> T addWidget(T p_100948_)
	{
		return super.addWidget(p_100948_);
	}
	
	@Override
	protected <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T p_170199_)
	{
		return super.addRenderableWidget(p_170199_);
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
			try {
				Stream<Path> stream = Files.walk(this.tempDataPackDir);
				
				try {
					stream.sorted(Comparator.reverseOrder()).forEach((p_170192_) -> {
						try {
							Files.delete(p_170192_);
						} catch (IOException ioexception1) {
							LOGGER.warn("Failed to remove temporary file {}", p_170192_, ioexception1);
						}		
					});
				} catch (Throwable throwable1) {
					if (stream != null)
					{
						try {
							stream.close();
						} catch (Throwable throwable) {
							throwable1.addSuppressed(throwable);
						}
					}
					
					throw throwable1;
				}
				
				if (stream != null)
				{
					stream.close();
				}
			} catch (IOException ioexception) {
				LOGGER.warn("Failed to list temporary dir {}", (Object)this.tempDataPackDir);
			}
			
			this.tempDataPackDir = null;
		}
	}
	
	private static void copyBetweenDirs(Path p_100913_, Path p_100914_, Path p_100915_)
	{
		try {
			Util.copyBetweenDirs(p_100913_, p_100914_, p_100915_);
		} catch (IOException ioexception) {
			LOGGER.warn("Failed to copy datapack file from {} to {}", p_100915_, p_100914_);
			throw new ModpackCreateWorldScreen.OperationFailedException(ioexception);
		}
	}
	
	private boolean copyTempDataPackDirToNewWorld()
	{
		if (this.tempDataPackDir != null)
		{
			try {
				LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().createAccess(this.resultFolder);
				
				try {
					Stream<Path> stream = Files.walk(this.tempDataPackDir);
					
					try {
						Path path = levelstoragesource$levelstorageaccess.getLevelPath(LevelResource.DATAPACK_DIR);
						Files.createDirectories(path);
						stream.filter((p_170174_) -> {
							return !p_170174_.equals(this.tempDataPackDir);
						}).forEach((p_170195_) -> {
							copyBetweenDirs(this.tempDataPackDir, path, p_170195_);
						});
					} catch (Throwable throwable2) {
						if (stream != null)
						{
							try {
								stream.close();
							} catch (Throwable throwable1) {
								throwable2.addSuppressed(throwable1);
							}
						}		
						throw throwable2;
					}
					
					if (stream != null)
					{
						stream.close();
					}
				} catch (Throwable throwable3) {
					if (levelstoragesource$levelstorageaccess != null)
					{
						try {
							levelstoragesource$levelstorageaccess.close();
						} catch (Throwable throwable) {
							throwable3.addSuppressed(throwable);
						}
					}		
					throw throwable3;
				}
				
				if (levelstoragesource$levelstorageaccess != null)
				{
					levelstoragesource$levelstorageaccess.close();
				}
			} catch (ModpackCreateWorldScreen.OperationFailedException | IOException ioexception) {
				LOGGER.warn("Failed to copy datapacks to world {}", this.resultFolder, ioexception);
				SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
				this.popScreen();
				return false;
			}
		}	
		return true;
	}

	@Nullable
	public static Path createTempDataPackDirFromExistingWorld(Path p_100907_, Minecraft p_100908_)
	{
		MutableObject<Path> mutableobject = new MutableObject<>();
		
		try {
			Stream<Path> stream = Files.walk(p_100907_);
			
			try {
				stream.filter((p_170177_) -> {
					return !p_170177_.equals(p_100907_);
				}).forEach((p_170186_) -> {
				Path path = mutableobject.getValue();
				if (path == null)
				{
					try {
						path = Files.createTempDirectory("mcworld-");
					} catch (IOException ioexception1) {
						LOGGER.warn("Failed to create temporary dir");
						throw new ModpackCreateWorldScreen.OperationFailedException(ioexception1);
					}
					mutableobject.setValue(path);
				}
				copyBetweenDirs(p_100907_, path, p_170186_);
				});
			} catch (Throwable throwable1) {
				if (stream != null)
				{
					try {
						stream.close();
					} catch (Throwable throwable) {
						throwable1.addSuppressed(throwable);
					}
				}
				
				throw throwable1;
			}
			
			if (stream != null)
			{
				stream.close();
			}
		} catch (ModpackCreateWorldScreen.OperationFailedException | IOException ioexception) {
			LOGGER.warn("Failed to copy datapacks from world {}", p_100907_, ioexception);
			SystemToast.onPackCopyFailure(p_100908_, p_100907_.toString());
			return null;
		}
		
		return mutableobject.getValue();
	}

	@SuppressWarnings("serial")
	@OnlyIn(Dist.CLIENT)
	static class OperationFailedException extends RuntimeException
	{
		public OperationFailedException(Throwable p_101023_)
		{
			super(p_101023_);
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
