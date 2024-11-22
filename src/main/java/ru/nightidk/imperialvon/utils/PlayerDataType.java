package ru.nightidk.imperialvon.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.level.GameType;

@AllArgsConstructor
@Getter
public class PlayerDataType {
    protected Location location;
    protected GameType gameType;
}
