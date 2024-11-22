package ru.nightidk.imperialvon.utils;

import lombok.Getter;
import net.minecraft.network.chat.Style;

@Getter
public class StyledPair {
    protected String string;
    protected Style style;

    public StyledPair(String string, Style style) {
        this.string = string;
        this.style = style;
    }
}
