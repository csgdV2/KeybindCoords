package com.coordsmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CoordsConfig implements ModMenuApi {

    public enum DisplayMode {
        HOLD,
        TOGGLE;
        @Override public String toString() {
            return switch (this) {
                case HOLD   -> "Hold";
                case TOGGLE -> "Toggle";
            };
        }
    }

    public enum LabelStyle {
        NONE,
        XYZ;
        @Override public String toString() {
            return switch (this) {
                case NONE -> "Numbers only";
                case XYZ  -> "Show X / Y / Z labels";
            };
        }
    }

    public enum Orientation {
        HORIZONTAL,
        VERTICAL;
        @Override public String toString() {
            return switch (this) {
                case HORIZONTAL -> "Horizontal";
                case VERTICAL   -> "Vertical";
            };
        }
    }

    public enum PositionPreset {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CUSTOM;
        @Override public String toString() {
            return switch (this) {
                case TOP_LEFT     -> "Top Left";
                case TOP_RIGHT    -> "Top Right";
                case BOTTOM_LEFT  -> "Bottom Left";
                case BOTTOM_RIGHT -> "Bottom Right";
                case CUSTOM       -> "Custom";
            };
        }
    }


    public static class Data {

        public boolean     enabled       = true;
        public DisplayMode displayMode   = DisplayMode.HOLD;
        public boolean     toggleVisible = false;   // runtime state for TOGGLE mode

        public LabelStyle  labelStyle    = LabelStyle.NONE;
        public Orientation orientation   = Orientation.HORIZONTAL;

        public int colorX = 0xFFFFFFFF;
        public int colorY = 0xFFFFFFFF;
        public int colorZ = 0xFFFFFFFF;

        public PositionPreset positionPreset = PositionPreset.TOP_LEFT;
        public int customX = 2;
        public int customY = 2;
    }

    private static final Gson GSON        = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "coordsmod.json");
    private static       Data instance    = new Data();

    public static Data get()  { return instance; }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(r, Data.class);
                if (instance == null) instance = new Data();
            } catch (IOException e) {
                instance = new Data();
            }
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(instance, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CoordsConfig::buildScreen;
    }


    public static Screen buildScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("CoordsDisplay"))
                .setSavingRunnable(CoordsConfig::save);

        ConfigEntryBuilder eb  = builder.entryBuilder();
        Data               cfg = instance;

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

        general.addEntry(eb.startBooleanToggle(Text.literal("Enabled"), cfg.enabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enable or disable the coordinates HUD entirely."))
                .setSaveConsumer(v -> cfg.enabled = v)
                .build());

        general.addEntry(eb.startEnumSelector(Text.literal("Keybind Mode"), DisplayMode.class, cfg.displayMode)
                .setDefaultValue(DisplayMode.HOLD)
                .setTooltip(
                        Text.literal("Hold  — show coords only while the key is held down."),
                        Text.literal("Toggle — press once to show, press again to hide."))
                .setSaveConsumer(v -> cfg.displayMode = v)
                .build());

        general.addEntry(eb.startTextDescription(
                        Text.literal("§7Change the keybind under  Options → Controls → CoordsDisplay"))
                .build());

        ConfigCategory display = builder.getOrCreateCategory(Text.literal("Display"));

        display.addEntry(eb.startEnumSelector(Text.literal("Labels"), LabelStyle.class, cfg.labelStyle)
                .setDefaultValue(LabelStyle.NONE)
                .setTooltip(
                        Text.literal("Numbers only — Shows only the numbers."),
                        Text.literal("Show X/Y/Z - Adds prefix before the numbers respectively."))
                .setSaveConsumer(v -> cfg.labelStyle = v)
                .build());

        display.addEntry(eb.startEnumSelector(Text.literal("Orientation"), Orientation.class, cfg.orientation)
                .setDefaultValue(Orientation.HORIZONTAL)
                .setTooltip(
                        Text.literal("Horizontal — all values on one line."),
                        Text.literal("Vertical   — each value on its own line."))
                .setSaveConsumer(v -> cfg.orientation = v)
                .build());

        ConfigCategory colors = builder.getOrCreateCategory(Text.literal("Colors"));

        colors.addEntry(eb.startColorField(Text.literal("X Color"), cfg.colorX & 0x00FFFFFF)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the X coordinate value."))
                .setSaveConsumer(v -> cfg.colorX = 0xFF000000 | v)
                .build());

        colors.addEntry(eb.startColorField(Text.literal("Y Color"), cfg.colorY & 0x00FFFFFF)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the Y coordinate value."))
                .setSaveConsumer(v -> cfg.colorY = 0xFF000000 | v)
                .build());

        colors.addEntry(eb.startColorField(Text.literal("Z Color"), cfg.colorZ & 0x00FFFFFF)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the Z coordinate value."))
                .setSaveConsumer(v -> cfg.colorZ = 0xFF000000 | v)
                .build());

        ConfigCategory position = builder.getOrCreateCategory(Text.literal("Position"));

        position.addEntry(eb.startEnumSelector(Text.literal("Preset"), PositionPreset.class, cfg.positionPreset)
                .setDefaultValue(PositionPreset.TOP_LEFT)
                .setTooltip(
                        Text.literal("Choose a corner, or pick Custom to set exact pixel coordinates below."))
                .setSaveConsumer(v -> cfg.positionPreset = v)
                .build());

        position.addEntry(eb.startIntField(Text.literal("Custom X"), cfg.customX)
                .setDefaultValue(2)
                .setMin(0).setMax(1920)
                .setTooltip(Text.literal("Horizontal pixel offset from the left edge. Only used when Preset = Custom."))
                .setSaveConsumer(v -> cfg.customX = v)
                .build());

        position.addEntry(eb.startIntField(Text.literal("Custom Y"), cfg.customY)
                .setDefaultValue(2)
                .setMin(0).setMax(1080)
                .setTooltip(Text.literal("Vertical pixel offset from the top edge. Only used when Preset = Custom."))
                .setSaveConsumer(v -> cfg.customY = v)
                .build());

        return builder.build();
    }
}
