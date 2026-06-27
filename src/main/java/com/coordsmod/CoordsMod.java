package com.coordsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CoordsMod implements ClientModInitializer {

    public static KeyBinding coordsKey;

    // For TOGGLE edge-detection: was the key physically down last tick?
    private static boolean prevKeyDown = false;

    @Override
    public void onInitializeClient() {
        CoordsConfig.load();

        // Register so it appears in Options → Controls → CoordsDisplay.
        // Default: TAB (GLFW_KEY_TAB = 258).
        coordsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.coordsmod.show_coords",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,
                KeyBinding.Category.create(Identifier.of("coordsmod", "coordsmod"))
        ));

        HudRenderCallback.EVENT.register(CoordsHud::render);

        // TOGGLE mode: detect rising edge using raw GLFW so Tab works even
        // though the player-list overlay also consumes the Tab KeyBinding.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (cfg().displayMode != CoordsConfig.DisplayMode.TOGGLE) return;

            boolean keyDown = isKeyDown(client);
            if (keyDown && !prevKeyDown) {
                cfg().toggleVisible = !cfg().toggleVisible;
                CoordsConfig.save();
            }
            prevKeyDown = keyDown;
        });
    }

    public static boolean isKeyDown(MinecraftClient client) {
        long handle = client.getWindow().getHandle();

        // getBoundKeyTranslationKey() returns e.g. "key.keyboard.tab" or
        // "key.mouse.left" — parse category from the prefix, code from the key.
        InputUtil.Key key = InputUtil.fromTranslationKey(
                coordsKey.getBoundKeyTranslationKey());

        return switch (key.getCategory()) {
            case KEYSYM -> GLFW.glfwGetKey(handle, key.getCode()) == GLFW.GLFW_PRESS;
            case MOUSE  -> GLFW.glfwGetMouseButton(handle, key.getCode()) == GLFW.GLFW_PRESS;
            default     -> false;
        };
    }

    static CoordsConfig.Data cfg() { return CoordsConfig.get(); }
}
