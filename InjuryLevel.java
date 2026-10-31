package com.ben.bodyinjury.capability;

import net.minecraft.ChatFormatting;

public enum InjuryLevel {
    GREEN(0, "Ổn", ChatFormatting.GREEN),
    YELLOW(1, "Nhẹ", ChatFormatting.YELLOW),
    ORANGE(2, "Chú ý", ChatFormatting.GOLD),
    RED(3, "Cần băng", ChatFormatting.RED),
    CRITICAL(4, "Nguy kịch", ChatFormatting.DARK_PURPLE);

    private final int value;
    private final String display;
    private final ChatFormatting color;

    InjuryLevel(int value, String display, ChatFormatting color) {
        this.value = value;
        this.display = display;
        this.color = color;
    }

    public int getValue() { return value; }
    public String getDisplay() { return display; }
    public ChatFormatting getColor() { return color; }

    public static InjuryLevel from(int v) {
        v = Math.max(0, Math.min(4, v));
        return values()[v];
    }
}
