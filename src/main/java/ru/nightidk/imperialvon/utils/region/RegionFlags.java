package ru.nightidk.imperialvon.utils.region;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class RegionFlags {
    @Getter
    public enum FlagsEnum {
        PVP("pvp"),
        BLOCK_BREAK("block_break"),
        CHEST_OPEN("chest_open");

        private final String flagName;

        FlagsEnum(String flagName) {
            this.flagName = flagName;
        }

        public static FlagsEnum fromString(String flagName) {
            for (FlagsEnum flag : values()) {
                if (flag.getFlagName().equalsIgnoreCase(flagName)) {
                    return flag;
                }
            }
            throw new IllegalArgumentException("Неизвестный флаг: " + flagName);
        }

        public static List<String> getAllFlagNames() {
            return Arrays.stream(values()).map(FlagsEnum::getFlagName).toList();
        }
    }

    private boolean pvpEnabled = false;
    private boolean blockBreakingEnabled = false;
    private boolean chestOpeningEnabled = false;

    public void setFlagFromEnum(FlagsEnum flag, boolean value) {
        switch (flag) {
            case PVP -> setPvpEnabled(value);
            case BLOCK_BREAK -> setBlockBreakingEnabled(value);
            case CHEST_OPEN -> setChestOpeningEnabled(value);
        }
    }
}
