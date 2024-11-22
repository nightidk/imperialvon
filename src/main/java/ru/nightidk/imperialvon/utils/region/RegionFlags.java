package ru.nightidk.imperialvon.utils.region;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegionFlags {
    private boolean pvpEnabled = false;
    private boolean blockBreakingEnabled = false;
    private boolean chestOpeningEnabled = false;

    public RegionFlags() {}
}
