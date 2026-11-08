package com.coordsmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Physical input polling keeps Tab working alongside the player list. */
public final class InputState {
    private InputState() {}
    public static final InputConstants.Type KEYBOARD = InputConstants.Type.KEYSYM;

    public static boolean isDown(Minecraft client, KeyMapping mapping) {
        InputConstants.Key key = InputConstants.getKey(mapping.saveString());
        return switch (key.getType()) {
            case KEYSYM -> InputConstants.isKeyDown(client.getWindow(), key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(client.getWindow().handle(), key.getValue()) == GLFW.GLFW_PRESS;
            default -> false;
        };
    }
}
