package com.coordsmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import org.lwjgl.sdl.SDLMouse;

/** Physical input polling keeps Tab working alongside the player list. */
public final class InputState {
    private InputState() {}
    public static final InputConstants.Type KEYBOARD = InputConstants.Type.KEYBOARD;

    public static boolean isDown(Minecraft client, KeyMapping mapping) {
        InputConstants.Key key = InputConstants.getKey(mapping.saveString());
        return switch (key.getType()) {
            case KEYBOARD -> InputConstants.isKeyDown(key.getValue());
            case MOUSE -> key.getValue() > 0 && key.getValue() <= 32 &&
                    (SDLMouse.nSDL_GetMouseState(0L, 0L) & (1 << (key.getValue() - 1))) != 0;
            default -> false;
        };
    }
}
