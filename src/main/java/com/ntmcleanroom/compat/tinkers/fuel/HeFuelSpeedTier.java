package com.ntmcleanroom.compat.tinkers.fuel;

/** Speed tiers cycled by right-clicking the HE Smeltery Fueler with a screwdriver. */
public enum HeFuelSpeedTier {

    NORMAL("Normal", 1.0F),
    OVERCLOCKED("Overclocked", 1.75F),
    MAX("Max Output", 2.5F);

    public static final HeFuelSpeedTier[] VALUES = values();

    public final String label;
    /** Multiplier applied to fuel fluid output (and, proportionally, HE draw) per tick. */
    public final float fuelMultiplier;

    HeFuelSpeedTier(String label, float fuelMultiplier) {
        this.label = label;
        this.fuelMultiplier = fuelMultiplier;
    }
}
