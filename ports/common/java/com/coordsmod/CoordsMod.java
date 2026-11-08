package com.coordsmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;


public class CoordsMod implements ClientModInitializer {

    public static KeyMapping coordsKey;

    // For TOGGLE edge-detection: was the key physically down last tick?
    private static boolean prevKeyDown = false;

    @Override
    public void onInitializeClient() {
        CoordsConfig.load();

        // Register so it appears in Options → Controls → CoordsDisplay.
        // Default: Tab.
        coordsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.coordsmod.show_coords",
                InputState.KEYBOARD,
                InputConstants.KEY_TAB,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("coordsmod", "coordsmod"))
        ));

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("coordsmod", "coordinates"), CoordsHud::extractRenderState);

        // TOGGLE mode: detect rising edge using physical input so Tab works even
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

    public static boolean isKeyDown(Minecraft client) {
        return InputState.isDown(client, coordsKey);
    }

    static CoordsConfig.Data cfg() { return CoordsConfig.get(); }
}
