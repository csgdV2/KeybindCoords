package com.coordsmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class CoordsHud {

    private static final int LINE_H  = 10;
    private static final int GAP     = 8;
    private static final int MARGIN  = 2;

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.getDebugHud().shouldShowDebugHud())  return;

        CoordsConfig.Data cfg = CoordsConfig.get();

        if (!cfg.enabled) return;

        boolean visible = (cfg.displayMode == CoordsConfig.DisplayMode.HOLD)
                ? CoordsMod.isKeyDown(mc)
                : cfg.toggleVisible;
        if (!visible) return;

        // Coords
        int x = (int) Math.floor(mc.player.getX());
        int y = (int) Math.floor(mc.player.getY());
        int z = (int) Math.floor(mc.player.getZ());

        // Strings to display
        boolean labels = cfg.labelStyle == CoordsConfig.LabelStyle.XYZ;
        String px = labels ? "X: " + x : String.valueOf(x);
        String py = labels ? "Y: " + y : String.valueOf(y);
        String pz = labels ? "Z: " + z : String.valueOf(z);

        // Measure total block size for anchor math
        int blockW, blockH;
        if (cfg.orientation == CoordsConfig.Orientation.HORIZONTAL) {
            blockW = mc.textRenderer.getWidth(px) + GAP
                   + mc.textRenderer.getWidth(py) + GAP
                   + mc.textRenderer.getWidth(pz);
            blockH = LINE_H;
        } else {
            blockW = Math.max(mc.textRenderer.getWidth(px),
                     Math.max(mc.textRenderer.getWidth(py),
                              mc.textRenderer.getWidth(pz)));
            blockH = LINE_H * 3;
        }

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int drawX = switch (cfg.positionPreset) {
            case TOP_RIGHT, BOTTOM_RIGHT -> sw - blockW - MARGIN;
            case CUSTOM                  -> cfg.customX;
            default                      -> MARGIN;
        };
        int drawY = switch (cfg.positionPreset) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> sh - blockH - MARGIN;
            case CUSTOM                    -> cfg.customY;
            default                        -> MARGIN;
        };

        // Draw
        if (cfg.orientation == CoordsConfig.Orientation.HORIZONTAL) {
            drawHorizontal(ctx, mc, px, py, pz, drawX, drawY, cfg);
        } else {
            drawVertical(ctx, mc, px, py, pz, drawX, drawY, cfg);
        }
    }

    private static void drawHorizontal(DrawContext ctx, MinecraftClient mc,
                                       String px, String py, String pz,
                                       int dx, int dy, CoordsConfig.Data cfg) {
        dx = drawColored(ctx, mc, px, dx, dy, cfg.colorX, cfg.labelStyle);
        dx += GAP;
        dx = drawColored(ctx, mc, py, dx, dy, cfg.colorY, cfg.labelStyle);
        dx += GAP;
        drawColored(ctx, mc, pz, dx, dy, cfg.colorZ, cfg.labelStyle);
    }

    private static void drawVertical(DrawContext ctx, MinecraftClient mc,
                                     String px, String py, String pz,
                                     int dx, int dy, CoordsConfig.Data cfg) {
        drawColored(ctx, mc, px, dx, dy,              cfg.colorX, cfg.labelStyle);
        drawColored(ctx, mc, py, dx, dy + LINE_H,     cfg.colorY, cfg.labelStyle);
        drawColored(ctx, mc, pz, dx, dy + LINE_H * 2, cfg.colorZ, cfg.labelStyle);
    }

    private static int drawColored(DrawContext ctx, MinecraftClient mc,
                                   String text, int x, int y,
                                   int axisColor, CoordsConfig.LabelStyle style) {
        if (style == CoordsConfig.LabelStyle.XYZ) {
            // Split "X: -312" → label "X: " in white, value "-312" in axisColor
            int colonIdx = text.indexOf(": ");
            if (colonIdx >= 0) {
                String label = text.substring(0, colonIdx + 2);   // "X: "
                String value = text.substring(colonIdx + 2);       // "-312"
                ctx.drawText(mc.textRenderer, label, x, y, 0xFFFFFFFF, true);
                int lw = mc.textRenderer.getWidth(label);
                ctx.drawText(mc.textRenderer, value, x + lw, y, axisColor, true);
                return x + lw + mc.textRenderer.getWidth(value);
            }
        }
        // NONE mode — full string in axis color
        ctx.drawText(mc.textRenderer, text, x, y, axisColor, true);
        return x + mc.textRenderer.getWidth(text);
    }
}
