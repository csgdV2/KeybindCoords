package com.coordsmod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CoordsHud {

    private static final int LINE_H  = 10;
    private static final int GAP     = 8;
    private static final int MARGIN  = 2;

    public static void extractRenderState(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.getDebugOverlay().showDebugScreen())  return;

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
            blockW = mc.font.width(px) + GAP
                   + mc.font.width(py) + GAP
                   + mc.font.width(pz);
            blockH = LINE_H;
        } else {
            blockW = Math.max(mc.font.width(px),
                     Math.max(mc.font.width(py),
                              mc.font.width(pz)));
            blockH = LINE_H * 3;
        }

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

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

    private static void drawHorizontal(GuiGraphicsExtractor ctx, Minecraft mc,
                                       String px, String py, String pz,
                                       int dx, int dy, CoordsConfig.Data cfg) {
        dx = drawColored(ctx, mc, px, dx, dy, cfg.colorX, cfg.labelStyle);
        dx += GAP;
        dx = drawColored(ctx, mc, py, dx, dy, cfg.colorY, cfg.labelStyle);
        dx += GAP;
        drawColored(ctx, mc, pz, dx, dy, cfg.colorZ, cfg.labelStyle);
    }

    private static void drawVertical(GuiGraphicsExtractor ctx, Minecraft mc,
                                     String px, String py, String pz,
                                     int dx, int dy, CoordsConfig.Data cfg) {
        drawColored(ctx, mc, px, dx, dy,              cfg.colorX, cfg.labelStyle);
        drawColored(ctx, mc, py, dx, dy + LINE_H,     cfg.colorY, cfg.labelStyle);
        drawColored(ctx, mc, pz, dx, dy + LINE_H * 2, cfg.colorZ, cfg.labelStyle);
    }

    private static int drawColored(GuiGraphicsExtractor ctx, Minecraft mc,
                                   String text, int x, int y,
                                   int axisColor, CoordsConfig.LabelStyle style) {
        if (style == CoordsConfig.LabelStyle.XYZ) {
            // Split "X: -312" → label "X: " in white, value "-312" in axisColor
            int colonIdx = text.indexOf(": ");
            if (colonIdx >= 0) {
                String label = text.substring(0, colonIdx + 2);   // "X: "
                String value = text.substring(colonIdx + 2);       // "-312"
                ctx.text(mc.font, label, x, y, 0xFFFFFFFF, true);
                int lw = mc.font.width(label);
                ctx.text(mc.font, value, x + lw, y, axisColor, true);
                return x + lw + mc.font.width(value);
            }
        }
        // NONE mode — full string in axis color
        ctx.text(mc.font, text, x, y, axisColor, true);
        return x + mc.font.width(text);
    }
}
