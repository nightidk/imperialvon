package ru.nightidk.imperialvon.utils;

import lombok.Getter;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

@Getter
public enum TextStyleUtil {
    DARK_AQUA (Style.EMPTY.withColor(TextColor.fromRgb(0x3f9efc))),
    WHITE (Style.EMPTY.withColor(TextColor.fromRgb(0xffffff))),
    RED (Style.EMPTY.withColor(TextColor.fromRgb(0xff0000))),
    YELLOW (Style.EMPTY.withColor(TextColor.fromRgb(0xffff00))),
    GREEN (Style.EMPTY.withColor(TextColor.fromRgb(0x00ff00)));


    private final Style style;

    TextStyleUtil(Style style) {
        this.style = style;
    }

    @Override
    public String toString() {
        return "Style{" +
                "style='" + style.toString() + '\'' +
                '}';
    }
}